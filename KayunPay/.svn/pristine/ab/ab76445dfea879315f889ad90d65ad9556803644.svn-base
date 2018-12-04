package com.dutiantech.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.admin.validator.LoanAuditLoanByLoanValidator;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Funds;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNxjd;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNxjdService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SettlementPlanService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.LiCai;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.loanState;
import com.dutiantech.util.SysEnum.traceState;
import com.dutiantech.util.SysEnum.traceType;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.UnFreezeRspData;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.service.JXService;
import com.jx.util.RetCodeUtil;

public class AuditLoanController extends BaseController {
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);

	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private UserService userService = getService(UserService.class);
	
	private SettlementPlanService settlementPlanService = getService(SettlementPlanService.class);
	
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	
	private SMSLogService smsLogService = getService(SMSLogService.class);
	
	private LoanNxjdService loanNxjdService = getService(LoanNxjdService.class);
	
	private static final Logger scanManBiaoLogger = Logger.getLogger("scanManBiaoLogger");
	private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private UserInfoService userInfoService=getService(UserInfoService.class);
	private JXTraceService jxTraceService=getService(JXTraceService.class);
	private static int isrefund = 1;//放款 1为放款到借款人   0为放款到吴总户
	
	private boolean isLoan = true;//批次放款调试开关(true:放款响应报文先更新,由人工手动调用接口处理后续流水;false:自动更新报文处理流水)
	
	static Map<String, LoanInfo> _LOCK_LOAN = new HashMap<String , LoanInfo >();
	
	static{
		LoggerUtil.initLogger("scanManBiao", scanManBiaoLogger);
	}
	
	private synchronized void lock(String loanCode , LoanInfo loan ){
		_LOCK_LOAN.put(loanCode, loan) ;
	}
	
	/**
	 * 	
	 * @param loanCode
	 * @return
	 * 		false		unlock
	 * 		true		locked
	 */
	private boolean isLock(String loanCode ){
		return (_LOCK_LOAN.get(loanCode) != null ) ;
	}
	
	private void unlock(String loanCode){
		_LOCK_LOAN.remove(loanCode) ;
	}
	
