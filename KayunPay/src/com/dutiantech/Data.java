package com.dutiantech;

public class Data{
	
	public long lastTime = 0 ;
	public Object value = null ;
	
	public Data(Object val , long t ){
		setValue( val , t ) ;
	}
	
	public boolean isTimeout(){
		long nowTime = System.currentTimeMillis() ;
		return lastTime > nowTime ;
	}
	
	public void setValue( Object val ){
		value = val ;
	}
	
	public void setValue( Object val , long t ){
		value = val ;
		lastTime = System.currentTimeMillis() + t ;
	}
	
	public Object getValue(){
		if( lastTime == 0 )
			return value ;
		
		if( isTimeout() ){
			return value ;
		}
		
		return value ;
	}
}