package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FundsRechargeValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateInteger("amount","validatorMsg_amount","请检查 充值金额");
	}
	
}
