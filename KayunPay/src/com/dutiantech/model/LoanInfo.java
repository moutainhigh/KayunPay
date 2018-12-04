package com.dutiantech.model;

import java.util.HashMap;
import java.util.Map;

import com.dutiantech.util.Number;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;

public class LoanInfo extends Model<LoanInfo> {
	
	public static Map<Integer, Integer> RATE_MAP = new HashMap<Integer , Integer>();
	static{
		RATE_MAP.put(1, 1700 );
		RATE_MAP.put(2, 1700 );
		RATE_MAP.put(3, 1700 );
		RATE_MAP.put(4, 1800 );
		RATE_MAP.put(5, 1800 );
		RATE_MAP.put(6, 1800 );
		RATE_MAP.put(7, 1900 );
		RATE_MAP.put(8, 1900 );
		RATE_MAP.put(9, 1900 );
		RATE_MAP.put(10, 2000 );
		RATE_MAP.put(11, 2000 );
		RATE_MAP.put(12, 2000 );
		RATE_MAP.put(13, 2200 );
		RATE_MAP.put(14, 2200 );
		RATE_MAP.put(15, 2200 );
		RATE_MAP.put(16, 2200 );
		RATE_MAP.put(17, 2200 );
		RATE_MAP.put(18, 2200 );
	}
	public enum LOAN{
		BALANCE {
			public String key(){
				return "loanAmount_";
			}
		};
		public abstract String key();
	}
	
	private static final long serialVersionUID = -7312354489306130427L;

	public static final LoanInfo loanInfoDao = new LoanInfo();
	
	public static final boolean checkIsLast(String loanCode ){
		
		int loan = Db.queryInt("select count(*) from t_loan_info where loanCode = ? and loanTimeLimit=reciedCount " , loanCode) ;
		
		return loan > 0  ;
		
	}

	public String getTip_() {
		String tip = "";
		long releaseDate = Long.parseLong(getStr("releaseDate"));
		if (getInt("benefits4new") == 0) {
			if (releaseDate >= 20171111 && releaseDate <= 20171117) {
				tip = "首月";
			}
		}
		return tip;
	}
	
	public int getRewardRateByYear() {
		int rewardRateByYear = getInt("rewardRateByYear");
		long releaseDate = Long.parseLong(getStr("releaseDate"));
		if (getInt("benefits4new") == 0) {
			if (releaseDate >= 20171111 && releaseDate <= 20171117) {
				rewardRateByYear = 400;
			}
		}
		return rewardRateByYear;
	}
	
	public String getRewardRateByYear_() {
		int rewardRateByYear = getRewardRateByYear();
		return Number.longToString1(rewardRateByYear);
	}

	/**
	 * 产品类型
	 * @author Administrator
	 *
	 */
	public enum productType {
		A {
			public String val() {return "A";}
			public String desc() {return "质押宝";}
		},
		B {
			public String val() {return "B";}
			public String desc() {return "车稳盈";}
		},
		C {
			public String val() {return "C";}
			public String desc() {return "房稳赚";}
		},
		D {
			public String val() {return "D";}
			public String desc() {return "其它";}
		},
		G {
			public String val() {return "G";}
			public String desc() {return "稳定投";}
		}
		,
		YFQ {
			public String val() {return "E";}
			public String desc() {return "易分期";}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 标类型
	 * @author Administrator
	 *
	 */
	public enum loanType{
		/**
		 * 信用标
		 */
		A{
			public String val(){return "A";}
			public String desc(){return "信用标";}
		},
		/**
		 * 抵押标
		 */
		B{
			public String val(){return "B";}
			public String desc(){return "抵押标";}
		},
		/**
		 * 担保标
		 */
		C{
			public String val(){return "C";}
			public String desc(){return "担保标";}
		},
		/**
		 * 流转标
		 */
		D{
			public String val(){return "D";}
			public String desc(){return "流转标";}
		},
		/**
		 * 质押标
		 */
		E{
			public String val(){return "E";}
			public String desc(){return "质押标";}
		},
		/**
		 * 抵押担保标
		 */
		F{
			public String val(){return "F";}
			public String desc(){return "抵押担保标";}
		},
		/**
		 * 抵押流转标
		 */
		G{
			public String val(){return "G";}
			public String desc(){return "抵押流转标";}
		},
		/**
		 * 担保流转标
		 */
		H{
			public String val(){return "H";}
			public String desc(){return "担保流转标";}
		},
		/**
		 * 质押流转标
		 */
		I{
			public String val(){return "I";}
			public String desc(){return "质押流转标";}
		},
		/**
		 * 质押流转标
		 */
		J{
			public String val(){return "J";}
			public String desc(){return "机构标";}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	public enum LoanState {
		N{
			public String val(){return "N";}
			public String desc(){return "还款中";}
		},
		J{
			public String val(){return "J";}
			public String desc(){return "招标中";}
		};
		public abstract String val();
		public abstract String desc();
	}
}
