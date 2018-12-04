package com.dutiantech.util;

public class RepaymentCountEnum {
	/**
	 * 批次类型
	 */
	public enum batchType{
		/**
		 * 正常还款
		 */
		A{
			public int val(){
				return 1;
			}
			public String desc(){
				return "正常还款";
			}
		},
		/**
		 * 逾期本金还款
		 */
		B{
			public int val(){
				return 2;
			}
			public String desc(){
				return "逾期本金还款";
			}
		},
		/**
		 * 逾期垫付利息还款
		 */
		C{
			public int val(){
				return 3;
			}
			public String desc(){
				return "逾期垫付利息还款";
			}
		},
		/**
		 * 补发
		 */
		D{
			public int val(){
				return 4;
			}
			public String desc(){
				return "补发";
			}
		};
		
		public abstract int val();
		public abstract String desc();
	}
	
	/**
	 * 批次状态
	 */
	public enum batchStatus{
		/**
		 * 未解冻
		 */
		A{
			public int val(){
				return 0;
			}
			public String desc(){
				return "未解冻";
			}
		},
		/**
		 * 已解冻
		 */
		B{
			public int val(){
				return 1;
			}
			public String desc(){
				return "已解冻";
			}
		},
		/**
		 * 全部失败
		 */
		C{
			public int val(){
				return 2;
			}
			public String desc(){
				return "全部失败";
			}
		},
		/**
		 * 部分失败
		 */
		D{
			public int val(){
				return 3;
			}
			public String desc(){
				return "部分失败";
			}
		},
		/**
		 * 失败已处理
		 */
		E{
			public int val(){
				return 4;
			}
			public String desc(){
				return "失败已处理";
			}
		};
		public abstract int val();
		public abstract String desc();
	}
	
	/**
	 * 还款进度
	 */
	public enum repaymentCountStatus{
		/**
		 * 批次发送中
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "批次发送中";
			}
		},
		/**
		 * 批次发送完成
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "批次发送完成";
			}
		},
		/**
		 * 还款完成
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "还款完成";
			}
		},
		/**
		 * 未开始
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "未开始";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
}
