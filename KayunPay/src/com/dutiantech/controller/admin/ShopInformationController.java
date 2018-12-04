package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.ShopInformation;
import com.dutiantech.service.ShopInformationService;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class ShopInformationController extends BaseController{

	private ShopInformationService shopInformationService=getService(ShopInformationService.class);
	
	//添加信息
	@ActionKey("/addInformation")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message addInformation(){
		
		String serveMobile  = getPara("serveMobile","");
		String shop  = getPara("shop","");
		String shopNum  = getPara("shopNum","");
		String shopAddress  = getPara("shopAddress","");
		String shopPhone  = getPara("shopPhone","");
		long materialMoney  = getParaToLong("materialMoney",0L);
		long GPSManageMoney  = getParaToLong("GPSManageMoney",0L);
		String gatheringName  = getPara("gatheringName","");
		String gatheringBankName  = getPara("gatheringBankName","");
		String gatheringBankNumber  = getPara("gatheringBankNumber","");
		String onlineName  = getPara("onlineName","");
		String onlineAddress  = getPara("onlineAddress","");
		String onlinePhone  = getPara("onlinePhone","");
		String court  = getPara("court","");
		
		if(StringUtil.isBlank(shopNum)){
			return error("01","门店号不能为空",null);
		}
		JSONObject js = new JSONObject();
		js.put("serveMobile", serveMobile);
		js.put("shop", shop);
		js.put("shopAddress", shopAddress);
		js.put("shopPhone", shopPhone);
		js.put("materialMoney", materialMoney);
		js.put("GPSManageMoney", GPSManageMoney);
		js.put("gatheringName", gatheringName);
		js.put("gatheringBankName", gatheringBankName);
		js.put("gatheringBankNumber", gatheringBankNumber);
		js.put("onlineName", onlineName);
		js.put("onlineAddress", onlineAddress);
		js.put("onlinePhone", onlinePhone);
		js.put("court", court);
		
		String shopContent = js.toJSONString();
	
		ShopInformation shopInformation = new ShopInformation();
		shopInformation.set("shopNum", shopNum);
		
		shopInformation.set("shopContent",shopContent);
		boolean saveInformation = shopInformationService.saveInformation(shopInformation);
		if(saveInformation){
			return succ("保存成功", "保存成功");
		}else{
			return error("01", "保存失败", null);
		}
		
	}
	
	//查询信息
	@ActionKey("/queryInformationByShopNum")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryInformationByShopNum(){
		String shopNum = getPara("shopNum");
		if(StringUtil.isBlank(shopNum)){
			return error("01", "门店为空", null);
		}
		Map<String,Object> result = new HashMap<String,Object>();
		ShopInformation shopInformation = shopInformationService.queryInformationByShopNum(shopNum);
		if(shopInformation != null){
			String shopContent= shopInformation.getStr("shopContent");
			JSONObject jsStr = JSONObject.parseObject(shopContent);
			result.put("shopNum",shopNum);
			result.put("shopContent",jsStr);
			return succ("查询成功",result);
		}
		return null;
		
	}
}
