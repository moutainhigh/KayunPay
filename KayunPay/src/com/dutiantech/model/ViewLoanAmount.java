package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

/**
 * 投资排名
 * @author shiqingsong
 *
 */
public class ViewLoanAmount extends Model<ViewLoanAmount> {
	
	private static final long serialVersionUID = -8981821098110403828L;
	
	public static final ViewLoanAmount viewLoanDao = new ViewLoanAmount();
	
	
	public long countByAmount(long amount ,String Symbol) {
		return Db.queryLong("select count(1) from view_loan_amount where traceAmount "+ Symbol + " ? ", amount);
	}
	
	public long countAll() {
		return Db.queryLong("select count(1) from view_loan_amount");
	}

}
