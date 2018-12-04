package com.dutiantech.controller.portal.validator;

import com.dutiantech.controller.admin.validator.BaseValidator;
import com.jfinal.core.Controller;

public class LoginValidate extends BaseValidator{
	
	@Override
	protected void validate(Controller c) {
		validateRequiredString("loginName","validatorMsg_loginName","登录名不可为空");
		validateRequiredString("loginPwd","validatorMsg_loginPwd","登录密码不可为空");
	}
	
}
