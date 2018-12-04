package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.Paginate;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.SysConfig;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class SysConfigController extends BaseController {
	

	@ActionKey("/querySysConfigList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message querySysConfigList(){
		
		Paginate pag = getPaginate() ;
		
		Page<SysConfig> cfgs = SysConfig.sysConfigDao.paginate(pag.getPageNum(), pag.getPageSize() , "select cfgCode,cfgName,cfgCreateDateTime,cfgModifyDateTime,cfgDesc"
				, " from t_sys_config" ) ;
		
		return succ("ok", cfgs ) ;
	}

	@ActionKey("/querySysConfigByCode")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message querySysConfigByCode(){
		
		String cfgCode = getPara("key","");
		
		if( StringUtil.isBlank(cfgCode) ){
			return error("01", "无效编号", cfgCode) ;
		}
		
		SysConfig cfgs = SysConfig.sysConfigDao.findById( cfgCode ) ;
		
		return succ("ok", cfgs ) ;
	}
	
	@ActionKey("/addNewConfig")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addNewConfig(){
		
		SysConfig cfg = getModel( SysConfig.class ) ;
		String dt = DateUtil.getNowDateTime() ;
		cfg.set("cfgCreateDateTime", dt);
		cfg.set("cfgModifyDateTime", dt);
		
		String code = cfg.getStr("cfgCode").toUpperCase();
		cfg.set("cfgCode", code);
		boolean result = cfg.save() ;
		if( result )
			return succ("ok" , cfg) ;
		else
			return error("02", "改配置编号已存在",  result ) ;
	}
	
	@ActionKey("/modNewConfig")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modNewConfig(){
		
		SysConfig cfg = getModel( SysConfig.class ) ;
		String dt = DateUtil.getNowDateTime() ;
		cfg.set("cfgModifyDateTime", dt);
		
		String code = cfg.getStr("cfgCode").toUpperCase();
		cfg.set("cfgCode", code);
		boolean result = cfg.update() ;
		if( result )
			return succ("ok" , cfg) ;
		else
			return error("02", "改配置编号已存在",  result ) ;
	}
	
}
