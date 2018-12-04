package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FundsUpdateTraceSynStateValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("traceCode","validatorMsg_traceCode","资金流水编码不可为空");
		validateRequiredString("traceSynState","validatorMsg_traceSynState","对账状态不可为空");
	}

}
