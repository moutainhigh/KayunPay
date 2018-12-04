package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class OPUserModifyValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("op_code", "validatorMsg_userMobile", "用户编码不合法");
		validateRequiredString("op_name", "validatorMsg_userName", "请检查 用户昵称");
		validateRequiredString("roleMap", "validatorMsg_roleMap", "请检查 用户角色");
		validateRequiredString("op_group", "validatorMsg_roleMap", "请检查 用户组织");
	}
	
}
