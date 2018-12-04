package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class OPUserCreateOPUserValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("op_mobile", "validatorMsg_userMobile", "请检查 手机号");
		validateRequiredString("op_name", "validatorMsg_userName", "请检查 用户名");
		validateRequiredString("op_pwd", "validatorMsg_userPwd", "请检查 用户登录密码");
		//validateRequiredString("op_map", "validatorMsg_roleMap", "请检查 用户角色");
		validateRequiredString("op_group", "validatorMsg_userGroup", "请检查用户 归属组织");
	}
	
}
