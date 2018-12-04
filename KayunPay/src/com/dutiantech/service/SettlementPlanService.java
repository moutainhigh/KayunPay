package com.dutiantech.service;

import java.util.HashMap;
import java.util.Map;

import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.SettlementPlan;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.plugin.activerecord.Db;

public class SettlementPlanService extends BaseService{
	
	/**
	 * 新增一期借款标的还款计划信息
	 * @param loanInfo
	 * @param params	包含以下字段：<br>
	 * 					backDate		还款日期<br>
	 * 					recyLimit		第N期还款<br>
	 * 					recyPrincipal	N期应还本金<br>
	 * 					recyInterest	N期应还利息<br>
	 * 					realRecyPrincipal	实际还款本金,默认0,结算时修改<br>
	 * 					realRecyInterest	实际还款利息,默认0,结算时修改<br>
	 * 					leftRecyPrincipal	剩余还款本金<br>
	 * 					leftRecyInterest	剩余还款利息<br>
	 * 					beRecyPrincipal		总计还款本金<br>
	 * 					beRecyInterest		总计还款利息<br>
	 * 					settlementState		N期的还款状态<br>
	 * 					
	 * @return
	 */
	public boolean save(LoanInfo loanInfo,Map<String, Object> params){
		SettlementPlan sp = new SettlementPlan();
		sp.set("loanCode", loanInfo.getStr("loanCode"));
		sp.set("loanTitle", loanInfo.getStr("loanTitle"));
		sp.set("loanType", loanInfo.getStr("loanType"));
		sp.set("loanUsedType", loanInfo.getStr("loanUsedType"));
		sp.set("productType", loanInfo.getStr("productType"));
		sp.set("refundType", loanInfo.getStr("refundType"));
		sp.set("loanAmount", loanInfo.getLong("loanAmount"));
		sp.set("rateByYear", loanInfo.getInt("rateByYear"));
		sp.set("rewardRateByYear", loanInfo.getInt("rewardRateByYear"));
		sp.set("benefits4new", loanInfo.getInt("benefits4new"));
		sp.set("releaseDate", loanInfo.getStr("releaseDate"));
		sp.set("releaseTime", loanInfo.getStr("releaseTime"));
		sp.set("effectDate", loanInfo.getStr("effectDate"));
		sp.set("effectTime", loanInfo.getStr("effectTime"));
		sp.set("userCode", loanInfo.getStr("userCode"));
		sp.set("userName", loanInfo.getStr("userName"));
		sp.set("loanTimeLimit", loanInfo.get("loanTimeLimit"));
		sp._setAttrs(params);
		return sp.save();
	}
	
