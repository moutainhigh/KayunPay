package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.SettlementEarly;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;

public class SettlementEarlyService extends BaseService {
	
	public boolean save(String loanCode,String userCode,String userMobile,String userTrueName,String earlyDate,int earlyLimit,int loanTimeLimit){
		SettlementEarly se = new SettlementEarly();
		se.set("loanCode", loanCode);
		se.set("userCode", userCode);
		se.set("userMobile", userMobile);
		se.set("userTrueName", userTrueName);
		se.set("earlyDate", earlyDate);
		se.set("earlyLimit", earlyLimit);
		se.set("loanTimeLimit", loanTimeLimit);
		se.set("estatus", "A");//A 预备提前还款   B提前还款结算中  C已提前还款，D取消提前还款
		se.set("settlementDate", "00000000");
		se.set("settlementTime", "000000");
		long x = Db.queryLong("select count(loanCode) from t_settlement_early where loanCode = ?",loanCode);
		if(x>0){
			return se.update();
		}
		return se.save();
	}
	
	public SettlementEarly findById(String loanCode){
		return SettlementEarly.settletmentEaryltDao.findById(loanCode);
	}
	
	public List<SettlementEarly> findListByToday(){
		return SettlementEarly.settletmentEaryltDao.find("select * from t_settlement_early where earlyDate = ? and estatus = 'A'",DateUtil.getNowDate());
	}
	
	/**
	 * 根据日期查询提前还款 WJW
	 * @param date 日期
	 * @return
	 */
	public List<SettlementEarly> findListByDate(String date){
		return SettlementEarly.settletmentEaryltDao.find("select * from t_settlement_early where earlyDate = ? and estatus = 'A'",date);
	}
	
	public SettlementEarly queryearly(String loanCode){
		return SettlementEarly.settletmentEaryltDao.findFirst("select * from t_settlement_early where loanCode = ? and estatus = 'A'",loanCode);
	}
	
	/**
	 * 查询全部提前还款标(不含已取消) WJW
	 * @param date
	 * @return
	 */
	public List<SettlementEarly> queryEarlyAll(){
		return SettlementEarly.settletmentEaryltDao.find("select loanCode,earlyDate,estatus from t_settlement_early where estatus in ('A','C')");
	}
	
	/**
	 * 更新提前还款表状态 WJW
	 * @param settlementEarly
	 * @return
	 */
	public boolean updateStatus(SettlementEarly settlementEarly,String estatus){
		settlementEarly.set("settlementDate", DateUtil.getNowDate());
		settlementEarly.set("settlementTime", DateUtil.getNowTime());
		settlementEarly.set("estatus", estatus);
		return settlementEarly.update();
	}
}
