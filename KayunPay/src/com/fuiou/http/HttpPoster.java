package com.fuiou.http;

/*
 * File ： HttpPoster.java
 * 
 * Project: wcmn
 *
 * Modify Information:
 * =============================================================================
 *   Author          Date                      Description
 *   ------------ ---------- ---------------------------------------------------
 *   IBM          2008-10-8          Implement Programming Spec V1.0
 * ============================================================================= 
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;


public class HttpPoster {
	private String responseString = "";
	  public HttpPoster() {
	  }
    /**
     * 使用http协议发送xmltext到url
     * @param url   将要发送的地址
     * @param xmltext  将要发送的内容
     * @return   http返回码
     */
	  public  int post(String url,Map parameters) throws IOException{
	    PostMethod xmlpost;
	    int responseStatCode = 0;
	    HttpClient httpclient = new HttpClient();
	    httpclient.setConnectionTimeout(1000*30);
	    xmlpost = new PostMethod(url);
	    //为什么我机器上用了setRequestHeader取不到参数返回域
	    httpclient.getParams().setParameter(
	    		HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");

	    //xmlpost.setRequestHeader("Content-type", "text/html; charset=UTF-8");
	    try {
	    	NameValuePair[] nameValuePairs = new NameValuePair[parameters.size()];
	    	Iterator keyIterator = parameters.keySet().iterator();
	    	int i = 0;
			while (keyIterator.hasNext())
			{
				Object key = keyIterator.next();
				String value = (String)parameters.get(key);
				NameValuePair name = new NameValuePair((String)key, value);
				nameValuePairs[i]=name;
				i++;
			}
	    	            
	         xmlpost.setRequestBody(nameValuePairs); 
	    	responseStatCode = httpclient.executeMethod(xmlpost);
	    	this.responseString = xmlpost.getResponseBodyAsString();
	    }
	    catch (IOException ex2) {
	    	throw ex2;
	    }
	    return responseStatCode;
	    
	  }
	  public  int post(String url,String body)  throws IOException{
		    PostMethod xmlpost;
		    int responseStatCode = 0;
		    byte[] input = null;
		    try {
		      input = body.getBytes("UTF-8");
		    }
		    catch (Exception ex) {
		    	ex.printStackTrace();
		    	return responseStatCode;
		    }
		    InputStream instream = new ByteArrayInputStream(input);
		    xmlpost = new PostMethod(url);
		    xmlpost.setRequestEntity(new InputStreamRequestEntity(instream, body.length()));
		    xmlpost.setRequestHeader("Content-type", "text/xml; charset=UTF-8");
		    HttpClient httpclient = new HttpClient();
		    httpclient.setConnectionTimeout(1000*10);
		    try {
		    	responseStatCode = httpclient.executeMethod(xmlpost);
		    	this.responseString = xmlpost.getResponseBodyAsString();
		    }
		    catch (IOException ex2) {
		    	ex2.printStackTrace();
		    	throw ex2;
		    }
		    return responseStatCode;
		  }
	public String getResponseString() {
		return responseString;
	} 
	
	   /**
     * 使用http协议发送xmltext到url
     * @param url   将要发送的地址
     * @param xmltext  将要发送的内容
     * @return   http返回码
     */
	  public  int post(String url,Map parameters,String charset) throws IOException{
	    PostMethod xmlpost;
	    int responseStatCode = 0;
	    HttpClient httpclient = new HttpClient();
	    httpclient.setConnectionTimeout(1000*30);
	    xmlpost = new PostMethod(url);
	    //为什么我机器上用了setRequestHeader取不到参数返回域
	    httpclient.getParams().setParameter(
	    		HttpMethodParams.HTTP_CONTENT_CHARSET, charset);

	    //xmlpost.setRequestHeader("Content-type", "text/html; charset=UTF-8");
	    try {
	    	NameValuePair[] nameValuePairs = new NameValuePair[parameters.size()];
	    	Iterator keyIterator = parameters.keySet().iterator();
	    	int i = 0;
			while (keyIterator.hasNext())
			{
				Object key = keyIterator.next();
				String value = (String)parameters.get(key);
				NameValuePair name = new NameValuePair((String)key, value);
				nameValuePairs[i]=name;
				i++;
			}
	    	            
	         xmlpost.setRequestBody(nameValuePairs); 
	    	responseStatCode = httpclient.executeMethod(xmlpost);
	    	this.responseString = xmlpost.getResponseBodyAsString();
	    }
	    catch (IOException ex2) {
	    	ex2.printStackTrace();
	    	throw ex2;
	    }
	    return responseStatCode;
	    
	  }
	  
	  /**
	     * 使用http协议发送xmltext到url
	     * @param url   将要发送的地址
	     * @param xmltext  将要发送的内容
	     * @return   http返回码
	     */
		  public  String getResponseString(String url,Map parameters,String charset) throws IOException{
			  PostMethod xmlpost;
			    int responseStatCode = 0;
			    HttpClient httpclient = new HttpClient();
			    httpclient.setConnectionTimeout(1000*30);
			    xmlpost = new PostMethod(url);
			    //为什么我机器上用了setRequestHeader取不到参数返回域
			    httpclient.getParams().setParameter(
			    		HttpMethodParams.HTTP_CONTENT_CHARSET, charset);

			    //xmlpost.setRequestHeader("Content-type", "text/html; charset=UTF-8");
			    try {
			    	NameValuePair[] nameValuePairs = new NameValuePair[parameters.size()];
			    	Iterator keyIterator = parameters.keySet().iterator();
			    	int i = 0;
					while (keyIterator.hasNext())
					{
						Object key = keyIterator.next();
						String value = (String)parameters.get(key);
						NameValuePair name = new NameValuePair((String)key, value);
						nameValuePairs[i]=name;
						i++;
					}
			    	            
			         xmlpost.setRequestBody(nameValuePairs); 
			    	responseStatCode = httpclient.executeMethod(xmlpost);
			    	this.responseString = xmlpost.getResponseBodyAsString();
			    }
			    catch (IOException ex2) {
			    	ex2.printStackTrace();
			    	throw ex2;
			    }
			    return this.responseString;
		    
		  }
		  
		  /**
		     * 使用http协议发送xmltext到url
		     * @param url   将要发送的地址
		     * @param xmltext  将要发送的内容
		     * @return   http返回码
		     */
			  public  String getResponseString(String url,String body,String charset) throws IOException{
				    PostMethod xmlpost;
				    int responseStatCode = 0;
				    byte[] input = null;
				    try {
				      input = body.getBytes(charset);
				    }
				    catch (Exception ex) {
				    	ex.printStackTrace();
				    	return "";
				    }
				    InputStream instream = new ByteArrayInputStream(input);
				    xmlpost = new PostMethod(url);
				    xmlpost.setRequestEntity(new InputStreamRequestEntity(instream, body.length()));
				    xmlpost.setRequestHeader("Content-type", "text/xml; charset=" + charset);
				    HttpClient httpclient = new HttpClient();
				    httpclient.setConnectionTimeout(1000*10);
				    try {
				    	responseStatCode = httpclient.executeMethod(xmlpost);
				    	this.responseString = new String (xmlpost.getResponseBody(),charset);
				    	//this.responseString = xmlpost.getResponseBodyAsString();
				    }
				    catch (IOException ex2) {
				    	ex2.printStackTrace();
				    	throw ex2;
				    }
				    return responseString;
			    
			  }
			  
}
