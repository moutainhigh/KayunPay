package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.Question;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class QuestionService extends BaseService{
	
	public boolean save(Question question,String creatUserName,String creatUserCode){
		question.set("qCode",UIDUtil.generate());
		question.set("creatDateTime", DateUtil.getNowDateTime());
		question.set("updateDateTime", DateUtil.getNowDateTime());
		question.set("tipNum",0);
		question.set("solveNum",0);
		question.set("creatUserName",creatUserName);
		question.set("creatUserCode",creatUserCode);
		return question.save();
	}
	
	/**
	 * 查询公告新闻列表
	 * @param pageNumber
	 * @param pageSize
	 * @param type
	 * @param isContent
	 * @return
	 */
	public Page<Question> queryQuestion(Integer pageNumber, Integer pageSize, String type) {
		String sqlSelect = "select * ";
		String sqlFrom = "from t_question ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		if(!StringUtil.isBlank(type)){
			makeExp(buff, ps, "type", "=", type, "and");
		}
		String sqlOrder = " order by updateDateTime desc ";
		return Question.questionDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + makeSql4Where(buff) + sqlOrder,ps.toArray());
	}
	
	/**
	 * 查询
	 * @param qCode
	 * @return
	 */
	public Question queryQuestionDetail(String qCode) {
		return Question.questionDao.findById(qCode);
	}
	
	/**
	 * 删除
	 * */
	public boolean delQuestion(String qCode){
		return Question.questionDao.deleteById(qCode);
	}
	
	/**
	 * 修改
	 * */
	public boolean update(Question question){
		question.set("updateDateTime", DateUtil.getNowDateTime());
		return question.update();
	}
	
	/**
	 * 查询常见列表
	 * */
	public List<Question> queryByIsUsually(String isUsually){
		String sql = "select * from t_question where isUsually = ?";
		return Question.questionDao.find(sql, isUsually);
	}
	
	/**
	 * 查询指定类型问题
	 * */
	public List<Question> queryByQtype(String qType){
		String sql = "select * from t_question where qType = ?";
		return Question.questionDao.find(sql, qType);
	}
	
	/**
	 * 查询关键字段的问题列表
	 * */
	public List<Question> queryByKeyWord(String keyWord){
		String sql = "select * from t_question where keyWord like ?";
		return Question.questionDao.find(sql, "%"+keyWord+"%");
	}
}
