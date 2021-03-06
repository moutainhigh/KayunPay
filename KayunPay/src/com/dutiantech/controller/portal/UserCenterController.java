package com.dutiantech.controller.portal;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.portal.validator.LoginValidate;
import com.dutiantech.controller.portal.validator.RegisterValidator;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AuthLog;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.CFCAInfo;
import com.dutiantech.model.FanLiTouUserInfo;
import com.dutiantech.model.Funds;
import com.dutiantech.model.QuestionnaireRecords;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.SignTrace;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.AuthenticationService;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.CFCAService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SignTraceService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.IdentityUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.util.UserUtil;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.ModifyMobileReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jx.service.JXService;
/**
 * 用户接口
 * @author shiqingsong
 *
 */
public class UserCenterController extends BaseController{

	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private AuthenticationService authenticationService = getService(AuthenticationService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private CFCAService cfcaService = getService(CFCAService.class);
	private SignTraceService signTraceService = getService(SignTraceService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
//	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
	
	@ActionKey("/isSign")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message isSign() {
		String userCode = getUserCode();
		if (userCode == null) {
			return error("02", "用户未登录", "noLogin");
		}
		boolean flag = signTraceService.isSign(userCode);
		if (flag) {
			return succ("已签到", true);
		}
		return error("01", "未签到", false);
	}
	
	/**
	 * 签到
	 * @return
	 */
	@ActionKey("/signIn")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message signIn() {
		String userCode = getUserCode();
		if (userCode == null) {
			return error("02", "用户未登录", "noLogin");
		}
		
		// 根据设置时间暂停签到功能
//		String pauseSatrtDate = "20180821";
//		if (DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), pauseSatrtDate) == 0 || 
//				DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), pauseSatrtDate) == 1) {
//			return error("99", "签到功能暂停~", "PAUSE");
//		}
		// 根据设置时间恢复签到功能
		String resumeDate = "20180905";
		if (DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), resumeDate) == -1) {
			return error("99", "签到功能暂停~", "PAUSE");
		}
		
		Map<String,Object> map = new HashMap<String, Object>();
		if (signTraceService.isSign(userCode)) {
			return error("01", "您今日已签到", "");
		} else {
			//防止恶意刷积分
			if(StringUtil.isBlank((String)Memcached.get("signToken_" + userCode)) 
					|| !DateUtil.getNowDate().equals(Memcached.get("signToken_" + userCode))){
				Memcached.set("signToken_" + userCode, DateUtil.getNowDate(), 30*1000);
			}else{
				return error("01", "请勿重复签到，若签到未成功，请30秒之后再操作！" ,"");
			}
			// 查询连续登录天数
			int sustainDay = signTraceService.findSustainDayByUser(userCode);
			// 账户积分变动
			int pointDay = sustainDay % 20 == 0 ? 20 : sustainDay % 20;
			int points = (SignTrace.POINT_MAP.get(pointDay) == null ? 0 : SignTrace.POINT_MAP.get(pointDay)) + 300; 
			fundsServiceV2.doPoints(userCode, 0 , points,"签到获取积分") ;
			// 保存签到记录
			signTraceService.saveSignTrace(null, userCode, pointDay, points, "");
			map.put("points", points / 100);
			map.put("sustain_day", sustainDay);
			return succ("恭喜您,签到成功!", map);
		}
	}
	
