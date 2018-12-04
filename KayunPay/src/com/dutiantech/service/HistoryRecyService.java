package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.HistoryRecy;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;

public class HistoryRecyService extends BaseService {
	
	/**
	 * 增加还款记录
	 * @param loanNo
	 * @param loanCode
	 * @param loanTitle
	 * @param loanState
	 * @param loanUserName
	 * @param loanUserCode
	 * @param payUserName
	 * @param payUserCode
	 * @param recyAmount
	 * @param recyInterest
	 * @param payAmount
	 * @param nextAdmount
	 * @param nextInterest
	 * @param recyLimit
	 * @param loanTimeLimit
	 * @return
	 */
	public boolean save(String loanNo,String loanCode,String loanTitle,String loanState,String loanUserName,String loanUserCode,String payUserName,String payUserCode,Long recyAmount,Long recyInterest,Long payAmount,Long nextAdmount,Long nextInterest,int recyLimit, int loanTimeLimit ){
		HistoryRecy historyRecy = new HistoryRecy();
		historyRecy.set("loanNo", loanNo);
		historyRecy.set("loanCode", loanCode);
		historyRecy.set("loanTitle", loanTitle);
		historyRecy.set("loanState", loanState);
		historyRecy.set("loanUserName", loanUserName);
		historyRecy.set("loanUserCode", loanUserCode);
		historyRecy.set("payUserName", payUserName);
		historyRecy.set("payUserCode", payUserCode);
		historyRecy.set("recyAmount", recyAmount);
		historyRecy.set("recyInterest", recyInterest);
		historyRecy.set("payAmount", payAmount);
		historyRecy.set("nextAdmount", nextAdmount);
		historyRecy.set("nextInterest", nextInterest);
		historyRecy.set("recyCode", UIDUtil.generate());
		historyRecy.set("recyLimit", recyLimit);
		historyRecy.set("loanTimeLimit",loanTimeLimit );
		historyRecy.set("recyDate", DateUtil.getNowDate());
		historyRecy.set("recyTime", DateUtil.getNowTime());
		return historyRecy.save();
	}
	
	/**
	 * 根据时间查询收益金额
	 * @param userCode
	 * @param beginDateTime
	 * @param endDateTime
	 * @return
	 */
	public long queryIncome4Month(String userCode ,String beginDate, String endDate){
		return Db.queryBigDecimal("select COALESCE(sum(recyInterest),0) from t_history_recy where recyDate >= ? and recyDate <= ? and payUserCode = ? ",beginDate,endDate,userCode).longValue();
	}
	
	/**
	 * 根据标编号及还款期数查询历史还款记录 WJW
	 * @param loanCode
	 * @param repayIndex
	 * @return
	 */
	public List<HistoryRecy> queryByLoanCodeAndRecyLimit(String loanCode,int repayIndex){
		String sql = "select * from t_history_recy where loanCode=? and recyLimit=?";
		return HistoryRecy.historyRecyDao.find(sql,loanCode,repayIndex);
	}

}














