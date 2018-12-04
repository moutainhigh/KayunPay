package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class LoanCreateNoticeValidator extends BaseValidator {
	
	@Override
	protected void validate(Controller c) {
		validateRequiredString("content", "validatorMsg_content", "发标公告内容不可为空");
		validateRequiredString("overDateTime", "validatorMsg_overDateTime", "发标公告发布时间不可为空");
	}
}
