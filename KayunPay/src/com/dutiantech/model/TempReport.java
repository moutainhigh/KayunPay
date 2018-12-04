package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 个人报表临时表
 * @author shiqingsong
 *
 */
public class TempReport extends Model<TempReport> {
	
	private static final long serialVersionUID = 5588804319237542949L;
	public static final TempReport tempReportDao = new TempReport();
	
	
	public long countLoanByAmount(long amount) {
		return Db.queryLong("select count(1) from temp_report where loan4Month < ? ", amount);
	}
	
	public long countLoanByIncome(long amount) {
		return Db.queryLong("select count(1) from temp_report where income4Month < ? ", amount);
	}
	
	public long countLoanByRegist(long regist4Now) {
		return Db.queryLong("select count(1) from temp_report where regist4Now < ? ", regist4Now);
	}
	
}
