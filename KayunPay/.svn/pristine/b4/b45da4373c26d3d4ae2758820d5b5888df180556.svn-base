package com.dutiantech.controller.portal;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutian.sohusdk.Mail;
import com.dutian.sohusdk.Sender;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.Market;
import com.dutiantech.model.MarketUser;
import com.dutiantech.model.Notice;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.RecommendReward;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.ShortPassword;
import com.dutiantech.model.SliderPic;
import com.dutiantech.model.TempReport;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.plugins.ContractPDFReportV2;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.plugins.PDFReport;
import com.dutiantech.plugins.VehiclePledgeReport;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.MarketService;
import com.dutiantech.service.MarketUserService;
import com.dutiantech.service.NoticeService;
import com.dutiantech.service.PasswordService;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SliderPicService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DESUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;

public class PortalController extends BaseController {

	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
//	private FundsTraceService fundsTraceService = getService(FundsTraceService.class ) ;
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private BanksService banksService = getService(BanksService.class);
	private NoticeService noticeService = getService(NoticeService.class);
	private RecommendInfoService risService = getService(RecommendInfoService.class);
	private RecommendRewardService rmdRewardService = getService(RecommendRewardService.class);
	private MarketService marketService = getService(MarketService.class);
	private MarketUserService marketUserService = getService(MarketUserService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private SliderPicService sliderService = getService(SliderPicService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private PasswordService passwordService = getService(PasswordService.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private LoanApplyService loanApplyService = getService(LoanApplyService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	

	
	/**
	 * 查询个人主页信息
	 * @return
	 */
	@ActionKey("/queryServerTime")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message quertServerTime(){
		return succ("查询服务器日期时间完成", DateUtil.getNowDateTime());
	}
	/**
	 * 查询个人主页信息
	 * @return
	 */
	@ActionKey("/queryMainPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryMainPage(){
		//获取用户标识
		String userCode = getUserCode();
		
		Map<String,Object> resultMap = new HashMap<String, Object>();
		
		User user;
		UserInfo userInfo;
		AutoLoan_v2 autoLoan;
		Object bankCount;
		Funds fund;
		int countTickets = 0;
		try{
			user = userService.findById(userCode);
			userInfo = userInfoService.findById(userCode);
			if(null == user || userInfo == null){
				return error("01", "查询失败", null);
			}
			autoLoan = autoLoanService.findByUserCode(userCode, "A");
			Record record = Db.findFirst("select count(1) bankCount from t_banks_v2 where status = 0 and userCode = ?", userCode);
			bankCount = record.getColumns().get("bankCount");
			fund = fundsServiceV2.findById(userCode);
			countTickets = ticketService.countTickets(userCode);
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.MAINPAGE, "查询失败", e);
			return error("02", "查询失败", e.getMessage());
		}
		
		//用户昵称, 用户积分,用户等级,等级描述,存管账户
		resultMap.put("userName", user.getStr("userName"));
		resultMap.put("userScore", user.getLong("userScore"));
		resultMap.put("vipLevel", user.getInt("vipLevel"));
		resultMap.put("vipLevelName", user.getStr("vipLevelName"));
		String loginid="";
		if(null!=user.getStr("loginId")&&!"".equals(user.getStr("loginId"))){
		try {
			loginid = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.MAINPAGE, "查询失败", e);
			return error("03", "查询失败", e.getMessage());
		}}
		resultMap.put("loginid",loginid);
		
		//账户安全等级 
		String userEmail = user.getStr("userEmail");
		String payPasswd = user.getStr("payPasswd");
		String isAuthed = userInfo.getStr("isAuthed");
		
		if(StringUtil.isBlank(payPasswd)){//支付密码
			resultMap.put("payPwd", 0);
		}else{
			resultMap.put("payPwd", 1);
		}
		if(!StringUtil.isBlank(userEmail) && !"00@yrhx.com".equals(userEmail)){//邮箱
			resultMap.put("email", 1);
		}else{
			resultMap.put("email", 0);
		}
		if("2".equals(isAuthed)){//身份证认证
			resultMap.put("cardId", 1);
		}else{
			resultMap.put("cardId", 0);
		}
		
		//自动投标状态 
		resultMap.put("isAvailable", null == autoLoan ? 0 : 1);
		
		//绑定银行卡数量 
		resultMap.put("countBanksCard", bankCount);
		
		//可用余额   冻结余额   代收本金  代收利息
		long avBalance = fund.getLong("avBalance");//可用余额
		long frozeBalance = fund.getLong("frozeBalance");//冻结余额
		long beRecyPrincipal = fund.getLong("beRecyPrincipal");//代收本金
//		long beRecyInterest = fund.getLong("beRecyInterest");//代收利息
		long totalRecharge = fund.getLong("totalRecharge");//充值统计
		long totalWithdraw = fund.getLong("totalWithdraw");//提现统计
		long rewardRateAmount = fund.getLong("rewardRateAmount");//加息额度
		resultMap.put("avBalance", avBalance);
		resultMap.put("frozeBalance", frozeBalance);
		resultMap.put("beRecyPrincipal", beRecyPrincipal);
		long[] leftAmount = fundsServiceV2.sumLeftAmount4Type(userCode, "E", "N");
		resultMap.put("beRecyAmount", leftAmount[0]+leftAmount[1]);
		resultMap.put("totalRecharge", totalRecharge);
		resultMap.put("totalWithdraw", totalWithdraw);
		resultMap.put("rewardRateAmount", rewardRateAmount);
		resultMap.put("points", fund.getLong("points"));//可用积分
		
		//回收中     招标中   已回收
		resultMap.put("count_hsz", fund.getInt("beRecyCount"));
		resultMap.put("sum_hsz",  fund.getLong("beRecyPrincipal"));
		resultMap.put("sum_yhs",  fund.getLong("reciedPrincipal"));
		resultMap.put("countTickets",  countTickets);
		
		long recoverDuiMonth = loanTransferService.sumTransferDuiAmount(userCode,"B","E");
		long recoverDuiQtr = loanTransferService.sumTransferDuiAmount(userCode,"B","F");
		resultMap.put("recoverDui", recoverDuiMonth+recoverDuiQtr);
		long cAmount = fundsTraceService.sumTraceAmount("00000000", "99999999", "C", "J", userCode);
		long wAmount = fundsTraceService.sumTraceAmount("00000000", "99999999", "G", "D", userCode);
		resultMap.put("cAmount", cAmount);
		resultMap.put("wAmount", wAmount);
		
		return succ("查询成功", resultMap);
	}	
	
	//TODO 发短信
	/**
	 * 发送短信验证码(无需输入手机号)
	 * @return
	 */
	@ActionKey("/sendMsgMac")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message sendMsgMac(){
		Message msg = null;
		
		int type = getParaToInt("type");
		
		if( type == 0 || type == 1 ){//银行卡绑定 修改支付密码 验证图形二维码
			if( validateCaptcha("cv") == false ){
				msg = error("11", "验证码错误", "");
				return msg;
			}
		}
		
		//获取用户标识
		String userCode = getUserCode();
			
		User user = User.userDao.findById(userCode);
		int msgResult = -4;
		String mobile = "";
		String msgMac = CommonUtil.getMathNumber(6);
		String memcachedKey = "";
		String msgContent = "";
		String smsType = "4";
		String smsTypeName = "银行卡绑定";
		switch (type) {
			case 0:
				msgContent = CommonUtil.SMS_MSG_BINDCARD.replace("[code]", msgMac);
				memcachedKey = "SMS_MSG_BINDCARD_";
				smsType = "4";
				smsTypeName = "银行卡绑定";
				break;
			case 1:
				msgContent = CommonUtil.SMS_MSG_PAYPWD.replace("[code]", msgMac);
				memcachedKey = "SMS_MSG_PAYPWD_";
				smsType = "5";
				smsTypeName = "修改支付密码";
				break;
			case 2:
				msgContent = CommonUtil.SMS_MSG_WITHDRAW.replace("[code]", msgMac);
				memcachedKey = "SMS_MSG_WITHDRAW_";
				smsType = "6";
				smsTypeName = "申请提现";
				break;
			case 3:
				msgContent = CommonUtil.SMS_MSG_TRANSFER.replace("[code]", msgMac);
				memcachedKey = "SMS_MSG_TRANSFER_";
				smsType = "7";
				smsTypeName = "手机承接债权";
				break;
			case 4:
				msgContent = CommonUtil.SMS_MSG_RECHARGE.replace("[code]", msgMac);
				memcachedKey = "SMS_MSG_RECHARGE_";
				smsType = "8";
				smsTypeName = "充值";
				break;
			default:
				break;
		}
		
		int smsCount4Day = 0;
		try {
			mobile = DESUtil.decode4string(user.getStr("userMobile"), CommonUtil.DESKEY);
			smsCount4Day = getSMSCount4Day(mobile);
			
			//限制发送频率
			Object object = Memcached.get("msgExpires_" + mobile);
			if(object != null){
				msg = error("02", "相同手机号每分钟只能发送一条短信,请稍后再操作！", "");
			}else{
				if(smsCount4Day >= 50){
					msg = error("02", "相同手机号每天只能发送50条短信,请明天再试！", "");
				}else{
					msgResult = SMSClient.sendSms(mobile, msgContent);
					//记录日志
					smsLogService.save(setSMSLog(mobile, msgContent, smsType,smsTypeName, msgResult));
				}
			}
		} catch (Exception e) {
			msgResult = -3;
		}
		
		if(msgResult != 0){
			msg = error("01", "发送失败", msgResult);
		}else{
			Memcached.set(memcachedKey + userCode , msgMac, 10*60*1000);
			Memcached.set("msgExpires_" + mobile , mobile, 60*1000);
			setSMSCount4Day(mobile, smsCount4Day++);
			msg = succ("发送成功", mobile);
		}
		
		return msg;
	}
	
	private Integer getSMSCount4Day(String mobile){
		String date = DateUtil.getNowDate();
		Object objectDay = Memcached.get("msgExpires_day_" + date + mobile);
		int smsCount4Day = 0;
		if(objectDay != null){
			smsCount4Day = Integer.parseInt(objectDay.toString());
		}

		return smsCount4Day;
	}
	
	private void setSMSCount4Day(String mobile,Integer value){
		String date = DateUtil.getNowDate();
		Memcached.set("msgExpires_day_" + date + mobile , value, 24*60*60*1000);
	}
	
	
	//TODO 发短信
	/**
	 * 用户发送短信验证码(需要手机号)
	 * @param type  0 注册用户  1 找回密码 2 绑定邮箱 3 验证码登录 4 修改手机号17线上申请贷款
	 * @return
	 */
	@ActionKey("/nSendMsgMobile")
	@AuthNum(value=999)
	public void sendMsgMobil(){
		
		Message msg = null;
		
		String mobile = getPara("mobile");
		Integer type = getParaToInt("type");

		msg = error("0x", "暂时关闭注册服务，至2月12日恢复！", "");
		renderJson(msg);
		
		if(StringUtil.isBlank(mobile)){
			msg = error("01", "手机号为空", "");
			renderJson(msg);
			return;
		}
		if(null == type){
			msg = error("01", "发送类型为空", "");
			renderJson(msg);
			return;
		}
		if( type != 3){//先让手机通行
			if( validateCaptcha("cv") == false ){
				msg = error("11", "验证码错误", "");
				renderJson(msg);
				return;
			}
		}
		
		//限制发送频率
		Object object = Memcached.get("msgExpires_" + mobile);
		int smsCount4Day = getSMSCount4Day(mobile);

		if(object != null){
			msg = error("02", "您操作太频繁,请稍后再操作!", "");
		}else{
			int msgResult = -9;
			boolean b = true;
			String msgMac = CommonUtil.getMathNumber(6);
			String memcachedKey = "";
			String msgContent = "";
			String smsType = "0";
			String smsTypeName = "用户注册";
			switch (type) {
				case 0:
					//注册时 验证手机号是否被注册
					User user = userService.find4mobile(mobile);
					if(null != user){
						b = false;
						break;
					}
					memcachedKey = "SMS_MSG_REGISTER_";
					smsType = "0";
					smsTypeName = "用户注册";
					msgContent = CommonUtil.SMS_MSG_REGISTER.replace("[code]", msgMac);
					
//					msg= error("04", "暂时关闭注册服务", "");
//					renderJson(msg);
//					return;
					break;
				case 1:
					memcachedKey = "SMS_MSG_FINDPWD_";
					smsType = "1";
					smsTypeName = "找回密码";
					msgContent = CommonUtil.SMS_MSG_FINDPWD.replace("[code]", msgMac);
					break;
				case 2:
					memcachedKey = "SMS_MSG_BINDEMAIL_";
					smsType = "2";
					smsTypeName = "绑定邮箱";
					msgContent = CommonUtil.SMS_MSG_BINDEMAIL.replace("[code]", msgMac);
					break;
				case 3:
					memcachedKey = "SMS_MSG_LOGIN_";
					smsType = "3";
					smsTypeName = "验证码登录";
					msgContent = CommonUtil.SMS_MSG_LOGIN.replace("[code]", msgMac);
					break;
				case 4:
					User newUser = userService.find4mobile(mobile);
					if (null != newUser) {
						msg= error("04", "新手机号已存在", "");
						renderJson(msg);
						return;
					}
					memcachedKey = "SMS_MSG_PHONE_";
					smsType = "4";
					smsTypeName = "更换手机号";
					msgContent = CommonUtil.SMS_MSG_PHONE.replace("[code]", msgMac);
					break;
//				case 17:
//					memcachedKey = "SMS_MSG_ONLINELOAN_";
//					smsType = "17";
//					smsTypeName = "线上申请贷款";
//					msgContent = CommonUtil.SMS_MSG_ONLINELOAN.replace("[code]", msgMac);
//					break;
//				case 4:
//					memcachedKey = "SMS_MSG_TEST_";
//					smsType = "4";
//					smsTypeName = "现金券测试";
//					msgContent = CommonUtil.SMS_MSG_LOGIN.replace("[code]", msgMac);
//					break;
			}
			try {
				if(b){
					if(smsCount4Day >= 10){
						msg = error("03", "相同手机号每天只能发送10条短信,请明天再试！", "");
					}else{
						msgResult = SMSClient.sendSms(mobile, msgContent);
						if(msgResult != 0){
							msg = error("01", "发送失败", msgResult);
						}else{
							Memcached.set(memcachedKey + mobile , msgMac, 10*60*1000);
							Memcached.set("msgExpires_" + mobile , mobile, 60*1000);
							setSMSCount4Day(mobile, smsCount4Day++);
							msg = succ("发送成功", mobile);
						}
						//记录日志
						smsLogService.save(setSMSLog(mobile, msgContent, smsType,smsTypeName, msgResult));
					}
				}else{
					msg = error("04", "该手机号已经被注册", null);
					//msg = error("04", "紧急维护，临时关闭注册至2月12日！望谅解！", null);
				}
			} catch (Exception e) {
				msg = error("05", "发送失败", null);
			}
		}
		renderJson(msg);
	}
	
	/**
	 * 邮箱认证
	 * @param remark
	 * @return
	 */
	@ActionKey("/sendEmail")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message sendEmail(){
		
		String email = getPara("email");
		
		//验证
		if(StringUtil.isBlank(email)){
			return error("01", "参数错误!", null);
		}
		
		//拼接激活路径
		HttpServletRequest request = getRequest();
		String basePath = request.getScheme()+"://"+request.getServerName()
				+":"+request.getServerPort()+request.getContextPath()+"/";  
		
		//日志记录参数
		JSONObject logJson = new JSONObject();
		logJson.put("email", email);
		
		//获取用户标识
		String userCode = getUserCode();
		
		String params = "";
		
		try {
			params = DESUtil.encode4string(userCode+","+email+","+new Date().getTime(), CommonUtil.DESKEY);
		} catch (Exception e) {
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.EMAIL, "用户申请邮箱认证失败，加密发送内容异常", e);
			return error("02", "发送邮件异常", e.getMessage());
		}
		
		String path = basePath + "emailAuth?userCode="+params;
		
		Sender sender = new Sender() ;
		Mail mail = new Mail() ;
		mail.setTo(email);
		Map<String , String> ps = new HashMap<String , String>();
		ps.put("emailurl", path);
		mail.setParams( ps );
		sender.putMail(mail);
		sender.setTemplate_invoke_name("22170_invoke_10");
		
		boolean b = sender.sendMail();
		if(!b){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.EMAIL, "用户申请邮箱认证失败，发送邮件异常",null);
			return error("02", "发送邮件异常", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.EMAIL, "用户申请邮箱认证成功");
		
		return succ("发送成功", "");
	}

	/**
	 * 邮箱激活
	 * @param userCode
	 * @return
	 * @throws IOException 
	 */
	@ActionKey("/emailAuth")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void emailAuth() throws IOException{
		if (emailIsAuth()) {
			forward("/portal/mailSuccess.html");
		}else{
			forward("/portal/mailFail.html");
		}
	}

	
	private boolean emailIsAuth(){
		String userCode = getPara("userCode");
		if(StringUtil.isBlank(userCode)){
			return false;
		}
		
		//解析激活参数
		String desResult = null;
		try{
			desResult = DESUtil.decode4string(userCode, CommonUtil.DESKEY);
		}catch(Exception e){
			BIZ_LOG_WARN(userCode, BIZ_TYPE.EMAIL, "激活邮箱解析参数异常");
			return false;
		}
		String[] desResultArr = desResult.split(",");
		if(desResultArr.length != 3){
			return false;
		}
		
		if((Long.parseLong(desResultArr[2])+30*60*1000) < new Date().getTime()){
			//记录日志
			BIZ_LOG_WARN(desResultArr[0], BIZ_TYPE.EMAIL, "邮箱认证失败，激活连接失效");
			return false;
		}
		
		//验证用户
		User user = User.userDao.findById(desResultArr[0]);
		if(null == user){
			//记录日志
			BIZ_LOG_WARN(desResultArr[0], BIZ_TYPE.EMAIL, "邮箱认证失败，非法参数");
			return false;
		}
		
		//验证是否已经验证
		String email = user.getStr("userEmail");
		if(!StringUtil.isBlank(email) && !"00@yrhx.com".equals(email)){
			BIZ_LOG_ERROR(desResultArr[0], BIZ_TYPE.EMAIL, "邮箱已认证", null);
			return true;
		}
		
		user.set("userEmail", desResultArr[1]);
		boolean saveResult = user.update();
		if(saveResult == false){
			BIZ_LOG_ERROR(desResultArr[0], BIZ_TYPE.EMAIL, "邮箱认证失败，非法参数", null);
			return false;
		}
		
		//删除更新缓存用户信息
		Memcached.delete("user_" + userCode);
		
		//记录日志
		BIZ_LOG_INFO(desResultArr[0], BIZ_TYPE.EMAIL, "用户邮箱认证成功");
		return true;
	}
	
	
	/**
	 * 发送【通过邮箱绑定手机】
	 * @param remark
	 * @return
	 */
	@ActionKey("/sendEmail4Bind")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void sendEmail4Bind(){
		Message msg = null;
		
		String email = getPara("email");
		
		//验证
		if(StringUtil.isBlank(email)){
			msg = error("01", "参数错误!", null);
		}else{
			//验证用户是否存在
			User user = userService.findByEmail(email);
			if(null == user){
				//记录日志
				BIZ_LOG_WARN(email, BIZ_TYPE.EMAIL, "绑定失败02");
				msg = error("02", "用户不存在", "");
			}else{

				//拼接激活路径
				HttpServletRequest request = getRequest();
				String basePath = request.getServerName()
						+":"+request.getServerPort()+request.getContextPath()+"/";  
				
				//日志记录参数
				JSONObject logJson = new JSONObject();
				logJson.put("email", email);
				
				String params = "";
				
				try {
					String mobile = "";
					try{
						mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
					}catch(Exception e){
					}
					
					if(CommonUtil.isMobile(mobile)){
						msg = error("03", "已经绑定手机,请使用手机号进行登录.", "");
					}else{
						params = DESUtil.encode4string(email+","+new Date().getTime(), CommonUtil.DESKEY);
						String path = basePath + "mailLogin?key="+params;
						
						Sender sender = new Sender() ;
						Mail mail = new Mail() ;
						mail.setTo(email);
						Map<String , String> ps = new HashMap<String , String>();
						ps.put("bindUrl", path);
						mail.setParams( ps );
						sender.putMail(mail);
						sender.setTemplate_invoke_name("22170_invoke_11");
						
						boolean b = sender.sendMail();
						if(!b){
							//记录日志
							BIZ_LOG_ERROR(email, BIZ_TYPE.EMAIL, "通过邮箱绑定手机失败[04]",null);
							msg = error("04", "发送邮件异常", "");
						}else{
							//记录日志
							BIZ_LOG_INFO(email, BIZ_TYPE.EMAIL, "用户申请邮箱认证成功");
							msg = succ("发送成功", "");
						}
					}
				} catch (Exception e) {
					//记录日志
					BIZ_LOG_ERROR(email, BIZ_TYPE.EMAIL, "通过邮箱绑定手机失败[05]", e);
					msg = error("05", "发送邮件异常", e.getMessage());
				}
			}
		}
		renderJson(msg);
	}

	/**
	 * 绑定【通过邮箱绑定手机】
	 * @param userCode
	 * @return
	 * @throws IOException 
	 */
	@ActionKey("/authEmail4Bind")
	@AuthNum(value=999)
	public void authEmail4Bind(){
		
		Message msg = null;
		
		String key = getPara("key");
		String loginPwd = getPara("loginPwd");
		String phone = getPara("phone");
		String phoneMsg = getPara("phoneMsg");
		
		if(StringUtil.isBlank(key) || StringUtil.isBlank(loginPwd) 
				|| StringUtil.isBlank(phone) || StringUtil.isBlank(phoneMsg)){
			msg = error("01", "参数错误", "");
			renderJson(msg);
			return;
		}
		
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_BINDEMAIL_" + phone, phoneMsg) == false){
			//记录日志
			BIZ_LOG_WARN(phone, BIZ_TYPE.EMAIL, "短信验证码不正确");
			msg = error("02", "短信验证码不正确", "");
			renderJson(msg);
			return;
		}else{
			//解析参数
			String desResult = null;
			try{
				desResult = DESUtil.decode4string(key, CommonUtil.DESKEY);
			}catch(Exception e){
				BIZ_LOG_WARN(key, BIZ_TYPE.EMAIL, "通过邮箱绑定手机解析参数异常");
				msg = error("03", "绑定失败[03]", "");
				renderJson(msg);
				return;
			}
			String[] desResultArr = desResult.split(",");
			if(desResultArr.length != 2){
				msg = error("04", "绑定失败[04]", "");
				renderJson(msg);
				return;
			}
			
			//验证是否超时
			if((Long.parseLong(desResultArr[1])+30*60*1000) < new Date().getTime()){
				//记录日志
				BIZ_LOG_WARN(desResultArr[0], BIZ_TYPE.EMAIL, "绑定失败，绑定连接失效[05]");
				msg = error("05", "绑定连接已经超时,请重新绑定", "");
				renderJson(msg);
				return;
			}
			
			//验证用户
			User user = userService.findByEmail(desResultArr[0]);
			if(null == user){
				//记录日志
				BIZ_LOG_WARN(desResultArr[0], BIZ_TYPE.EMAIL, "绑定失败06");
				msg = error("06", "用户不存在,请重新绑定", "");
				renderJson(msg);
				return;
			}
			
			String authcode = "";
			String passwd = "";
			String uMobile = "";
			try {
				
				//验证是否已经绑定
				if(CommonUtil.isMobile(CommonUtil.decryptUserMobile(user.getStr("userMobile")))){
					msg = error("10", "已经绑定手机,请使用手机号进行登录.", "");
					renderJson(msg);
					return;
				}
				
				//验证密码
				if(user.getStr("loginPasswd").equals(CommonUtil.encryptPasswd(loginPwd)) == false){
					msg = error("07", "密码错误", "");
					renderJson(msg);
					return;
				}
				passwd = CommonUtil.encryptPasswd(loginPwd);
				authcode = CommonUtil.buildLoginAuthCode(phone, loginPwd);
				uMobile = CommonUtil.encryptUserMobile(phone);
				//passwd = MD5Code.md5(passwd);
			} catch (Exception e) {
				msg = error("08", "绑定失败[08]", "");
				renderJson(msg);
				return;
			}
			
			user.set("userMobile", uMobile);
			user.set("loginPasswd", passwd);
			user.set("loginAuthCode", authcode);
			
			boolean saveResult = user.update();
			if(saveResult == false){
				BIZ_LOG_ERROR(desResultArr[0], BIZ_TYPE.EMAIL, "绑定失败09", null);
				msg = error("09", "绑定失败[09]", "");
				renderJson(msg);
				return;
			}
			
			//记录日志
			BIZ_LOG_INFO(desResultArr[0], BIZ_TYPE.EMAIL, "用户通过邮箱绑定手机号成功");
	
			msg = succ("绑定成功", "");
			renderJson(msg);
		}
	}

	
	
