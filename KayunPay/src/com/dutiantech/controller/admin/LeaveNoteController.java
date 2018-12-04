package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.LeaveNote;
import com.dutiantech.service.LeaveNoteService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class LeaveNoteController extends BaseController{

	private LeaveNoteService leaveNoteService = getService(LeaveNoteService.class);
	
	@ActionKey("/queryLeaveNotesAdmin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryLeaveNotesAdmin(){
		
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String userMobile = getPara("userMobile");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		Page<LeaveNote> leaveNotes = leaveNoteService.queryLeaveNotes(pageNumber, pageSize, userMobile);
		Message msg = succ("查询成功", leaveNotes);
		renderJson(msg);
	}
}
