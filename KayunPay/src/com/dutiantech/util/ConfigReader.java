package com.dutiantech.util;

import java.util.ResourceBundle;

public class ConfigReader {

	private static final ResourceBundle ERROR_BUNDLE = ResourceBundle.getBundle("error");
	 public static String getProperties(String key){
	    	try{
	    		System.out.println("key值====" + new String(ERROR_BUNDLE.getString(key).getBytes("ISO-8859-1"),"utf-8"));
	        	//return ERROR_BUNDLE.getString(key);
	        	return new String(ERROR_BUNDLE.getString(key).getBytes("ISO-8859-1"),"utf-8");
	    	}catch(Exception e) {
	    		
	    		return null;
	    	}
	    	
	    	
	    }
}