	/**
	 * 查询签到记录
	 * @return
	 */
	@ActionKey("/signHistory")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class}) 
	public Message signHistory() {
		Map<String, Object> map = new HashMap<String, Object>();
		String userCode = getUserCode();
		if (userCode == null) { 
			userCode = getPara("userCode");
		}
		if ("null".equals(userCode) || userCode == null) {
			return error("01", "用户未登录", "noLogin");
		}
		Calendar calendar = Calendar.getInstance();
		// 获取用户签到日期
		String[] signDays = signTraceService.getSignInDaysByMonth(userCode);
		map.put("signDays", signDays);
		// 遍历并剔除已签到日期， 获取用户未签到日期
		int nowDay = calendar.get(Calendar.DAY_OF_MONTH);
		List<String> notSignDays = new ArrayList<String>();
		for (int i = 0; i < nowDay; i++) {
			notSignDays.add(String.valueOf(i+1));
		}
		for (int i = 0; i < signDays.length; i++) {
			if (notSignDays.indexOf(signDays[i]) != -1) {
				notSignDays.remove(signDays[i]);
			}
		}
		map.put("notSignDays", notSignDays.toArray());
		return succ("", map);
	}
	
	@ActionKey("/replenishSignIn")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message replenishSignIn() {
		String userCode = getUserCode();
		List<SignTrace> lstSignTrace = SignTrace.signTraceDao.find("SELECT * FROM t_sign_trace WHERE userCode = ? AND signDate BETWEEN 20180521 AND DATE_FORMAT(now(),'%Y%m%d') - 1", userCode);
		if (lstSignTrace.size() > 0) {
			return error("01", "您已补签过，请勿重复提交", null);
		}
		String beginDate = "20180521";
		int differentDays = DateUtil.differentDaysByMillisecond(beginDate, DateUtil.getNowDate(), "yyyyMMdd");
		// 查询20日连续签到天数
		SignTrace signTrace = SignTrace.signTraceDao.findFirst("SELECT * FROM t_sign_trace WHERE userCode = ? AND signDate = 20180520", userCode);
		int lastSustainDay = signTrace == null ? 0 :signTrace.getInt("sustainDay");
		for (int i = 1; i <= differentDays; i++) {
			// 账户积分变动
			int pointDay = lastSustainDay + i;
			pointDay = pointDay > 20 ? pointDay - 20 : pointDay;
			int points = (SignTrace.POINT_MAP.get(pointDay) == null ? 0 : SignTrace.POINT_MAP.get(pointDay)) + 300;
			fundsServiceV2.doPoints(userCode, 0 , points,"签到获取积分");
			// 保存签到记录
			signTraceService.signIn(userCode, pointDay, points, DateUtil.delDay(DateUtil.getNowDate(), differentDays - i + 1), "");
		}
		return succ("恭喜您,补签成功!", "恭喜您,补签成功!");
	}
	
	/**
	 * 手机号登录
	 * @param mobile
	 * @param pwd
	 * @return
	 */
	@ActionKey("/mobileLogin")
	@Before(LoginValidate.class)
	public void mobileLogin() {
		Message msg = null;
		
		//获取参数
		String userMobile = getPara("loginName");
		String userPwd = getPara("loginPwd");
		
		if(StringUtil.isBlank(userMobile)){
			msg = error("01", "手机号为空", "");
			renderJson(msg);
			return ;
		}
		
		if(StringUtil.isBlank(userPwd)){
			msg = error("01", "密码为空", "");
			renderJson(msg);
			return ;
		}
		
		//获取用户登录错误次数
		long errorCount = Memcached.incr("LOGIN_PWDERROR_" + userMobile, 1);
		//密码错误3次   加验证码 并验证
		if(errorCount >= 4){
			msg = checkCapTicket("cac_z02_v1");
			if( msg != null ){
				renderJson(msg);
				return ;
			}
		}
		
		//密码加密
		String loginAuth = "" ;
		User user ;
		try {
			//原始加密算法
			String oldPwd = CommonUtil.getSourcePwd(userPwd);
			loginAuth = MD5Code.md5( userMobile + oldPwd ) ;
			user = userService.find4AuthCode(loginAuth);
			
			//判断错误返回
			if(null == user){
				if(errorCount > 3){
					msg = error("05", "用户名或密码错误,加验证码", "");
				}else{
					//Memcached.storeCounter("LOGIN_PWDERROR_" + userMobile, 1);
					msg = error("02", "用户名或密码错误", "");
				}
			}else{
				if (user.isBorrower()) {	// 借款人用户不允许登录
					msg = error("EU001", "用户名或密码错误", "");
				} else if (user.isFrozen()) {	// 冻结用户不允许登录
					msg = error("EU002", "用户被冻结 " + user.getStr("userState"), null);
				} else {
					// 缓存用户信息
					String userCode = user.getStr("userCode");
					String userName = user.getStr("userName");
					String jxAccountId = user.getStr("jxAccountId");
					String verifyPwd = "n";
					String verifyAuth = "n";
					
					// 同步用户江西银行存管信息
					if (!StringUtil.isBlank(jxAccountId)) {	//有电子账号
						// 同步银行卡
						JXController jxController = new JXController();
						jxController.jx_synBankCard(user);						
						verifyPwd = JXTraceService.verifyPwd(jxAccountId);
						
						// 同步存管授权信息
						userTermsAuthService.syncUserTermsAuth(user);
						
						// 获取用户授权信息
						UserTermsAuth userTermsAuth = userTermsAuthService.findByUserCode(userCode);
						verifyAuth = userTermsAuth.isPaymentAuth() ? "y" : "n";
						
						// 存管金额同步
//						if (!loanTraceService.unusualUserCode(userCode)) {
//							if (!StringUtil.isBlank(user.getStr("jxAccountId"))) {
//								Map<String, String> accountBalance = JXQueryController.balanceQuery(user.getStr("jxAccountId"));
//								Map<String, String> freezeBalance = JXQueryController.freezeAmtQuery(user.getStr("jxAccountId"));
//								long avBalance = StringUtil.getMoneyCent(accountBalance.get("availBal"));
//								long frozeBalance = StringUtil.getMoneyCent(freezeBalance.get("bidAmt")) + StringUtil.getMoneyCent(freezeBalance.get("repayAmt")) + StringUtil.getMoneyCent(freezeBalance.get("plAmt"));
//								fundsServiceV2.syncAccount(userCode, avBalance, frozeBalance);
//							}
//						}
					}
					
					// 更新含"x"的身份证号为大写
					userInfoService.update4CardIdUpperCase(userCode);

					Map<String , Object> resultMap = new HashMap<String , Object>() ;
					resultMap.put("userCode" , user.getStr("userCode") ) ;
					resultMap.put("userName" , userName ) ;
					resultMap.put("userMobile" , user.getStr("userMobile") ) ;
					resultMap.put("userEmail", user.getStr("userEmail") ) ;
					resultMap.put("lastLoginDateTime", user.getStr("lastLoginDateTime")) ;
					resultMap.put("lastLoginIp", user.getStr("lastLoginIp") ) ;
					resultMap.put("loginCount", user.getInt("loginCount") ) ;
					resultMap.put("vipLevel", user.getInt("vipLevel") ) ;
					resultMap.put("vipLevelName", user.getStr("vipLevelName") );
					resultMap.put("vipInterestRate", user.getInt("vipInterestRate") ) ;
					resultMap.put("vipRiskRate", user.getInt("vipRiskRate") ) ;
					resultMap.put("loginId", jxAccountId);//存管ID
					resultMap.put("verifyPwd", verifyPwd);	// 是否设置存管交易密码
					resultMap.put("verifyAuth", verifyAuth);	// 是否开通缴费授权
					//session 缓存
					msg = succ("ok", resultMap );
					String token = UserUtil.UserEnCode(userCode, getRequestIP(), null) ;
					msg.setToken(token);
					setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
					//cached
					Memcached.set("PORTAL_USER_" + userCode , user ) ;
					
					//修改用户登录相关字段
					userService.updateUser4Login(userCode, getRequestIP());
					
					//清除登录错误次数
					Memcached.delete("LOGIN_PWDERROR_" + userMobile);
					//清除短密码登录错误次数
					Memcached.delete("SHORT_PWDERROR_" + userCode);
					
					//绑定返利投
					String source = getPara("source");
					String fltuid = getPara("fltuid");
					if( StrKit.isBlank(source) == false ){
						if( "fanlitou".equals(source.toLowerCase()) == true ){
							//bind 
							Object obj = Memcached.get(source + fltuid );
							if( obj != null ){
								FanLiTouUserInfo.fanlitouDao.saveUser(userCode, userName, userMobile, 
										source , fltuid , 1 );
								Memcached.delete(source + fltuid);
							}
						}
						
					}
					
					//记录日志
					BIZ_LOG_INFO( userCode , BIZ_TYPE.LOGIN , "用户使用手机号登录 " );
				}
			}
		} catch (Exception e) {
			//记录日志
			BIZ_LOG_ERROR( userMobile , BIZ_TYPE.LOGIN , "登录异常：" , e);
			msg = error("AX", "系统错误", null);
		}
		//成功返回
		renderJson(msg);
	}
	
	/**
	* 2017.8.10 验证是否开通存管 rain
	**/
	@ActionKey("/isLogin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public void isLogin(){
		User user=getUser();
		Message message=null;
		if (user == null) {
			message = error("01", "用户未登录", false);
		}
		String chargeType=getPara("chargeType");
		boolean isOk = FuiouController.isFuiouAccount(user);
		if(isOk){
			message=succ("存管已激活",chargeType);
		}
		else{
			message=error("03", "存管未激活", isOk);
		}
		
		renderJson(message);
	}
	
	/**
	 * 手机验证码登录
	 * @param mobile
	 * @return
	 */
	@ActionKey("/smsLogin")
	public void smsLogin() {
		Message msg = null;
		
		//获取参数
		String userMobile = getPara("loginName");
		String smsMsg = getPara("smsMsg");
		if(StringUtil.isBlank(userMobile)){
			msg = error("01", "手机号为空", "");
			renderJson(msg);
			return ;
		}
		
		if(StringUtil.isBlank(smsMsg)){
			msg = error("01", "验证码为空", "");
			renderJson(msg);
			return ;
		}
		
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_LOGIN_" + userMobile, smsMsg) == false){
//			//记录日志
//			BIZ_LOG_WARN(userMobile, BIZ_TYPE.REGISTER, "短信验证码不正确");
			msg = error("02", "短信验证码不正确", "");
		}else{
			try {
				User user = userService.find4mobile(userMobile);
				
				//判断错误返回
				if(null == user){
//					//记录日志
//					BIZ_LOG_WARN( userMobile , BIZ_TYPE.LOGIN  ,  "登录失败，用户名或密码错误 ");
					msg = error("03", "用户名或密码错误", "");
				}else{
					String userType = user.getStr("userType");//验证用户是否为借款人
					if("J".equals(userType)&&null!=user.getStr("loginId")&&!"".equals(user.getStr("loginId"))){
						msg = error("06", "用户名或密码错误", "");
					}else{
						String userState = user.getStr("userState");
						String userCode = user.getStr("userCode");
						if( userState.equals("N") == false ){
							msg = error("04", "用户被冻结 " + userState, null ) ;
						}else{
							// 同步银行卡
							JXController jxController = new JXController();
							jxController.jx_synBankCard(user);
							
							// 缓存用户信息
							Map<String , Object> resultMap = new HashMap<String , Object>() ;
							String userName = user.getStr("userName");
							resultMap.put("userCode" , user.getStr("userCode") ) ;
							resultMap.put("userName" , userName ) ;
							resultMap.put("userMobile" , user.getStr("userMobile") ) ;
							resultMap.put("userEmail", user.getStr("userEmail") ) ;
							resultMap.put("lastLoginDateTime", user.getStr("lastLoginDateTime")) ;
							resultMap.put("lastLoginIp", user.getStr("lastLoginIp") ) ;
							resultMap.put("loginCount", user.getInt("loginCount") ) ;
							resultMap.put("vipLevel", user.getInt("vipLevel") ) ;
							resultMap.put("vipLevelName", user.getStr("vipLevelName") );
							resultMap.put("vipInterestRate", user.getInt("vipInterestRate") ) ;
							resultMap.put("vipRiskRate", user.getInt("vipRiskRate") ) ;
							//session 缓存
							msg = succ("ok", resultMap );
							long exTime = 7*24*60*60 ;
							String token = UserUtil.UserEnCode(userCode, getRequestIP(),exTime, null) ;
							msg.setToken(token);
							setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
	//						msg.setToken(UserUtil.UserEnCode(userCode, getRequestIP(), null));
							//cached
							Memcached.set("PORTAL_USER_" + userCode , user ) ;
							
							//修改用户登录相关字段
							userService.updateUser4Login(userCode, getRequestIP());
							
							//清除登录错误次数
							Memcached.delete("LOGIN_PWDERROR_" + userMobile);
							Memcached.delete("SHORT_PWDERROR_" + userCode);
							
							//记录日志
							BIZ_LOG_INFO( userCode , BIZ_TYPE.LOGIN , "用户使用手机号登录 " );
						}
					}
				}
			} catch (Exception e) {
				//记录日志
				BIZ_LOG_ERROR( userMobile , BIZ_TYPE.LOGIN , "登录异常：" , e);
				msg = error("AX", "系统错误", e.getMessage());
			}
		}
		
		//成功返回
		renderJson(msg);
	}
	
	/**
	 * 通过手机号注册
	 * @return
	 */
	@ActionKey("/register4mobile")
	@Before(RegisterValidator.class)
	public void register(){
		Message msg = null;
		if (true) {
			msg = error("99", "注册通道暂时关闭", "");
			renderJson(msg);
			return;
		}
		
		//获取参数并验证
		String userMobile = getPara("userMobile");
		String loginPasswd = getPara("loginPasswd");
		String userName =getPara("userName");
		String uCheckCode = getPara("uCheckCode");
		String fMobile = getPara("fMobile");
		String source = getCookie("platform");
		
		if(StringUtil.isNumeric(userName)){
			msg = error("02", "用户昵称不能为纯数字", "");
			renderJson(msg);
			return;
		}
		
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_REGISTER_" + userMobile, uCheckCode) == false){
			msg = error("05", "短信验证码不正确", "");
		}else{
			//验证用户名是否被注册
			List<User> listUser = userService.find4userName(userName);
			if(null != listUser && listUser.size() > 0){
				msg = error("06", "用户昵称已经被注册", "");
				renderJson(msg);
				return;
			}
			
			//验证是否已经存在此手机号
			User user = userService.find4mobile(userMobile);
			if(null != user){
				msg = error("06", "手机号已经被注册", "");
			}else{
				String regUserCode = UIDUtil.generate();
				try {
					//添加
					String sysDesc = "用户自助注册" + DateUtil.getNowDateTime();
					if ("p2peye".equals(source)) {
						sysDesc = "[p2peye][" + DateUtil.getNowDateTime() + "]";
					}
					boolean b = userService.save(regUserCode,userMobile, "00@yrhx.com", loginPasswd,userName, getRequestIP(), sysDesc);
					if(b == false){
						//记录日志
						BIZ_LOG_ERROR(userMobile, BIZ_TYPE.REGISTER, "注册失败",null);
						msg = error("07", "注册失败", "");
					}else{
						// TODO 新用户注册奖励
						// 注册成功送518现金券
						ticketService.toReward4newUser(regUserCode);
						// 注册成功送可用积分
//						fundsServiceV2.doPoints(regUserCode, 0 , 1000, "注册送积分") ;
						
						// 检查是否有推荐人
						String fUserCode = getCookie("fc","");
						if(StringUtil.isBlank(fUserCode)){
							fUserCode=getPara("fc","");
						}
						User fuser = null;
						if( StringUtil.isBlank(fUserCode) == false ){
							fuser = userService.findById(fUserCode) ;
						}else if(StringUtil.isBlank(fMobile) == false){
							if(StringUtil.isNumeric(fMobile.trim()) && (fMobile.trim().length() == 11)){//手机号
								fuser = userService.find4mobile(fMobile);
							}else{//邀请码
								fuser = userService.findByInviteCode(fMobile);
							}
						}
						
						if( fuser != null ){
							// 添加邀请记录
							RecommendInfo rmdInfo = new RecommendInfo();
							rmdInfo.set("aUserCode", fuser.getStr("userCode"));
							rmdInfo.set("aUserName", fuser.getStr("userName"));
							rmdInfo.set("bUserCode", regUserCode);
							rmdInfo.set("bUserName", userName);
							rmdInfo.set("bRegDate", DateUtil.getNowDate());
							rmdInfo.set("bRegTime", DateUtil.getNowTime());
							rmdInfo.set("rmdType", "");
							rmdInfo.set("rmdRemark", "好友推荐注册");
							rmdInfo.save();
							removeCookie("fc");
						}
						//记录用户注册日期
						String encryptUserMobile = CommonUtil.encryptUserMobile(userMobile);
						BIZ_LOG_INFO(encryptUserMobile, BIZ_TYPE.REGISTER, "注册用户成功");
					}
				} catch (Exception e) {
					//记录日志
					BIZ_LOG_ERROR(userMobile, BIZ_TYPE.REGISTER, "注册失败",e);
					msg = error("08", "注册失败", "");
				}
				
				msg = succ("恭喜您,注册成功!", "");
				//注册完直接登录
				String oldPwd = CommonUtil.getSourcePwd(loginPasswd);
				String loginAuth="";
				try {
					loginAuth = MD5Code.md5( userMobile + oldPwd );
				} catch (Exception e) {
					e.printStackTrace();
				}
				User newUser = userService.find4AuthCode(loginAuth);
				if(null!=newUser){
				Map<String , Object> resultMap = new HashMap<String , Object>() ;
				resultMap.put("userCode" , regUserCode ) ;
				String userName2 = newUser.getStr("userName");
				resultMap.put("userName" , userName2 ) ;
				resultMap.put("userMobile" , newUser.getStr("userMobile") ) ;
				resultMap.put("userEmail", newUser.getStr("userEmail") ) ;
				resultMap.put("lastLoginDateTime", newUser.getStr("lastLoginDateTime")) ;
				resultMap.put("lastLoginIp", newUser.getStr("lastLoginIp") ) ;
				resultMap.put("loginCount", newUser.getInt("loginCount") ) ;
				resultMap.put("vipLevel", newUser.getInt("vipLevel") ) ;
				resultMap.put("vipLevelName", newUser.getStr("vipLevelName") );
				resultMap.put("vipInterestRate", newUser.getInt("vipInterestRate") ) ;
				resultMap.put("vipRiskRate", newUser.getInt("vipRiskRate") ) ;
				
				//session 缓存
				msg = succ("ok", resultMap );
				String token = UserUtil.UserEnCode(regUserCode, getRequestIP(), null) ;
				msg.setToken(token);
				setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
				//cached
				Memcached.set("PORTAL_USER_" + regUserCode , newUser ) ;
				
				//修改用户登录相关字段
				userService.updateUser4Login(regUserCode, getRequestIP());
				
				//绑定返利投
				String source2 = getPara("source");
				String fltuid = getPara("fltuid");
				if( StrKit.isBlank(source2) == false ){
					if( "fanlitou".equals(source2.toLowerCase()) == true ){
						//bind 
						Object obj = Memcached.get(source2 + fltuid );
						if( obj != null ){
							FanLiTouUserInfo.fanlitouDao.saveUser(regUserCode, userName2, userMobile, 
									source2 , fltuid , 1 );
							Memcached.delete(source2 + fltuid);
						}
					}
					
				}
				
				//记录日志
				BIZ_LOG_INFO( regUserCode , BIZ_TYPE.LOGIN , "用户使用手机号登录 " );
				}
			}
		}
		renderJson(msg);
	}
	
	@ActionKey("/logout")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class})
	public void logout(){
		removeCookie( AuthInterceptor.COOKIE_NAME );
		renderJson(succ("deleted", ""));
	}
	
	/**
	 * 是否设置支付密码
	 * @return	
	 */
	@ActionKey("/hasPayPwd")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message hasPayPwd() {
		String userCode = getUserCode();
		String payPasswd = userService.findByField(userCode, "payPasswd").getStr("payPasswd");
		int isSetPayPwd = 1;
		if(StringUtil.isBlank(payPasswd)){
			isSetPayPwd = 0;
		}
		return succ("是否设置支付密码", isSetPayPwd);
	}
	
	
	/**
	 * 查询用户基本信息
	 * @return
	 */
	@ActionKey("/queryUser")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryUser() {
		String userCode = getUserCode();
		
		User user = User.userDao.findByIdLoadColumns(userCode, "lastLoginDateTime,lastLoginIP,regDate,"
				+ "regTime,regIP,userEmail,userMobile,userState,vipInterestrate,vipLevel,vipLevelName,vipRiskrate");
		
		try {
			user.set("userEmail", CommonUtil.decryptUserMobile(user.getStr("userEmail")));
			user.set("userMobile", CommonUtil.decryptUserMobile(user.getStr("userMobile")));
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
		return succ("查询用户基本信息完成", user);
	}
	
	
	/**
	 * 查询用户补充认证信息
	 * @return
	 */
	@ActionKey("/queryUserInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryUserInfo() {
		
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		
		if(null == userInfo){
			return error("01", "未进行实名认证!", "");
		}
		
		Map<String,Object> result = new HashMap<String, Object>();
		String cardId = "";
		try {
			//身份证号
			cardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			if(StringUtil.isBlank(cardId)){
				return error("01", "未进行实名认证!", "");
			}
			
			result.put("ecpMbile1", userInfo.getStr("ecpMbile1"));
			result.put("ecpMbile2", userInfo.getStr("ecpMbile2"));
			result.put("userCardName", userInfo.getStr("userCardName"));
			result.put("userAddress", userInfo.getStr("userAdress"));
			result.put("isAuthed", userInfo.getStr("isAuthed"));
			result.put("ecpName1", userInfo.getStr("ecpNme1"));
			result.put("ecpRlation1", userInfo.getStr("ecpRlation1"));
			result.put("ecpName2", userInfo.getStr("ecpNme2"));
			result.put("ecpRlation2", userInfo.getStr("ecpRlation2"));
		
			if(!StringUtil.isBlank(cardId)&&cardId.length()>=15){
				String gender = IdCardUtils.getGenderByIdCard(cardId);
				String birth = IdCardUtils.getBirthByIdCard(cardId);
				result.put("birth", birth);
				if(gender.equals("M")){
					result.put("gender", "男");
				}else if(gender.equals("F")){
					result.put("gender", "女");
				}else{
					result.put("gender", "未知");
				}
			}else{
				result.put("gender", "");
				result.put("birth", "");
			}
			
			String mobile = "";
			try{
				mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			}catch(Exception e){
			}
			result.put("mobile", mobile);
			result.put("userCardId", StringUtil.isBlank(cardId) ? "" : IdCardUtils.subCardId(cardId));
		} catch (Exception e) {
			return error("02", "查询用户基本信息失败!", "");
		}
		
		return succ("查询用户基本信息完成", result);
	}
	
	
	
	/**
	 * 修改用户信息
	 * @return
	 */
	@ActionKey("/updateUserInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateUserInfo() {
		//获取参数
		UserInfo userInfo = getModel(UserInfo.class, "userInfo");
		
		//验证
		
		//获取用户标识
		String userCode = getUserCode();
		userInfo.set("userCode", userCode);
		//删除不可修改字段
		userInfo.remove("userCardName","userCardId","cardImg","isAuthed","userInfoMac");
		
		//修改
		boolean update = userInfo.update();
		if(update == false){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "修改用户信息失败-02", null);
			return error("02", "修改失败!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "修改用户信息成功");
		
		return succ("修改成功","");
	}
	
	
	/**
	 * 修改登录密码
	 * @return
	 */
	@ActionKey("/updateLoginPwd")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateLoginPwd() {
		
		String oldPwd = getPara("oldPwd");
		String newPwd = getPara("newPwd");
		
		//数据验证
		
		//获取用户标识
		String userCode = getUserCode();

		//获取用户信息
		User user = User.userDao.findById(userCode);
		
		try {
			oldPwd = MD5Code.md5(CommonUtil.getSourcePwd(oldPwd)); 
			if(!oldPwd.equals(user.getStr("loginPasswd"))){
				//记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改用户登录密码失败，旧密码不正确");
				return error("03", "旧密码不正确", "");
			}
			newPwd = CommonUtil.getSourcePwd(newPwd);
			String authcode = MD5Code.md5(CommonUtil.decryptUserMobile(user.getStr("userMobile")) + newPwd ) ;
			user.set("loginAuthCode", authcode);
			user.set("loginPasswd",MD5Code.md5( newPwd ));
			user.update();
		} catch (Exception e) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USER, "修改用户登录密码失败04", e);
			return error("04", "修改密码异常", e.getMessage());
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改用户登录密码成功");
		return succ("修改密码成功", "");
	}

	
	/**
	 * 修改支付密码
	 * @return
	 */
	@ActionKey("/updatePayPwd")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updatePayPwd() {
		
		String payPwd = getPara("payPwd");
		String msgPayMac = getPara("msgPayMac");
		
		//数据验证
		if(StringUtil.isBlank(payPwd)){
			return error("01", "支付密码不能为空!", "");
		}
		if(StringUtil.isBlank(msgPayMac)){
			return error("01", "短信验证码不能为空!", "");
		}
		
		String userCode = getUserCode();

		//获取用户信息
		User user = User.userDao.findById(userCode);
		if(null == user){
			return error("02", "用户查询错误", "");
		}
		
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_PAYPWD_" + userCode, msgPayMac) == false){
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改支付密码失败，短信验证码不正确");
			return error("05", "短信验证码不正确", "");
		}
		
		try {
			payPwd = CommonUtil.encryptPasswd(payPwd);
			user.set("payPasswd", payPwd);
			user.update();
		} catch (Exception e) {
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USER, "修改支付密码异常", e);
			return error("05", "修改支付密码异常", e.getMessage());
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改支付密码成功");
		
		return succ("修改支付密码成功", "");
	}

	
	
	/**
	 * 找回密码
	 * @param userMobile
	 * @param newPwd
	 * @param smsmsg
	 * @return
	 */
	@ActionKey("/findPwd4user")
	public void findPwd4user(){
		
		Message msg = null;
		
		String userMobile = getPara("userMobile");
		String newPwd = getPara("newPwd");
		String smsmsg = getPara("smsmsg");
		
		//数据验证
		if(StringUtil.isBlank(userMobile) || StringUtil.isBlank(newPwd) || StringUtil.isBlank(smsmsg)){
			msg = error("01", "请检查是否输入正确!", "");
		}else{
			//验证短信验证码
			if(CommonUtil.validateSMS("SMS_MSG_FINDPWD_" + userMobile, smsmsg) == false){
				msg = error("03", "短信验证码不正确", "");
			}else{
				//获取用户信息
				User user = userService.find4mobile(userMobile);
				if(null == user){
					msg = error("02", "用户不存在", "");
				}else{
					//修改密码
					try {
						newPwd = CommonUtil.getSourcePwd(newPwd);
						String authcode = MD5Code.md5( userMobile + newPwd ) ;
						user.set("loginAuthCode", authcode);
						user.set("loginPasswd", MD5Code.md5(newPwd));
						user.update();
						//记录日志
						BIZ_LOG_INFO(user.getStr("userCode"), BIZ_TYPE.FINDPWD, "用户通过手机验证找回密码成功");
						msg = succ("修改密码成功", "");
					} catch (Exception e) {
						//记录日志
						BIZ_LOG_ERROR(user.getStr("userCode"), BIZ_TYPE.FINDPWD, "用户通过手机验证找回密码失败", e);
						msg = error("04", "修改密码异常", e.getMessage());
					}
				}
			}
		}
		renderJson(msg); 
	}

	
	
	/**
	 * 身份认证
	 * @param remark
	 * @return
	 */
	@ActionKey("/certification")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message certification(){
		//获取参数
//		Files fileInfo = getModel(Files.class, "fileInfo");
		String cardIdImg = getPara("cardIdImg");
		String trueName = getPara("trueName");
		String cardId = getPara("cardId").toUpperCase();
		
		//获取用户标识
		String userCode = getUserCode();
		
		//验证
		if(StringUtil.isBlank(cardIdImg)){
			return error("01", "身份证图片为空!", "");
		}
		if(StringUtil.isBlank(trueName)){
			return error("01", "姓名为空!", "");
		}
		if(!IdCardUtils.validateCard(cardId)){
			return error("01", "身份证不正确!", "");
		}
		
		String md5CardId = "";
		try{
			md5CardId = CommonUtil.encryptUserCardId(cardId);
		}catch(Exception e){
			return error("05", "身份证加密错误", "");
		}
		
		//验证身份证是否已经被认证
		UserInfo userInfo = UserInfo.userInfoDao.findFirst("select isAuthed from t_user_info where userCardId = ?" , md5CardId);
		if(userInfo != null && "2".equals(userInfo.getStr("isAuthed"))){
			return error("06", "身份证已被认证", "");
		}
		
		//图片路径保存到用户信息扩展表
//		UserInfo updateUserInfo = new UserInfo();
//		updateUserInfo.set("userCode", userCode);
//		updateUserInfo.set("userCardName", trueName);
//		updateUserInfo.set("userCardId", md5CardId);
//		updateUserInfo.set("isAuthed", "1");
//		updateUserInfo.set("cardImg", cardIdImg);
//		boolean update = updateUserInfo.update();
		boolean update = userInfoService.userAuth(userCode,trueName, md5CardId, cardIdImg,"1");
		if(!update){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "人工认证异常或重复认证", null);
			return error("05", "已经提交,请等待审核!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户提交身份认证材料成功");
		
		return succ("添加成功", "");
	}

	/**
	 * 自动身份认证
	 * @param remark
	 * @return
	 */
	@ActionKey("/certificationAuto")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message certificationAuto(){
		//获取参数
		String trueName = getPara("trueName");
		String cardId = getPara("cardId").toUpperCase();
		
		//获取用户标识
		String userCode = getUserCode();
		
		//验证
		if(StringUtil.isBlank(trueName)){
			return error("01", "姓名为空!", "");
		}
		if(!IdCardUtils.validateCard(cardId)){
			return error("01", "身份证不正确!", "");
		}
		
		String md5CardId = "";
		try{
			md5CardId = CommonUtil.encryptUserCardId(cardId);
		}catch(Exception e){
			return error("02", "身份证加密错误", "");
		}
		
		//验证身份证是否已经被认证
		UserInfo userInfo = UserInfo.userInfoDao.findFirst("select isAuthed from t_user_info where userCardId = ?" , md5CardId);
		if(userInfo != null && "2".equals(userInfo.getStr("isAuthed"))){
			return error("06", "身份证已被认证", "");
		}
		
		//次数限制
		Long count = Db.queryLong("select count(1) from t_auth_log where userCode = ?" ,userCode);
		if(count > 3){
			return error("09", "认证次数超限制", "");
		}
		
		//调用自动认证
		int ret = authenticationService.autoAuth(trueName, cardId);
		if(0 != ret){
			saveAuthLog(userCode);
			return error("04", "请输入正确的姓名和身份证号", "");
		}
		
		//保存用户信息扩展表
//		UserInfo updateUserInfo = new UserInfo();
//		updateUserInfo.set("userCode", userCode);
//		updateUserInfo.set("userCardName", trueName);
//		updateUserInfo.set("userCardId", md5CardId);
//		updateUserInfo.set("isAuthed", "2");
//		boolean update = updateUserInfo.update();
//		if(!update){
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常", null);
//			return error("05", "自动认证异常!", "");
//		}
		
		boolean update = userInfoService.userAuth(userCode,trueName, md5CardId, "","2");
		if(!update){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
			return error("05", "已经认证,请勿重复提交!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户自动认证成功");
		
		// 身份认证成功赠送可用积分	add by stonexk at 20170601
//		fundsServiceV2.doPoints(userCode, 0 , 2000, "注册送积分");
		// end add
		
		// 身份认证后，邀请人添加30元现金抵用券  5月活动,9月继续，常态
		try{
			User user = userService.findById(userCode);
			RecommendInfo rmd = RecommendInfo.rmdInfoDao.findFirst("select * from t_recommend_info where bUserCode = ?",user.getStr("userCode"));
			if(rmd != null){
				User shareUser = userService.findById(rmd.getStr("aUserCode"));
				if(shareUser!=null){
					//实名认证送券
//					boolean aa = ticketService.save(shareUser.getStr("userCode"), shareUser.getStr("userName"),CommonUtil.decryptUserMobile(shareUser.getStr("userMobile")) , "", 
//							"30元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 15), "F", null, SysEnum.makeSource.A);
					boolean aa = ticketService.saveADV(shareUser.getStr("userCode"), "50元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
					if(aa){
						String mobile = userService.getMobile(shareUser.getStr("userCode"));
						String content = CommonUtil.SMS_MSG_TICKET.replace("[huoDongName]", "推荐好友实名认证").replace("[ticketAmount]", "50");
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", content);
						smsLog.set("userCode", shareUser.getStr("userCode"));
						smsLog.set("userName", shareUser.getStr("userName"));
						smsLog.set("type", "15");smsLog.set("typeName", "送现金券活动");
						smsLog.set("status", 9);
						smsLog.set("sendDate", DateUtil.getNowDate());
						smsLog.set("sendDateTime", DateUtil.getNowDateTime());
						smsLog.set("break", "");
						smsLogService.save(smsLog);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return succ("认证成功", "");
	}
	
	/**
	 * 保存自动认证次数
	 * @param userCode
	 */
	private void saveAuthLog(String userCode){
		AuthLog authLog = new AuthLog();
		authLog.set("userCode", userCode);
		authLog.set("dateTime", DateUtil.getNowDateTime());
		authLog.save();
	}
	
	
	/**
	 * 获取认证信息
	 * @return
	 */
	@ActionKey("/getAuthInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getAuthInfo(){
		//获取用户标识
		String userCode = getUserCode();
		User user = User.userDao.findById(userCode);
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		
		Map<String, String> authMap = new HashMap<String, String>();
		String isAuth = "0";
		String trueName = "";
		String cardId = "";
		String email = "";
		String isPayPasswd = "0";
		String isCFCA = "0";
		if (userInfo != null && !"0".equals(userInfoService.isAuthed(userCode))) {
			isAuth = userInfo.getStr("isAuthed");
			trueName = userInfo.getStr("userCardName");
			cardId = userInfo.getStr("userCardId");
			if(StringUtil.isBlank(cardId)){
				cardId = "";
			}else{
				try {
					cardId = CommonUtil.decryptUserCardId(cardId);
				} catch (Exception e) {
					return error("01", "查询失败-[01]", "");
				}
				cardId = IdCardUtils.subCardId(cardId);
			}
		}
		
		if(user != null && !StringUtil.isBlank(user.getStr("userEmail")) 
				&& !user.getStr("userEmail").equals("00@yrhx.com")){
			email = user.getStr("userEmail");
		}
		
		if(user != null && !StringUtil.isBlank(user.getStr("payPasswd"))){
			isPayPasswd = "1";
		}
		
		if(userInfo != null){
			isCFCA = userInfo.getStr("isCFCA");
		}
		String userType = user.getStr("userType");
		
		// 江西银行存管授权信息
		String paymentAuth = null, paymentDeadline = null, paymentMaxAmt = null;
		String repayAuth = null, repayDeadline = null, repayMaxAmt = null;
		String riskReminder = null;//风险提示授权书
		UserTermsAuth userTermsAuth = userTermsAuthService.findByUserCode(userCode);
		if (userTermsAuth != null) {
			paymentAuth = userTermsAuth.getStr("paymentAuth");	// 缴费授权状态：1-开通；0-未开通
			paymentDeadline = userTermsAuth.getStr("paymentDeadline");	// 缴费授权到期时间
			paymentMaxAmt = StringUtil.getMoneyYuan(userTermsAuth.getLong("paymentMaxAmt"));	// 缴费授权签约最高金额
			repayAuth = userTermsAuth.getStr("repayAuth");	// 还款授权状态：1-开通；0-未开通
			repayDeadline = userTermsAuth.getStr("repayDeadline");	// 还款授权到期时间
			repayMaxAmt = StringUtil.getMoneyYuan(userTermsAuth.getLong("repayMaxAmt"));
			riskReminder = userTermsAuth.getStr("riskReminder");
		}
		
		authMap.put("paymentState", paymentAuth);
		authMap.put("paymentDate", paymentDeadline);
		authMap.put("paymentAmount", paymentMaxAmt);
		authMap.put("repayState", repayAuth);
		authMap.put("repayDate", repayDeadline);
		authMap.put("repayAmount", repayMaxAmt);
		authMap.put("isAuth", isAuth);
		authMap.put("trueName", trueName);
		authMap.put("cardId", cardId);
		authMap.put("email", email);
		authMap.put("isPayPasswd", isPayPasswd);
		authMap.put("isCFCA", isCFCA);
		authMap.put("evaluationLevel", user.getStr("evaluationLevel"));
		authMap.put("userType", userType);
		authMap.put("riskReminder", riskReminder);
		return succ("查询成功", authMap);
	}
	
	
	/**
	 * 安全等级
	 * @param remark
	 * @return
	 */
	@ActionKey("/safeLive")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message safeLive(){
		Map<String,Integer> safeMap = new HashMap<String, Integer>();
		String userCode = getUserCode();
		
		User user = User.userDao.findById(userCode);
		
		//支付密码
		if(StringUtil.isBlank(user.getStr("payPasswd"))){
			safeMap.put("payPwd", 0);
		}else{
			safeMap.put("payPwd", 1);
		}
		
		//邮箱
		if(!StringUtil.isBlank(user.getStr("userEmail")) && !"00@yrhx.com".equals(user.getStr("userEmail"))){
			safeMap.put("email", 1);
		}else{
			safeMap.put("email", 0);
		}
		
		//身份验证
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		if("2".equals(userInfo.getStr("isAuthed"))){
			safeMap.put("cardId", 1);
		}else{
			safeMap.put("cardId", 0);
		}
		
		return succ("查询成功", safeMap);
	}

	
	/**
	 * 通过手机验证用户是否存在
	 * @return
	 */
	@ActionKey("/validate4mobile")
	@Before( PkMsgInterceptor.class )
	public Message validate4mobile(){
		String mobile = getPara("mobile","");
		if(StringUtil.isBlank(mobile)){
			return error("01", "手机号为空", "");
		}
		//验证手机号是否存在
		User user = userService.find4mobile(mobile);
		int a = 1;
		if(null == user){
			a = 0;
		}
		return succ("查询成功", a);
	}
	
	
	/**
	 * CFCA认证
	 * @return
	 */
	@ActionKey("/userCFCA")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message userCFCA(){
		String userCode = getUserCode();
		userInfoService.userCFCA(userCode);
		return succ("操作成功", "");
	}
	
	/**
	 * 查询CFCA
	 * @return
	 */
	@ActionKey("/queryCFCA")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryCFCA(){
		String bCode = getPara("bCode");
		CFCAInfo cfcaInfo = cfcaService.queryByCode(bCode);
		return succ("查询成功", cfcaInfo);
	}
	
	/**
	 * 修改平台用户手机号
	 */
	@ActionKey("/updateYrMobile")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message updateYrMobile() {
		// 获取用户标识
		String userCode = getUserCode();

		// 获取用户信息
		User user = User.userDao.findById(userCode);

		String oldMobile = "";
		try {
			oldMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String newMobile = getPara("newMobile", "");
		String msgMac = getPara("msgMac");
		String loginPwd = getPara("loginPwd", "");

		if (StringUtil.isBlank(newMobile)) {
			return error("01", "手机号不能为空", "");
		}
		if (StringUtil.isBlank(loginPwd)) {
			return error("02", "密码不能为空", "");
		}

		if (CommonUtil.isMobile(newMobile) == false) {
			return error("03", "手机号格式不正确", "");
		}

		User newUser = userService.find4mobile(newMobile);

		if (null != newUser) {
			return error("04", "新手机号已存在", "");
		}
		if (CommonUtil.validateSMS("SMS_MSG_PHONE_" + newMobile, msgMac) == false) {
			return error("05", "短信验证码不正确", "");
		}
		String oldPwd = "";
		try {
			oldPwd = MD5Code.md5(CommonUtil.getSourcePwd(loginPwd));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (!oldPwd.equals(user.getStr("loginPasswd"))) {
			// 记录日志
			BIZ_LOG_INFO(user.getStr("userCode"), BIZ_TYPE.USER,
					"修改用户手机号失败，登录密码不正确");
			return error("06", "登录密码不正确", "");
		}
		boolean b = userService.updateUserMobile(user.getStr("userCode"),
				newMobile, loginPwd);
		if (b == false) {
			return error("07", "修改异常", "");
		}

		BIZ_LOG_INFO(user.getStr("userCode"), BIZ_TYPE.USER,
				"修改平台用户手机号：平台用户手机号【" + oldMobile + "】修改为 【" + newMobile + "】");

		return succ("平台手机号修改成功,请重新登录!", "");
	}

	/**
	 * 修改存管用户手机号
	 */
	@ActionKey("/updateHfMobile")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void updateHfMobile() {
		String userCode = getUserCode();
		User user = User.userDao.findById(userCode);
		String loginId = user.getStr("loginId");
		
		if (!StringUtil.isBlank(loginId) ) {
			try {
				loginId = CommonUtil.decryptUserMobile(loginId);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ModifyMobileReqData modifyMobileReqData = new ModifyMobileReqData();
			modifyMobileReqData.setLogin_id(loginId);
			modifyMobileReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			modifyMobileReqData.setMchnt_txn_ssn(CommonUtil.genMchntSsn());
			modifyMobileReqData.setPage_notify_url(CommonUtil.ADDRESS
					+ "/changUserMobileSign");
			try {
				FuiouService.p2p400101(modifyMobileReqData, getResponse());
			} catch (Exception e) {
				e.printStackTrace();
			}
			renderNull();
		}

	}
	@ActionKey("/queryInviteCode")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class} )
	public Message queryInviteCode(){
		User user = getUser();
		
		String inviteCode = null;
		if(user != null){
			inviteCode = user.getStr("inviteCode");
		}
		return succ("查询成功", inviteCode);
	}
/////////////////////////////////   私有函数       ////////////////////////////////////////////////////
	
	/*
	 * --start--
	 * 风险评测    20180125   
	 */
	@ActionKey("/exposureRating")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class,PkMsgInterceptor.class })
	public Message exposureRating(){
		String userCode = getUserCode();
		User user = getUser();
	
		String age = getPara("age","");//年龄
		String familyIncome = getPara("familyIncome","");//家庭年收入
		String education = getPara("education","");//最高学历
		String investratio = getPara("investratio","");//投资比例
		String investexperience = getPara("investexperience","");//投资经验
		String investproduct = getPara("investproduct","");//投过的产品
		String investaim = getPara("investaim","");//投资目标
		String investlimit = getPara("investlimit","");//投资期限
		String compare = getPara("compare","");//投资比较
		String moodswing = getPara("moodswing","");//什么情况产生焦虑
		int sum = 2;//默认分数最低
			
		//测评分数
		Map<String,Integer> map = new HashMap<String,Integer>();
		   map.put("A",1);
		   map.put("B",2);
		   map.put("C",3);
		   map.put("D",4);
		   map.put("E",5);

		String surveyResult = null;//评测结果 ABCD

		Map<String,String> result = new HashMap<String,String>();

		if(!compare.matches("[ABCD]")||!moodswing.matches("[ABCDE]")){
			return error("01","请认证填写评测表！",null);
		}
			
		sum = map.get(compare)+map.get(moodswing);//评测总分
			
			//评测结果 ：A为保守型   B为稳健型  C为进取型  D为冒险型
			if(sum<=3){
				surveyResult = "A";
			}else if(sum>3&&sum<=5){
				surveyResult = "B";
			}else if(sum>5&&sum<=7){
				surveyResult = "C";
			}else{
				surveyResult = "D";
			}

			//将评测答案 以json字符串保存
			JSONObject recordObj = new JSONObject();
			recordObj.put("age",age);
			recordObj.put("familyIncome",familyIncome);
			recordObj.put("education",education);
			recordObj.put("investratio",investratio);
			recordObj.put("investexperience",investexperience);
			recordObj.put("investproduct",investproduct);
			recordObj.put("investaim",investaim);
			recordObj.put("investlimit",investlimit);
			recordObj.put("compare",compare);
			recordObj.put("moodswing",moodswing);
			
			//保存评测表
			QuestionnaireRecords questionnaireRecords = new QuestionnaireRecords();
			questionnaireRecords.set("userCode",userCode);
			questionnaireRecords.set("userName",user.getStr("userName"));
			questionnaireRecords.set("surveyRecord", recordObj.toJSONString());
			questionnaireRecords.set("surveyResult",surveyResult);
			questionnaireRecords.set("addDateTime", DateUtil.getNowDateTime());
		
				if(questionnaireRecords.save()){
					boolean updateSurvey = userService.updateSurvey(userCode,surveyResult);//更新user评测结果
					if(!updateSurvey){
						return error("01", "评测结果更新失败", null);
					}else{
						user.set("evaluationLevel",surveyResult);
						Memcached.set("PORTAL_USER_" + userCode,user);
					}
					result.put("result",surveyResult);
			}else{
				return error("01", "评测结果更新失败", null);
			}
			
		return succ("评测成功", result);
	}
	
	/*------------end----------------*/
	
	/*
	 * 自动评测 20180202
	 */
	public Message evaluationResult(String userCode){
		User user = userService.findById(userCode);
		//测评分数
		Map<String,Integer> map = new HashMap<String,Integer>();
			map.put("A",1);
			map.put("B",2);
			map.put("C",3);
			map.put("D",4);
			map.put("E",5);
		String age = "";
		String compare = "";
		String moodswing = "";

		UserInfo userInfo = userInfoService.findById(userCode);
		String cardId=null;
		
		try {
			cardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			return error("02","身份证解密错误",null);
		}
	
		int age1 = IdentityUtil.getAge(cardId);
		
		if(age1<=30){
			age="A";
		}else if(age1>=31&&age1<=45){
			age="B";
		}else if(age1>=45&&age1<=60){
			age="C";
		}else{
			age="D";
		}
		
		AutoLoan_v2 autoLoan = autoLoanService.queryAutoLoanByUserCode(userCode);
		if(autoLoan == null){ //没设置自动投标分数最低
			compare = "A";
			moodswing = "A";
		}else{
			int onceMinAmount = autoLoan.getLong("onceMinAmount").intValue();
			int autoMinLim = autoLoan.getInt("autoMinLim");
			
			if(onceMinAmount<1000000){
				if(autoMinLim<=3){
					compare = "A";
					moodswing = "A";
				}else if(autoMinLim>=4&&autoMinLim<=18){
					compare = "B";
					moodswing = "A";
				}else{
					compare = "C";
					moodswing = "B";
				}
			}else{
				if(autoMinLim<=3){
					compare = "A";
					moodswing = "A";
				}else if(autoMinLim>=4&&autoMinLim<=18){
					compare = "C";
					moodswing = "A";
				}else{
					compare = "D";
					moodswing = "B";
				}
			}
		}
	
		int sum = map.get(compare)+map.get(moodswing);//评测总分
		
		String surveyResult = "A";
			
			//评测结果 ：A为保守型   B为稳健型  C为进取型  D为冒险型
			if(sum<=3){
				surveyResult = "A";
			}else if(sum>3&&sum<=5){
				surveyResult = "B";
			}else if(sum>5&&sum<=7){
				surveyResult = "C";
			}else{
				surveyResult = "D";
			}

			//将评测答案 以json字符串保存
			JSONObject recordObj = new JSONObject();
			recordObj.put("age",age);
			/*recordObj.put("familyIncome",familyIncome);
			recordObj.put("education",education);
			recordObj.put("investratio",investratio);
			recordObj.put("investexperience",investexperience);
			recordObj.put("investproduct",investproduct);
			recordObj.put("investaim",investaim);
			recordObj.put("investlimit",investlimit);*/
			recordObj.put("compare",compare);
			recordObj.put("moodswing",moodswing);
			
			//保存评测表
			QuestionnaireRecords questionnaireRecords = new QuestionnaireRecords();
			questionnaireRecords.set("userCode",userCode);
			questionnaireRecords.set("userName",user.getStr("userName"));
			questionnaireRecords.set("surveyRecord", recordObj.toJSONString());
			questionnaireRecords.set("surveyResult",surveyResult);
			questionnaireRecords.set("addDateTime", DateUtil.getNowDateTime());
			
			Map<String ,String> result = new HashMap<String,String>();
		
				if(questionnaireRecords.save()){
					boolean updateSurvey = userService.updateSurvey(userCode,surveyResult);//更新user评测结果
					if(!updateSurvey){
						return error("01", "评测结果更新失败", null);
					}else{
						user.set("evaluationLevel",surveyResult);
						Memcached.set("PORTAL_USER_" + userCode,user);
					}
					result.put("result",surveyResult);
			}else{
				return error("01", "评测结果更新失败", null);
			}
			
		return succ("评测成功", result);
	}
		
	
	/**
	 * 会员中心页面
	 * @return
	 */
	@ActionKey("/queryVIPCenterPage")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryVIPCenterPage(){
		String userCode = getUserCode();
		if( userCode == null){
			return error("01", "用户未登录", false);
		}
		User user = userService.findById(userCode);
		if(user == null){
			return error("02", "未查找到用户信息", null);
		}
		Map<String, Object> resultMap = new HashMap<String, Object>();
		//当前会员名称
		String vipLevelName = user.getStr("vipLevelName");
		int vipLevel = user.getInt("vipLevel");
		
		if(vipLevelName == null){
			return error("03", "未查找到会员信息", null);
		}
		//实时待收
		long nowBeRecyAmount = 0l;
		//实时会员等级待收上限
		long limitAmount = 0l;
		//距离下一等级的差额
		long leftAmount = 0l;
		Funds funds = null;

		try {
			funds = fundsServiceV2.findById(userCode);
			nowBeRecyAmount = funds.getLong("beRecyPrincipal") + funds.getLong("beRecyInterest");
			//nowBeRecyAmount = loanTraceService.queryNowBeRecyByUserCode(userCode);
			//实时会员等级
			VipV2 vipV2 = VipV2.getVipBybeRecyAmount(nowBeRecyAmount);
			if(vipV2.getVipLevel() != VipV2.VIPS.size()){//非至尊级别
				limitAmount = vipV2.getVipMaxAmount();
				leftAmount = limitAmount - nowBeRecyAmount;
			}
		} catch (Exception e) {
			return error("04", "实时待收加载中", null);
		}
		resultMap.put("vipLevelName", vipLevelName);
		resultMap.put("vipLevel", vipLevel);
		resultMap.put("nowBeRecyAmount", Number.longToString(nowBeRecyAmount));
		resultMap.put("leftAmount", Number.longToString(leftAmount));
		
		return succ("查询成功", resultMap);
	}
}
