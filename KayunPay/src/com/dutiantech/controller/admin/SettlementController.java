package com.dutiantech.controller.admin;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Funds;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.HistoryRecyService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanOverdueService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.RepaymentCountService;
import com.dutiantech.service.ReturnedAmountService;
import com.dutiantech.service.SettlementEarlyService;
import com.dutiantech.service.SettlementPlanService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.service.asyn.SMSTaskService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.RepaymentCountEnum.repaymentCountStatus;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.jxTraceStatus;
import com.dutiantech.util.SysEnum.jxTxCode;
import com.dutiantech.util.SysEnum.loanOverdueType;
import com.dutiantech.util.SysEnum.loanState;
import com.dutiantech.util.SysEnum.traceState;
import com.dutiantech.util.SysEnum.traceType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.http.WebUtils;
import com.jx.service.JXService;
import com.jx.util.RetCodeUtil;

public class SettlementController extends BaseController {
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	
	private UserService userService = getService(UserService.class);
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	private HistoryRecyService historyRecyService = getService(HistoryRecyService.class);
	
	private SettlementEarlyService settlementEarlyService = getService(SettlementEarlyService.class);
	
	private SettlementPlanService settlementPlanService = getService(SettlementPlanService.class);
	
	private RecommendRewardService rrService = getService(RecommendRewardService.class);
	
	private static final Logger autoRecy4loanLogger = Logger.getLogger("autoRecy4loanLogger");
	
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	
	private RecommendInfoService recommendInfoService = getService(RecommendInfoService.class);
	
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	
	private ReturnedAmountService returnedAmountService = getService(ReturnedAmountService.class);
	
	private LoanOverdueService loanOverdueService = getService(LoanOverdueService.class);
	
	private OPUserV2Service oPUserV2Service = getService(OPUserV2Service.class);
	
	private RepaymentCountService repaymentCountService = getService(RepaymentCountService.class);
	
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
	
	private boolean isDate = false;//还款日期验证开关
	
	private static Map<String, User> payUserMap = new HashMap<String,User>();//还款投资人用户缓存,key:userCode,value:user
	private static Map<String, Long> paymentAuthPageStateMap = new HashMap<String,Long>();//用户开通缴费授权状态缓存,自动回款时初始化,key:电子账号,value:授权金额(仅目前开通状态下,其他状态均为null)
	
//	private static List<String> _LOCK_JxTrace = new LinkedList<String>();//jxTrace回款锁,避免即信同一响应短时间发送服务器处理不及时造成数据库死锁
	
	static{
		LoggerUtil.initLogger("autoRecy4loan", autoRecy4loanLogger);
	}
	
//	/**
//	 * 加锁	WJW
//	 * @param jxTraceCode
//	 */
//	private synchronized void lock(String jxTraceCode){
//		_LOCK_JxTrace.add(jxTraceCode);
//	}
	
//	/**
//	 * 判断是否处于锁定中 WJW
//	 * @param jxTraceCode
//	 * @return
//	 */
//	private boolean isLock(String jxTraceCode){
//		return (_LOCK_JxTrace.indexOf(jxTraceCode) != -1);
//	}
	
//	/**
//	 * 解锁 WJW
//	 * @param jxTraceCode
//	 */
//	private void unlock(String jxTraceCode){
//		_LOCK_JxTrace.remove(jxTraceCode);
//	}
	
	/**
	 * 获取还款投资人User WJW
	 * @param userCode
	 * @return
	 */
	private User getPayUser(String userCode){
		if(payUserMap.containsKey(userCode)){
			return payUserMap.get(userCode);
		}else {
			User user = userService.findByFields(userCode, "userCode","jxAccountId","vipInterestRate","beforeVip","regDate");
			payUserMap.put(userCode, user);
			return user;
		}
	}
	
	/**
	 * 根据实际回款日期获取原定回款日期 WJW
	 * @return
	 */
	private String getRepaymentDate(String repaymentYesDate){
		return DateUtil.delDay(repaymentYesDate, 1);//目前实际回款T+1
	}
	
	/**
	 * 根据原定回款日获取实际回款日期 WJW
	 * @param repaymentDate
	 * @return
	 */
	private String getRepaymentYesDate(String repaymentDate){
		return DateUtil.addDay(repaymentDate, 1);//目前实际回款T+1
	}
	
	/**
	 * 单个标提前还款 WJW
	 * @return
	 */
	@ActionKey("/advRecy4loan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message advRecy4loan(){
		String loanCode = getPara("loanCode","");
		if(StringUtil.isBlank(loanCode)){
			return error("", "请求参数不完整!", null ) ;
		}
		LoanInfo loan = LoanInfo.loanInfoDao.findById(loanCode);
		if(!loan.getStr("loanState").equals("N")){
			return error("", "借款标不是还款中", null);
		}
		
		//取消该标未成功债转
		try {
			doTransfer(loanCode,null);
		} catch (Exception e) {
			return error("", "取消债转失败", null);
		}
		
		int reciedCount = loan.getInt("reciedCount");//已还期数
		
		boolean isOverdue = false;//不逾期
		loanOverdueType overdueType = loanOverdueType.A;//逾期类型
		List<LoanOverdue> loanOverdueNots = loanOverdueService.queryLastRepayIndexByStatusAndTime("n","");
		for (LoanOverdue loanOverdue : loanOverdueNots) {
			if(loanCode.equals(loanOverdue.getStr("loanCode"))){
				isOverdue = true;//逾期
				overdueType = loanOverdueType.valueOf(loanOverdue.getStr("overdueType"));//逾期类型
				break;
			}
		}
		
		if(isOverdue){//逾期新增记录
			try {
				long sumLeftAmount = loanTraceService.sumLeftAmount(loanCode);
				boolean isLoanOverdue = addOverdue(loan, sumLeftAmount, 0l, overdueType, "标["+loan.getStr("loanNo")+"]第"+(reciedCount+1)+"期提前还款");
				autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期提前还款"+(isLoanOverdue?"成功":"失败"));
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期提前还款异常");
				return error("", "逾期新增记录异常", null);
			}
		}
		
		//更新loanTrace
		autoRecy4loanLogger.log(Level.INFO,"开始更新loanTrace..........");
		List<LoanTrace> loanTraces = loanTraceService.findLoanTraceByJieSuan(loanCode) ;
		if(loanTraces.size() < 1){
			return error("", "loanTraces为空", null);
		}
		try {
			doLoanTrace(loan, loanTraces,2,overdueType);//处理投标流水
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"loanCode:"+loanCode+",loanTrace结算异常......");
			return error("", "loanTrace结算异常", null);
		}
		
		//更新loanInfo
		autoRecy4loanLogger.log(Level.INFO,"开始更新loanInfo..........");
		try {
			doLoanInfo(loan,2);//更新标相关事物
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"loanCode:"+loanCode+",loanInfo结算异常......");
			return error("", "loanInfo结算异常", null);
		}
		
		//更新settlementEarly
		try {
			SettlementEarly seEarly = settlementEarlyService.findById(loanCode);
			settlementEarlyService.updateStatus(seEarly,"C");
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"结算标settlementEarly异常,loanCode:["+loanCode+"],跳过............");
			return error("", "settlementEarly更新异常", null);
		}
		
		//更新回款统计表
		autoRecy4loanLogger.log(Level.INFO,"开始更新回款统计表..........");
		try {
			String nowDate = DateUtil.getNowDate();
			boolean updateRepaymentCount = repaymentCountService.updateRepaymentCount(DateUtil.delDay(nowDate, 1),nowDate);
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表"+(updateRepaymentCount?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"loanCode:"+loanCode+",更新回款统计表异常..........");
		}
				
		return succ("结算完成","");
	}
	
	/**
	 * 单个标正常还款 WJW
	 * @return
	 */
	@ActionKey("/recy4loan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message recy4loan(){
		String loanCode = getPara("loanCode") ;
		if(StringUtil.isBlank(loanCode)){
			return error("", "请求参数不完整!", null ) ;
		}
		
		//取消未成功债转
		try {
			doTransfer(loanCode,null);
		} catch (Exception e) {
			return error("", "取消债转失败", null);
		}

		LoanInfo loan = LoanInfo.loanInfoDao.findFirst("select * from t_loan_info where loanCode=? and clearDate!=?", loanCode , DateUtil.getStrFromDate(new Date(), "MMdd")) ;
		if(loan == null){
			return error("", "标不存在" , null) ;
		}
		String loanState = loan.getStr("loanState");
		
		if(!"N".equals(loanState)){
			return error("02", "标不是还款中", null) ;
		}
		
//		if(isRecy4loan){
//			//判断阶段日期是否符合预期
//			int clearDay = loan.getInt("clearDay");
//			if( isCanClear(clearDay) == false ){
//				//当前是否符合结算日期，比如2月28号时，30号的结算日同样可以结算
//				return error("04", "未到结算日，当前是" + clearDay + "号！" , clearDay ) ;
//			}
//		}
		
		int reciedCount = loan.getInt("reciedCount");//已还期数
		
		boolean isOverdue = false;//不逾期
		loanOverdueType overdueType = loanOverdueType.A;//逾期类型
		List<LoanOverdue> loanOverdueNots = loanOverdueService.queryLastRepayIndexByStatusAndTime("n","");
		for (LoanOverdue loanOverdue : loanOverdueNots) {
			if(loanCode.equals(loanOverdue.getStr("loanCode"))){
				isOverdue = true;//逾期
				overdueType = loanOverdueType.valueOf(loanOverdue.getStr("overdueType"));//逾期类型
				break;
			}
		}
		
		if(isOverdue){//逾期新增记录
			try {
				long sumNextAmount = loanTraceService.sumNextAmount(loanCode);
				boolean isLoanOverdue = addOverdue(loan, sumNextAmount, 0l, overdueType, "标["+loan.getStr("loanNo")+"]第"+(reciedCount+1)+"期正常还款");
				autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期正常还款"+(isLoanOverdue?"成功":"失败"));
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期正常还款异常");
			}
		}
		
		List<LoanTrace> loanTraces = loanTraceService.findLoanTraceByJieSuan(loanCode) ;//需要结算的投标流水
		if(loanTraces.size() < 1){
			return error("", "loanTrace为空", null);
		}
		
		//更新loanTrace
		autoRecy4loanLogger.log(Level.INFO,"开始更新loanTrace..........");
		try {
			doLoanTrace(loan, loanTraces,1,overdueType);//处理标投标流水
		} catch (Exception e1) {
			autoRecy4loanLogger.log(Level.INFO, "loanCode:"+loanCode+",loanTrace结算异常");
			return error("", "loanTrace结算异常", null);
		}
		
		
  		//更新loanInfo
		autoRecy4loanLogger.log(Level.INFO,"开始更新loanInfo..........");
		try {
			boolean doResult = doLoanInfo(loan,1) ;//更新标相关事物
			autoRecy4loanLogger.log(Level.INFO, "loanCode:"+loanCode+",loanInfo结算"+(doResult?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO, "loanCode:"+loanCode+",loanInfo结算异常......");
			return error("", "", null);
		}
		
		//更新回款统计表
		autoRecy4loanLogger.log(Level.INFO,"开始更新回款统计表..........");
		try {
			String nowDate = DateUtil.getNowDate();
			boolean updateRepaymentCount = repaymentCountService.updateRepaymentCount(DateUtil.delDay(nowDate, 1),nowDate);
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表"+(updateRepaymentCount?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"loanCode:"+loanCode+",更新回款统计表异常..........");
		}
		
		return succ("结算完成","") ;
	}
	
//	/**
//	 * 	结算时，标相关处理
//	 * @param loan
//	 * @return
//	 */
//	private boolean doLoanInfo(LoanInfo loan ){
//		String nowSDate = DateUtil.getStrFromDate(new Date(), "MMdd");
//		int reciedCount = loan.getInt("reciedCount");
//		loan.set("reciedCount", (reciedCount+1) ) ;
//		loan.set("clearDate", nowSDate ) ;	//更新日期
//		String loanUserCode = loan.getStr("userCode");
//		//更新下一个还款日
//		loan.set("updateDate", DateUtil.getNowDate());
//		loan.set("updateTime", DateUtil.getNowTime());
//		if( loan.update() == true ){
//			long amount = loan.getLong("loanAmount");
//			int limit = loan.getInt("loanTimeLimit");
//			int rate = loan.getInt("rateByYear");
//			int rewardRateByYear = loan.getInt("rewardRateByYear");
//			int benefits4new = loan.getInt("benefits4new");
//			rate = rate + rewardRateByYear + benefits4new;
//
//			String refundType = loan.getStr("refundType");
//			//标信息修改成功，修改借款人资金账户
//			Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode) ;
//
//			int loanCount = funds.getInt("loanCount");
//			int loanSuccessCount = funds.getInt("loanSuccessCount");
//			int loanBySysCount = funds.getInt("loanBySysCount");
//			long beRecyPrincipal4loan = funds.getLong("beRecyPrincipal4loan");
//			long beRecyInterest4loan = funds.getLong("beRecyInterest4loan");
//			
//			//计算当期需还本金和利息
//			long[] benxi = CommonUtil.f_000(amount, limit, rate, (reciedCount+1) , refundType );
//			long ben = benxi[0];
//			long xi = benxi[1] ;
//			long reciedAmount = benxi[0] + benxi[1] ;
//			long avBalance = funds.getLong("avBalance") ;
//			if( reciedAmount > avBalance ){
//				funds = fundsServiceV2.recharge( loanUserCode, reciedAmount-avBalance,0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
//				loanBySysCount += 1;
//			}else{
//				loanSuccessCount += 1 ;
//			}
//			if( ben > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, ben , 0 , traceType.U , fundsType.D , 
//						String.format("标[%s]第%d/%d期还本金", loan.getStr("loanNo") , reciedCount+1,limit ) );
//				beRecyPrincipal4loan -= ben ;
//			}
//			if( xi > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, xi , 0 , traceType.I , fundsType.D , 
//						String.format("标[%s]第%d/%d期还利息", loan.getStr("loanNo") , reciedCount+1,limit ) );
//				beRecyInterest4loan -= xi ;
//			}
//			settlementPlanService.settlement(loan.getStr("loanCode"), reciedCount+1, ben, xi, SysEnum.settlementState.B);
//			//loanCount-1 ，loanSuccessCount+1，beRecyPrincipal4loan-本金,beRecyInterest4loan-利息，updateDate/time
//			//更新资金账户冗余信息
//			funds.set("loanCount", loanCount  - 1 ) ;
//			funds.set("loanSuccessCount", loanSuccessCount ) ;
//			funds.set("loanBySysCount", loanBySysCount ) ;
//			funds.set("beRecyPrincipal4loan", beRecyPrincipal4loan ) ;
//			funds.set("beRecyInterest4loan", beRecyInterest4loan ) ;
//			return funds.update() ;
//			//更新账户余额,需要加流水
//			//funds = fundsServiceV2.doAvBalance(loanUserCode, 1 , reciedAmount ) ;
//			//流水
//		}
//		
//		return false ;
//	}
	
	/**
	 * 更新loanInfo WJW
	 * @param loan
	 * @param repaymentType	还款类型 1:正常还款,2:提前还款
	 * @return
	 */
	private boolean doLoanInfo(LoanInfo loan,int repaymentType){
		String loanCode = loan.getStr("loanCode");
		int limit = loan.getInt("loanTimeLimit");
		String effectDate = loan.getStr("effectDate");
		int reciedCount = loan.getInt("reciedCount");
		if(repaymentType == 1){//正常还款
			if(limit == (reciedCount+1)){//最后一期还款,更新标状态
				loan.set("backDate", DateUtil.getNowDate());
				loan.set("loanState", SysEnum.loanState.O.val()) ;
			}else{
				loan.set("backDate", CommonUtil.anyRepaymentDate4string(effectDate,reciedCount+1+1));
			}
		}else if(repaymentType == 2){//提前还款
			loan.set("loanState", loanState.P.val());
			loan.set("backDate", DateUtil.getNowDate());//更新日期
		}
		loan.set("reciedCount", reciedCount+1);
		loan.set("clearDate", DateUtil.getStrFromDate(new Date(),"MMdd"));//更新日期
		loan.set("updateDate", DateUtil.getNowDate());
		loan.set("updateTime", DateUtil.getNowTime());
		boolean updateLoan = loan.update();
		
		if(updateLoan){
			Long[] doLoanUserFunds = doLoanUserFunds(loan, repaymentType);//更新借款人资金
			//更新借款人还款计划表
			settlementPlanService.settlement(loanCode, reciedCount+1, doLoanUserFunds[0], doLoanUserFunds[1], repaymentType == 1 ? SysEnum.settlementState.B:SysEnum.settlementState.C);
		}
		return true;
	}
	
	/**
	 * 更新借款人资金流水 WJW
	 * @param loan
	 * @param repaymentType	还款类型 1:正常还款,2:提前还款
	 * @return	Long[0]:还款本金,Long[1]:还款利息
	 */
	private Long[] doLoanUserFunds(LoanInfo loan,int repaymentType){
		String loanUserCode = loan.getStr("userCode");
		String loanNo = loan.getStr("loanNo");
		int limit = loan.getInt("loanTimeLimit");
		int reciedCount = loan.getInt("reciedCount");
		long amount = loan.getLong("loanAmount");
		String refundType = loan.getStr("refundType");
		int rate = loan.getInt("rateByYear");
		int rewardRateByYear = loan.getInt("rewardRateByYear");
		int benefits4new = loan.getInt("benefits4new");
		rate = rate + rewardRateByYear + benefits4new;
		int loanRecyCount = limit - (reciedCount-1);
		
		Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode);
		int loanCount = funds.getInt("loanCount");//历史借款总次数
		int loanSuccessCount = funds.getInt("loanSuccessCount");//成功还款笔数
		int loanBySysCount = funds.getInt("loanBySysCount");//平台代还笔数
		long beRecyPrincipal4loan = funds.getLong("beRecyPrincipal4loan");//待还本金总额
		long beRecyInterest4loan = funds.getLong("beRecyInterest4loan");//待还利息总额
		long avBalance = funds.getLong("avBalance");//可用余额
		
		long[] benxi = CommonUtil.f_000(amount, limit, rate, reciedCount, refundType);//计算当期需还本金和利息
		long principal = 0;//还款本金
		long interest = benxi[1];//还款利息
		
		if(repaymentType == 1){//正常还款
			principal = benxi[0];
			loanCount = loanCount - 1;
			beRecyInterest4loan -= interest;
		}else if(repaymentType == 2){//提前还款
			long[] lastBenxi = CommonUtil.f_006(amount, rate, limit, (reciedCount-1) , refundType);//计算剩余本金利息
			principal = lastBenxi[0];
			loanCount -= loanRecyCount;
			beRecyInterest4loan -= lastBenxi[1];
		}
		
		//借款人穷了,平台代充还款
		if(principal + interest > avBalance){//余额不足
			funds = fundsServiceV2.recharge( loanUserCode, principal + interest - avBalance,0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val());
			if(repaymentType == 1){
				loanBySysCount += 1;
			}else if(repaymentType == 2){
				loanBySysCount += loanRecyCount;
			}
		}else {//余额充足
			if(repaymentType == 1){
				loanSuccessCount += 1;
			}else if(repaymentType == 2){
				loanSuccessCount += loanRecyCount;
			}
		}
		
		//本金还款
		if(principal > 0){
			funds = fundsServiceV2.doAvBalance4biz(loanUserCode, principal , 0 , traceType.U , fundsType.D , 
					String.format("标[%s]第%d/%d期"+(repaymentType == 1 ? "":"提前还款")+"还本金", loanNo , reciedCount,limit ) );
		}
		//利息还款
		if(interest > 0){
			funds = fundsServiceV2.doAvBalance4biz(loanUserCode, interest , 0 , traceType.I , fundsType.D , 
					String.format("标[%s]第%d/%d期"+(repaymentType == 1 ? "":"提前还款")+"还利息", loanNo , reciedCount,limit ) );
		}
		
		funds.set("loanCount", loanCount);
		funds.set("beRecyInterest4loan", beRecyInterest4loan);
		funds.set("beRecyPrincipal4loan", beRecyPrincipal4loan - principal);
		funds.set("loanBySysCount", loanBySysCount);
		funds.set("loanSuccessCount", loanSuccessCount);
		funds.update();
		return new Long[]{principal,interest};
	}
	
	/**
	 * 检查债权转让，如果有结算当天的债权，直接取消债权
	 * @param loanCode
	 * @param nowDate	取消日期
	 */
	private void doTransfer(String loanCode,String nowDate){
		if(StringUtil.isBlank(loanCode)){
			//取消所有当天结算的债权
			Db.update("update t_loan_transfer set transState='C' where transState='A' and nextRecyDay=? ",nowDate);
			//修改投标流水状态
			Db.update("update t_loan_trace set isTransfer='B' where isTransfer='A' and loanRecyDate=? and traceCode in(select traceCode from t_loan_transfer where transState='B' group by traceCode)" , nowDate ) ;
			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanRecyDate=? and traceCode not in(select traceCode from t_loan_transfer where transState='B' group by traceCode)" , nowDate ) ;
		}else{
			Db.update("update t_loan_transfer set transState='C' where transState='A' and loanCode=? ",loanCode);
			//修改投标流水状态
			Db.update("update t_loan_trace set isTransfer='B' where isTransfer='A' and loanCode=? and traceCode in (select traceCode from t_loan_transfer where loanCode=? and transState='B' group by traceCode)",loanCode,loanCode);
			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanCode=? and traceCode not in (select traceCode from t_loan_transfer where loanCode=? and transState='B' group by traceCode)",loanCode,loanCode);
		}
		
	}
	
	private boolean isCanClear(int nowDay){
		Integer[] days = getClearDay() ;
		for( int day : days ){
			if( day == nowDay ){
				return true ;
			}
		}
		return false ;
	}
	
	private Integer[] getClearDay(){
		Calendar nowCal = Calendar.getInstance() ;
		int nowDay = nowCal.get( Calendar.DAY_OF_MONTH ) ;
//		nowCal.set(Calendar.DAY_OF_MONTH , -1 );
//		int lastDay = nowCal.get( Calendar.DAY_OF_MONTH );
		int lastDay = nowCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
		Integer[] days = null ;
		if( nowDay < lastDay ){
			days = new Integer[]{nowDay};
		}else{
			int dayLimit = 32 - nowDay ;//(31-nowDay) + 1
			days = new Integer[ dayLimit ] ;
			for(int count = 0 ; count < dayLimit ; count ++ ){
				days[count] = nowDay + count ;
			}
		}
		return days ;
	}
	
	
