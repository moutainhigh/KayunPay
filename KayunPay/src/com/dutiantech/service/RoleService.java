package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.RoleV2;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class RoleService extends BaseService {

	public List<RoleV2> queryRoleList(){
		return RoleV2.roleDao.find("select role_code,role_name,role_group,role_status,role_desc from t_role_v2") ;
	}
	
	public boolean saveRole(RoleV2 role){
		role.put("role_code" , UIDUtil.generate());
		role.put("role_status", "A") ;
		return role.save() ;
	}
	
	public boolean modRole(RoleV2 role){
		
		return role.update() ;
	}
	
	public RoleV2 queryRoleByCode(String roleCode){
		return RoleV2.roleDao.findById(roleCode) ;
	}
	
	public RoleV2 queryRoleByName(String roleName){
		return RoleV2.roleDao.findFirst("select * from t_role_v2 where role_name = ?",roleName);
	}
	
	public Page<RoleV2> findByPage(Integer pageNumber, Integer pageSize,String allkey,String roleStatus){
		String sqlSelect = "select *";
		String sqlFrom = " from t_role_v2";
		
		StringBuffer buff = new StringBuffer("");
		List<Object> paras=new ArrayList<Object>();
		makeExp(buff, paras, "role_status", "=", roleStatus, "and");
		String[] keys = new String[]{"role_name"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		return RoleV2.roleDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff),paras.toArray());
	}
	
	public boolean updateById(String roleCode,Map<String,Object> para){
		RoleV2 roleV2 = RoleV2.roleDao.findById(roleCode);
		roleV2._setAttrs(para);
		return roleV2.update();
	}
	
	public boolean delectRole(String roleCode){
		return RoleV2.roleDao.deleteById(roleCode);
	}
	
}