//	@ActionKey("/auditFullLoan")
//	@AuthNum(value=999)
//	@Before({LoanAuditLoanByLoanValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
//	public Message auditFullLoan(){
//		String loanCode = getPara("loanCode");
//		try {
//			return fuckyou(loanCode);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return error("BB", "放款异常", false);
//		}
//	}
	
	private Message fuckyou(String loanCode12){
		String loanCode = loanCode12;
		if( StringUtil.isBlank( loanCode ) == true )
			return error("01", "请求参数异常!", null) ;
		
		if( loanCode.length() != 32)
			return error("02", "请求参数异常！", null );
		
		LoanInfo loan = loanInfoService.findById(loanCode);
		if( SysEnum.loanState.J.val().equals( loan.getStr("loanState") ) == false){
			return error("03", "不满足满标放款审核条件！借款标状态必须为【招标】",
					null ) ;
		}
		
		long loanBalance = loan.getLong("loanBalance") ;
		if( loanBalance != 0 ){
			return error("04", "当前标的剩余投标金额为：" + loanBalance + "分,不满足满标放款审核条件！",
					null ) ;
		}
		
		if( isLock(loanCode) == true ){
			return error("11", "标正在审核中", null ) ;
		}
		
		//lock loan
		lock(loanCode, loan);
		
		//新人福利 benefits4new
		List<LoanTrace> traces = loanTraceService.findAllByLoanCode( loanCode );

		if(CommonUtil.jxPort){
			//撤销多余投标流水
			boolean isBidCancel = false;//撤销投标申请结果
			try {
				isBidCancel = loanTraceExcessBidCancel(loan, traces, getResponse());
			} catch (Exception e) {
				unlock(loanCode);
				scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款,撤销多余投标流水异常");
				return error("", "loanCode:["+loanCode+"]满标自动放款,撤销多余投标流水异常", null);
			}
			
			//满标自动放款
			boolean isAutoLendPay = false;//满标自动放款结果
			if(isBidCancel){
				try {
					String retCode = autoLendPay(loan);
					if("00000000".equals(retCode)){
						isAutoLendPay = true;
					}else {
						unlock(loanCode);
						scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款异常,retCode:"+retCode);
						return error("", "loanCode:["+loanCode+"]满标自动放款异常,retCode:"+retCode, null);
					}
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款异常");
					return error("", "loanCode:["+loanCode+"]满标自动放款异常", null);
				}
			}
			
			//更新loanTrace
			boolean isLoanTrace = false;//更新loanTrace结果
			if(isAutoLendPay){
				try {
					isLoanTrace = doLoanTrace(loan, traces);
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款更新loanTrace异常");
					return error("", "loanCode:["+loanCode+"]满标自动放款更新loanTrace异常", null);
				}
			}
			
			//更新loanInfo
			boolean isLoanInfo = false;//更新loanInfo结果
			if(isLoanTrace){
				try {
					isLoanInfo = doLoanInfo(loan);
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款更新loanInfo异常");
					return error("", "loanCode:["+loanCode+"]满标自动放款更新loanInfo异常", null);
				}
			}
			
			//满标放款成功,若标类型为易分期,则通知旅游分期平台
			if(isLoanInfo && "E".equals(loan.getStr("productType"))){
				LoanNxjd loanNxjd = loanNxjdService.findLastByLoanCode(loanCode);
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("orderNo", loanNxjd.getStr("ztyLoanCode"));
				jsonObject.put("status", 1);
				try {
					String sendPost = HttpRequestUtil.sendPost(CommonUtil.YISTAGE_URL+"/lyfq/installment/fund/loanStatus", jsonObject.toJSONString());
					JSONObject parseObject = JSONObject.parseObject(sendPost);
					scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款通知旅游分期平台状态:"+parseObject.getString("msg"));
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO, "loanCode:["+loanCode+"]满标自动放款通知旅游分期平台异常");
				}
			}
			
			if(isLoanInfo){
				unlock(loanCode);
				return succ("满标自动放款完成", null);
			}else {
				return error("","满标自动放款失败", null);
			}
			
			/*//发送批次放款请求
			List<JXTrace> queryAutoLendPayByLoanCode = jxTraceService.queryBatchLendPayByLoanCode(loanCode);
			if(queryAutoLendPayByLoanCode.size() < 1){
				try {
					doLoanTraceBatchReq(loan, traces);
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO, "发送批次放款请求loanCode:["+loanCode+"]异常......");
					return null;
				}
				unlock(loanCode);
				return succ("[满标放款]loanCode:["+loanCode+"]发送批次放款请求成功", null);
			}
			//更新放款流水
			try {
				//批次处理进度验证,未处理完,跳过
				for (JXTrace jxTrace : queryAutoLendPayByLoanCode) {
					String remark = jxTrace.getStr("remark");
					if("y".equals(remark)){//该批次放款已处理
						continue;
					}
					String jxTraceCode = jxTrace.getStr("jxTraceCode");
					String txDate = jxTrace.getStr("txDate");
					String txTime = jxTrace.getStr("txTime");
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject parseObject = JSONObject.parseObject(requestMessage);
					String batchNo = parseObject.getString("batchNo");
					
					//距离发送批次2小时,未收到回调,主动查询
					if(Long.valueOf(DateUtil.updateDate(new Date(), -2, Calendar.HOUR_OF_DAY, "yyyyMMddHHmmss")) > Long.valueOf(txDate+txTime)){
						Map<String, String> batchQuery = null;
						try {
							batchQuery = JXQueryController.batchQuery(txDate, batchNo);
						} catch (Exception e) {
							unlock(loanCode);
							return error("", "[满标放款]loanCode:["+loanCode+"]批次查询通道异常", null);
						}
						if(batchQuery == null || !"S".equals(batchQuery.get("batchState"))){//批次未处理完
							unlock(loanCode);
							return error("", "[满标放款]loanCode:["+loanCode+"]批次未处理完", null);
						}else {
							boolean result = jxTraceService.updateResponseMessage("admin", jxTraceCode);
							scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]响应报文更新"+(result?"成功":"失败"));
						}
					}else {//未超过2小时,查询本地状态
						int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);
						if(!(jxTraceState == 5 || jxTraceState == 6 || jxTraceState == 7)){//即信未发回调,不处理
							unlock(loanCode);
							return error("", "[满标放款]loanCode:["+loanCode+"]批次未处理完", null);
						}
					}
				}
				doLoanTrace(loanCode,queryAutoLendPayByLoanCode);
			} catch (Exception e) {
				scanManBiaoLogger.log(Level.INFO, "更新满标放款loanCode:["+loanCode+"]流水异常......");
				return error("", "更新满标放款loanCode:["+loanCode+"]流水异常......", null);
			}
			unlock(loanCode);
			return succ("更新满标放款loanCode:["+loanCode+"]流水更新完成", null);*/
		}
		
		// 验证存管户与平台资金是否同步
		int syncCount = 0;
		for (LoanTrace loanTrace : traces) {
			User user = userService.findById(loanTrace.getStr("payUserCode"));
			Funds funds = fundsServiceV2.findById(loanTrace.getStr("payUserCode"));
			QueryBalanceResultData fuiouFunds =	fuiouTraceService.BalanceFunds(user);
			if (funds.getLong("avBalance") == Long.parseLong(fuiouFunds.getCa_balance()) && 
					funds.getLong("frozeBalance") == Long.parseLong(fuiouFunds.getCf_balance())) {
				syncCount++;
			} else {
				scanManBiaoLogger.log(Level.INFO, "[" + user.getStr("userCode") + "][" + user.getStr("userName") + "]账户资金异常");
			}
		}
		if (syncCount != traces.size()) {
			unlock(loanCode);
			return error("40", "平台资金与存管户资金不匹配，[" + loanCode + "]放款失败", null);
		}
		
		int doResult = doLoanTrace( 0 , loan , traces );
		if( doResult == 0 ){
			String loanUserCode = loan.getStr("userCode") ;
			Integer rateByYear = loan.get("rateByYear");//年利率
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			int benefits4new = loan.getInt("benefits4new");
			rateByYear = rateByYear + rewardRateByYear + benefits4new;
			long loanAmount = loan.getLong("loanAmount");//贷款标总金额
			Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
			String refundType = loan.getStr("refundType");
			long[] zonglixi = CommonUtil.f_004( loanAmount , rateByYear , loanTimeLimit , refundType) ;
			//交易管理费默认给0,需要考虑加入计算中
			Funds zz = null;
			try {
				zz = fundsServiceV2.doAvBalance4biz(loanUserCode, zonglixi[0], 0, traceType.S, fundsType.J, "招标成功,满标审核放款。");
			} catch (Exception e) {
				return error("06", "更新借款人资金账户发生异常！", e.getMessage());
			}
			if(zz==null){
				return error("07", "更新借款人资金账户发生异常！",null);
			}
			doResult = fundsServiceV2.updateBeRecyFunds4loan(loanUserCode, loanTimeLimit , zonglixi[0],
					zonglixi[1] , 0 ) ;
			
			if(doResult > 0 ){

				boolean z1 = loanInfoService.updateLoanByLent(loanCode,loanUserCode,loanTimeLimit );
				if( z1 == true ){
					//结算完成
					//return succ("结算完成", null ) ;
					//生成还款计划
					long leftRecyPrincipal = loanAmount;//剩余待还本金
					long leftRecyInterest = zonglixi[1];//剩余待还利息
					LiCai ff = new LiCai( loanAmount , rateByYear, loanTimeLimit );
					List<Map<String , Long>> xxx = null;
					if(refundType.equals("A")){
						xxx = ff.getDengEList() ;
					}else if(refundType.equals("B")){
						 xxx = ff.getDengXiList();
					}
					for (int j = 1; j <= xxx.size(); j++) {
						Map<String,Long> ck = xxx.get(j-1);
						long ben = ck.get("ben");
						long xi = ck.get("xi");
						Map<String,Object> params = new HashMap<String, Object>();
						String backDate = CommonUtil.anyRepaymentDate4string(DateUtil.getNowDate(), j);
						params.put("backDate", backDate);
						params.put("recyLimit", j);
						if(ben < 0)
							ben = 0;
						if(xi < 0)
							xi = 0;
						params.put("recyPrincipal",ben);
						params.put("recyInterest", xi);
						params.put("realRecyPrincipal", 0);
						params.put("realRecyInterest", 0);
						leftRecyPrincipal = leftRecyPrincipal - ben;
						leftRecyInterest = leftRecyInterest - xi;
						if(leftRecyPrincipal<0)
							leftRecyPrincipal = 0;
						if(leftRecyInterest<0)
							leftRecyInterest = 0;
						if(j==loanTimeLimit){
							leftRecyPrincipal = 0;
							leftRecyInterest = 0;
						}
						params.put("leftRecyPrincipal", leftRecyPrincipal);
						params.put("leftRecyInterest", leftRecyInterest);
						params.put("beRecyPrincipal", loanAmount);
						params.put("beRecyInterest", zonglixi[1]);
						params.put("settlementState", SysEnum.settlementState.A.val());
						settlementPlanService.save(loan, params);
					}
					
				}
			}else{
				unlock(loanCode);
				return error("06", "更新借款人账户异常", null ) ;
			}
			
			/*
			int x1 = fundsService.updateByLentLoan4LoanUser(loanUserCode, 
					loanAmount, zonglixi , 0, 0);//满标放款后，更新借款人资金账户
			if( x1 == 0 ){
				long loanTotal = fundsService.findLoanTotalById(loanUserCode);
				int loanCount = fundsService.buildLoanCount(loanUserCode);//正在还款笔数+已还款笔数+系统代还笔数
				boolean z1 = loanInfoService.updateLoanByLent(loanCode,loanUserCode,loanTimeLimit,loanTotal,loanCount);
				if( z1 )
					return succ("满标审核完成!" , null ) ;
				else
					return error("03", "重置流水状态错误!", null ) ;
			}else
				return error("04", "更新借款人资金账户错误!", null ) ;
			*/
		}else{
			unlock(loanCode);
			return error("05", "标审核失败:<br />"  , null ) ;
		}
		unlock(loanCode);

		return succ("结算完成", null ) ;
	}
	
	/**
	 * 遍历标的对应投标记录 生成回款计划
	 * @param loanCode	标号
	 * @return
	 */
	public Message generateRepaymentPlan(String loanCode) {
		List<LoanTrace> lstLoanTrace = loanTraceService.findAllByLoanCode(loanCode);
		for (int j = 0; j < lstLoanTrace.size(); j++) {
			String loanTraceCode = lstLoanTrace.get(j).getStr("traceCode");
			List<LoanRepayment> lstLoanRepayment = loanRepaymentService.findByLoanTrace(loanTraceCode);	// 根据投标记录查询回款计划是否已生成
			if (lstLoanRepayment.size() <= 0) {
				LoanTrace loanTrace = loanTraceService.findById(loanTraceCode);
				LoanInfo loanInfo = loanInfoService.findById(loanTrace.getStr("loanCode"));
				Long payAmount = loanTrace.getLong("payAmount");	// 投标金额
				Integer loanTimeLimit = loanInfo.getInt("loanTimeLimit");	// 标的借款期限
				Integer rateByYear = loanInfo.getInt("rateByYear");	// 年利率
				Integer rewardRateByYear = loanTrace.getInt("rewardRateByYear");	// 奖励年利率
				String refundType = loanInfo.getStr("refundType");	// 还款方式
//				int benefits4new = loanInfo.getInt("benefits4new");	// 新手标奖励利率
//				int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));	// 标的发布日期
				
				long sumRepaymentPrincipal = 0l;
				for (int i = 1; i <= loanTimeLimit; i++) {
					long[] repayment = CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, i, refundType);
					if (i == loanTimeLimit) {
						repayment[0] = payAmount - sumRepaymentPrincipal;
					}
					String payUserCode = loanTrace.get("payUserCode");
					long repaymentPrincipal = repayment[0];
					long repaymentInterest = repayment[1];
					long interestFee = 0l;
					sumRepaymentPrincipal += repaymentPrincipal;
					
					// 首月加息活动计算利息(已过期)
//					if (releaseDate >= 20171111 && releaseDate <= 20171117) {
//						if (i == 1 && benefits4new == 0) {
//							repaymentInterest = CommonUtil.f_004(payAmount, rateByYear + 400, i, refundType)[1];
//						}
//					}

					//	计算利息管理费
					if( repaymentInterest > 0 ){
						User user = userService.findUserAllInfoById(payUserCode) ;
						int vipInterestRate = user.getInt("vipInterestRate") ;	// 利息管理费费率
						int vipRiskRate = user.getInt("vipRiskRate") ;	// 风险储备金费率
						// 利息管理费
						long tmpInterestFee = 0 ;
						if( vipInterestRate > 0 ){
							tmpInterestFee = repaymentInterest * vipInterestRate / 10 / 10 / 10 / 10;
						}
						// 风险储备金费
						long tmpRiskFee = 0 ;
						if( vipRiskRate > 0 ){
							tmpRiskFee = repaymentInterest * vipRiskRate / 10 / 10 / 10 / 10;
							if( tmpRiskFee > 0 ){
								Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1", tmpRiskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
							}
						}
						interestFee = tmpInterestFee + tmpRiskFee ;
					}
					
					Map<String, Object> para = new HashMap<String, Object>();
					para.put("userCode", payUserCode);
					para.put("userName", loanTrace.get("payUserName"));
					para.put("loanNo", loanInfo.get("loanNo"));
					para.put("loanCode", loanInfo.get("loanCode"));
					para.put("payAmount", loanTrace.get("payAmount"));
					para.put("recyPeriod", i);
					para.put("recyStatus", 0);
					para.put("repaymentAmount", repaymentPrincipal + repaymentInterest);
					para.put("repaymentPrincipal", repaymentPrincipal);
					para.put("repaymentInterest", repaymentInterest);
					para.put("interestFee", interestFee);
					para.put("repaymentDate", DateUtil.addMonth(loanInfo.getStr("effectDate"), i));
					para.put("loanTraceCode", loanTrace.get("traceCode"));
					loanRepaymentService.save(para);
				}
			}
		}
		return succ("test", "success");
	}
	
	/**
	 * 	处理单条流水
	 * @param index
	 * @param traces
	 * @param failResult
	 * @return
	 * 		0 - 成功
	 * 		非0 异常
	 */
	@SuppressWarnings("unused")
	public int doLoanTrace(int index ,LoanInfo loan ,  List<LoanTrace> traces  ){
		if( index == traces.size() ){
			return 0 ;
		}
		//理财信息初始化
		LoanTrace loanTrace = traces.get(index) ;
		//标信息初始化
		Integer rateByYear = loan.get("rateByYear");//年利率
		//投标流水的奖励年利率，已经包含了新手利率和自动投标加息,加息券
		Integer rewardRateByYear = loanTrace.get("rewardRateByYear");
//		rateByYear = rateByYear + rewardRateByYear;//年利率+奖励年利率
		Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
//		Integer loanAmount = loan.get("loanAmount");//贷款标总金额
//		int benefits4new = loan.get("benefits4new",0);
		
		index ++ ;	//下一个

		long payAmount = loanTrace.getLong("payAmount");//投标金额
		
		long ticket_amount = 0;
		try {
			/**
			 * TODO 券json解析
			 */
			String json_tickets = loanTrace.getStr("loanTicket");
			if(StringUtil.isBlank(json_tickets)==false){
				JSONArray ja = JSONArray.parseArray(json_tickets);
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jsonObj = ja.getJSONObject(i);
					if(jsonObj.getString("type").equals("A")){
						ticket_amount = ticket_amount + jsonObj.getLong("amount");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		String payUserCode = loanTrace.getStr("payUserCode") ;
		String loanUserCode=loan.getStr("userCode");
		//User user = userService.findById(payUserCode) ;
		//int vipInterestRate = user.getInt("vipInterestRate");	//利息管理费
		//int vipRiskRate = user.getInt("vipRiskRate");			//风险储备金
		//int vipManagerRate = vipInterestRate + vipRiskRate ;	//总管理费
		
		String refundType = loanTrace.getStr("refundType") ;
		String traceCode = loanTrace.getStr("traceCode");
		//单笔投标年利息
		long[] pay_zonglixi = CommonUtil.f_004(payAmount, rateByYear + rewardRateByYear, loanTimeLimit , refundType);	//资金总利息
		//long reward_zonglixi = CommonUtil.f_004(payAmount, rewardRateByYear , loanTimeLimit , refundType ) ; //奖励利息
		//long manager_fee = CommonUtil.f_004( pay_zonglixi, vipManagerRate, loanTimeLimit , refundType ) ;	//总管理费
		
		
		//更新用户资金账户
		//计算用户可增加积分
		long points = CommonUtil.f_005( payAmount , loanTimeLimit , refundType) ;
		if(loan.getInt("benefits4new")>0){
			int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
			points = points * xx;
		}else{
			int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
			points = points * xx;
		}
		//会员生日当天，根据会员等级投资送积分 2018.03
		UserInfo userInfo= userInfoService.findById(payUserCode);
		User user = userService.findById(payUserCode);//投资人
		String userCardId= userInfo.getStr("userCardId");
		boolean flag= CommonUtil.isBirth(userCardId);
		if(flag){
			int vipLevel = user.getInt("vipLevel");
			if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
				points=points*2;
			}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
				points=points*3;
			}
		}
		//TODO 理财人资金流水 投标支出，减去抵扣金额
		//用户存管账户余额解冻并划拨 20170526 ws
		User loanUser=userService.findById(loanUserCode);//借款人
		 
		
		if(null!=user.getStr("loginId")&&!"".equals(user.getStr("loginId"))){
			//解冻资金
			long amount = payAmount-ticket_amount;
			if(amount>0){
				UnFreezeRspData unFreezeRspData = fuiouTraceService.unFreeFunds(user,amount);
				if("0000".equals(unFreezeRspData.getResp_code())){
					User wzuser= userService.findByMobile(CommonUtil.OUTCUSTNO);
					if(null!=loanUser.getStr("loginId")&&!"".equals(loanUser.getStr("loginId"))){
					//划拨
						CommonRspData comm=null;
						if(isrefund==1){
							comm=fuiouTraceService.refund(amount, FuiouTraceType.R,user,loanUser,loan.getStr("loanCode"));}
						if(isrefund==0){
							comm=fuiouTraceService.refund(amount, FuiouTraceType.R,user,wzuser,loan.getStr("loanCode"));
						}
					if("0000".equals(comm.getResp_code())){
						if(ticket_amount>0){
							if(isrefund==1){
						fuiouTraceService.refund(ticket_amount,FuiouTraceType.G,wzuser, loanUser,loan.getStr("loanCode"));}
						}
					}else{
						fuiouTraceService.freeze(user, amount);
						doLoanTrace(index , loan , traces);
					}
					}else{
						CommonRspData comm =fuiouTraceService.refund(amount, FuiouTraceType.R, user,wzuser,loan.getStr("loanCode"));
						if(!"0000".equals(comm.getResp_code())){
							fuiouTraceService.freeze(user, amount);
							doLoanTrace(index , loan , traces);
						}
					}
				}else{
					doLoanTrace(index , loan , traces);
				}
			}
		}
		//end
		Funds funds = fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount-ticket_amount, points ) ;
		
		if(ticket_amount > 0){
			//TODO 同步记录抵用券抵扣金额流水
			fundsServiceV2.updateFundsWhenFullLoanWithTicket(payUserCode, ticket_amount);
		}
		
		//更新投标流水冗余
		loanTrace.set("leftAmount", payAmount ) ;
		loanTrace.set("leftInterest", pay_zonglixi[1] ) ;
		long[] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit , rateByYear + rewardRateByYear , 1 , refundType ) ;
		
		loanTrace.set("nextAmount", nextBenXi[0] ) ;
		loanTrace.set("nextInterest", nextBenXi[1] ) ;
