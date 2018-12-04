package com.dutiantech.interceptor;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthRequire;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.model.User;
import com.dutiantech.util.ErrorCode;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class EAuthInterceptor implements Interceptor {
	
	public static int INVALIDTIME = 1800*4 ;	//单位秒
	public static int APPINVALIDTIME = 60*60*24*30 ;	//单位秒
	public static String USERCODEKEY = "userCode";
	public static String TOKENKEY = "token" ;
	public static String COOKIE_NAME = "d3e5feae0a8c7b3e_" + AdminConfig.workType;
	public static String IS_HEADER_TOKEN_KEY = "isHeaderToken";
	
	static{
		if( AdminConfig.workType == 0 ){
			INVALIDTIME = 1800*4 ;
		}
	}
	
	public void intercept(Invocation inv) {
		BaseController controller = (BaseController) inv.getController();
		HttpServletRequest request = controller.getRequest();
//		HttpServletResponse response = controller.getResponse();
		String token = controller.getCookie(COOKIE_NAME);
		boolean isHeaderToken = false;
		if (StringUtil.isBlank(token)) {
			token = request.getHeader(EAuthInterceptor.TOKENKEY);
			isHeaderToken = true;
		}
		request.setAttribute(IS_HEADER_TOKEN_KEY, isHeaderToken);
		String userCode = "";
		String requestIp = "";
		Message msg = null;
		
		if (msg == null) {
			try {
				requestIp = BaseController.getRequestIP(request);
				// 验证并获取用户userCode
				userCode = UserUtil.UserDeCode(token, requestIp, UserUtil.defaultEnCodeKey);
				// 根据userCode，返回用户(User)实例
				User user = User.userDao.findById(userCode);
				
				boolean authAccept = false;	// 鉴权结果，默认为失败
				Method method = inv.getMethod();	// 当前访问的Action
				
				/**
				 * 解析鉴权模式：
				 * 1） 先判断Action是否要求鉴权；
				 * 2） 如果没有，则继续判断Controller是否要求鉴权；
				 * 3） 如果仍然没有，则默认允许匿名访问
				 */
				AuthMode authMode = new AuthMode();	// 鉴权模式对象封装
				getMethodAuthMode(authMode, method, controller.getClass());
				
				if (authMode.authCode == ErrorCode.REQ_GUEST) {	// 允许匿名
					authAccept = true;	// 鉴权通过
				} else if (user != null) {	// 不允许 匿名，且当前已登录
					switch (authMode.authCode) {
					case REQ_ROLE:	// 根据单个角色进行鉴权
						if (user.checkRole(authMode.authId)) authAccept = true;
						break;
					case REQ_ROLES:	// 根据多个角色进行鉴权
						if (user.checkRoles(authMode.authIds)) authAccept = true;
						break;
					case REQ_PERM:	// 根据单个权限进行鉴权
						if (user.checkPerm(authMode.authId)) authAccept = true;
						break;
					case REQ_PERMS:	// 根据多个权限进行鉴权
						if (user.checkPerms(authMode.authIds)) authAccept = true;
						break;
					case REQ_LOGIN:	// 根据用户的登录状态进行鉴权
						authAccept = true;
						break;
					default:
						break;
					}
				}
				
				if (!authAccept) {	// 身份认证失败
					controller.renderError(authMode.authCode, "身份认证失败");
					return;
				} else {
					inv.invoke();
				}
				
				inv.invoke();
				msg = inv.getReturnValue();
				
				
			} catch (Exception e) {
				// TODO: handle exception
			}
		} 
		
		if (msg != null) {
			inv.setReturnValue(msg);
			//此部分代码兼容未假如 PkMsgInterceptor的情况
			//仅有异常情况是，该部分才生生效
			if( msg.getReturn_code().equals("AR") == false ){
				token = UserUtil.UserEnCode( userCode , requestIp , UserUtil.defaultEnCodeKey ) ;
				if( isHeaderToken )
					msg.setToken( token );
				controller.setCookieByHttpOnly( COOKIE_NAME , token , INVALIDTIME );
			}
			controller.renderJson( msg );
		}
	}
	
	/**
	 * 优先进行Action层面的鉴权
	 * @param mode
	 * @param method
	 * @param ctrl
	 */
	private void getMethodAuthMode(AuthMode mode, Method method, Class<?> ctrl) {
		if (method.isAnnotationPresent(AuthRequire.Role.class)) {
			mode.authCode = ErrorCode.REQ_ROLE;
			mode.authId = method.getAnnotation(AuthRequire.Role.class).value();
		} else if (method.isAnnotationPresent(AuthRequire.Roles.class)) {
			mode.authCode = ErrorCode.REQ_ROLES;
			mode.authIds = method.getAnnotation(AuthRequire.Roles.class).value();
		} else if (method.isAnnotationPresent(AuthRequire.Perm.class)) {
			mode.authCode = ErrorCode.REQ_PERM;
			mode.authId = method.getAnnotation(AuthRequire.Perm.class).value();
		} else if (method.isAnnotationPresent(AuthRequire.Perms.class)) {
			mode.authCode = ErrorCode.REQ_PERMS;
			mode.authIds = method.getAnnotation(AuthRequire.Perms.class).value();
		} else if (method.isAnnotationPresent(AuthRequire.Logined.class)) {
			mode.authCode = ErrorCode.REQ_LOGIN;
		} else if (method.isAnnotationPresent(AuthRequire.Guest.class)) {
			mode.authCode = ErrorCode.REQ_GUEST;
		} else {
			getControllerAuthMode(mode, ctrl);
		}
	}
	
	/**
	 * 进行Controller层面的鉴权，只有当Action未设置时有效
	 * @param mode
	 * @param ctrl
	 */
	private void getControllerAuthMode(AuthMode mode, Class<?> ctrl) {
		if (ctrl.isAnnotationPresent(AuthRequire.Role.class)) {
			mode.authCode = ErrorCode.REQ_ROLE;
			mode.authId = ctrl.getAnnotation(AuthRequire.Role.class).value();
		} else if (ctrl.isAnnotationPresent(AuthRequire.Roles.class)) {
			mode.authCode = ErrorCode.REQ_ROLES;
			mode.authIds = ctrl.getAnnotation(AuthRequire.Roles.class).value();
		} else if (ctrl.isAnnotationPresent(AuthRequire.Perm.class)) {
			mode.authCode = ErrorCode.REQ_PERM;
			mode.authId = ctrl.getAnnotation(AuthRequire.Perm.class).value();
		} else if (ctrl.isAnnotationPresent(AuthRequire.Perms.class)) {
			mode.authCode = ErrorCode.REQ_PERMS;
			mode.authIds = ctrl.getAnnotation(AuthRequire.Perms.class).value();
		} else if (ctrl.isAnnotationPresent(AuthRequire.Logined.class)) {
			mode.authCode = ErrorCode.REQ_LOGIN;
		} else {
			mode.authCode = ErrorCode.REQ_GUEST;
		}
	}

	/**
	 * 鉴权模式封装
	 * @author SToNE
	 *
	 */
	class AuthMode {
		private ErrorCode authCode = null;	// 各种鉴权模式枚举
		private String authId = null;	// 单条鉴权标识（单角色、单权限）
		private String[] authIds = null;	// 多条鉴权标识（多角色、多权限）
	}
}

















