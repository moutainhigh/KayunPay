package com.dutiantech.interceptor;

import javax.servlet.http.HttpServletRequest;

import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * 	打包统一响应处理
 * 		该拦截器必须是最后一个Interceptor,否则会阻拦后面的interceptor执行
 * 		该拦截器提供Cached功能，可以对return_code为00的请求进行Cached，Cached可在本地内存，也可在远程内存
 * @author five
 */
public class PkMsgInterceptor implements Interceptor{

	@SuppressWarnings("unused")
	@Override
	public void intercept(Invocation inv) {

		BaseController controller = (BaseController) inv.getController() ;
		HttpServletRequest request = controller.getRequest() ;
		String userCode = controller.getUserCode() ;
		String cachedKey = "";
		boolean isRemote = false ;
		
		Message msg = null ;
		
		//cached
		ResponseCached cached = inv.getMethod().getAnnotation( ResponseCached.class ) ;
		if( AdminConfig.isDevMode == true )
			cached = null ;
		if( cached != null ){
			//has cached config
			//设置缓存字段名称
			cachedKey = cached.cachedKey() ;
			String tmpKeyParams = cached.cachedKeyParm() ;
			if( StringUtil.isBlank(tmpKeyParams) == false ){
				String[] tmpParams = tmpKeyParams.split("\\|") ;
				for( String tp : tmpParams ){
					if(StringUtil.isBlank(tp) == false ){
						if( tp.charAt(0) == '@'){
							//发现常量是否
							if( tp.equals("@userCode") == true ){
								cachedKey += controller.getUserCode() ;
							}
							
							if( tp.indexOf("@datetime") == 0){
								//datetime
								String timeRex = StringUtil.getFirstParam(tp) ;
								cachedKey += DateUtil.getStrFromNowDate( timeRex );
							}
							
						}else{
							//request 变量
							cachedKey += controller.getPara( tp )+"|";
						}
					}
				}
			}
			
			//设置缓存数据源
			isRemote = "local".equals( cached.mode())? false : true ;
			//检查缓存区是否有需要的数据
			msg = get4cached( cachedKey , isRemote );
		}
		
		if( msg == null ){
			//缓存内无数据
			try{
				inv.invoke(); 	//最后调用
				msg = inv.getReturnValue() ;
			}catch(Exception e){
				if( AdminConfig.isDevMode == true ){
					e.printStackTrace();
				}
				if( e instanceof BaseBizRunTimeException ){
					msg = ((BaseBizRunTimeException)e).getExMsg() ;
				}else{
					msg = Message.error("BB", "运行异常"  , null) ;
					System.out.println(e);
				}
			}
			
			if( cached != null ){
				//如果缓存内有数据，并且return_code=="00"则进行Cached
				put4cached(cachedKey, msg, isRemote, cached.time() );
			}
		}
		
		if( msg == null ){
			//异常处理
			msg = Message.error("BA", "无返回消息", null ) ;
		}else{
			if(StringUtil.isBlank( userCode ) == false ){
				String requestIp = BaseController.getRequestIP(controller.getRequest() ); 
				if( msg == null )
					msg = Message.error("CB", "异常情况!", null ) ;
				String token = UserUtil.UserEnCode( userCode , requestIp , UserUtil.defaultEnCodeKey );
				boolean isHeaderToken = (boolean) request.getAttribute( AuthInterceptor.IS_HEADER_TOKEN_KEY );
				if( isHeaderToken == true )
					msg.setToken( token );
				controller.setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );
			}
			
			//渲染结果
			controller.renderJson(msg); 
		}
		
	}
	
	private void put4cached(String key , Message msg ,boolean isRemote, long time ){
		if( msg != null ){
			if( "00".equals( msg.getReturn_code()) == true )
				CACHED.put(key, msg, isRemote, time);
		}
	}

	private Message get4cached(String key , boolean isRemote ){
		Object obj = CACHED.get(key , isRemote ) ;
		if( obj != null ){
			return (Message)obj ;
		}
		return null ;
	}
	
}
