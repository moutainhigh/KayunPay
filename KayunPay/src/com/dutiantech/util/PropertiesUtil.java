package com.dutiantech.util;

import java.io.InputStream;
import java.util.Properties;


/**
 * system.properties配置文件读取工具类
 * @author open
 *
 */
@SuppressWarnings("serial")
public final class PropertiesUtil extends Properties {
	
	private static PropertiesUtil instance;
	
	PropertiesUtil() {
		try {
			InputStream is = getClass().getResourceAsStream("/runtime-config.txt");
		    load(is);
		} catch (Exception e) {
			System.out.println("错误："+getClass().getPackage());
			e.printStackTrace();
		}
	}
	
	
	public static PropertiesUtil getInstance(){
		if(instance!=null){
			return instance;
		}else{
			makeInstance();
			return instance;
		}
	}
	
	private static synchronized void makeInstance(){
		instance = new PropertiesUtil();
	}
	
	public static void cleanData(){
		PropertiesUtil.getInstance().clear();
		System.gc();
		instance = null;
	}

}
