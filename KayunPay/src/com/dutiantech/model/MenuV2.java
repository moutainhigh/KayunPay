package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class MenuV2 extends Model<MenuV2> {
	
	private static final long serialVersionUID = 4332843558362402911L;
	
	public static final String TNAME = "t_menu_v2";
	public static final String MKEY = "menu_id";
	
	public static final MenuV2 menuDao = new MenuV2();
}
