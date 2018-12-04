package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class OPUserResetOPUserPasswordValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("op_code", "validatorMsg_opcode", "请检查 是否选择用户");
	}
	
}
