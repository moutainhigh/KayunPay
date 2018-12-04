package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class UserCount extends Model<UserCount> {
	
	private static final long serialVersionUID = -2680295761436973353L;
	
	public static final UserCount userCountDao = new UserCount();
	
}
