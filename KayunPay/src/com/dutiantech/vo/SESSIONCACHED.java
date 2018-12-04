package com.dutiantech.vo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SESSIONCACHED implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 81371980899111766L;
	
	private String AUTH_MAP_KEY = "authMap" ;
	
	private Map<String , Object> session = null ;
	
	public SESSIONCACHED(){
		setSession(new HashMap<String , Object> ()) ;
	}
	
	public SESSIONCACHED genCache(){
		return new SESSIONCACHED() ;
	}
	
	public void setAuthMap(String authMap){
		session.put( AUTH_MAP_KEY , authMap ) ;
	}
	
	public char[] getAuthMap(){
		return session.get( AUTH_MAP_KEY ).toString().toCharArray() ;
	}

	public Map<String , Object> getSession() {
		return session;
	}

	public void setSession(Map<String , Object> session) {
		this.session = session;
	}
	
}
