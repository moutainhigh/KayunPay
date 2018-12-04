package com.dutiantech.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.util.DESUtil;
import com.dutiantech.util.UIDUtil;

public class UserUtil {

	public static String defaultEnCodeKey = "selangshiwo.com" ;
	/**
	 * 	用户Cookie，用于身份处理
	 * @param userCode
	 * @param enCodeKey
	 * @return
	 * 		返回 null 则加密处理失败，否则返回一个字符串 char>96
	 */
	public static String UserEnCode(String userCode , String addrip , String enCodeKey ){
		
		if( StringUtil.isBlank(enCodeKey) ){
			enCodeKey = UserUtil.defaultEnCodeKey ;
		}
		long nowTime = System.currentTimeMillis() ;
		
		StringBuffer buff = new StringBuffer() ;
		buff.append(userCode + "|" ) ;
		buff.append(addrip.trim() + "|" ) ;
		buff.append( nowTime + "|" ) ;
		//buff.append( enCodeKey  + "|" ) ;
//		String md5Value = "eb745998594d9813" ;
		//不浪费CPU
//		try {
//			md5Value = MD5Code.md5( buff.toString() , enCodeKey ) ;
//		} catch (Exception e) {
//			//兼容出错情况
//			md5Value = "eb745998594d9813" ;
//			e.printStackTrace(); 
//		}
//		buff.append( md5Value ) ;
		String desValue = null ;
		try {
			desValue = DESUtil.encode4string( buff.toString() ,  enCodeKey ) ;
		} catch (Exception e) {
			e.printStackTrace();
			desValue = null ;
		}
		
		return desValue ;
	}
	
	/**
	 * 	用户Cookie，用于身份处理
	 * @param userCode
	 * @param addrip
	 * @param exTime	单位秒
	 * @param enCodeKey
	 * @return
	 * 		返回 null 则加密处理失败，否则返回一个字符串 char>96
	 */
	public static String UserEnCode(String userCode , String addrip , long exTime , String enCodeKey ){
		
		if( StringUtil.isBlank(enCodeKey) ){
			enCodeKey = UserUtil.defaultEnCodeKey ;
		}
		long nowTime = System.currentTimeMillis() + exTime*1000 ;
		
		StringBuffer buff = new StringBuffer() ;
		buff.append(userCode + "|" ) ;
		buff.append(addrip.trim() + "|" ) ;
		buff.append( nowTime + "|" ) ;
		//buff.append( enCodeKey  + "|" ) ;
//		String md5Value = "eb745998594d9813" ;
		//不浪费CPU
//		try {
//			md5Value = MD5Code.md5( buff.toString() , enCodeKey ) ;
//		} catch (Exception e) {
//			//兼容出错情况
//			md5Value = "eb745998594d9813" ;
//			e.printStackTrace(); 
//		}
//		buff.append( md5Value ) ;
		String desValue = null ;
		try {
			desValue = DESUtil.encode4string( buff.toString() ,  enCodeKey ) ;
		} catch (Exception e) {
			e.printStackTrace();
			desValue = null ;
		}
		
		return desValue ;
	}
	
	/**
	 * 	如果验证正常，则返回 userCode(32)
	 * @param enCodeValue	该值由UserEnCode产生
	 * @param deCodeKey		应为使用的DES加密，所以解密密钥和加密密钥一致
	 * @return
	 * 		正常 返回32位用户编号
	 * 		异常	返回null
	 */
	@SuppressWarnings("unused")
	public static String UserDeCode(String enCodeValue ,String ip ,  String deCodeKey ){
		if( StringUtil.isBlank(deCodeKey) ){
			deCodeKey = UserUtil.defaultEnCodeKey ;
		}
		String uCode = null ;
		try {
			String desValue = DESUtil.decode4string( enCodeValue ,  deCodeKey ) ;
			
			String[] fields = desValue.split("\\|") ;
			String addrip = fields[1] ;
			
//			if( addrip.equals( ip.trim() ) == false ){
//				throw new BaseBizRunTimeException("AP", "非法登陆状态", addrip + "  " + ip) ;
//			}
			
			long exTime = Long.valueOf( fields[2] ) + AuthInterceptor.INVALIDTIME * 1000 ;
			long nowTime = System.currentTimeMillis() ;
			
//			System.out.println("时间差：" + (exTime - nowTime)/1000 + "s,exTime:" + exTime + ",nowTime:"+nowTime );
			
			if( exTime < nowTime ){
				throw new BaseBizRunTimeException("AT", "登陆超时,请重新登陆", null ) ;
			}
			
			uCode = fields[ 0 ] ;
			if( StringUtil.isBlank( uCode ) )
				return null ;
			
		} catch (Exception e) {
			//e.printStackTrace();
			uCode = null ;
		}
		
		return uCode  ;
	}
	@SuppressWarnings("unused")
	public static String UserDeCodeForApp(String enCodeValue ,String ip ,  String deCodeKey ){
		if( StringUtil.isBlank(deCodeKey) ){
			deCodeKey = UserUtil.defaultEnCodeKey ;
		}
		String uCode = null ;
		try {
			String desValue = DESUtil.decode4string( enCodeValue ,  deCodeKey ) ;
			
			String[] fields = desValue.split("\\|") ;
			String addrip = fields[1] ;
			
//			if( addrip.equals( ip.trim() ) == false ){
//				throw new BaseBizRunTimeException("AP", "非法登陆状态", addrip + "  " + ip) ;
//			}
			
			long exTime = Long.valueOf( fields[2] ) + AppInterceptor.APPINVALIDTIME * 1000 ;
			long nowTime = System.currentTimeMillis() ;
			
//			System.out.println("时间差：" + (exTime - nowTime)/1000 + "s,exTime:" + exTime + ",nowTime:"+nowTime );
			
			if( exTime < nowTime ){
				throw new BaseBizRunTimeException("AT", "登陆超时,请重新登陆", null ) ;
			}
			
			uCode = fields[ 0 ] ;
			if( StringUtil.isBlank( uCode ) )
				return null ;
			
		} catch (Exception e) {
			//e.printStackTrace();
			uCode = null ;
		}
		
		return uCode  ;
	}
	public static String doCallback(String callback ){
		Pattern pattern = Pattern.compile("[0-9]*");
		String temp = callback.replace("jQuery", "").replace("_", "" ) ;
		Matcher isNum = pattern.matcher( temp );

        if(isNum.matches()) {
        	return callback ;
        } else {
    		return "callback" ;
        }
	}
	
	public static void main(String[] args ) throws InterruptedException {
		
		String ip = "192.168.3.143" ;
		String uCode = UIDUtil.generate() ;
		
		System.out.println( "UserCode : " + uCode  );
		
		String desValue = UserUtil.UserEnCode(uCode, ip,  UserUtil.defaultEnCodeKey ) ;

		System.out.println( "DesValue : " + desValue  ) ;
		System.out.println( "DesValueLength : " + desValue.length()  ) ;
		
		String newCode = UserUtil.UserDeCode( "060191c6e84c0068422fee81601301f32d970e6ee8ceb2c5be5cefe452dd26a1e10752ee0edd3f1551aa1e154d08fbaeaec4ab0643cc6ba1c2bbbe34359aa62f" , ip , UserUtil.defaultEnCodeKey ) ;
		
		System.out.println( "NewCode : " + newCode );
		
	}
	
}