	/**
	 * 结算时修改借款标当前这一期的还款计划信息
	 * @param loanCode				借款标编码
	 * @param recyLimit				第N期还款
	 * @param realRecyPrincipal		实际还款本金
	 * @param realRecyInterest		实际还款利息
	 * @param settlementState		这一期还款计划的状态
	 * @return
	 */
	public boolean settlement(String loanCode, int recyLimit, long realRecyPrincipal, long realRecyInterest, SysEnum.settlementState settlementState){
		try {
			SettlementPlan sp = SettlementPlan.settlementPlanDao.findFirst("select * from t_settlement_plan where loanCode = ? and recyLimit =?",loanCode,recyLimit);
			if(sp!=null){
				sp.set("realRecyPrincipal", realRecyPrincipal);
				sp.set("realRecyInterest", realRecyInterest);
				sp.set("settlementState", settlementState.val());
				if(SysEnum.settlementState.C == settlementState){//提前还款更新全部未还期数还款状态
					Db.update("update t_settlement_plan set settlementState = ? where loanCode = ? and recyLimit > ?",SysEnum.settlementState.D.val(),loanCode,recyLimit);
				}
				return sp.update();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * 根据loanCode和还款期数查询还款记录 WJW
	 * @param loanCode
	 * @param recyLimit
	 * @return
	 */
	public SettlementPlan findByLoanCodeAndRecyLimit(String loanCode,int recyLimit){
		String sql = "select * from t_settlement_plan where loanCode=? and recyLimit=?";
		return SettlementPlan.settlementPlanDao.findFirst(sql,loanCode,recyLimit);
	}
	
	/**
	 * 根据loanCode,还款期数       追加jxTraceCode(分隔符号:&&) WJW
	 * @param loanCode
	 * @param recyLimit
	 * @param jxTraceCode
	 * @return
	 */
	public boolean addJxTraceCode(String loanCode,int recyLimit,String jxTraceCode){
		String sql = "update t_settlement_plan set jxTraceCode = if(jxTraceCode is null or jxTraceCode='','"+jxTraceCode+"',concat(jxTraceCode,'&&"+jxTraceCode+"')) where loanCode=? and recyLimit=?";
		return Db.update(sql,loanCode,recyLimit) > 0;
	}
	
//	/**
//	 * 结算时修改借款标当前这一期的还款计划信息
//	 * @param loanCode				借款标编码
//	 * @param recyLimit				第N期还款
//	 * @param realRecyPrincipal		实际还款本金
//	 * @param realRecyInterest		实际还款利息
//	 * @param settlementState		这一期还款计划的状态
//	 * @return
//	 */
//	public boolean settlement2(String loanCode, int recyLimit, long realRecyPrincipal, long realRecyInterest){
//		try {
//			SettlementPlan sp = SettlementPlan.settlementPlanDao.findFirst("select * from t_settlement_plan where loanCode = ? and recyLimit =?",loanCode,recyLimit);
//			if(sp!=null){
//				sp.set("realRecyPrincipal", realRecyPrincipal);
//				sp.set("realRecyInterest", realRecyInterest);
//				sp.set("settlementState", SysEnum.settlementState.C.val());
//				Db.update("update t_settlement_plan set settlementState = ? where loanCode = ? and recyLimit > ?",SysEnum.settlementState.D.val(),loanCode,recyLimit);
//				return sp.update();
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		return true;
//	}

	/**
	 * 查询回款金额(默认当前日期，回款为t+1，查询日期为前一日)
	 * */
	public Map<String, Long> queryBackAmount(String date){
		if(StringUtil.isBlank(date)){
			date = DateUtil.getNowDate();
		}
		String yesterday = DateUtil.delDay(date, 1);
		//查询代偿正常回款
		String sqlDNormal = "SELECT COALESCE(SUM(nextAmount+nextInterest),0) from t_loan_trace where loanState = 'N' and (overdueInterest = 0 or overdueInterest is null) and loanRecyDate = ? and authCode is not null";
		//查询代偿逾期利息
		String sqlDOverdueInterest = "SELECT COALESCE(SUM(overdueInterest),0) from t_loan_trace where loanState = 'N' and overdueInterest > 0 and loanRecyDate = ? and authCode is not null";
		//查询代偿归还逾期本金
		String sqlDOverduePrincipal = "SELECT COALESCE(SUM(overdueAmount),0) from t_loan_trace where loanCode in (SELECT loanCode from t_loan_overdue where disposeStatus = 'n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')<? GROUP BY loanCode) and authCode is not null";
		//查询若当天设置的代偿逾期归还本金
		String sqlDNextAmount = "SELECT COALESCE(SUM(nextAmount),0) from t_loan_trace where loanCode in (SELECT loanCode from t_loan_overdue where disposeStatus = 'n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')<? GROUP BY loanCode) and loanState = 'N' and loanRecyDate = ? and authCode is not null";
		long dNormalAmount = Db.queryBigDecimal(sqlDNormal,yesterday).longValue();
		long dOverdueInterest = Db.queryBigDecimal(sqlDOverdueInterest,yesterday).longValue();
		long dOverduePrincipal = Db.queryBigDecimal(sqlDOverduePrincipal,date).longValue();
		long dNextAmount = Db.queryBigDecimal(sqlDNextAmount,date,yesterday).longValue();
		//-----------------------------------------------------
		//查询红包正常回款
		String sqlHNormal = "SELECT COALESCE(SUM(nextAmount+nextInterest),0) from t_loan_trace where loanState = 'N' and (overdueInterest = 0 or overdueInterest is null) and loanRecyDate = ? and authCode is null;";
		//查询红包逾期利息
		String sqlHOverdueInterest = "SELECT COALESCE(SUM(overdueInterest),0) from t_loan_trace where loanState = 'N' and overdueInterest > 0 and loanRecyDate = ? and authCode is null;";
		//查询红包归还逾期本金
		String sqlHOverduePrincipal = "SELECT COALESCE(SUM(overdueAmount),0) from t_loan_trace where loanCode in (SELECT loanCode from t_loan_overdue where disposeStatus = 'n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')<? GROUP BY loanCode) and authCode is null;";
		//查询若当天设置的红包逾期归还本金
		String sqlHNextAmount = "SELECT COALESCE(SUM(nextAmount),0) from t_loan_trace where loanCode in (SELECT loanCode from t_loan_overdue where disposeStatus = 'n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')<? GROUP BY loanCode) and loanState = 'N' and loanRecyDate = ? and authCode is null;";
		long hNormalAmount = Db.queryBigDecimal(sqlHNormal,yesterday).longValue();
		long hOverdueInterest = Db.queryBigDecimal(sqlHOverdueInterest,yesterday).longValue();
		long hOverduePrincipal = Db.queryBigDecimal(sqlHOverduePrincipal,date).longValue();
		long hNextAmount = Db.queryBigDecimal(sqlHNextAmount,date,yesterday).longValue();
		Map<String, Long> result = new HashMap<String, Long>();
		result.put("dNormalAmount", dNormalAmount+dNextAmount);
		result.put("dOverdueInterest", dOverdueInterest);
		result.put("dOverduePrincipal", dOverduePrincipal);
		result.put("hNormalAmount", hNormalAmount+hNextAmount);
		result.put("hOverdueInterest", hOverdueInterest);
		result.put("hOverduePrincipal", hOverduePrincipal);
		return result;
	}
}
