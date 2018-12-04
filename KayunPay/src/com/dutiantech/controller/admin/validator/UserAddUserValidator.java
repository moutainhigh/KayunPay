package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class UserAddUserValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("userMobile","validatorMsg_userMobile","请检查 手机号");
		validateEmail("userEmail","validatorMsg_userEmail","请检查 邮箱");
		validateRequiredString("userName", "validatorMsg_userName", "请检查 用户昵称");
		validateRequiredString("loginPasswd","validatorMsg_loginPasswd","请检查 登录密码");
	}

}
