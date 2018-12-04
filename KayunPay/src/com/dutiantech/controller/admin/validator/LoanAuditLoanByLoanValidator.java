package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanAuditLoanByLoanValidator extends BaseValidator{

	@Override
	protected void validate(Controller c) {
		validateRequiredString("loanCode", "validateMsg_loanCode", "贷款标编码不可为空");;
	}
	
}
