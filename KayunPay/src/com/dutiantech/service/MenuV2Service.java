package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.MenuV2;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Page;

public class MenuV2Service extends BaseService {
	
	/**
	 * 根据菜单ID 查询功能菜单
	 * @param menu_id
	 * @return
	 */
	public MenuV2 findById(String menu_id){
		return MenuV2.menuDao.findById(menu_id);
	}
	
	public List<MenuV2> queryMenus(){
		return MenuV2.menuDao.find("select menu_id,menu_id_p,menu_name,menu_status,menu_type from t_menu_v2 ");
	}
	
	public Page<MenuV2> findByPage(int pageNumber , int pageSize ){
		String sel = "select menu_id,menu_id_p,menu_name,menu_type,menu_url,menu_status ";
		return MenuV2.menuDao.paginate(pageNumber, pageSize, sel , " from t_menu_v2 " );
	}
	
	/**
	 * 查询所有功能菜单
	 * @return
	 */
	public List<MenuV2> findAll(){
		return MenuV2.menuDao.find("select menu_id,menu_id_p,menu_name from t_menu_v2");
	}
	
	/**
	 * 	根据权限Map获取菜单列表
	 * @param menu
	 * @return
	 */
	public List<MenuV2> queryShowMenus(String role){
		
		char[] roles = role.toCharArray() ;
		
		StringBuffer sqlBuff = 
				new StringBuffer("select menu_id,menu_name,menu_id_p,menu_url,menu_type from t_menu_v2");
		sqlBuff.append(" where menu_status='A' and menu_type='A' and menu_id in ") ;
		sqlBuff.append("(") ;
		String roleMap = "" ;
		int rLen = roles.length ;
		for(int index = 0 ; index < rLen ; index ++ ){
			char r = roles[index] ;
			if( r == '1' ){
				roleMap += "'" + index + "',"  ;
			}
		}
		sqlBuff.append( roleMap.substring( 0 , roleMap.length() - 1 ) ) ;
		sqlBuff.append(")") ;
		
		return MenuV2.menuDao.find( sqlBuff.toString() ); 
	}
	
	/**
	 * 新增一个功能菜单
	 * @param menu_name
	 * @param menu_status
	 * @param menu_level
	 * @param menu_id_p
	 * @param menu_url
	 * @param menu_icon3
	 * @param menu_icon2
	 * @param menu_icon1
	 * @return
	 */
	public boolean save(MenuV2 menu){
		menu.set("menu_status", "A");
		
		return menu.save();
	}
	
	/**
	 * 更新功能菜单信息 NULL不更新
	 * @param menu_id
	 * @param menu_name
	 * @param menu_status
	 * @param menu_level
	 * @param menu_id_p
	 * @param menu_url
	 * @param menu_icon3
	 * @param menu_icon2
	 * @param menu_icon1
	 * @return
	 */
	public boolean updateById(String menu_id,String menu_name, String menu_status, String menu_level, String menu_id_p, String menu_url, String menu_icon3, String menu_icon2, String menu_icon1){
		MenuV2 menu = MenuV2.menuDao.findById(menu_id);
		if(!StringUtil.isBlank(menu_name))
			menu.set("menu_name", menu_name);
		else if(!StringUtil.isBlank(menu_status))
			menu.set("menu_status", menu_status);
		else if(!StringUtil.isBlank(menu_level))
			menu.set("menu_level", menu_level);
		else if(!StringUtil.isBlank(menu_id_p))
			menu.set("menu_id_p", menu_id_p);
		else if(!StringUtil.isBlank(menu_url))
			menu.set("menu_url", menu_url);
		else if(!StringUtil.isBlank(menu_icon3))
			menu.set("menu_icon3", menu_icon3);
		else if(!StringUtil.isBlank(menu_icon2))
			menu.set("menu_icon3", menu_icon2);
		else if(!StringUtil.isBlank(menu_icon1))
			menu.set("menu_icon3", menu_icon1);
		return menu.update();
	}
	
	/**
	 * 删除一个功能菜单
	 * @param menu_id
	 * @return
	 */
	public boolean deleteById(String menu_id){
		return MenuV2.menuDao.deleteById(menu_id);
	}

}