	/**
	 * 申请提现页面初始化
	 * @return	
	 */
	@ActionKey("/withdrawalsInit")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message withdrawalsInit() {
		String userCode = getUserCode();
		Map<String,Object> map = new HashMap<String, Object>();
		
		//账户信息
		Funds funds = fundsServiceV2.findById(userCode);
		map.put("funds", funds);
		
		Long totalWithdraw = funds.getLong("totalWithdraw");//提现总额
		Long beRecyPrincipal = funds.getLong("beRecyPrincipal");//待收回本金
		Long reciedPrincipal = funds.getLong("reciedPrincipal");//已回收本金
		Long reciedInterest = funds.getLong("reciedInterest");//已回收利息
		
		//投资总额 (待收回本金 + 已回收本金 + 已回收利息)
		Long loanTotalAmount = beRecyPrincipal+reciedPrincipal+reciedInterest;
		//无需收手续费部分(投资总额 - (提现总额 - 提现金额)) 备注：提现之前已经把提现金额加入提现总额
		Long noFee = loanTotalAmount - totalWithdraw;
		map.put("noFee", noFee);
		
		//是否设置支付密码
		String payPasswd = userService.findByField(userCode, "payPasswd").getStr("payPasswd");
		int isSetPayPwd = 1;
		if(StringUtil.isBlank(payPasswd)){
			isSetPayPwd = 0;
		}
		map.put("isSetPayPwd", isSetPayPwd);
		
		//银行卡列表
		List<BanksV2> listBank = banksService.findBanks4User(userCode);
		map.put("banks", listBank);
		
		return succ("查询成功", map);
	}
	
