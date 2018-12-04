package com.dutiantech.service;


import java.util.List;

import com.dutiantech.model.AutoLoan;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.Funds;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class AutoLoanService extends BaseService {
	
	@SuppressWarnings("unused")
	private static final String basic_selectFields = " userCode,isAvailable,autoCount,autoAmount,autoOnceAmount,autoIntMin,autoIntMax,autoLimMin,autoLimMax,autoRefundType,autoCreditMin,autoCreditMax,autoKeepBalance,lastDate ";

	/**
	 * 验证是否存在
	 * @param userCode
	 * @param autoState
	 * @return true 已存在
	 */
	public boolean isExist(String userCode, String autoState){
		long result = Db.queryLong("select count(userCode) from t_auto_loan_v2 where userCode = ? and autoState = ?",userCode,autoState);
		if(result>0)
			return true;
		return false;
	}
	
	public AutoLoan findById(String userCode){
		return AutoLoan.autoLoanDao.findById(userCode);
	}
	
	/**
	 * 自动投标一次时，更新时间和投标次数
	 * @param userCode
	 * @return
	 */
	public boolean updateOnce(String userCode){
		int zhazha = Db.update("update t_auto_loan set autoCount = autoCount + 1,lastDate=?",DateUtil.getNowDateTime());
		if(zhazha>0)
			return true;
		return false;
	}
	
	/**
	 * * 分页查询自动投标信息
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @return
	 */
	@Deprecated
	public Page<AutoLoan> findByPage(Integer pageNumber, Integer pageSize ,
			Integer minLoanAmount , Integer maxLoanAmount , String refundType ,
			Integer loanTimeLimit , Integer rateValue){
		String sqlSelect = "select a.*";
		String sqlFrom = "from t_auto_loan a,t_funds f ";
		String sqlWhere = "where a.isAvailable=1 ";
		sqlWhere += "and a.autoOnceAmount >= ? and a.autoOnceAmount <= ? ";
		sqlWhere += "and (a.autoRefundType = ? or a.autoRefundType = 'D' )";	//兼容D的情况
//		sqlWhere += "and a.autoIntMin <= ? and a.autoIntMax >= ? ";
		sqlWhere += "and a.autoLimMin <= ? and a.autoLimMax >= ?  ";
		sqlWhere += "and f.avBalance > 5000 and f.userCode = a.userCode "; 
		//sqlWhere += "f.avBalance >= 5000 and f.userCode = a.userCode ";
		String sqlOrder = " order by a.lastDate asc";
		return AutoLoan.autoLoanDao.paginate(pageNumber, pageSize, 
				sqlSelect, sqlFrom+sqlWhere.toString()+sqlOrder ,
				minLoanAmount , maxLoanAmount , refundType ,// rateValue ,rateValue ,
				loanTimeLimit ,loanTimeLimit );
	}
	
	
	/**
	 * 保存或修改自动投标信息
	 * @param autoLoan
	 * @return
	 */
	@Deprecated
	public boolean saveOrUpdateAutoLoan(AutoLoan autoLoan){
		
		String userCode = autoLoan.getStr("userCode");
		AutoLoan selectAutoLoan = AutoLoan.autoLoanDao.findById(userCode);
		if(null == selectAutoLoan){
			AutoLoan inintAutoLoan = inintAutoLoan(userCode);
			inintAutoLoan.save();
		}
		autoLoan.remove("lastDate");
		boolean updateResult = autoLoan.update();
		
		return updateResult;
	}
	
	
	/**
	 * 设置自动投标状态(启用 停用)
	 * @param userCode
	 * @param isAvailable
	 * @return
	 */
	@Deprecated
	public boolean setAutoLoanState(String userCode , Integer isAvailable){
		AutoLoan autoLoan = AutoLoan.autoLoanDao.findById(userCode);
		boolean result = false;
		if(null == autoLoan){
			//初始化新增
			AutoLoan inintAutoLoan = inintAutoLoan(userCode);
			inintAutoLoan.set("isAvailable", isAvailable);
			result = inintAutoLoan.save();
		}else{
			autoLoan.set("isAvailable", isAvailable);
			autoLoan.set("lastDate", DateUtil.getNowDateTime());
			result = autoLoan.update();
		}
		
		return result;
	}
	
	
	/**
	 * 查询排名
	 * @param userCode
	 */
	@Deprecated
	public Long getAutoIndex(String userCode){
		String sql = "select count(1) from t_auto_loan WHERE lastDate <= (select lastDate from t_auto_loan where userCode=?)";
		return Db.queryLong(sql, userCode );
	}
	
	
	
	/**
	 * 初始化自动投标信息
	 * @param userCode
	 * @return
	 */
	@Deprecated
	private AutoLoan inintAutoLoan(String userCode){
		AutoLoan autoLoan = new AutoLoan();
		autoLoan.set("userCode", userCode);
		autoLoan.set("isAvailable", 0);
		autoLoan.set("autoCount", 0);
		autoLoan.set("autoAmount", 99999999);
		autoLoan.set("autoOnceAmount", 5000);
		autoLoan.set("autoIntMin", 100);
		autoLoan.set("autoIntMax", 2400);
		autoLoan.set("autoLimMin", 1);
		autoLoan.set("autoLimMax", 18);
		autoLoan.set("autoRefundType", "D");
		autoLoan.set("autoCreditMin", 1);
		autoLoan.set("autoCreditMax", 5);
		autoLoan.set("autoKeepBalance", 0);
		autoLoan.set("lastDate", DateUtil.getNowDateTime());
		return autoLoan;
	}
	
	///////////////////////////////////				自动投标  V2 版本方法				///////////////////////////////////
	
	
	/**
	 * 查询开启自动投标配置
	 * @param userCode
	 * @param autoState
	 * @return
	 */
	public AutoLoan_v2 findByUserCode(String userCode,String autoState){
		return AutoLoan_v2.autoLoanDao.findFirst(
				"select * from t_auto_loan_v2 where userCode = ? and autoState = ?", userCode,autoState);
	}
	
	/**
	 * 查询开启或关闭状态下自动投标配置
	 * @param userCode
	 * @param autoState
	 * @return
	 */
	public AutoLoan_v2 findByUserCode(String userCode){
		return AutoLoan_v2.autoLoanDao.findFirst(
				"select * from t_auto_loan_v2 where userCode = ? and (autoState = 'A' or autoState = 'C')", userCode);
	}
	
	/**
	 * 停用自动投标
	 * @param userCode
	 * @return
	 */
	public boolean stopAutoLoan_v2(String userCode){
		Db.update("update t_auto_loan_v2 set autoState = 'C' where autoState = 'A' and userCode = ? ", userCode);
		return true;
	}
	
	/**
	 * 设置自动投标状态
	 * @param aid
	 * @param autoState
	 * @return
	 */
	public boolean stopAutoLoan_v2(int aid,String autoState){
		Db.update("update t_auto_loan_v2 set autoState = ? where aid = ? ", autoState, aid);
		return true;
	}
	
	/**
	 * 设置自动投标状态(启用 停用)
	 * @param userCode
	 * @param autoState
	 * @return
	 */
	public boolean setAutoLoanState_v2(String userCode , String autoState){
		AutoLoan_v2 autoLoan = new AutoLoan_v2();
		boolean result = false;
		if("A".equals(autoState)){
			//初始化新增
			autoLoan = findByUserCode(userCode,"A");
			if(null == autoLoan){
				AutoLoan_v2 inintAutoLoan = inintAutoLoan_v2(userCode);
				result = inintAutoLoan.save();
			}
		}else{
			//停用
			result = stopAutoLoan_v2(userCode);
		}
		
		return result;
	}
	
	/**
	 * 保存或修改自动投标信息
	 * @param autoLoan
	 * @return
	 */
	public boolean saveOrUpdateAutoLoan_v2(AutoLoan_v2 autoLoan){
		String userCode = autoLoan.getStr("userCode");
		AutoLoan_v2 autoLoanDb = findByUserCode(userCode);
		if(null == autoLoanDb){
			//保存
			Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
			if(null != funds){
				autoLoan.set("autoAmount", funds.getLong("avBalance"));
			}
			autoLoan.set("autoDateTime", DateUtil.getNowDateTime());
			autoLoan.set("autoState", "A");
			//初次保存如果触发两次保存   检查一下
			if(isExist(userCode, "A"))
				return false;
			autoLoan.save();
		}else{
			int aid = autoLoanDb.getInt("aid");
			//修改
			if("C".equals(autoLoanDb.getStr("autoState"))){
				//销毁之前排队数据
				stopAutoLoan_v2(aid,"B");
				//新增排队记录，重新排队
				autoLoan.set("autoDateTime", DateUtil.getNowDateTime());
				if(StringUtil.isBlank(autoLoan.getStr("autoState"))){
					autoLoan.set("autoState", "A");
				}
				autoLoan.save();
			}else{
				autoLoan.set("aid", autoLoanDb.getInt("aid"));
				updateAutoLoan_v2(autoLoan);
			}
		}
		return true;
	}
	
	/**
	 * 修改自动投标
	 * @param autoLoan
	 */
	public void updateAutoLoan_v2(AutoLoan_v2 autoLoan){
		String sql = "update t_auto_loan_v2 set onceMinAmount=? ,onceMaxAmount=?,autoMinLim=?,autoMaxLim=?,refundType=?,productType=? where aid = ?";
		Db.update(sql, autoLoan.getLong("onceMinAmount"),autoLoan.getLong("onceMaxAmount"),
				autoLoan.getInt("autoMinLim"),autoLoan.getInt("autoMaxLim"),
				autoLoan.getStr("refundType"),autoLoan.getStr("productType"),autoLoan.getInt("aid"));
	}
	
	
	public Long[] queryRank(String userCode){
		
//		String sql = "select sum(autoAmount) as a,count(*) as c from t_auto_loan_v2 where "
//				+ "autoState='A' and aid < (select aid from t_auto_loan_v2 where autoState='A' and userCode=?) ";
		String sql = "select sum(f.avBalance) as a,count(a.aid) as c,(select count(1) from t_auto_loan_v2 where autoState = 'A') as total from t_auto_loan_v2 a,t_funds f where "
				+ "a.autoState='A' and a.userCode=f.userCode and a.aid < (select aid from t_auto_loan_v2 where autoState='A' and userCode=?)";
		Object[] os =  Db.queryFirst(sql, userCode );
		Long[] vs = new Long[3];
		if( os[0] == null )
			vs[0] = 0L ;
		else
			vs[0] = Long.valueOf(os[0] +"");
		vs[1] = Long.valueOf(os[1]+"") ;
		vs[2] = Long.valueOf(os[2]+"") ;
		return vs;
	}
	
	
	/**
	 * 初始化自动投标信息
	 * @param userCode
	 * @return
	 */
	private AutoLoan_v2 inintAutoLoan_v2(String userCode){
		AutoLoan_v2 autoLoan = new AutoLoan_v2();
		Funds funds = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance");
		if(null != funds){
			autoLoan.set("autoAmount", funds.getLong("avBalance"));
		}
		autoLoan.set("userCode", userCode);
		autoLoan.set("onceMaxAmount", 99999999);
		autoLoan.set("onceMinAmount", 5000);
		autoLoan.set("autoMinLim", 1);
		autoLoan.set("autoMaxLim", 18);
		autoLoan.set("refundType", "D");
		autoLoan.set("autoDateTime", DateUtil.getNowDateTime());
		autoLoan.set("autoState", "A");
		autoLoan.set("productType", "ABCD");
		return autoLoan;
	}
	
	/*
	 * 查询自动投标设置通过userCode 20180125 hw
	 */
	public AutoLoan_v2 queryAutoLoanByUserCode(String userCode){
		List<AutoLoan_v2> autoLoan = AutoLoan_v2.autoLoanDao.find("select userCode,userName,autoAmount,onceMinAmount,onceMaxAmount,autoMinLim,autoMaxLim,refundType,autoDateTime,autoState,PayAmount,productType,priorityMode,useTicket,autoType from t_auto_loan_v2 where userCode=?", userCode);
		
		if(autoLoan.size()>=0){
			return autoLoan.get(0);
		}else{
			return null;
		}
		
	}
	
	
}











