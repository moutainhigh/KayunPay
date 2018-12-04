package com.dutiantech.controller.admin;

import org.json.JSONObject;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.tencent.xinge.XingeApp;
/**
 * 信鸽推送服务
 * @author shiqingsong
 *
 */
public class XingeController extends BaseController {
	
	private static long ANDROID_ACCESSID = 2100184341L;
	private static String ANDROID_SECRETKEY = "3cc3ea4412998e274df0441970fc4dff";
	private static long IOS_ACCESSID = 2200182665L;
	private static String IOS_SECRETKEY = "4e0cb84fcc759b9ead788be343ded6f3";
	
	/**
	 * 推送所有人
	 * 
	 * @param	title		推送标题
	 * @param	content		推送内容
	 * @param	appType		0,android+ios  1,android  2,ios
	 * @return
	 */
	@ActionKey("/pushAll")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message pushAll(){
		String title = getPara("title");
		String content = getPara("content");
		String appType = getPara("appType","0");
		if(StringUtil.isBlank(content)){
			return error("01", "推送内容不能为空", "");
		}
		
		String android_ret_code = "0";
		String android_msg = ""; 
		String ios_ret_code = "0";
		String ios_msg = "";
		
		if("0".equals(appType) || "1".equals(appType)){
			JSONObject androidResult = XingeApp.pushAllAndroid(ANDROID_ACCESSID, ANDROID_SECRETKEY, title, content);
			//android返回
			android_ret_code = androidResult.get("ret_code").toString();
			if("0".equals(android_ret_code) == false){
				android_msg = androidResult.get("err_msg").toString();
			}
		}
		if("0".equals(appType) || "2".equals(appType)){
			JSONObject iosResult = XingeApp.pushAllIos(IOS_ACCESSID, IOS_SECRETKEY, content, XingeApp.IOSENV_PROD);
			//IOS返回
			ios_ret_code = iosResult.get("ret_code").toString();
			ios_msg = "";
			if("0".equals(ios_ret_code) == false){
				ios_msg = iosResult.get("err_msg").toString();
			}
		}
		
		//返回
		if("0".equals(android_ret_code) && "0".equals(ios_ret_code)){
			return succ("推送成功", "推送成功");
		}else{
			String msg = "";
			if(StringUtil.isBlank(android_msg) == false){
				msg = "Android : " + android_ret_code + "_" + android_msg;
			}
			if(StringUtil.isBlank(ios_msg) == false){
				msg += "  IOS : " + ios_ret_code + "_" + ios_msg;
			}
			return error("02", "推送失败", msg);
		}
		
	}
	
	
}





