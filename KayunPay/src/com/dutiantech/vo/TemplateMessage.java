package com.dutiantech.vo;

import com.alibaba.fastjson.JSONObject;

/**
 * 模版消息
 * @author five
 *
 */
public class TemplateMessage extends JSONObject {
	
	private static final long serialVersionUID = 6178931495437507941L;
	private JSONObject _DATA = new JSONObject() ;
	
	public TemplateMessage(){
		this.setTopcolor("#FF0000");
	}
	/**
	 * 	设置接受消息用户的openid
	 * @param touser
	 */
	public void setTouser(String touser){
		this.put("touser", touser ) ;
	}
	
	/**
	 * 	设置消息模版ID
	 * @param template_id
	 */
	public void setTemplateId(String template_id ){
		this.put("template_id", template_id ) ;
	}
	
	/**
	 * 	设置消息链接
	 * @param url
	 */
	public void setUrl(String url ){
		this.put("url", url ) ;
	}
	
	/**
	 * 	设置消息头字体颜色
	 * @param url
	 */
	public void setTopcolor(String topcolor ){
		this.put("topcolor", topcolor ) ;
	}
	
	/**
	 * 	设置消息模版key的值
	 * @param key
	 * @param value
	 * @param color
	 */
	public void setData(String key , String value , String color ){
		JSONObject json = new JSONObject() ;
		json.put("value", value );
		json.put("color", color ) ;
		
		_DATA.put( key , json ) ;
	}
	
	public void setData(String key , String value ){
		this.setData(key, value, "#173177" ) ;
	}
	
	@Override
	public String toJSONString(){
		this.put("data", _DATA ) ;
		return super.toJSONString() ;
	}
	
}
