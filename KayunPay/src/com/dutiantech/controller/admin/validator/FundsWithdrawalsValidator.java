package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FundsWithdrawalsValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateInteger("amount", "validatorMsg_amount", "提现金额不可为空");
		validateRequiredString("fundsUserCode", "validatorMsg_fundsUserCode", "用户不可为空");;
	}
	
}
