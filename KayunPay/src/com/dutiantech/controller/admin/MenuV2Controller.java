package com.dutiantech.controller.admin;

import java.util.List;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.MenuV2;
import com.dutiantech.service.MenuV2Service;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

@Before(AuthInterceptor.class)
public class MenuV2Controller extends BaseController{

	private MenuV2Service menuService = getService(MenuV2Service.class);
	@ActionKey("/queryMenuListV2")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message getMenuV2List(){
		List<MenuV2> list = menuService.findAll() ;
		
		return succ("ok", list ) ;
	}
	
	@ActionKey("/queryRoleListV2")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message getRoleList(){
//		List<Role> list = roleService.queryRoleList() ;
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		Page<MenuV2> infos = menuService.findByPage(pageNumber, pageSize) ;
		return succ("ok",infos) ;
	}
}
