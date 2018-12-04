package com.dutiantech;

import com.dutiantech.config.AdminConfig;

public class Log {

	public static boolean devModel = AdminConfig.isDevMode ;
	
	public static void info(String str){
//		if( devModel ){
			//format
		System.out.println(str);
//		}
	}
	
	public static void debug(Object o ){
		if( devModel ){
			System.out.println(o);
		}
	}
	
	public static void error(String str , Exception e){
		System.out.println(str + ",e:" + e.getLocalizedMessage() );
	}
	
}
