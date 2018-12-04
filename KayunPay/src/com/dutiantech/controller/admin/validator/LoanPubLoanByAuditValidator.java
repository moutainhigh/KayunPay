package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanPubLoanByAuditValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("loanCode", "validatorMsg_loanCode", "贷款标 编码不可为空");
		validateRequiredString("pubDate", "validatorMsg_pubDate", "发表日期不可为空");
		validateRequiredString("pubTime", "validatorMsg_pubTime", "发标时间不可为空");
	}

}