//	/**
//	 * 	提前还款时，标相关处理
//	 * @param loan
//	 * @return
//	 */
//	private boolean doLoanInfo4adv(LoanInfo loan ){
//		String nowSDate = DateUtil.getStrFromDate(new Date(), "MMdd");
//		int reciedCount = loan.getInt("reciedCount");
//		int limit = loan.getInt("loanTimeLimit");
//		loan.set("reciedCount", reciedCount+1 ) ;
////		loan.set("traceState", traceState.B.val() ) ;
//		loan.set("loanState", loanState.P.val() ) ;
//		loan.set("clearDate", nowSDate ) ;	//更新日期
//		loan.set("backDate", DateUtil.getNowDate() ) ;	//更新日期
//		String loanUserCode = loan.getStr("userCode");
//		loan.set("updateDate", DateUtil.getNowDate());
//		loan.set("updateTime", DateUtil.getNowTime());
//		if( loan.update() == true ){
//			long amount = loan.getLong("loanAmount");
//			int rate = loan.getInt("rateByYear");
//			int rewardRateByYear = loan.getInt("rewardRateByYear");
//			int benefits4new = loan.getInt("benefits4new");
//			rate = rate + rewardRateByYear + benefits4new;
//			String refundType = loan.getStr("refundType");
//			//标信息修改成功，修改借款人资金账户
//			Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode) ;
//
//			int loanCount = funds.getInt("loanCount");
//			int loanSuccessCount = funds.getInt("loanSuccessCount");
//			int loanBySysCount = funds.getInt("loanBySysCount");
//			long beRecyPrincipal4loan = funds.getLong("beRecyPrincipal4loan");
//			long beRecyInterest4loan = funds.getLong("beRecyInterest4loan");
//			
//			//计算剩余本金利息
//			long[] lastBenxi = CommonUtil.f_006(amount, rate, limit, reciedCount , refundType) ;
//			
//			//计算当期需还本金和利息
//			long[] benxi = CommonUtil.f_000(amount, limit, rate, (reciedCount+1) , refundType );
//			//long ben = benxi[0];//提前结算不用验证当期要还的本金是否大于余额，尼玛先息后本提前还款的话当期没本金 0>0  false
//			long xi = benxi[1] ;
//			long reciedAmount = lastBenxi[0] + xi ;
//			long avBalance = funds.getLong("avBalance") ;
//			if( reciedAmount > avBalance ){
//				funds = fundsServiceV2.recharge( loanUserCode, reciedAmount-avBalance,0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
//				loanBySysCount += (limit-reciedCount);
//			}else{
//				loanSuccessCount += (limit-reciedCount) ;
//			}
//			
//			if( lastBenxi[0] > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, lastBenxi[0] , 0 , traceType.U , fundsType.D , 
//						String.format("标[%s]第%d/%d期提前还款还本金", loan.getStr("loanNo") , reciedCount+1,limit ) );
//			}
//			if( xi > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, xi , 0 , traceType.I , fundsType.D , 
//						String.format("标[%s]第%d/%d期提前还款还利息", loan.getStr("loanNo") , reciedCount+1,limit ) );
//			}
//			
//			settlementPlanService.settlement(loan.getStr("loanCode"), reciedCount+1, lastBenxi[0], xi, SysEnum.settlementState.C);
//			//loanCount-1 ，loanSuccessCount+1，beRecyPrincipal4loan-本金,beRecyInterest4loan-利息，updateDate/time
//			//更新资金账户冗余信息
//			funds.set("loanCount", loanCount  - (limit-reciedCount) ) ;
//			funds.set("loanSuccessCount", loanSuccessCount ) ;
//			funds.set("loanBySysCount", loanBySysCount ) ;
//			funds.set("beRecyPrincipal4loan", beRecyPrincipal4loan - reciedAmount + xi ) ;
//			funds.set("beRecyInterest4loan", beRecyInterest4loan - lastBenxi[1]  ) ;
//			return funds.update() ;
//			//更新账户余额,需要加流水
//		}
//		
//		return true ;
//	}
	
	/**
	 * 自动还款 WJW
	 * @return
	 */
	@ActionKey("/autoRecy4loan")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public synchronized Message AutoRecy4loan(){
		String date = getPara("date",DateUtil.delDay(DateUtil.getNowDate(), 1));//还款日期(默认昨天)
		int num = getParaToInt("num",10000);//限制正常还款条数
		if(StringUtil.isBlank(date) || !StringUtil.isNumeric(date) || date.length() != 8){
			return error("", "还款日期输入错误", null);
		}
		
		// 密钥验证
		String key = getPara("key");
		if(!key.equals("nicaizhecanshushiganshade201703031944")){
			return error("05", "XXXXXXXXXX", false);
		}
		
		//查询当日待还数据
		Map<String, Long> repaymentCountMap = loanInfoService.repaymentCount(date);
		
		//验证存管代偿户和红包户回款资金是否充足
		try {
			long sumBatchAmount = repaymentCountMap.get("pcdcyhzbj") + repaymentCountMap.get("pcdcyhzlx");
			long sumVoucherPayInterest =repaymentCountMap.get("hbyhzbj") + repaymentCountMap.get("hbyhzlx");
			if(!interceptJXFunds(sumBatchAmount,sumVoucherPayInterest)){
				WebUtils.writePromptHtml("存管资金不足,请核对资金", "/main#", "UTF-8",getResponse());
				return error("", "存管资金不足,请核对资金", null);
			}
		} catch (Exception e) {
			WebUtils.writePromptHtml("存管资金验证异常", "/main#", "UTF-8",getResponse());
			return error("", "存管资金验证异常", null);
		}
		
		//验证页面参数是否正常
		/*try {
			String userCode = getUserCode();
			if(!autoLoanIntercept(userCode)){
				return error("", "页面参数验证失败", null);
			}
		} catch (Exception e) {
			return error("", "页面参数验证异常", null);
		}*/
		
		//新增回款统计记录
		try {
			Map<String, Object> repaymentMap = new HashMap<String,Object>();
			repaymentMap.putAll(repaymentCountMap);
			repaymentMap.put("repaymentDate", date);
			repaymentMap.put("repaymentYesDate", DateUtil.getNowDate());
			repaymentMap.put("repaymentStatus", repaymentCountStatus.A.val());//批次发送中
			boolean isRepaymentCount = repaymentCountService.save(repaymentMap);
			autoRecy4loanLogger.log(Level.INFO,DateUtil.getNowDate()+",回款统计表新增记录"+(isRepaymentCount?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,DateUtil.getNowDate()+",回款统计表新增记录异常");
		}
		
		try {
			SMSClient.sendSms("13377851306", DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"结算回款,发起者："+getRequestIP()+"...【易融恒信】");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// 发送回款结算通知给监管
		try {
			String start = DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"结算回款,密钥验证通过,开始自动结算...发起者："+getRequestIP()+"【易融恒信】";
//			SMSClient.sendSms("13071238735", start);
			SMSClient.sendSms("13100690680", start);
			SMSClient.sendSms("13377851306", start);	// 公司手机号
		} catch (Exception e8) {
			e8.printStackTrace();
			System.out.println("回款监控短信发送异常...");
		}
		
		autoRecy4loanLogger.log(Level.INFO,"开始扫描今天结算还款的借款标...");
		
		payUserMap.clear();//还款投资人用户缓存初始化
		paymentAuthPageStateMap.clear();//用户缴费授权开通状态缓存初始化
		
		//扫描当日逾期结算列表
		List<String> overdueLoanCodes = new ArrayList<String>();//当日逾期需结算loanCode
		List<String> loanOverdueLoanCodes = loanOverdueService.queryBydisposeStatusAndEndDate("n", DateUtil.getNowDate());
		for (String loanCode : loanOverdueLoanCodes) {
			if(overdueLoanCodes.indexOf(loanCode) == -1){
				overdueLoanCodes.add(loanCode);
			}
		}
		
		//扫描逾期不结算列表
		Map<String, loanOverdueType> loanOverdueNotMap = new HashMap<String,loanOverdueType>();//key:loanCode,value:loanOverdueType
		List<LoanOverdue> loanOverdueNots = loanOverdueService.queryLastRepayIndexByStatusAndTime("n", "");
		for (LoanOverdue loanOverdue : loanOverdueNots) {
			String loanCode = loanOverdue.getStr("loanCode");
			String overdueType = loanOverdue.getStr("overdueType");
			loanOverdueNotMap.put(loanCode, loanOverdueType.valueOf(overdueType));
		}
		
		//扫描标已结清,今日需补发逾期利息标
		int[] clearDays = loanInfoService.getClearDay(date);
		List<LoanInfo> queryEndOverdueLoan = loanInfoService.queryEndOverdueLoan(clearDays[0],clearDays[1]);
		
		List<SettlementEarly> settlementsEarlys = settlementEarlyService.findListByDate(date);//提前回款列表
		List<LoanInfo> settlements = loanInfoService.findJTHK(date);//正常回款列表
		
		//取消今日还款未转让成功的债权
		autoRecy4loanLogger.log(Level.INFO,"开始取消今日还款未转让成功的债权......");
		try {
			List<String> cancelTransferLoanCodes = new ArrayList<String>();//取消债转loanCodes
			//逾期还本金标
			for(String loanCode:overdueLoanCodes){
				if(!cancelTransferLoanCodes.contains(loanCode)){
					cancelTransferLoanCodes.add(loanCode);
				}
			}
			//补还逾期利息标
			for(LoanInfo loanInfo:queryEndOverdueLoan){
				String loanCode = loanInfo.getStr("loanCode");
				if(!cancelTransferLoanCodes.contains(loanCode)){
					cancelTransferLoanCodes.add(loanCode);
				}
			}
			//提前还款标
			for (SettlementEarly settlementEarly : settlementsEarlys) {
				String loanCode = settlementEarly.getStr("loanCode");
				if(!cancelTransferLoanCodes.contains(loanCode)){
					cancelTransferLoanCodes.add(loanCode);
				}
			}
			//正常还款标
			for (LoanInfo loanInfo:settlements) {
				String loanCode = loanInfo.getStr("loanCode");
				if(!cancelTransferLoanCodes.contains(loanCode)){
					cancelTransferLoanCodes.add(loanCode);
				}
			}
			//开始循环取消债转
			for (int i = 0; i < cancelTransferLoanCodes.size(); i++) {
				String loanCode = cancelTransferLoanCodes.get(i);
				autoRecy4loanLogger.log(Level.INFO,"开始取消债转loanCode:["+loanCode+"],取消进度("+(i+1)+"/"+cancelTransferLoanCodes.size()+")");
				doTransfer(loanCode, null);
			}
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"还款取消债权异常......");
			return error("", "还款取消债权异常", null);
		}
		
		//提前还款
		autoRecy4loanLogger.log(Level.INFO,"开始进行提前还款......");
		List<String> settlementLoanCodes = new ArrayList<String>();//提前还款loanCode
		for (int i = 0; i < settlementsEarlys.size(); i++) {
			SettlementEarly se = settlementsEarlys.get(i);
			String loanCode = se.getStr("loanCode");
			autoRecy4loanLogger.log(Level.INFO,"借款标["+loanCode+"]本期提前还款");
			try {
				LoanInfo loanInfo = loanInfoService.findById(loanCode);
				if(!loanInfo.getStr("loanState").equals("N")){
					autoRecy4loanLogger.log(Level.INFO,"借款标["+loanCode+"]状态非【还款中】，跳过提前还款设置...");
					continue;
				}
				int reciedCount = loanInfo.getInt("reciedCount");//已还期数
				
				settlementLoanCodes.add(loanCode);//防止正常还款重复提交还款
				
				boolean isOverdue = loanOverdueNotMap.containsKey(loanCode);//逾期不结算
				loanOverdueType overdueType = loanOverdueNotMap.get(loanCode) == null ? loanOverdueType.A:loanOverdueNotMap.get(loanCode);//逾期类型
				
				if(isOverdue){//逾期新增记录
					try {
						long sumLeftAmount = loanTraceService.sumLeftAmount(loanCode);
						boolean isLoanOverdue = addOverdue(loanInfo, sumLeftAmount, 0l, overdueType, "标["+loanInfo.getStr("loanNo")+"]第"+(reciedCount+1)+"期提前还款");
						autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期提前还款"+(isLoanOverdue?"成功":"失败"));
					} catch (Exception e) {
						autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期提前还款异常");
					}
				}
				
				//更新loanTrace
				List<LoanTrace> loanTraces = loanTraceService.findLoanTraceByJieSuan(loanCode);
				if(loanTraces.size() < 1){
					autoRecy4loanLogger.log(Level.INFO,"结算标loanTrace异常,loanCode:["+loanCode+"],loanTrace为空,跳过............");
					continue;
				}
				try {
					doLoanTrace(loanInfo, loanTraces,2,overdueType);//处理投标流水
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"结算标loanTrace异常,loanCode:["+loanCode+"],跳过............");
					continue;
				}
				
				//更新loanInfo
				try {
					doLoanInfo(loanInfo,2);//更新标相关事物
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"结算标loanInfo异常,loanCode:["+loanCode+"],跳过............");
					continue;
				}
				
				//更新settlementEarly
				try {
					settlementEarlyService.updateStatus(se,"C");
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"结算标settlementEarly异常,loanCode:["+loanCode+"],跳过............");
					continue;
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]提前还款异常........");
			}
		}
		
		//正常还款
		autoRecy4loanLogger.log(Level.INFO,"开始进行正常还款......");
		for (int i = 0; i < settlements.size(); i++) {
			LoanInfo loanInfo = settlements.get(i);
			String loanCode = loanInfo.getStr("loanCode");
			try {
				if(i >= num){
					break;
				}
				String productType = loanInfo.getStr("productType");
				if(!"E".equals(productType)){
					autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]非易分期跳过......");
					continue;
				}
				String loanTitle = loanInfo.getStr("loanTitle");
				autoRecy4loanLogger.log(Level.INFO,"开始结算标["+loanCode+"],("+loanTitle+")"+(i+1)+"/"+settlements.size());
				
				//该标已被提前还款处理
				if(settlementLoanCodes.indexOf(loanCode) != -1){
					autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]已被提前还款处理......");
					continue;
				}
				
				LoanInfo loan = LoanInfo.loanInfoDao.findFirst("select * from t_loan_info where loanCode=? and clearDate!=?", loanCode , DateUtil.getStrFromDate(new Date(),"MMdd")) ;
				if(loan == null){
					continue;
				}
				String loanState = loan.getStr("loanState");
				if(!"N".equals(loanState)){
					continue;
				}
