package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FundsConvertBalanceValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateInteger("amount", "validatorMsg_amount", "金额不可为空");
		validateRequiredString("fundsUserCode","validatorMsg_fundsUserCode","资金账户关联用户不可为空");
		validateInteger("type", 0, 1, "validatorMsg_e", "资金转换类型不可为空，1可用余额划入冻结余额 0冻结余额划入可用余额 ");
	}
	
}
