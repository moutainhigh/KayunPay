package com.dutiantech.task;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TaskService extends Thread {
	
	protected boolean isRun = true ;
	protected String scKey = "3.14159265358";	//服务密钥
	protected Map<String , String> requestParam = new HashMap<String ,String>() ;
	
	public void stopRun(){
		isRun = false ;
		this.interrupt(); 
	}
	
	public void addParam(String key , String value){
		requestParam.put( key , value ) ;
	}
	
	public String getParamToString(){
		StringBuffer buff = new StringBuffer("");
		Iterator< String> it = requestParam.keySet().iterator() ;
		while(it.hasNext()){
			String key = it.next() ;
			buff.append(key + "=" + requestParam.get(key) + "&") ;
		}
		return buff.toString() ;
	}
	
	public void setSCKey(String key){
		this.scKey = key ;
		requestParam.put("key", scKey);
	}
	
	public String getSCKey(){
		return scKey ;
	}
	
	public TaskService(String serviceName ){
		this.setName(serviceName);
	}
	
	public void run(){
		System.out.println("Default task!");
	}
	
}
