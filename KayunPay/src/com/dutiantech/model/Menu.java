package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class Menu extends Model<Menu> {
	
	private static final long serialVersionUID = 4332843558362402911L;
	
	public static final Menu menuDao = new Menu();
}
