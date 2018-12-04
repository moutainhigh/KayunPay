package com.dutiantech.controller;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.UploadResult;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.SysConfig;
import com.dutiantech.plugins.QCloudPic;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.DoAXActionService;
import com.dutiantech.tcsec.CSECAPI;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DESUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.vo.UploadFile;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.render.ContentType;

public class PublicController extends BaseController{
	
	static{
		CSECAPI.init("AKIDHAkPEim3P4spPVscZFq3PtWE2RGXOpLn",
				"jP69rrTIwCM1hHRITwxwNAs3ny2ZCstK", "v1", "1");
	}
	
	DoAXActionService actionService = getService( DoAXActionService.class ) ;
	
	@ActionKey("/upload")
	public void upload(){
		String c = getPara("z");
		JSONObject obj = new JSONObject() ;
		try {
			UploadFile file = UploadFile.makeFile(getRequest()) ;
			if(null != file){
				if(!file.verifyExtensionName()){
					obj.put("error", -4 );
					obj.put("message", "图片格式错误" ) ;
				}else{
					if(file.getData().length / 1048576>10){
						obj.put("error", -5 );
						obj.put("message", "图片大小超过10MB" ) ;
					}else{
				UploadResult result = new UploadResult();
				int ret = QCloudPic.upload( file.getData() , result);
				if (ret == 0) {
					Map<String , String > ps = file.getParas() ;
					obj.put("error", ret );
					obj.put("fileid", result.fileid  );
					obj.put("id", ps.get("id") );
					obj.put("url", result.download_url.replace("/original", "/"+c) ) ;
				}else{
					obj.put("error", ret );
					obj.put("message", "error" ) ;
				}
				}
				}
			}else{
				obj.put("error", -2 );
				obj.put("message", "图片不能为空" ) ;
			}
		} catch (Exception e) {
			obj.put("error", -3 );
			obj.put("message", "服务器繁忙，请稍后再试" ) ;
		}
		renderJson( obj );
	}
	
	@ActionKey("/cap/cjcu4auth")
	@AuthNum(value=999,desc="获取验证码javascripturl")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getCapJavascriptUrl4auth(){
		String userip = getRequestIP() ;
		String uCode = getUserCode() ;
		int buid = getParaToInt("buid");
		int uid = getUser().getInt("uid") ;
		int sceneid = getParaToInt("sid") ;
		try {
			setCookie4cap("CAP_" + uCode , buid , sceneid );
			String url = CSECAPI.createQueryUrl(userip, buid, uid, sceneid, null ) ;
			return Message.succ("ok", url ) ;
		} catch (UnsupportedEncodingException e) {
			return Message.error("01", "生成URL失败", e ) ;
		}
		
	}
	
	private void setCookie4cap(String ckey , int buid , int sceneid ){
//		String aCode = UIDUtil.generate() ;
		//加入验证码识别码
//		setCookie("cac_z02_v1", ckey , 60) ;
//		CACHED.put( ckey , buid+"|"+sceneid , true , 360 );	//设置buid和sid
	}
	
	@ActionKey("/cap/cjcu")
	@Before(PkMsgInterceptor.class)
	public Message getCapJavascriptUrl(){
		String userip = getRequestIP() ;
		int buid = getParaToInt("buid");
		int uid = 1 ;
		int sceneid = getParaToInt("sid") ;
		try {
			String aCode = UIDUtil.generate() ;
			setCookie4cap(aCode, buid, sceneid);
			String url = CSECAPI.createQueryUrl(userip, buid, uid, sceneid, null ) ;
			return Message.succ("ok", url ) ;
		} catch (UnsupportedEncodingException e) {
			return Message.error("01", "生成URL失败", e ) ;
		}
	}
	