//				if(isDate){
//					//判断阶段日期是否符合预期
//					int clearDay = loan.getInt("clearDay");
//					if( isCanClear(clearDay) == false ){
//						//当前是否符合结算日期，比如2月28号时，30号的结算日同样可以结算
//						continue;
//					}
//				}
				int reciedCount = loan.getInt("reciedCount");//已还期数
				
				boolean isOverdue = loanOverdueNotMap.containsKey(loanCode);//逾期不结算
				loanOverdueType overdueType = loanOverdueNotMap.get(loanCode) == null ? loanOverdueType.A:loanOverdueNotMap.get(loanCode);//逾期类型
				
				if(isOverdue){//逾期新增记录
					try {
						long sumNextAmount = loanTraceService.sumNextAmount(loanCode);
						boolean isLoanOverdue = addOverdue(loan, sumNextAmount, 0l, overdueType, "标["+loan.getStr("loanNo")+"]第"+(reciedCount+1)+"期正常还款");
						autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期正常还款"+(isLoanOverdue?"成功":"失败"));
					} catch (Exception e) {
						autoRecy4loanLogger.log(Level.INFO, "逾期表新增记录loanCode:"+loanCode+"第"+(reciedCount+1)+"期正常还款异常");
					}
				}
				
				//更新loanTrace
				List<LoanTrace> loanTraces = loanTraceService.findLoanTraceByJieSuan(loanCode);//需要结算的投标流水
				if(loanTraces.size() < 1){
					autoRecy4loanLogger.log(Level.INFO,"结算标loanTrace异常,loanCode:["+loanCode+"],loanTrace为空,跳过............");
					continue;
				}
				try {
					doLoanTrace(loan, loanTraces,1,overdueType);
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"结算标loanTrace异常,loanCode:["+loanCode+"],跳过............");
					continue;
				}
				
				//更新loanInfo
				try {
					boolean doResult = doLoanInfo(loan,1);//更新标相关事物
					autoRecy4loanLogger.log(Level.INFO,"开始结算标["+loanCode+"],("+loanTitle+")"+",更新借款标"+(doResult?"成功":"失败"));
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"结算标loanInfo异常,loanCode:["+loanCode+"],跳过............");
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]正常还款异常........");
			}
		}
		
		//扫描当日还款红包发放记录,更改还款计划表回款结果
//		autoRecy4loanLogger.log(Level.INFO,"开始更新还款计划表,红包还款期数状态..........");
//		try {
//			updateFailRepayment(date);
//		} catch (Exception e) {
//			autoRecy4loanLogger.log(Level.INFO,"更新还款记录表还款期数状态异常..........");
//		}
		
		//补发标期数结清后,还款日逾期利息
		autoRecy4loanLogger.log(Level.INFO,"开始补发标期数结清后,还款日逾期利息..........");
		try {
			reissueOverdueInterest(queryEndOverdueLoan);
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"补发标期数结清后,还款日逾期利息异常..........");
		}
		
		//逾期补发金额
		autoRecy4loanLogger.log(Level.INFO,"开始补发逾期本金..........");
		try {
			reissueOverdueAmount(overdueLoanCodes.subList(0, num>overdueLoanCodes.size()?overdueLoanCodes.size():num));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"逾期还款本金补还异常.......");
		}
		
		//标的还款信息更新完成,更新回款统计表
		autoRecy4loanLogger.log(Level.INFO,"开始更新回款统计表..........");
		try {
			boolean updateRepaymentCount = repaymentCountService.updateRepaymentCount(date,getRepaymentYesDate(date));
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表"+(updateRepaymentCount?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表异常..........");
		}
		
		payUserMap.clear();//还款投资人用户缓存释放
		paymentAuthPageStateMap.clear();//用户缴费授权开通状态缓存释放
		
		autoRecy4loanLogger.log(Level.INFO,"自动还款完成!");
		// 发送回款结算结束通知给监管
		try {
			String report = DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"结算回款,提前还款"+settlementsEarlys.size()+"笔,正常还款"+settlements.size()+"笔,结算完成-回款数据事务正式生效【易融恒信】";
//			SMSClient.sendSms("13071238735", report);
			SMSClient.sendSms("13100690680", report);
			SMSClient.sendSms("13377851306", report);	// 公司手机号
		} catch (Exception e7) {
			e7.printStackTrace();
			System.out.println("回款监控短信发送异常...");
		}
		
		return succ("00", "结算完成!");
	}
	
	/**
	 * 赏金计算 WJW
	 * @param loanInfo
	 * @param loanTrace
	 * @param isTransfer	true:有过债转,false:无
	 * @return
	 */
	private long rewardAmount(LoanInfo loanInfo,LoanTrace loanTrace,boolean isTransfer){
		String json_ticket = loanTrace.getStr("loanTicket");
		Long payAmount=loanTrace.getLong("payAmount");
		if(isTransfer){//发生过债权转让
			return 0L;
		}
		
		String code = "";//奖券code
		Integer rate = 0;//奖励利率
		if(!StringUtil.isBlank(json_ticket)){//使用了抵用券
			String type="";//抵用券类型
			Long amount=0L;//抵用券金额(A、B)
			JSONArray jsonArray = JSONArray.parseArray(json_ticket);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				type = jsonObject.getString("type");
				amount = jsonObject.getLong("amount");
				code = jsonObject.getString("code");
				rate = jsonObject.getInteger("rate");
			}
			
			if("C".equals(type) && !Tickets.rewardRateInterestTcode.equals(code)){//使用了加息券,加息额度(不包含会员自动加息)
				return 0L;
			}
			if("A".equals(type)||"B".equals(type)){//使用了现金抵用券
				payAmount=payAmount-amount;
			}
		}
		Integer loanTimeLimit=loanTrace.getInt("loanTimeLimit");
		Integer rateByYear=loanTrace.getInt("rateByYear");
		Integer rewardRateByYear=loanTrace.getInt("rewardRateByYear");
		Integer loanRecyCount=loanTrace.getInt("loanRecyCount");
		String refundType=loanTrace.getStr("refundType");
		String loanState = loanTrace.getStr("loanState");
		//提前还款返佣按当期计算
		if("P".equals(loanState)){
			int reciedCount = loanInfo.getInt("reciedCount");//已还期数
			loanRecyCount = loanTimeLimit - reciedCount-1;
		}
		if(Tickets.rewardRateInterestTcode.equals(code)){//使用了会员自动加息,不享受会员加息部分赏金 
			return (CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear+rewardRateByYear, loanTimeLimit-loanRecyCount, refundType)[1]*5/100)*(rateByYear+rewardRateByYear-rate)/(rateByYear+rewardRateByYear);
		}
		return CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear+rewardRateByYear, loanTimeLimit-loanRecyCount, refundType)[1]*5/100;
	}
	
	/**
	 * 会员等级更新之前，用户所投标利息管理费费率 WJW
	 * @param beforeVip	用户旧等级
	 * @return
	 */
	private int historyInterest(Integer beforeVip){
		switch (beforeVip) {
			case 1://少尉
				return 300;
			case 2://中尉
				return 300;
			case 3://上尉
				return 300;
			case 4://少校
				return 200;
			case 5://中校
				return 200;
			case 6://上校
				return 100;
			case 7://将军
				return 0;
		}
		return 0;
	}

	/**
	 * 单个标还款流水处理 WJW
	 * @param loan			还款标
	 * @param traces		投标流水
	 * @param repaymentType	还款类型 1:正常还款,2:提前还款
	 * @param overdueType 	逾期类型
	 * @throws Exception 
	 */
	private void doLoanTrace(LoanInfo loan , List<LoanTrace> traces , int repaymentType , loanOverdueType overdueType) throws Exception{
		boolean isPrincipal = loanOverdueType.A == overdueType || loanOverdueType.P == overdueType;//是否还本金
		boolean isInterest = loanOverdueType.A == overdueType || loanOverdueType.I == overdueType;//是否还利息
		
		String loanNo = loan.getStr("loanNo");//标号
		String loanCode = loan.getStr("loanCode");//标编码
		int reciedCount = loan.getInt("reciedCount");//已还期数
		int loanTimeLimit = loan.getInt("loanTimeLimit");//还款期数 
		String productType = loan.getStr("productType");//产品类型
		long sumOverdueInterest = 0;//逾期时利息和
		JSONArray jsonArray = new JSONArray();//批次还款明细
		for (LoanTrace loanTrace : traces) {
			String traceCode = loanTrace.getStr("traceCode");//投标编码
			String loanTitle = loanTrace.getStr("loanTitle");//标题
			String loanState = loanTrace.getStr("loanState");//标状态
			String loanUserCode = loanTrace.getStr("userCode");//借款人用户编码
			String loanUserName = loanTrace.getStr("loanUserName");//借款人用户名
			long payAmount = loanTrace.getLong("payAmount");//投标金额 
			long leftInterest = loanTrace.getLong("leftInterest");//剩余利息
			long nextInterest = loanTrace.getLong("nextInterest");//下一期利息
			long overdueInterest = loanTrace.getLong("overdueInterest");//逾期时利息
			String payUserCode = loanTrace.getStr("payUserCode");//投资人用户编码
			String payUserName = loanTrace.getStr("payUserName");//投资人用户名
			User user = getPayUser(payUserCode);//userService.findById(payUserCode);//投标人
			String forAccountId = user.getStr("jxAccountId");//投资人电子账号
			
			sumOverdueInterest += overdueInterest;

			//计算还款资金
			long[] repaymentAmount = repaymentType == 1 ? repaymentAmount(loan, loanTrace, user):prepaymentAmount(loan, loanTrace, user);
			
			//存管实际交易资金
			long txAmount = isPrincipal ? repaymentAmount[0]:0;//还款本金
			long intAmount = isInterest ? repaymentAmount[1]:0;//还款利息
			long txFeeIn = isInterest ? repaymentAmount[2]:0;//手续费
			if(!"E".equals(productType)){//非易分期利息为0
				intAmount = 0;
				txFeeIn = 0;
			}
			
			String authCode = loanTrace.getStr("authCode");//授权码
			boolean isTransfer = loanTransferService.vilidateIsTransfer(traceCode);//是否有过债转
			//发生过债转
			if(isTransfer){
				List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
				String transferAuthCode = loanTransfers.get(loanTransfers.size() - 1).getStr("authCode");//最后一债转authCode
				if(!StringUtil.isBlank(transferAuthCode)){
					authCode = transferAuthCode;
				}
			}
			
			//资金流向:1.未开户记账,2.红包还款,3.批次还款
			int recyStatus = 0;//还款状态 -1:失败,1:正常,2:未开户,3:批次,5:红包发放
			if(StringUtil.isBlank(forAccountId)){//未开户还款记账
				recyStatus = 2;//未开户
			}else if(StringUtil.isBlank(authCode)){//保存红包还款jxTrace
				if(txAmount > 0){
					jxTraceService.saveVoucherPayRequest(JXService.RED_ENVELOPES, txAmount, forAccountId, "1", "还款本金["+traceCode+"]","");
				}
				if(intAmount - txFeeIn > 0){
					jxTraceService.saveVoucherPayRequest(JXService.RED_ENVELOPES, intAmount - txFeeIn, forAccountId, "1", "还款利息["+traceCode+"]","");
				}
				recyStatus = 5;//红包
			}else if(overdueType != loanOverdueType.N){//构建批次还款明细
				jsonArray = jxTraceService.batchSubstRepayJson(jsonArray, loanCode, forAccountId, txAmount, intAmount, txFeeIn, authCode, getPaymentAuthPageState(forAccountId));
				recyStatus = 3;//批次
			}
			
			//更新平台funds,fundsTrace
			try {
				//更新本金
				if(txAmount > 0){
					if(recyStatus == 2 || recyStatus == 5){//红包返还本金或未开户,操作可用余额
						fundsServiceV2.doAvBalance4biz(payUserCode, txAmount , 0 ,  traceType.R , fundsType.J , 
								String.format("标[%s]第%d/%d期"+(repaymentType == 2 ? "提前还款":"")+"回收本金", loanNo , (reciedCount+1),loanTimeLimit));
					}else if(recyStatus == 3){//批次返还本金,操作冻结余额
						fundsServiceV2.doAvBalanceRepayment(payUserCode, txAmount , 0 ,  traceType.R , fundsType.J , 
								String.format("标[%s]第%d/%d期"+(repaymentType == 2 ? "提前还款":"")+"回收本金(冻结,待银行批次解冻)", loanNo , (reciedCount+1),loanTimeLimit));
					}
					
					if(recyStatus == 2){//未开户记录待还本金
						returnedAmountService.save(payUserCode, payUserName, txAmount, traceCode, 1, 2);
					}
				}
				
				//更新利息
				if(intAmount > 0){
					if(recyStatus == 2 || recyStatus == 5){//红包返还利息或未开户,操作可用余额
						fundsServiceV2.doAvBalance4biz(payUserCode, intAmount , txFeeIn ,  traceType.L , fundsType.J , 
								String.format("标[%s]第%d/%d期"+(repaymentType == 2 ? "提前还款":"")+"回收利息", loanNo , (reciedCount+1),loanTimeLimit));
					}else if(recyStatus == 3){//批次返还利息,操作冻结余额
						fundsServiceV2.doAvBalanceRepayment(payUserCode, intAmount , txFeeIn ,  traceType.L , fundsType.J , 
								String.format("标[%s]第%d/%d期"+(repaymentType == 2 ? "提前还款":"")+"回收利息(冻结,待银行批次解冻)", loanNo , (reciedCount+1),loanTimeLimit));
					}
					
					if(recyStatus == 2){//未开户记录待还利息
						returnedAmountService.save(payUserCode, payUserName, intAmount-txFeeIn, traceCode, 2, 2);
					}
					
					Db.update("update t_funds set recyMFee4loan = recyMFee4loan + ? where userCode = ?",txFeeIn,payUserCode);
				}
				
				//更新投资人待收
				long recyInterest = repaymentType == 1 ? nextInterest:leftInterest;//更新待收利息
				fundsServiceV2.updateBeRecyFunds(payUserCode, -1 , -txAmount , -recyInterest , txAmount, intAmount);
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"traceCode["+traceCode+"]投资人资金更新异常......");
				//throw new Exception("traceCode["+traceCode+"]投资人资金更新异常......");
			}
			
			//原始结算资金,用于更新平台债权相关流水
			long principal = repaymentAmount[0];//还款本金
			long interest = repaymentAmount[1];//还款利息
			long traceFee = repaymentAmount[2];//手续费
			if(!"E".equals(productType)){//非易分期利息为0
				interest = 0;
				traceFee = 0;
			}

			//更新loanTrace
			try {
				long[] nextFunds = repaymentType == 1 ? loanTraceSingle(loan, loanTrace,isPrincipal ? 0:principal):advLoanTraceSingle(loan, loanTrace,isPrincipal ? 0:principal);
				//新增还款历史记录
				historyRecyService.save(loanNo, loanCode, loanTitle, loanState, loanUserName, loanUserCode, payUserName, payUserCode, principal, interest, payAmount,nextFunds[0], nextFunds[1],reciedCount+1,loanTimeLimit);
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"traceCode["+traceCode+"]投标流水更新异常......");
				throw new Exception("traceCode["+traceCode+"]投标流水更新异常......");
			}
			
			//更新还款计划表
			try {
				if(recyStatus == -1){//还款失败
					if(repaymentType == 1){
						loanRepaymentService.repaymentFail(traceCode, reciedCount+1, principal, interest, traceFee, 0, 0, DateUtil.getNowDate());
					}else {
						loanRepaymentService.advRepaymentFail(traceCode, reciedCount+1, principal, interest, traceFee, 0, 0);
					}
				}else {//还款成功
					if(repaymentType == 1){
						loanRepaymentService.repayment(traceCode, reciedCount+1, principal, interest, traceFee, 0, 0, DateUtil.getNowDate());
					}else {
						loanRepaymentService.advRepayment(traceCode, reciedCount+1, principal, interest, traceFee, 0, 0);
					}
					
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"traceCode["+traceCode+"]还款计划表更新异常......");
				//throw new Exception("traceCode["+traceCode+"]还款计划表更新异常......");
			}

			//更新回款短信
			try {
				long msgAmount = txAmount + intAmount -txFeeIn;
				if(msgAmount > 0){
					String msgContent = payUserName+","+msgAmount+","+loanNo;
					new SMSTaskService(msgContent, "10", "回款", payUserCode).run();
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"traceCode["+traceCode+"]回款短信更新异常......");
			}
			
