package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.LeaveNote;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Page;

public class LeaveNoteService extends BaseService{
	
	public Page<LeaveNote> queryLeaveNotes(Integer pageNumber, Integer pageSize, String userMobile){
		String sqlSelect = "select * ";
		String sqlFrom = "from t_leave_note ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		if(!StringUtil.isBlank(userMobile)){
			try {
				userMobile = CommonUtil.encryptUserMobile(userMobile);
				makeExp(buff, ps, "userMobile", "=", userMobile, "and");
			} catch (Exception e) {
				return null;
			}
		}
		String sqlOrder = " order by addDateTime desc";
		return LeaveNote.leaveNoteDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + makeSql4Where(buff) + sqlOrder,ps.toArray());
	}
}
