package com.dutiantech.controller.admin;

import com.dutiantech.Message;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Redeem;
import com.dutiantech.service.RedeemService;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class RedeemController extends BaseController{
	
	private RedeemService redeemService = getService(RedeemService.class);
	/**
	 * 产生新的随机8位兑换码
	 * */
	@ActionKey("/createRedeemCode")
	@Before({PkMsgInterceptor.class})
	public Message createRedeemCode(){
		String num = getPara("num");
		String type = getPara("type");
		String rName = getPara("rName");
		int rate = Integer.parseInt(getPara("rate","100"));
		String expDate=getPara("expDate");
		Redeem redeem = new Redeem();
		if(StringUtil.isBlank(num)){
			return error("01", "生成失败", null);
		}
		if("A".equals(type)){
			redeem.set("type", type);
			redeem.set("rate", rate);
			redeem.set("rName", rName);
			int nowdays=DateUtil.getDaysByYearMonth(expDate);
			expDate=expDate+nowdays;
			redeem.set("expDate", expDate);
			int num1=Integer.parseInt(num);
			for(int i = 0;i<num1;i++){
				redeemService.saveRedeemCode(redeem);
			}
		}else{
			return error("01", "生成失败", null);
		}
		return succ("OK", "生成"+num+"张随机兑换码");
	}

}
