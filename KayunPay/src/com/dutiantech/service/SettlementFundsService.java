package com.dutiantech.service;

import com.dutiantech.model.SettlementFunds;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;

public class SettlementFundsService extends BaseService {
	
	public String save(String userCode, String loanNo, int recyLimit,int loanTimeLimit, String sfState, long principal, long interest, long traceFee){
		SettlementFunds sf = new SettlementFunds();
		String seCode = UIDUtil.generate();
		sf.set("seCode", seCode);
		sf.set("userCode", userCode);
		sf.set("loanNo", loanNo);
		sf.set("recyLimit", recyLimit);
		sf.set("loanTimeLimit", loanTimeLimit);
		sf.set("sfState", sfState);
		sf.set("crDate", DateUtil.getNowDate());
		sf.set("crTime", DateUtil.getNowTime());
		sf.set("okDate", "00000000");
		sf.set("okTime", "000000");
		sf.set("principal", principal);
		sf.set("interest", interest);
		sf.set("traceFee", traceFee);
		sf.set("fks", "1");//1未处理，0已处理
		if(sf.save())
			return seCode;
		return null;
	}

}
