package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.LoanNotice;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Page;

public class LoanNoticeService extends BaseService {
	
	private static final String basic_selectFields = " noticeCode,content,overDateTime,createDateTime,state ";
	
	/**
	 * 查询一个明细啊
	 * @param noticeCode
	 * @return
	 */
	public LoanNotice findById(String noticeCode){
		return LoanNotice.loanNoticeDao.findById(noticeCode);
	}
	
	/**
	 * 新增一个发标公告
	 * @param content
	 * @param releaseDateTime
	 * @return
	 */
	public boolean save(String content, String overDateTime){
		LoanNotice loanNotice = new LoanNotice();
		loanNotice.set("noticeCode", UIDUtil.generate());
		loanNotice.set("content", content);
		loanNotice.set("overDateTime", overDateTime);
		loanNotice.set("createDateTime", DateUtil.getNowDateTime());
		loanNotice.set("state", "1");
		return loanNotice.save();
	}
	/**
	 * 审核发标公告
	 * @param noticeCode
	 * @param state
	 * @return
	 */
	public boolean audit(String noticeCode, String state){
		LoanNotice loanNotice = LoanNotice.loanNoticeDao.findByIdLoadColumns(noticeCode, "noticeCode,state");
		loanNotice.set("state", state);
		return loanNotice.update();
	}
	
	/**
	 * 删除发标公告
	 * @param noticeCode
	 * @return
	 */
	public boolean deleteById(String noticeCode){
		return LoanNotice.loanNoticeDao.deleteById(noticeCode);
	}
	
	/**
	 * * 分页查询发标预告(前台网站)
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @param beginDate		开始日期(注册日期)
	 * @param endDate		结束日期(注册日期)
	 * @param state			用户状态
	 * @return
	 */
	public Page<LoanNotice> findByPage(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String state, String allkey){
		String sqlSelect = "select "+basic_selectFields + " ";
		String sqlFrom = " from t_loan_notice ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by state desc,overDateTime asc";
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"content"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		makeExp(buff, paras, "overDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "overDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "state", "=", state, "and");
		return LoanNotice.loanNoticeDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * * 分页查询发标预告(后台审核)
	 * @param pageNumber	第几页
	 * @param pageSize		每页大小
	 * @param beginDate		开始日期(注册日期)
	 * @param endDate		结束日期(注册日期)
	 * @param state			用户状态
	 * @return
	 */
	public Page<LoanNotice> findByPage2(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String state, String allkey){
		String sqlSelect = "select "+basic_selectFields + " ";
		String sqlFrom = " from t_loan_notice ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by state desc,overDateTime desc";
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"content"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		makeExp(buff, paras, "overDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "overDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "state", "=", state, "and");
		return LoanNotice.loanNoticeDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}

}