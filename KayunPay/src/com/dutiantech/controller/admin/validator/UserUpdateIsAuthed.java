package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class UserUpdateIsAuthed extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("paraUserCode", "validatorMsg_paraUserCode", "用户编码不可为空");
		validateRequiredString("isAuthed", "validatorMsg_isAuthed", "是否通过认证 不可为空");
	}

}
