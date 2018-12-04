package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class SMSLog extends Model<SMSLog> {
	
	private static final long serialVersionUID = -887055945710219838L;
	public static final SMSLog smsLogDao = new SMSLog();
	
	public static SMSLog setSMSLog(String mobile,String content,String type,String typeName,int status){
		SMSLog smsLog = new SMSLog();
		smsLog.set("mobile", mobile);
		smsLog.set("content", content);
		smsLog.set("type", type);
		smsLog.set("typeName", typeName);
		smsLog.set("status", status);
		return smsLog;
	}
	
}