	/**
	 * 查询公告新闻信息列表
	 */
	@ActionKey("/queryNewsByPage")
	@ResponseCached(cachedKey="queryNewsByPage", cachedKeyParm="",mode="remote" , time=5*60)
	@AuthNum(value=999)
	public void queryNewsByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String type = getPara("type");
		String isContent = getPara("isContent");
		
		Page<Notice> queryNotice = noticeService.queryNotice(pageNumber , pageSize , type , isContent);
		Message msg = succ("查询成功", queryNotice);
		renderJson(msg);
	}
	
	
	/**
	 * 查询公告新闻详细信息
	 */
	@ActionKey("/queryNewsDetail")
	@ResponseCached(cachedKey="queryNewsDetail", cachedKeyParm="",mode="remote" , time=5*60)
	@AuthNum(value=999)
	public void queryNewsDetail(){
		Message msg = null;
		String id = getPara("id");
		Notice notice = new Notice();
		if(StringUtil.isBlank(id)){
			msg = error("01", "参数错误", "");
		}else{
			notice = noticeService.queryNewsDetail(id);
			msg = succ("查询成功!", notice);
		}
		renderJson(msg);
	}
	
	/**
	 * 查询轮播图
	 */
	@ActionKey("/querySliderList")
	@AuthNum(value=999)
	public void querySliderList(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		Page<SliderPic> sliderList = sliderService.querySliderList(pageNumber, pageSize, "1");
		Message msg = succ("查询成功!", sliderList );
		renderJson(msg);
	}
	
	
	/**
	 * 查询推荐奖励列表
	 */
	@ActionKey("/queryShareByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryShareByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		String userCode = getUserCode();
		//好友投资返现/ 好友投资返佣， 记录查询
		Page<RecommendReward> pages = rmdRewardService.queryListByUser(pageNumber, pageSize, userCode);
		//返现+返佣总计
		long totalAmount = rmdRewardService.queryTZJL(userCode,1);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("shares", pages);
		map.put("totalAmount", totalAmount);
		return succ("查询成功", map);
	}
	
	/**
	 * 推荐好友收益统计----IOS
	 */
	@ActionKey("/queryShareCount4iOS")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryShareCount4iOS", cachedKeyParm="@userCode",mode="remote" , time=2*60)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryShareCount4iOS(){
		String userCode = getUserCode();
		Map<String,Object> result = new HashMap<String,Object>();
		long ysyj = rmdRewardService.queryTZJL(userCode, 3);
		long dsyj = rmdRewardService.querySYFY(userCode);
		long yqrs = risService.queryMyTuiJian(userCode);
		result.put("ysyj", ysyj);
		result.put("dsyj", dsyj);
		result.put("yqrs", yqrs);
		return succ("查询成功", result);
	}
	
	/**
	 * 查询我的推荐人的明细
	 */
	@ActionKey("/queryMyTuiJianDetail")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryMyTuiJianDetail", cachedKeyParm="pageNumber|pageSize|@userCode",mode="remote" , time=2*60)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFanYongHistory(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",10);
		String userCode = getUserCode();
		Map<String,Object> result = risService.queryMyTuiJianDetail(pageNumber, pageSize, userCode);
		return succ("查询成功", result);
	}
	
	/**
	 * 推荐好友收益统计--PC端
	 */
	@ActionKey("/queryShareCount")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryShareCount", cachedKeyParm="pageNumber|pageSize|@userCode",mode="remote" , time=2*60)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryShareCount(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",10);
		String userCode = getUserCode();
		Map<String,Object> result = new HashMap<String,Object>();
		//返现+返佣总计
		long amount_all = rmdRewardService.queryTZJL(userCode,1);
		long amount_fx = rmdRewardService.queryTZJL(userCode,2);
		long amount_fy = rmdRewardService.queryTZJL(userCode,3);
		long tjrs = risService.queryMyTuiJian(userCode);
		result.put("amount_all", amount_all);
		result.put("amount_fx", amount_fx);
		result.put("amount_fy", amount_fy);
		result.put("tjrs", tjrs);
		result.put("userCode", userCode);
		
		Page<RecommendInfo> pages = risService.queryShareList(pageNumber, pageSize, userCode);
		result.put("pages", pages);
		String userName = " ";
		try {
			userName = Db.queryStr("select userName from t_user where userCode = ?",userCode);
		} catch (Exception e) {
			userName = "";
		}
		result.put("userName", userName);
		return succ("查询成功", result);
	}
	
	/**
	 * 查询积分兑换商品列表
	 */
	@ActionKey("/queryMarketByPage")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryMarketByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Page<Market> market = marketService.queryMarket(pageNumber, pageSize, "1");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("firstPage", market.isFirstPage());
		map.put("lastPage", market.isLastPage());
		map.put("pageNumber", market.getPageNumber());
		map.put("pageSize", market.getPageSize());
		map.put("totalPage", market.getTotalPage());
		map.put("totalRow", market.getTotalRow());
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询投资券商品列表
	 */
	@ActionKey("/queryMarketLCQByPage")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryMarketLCQByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Page<Market> market = marketService.queryMarketLCQByPage(pageNumber, pageSize, "1");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("firstPage", market.isFirstPage());
		map.put("lastPage", market.isLastPage());
		map.put("pageNumber", market.getPageNumber());
		map.put("pageSize", market.getPageSize());
		map.put("totalPage", market.getTotalPage());
		map.put("totalRow", market.getTotalRow());
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询实物商品列表
	 */
	@ActionKey("/queryMarketSWSPByPage")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryMarketSWSPByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Page<Market> market = marketService.queryMarketSWSP(pageNumber, pageSize, "1");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("firstPage", market.isFirstPage());
		map.put("lastPage", market.isLastPage());
		map.put("pageNumber", market.getPageNumber());
		map.put("pageSize", market.getPageSize());
		map.put("totalPage", market.getTotalPage());
		map.put("totalRow", market.getTotalRow());
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询电子卡商品列表
	 */
	@ActionKey("/queryMarketDZKByPage")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryMarketDZKByPage(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Page<Market> market = marketService.queryMarketDZK(pageNumber, pageSize, "1");
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("firstPage", market.isFirstPage());
		map.put("lastPage", market.isLastPage());
		map.put("pageNumber", market.getPageNumber());
		map.put("pageSize", market.getPageSize());
		map.put("totalPage", market.getTotalPage());
		map.put("totalRow", market.getTotalRow());
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询商品详情
	 */
	@ActionKey("/queryMarketDetail")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryMarketDetail(){
		String mCode = getPara("mCode");
		Market market = marketService.queryMarketDetail(mCode);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("market", market);
		return succ("查询成功", map);
	}
	
	/**
	 * 积分兑换商品
	 */
	@ActionKey("/exchange")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message exchange(){
		String mCode = getPara("mCode");
		String mobile = "";
		String address = "";
		String remark = getPara("remark","无");
		int num = Integer.parseInt(getPara("num"));
		//获取用户信息
		String userCode = getUserCode();
		User user = User.userDao.findById(userCode);
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		if(num<=0){
			return error("01", "参数错误!", "");
		}
		if(StringUtil.isBlank(mCode)){
			return error("01", "参数错误!", "");
		}
		
		Market market = marketService.queryMarketDetail(mCode);
		//验证是否存在此商品
		if(null == market){
			return error("02", "商品不存在!", "");
		}
		if(("A").equals(market.getStr("mType"))){
			//实物商品
			mobile = getPara("mobile");
			address = getPara("address");
			if(StringUtil.isBlank(mobile)){
				return error("13", "收件人手机号不能为空","");
			}
			if(StringUtil.isBlank(address)){
				return error("14", "收件人地址不能为空","");
			}
		}
		else{
			//现金券或者加息券或京东卡
			try {
				mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			} catch (Exception e) {
				return error("13", "手机号解析异常", "");
			}
			address = userInfo.getStr("userAdress");
		}
		
		if(!JXController.isJxAccount(user)){
			return error("02", "请开通存管后继续兑换!", "");
		}
		//验证用户等级
		if(user.getInt("vipLevel") < market.getInt("level")){
			return error("03", "该商品只有 [" + market.getStr("") + "]及以上等级可以兑换!快去投标升级吧!" , "");
		}
		//验证商品发布   截至日期
		String startDateTime = market.getStr("startDateTime");
		String endDateTime = market.getStr("endDateTime");
		String nowDateTime = DateUtil.getNowDateTime();
		int start = "00000000000000".equals(startDateTime) ? 0 : DateUtil.compareDateByStr("yyyyMMddHHmmss",nowDateTime, startDateTime);
		int end = "99999999999999".equals(endDateTime) ? 0 : DateUtil.compareDateByStr("yyyyMMddHHmmss", endDateTime, nowDateTime);
		if(start != 0 && start != 1){
			return error("04", "商品兑换时间还没到呢!", "");
		}
		
		if(end != 0 && end != 1){
			return error("05", "商品兑换时间已经截至了!", "");
		}
		
		//验证是否已经兑换过
//		MarketUser ismarket = marketUserService.queryDetail(userCode,mCode);
//		if(null != ismarket){
//			return error("07", "你已经兑换过此商品啦,试试其它商品吧!", "");
//		}
		
		//递减商品剩余数量
		boolean hasMarket = marketService.updateRemainCountNum(mCode, num);
		if(hasMarket == false){
			return error("09", "商品已经被抢光啦,试试其它商品吧!", "");
		}
		
		//扣除可用积分 添加foundstrace记录
		Long mPoint = market.getLong("point");
		mPoint=mPoint*num;
		
		try{
			fundsServiceV2.doPoints(userCode, 1, mPoint, "商品兑换支出");
		}catch(BaseBizRunTimeException e){
			return error("10", "积分不足，投资可以赚取积分哦！", "");
		}
		
		//封装对象  添加兑换记录
		
		for(int i=0;i<num;i++){
		MarketUser marketUser = new MarketUser();
		marketUser.set("userCode", userCode);
		marketUser.set("userName", user.getStr("userName"));
		marketUser.set("userCardName", userInfo.getStr("userCardName"));
		marketUser.set("userMobile",mobile);
		marketUser.set("userAddress",address);
		marketUser.set("remark", remark);
		marketUser.set("mCode", mCode);
		marketUser.set("mName", market.getStr("mName"));
		marketUser.set("point", market.getLong("point"));
		boolean save = marketUserService.save(marketUser);
		if(save == false){
			return error("08", "商品已经被抢光啦,试试其它商品吧!", "");
		}
		//20170508添加 判断商品类型 为现金券直接添加
		if(market.getStr("mType").length()>1){
			//修改商品兑换订单状态
			
			String muCode=marketUser.getStr("muCode");
			boolean aa=marketUserService.updateIssue(muCode, "1");
			if(aa==false){
				return error("11", "现金券兑换失败", "");
			}else{
				//添加现金券
				String mType=market.getStr("mType");
				String settingsType=mType.substring(1,mType.length());
				String opUserCode = null;
				String userMobile = marketUser.getStr("userMobile");
				String userName = marketUser.getStr("userName");
				String userTrueName = marketUser.getStr("userCardName");
				String tname = market.getStr("mName");
				Calendar now = Calendar.getInstance();
			    now.add(Calendar.DAY_OF_MONTH, 30);
			    String endDate = new SimpleDateFormat("yyyyMMdd").format(now.getTime());
				String expDate = endDate;
				//20170726 ws  save加两值
				boolean xyz = ticketService.save(userCode, userName, userMobile, userTrueName, tname, expDate, settingsType, opUserCode, SysEnum.makeSource.A,"0","N");
				if(xyz == false){
								return error("12", "现金券兑换失败", "");
					             }
				}
					 }
			}
			return succ("兑换成功!工作人员会尽快给您处理,请耐心等待,谢谢!", "");
			
	}
	
	
	/**
	 * 查询用户已兑换商品列表
	 */
	@ActionKey("/queryExchange")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryExchange(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String userCode = getUserCode();
		Map<String,Object> result =  marketUserService.queryMarketUser(pageNumber, pageSize, null, userCode,null);
		return succ("查询成功!", result);
	}
	
	/**
	 * 查询可以积分排名前五位
	 */
	@ActionKey("/queryPointTop5")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public Message queryPointTop5(){
		Page<Funds> fund =  fundsServiceV2.queryPointTop5();
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("fund", fund);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询可以积分六到十位
	 */
	@ActionKey("/queryPoint6_10")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public Message queryPoint6_10(){
		Page<Funds> fund =  fundsServiceV2.queryPoint6_10();
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("fund", fund);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询用户即时兑换
	 */
	@ActionKey("/queryExchangeNow")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public Message queryExchangeNow(){
		Map<String,Object> result =  marketUserService.queryExchangeNow();
		return succ("查询成功!", result);
	}
	
	/**
	 * 下载电子合同
	 */
	@ActionKey("/downContractPDF")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void downContractPDF(){
		String type = getPara("type");
		String traceCode = getPara("traceCode");
		String transCode = getPara("transCode");
		LoanTransfer loanTransfer = null;
		if(StringUtil.isBlank(traceCode)){
			if(StringUtil.isBlank(transCode)){
				renderJson(error("01", "参数错误", ""));
				return;
			}else{
				loanTransfer = loanTransferService.findById(transCode);
				traceCode = loanTransfer.getStr("traceCode");
			}
		}
		
		//验证投标流水
		LoanTrace loanTrance = loanTraceService.findById(traceCode);
		if(null == loanTrance){
			renderJson(error("02", "投标流水未找到", ""));
			return;
		}
		
		String userCode = getUserCode();
		String loanCode = loanTrance.getStr("loanCode");
		//查询标书信息
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		//获取用户信息
		UserInfo userInfo = userInfoService.findById(userCode);
		//获取出借用户信息
		User userAllInfo = userService.findUserAllInfoById(userCode);
		// 获取借款用户类型
		User loanUser = userService.findById(loanInfo.getStr("userCode"));
		//获取借款人的账户银行卡信息
		BanksV2 loanBanks = banksService.findByUserCode(loanInfo.getStr("userCode"));
		//获取出借人的账户银行卡信息
		BanksV2 userBanks = banksService.findByUserCode(userCode);
		//标书类型
//		String loanType = loanInfo.getStr("loanType");
		Map<String , Object> map = new HashMap<String, Object>();
		map.put("loanInfo", loanInfo);
		map.put("loanTransfer", loanTransfer);
		map.put("userInfo", userInfo);
		map.put("userAllInfo",userAllInfo);
		map.put("loanUser", loanUser);
		map.put("loanBanks",loanBanks);
		map.put("userBanks", userBanks);
		map.put("loanTrace",loanTrance);
		
		if("E".equals(loanInfo.getStr("productType"))){
			YiStageUserInfo yistageUse = yiStageUserInfoService.queryByUserCode(loanInfo.getStr("userCode"));
			if(yistageUse != null){
				map.put("qq",yistageUse.getStr("qq"));
				map.put("wechat",yistageUse.getStr("wechat"));
				map.put("email",yistageUse.getStr("email"));
			}else{
				map.put("qq","");
				map.put("wechat","");
				map.put("email","");
			}
		}
		
		String loanUserType = loanUser.getStr("userType");
		map.put("loanUserType", loanUser.get("userType"));
		// 如果借款人主体性质为企业 则获取统一社会信用代码
		String loanUserName = "";
		String loanUserCardId = "";
		if (SysEnum.UserType.C.val().equals(loanUserType)) {
			LoanApply loanApply = loanApplyService.findById(Integer.parseInt(loanInfo.getStr("loanNo")));
			loanUserName = loanApply.getStr("loanTrueName");
			loanUserCardId = loanApply.getStr("loanCardId");
		} else {
			try {
				loanUserCardId = CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		loanUserCardId = "***"+loanUserCardId.substring(loanUserCardId.length()-4, loanUserCardId.length());
		map.put("loanUserName", loanUserName);
		map.put("loanUserCardId", loanUserCardId);
		
		HttpServletResponse response = getResponse();
		response.reset();
		try {
			if("1".equals(type)){
				response.addHeader("Content-Disposition", 
						"attachment;filename=" + new String(( "易融恒信 _" + loanInfo.getStr("loanNo") + "_" + loanInfo.getStr("loanCode") +".pdf").getBytes("utf-8"),"iso8859-1"));
				response.setContentType("application/pdf");
			}
			PDFReport pdfReport = null;
			
//			if("D".equals(loanType)){
//				pdfReport = new TransferPDFReport(response.getOutputStream());
//				LoanTrace loanTrace = loanTraceService.findById(traceCode);
//				map.put("loanTrace", loanTrace);
//			}else{
				List<LoanTrace> listLoanTrace = loanTraceService.findAllByLoanCode(loanCode);
				map.put("listLoanTrace", listLoanTrace);
				pdfReport =	new ContractPDFReportV2(response.getOutputStream());
//			}
			
			pdfReport.setContractMap(map);
			if("E".equals(loanInfo.getStr("productType"))){
				pdfReport.generatePDFForYfq();
			}else{
				if(StringUtil.isBlank(transCode)){
					pdfReport.generatePDF();
				}else{
					if(loanTransfer.getStr("refundType").equals("E")){
						pdfReport.generatePDFMonth();
					}else if(loanTransfer.getStr("refundType").equals("F")){
						pdfReport.generatePDFQtr();
					}else if(loanTransfer.getStr("refundType").equals("H")){
						pdfReport.generatePDFHalf();
					}else{
						renderJson(error("04", "非常抱歉，电子合同异常，请联系客服处理！", ""));
						return;
					}
				}
			}
			
		} catch (Exception e) {
			renderJson(error("03", "非常抱歉，电子合同异常，请联系客服处理！", ""));
			return;
		}
		renderNull();
	}
	
	/**
	 * 按投标金额排行查询理财人
	 * @return
	 */
	@ActionKey("/queryFundsOrderByBenJin")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryFundsOrderByBenJin", cachedKeyParm="pageNumber|pageSize",mode="remote" , time=5*60)
	@Before({PkMsgInterceptor.class})
	public Message queryFundsOrderByBenJin(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		Map<String,Object> result = fundsServiceV2.queryFundsOrderByMoney(pageNumber, pageSize);
		return succ("查询完成", result);
	}
	
	/**
	 * 平台统计数据查询(今天成交金额、本月成交金额、风险备用金)
	 * @return
	 */
	@ActionKey("/queryCountData1")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryCountData1", cachedKeyParm="",mode="remote" , time=5*60)
	@Before({PkMsgInterceptor.class})
	public Message queryCountData1(){
		String todayDate = DateUtil.getNowDate();
		String monthDate[] = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, "yyyyMMdd");
		
		long today_count = loanInfoService.countLoanAmountByEffectDate(todayDate, todayDate);
		
		long month_count = loanInfoService.countLoanAmountByEffectDate(monthDate[0], monthDate[1]);
		
		long all_loanAmount = loanInfoService.sumLoanAmount();

		long riskTotal_count = fundsServiceV2.queryRiskTotal();
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("today_count", today_count);
		result.put("month_count", month_count);
		result.put("riskTotal_count", riskTotal_count);
		result.put("all_loanAmount", all_loanAmount);
		return succ("查询完成", result);
		
	}

	/**
	 * 查询债权人信息
	 */
	@ActionKey("/queryTransferUser")
	@AuthNum(value=999)
	public void queryTransferUser(){
		renderJson(succ("查询成功", VehiclePledgeReport.listTransferUser));
	}
	
	
	private SMSLog setSMSLog(String mobile,String content,String type,String typeName,int status){
		SMSLog smsLog = new SMSLog();
		smsLog.set("mobile", mobile);
		smsLog.set("content", content);
		smsLog.set("type", type);
		smsLog.set("typeName", typeName);
		smsLog.set("status", status);
		return smsLog;
	}
	
	/**
	 * 按投标金额排行查询理财人
	 * @return
	 */
	@ActionKey("/appUpdate")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message appLive(){
		Map<String,String> map = new HashMap<String, String>();
		map.put("version", "1.0.0");
		map.put("url", "");
		map.put("type", "all");
		return succ("查询完成", map);
	}
	
	/**
	 * 个人报表
	 * @author shiqingsong
	 * @return
	 */
	@ActionKey("/report4User")
	@AuthNum(value=999)
//	@ResponseCached(cachedKey="report4User", cachedKeyParm="",mode="remote" , time=60)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message report4User(){
		String userCode = getUserCode();
		TempReport tempReport = TempReport.tempReportDao.findById(userCode);
//		long countLoanByAmount = TempReport.tempReportDao.countLoanByAmount(tempReport.getLong("loan4Month"));
//		
//		long countLoanByIncome = TempReport.tempReportDao.countLoanByIncome(tempReport.getLong("income4Month"));
//		
//		long countLoanByRegist = TempReport.tempReportDao.countLoanByRegist(tempReport.getLong("regist4Now"));
		
		Map<String , Object> map = new HashMap<String, Object>();
		map.put("tempReport", tempReport);
		map.put("percent4Amount", percent4Amount(userCode ,tempReport.getLong("loan4Month")));
		map.put("percent4Income", percent4Income(userCode ,tempReport.getLong("income4Month")));
		map.put("percent4Regist", percent4Regist(userCode ,tempReport.getLong("regist4Now")));

		return succ("查询成功", map);
	}
	
	
	/**
	 * 个人报表
	 * @author shiqingsong
	 * @return
	 */
	@ActionKey("/report4UserCode")
	@AuthNum(value=999)
//	@ResponseCached(cachedKey="report4UserCode", cachedKeyParm="",mode="remote" , time=60)
	@Before({PkMsgInterceptor.class})
	public Message report4UserCode(){
		String userCode = getPara("userCode");
		if(StringUtil.isBlank(userCode)){
			return error("01", "参数为空", "");
		}
		TempReport tempReport = TempReport.tempReportDao.findById(userCode);
		
//		long countLoanByAmount = TempReport.tempReportDao.countLoanByAmount(tempReport.getLong("loan4Month"));
//		
//		long countLoanByIncome = TempReport.tempReportDao.countLoanByIncome(tempReport.getLong("income4Month"));
//		
//		long countLoanByRegist = TempReport.tempReportDao.countLoanByRegist(tempReport.getLong("regist4Now"));
		
		Map<String , Object> map = new HashMap<String, Object>();
		map.put("tempReport", tempReport);
		map.put("percent4Amount", percent4Amount(userCode ,tempReport.getLong("loan4Month")));
		map.put("percent4Income", percent4Income(userCode ,tempReport.getLong("income4Month")));
		map.put("percent4Regist", percent4Regist(userCode ,tempReport.getLong("regist4Now")));
		
		return succ("查询成功", map);
	}
	
	
	private String percent4Amount(String userCode ,Long loan4Month){
		String result = "0";
		Object object = Memcached.get("percent4Amount_10_" + userCode);
		if(null != object){
			result = object.toString();
		}else{
			if(loan4Month >= 25000000){
				result = "99";
			}else if(loan4Month >= 20000000){
				result = 9+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 15000000){
				result = 8+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 10000000){
				result = 7+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 8000000){
				result = 6+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 6000000){
				result = 5+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 5000000){
				result = 4+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 4000000){
				result = 3+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 3000000){
				result = 2+CommonUtil.getMathNumber(1);
			}else if(loan4Month >= 1000000){
				result = 1+CommonUtil.getMathNumber(1);
			}else if(loan4Month > 0){
				result = CommonUtil.getMathNumber(1);
			}
		}
		
		Memcached.set("percent4Amount_10_" + userCode, result);
		return result;
	}
	
	private String percent4Income(String userCode , Long income4Month){
		String result = "0";
		Object object = Memcached.get("percent4Income_10_" + userCode);
		if(null != object){
			result = object.toString();
		}else{
			if(income4Month >= 3000000){
				result = "99";
			}else if(income4Month >= 1000000){
				result = 9+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 900000){
				result = 8+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 8000000){
				result = 7+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 700000){
				result = 6+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 600000){
				result = 5+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 500000){
				result = 4+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 400000){
				result = 3+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 300000){
				result = 2+CommonUtil.getMathNumber(1);
			}else if(income4Month >= 100000){
				result = 1+CommonUtil.getMathNumber(1);
			}else if(income4Month > 0){
				result = CommonUtil.getMathNumber(1);
			}
		}
		
		Memcached.set("percent4Income_10_" + userCode, result);
		return result;
	}
	
	private String percent4Regist(String userCode ,Long regist4Now){
		String result = "0";
		Object object = Memcached.get("percent4Regist_10_" + userCode);
		if(null != object){
			result =  object.toString();
		}else{
			if(regist4Now >= 1000){
				result =  "99";
			}else if(regist4Now >= 900){
				result =  9+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 800){
				result =  8+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 700){
				result =  7+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 600){
				result =  6+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 500){
				result =  5+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 400){
				result =  4+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 300){
				result =  3+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 200){
				result =  2+CommonUtil.getMathNumber(1);
			}else if(regist4Now >= 100){
				result =  1+CommonUtil.getMathNumber(1);
			}else if(regist4Now > 0){
				result =  CommonUtil.getMathNumber(1);
			}
		}
		Memcached.set("percent4Regist_10_" + userCode, result);
		return result;
	}
	
	
	
	/////////////////////////////       手机端接口            ///////////////////////////////////////////////
	
	/**
	 * 我的账户
	 * @return
	 */
	@ActionKey("/mobile_queryUserInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message mobile_queryUserInfo(){
		//获取用户标识
		String userCode = getUserCode();
		
		Map<String,Object> resultMap = new HashMap<String, Object>();
		
		User user;
		UserInfo userInfo;
		AutoLoan_v2 autoLoan;
		Funds fund;
//		Object[] hsz = null;
//		Object[] zbz = null;
//		Object[] yhs = null;
		try{
			user = userService.findById(userCode);
			userInfo = userInfoService.findById(userCode);
			if(null == user || userInfo == null){
				return error("01", "查询失败", null);
			}
			autoLoan = autoLoanService.findByUserCode(userCode, "A");
			fund = fundsServiceV2.findById(userCode);
//			hsz = loanTraceService.countByLoanState(userCode, "N");//回收中的标书数量
//			zbz = loanTraceService.countByLoanState(userCode, "A");//招标中的标书数量
//			yhs = loanTraceService.countByLoanState(userCode, "B");//已回收的标书数量
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.MAINPAGE, "查询失败", e);
			return error("02", "查询失败", e.getMessage());
		}
		
		//回收中     招标中   已回收
//		if(null != hsz && hsz.length == 2){		//回收中  
//			resultMap.put("count_hsz", hsz[0]);
//			resultMap.put("sum_hsz",  hsz[1]);
//		}else{
//			resultMap.put("count_hsz", 0);
//			resultMap.put("sum_hsz", 0);
//		}
//		if(null != zbz && zbz.length == 2){		//招标中
//			resultMap.put("count_zbz", zbz[0]);
//			resultMap.put("sum_zbz",  zbz[1]);
//		}else{
//			resultMap.put("count_zbz", 0);
//			resultMap.put("sum_zbz", 0);
//		}
//		if(null != yhs && yhs.length == 2){		//已回收
//			resultMap.put("count_yhs", yhs[0]);
//			resultMap.put("sum_yhs",  yhs[1]);
//		}else{
//			resultMap.put("count_yhs", 0);
//			resultMap.put("sum_yhs", 0);
//		}
		
		
		//用户昵称, 用户积分,用户等级,等级描述
		resultMap.put("userName", user.getStr("userName"));
		resultMap.put("userScore", user.getLong("userScore"));
		resultMap.put("vipLevel", user.getInt("vipLevel"));
		resultMap.put("vipLevelName", user.getStr("vipLevelName"));
		
		//账户安全等级 
		String userEmail = user.getStr("userEmail");
		String payPasswd = user.getStr("payPasswd");
		String isAuthed = userInfo.getStr("isAuthed");
		
		if(StringUtil.isBlank(payPasswd)){//支付密码
			resultMap.put("payPwd", 0);
		}else{
			resultMap.put("payPwd", 1);
		}
		if(!StringUtil.isBlank(userEmail) && !"00@yrhx.com".equals(userEmail)){//邮箱
			resultMap.put("email", 1);
		}else{
			resultMap.put("email", 0);
		}
		if("2".equals(isAuthed)){//身份证认证
			resultMap.put("cardId", 1);
		}else{
			resultMap.put("cardId", 0);
		}
		
		//自动投标状态 
		resultMap.put("isAvailable", null == autoLoan ? 0 : 1);
		
		//资金信息
		resultMap.put("fund", fund);
		
//		//可用余额 
//		long avBalance = fund.getLong("avBalance");//可用余额
//		resultMap.put("avBalance", avBalance);
		
		return succ("查询成功", resultMap);
	}
	
//	/**
//	 * 分享奖励
//	 * @return
//	 */
//	@ActionKey("/shareAward")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message shareAward(){
//		
//		String userCode = getUserCode();
//		String type = getPara("type","1");
//		String channels = getPara("channels" , "1");
//		String content = getPara("content" , "");
//		
//		//保存分享记录
//		ShareAward sa = new ShareAward();
//		sa.set("userCode", userCode);
//		sa.set("type", type);
//		sa.set("content", content);
//		sa.set("channels", channels);
//		sa.set("shareDate", DateUtil.getNowDate());
//		sa.set("shareDateTime", DateUtil.getNowDateTime());
//		sa.save();
//		
//		Object object = Memcached.get("shareAward_" + userCode);
//		if(null == object){
//			//分享成功，记录资金流水
//			int amount = randomAmount();
//			Funds funds = fundsServiceV2.doAvBalance(userCode, 0, amount);
//			fundsTraceService.bidTrace4Share(userCode , amount, funds.getLong("avBalance"), 
//					funds.getLong("frozeBalance")) ;
//			Memcached.set("shareAward_" + userCode, userCode, DateUtil.getTodayToTomTime());
//		}
//		
//		return succ("操作成功", "");
//	}
//	
//	/**
//	 * 投标分享加息
//	 * @return
//	 */
//	@ActionKey("/share4Loan")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message share4Loan(){
//		
//		String userCode = getUserCode();
//		String channels = getPara("channels" , "1");
//		String content = getPara("content" , "");
//		String traceCode = getPara("traceCode");
//		
//		if(StringUtil.isBlank(traceCode)){
//			return error("01", "参数错误", "");
//		}
//		
//		LoanTrace loanTrace = loanTraceService.findById(traceCode);
//		String loanCode = loanTrace.getStr("loanCode");
//		//保存分享记录
//		ShareAward sa = new ShareAward();
//		sa.set("userCode", userCode);
//		sa.set("type", "2");
//		sa.set("content", content+"_"+traceCode);
//		sa.set("channels", channels);
//		sa.set("shareDate", DateUtil.getNowDate());
//		sa.set("shareDateTime", DateUtil.getNowDateTime());
//		sa.save();
//		
//		Object object = Memcached.get("share4Loan_" + loanCode + userCode);
//		if(null == object){
//			
//			Memcached.set("share4Loan_" + loanCode + userCode, userCode, DateUtil.getTodayToTomTime());
//			
//			//分享成功，进行加息操作
//			loanTraceService.updateRewardRateByYear(loanTrace.getStr("traceCode"), 400, 200);
//			
//		}
//		
//		return succ("操作成功", "");
//	}
//	
//	/**
//	 * 查询投标分享记录
//	 * @return
//	 */
//	@ActionKey("/queryLoan4Share")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message queryLoan4Share(){
//		String userCode = getUserCode();
//		Paginate paginate = getPaginate();
//		Page<LoanTrace> queryLoanTraceByPage = loanTraceService.queryLoanTraceByPage(paginate.getPageNum()	, paginate.getPageSize(), userCode);
//		return succ("操作成功", queryLoanTraceByPage);
//	}
//	
//	
//	
//	
//	public int randomAmount() {
//		int max=200;
//        int min=100;
//        Random random = new Random();
//        return random.nextInt(max)%(max-min+1) + min;
//	}
	
	
	/////////////////////////////////   新版接口    start   ////////////////////////////////////////////////////	
	
	
	
	/**
	 * 我的账户
	 * @return
	 */
	@ActionKey("/main4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message main4User(){
		//获取用户标识
		String userCode = getUserCode();
		Map<String,Object> resultMap = new HashMap<String, Object>();
		User user;
		Funds fund;
		Integer countBanks = 0;
		Integer countTickets = 0;
		Integer countTrace = 0;
		try{
			user = userService.findById(userCode);
			if(null == user){
				return error("01", "查询失败", null);
			}
			fund = fundsServiceV2.findById(userCode);
			countBanks = banksService.countBanks4User(userCode);
			countTickets = ticketService.countTickets(userCode);
			countTrace = loanTraceService.countTrace4User(userCode);
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.MAINPAGE, "查询失败", e);
			return error("02", "查询失败", e.getMessage());
		}
		
		resultMap.put("fund", fund);
		resultMap.put("user", user);
		resultMap.put("countBanks", countBanks);
		resultMap.put("countTickets", countTickets);
		resultMap.put("countTrace", countTrace);
		return succ("查询成功", resultMap);
	}
	
	
	
	
	
	/**
	 * 查询用户短密码
	 * @return
	 */
	@ActionKey("/queryShortPassword")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryShortPassword(){
		String mobileType = getPara("mobileType");
		if(StringUtil.isBlank(mobileType)){
			return error("01", "参数错误", "");
		}
		String userCode = getUserCode();
		
		ShortPassword sp = passwordService.queryShortPassword(userCode, mobileType);
		Map<String,String> map = new HashMap<String, String>();
		if(null == sp){
			map.put("state", "0");
		}else{
			map.put("state", "1");
		}
		return succ("查询成功", map);
	}
	
	/**
	 * 查询用户短密码
	 * @return
	 */
	@ActionKey("/queryPassword4WX")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryPassword4WX(){
		String openid = getPara("openid");
		if(StringUtil.isBlank(openid)){
			return error("01", "参数错误", "");
		}
		ShortPassword sp = passwordService.queryShortPassword(openid);
		Map<String,String> map = new HashMap<String, String>();
		if(null == sp){
			map.put("state", "0");
		}else{
			map.put("state", "1");
		}
		return succ("查询成功", map);
	}
	
	
	/**
	 * 删除(取消)用户短密码
	 * @return
	 */
	@ActionKey("/deleteShortPassword")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message deleteShortPassword(){
		String mobileType = getPara("mobileType");
		if(StringUtil.isBlank(mobileType)){
			return error("01", "参数错误", "");
		}
		String userCode = getUserCode();
		boolean b = passwordService.deleteShortPassword(userCode, mobileType);
		if(b == false){
			return error("02", "操作失败", "");
		}
		return succ("操作成功", "");
	}
	
	/**
	 * 初始化用户短密码
	 * @return
	 */
	@ActionKey("/inintShortPassword")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message inintShortPassword(){
		String mobileType = getPara("mobileType");
		String password = getPara("password");
		String openId = getCookie("openid");
		if(StringUtil.isBlank(mobileType)){
			return error("01", "参数mobileType错误", "");
		}
		if(StringUtil.isBlank(password)){
			return error("01", "参数password错误", "");
		}
		if("WX".equals(mobileType) && StringUtil.isBlank(openId)){
			return error("05", "非法登录", "");
		}
		
		String userCode = getUserCode();
		boolean b = false;
		try{
			b = passwordService.inintShortPassword(userCode, password,mobileType,openId);
		}catch(Exception e){
			return error("04", "数据异常或重复提交", "");
		}
		
		if(b == false){
			return error("02", "操作失败", "");
		}
		
		String mid = "";
		Map<String,String> map = new HashMap<String, String>();
		try {
			if("WX".equals(mobileType)){
				mid = MD5Code.md5(openId+"WX");
			}else{
				mid = MD5Code.md5(userCode+mobileType);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return error("03", "操作失败", "");
		}
		map.put("mid", mid);
		return succ("操作成功", map);
	}
	
	@ActionKey("/login4ShortPassword")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message login4ShortPassword(){
		return error("01", "存管升级，旧APP停用，请耐心等待新APP发布", "");
	}
	
	/**
	 * 短密码登录验证
	 * @return
	 */
//	@ActionKey("/login4ShortPassword")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public Message login4ShortPassword(){
//		String mid = getPara("mid");
//		String password = getPara("password");
//		String openId = getCookie("openid");
//		if(StringUtil.isBlank(mid) && StringUtil.isBlank(openId)){
//			return error("01", "参数错误", "");
//		}
//		if(StringUtil.isBlank(password)){
//			return error("01", "密码为空", "");
//		}
//		
//		//如果是微信登录则mid 重新使用openid加密赋值
//		ShortPassword sp = null;
//		if(StringUtil.isBlank(openId)){
//			//检查用户是否存在
//			sp = ShortPassword.passwordDao.findById(mid);
//			if(null == sp){
//				Memcached.incr("SHORT_PWDERROR_" + mid, 1);
//				return error("04", "用户不存在!", "");
//			}
//		}else{
//			try {
//				sp = passwordService.queryShortPassword(openId);
//				if(null == sp){
//					Memcached.incr("SHORT_PWDERROR_" + mid, 1);
//					return error("02", "用户不存在!", "");
//				}
//				mid = MD5Code.md5(sp.getStr("userCode")+"WX");
//			} catch (Exception e) {
//				return error("03", "登录失败,请稍后再试!", "");
//			}
//		}
//		
//		String userCode = sp.getStr("userCode");
//		//获取用户登录错误次数
//		long errorCount = Memcached.incr("SHORT_PWDERROR_" + userCode, 1);
//		if(errorCount <= 0){
//			Memcached.storeCounter("SHORT_PWDERROR_" + userCode, 1);
//		}
//		
//		//密码错误5次   加验证码 并验证
//		if(errorCount >= 6){
//			return error("05", "密码错误次数超过限制!", "");
//		}
//		
//		
//		//验证密码
////		ShortPassword sp = passwordService.validateShortPassword(mid,password);
//		try {
//			String dbPassword = MD5Code.md5(password);
//			String shortPasswd = sp.getStr("shortPasswd");
//			if(dbPassword.equals(shortPasswd) == false){
//				Memcached.incr("SHORT_PWDERROR_" + mid, 1);
//				return error("06", "密码错误!", "");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return error("07", "密码错误[07]!", "");
//		}
//
//		//清除短密码登录错误次数
//		Memcached.delete("SHORT_PWDERROR_" + userCode);
//		
//		String mobileType = sp.getStr("mobileType");
//		User user = userService.findById(userCode);
//		Map<String,String> map = new HashMap<String, String>();
//		map.put("userCode", userCode);
//		map.put("userName", user.getStr("userName"));
//		Message msg = succ("登录成功", map);
//		//session 缓存
//		String token = UserUtil.UserEnCode(userCode, getRequestIP(),30*60, null) ;
//		msg.setToken(token);
//		setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
//		
//		//修改用户登录相关字段
//		userService.updateUser4Login(userCode, getRequestIP());
//		
//		//记录日志
//		BIZ_LOG_INFO( userCode , BIZ_TYPE.LOGIN , "用户使用"+mobileType+"登录 " );
//
//		return msg;
//	}
	
	
//	/**
//	 * 微信授权
//	 * @return
//	 */
//	@ActionKey("/oauth4wx")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public Message oauth4wx(){
//		
//		//拼接激活路径
//		HttpServletRequest request = getRequest();
//		String redirectUri = request.getServerName()
//				+":"+request.getServerPort()+request.getContextPath()+"/back4oauth";  
//		String reqResult = HttpRequestUtil.sendGet(CommonUtil.OAUTH_CODE_URL, "redirectUri="+redirectUri+"&state=STATE&snsapiBase=1");
//		JSONObject parseResult = JSONObject.parseObject(reqResult);
//		String url = parseResult.getString("url");
//		System.out.println(url);
//		
//		return succ("获取成功", url);
//		
//	}
//	
	
	/**
	 * 微信回调
	 * @return
	 */
	@ActionKey("/back4oauth")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void back4oauth(){
		String code = getPara("code");
		String state = getPara("state");
		Message msg = weixinCallBack(code , state);
		
		String returnCode = msg.getReturn_code() ;
		if( "00".equals( returnCode ) == true ){
			
			setCookieByHttpOnly( "openid" , msg.getReturn_data().toString() , AuthInterceptor.INVALIDTIME );
			//已认证
			/*
			 * 根据openid获取UserInfo，enCode，setToekn
			 */
			redirect("/loginToGasture");
		}else if("W0".equals(returnCode) == true ){
			//未认证
			setCookieByHttpOnly( "openid" , msg.getReturn_data().toString() , AuthInterceptor.INVALIDTIME );
			redirect("/loginToMobile");
		}else{
			//重新微信授权流程,或者跳转到reAuth页面
			renderText("认证失败!");
		}
	}
	
	private Message weixinCallBack(String code , String state ){
		if(StringUtil.isBlank(code)){
			return error("01", "系统繁忙,请稍后再试!", "");
		}
		
		//用code 换取openid
		String reqResult = HttpRequestUtil.sendGet(CommonUtil.OAUTH_ACCESSTOKEN_URL, "appid=wx377e1b9c96a05ce6&code="+code+"&state="+state);
		Message parseResult = JSONObject.parseObject(reqResult , Message.class);
		if( "00".equals( parseResult.getReturn_code() ) == false ){
			return parseResult ;
		}
		JSONObject result = (JSONObject)parseResult.getReturn_data() ;
		String openid = result.getString("openid");
		
		//查询是否绑定手势密码
		ShortPassword sp = passwordService.queryShortPassword(openid);
		if(null == sp){
			return error("W0","未绑定", openid ) ;
		}else{
			return error("00","已绑定", openid ) ;
		}
		
	}
	
	/**
	 * 首页查询最新投标记录 ws
	 * */
	@ActionKey("/queryNewInvestBid")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryNewInvestBid(){
		int limit = 6;
		List<LoanTrace> loanTraces = loanTraceService.queryNewInvest(limit);
		List<LoanTransfer> loanTransfers = loanTransferService.queryNewTransfer(limit);
		List<Object> result = new ArrayList<Object>();
		int n=0;
		for(int i = 0;i<limit;i++){
			LoanTransfer loanTransfer = loanTransfers.get(i);
			String gotDateTime = loanTransfer.getStr("gotDate")+loanTransfer.getStr("gotTime");
			for(int j = n; j<loanTraces.size();j++){
				Map<String, Object> map = new HashMap<String, Object>();
				if(result.size()==limit){
					return succ("OK", result);
				}
				LoanTrace loanTrace = loanTraces.get(j);
				String loanDateTime = loanTrace.getStr("loanDateTime");
				if(Long.parseLong(gotDateTime)>Long.parseLong(loanDateTime)){
					String minute = String.valueOf(DateUtil.differentMinuteByMillisecond(gotDateTime, DateUtil.getNowDateTime(), "yyyyMMddHHmmss"));
					map.put("loanDateTime", minute);
					map.put("payUserName", loanTransfer.getStr("gotUserName"));
					map.put("payAmount", loanTransfer.getInt("transAmount"));
					map.put("productType", loanTransfer.getStr("productType"));
					map.put("loanNo", loanTransfer.getStr("loanNo"));
					map.put("loanTitle", loanTransfer.getStr("loanTitle"));
					map.put("type", "B");
					result.add(map);
					break;
					
				}else{
					String minute = String.valueOf(DateUtil.differentMinuteByMillisecond(loanDateTime, DateUtil.getNowDateTime(), "yyyyMMddHHmmss"));
					map.put("loanDateTime", minute);
					map.put("payUserName", loanTrace.getStr("payUserName"));
					map.put("payAmount", loanTrace.getLong("payAmount"));
					map.put("productType", loanTrace.getStr("productType"));
					map.put("loanNo", loanTrace.getStr("loanNo"));
					map.put("loanTitle", loanTrace.getStr("loanTitle"));
					map.put("type", "C");
					result.add(map);
					n++;
				}
			}
		}
		return succ("OK", result);
	}
	
	
	/////////////////////////////////   新版接口    end   ////////////////////////////////////////////////////	
	
	/**
	 * 查询用户的邀请奖励信息--PC端
	 * @return
	 */
	@ActionKey("/inviteRewardInfo")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class })
	public Message inviteRewardInfo(){
		String userCode = getUserCode();//获取用户userCode
	    if(StringUtil.isBlank(userCode)){
	    	return error("01", "用户未登录！", null);
	    }
		
	    String beginDate = getPara("beginDate", "");
	    String endDate = getPara("endDate", "");
	    
	    if(StringUtil.isBlank(beginDate)){
			return error("02", "活动开始时间有误!", null);
		}
		if(StringUtil.isBlank(endDate)){
			endDate = DateUtil.getNowDate();
		}
		if(!(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == -1)){
			return error("01", "活动时间有误，请检查！", "");
		}
		
		Map<String, Object> map = new HashMap<>();
		User user = userService.findById(userCode);
		map.put("inviteCode", user.getStr("inviteCode"));
		
		//累计收益 = 已赚取赏金 + 待赚取赏金
		Long tempBeRecyReward = 0L;
		Long tempReciedReward = 0L;
		Long sumReward = 0L;
		
		FundsController fundsController = new FundsController();
		List<LoanTrace> loanTraces = loanTraceService.queryTraceByDate(userCode, beginDate, endDate);
		for (LoanTrace loanTrace : loanTraces) {
			tempBeRecyReward += fundsController.rewardRemain(loanTrace);//待赚取赏金
		}
		//已赚取赏金
		tempReciedReward = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType.W.val(), fundsType.J.val(), userCode);
		sumReward = tempBeRecyReward+tempReciedReward;
		map.put("sumReward", Number.doubleToStr((double)sumReward));
		
		map.put("tNum", ticketsService.findTicketsByUserCode(userCode));
		return succ("查询成功", map);
	}
}











