package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.RoleV2;
import com.dutiantech.service.RoleService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

@Before(value=AuthInterceptor.class)
public class RoleController extends BaseController {

	private RoleService roleService = getService(RoleService.class);

	/**角色列表查询
	 * 
	 * @return
	 */
	@ActionKey("/queryRoleV2List")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryRoleV2List() {
		Integer pageNumber = getParaToInt("pageNumber", 1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize", 10);
		
		String allkey=getPara("allkey","");
		String roleStatus=getPara("roleStatus","");
		
		Page<RoleV2> pageRoleV2s = roleService.findByPage(pageNumber, pageSize,allkey,roleStatus);
		return succ("分页查询完成",pageRoleV2s);
	}
	
	@ActionKey("/newRole")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message newRole(){
		RoleV2 role = getModel( RoleV2.class ) ;
		try {
			boolean result = roleService.saveRole(role);
			if( result )
				return succ("ok", result );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error("no", "新增角色失败" , null );
	}
	
	@ActionKey("/modRole")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message modRole(){
		RoleV2 role = getModel( RoleV2.class ) ;
		boolean result = roleService.modRole(role) ;
		if( result )
			return succ("ok", result );
		return error("no", "修改失败" , null );
	}
	
	@ActionKey("/queryRoleByCode")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryRole(){
		String roleCode = getPara("role_code");
		if( StringUtil.isBlank(roleCode) == true ){
			return error("01", "角色编号不可为空!", null ) ;
		}
		
		RoleV2 role = roleService.queryRoleByCode(roleCode) ;
		if( role == null )
			return error("02", "无效角色编号", role ) ;
		return succ("ok", role ) ;
	}
	
	@ActionKey("/getRoleInfoByRoleName")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getRoleInfoByRoleName(){
		String roleName = getPara("roleName");
		RoleV2 role = roleService.queryRoleByName(roleName) ;
		return succ("ok", role ) ;
	}
	
	@ActionKey("/deleteRole")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message deleteRole(){
		String roleCode = getPara("roleCode");
		return succ("删除结果", roleService.delectRole(roleCode)) ;
	}

	@ActionKey("/disabledRole")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message disabledRole(){
		String roleCode = getPara("unUsedRoleCode");
		if( StringUtil.isBlank(roleCode) == true){
			return error("02","角色模板编码不合法!",roleCode) ;
		}
		Map<String, Object> para = new HashMap<String, Object>(); 
		para.put("role_status", "B");
		
		if (roleService.updateById(roleCode, para)) {
			return succ("ok", "角色模板已禁用");
		}
		return error("01", "禁用失败！", null);
	}
	
}
