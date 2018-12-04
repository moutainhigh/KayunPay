package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class LoanNxjd extends Model<LoanNxjd> {

	private static final long serialVersionUID = 5001524786188694185L;
	
	public static final LoanNxjd loanNxjdDao = new LoanNxjd();
	
	public enum STATUS {
		A {
			public String key() {return "A";}
			public String desc() {return "借款中";}
		},
		B {
			public String key() {return "B";}
			public String desc() {return "已结清";}
		};
		public abstract String key();
		public abstract String desc();
	}
}
