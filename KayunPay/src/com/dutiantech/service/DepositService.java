package com.dutiantech.service;

import java.util.List;
import java.util.Map;

import com.dutiantech.controller.JXQueryController;
import com.dutiantech.model.Deposit.authorization;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;

public class DepositService extends BaseService{

	/**
	 * 验证是否开通存管
	 * @param user	验证用户
	 * @param validPort	验证通道，0-本地验证；1-线上验证, 默认为本地验证
	 * @return
	 */
	public boolean checkDepositStatus(User user, String... validPort) {
		if (CommonUtil.jxPort) {
			if (StringUtil.isBlank(user.getStr("jxAccountId"))) {
				return false;
			}
		} else if (CommonUtil.fuiouPort) {
			if (StringUtil.isBlank(user.getStr("loginId"))) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
	
	/**
	 * 验证用户存管授权
	 * @param user	验证用户
	 * @param name	验证类型[是否绑卡、是否设置交易密码、是否开通缴费授权]，默认为验证所有
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public boolean checkAuthorization(User user, authorization...name) {
		String jxAccountId = user.getStr("jxAccountId");
//		String jxAccountId = "6212462490000070010";
		if (name.length <= 0) {
			name = new authorization[]{authorization.CARD_BIND, authorization.PASSWORD_SET, authorization.PAYMENT_AUTH};
		}
		for (authorization auth : name) {
			// 验证存管账户是否绑卡
			if (auth.equals(authorization.CARD_BIND)) {
				Map<String, Object> cardDetail = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
				if (cardDetail != null && "00000000".equals(cardDetail.get("retCode"))) {
					List<Map<String, String>> list = (List<Map<String, String>>) cardDetail.get("subPacks");
					if (list == null || list.size() <= 0) {
						return false;
					}
				}
			}
			// 验证存管账户是否是否设置交易密码
			if (auth.equals(authorization.PASSWORD_SET)) {
				Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
				if (pwdMap == null || !"1".equals(pwdMap.get("pinFlag"))) {
					return false;
				}
			}
			// 验证存管账户是否开通缴费授权
			if (auth.equals(authorization.PAYMENT_AUTH)) {
				Map<String, String> authDetail = JXQueryController.termsAuthQuery(jxAccountId);
				if ("1".equals(authDetail.get("paymentAuth"))) {	// 已开通缴费授权，验证是否已过期
					String paymentDeadline = authDetail.get("paymentDeadline");
					int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline, DateUtil.getNowDate());
					if (x < 0) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		DepositService service = new DepositService();
		service.checkAuthorization(null);
	}
}