//			//好友返佣
//			if(DateUtil.compareDateByStr("yyyyMMdd", user.getStr("regDate"), "20180101") >= 0 && DateUtil.compareDateByStr("yyyyMMdd", user.getStr("regDate"), "20180918") <= 0 && overdueType == loanOverdueType.A){//判断投资人注册日期是否在活动时间内,且不逾期
//				try {
//					recommendReward(loan,loanTrace,isTransfer);
//				} catch (Exception e) {
//					autoRecy4loanLogger.log(Level.INFO,"traceCode["+traceCode+"]好友返佣异常......");
//				}
//			}
		}
		
		//保存回款批次jxTrace
		Map<String, Boolean> saveBatchSubstRepay = saveBatchSubstRepay(jsonArray,"");
		for (Map.Entry<String, Boolean> entry:saveBatchSubstRepay.entrySet()) {
			settlementPlanService.addJxTraceCode(loanCode, reciedCount+1, entry.getKey());//添加批次还款关联记录
		}
		
		//存在逾期时利息,若标逾期已处理,则清空逾期利息
		if(sumOverdueInterest > 0){
			try {
				List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", null);
				if(loanOverdues == null || loanOverdues.size() == 0){
					boolean updateOverdueInterest = loanTraceService.updateOverdueInterest(0, loanCode);//清零逾期时利息
					autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]逾期时利息清零"+(updateOverdueInterest ? "成功":"失败"));
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]逾期时利息清零异常......");
			}
		}
	}
	
	/**
	 * 批次交易更新响应报文 WJW
	 */
	@ActionKey("/notifyURL")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void notifyURL(){
		String bgData = getRequest().getParameter("bgData");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		renderText("success");//bgData接收成功,返回success至江西,停止骚扰
		
		Map<String, Object> responseMap = JSONObject.parseObject(bgData);
		String txDate = String.valueOf(responseMap.get("txDate"));//交易日期
		String txTime = String.valueOf(responseMap.get("txTime"));//交易时间
		String seqNo = String.valueOf(responseMap.get("seqNo"));//交易流水号
		String jxTraceCode = txDate + txTime + seqNo;//t_jx_trace流水号
		
		int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//jxTrace流水处理状态
		if(jxTraceState == 5 || jxTraceState == 6 || jxTraceState == 7){
			autoRecy4loanLogger.log(Level.INFO,"[处理批次还款流水jxTraceCode:"+jxTraceCode+"]响应报文更新已过时");
			return;
		}
		
		boolean updateResponseMessage = jxTraceService.updateResponseMessage(bgData, jxTraceCode);
		autoRecy4loanLogger.log(Level.INFO,"[处理批次还款流水jxTraceCode:"+jxTraceCode+"]响应报文更新"+(updateResponseMessage?"成功":"失败"));
		
	}
		
	/**
	 * 批次撤销 WJW
	 * 仅限当天批次
	 * @return
	 */
	@ActionKey("/batchCancel")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message batchCancel(){
		String batchNo = getPara("batchNo");
		JXTrace jxTrace = jxTraceService.findByBatchNoAndTxDate(DateUtil.getNowDate(), batchNo);
		String requestMessage = jxTrace.getStr("requestMessage");//请求报文
		JSONObject parseObject = JSONObject.parseObject(requestMessage);
		String txAmount = parseObject.getString("txAmount");
		String txCounts = parseObject.getString("txCounts");
		Map<String, String> batchCancel = JXController.batchCancel(Integer.valueOf(batchNo), StringUtil.getMoneyCent(txAmount), Integer.valueOf(txCounts));
		return succ("", batchCancel);
	}
	
	/**
	 * 发送批次结束债权请求 WJW
	 * 如果债权有过债转,则最初投资人与后续所有承接债权用户,都需与借款人进行该笔债权结清
	 * @return
	 */
	public void batchCreditEnd(List<String> loanCodes){
		JSONArray jsonArray = new JSONArray();//江西银行存管批次结束债权请求参数
		for(String loanCode:loanCodes){
			List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
			for (LoanTrace loanTrace : loanTraces) {
				String authCode = loanTrace.getStr("authCode");//授权码
				if(StringUtil.isBlank(authCode)){//无投标授权码,无债权结清
					continue;
				}
				String traceCode = loanTrace.getStr("traceCode"); 
				String payUserCode = loanTrace.getStr("payUserCode");//投标人code
				String loanUserCode = loanTrace.getStr("loanUserCode");//贷款人code
				User payUser = userService.findById(payUserCode);//投资人
				User loanUser = userService.findById(loanUserCode);//融资人
				String forAccountId = payUser.getStr("jxAccountId");//投资人账号
				if(StringUtil.isBlank(forAccountId)){//投资人电子账号为空,跳过结束债权
					continue;
				}
				
				String accountId = loanUser.getStr("jxAccountId");//融资人电子账号
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("accountId", accountId);
				jsonObject.put("forAccountId", forAccountId);
				jsonObject.put("productId", loanCode);
				jsonObject.put("authCode", authCode);
				jsonArray.add(jsonObject);
				
				//可能发生过债转,loanTrace的jxTraceCode若为空,则为批量迁移
				boolean vilidateIsTransfer = loanTransferService.vilidateIsTransfer(traceCode);//判断是否发生过债转
				if(vilidateIsTransfer && !StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
					List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
					for (LoanTransfer loanTransfer : loanTransfers) {
						String authCode1 = loanTransfer.getStr("authCode");
						if(!StringUtil.isBlank(authCode1)){
							JSONObject jsonObject1 = new JSONObject();
							jsonObject1.put("accountId", accountId);
							jsonObject1.put("forAccountId", forAccountId);
							jsonObject1.put("productId", loanCode);
							jsonObject1.put("authCode", authCode1);
							jsonArray.add(jsonObject1);
						}
					}
				}
			}
		}
		int batchNo = jxTraceService.batchNoByToday();//批次号
		Map<String, String> batchCreditEnd = JXController.batchCreditEnd(batchNo, jsonArray.toJSONString());
		boolean isReceived = "success".equals(batchCreditEnd.get("received"));
		autoRecy4loanLogger.log(Level.INFO,"[批次号:"+batchNo+"批次结束债权请求发送"+(isReceived?"成功":"失败")+"...]");
	}
	
