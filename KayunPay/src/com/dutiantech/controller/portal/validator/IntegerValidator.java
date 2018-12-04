package com.dutiantech.controller.portal.validator;

import com.dutiantech.controller.admin.validator.BaseValidator;
import com.jfinal.core.Controller;

public class IntegerValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateInteger(0, "validatorMsg_Integer", "参数类型错误");
		validateLong(0, "validatorMsg_Long", "参数类型错误");
	}

}
