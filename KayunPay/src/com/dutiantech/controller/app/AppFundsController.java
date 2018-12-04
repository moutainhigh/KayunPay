package com.dutiantech.controller.app;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.service.VIPService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.traceState;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.AppTransReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class AppFundsController extends BaseController {
	private  UserService userService=getService(UserService.class);
	private  FundsServiceV2 fundsServiceV2=getService(FundsServiceV2.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private VIPService vipService = getService(VIPService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private RechargeTraceService rechargeTraceService=getService(RechargeTraceService.class);
	private FundsTraceService fundsTraceService=getService(FundsTraceService.class);
	private BanksService banksV2Service = getService(BanksService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
//	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	
	/**
	 * app查询账户资金信息
	 * @return
	 */
	@ActionKey("/app_queryFund")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryFund(){
		Message message=null;
		//获取用户标识
		String userCode = getUserCode();
		
		Map<String,Object> resultMap = new HashMap<String, Object>();
		
		User user=null;
		UserInfo userInfo=null;
		//Object bankCount;
		Funds fund=null;
		//int countTickets = 0;
		try{
			user = userService.findById(userCode);
			userInfo = userInfoService.findById(userCode);
			if(null == user || userInfo == null){
				message= error("01", "查询失败", null);
				renderJson(message);
			}
			//Record record = Db.findFirst("select count(1) bankCount from t_banks_v2 where status = 0 and userCode = ?", userCode);
			//bankCount = record.getColumns().get("bankCount");
			fund = fundsServiceV2.findById(userCode);
			//countTickets = ticketService.countTickets(userCode);
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.MAINPAGE, "查询失败", e);
			message= error("02", "查询失败", e.getMessage());
			renderJson(message);
			}
				Object[] nextSUM = Db.queryFirst("select SUM(nextAmount),SUM(nextInterest) from t_loan_trace where traceState=? and payUserCode=?;", 
						traceState.N.val() , userCode );
				if( null!= nextSUM[0]  ){
					resultMap.put("nextAmount",Number.longToString(Number.ObjectTOLong(nextSUM[0])) ) ;//30天内待收本金
				}
				else{
					resultMap.put("nextAmount","0.00") ;//30天内待收本金
					
				}
				if(  null!= nextSUM[1] ){
					resultMap.put("nextInterest",Number.longToString(Number.ObjectTOLong(nextSUM[1]))) ;//30天内待收利息
				}else{
					resultMap.put("nextInterest","0.00") ;//30天内待收本金
				}
				
		//可用余额   冻结余额   代收本金  代收利息
		long avBalance = fund.getLong("avBalance");//可用余额
		long frozeBalance = fund.getLong("frozeBalance");//冻结余额
		long recyMFee4loan=fund.getInt("recyMFee4loan");//利息管理费
		//回报统计
		long beRecyPrincipal = fund.getLong("beRecyPrincipal");//待收本金
		long beRecyInterest = fund.getLong("beRecyInterest");//待收利息
		long reciedInterest=fund.getLong("reciedInterest");//已收利息
		long reciedPrincipal=fund.getLong("reciedPrincipal");//已收本金
		
		//资金统计
		long outFundsCount=reciedPrincipal+beRecyPrincipal;//总借出金额
		long forRecovery=beRecyPrincipal+beRecyInterest;//待回收本息
		long haveRecovery= reciedPrincipal+reciedInterest;//已回收本息
		long beRecyCount=fund.getInt("beRecyCount");//待回收笔数
		long accumulatedEarnings=reciedInterest-recyMFee4loan ;//累计赚取收益
		long totalBalance=avBalance+frozeBalance+beRecyPrincipal+beRecyInterest;//总资产
		//long totalRecharge = fund.getLong("totalRecharge");//充值统计
		//long totalWithdraw = fund.getLong("totalWithdraw");//提现统计
		resultMap.put("userName",user.getStr("userName"));
		resultMap.put("avBalance", Number.longToString(avBalance));
		resultMap.put("frozeBalance", Number.longToString(frozeBalance));
		resultMap.put("beRecyPrincipal", Number.longToString(beRecyPrincipal));
		resultMap.put("beRecyInterest", Number.longToString(beRecyInterest));
		resultMap.put("reciedInterest", Number.longToString(reciedInterest));
		resultMap.put("reciedPrincipal", Number.longToString(reciedPrincipal));
		resultMap.put("outFundsCount", Number.longToString(outFundsCount));
		resultMap.put("forRecovery", Number.longToString(forRecovery));
		resultMap.put("haveRecovery", Number.longToString(haveRecovery));
		resultMap.put("beRecyCount", beRecyCount);
		resultMap.put("accumulatedEarnings", Number.longToString(accumulatedEarnings));
		resultMap.put("totalBalance", Number.longToString(totalBalance));
		int ticketNum = ticketsService.countTickets(userCode);
		resultMap.put("ticketNum", String.valueOf(ticketNum));
		message = succ("查询成功", resultMap);
		renderJson(message);
	}
	
	
	/**
	 * App提现页面信息
	 * 银行名称+银行卡尾号+银行卡
	 * @return
	 */
	@ActionKey("/app_querywithdraw")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryWithdraw() {
		String userCode=getUserCode();
		User user=userService.findById(userCode);
		Message msg=null;
		String accountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(accountId)){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		//验证是否存在有效卡
		String verifyBindCard = JXappController.verifyBindCard(accountId, "1");
		if("n".equals(verifyBindCard)){
			msg = error("03", "请先绑定银行卡！", null);
			renderJson(msg);
			return;
		}
		BanksV2 banksV2=BanksV2.bankV2Dao.findById(userCode);
		if(null==banksV2){
			msg=error("02", "未绑定银行卡", null);
		}else{
			Map<String,Object> result = new HashMap<String, Object>();
			String bankName=banksV2.getStr("bankName");
			String bankNo=banksV2.getStr("bankNo");
			int ll=bankNo.length();
			String banknolast4=bankNo.substring(ll-4, ll);
			Funds funds=fundsServiceV2.findById(userCode);
			String avbalance=Number.longToString(funds.getLong("avBalance"));
			String points=Number.longToString(funds.getLong("points"));//可用积分
			String leftfree="";
			//免费提现次数
			int x1 = withdrawFreeService.findFreeCountByUserCode(userCode);//已免费提现次数
			int x2 = vipService.findUserVipLevelByUserCode(userCode);//vip免费次数
			if(x1<0){x1=0;}//userFree
			x2=VipV2.getVipByLevel(x2).getVipTxCount();//vipFree
			if(-1==x2){
				leftfree="不限";
			}else{
				int x3=x2-x1;
				if(x3<=0){
					leftfree="0";
				}else if(x3>0){
					leftfree=String.valueOf(x3);
				}
			}
			result.put("bankName", bankName);//银行名
			result.put("avbalance", avbalance);//余额
			result.put("banknolast4", banknolast4);//卡号后4位
			result.put("points", points);//可用积分
			result.put("leftfree", leftfree);//剩余提现免费次数
			result.put("charges", "2.00");//手续费
			result.put("integral", "200.00");//需要积分
			Long totalVest=funds.getLong("beRecyPrincipal")+funds.getLong("reciedPrincipal")+funds.getLong("reciedInterest");
			Long noChargeMoney = totalVest -funds.getLong("totalWithdraw");					
			if(  noChargeMoney < 0  ){						
				noChargeMoney = (long) 0;						
			}else{
				 if( noChargeMoney >= funds.getLong("avBalance")  ){
					 noChargeMoney = funds.getLong("avBalance");
				 }
			}
			result.put("noChargeMoney", Number.longToString(noChargeMoney));//无服务费可提现额度
			msg=succ("withdraw", result);
		}
		renderJson(msg);
	}

//	
//	/**
//	 * App提现
//	 * @return
//	 */
//	/*@ActionKey("/app_withdraw")*/
//	@AuthNum(value=999)
//	@Before({AppInterceptor.class,PkMsgInterceptor.class})
//	public void AppWithdraw() {
//		String userCode=getUserCode();
//		// 获取参数并验证
//		long amount = 0;
//		double am=Double.parseDouble(getPara("amount"))*100;
//		amount =new Double(am).longValue();
//		String useFree = getPara("useFree");// 如果使用免费提现，验证免费提现次数
//		String isScore = getPara("isScore");//是否使用积分
//
//		try {
//			Funds funds=fundsServiceV2.getFundsByUserCode(userCode);
//			BanksV2 bank=BanksV2.bankV2Dao.findById(userCode);
//			UserInfo userInfo = userInfoService.findById(userCode);
//			String withdrawCode = CommonUtil.genMchntSsn();
//			// 新增提现申请记录
//			boolean result = false;
//			result = withdrawTraceService.save(withdrawCode, userCode,
//					funds.getStr("userName"), userInfo.getStr("userCardName"),
//					bank.getStr("bankNo"), bank.getStr("bankNo"),
//					bank.getStr("bankType"), bank.getStr("bankName"),
//					bank.getStr("cardCity"), amount, "2", isScore, "用户申请提现", "",
//					useFree, false);
//			if (result == false) {
//				// 记录日志
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS,
//						"用户申请提现失败，提现异常-07", null);
//			} else {
//				// 记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ："
//						+ amount / 10.0 / 10.0);				
//			}
//			//  计算提现手续费
//			long fees = withdrawTraceService.calculateSysFee(userCode, amount, isScore, useFree);
//			long realAmt = amount - fees;
//			AppTransReqData appTransReqData = new AppTransReqData();
//			String login_id = User.userDao.findByIdLoadColumns(userCode,"loginId").getStr("loginId");
//			try {
//				login_id = CommonUtil.decryptUserMobile(login_id);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			appTransReqData.setAmt(String.valueOf(realAmt));
//			appTransReqData.setLogin_id(login_id);
//			appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
//			appTransReqData.setMchnt_txn_ssn(withdrawCode);
//			appTransReqData.setPage_notify_url(CommonUtil.APP_URL + "/app_withdrawPageNotify");
//			//商户APP个人用户免登录提现
//			FuiouService.app500003(appTransReqData, getResponse());
//			renderNull();
//		} catch (Exception e) {
//			e.printStackTrace();
//			// 记录日志
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
//		}
//	}

	/**
	 * app充值信息查询
	 * */
	@ActionKey("app_queryRecharge")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryRecharge(){
		String userCode=getUserCode();
		User user=userService.findById(userCode);
		Message msg=null;
		String accountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(accountId)){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		//验证是否存在有效卡
		String verifyBindCard = JXappController.verifyBindCard(accountId, "1");
		if("n".equals(verifyBindCard)){
			msg = error("03", "请先绑定银行卡！", null);
			renderJson(msg);
			return;
		}
		BanksV2 banksV2=BanksV2.bankV2Dao.findById(userCode);
		long avBalance = fundsServiceV2.findAvBalanceById(userCode);//可用余额
		if(null==banksV2){
			msg=error("02", "未绑定银行卡", null);
		}else{
			Map<String,Object> resultMap = new HashMap<String, Object>();
			String bankName=banksV2.getStr("bankName");
			String bankNo=banksV2.getStr("bankNo");
			String banknolast4=bankNo.substring(bankNo.length()-4, bankNo.length());
			resultMap.put("bankName", bankName);
			resultMap.put("bankNo", banknolast4);//银行卡后四位
			resultMap.put("avBalance", StringUtil.getMoneyYuan(avBalance));
			msg=succ("查询成功", resultMap);
		}
		renderJson(msg);
		return;
	}
	
	/**
	 * app充值记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_queryRechargeTrace")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryRechargeTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        
		//获取用户标识
		String userCode = getUserCode();
		
		//获取充值记录
		Map<String, Object> result = rechargeTraceService.appQueryRecharge(
	 			userCode , pageNumber, pageSize,null);
		
		message= succ("充值记录查询成功", result);
		renderJson(message);
		
	}
	
	/**
	 * app提现记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_queryWithdrawTrace")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryWithdrawTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        
		//获取用户标识
		String userCode = getUserCode();
		
		//获取充值记录
		Map<String, Object> result = withdrawTraceService.appQueryWithdraw(
	 			userCode , pageNumber, pageSize,null);
		
		message= succ("提现记录查询成功", result);
		renderJson(message);
		
	}
	
	/**
	 * app资金记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_QueryFundsTraceList")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryFundsTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        //获取用户标识
		String userCode = getUserCode();
		Map<String, Object> result=null;
		//获取资金记录
		result = fundsTraceService.appQueryFundsTraceList(pageNumber, pageSize,userCode);
		message= succ("资金记录查询成功", result);
		renderJson(message);
		
	}
	
	/**
	 * app资金详情
	 * */
	@ActionKey("/app_queryfundsDetail")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryfundsDetail", cachedKeyParm="traceCode|@userCode",mode="remote" , time=2)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryfundsDetail(){
		String traceCode = getPara("traceCode");
	
		Message message=null;
		if(StringUtil.isBlank(traceCode)){
			message= error("01", "资金流水号不合法", null);
			renderJson(message);
			return ;
		}
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize(); 
		Map<String, Object> result=null;
		result = fundsTraceService.appQueryFundsTraceDetail(pageNumber, pageSize,traceCode);
		message= succ("资金详情查询成功", result);
		renderJson(message);
	}
	
	/**
	 * app快捷充值
	 */
	@ActionKey("/app_fastRecharge")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void quickRecharge(){
		Message msg = new Message();
		
		// 获取参数
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		long amount = StringUtil.getMoneyCent(getPara("amt", "0"));	// 充值金额
		BanksV2 banksV2 = banksV2Service.findByUserCode(userCode);	// 银行卡信息
		UserInfo userInfo = userInfoService.findById(userCode);	// 用户信息
		
		// 参数验证
		if (amount <= 0) {
			msg = error("01", "参数异常", amount);
			renderJson(msg);
			return;
		}
		if (amount < 10000) {
			msg = error("02", "最低充值金额100元", amount);
			renderJson(msg);
			return;
		}
		if (banksV2 == null) {
			msg = error("04", "未查找到理财卡信息", null);
			renderJson(msg);
			return;
		}
		if (userInfo == null) {
			msg = error("03", "未查找到用户信息", null);
			renderJson(msg);
			return;
		}
		if (StringUtil.isBlank(banksV2.getStr("bankNo"))) {
			msg = error("02", "未绑定理财卡", null);
			renderJson(msg);
			return;
		}
		if(StringUtil.isBlank(userInfo.getStr("userCardId"))){
			msg = error("10", "用户未做实名认证", null);
			renderJson(msg);
			return;
		}
		
		// 参数解析
		String mobile = null;
		String cardId = null;
		try {
			cardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			mobile = CommonUtil.decryptUserMobile(banksV2.getStr("mobile"));
		} catch (Exception e) {
			msg = error("04", "身份证或手机号解析错误", null);
			renderJson(msg);
			return;
		}
		
		// 通道选择
		if (CommonUtil.jxPort) {
			JXappController jxAppController = new JXappController();
			HttpServletResponse response = getResponse();
			
			String jxAccountId = user.getStr("jxAccountId");
			// 数据校验
			if (StringUtil.isBlank(jxAccountId)) {
				msg = error("03", "未激活存管账户，请到用户管理页面激活", null);
				renderJson(msg);
				return;
			}
			try {
				Map<String, String> pwdSetResult = JXQueryController.passwordSetQuery(jxAccountId);
				if (pwdSetResult == null || "0".equals(pwdSetResult.get("pinFlag"))) {
					msg = error("02", "还未设置电子账户交易密码", "");
					renderJson(msg);
					return;
				}
			} catch (Exception e) {
				msg = error("09", "充值_获取信息超时", "");
				renderJson(msg);
				return;
			}
			
			// 存储订单信息
			Map<String, String> payParam = new HashMap<String, String>();
			payParam.put("no_order", CommonUtil.genMchntSsn());	// 流水号
			payParam.put("user_id", userCode);
			payParam.put("bank_code", "");//银行编码
			payParam.put("userBankName", banksV2.getStr("bankName"));//银行名称
			payParam.put("info_order", "易融恒信理财充值-快捷充值");
			
			//订单入库
			RechargeTrace trace = map2trace(payParam);
			trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.PHONE.key());
			trace.set("traceAmount", amount);
			trace.set("bankRemark", "手机支付,发起申请");
			trace.set("traceRemark", "手机支付,发起申请");
			trace.set("userName", user.getStr("userName"));
			trace.set("userBankNo", banksV2.getStr("bankNo"));
			if(trace.save()){
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "生成充值订单成功！");
			}else{
				msg = error("10", "生成充值订单失败!", null);
				renderJson(msg);
				return;
			}
			
			// 返回交易页面链接
			String retUrl = CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro";
			String successfulUrl = CommonUtil.APP_URL + "/ispaysuccforapp?type=paysucc";
			// 后台响应链接
			String notifyUrl = CommonUtil.CALLBACK_URL + "/quickRechargeCallback?traceCode=" + payParam.get("no_order");
			// 忘记密码链接
			String forgotPwdUrl = CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+mobile;
			// 发起订单申请
			msg = jxAppController.directRechargePage(jxAccountId, banksV2.getStr("bankNo"), userInfo.getStr("userCardName"), 
					mobile, userInfo.getStr("idType"), cardId, StringUtil.getMoneyYuan(amount), 
					forgotPwdUrl, retUrl, notifyUrl, successfulUrl, response);
			
			String jxTraceCode = (String)msg.getReturn_data();
			if(!StringUtil.isBlank(jxTraceCode)){
				trace.set("bankTraceCode", jxTraceCode.trim());
				if(trace.update()){
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！");
				}else{
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！", null);
				}
			}
			renderNull();
		} else if (CommonUtil.fuiouPort) {
			String fuiouLoginId = user.getStr("loginId");
			if (StringUtil.isBlank(fuiouLoginId)) {
				msg = error("03", "未激活存管账户，请到用户管理页面激活", null);
				renderJson(msg);
				return;
			}
			try {
				fuiouLoginId = CommonUtil.decryptUserMobile(fuiouLoginId);
			} catch (Exception e) {
				msg = error("04", "解析存管账户异常", null);
				renderJson(msg);
				return;
			}
			
			// 存储订单信息
			Map<String, String> payParam = initPayInfo();
			payParam.put("user_id", userCode);
			payParam.put("name_goods", "易融恒信用户app充值");
			payParam.put("info_order", "易融恒信app理财充值-快捷充值.");
			payParam.put("money_order", String.valueOf(amount));
			payParam.put("userreq_ip", getRequestIP());
			payParam.put("bank_code", banksV2.getStr("bankType"));
			payParam.put("money_order", StringUtil.getMoneyYuan(amount));
			payParam.put("id_type", "0");
			payParam.put("id_no", cardId);
			payParam.put("acct_name", userInfo.getStr("userCardName"));
			payParam.put("userBankName", banksV2.getStr("bankName"));
			payParam.put("card_no", banksV2.getStr("bankNo"));
			payParam.put("page_notify_url", CommonUtil.APP_URL + "/app_fast_showResult4fuiou");
			payParam.put("risk_item", bindRiskInfo(user));	// 风控信息
			
			// 订单入库
			RechargeTrace trace = map2trace(payParam);
			trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.PHONE.key());
			trace.set("traceAmount", amount);
			trace.set("bankRemark", "手机支付,发起申请");
			trace.set("traceRemark", "手机支付,发起申请");
			trace.set("userName", user.getStr("userName"));
			trace.set("userBankNo", banksV2.getStr("bankNo"));
			
			AppTransReqData appTransReqData =new AppTransReqData();
			appTransReqData.setAmt(String.valueOf(amount));
			appTransReqData.setLogin_id(fuiouLoginId);
			appTransReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
			appTransReqData.setMchnt_txn_ssn(payParam.get("no_order"));
			appTransReqData.setPage_notify_url(payParam.get("page_notify_url"));
			try {
				FuiouService.app500002(appTransReqData, getResponse());
				renderNull();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (trace.save()) {
				msg = succ("ok", "生成充值订单成功");
			} else {
				msg = error("14", "生成充值订单失败!", null);
			}
			renderJson(msg);
		} else {
			msg = error("99", "存管系统对接中...", null);
			renderJson(msg);
			return;
		}
	}
	
	/**
	 * app提现——江西银行
	 */
	@ActionKey("/app_withdrawByJX")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void withdraw(){
		Message msg = new Message();
		
		// 获取参数
		String userCode = getUserCode();
		
		UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
		if(!"1".equals(userTermsAuth.getStr("riskReminder"))){
			msg = error("02", "未授权风险提示书,请至PC端授权提现!", "");
			renderJson(msg);
			return;
		}
		
		User user = userService.findById(userCode);
		long amount = StringUtil.getMoneyCent(getPara("amount", "0"));
		String routeCode = getPara("routeCode", "");	// 路由代码
		String cardBankCnaps = getPara("cardBankCnaps", "");	// 银行联行
		UserInfo userInfo = userInfoService.findById(userCode);	// 用户信息
		BanksV2 banksV2 = banksV2Service.findByUserCode(userCode);	// 获取银行卡信息
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		String useFree = getPara("useFree");	// 是否使用免费提现次数
		String isScore = getPara("isScore");	// 是否使用积分抵扣
		String businessAccountIdFlag = getPara("businessAccountIdFlag","N");	// 对公账户提现标识  Y:对公  N:对私 不上送默认为N
		
		// 参数验证
		if (amount < 300) {	// 提现金额验证
			msg = error("01", "提现金额不能小于3元", "");
			renderJson(msg);
			return;
		}
		if ("2".equals(routeCode)) {	// 提现通道验证
			if (amount <= 5000000) {
				msg = error("02", "5万以下的提现，请走普通提现", "");
				renderJson(msg);
				return;
			}
			if (StringUtil.isBlank(cardBankCnaps)) {
				msg = error("03", "大额提现_联行号不能为空", "");
				renderJson(msg);
				return;
			}
		} else {
			if (amount > 5000000) {
				msg = error("04", "5万以上的提现，请走大额提现", "");
				renderJson(msg);
				return;
			}
		}
		if (!user.isInvester()) {	// 提现角色验证
			msg = error("55", "非出借人请勿操作", "");
			renderJson(msg);
			return;
		}
		if(banksV2 == null){	// 绑卡验证
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，未查找到理财卡信息");
			msg = error("05", "未查找到理财卡信息", "");
			renderJson(msg);
			return;
		}
		if (StringUtil.isBlank(banksV2.getStr("bankNo"))) {
			msg = error("05", "还未绑定银行卡", "");
			renderJson(msg);
			return;
		}
		
		// 防诈骗，未投资用户冻结账户
		long loanAmount = fundsTraceService.findTraceAmount(userCode, SysEnum.traceType.P.val());
		long transferAmount = fundsTraceService.findTraceAmount(userCode, SysEnum.traceType.A.val());
		if (loanAmount + transferAmount <= 0 && !SysEnum.userState.F.val().equals(user.getStr("userState"))) {
			user.set("userState", SysEnum.userState.S.val());
			user.update();
			msg = error("05", "此账户提现冻结，请联系客服进行解冻", "");
			renderJson(msg);
			return;
		}
		
		// 冻结账户无法提现
		if (user.isFrozen()) {
			msg = error("55", "账户已被冻结，请联系客服进行解冻", "");
			renderJson(msg);
			return;
		}
		
		// 参数解析
		String userCardId = null;
		String userMobile = null;
		try {
			userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));	// 解析用户身份证号
			userMobile = CommonUtil.decryptUserMobile(banksV2.getStr("mobile"));	// 解析存管预留手机号
		} catch (Exception e) {
			msg = error("06", "身份证或手机号解析异常", null);
			renderJson(msg);
			return;
		}
		
		// 限制首次提现金额 大于等于50元
		Page<WithdrawTrace> withdrawTrace = withdrawTraceService.findByPage(
				userCode, 1, 10, null, null, null, null);
		if (null == withdrawTrace || withdrawTrace.getTotalRow() <= 0) {
			if (amount < 5000) {
				msg = error("03", "首次提现金额不能小于50元", "");
				renderJson(msg);
				return;
			}
		}
		
		//是否使用免费提现次数
		if("y".equals(useFree)){
			int useFreeCount = withdrawFreeService.findFreeCountByUserCode(userCode);// 已经免费提现的次数
			int userVipLevel = user.getInt("vipLevel");
			VipV2 vip = VipV2.getVipByLevel(userVipLevel);
			int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411", DateUtil.getNowDate());
			if (x > 0) {
				msg = error("03", "新会员免费提现制度2016年4月11后生效", false);
				renderJson(msg);
				return;
			}
			if (vip.getVipTxCount() != -1) {
				if (useFreeCount >= vip.getVipTxCount()) {
					msg = error("117", "您已免费提现" + useFreeCount + "次，您的会员级别目前最多可免费提现" + vip.getVipTxCount()
							+ "次", false);
					renderJson(msg);
					return;
				}
			}
		}
		
		// 验证抵扣方式
		if("1".equals(isScore) && "y".equals(useFree)){
			msg = error("110", "积分抵扣、免费提现只能选一种方式", false);
			renderJson(msg);
			return;
		}
		
		// 验证积分是否足够
		if ("1".equals(isScore) && funds.getLong("points") < 20000) {
			// 记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户积分不足");
			msg = error("09", "账户积分不足", "");
			renderJson(msg);
			return;
		}
		
		// 计算提现手续费
		long fees = withdrawTraceService.calculateSysFee(userCode, amount, isScore, useFree);
		long realAmt = amount - fees;	//实际提现金额
		
		// 验证用户资金是否存在异常
		if (!fundsServiceV2.checkBalance(user)) {
			msg = error("08", "用户资金异常", "");
			renderJson(msg);
			return;
		}
		
		// 验证提现资金是否足够
		long avBalance = funds.getLong("avBalance");
		if (avBalance < amount) {
			msg = error("08", "可用余额不足", "");
			renderJson(msg);
			return;
		}
		
		
		// 验证提现金额是否大于存管金额
