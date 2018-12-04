package com.dutiantech.controller.admin.validator;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.dutiantech.Message;
import com.jfinal.core.Controller;
import com.jfinal.validate.Validator;

public class BaseValidator extends Validator {

	@Override
	protected void validate(Controller c) {
		
	}

	@Override
	protected void handleError(Controller c) {
		Map<String, String> validatorMsgs = new HashMap<String, String>();
		Enumeration<String> vv = c.getRequest().getAttributeNames();
		for (Enumeration<String> zhazha = vv; zhazha.hasMoreElements();) {
			String attrName = zhazha.nextElement().toString();
			String attrValue = (String) c.getRequest().getAttribute(attrName);
			validatorMsgs.put(attrName, attrValue);
		}
		c.renderJson(Message.error("-315", "请检查内容是否输入合法", validatorMsgs));
	}

}
