package com.dutiantech.service;
import com.dutiantech.plugins.HttpRequestor;

public class DoAXActionService extends BaseService {

	public String doQuery(String url ){
		return doActionByPost( url , "" ) ;
	}
	
	public String doActionByPost(String url , String data ){
		
		HttpRequestor request = new HttpRequestor() ;

		try {
			return request.doPost(url, data);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}
