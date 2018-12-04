package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class Funds extends Model<Funds> {
	
	public enum FUNDS{
		
		AVBALANCE{
			public String key(){
				return "FUNDS_AVBALANCE4USER_" ;
			}
		},
		FROZEBALANCE{
			public String key(){
				return "FUNDS_FROZEBALANCE4USER_";
			}
		};
		public abstract String key();
	}
	
	private static final long serialVersionUID = 190147253142801637L;
	
	public static final Funds fundsDao = new Funds();

}
