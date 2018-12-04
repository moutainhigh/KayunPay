package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

/**
 * 用户待还余额类
 * @author ni959
 *
 */
public class ReturnedAmount extends Model<ReturnedAmount>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 367405524405326554L;
	public static final ReturnedAmount returnedAmountDao = new ReturnedAmount();
}
