package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.Permit;
import com.dutiantech.util.StringUtil;

public class PermitService {
	
	public Permit findById(String permit_index){
		return Permit.permitDao.findById(permit_index);
	}
	
	public List<Permit> findAll(){
		return Permit.permitDao.find("select * from t_permit");
	}
	
	public boolean save(String role_id, String menu_id, String menu_flag){
		Permit permit = new Permit();
		if(!StringUtil.isBlank(role_id))
			return false;
		else if(!StringUtil.isBlank(menu_id))
			return false;
		else if(!StringUtil.isBlank(menu_flag))
			return false;
		permit.set("menu_id", menu_id);
		permit.set("menu_flag", menu_flag);
		permit.set("role_id", role_id);
		permit.set("permit_index", role_id+menu_id);
		return permit.save();
	}
	
	public boolean updateFlag(String idValue, String menu_flag){
		Permit permit = Permit.permitDao.findById(idValue);
		if(!StringUtil.isBlank(menu_flag))
			return false;
		else if(StringUtil.isBlank(idValue))
			return false;
		permit.set("menu_flag", menu_flag);
		return permit.update();
	}
	
	public boolean deleteById(String permit_index){
		return Permit.permitDao.deleteById(permit_index);
	}

}
