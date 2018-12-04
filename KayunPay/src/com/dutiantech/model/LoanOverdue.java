package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class LoanOverdue extends Model<LoanOverdue> {
	
	private static final long serialVersionUID = -429322829006022610L;
	
	public static final LoanOverdue overdueTraceDao = new LoanOverdue();

}
