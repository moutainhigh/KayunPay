package com.dutiantech.plugins;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import com.jfinal.plugin.IPlugin;

public class MemcachedPlugin implements IPlugin{

	MemCachedClient client = null ;
	
	private String name = "local";
	private String address = "127.0.0.1:11211";
	private int weight = 5;
	
	private int initConn = 5 ;
	private int minConn = 5 ; 
	private int maxConn = 500 ;
	private long  maxIdle = 1000 * 60 * 60 * 6;
	private long sleepTime = 30 ;
	
	private int socketTo = 3*1000 ;
	private int connTo = 2*1000 ;
	
	public MemcachedPlugin(String name , String ip , int port , int weight){
		this.name = name ;
		this.address = ip + ":" + port;
		this.weight = weight ;
	}
	
	public String getName(){
		return this.name ;
	}
	
	public void setInitConn(int initConn){
		this.initConn = initConn ;
	}
	
	public void setMinConn( int minConn ){
		this.minConn = minConn ;
	}
	
	public void setMaxConn(int maxConn){
		this.maxConn = maxConn ;
	}
	
	public void setMaxIdle(long maxIdle ){
		this.maxIdle = maxIdle ;
	}
	
	public void setMaintSleep(long sleepTime){
		this.sleepTime = sleepTime ;
	}
	
	public void setSocketTO(int socketTo){
		this.socketTo = socketTo ;
	}
	
	public void setSocketConnectTO(int connTo){
		this.connTo = connTo ;
	}
	
	public boolean start() {
		try{
			SockIOPool pool = SockIOPool.getInstance(); 
			
			// 设置服务器信息 
	        pool.setServers(new String[]{ address }); 
	        pool.setWeights(new Integer[]{ weight }); 
			
			// 设置初始连接数、最小和最大连接数以及最大处理时间 
	        pool.setInitConn( initConn ); 
	        pool.setMinConn( minConn ); 
	        pool.setMaxConn( maxConn ); 
	        pool.setMaxIdle( maxIdle ); 
	        
	        // 设置主线程的睡眠时间 
	        pool.setMaintSleep( sleepTime ); 
	        // 设置TCP的参数，连接超时等 
	        pool.setNagle(false); 
	        pool.setSocketTO( socketTo ); 
	        pool.setSocketConnectTO( connTo ); 
	        
	        // 初始化连接池 
	        pool.initialize(); 
	        
	        client = new MemCachedClient() ;
	        
	        Memcached.addClient( this );
	        
			return true ;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}

	public boolean stop() {
		return true ;
	}
	
	public MemCachedClient getClient(){
		return client ;
	}
	
}
