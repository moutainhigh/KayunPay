package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class Market extends Model<Market> {
	
	private static final long serialVersionUID = 1673348941279879124L;
	public static final Market marketDao = new Market();
	public String settingsType="A";
}
