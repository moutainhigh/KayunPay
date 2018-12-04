package com.dutiantech.model;

public class Deposit {
	public enum authorization {

		PAYMENT_AUTH {
			public String val() {
				return "PAYMENT";
			}

			public String desc() {
				return "缴费授权";
			}
		},
		PASSWORD_SET {
			public String val() {
				return "PWD";
			}

			public String desc() {
				return "交易密码";
			}
		},
		CARD_BIND {
			public String val() {
				return "CARD";
			}

			public String desc() {
				return "绑卡关系";
			}
		};

		public abstract String val();

		public abstract String desc();
	}
}
