package com.fuiou.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import org.apache.commons.httpclient.protocol.Protocol;



public class SendHTTPTread extends Thread{
	private Map parameters;
	private String back_notify_url;
	public SendHTTPTread(Map parameters,String back_notify_url)
	{
		this.parameters = parameters;
		this.back_notify_url = back_notify_url;
	}
	
	public void run()
	{
		try {
			if(back_notify_url.indexOf("https://")!=-1){
				//创建SSL连接
				Protocol myhttps = new Protocol("https", new MySSLSocketFactory (), 443);
				Protocol.registerProtocol("https", myhttps);
				HttpPoster poster = new HttpPoster();
				poster.post(back_notify_url, parameters);
				Protocol.unregisterProtocol("https");
			}
			else{
				HttpPoster poster = new HttpPoster();
				int statusCode=poster.post(back_notify_url, parameters);
				if(statusCode<HttpURLConnection.HTTP_OK || statusCode>=HttpURLConnection.HTTP_MULT_CHOICE){
	                System.out.println("失败返回码[" + statusCode + "]");
	                throw new Exception("后台通知失败，失败码[ "+ statusCode +" ]");
	            }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
