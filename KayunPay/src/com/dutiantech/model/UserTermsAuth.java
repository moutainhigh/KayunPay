package com.dutiantech.model;

import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Model;

public class UserTermsAuth extends Model<UserTermsAuth> {

	private static final long serialVersionUID = -6254441670635025051L;

	public static final UserTermsAuth userTermsAuthDao = new UserTermsAuth();

	public boolean isAutoBid() {
		if ("1".equals(getStr("autoBid"))) {
			if (DateUtil.compareDateByStr("yyyyMMdd", getStr("autoBidDeadline"), DateUtil.getNowDate()) == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean isAutoCredit() {
		if ("1".equals(getStr("autoCredit"))) {
			if (DateUtil.compareDateByStr("yyyyMMdd", getStr("autoCreditDeadline"), DateUtil.getNowDate()) == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean isPaymentAuth() {
		if ("1".equals(getStr("paymentAuth"))) {
			if (DateUtil.compareDateByStr("yyyyMMdd", getStr("paymentDeadline"), DateUtil.getNowDate()) == 1) {
				return true;
			}
		}
		return false;
	}

	public boolean isRepayAuth() {
		if ("1".equals(getStr("repayAuth"))) {
			if (DateUtil.compareDateByStr("yyyyMMdd", getStr("repayDeadline"), DateUtil.getNowDate()) == 1) {
				return true;
			}
		}
		return false;
	}
}