	/**
	 * 非常危险的接口
	 */
	@ActionKey("/Sw8VP7Aq")
	public void reloadConfig(){
		String opKey = getPara("opKey") ;
		if( "rMijHJMBZsnZGLWX".equals( opKey ) == false ){
			renderText("");
		}else{
			List<SysConfig> cfgs = SysConfig.sysConfigDao.find("select * from t_sys_config");
			StringBuffer buff = new StringBuffer();
			for(SysConfig cfg : cfgs ){
				JSONArray array = JSONArray.parseArray( cfg.getStr("cfgContent") ) ;
				String keyCode = cfg.getStr("cfgCode");
				for(Object obj : array ){
					JSONObject json = (JSONObject)obj ;
					String key = keyCode+"."+json.getString("key");
					String val = json.getString("value");
					String desc = json.getString("desc");
					CACHED.put( key , val , false );
					buff.append( String.format("加载[%s][key=%s][value=%s]成功\n", desc , key , val ));
				}
			}
			renderText( buff.toString() );
		}
		
	}
	
	@ActionKey("/captcha4sys")
	public void getCaptionBySys(){
		renderCaptcha();
	}
	
	@ActionKey("/checkCaptcha4sys")
	public void checkCaption4sys(){
		renderText( validateCaptcha("cv") +"");
	}
	
	private void doexAction(String userCode){
		String projectName = getPara("pn") ;
		String sk = "90iHGv^BMdems94K";
		
		if( StringUtil.isBlank(projectName) == true ){
			renderJson( error("02", "非法请求!", ""));
		}else{
			
			String requestURI = CACHED.getStr("PN."+projectName) ;
			if( StringUtil.isBlank(requestURI) == true ){ 
				renderJson( error("02", "非法请求", "" ));
			}else{
				String requestParams = "";//getRequest().getQueryString() ;
				Enumeration<String> keys = getParaNames() ;
				while( keys.hasMoreElements() ){
					if( requestParams.length() > 0 ){
						requestParams += "&";
					}
					String key = keys.nextElement() ;
					String val = getPara(key,"") ;
					if( StrKit.isBlank(val) == false )
						requestParams += key + "=" + getPara(key ,"");
				}
				if(StringUtil.isBlank(userCode) == false ){
					if( requestParams.length() > 0 ){
						requestParams += "&";
					}
					try {
						requestParams += "userCode=" + DESUtil.encode4string( userCode , sk );
					} catch (Exception e) {
					}
				}
				
				String userAgent = getRequest().getHeader("User-Agent");
				boolean isFromMobile = CommonUtil.check(userAgent) ;
				if( requestParams.length() > 0 ){
					requestParams += "&";
				}
				requestParams += "isFromMobile="+isFromMobile;
				
				String result = actionService.doActionByPost( requestURI , requestParams) ;
				if( StringUtil.isBlank(result) == true ){
					renderJson( error("01", "服务器忙，请稍后再试!", ""));
				}else{
					renderText(result, ContentType.JSON);
				}
			}
		}
	}
	
	@ActionKey("/doex/actionna")
	public void doexActionNoAuth(){
		doexAction(null);
	}

	/**
	 * 	扩展支持
	 * 		不实用Message序列话，会增加序列化和反序列的成本
	 */
	@ActionKey("/doex/action")
	@Before(AuthInterceptor.class)
	@AuthNum(value=999)
	public void doexAction(){
		doexAction( getUserCode() );
	}
	
	@ActionKey("/test/automap")
	public void automap(){
		
		Db.update("delete from t_auto_map");
		AutoMapSerivce service = new AutoMapSerivce() ;
		service.genAutoMap(AutoLoan_v2.autoLoanDao.find("select * from t_auto_loan_v2 where autoState='A'"));
		long x = Db.queryLong("select COUNT(DISTINCT userCode) from t_auto_map where autoState = 'A'");
		long y = Db.queryLong("select COUNT(userCode) from t_auto_loan_v2 where autoState = 'A'");
		renderText("map:"+x+"，autoloan:"+y ) ;
	}
	
}
