package com.fuiou.util;


import java.util.ResourceBundle;

public class ConfigReader {
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("fuiou");
	
	/**
	 * @param propName
	 * @param key
	 */
	public static String getConfig(String key) {
		
		try {
			System.out.println("key值====" + RESOURCE_BUNDLE.getString(key));
			return RESOURCE_BUNDLE.getString(key);
		} catch (RuntimeException e) {
			e.printStackTrace();
			return "";
		}
    }
    public static int getInt(String key) {
    	
    	System.out.println("key值====" + RESOURCE_BUNDLE.getString(key));
    	
		return Integer.parseInt(RESOURCE_BUNDLE.getString(key));
    }
}