//		loanTrace.set("loanRecyDate", CommonUtil.anyRepaymentDate4string(DateUtil.getNowDate(), 1));
		loanTrace.update() ;
		
		//更新冗余内容
		int upResult = fundsServiceV2.updateBeRecyFunds(payUserCode, loanTimeLimit , payAmount ,//pay_zonglixi[0],
				pay_zonglixi[1] , 0,  0 ) ;
		
		//5月升级活动
//		userService._51ShengJiHuoDong(payUserCode,loanTrace.getStr("loanCode"));
		
		
		if( upResult > 0 ){
			//更新活跃积分
			//userService.updateScore(payUserCode, points) ;
			
//			try {
//				//判断投资人是否有9月1日后的推荐人，有的话
//				//且满足注册5天投资1万及以上，返推荐人100元可用余额;
//				long hasTuiJian = Db.queryLong("select COALESCE(count(rid),0) from t_recommend_info where bUserCode = ? and bRegDate >= '20160901'",payUserCode);
//				if(hasTuiJian > 0){
//					String aUserCode = Db.queryStr("select aUserCode from t_recommend_info where bUserCode = ? and bRegDate >= '20160901'",payUserCode);
//					if(StringUtil.isBlank(aUserCode) == false){
//						long tzfx = Db.queryLong("select COALESCE(count(rewardId),0) from t_recommend_reward where aUserCode = ? and bUserCode = ? and rewardType = 'A'",aUserCode,payUserCode);
//						if(tzfx < 1){
//							String regDate = userService.findByField(payUserCode, "regDate").getStr("regDate");
//							String startDateTime = regDate + "000000";
//							String lastDateTime = DateUtil.addDay(regDate, 5) + "235959";
//							long total_payAmount = Db.queryBigDecimal("select COALESCE(SUM(payAmount),0) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P') and loanDateTime >=? and loanDateTime <= ? and isTransfer != 'B' ",payUserCode,startDateTime,lastDateTime).longValue();
//							String loanCode = loan.getStr("loanCode");
//							long bb = Db.queryBigDecimal("select COALESCE(SUM(payAmount),0) from t_loan_trace where payUserCode = ? and loanCode = ? and isTransfer != 'B'",payUserCode,loanCode).longValue();
//							if( total_payAmount >= (1000000-bb)){
//								fundsServiceV2.rechargeTZFX_HY(aUserCode, 10000, 0, "邀请好友投资奖励");
//								rrService.save(aUserCode, payUserCode, 10000, "A", "好友投资满1万，奖励推荐人100元");
//							}
//						}
//					}
//				}
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally{
//				
//			}
			doLoanTrace(index , loan , traces  );
		}
		
		return 0 ;
	}
	
	//流标 2017 5.31 rain
	@ActionKey("/auditOverLoan")
	@AuthNum(value=999)
	@Before({LoanAuditLoanByLoanValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message auditOverLoan(){

		String loanCode = getPara("loanCode");
		if( StringUtil.isBlank( loanCode ) == true )
			return error("01", "借款标编码异常!", null) ;
		if( loanCode.length() != 32)
			return error("02", "借款标编码异常！", null );
		LoanInfo loan = loanInfoService.findById(loanCode);
		
		if( SysEnum.loanState.J.val().equals( loan.getStr("loanState") ) == false){
			return error("03", "不满足流标审核条件！借款标状态必须为【招标中】",null ) ;
		}
		List<LoanTrace> traces = loanTraceService.findAllByLoanCode( loanCode );
		String loanUserCode=loan.getStr("userCode");
		User loanUser=userService.findById(loanUserCode);
		if(loanUser==null){
			return error("08", "借款人账户信息异常",null ) ;
		}
		String loanUserJxAccountId=loanUser.getStr("jxAccountId");
		String txAmount="";
		long payAmount=0;
		String productId=loan.getStr("loanCode");
		//modified 20180427
		if(traces != null && traces.size() > 0){
			for(LoanTrace trace : traces ){
				//处理标流水
				payAmount=trace.getLong("payAmount");
				txAmount= StringUtil.getMoneyYuan(payAmount);
				String payUserCode = trace.getStr("payUserCode");
				User user=userService.findById(payUserCode);
				if(user==null){
					return error("04", "出借人账户信息异常",null ) ;
				}
				if(StringUtil.isBlank(user.getStr("jxAccountId"))){
					return error("05", "出借人存管账户信息异常",null ) ;
				}
				String jxTraceCode=trace.getStr("jxTraceCode");
				JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				String orderId=parseObject.getString("orderId");
				if(StringUtil.isBlank(orderId)){
					return error("06", "无此流水号对应的标流水",null);
				}
				
				//有投标流水的先撤销投标申请
				Map<String,String> reqMap=JXController.bidCancel(user.getStr("jxAccountId"),txAmount, productId,orderId, getResponse());
			if(JXController.isRespSuc(reqMap)){
				long ticketAmount = 0;
				try {
					/**
					 * TODO 券json解析
					 */
					String json_tickets = trace.getStr("loanTicket");
					if(StringUtil.isBlank(json_tickets)==false){
						JSONArray ja = JSONArray.parseArray(json_tickets);
						for (int i = 0; i < ja.size(); i++) {
							JSONObject jsonObj = ja.getJSONObject(i);
//							if(jsonObj.getString("type").equals("A")){ 
//								ticketAmount = ticketAmount + jsonObj.getLong("amount");
//							}
							
							if (Tickets.rewardRateAomuntTcode.equals(jsonObj.getString("code"))) {
								fundsServiceV2.deductRewardRateAmount(payUserCode, payAmount, 0);
							} else {
								// 代金券回滚
								ticketsService.rollBackTicket(jsonObj.getString("code"));
							}
						}
					}
					
					
				} catch (Exception e) {
					return error("119", "获取奖券时发生异常，不可流标", false);
				}
				long frozeBalance = fundsServiceV2.findFrozeBalanceById(payUserCode);
				if( frozeBalance < (payAmount-ticketAmount)){
					return error("04","冻结余额不足"+payUserCode+"【"+trace.getStr("payUserName")+"】",false);
				}
				try {
					fundsServiceV2.overLoan4funds(payUserCode, payAmount);
				} catch (BaseBizRunTimeException e) {
					return e.getExMsg();
				}
				trace.set("traceState", traceState.H.val());
				trace.set("loanState", loanState.L.val() );
				if(trace.update()){
					try {
						String mobile = userService.getMobile(payUserCode);
						double payAmount2 = CommonUtil.yunsuan(payAmount+"", "100", "chu", 2).doubleValue();
						String content = CommonUtil.SMS_MSG_LIUBIAO.replace("[userName]", trace.getStr("payUserName"))
								.replace("[loanNo]", trace.getStr("loanNo"))
								.replace("[payAmount]", payAmount2+"");
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", content);
						smsLog.set("userCode", trace.getStr("payUserCode"));
						smsLog.set("userName", trace.getStr("payUserName"));
						smsLog.set("type", "14");
						smsLog.set("typeName", "流标");
						smsLog.set("status", 8);
						smsLog.set("sendDate", DateUtil.getNowDate());
						smsLog.set("sendDateTime", DateUtil.getNowDateTime());
						smsLog.set("break", "");
						smsLogService.save(smsLog);
					} catch (Exception e) {
						
					}
				}
			}
			else{
				return error("08","撤销投标申请失败",false);
			}
			}
		}
		//撤销 标的登记
		Map<String,String> cancelMap=JXController.debtRegisterCancel(loanUserJxAccountId, loanCode,loan.getStr("releaseDate"));
		if(JXController.isRespSuc(cancelMap)){
			//更新标状态
			loan.set("updateDate", DateUtil.getNowDate() ) ;
			loan.set("updateTime", DateUtil.getNowTime() ) ;
			loan.set("loanBalance", loan.getLong("loanAmount"));
			loan.set("releaseDate", "");
			loan.set("releaseTime", "");
			loan.set("loanState", loanState.L.val() ) ;
			if( loan.update() == false ){
				return error("06","操作未生效",false);
			}
			Db.update("update t_sms_log set `status`=9 where `status`=8 and type='14' and sendDate = ?",DateUtil.getNowDate());
			return succ("流标处理完成!",true);
		}
		return error("07","操作未生效",false);
	}
	
	@ActionKey("/scanManBiao")
	@Before({Tx.class,PkMsgInterceptor.class})
	public void scanTouBiao(){
		Message msg = manbiaoTask();
//		String returnCode = msg.getReturn_code()  ;
		renderJson(msg);
	}
	
	@SuppressWarnings("unchecked")
	private Message manbiaoTask(){

		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.scanTouBiaoKey");
		if(!key.equals(preKey)){
			return error("01","密匙错误", false );
		}
		try {
			long total = Db.queryLong("select count(uid) from t_loan_info where loanBalance = 0 and loanState='J'");
			int doCount = 1 ;
			scanManBiaoLogger.log(Level.INFO,"[定时任务:自动扫描满标并处理相关信息]扫描中......共计"+total+"已满标...");
			List<Object[]> loanInfos = getLoanInfos() ;
			for(Object[] loanInfo : loanInfos){
				String loanCode = (String) loanInfo[0];
				String loanTitle = (String) loanInfo[1];
				scanManBiaoLogger.log(Level.INFO,"[定时任务:自动扫描满标并处理相关信息],借款["+loanTitle+"]loanCode:["+loanCode+"],第"+doCount+"个...共"+total+"个已满标");
				Record rc = Db.findFirst("select loanDateTime from t_loan_trace where loanCode = ? order by uid desc",loanCode);
				if(rc!=null){
					String lastPayLoanDateTime = rc.getStr("loanDateTime");
					Db.update("update t_loan_info set lastPayLoanDateTime = ? where loanCode = ?",lastPayLoanDateTime,loanCode);
					boolean x = CommonUtil.vilidataManbiao(DateUtil.getNowDateTime(), lastPayLoanDateTime);
					if(x){
						try {
							/*if("de3eeaf51b08469eab53d56e2d092eff".equals(loanCode)){
								System.out.println("跳过异常标:"+loanCode);
								continue;
							}*/
							fuckyou(loanCode);
						} catch (Exception e) {
							scanManBiaoLogger.log(Level.SEVERE,"满标放款异常["+loanCode+"]("+loanTitle+")");
							throw e;
						}
					}
				}
				doCount ++ ;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			scanManBiaoLogger.log(Level.SEVERE,"自动扫描满标并处理相关信息时发生异常"+e.getMessage());
			throw e;
		}
		scanManBiaoLogger.log(Level.INFO,"自动扫描满标并处理相关信息任务完成");
		return succ("自动扫描满标并处理相关信息任务完成", true ) ;
	}
	
	/**
	 * 	获取已满标的标
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getLoanInfos(){
		String querySql = "select loanCode,loanTitle from t_loan_info where loanBalance = 0 and loanState='J'";
//		querySql = querySql.replace("${index}", index+"" ) ;
//		querySql = querySql.replace("${size}", size+"" ) ;
		List loanInfos =  Db.query(querySql ) ;
		return loanInfos ;
	}
	
	/**
	 * 发送批次放款请求 WJW
	 * @param loan		放款标
	 * @param traces	投标流水
	 * @return
	 */
	private void doLoanTraceBatchReq(LoanInfo loan ,  List<LoanTrace> traces){
		JSONArray jsonArray = new JSONArray();//江西银行存管批次放款请求参数
		Long loanAmount = loan.getLong("loanAmount");//借款标金额
		String loanCode = loan.getStr("loanCode");//标号
		long sumPayAmount = 0;//投标总额
		
		List<String> jxTraceCodes = new ArrayList<String>();//本地单个标投标流水全部jxTraceCode
		for (LoanTrace loanTrace : traces) {
			long payAmount = loanTrace.getLong("payAmount");//投标金额
			sumPayAmount += payAmount;
			String payUserCode = loanTrace.getStr("payUserCode") ;
			String authCode = loanTrace.getStr("authCode");
			String jxTraceCode = loanTrace.getStr("jxTraceCode");//投标流水jxTraceCode
			String loanUserCode=loan.getStr("userCode");
			User user = userService.findById(payUserCode);//投资人
			String accountId = user.getStr("jxAccountId");
			if(StringUtil.isBlank(accountId)){
				scanManBiaoLogger.log(Level.INFO,"投资人userCode["+payUserCode+"]的jxAccountId不存在");
				return;
			}
			
			User loanUser=userService.findById(loanUserCode);//借款人
			String forAccountId = loanUser.getStr("jxAccountId");
			if(StringUtil.isBlank(forAccountId)){
				scanManBiaoLogger.log(Level.INFO,"借款人userCode["+loanUserCode+"]的jxAccountId不存在");
				return;
			}
			
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("accountId", accountId);//投资人电子账号
			jsonObject.put("txAmount", payAmount);//交易金额(投标金额)
			jsonObject.put("forAccountId", forAccountId);//融资人账户
			jsonObject.put("productId", loanCode);//标号
			jsonObject.put("authCode", authCode);
			jsonArray.add(jsonObject);
			
			jxTraceCodes.add(jxTraceCode);
		}
		
		//江西多出流水删除处理
		String releaseDate = loan.getStr("releaseDate");//标发布日期
		List<JXTrace> jxTraces = jxTraceService.queryBidApplyByDate(releaseDate, DateUtil.getNowDate());//查询时间段内投标流水
		List<String> orderIds = new ArrayList<String>();//loanTrace对应投标成功订单号
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			if(jxTraceCodes.indexOf(jxTraceCode) != -1){
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				String orderId = parseObject.getString("orderId");
				orderIds.add(orderId);
			}
		}
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String requestMessage = jxTrace.getStr("requestMessage");//请求报文
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String accountId = parseObject.getString("accountId");//投标人电子账号
			String orgOrderId = parseObject.getString("orderId");//原订单号
			String productId = parseObject.getString("productId");//标号
			String txAmount = parseObject.getString("txAmount");//投标金额
			if(!loanCode.equals(productId)){//不是当前放款标流水
				continue;
			}
			Map<String, String> bidApplyQuery = JXQueryController.bidApplyQuery(accountId, orgOrderId);//查询投标结果
 			String state = bidApplyQuery.get("state");//投标状态
			if("1".equals(state)){//投标中(已投上)
				if(orderIds.indexOf(orgOrderId) == -1){//江西多出成功投标流水
					Map<String, String> bidCancel = JXController.bidCancel(accountId, txAmount, productId, orgOrderId, getResponse());//投标撤销处理
					if("00000000".equals(bidCancel.get("retCode"))){
						scanManBiaoLogger.log(Level.INFO,"[满标放款]江西多出投标流水删除成功,jxTraceCode:"+jxTraceCode);
					}else {
						scanManBiaoLogger.log(Level.INFO,"[满标放款]江西多出投标流水删除失败,jxTraceCode:"+jxTraceCode);
						return;
					}
				}
			}
		}
		
		//投标总金额与借款标金额不符
		if(loanAmount != sumPayAmount){
			scanManBiaoLogger.log(Level.INFO,"[满标放款]借款标资金与投标总额不符,loanCode:"+loan.getStr("loanCode"));
			return;
		}
		
		int batchNo = jxTraceService.batchNoByToday();//批次号
		Map<String, String> batchLendPay = JXController.batchLendPay(batchNo, loanAmount, jsonArray.toJSONString());
		boolean isReceived = "success".equals(batchLendPay.get("received"));
		scanManBiaoLogger.log(Level.INFO,"[批次号:"+batchNo+"批次放款请求发送"+(isReceived?"成功":"失败")+"...]");
	}
	
	/**
	 * 失败放款批次处理 WJW
	 * @param txDate	交易日期
	 * @param batchNo 	批次号
	 */
	@ActionKey("/failureLoanBatch")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class,Tx.class})
	public Message failureLoanBatch(){
		String txDate = getPara("txDate",DateUtil.getNowDate());//交易日期
		List<JXTrace> jxTraces = jxTraceService.queryByTxCodeAndTxDate("batchLendPay", txDate);//根据日期查询江西批次放款交易流水
		
		JSONArray jsonArray = new JSONArray();//江西银行存管失败批次放款参数(可能包含成功)
		JSONArray jsonArrayReq = new JSONArray();//江西银行存管失败批次放款请求参数(全失败)
		List<String> authCodes = new ArrayList<String>();//防止重复处理authCode集
		List<String> authCodeSucs = new ArrayList<String>();//处理成功authCode集
		List<String> authCodeErrs = new ArrayList<String>();//处理失败authCode集
		List<String> authCodeIngs = new ArrayList<String>();//处理中authCode集
		long sumTxAmount = 0;//交易总金额
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//流水处理状态
			if(jxTraceState == 6){//全部成功不处理
				continue;
			}
			
			String requestMessage = jxTrace.getStr("requestMessage");//请求报文
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");//批次号
			
			Map<String, Object> batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");//批次放款存管处理明细
			if(batchDetailsQueryAll == null){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
				continue;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]"+retMsg);
				continue;
			}
			List<Map<String, String>> subPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			
			//查询authCode目前处理状态,防止authCode后续错误处理
			for (int i = 0; i < subPacks.size(); i++) {
				Map<String, String> subPack = subPacks.get(i);//放款单笔记录
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
					
			//将批次放款中处理失败的流水重新打包批次发送
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
					
					sumTxAmount += txAmount;//失败金额汇总
					
					JSONObject jsonObjectReq = new JSONObject();
					jsonObjectReq.put("accountId", accountId);//投资人电子账号
					jsonObjectReq.put("txAmount", txAmount);//交易金额(投标金额)
					jsonObjectReq.put("forAccountId", forAccountId);//融资人账户
					jsonObjectReq.put("productId", productId);//标号
					jsonObjectReq.put("authCode", authCode);
					jsonArray.add(jsonObjectReq);
				}
			}
		}
		
		//将重新打包批次中，剔除已成功流水或正在处理中，向jsonArrayReq写入全失败流水
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String authCode = jsonObject.getString("authCode");
			
			if(authCodeSucs.indexOf(authCode) != -1 || authCodeIngs.indexOf(authCode) != -1){
				sumTxAmount -= Long.valueOf(jsonObject.getString("txAmount"));
			}else {
				jsonArrayReq.add(jsonObject);
			}
		}
		
		if(jsonArrayReq.size() < 1){
			return error("03", "未扫描到失败批次放款流水", "");
		}
		
		int batchNoReq = jxTraceService.batchNoByToday();//批次号
		Map<String, String> batchLendPay = JXController.batchLendPay(batchNoReq, sumTxAmount, jsonArrayReq.toJSONString());
		boolean isReceived = "success".equals(batchLendPay.get("received"));
		scanManBiaoLogger.log(Level.INFO,"[批次号:"+batchNoReq+"批次放款请求发送"+(isReceived?"成功":"失败")+"...]");
		return succ("批次放款请求发送"+(isReceived?"成功":"失败"), "batchNo:"+batchNoReq);
	}
	
	/**
	 * 批次放款合法性校验通知 WJW
	 */
	@ActionKey("/batchLendPayNotifyURL")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void batchLendPayNotifyURL(){
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
		
		boolean updateResponseMessage = jxTraceService.updateResponseMessage(bgData, jxTraceCode);
		scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"数据合法性校验]响应报文更新"+(updateResponseMessage?"成功":"失败"));
		
	}
	
	/**
	 * 处理批次放款流水 WJW
	 * batchNo:人工处理批次号
	 * txDate:人工处理批次日期(未赋值默认当日)
	 */
	@ActionKey("/doLoanTraceBatchCallback")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public void doLoanTraceBatchCallback(){
		String bgData = getRequest().getParameter("bgData");
		String batchNo = "";//批次号
		String txDate = "";//交易日期
		String jxTraceCode = "";//t_jx_trace流水号
		if(StringUtil.isBlank(bgData)){//bgData为空(适用即信未返回异步信息,本地手动处理批次流水)
			if(true){
				return;
			}
			batchNo = getPara("batchNo");
			txDate = getPara("txDate",DateUtil.getNowDate());
			if(StringUtil.isBlank(batchNo)){//人工操作未输入批次号或即信返回bgData为空
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水,人工操作未输入批次号或即信返回bgData为空]");
				return;
			}
			JXTrace jxTrace = jxTraceService.findByBatchNoAndTxDate(txDate, batchNo);
			if(jxTrace == null){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水,批次号不存在]");
				return;
			}
			jxTraceCode = jxTrace.getStr("jxTraceCode");
			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//jxTrace流水处理状态
			if(6 != jxTraceState){
				boolean result = jxTraceService.updateResponseMessage("admin", jxTraceCode);
				scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]响应报文更新"+(result?"成功":"失败"));
			}
		}else {//正常处理即信流水
			renderText("success");//bgData接收成功,返回success至江西,停止骚扰
			
			Map<String, Object> responseMap = JSONObject.parseObject(bgData);
			txDate = String.valueOf(responseMap.get("txDate"));//交易日期
			String txTime = String.valueOf(responseMap.get("txTime"));//交易时间
			String seqNo = String.valueOf(responseMap.get("seqNo"));//交易流水号
			jxTraceCode = txDate + txTime + seqNo;//t_jx_trace流水号
			
			int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);//jxTrace流水处理状态
			if(jxTraceState != 1 && jxTraceState != 2 && jxTraceState !=3){
				return;
			}
			
			Map<String, String> map = net.sf.json.JSONObject.fromObject(bgData);
			// 生成本地报文流水号
			String jxCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
			// 将响应报文存入数据库
			boolean result = JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
			scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]响应报文更新"+(result?"成功":"失败"));
			
			if(isLoan){//放款调试开关true:开启,仅更新响应报文
				return;
			}
			
			batchNo = String.valueOf(responseMap.get("batchNo"));//批次号
		}
		
		Map<String, Object> batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");//批次放款存管处理明细
		if(batchDetailsQueryAll == null){
			scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
			return;
		}
		
		String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
		String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
		if(!"00000000".equals(retCode)){
			scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]"+retMsg);
			return;
		}
		List<Map<String, String>> subPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
		
		LoanInfo loan = null;//借款标
		String loanCode = "";//标号
		HashMap<String, Boolean> isTxStateMap = new HashMap<>();//key:loanCode,value:(true:允许结算,false:不允许结算)
		for (int i = 0; i < subPacks.size(); i++) {
			Map<String, String> subPack = subPacks.get(i);//单笔放款流水
			String authCode = subPack.get("authCode");//订单号
			String txState = subPack.get("txState");//交易状态(S-成功,F-失败,A-待处理,D-正在处理,C-撤销)
			String failMsg = subPack.get("failMsg");//失败描述(txState为F时有效)
			loanCode = subPack.get("productId");//标号
			
			if(StringUtil.isBlank(loanCode)){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]:loanCode为空");
				return;
			}
			
			if(isTxStateMap.containsKey(loanCode)){
				if(!"S".equals(txState)){//非成功状态
					isTxStateMap.put(loanCode, false);//不允许资金结算
				}
			}else {
				if("S".equals(txState)){//成功状态
					isTxStateMap.put(loanCode, true);//允许资金结算
				}else {
					isTxStateMap.put(loanCode, false);//禁止允许资金结算
				}
			}
			
			if(isTxStateMap.size() > 1 || i == 0){//避免进行同一标处理时,重复查询数据库
				loan = loanInfoService.findById(loanCode);//借款标
				if(loan == null){
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]标不存在loanCode:"+loanCode);
					return;
				}
			}
			
			LoanTrace loanTrace = loanTraceService.findByAuthCode(authCode);//单笔投标记录
			if(loanTrace == null){
				scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]投标授权码不存在:"+authCode);
				continue;
			}
			if("S".equals(txState)){//交易成功
				//避免标放款中途失败,重新更新时,处理已更新流水
				Long nextAmount = loanTrace.getLong("nextAmount");
				Long nextInterest = loanTrace.getLong("nextInterest");
				if((nextAmount != null && nextAmount != 0) || (nextInterest != null && nextInterest != 0)){//该loanTrace已处理过
					continue;
				}
				
				//标信息初始化
				Integer rateByYear = loan.get("rateByYear");//年利率
				//投标流水的奖励年利率，已经包含了新手利率和自动投标加息,加息券
				Integer rewardRateByYear = loanTrace.get("rewardRateByYear");
				Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
				long payAmount = loanTrace.getLong("payAmount");//投标金额
				
				long ticket_amount = 0;
				try {
					/**
					 * TODO 券json解析
					 */
					String json_tickets = loanTrace.getStr("loanTicket");
					if(StringUtil.isBlank(json_tickets)==false){
						JSONArray ja = JSONArray.parseArray(json_tickets);
						for (int j = 0; j < ja.size(); j++) {
							JSONObject jsonObj = ja.getJSONObject(j);
							if(jsonObj.getString("type").equals("A")){
								ticket_amount = ticket_amount + jsonObj.getLong("amount");
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				
				String payUserCode = loanTrace.getStr("payUserCode") ;
				String loanType = loanTrace.getStr("loanType");//投标方式
				User user = userService.findById(payUserCode);//投资人
				if(user == null){
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水]投资人["+payUserCode+"]自动投标现金券返现红包"+(ticket_amount*0.01)+"元,发放失败");
				}
				
				//投标使用现金券,返对应红包金额至投资人 WJW
				if(ticket_amount > 0){
//					if("A".equals(loanType)){//自动投标
						String forAccountId = user.getStr("jxAccountId");//投资人电子账号
						if(StringUtil.isBlank(forAccountId)){
							scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水]投资人["+payUserCode+"]jxAccountId为空");
						}
						//发红包至投资人
						Map<String, String> voucherPay = JXController.voucherPay(JXService.RED_ENVELOPES, ticket_amount, forAccountId, "1", "现金券抵扣["+loanTrace.getStr("traceCode")+"]");
						if(voucherPay != null && "00000000".equals(voucherPay.get("retCode"))){
							//添加现金券抵扣红包返现流水
							fundsServiceV2.doAvBalance4biz(payUserCode, ticket_amount , 0 ,  traceType.Cashback , fundsType.J , "自动投标现金券返现");
						}
//						}else {//非自动投标
							//同步记录抵用券抵扣金额流水
//							fundsServiceV2.updateFundsWhenFullLoanWithTicket(payUserCode, ticket_amount);
//						}
				}
				
				String refundType = loanTrace.getStr("refundType") ;
				//单笔投标年利息
				long[] pay_zonglixi = CommonUtil.f_004(payAmount, rateByYear + rewardRateByYear, loanTimeLimit , refundType);	//资金总利息
				
				
				//更新用户资金账户
				//计算用户可增加积分
				long points = CommonUtil.f_005( payAmount , loanTimeLimit , refundType) ;
				if(loan.getInt("benefits4new")>0){
					int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
					points = points * xx;
				}else{
					int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
					points = points * xx;
				}
				//会员生日当天，根据会员等级投资送积分 2018.03
				UserInfo userInfo= userInfoService.findById(payUserCode);
				String userCardId= userInfo.getStr("userCardId");
				boolean flag= CommonUtil.isBirth(userCardId);
				if(flag){
					int vipLevel = user.getInt("vipLevel");
					if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
						points=points*2;
					}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
						points=points*3;
					}
				}
//				//理财人资金流水 投标支出，减去抵扣金额
//				if("A".equals(loanType)){//自动投标,使用的现金抵用券通过红包返现
					fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount, points ) ;
