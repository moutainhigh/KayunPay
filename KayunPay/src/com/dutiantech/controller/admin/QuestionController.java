package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.Question;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.QuestionService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class QuestionController extends BaseController{
	
	private QuestionService questionService = getService(QuestionService.class);
	private OPUserV2Service opUserService = getService( OPUserV2Service.class ) ;
	
	@ActionKey("/saveOrUpdateQuestion")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message saveQuestion(){
		Question question = getModel( Question.class ,"question") ;
		String userCode = getUserCode() ;
		OPUserV2 opUser = opUserService.findById(userCode) ;
		String qCode = question.getStr("qCode");
		if(StringUtil.isBlank(qCode)){
			boolean isOK = questionService.save(question, opUser.getStr("op_name"), userCode);
			if(isOK){
				return succ("OK", ""); 
			}else{
				return error("01", "添加失败", "");
			}
		}else{
			boolean isUpdate = questionService.update(question);
			if(isUpdate){
				return succ("OK", ""); 
			}else{
				return error("01", "修改失败", "");
			}
		}
	}
	
	/**
	 * 查询问题列表
	 */
	@ActionKey("/queryQuestionsAdmin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryQuestionsAdmin(){
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		String type = getPara("type");
		
		//验证数据完整性
		if(null == pageNumber || pageNumber <= 0){
			pageNumber = 1;
		}
		if(null == pageSize || pageSize <= 0 || pageSize > 20){
			pageSize = 20;
		}
		
		Page<Question> queryQuestion = questionService.queryQuestion(pageNumber , pageSize , type );
		Message msg = succ("查询成功", queryQuestion);
		renderJson(msg);
	}
	
	/**
	 * 查询问题详细信息
	 * */
	@ActionKey("/queryQuestionDetailAdmin")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void queryQuestionDetailAdmin(){
		Message msg = null;
		String qCode = getPara("qCode");
		Question question = new Question();
		if(StringUtil.isBlank(qCode)){
			msg = error("01", "参数错误", "");
		}else{
			question = questionService.queryQuestionDetail(qCode);
			msg = succ("查询成功!", question);
		}
		renderJson(msg);
	}
	
	/**
	 * 删除问题
	 * */
	public Message delQuestion(){
		
		String qCode = getPara("qCode");
		if(StringUtil.isBlank(qCode)){
			return error("01", "参数错误", "");
		}
		boolean delQuestion = questionService.delQuestion(qCode);
		if(delQuestion == false){
			return error("02", "删除异常", "");
		}
		return succ("删除成功", null ) ;
	}
}
