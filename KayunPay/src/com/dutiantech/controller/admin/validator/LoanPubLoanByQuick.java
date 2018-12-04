package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanPubLoanByQuick extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("loanCode", "validatorMsg_loanCode", "贷款标 编码不可为空");
	}

}
