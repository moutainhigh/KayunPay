package com.dutiantech.interceptor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

public class AuthInterceptor implements Interceptor {

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
		
		BaseController controller = (BaseController) inv.getController() ;
		HttpServletRequest request = controller.getRequest() ;
		String token = controller.getCookie( COOKIE_NAME );
		boolean isHeaderToken = false ;
		if( StringUtil.isBlank(token) == true ){
			token = request.getHeader( AuthInterceptor.TOKENKEY ) ;
			isHeaderToken = true ;
		}
		request.setAttribute( IS_HEADER_TOKEN_KEY , isHeaderToken );
		String userCode = "";
		String requestIp = "";
//		token = "03b2267e5ab07b26420d8175756d6366539850a44e822c43c7718c4f52dd64c388f293faa5970bfab1bff9b8d1a1b17adc2e6adff12bbe4db865215da81f6e2e";
		Message msg = null ;
		
		//check referer
//		String referer=request.getHeader("Referer");
//		if( AdminConfig.isDevMode == false ){
//			if( referer.indexOf( "yrhx.com" ) < 0  )
//				msg = Message.error("AR", "NO ACCESS!", null ) ;
//		}
		if ( msg == null ){
			try{
				requestIp = BaseController.getRequestIP(request); 
				userCode = UserUtil.UserDeCode( token ,  requestIp , UserUtil.defaultEnCodeKey  ) ;
				request.setAttribute( AuthInterceptor.USERCODEKEY , userCode );
				//check guest auth
				if( isHasAuth(inv.getMethod() , userCode) == false ){
					msg = Message.error( "AC", "No guest auth!", null ) ;
				}else{
					//invoke
					inv.invoke();
					msg = inv.getReturnValue() ;
				}
			}catch(Exception e ){
				if( e instanceof BaseBizRunTimeException ){
					msg = ((BaseBizRunTimeException)e).getExMsg() ;
				}else{
					msg = Message.error("BC", "Exception:" + e.getMessage() , null ) ;
				}
				e.printStackTrace(); 
			}
		}
		if( msg != null ){
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
//		controller.renderJson( msg );
		
	}
	
	//true 
	private boolean isHasAuth(Method method , String userCode ){ 
		try{
			AuthNum authNum = method.getAnnotation( AuthNum.class ) ;
			if( authNum == null ){
				return false ;
			}
			
			if( authNum.value() == 999 ){
				return true ;
			}
			
			Object obj = CACHED.get( OPUserV2.USER.OPMAP.key()  + userCode ) ;
			if( obj == null )
				return false ;
			
			char[] authMap = obj.toString().toCharArray() ; //to char[]
			
			char isHasAuth = authMap[ authNum.value() ] ;
			return isHasAuth=='1' ;
			
		}catch(Exception e){
			return false ;
		}
	}
	

}
