package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;

public class RechargeTrace extends Model<RechargeTrace> {
	
	public enum BANK_STATE{
		ACCEPT{
			public String key(){
				return "A";
			};
			public String desc(){
				return "已受理";
			}
		},
		SUCCESS {
			public String key(){
				return "B";
			};
			public String desc(){
				return "充值成功";
			}
		},
		FAILD {
			public String key(){
				return "C";
			};
			public String desc(){
				return "充值失败";
			}
		},
		REBACK {
			public String key(){
				return "D";
			};
			public String desc(){
				return "充值回退";
			}
		},
		OTHER {
			public String key(){
				return "E";
			};
			public String desc(){
				return "其他状态";
			}
		},
		LOCKED {
			public String key(){
				return "F";
			};
			public String desc(){
				return "锁定订单";
			}
		},
		Quota {
			public String key(){
				return "X";
			};
			public String desc(){
				return "限额充值失败";
			}
		};
		public abstract String key();
		public abstract String desc();
	}
	
	public enum TRACE_STATE{
		DOING{
			public String key(){
				return "A";
			};
			public String desc(){
				return "充值提交";
			}
		},
		SUCCESS{
			public String key(){
				return "B";
			};
			public String desc(){
				return "充值成功";
			}
		},
		FAILD{
			public String key(){
				return "C";
			};
			public String desc(){
				return "充值失败";
			}
		},
		LOCKED {
			public String key(){
				return "F";
			};
			public String desc(){
				return "锁定订单";
			}
		};
		public abstract String key();
		public abstract String desc();
	}
	
	public enum RECHARGE_TYPE{
		SYSTEM{
			public String key(){
				return "SYS";
			};
			public String desc(){
				return "系统";
			}
		},
		LL{
			public String key(){
				return "LL";
			};
			public String desc(){
				return "连连支付";
			}
		},
		LLAUTH{
			public String key(){
				return "LLAT";
			};
			public String desc(){
				return "连连支付认证";
			}
		},
		BFZF{
			public String key(){
				return "BFZF";
			};
			public String desc(){
				return "宝付支付";
			}
		},
		SYX{
			public String key(){
				return "SYX";
			};
			public String desc(){
				return "商银信";
			}
		},
		FAST{
			public String key(){
				return "FAST";
			};
			public String desc(){
				return "快捷支付";
			}			
		},
		QC{
			public String key() {
				return "QC";
			}
			public String desc() {
				return "圈存";
			}
		},
		WY{
			public String key(){
				return "WY";
			};
			public String desc(){
				return "网银支付";
			}			
		},
		OFFLINE{
			public String key() {
				return "OFL";
			}
			public String desc() {
				return "线下支付";
			}
		},
		PHONE{
			public String key(){
				return "PHONE";
			};
			public String desc(){
				return "手机支付";
			}			
		};
		public abstract String key();
		public abstract String desc();
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8822097383105784656L;
	
	public static final RechargeTrace rechargeTraceDao = new RechargeTrace();

}
