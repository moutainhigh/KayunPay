 package com.dutiantech;

import java.util.HashMap;
import java.util.Map;

import com.dutiantech.plugins.Memcached;

public class CACHED {
	
	private static Map<String , Object> CACHEDMAP = new HashMap<String , Object>() ;
	@SuppressWarnings("unused")
	private static Map<String , Long > CACHEDTIME = new HashMap<String , Long>() ; 

	
	/**
	 * 	添加缓存，切记，本地缓存最多30000条，否则会溢出，并且影响效率，如果是用户运行时数据，必须做远程缓存
	 * 		值为null的数据不做缓存
	 * @param key
	 * @param value
	 * @param isRemote
	 * @param time		过期时间，秒数,每个字段必须有过期时间
	 */
	public static void put(String key , Object value , boolean isRemote , long time){
		if( value != null ){
			long lostTime = time*1000 ;
			//生产模式，腾讯云Memcached时间参数兼容
			//测试模式下补全毫秒数，生产模式不需要
			/*	关闭腾讯云Memcached模式，改用自建，2016-01-27
			if( AdminConfig.isDevMode == true ){
				lostTime = System.currentTimeMillis() + lostTime ;
			}
			*/
			lostTime = System.currentTimeMillis() + lostTime ;
			
			if( isRemote == true ){
				//远程存储
				Memcached.set(key, value , lostTime) ;
			}else{
				CACHEDMAP.put(key, new Data(value , time*1000 ));
//				CACHED.put(key, value);
//				CACHEDTIME.put(key, lostTime ) ;
			}
		}
	}

	public static Object get(String key , boolean isRemote ){
		Object obj = null ;
		if( isRemote == true ){
			obj = Memcached.get(key) ;
		}else{
			Data tmpData = (Data)CACHEDMAP.get(key) ;
			if( tmpData != null ){
				if( tmpData.isTimeout() == true ){
					CACHEDMAP.remove(key) ;
				}else{
					obj = tmpData.getValue();
				}
				
			}
		}
		return obj ;
	}
	
	public static void remove(String key , boolean isRemote){
		if( isRemote == true ){
			Memcached.delete(key) ;
		}else{
			CACHEDMAP.remove(key) ;
		}
	}
	
	public static void put(String key , Object obj){
		if( obj != null ){
			CACHEDMAP.put(key, obj) ;
			Memcached.set(key, obj) ;
		}
	}
	
	public static void put(String key , Object obj , boolean isRemote){
		if( isRemote == true ){
			put(key , obj);
		}else{
			CACHEDMAP.put(key, obj) ;
		}
	}
	
	public static Object get(String key ){
		Object obj = CACHEDMAP.get(key);
		if( obj == null ){
			obj = Memcached.get(key);
			if( obj != null )
				CACHEDMAP.put(key , obj );
		}
		return obj ;
	}
	
	public static String getStr(String key ){

		Object obj = get( key );
		if( obj == null )
			return "" ;
		return (String)obj ;
	}
	
	public static int getInt(String key){
		Object obj = get( key );
		if( obj == null )
			return 0 ;
		return Integer.parseInt( (String) obj ) ;
	}
	
}
