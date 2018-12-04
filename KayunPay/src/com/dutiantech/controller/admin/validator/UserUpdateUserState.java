package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class UserUpdateUserState extends BaseValidator {

	protected void validate(Controller c) {
		validateRequiredString("paraUserCode", "validatorMsg_paraUserCode", "用户编码不可为空");
		validateRequiredString("userState", "validatorMsg_userState", "请检查 用户状态");
	}

}
