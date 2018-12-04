package com.dutiantech.controller.portal.validator;

import com.dutiantech.controller.admin.validator.BaseValidator;
import com.jfinal.core.Controller;

public class RegisterValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("userMobile","validatorMsg_userMobile","手机号不能为空");
		validateRequiredString("userName", "validatorMsg_userName", "用户名不能为空");
		validateRequiredString("loginPasswd","validatorMsg_loginPasswd","登录密码不能为空");
		validateRequiredString("uCheckCode","validatorMsg_uCheckCode","验证码不能为空");
	}

}
