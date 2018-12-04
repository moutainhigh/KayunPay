package com.dutiantech.service;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.util.HttpRequestUtil;

/**
 * 身份证自动认证服务
 * @author shiqingsong
 *
 */
public class AuthenticationService extends BaseService{

	public int autoAuth(String realname , String cardno){
		return 0;
//		String url = "http://api.yunxinku.com/idcard/check";
//		String getData = "key=fb00c30845f019b15ee7e6cbfac1a7a8&realname=[realname]&cardno=[cardno]".replace("[realname]", realname).replace("[cardno]", cardno);
//		String result = HttpRequestUtil.sendGet(url, getData);
//		JSONObject object = JSONObject.parseObject(result);
//		return Integer.parseInt(object.get("ret").toString());
	}
}
