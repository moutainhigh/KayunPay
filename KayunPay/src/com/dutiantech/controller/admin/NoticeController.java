package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Notice;
import com.dutiantech.service.NoticeService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class NoticeController extends BaseController {
	
	private NoticeService noticeService = getService(NoticeService.class) ;
	
	/**
	 * 查询公告新闻信息列表
	 */
	@ActionKey("/queryNewsAdmin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryNewsByPage(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String type = getPara("type");
		String isContent = getPara("isContent");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		
		Page<Notice> queryNotice = noticeService.queryNotice(pageNumber , pageSize , type , isContent);
		Message msg = succ("查询成功", queryNotice);
		renderJson(msg);
	}
	
	
	/**
	 * 查询公告新闻详细信息
	 */
	@ActionKey("/queryNewsDetailAdmin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryNewsDetail(){
		Message msg = null;
		String id = getPara("id");
		Notice notice = new Notice();
		if(StringUtil.isBlank(id)){
			msg = error("01", "参数错误", "");
		}else{
			notice = noticeService.queryNewsDetail(id);
			msg = succ("查询成功!", notice);
		}
		renderJson(msg);
	}
	
	/**
	 * 保存
	 * @return
	 */
	@ActionKey("/saveNotice")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message saveNotice(){
		Notice notice = getModel(Notice.class, "notice");
		if(notice == null){
			return error("01", "参数错误", "");
		}
		if(StringUtil.isBlank(notice.getStr("title"))){
			return error("02", "标题不能为空", "");
		}
//		if(StringUtil.isBlank(notice.getStr("content"))){
//			return error("03", "内容不能为空", "");
//		}
		if(StringUtil.isBlank(notice.getStr("type"))){
			return error("04", "类型不能为空", "");
		}
		boolean saveNotice = noticeService.saveNotice(notice);
		if(saveNotice == false){
			return error("05", "保存异常", "");
		}
		return succ("保存成功", null ) ;
	}
	
	
	/**
	 * 删除
	 * @return
	 */
	@ActionKey("/delNotice")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message delNotice(){
		String id = getPara("id");
		if(StringUtil.isBlank(id)){
			return error("01", "参数错误", "");
		}
		boolean delNotice = noticeService.delNotice(id);
		if(delNotice == false){
			return error("02", "删除异常", "");
		}
		return succ("删除成功", null ) ;
	}

	/**
	 * 修改
	 * @return
	 */
	@ActionKey("/updateNotice")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateNotice(){
		Notice notice = getModel(Notice.class, "notice");
		if(notice == null){
			return error("01", "参数错误", "");
		}
		if(StringUtil.isBlank(notice.getStr("title"))){
			return error("02", "标题不能为空", "");
		}
//		if(StringUtil.isBlank(notice.getStr("content"))){
//			return error("03", "内容不能为空", "");
//		}
		if(StringUtil.isBlank(notice.getStr("type"))){
			return error("04", "类型不能为空", "");
		}
		if(notice.getInt("id") == null){
			return error("05", "标识ID不能为空", "");
		}
		boolean saveNotice = noticeService.updateNotice(notice);
		if(saveNotice == false){
			return error("06", "保存异常", "");
		}
		return succ("保存成功", null ) ;
	}
	
}





