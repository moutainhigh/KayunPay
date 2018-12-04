package com.dutiantech.controller.portal.validator;

import com.dutiantech.controller.admin.validator.BaseValidator;
import com.jfinal.core.Controller;

public class WithDrawalsValidate extends BaseValidator{
	
	@Override
	protected void validate(Controller c) {
		validateRequiredString("payPwd","validatorMsg_payPwd","支付密码不可为空");
		validateRequiredString("msgMac","validatorMsg_msgMac","短信验证码不可为空");
		validateRequiredString("bankCode","validatorMsg_bankCode","银行卡标识不可为空");
		validateRequiredString("isScore","validatorMsg_isScore","是否使用积分不可为空");
	}
	
}
