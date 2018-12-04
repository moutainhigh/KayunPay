package com.dutiantech.controller.pay;

import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.plugin.syxpay.SYXPayKit;
import com.dutiantech.plugins.HttpRequestor;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;

public class LianLianPayController extends BaseController{

	private UserService userService = getService( UserService.class ) ;
	private UserInfoService userInfoService = getService( UserInfoService.class ) ;
	private FundsServiceV2 fundsServiceV2 = getService( FundsServiceV2.class ) ;
	private BanksService banksService = getService(BanksService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private RechargeTraceService rtService = getService(RechargeTraceService.class);
	private WithdrawTraceService withdrawtraceService = getService(WithdrawTraceService.class);
	
	String MER_NO = String.valueOf( CACHED.get("S2.paymerno_lianlian") );
	String NOTIFY_HOST = String.valueOf( CACHED.get("S2.notify_host_lianlian") );
	
	private static Map<String , String> BANK_CODE = new HashMap<String , String>() ;
	
	static{
		BANK_CODE.put("01020000", "工商银行");
		BANK_CODE.put("01050000", "建设银行");
		BANK_CODE.put("01030000", "农业银行");
		BANK_CODE.put("03080000", "招商银行");
		BANK_CODE.put("03010000", "交通银行");
		BANK_CODE.put("01040000", "中国银行");
		BANK_CODE.put("03030000", "光大银行");
		BANK_CODE.put("03050000", "民生银行");
		BANK_CODE.put("03090000", "兴业银行");
		BANK_CODE.put("03020000", "中信银行");
		BANK_CODE.put("03060000", "广发银行");
		BANK_CODE.put("03100000", "浦发银行");
		BANK_CODE.put("03070000", "平安银行");
		BANK_CODE.put("03040000", "华夏银行");
		BANK_CODE.put("04083320", "宁波银行");
		BANK_CODE.put("03200000", "东亚银行");
		BANK_CODE.put("04012900", "上海银行");
		BANK_CODE.put("01000000", "中国邮储银行");
		BANK_CODE.put("04243010", "南京银行");
		BANK_CODE.put("65012900", "上海农商行");
		BANK_CODE.put("03170000", "渤海银行");
		BANK_CODE.put("64296510", "成都银行");
		BANK_CODE.put("04031000", "北京银行");
	}
	
	private Map<String ,String> initPayInfo(){
		Map<String , String> payParam = new TreeMap<String , String>(); 
		String nowDateTime = DateUtil.getNowDateTime() ;
		
		payParam.put("version", "1.0") ;
		payParam.put("charset_name", "UTF-8") ;
		payParam.put("oid_partner", MER_NO ) ;	//商户号
		payParam.put("timestamp", nowDateTime ) ;
		payParam.put("sign_type", "RSA" ) ;
		//payParam.put("sign", "");		//签名值
		
		payParam.put("busi_partner", "101001") ;
		payParam.put("no_order", UIDUtil.generate() ) ;
		payParam.put("dt_order", nowDateTime ) ;
		payParam.put("name_goods", "易融恒信理财充值");
		payParam.put("info_order", "易融恒信理财充值-连连支付网银充值.");
		payParam.put("url_order", NOTIFY_HOST + "/lianlian_notify4lianlian") ;
		payParam.put("valid_order", "30") ;	//默认30分钟
		payParam.put("pay_type", "1" ) ;	//默认只支持借记卡
		
		return payParam ;
	}
	
	private String bindRiskInfo( User user){
		JSONObject params = new JSONObject() ;
		if( user != null ){
			String userCode = user.getStr("userCode") ;
			params.put("user_info_mercht_userno", userCode ) ;
			String tmpMobile = user.getStr("userMobile") ;
			try {
				String uMobile = CommonUtil.decryptUserMobile(tmpMobile) ;
				params.put("user_info_bind_phone", uMobile);
				params.put("user_info_mercht_userlogin", uMobile);
				params.put("user_info_mail", user.getStr("userEmail"));
			} catch (Exception e) {
				params.put("user_info_bind_phone", "0000000");
			}
			params.put("user_info_dt_register", user.getStr("regDate")+user.getStr("regTime"));
			params.put("user_info_dt_register", user.getStr("regIP") );	
			params.put("frms_ware_category","2009");
			
			UserInfo uInfo = UserInfo.userInfoDao.findById( userCode ) ;

			params.put("user_info_full_name",uInfo.getStr("userCardName"));
			try {
				String tmpCardId = CommonUtil.decryptUserCardId( uInfo.getStr("userCardId")) ;
				if( StringUtil.isBlank(tmpCardId) == false ){
					params.put("user_info_id_no", tmpCardId );
					params.put("user_info_identify_state","1");
					params.put("user_info_identify_type","3");
				}else{
					params.put("user_info_identify_state","0");
				}
			} catch (Exception e) {
				params.put("user_info_id_no", "" );
			}
		}else{
			throw new BaseBizRunTimeException("11", "无法获取该用户的信息", null ) ;
		}
		return params.toJSONString() ;
	}
	
	public void sign4lianlianWithRSA(Map<String, String> params){
		Iterator<String> keys = params.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = params.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		String tmpString = buff.toString() ;
		String priKey = String.valueOf( CACHED.get("S2.rsa_prikey_lianlian"));
		String signValue = SafeUtil.sign( priKey , tmpString ) ;
		if( signValue != null ){
			params.put("sign", signValue ) ;
		}
	}
	
	public boolean checkSign4lianlianWithRSA( Map<String, String> response ){
		String pubKey = String.valueOf( CACHED.get("S2.rsa_pubkey_lianlian"));
		String signValue = response.get("sign") ;
		response.remove("sign") ;
		
		Iterator<String> keys = response.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = response.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		
		return SafeUtil.checksign(pubKey, buff.toString(), signValue) ;
	}
	
	public void sign4lianlianWithMD5(Map<String, String> params ) {
		Iterator<String> keys = params.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = params.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		String tmpString = buff.toString() + "&key=" + CACHED.get("S2.md5key_lianlian");
		System.out.println(tmpString);
		try {
			String signValue = MD5Code.md5Pure( tmpString ) ;
			params.put("sign", signValue ) ;
		} catch (Exception e) {
			throw new BaseBizRunTimeException("12", "签名异常", null ) ;
		}
 	}
	
	//TODO 移动端代扣
	@ActionKey("/lianlian_authPay4mobile")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class , PkMsgInterceptor.class})
	public Message authPay4lianlian4mobile(){

		long payAmount = 0 ;
		try{
			payAmount = getParaToLong("payAmount");
			if( payAmount <= 0 ){
				return error("01","参数异常", payAmount);
			}
			if(payAmount<10000){
				return error("777", "最低充值金额100元", payAmount);
			}
		}catch(Exception e){
			return error("01","参数异常," + e.getMessage(), null );
		}
		String userCode = getUserCode() ;
		
		BanksV2 bankV2 = BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where userCode=?" , userCode) ;
		if( bankV2 == null ){
			return error("02", "未绑定理财卡", null ) ;
		}
		String cardId = "";
		String bankType = bankV2.getStr("bankType");
		String trueName = bankV2.getStr("trueName");
		String bankNo = bankV2.getStr("bankNo");
		try {
			cardId = CommonUtil.decryptUserCardId( bankV2.getStr("cardid") );
		} catch (Exception e) {
		}
		
		if( StringUtil.isBlank(cardId) == true){
			return error("10", "未做实名认证", null ) ;
		}
		User user = userService.findById(userCode) ;
		String msgMac = getPara("msgMac","");//充值没有验证码,则按以前的，兼容老app
		if(StringUtil.isBlank(msgMac)){
			Map<String , String > payParam = initPayInfo() ;
			String app_request = getPara("app_request","");
			if( StringUtil.isBlank( app_request) == false ){
				payParam.put("app_request", app_request ) ;
			}else{
				String f = getPara("from","");
				if(f.equals("PC") == false){
					payParam.put("app_request", "3" ) ;
				}
			}
			payParam.put("user_id", userCode ) ;
			payParam.put("name_goods", "易融恒信用户充值");
			
			String mobileDesc = getPara("md","");
			if( StringUtil.isBlank(mobileDesc) == false){
				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.[Mobile-" + mobileDesc + "]");
			}else{
				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.");
			}
			
			payParam.put("pay_type", "D");
			payParam.put("money_order", String.valueOf( payAmount ) );
			payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
			payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
			payParam.put("userreq_ip", getRequestIP() ) ;
			payParam.put("bank_code", bankType ) ;
			//payParam.put("no_agree", agreeCode ) ;
			payParam.put("money_order", String.valueOf( payAmount/10.00/10.00 )) ;
			payParam.put("id_type", "0" ) ;
			payParam.put("id_no", cardId ) ;
			payParam.put("acct_name", trueName ) ;
			payParam.put("card_no", bankNo ) ;
			//风控信息
			payParam.put("risk_item", bindRiskInfo( user ));
			sign4lianlianWithRSA( payParam );
			//订单入库
			RechargeTrace trace = map2trace(payParam) ;
			trace.set("traceAmount", payAmount );
			trace.set("bankRemark", "连连支付,认证支付,发起申请" );
			trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
			trace.set("userName" , user.getStr("userName"));
			trace.set("userBankNo",bankNo);
			
			//缓存订单
			Memcached.set("lianlian_recharge_"+payParam.get("no_order"), payParam.get("info_order"), 10*60*1000);
			
			if( trace.save() ){
				return Message.succ("LLZF", payParam );
			}else{
				return Message.error("14", "生成充值订单失败!", null ) ;
			}
		}else{
			//验证短信验证码
			if(CommonUtil.validateSMS("SMS_MSG_RECHARGE_" + userCode, msgMac) == false){
				//记录日志
				BIZ_LOG_WARN(userCode, BIZ_TYPE.RECHARGE, "用户申请充值失败，短信验证码不正确");
				return error("01", "短信验证码不正确", "");
			}
		}
		
		
		Map<String,Object> rs = new HashMap<String, Object>();
		
		//先判断是不是开启了商银信的支付分流
		String SYX_PAYOUT_SWITCH = "";
		try {
			SYX_PAYOUT_SWITCH = (String) CACHED.get("ST.SYX_PAYOUT_SWITCH");
		} catch (Exception e) {
			SYX_PAYOUT_SWITCH = "";
		}
		if(StringUtil.isBlank(SYX_PAYOUT_SWITCH)== false && SYX_PAYOUT_SWITCH.equals("ON")){
			String[] fld = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, null);
			//商银信本月内成功充值总额
			long syx_amount = rtService.countAmountByRechargeType(RechargeTrace.RECHARGE_TYPE.SYX,fld[0],fld[1]);
			//充值总额判断
			if(syx_amount < 9999999900L){
				//星期四到星期天 ，如果银行类型符合，用户手机号无异，就是用商银信代扣
				boolean validateDay = false;
				try {
					Calendar cal = Calendar.getInstance();
					int d = cal.get(Calendar.DAY_OF_WEEK);
					String [] day_week = ((String) CACHED.get("ST.SYX_PAYOUT_DAY4WEEK")).split(",");
					if(day_week != null){
						for (int i = 0; i < day_week.length; i++) {
							if(Integer.parseInt(day_week[i]) == d)
								validateDay = true;
						}
					}
				} catch (Exception e) {
				}
				if(validateDay){
					String bankid = "";
					String mobileNo ="";
					try {
						bankid = SYXPayKit.LianLian_PayOut_CONVERT_BANK_CODE.get(bankType);//连连支付银行编码转商银信，没有则空
					} catch (Exception e) {
						bankid = "";
					}
					try {
						mobileNo = userService.getMobile(userCode);//获取并验证用户手机号
					} catch (Exception e) {
						mobileNo = "";
					}
					if(StringUtil.isBlank(bankid) == false && StringUtil.isBlank(mobileNo) == false){
						String traceCode = CommonUtil.genShortUID();
						Map<String,String> result = SYXPayKit.payOut(traceCode, "1", payAmount , "账户充值", bankid, bankNo, trueName, cardId, mobileNo);
						if(result.get("retCode").equals("0000")){
							RechargeTrace trace = new RechargeTrace();
							trace.set("userBankNo",bankNo);
							trace.set("traceCode", traceCode);
							trace.set("bankTraceCode", result.get("orderId"));
							trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key() );	//A-已受理
							trace.set("bankRemark", "商银信,认证支付,发起申请" );
							trace.set("userCode" , userCode ) ;
							trace.set("userTrueName" ,trueName);
							trace.set("userBankCode" ,bankid);
							trace.set("userBankName", SYXPayKit.PAYOUT_BANK_CODE.get(bankid));
							trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key() );
							trace.set("traceDateTime",DateUtil.getNowDateTime() ) ;
							trace.set("traceDate",DateUtil.getNowDate() ) ;
							trace.set("modifyDateTime",DateUtil.getNowDateTime() ) ;
							trace.set("modifyDate",DateUtil.getNowDate() ) ;
							trace.set("traceRemark","商银信-在线充值,"+SYXPayKit.PAYOUT_BANK_CODE.get(bankid) ) ;
							trace.set("traceAmount", payAmount );
							trace.set("bankRemark", "商银信,认证支付,发起申请" );
							trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.SYX.key() );
							trace.set("userName" , user.getStr("userName"));
							if(trace.save()){
								rs.put("rType", "SYX");
								rs.put("rData", traceCode);
								return succ("SYX", rs);
							}else{
								return Message.error("14", "生成充值订单失败!", null ) ;
							}
						}else {
							System.out.println("商银信充值失败："+JSON.toJSONString(result)+"；转连连支付了");
						}
					}
				}
			}
		}

		Map<String , String > payParam = initPayInfo() ;
		String app_request = getPara("app_request","");
		if( StringUtil.isBlank( app_request) == false ){
			payParam.put("app_request", app_request ) ;
		}else{
			String f = getPara("from","");
			if(f.equals("PC") == false){
				payParam.put("app_request", "3" ) ;
			}
		}
		payParam.put("user_id", userCode ) ;
		payParam.put("name_goods", "易融恒信用户充值");
		
		String mobileDesc = getPara("md","");
		if( StringUtil.isBlank(mobileDesc) == false){
			payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.[Mobile-" + mobileDesc + "]");
		}else{
			payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.");
		}
		
		payParam.put("pay_type", "D");
		payParam.put("money_order", String.valueOf( payAmount ) );
		payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
		payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
		payParam.put("userreq_ip", getRequestIP() ) ;
		payParam.put("bank_code", bankType ) ;
		//payParam.put("no_agree", agreeCode ) ;
		payParam.put("money_order", String.valueOf( payAmount/10.00/10.00 )) ;
		payParam.put("id_type", "0" ) ;
		payParam.put("id_no", cardId ) ;
		payParam.put("acct_name", trueName ) ;
		payParam.put("card_no", bankNo ) ;
		//风控信息
		payParam.put("risk_item", bindRiskInfo( user ));
		sign4lianlianWithRSA( payParam );
		//订单入库
		RechargeTrace trace = map2trace(payParam) ;
		trace.set("traceAmount", payAmount );
		trace.set("bankRemark", "连连支付,认证支付,发起申请" );
		trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
		trace.set("userName" , user.getStr("userName"));
		trace.set("userBankNo",bankNo);
		
		//缓存订单
		Memcached.set("lianlian_recharge_"+payParam.get("no_order"), payParam.get("info_order"), 10*60*1000);
		
		rs.put("rType", "LLZF");
		rs.put("rData", payParam);
		if( trace.save() ){
			return Message.succ("LLZF", rs );
		}else{
			return Message.error("14", "生成充值订单失败!", null ) ;
		}
		
	}
	
	//TODO 认证支付代扣
	@SuppressWarnings("unused")
	@ActionKey("/lianlian_authPay")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class , PkMsgInterceptor.class})
	public Message authPay4lianlian(){
//		long payAmount = 0 ;
//		try{
//			payAmount = getParaToLong("payAmount");
//			if( payAmount <= 0 ){
//				return error("01","参数异常", payAmount);
//			}
//			if(payAmount<10000){
//				return error("777", "最低充值金额100元", payAmount);
//			}
//		}catch(Exception e){
//			return error("01","参数异常," + e.getMessage(), null );
//		}
//		
//		String userCode = getUserCode() ;
//		
//		BanksV2 bankV2 = BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where userCode=?" , userCode) ;
//		if( bankV2 == null ){
//			return error("02", "未绑定理财卡", null ) ;
//		}
//		String cardId = "";
//		String bankType = bankV2.getStr("bankType");
//		String agreeCode = bankV2.getStr("agreeCode");
//		String trueName = bankV2.getStr("trueName");
//		String bankNo = bankV2.getStr("bankNo");
//		try {
//			cardId = CommonUtil.decryptUserCardId( bankV2.getStr("cardid") );
//		} catch (Exception e) {
//		}
//		
//		if( StringUtil.isBlank(cardId) == true){
//			return error("10", "未做实名认证", null ) ;
//		}
//		
//		String msgMac = getPara("msgMac","");//如果是空的话，就按老app兼容
//		User user = userService.findById(userCode) ;
//		if(StringUtil.isBlank(msgMac)){
//			
//			Map<String , String > payParam  = initPayInfo() ;
//			String app_request = getPara("app_request","");
//			if( StringUtil.isBlank( app_request) == false ){
//				payParam.put("app_request", app_request ) ;
//			}else{
//				String f = getPara("from","");
//				if(f.equals("PC") == false){
//					payParam.put("app_request", "3" ) ;
//				}
//			}
//			payParam.put("user_id", userCode ) ;
//			payParam.put("name_goods", "易融恒信用户充值");
//			
//			String mobileDesc = getPara("md","");
//			if( StringUtil.isBlank(mobileDesc) == false){
//				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.[Mobile-" + mobileDesc + "]");
//			}else{
//				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.");
//			}
//			
//			payParam.put("pay_type", "D");
//			payParam.put("money_order", String.valueOf( payAmount ) );
//			payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
//			payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
//			payParam.put("userreq_ip", getRequestIP() ) ;
//			payParam.put("bank_code", bankType ) ;
//			//payParam.put("no_agree", agreeCode ) ;
//			payParam.put("money_order", String.valueOf( payAmount/10.00/10.00 )) ;
//			payParam.put("id_type", "0" ) ;
//			payParam.put("id_no", cardId ) ;
//			payParam.put("acct_name", trueName ) ;
//			payParam.put("card_no", bankNo ) ;
//			//风控信息
//			payParam.put("risk_item", bindRiskInfo( user ));
//			sign4lianlianWithRSA( payParam );
//			//订单入库
//			RechargeTrace trace = map2trace(payParam) ;
//			trace.set("traceAmount", payAmount );
//			trace.set("bankRemark", "连连支付,认证支付,发起申请" );
//			trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
//			trace.set("userName" , user.getStr("userName"));
//			trace.set("userBankNo",bankNo);
//			//缓存订单
//			Memcached.set("lianlian_recharge_"+payParam.get("no_order"), payParam.get("info_order"), 10*60*1000);
//			if( trace.save() ){
//				return Message.succ("ok",payParam);
//			}else{
//				return Message.error("14", "生成充值订单失败!", null ) ;
//			}
//		}else{
//			//验证短信验证码
//			if(CommonUtil.validateSMS("SMS_MSG_RECHARGE_" + userCode, msgMac) == false){
//				//记录日志
//				BIZ_LOG_WARN(userCode, BIZ_TYPE.RECHARGE, "用户申请充值失败，短信验证码不正确");
//				return error("01", "短信验证码不正确", "");
//			}
//		}
//
//
//		Map<String,Object> rs = new HashMap<String, Object>();
//
//		//先判断是不是开启了商银信的支付分流
//		String SYX_PAYOUT_SWITCH = "";
//		try {
//			SYX_PAYOUT_SWITCH = (String) CACHED.get("ST.SYX_PAYOUT_SWITCH");
//		} catch (Exception e) {
//			SYX_PAYOUT_SWITCH = "";
//		}
//		if(StringUtil.isBlank(SYX_PAYOUT_SWITCH)== false && SYX_PAYOUT_SWITCH.equals("ON")){
//			String[] fld = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, null);
//			//商银信本月内成功充值总额
//			long syx_amount = rtService.countAmountByRechargeType(RechargeTrace.RECHARGE_TYPE.SYX,fld[0],fld[1]);
//			//充值总额判断
//			if(syx_amount < 9999999900L){
//				//星期四到星期天 ，如果银行类型符合，用户手机号无异，就是用商银信代扣
//				boolean validateDay = false;
//				try {
//					Calendar cal = Calendar.getInstance();
//					int d = cal.get(Calendar.DAY_OF_WEEK);
//					String [] day_week = ((String) CACHED.get("ST.SYX_PAYOUT_DAY4WEEK")).split(",");
//					if(day_week != null){
//						for (int i = 0; i < day_week.length; i++) {
//							if(Integer.parseInt(day_week[i]) == d)
//								validateDay = true;
//						}
//					}
//				} catch (Exception e) {
//				}
//				if(validateDay){
//					String bankid = "";
//					String mobileNo ="";
//					try {
//						bankid = SYXPayKit.LianLian_PayOut_CONVERT_BANK_CODE.get(bankType);//连连支付银行编码转商银信，没有则空
//					} catch (Exception e) {
//						bankid = "";
//					}
//					try {
//						mobileNo = userService.getMobile(userCode);//获取并验证用户手机号
//					} catch (Exception e) {
//						mobileNo = "";
//					}
//					if(StringUtil.isBlank(bankid) == false && StringUtil.isBlank(mobileNo) == false){
//						String traceCode = CommonUtil.genShortUID();
//						Map<String,String> result = SYXPayKit.payOut(traceCode, "1", payAmount , "账户充值", bankid, bankNo, trueName, cardId, mobileNo);
//						if(result.get("retCode").equals("0000")){
//							RechargeTrace trace = new RechargeTrace();
//							trace.set("userBankNo",bankNo);
//							trace.set("traceCode", traceCode);
//							trace.set("bankTraceCode", result.get("orderId"));
//							trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key() );	//A-已受理
//							trace.set("bankRemark", "商银信,认证支付,发起申请" );
//							trace.set("userCode" , userCode ) ;
//							trace.set("userTrueName" ,trueName);
//							trace.set("userBankCode" ,bankid);
//							trace.set("userBankName", SYXPayKit.PAYOUT_BANK_CODE.get(bankid));
//							trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key() );
//							trace.set("traceDateTime",DateUtil.getNowDateTime() ) ;
//							trace.set("traceDate",DateUtil.getNowDate() ) ;
//							trace.set("modifyDateTime",DateUtil.getNowDateTime() ) ;
//							trace.set("modifyDate",DateUtil.getNowDate() ) ;
//							trace.set("traceRemark","商银信-在线充值,"+SYXPayKit.PAYOUT_BANK_CODE.get(bankid) ) ;
//							trace.set("traceAmount", payAmount );
//							trace.set("bankRemark", "商银信,认证支付,发起申请" );
//							trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.SYX.key() );
//							trace.set("userName" , user.getStr("userName"));
//							if(trace.save()){
//								rs.put("rType", "SYX");
//								rs.put("rData", traceCode);
//								return succ("SYX", rs);
//							}else{
//								return Message.error("14", "生成充值订单失败!", null ) ;
//							}
//						}else {
//							System.out.println("商银信充值失败："+JSON.toJSONString(result)+"；转连连支付了");
//						}
//					}
//					
//				}
//			}
//		}
//		
//		Map<String , String > payParam = initPayInfo() ;
//		String app_request = getPara("app_request","");
//		if( StringUtil.isBlank( app_request) == false ){
//			payParam.put("app_request", app_request ) ;
//		}else{
//			String f = getPara("from","");
//			if(f.equals("PC") == false){
//				payParam.put("app_request", "3" ) ;
//			}
//		}
//		payParam.put("user_id", userCode ) ;
//		payParam.put("name_goods", "易融恒信用户充值");
//		
//		String mobileDesc = getPara("md","");
//		if( StringUtil.isBlank(mobileDesc) == false){
//			payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.[Mobile-" + mobileDesc + "]");
//		}else{
//			payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.");
//		}
//		
//		payParam.put("pay_type", "D");
//		payParam.put("money_order", String.valueOf( payAmount ) );
//		payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
//		payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
//		payParam.put("userreq_ip", getRequestIP() ) ;
//		payParam.put("bank_code", bankType ) ;
//		//payParam.put("no_agree", agreeCode ) ;
//		payParam.put("money_order", String.valueOf( payAmount/10.00/10.00 )) ;
//		payParam.put("id_type", "0" ) ;
//		payParam.put("id_no", cardId ) ;
//		payParam.put("acct_name", trueName ) ;
//		payParam.put("card_no", bankNo ) ;
//		//风控信息
//		payParam.put("risk_item", bindRiskInfo( user ));
//		sign4lianlianWithRSA( payParam );
//		//订单入库
//		RechargeTrace trace = map2trace(payParam) ;
//		trace.set("traceAmount", payAmount );
//		trace.set("bankRemark", "连连支付,认证支付,发起申请" );
//		trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
//		trace.set("userName" , user.getStr("userName"));
//		trace.set("userBankNo",bankNo);
//		
//		//缓存订单
//		Memcached.set("lianlian_recharge_"+payParam.get("no_order"), payParam.get("info_order"), 10*60*1000);
//		
//		if( trace.save() ){
//			rs.put("rType", "LLZF");
//			rs.put("rData", payParam);
//			return Message.succ("LLZF", rs );
//		}else{
//			return Message.error("14", "生成充值订单失败!", null ) ;
//		}
//		
		return Message.error("14", "生成充值订单失败!", null ) ;
	}
	
	//TODO 商银信，代扣后轮询查询接口，如果限额的话就转连连支付了
	/**
	 * 商银信代扣查询，如果限额就转连连支付
	 * @return
	 */
	@ActionKey("/chayixia")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message chayixia(){
		Map<String,Object> rs = new HashMap<String, Object>();
		Map<String,String> syx_rsult = new HashMap<String, String>();
		String traceCode = getPara("traceCode","");
		RechargeTrace rt = RechargeTrace.rechargeTraceDao.findById(traceCode);
		if(rt == null){
			return error("01", "充值订单不存在", false);
		}
		if(rt.getStr("bankState").equals("B") && rt.getStr("traceState").equals("B")){
			syx_rsult.put("st", "B");
			syx_rsult.put("inf", "充值成功");
			rs.put("rType", "SYX");
			rs.put("rData", syx_rsult);
		}else if(rt.getStr("bankState").equals("A") && rt.getStr("traceState").equals("F")){
			syx_rsult.put("st", "A");
			syx_rsult.put("inf", "充值中");
			rs.put("rType", "SYX");
			rs.put("rData", syx_rsult);
		}else if(rt.getStr("bankState").equals("A") && rt.getStr("traceState").equals("A")){
			syx_rsult.put("st", "A");
			syx_rsult.put("inf", "充值中");
			rs.put("rType", "SYX");
			rs.put("rData", syx_rsult);
		}
//		else if(rt.getStr("bankState").equals("X") && rt.getStr("traceState").equals("C")){
		else{
			//商银信失败了，但是转连连支付了，这种情况更改状态以便记录
			rt.set("bankState", "Y");
			rt.set("traceState", "Y");
			rt.update();
			//商银信失败，转连连支付
			BanksV2 bankV2 = BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where userCode=?" , rt.getStr("userCode")) ;
			String bankCode = bankV2.getStr("bankType");
//			String agreeCode = bankV2.getStr("agreeCode");
			String trueName = bankV2.getStr("trueName");
			String bankNo = bankV2.getStr("bankNo");
			String cardId = "";
			try {
				cardId = CommonUtil.decryptUserCardId( bankV2.getStr("cardid") );
			} catch (Exception e) {
			}
			
			if( StringUtil.isBlank(cardId) == true){
				return error("10", "未做实名认证", null ) ;
			}
			User user = userService.findById(rt.getStr("userCode")) ;
			Map<String , String > payParam = initPayInfo() ;
			String app_request = getPara("app_request","");
			if( StringUtil.isBlank( app_request) == false ){
				payParam.put("app_request", app_request ) ;
			}else{
				String f = getPara("from","");
				if(f.equals("PC") == false){
					payParam.put("app_request", "3" ) ;
				}
			}
			
			payParam.put("user_id", rt.getStr("userCode") ) ;
			payParam.put("name_goods", "易融恒信用户充值");
			
			String mobileDesc = getPara("md","");
			if( StringUtil.isBlank(mobileDesc) == false){
				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.[Mobile-" + mobileDesc + "]");
			}else{
				payParam.put("info_order", "易融恒信理财充值-连连支付认证充值.");
			}
			
			payParam.put("pay_type", "D");
			payParam.put("money_order",CommonUtil.yunsuan(rt.getLong("traceAmount")+"", "100.00", "chu", 2).doubleValue()+"");
			payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
			payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
			payParam.put("userreq_ip", getRequestIP() ) ;
			payParam.put("bank_code", bankCode ) ;
			//payParam.put("no_agree", agreeCode ) ;
			payParam.put("id_type", "0" ) ;
			payParam.put("id_no", cardId ) ;
			payParam.put("acct_name", trueName ) ;
			payParam.put("card_no", bankNo ) ;
			//风控信息
			payParam.put("risk_item", bindRiskInfo( user ));
			sign4lianlianWithRSA( payParam );
			//订单入库
			RechargeTrace trace = map2trace(payParam) ;
			trace.set("traceAmount", rt.getLong("traceAmount") );
			trace.set("bankRemark", "连连支付,认证支付,发起申请" );
			trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
			trace.set("userName" , user.getStr("userName"));
			trace.set("userBankNo",bankNo);
			
			
			if( trace.save() ){
				rs.put("rType", "LLZF");
				rs.put("rData", payParam);
				return Message.succ("LLZF", rs );
			}else{
				return error("01", "生成订单异常，请联系客服", null);
			}
		}
//		else{
//			result.put("st", "C");
//			result.put("inf", rt.getStr("bankRemark"));
//		}
		return succ("SYX",rs);
	}
	
	@ActionKey("/validateAuthCard")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message _111(){
		String userCode = getPara("userCode") ;
		String bankNo = getPara("bankNo");
		boolean x = banksService.isAuthCard(userCode, bankNo);
		return succ("查询认证信息完成",x);
	}

	@ActionKey("/lianlian_queryBankByBin")
	@AuthNum(value=999)
	@Before({ PkMsgInterceptor.class})
	public Message queryBankInfoByBin(){
		String cardNo = getPara("cardNo");
		if(StringUtil.isBlank(cardNo) == true ){
			return error("01", "无卡号", null ) ;
		}
		
		
		Map<String, String> bankInfo = banksService.queryBankInfoByBin(cardNo);
		if(null == bankInfo){
			return error("04", "查询失败,请检查卡号是否正确!", null );
		}
		
		return succ("ok", bankInfo ) ;
		
//		Map<String , String > requestInfo = new TreeMap<String , String>();
//		requestInfo.put("oid_partner", MER_NO );
//		requestInfo.put("sign_type", "RSA");
//		requestInfo.put("card_no", cardNo);
////		requestInfo.put("oid_partner", "");
//		sign4lianlianWithRSA( requestInfo ) ;
//		
//		HttpRequestor http = new HttpRequestor() ;
//		try {
//			String responseBody = http.doPost("https://yintong.com.cn/queryapi/bankcardbin.htm", JSONObject.toJSONString(requestInfo) ) ;
//			@SuppressWarnings("unchecked")
//			Map<String , String > responseData = JSONObject.parseObject(responseBody , TreeMap.class ) ;
//			
//			String resultCode = responseData.get("ret_code");
//			if( "0000".equals(resultCode) == false ){
//				return error("04", "查询异常," + responseData.get("ret_msg"), null );
//			}
//			
//			if( checkSign4lianlianWithRSA( responseData ) == false ){
//				//签名验证失败
////				renderText("00");
//				return error("04", "请求远程接口失败" , null ) ;
//			}
//			
//
//			responseData.remove("ret_code");
//			responseData.remove("ret_msg");
//			responseData.remove("sign_type");
//			responseData.remove("sign");
//			
//			return succ("ok", responseData ) ;
//			
//		} catch (Exception e) {
//			return error("03", "请求异常," + e.getMessage(), null ) ;
//		}
		
	}
	
	
	@ActionKey("/lianlian_webBankPay")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class , PkMsgInterceptor.class})
	public Message webBankPay4lianlian(){
		long payAmount = 0;
		try{
			payAmount = getParaToLong("payAmount");
//			if( payAmount <= 0 ){
//				return error("01","参数异常", payAmount);
//			}
//			if(payAmount<50000){
//				return error("777", "最低充值金额500元", payAmount);
//			}
		}catch(Exception e){
			return error("01","参数异常," + e.getMessage(), null );
		}
		//String hostName = getPara("hostName") ;	//主域名地址
		String bankCode = getPara("bankType");
		String userCode = getUserCode() ;
		String bankName = BANK_CODE.get(bankCode) ;
		if( StringUtil.isBlank(bankName) == true ){
			return Message.error("13", "银行信息异常" , bankCode ) ;
		}
		
		Map<String , String > payParam = initPayInfo() ;
		payParam.put("user_id", userCode ) ; 
		payParam.put("notify_url", NOTIFY_HOST + "/lianlian_notify4lianlian");
		payParam.put("url_return", NOTIFY_HOST + "/lianlian_showResult4lianlian") ;
		payParam.put("userreq_ip", getRequestIP() ) ;
		payParam.put("bank_code", bankCode ) ;
		payParam.put("money_order", String.valueOf( payAmount/10.00/10.00 )) ;

		User user = userService.findById(userCode) ;
		payParam.put("risk_item", bindRiskInfo( user ));
		sign4lianlianWithRSA( payParam );
		
		//订单入库
		RechargeTrace trace = map2trace(payParam) ;
		trace.set("traceAmount", payAmount );
		trace.set("userName", user.getStr("userName") ) ;
		
		if( trace.save() ){
			return Message.succ("ok", payParam );
		}else{
			return Message.error("14", "生成充值订单失败!", null ) ;
		}
		
	}
	
	private RechargeTrace map2trace(Map<String , String> map ){
		String userCode = String.valueOf( map.get("user_id") );
		UserInfo info = userInfoService.findById(userCode) ;
		
		RechargeTrace trace = new RechargeTrace();
		trace.set("traceCode", map.get("no_order"));
		trace.set("bankTraceCode", map.get("no_order"));
		trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key() );	//A-已受理
		trace.set("bankRemark", "连连支付,WEB网银支付,发起申请" );
		trace.set("userCode" , userCode ) ;
		if( info != null )
			trace.set("userTrueName" , info.getStr("userCardName"));
		
		String tmpBankCode = String.valueOf( map.get("bank_code")) ;
		trace.set("userBankCode" ,tmpBankCode);
		trace.set("userBankName", BANK_CODE.get(tmpBankCode));
		trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key() );
		trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.LL.key() );
		trace.set("traceDateTime",DateUtil.getNowDateTime() ) ;
		trace.set("traceDate",DateUtil.getNowDate() ) ;
		trace.set("modifyDateTime",DateUtil.getNowDateTime() ) ;
		trace.set("modifyDate",DateUtil.getNowDate() ) ;
		trace.set("traceRemark","连连支付-在线充值,"+BANK_CODE.get(tmpBankCode) ) ;
		
		return trace ;
	}

	private int checkNotifyResult(Map<String , String> responseData){
		
		if( checkSign4lianlianWithRSA( responseData ) == false ){
			//签名验证失败
//			renderText("00");
			return 1 ;
		}
		
		//正常处理
		String tmpMerNo = String.valueOf( responseData.get("oid_partner") );
		if( tmpMerNo.equals(MER_NO) == false ){
			//商户号不符
			return 2 ;
		}

		String orderNo = responseData.get("no_order") ;
		String bankOrderNo = responseData.get("oid_paybill");
		double tmpPayAmount = Double.valueOf( responseData.get("money_order") );
		long payAmount = CommonUtil.yunsuan(tmpPayAmount+"",100+"", "cheng", 0).longValue();
		String payResult = responseData.get("result_pay");
		String settleDate = responseData.get("settle_date");
		String orderInfo = responseData.get("info_order");
		//String bankCode = responseData.get("bank_code");
		//String payType = responseData.get("pay_type");
		
		
		RechargeTrace trace = RechargeTrace.rechargeTraceDao.findById( orderNo );
		if( trace == null ){
			//交易不存在
			return 3 ;
		}
		
		if( payAmount != trace.getLong("traceAmount")){
			//交易金额不符
			return 4 ;
		}
		
//		if( bankCode.equals( trace.getStr("userBankCode")) == false){
//			//交易银行不匹配
//			return 5 ;
//		}
		String bankState = trace.getStr("bankState") ;
		if( bankState.equals( BANK_STATE.ACCEPT.key()) == false ){
			//如果交易状态是SUCCESS 忽略
//			if( bankState.equals( BANK_STATE.SUCCESS.key() ) == true ){
//				//已经成功,继续处理
//			}else{
//				//失败或者其他情况
//				
//			}
			//交易已处理
			return 0 ;
		}

		//锁定订单,避免重复操作
		int lockOrder = Db.update("update t_recharge_trace set traceState=? where traceCode=? and traceState=? " , 
				BANK_STATE.LOCKED.key() , orderNo ,  BANK_STATE.ACCEPT.key() );
		if( lockOrder < 1 ){
			return 0 ;
		}
		
		//更新订单状态
		trace.set("bankTraceCode", bankOrderNo );
		trace.set("bankRemark", orderInfo );
		if( "SUCCESS".equals(payResult) ){
			//成功
			trace.set("bankState", BANK_STATE.SUCCESS.key() );
			trace.set("traceState",  TRACE_STATE.SUCCESS.key() ) ;
			trace.set("settleDate", settleDate );
			trace.set("traceRemark", "连连支付充值成功");
			trace.set("okDateTime", DateUtil.getNowDateTime());
			if( AdminConfig.isDevMode == true )
				payAmount = 0;
			
			try {
				String mobile = userService.getMobile(trace.getStr("userCode"));
				
				String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE.replace("[userName]",trace.getStr("userName")).replace("[payAmount]", tmpPayAmount+"");
				SMSLog smsLog = new SMSLog();
				smsLog.set("mobile", mobile);
				smsLog.set("content", content);
				smsLog.set("userCode", trace.getStr("userCode"));
				smsLog.set("userName", trace.getStr("userName"));
				smsLog.set("type", "12");smsLog.set("typeName", "在线充值");
				smsLog.set("status", 9);
				smsLog.set("sendDate", DateUtil.getNowDate());
				smsLog.set("sendDateTime", DateUtil.getNowDateTime());
				smsLog.set("break", "");
				smsLogService.save(smsLog);
			} catch (Exception e) {
				
			}
			//修改资金账户
			fundsServiceV2.recharge( trace.getStr("userCode"), payAmount, 0 , "连连支付-充值完成!",SysEnum.traceType.C.val());
		}else{
			//失败
			trace.set("bankState", BANK_STATE.FAILD.key() );
			trace.set("traceState",  TRACE_STATE.FAILD.key() ) ;
			trace.set("traceRemark", "连连支付充值失败");
		}
		
		if( trace.update() ){
			return 0 ;
			//forward("/pay/paySuccess.html");
		}else{
			return 6 ;
		}
	}

	@SuppressWarnings("unchecked")
	@ActionKey("/lianlian_notify4lianlian")
	public void payNotify4lianlian(){
		JSONObject result = new JSONObject();
		result.put("ret_code", "0001" ) ;
		result.put("ret_msg", "未收到请求数据" ) ;
		//异步通知
		String requestBody = getRequestString4stream();
		//requestBody = "{\"bank_code\":\"01050000\",\"dt_order\":\"20160123153406\",\"info_order\":\"易融恒信理财充值-连连支付认证充值.\",\"money_order\":\"0.01\",\"no_order\":\"0073ba95f924436e9fbd69ce323914ae\",\"oid_partner\":\"201505261000341507\",\"oid_paybill\":\"2016012319667049\",\"pay_type\":\"D\",\"result_pay\":\"SUCCESS\",\"settle_date\":\"20160123\",\"sign\":\"Lo5YnFmLAfZNTHsax62/cphA/x9AHu99nrJiejQcRKdhzktWWjZqDqoJ5c/j+2CGRsy7KD9ydiCJgdXWYyimdHK0bV3mjuAApEcj3PzzO5Q+JBjaGapxWYC+7qUQWJnDwO2Ee6cnmpVQs8CD4Baya9X3d3vtgsSQYMzm0OZU/00=\",\"sign_type\":\"RSA\"} ";
//		String requestBody = "{\"dt_order\":\"20151210161237\",\"info_order\":\"易融恒信理财充值-连连支付网银充值.\",\"money_order\":\"0.01\",\"no_order\":\"4a9f55763869442ea70977ec3dea9f7f\",\"oid_partner\":\"201505261000341507\",\"oid_paybill\":\"2015121011656689\",\"pay_type\":\"1\",\"result_pay\":\"SUCCESS\",\"settle_date\":\"20151210\",\"sign\":\"GQmHQ3sMQdpgx+H+K1Ij5W17TGAoDvx+ta2U3wG9cliVoTZ7en9pcEe3NaTNXn7tuk5Fpu7HJMX6R1nK/anPpd1NjBJrSNqahuaGYskj9xQCojgEXac5GcBPUyjG197c2sg+ZOoDY+aWSq33Atp4rdNoYZhl8nW0b93rNyxIF5g=\",\"sign_type\":\"RSA\"}";
		if( StringUtil.isBlank( requestBody ) == true ){
			renderJson( result );
		}
		Map<String , String> responseData = null ;
		try{
			responseData = JSONObject.parseObject(requestBody , TreeMap.class ) ;
			if( responseData != null ){
				int retCode = checkNotifyResult(responseData);
				result.put("ret_code", "000" + retCode ) ;
				if( retCode == 0 ){
					result.put("ret_msg", "交易成功" ) ;
				}else{
					result.put("ret_msg", "交易失败，返回码：" + retCode ) ;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("ret_code", "000x" ) ;
			result.put("ret_msg", "数据解析异常:" + requestBody ) ;
//			renderJson(result);
		}
		renderJson(result);
		
	}

	@SuppressWarnings("unchecked")
	@ActionKey("/lianlian_showResult4lianlian")
	public void showResult4lianlian(){
		
		boolean isMobile = false ;
		//实时通知
		Map<String , String> responseData ;

		//从缓存获取订单信息
		String res_data = getPara("res_data");
		if( StringUtil.isBlank(res_data) == true ){
			responseData = new TreeMap<String , String>(); 
			//pc
			//序列化请求数据
			Enumeration<String> keys = getParaNames() ;
			while( keys.hasMoreElements() ){
				String key = keys.nextElement() ;
				responseData.put(key, getPara(key)) ;
			}
		}else{
			//mobile
			responseData  = JSONObject.parseObject(res_data , TreeMap.class );
			isMobile = true ;
		}
		
		int retCode = checkNotifyResult(responseData);
		if( isMobile ){
			if( retCode == 0 ){
				redirect("http://m.yrhx.com/m/#pay_success");
			}else{
				redirect("http://m.yrhx.com/m/#pay_failed");
			}
		}else{
			if( retCode == 0 ){
				forward("/pay/paySuccess.html",true);
			}else{
				forward("/pay/payFaild.html",false);
			}
		}
	}

	@ActionKey("/lianlian_pay1")
	public void test4pay(){
		forward("/pay/pay4lianlian.html",true);
	}
	
	@ActionKey("/lianlian_authPay4lianlian")
	public void authPay(){
		forward("/pay/authPay4lianlian.html",true);
	}
	
	
	private Map<String , String> initRepayInfo(){
		Map<String , String> trace = new TreeMap<String ,String>();
		String dt = DateUtil.getNowDateTime() ;
		
		trace.put("platform", "yrhx.com") ;
		trace.put("oid_partner", MER_NO );
		trace.put("sign_type", "RSA");
		trace.put("no_order", UIDUtil.generate() );
		trace.put("dt_order", dt );
		trace.put("notify_url", NOTIFY_HOST + "/lianlian_repayNotify4lianlian");
		trace.put("api_version", "1.2");
		
		return trace ;
	}
	
	/**
	 * 	连连支付代付（提现）申请
	 * 
	 * @return
	 */
	@ActionKey("/lianlian_repay4lianlian")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class , PkMsgInterceptor.class})
	public Message repay4lianlian(){
		String applyCode = getPara("withdrawCode");
		//省市/处理
		String provinceCode = getPara("provinceCode","");
		String cityCode = getPara("cityCode","");
		
		//applyCode = "7c084fe9d7774167ac208d06419fc041";
		return repay4lianlian(applyCode,provinceCode,cityCode) ;
	}
	
	//TODO 代付
	public Message repay4lianlian(String applyCode,String provinceCode,String cityCode){

		WithdrawTrace trace = WithdrawTrace.withdrawTraceDao.findById(applyCode) ;
		if( trace == null ){
			return error("03", "无此提现申请" , null );
		}
		
		String status = trace.getStr("status") ;
		if( "1".equals(status) == false ){
			Db.update("update t_withdraw_trace set status = '0' where withdrawCode = ?",applyCode);
			return error("01", "不符合提现条件-" + status , status );
		}
		
		String bankCode = trace.getStr("bankType");
		String bankName = trace.getStr("bankName");
		String cardNo = trace.getStr("bankNo");
		String accName = trace.getStr("userTrueName");
		long rePayAmount = trace.getLong("withdrawAmount");
		int sxf = trace.getInt("sxf");
		rePayAmount = rePayAmount - sxf;
		double rePayAmount4double = CommonUtil.yunsuan(rePayAmount + "", "100.00", "chu", 2).doubleValue();
		String infoOrder = "连连支付提现-" + accName + "(" + trace.getStr("userName") +")";
		String prcptcd = "";

		Map<String , String> repayInfo = initRepayInfo() ;
		repayInfo.put("money_order", String.valueOf( rePayAmount4double ) );
		repayInfo.put("acct_name", accName );
		repayInfo.put("card_no", cardNo );
		repayInfo.put("flag_card", "0" );
		repayInfo.put("bank_code", bankCode);
		repayInfo.put("province_code", provinceCode);
		repayInfo.put("city_code", cityCode);
		repayInfo.put("brabank_name", bankName);
		repayInfo.put("info_order", infoOrder);
		repayInfo.put("prcptcd", prcptcd);
		repayInfo.put("no_order", applyCode);
		sign4lianlianWithRSA( repayInfo );
		
		HttpRequestor http = new HttpRequestor() ;
		JSONObject json = null ;
		String responseBody = "";
		try {
			responseBody = http.doPost( "https://traderapi.lianlianpay.com/cardandpay.htm" , JSONObject.toJSONString(repayInfo) ) ;
			json = JSONObject.parseObject(responseBody) ;
		} catch (Exception e) {
			e.printStackTrace();
			json = new JSONObject() ;
			json.put("ret_code", "xxxx");
			json.put("ret_msg", "系统异常：" + e.getMessage()+"--------"+responseBody );
			Db.update("update t_withdraw_trace set status = '0' where withdrawCode = ?",applyCode);
		}
		
		String retCode = json.getString("ret_code") ;
		
		if( "0000".equals(retCode) == true ){
			//请求成功
			/*String payResult = json.getString("result_pay");
			if( "SUCCESS".equals(payResult) == true ){
				trace.put("status","3");
				trace.put("withdrawType","LL");
				trace.put("withdrawRemark",json.getString("info_order") + 
						".付款成功,流水号：" + json.getString("oid_paybill"));
			}else if( "PROCESSING".equals(payResult) == true ){
				trace.put("status","2");
			}else if( "WAITING".equals(payResult) == true ){
				trace.put("status","2");
			}else if( "FAILURE".equals(payResult) == true ){
				trace.put("status","4");
				trace.put("withdrawRemark","付款失败：" + json.getString("memo"));
			}else{
				trace.put("status","4");
			}

			trace.put("modifyDateTime", DateUtil.getNowDateTime() ) ;
			if( trace.update() ){
				return succ("ok", "提现已受理！");
			}*/
			trace.set("status","2");
			trace.set("withdrawType","LL");
			trace.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
			if( trace.update() ){
				try {
					String mobile = userService.getMobile(trace.getStr("userCode"));
					
					String content = CommonUtil.SMS_MSG_WITHDRAW_ONLINE.replace("[userName]", trace.getStr("userName")).replace("[payAmount]", rePayAmount4double+"").replace("[fee]", CommonUtil.yunsuan(sxf + "", "100.00", "chu", 2).doubleValue()+"");
					SMSLog smsLog = new SMSLog();
					smsLog.set("mobile", mobile);
					smsLog.set("content", content);
					smsLog.set("userCode", trace.getStr("userCode"));
					smsLog.set("userName", trace.getStr("userName"));
					smsLog.set("type", "16");smsLog.set("typeName", "提现审核通过");
					smsLog.set("status", 9);
					smsLog.set("sendDate", DateUtil.getNowDate());
					smsLog.set("sendDateTime", DateUtil.getNowDateTime());
					smsLog.set("break", "");
					smsLogService.save(smsLog);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return succ("提现已受理", true);
			}
		}else{
			Db.update("update t_withdraw_trace set status = '0',withdrawType = 'SYS' where withdrawCode = ?",applyCode);
			//暂不处理
			return error("03", String.format("提现处理失败[%s][%s]", retCode , json.getString("ret_msg")), json ) ;
		}
		Db.update("update t_withdraw_trace set status = '0',withdrawType = 'SYS' where withdrawCode = ?",applyCode);
		return error("02", "提现申请失败！" , json);
	}
	
	
	@SuppressWarnings("unchecked")
	@ActionKey("/lianlian_repayNotify4lianlian")
	public void repayNotify4lianlian(){

		JSONObject result = new JSONObject();
		result.put("ret_code", "0001" ) ;
		result.put("ret_msg", "未收到请求数据" ) ;
		//异步通知
		String requestBody = getRequestString4stream();
//		String requestBody = "{\"dt_order\":\"20151210161237\",\"info_order\":\"易融恒信理财充值-连连支付网银充值.\",\"money_order\":\"0.01\",\"no_order\":\"4a9f55763869442ea70977ec3dea9f7f\",\"oid_partner\":\"201505261000341507\",\"oid_paybill\":\"2015121011656689\",\"pay_type\":\"1\",\"result_pay\":\"SUCCESS\",\"settle_date\":\"20151210\",\"sign\":\"GQmHQ3sMQdpgx+H+K1Ij5W17TGAoDvx+ta2U3wG9cliVoTZ7en9pcEe3NaTNXn7tuk5Fpu7HJMX6R1nK/anPpd1NjBJrSNqahuaGYskj9xQCojgEXac5GcBPUyjG197c2sg+ZOoDY+aWSq33Atp4rdNoYZhl8nW0b93rNyxIF5g=\",\"sign_type\":\"RSA\"}";
		if( StringUtil.isBlank( requestBody ) == true ){
			renderJson( result );
		}
		Map<String , String> responseData = null ;
		try{
			responseData = JSONObject.parseObject(requestBody , TreeMap.class ) ;
			int retCode = checkRePayNotifyRequest(responseData);
			result.put("ret_code", "000" + retCode ) ;
			if( retCode == 0 ){
				result.put("ret_msg", "交易成功" ) ;
			}else{
				result.put("ret_msg", "交易失败，返回码：" + retCode ) ;
			}
		}catch(Exception e){
			e.printStackTrace();
			result.put("ret_code", "000x" ) ;
			result.put("ret_msg", "数据解析异常:" + requestBody ) ;
//			renderJson(result);
		}
		renderJson(result);
	}
	
	@SuppressWarnings("unused")
	public int checkRePayNotifyRequest(Map<String , String> request){
		String tmpSignValue = request.get("sign");
		
		String mer_no = request.get("oid_partner");
		if( MER_NO.equals( mer_no ) == false ){
			return 1 ;
		}
		
		if( checkSign4lianlianWithRSA(request) == false ){
			return 5 ;
		}
		String traceCode = request.get("no_order");
		
		int locked = Db.update("update t_withdraw_trace set status='9' where withdrawCode=? and status='2'",traceCode);
		if( locked < 1 ){
			return 0 ;
		}
		
		WithdrawTrace trace = WithdrawTrace.withdrawTraceDao.findById(traceCode) ;
		if( trace == null ){
			return 2 ;
		}
		
		if( "3".equals( trace.get("status"))){
			return 0 ;
		}
		boolean nimei = false;
		String rePayResult = request.get("result_pay");
		if( "SUCCESS".equals(rePayResult) == true ){
			trace.set("status", "3" ) ;
			trace.set("withdrawRemark", "连连支付-提现成功,订单号：" + request.get("oid_paybill") ) ;
			trace.set("okDateTime", DateUtil.getNowDateTime());//成功时间
			fundsServiceV2.withdrawals3_ok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getInt("sxf"),"连连支付-提现成功,订单号："+ request.get("oid_paybill"),SysEnum.traceType.G);
			trace.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
			if(trace.update()){
				nimei = true;
			}
		}else{
			trace.set("status", "4" ) ;
			trace.set("withdrawRemark", String.format("连连支付-提现失败,[%s][%s]", rePayResult ,request.get("oid_paybill"))) ;
			String newWCode = UIDUtil.generate();
			boolean axs = false;
			try {
				//代付通知失败后，不再恢复账户资金和积分，重新生成一笔待审核的提现流水
				axs = withdrawtraceService.madeNewWithdrawTrace(trace);
			} catch (Exception e) {
				axs = false;
			}
			
			//如果生成新的待审核提现流水失败，则恢复账户资金和积分
			if(axs == false){
				fundsServiceV2.withdrawals3_buok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getStr("isScore"),"商银信-提现失败,补回提现金额、积分");
				if(trace.getStr("useFree").equals("y")){
					withdrawFreeService.setFreeCount(trace.getStr("userCode"), -1);
				}
			}else{
				trace.set("status", "Y");
				trace.set("withdrawRemark", String.format("连连支付-提现失败,[%s][%s],自动重新提现(待审)", rePayResult ,request.get("oid_paybill"))) ;
			}
			trace.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
			if(trace.update()){
				nimei = true;
			}
		}
		
		
		if( nimei ){
			return 0 ;
		}else{
			return 3 ;	
		}
		
	}
	
}
