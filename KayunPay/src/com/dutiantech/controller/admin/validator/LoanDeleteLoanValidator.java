package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanDeleteLoanValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("loanCode", "validatorMsg_loanCode", "标书编码不可为空");
	}

}
