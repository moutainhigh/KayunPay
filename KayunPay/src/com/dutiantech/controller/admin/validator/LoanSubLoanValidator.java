package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanSubLoanValidator extends BaseValidator {

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
		
		validateInteger("hasInvedByTrips", "validatorMsg_hasInvedByTrips", "请检查 是否实地考察");
		validateInteger("isInterest", "validatorMsg_isInterest", "请检查 本息保障");
		validateInteger("isAutoLoan", "validatorMsg_isAutoLoan", "请检查 自动放款");
		validateInteger("hasCaptcha", "validatorMsg_hasCaptcha", "请检查 是否需要验证码");
		validateInteger("invedTripFees", "validatorMsg_invedTripFees", "请检查 实地考察费用");
		validateInteger("serviceFees", "validatorMsg_serviceFees", "请检查 服务费");
		validateInteger("managerRate", "validatorMsg_managerRate", "请检查 管理费率");
		validateInteger("rateByYear", "validatorMsg_rateByYear", "请检查 年利率");
		validateInteger("rewardRateByYear", "validatorMsg_rewardRateByYear", "请检查 奖励年利率");
		validateInteger("minLoanAmount", "validatorMsg_minLoanAmount", "请检查 最小投标金额");
		validateInteger("maxLoanAmount", "validatorMsg_maxLoanAmount", "请检查 最大投标金额");
		validateInteger("benefits4new", "validatorMsg_benefits4new", "请检查 新人专享福利");
		validateInteger("maxLoanCount", "validatorMsg_maxLoanCount", "请检查 最大投标次数");
		validateRequiredString("loanBaseDesc", "validatorMsg_loanBaseDesc", "请检查 基本说明");
		validateRequiredString("loanUsedDesc", "validatorMsg_loanUsedDesc", "请检查 借款用途描述");
		validateRequiredString("loanerDataDesc", "validatorMsg_loanerDataDesc", "请检查 借款人提供的资料描述");
		validateRequiredString("loanInvDesc", "validatorMsg_loanInvDesc", "请检查 借款人被考察描述");
		validateRequiredString("loanRiskDesc", "validatorMsg_loanRiskDesc", "请检查 风控审核描述");
		validateRequiredString("loanCode", "validatorMsg_loanCode", "贷款编码不可为空");
	}

}
