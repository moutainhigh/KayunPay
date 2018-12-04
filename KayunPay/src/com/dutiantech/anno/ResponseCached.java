package com.dutiantech.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseCached {
	
	/**
	 * 	缓存数据Key,必须人工设置
	 * @return
	 */
	String cachedKey() ;
	
	/**
	 * 	可使用动态的缓存key，Key可有Request请求参数中获取，多个参数按顺序用“|”分割
	 * 		系统保留参数： 
	 * 			@userCode	获取用户Code
	 * 			@datetime	获取 yyyyMMddHHmmss，用法 @datetime(yyyyMMddHHmmss)
	 * @return
	 */
	String cachedKeyParm() ;
	/**
	 * 	默认有此标签则缓存,默认值true
	 * @return
	 */
	boolean isCached() default true ;	
	/**
	 * 	数据源		
	 * 		local-本地缓存	
	 * 		remote 内存服务器
	 * @return
	 */
	String mode() default "local";
	/**
	 * 	缓存时间，单位秒数，默认60s
	 * @return
	 */
	long time() default 60 ;
	
}
