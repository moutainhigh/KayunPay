package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class UserInfo extends Model<UserInfo> {
	
	private static final long serialVersionUID = 669581535864407422L;
	
	public static final UserInfo userInfoDao = new UserInfo();

}
