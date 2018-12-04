package com.dutiantech.service;

import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.StringUtil;

/**
 * 短信服务
 * @author shiqingsong
 *
 */
public class SMSService extends BaseService{

	
	/**
	 * 实时发短信
	 * @param mobile	手机号
	 * @param content	短信内容
	 * @return
	 */
	public int sendSms(String mobile, String content){
		String un = "N3002916";
		String pwd = "2ZpMR4UHTF947f";
		String getData = "un="+un+"&pw="+pwd+"&phone="+mobile+"&msg="+content+"&rd=1";
		try {
			String result = HttpRequestUtil.sendGet("http://sms.253.com/msg/send", getData);
			String sendDateTime = result.split(",")[0];
			String sendResult = result.split(",")[1];
			if(StringUtil.isBlank(sendDateTime) == false && StringUtil.isBlank(sendResult) == false){
				if(sendResult.startsWith("0")){
					return 0;
				}else{
					return Integer.parseInt(sendResult);
				}
			}
		} catch (Exception e) {
			System.out.println("发送短信异常..."+e.getMessage());
			e.printStackTrace();
			return -99;
		}
		return -1;
	}
	
	/**
	 * 定时发送短信内容
	 * @param mobile	手机号
	 * @param content	短信内容
	 * @param delay		发送时间yyyyMMddHHmm
	 * @return
	 */
	public int sendSmsByDelay(String mobile, String content, String delay){
		String un = "N3002916";
		String pwd = "2ZpMR4UHTF947f";
		String getData = "un="+un+"&pw="+pwd+"&phone="+mobile+"&msg="+content+"&rd=1&delay="+delay;
		try {
			String result = HttpRequestUtil.sendGet("http://sms.253.com/msg/sendDelay", getData);
			String sendDateTime = result.split(",")[0];
			String sendResult = result.split(",")[1];
			if(StringUtil.isBlank(sendDateTime) == false && StringUtil.isBlank(sendResult) == false){
				if(sendResult.startsWith("0")){
					return 0;
				}else{
					return Integer.parseInt(sendResult);
				}
			}
		} catch (Exception e) {
			System.out.println("发送短信异常..."+e.getMessage());
			e.printStackTrace();
			return -99;
		}
		return -1;
	}
	
}

