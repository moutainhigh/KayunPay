package com.dutiantech.controller.admin.validator;

import com.jfinal.core.Controller;

public class UserUpdateUserInfoValidator extends BaseValidator {

	@Override
	protected void validate(Controller c) {
		validateRequiredString("paraUserCode","validatorMsg_paraUserCode", "用户编码不可为空");
//		validateRequiredString("userCardName","validatorMsg_userCardName","请检查 真实姓名");
//		validateRequiredString("userCardId","validatorMsg_userCardId","请检查 身份证编号");
//		validateRequiredString("userAddress","validatorMsg_userAddress","请检查 登记地址");
//		validateRequiredString("ecpNme1","validatorMsg_ecpNme1","请检查 紧急联系人1姓名");
//		validateRequiredString("ecpRlation1","validatorMsg_ecpRlation1","请检查 紧急联系人1与本人关系");
//		validateRequiredString("ecpMbile1","validatorMsg_ecpMbile1","请检查 紧急联系人1联系电话");
//		validateRequiredString("ecpNme2","validatorMsg_ecpNme2","请检查 紧急联系人2姓名");
//		validateRequiredString("ecpRlation2","validatorMsg_ecpRlation2","请检查 紧急联系人2与本人关系");
//		validateRequiredString("ecpMbile2","validatorMsg_ecpMbile2","请检查 紧急联系人2联系电话");
	}

}
