package com.dutiantech.controller.portal;

import java.util.List;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Question;
import com.dutiantech.service.QuestionService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class QuestionController extends BaseController{
	private QuestionService questionService = getService(QuestionService.class);
	
	@ActionKey("/queryQuestion")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryQuestion(){
		String qType = getPara("type");
		if(StringUtil.isBlank(qType)){
			return error("01", "输入不可为空", null);
		}
		List<Question> questions = null;
		if("Y".equals(qType)){
			questions = questionService.queryByIsUsually(qType);
		}else{
			questions = questionService.queryByQtype(qType);
		}
		return succ("查询成功", questions);
	}
	
	@ActionKey("/queryQuestionDetail")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void queryQuestionDetail(){
		Message msg = null;
		String qCode = getPara("qCode");
		Question question = new Question();
		if(StringUtil.isBlank(qCode)){
			msg = error("01", "参数错误", "");
		}else{
			question = questionService.queryQuestionDetail(qCode);
			msg = succ("查询成功!", question);
			addTipNum(question);
		}
		renderJson(msg);
	}
	private void addTipNum(Question question){
		long tipNum = question.getLong("tipNum");
		question.set("tipNum", tipNum+1);
		question.update();
	}
	
	@ActionKey("/queryByKeyWord")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryByKeyWord(){
		String keyWord = getPara("keyword");
		List<Question> questions = questionService.queryByKeyWord(keyWord);
		if(null==questions||questions.size()==0){
			return error("01", "未查询到相关问题", "");
		}else{
			return succ("查询成功", questions);
		}
	}
}
