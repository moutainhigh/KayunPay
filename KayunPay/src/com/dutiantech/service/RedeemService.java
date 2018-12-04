package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.Redeem;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;

public class RedeemService extends BaseService {
	/**
	 * 添加兑换码
	 * */
	public boolean saveRedeemCode(Redeem redeem){
		redeem.set("createDateTime", DateUtil.getNowDateTime());
		redeem.set("redeemCode", CommonUtil.getRedeemCode());
		redeem.set("rState", "A");
		return redeem.save();
	}
	/**
	 * 查询兑换码
	 * */
	public List<Redeem> queryRedeemCode(String type,String rState,String expDate){
		return Redeem.redeemDao.find("select * from t_redeem where type=? and rState=? and expDate=?",type,rState,expDate);
	}
	/**
	 * 修改使用状态
	 * */
	public boolean upDateRedeemCode(Redeem redeem){
		redeem.set("useDateTime", DateUtil.getNowDateTime());
		redeem.set("rState", "E");
		return redeem.update();
	}
}
