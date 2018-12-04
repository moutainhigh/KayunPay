package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanCreateBasicLoanValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("loanTitle", "validatorMsg_loanTitle", "请检查 标题");
		validateRequiredString("loanType", "validatorMsg_loanType", "请检查 贷款标类型");
		validateRequiredString("loanTypeDesc", "validatorMsg_loanTypeDesc", "标类型描述不可为空");
		validateInteger("loanAmount", "validatorMsg_loanAmount", "请检查 贷款金额");
		validateRequiredString("loanArea", "validatorMsg_loanArea", "请检查 贷款地区");
		validateInteger("loanTimeLimit", "validatorMsg_loanTimeLimit", "请检查 还款期限");
		validateRequiredString("refundType", "validatorMsg_refundType", "请检查 还款方式");
		validateInteger("rateByYear", "validatorMsg_rateByYear", "请检查 年利率");
		validateRequiredString("loanUsedType", "validatorMsg_loanUsedType", "请检查 借款用途");
		validateRequiredString("loanUserCode", "validatorMsg_loanUserCode", "借款人不可为空");
		
	}

}
