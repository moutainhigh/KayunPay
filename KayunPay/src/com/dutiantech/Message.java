package com.dutiantech;

import java.io.Serializable;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class Message implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5135478887838576340L;

	private String return_code = "00";
	
	private String return_info = "ok";
	
	private Object return_data = null ;
	
	private String token = "" ;	//cookie id 

	public Message(){
		
	}
	
	public Message(String return_code , String return_info , Object return_data){
		
	}
	
	public static Message succ(String return_info , Object return_data ){
		Message msg = new Message() ;
		msg.setReturn_info(return_info );
		msg.setReturn_data(return_data);
		return msg ;
	}
	
	public static Message error( String return_code , String return_info , Object return_data){
		Message msg = new Message() ;
		msg.setReturn_code(return_code);
		msg.setReturn_info(return_info );
		msg.setReturn_data(return_data);
		return msg ;
		
	}
	
	/**
	 * 返回只有一层json的响应
	 * @param return_info	成功信息
	 * @param map	参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject succ4Json(String return_info, Object object){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("return_code", "0000");
		jsonObject.put("return_info", return_info);
		if (object instanceof Map<?, ?>) {
			jsonObject.putAll((Map<? extends String, ? extends Object>) object);
		} else {
			jsonObject.put("return_data", JSONObject.toJSON(object));
		}
		return jsonObject;
	}
	
	/**
	 * 返回只有一层json的响应
	 * @param return_code	错误代码
	 * @param return_info	错误信息
	 * @param map	参数
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject error4Json(String return_code, String return_info, Object object) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("return_code", return_code);
		jsonObject.put("return_info", return_info);
		if (object instanceof Map<?, ?>) {
			jsonObject.putAll((Map<? extends String, ? extends Object>) object);
		} else {
			jsonObject.put("return_data", JSONObject.toJSON(object));
		}
		return jsonObject;
	}
	
	public String getReturn_code() {
		return return_code;
	}
	
	public void setReturn_code(String return_code) {
		this.return_code = return_code;
	}

	public String getReturn_info() {
		return return_info;
	}

	public void setReturn_info(String return_info) {
		this.return_info = return_info;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Object getReturn_data() {
		return return_data;
	}

	public void setReturn_data(Object return_data) {
		this.return_data = return_data;
	}
	
	
}
