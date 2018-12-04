package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutian.SMSClient;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.MenuV2;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.SMSLog;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.MenuV2Service;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class AuthV2Controller extends BaseController {
	
	private MenuV2Service menuService = getService(MenuV2Service.class);
	private OPUserV2Service opUserService = getService(OPUserV2Service.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private OPUserV2Service oPUserV2Service = getService(OPUserV2Service.class);
//	private SMSService smsService = getService(SMSService.class);
	
	@ActionKey("/doLoginV2")
	@Before(value=PkMsgInterceptor.class)
	public void doLogin(){
		String tmpUserMobile = getPara("userName");
		String userPwd = getPara("userPwd");
		Message msg = null ;
		if( StringUtil.isBlank(tmpUserMobile)) {
			msg = error("10", "登录手机号为空！", null ) ;
		}
		
		if( StringUtil.isBlank(userPwd)){
			msg = error("11", "登录密码为空！", null ) ;
		}
		
		if( msg == null ){
			if( validateCaptcha("cv") == false ){
				msg = error("14", "验证码错误", "");
			}
//			msg = checkCapTicket("cac_login_v1");
		}
		
		if( msg == null ){
			
			try {
				String userMobile = CommonUtil.encryptUserMobile(tmpUserMobile);
				userPwd = CommonUtil.encryptPasswd(userPwd);
				OPUserV2 user = opUserService.doLogin( userMobile, userPwd) ;
				if( user != null ){
					msg = succ("ok", null );
					String userCode = user.getStr("op_code") ;
					String menuMap = user.getStr("op_map") ;
					String token =  UserUtil.UserEnCode(userCode, getRequestIP() , UserUtil.defaultEnCodeKey ) ;
					msg.setToken( token );
					CACHED.put( OPUserV2.USER.OPMAP.key() + userCode , menuMap );
					CACHED.put( OPUserV2.USER.OPCODE.key() + userCode , user );
//					String tmp1 = user.getStr("role_code");
//					if(!StringUtil.isBlank(tmp1)){
//						String tmp2 = Db.queryStr("select role_map from t_role_v2 where role_code = ?",tmp1);
//						if(!StringUtil.isBlank(tmp2)){
//							CACHED.put(OPUserV2.USER.ROLEMAP.key()+userCode, tmp2);
//						}
//					}
					
					user.put("userMobile", tmpUserMobile ) ;
					user.remove("op_map");
					msg.setReturn_data(user);
					setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
				}else{
					msg = error("13", "用户名或者密码错误", null ) ;
				}
			} catch (Exception e) {
				e.printStackTrace( );
				msg = error("12", "登录异常，e:" + e.getMessage(), null ) ;
			}
		}
		renderJson( msg ); 
	}
	
	/**
	 * 后台登录
	 * @author shiqingsong
	 * @return
	 */
	@ActionKey("/doLoginV3")
	@Before(value=PkMsgInterceptor.class)
	public Message doLoginV3(){
		String tmpUserMobile = getPara("userName");
		String smsMsg = getPara("smsMsg");
		if(StringUtil.isBlank(tmpUserMobile)) {
			return error("10", "登录手机号为空！", null ) ;
		}
		
		if(CommonUtil.validateSMS("SMS_ADMIN_LOGIN_" + tmpUserMobile, smsMsg) == false){
			return  error("14", "短信码错误", null ) ;
		}
		
		OPUserV2 user = opUserService.query4mobile(tmpUserMobile);
		if( user == null ){
			return error("13", "用户不存在", null ) ;
		}
		
		Message msg = succ("ok", null );
		String userCode = user.getStr("op_code") ;
		String menuMap = user.getStr("op_map") ;
		String token =  UserUtil.UserEnCode(userCode, getRequestIP() , UserUtil.defaultEnCodeKey ) ;
		msg.setToken( token );
		CACHED.put( OPUserV2.USER.OPMAP.key() + userCode , menuMap );
		CACHED.put( OPUserV2.USER.OPCODE.key() + userCode , user );
		user.put("userMobile", tmpUserMobile ) ;
		user.remove("op_map");
		msg.setReturn_data(user);
		setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );	
		return succ("登录成功!", msg); 
	}

	
	
	@SuppressWarnings("unused")
	@ActionKey("/getMenusV2")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value=999)
	public Message getMenuV2s(){
		
		String userCode = getUserCode() ;
		String menuCachedKey = "MENU_LIST_" + userCode;
		String mapCachedKey = OPUserV2.USER.OPMAP.key() + userCode;
		String userKey = OPUserV2.USER.OPCODE.key() + userCode ;
//		String roleMapCachedKey = OPUserV2.USER.ROLEMAP.key() + userCode;//角色权限
		List<MenuV2> menus = null ;
		
//		Object menuObj = CACHED.get(menuCachedKey) ;
//		if( menuObj == null ){
//			
			Object obj = CACHED.get(mapCachedKey ) ;
			if( obj == null )
				return error("AX", "授权过期!", null ) ;
			
			String roleMap = (String) obj ;
			//TODO 加角色权限综合得到菜单
			menus = menuService.queryShowMenus(roleMap) ;	//cached
//			CACHED.put(menuCachedKey, menus ); 
//		}else{
//			menus = (List<MenuV2>)(menuObj) ;
//		}
		return succ("ok", menus ) ;
	}
	
	@ActionKey("/getOPName")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value=999)
	public Message getOPName(){
		
		String userCode = getUserCode() ;
		String userName = opUserService.findUserNameById(userCode);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("opUserName", userName);
		return succ("ok", result ) ;
	}
	
	@ActionKey("/getOPUserInfo")
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	@AuthNum(value=999)
	public Message getOPUserInfo() {
		String userCode = getUserCode();
		OPUserV2 opUserV2 = opUserService.findById(userCode);
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("opUser", opUserV2);
		return succ("ok", result);
	}
	
	@ActionKey("/modifyOPUser")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value=999)
	public Message modifyOPUser(){
		String userCode = getUserCode() ;
		String userName = getPara("userName","");
		String newPwd = getPara("newPwd","");
		if(!StringUtil.isBlank(userName)){
			opUserService.updateUserName(userCode, userName);
		}
		if(!StringUtil.isBlank(newPwd)){
			try {
				newPwd = CommonUtil.encryptPasswd(newPwd);
				opUserService.updatePassword(userCode, newPwd);
			} catch (Exception e) {
			}
			
		}
		return succ("ok", true ) ;
	}
	
	public static void main(String[] args) throws Exception {
		String userMobile = CommonUtil.encryptUserMobile("15871727350");
		System.out.println(userMobile);
		System.out.println(CommonUtil.encryptPasswd("123456"));
	}
	
	
	
	/**
	 * 用户发送短信验证码
	 * @param type
	 * @return
	 */
	@ActionKey("/sendMsg4AdminLogin")
	@Before({PkMsgInterceptor.class})
	public void sendMsg4AdminLogin(){
		Message msg = null;
		String mobile = getPara("mobile");
		if(StringUtil.isBlank(mobile)){
			msg = error("01", "手机号为空", "");
			renderJson(msg);
			return;
		}
		OPUserV2 user = opUserService.query4mobile(mobile);
		if(user == null){
			msg = error("02", "用户名不存在", "");
			renderJson(msg);
			return;
		}
		//限制发送频率
		Object object = Memcached.get("msgExpires_" + mobile);
		if(object != null){
			msg = error("03", "您操作太频繁,请稍后再操作!", "");
		}else{
			int msgResult = -9;
			String msgMac = CommonUtil.getMathNumber(6);
			String memcachedKey = "SMS_ADMIN_LOGIN_";
			String msgContent = msgMac + "是您的验证码，您正在使用后台登录功能，若非本人操作，请忽略！【易融恒信】";
			String smsType = "9";
			String smsTypeName = "后台用户登录";
			try {
				msgResult = SMSClient.sendSms(mobile, msgContent);
				if(msgResult != 0){
					msg = error("04", "发送失败", msgResult);
				}else{
					Memcached.set(memcachedKey + mobile , msgMac, 10*60*1000);
					Memcached.set("msgExpires_" + mobile , mobile, 60*1000);
					msg = succ("发送成功", mobile);
				}
				//记录日志
				smsLogService.save(setSMSLog(mobile, msgContent, smsType,smsTypeName, msgResult));
			} catch (Exception e) {
				msg = error("05", "发送失败", e.getMessage());
			}
		}
		renderJson(msg);
	}
	
	/**
	 * 发送后台自动还款短信验证码 WJW
	 */
	@ActionKey("/sendMsgAuthLoan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void sendMsgAuthLoan(){
		Message msg = new Message();
		String userCode = getPara("opCode");
		if(StringUtil.isBlank(userCode)){
			msg = error("", "请登录", null);
			renderJson(msg);
			return;
		}
		OPUserV2 opUserV2 = oPUserV2Service.findById(userCode);
		if(opUserV2 == null){
			msg = error("", "用户不存在", null);
			renderJson(msg);
			return;
		}
		String opMobile = opUserV2.getStr("op_mobile");
		
		String mobile = "";
		try {
			mobile = CommonUtil.decryptUserMobile(opMobile);
		} catch (Exception e1) {
			msg = error("", "手机号错误", null);
			renderJson(msg);
			return;
		}
		
		//限制发送频率
		Object object = Memcached.get("sendMsgAuthLoan_" + mobile);
		if(object != null){
			msg = error("03", "您操作太频繁,请稍后再操作!", "");
		}else{
			int msgResult = -9;
			String msgMac = CommonUtil.getMathNumber(6);//6位验证码
			String msgContent = msgMac + "是您的验证码，您正在使用后台自动还款功能，若误点，请停止操作【易融恒信】";
			String smsType = "18";
			String smsTypeName = "后台自动还款";
			try {
				msgResult = SMSClient.sendSms(mobile, msgContent);
				if(msgResult != 0){
					msg = error("04", "发送失败", msgResult);
				}else{
					Memcached.set("sendMsgAuthLoan_" + mobile , msgMac, 60*1000);//验证码时效1分钟
					msg = succ("发送成功", mobile);
				}
				//记录日志
				smsLogService.save(setSMSLog(mobile, msgContent, smsType,smsTypeName, msgResult));
			} catch (Exception e) {
				msg = error("05", "发送失败", e.getMessage());
			}
		}
		renderJson(msg);
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

}
