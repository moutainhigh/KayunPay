package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.PlatformCount;
import com.dutiantech.util.CommonUtil;
import com.jfinal.plugin.activerecord.Db;

public class PlatformCountService extends BaseService {
	
	/**
	 * 初始化某日统计数据,若要统计今天的数据，必须在还款前初始化
	 * @param countDate
	 * @return
	 */
	public boolean initRecord(String countDate){
		long jryhbj = 0;//今日应还本金
		long jryhlx = 0;//今日应还利息
		List<LoanInfo> loanInfos = LoanInfo.loanInfoDao.find("select * from t_loan_info where loanState = 'N' and backDate = ?", countDate);
		for (int i = 0; i < loanInfos.size(); i++) {
			LoanInfo tmp = loanInfos.get(i);
			int rate = tmp.getInt("rateByYear") + tmp.getInt("rewardRateByYear") + tmp.getInt("benefits4new");
			int reciedCount = tmp.getInt("reciedCount");
			int loanTimeLimit = tmp.getInt("loanTimeLimit");
			long loanAmount = tmp.getLong("loanAmount");
			String refundType = tmp.getStr("refundType");
			long[] bx = CommonUtil.f_000(loanAmount, loanTimeLimit, rate, reciedCount+1, refundType);
			jryhbj = jryhbj + bx[0];
			jryhlx = jryhlx + bx[1];
		}
		long ysbj = Db.queryBigDecimal("select COALESCE(SUM(nextAmount),0) from t_loan_trace where loanState = 'N' and loanRecyDate = ?",countDate).longValue();
		long yslx = Db.queryBigDecimal("select COALESCE(SUM(nextInterest),0) from t_loan_trace where loanState = 'N' and loanRecyDate = ?",countDate).longValue();
		PlatformCount pfc = new PlatformCount();
		pfc.set("countDate", countDate);
		pfc.set("yhbj", jryhbj);
		pfc.set("yhlx", jryhlx);
		pfc.set("ysbj", ysbj);
		pfc.set("yslx", yslx);
		return pfc.save();
	}
	
	public PlatformCount findPlatformCountByDate(String date){
		return PlatformCount.platformCountDao.findById(date);
	}
	
	//生成统计数据
	public boolean madePlatformData(String date){
		long kyye = Db.queryBigDecimal("select COALESCE(SUM(avBalance),0) from t_funds where loanTotal < 1").longValue();
		long djye = Db.queryBigDecimal("select COALESCE(SUM(frozeBalance),0) from t_funds where loanTotal < 1").longValue();
		long rgcz = Db.queryBigDecimal("select COALESCE(SUM(traceAmount),0) from t_recharge_trace where rechargeType = 'SYS' and traceState = 'B' and bankState = 'B' and traceDate = ?",date).longValue();
		long llzfcz = Db.queryBigDecimal("select COALESCE(SUM(traceAmount),0) from t_recharge_trace where rechargeType = 'LL' and traceState = 'B' and bankState = 'B' and traceDate = ?",date).longValue();
		long syxcz = Db.queryBigDecimal("select COALESCE(SUM(traceAmount),0) from t_recharge_trace where rechargeType = 'SYX' and traceState = 'B' and bankState = 'B' and traceDate = ?",date).longValue();
		long jrcjze = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and effectDate = ?",date).longValue();
		long rgtx = Db.queryBigDecimal("select COALESCE(SUM(withdrawAmount),0) from t_withdraw_trace where withdrawType = 'SYS' and `status` = '3' and createDateTime >= ? and createDateTime <= ?",date+"000000",date+"235959").longValue();
		long llzftx = Db.queryBigDecimal("select COALESCE(SUM(withdrawAmount),0) from t_withdraw_trace where withdrawType = 'LL' and `status` = '3' and createDateTime >= ? and createDateTime <= ?",date+"000000",date+"235959").longValue();
		long syxtx = Db.queryBigDecimal("select COALESCE(SUM(withdrawAmount),0) from t_withdraw_trace where withdrawType = 'SYX' and `status` = '3' and createDateTime >= ? and createDateTime <= ?",date+"000000",date+"235959").longValue();
		long sjhkbj = Db.queryBigDecimal("select COALESCE(SUM(recyAmount),0) from t_history_recy where recyDate = ?",date).longValue();
		long sjhklx = Db.queryBigDecimal("select COALESCE(SUM(recyInterest),0) from t_history_recy where recyDate = ?",date).longValue();
		long yihbj = sjhkbj;
		long yihlx = sjhklx;
		long tqhkbj = Db.queryBigDecimal("select COALESCE(SUM(recyAmount),0) from t_history_recy where loanState = 'P' and recyDate = ?",date).longValue();
		long ljcjze = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R');").longValue();
		long ljzq = Db.queryBigDecimal("select COALESCE(SUM(reciedInterest),0) from t_funds").longValue();
		long m13 = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and loanTimeLimit >= 1 and loanTimeLimit <=3 and effectDate =?",date).longValue();
		long m46 = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and loanTimeLimit >= 4 and loanTimeLimit <=6 and effectDate =?",date).longValue();
		long m712 = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and loanTimeLimit >= 7 and loanTimeLimit <=12 and effectDate =?",date).longValue();
		long m1318 = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and loanTimeLimit >= 13 and loanTimeLimit <=18 and effectDate =?",date).longValue();
		long pa = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and productType='A' and effectDate = ?",date).longValue();
		long pb = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and productType='B' and effectDate = ?",date).longValue();
		long pc = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and productType='C' and effectDate = ?",date).longValue();
		long noob = Db.queryBigDecimal("select COALESCE(SUM(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q','R') and benefits4new > 0 and effectDate =?",date).longValue();
		long jrzqzr = Db.queryBigDecimal("select COALESCE(SUM(transAmount),0) from t_loan_transfer where gotDate = ? and transState = 'B'",date).longValue();
		long ljzqzr = Db.queryBigDecimal("select COALESCE(SUM(transAmount),0) from t_loan_transfer where transState = 'B'").longValue();
		long zjds =Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal+beRecyInterest),0) from t_funds").longValue();
		long fxbfj = Db.queryBigDecimal("select COALESCE(sum(riskTotal),0) from t_sys_funds").longValue();
		PlatformCount platformCount = PlatformCount.platformCountDao.findById(date);
		if(platformCount!=null){
			platformCount.set("kyye", kyye);platformCount.set("djye", djye);platformCount.set("rgcz", rgcz);platformCount.set("llzfcz", llzfcz);platformCount.set("syxcz", syxcz);platformCount.set("jrcjze", jrcjze);
			platformCount.set("rgtx", rgtx);platformCount.set("llzftx", llzftx);platformCount.set("syxtx", syxtx);platformCount.set("sjhkbj", sjhkbj);platformCount.set("sjhklx", sjhklx);
			platformCount.set("yihbj", yihbj);platformCount.set("yihlx", yihlx);platformCount.set("tqhkbj", tqhkbj);platformCount.set("ljcjze", ljcjze);
			platformCount.set("ljzq", ljzq);platformCount.set("m13", m13);platformCount.set("m46", m46);platformCount.set("m712", m712);platformCount.set("m1318", m1318);
			platformCount.set("pa", pa);platformCount.set("pb", pb);platformCount.set("pc", pc);platformCount.set("noob", noob);
			platformCount.set("jrzqzr", jrzqzr);platformCount.set("ljzqzr", ljzqzr);platformCount.set("zjds", zjds);platformCount.set("fxbfj", fxbfj);
			return platformCount.update();
		}
		return false;
	}

}
