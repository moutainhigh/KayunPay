package com.dutiantech.task;
import java.util.List;

import com.dutiantech.Log;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SMSService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author shiqingsong
 *
 */
public class SMSThread extends Thread {
	
	public static boolean isRun = false;
	private String mobiles;
	private String type;
	private String content;
	
	public SMSThread(){};
	public SMSThread(String mobiles,String type,String content){
		this.mobiles = mobiles;
		this.type = type;
		this.content = content;
	}
	
	public void run(){
		isRun = true;
		try{
			Log.info("开启服务：" + getName() );
			//发送给所有人
			if("1".equals(type) && StringUtil.isBlank(mobiles)){
				Log.info("短信类型：发送给所有人...");
				//查询所有用户手机号
				int pageNumber = 1;
				while(true){
					Log.info("正在拉取用户手机号..." + pageNumber * 1000);
					Page<User> pageUser = new UserService().findByPage(pageNumber, 1000, null, null, null, null);
					List<User> listUser = pageUser.getList();
					if(null != listUser && listUser.size() > 0){
						for (int i = 0; i < listUser.size(); i++) {
							String mobile_pw = listUser.get(i).getStr("userMobile");
							//解密手机号
							String mobile = "";
							try {
								mobile = CommonUtil.decryptUserMobile(mobile_pw);
							} catch (Exception e) {
							}
							if(CommonUtil.isMobile(mobile)){
								mobiles += mobile+",";
							}
						}
						pageNumber++;
					}else{
						mobiles = mobiles.substring(0, mobiles.length()-1);
						break;
					}
				}
			}else{
				Log.info("短信类型：发送指定用户...");
			}
			
			int error = 0;
			String[] mobileArr = mobiles.split(",");
			for (int i = 0; i < mobileArr.length; i++) {
				Log.info("正在排队发送中..."+(i+1));
				//验证手机号
				int result = -6;
				if(CommonUtil.isMobile(mobileArr[i])){
					result = new SMSService().sendSms(mobileArr[i], content+"【易融恒信】");
				}
				//记录日志
				new SMSLogService().save(SMSLog.setSMSLog(mobileArr[i], content, "8","营销短信",result));
				if(0 != result){
					error++;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					error++;
				}
			}
			Log.info("服务执行完成，发送失败数量：" + error + " 个");
		}catch(Exception e){
		}finally{
			isRun = false;
		}
		
	}
	
}
