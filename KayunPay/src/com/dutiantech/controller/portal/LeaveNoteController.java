package com.dutiantech.controller.portal;

import java.util.regex.Pattern;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.LeaveNote;
import com.dutiantech.model.User;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class LeaveNoteController extends BaseController{
//	private LeaveNoteService leaveNoteService = getService(LeaveNoteService.class);
	private UserService userService = getService(UserService.class);
	
	/**
	 * 
	 * */
	@ActionKey("/saveLeaveNote")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message saveLeaveNote(){
		String userMobile = getPara("phone");
		String userTrueName = getPara("name");
		String userName = getPara("username");
		User user = userService.findByMobile(userMobile);
		if(null==user){
			return error("01", "请先注册易融恒信平台帐号", "");
		}
		if(userName.equals(user.getStr("userName"))&&userTrueName.equals(user.getStr("userCardName"))){
			String eMail = getPara("email");
			String regex = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*";
			if(Pattern.matches(regex, eMail)){
				String leaveNoteMsg = getPara("txtval");
				if(StringUtil.isBlank(leaveNoteMsg)){
					return error("04", "留言内容不可为空", "");
				}else{
					LeaveNote leaveNote = new LeaveNote();
					leaveNote.set("leaveNoteCode", UIDUtil.generate());
					leaveNote.set("userCode", user.getStr("userCode"));
					leaveNote.set("userName", userName);
					leaveNote.set("userTrueName", userTrueName);
					try {
						userMobile = CommonUtil.encryptUserMobile(userMobile);
					} catch (Exception e) {
						return error("05", "系统错误", "");
					}
					leaveNote.set("userMobile", userMobile);
					leaveNote.set("leaveNote", leaveNoteMsg);
					leaveNote.set("addDateTime", DateUtil.getNowDateTime());
					if(leaveNote.save()){
						return succ("OK", "提交成功");
					}else{
						return error("06", "提交失败", "");
					}
				}
			}else{
				return error("03", "邮箱地址不正确","");
			}
		}else{
			return error("02", "手机号与姓名或用户名不匹配","");
		}
	}
}
