package com.dutiantech.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Log;
import com.dutiantech.Message;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.model.BizLog;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.User;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BaseService;
import com.dutiantech.tcsec.CSECAPI;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.ErrorCode;
import com.dutiantech.util.StringUtil;
import com.jfinal.core.ActionException;
import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.DbKit;
import com.jfinal.render.ContentType;

public class BaseController extends Controller {
	
	protected static Map<String , Object> SERVICES = new HashMap<String , Object>();
	private HttpServletRequest request;
	protected Paginate getPaginate(){
		Paginate pag = new Paginate() ;

		Integer pageNumber = getParaToInt("pageNumber", 1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize", 10);
		
		pag.setPageNum(pageNumber);
		pag.setPageSize(pageSize);
		
		return pag ;
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T getService(final Class<? extends BaseService> cls){
		String clsName = cls.getName() ;
		Object obj = SERVICES.get(clsName) ;
		if( obj == null ){
			try {
				obj = cls.newInstance() ;
				//System.out.println("new----");
				SERVICES.put(clsName, obj) ;
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		return ((T)obj) ;
	}

	protected void BIZ_LOG_ERROR(String uCode , BIZ_TYPE type , String bizContent , Exception e) {
		String emsg = "";
		if( e != null )
			emsg = e.getMessage();
		bizLog(uCode, type, "E", bizContent + " " + emsg);
	}
	
	protected void BIZ_LOG_INFO(String uCode , BIZ_TYPE type , String bizContent ) {
		bizLog(uCode, type, "I", bizContent);
	}
	protected void BIZ_LOG_INFO(String uCode , BIZ_TYPE type , String bizContent ,String bizData) {
		bizLog(uCode, type, "I", bizContent, bizData);
	}
	protected void BIZ_LOG_WARN(String uCode , BIZ_TYPE type , String bizContent ) {
		bizLog(uCode, type, "W", bizContent);
	}
	
	protected void BIZ_LOG_DEBUG(String uCode , BIZ_TYPE type , String bizContent ) {
		if( AdminConfig.isDevMode == true )
			bizLog(uCode, type, "D", bizContent);
	}
	
	protected void bizLog(String uCode , BIZ_TYPE type , String bizLevel , String bizContent ){
		BizLog bizLog = new BizLog();
		bizLog.set("userCode", uCode ) ;
		bizLog.set("bizLevel", bizLevel );
		bizLog.set("opType", type.key());
		bizLog.set("bizTitle", type.desc());
		bizLog.set("bizContent", bizContent);
//		Enumeration<String> em = getRequest().getHeaderNames();
//		while(em.hasMoreElements() ) {
//			String key = em.nextElement() ;
//			System.out.println(key + "  " + getRequest().getHeader(key));
//		}
		bizLog.set("bizFrom", getRequest().getHeader("Referer"));
		bizLog.set("httpInfo", getRequest().getHeader("User-Agent"));
		bizLog.set("bizData", getRequest().getQueryString() ) ;
		new com.dutiantech.service.asyn.BizLogService(bizLog).run(); 
	}
	protected void bizLog(String uCode , BIZ_TYPE type , String bizLevel , String bizContent ,String bizData){
		BizLog bizLog = new BizLog();
		bizLog.set("userCode", uCode ) ;
		bizLog.set("bizLevel", bizLevel );
		bizLog.set("opType", type.key());
		bizLog.set("bizTitle", type.desc());
		bizLog.set("bizContent", bizContent);
//		Enumeration<String> em = getRequest().getHeaderNames();
//		while(em.hasMoreElements() ) {
//			String key = em.nextElement() ;
//			System.out.println(key + "  " + getRequest().getHeader(key));
//		}
		bizLog.set("bizFrom", getRequest().getHeader("Referer"));
		bizLog.set("httpInfo", getRequest().getHeader("User-Agent"));
		bizLog.set("bizData", bizData ) ;
		new com.dutiantech.service.asyn.BizLogService(bizLog).run(); 
	}
	
	public void forward(String url , boolean isAllowFrame){

		try {
//			render(new ActionRender(url));
			forwardAction(url); 
			if( isAllowFrame == false )
				getResponse().addHeader("X-Frame-Options", "SAMEORIGIN");
			getRequest().getRequestDispatcher(url).forward(getRequest(), getResponse());
		} catch (Exception e) {
			Log.error("未找到跳转URL：" + url , e);
		} ;
		//forwardAction("pages/loan-finish-list.html");
	}
	
	public void forward(String url){
		forward(url, false );
	}
	
	public Message succ( String return_info , Object return_data ){
		return Message.succ(return_info, return_data) ;
	}
	
	public JSONObject succ4json(String return_info, Object return_data) {
		return Message.succ4Json(return_info, return_data);
	}
	
	public Message error(String return_code ,String return_info , Object return_data ) {
		try {
			StackTraceElement[] stack = new Throwable().getStackTrace();
	        Method method = this.getClass().getMethod(stack[1].getMethodName());
	        for(Annotation an : method.getAnnotations()){
	        	int x = an.toString().indexOf("com.jfinal.plugin.activerecord.tx.Tx");
	        	if(x!=-1 && x >0){
	        		DbKit.getConfig().getConnection().rollback();
	        	}
	        }
		} catch (Exception e) {
			//e.printStackTrace(); 
			//throw new BaseBizRunTimeException("-100", "回滚事务时发生错误", e.getMessage());
		}
		return  Message.error( return_code , return_info, return_data) ;
	}
	
	public JSONObject error4json(String return_code, String return_info, Object return_data) {
		try {
			StackTraceElement[] stack = new Throwable().getStackTrace();
	        Method method = this.getClass().getMethod(stack[1].getMethodName());
	        for(Annotation an : method.getAnnotations()){
	        	int x = an.toString().indexOf("com.jfinal.plugin.activerecord.tx.Tx");
	        	if(x!=-1 && x >0){
	        		DbKit.getConfig().getConnection().rollback();
	        	}
	        }
		} catch (Exception e) {
			//e.printStackTrace(); 
			//throw new BaseBizRunTimeException("-100", "回滚事务时发生错误", e.getMessage());
		}
		return Message.error4Json(return_code, return_info, return_data);
	}
	
	/**
	 * 	检查callback参数，防止放射xss
	 * @param callback
	 * @return
	 */
	private boolean checkCallback(String callback){
		try {  
	        for (byte c : callback.getBytes("US-ASCII")) {  
	            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_')  
	            {  
	                continue;  
	            } else {  
	                return false;  
	            }  
	        }  
	        
	        if( callback.indexOf("jQuery") < 0 ){
	        	return false ;
	        }
	        
	        return true;  
	    } catch (Throwable t) {  
	        return false;  
	    } 
	}
	
	/**
	 * 	JSONP 兼容
	 * @param callback
	 * @param json
	 */
	public void renderJSONP(String callback  , Object json ){
		if( checkCallback(callback) == false ){
			renderText( "alert('不好玩!');" , ContentType.JAVASCRIPT );
		}else{
			renderText( callback + "(" + JsonKit.toJson( json ) + ");" , ContentType.JAVASCRIPT );
		}
	}
	
	/**
	 * 获取UserCode
	 * @return
	 */
	public String getUserCode(){
		Object obj = getRequest().getAttribute(AuthInterceptor.USERCODEKEY ) ;
		if( obj == null )
			return null ;
		return (String) obj ;
	}
	
	public String getFuiouLoginId() {
		String userCode = getUserCode();
		if (userCode != null) {
			Object obj = Memcached.get("PORTAL_USER_" + userCode);
			if (obj != null) {
				try {
					return CommonUtil.decryptUserMobile(((User)obj).getStr("loginId"));
				} catch (Exception e) {
				}
			}
		}
		return null;
	}
	
	public User getUser(){
		String userCode = getUserCode() ;
		if( userCode != null ){
			Object obj = Memcached.get("PORTAL_USER_" + userCode );
			if( obj != null ){
				return (User)obj ;
			}
		}
		return null ;
	}
	
	/**
	 * 	前端送的参数必须包含
	 * 			ticket
	 * 			buid
	 * 			sid
	 * @return
	 */
	public Message checkCapTicket(String cookieKey ){
		Message msg = null ;
		int buid = 11 ;
		int sid = 11 ;
		int uid = 1 ;
		
		String capTicket = getPara("ticket","") ;
		if(StringUtil.isBlank(capTicket) == true ){
			return error("33", "异常登录,重新登录", null ) ;
		}
		
		//验证码验证
//		String capCookieValue = getCookie(cookieKey ,"");
//		if( StringUtil.isBlank(capCookieValue) == true ){
//			return error("31", "异常登录,重新登录", null ) ;
//		}
		
//		Object cKey = CACHED.get(capCookieValue, true) ;
//		if( cKey == null ){
//			return error("32", "异常登录,重新登录", null ) ;
//		}else{
//			String[] tmpValue = ((String)cKey).split("\\|");
//			buid = Integer.parseInt(tmpValue[0]);
//			sid = Integer.parseInt(tmpValue[1]);
//		}

		buid = getParaToInt("buid",0);
		sid = getParaToInt("sid",0);
		if( buid == 0 ){
			return error("31", "缺少buid", null ) ;
		}
		
		if( sid == 0 ){
			return error("32", "缺少sid", null ) ;
		}
		
		boolean checkResult = CSECAPI.checkTicket( capTicket , getRequestIP() , uid, buid, sid ) ;
		if( checkResult == false ){
			msg = error("34", "验证码错误", null ) ;
		}else{
		}
		
		return msg ;
	}
	
	public OPUserV2 getUserInfo(){
		//CACHED.put( OPUserV2.USER.OPCODE.key() + userCode , user );
		String userCode = getUserCode() ;
		Object obj = CACHED.get( OPUserV2.USER.OPCODE.key() + userCode ) ;
		if( obj == null )
			throw new BaseBizRunTimeException("AX", "未登录或者登录过期!", userCode ) ;
		
		return (OPUserV2)obj;
	}
	
	public String getRequestIP(){
		return getRequestIP( getRequest() ) ;
	}
	
	public static String getRequestIP( HttpServletRequest request ){
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		if (ip.equals("0:0:0:0:0:0:0:1")) {
			return "127.0.0.1";
		}
		return ip;
	}
	
	public String getRequestString4stream() {
		StringBuffer buff = new StringBuffer();

		HttpServletRequest request = getRequest() ;
		try {
			ServletInputStream is = request.getInputStream() ;
			BufferedReader br = new BufferedReader(new InputStreamReader(is,"utf-8") );
			String line = null ;
			while( (line = br.readLine()) != null ){
				buff.append(line) ;
			}
		} catch (IOException e) {
			e.printStackTrace(); 
			return ""; 
		}
		return buff.toString() ;
	}
	
	public void output(Object o){
		renderJson( o ); 
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param maxAge	单位秒
	 */
	public void setCookieByHttpOnly(String key , String value , int maxAge){
		Cookie cookie = new Cookie(key, value) ;
//		cookie.setDomain("/");
		cookie.setPath("/");
		cookie.setHttpOnly(true);
		if( maxAge > 0 )
			cookie.setMaxAge(maxAge);
		setCookie(cookie) ;
	}
	
	public void renderJson(Message msg ){
		String callback = getPara("callback") ;
		getResponse().addHeader("X-Frame-Options", "SAMEORIGIN");
		if( StringUtil.isBlank(callback) == true ){
			renderJson((Object)msg);
		}else{
			renderJSONP(callback, msg); 
		}
	}
	
	/**
	 * 	参数名必须是PageSize
	 * @return
	 */
	public int getPageSize(){
		int pageSize = getParaToInt("pageSize",20) ;
		if( pageSize > 20 )
			pageSize = 20 ;
		return pageSize;
	}
	
	/**
	 * 	参数名必须是pageNumber
	 * @return
	 */
	public int getPageNumber(){
		int pageNumber = getParaToInt("pageNumber",1) ;
		if(pageNumber <= 0){
			pageNumber = 1;
		}
		return pageNumber;
	}
	
	@SuppressWarnings("unused")
	public String getRequestAgent(){
		String userAgent = getRequest().getHeader("User-Agent");
		String[] keywords = {"Android","iPhone","iPad","Windows Phone"};
		
		if( userAgent.contains("Windows NT") || userAgent.contains("MSIE") ){
			return "PC";
		}
		
		if( userAgent.contains("MicroMessenger") ){
			return "WX";
		}
		
		return "";
	}
	
	public String getPara(String name) {
		request=getRequest();
		String result= request.getParameter(name);
//		if(!StringUtil.isBlank(result)){
//			result=result.replaceAll("([';]+|(--)+)", "");
//		}
		if(!StringUtil.isValid(result)){
			throw new BaseBizRunTimeException("B11", "输入非法", null ) ;
		}
		return result;
	}
	public String getPara(String name, String defaultValue) {
		request=getRequest();
		String result = request.getParameter(name);
//		if(!StringUtil.isBlank(result)){
//			result=result.replaceAll("([';]+|(--)+)", "");
//		}
		if(!StringUtil.isValid(result)){
			throw new BaseBizRunTimeException("B11", "输入非法", null ) ;
		}
		return result != null && !"".equals(result) ? result : defaultValue;
	}
	
	public void renderError(ErrorCode error, String errorDesc) {
		renderJson(error(error.val(), errorDesc, null));
	} 
}


