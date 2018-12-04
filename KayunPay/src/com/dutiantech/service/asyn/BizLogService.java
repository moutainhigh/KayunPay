package com.dutiantech.service.asyn;

import com.dutiantech.model.BizLog;
import com.dutiantech.util.DateUtil;

public class BizLogService extends Thread{

	BizLog bizLog = null ;
	
	public BizLogService(BizLog bizLog){
		this.bizLog = bizLog ;
	}
	
	public void run(){
		bizLog.set("bizDateTime", DateUtil.getNowDateTime() ) ;
		bizLog.set("bizDate", DateUtil.getNowDate() ) ;
		bizLog.save() ;
	}
	
}
