package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FundsWithdrawalsByApplyValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("withdrawCode", "validatorMsg_applyCode", "提现申请记录异常");
	}
	
}
