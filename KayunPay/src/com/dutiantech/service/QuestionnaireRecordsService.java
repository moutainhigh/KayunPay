package com.dutiantech.service;

import com.dutiantech.model.QuestionnaireRecords;
import com.jfinal.plugin.activerecord.Db;

public class QuestionnaireRecordsService extends BaseService {

	/*
	 * 通过userCode查询评测
	 */
	public QuestionnaireRecords querySurveyResultById(String userCode){
		
		return QuestionnaireRecords.questionnaireRecordsDao.findFirst("select tid,userCode,userName,surveyRecord,surveyResult,addDateTime from t_questionnaire_records where userCode=?", userCode);
		
		
		
	}
	
	/**
	 * 更新
	 */
	public boolean updateSurverResultById(String userCode,String surveyRecord,String surveyResult,String addDateTime){
		String sql = "update t_questionnaire_records set surveyRecord=?,surveyResult=?,addDateTime=? where userCode=?";
		int num = Db.update(sql,surveyRecord,surveyResult,addDateTime,userCode);
		if(num>0){
			return true;
		}else{
			return false;
		}
	}
	
	
}