//	/**
//	 * 处理批次结束债权流水 WJW
//	 * batchNo:人工处理批次号
//	 * txDate:人工处理批次日期(未赋值默认当日)
//	 */
//	@ActionKey("/batchCreditEndCallback")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public void batchCreditEndCallback(){
//		String bgData = getRequest().getParameter("bgData");
//		String batchNo = "";//批次号
//		String txDate = "";//交易日期
//		String jxTraceCode = "";//t_jx_trace流水号
//		if(StringUtil.isBlank(bgData)){//bgData为空(适用即信未返回异步信息,本地手动处理批次流水)
//			batchNo = getPara("batchNo");
//			txDate = getPara("txDate",DateUtil.getNowDate());
//			if(StringUtil.isBlank(batchNo)){//人工操作未输入批次号或即信返回bgData为空
//				autoRecy4loanLogger.log(Level.INFO, "[处理批次结束债权流水,人工操作未输入批次号或即信返回bgData为空]");
//				return;
//			}
//			JXTrace jxTrace = jxTraceService.findByBatchNoAndTxDate(txDate, batchNo);
//			jxTraceCode = jxTrace.getStr("jxTraceCode");
//			
//			if(isLock(jxTraceCode)){//同一回调正在处理
//				autoRecy4loanLogger.log(Level.INFO, "[处理批次代偿还款流水,["+jxTraceCode+"]正在处理中,_LOCK_JxTrace锁定生效]");
//				return;
//			}
//			lock(jxTraceCode);//加锁
//			
//		}else {//正常处理即信流水
//			Map<String, Object> responseMap = JSONObject.parseObject(bgData);
//			txDate = String.valueOf(responseMap.get("txDate"));//交易日期
//			String txTime = String.valueOf(responseMap.get("txTime"));//交易时间
//			String seqNo = String.valueOf(responseMap.get("seqNo"));//交易流水号
//			jxTraceCode = txDate + txTime + seqNo;//t_jx_trace流水号
//			
//			if(isLock(jxTraceCode)){//同一回调正在处理
//				autoRecy4loanLogger.log(Level.INFO, "[处理批次代偿还款流水,["+jxTraceCode+"]正在处理中,_LOCK_JxTrace锁定生效]");
//				return;
//			}
//			lock(jxTraceCode);//加锁
//			
//			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//jxTrace流水处理状态
//			if(jxTraceState != 1 && jxTraceState != 2 && jxTraceState !=3){
//				unlock(jxTraceCode);//回调已处理,解锁
//				return;
//			}
//		}
//		
//		boolean result = false;
//		if(StringUtil.isBlank(bgData)){//人工处理,仅更新responseMessage
//			result = jxTraceService.updateResponseMessage("admin", jxTraceCode);
//		}else {
//			Map<String, String> map = net.sf.json.JSONObject.fromObject(bgData);
//			// 生成本地报文流水号
//			String jxCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
//			// 将响应报文存入数据库
//			result = JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
//		}
//		autoRecy4loanLogger.log(Level.INFO,"[处理批次结束债权jxTraceCode:"+jxTraceCode+"]响应报文更新"+(result?"成功":"失败"));
//		
//		renderText("success");//bgData接收成功,返回success至江西,停止骚扰
//		unlock(jxTraceCode);//回调处理完毕,解锁
//	}
	
	/**
	 * 失败批次结束债权流水打包重新发送请求 WJW
	 * @return
	 */
	@ActionKey("/batchCreditEndFailure")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message batchCreditEndFailure(){
		String txDate = getPara("txDate",DateUtil.getNowDate());//交易日期
		List<JXTrace> jxTraces = jxTraceService.queryByTxCodeAndTxDate("batchCreditEnd", txDate);//根据日期查询江西批次结束债权交易流水
		
		JSONArray jsonArray = new JSONArray();//江西银行存管失败批次结束债权参数(可能包含成功)
		JSONArray jsonArrayReq = new JSONArray();//江西银行存管失败批次结束债权请求参数(全失败)
		List<String> authCodes = new ArrayList<String>();//防止重复处理authCode集
		List<String> authCodeSucs = new ArrayList<String>();//处理成功authCode集
		List<String> authCodeErrs = new ArrayList<String>();//处理失败authCode集
		List<String> authCodeIngs = new ArrayList<String>();//处理中authCode集
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//流水处理状态
			if(jxTraceState == 6){//全部成功不处理
				continue;
			}
			
			String requestMessage = jxTrace.getStr("requestMessage");//请求报文
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");//批次号
			
			Map<String, Object> batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");//批次结束债权处理明细
			if(batchDetailsQueryAll == null){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次结束债权流水jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
				continue;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次结束债权流水jxTraceCode:"+jxTraceCode+"]"+retMsg);
				continue;
			}
			List<Map<String, String>> subPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			
			//查询authCode目前处理状态,防止authCode后续错误处理
			for (int i = 0; i < subPacks.size(); i++) {
				Map<String, String> subPack = subPacks.get(i);//单笔记录
				String txState = subPack.get("txState");//交易状态(S-成功,F-失败,A-待处理,D-正在处理,C-撤销)
				String authCode = subPack.get("authCode");//授权码
				
				if("S".equals(txState)){//处理成功
					if(authCodeSucs.indexOf(authCode) == -1){
						authCodeSucs.add(authCode);
					}
				}
				if("F".equals(txState)){//处理失败
					if(authCodeErrs.indexOf(authCode) == -1){
						authCodeErrs.add(authCode);
					}
				}
				if("A".equals(txState) || "D".equals(txState)){//处理中
					if(authCodeIngs.indexOf(authCode) == -1){
						authCodeIngs.add(authCode);
					}
				}
			}
					
			//将批次结束债权中处理失败的流水重新打包批次发送
			String subPacks1 = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks1);
			for (int j = 0; j < parseArray.size(); j++) {
				JSONObject jsonObject = parseArray.getJSONObject(j);
				String authCode = jsonObject.getString("authCode");
				
				if(authCodeErrs.indexOf(authCode) != -1){
					//防止重复添加失败流水
					if(authCodes.indexOf(authCode) == -1){
						authCodes.add(authCode);
					}else {
						continue;
					}
					
					//失败还款流水重新打包
					String accountId = jsonObject.getString("accountId");//融资人电子账号（或者标的登记的担保户）
					String forAccountId = jsonObject.getString("forAccountId");//投资人账号
					String productId = jsonObject.getString("productId");//标号
					
					JSONObject jsonObject2 = new JSONObject();
					jsonObject2.put("accountId", accountId);//融资人电子账号
					jsonObject2.put("forAccountId", forAccountId);//投资人账号
					jsonObject2.put("productId", productId);//标号
					jsonObject2.put("authCode", authCode);
					jsonArray.add(jsonObject2);
				}
			}
		}
		
		//将重新打包批次中，剔除已成功流水或正在处理中，向jsonArrayReq写入全失败流水
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String authCode = jsonObject.getString("authCode");
			
			if(authCodeSucs.indexOf(authCode) == -1 && authCodeIngs.indexOf(authCode) == -1){
				jsonArrayReq.add(jsonObject);
			}
		}
		
		if(jsonArrayReq.size() < 1){
			return error("03", "未扫描到失败批次结束债权流水", "");
		}
		
		int batchNoReq = jxTraceService.batchNoByToday();//批次号
		Map<String, String> batchCreditEnd = JXController.batchCreditEnd(batchNoReq, jsonArrayReq.toJSONString());
		boolean isReceived = "success".equals(batchCreditEnd.get("received"));//处理结果
		autoRecy4loanLogger.log(Level.INFO,"[批次号:"+batchNoReq+"批次结束债权请求发送"+(isReceived?"成功":"失败")+"...]");
		return succ("批次结束债权请求发送"+(isReceived?"成功":"失败"), "batchNo:"+batchNoReq);
	}
	
	/**
	 * 失败批次代偿还款流水打包重新发送请求 WJW
	 * @return
	 */
	@ActionKey("/compensatoryFailureReq")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message compensatoryFailureReq(){
		String txDate = getPara("txDate",DateUtil.getNowDate());//交易日期
		List<JXTrace> jxTraces = jxTraceService.queryByTxCodeAndTxDate("batchSubstRepay", txDate);//根据日期查询江西批次还款交易流水
		System.out.println("jxTraces.size():"+jxTraces.size());
		JSONArray jsonArray = new JSONArray();//江西银行存管失败批次还款参数(可能包含成功)
		JSONArray jsonArrayReq = new JSONArray();//江西银行存管失败批次还款请求参数(全失败)
		List<String> authCodes = new ArrayList<String>();//防止重复处理authCode集
		List<String> authCodeSucs = new ArrayList<String>();//处理成功authCode集
		List<String> authCodeErrs = new ArrayList<String>();//处理失败authCode集
		List<String> authCodeIngs = new ArrayList<String>();//处理中authCode集
		
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//流水处理状态
			if(jxTraceState == 6){//全部成功不处理
				continue;
			}
			
			String requestMessage = jxTrace.getStr("requestMessage");//请求报文
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");//批次号
			
			Map<String, Object> batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");//批次还款存管处理明细
			if(batchDetailsQueryAll == null){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款流水jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
				continue;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款流水jxTraceCode:"+jxTraceCode+"]"+retMsg);
				continue;
			}
			List<Map<String, String>> subPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			
			//查询authCode目前处理状态,防止authCode后续错误处理
			for (int i = 0; i < subPacks.size(); i++) {
				Map<String, String> subPack = subPacks.get(i);//还款单笔记录
				String txState = subPack.get("txState");//交易状态(S-成功,F-失败,A-待处理,D-正在处理,C-撤销)
				String authCode = subPack.get("authCode");//授权码
				
				if("S".equals(txState)){//处理成功
					if(authCodeSucs.indexOf(authCode) == -1){
						authCodeSucs.add(authCode);
					}
				}
				if("F".equals(txState)){//处理失败
					if(authCodeErrs.indexOf(authCode) == -1){
						authCodeErrs.add(authCode);
					}
				}
				if("A".equals(txState) || "D".equals(txState)){//处理中
					if(authCodeIngs.indexOf(authCode) == -1){
						authCodeIngs.add(authCode);
					}
				}
			}
					
			//将批次还款中处理失败的流水重新打包批次发送
			String subPacks1 = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks1);
			for (int j = 0; j < parseArray.size(); j++) {
				JSONObject jsonObject = parseArray.getJSONObject(j);
				String authCode = jsonObject.getString("authCode");
				
				if(authCodeErrs.indexOf(authCode) != -1){
					//防止重复添加失败流水
					if(authCodes.indexOf(authCode) == -1){
						authCodes.add(authCode);
					}else {
						continue;
					}
					
					//失败还款流水重新打包
					String accountId = jsonObject.getString("accountId");//融资人电子账号（或者标的登记的担保户）
					String forAccountId = jsonObject.getString("forAccountId");//投资人账号
					String productId = jsonObject.getString("productId");//标号
					long txAmount = StringUtil.getMoneyCent(jsonObject.getString("txAmount"));
					long intAmount = StringUtil.getMoneyCent(jsonObject.getString("intAmount"));
					
					JSONObject jsonObjectReq = new JSONObject();
					if(!StringUtil.isBlank(jsonObject.getString("txFeeIn"))){
						long txFeeIn = StringUtil.getMoneyCent(jsonObject.getString("txFeeIn"));
						jsonObjectReq.put("txFeeIn", txFeeIn);
					}
					jsonObjectReq.put("accountId", accountId);
					jsonObjectReq.put("txAmount", txAmount);
					jsonObjectReq.put("intAmount", intAmount);
					jsonObjectReq.put("forAccountId", forAccountId);
					jsonObjectReq.put("productId", productId);
					jsonObjectReq.put("authCode", authCode);
					jsonArray.add(jsonObjectReq);
				}
			}
		}
		
		System.out.println("jsonArray:"+jsonArray.toString());
		System.out.println("jsonArrayReq:"+jsonArrayReq.toString());
		System.out.println("authCodes:"+authCodes.toString());
		System.out.println("authCodeSucs:"+authCodeSucs.toString());
		System.out.println("authCodeErrs:"+authCodeErrs.toString());
		System.out.println("authCodeIngs:"+authCodeIngs.toString());
		
		//将重新打包批次中，剔除已成功流水或正在处理中，向jsonArrayReq写入全失败流水
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String authCode = jsonObject.getString("authCode");
			if(authCodeSucs.indexOf(authCode) == -1 && authCodeIngs.indexOf(authCode) == -1){
				jsonArrayReq.add(jsonObject);
			}
		}
		
		//保存还款批次
		Map<String, Boolean> saveBatchSubstRepayMap = saveBatchSubstRepay(jsonArrayReq,"");
		return succ("批次还款请求保存完成", "jxTraceCode:"+saveBatchSubstRepayMap.toString());
	}
	
	/**
	 * 当日通道异常批次代偿补发 WJW
	 * @return
	 */
	@ActionKey("/batchSubstRepayResend")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message batchSubstRepayResend(){
		String txDate = getPara("txDate",DateUtil.getNowDate());
		if(StringUtil.isBlank(txDate) || txDate.length() != 8 || !StringUtil.isNumeric(txDate)){
			return error("01", "看,日期输错了", null);
		}
		List<JXTrace> jxTraces = jxTraceService.queryTPSUntreatedBatch(txDate);//TPS或交易开通验证异常批次未处理代偿还款
		List<JXTrace> queryResponseMessageNull = jxTraceService.queryResponseMessageNull(txDate);//发送批次未接收到回调
		if(queryResponseMessageNull.size() > 0){
			for (JXTrace jxTrace : queryResponseMessageNull) {
				String batchTxDate = jxTrace.getStr("txDate");
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				String batchNo = parseObject.getString("batchNo");
				Map<String, String> batchQuery = JXQueryController.batchQuery(batchTxDate, batchNo);
				String retCode = batchQuery.get("retCode");
				if("JX900612".equals(retCode)){//JX900612查询交易批次不存在
					jxTraces.add(jxTrace);
				}
			}
		}
		Map<String, Boolean> TPSMap = new HashMap<String,Boolean>();//补发通道异常批次记录
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String remark = jxTrace.getStr("remark");
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String subPacks = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks);
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < parseArray.size(); i++) {
				JSONObject jsonObject = parseArray.getJSONObject(i);
				String accountId = jsonObject.getString("accountId");//融资人电子账号（或者标的登记的担保户）
				String forAccountId = jsonObject.getString("forAccountId");//投资人账号
				String productId = jsonObject.getString("productId");//标号
				String authCode = jsonObject.getString("authCode");//授权号
				String txFeeIn = jsonObject.getString("txFeeIn");
				
				JSONObject jsonObjectReq = new JSONObject();
				jsonObjectReq.put("accountId", accountId);
				jsonObjectReq.put("txAmount", StringUtil.getMoneyCent(jsonObject.getString("txAmount")));
				jsonObjectReq.put("intAmount", StringUtil.getMoneyCent(jsonObject.getString("intAmount")));
				jsonObject.put("txFeeIn", StringUtil.isBlank(txFeeIn) ? 0:StringUtil.getMoneyCent(txFeeIn));
				jsonObjectReq.put("forAccountId", forAccountId);
				jsonObjectReq.put("productId", productId);
				jsonObjectReq.put("authCode", authCode);
				jsonArray.add(jsonObjectReq);
			}
			
			Map<String, Boolean> saveBatchSubstRepay = saveBatchSubstRepay(jsonArray, remark);
			for(Map.Entry<String, Boolean> entry:saveBatchSubstRepay.entrySet()){
				TPSMap.put(entry.getKey(), entry.getValue());
			}
			jxTraceService.updateRemark("y", jxTraceCode);//更改原失败批次处理状态
			
		}
		autoRecy4loanLogger.log(Level.INFO,txDate+" 通道异常批次代偿补发完成"+TPSMap.toString());
		
		//更新回款统计表批次信息
		autoRecy4loanLogger.log(Level.INFO,"补发批次,开始更新回款统计表批次信息.......");
		try {
			boolean updateRepaymentCountBatch = repaymentCountService.updateRepaymentCountBatch(getRepaymentDate(txDate), txDate);
			autoRecy4loanLogger.log(Level.INFO,"补发批次,更新回款统计表批次信息:"+(updateRepaymentCountBatch?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"补发批次,更新回款统计表批次信息异常");
		}
		return succ("", "通道异常批次代偿补发完成"+TPSMap.toString());
	}
	/**
	 * 正常还款资金计算 WJW
	 * @param loan			还款标
	 * @param loanTrace		还款单条流水
	 * @return	0:还款本金,1:还款利息,2:投资人手续费
	 */
	private long[] repaymentAmount(LoanInfo loan,LoanTrace loanTrace,User user){
		long payAmount = loanTrace.getLong("payAmount");//投标金额 
		String trace_refundType = loanTrace.get("refundType","O");//投标还款方式 
		long txAmount = loanTrace.getLong("nextAmount");//还款本金
		long intAmount = loanTrace.getLong("nextInterest");//还款利息
		long overdueInterest = loanTrace.getLong("overdueInterest");//逾期时利息
		long txFeeIn = 0;//手续费
		
		//11.11重新计算利息
		int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
		if(releaseDate>=20171111&&releaseDate<=20171117){
			if(0==loan.getInt("reciedCount")){//若第一期
				int loanrateyear=loan.getInt("rateByYear");
				int benefits4new=loan.getInt("benefits4new");
				if(0==benefits4new){//新手标不可用
					loanrateyear+=400;
					intAmount=CommonUtil.f_004(payAmount, loanrateyear, 1, trace_refundType)[1];
				}
			}
		}
		
		intAmount = overdueInterest > 0 ? overdueInterest:intAmount;//存在逾期时利息,则取逾期时利息进行还款

		if( intAmount > 0 ){
			String effectDate = loan.getStr("effectDate");//满标日期
			txFeeIn = calculationTraceFee(user, effectDate, intAmount);//计算手续费
		}
		return new long[]{txAmount,intAmount,txFeeIn};
	}
	 
	/**
	 * 提前还款资金计算 WJW
	 * @param loan			提前还款标
	 * @param loanTrace		提前还款单条流水
	 * @return	0:还款本金,1:还款利息,2:投资人手续费
	 */
	private long[] prepaymentAmount(LoanInfo loan,LoanTrace loanTrace,User user){
		long payAmount = loanTrace.getLong("payAmount");//投标金额 
		String trace_refundType = loanTrace.get("refundType","O");//投标还款方式 
		int trace_rateByYear = loanTrace.getInt("rateByYear");//投标年利率 
		int rewardRateByYear = loanTrace.getInt("rewardRateByYear");//奖励年利率 
		trace_rateByYear = trace_rateByYear + rewardRateByYear;
		long interest = loanTrace.getLong("nextInterest");	//只需要利息
		long overdueInterest = loanTrace.getLong("overdueInterest");//逾期时利息
		long traceFee = 0 ;
		
		//11.11提前还款重新计算利息
		int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
		if(releaseDate>=20171111&&releaseDate<=20171117){
			if(0==loan.getInt("reciedCount")){//若第一期
				int loanrateyear=loan.getInt("rateByYear");
				int benefits4new=loan.getInt("benefits4new");
				if(0==benefits4new){//新手标不可用
					loanrateyear+=400;
					interest=CommonUtil.f_004(payAmount, loanrateyear, 1, trace_refundType)[1];
				}
			}
		}
		
		interest = overdueInterest > 0 ? overdueInterest : interest;//存在逾期时利息,则取逾期时利息进行还款
		
		if( interest > 0 ){
			String effectDate = loan.getStr("effectDate");//满标日期
			traceFee = calculationTraceFee(user, effectDate, interest);//计算手续费
		}
		
		long txAmount = loanTrace.getLong("leftAmount");//还款本金
		long intAmount = interest;//还款利息
		long txFeeIn = traceFee;//投资人手续费
		
		return new long[]{txAmount,intAmount,txFeeIn};
	}

	/**
	 * 处理本地单笔正常还款流水 WJW
	 * @param loan			还款标
	 * @param trace			单条投标记录
	 * @param overdueAmount 逾期本金
	 * @return				long[0]:下一期本金     long[1]:下一期利息
	 */
	private long[] loanTraceSingle(LoanInfo loan,LoanTrace trace,long overdueAmount){
			int loanTimeLimit = loan.getInt("loanTimeLimit");//还款期数 
			int reciedCount = loan.getInt("reciedCount");//已还期数
			long payAmount = trace.getLong("payAmount");//投标金额 
			String trace_refundType = trace.get("refundType","O");//投标还款方式 
			int trace_rateByYear = trace.getInt("rateByYear");//投标年利率 
			int rewardRateByYear = trace.getInt("rewardRateByYear");//奖励年利率 
			long recyPrincipal = trace.getLong("nextAmount");
			long recyInterest = trace.getLong("nextInterest");

			long nb = 0;
			long nx = 0;
			int loanRecyCount = trace.getInt("loanRecyCount") - 1;
			trace.set("loanRecyCount", loanRecyCount) ;
			if( loanRecyCount == 0 ){
				
				trace.set("nextAmount",0 );
				trace.set("nextInterest",0 );
				trace.set("leftAmount" , 0 );
				trace.set("leftInterest" , 0 );
				trace.set("loanState", loanState.O.val() ) ;
				trace.set("traceState", traceState.B.val() ) ;
				trace.set("loanRecyDate", DateUtil.getNowDate()) ;
			}else{
				//计算下一期待还本金和利息
				long nowLeftAmount = trace.getLong("leftAmount") ;
				long nowLeftInterest = trace.getLong("leftInterest") ;
				long nextAmount = 0 ;
				long nextInterest = 0 ;
				if( loanRecyCount == 1 ){
					//最后一期
					nextAmount = nowLeftAmount - recyPrincipal;
					nextInterest = nowLeftInterest - recyInterest;
				}else{
					long[] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit ,trace_rateByYear+rewardRateByYear , (reciedCount+2) , trace_refundType ) ;
					nextAmount = nextBenXi[0] ;
					nextInterest = nextBenXi[1] ;
				}
				//查询是否转让过
				List<LoanTransfer> isTransfer =  loanTransferService.queryLoanTransferByTraceCode(trace.getStr("traceCode") , "B");
				if(isTransfer.size()>0){
					if("A".equals(trace_refundType)){
						//判断是否使用加息券
						String type="";
						String json_tickets = trace.getStr("loanTicket");
						if(StringUtil.isBlank(json_tickets)==false){
							JSONArray ja = JSONArray.parseArray(json_tickets);
							for (int j = 0; j < ja.size(); j++) {
								JSONObject jsonObj = ja.getJSONObject(j);
								type=jsonObj.getString("type");
							}
						}
						if("C".equals(type)){
						//取最早的一条债转记录
						LoanTransfer transfer=isTransfer.get(0);
						int leftRecyCount = transfer.getInt("loanRecyCount");
						long leftpayamount=transfer.getInt("leftAmount");
						int a=loanTimeLimit-leftRecyCount;//债转时已还期数
						long[] nextBenXi = CommonUtil.f_000(leftpayamount, leftRecyCount ,trace_rateByYear+rewardRateByYear , (reciedCount+2-a) , trace_refundType ) ;
						nextAmount = nextBenXi[0] ;
						nextInterest = nextBenXi[1] ;
						}
					}
				}
				
				//更新代还本金和利息  by five 2015-12-4
				nb = nextAmount;
				nx = nextInterest;
				trace.set("nextAmount",nextAmount );
				trace.set("nextInterest",nextInterest );
				trace.set("leftAmount" , nowLeftAmount - recyPrincipal);
				trace.set("leftInterest" , nowLeftInterest - recyInterest );
				trace.set("loanRecyDate", CommonUtil.anyRepaymentDate4string(loan.getStr("effectDate"),reciedCount+1+1)) ;
				
				//兼容4个流转标差期数
				if(nextAmount<1 && nextInterest < 1){
					trace.set("nextAmount",0 );
					trace.set("nextInterest",0 );
					trace.set("leftAmount" , 0 );
					trace.set("leftInterest" , 0 );
					trace.set("loanState", loanState.O.val() ) ;
					trace.set("traceState", traceState.B.val() ) ;
					trace.set("loanRecyDate", DateUtil.getNowDate()) ;
				}
			}
			
			trace.set("overdueAmount", trace.getLong("overdueAmount")+overdueAmount);//设置逾期金额
			trace.update();
			return new long[]{nb,nx};
	}

	/**
	 * 处理本地单笔提前还款流水 WJW
	 * @param 	loan			还款标
	 * @param 	trace			单条投标记录
	 * @param 	overdueAmount 	逾期本金
	 * @return					long[0]:下一期本金     long[1]:下一期利息
	 */
	private long[] advLoanTraceSingle(LoanInfo loan,LoanTrace trace,long overdueAmount){
			int loanTimeLimit = loan.getInt("loanTimeLimit");//还款期数 
			int reciedCount = loan.getInt("reciedCount");	//已还期数
			long x1 = trace.getLong("leftInterest");
			long x2 = trace.getLong("nextInterest");
			int loanRecyCount = trace.getInt("loanRecyCount") - (loanTimeLimit-reciedCount);
			trace.set("loanRecyCount", loanRecyCount) ;
			trace.set("loanState", loanState.P.val() ) ;
			trace.set("traceState", traceState.B.val() ) ;
			trace.set("loanRecyDate", DateUtil.getNowDate()) ;
			trace.set("nextAmount",0 );
			trace.set("nextInterest",0 );
			trace.set("leftAmount" , 0 );
			trace.set("leftInterest" , x1-x2 );
			trace.set("overdueAmount", trace.getLong("overdueAmount")+overdueAmount);//设置逾期金额
			trace.update() ;
			return new long[]{0,0};
	}

	/**
	 * 解冻批次代偿还款资金 WJW
	 */
	@ActionKey("/unfreezeBatchSubstRepay")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public synchronized Message unfreezeBatchSubstRepay(){
		int num = getParaToInt("num",10000);//解冻jxTrace条数限制
		String txDate = getPara("txDate",DateUtil.getNowDate());//批次还款发送日期
		if(StringUtil.isBlank(txDate) || txDate.length() != 8 || !StringUtil.isNumeric(txDate)){
			return error("01", "看,日期输错了", null);
		}
		List<JXTrace> jxTraces = jxTraceService.queryUnthawingBatchByTxDate(txDate);//资金未解冻批次代偿还款记录
		Map<String, String> loanCodeMap = new HashMap<String,String>();//key:loanCode,value:fundsRemark
		for (int i = 0; i < jxTraces.size(); i++) {
			if(num <= i){
				break;
			}
			autoRecy4loanLogger.log(Level.INFO, "批次还款资金解冻 "+(i+1)+"/"+jxTraces.size());
			JXTrace jxTrace = jxTraces.get(i);
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String remark = jxTrace.getStr("remark");
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");//批次号
			
			//查询批次处理状态
			Map<String, String> batchQuery = null;
			try {
				batchQuery = JXQueryController.batchQuery(txDate, batchNo);
				if(!RetCodeUtil.isSuccRetCode(batchQuery.get("retCode")) || !"S".equals(batchQuery.get("batchState"))){//批次未处理完
					continue;
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
				continue;
			}
			
			//查询批次处理明细
			Map<String, Object> batchDetailsQueryAll = null;
			try {
				batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]批次明细查询失败batchNo:"+batchNo);
				continue;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]"+retMsg);
				continue;
			}
			List<Map<String, String>> subPacklst = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			if(subPacklst == null || subPacklst.size() < 1){
				autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]subPacks查询为空");
				continue;
			}
			
			Map<String, Boolean> isAuthCodeMap = new HashMap<String,Boolean>();//key:authCode,value:boolean
			boolean isTxState = false;//该批次状态
			for (int j = 0; j < subPacklst.size(); j++) {
				Map<String, String> subPack = subPacklst.get(j);//还款单笔记录
				String txState = subPack.get("txState");//交易状态(S-成功,F-失败,A-待处理,D-正在处理,C-撤销)
				String authCode = subPack.get("authCode");//授权码
				if("S".equals(txState)){//成功
					isAuthCodeMap.put(authCode, true);
				}else if("F".equals(txState)){//失败
					isAuthCodeMap.put(authCode, false);
				}else {//A-待处理,D-正在处理,C-撤销
					isTxState = true;
					break;
				}
			}
			
			if(isTxState){//批次未处理完或已撤销,不更新funds
				continue;
			}
			
			String subPacks = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks);
			for (int j = 0; j < parseArray.size(); j++) {
				JSONObject jsonObject = parseArray.getJSONObject(j);
				String authCode = jsonObject.getString("authCode");
				if(StringUtil.isBlank(authCode)){
					autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]authCode为空");
					continue;
				}
				String forAccountId = jsonObject.getString("forAccountId");//投资人电子账号
				if(StringUtil.isBlank(forAccountId)){
					autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]forAccountId为空");
					continue;
				}
				User user = userService.findByJXAccountId(forAccountId);
				if (user == null) {
					autoRecy4loanLogger.log(Level.INFO, "[处理批次还款资金解冻jxTraceCode:"+jxTraceCode+"]forAccountId:"+forAccountId+"不存在");
					continue;
				}
				long txAmount = StringUtil.getMoneyCent(jsonObject.getString("txAmount"));//本金
				long intAmount = StringUtil.getMoneyCent(jsonObject.getString("intAmount"));//利息
				long txFeeIn = 0;//手续费
				if(!StringUtil.isBlank(jsonObject.getString("txFeeIn"))){
					txFeeIn = StringUtil.getMoneyCent(jsonObject.getString("txFeeIn"));
				}
				String loanCode = jsonObject.getString("productId");//标号
				LoanInfo loanInfo = loanInfoService.findById(loanCode);
				String loanNo = loanInfo.getStr("loanNo");
				
				//逾期本金还款更新解冻资金流水
				if("overdue".equals(remark)){
					if(isAuthCodeMap.containsKey(authCode) && isAuthCodeMap.get(authCode)){//交易成功处理流水
						if(txAmount > 0){
							try{
								fundsServiceV2.frozeBalance(user.getStr("userCode"), txAmount, 0, traceType.Y, fundsType.J, "标["+loanNo+"]逾期本金解冻");
							} catch (Exception e) {
								autoRecy4loanLogger.log(Level.INFO, "[处理批次逾期还款本金解冻:["+user.getStr("userName")+"]账户冻结余额转可用余额异常，冻结余额不足或其他原因");
							}
						}
					}
					continue;
				}
				
				//逾期利息还款更新解冻资金流水
				if("overdueInterest".equals(remark)){
					if(isAuthCodeMap.containsKey(authCode) && isAuthCodeMap.get(authCode)){//交易成功处理流水
						if(intAmount > 0){
							try{
								fundsServiceV2.frozeBalance(user.getStr("userCode"), intAmount, txFeeIn, traceType.Y, fundsType.J, "逾期利息解冻");
							} catch (Exception e) {
								autoRecy4loanLogger.log(Level.INFO, "[处理批次逾期还款利息解冻:["+user.getStr("userName")+"]账户冻结余额转可用余额异常，冻结余额不足或其他原因");
							}
						}
					}
					continue;
				}
				
				String fundsRemark = "";//资金解冻备注
				if(loanCodeMap.containsKey(loanCode)){
					fundsRemark = loanCodeMap.get(loanCode);
				}else {
					int reciedCount = loanInfo.getInt("reciedCount");
					int loanTimeLimit = loanInfo.getInt("loanTimeLimit");
					String loanState = loanInfo.getStr("loanState");
					if("P".equals(loanState)){
						fundsRemark = "标["+loanNo+"]第"+reciedCount+"/"+loanTimeLimit+"期提前还款";
					}else {
						fundsRemark = "标["+loanNo+"]第"+reciedCount+"/"+loanTimeLimit+"期还款";
					}
					loanCodeMap.put(loanCode, fundsRemark);
				}
				
				//批次明细银行处理成功,冻结余额划入可用余额
				if(isAuthCodeMap.containsKey(authCode) && isAuthCodeMap.get(authCode)){
					if(txAmount > 0){
						try{
							fundsServiceV2.frozeBalance(user.getStr("userCode"), txAmount, 0, traceType.Y, fundsType.J, fundsRemark+"本金解冻");
						} catch (Exception e) {
							autoRecy4loanLogger.log(Level.INFO, "[处理批次还款本金解冻:["+user.getStr("userName")+"]账户冻结余额转可用余额异常，冻结余额不足或其他原因");
						}
					}
					if(intAmount > 0){
						try{
							fundsServiceV2.frozeBalance(user.getStr("userCode"), intAmount, txFeeIn, traceType.Y, fundsType.J, fundsRemark+"利息解冻");
						} catch (Exception e) {
							autoRecy4loanLogger.log(Level.INFO, "[处理批次还款利息解冻:["+user.getStr("userName")+"]账户冻结余额转可用余额异常，冻结余额不足或其他原因");
						}
					}
				}
			}
			
			boolean result = jxTraceService.updateRemark("y", jxTraceCode);
			autoRecy4loanLogger.log(Level.INFO,"[处理批次还款流水jxTraceCode:"+jxTraceCode+"]备注信息更改"+(result?"成功":"失败"));
			
		}
		
		//更新回款统计表批次信息
		autoRecy4loanLogger.log(Level.INFO,"开始更新回款统计表批次信息.........");
		try {
			boolean updateRepaymentCountBatch = repaymentCountService.updateRepaymentCountBatch(getRepaymentDate(txDate),txDate);
			autoRecy4loanLogger.log(Level.INFO,"批次解冻,更新回款统计表批次信息:"+(updateRepaymentCountBatch?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"批次解冻,更新回款统计表批次信息:异常");
		}
		
		//大于等于当日批次已全部处理完,更新还款短信准备发送
		long countBatch = jxTraceService.countBatchSubstRepayByTxDate(txDate);//已发放批次代偿还款数量
		long countNotBatch = jxTraceService.countNotUntreatedBatch(txDate);//未解冻批次数量
		if(countBatch > 0 && countNotBatch == 0 && jxTraces.size() > 0){
			Db.update("update t_sms_log set `status`=9 where `status`=8 and type ='10' and sendDate = ?",DateUtil.getNowDate());
		}
				
		return succ("", "批次还款冻结余额解冻完成");
	}

