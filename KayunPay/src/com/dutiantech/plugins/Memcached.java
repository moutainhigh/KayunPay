package com.dutiantech.plugins;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.danga.MemCached.MemCachedClient;

public class Memcached extends HashMap<String , MemcachedPlugin>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 930866266343098145L;
	
	static Memcached clients = null ;
	static MemcachedPlugin lastClient = null ;
	static MemCachedClient client = null ;
	
	static{
		clients = new Memcached() ;
	}
	
	public static void addClient( MemcachedPlugin plugin){
		clients.put( plugin.getName() , plugin ) ;
		lastClient = plugin ;
		client = plugin.getClient() ;
	}

	@SuppressWarnings("static-access")
	public static MemcachedPlugin getClient(String name ){
		return (MemcachedPlugin) clients.get(name) ;
	}
	public static MemcachedPlugin getClient(){
		return lastClient ;
	}
	
	public Memcached use(String name){
		lastClient = getClient(name) ;
		client = lastClient.getClient() ;
		return this ;
	}
	
	private static Date getDate(long time){
		//long to = System.currentTimeMillis() + time ;
		return new Date(time);
	}
	
	/**
	 * 
	 * @param key
	 * @param value
	 * @param time	如果使用腾讯云，才参数只需要是过期的时间即可，自建Memcached则需要补全过期时间节点毫秒数
	 * 		比如10s过期，腾讯云下送 10*1000，自建送 System.currentTimeMillis() + 10*1000 
	 * @return
	 */
	public static boolean set(String key , Object value , long time ){
		Date date = getDate(time) ;
		boolean result = client.set(key, value , date) ;
		debug("set",key , value , date , result );
		return  result ;
	}
	
	public static boolean set(String key , Object value ){
		boolean result = client.set(key, value) ;
		debug("set",key,value , null , result );
		return result ;
	}
	
	public static Object get(String key ){
		Object result = client.get(key) ;
		debug("get",key, "" , null , result );
		return result ;
	}
	
	public static Map<String , Object> gets(String[] keys){
		Map<String , Object> result = client.getMulti(keys) ;
		debug("gets" , keys.toString() , "" , null , result );
		return result  ;
	}
	
	public static boolean delete(String key){
		boolean result = client.delete(key) ;
		debug("delete",key, "" , null , result );
		return result ;
	}
	
	public static boolean delete(String key , long time ){
		Date date = getDate(time);
		boolean result = client.delete(key , date ) ;
		debug("delete",key, "" , date , result );
		return result ;
	}
	
	public static long incr( String key , long inc){
		long result = client.incr(key, inc) ;
		debug("incr",key, inc , null , result );
		return  result ;
	}
	
	public static long decr(String key , long inc){
		long result = client.decr(key, inc);
		debug("decr",key, inc , null , result );
		return  result ;
	}
	
	public static MemCachedClient getMemcachedClient(){
		return client ;
	}
	
	public static long getCounter(String key){
		long result = client.getCounter(key) ;
		debug("getCounter",key, "" , null , result );
		return result ;
	}
	
	public static boolean storeCounter(String key , long inc){
		delete(key) ;	//兼容
		boolean result = client.storeCounter(key, inc) ;
		debug("storeCounter",key, inc , null , result );
		return result ;
	}
	
	public static Integer getInteger(String key ){
		Object obj = get(key); 
		if( obj != null ){
			return Integer.parseInt(obj.toString()) ;
		}
		return null ;
	}
	
	public static Long getLong(String key ){
		Object obj = get(key); 
		if( obj != null ){
			return Long.parseLong(obj.toString()) ;
		}
		return null ;
	}
	
	private static void debug(String op , String key , Object value , Date d ,  Object result ){
		log( String.format("[option=%s][key=%s][value=[%s][date=%s][result=%s]]", op , 
				key , value.toString() , d==null?"0":d.toString() , result ) ) ;
	}
	
	private static void log( Object o ){
		System.out.println("Memcached: " + o.toString());
	}
	
	public static void main(String[] args) {
		MemcachedPlugin plugin = new MemcachedPlugin("test", "192.168.3.104", 11211 , 5 ) ; 
		plugin.start() ;
		
		MemCachedClient c = Memcached.getMemcachedClient();
		String key = "testkey";
		log( c.delete(key));
		log( c.storeCounter( key , 1024 ));
		log( c.incr(key , 10) );
		log( c.getCounter(key));
		
	}
	
}