//				}else {
//					fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount-ticket_amount, points ) ;
//				}
				
				//更新投标流水冗余
				loanTrace.set("leftAmount", payAmount ) ;
				loanTrace.set("leftInterest", pay_zonglixi[1] ) ;
				long[] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit , rateByYear + rewardRateByYear , 1 , refundType ) ;
				
				loanTrace.set("nextAmount", nextBenXi[0] ) ;
				loanTrace.set("nextInterest", nextBenXi[1] ) ;
				loanTrace.update() ;
				
				//更新冗余内容
				int upResult = fundsServiceV2.updateBeRecyFunds(payUserCode, loanTimeLimit , payAmount ,//pay_zonglixi[0],
						pay_zonglixi[1] , 0,  0 ) ;
				
			}else if ("F".equals(txState)) {//交易失败
				scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]authCode:["+authCode+"]"+failMsg);
			}
		}
		
		fundsClearing(isTxStateMap);//资金结算
	}
	
	/**
	 * 投标流水处理成功,进行资金结算(必需单个标对应所有流水处理完成) WJW
	 * @param isTxStateMap	Map<String, Boolean>
	 */
	public void fundsClearing(Map<String, Boolean> isTxStateMap){
		for(Map.Entry<String, Boolean> entry:isTxStateMap.entrySet()){
			String loanCode = entry.getKey();//标号
			boolean isTxState = entry.getValue();//资金结算开关
			
			if(isTxState){//单个标所有投标流水处理成功进行结算
				LoanInfo loan = loanInfoService.findById(loanCode);//借款标
				String loanUserCode = loan.getStr("userCode") ;
				User loanUser = userService.findById(loanUserCode);//借款人
				Integer rateByYear = loan.get("rateByYear");//年利率
				int rewardRateByYear = loan.getInt("rewardRateByYear");
				int benefits4new = loan.getInt("benefits4new");
				rateByYear = rateByYear + rewardRateByYear + benefits4new;
				long loanAmount = loan.getLong("loanAmount");//贷款标总金额
				Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
				String refundType = loan.getStr("refundType");
				long[] zonglixi = CommonUtil.f_004( loanAmount , rateByYear , loanTimeLimit , refundType) ;
				//交易管理费默认给0,需要考虑加入计算中
				Funds zz = null;
				try {
					zz = fundsServiceV2.doAvBalance4biz(loanUserCode , zonglixi[0], 0, traceType.S, fundsType.J, "招标成功,满标审核放款。");
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人资金账户发生异常");
					return;
				}
				if(zz==null){
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人资金账户发生异常");
					return;
				}
				int doResult = fundsServiceV2.updateBeRecyFunds4loan(loanUserCode, loanTimeLimit , zonglixi[0],
						zonglixi[1] , 0 ) ;
				
				if(doResult > 0 ){

					boolean z1 = loanInfoService.updateLoanByLent(loanCode,loanUserCode,loanTimeLimit );
					if( z1 == true ){
						//结算完成
						//return succ("结算完成", null ) ;
						//生成还款计划
						long leftRecyPrincipal = loanAmount;//剩余待还本金
						long leftRecyInterest = zonglixi[1];//剩余待还利息
						LiCai ff = new LiCai( loanAmount , rateByYear, loanTimeLimit );
						List<Map<String , Long>> xxx = null;
						if(refundType.equals("A")){
							xxx = ff.getDengEList() ;
						}else if(refundType.equals("B")){
							 xxx = ff.getDengXiList();
						}
						for (int j = 1; j <= xxx.size(); j++) {
							Map<String,Long> ck = xxx.get(j-1);
							long ben = ck.get("ben");
							long xi = ck.get("xi");
							Map<String,Object> params = new HashMap<String, Object>();
							String backDate = CommonUtil.anyRepaymentDate4string(DateUtil.getNowDate(), j);
							params.put("backDate", backDate);
							params.put("recyLimit", j);
							if(ben < 0)
								ben = 0;
							if(xi < 0)
								xi = 0;
							params.put("recyPrincipal",ben);
							params.put("recyInterest", xi);
							params.put("realRecyPrincipal", 0);
							params.put("realRecyInterest", 0);
							leftRecyPrincipal = leftRecyPrincipal - ben;
							leftRecyInterest = leftRecyInterest - xi;
							if(leftRecyPrincipal<0)
								leftRecyPrincipal = 0;
							if(leftRecyInterest<0)
								leftRecyInterest = 0;
							if(j==loanTimeLimit){
								leftRecyPrincipal = 0;
								leftRecyInterest = 0;
							}
							params.put("leftRecyPrincipal", leftRecyPrincipal);
							params.put("leftRecyInterest", leftRecyInterest);
							params.put("beRecyPrincipal", loanAmount);
							params.put("beRecyInterest", zonglixi[1]);
							params.put("settlementState", SysEnum.settlementState.A.val());
							settlementPlanService.save(loan, params);
						}
						
					}
				}else{
					unlock(loanCode);
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人账户异常");
					return;
				}
				
				generateRepaymentPlan(loanCode);//生成回款计划
			}else{
				unlock(loanCode);
				scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]标审核失败:<br />");
				return;
			}

			unlock(loanCode);
			scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]结算完成");
		}
	}
	
	/**
	 * 更新满标放款流水(批次) WJW
	 * @param loanCode
	 * @param jxTraces	该标发送批次代偿记录
	 */
	public void doLoanTrace(String loanCode,List<JXTrace> jxTraces){
		for (JXTrace jxTrace : jxTraces) {
			String remark = jxTrace.getStr("remark");
			if("y".equals(remark)){//该批次放款已处理
				continue;
			}
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String txDate = jxTrace.getStr("txDate");
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");
			
			Map<String, Object> batchDetailsQueryAll = null;
			try {
				batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");//批次放款存管处理明细
			} catch (Exception e) {
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]查询批次明细通道异常");
				return;
			}
			
			if(batchDetailsQueryAll == null){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]批次查询失败batchNo:"+batchNo);
				return;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				scanManBiaoLogger.log(Level.INFO, "[处理批次放款流水jxTraceCode:"+jxTraceCode+"]"+retMsg);
				return;
			}
			List<Map<String, String>> subPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			
			LoanInfo loan = null;//借款标
			HashMap<String, Boolean> isTxStateMap = new HashMap<>();//key:loanCode,value:(true:允许结算,false:不允许结算)
			for (int i = 0; i < subPacks.size(); i++) {
				Map<String, String> subPack = subPacks.get(i);//单笔放款流水
				String authCode = subPack.get("authCode");//订单号
				String txState = subPack.get("txState");//交易状态(S-成功,F-失败,A-待处理,D-正在处理,C-撤销)
				String failMsg = subPack.get("failMsg");//失败描述(txState为F时有效)
				String productId = subPack.get("productId");//标号
				
				if(!loanCode.equals(productId)){//该批次可能为多个标批次
					continue;
				}
				
				if(isTxStateMap.containsKey(loanCode)){
					if(!"S".equals(txState)){//非成功状态
						isTxStateMap.put(loanCode, false);//不允许资金结算
					}
				}else {
					if("S".equals(txState)){//成功状态
						isTxStateMap.put(loanCode, true);//允许资金结算
					}else {
						isTxStateMap.put(loanCode, false);//禁止允许资金结算
					}
				}
				
				if(isTxStateMap.size() > 1 || i == 0){//避免进行同一标处理时,重复查询数据库
					loan = loanInfoService.findById(loanCode);//借款标
					if(loan == null){
						scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]标不存在loanCode:"+loanCode);
						return;
					}
				}
				
				LoanTrace loanTrace = loanTraceService.findByAuthCode(authCode);//单笔投标记录
				if(loanTrace == null){
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]投标授权码不存在:"+authCode);
					continue;
				}
				if("S".equals(txState)){//交易成功
					//避免标放款中途失败,重新更新时,处理已更新流水
					Long nextAmount = loanTrace.getLong("nextAmount");
					Long nextInterest = loanTrace.getLong("nextInterest");
					if((nextAmount != null && nextAmount != 0) || (nextInterest != null && nextInterest != 0)){//该loanTrace已处理过
						continue;
					}
					
					//标信息初始化
					Integer rateByYear = loan.get("rateByYear");//年利率
					//投标流水的奖励年利率，已经包含了新手利率和自动投标加息,加息券
					Integer rewardRateByYear = loanTrace.get("rewardRateByYear");
					Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
					long payAmount = loanTrace.getLong("payAmount");//投标金额
					
					long ticket_amount = 0;
					try {
						/**
						 * TODO 券json解析
						 */
						String json_tickets = loanTrace.getStr("loanTicket");
						if(StringUtil.isBlank(json_tickets)==false){
							JSONArray ja = JSONArray.parseArray(json_tickets);
							for (int j = 0; j < ja.size(); j++) {
								JSONObject jsonObj = ja.getJSONObject(j);
								if(jsonObj.getString("type").equals("A")){
									ticket_amount = ticket_amount + jsonObj.getLong("amount");
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
					
					String payUserCode = loanTrace.getStr("payUserCode") ;
					String loanType = loanTrace.getStr("loanType");//投标方式
					User user = userService.findById(payUserCode);//投资人
					if(user == null){
						scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水]投资人["+payUserCode+"]自动投标现金券返现红包"+(ticket_amount*0.01)+"元,发放失败");
					}
					
					//投标使用现金券,返对应红包金额至投资人 WJW
					if(ticket_amount > 0){
//						if("A".equals(loanType)){//自动投标
							String forAccountId = user.getStr("jxAccountId");//投资人电子账号
							if(StringUtil.isBlank(forAccountId)){
								scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水]投资人["+payUserCode+"]jxAccountId为空");
							}
							//发红包至投资人
							Map<String, String> voucherPay = JXController.voucherPay(JXService.RED_ENVELOPES, ticket_amount, forAccountId, "1", "现金券抵扣["+loanTrace.getStr("traceCode")+"]");
							if(voucherPay != null && "00000000".equals(voucherPay.get("retCode"))){
								//添加现金券抵扣红包返现流水
								fundsServiceV2.doAvBalance4biz(payUserCode, ticket_amount , 0 ,  traceType.Cashback , fundsType.J , "自动投标现金券返现");
							}
//							}else {//非自动投标
								//同步记录抵用券抵扣金额流水
//								fundsServiceV2.updateFundsWhenFullLoanWithTicket(payUserCode, ticket_amount);
//							}
					}
					
					String refundType = loanTrace.getStr("refundType") ;
					//单笔投标年利息
					long[] pay_zonglixi = CommonUtil.f_004(payAmount, rateByYear + rewardRateByYear, loanTimeLimit , refundType);	//资金总利息
					
					
					//更新用户资金账户
					//计算用户可增加积分
					long points = CommonUtil.f_005( payAmount , loanTimeLimit , refundType) ;
					if(loan.getInt("benefits4new")>0){
						int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
						points = points * xx;
					}else{
						int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
						points = points * xx;
					}
					//会员生日当天，根据会员等级投资送积分 2018.03
					UserInfo userInfo= userInfoService.findById(payUserCode);
					String userCardId= userInfo.getStr("userCardId");
					boolean flag= CommonUtil.isBirth(userCardId);
					if(flag){
						int vipLevel = user.getInt("vipLevel");
						if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
							points=points*2;
						}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
							points=points*3;
						}
					}
//					//理财人资金流水 投标支出，减去抵扣金额
//					if("A".equals(loanType)){//自动投标,使用的现金抵用券通过红包返现
						fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount, points ) ;
