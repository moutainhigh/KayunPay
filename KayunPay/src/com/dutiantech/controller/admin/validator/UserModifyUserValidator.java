package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class UserModifyUserValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("paraUserCode", "validatorMsg_paraUserCode", "用户编码不可为空");
	}

}
