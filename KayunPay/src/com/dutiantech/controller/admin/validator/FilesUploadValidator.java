package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class FilesUploadValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("fileType", "validatorMsg_fileType", "文件类型不可为空");
		validateRequiredString("fileRemark", "validatorMsg_fileRemark", "文件描述不可为空");
		validateRequiredString("userCode", "validatorMsg_userCode", "文件关联用户不可为空");
	}

}
