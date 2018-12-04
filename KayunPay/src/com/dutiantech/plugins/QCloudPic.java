package com.dutiantech.plugins;
import java.util.HashMap;

import com.dutiantech.PicCloud;
import com.dutiantech.UploadResult;

public class QCloudPic extends HashMap<String , PicCloud>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1072473917820099755L;
	
	static QCloudPic clients = null ;
	static PicCloud lastClient = null ;
	
	static{
		clients = new QCloudPic();
	}
	
	public static void addClient( String clientName ,PicCloud client){
		clients.put("clientName", client ) ;
		lastClient = client ;
	}
	
	public static void use(String clientName){
		lastClient = clients.get(clientName) ;
	}
	
	public static int upload( byte[] data ,UploadResult result ){
		return lastClient.Upload(data, result) ;
	}
}
