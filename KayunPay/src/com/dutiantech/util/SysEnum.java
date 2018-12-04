package com.dutiantech.util;

public class SysEnum {
	
	/**
	 * 提现状态
	 */
	public enum withdrawTraceState{
		/**
		 * 未审核
		 */
		A{
			public String val(){
				return "0";
			}
			public String desc(){
				return "待审核";
			}
		},
		/**
		 * 已审核
		 */
		B{
			public String val(){
				return "1";
			}
			public String desc(){
				return "已审核";
			}
		},
		/**
		 * 提现中
		 */
		C{
			public String val(){
				return "2";
			}
			public String desc(){
				return "申请提现";
			}
		},
		/**
		 * 提现成功
		 */
		D{
			public String val(){
				return "3";
			}
			public String desc(){
				return "提现成功";
			}
		},
		/**
		 * 提现失败
		 */
		E{
			public String val(){
				return "4";
			}
			public String desc(){
				return "提现失败";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 对账状态
	 */
	public enum traceSynState{
		/**
		 * 未对账
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "未对账";
			}
		},
		/**
		 * 不平
		 */
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "不平";
			}
		},
		/**
		 * 对账完成
		 */
		O{
			public String val(){
				return "O";
			}
			public String desc(){
				return "对账完成";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 资金类型
	 */
	public enum fundsType{
		/**
		 * 收入
		 */
		J{
			public String val(){
				return "J";
			}
			public String desc(){
				return "收入";
			}
		},
		/**
		 * 支出
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "支出";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 交易类型-资金流水
	 */
	public enum traceType{
		/**
		 * 债权转让支出
		 */
		A {
			public String val(){
				return "A";
			}
			public String desc(){
				return "债权转让支出";
			}
		},
		/**
		 * 债权转让收入
		 */
		B {
			public String val(){
				return "B";
			}
			public String desc(){
				return "债权转让收入";
			}
		},
		/**
		 * 充值
		 */
		C {
			public String val(){
				return "C";
			}
			public String desc(){
				return "充值";
			}
		},
		/**
		 * 人工充值
		 */
		D {
			public String val(){
				return "D";
			}
			public String desc(){
				return "人工充值";
			}
		},
		/**
		 * 投标体验金
		 */
		V {
			public String val(){
				return "V";
			}
			public String desc(){
				return "投标体验金";
			}
		},
		/**
		 * 好友投资返佣
		 */
		O {
			public String val(){
				return "O";
			}
			public String desc(){
				return "好友投资返佣";
			}
		},
		/**
		 * 好友投资返现
		 */
		Q {
			public String val(){
				return "Q";
			}
			public String desc(){
				return "好友投资返现";
			}
		},
		/**
		 * 推荐奖励
		 */
		W {
			public String val(){
				return "W";
			}
			public String desc(){
				return "推荐奖励";
			}
		},
		/**
		 * 老平台
		 */
		H {
			public String val(){
				return "H";
			}
			public String desc(){
				return "老平台";
			}
		},
		/**
		 * 提现
		 */
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "提现";
			}
		},
		/**
		 * 提现失败
		 */
		K{
			public String val(){
				return "K";
			}
			public String desc(){
				return "提现失败";
			}
		},
		/**
		 * 人工提现
		 */
		E {
			public String val(){
				return "E";
			}
			public String desc(){
				return "人工提现";
			}
		},
		/**
		 * 投标支出
		 */
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "投标支出";
			}
		},
		/**
		 * 收益-本金
		 */
		R{
			public String val(){
				return "R";
			}
			public String desc(){
				return "收益-本金";
			}
		},
		/**
		 * 收益-利息
		 */
		L {
			public String val(){
				return "L";
			}
			public String desc(){
				return "收益-利息";
			}
		},
		/**
		 * 管理费支出
		 */
		M{
			public String val(){
				return "M";
			}
			public String desc(){
				return "管理费支出";
			}
		},
		/**
		 * 积分收入
		 */
		J{
			public String val(){
				return "J";
			}
			public String desc(){
				return "积分收入";
			}
		},
		/**
		 * 积分支出
		 */
		Z{
			public String val(){
				return "Z";
			}
			public String desc(){
				return "积分支出";
			}
		},
		/**
		 * 预投标-可用余额转入冻结余额
		 */
		X{
			public String val(){
				return "X";
			}
			public String desc(){
				return "预投标";
			}
		},
		/**
		 * 预投标-可用余额转入冻结余额
		 */
		T{
			public String val(){
				return "T";
			}
			public String desc(){
				return "分享奖励";
			}
		},
		/**
		 * 可用余额转入冻结余额
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "可用余额转入冻结余额";
			}
		},
		/**
		 * 冻结余额转入可用余额
		 */
		Y{
			public String val(){
				return "Y";
			}
			public String desc(){
				return "冻结余额转入可用余额";
			}
		},
		
		/**
		 * 招标成功，借款人收款
		 */
		S{
			public String val(){
				return "S";
			}
			public String desc(){
				return "招标成功";
			}
		},
		/**
		 * 还款-利息
		 */
		I{
			public String val(){
				return "I";
			}
			public String desc(){
				return "还款-利息";
			}
		},
		/**
		 * 还款-本金
		 */
		U{
			public String val(){
				return "U";
			}
			public String desc(){
				return "还款-本金";
			}
		},
		/**
		 * 现金抵用券(投标支出)
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "现金抵用券(投标支出)";
			}
		},
		/**
		 * 后台放款
		 */
		HF{
			public String val(){
				return "HF";
			}
			public String desc(){
				return "后台放款";
			}
		},
		/**
		 * 红包发放
		 */
		Voucher{
			public String val(){
				return "Voucher";
			}
			public String desc(){
				return "红包发放";
			}
		},
		/**
		 * 红包撤回
		 */
		VoucherRollback{
			public String val(){
				return "VoucherRollback";
			}
			public String desc(){
				return "红包撤回";
			}
		},
		/**
		 * 现金券红包返现
		 */
		Cashback{
			public String val(){
				return "Cashback";
			}
			public String desc(){
				return "红包返现";
			}
		},
		INCOME {
			public String val() { return "INCOME"; }
			public String desc() { return "收益"; }
		};
		public abstract String val();
		public abstract String desc();
	}

