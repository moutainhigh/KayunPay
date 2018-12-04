package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanDeleteNoticeValidator extends BaseValidator {
	
	@Override
	protected void validate(Controller c) {
		validateRequiredString("noticeCode", "validatorMsg_noticeCode", "发布公告编码不可为空");
	}
}
