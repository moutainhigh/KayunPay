package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class LoanApplyService extends BaseService {
	
	public String findLoanUserDetail(String loanNo){
		String querySql = "select loanUserInfo from t_loan_apply where loanNo = ?";
		LoanApply loanApply = LoanApply.loanApplyDao.findFirst(querySql,loanNo);
		if(null == loanApply){
			return "";
		}
		return loanApply.getStr("loanUserInfo");
	}
	
	// 20170721   WCF
	public String findLoanDesc(String loanNo){
		String querySql = "select * from t_loan_apply where loanNo = ?";
		LoanApply loanApply = LoanApply.loanApplyDao.findFirst(querySql,loanNo);
		if(null == loanApply){
			return "";
		}
		return loanApply.getStr("loanDesc");
	}
	// end
	
	public boolean saveOrUpdateLoanApply(LoanApply loanApply){
		String querySql = "select * from t_loan_apply where loanCode=?";
		String loanCode = loanApply.getStr("loanCode");
		boolean doResult = false ;
		if( StringUtil.isBlank(loanCode) == false ){ 
			LoanApply tmpApply = LoanApply.loanApplyDao.findFirst( querySql , loanCode );
			if( tmpApply == null ){
				doResult = createLoanApply(loanApply) ;
			}else{
				tmpApply._setAttrs(loanApply) ;
				tmpApply.set("modifyDateTime", DateUtil.getNowDateTime());
				tmpApply.set("modifyDate", DateUtil.getNowDate());
				doResult = tmpApply.update() ;
			}
		}else{
			doResult = createLoanApply(loanApply) ;
		}
		return doResult ;
	}
	
	/**创建发标申请
	 * @param loanApply
	 * @return
	 */
	public boolean createLoanApply(LoanApply loanApply){
		loanApply.set("loanNo", null ) ;
		loanApply.set("loanCode", UIDUtil.generate() ) ;
		loanApply.set("applyDateTime", DateUtil.getNowDateTime());
		loanApply.set("applyDate", DateUtil.getNowDate());
		loanApply.set("modifyDateTime", DateUtil.getNowDateTime());
		loanApply.set("modifyDate", DateUtil.getNowDate());
		return loanApply.save();
	}
	
	public boolean audit4loan(String loanCode , String auditDesc , String auditState){
		String upSql = "update t_loan_apply set applyState=?,auditDesc=?,modifyDateTime=?,modifyDate=? where loanCode=?";
		return Db.update(upSql, auditState ,auditDesc , DateUtil.getNowDateTime() , DateUtil.getNowDate(), loanCode ) == 1;
	}
	
	/**
	 * 分页查询发标申请
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param allKey
	 * @param loanUserCode
	 * @return
	 */
	public Page<LoanApply> findByPage(Integer pageNumber, Integer pageSize, String beginDate,String endDate, String applyState,String allKey , String loanUserCode,String opUserCode){
		String sqlSelect = "select t1.*,t2.branchArea ";
		String sqlFrom = " from t_loan_apply t1 left join t_op_user_v2 t2 on t1.applyUserCode = t2.op_code ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.loanNo desc";
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.applyUserCode", "=", opUserCode, "and");
		
		makeExp(buff, paras, "t1.loanUserCode", "=", loanUserCode, "and");
		makeExp(buff, paras, "t1.applyDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.applyDate", "<=", endDate, "and");
		makeExp(buff, paras, "t1.applyState", "=", applyState, "and");
		
		String[] keys = new String[]{"t1.loanMobile","t1.loanUserName","t1.loanTrueName","t1.loanTitle","t1.loanMail","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allKey, "and","or");
		return LoanApply.loanApplyDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray()); 
	}
	
	public LoanApply findByCode(String loanCode){
		return LoanApply.loanApplyDao.findFirst("select * from t_loan_apply where loanCode=?", loanCode ) ;
	}
	
	/**
	 * 根据id查询发标申请明细
	 * @param loanNo	标明细
	 * @return
	 */
	public LoanApply findById(int loanNo){
		return LoanApply.loanApplyDao.findById(loanNo);
	}
	
	/**
	 * 更新发标申请状态
	 * @param loanNo			发标申请编号
	 * @param oldApplyState		当前状态
	 * @param newApplyState		预设的新状态
	 * @return
	 */
	public boolean updateApplyState(String loanNo, String oldApplyState, String newApplyState){
		LoanApply loanApply = LoanApply.loanApplyDao.findByIdLoadColumns(loanNo, "loanNo,applyState");
		String oApplyState = loanApply.getStr("applyState");
		if(oApplyState.equals(oldApplyState)){
//			Db.update("update t_loan_info set loanProcess = ? where loanCode = ?",newApplyState);
			loanApply.set("applyState", newApplyState);
			return loanApply.update();
		}
			
		return false;
	}
	
	/**
	 * 根据发标申请生成贷款标基本信息(不是发布操作)
	 * @param opUser			操作人, new String[]{"userCode","userName"}
	 * @param loanNo			发标申请编号
	 * @param loanType			贷款标类型
	 * @param applyState		审核状态
	 * @param loanState			贷款标状态
	 * @param rewardRateByYear	奖励年利率
	 * @return
	 */
	public boolean apply2loan(String[] opUser,int loanNo, SysEnum.loanType loanType, SysEnum.applyState applyState, SysEnum.loanState loanState,int rewardRateByYear){
		LoanApply loanApply = LoanApply.loanApplyDao.findById(loanNo);
		LoanInfo loanInfo = new LoanInfo();
		String loanCode = loanApply.getStr("loanCode");
		loanInfo.set("loanCode", loanCode);
		loanInfo.set("loanNo", loanApply.getInt("loanNo").toString());
		try {
			loanInfo.set("loanIndexByUser", MD5Code.md5(loanCode+loanCode.substring(0,15)));
		} catch (Exception e) {
			return false;
		}
		loanInfo.set("loanTitle", loanApply.getStr("loanTitle"));
		loanInfo.set("loanType", loanType.val());
		loanInfo.set("loanTypeDesc", loanType.desc());
		loanInfo.set("hasInvedByTrips", 0);//是否实地考察
		loanInfo.set("isInterest", 0);//本息保障
		loanInfo.set("isAutoLoan", 0);//自动放款
		loanInfo.set("hasCaptcha", 0);//是否需要验证码
		loanInfo.set("createDate", DateUtil.getNowDate());
		loanInfo.set("createTime", DateUtil.getNowTime());
		loanInfo.set("updateDate", DateUtil.getNowDate());
		loanInfo.set("updateTime", DateUtil.getNowTime());
		loanInfo.set("loanAmount", loanApply.getLong("loanAmount"));
		loanInfo.set("loanBalance", loanApply.getLong("loanAmount"));
		loanInfo.set("invedTripFees", 0);//实地考察费用
		loanInfo.set("serviceFees", 0);//服务费
		loanInfo.set("managerRate", 0);//管理费
		loanInfo.set("riskRate", 0);//风险储备金率
		loanInfo.set("userCode", loanApply.getStr("loanUserCode"));
		loanInfo.set("userName", loanApply.getStr("loanTrueName"));
		try {
			loanInfo.set("userCardId", CommonUtil.encryptUserCardId(loanApply.getStr("loanCardId")));
		} catch (Exception e) {
			return false;
		}
		loanInfo.set("loanArea", "");//地区
		loanInfo.set("loanProcess", applyState.val());
		loanInfo.set("loanState", loanState.val());
		loanInfo.set("opCode", opUser[0]);
		loanInfo.set("opName", opUser[1]);
		loanInfo.set("releaseDate","00000000");
		loanInfo.set("releaseTime", "000000");
		loanInfo.set("loanTimeLimit", loanApply.getInt("loanTimeLimit"));
		loanInfo.set("reciedCount", 0);
		loanInfo.set("loadByDay", 0);
		loanInfo.set("refundType", loanApply.getStr("refundType"));
		loanInfo.set("rateByYear", loanApply.getInt("rateByYear"));
		loanInfo.set("rewardRateByYear", rewardRateByYear);
		loanInfo.set("loanTotal", 0);//贷款总额
		loanInfo.set("loanCount", 0);//贷款总次数
		loanInfo.set("minLoanAmount", 0);
		loanInfo.set("maxLoanAmount", 0);
		loanInfo.set("benefits4new", 0);//新人专享福利
		loanInfo.set("maxLoanCount", 0);//最大投标次数
		loanInfo.set("loanUsedType", loanApply.getStr("loanUsedType"));
		loanInfo.set("loanDesc", loanApply.getStr("loanDesc"));
		loanInfo.set("backDate", "00000000");
		loanInfo.set("loanMac", "0000");
		loanInfo.set("effectDate", "00000000");
		loanInfo.set("effectTime", "000000");
		loanInfo.set("clearDay", -1);
		loanInfo.set("clearDate", "0000");
		loanInfo.set("loan_pic", "图偏");
		return loanInfo.save();
	}
	
	/**
	 * 根据标编号与借款人编号查询最新标
	 * */
	public LoanApply findByLoanNoAndUserCode(String loanNo,String userCode){
		return LoanApply.loanApplyDao.findFirst("select t1.*,t2.loanCode loanInfoCode from t_loan_apply t1 LEFT JOIN t_loan_info t2 on t1.loanNo=t2.loanNo where t2.loanState in ('N','O','P') and t1.loanNo = ? and t1.loanUserCode = ? order by applyDate desc",loanNo, userCode);
	}
}
