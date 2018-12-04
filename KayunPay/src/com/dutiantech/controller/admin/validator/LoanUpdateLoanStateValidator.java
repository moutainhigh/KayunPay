package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanUpdateLoanStateValidator extends BaseValidator {

	protected void validate(Controller c) {
		validateRequiredString("loanCode", "validatorMsg_loanCode", "贷款标 编码不可为空");
		validateRequiredString("loanState", "validatorMsg_loanState", "请检查 贷款标 状态");
		validateRequiredString("oLoanState", "validatorMsg_oLoanState", "贷款标 当前标状态异常");
	}

}