//					}else {
//						fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount-ticket_amount, points ) ;
//					}
					
					//更新投标流水冗余
					loanTrace.set("leftAmount", payAmount ) ;
					loanTrace.set("leftInterest", pay_zonglixi[1] ) ;
					long[] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit , rateByYear + rewardRateByYear , 1 , refundType ) ;
					
					loanTrace.set("nextAmount", nextBenXi[0] ) ;
					loanTrace.set("nextInterest", nextBenXi[1] ) ;
					loanTrace.update() ;
					
					//更新冗余内容
					int upResult = fundsServiceV2.updateBeRecyFunds(payUserCode, loanTimeLimit , payAmount ,//pay_zonglixi[0],
							pay_zonglixi[1] , 0,  0 ) ;
					
				}else if ("F".equals(txState)) {//交易失败
					scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水jxTraceCode:"+jxTraceCode+"]authCode:["+authCode+"]"+failMsg);
				}
			}
			
			fundsClearing(isTxStateMap);//资金结算
			jxTraceService.updateRemark("y", jxTraceCode);//更新批次处理状态
		}
	}
	
	/**
	 * 撤销标多出投标流水 WJW
	 * @param loan
	 * @param loanTraces
	 * @param response
	 * @return
	 */
	public boolean loanTraceExcessBidCancel(LoanInfo loan,List<LoanTrace> loanTraces,HttpServletResponse response){
		String loanCode = loan.getStr("loanCode");
		String releaseDate = loan.getStr("releaseDate");//标发布日期
		
		//查询投标流水对应orderId
		List<String> loanTraceOrderIds = new ArrayList<String>();
		for (LoanTrace loanTrace : loanTraces) {
			String jxTraceCode = loanTrace.getStr("jxTraceCode");
			JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String orgOrderId = parseObject.getString("orderId");
			loanTraceOrderIds.add(orgOrderId);
		}
		
		//根据loanTraceOrderIds,撤销多余成功投标流水
		List<JXTrace> queryBidApplyByLoanCode = jxTraceService.queryBidApplyByDateAndLoanCode(releaseDate,loanCode);
		for (JXTrace jxTrace : queryBidApplyByLoanCode) {
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String orgOrderId = parseObject.getString("orderId");
			if(loanTraceOrderIds.indexOf(orgOrderId) == -1){//不在已记录投标流水中
				String accountId = parseObject.getString("accountId");
				String txAmount = parseObject.getString("txAmount");
				Map<String, String> bidApplyQuery = JXQueryController.bidApplyQuery(accountId, orgOrderId);
				String state = bidApplyQuery.get("state");//投标状态	1:投标中, 2:计息中,4:本息已返还,9:已撤销
				if("1".equals(state)){
					Map<String, String> bidCancel = JXController.bidCancel(accountId, txAmount, loanCode, orgOrderId, response);
					String retCode = bidCancel.get("retCode");
					if(!"00000000".equals(retCode)){
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 满标自动放款 WJW
	 * @param loan
	 * @return	retCode/null
	 */
	public String autoLendPay(LoanInfo loan){
		String loanCode = loan.getStr("loanCode");
		//根据标号查询自动放款记录(不含无效已处理记录)
		List<JXTrace> autoLendPays = jxTraceService.queryAutoLendPayNotFailByLoanCode(loanCode);
		if(autoLendPays.size() < 1){//不存在有效满标自动放款记录
			String loanUserCode = loan.getStr("userCode");
			User loanUser = userService.findById(loanUserCode);
			String accountId = loanUser.getStr("jxAccountId");
			String orderId = CommonUtil.genShortUID();//订单号
			long txAmount = loan.getLong("loanAmount");
			Map<String, String> autoLendPay = null;
			try {
				long feeAmount = "E".equals(loan.getStr("productType"))?loan.getInt("serviceFees"):0;//手续费
				//发送满标自动放款请求
				autoLendPay = JXController.autoLendPay(accountId, orderId, txAmount, feeAmount, loanCode);
				return autoLendPay.get("retCode");
			} catch (Exception e) {//满标放款异常,主动查询放款结果
				Map<String, String> autoLendPayQuery = null;
				try {
					autoLendPayQuery = JXQueryController.autoLendPayQuery(accountId, orderId, loanCode);
					String retCode = autoLendPayQuery.get("retCode");
					//满标放款记录不存在(通道异常),设置此条满标自动放款记录无效
					if("CA110691".equals(retCode)){
						List<JXTrace> jxTraces = jxTraceService.queryAutoLendPayNotFailByLoanCode(loanCode);
						if(jxTraces.size() != 1){
							return null;
						}
						String jxTraceCode = jxTraces.get(0).getStr("jxTraceCode");
						jxTraceService.updateRemark("y", jxTraceCode);
					}
					
					return retCode;
				} catch (Exception e2) {//满标放款查询通道异常
					return null;
				}
			}
		}else {//存在有效满标自动放款记录,主动查询满标自动放款状态
			if(autoLendPays.size() != 1){
				return null;
			}
			JXTrace jxTrace = autoLendPays.get(0);
			if(RetCodeUtil.isSuccRetCode(jxTrace.getStr("retCode"))){
				return jxTrace.getStr("retCode");
			}
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String accountId = parseObject.getString("accountId");
			String lendPayOrderId = parseObject.getString("orderId");
			Map<String, String> autoLendPayQuery = null;
			try {
				autoLendPayQuery = JXQueryController.autoLendPayQuery(accountId, lendPayOrderId, loanCode);
				String retCode = autoLendPayQuery.get("retCode");
				if("CA110691".equals(retCode)){//满标放款记录不存在
					String jxTraceCode = autoLendPays.get(0).getStr("jxTraceCode");
					jxTraceService.updateRemark("y", jxTraceCode);//设置原满标自动放款记录无效
				}
				return retCode;
			} catch (Exception e) {//满标放款查询通道异常
				return null;
			}
		}
	}

	/**
	 * 满标放款更新loanTrace及投资人资金 WJW
	 * @param loan
	 * @param loanTraces
	 * @return
	 */
	public boolean doLoanTrace(LoanInfo loan,List<LoanTrace> loanTraces){
		for (LoanTrace loanTrace : loanTraces) {
			//避免标放款中途失败,重新更新时,处理已更新流水
			Long nextAmount = loanTrace.getLong("nextAmount");
			Long nextInterest = loanTrace.getLong("nextInterest");
			if((nextAmount != null && nextAmount != 0) || (nextInterest != null && nextInterest != 0)){//该loanTrace已处理过
				continue;
			}
			
			Integer rateByYear = loan.get("rateByYear");//年利率
			//投标流水的奖励年利率，已经包含了新手利率和自动投标加息,加息券
			Integer rewardRateByYear = loanTrace.get("rewardRateByYear");
			Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
			long payAmount = loanTrace.getLong("payAmount");//投标金额
			
			long ticket_amount = 0;
			try {
				/**
				 * TODO 券json解析
				 */
				String json_tickets = loanTrace.getStr("loanTicket");
				if(StringUtil.isBlank(json_tickets)==false){
					JSONArray ja = JSONArray.parseArray(json_tickets);
					for (int j = 0; j < ja.size(); j++) {
						JSONObject jsonObj = ja.getJSONObject(j);
						if(jsonObj.getString("type").equals("A")){
							ticket_amount = ticket_amount + jsonObj.getLong("amount");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			
			String payUserCode = loanTrace.getStr("payUserCode") ;
//			String loanType = loanTrace.getStr("loanType");//投标方式
			User user = userService.findById(payUserCode);//投资人
			if(user == null){
				scanManBiaoLogger.log(Level.INFO,"[满标放款]投资人["+payUserCode+"]自动投标现金券返现红包"+(ticket_amount*0.01)+"元,发放失败");
			}
			
			//投标使用现金券,返对应红包金额至投资人 WJW
			if(ticket_amount > 0){
				String forAccountId = user.getStr("jxAccountId");//投资人电子账号
				//发红包至投资人
				Map<String, String> voucherPay = null;
				try {
					voucherPay = JXController.voucherPay(JXService.RED_ENVELOPES, ticket_amount, forAccountId, "1", "现金券抵扣["+loanTrace.getStr("traceCode")+"]");
				} catch (Exception e) {
					scanManBiaoLogger.log(Level.INFO,"[满标放款]投资人["+payUserCode+"]现金券抵扣红包￥:"+StringUtil.getMoneyYuan(ticket_amount)+"元发送失败");
				}
				if(voucherPay != null && "00000000".equals(voucherPay.get("retCode"))){
					//添加现金券抵扣红包返现流水
					fundsServiceV2.doAvBalance4biz(payUserCode, ticket_amount , 0 ,  traceType.Cashback , fundsType.J , "自动投标现金券返现");
				}
			}
			
			String refundType = loanTrace.getStr("refundType") ;
			//单笔投标年利息
			long[] pay_zonglixi = CommonUtil.f_004(payAmount, rateByYear + rewardRateByYear, loanTimeLimit , refundType);	//资金总利息
			
			
			//更新用户资金账户
			//计算用户可增加积分
			long points = CommonUtil.f_005( payAmount , loanTimeLimit , refundType) ;
			if(loan.getInt("benefits4new")>0){
				int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
				points = points * xx;
			}else{
				int xx = Integer.valueOf((String)CACHED.get("S3.xsjfbs"));
				points = points * xx;
			}
			//会员生日当天，根据会员等级投资送积分 2018.03
			UserInfo userInfo= userInfoService.findById(payUserCode);
			String userCardId= userInfo.getStr("userCardId");
			boolean flag= CommonUtil.isBirth(userCardId);
			if(flag){
				int vipLevel = user.getInt("vipLevel");
				if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
					points=points*2;
				}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
					points=points*3;
				}
			}
//			//理财人资金流水 投标支出，减去抵扣金额
			try {
				fundsServiceV2.updateFundsWhenFullLoan( payUserCode , payAmount, points ) ;
			} catch (Exception e) {
				scanManBiaoLogger.log(Level.INFO,"[满标放款]扣除投资人["+payUserCode+"]冻结余额异常");
				return false;
			}
			
			//更新投标流水冗余
			loanTrace.set("leftAmount", payAmount ) ;
			loanTrace.set("leftInterest", pay_zonglixi[1] ) ;
			long[] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit , rateByYear + rewardRateByYear , 1 , refundType ) ;
			
			loanTrace.set("nextAmount", nextBenXi[0] ) ;
			loanTrace.set("nextInterest", nextBenXi[1] ) ;
			loanTrace.update() ;
			
			//更新冗余内容
			int upResult = fundsServiceV2.updateBeRecyFunds(payUserCode, loanTimeLimit , payAmount ,//pay_zonglixi[0],
					pay_zonglixi[1] , 0,  0 ) ;
		}
		return true;
	}

	/**
	 * 满标放款更新loanInfo及借款人资金 WJW
	 * @param loan
	 * @return
	 */
	public boolean doLoanInfo(LoanInfo loan){
		String loanCode = loan.getStr("loanCode");//标号
		String loanUserCode = loan.getStr("userCode") ;
		Integer rateByYear = loan.get("rateByYear");//年利率
		int rewardRateByYear = loan.getInt("rewardRateByYear");
		int benefits4new = loan.getInt("benefits4new");
		rateByYear = rateByYear + rewardRateByYear + benefits4new;
		long loanAmount = loan.getLong("loanAmount");//贷款标总金额
		Integer loanTimeLimit = loan.get("loanTimeLimit");//还款期数
		String refundType = loan.getStr("refundType");
		int serviceFees = loan.getInt("serviceFees");
		long[] zonglixi = CommonUtil.f_004( loanAmount , rateByYear , loanTimeLimit , refundType) ;
		//交易管理费默认给0,需要考虑加入计算中
		Funds zz = null;
		try {
			zz = fundsServiceV2.doAvBalance4biz(loanUserCode , zonglixi[0], serviceFees, traceType.S, fundsType.J, "招标成功,满标审核放款。");
		} catch (Exception e) {
			scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人资金账户发生异常");
			return false;
		}
		if(zz==null){
			scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人资金账户发生异常");
			return false;
		}
		int doResult = fundsServiceV2.updateBeRecyFunds4loan(loanUserCode, loanTimeLimit , zonglixi[0],
				zonglixi[1] , 0 ) ;
		
		if(doResult > 0 ){

			boolean z1 = loanInfoService.updateLoanByLent(loanCode,loanUserCode,loanTimeLimit );
			if( z1 == true ){
				//结算完成
				//return succ("结算完成", null ) ;
				//生成还款计划
				long leftRecyPrincipal = loanAmount;//剩余待还本金
				long leftRecyInterest = zonglixi[1];//剩余待还利息
				LiCai ff = new LiCai( loanAmount , rateByYear, loanTimeLimit );
				List<Map<String , Long>> xxx = null;
				if(refundType.equals("A")){
					xxx = ff.getDengEList() ;
				}else if(refundType.equals("B")){
					 xxx = ff.getDengXiList();
				}
				for (int j = 1; j <= xxx.size(); j++) {
					Map<String,Long> ck = xxx.get(j-1);
					long ben = ck.get("ben");
					long xi = ck.get("xi");
					Map<String,Object> params = new HashMap<String, Object>();
					String backDate = CommonUtil.anyRepaymentDate4string(DateUtil.getNowDate(), j);
					params.put("backDate", backDate);
					params.put("recyLimit", j);
					if(ben < 0)
						ben = 0;
					if(xi < 0)
						xi = 0;
					params.put("recyPrincipal",ben);
					params.put("recyInterest", xi);
					params.put("realRecyPrincipal", 0);
					params.put("realRecyInterest", 0);
					leftRecyPrincipal = leftRecyPrincipal - ben;
					leftRecyInterest = leftRecyInterest - xi;
					if(leftRecyPrincipal<0)
						leftRecyPrincipal = 0;
					if(leftRecyInterest<0)
						leftRecyInterest = 0;
					if(j==loanTimeLimit){
						leftRecyPrincipal = 0;
						leftRecyInterest = 0;
					}
					params.put("leftRecyPrincipal", leftRecyPrincipal);
					params.put("leftRecyInterest", leftRecyInterest);
					params.put("beRecyPrincipal", loanAmount);
					params.put("beRecyInterest", zonglixi[1]);
					params.put("settlementState", SysEnum.settlementState.A.val());
					settlementPlanService.save(loan, params);
				}
				
			}
		}else{
			scanManBiaoLogger.log(Level.INFO,"[处理批次放款流水loanCode:"+loanCode+"]更新借款人账户异常");
			return false;
		}
		
		generateRepaymentPlan(loanCode);//生成回款计划
		return true;
	}
}