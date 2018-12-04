package com.dutiantech.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * @description 读取properties配置文件信息
 * @author 充满智慧的威哥
 */
public class Property {
	
	/**
	 * 根据主键查找配置文件中的信息
	 * @param configPath 配置文件路径 例如: 当前项目跟路径 + /WEB-INF/config.properties
	 * @param key 根据键找值
	 * @return
	 */
	public static String getProperty(String configPath, String key) {
		Properties pro = new Properties();
		FileInputStream fin = null;
		String content = null;
		try {
			fin = new FileInputStream(configPath);
			pro.load(fin);
			content = pro.getProperty(key);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return content;
	}
	
	public static int getProperty(HttpServletRequest request, String key) {
		String path = "/WEB-INF/config.properties";
		String value = Property.getProperty(request.getSession().getServletContext().getRealPath("/") + path, key);
		return Integer.parseInt(value);
	}
	
	/**
	 * 查找配置文件中所有信息
	 * @param configPath 配置文件路径
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, String> getProperties(String configPath) {
		Map<String, String> map = new HashMap<String, String>();
		Properties pro = new Properties();
		FileInputStream fin = null;
		try {
			fin = new FileInputStream(configPath);
			pro.load(fin);
			Set set = pro.keySet();
			for (Iterator iterator = set.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				map.put(key, pro.getProperty(key));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		return map;
		/**
		  迭代map集合
		  Set<String> set = map.keySet();
		  for (Iterator iterator = set.iterator(); iterator.hasNext();) {
			 String str = (String) iterator.next();
			 System.out.println(str + " " + map.get(str));
		  }
		 */
	}
	@SuppressWarnings("rawtypes")
	public static Map<String, String> getPropertyValue(String path){
		Properties pro = new Properties();
		Map<String, String> maps = new HashMap<String, String>();
		try {
		    InputStream inStr = ClassLoader.getSystemResourceAsStream(path);
		    pro.load(inStr);
		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		//Properties 继承于 Hashtable，entrySet()是Hashtable的方法，
		//返回此 Hashtable 中所包含的键的 Set 视图。此 collection 中每个元素都是一个 Map.Entry
		Iterator it=pro.entrySet().iterator();
		while(it.hasNext()){
		    Map.Entry entry=(Map.Entry)it.next();
		    String key = entry.getKey().toString();
		    String value = entry.getValue().toString();
		    maps.put(key, value);   
		} 
		return maps;
	}
	public static String getPropertyValueByKey(String path,String key){
		ResourceBundle rb = ResourceBundle.getBundle(path);
		if(null!=rb.getObject(key)){
			return rb.getObject(key).toString();
		}	
		return null;
	}
	public static void main(String[] args) {
	/*	ResourceBundle rBundle  = ResourceBundle.getBundle("city");
		Enumeration<String> enumeration = rBundle.getKeys();
		while (enumeration.hasMoreElements()) {
			String string = (String) enumeration.nextElement();
			System.out.println(string);
		}*/
		System.out.println(Property.getPropertyValueByKey("config", "USER_PICTURE_MIN_WIDTH"));
	}
}
