package com.dutiantech.service.asyn;

import com.dutiantech.model.SMSLog;
import com.dutiantech.service.SMSLogService;

public class SMSTaskService extends Thread{

	SMSLog smsLog = null ;
	String content = "";
	String type = "";
	String typeName = "";
	String userCode = "";
	private static SMSLogService smsLogService = new SMSLogService();
	
	public SMSTaskService(String content, String type ,String typeName,String userCode){
		this.content = content ;
		this.type = type ;
		this.typeName = typeName ;
		this.userCode = userCode ;
	}
	
	public void run(){
		smsLogService.save(content, type, typeName, userCode);
	}
	
}
