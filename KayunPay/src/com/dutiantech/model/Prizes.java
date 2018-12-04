package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 奖品表
 * @author shiqingsong
 *
 */
public class Prizes extends Model<Prizes> {
	
	private static final long serialVersionUID = 7833560590950938321L;
	
	public static final Prizes prizeDao = new Prizes();
	
	
	/**
	 * 初始化活动奖品
	 * @param activeCode
	 * @return
	 */
	public boolean initPrizeCount(String activeCode ){
		Db.update("UPDATE t_prizes SET count=0 WHERE activeCode=? and prizeLevl < 6", activeCode ) ;
		return true ;
	}
	
	
}