//		if (CommonUtil.jxPort) {
//			Map<String, String> balanceQuery = JXQueryController.balanceQuery(user.getStr("jxAccountId"));
//			String availBal = balanceQuery.get("availBal");	// 可用余额
//			long jxAvailBal = StringUtil.getMoneyCent(availBal);
//			// 平台可用余额、冻结金额
//			long avBalance = funds.getLong("avBalance");
//			
//			// 验证用户存管资金是否小与提现资金
//			if(jxAvailBal < amount){
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，存管账户可用余额不足，平台可用余额："+StringUtil.getMoneyYuan(avBalance)+",存管可用余额："+availBal);
//				msg = error("08", "存管账户余额不足", "");
//				renderJson(msg);
//				return;
//			}
//		} else if (CommonUtil.fuiouPort) {
//		}
		
		// 江西银行存管缴费授权验证
		if (CommonUtil.jxPort) {
			if (!StringUtil.isBlank(user.getStr("jxAccount"))) {
				// 验证缴费授权
				Map<String, String> authDetail = JXQueryController.termsAuthQuery(user.getStr("jxAccountId"));
				if("1".equals(authDetail.get("paymentAuth"))){//开通了缴费授权
					//缴费授权到期日
					String paymentDeadline = authDetail.get("paymentDeadline");
					//缴费签约最高金额
					String paymentMaxAmt = authDetail.get("paymentMaxAmt");
					int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline, DateUtil.getNowDate());
					if(x < 0){
						msg = error("05", "缴费授权已过期", "");
						renderJson(msg);
						return;
					}
					if(StringUtil.getMoneyCent(paymentMaxAmt) < fees){
						msg = error("04", "手续费超过了授权金额", "");
						renderJson(msg);
						return;
					}
				}else{
					msg = error("05", "提现未授权", "");
					renderJson(msg);
					return;
				}
			}
		}
		
		// 新增提现申请记录
		String withdrawCode = CommonUtil.genMchntSsn();	// 提现交易号
		boolean result = false;
		result = withdrawTraceService.save(withdrawCode, userCode,funds.getStr("userName"), userInfo.getStr("userCardName"),
				banksV2.getStr("bankNo"), banksV2.getStr("bankNo"),"", banksV2.getStr("bankName"),
				"", amount, "2", isScore, "用户申请提现", "", useFree, false);
		if (result == false) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-07", null);	// 记录日志
			msg = error("07", "提现异常07", "");
			renderJson(msg);
			return;
		} else {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ：" + StringUtil.getMoneyYuan(amount));	// 记录日志				
		}
		
		// 通道选择
		if (CommonUtil.jxPort) {
			String jxAccountId = user.getStr("jxAccountId");
			// 验证是否开通存管
			if (StringUtil.isBlank(jxAccountId)) {
				msg = error("06", "用户还未激活存管账户", "");
				renderJson(msg);
				return;
			}
			
			String retUrl = CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro";
			String forgotPwdUrl = CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+userMobile;
			String successfulUrl = CommonUtil.APP_URL + "/ispaysuccforapp?type=getsucc";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/withdrawCallback?withdrawCode=" + withdrawCode;

			JXappController jxAppController = new JXappController();
			HttpServletResponse response = getResponse();
			msg = jxAppController.withdrawPage(jxAccountId, userInfo.getStr("userCardName"), banksV2.getStr("bankNo"), 
					userInfo.getStr("idType"), userCardId, userMobile, StringUtil.getMoneyYuan(realAmt), StringUtil.getMoneyYuan(fees), 
					routeCode, cardBankCnaps, retUrl, notifyUrl, forgotPwdUrl, successfulUrl, response, businessAccountIdFlag);
			String jxTraceCode = (String)msg.getReturn_data();
			if(!StringUtil.isBlank(jxTraceCode)){
				WithdrawTrace trace = withdrawTraceService.findById(withdrawCode);
				if(trace != null){
					trace.set("bankTraceCode", jxTraceCode.trim());
					if(trace.update()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号 失败！", null);
					}
				}
			}
			renderNull();
		} else if (CommonUtil.fuiouPort) {
			String fuiouLoginId = user.getStr("loginId");
			try {
				fuiouLoginId = CommonUtil.decryptUserMobile(fuiouLoginId);
			} catch (Exception e) {
				msg = error("06", "解析存管账号异常", null);
				renderJson(msg);
				return;
			}
			
			AppTransReqData appTransReqData = new AppTransReqData();
			appTransReqData.setAmt(String.valueOf(realAmt));
			appTransReqData.setLogin_id(fuiouLoginId);
			appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			appTransReqData.setPage_notify_url(CommonUtil.APP_URL + "/app_withdrawPageNotify");
			// 商户APP个人用户免登录提现
			try {
				FuiouService.app500003(appTransReqData, getResponse());
			} catch (Exception e) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
			}
			renderNull();
		} else {
			msg = error("99", "存管系统对接中...", "");
			renderJson(msg);
			return;
		}
	}
	
	
	/**************************************************** PRIVATE METHOD ******************************************************/

	
	/**
	 * 初始化充值信息 商户号，流水号 
	 * @return
	 */
	private Map<String, String> initPayInfo() {
		Map<String, String> payParam = new TreeMap<String, String>();
		String nowDateTime = DateUtil.getNowDateTime();

		payParam.put("version", "1.0");
		payParam.put("charset_name", "UTF-8");
		payParam.put("oid_partner", CommonUtil.MCHNT_CD); // 商户号
		payParam.put("timestamp", nowDateTime);
		payParam.put("sign_type", "RSA");
		payParam.put("busi_partner", "101001");
		payParam.put("no_order", CommonUtil.genMchntSsn()); // 流水号
		payParam.put("dt_order", nowDateTime);
		payParam.put("valid_order", "30"); // 默认30分钟
		payParam.put("pay_type", "1"); // 默认只支持借记卡

		return payParam;
	}
	
	private String bindRiskInfo(User user) {
		JSONObject params = new JSONObject();
		if (user != null) {
			String userCode = user.getStr("userCode");
			UserInfo uInfo = UserInfo.userInfoDao.findById(userCode);
			params.put("user_info_mercht_userno", userCode);
			String tmpMobile = user.getStr("userMobile");
			try {
				String uMobile = CommonUtil.decryptUserMobile(tmpMobile);
				params.put("user_info_bind_phone", uMobile);
				params.put("user_info_mercht_userlogin", uMobile);
				params.put("user_info_mail", user.getStr("userEmail"));
			} catch (Exception e) {
				params.put("user_info_bind_phone", "0000000");
			}
			params.put("user_info_dt_register", user.getStr("regDate") + user.getStr("regTime"));
			params.put("user_info_dt_register", user.getStr("regIP"));
			params.put("frms_ware_category", "2009");
			params.put("user_info_full_name", uInfo.getStr("userCardName"));
			try {
				String tmpCardId = CommonUtil.decryptUserCardId(uInfo.getStr("userCardId"));
				if (StringUtil.isBlank(tmpCardId) == false) {
					params.put("user_info_id_no", tmpCardId);
					params.put("user_info_identify_state", "1");
					params.put("user_info_identify_type", "3");
				} else {
					params.put("user_info_identify_state", "0");
				}
			} catch (Exception e) {
				params.put("user_info_id_no", "");
			}
		} else {
			throw new BaseBizRunTimeException("11", "无法获取该用户的信息", null);
		}
		return params.toJSONString();
	}
	
	private RechargeTrace map2trace(Map<String, String> map) {
		RechargeTrace trace = new RechargeTrace();
		String userCode = String.valueOf(map.get("user_id"));
		UserInfo info = userInfoService.findById(userCode);
		if (info != null) {
			trace.set("userTrueName", info.getStr("userCardName"));
		}
		String tmpBankCode = String.valueOf(map.get("bank_code"));
		trace.set("userBankCode", tmpBankCode);
		trace.set("userBankName", map.get("userBankName"));
		trace.set("traceCode", map.get("no_order"));
		trace.set("bankTraceCode", map.get("no_order"));
		trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key()); // A-已受理
		trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key());
		trace.set("userCode", userCode);
		trace.set("traceDateTime", DateUtil.getNowDateTime());
		trace.set("traceDate", DateUtil.getNowDate());
		trace.set("modifyDateTime", DateUtil.getNowDateTime());
		trace.set("modifyDate", DateUtil.getNowDate());

		return trace;
	}
 }
