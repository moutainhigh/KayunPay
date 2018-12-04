package com.dutiantech.model;

import com.jfinal.plugin.activerecord.Model;
/**
 * 系统日志
 * @author shiqingsong
 *
 */
public class BizLog extends Model<BizLog>{

	public enum BIZ_TYPE{
		LOGIN {
			public String key(){
				return "A";
			};
			public String desc(){
				return "登录";
			}
		},
		DOLOAN {
			public String key(){
				return "B";
			};
			public String desc(){
				return "投标";
			}
		},
		REGISTER {
			public String key(){
				return "C";
			};
			public String desc(){
				return "注册";
			}
		},
		WITHDRAWALS {
			public String key(){
				return "D";
			};
			public String desc(){
				return "提现";
			}
		},
		BANK {
			public String key(){
				return "E";
			};
			public String desc(){
				return "银行卡";
			}
		},
		USER{
			public String key(){
				return "F";
			};
			public String desc(){
				return "用户信息";
			}
		},
		USERINFO {
			public String key(){
				return "G";
			};
			public String desc(){
				return "用户扩展信息";
			}
		},
		AUTOLOAN {
			public String key(){
				return "H";
			};
			public String desc(){
				return "自动投标";
			}
		},
		FINDPWD {
			public String key(){
				return "I";
			};
			public String desc(){
				return "帐号密码";
			}
		},
		FILEINFO {
			public String key(){
				return "J";
			};
			public String desc(){
				return "文件信息";
			}
		},
		EMAIL {
			public String key(){
				return "K";
			};
			public String desc(){
				return "邮箱";
			}
		},
		TRANSFER {
			public String key(){
				return "L";
			};
			public String desc(){
				return "债权转让";
			}
		},
		MAINPAGE {
			public String key(){
				return "M";
			};
			public String desc(){
				return "主页";
			}
		},
		POINT {//可用积分
			public String key(){
				return "N";
			};
			public String desc(){
				return "可用积分操作";
			}
		},
		IDENTIFY{
			public String key() {
				return "O";
			};
			public String desc() {
				return "实名";
			}
		},
		RECHARGE {
			public String key(){
				return "V";
			};
			public String desc(){
				return "充值";
			}
		},SYNC{
			
			public String key(){
				return "S";
			};
			public String desc(){
				return "同步账户";
			}
		},TLIVE{
			
			public String key(){
				return "T";
			};
			public String desc(){
				return "激活账户成功";
			}
		},TERROR{

			public String key() {
				return "U";
			}

			@Override
			public String desc() {
				return "绑卡失败";
			}
			
		},R{
			
			public String key() {
				return "R";
			}

			@Override
			public String desc() {
				return "放款";
			}
		}
		;
		public abstract String key();
		public abstract String desc();
	}
	
	private static final long serialVersionUID = -1688985299661927575L;
	
	public static final BizLog bizLogDao = new BizLog();
	
}