//	/**
//	 * 更新还款记录表还款期数状态 WJW
//	 */
//	private void updateFailRepayment(String date){
//		List<JXTrace> jxTraces = null;
//		if(isDate){
//			jxTraces = jxTraceService.queryFailRepaymentVoucher(DateUtil.getNowDate());
//		}else {
//			jxTraces = jxTraceService.queryFailRepaymentVoucher(date);
//		}
//		for (JXTrace jxTrace : jxTraces) {
//			String jxTraceCode = jxTrace.getStr("jxTraceCode");
//			String requestMessage = jxTrace.getStr("requestMessage");
//			JSONObject parseObject = JSONObject.parseObject(requestMessage);
//			
//			//retCode为空,银行没有回调,主动查询即信,若成功则更新该红包状态
//			String retCode = jxTrace.getStr("retCode");
//			if(StringUtil.isBlank(retCode)){
//				String orgTxDate = jxTrace.getStr("txDate");
//				String orgTxTime = jxTrace.getStr("txTime");
//				String orgSeqNo = jxTrace.getStr("seqNo");
//				Map<String, String> fundTransQuery = null;
//				try {
//					fundTransQuery = JXQueryController.fundTransQuery(JXService.RED_ENVELOPES, orgTxDate, orgTxTime, orgSeqNo);
//				} catch (Exception e) {
//					autoRecy4loanLogger.log(Level.INFO,"jxTraceCode:["+jxTraceCode+"]红包发放查询通道异常");
//					continue;
//				}
//				if("00000000".equals(fundTransQuery.get("retCode")) && "0".equals(fundTransQuery.get("orFlag"))  && "00".equals(fundTransQuery.get("result"))){
//					//更新红包处理状态
//					jxTraceService.updateRemark("y", jxTraceCode);
//					continue;
//				}
//			}
//			
//			String desLine = parseObject.getString("desLine");
//			if(StringUtil.isBlank(desLine)){
//				continue;
//			}
//			int start = desLine.indexOf("[");
//			int end = desLine.indexOf("]");
//			if(start != -1 && end != -1){//存在traceCode
//				String traceCode = desLine.substring(start+1, end);
//				LoanTrace loanTrace = loanTraceService.findById(traceCode);
//				int recyPeriod = loanTrace.getInt("loanTimeLimit") - loanTrace.getInt("loanRecyCount");
//				boolean updateRecyStatus = loanRepaymentService.updateRecyStatus(-1, traceCode, recyPeriod);//红包发放失败,还款状态更新为-1
//				autoRecy4loanLogger.log(Level.INFO,"更新traceCode:"+traceCode+"第"+recyPeriod+"期还款状态"+(updateRecyStatus?"成功":"失败"));
//			}
//		}
//	}
	
	/**
	 * 还款红包发放失败,重发 WJW
	 * @return
	 */
	@ActionKey("/voucherPayRetry")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message voucherPayRetry(){
		String txDate = getPara("txDate",DateUtil.getNowDate());
		if(StringUtil.isBlank(txDate) || txDate.length() != 8 || !StringUtil.isNumeric(txDate)){
			return error("01", "看,日期输错了", null);
		}
		List<JXTrace> jxTraces = jxTraceService.queryFailUntreatedVoucherPay(txDate);
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String forAccountId = parseObject.getString("forAccountId");
			
			//retCode为空,银行没有回调,主动查询即信,若成功则更新该红包状态
			String retCode = jxTrace.getStr("retCode");
			if(StringUtil.isBlank(retCode)){
				String orgTxDate = jxTrace.getStr("txDate");
				String orgTxTime = jxTrace.getStr("txTime");
				String orgSeqNo = jxTrace.getStr("seqNo");
				Map<String, String> fundTransQuery = null;
				try {
					fundTransQuery = JXQueryController.fundTransQuery(JXService.RED_ENVELOPES, orgTxDate, orgTxTime, orgSeqNo);
				} catch (Exception e) {
					autoRecy4loanLogger.log(Level.INFO,"jxTraceCode:["+jxTraceCode+"]红包发放查询通道异常");
					continue;
				}
				if("00000000".equals(fundTransQuery.get("retCode")) && "0".equals(fundTransQuery.get("orFlag"))  && "00".equals(fundTransQuery.get("result"))){
					//更新红包处理状态
					jxTraceService.updateRemark("y", jxTraceCode);
					continue;
				}
			}
			
			if(StringUtil.isBlank(parseObject.getString("txAmount"))){
				continue;
			}
			if(StringUtil.isBlank(forAccountId)){
				continue;
			}
			long txAmount = StringUtil.getMoneyCent(parseObject.getString("txAmount"));//红包发放金额
			if(txAmount <= 0){
				continue;
			}
			String desLine = parseObject.getString("desLine");
			String traceCode = "";
			if(!StringUtil.isBlank(desLine)){
				int start = desLine.indexOf("[");
				int end = desLine.indexOf("]");
				if(start != -1 && end != -1){
					traceCode = desLine.substring(start+1, end);
				}
			}
			
			if(!StringUtil.isBlank(traceCode)){
				LoanTrace loanTrace = loanTraceService.findById(traceCode);
				if(loanTrace == null){
					autoRecy4loanLogger.log(Level.INFO,"红包补发,traceCode:"+traceCode+"不存在");
					continue;
				}
				
				if(desLine.indexOf("还款") != -1){//失败还款红包补发
					Map<String, String> voucherPay = null;
					try {
						voucherPay = JXController.voucherPay(JXService.RED_ENVELOPES, txAmount, forAccountId, "1", "还款["+traceCode+"]补发");
					} catch (Exception e) {
						jxTraceService.updateRemark("y", jxTraceCode);//更新原失败红包补发状态
						autoRecy4loanLogger.log(Level.INFO,"还款["+traceCode+"]补发红包通道异常");
						continue;
					}
					//更新还款计划表
					if(voucherPay != null && "00000000".equals(voucherPay.get("retCode"))){
						int recyPeriod = loanTrace.getInt("loanTimeLimit") - loanTrace.getInt("loanRecyCount");
						boolean updateRecyStatus = loanRepaymentService.updateRecyStatus(1, traceCode, recyPeriod);//红包发放成功,还款状态更新为1
						autoRecy4loanLogger.log(Level.INFO,"更新traceCode:"+traceCode+"第"+recyPeriod+"期还款状态"+(updateRecyStatus?"成功":"失败"));
					}
				}else {//失败返佣红包补发
					try {
						JXController.voucherPay(JXService.RED_ENVELOPES, txAmount, forAccountId, "1", "返佣["+traceCode+"]补发");
					} catch (Exception e) {
						jxTraceService.updateRemark("y", jxTraceCode);//更新原失败红包补发状态
						autoRecy4loanLogger.log(Level.INFO,"红包补发通道异常");
						continue;
					}
				}
				
				//更新失败红包补发状态
				jxTraceService.updateRemark("y", jxTraceCode);
			}
		}
		
		//更新回款计划表红包信息
		autoRecy4loanLogger.log(Level.INFO,"红包补发,开始更新回款计划表红包信息.......");
		try {
			boolean updateRepaymentCountVoucher = repaymentCountService.updateRepaymentCountVoucher(getRepaymentDate(txDate),txDate);
			autoRecy4loanLogger.log(Level.INFO,"红包补发,更新回款计划表红包信息:"+(updateRepaymentCountVoucher?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"红包补发,更新回款计划表红包信息异常");
		}
		return succ("", "红包还款补发成功........");
	}
	
	/**
	 * 保存批次还款jxTrace WJW
	 * @param jsonArray
	 * @return	key:jxTraceCode,value:true(成功)/false(失败)
	 */
	private Map<String, Boolean> saveBatchSubstRepay(JSONArray jsonArray,String remark){
		Map<String, Boolean> isBatchMap = new HashMap<String,Boolean>();//批次保存结果	key:jxTraceCode,value:true(成功)/false(失败)
		if(jsonArray.size() == 0){
			return null;
		}
		if(jsonArray.size() > 150){//拆分批次
			int num = jsonArray.size()%150 == 0 ? jsonArray.size()/150:jsonArray.size()/150+1;//需要发送批次数
			int i = 0;
			while (i < num) {
				JSONArray jsonArrayI = new JSONArray();//子批次
				for (int j = i*150; j < jsonArray.size(); j++) {
					if(j == (i+1)*150){
						break;
					}
					JSONObject jsonObject = jsonArray.getJSONObject(j);
					jsonArrayI.add(jsonObject);
				}
				int batchNo = jxTraceService.batchNoByToday();//批次号
				Map<String, String> reqMap = JXService.getHeadReq();
				String jxTraceCode = reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo");
				boolean saveBatchSubstRepay = jxTraceService.saveBatchSubstRepay(reqMap, batchNo, CommonUtil.NIUX_URL+"/notifyURL", jsonArrayI.toJSONString(),remark);
				isBatchMap.put(jxTraceCode, saveBatchSubstRepay);
				i++;
			}
		}else {
			int batchNo = jxTraceService.batchNoByToday();
			Map<String, String> reqMap = JXService.getHeadReq();
			String jxTraceCode = reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo");
			boolean saveBatchSubstRepay = jxTraceService.saveBatchSubstRepay(reqMap, batchNo, CommonUtil.NIUX_URL+"/notifyURL", jsonArray.toJSONString(),remark);
			isBatchMap.put(jxTraceCode, saveBatchSubstRepay);
		}
		return isBatchMap;
	}

	/**
	 * 补发逾期还款本金 WJW
	 * @param overdueLoanCodes 逾期本金结算loanCode列表
	 */
	private void reissueOverdueAmount(List<String> overdueLoanCodes){
		for (int i = 0; i < overdueLoanCodes.size(); i++) {
			String loanCode = overdueLoanCodes.get(i);
			autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",进度:第"+(i+1)+"/"+overdueLoanCodes.size()+"条开始........");

			LoanInfo loanInfo = loanInfoService.findById(loanCode);
			int reciedCount = loanInfo.getInt("reciedCount");//已还期数
			String effectDate = loanInfo.getStr("effectDate");//满标时间
			String repaymentDate = CommonUtil.anyRepaymentDate4string(effectDate,reciedCount);//根据已还期数查询标上次还款日期
			String loanState = loanInfo.getStr("loanState");//标状态
			long sumOverdueAmount = loanTraceService.sumOverdueAmount(loanCode);
			if(sumOverdueAmount == 0){//该标仅设置了逾期,还未产生实际逾期,更新逾期处理状态
				boolean updateStatus = loanOverdueService.updateStatus(loanCode, "y", "");//更新逾期表还款状态
				autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",未产生实际逾期,还款逾期表状态更改为已处理"+(updateStatus?"成功":"失败"));
				
				//逾期回款日期距上次正常回款日期差<=15天或标已结清,清零逾期时利息
				int dateDiff = DateUtil.differentDaysByMillisecond(repaymentDate, DateUtil.getNowDate(), "yyyyMMdd");//今日距上次回款时间日期差
				if(dateDiff <= 15 || "O".equals(loanState) || "P".equals(loanState)){
					boolean updateOverdueInterest = loanTraceService.updateOverdueInterest(0, loanCode);
					autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",逾期时利息清零"+(updateOverdueInterest?"成功":"失败"));
				}
				continue;
			}
			try {
				JSONArray jsonArray = new JSONArray();
				List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
				for (LoanTrace loanTrace : loanTraces) {
					String traceCode = loanTrace.getStr("traceCode");
					String payUserCode = loanTrace.getStr("payUserCode");
					String payUserName = loanTrace.getStr("payUserName");
					String loanNo = loanTrace.getStr("loanNo");
					Long overdueAmount = loanTrace.getLong("overdueAmount");
					User user = getPayUser(payUserCode);//userService.findById(payUserCode);//投标人
					String forAccountId = user.getStr("jxAccountId");//投资人电子账号
					
					if(overdueAmount <= 0){
						autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],traceCode["+traceCode+"]逾期金额为0或异常,跳过...........");
						continue;
					}
					
					String authCode = loanTrace.getStr("authCode");//授权码
					//发生过债转
					if(loanTransferService.vilidateIsTransfer(traceCode)){
						List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
						String transferAuthCode = loanTransfers.get(loanTransfers.size() - 1).getStr("authCode");//最后一债转authCode
						if(!StringUtil.isBlank(transferAuthCode)){
							authCode = transferAuthCode;
						}
					}
					
					int recyStatus = 0;//还款类型 2:未开户,3:批次,5:红包发放
					if(StringUtil.isBlank(forAccountId)){//未开户记账
						recyStatus = 2;
					}else if(StringUtil.isBlank(authCode)){//红包还款
						if(overdueAmount > 0){
							jxTraceService.saveVoucherPayRequest(JXService.RED_ENVELOPES, overdueAmount, forAccountId, "1", "还款本金["+traceCode+"]","");
						}
						recyStatus = 5;
					}else {//代偿还款
						jsonArray = jxTraceService.batchSubstRepayJson(jsonArray, loanCode, forAccountId, overdueAmount, 0, 0, authCode, getPaymentAuthPageState(forAccountId));
						recyStatus = 3;
					}
					boolean updateOverdueTrace = updateOverdueTrace(traceCode, payUserCode, payUserName, loanNo, overdueAmount, recyStatus);//更新资金流水
					autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],traceCode["+traceCode+"]逾期金额:"+StringUtil.getMoneyYuan(overdueAmount)+"元还款"+(updateOverdueTrace?"成功":"失败"));
				}
				
				boolean updateOverdueAmount = loanTraceService.updateOverdueAmount(0, loanCode);//loanTrace逾期本金清零
				autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",逾期本金清零"+(updateOverdueAmount?"成功":"失败"));
				
				saveBatchSubstRepay(jsonArray, "overdue");//保存还款批次
				boolean updateStatus = loanOverdueService.updateStatus(loanCode, "y", "");//更新逾期表还款状态
				autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",还款逾期表状态更改为已处理"+(updateStatus?"成功":"失败"));
				
				//逾期回款日期距上次正常回款日期差<=15天或标已结清,清零逾期时利息
				int dateDiff = DateUtil.differentDaysByMillisecond(repaymentDate, DateUtil.getNowDate(), "yyyyMMdd");//今日距上次回款时间日期差
				if(dateDiff <= 15 || "O".equals(loanState) || "P".equals(loanState)){
					boolean updateOverdueInterest = loanTraceService.updateOverdueInterest(0, loanCode);
					autoRecy4loanLogger.log(Level.INFO,"[逾期本金还款],loanCode:"+loanCode+",逾期时利息清零"+(updateOverdueInterest?"成功":"失败"));
				}
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]逾期本金补还异常...........");
			}
		}
	}
	
	/**
	 * 标期数结清后,仍在逾期,到结算日返还逾期利息 WJW
	 * @param loanInfos
	 */
	private void reissueOverdueInterest(List<LoanInfo> loanInfos){
		for (LoanInfo loanInfo : loanInfos) {
			String loanCode = loanInfo.getStr("loanCode");
			try {
				String effectDate = loanInfo.getStr("effectDate");
				List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
				JSONArray jsonArray = new JSONArray();
				for (LoanTrace loanTrace : loanTraces) {
					String traceCode = loanTrace.getStr("traceCode");
					String payUserCode = loanTrace.getStr("payUserCode");
					String payUserName = loanTrace.getStr("payUserName");
					String loanNo = loanTrace.getStr("loanNo");
					Long overdueAmount = loanTrace.getLong("overdueAmount");
					Long overdueInterest = loanTrace.getLong("overdueInterest");
					User user = getPayUser(payUserCode);//userService.findById(payUserCode);//投标人
					String forAccountId = user.getStr("jxAccountId");//投资人电子账号
					
					long overdueTraceFee = calculationTraceFee(user, effectDate, overdueInterest);//逾期利息手续费
					
					if(overdueAmount <= 0){
						autoRecy4loanLogger.log(Level.INFO,"[标期数结清,逾期利息还款],traceCode["+traceCode+"]逾期金额为0或异常,跳过...........");
						continue;
					}
					
					String authCode = loanTrace.getStr("authCode");//授权码
					//发生过债转
					if(loanTransferService.vilidateIsTransfer(traceCode)){
						List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
						String transferAuthCode = loanTransfers.get(loanTransfers.size() - 1).getStr("authCode");//最后一债转authCode
						if(!StringUtil.isBlank(transferAuthCode)){
							authCode = transferAuthCode;
						}
					}
					
					int recyStatus = 0;//还款类型 2:未开户,3:批次,5:红包发放
					if(StringUtil.isBlank(forAccountId)){//未开户记账
						recyStatus = 2;
					}else if(StringUtil.isBlank(authCode)){//红包还款
						if(overdueInterest-overdueTraceFee > 0){
							jxTraceService.saveVoucherPayRequest(JXService.RED_ENVELOPES, overdueInterest-overdueTraceFee, forAccountId, "1", "还款利息["+traceCode+"]","");
						}
						recyStatus = 5;
					}else {//批次还款
						jsonArray = jxTraceService.batchSubstRepayJson(jsonArray, loanCode, forAccountId, 0, overdueInterest, overdueTraceFee, authCode, getPaymentAuthPageState(forAccountId));
						recyStatus = 3;
					}
					boolean endOverdueInterestTrace = endOverdueInterestTrace(loanNo, traceCode, payUserCode, payUserName, overdueInterest, overdueTraceFee, recyStatus);//更新资金流水
					autoRecy4loanLogger.log(Level.INFO,"[标期数结清,逾期利息还款],traceCode["+traceCode+"]逾期利息:"+StringUtil.getMoneyYuan(overdueInterest)+"元还款"+(endOverdueInterestTrace?"成功":"失败"));
				}
				
				saveBatchSubstRepay(jsonArray, "overdueInterest");//保存批次信息
				boolean updateLoan4clear = loanInfoService.updateLoan4clear(loanInfo);//更新标结算日期,防止回款程序今日二次运行导致多发利息回款
				autoRecy4loanLogger.log(Level.INFO,"loanCode:["+loanCode+"]逾期利息补还,更新loanInfo结算日期"+(updateLoan4clear?"成功":"失败"));
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"loanCode["+loanCode+"]逾期利息补还异常...........");
			}
		}
	}
	
	/**
	 * 更新投资人逾期还款本金资金信息 WJW
	 * @param payUserCode	投资人编码
	 * @param loanNo		标号
	 * @param overdueAmount	逾期金额
	 * @param recyStatus 	还款类型	2:未开户,3:批次,5:红包发放
	 * @return
	 */
	private boolean updateOverdueTrace(String traceCode,String payUserCode,String payUserName,String loanNo,long overdueAmount,int recyStatus){
		try {
			if(recyStatus == 2 || recyStatus == 5){//红包返还本金或未开户,操作可用余额
				fundsServiceV2.doAvBalance4biz(payUserCode, overdueAmount , 0 ,  traceType.R , fundsType.J , "标["+loanNo+"]逾期本金还款");
			}else if(recyStatus == 3){//批次返还本金,操作冻结余额
				fundsServiceV2.doAvBalanceRepayment(payUserCode, overdueAmount , 0 ,  traceType.R , fundsType.J ,"标["+loanNo+"]逾期本金还款(冻结,待银行批次解冻)");
			}
			
			if(recyStatus == 2){//未开户记录待还本金
				returnedAmountService.save(payUserCode, payUserName, overdueAmount, traceCode, 1, 2);
			}
			
			return fundsServiceV2.updateBeRecyFunds(payUserCode, 0, -overdueAmount , 0, overdueAmount, 0) > 0;//更新待收本金
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 标期数结清后,逾期利息还款资金流水处理 WJW
	 * @param loanNo			标号
	 * @param traceCode			投标编码
	 * @param payUserCode		用户编码
	 * @param payUserName		用户名
	 * @param overdueInterest	逾期利息
	 * @param traceFee			逾期利息手续费
	 * @param recyStatus		还款类型	2:未开户,3:批次,5:红包发放
	 * @return
	 */
	private boolean endOverdueInterestTrace(String loanNo,String traceCode,String payUserCode,String payUserName,long overdueInterest,long traceFee,int recyStatus){
		try {
			if(recyStatus == 2 || recyStatus == 5){//红包返还利息或未开户,操作可用余额
				fundsServiceV2.doAvBalance4biz(payUserCode, overdueInterest , traceFee ,  traceType.L , fundsType.J ,"标["+loanNo+"]期数结清后逾期回收利息");
			}else if(recyStatus == 3){//批次返还利息,操作冻结余额
				fundsServiceV2.doAvBalanceRepayment(payUserCode, overdueInterest , traceFee ,  traceType.L , fundsType.J ,"标["+loanNo+"]期数结清后逾期回收利息(冻结,待银行批次解冻)");
			}
			
			if(recyStatus == 2){//未开户记录待还利息
				returnedAmountService.save(payUserCode, payUserName, overdueInterest-traceFee, traceCode, 2, 2);
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
	
	/**
	 * 添加逾期记录 WJW
	 * @param loanInfo		逾期标
	 * @param principal		逾期本金
	 * @param interest		逾期利息
	 * @param overdueType	逾期类型(A:还本金利息,P:还本金,不还利息,I:不还本金,还利息,N:不还本金利息)
	 * @param remark		备注
	 * @return
	 */
	private boolean addOverdue(LoanInfo loanInfo,Long principal,Long interest,loanOverdueType overdueType,String remark){
			String loanNo = loanInfo.getStr("loanNo");//标号
			String loanCode = loanInfo.getStr("loanCode");//借款编码
			String loanUserCode = loanInfo.getStr("userCode");//借款人用户编码
			String loanUserName = loanInfo.getStr("userName");//借款人用户名
			Long loanAmount = loanInfo.getLong("loanAmount");//标金额
			String loanTitle = loanInfo.getStr("loanTitle");//标题
			Integer loanTimeLimit = loanInfo.getInt("loanTimeLimit");//期数
			Integer reciedCount = loanInfo.getInt("reciedCount");//已还期数
			String refundType = loanInfo.getStr("refundType");//还款方式
			
			//标如果已有对应期数逾期记录,则跳过
			List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", null);
			for (LoanOverdue loanOverdue : loanOverdues) {
				Integer repayIndex = loanOverdue.getInt("repayIndex");
				if(repayIndex == reciedCount+1){
					return true;
				}
			}
			
			Map<String, Object> paraMap = new HashMap<String,Object>();
			paraMap.put("loanNo", loanNo);//标的编号
			paraMap.put("loanCode", loanCode);//贷款标编码
			paraMap.put("loanUserCode", loanUserCode);//借款人用户编码
			paraMap.put("loanUserName", loanUserName);//借款人用户名
			paraMap.put("loanAmount", loanAmount.intValue());//贷款标金额
			paraMap.put("loanTitle", loanTitle);//贷款标标题
			paraMap.put("loanTimeLimit", loanTimeLimit);//还款总期数
			paraMap.put("repayIndex", reciedCount+1);//第几期逾期
			paraMap.put("principal", principal.intValue());//应还本金
			paraMap.put("interest", interest.intValue());//应还利息
			paraMap.put("overdueAmount", principal);//实际逾期欠款金额
			paraMap.put("refundType", refundType);//还款方式
			paraMap.put("overdueType", overdueType.val());//逾期类型
			paraMap.put("overdueDate", DateUtil.getNowDate());
			paraMap.put("overdueTime", DateUtil.getNowTime());
			paraMap.put("remark", remark);//备注
			return loanOverdueService.save(paraMap);
	}
	
	/**
	 * 回款存管资金验证 WJW
	 * @param sumBatchAmount 批次代偿总金额
	 * @param sumVoucherPayInterest 红包总金额
	 * @return
	 */
	private boolean interceptJXFunds(long sumBatchAmount,long sumVoucherPayInterest){
		//验证代偿户代偿冻结余额是否充足
		Map<String, String> freezeAmtQuery = JXQueryController.freezeAmtQuery(JXService.RISK_RESERVE);
		String dcAmt = freezeAmtQuery.get("dcAmt");
		if(sumBatchAmount > StringUtil.getMoneyCent(dcAmt)){
			return false;
		}
		
		//验证红包户余额是否充足
		Map<String, String> balanceQuery = JXQueryController.balanceQuery(JXService.RED_ENVELOPES);
		String availBal = balanceQuery.get("availBal");
		if(sumVoucherPayInterest > StringUtil.getMoneyCent(availBal)){
			return false;
		}
		return true;
	}
	
	/**
	 * 自动还款页面校验 WJW
	 * @param smsMsg	短信验证码
	 * @return
	 */
	private boolean autoLoanIntercept(String userCode){
		String smsMsg = getPara("smsMsg");//短信验证码
		OPUserV2 opUserV2 = oPUserV2Service.findById(userCode);
		String opName = opUserV2.getStr("op_name");
		String opMobile = opUserV2.getStr("op_mobile");
		String decryptUserMobile = "";
		try {
			decryptUserMobile = CommonUtil.decryptUserMobile(opMobile);
		} catch (Exception e1) {
			return false;
		}
		
		//验证码校验
		if(StringUtil.isBlank(smsMsg)){
			WebUtils.writePromptHtml("短信验证码为空", "/main#", "UTF-8",getResponse());
			return false;
		}
		if(!smsMsg.equals(String.valueOf(Memcached.get("sendMsgAuthLoan_" + decryptUserMobile)))){
			WebUtils.writePromptHtml("短信验证码错误", "/main#", "UTF-8",getResponse());
			return false;
		}
		
		//防止用户重复发起回款操作,限制时长2小时
		if(userCode.equals(String.valueOf(Memcached.get("autoRecy4loan_"+userCode)))){
			WebUtils.writePromptHtml("当日还款正在进行中,请勿重复操作", "/main#", "UTF-8",getResponse());
			return false;
		}
		Memcached.set("autoRecy4loan_"+userCode, userCode,2*60*60*1000);//间隔时间2小时
		WebUtils.writePromptHtml("当日还款已开始,请耐心等待结果", "/main#", "UTF-8",getResponse());
		
		try {
			SMSClient.sendSms(decryptUserMobile, "回款程序已开始运行,请勿重复发起,操作人:"+opName+",操作日期:"+DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"【易融恒信】");
		} catch (Exception e) {
			e.printStackTrace();
		}
		autoRecy4loanLogger.log(Level.INFO, "回款程序已开始运行,操作人:"+opName+",操作日期:"+DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"【易融恒信】");
		return true;
	}

	/**
	 * 根据利息计算手续费 WJW
	 * @param user
	 * @param effectDate	满标时间
	 * @param interest
	 * @return
	 */
	private long calculationTraceFee(User user,String effectDate,long interest){
		int vipInterestRate = user.getInt("vipInterestRate") ;//利息管理费费率
		if(Long.parseLong(effectDate) < 20180319){//会员等级更新之前，利息管理费费率不变
			if(user.getInt("beforeVip") != null){
				vipInterestRate = historyInterest(user.getInt("beforeVip"));//根据原用户等级，获取对应等级利息管理费费率
			}
		}
		return vipInterestRate > 0 ? interest*vipInterestRate/10/10/10/10:0;
	}
	
	/**
	 * 赏金返佣 WJW
	 * @param loan
	 * @param trace
	 * @param isTransfer	true:有过债转,false:无
	 */
	private Boolean recommendReward(LoanInfo loan,LoanTrace trace,boolean isTransfer){
		String loanNo = trace.getStr("loanNo");
		String traceCode = trace.getStr("traceCode");
		String payUserCode = trace.getStr("payUserCode");
		String aUserCode = recommendInfoService.queryAUserCodeByBUserCode(payUserCode);//查询用户推荐人userCode
		if(!StringUtil.isBlank(aUserCode)){//存在推荐人,返佣
			long rewardAmount = rewardAmount(loan,trace,isTransfer);//返佣金额
			if(rewardAmount > 0){
				fundsServiceV2.doAvBalance4biz(aUserCode,rewardAmount, 0, SysEnum.traceType.W, SysEnum.fundsType.J, "好友投资返佣,借款标["+loanNo+"]");
//				User wzuser =userService.findByMobile(CommonUtil.OUTCUSTNO);
//				if(CommonUtil.fuiouPort){
//					FuiouController fuiouController = new FuiouController();
//					fuiouController.transferBu(rewardAmount, FuiouTraceType.MANUAL_RECHARGE, wzuser, userService.findById(aUserCode), loanCode, "好友投资返佣,借款标["+loanNo+"]");
//				}
				if(CommonUtil.jxPort){
					User aUser = userService.findById(aUserCode);//推荐人
					String forAccountId = aUser.getStr("jxAccountId");//推荐人电子账号
					if(!StringUtil.isBlank(forAccountId)){
						try {
							JXController.voucherPay(JXService.RED_ENVELOPES, rewardAmount, forAccountId, "1", "好友返佣["+traceCode+"]");
						} catch (Exception e) {
							autoRecy4loanLogger.log(Level.INFO, "好友返佣["+traceCode+"]红包发放通道异常");
						}
					}else {//推荐人未开通存管,保存未还金额
						returnedAmountService.save(aUserCode, aUser.getStr("userName"), rewardAmount, traceCode, 3, 2);
					}
				}
				return rrService.save(aUserCode, payUserCode, rewardAmount, "C", "好友投资返佣,借款标["+loanNo+"]");
			}
		}
		return null;
	}
	
	/**
	 * 根据用户电子账号获取缴费授权状态 WJW
	 * @param forAccountId
	 * @return
	 */
	private long getPaymentAuthPageState(String forAccountId){
		if(paymentAuthPageStateMap.containsKey(forAccountId)){
			return paymentAuthPageStateMap.get(forAccountId);
		}else {
			UserTermsAuth userTermsAuth = null;
			try {
				userTermsAuth = userTermsAuthService.findByJxAccountId(forAccountId);
			} catch (Exception e) {}
			if(userTermsAuth == null){
				paymentAuthPageStateMap.put(forAccountId, 0l);
				return 0;
			}
			String paymentAuth = userTermsAuth.getStr("paymentAuth");
			long paymentMaxAmt = userTermsAuth.getLong("paymentMaxAmt");
			String paymentDeadline = userTermsAuth.getStr("paymentDeadline");
			if("1".equals(paymentAuth) && DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), paymentDeadline) < 0){
				paymentAuthPageStateMap.put(forAccountId, paymentMaxAmt);
				return paymentMaxAmt;
			}else {
				paymentAuthPageStateMap.put(forAccountId, 0l);
				return 0;
			}
		}
	}
	
	/**
	 * 发送还款资金交易请求 WJW
	 * @return
	 */
	@ActionKey("/sendBatchSubstRepay")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public synchronized Message sendBatchSubstRepay(){
		String txDate = getPara("txDate",DateUtil.getNowDate());
		int num = getParaToInt("num",10000);
		List<JXTrace> jxTraces = jxTraceService.queryByTxCodeTxDateStatus(txDate, jxTraceStatus.A.val(), jxTxCode.batchSubstRepay.val(),jxTxCode.voucherPay.val());
		for (int i = 0; i < jxTraces.size(); i++) {
			if(i >= num){//限制发送条数
				break;
			}
			JXTrace jxTrace = jxTraces.get(i);
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			try {
				Map<String, String> jxTraceRequest = JXService.requestCommonByJxTrace(jxTrace);
				autoRecy4loanLogger.log(Level.INFO,"jxTraceCode["+jxTraceCode+"]交易发送"+(RetCodeUtil.isSuccRetCode(jxTraceRequest.get("retCode")) || "success".equals(jxTraceRequest.get("received"))?"成功":"失败......"));
			} catch (Exception e) {
				autoRecy4loanLogger.log(Level.INFO,"jxTraceCode["+jxTraceCode+"]交易发送异常........");
			}
		}
		
		//标的还款信息更新完成,更新回款统计表
		autoRecy4loanLogger.log(Level.INFO,"开始更新回款统计表..........");
		try {
			String repaymentDate = getRepaymentDate(txDate);
			repaymentCountService.updateRepaymentCountVoucher(repaymentDate,txDate);//更新红包信息
			repaymentCountService.updateRepaymentCountBatch(repaymentDate,txDate);//更新批次信息
			Map<String, Object> repaymentCountMap = new HashMap<String,Object>();
			repaymentCountMap.put("repaymentDate", repaymentDate);
			repaymentCountMap.put("repaymentStatus", repaymentCountStatus.B.val());
			boolean updateRepaymentCount = repaymentCountService.updateRepaymentCount(repaymentCountMap);//更新还款状态
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表"+(updateRepaymentCount?"成功":"失败"));
		} catch (Exception e) {
			autoRecy4loanLogger.log(Level.INFO,"更新回款统计表异常..........");
		}
		return succ("", "交易发送完成");
	}
}