	/**
	 * 标状态
	 */
	public enum loanState{
		
		/**
		 * 所有贷款
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "所有贷款";
			}
		},
		/**
		 * 成功贷款
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "成功贷款";
			}
		},
		/**
		 * 失败贷款
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "失败贷款";
			}
		},
		/**
		 * 草稿
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "草稿";
			}
		},
		/**
		 * 无效
		 */
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "无效";
			}
		},
		/**
		 * 审核失败
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "审核失败";
			}
		},
		/**
		 * 待审核
		 */
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "待审核";
			}
		},
		/**
		 * 待发布
		 */
		H{
			public String val(){
				return "H";
			}
			public String desc(){
				return "待发布";
			}
		},
		/**
		 * 等待材料
		 */
		I{
			public String val(){
				return "I";
			}
			public String desc(){
				return "等待材料";
			}
		},
		/**
		 * 招标中
		 */
		J{
			public String val(){
				return "J";
			}
			public String desc(){
				return "招标中";
			}
		},
		/**
		 * 流标中
		 */
		K{
			public String val(){
				return "K";
			}
			public String desc(){
				return "流标中";
			}
		},
		/**
		 * 已流标
		 */
		L{
			public String val(){
				return "L";
			}
			public String desc(){
				return "已流标";
			}
		},
		/**
		 * 满标待审
		 */
		M{
			public String val(){
				return "M";
			}
			public String desc(){
				return "满标待审";
			}
		},
		/**
		 * 还款中
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "还款中";
			}
		},
		/**
		 * 还款成功
		 */
		O{
			public String val(){
				return "O";
			}
			public String desc(){
				return "还款成功";
			}
		},
		/**
		 * 提前还款
		 */
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "提前还款";
			}
		},
		/**
		 * 系统已代还
		 */
		Q{
			public String val(){
				return "Q";
			}
			public String desc(){
				return "系统已代还";
			}
		},
		/**
		 * 代还已回收
		 */
		R{
			public String val(){
				return "R";
			}
			public String desc(){
				return "代还已回收";
			}
		},
		/**
		 * 结算异常
		 */
		T{
			public String val(){
				return "T";
			}
			public String desc(){
				return "结算异常";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 投标流水状态
	 */
	public enum traceState {
		/**
		 * 进行中
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "进行中";
			}
		},
		/**
		 * 已结束
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "已结束";
			}
		},
		/**
		 * 逾期中
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "逾期中";
			}
		},
		/**
		 * 满标待审
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "满标待审";
			}
		},
		/**
		 * 还款中
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "还款中";
			}
		},
		/**
		 * 结算异常
		 */
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "结算异常";
			}
		},
		/**
		 * 已承接债权
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "已承接债权";
			}
		},
		/**
		 * 债权转让中
		 */
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "债权转让中";
			}
		},
		/**
		 * 已流标
		 */
		H{
			public String val(){
				return "H";
			}
			public String desc(){
				return "已流标";
			}
		},
		/**
		 * 已撤回
		 */
		I{
			public String val(){
				return "I";
			}
			public String desc(){
				return "已撤回";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 还款方式
	 */
	public enum refundType {
		
		/**
		 * 按月等额本息
		 */
		A{
			public String val(){
				return "A";
			}
			
			public String desc(){
				return "按月等额本息";
			}
		},
		/**
		 * 按月付息,到期还款
		 */
		B{
			public String val(){
				return "B";
			}
			
			public String desc(){
				return "按月付息,到期还款";
			}
		},
		/**
		 * 到期还本息
		 */
		C{
			public String val(){
				return "C";
			}
			
			public String desc(){
				return "到期还本息";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 标类型
	 */
	public enum loanType{
		
		/**
		 * 信用标
		 */
		A{
			public String val(){
				return "A";
			}
			
			public String desc(){
				return "信用标";
			}
		},
		/**
		 * 抵押标
		 */
		B{
			public String val(){
				return "B";
			}
			
			public String desc(){
				return "抵押标";
			}
		},
		/**
		 * 担保标
		 */
		C{
			public String val(){
				return "C";
			}
			
			public String desc(){
				return "担保标";
			}
		},
		/**
		 * 流转标
		 */
		D{
			public String val(){
				return "D";
			}
			
			public String desc(){
				return "流转标";
			}
		},
		/**
		 * 质押标
		 */
		E{
			public String val(){
				return "E";
			}
			
			public String desc(){
				return "质押标";
			}
		},
		/**
		 * 抵押担保标
		 */
		F{
			public String val(){
				return "F";
			}
			
			public String desc(){
				return "抵押担保标";
			}
		},
		/**
		 * 抵押流转标
		 */
		G{
			public String val(){
				return "G";
			}
			
			public String desc(){
				return "抵押流转标";
			}
		},
		/**
		 * 担保流转标
		 */
		H{
			public String val(){
				return "H";
			}
			
			public String desc(){
				return "担保流转标";
			}
		},
		/**
		 * 质押流转标
		 */
		I{
			public String val(){
				return "I";
			}
			
			public String desc(){
				return "质押流转标";
			}
		},
		/**
		 * 质押流转标
		 */
		J{
			public String val(){
				return "J";
			}
			
			public String desc(){
				return "机构标";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 借款用途
	 */
	public enum loanUsedType{
		/**
		 * 短期周转
		 */
		A{
			public String val(){
				return "A";
			}
			
			public String desc(){
				return "短期周转";
			}
		},
		/**
		 * 个人消费
		 */
		B{
			public String val(){
				return "B";
			}
			
			public String desc(){
				return "个人消费";
			}
		},
		/**
		 * 投资创业
		 */
		C{
			public String val(){
				return "C";
			}
			
			public String desc(){
				return "投资创业";
			}
		},
		/**
		 * 购车借款
		 */
		D{
			public String val(){
				return "D";
			}
			
			public String desc(){
				return "购车借款";
			}
		},
		/**
		 * 装修借款
		 */
		E{
			public String val(){
				return "E";
			}
			
			public String desc(){
				return "装修借款";
			}
		},
		/**
		 * 婚礼筹备
		 */
		F{
			public String val(){
				return "F";
			}
			
			public String desc(){
				return "婚礼筹备";
			}
		},
		/**
		 * 教育培训
		 */
		G{
			public String val(){
				return "G";
			}
			
			public String desc(){
				return "教育培训";
			}
		},
		/**
		 * 医疗支出
		 */
		H{
			public String val(){
				return "H";
			}
			
			public String desc(){
				return "医疗支出";
			}
		},
		/**
		 * 其它借款
		 */
		I{
			public String val(){
				return "I";
			}
			
			public String desc(){
				return "其它借款";
			}
		},
		/**
		 * 购房借款
		 */
		J{
			public String val(){
				return "J";
			}
			
			public String desc(){
				return "购房借款";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 用户状态(网站用户)
	 */
	public enum userState{
		/**
		 * 正常
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "正常";
			}
		},
		/**
		 * 人工冻结
		 */
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "人工冻结";
			}
		},
		/**
		 * 系统冻结
		 */
		S{
			public String val(){
				return "S";
			}
			public String desc(){
				return "系统冻结";
			}
		},
		/**
		 * 未投资放行
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "未投资放行";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 申请发标状态
	 */
	public enum applyState{
		/**
		 * 待信审
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "待信审";
			}
		},
		/**
		 * 临时保存
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "临时保存";
			}
		},
		/**
		 * 信审失败
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "信审失败";
			}
		},
		/**
		 * 信审通过
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "信审通过";
			}
		},/**
		 * 风控审核失败
		 */
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "风控审核失败";
			}
		},
		/**
		 * 风控审核通过
		 */
		H{
			public String val(){
				return "H";
			}
			public String desc(){
				return "风控审核通过";
			}
		},
		
		/**
		 * 进行中
		 */
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "已制作新标";
			}
		},
		Y{
			public String val(){
				return "Y";
			}
			public String desc(){
				return "已放款";
			}
		};
		public abstract String val();
		public abstract String desc();
		
	}
	
	public enum productType{
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "质押宝";
			}
		},
		/**
		 * 进行中
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "车稳赢";
			}
		},
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "房稳赚";
			}
		},
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "稳定投";
			}
		},
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "其它";
			}
		},
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "易分期";
			}
		};
		public abstract String val();
		public abstract String desc();
		
	}
	
	public enum makeSource{
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "系统";
			}
		},
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "活动";
			}
		},
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "返现";
			}
		},
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "人工赠送";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	public enum ttype{
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "现金抵用券";
			}
		},
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "投资返现券";
			}
		},
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "加息券";
			}
		};
		
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * 借款人还款计划表还款状态
	 */
	public enum settlementState{
		/**
		 * 待结算
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "待结算";
			}
		},
		/**
		 * 正常还款
		 */
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "正常还款完成";
			}
		},
		/**
		 * 提前还款完成
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "提前还款完成";
			}
		},
		/**
		 * 已取消
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "取消";
			}
		},
		/**
		 * 正常还款中
		 */
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "正常还款中";
			}
		},
		/**
		 * 提前还款中
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "提前还款中";
			}
		},
		/**
		 * 逾期还款中
		 */
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "逾期还款中";
			}
		},
		/**
		 * 逾期还款完成
		 */
		H{
			public String val(){
				return "H";
			}
			public String desc(){
				return "逾期还款完成";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	public enum FuiouTraceType{
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "充值成功";
			}
		},
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "提现成功";
			}
		},
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "冻结";
			}
		},
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "解冻";
			}
		},
		E{
			public String val(){
				return "E";
			}
			public String desc(){
				return "收入";
			}
		},
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "支出";
			}
		},
		G{
			public String val(){
				return "G";
			}
			public String desc(){
				return "放款抵用券";
			}
		},
		H{
			public String val(){
				return "H";
			}
			public String desc(){
				return "放款抵用券失败";
			}
		},
		I{
			public String val(){
				return "I";
			}
			public String desc(){
				return "提现手续费";
			}
		},
		J{
			public String val(){
				return "J";
			}
			public String desc(){
				return "提现手续费失败";
			}
		},
		K{
			public String val(){
				return "K";
			}
			public String desc(){
				return "冻结失败";
			}
		},
		L{
			public String val(){
				return "L";
			}
			public String desc(){
				return "解冻失败";
			}
		},
		M{
			public String val(){
				return "M";
			}
			public String desc(){
				return "收入失败";
			}
		},
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "支出失败";
			}
		},
		O{
			public String val(){
				return "O";
			}
			public String desc(){
				return "充值失败";
			}
		},
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "提现失败";
			}
		},MANUAL_RECHARGE {
			public String val() {return "Q";}
			public String desc() {return "人工充值";}
		},
		Q{
			public String val(){
				return "Q";
			}
			public String desc(){
				return "人工充值";
			}
		},R{
			public String val(){
				return "R";
			}
			public String desc(){
				return "满标放款";
			}
		},
		HFK{
			public String val(){
				return "HFK";
			}
			public String desc(){
				return "后台放款";
			}
		},
		S{
			public String val(){
				return "S";
			}
			public String desc(){
				return "满标放款失败";
			}
		},T{
			public String val(){
				return "T";
			}
			public String desc(){
				return "债权转让";
			}
		},U{
			public String val(){
				return "U";
			}
			public String desc(){
				return "债权转让失败";
			}
		},V{
			public String val(){
				return "V";
			}
			public String desc(){
				return "债权服务费+抵用券";
			}
		},W{
			public String val(){
				return "W";
			}
			public String desc(){
				return "债权服务费+抵用券获取失败";
			}
		},X{
			public String val(){
				return "X";
			}
			public String desc(){
				return "批量划拨";
			}
		},Y{
			public String val(){
				return "Y";
			}
			public String desc(){
				return "批量划拨失败";
			}
		},Z{
			public String val(){
				return "Z";
			}
			public String desc(){
				return "划拨失败";
			}
		},HKL{
			public String val(){
				return "HKL";
			}
			public String desc(){
				return "回款利息";
			}
		},HKB{
			public String val(){
				return "HKB";
			}
			public String desc(){
				return "回款本金";
			}
		},HKLE{
			public String val(){
				return "HKLE";
			}
			public String desc(){
				return "回款利息失败";
			}
		},HKBE{
			public String val(){
				return "HKBE";
			}
			public String desc(){
				return "回款本金失败";
			}
		}, INCOME {
			public String val() {
				return "E";
			}
			public String desc() {
				return "收入";
			} 
		}, INCOME_ERR {
			public String val() {
				return "M";
			}
			public String desc() {
				return "收入失败";
			}
		}, PAY_ERR {
			public String val() {
				return "N";
			}
			public String desc() {
				return "支出失败";
			}
		}, PAY {
			public String val() {
				return "F";
			}
			public String desc() {
				return "支出";
			}
		}, FREEZE {
			public String val() {
				return "C";
			}
			public String desc() {
				return "冻结";
			}
		}, FREEZE_ERR {
			public String val() {
				return "K";
			}
			public String desc() {
				return "冻结失败";
			}
		}, REPAY_INTEREST {
			public String val() {
				return "HKL";
			}
			public String desc() {
				return "回款利息";
			}
		}, REPAY_INTEREST_ERR {
			public String val() {
				return "HKLE";
			}
			public String desc() {
				return "回款利息失败";
			}
		}, REPAY_PRINCIPAL {
			public String val() {
				return "HKB";
			}
			public String desc() {
				return "回款本金";
			}
		}, REPAY_PRINCIPAL_ERR {
			public String val() {
				return "HKBE";
			}
			public String desc() {
				return "回款本金失败";
			}
		}, RECHARGE {
			public String val() {return "PW11";}
			public String desc() {return "充值";}
		}, WITHDRAW {
			public String val() {return "PWTX";}
			public String desc() {return "提现";}
		};
		
		public abstract String val();
		public abstract String desc();
		
	}
	public enum UserType{
		J{
			public String val(){
				return "J";
			}
			public String desc(){
				return "借款人";
			}
		},T{
			public String val(){
				return "T";
			}
			public String desc(){
				return "投资人";
			}
		}, C{
			public String val() {return "C";}
			public String desc() {return "借款公司";}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	public enum SMSType{
		REG{
			public int val(){return 0;}
			public String desc(){return "用户注册";}
		},
		BIRTHDAY{
			public int val(){return 17;}
			public String desc(){return "生日短信";}
		};
		public abstract int val();
		public abstract String desc();
	}
	
	public enum StoreNo{
		ZZ01{
			public String val(){return "汉街分公司";}
			public String desc(){return "ZZ01";}
		},
		ZZ02{
			public String val(){return "汉口分公司";}
			public String desc(){return "ZZ02";}
		},
		ZZ03{
			public String val(){return "安庆分公司";}
			public String desc(){return "ZZ03";}
		},
		ZZ04{
			public String val(){return "娄底分公司";}
			public String desc(){return "ZZ04";}
		},
		ZZ05{
			public String val(){return "昆明分公司";}
			public String desc(){return "ZZ05";}
		},
		ZZ06{
			public String val(){return "曲靖分公司";}
			public String desc(){return "ZZ06";}
		},
		ZZ07{
			public String val(){return "楚雄分公司";}
			public String desc(){return "ZZ07";}
		},
		QT01{
			public String val(){return "成都齐通商务信息咨询有限公司";}
			public String desc(){return "QT01";}
		},
		QT02{
			public String val(){return "武汉世纪齐通商务信息有限公司";}
			public String desc(){return "QT02";}
		},
		QT03{
			public String val(){return "武汉弘盛齐通商务咨询服务有限公司";}
			public String desc(){return "QT03";}
		},
		QT04{
			public String val(){return "武汉盛世齐通商务信息有限公司";}
			public String desc(){return "QT04";}
		},
		QT05{
			public String val(){return "黄石齐通商务信息咨询有限公司";}
			public String desc(){return "QT05";}
		},
		QT06{
			public String val(){return "兰州齐通商务信息咨询有限公司";}
			public String desc(){return "QT06";}
		},
		QT07{
			public String val(){return "十堰齐通商务信息有限公司";}
			public String desc(){return "QT07";}
		},
		QT08{
			public String val(){return "咸宁齐通商务信息咨询有限公司";}
			public String desc(){return "QT08";}
		},
		QT09{
			public String val(){return "孝感齐通商务信息咨询有限公司";}
			public String desc(){return "QT09";}
		},
		QT10{
			public String val(){return "南京齐通商务服务有限公司";}
			public String desc(){return "QT10";}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	public enum AppSource {
		IOS{
			public String val(){return "IOS";}
		},
		Android{
			public String val(){return "Android";}
		},
		Other{
			public String val(){return "Other";}
		};
		public abstract String val();
	}
	
	/**
	 * 逾期类型
	 */
	public enum loanOverdueType{
		/**
		 * 还本金利息(all)
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "还本金利息";
			}
		},
		/**
		 * 还本金,不还利息(principal)
		 */
		P{
			public String val(){
				return "P";
			}
			public String desc(){
				return "还本金";
			}
		},
		/**
		 * 还利息,不还本金(interest)
		 */
		I{
			public String val(){
				return "I";
			}
			public String desc(){
				return "还利息";
			}
		},
		/**
		 * 不还本金利息(none)
		 */
		N{
			public String val(){
				return "N";
			}
			public String desc(){
				return "不还本金利息";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * jxTrace交易发送状态
	 */
	public enum jxTraceStatus{
		/**
		 * 发送失败
		 */
		F{
			public String val(){
				return "F";
			}
			public String desc(){
				return "发送失败";
			}
		},
		/**
		 * 待发送
		 */
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "待发送";
			}
		},
		/**
		 * 发送成功
		 */
		D{
			public String val(){
				return "D";
			}
			public String desc(){
				return "发送成功";
			}
		},
		/**
		 * 撤销
		 */
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "撤销";
			}
		},
		/**
		 * 人工处理完成
		 */
		M{
			public String val(){
				return "M";
			}
			public String desc(){
				return "人工处理完成";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	
	/**
	 * jx交易代码
	 *
	 */
	public enum jxTxCode{
		/**
		 * 红包发放
		 */
		voucherPay{
			public String val(){
				return "voucherPay";
			}
			public String desc(){
				return "红包发放";
			}
		},
		/**
		 * 批次代偿
		 */
		batchSubstRepay{
			public String val(){
				return "batchSubstRepay";
			}
			public String desc(){
				return "批次代偿";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
	/**
	 * 债权转让状态
	 * */
	public enum transState{
		A{
			public String val(){
				return "A";
			}
			public String desc(){
				return "债转中";
			}
		},
		B{
			public String val(){
				return "B";
			}
			public String desc(){
				return "债转成功";
			}
		},
		C{
			public String val(){
				return "C";
			}
			public String desc(){
				return "债转取消";
			}
		};
		public abstract String val();
		public abstract String desc();
	}
}