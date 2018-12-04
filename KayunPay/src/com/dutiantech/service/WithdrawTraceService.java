package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.Funds;
import com.dutiantech.model.User;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class WithdrawTraceService extends BaseService {
	
	private static final String basic_selectFields = " withdrawCode,bankTraceCode,userCode,userName,userTrueName,bankCode,bankNo,bankType,bankName,cardCity,withdrawAmount,status,isScore,createDateTime,modifyDateTime,withdrawRemark,useFree,sxf";
	
	/**
	 * 查询所有未审核的提现申请记录
	 * @return
	 */
	public List<WithdrawTrace> findByPage4WSH(){
		return WithdrawTrace.withdrawTraceDao.find("select "+basic_selectFields +" from t_withdraw_trace where status='0' ");
	}
	
	/**
	 * 保存申请提现记录
	 * @param userCode
	 * @param userName
	 * @param userTrueName
	 * @param bankCode
	 * @param bankNo
	 * @param bankType
	 * @param bankName
	 * @param cardCity
	 * @param withdrawAmount
	 * @param isScore
	 * @param withdrawRemark
	 * @param okDateTime		成功时间  ""
	 * @return
	 */
	public boolean save(String withdrawCode,String userCode,String userName,String userTrueName,
			String bankCode,String bankNo,String bankType,String bankName,
			String cardCity,Long withdrawAmount,String status,String isScore,String withdrawRemark,String okDateTime,String useFree,boolean renGong){
		
		WithdrawTrace withdrawTrace = new WithdrawTrace();
		withdrawTrace.set("withdrawCode", withdrawCode);
		withdrawTrace.set("bankTraceCode", withdrawCode);
		withdrawTrace.set("userCode", userCode);
		withdrawTrace.set("userName", userName);
		withdrawTrace.set("userTrueName", userTrueName);
		withdrawTrace.set("bankCode", bankCode);
		withdrawTrace.set("bankNo", bankNo);
		withdrawTrace.set("bankType", bankType);
		withdrawTrace.set("withdrawType", "JX");
		withdrawTrace.set("bankName", bankName);
		withdrawTrace.set("cardCity", cardCity);
		withdrawTrace.set("withdrawAmount", withdrawAmount);
		withdrawTrace.set("status", status);
		withdrawTrace.set("isScore", isScore);
		withdrawTrace.set("createDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
		if(StringUtil.isBlank(useFree)){
			withdrawTrace.set("useFree", "n");
		}else{
			withdrawTrace.set("useFree", useFree);
		}
		
		withdrawTrace.set("bankMac", "0000");
		withdrawTrace.set("withdrawRemark", withdrawRemark);
		
		if(renGong){
			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
		}else{
			//计算手续费
			long sysFee = calculateSysFee(userCode, withdrawAmount, isScore, useFree);
			withdrawTrace.set("sxf", sysFee);
		}
		return withdrawTrace.save();
	}
	
	/**
	 * 借款人后台提现保存提现流水
	 * @param userCode
	 * @param userName
	 * @param userTrueName
	 * @param bankCode
	 * @param bankNo
	 * @param bankType
	 * @param bankName
	 * @param cardCity
	 * @param withdrawAmount
	 * @param isScore
	 * @param withdrawRemark
	 * @param okDateTime		成功时间  ""
	 * @param opUserCode
	 * @param opUserName
	 * @param loanApplyCode
	 * @return 
	 * */
	public boolean save4loanUser(String withdrawCode,String userCode,String userName,String userTrueName,
			String bankCode,String bankNo,String bankType,String bankName,
			String cardCity,Long withdrawAmount,String status,String isScore,String withdrawRemark,String opUserCode,String opUserName,String loanApplyCode){
		
		WithdrawTrace withdrawTrace = new WithdrawTrace();
		withdrawTrace.set("withdrawCode", withdrawCode);
		withdrawTrace.set("bankTraceCode", withdrawCode);
		withdrawTrace.set("userCode", userCode);
		withdrawTrace.set("userName", userName);
		withdrawTrace.set("userTrueName", userTrueName);
		withdrawTrace.set("bankCode", bankCode);
		withdrawTrace.set("bankNo", bankNo);
		withdrawTrace.set("bankType", bankType);
		withdrawTrace.set("withdrawType", "JX");
		withdrawTrace.set("bankName", bankName);
		withdrawTrace.set("cardCity", cardCity);
		withdrawTrace.set("withdrawAmount", withdrawAmount);
		withdrawTrace.set("status", status);
		withdrawTrace.set("isScore", isScore);
		withdrawTrace.set("createDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("opUserCode", opUserCode);
		withdrawTrace.set("opUserName", opUserName);
		withdrawTrace.set("useFree", "n");
		withdrawTrace.set("bankMac", "0000");
		withdrawTrace.set("withdrawRemark", withdrawRemark);
		withdrawTrace.set("sxf", 0);
		withdrawTrace.set("loanApplyCode", loanApplyCode);
		return withdrawTrace.save();
	}
	
	/**
	 * 平台手续费/红包账户提现流水
	 * @param withdrawCode
	 * @param userCode
	 * @param userName
	 * @param userTrueName
	 * @param bankCode
	 * @param bankNo
	 * @param bankType
	 * @param bankName
	 * @param cardCity
	 * @param withdrawAmount
	 * @param status
	 * @param isScore
	 * @param withdrawRemark
	 * @param opUserCode
	 * @param opUserName
	 * @return
	 */
	public boolean savePlatUser(String withdrawCode,String userCode,String userName,String userTrueName,
			String bankCode,String bankNo,String bankType,String bankName,
			String cardCity,Long withdrawAmount,String status,String isScore,String withdrawRemark,String opUserCode,String opUserName){
		
		WithdrawTrace withdrawTrace = new WithdrawTrace();
		withdrawTrace.set("withdrawCode", withdrawCode);
		withdrawTrace.set("bankTraceCode", withdrawCode);
		withdrawTrace.set("userCode", userCode);
		withdrawTrace.set("userName", userName);
		withdrawTrace.set("userTrueName", userTrueName);
		withdrawTrace.set("bankCode", bankCode);
		withdrawTrace.set("bankNo", bankNo);
		withdrawTrace.set("bankType", bankType);
		withdrawTrace.set("withdrawType", "JX");
		withdrawTrace.set("bankName", bankName);
		withdrawTrace.set("cardCity", cardCity);
		withdrawTrace.set("withdrawAmount", withdrawAmount);
		withdrawTrace.set("status", status);
		withdrawTrace.set("isScore", isScore);
		withdrawTrace.set("createDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("opUserCode", opUserCode);
		withdrawTrace.set("opUserName", opUserName);
		withdrawTrace.set("useFree", "n");
		withdrawTrace.set("bankMac", "0000");
		withdrawTrace.set("withdrawRemark", withdrawRemark);
		withdrawTrace.set("sxf", 0);
		return withdrawTrace.save();
	}
	/**
	 * 根据旧的提现流水生成一个新的待审核的提现流水
	 * @param oldTrace
	 * @return
	 */
	public boolean madeNewWithdrawTrace(WithdrawTrace oldTrace){
		WithdrawTrace withdrawTrace = new WithdrawTrace();
		withdrawTrace.set("withdrawCode", UIDUtil.generate());
		withdrawTrace.set("userCode", oldTrace.getStr("userCode"));
		withdrawTrace.set("userName", oldTrace.getStr("userName"));
		withdrawTrace.set("userTrueName", oldTrace.getStr("userTrueName"));
		withdrawTrace.set("bankCode", oldTrace.getStr("bankCode"));
		withdrawTrace.set("bankNo", oldTrace.getStr("bankNo"));
		withdrawTrace.set("bankType", oldTrace.getStr("bankType"));
		withdrawTrace.set("withdrawType", "SYS");
		withdrawTrace.set("bankName", oldTrace.getStr("bankName"));
		withdrawTrace.set("cardCity", oldTrace.getStr("cardCity"));
		withdrawTrace.set("withdrawAmount", oldTrace.getLong("withdrawAmount"));
		withdrawTrace.set("status", "0");
		withdrawTrace.set("isScore", oldTrace.getStr("isScore"));
		withdrawTrace.set("createDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
		withdrawTrace.set("useFree", oldTrace.getStr("useFree"));
		withdrawTrace.set("bankMac", "0000");
		withdrawTrace.set("withdrawRemark", "申请提现["+oldTrace.getStr("withdrawCode")+"]");
		withdrawTrace.set("okDateTime", "");
		withdrawTrace.set("sxf", oldTrace.getInt("sxf"));
		return withdrawTrace.save();
	}
	
	/**
	 * 计算手续费
	 * @param userCode
	 * @param withdrawAmount
	 * @param isScore
	 * @return
	 */
	public long calculateSysFee(String userCode,Long withdrawAmount,String isScore, String useFree){
		long sysFee = 0;
		User user = User.userDao.findById(userCode);
		Funds funds = Funds.fundsDao.findById(userCode);
		Integer vipLevel = user.getInt("vipLevel");
		
		//vip 上校以上不需要手续费
		if(vipLevel < 8){
			//使用积分抵扣不需要手续费
			if("1".equals(isScore) == false && useFree.equals("y") == false){
				sysFee = 200;
			}
		}

		//计算充值未投资管理费
		Long totalWithdraw = funds.getLong("totalWithdraw");//提现总额
		Long beRecyPrincipal = funds.getLong("beRecyPrincipal");//待收回本金
		Long reciedPrincipal = funds.getLong("reciedPrincipal");//已回收本金
		Long reciedInterest = funds.getLong("reciedInterest");//已回收利息
		
		//投资总额 (待收回本金 + 已回收本金 + 已回收利息)
		Long loanTotalAmount = beRecyPrincipal+reciedPrincipal+reciedInterest;
		
		//无需收手续费部分(投资总额 - (提现总额 - 提现金额)) 备注：提现之前已经把提现金额加入提现总额
		//Long noFee = loanTotalAmount - (totalWithdraw - withdrawAmount);
		
		//无需收手续费部分(投资总额 - 提现总额)
		Long noFee = loanTotalAmount - totalWithdraw;  //  WCF  20170710
		
		//需要收取服务费部分( 提现总额 - 投资总额 )  备注：此计算方法 当需要收取服务费部分大于提现金额时有问题
		//Long yesFee = totalWithdraw - loanTotalAmount;
		
		//需要收取服务费部分( 提现金额 - 无需收手续费部分)
		Long yesFee = withdrawAmount - (noFee < 0 ? 0 : noFee);
		
		//手续费
		if(yesFee > 0){
			sysFee += CommonUtil.yunsuan(yesFee+"", "100", "chu", 0).longValue();
		}
		
		return sysFee;
	}
	
	public static void main(String[] args) {
		
		long sysFee = 0;
		System.out.println("系统手续费(2元)："+0);
		
		long withdrawAmount = 187093;
		System.out.println("当前提现金额："+187093);
		//计算充值未投资管理费
		long totalWithdraw = 69262468;
		System.out.println("提现总额："+69262468);
		long beRecyPrincipal = 0;
		System.out.println("待回收本金"+0);
		long reciedPrincipal = 65803938;
		System.out.println("已回收本金"+65803938);
		long reciedInterest = 3335122;
		System.out.println("已回收利息"+3335122);
		
		//投资总额 (待收回本金 + 已回收本金 + 已回收利息)
		long loanTotalAmount = beRecyPrincipal+reciedPrincipal+reciedInterest;
		System.out.println("投资总额 (待收回本金 + 已回收本金 + 已回收利息)"+loanTotalAmount);
		
		//无需收手续费部分(投资总额 - (提现总额 - 提现金额)) 备注：提现之前已经把提现金额加入提现总额
		long noFee = loanTotalAmount - (totalWithdraw - withdrawAmount);
		System.out.println("无需收手续费部分(投资总额 - (提现总额 - 提现金额))"+noFee);
		
		//需要收取服务费部分( 提现总额 - 投资总额 )  备注：此计算方法 当需要收取服务费部分大于提现金额时有问题
		//Long yesFee = totalWithdraw - loanTotalAmount;
		
		//需要收取服务费部分( 提现金额 - 无需收手续费部分)
		long yesFee = withdrawAmount - (noFee < 0 ? 0 : noFee);
		System.out.println("需要收取服务费部分( 提现金额 - 无需收手续费部分)"+yesFee);
		
		//手续费
		if(yesFee > 0){
			sysFee += CommonUtil.yunsuan(yesFee+"", "100", "chu", 0).longValue();
		}
		
		System.out.println("最终手续费,系统手续费(2元)+需要收取手续费部分的1%："+sysFee);
	}
	
	
	/**
	 * 根据编码查询一条提现申请详细信息
	 * @param applyCode		提现申请编码
	 * @return
	 */
	public WithdrawTrace findById(String applyCode){
		return WithdrawTrace.withdrawTraceDao.findByIdLoadColumns(applyCode, basic_selectFields);
	}
	
	/**
	 * 分页查询提现申请
	 * @param pageNumber	第几页
	 * @param pageSize		每页多少条
	 * @return
	 */
	public Map<String,Object> findByPage4Apply4Noob(Integer pageNumber, Integer pageSize, String allkey, String status){
		String sqlSelect = "select t1.* ";
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.withdrawAmount desc,t1.uid desc";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","=", status, "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<WithdrawTrace> pages = WithdrawTrace.withdrawTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}
	
	/**
	 * 分页查询提现申请
	 * @param pageNumber	第几页
	 * @param pageSize		每页多少条
	 * @return
	 */
	public Map<String,Object> findByPage4Apply4NoobWithSum(Integer pageNumber, Integer pageSize, String allkey, String status){
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","=", status, "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		long count_withdrawAmount = Db.queryBigDecimal("select COALESCE(sum(withdrawAmount),0) "+ sqlFrom+(makeSql4Where(buff)).toString(),paras.toArray()).longValue();
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("count_withdrawAmount", count_withdrawAmount);
		return result;
	}
	
	
	/**
	 * 分页查询提现流水原始记录对应的统计数据,返回Map,可用于生成excel
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param allkey
	 * @param status
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSum(String userCode,String beginDate, String endDate, String allkey,String status, String sharen, String withdrawType){
		
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode left join t_funds t3 on t1.userCode = t3.userCode";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","=", status, "and");
		makeExp(buff, paras, "t1.status","!=", "Y", "and");
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.okDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.okDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.withdrawType", "=", withdrawType, "and");
		
		
		if(!StringUtil.isBlank(sharen)){
			if(sharen.equals("1")){
				makeExp(buff, paras, "t3.loanTotal","<=", 0+"", "and");
			}else if(sharen.equals("0")){
				makeExp(buff, paras, "t3.loanTotal",">", 0+"", "and");
			}
		}
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Map<String,Object> result = new HashMap<String, Object>();
		long count_withdrawAmount = Db.queryBigDecimal("select COALESCE(sum(withdrawAmount),0) "+ sqlFrom+(makeSql4Where(buff)).toString(),paras.toArray()).longValue();
		long count_sxf = Db.queryBigDecimal("select COALESCE(sum(sxf),0) "+ sqlFrom+(makeSql4Where(buff)).toString(),paras.toArray()).longValue();
		long count_dzje = count_withdrawAmount - count_sxf;
		result.put("count_withdrawAmount", count_withdrawAmount);result.put("count_sxf", count_sxf);result.put("count_dzje", count_dzje);
		return result;
	}
	
	/**
	 * 分页查询提现流水原始记录,返回Map,可用于生成excel
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param allkey
	 * @param status
	 * @return
	 */
	public Map<String,Object> findByPage4Noob(String userCode,Integer pageNumber, Integer pageSize, String beginDate, String endDate, String allkey,String status,String sharen, String withdrawType){
		
		String sqlSelect = "select t1.* ";
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode left join t_funds t3 on t1.userCode = t3.userCode";
		String sqlOrder = " order by t1.uid desc";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","=", status, "and");
		makeExp(buff, paras, "t1.status","!=", "Y", "and");
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.okDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.okDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.withdrawType", "=", withdrawType, "and");
		
		if(!StringUtil.isBlank(sharen)){
			if(sharen.equals("1")){
				makeExp(buff, paras, "t3.loanTotal","<=", 0+"", "and");
			}else if(sharen.equals("0")){
				makeExp(buff, paras, "t3.loanTotal",">", 0+"", "and");
			}
		}
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<WithdrawTrace> pages = WithdrawTrace.withdrawTraceDao.paginate(pageNumber, pageSize, sqlSelect,sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}
	
	/**
	 * 分页查询提现申请
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param status
	 * @return
	 */
	public Page<WithdrawTrace> findByPage(String userCode, Integer pageNumber, Integer pageSize, String beginDate,
			String endDate, String status, String allkey) {
		String sqlSelect = "select t1.* ";
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.uid desc";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","=", status, "and");
		makeExp(buff, paras, "t1.status","!=", "Y", "and");
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.createDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDateTime", "<=", endDate, "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		return WithdrawTrace.withdrawTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
	}
	
	/**
	 * 修改提现申请状态
	 * @param withdrawCode
	 * @param status
	 * @param oStatus
	 * @return
	 */
	public boolean updateById(String withdrawCode, String status, String oStatus){
		WithdrawTrace withdrawTrace = WithdrawTrace.withdrawTraceDao.findByIdLoadColumns(withdrawCode, "withdrawCode,status");
		String x = withdrawTrace.getStr("status");
		if(x.equals(oStatus)){
			withdrawTrace.set("status", status);
			return withdrawTrace.update();
		}
		return false;
	}
	
	
	/**
	 * 删除一条提现申请
	 * @param applyCode		提现申请编码
	 * @return
	 */
	public boolean deleteById(String applyCode){
		return WithdrawTrace.withdrawTraceDao.deleteById(applyCode);
	}
	/**
	 * app分页查询提现记录
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param status
	 * @return
	 */
	public Map<String,Object> appQueryWithdraw(String userCode, Integer pageNumber, Integer pageSize, 
			  String allkey) {
		String sqlSelect = "select t1.* ";
		String sqlFrom = "from t_withdraw_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.uid desc";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.status","!=", "Y", "and");
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<WithdrawTrace> pages =WithdrawTrace.withdrawTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());

		 if(0==pages.getList().size()){
			 
			 result.put("tag", "0") ;
		 }
		 else{
			 
			 result.put("tag", "1") ; 
		 }
		@SuppressWarnings("rawtypes")
		ArrayList list=(ArrayList) pages.getList();
		for (int i = 0; i <list.size(); i++) {
			WithdrawTrace withdrawTrace=(WithdrawTrace)list.get(i);
			String traceDateTime=withdrawTrace.getStr("createDateTime");
			String withdrawAmount=Number.longToString(withdrawTrace.getLong("withdrawAmount"));
			withdrawTrace.put("withdrawAmount",withdrawAmount);
			 String traceState= withdrawTrace.getStr("status");
			String traceDateTimeFormat=DateUtil.parseDateTime(DateUtil.getDateFromString(traceDateTime, "yyyyMMddHHmmss"),
						"yyyy/MM/dd HH:mm:ss");
			 
			withdrawTrace.put("createDateTime", traceDateTimeFormat);
			 switch (traceState) {
			case "1":
				withdrawTrace.put("statusDesc", "已审核");
				break;
			case "2":
				withdrawTrace.put("statusDesc", "申请提现");
				break;
	        case "3":
	        	withdrawTrace.put("statusDesc", "提现成功");
				break;
	         case "4":
	        	 withdrawTrace.put("statusDesc", "提现失败");
		   break;
		   
			default:
				break;
			}
			 result.put("list", pages.getList());
		}
		return result;
	}
	
	/**
	 * 查询借款人提现记录
	 * */
	public WithdrawTrace findByUcodeAndLcode(String userCode,String loanApplyCode,String status){
		return WithdrawTrace.withdrawTraceDao.findFirst("select * from t_withdraw_trace where userCode=? and loanApplyCode=? and status=?",userCode,loanApplyCode,status);
	}
	
	/**
	 * 根据提现状态查询提现流水
	 * @param status
	 */
	public List<WithdrawTrace> findByStatus(String status){
		return WithdrawTrace.withdrawTraceDao.find("SELECT * FROM t_withdraw_trace where status=? ", status);
	}
	
	/**
	 * 根据提现状态和时间查询提现流水
	 * @param status
	 */
	public List<WithdrawTrace> findByStatusAndTime(String statu, String startTime, String endTime){
		String[] status = statu.split(",");
		return WithdrawTrace.withdrawTraceDao.find("SELECT * FROM t_withdraw_trace where (status=? OR status=?) AND createDateTime BETWEEN ? AND ?", status[0], status[1], startTime, endTime);
	}
}
