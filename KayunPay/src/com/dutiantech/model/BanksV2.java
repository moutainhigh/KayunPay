package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class BanksV2 extends Model<BanksV2> {
	
	private static final long serialVersionUID = -9060488527257287190L;
	
	public static final BanksV2 bankV2Dao = new BanksV2();
	
	public enum PAY_TYPE{
		LL{
			public String key(){
				return "LL" ;
			};
			public String desc(){
				return "连连支付" ;
			}
		};
		public abstract String key();
		public abstract String desc();
	}
	
}
