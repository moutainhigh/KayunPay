package com.dutiantech.service;

import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class WithdrawFreeService extends BaseService {
	
	/**
	 * 设置免费提现次数
	 * @param userCode 用户编码
	 * @param num	增加的数量，可以+1 也可以-1
	 * @return
	 */
	public boolean setFreeCount(String userCode, int num){
		long x = Db.queryLong("select count(userCode) from t_withdraw_free where userCode = ?", userCode);
		if(x < 1){
			Record free = new Record();
			free.set("userCode", userCode);
			free.set("updateDate", DateUtil.getNowDate());
			free.set("updateTime", DateUtil.getNowTime());
			free.set("freeCount", 0);
			Db.save("t_withdraw_free","userCode", free);
		}
		return Db.update("update t_withdraw_free set freeCount = freeCount + ?,updateDate = ?,updateTime = ? where userCode = ?",num,DateUtil.getNowDate(),DateUtil.getNowTime(), userCode) > 0;
	}
	
	public int findFreeCountByUserCode(String userCode){
		Integer x =  Db.queryInt("select freeCount from t_withdraw_free where userCode = ?",userCode);
		return x==null ? 0 : x.intValue() ;
	}

}
