package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.ReturnedAmount;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;

public class ReturnedAmountService extends BaseService{
	/**
	 * 
	 * @param userCode
	 * @param userName
	 * @param amount
	 * @param traceCode
	 * @param type		1:还款本金,2:还款利息-手续费,3:返佣
	 * @param state	1:已发,2:未发
	 * @return
	 */
	public boolean save(String userCode,String userName,long amount,String traceCode,int type,int state){
		ReturnedAmount returnedAmount = new ReturnedAmount();
		returnedAmount.set("userCode", userCode);
		returnedAmount.set("userName", userName);
		returnedAmount.set("amount", amount);
		returnedAmount.set("traceCode", traceCode);
		String nowDate = DateUtil.getNowDateTime();
		returnedAmount.set("updateTime", nowDate);
		returnedAmount.set("createDateTime", nowDate);
		returnedAmount.set("type", type);
		returnedAmount.set("state", state);
		return returnedAmount.save();
	}
	
	public boolean update(String userCode,int state){
		return Db.update("update t_returned_amount set state=?,updateTime=? where userCode = ?",state,DateUtil.getNowDateTime(),userCode)>0;
	}
	
	public long findByAMount(String userCode,String state){
		return Db.queryLong("select sum(amount) from t_returned_amount where state = ? and userCode = ?",state,userCode);
	}

	/**
	 * 查询未开户用户未发资金总额 WJW
	 * @return
	 */
	public List<ReturnedAmount> queryUnbilled(){
		String sql = "select userCode,userName,sum(amount) sumAmount from t_returned_amount where state=2 group by userCode";
		return ReturnedAmount.returnedAmountDao.find(sql);
	}
	
	/**
	 * 根据日期查询当日未开户还款金额总额 WJW
	 * @param date
	 * @return
	 */
	public long sumAmountByDate(String date){
		String sql = "select COALESCE(sum(amount),0) from t_returned_amount where DATE_FORMAT(createDateTime,'%Y%m%d')=?";
		return Db.queryBigDecimal(sql,date).longValue();
	}
}
