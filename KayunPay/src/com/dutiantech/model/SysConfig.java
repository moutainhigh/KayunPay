package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class SysConfig extends Model<SysConfig>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4000789776529062541L;
	
	public static final SysConfig sysConfigDao = new SysConfig() ;
}
