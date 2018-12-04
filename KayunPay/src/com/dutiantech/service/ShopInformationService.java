package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.ShopInformation;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;

public class ShopInformationService extends BaseService{
	
	//添加或更新门店信息
	public boolean saveInformation(ShopInformation shopInformation){
		String shopNum = shopInformation.getStr("shopNum");
		List<ShopInformation> find = ShopInformation.shopInformationDao.find("select * from t_shop_information where shopNum =?", shopNum);
		if(find.isEmpty()||find.size()<0){
			shopInformation.set("addDateTime", DateUtil.getNowDateTime());
			shopInformation.set("upDateTime",DateUtil.getNowDateTime());
			return shopInformation.save();
		}else{
			int update = Db.update("update t_shop_information set shopContent=?,upDateTime=? where shopNum=?",shopInformation.getStr("shopContent"),DateUtil.getNowDateTime(),shopNum);
			return update>0?true:false;
		}

	}
	
	//查询信息
	public ShopInformation queryInformationByShopNum(String shopNum){
		
		List<ShopInformation> list = ShopInformation.shopInformationDao.find("select * from t_shop_information where shopNum=?", shopNum);
		if(!list.isEmpty()&&list.size()>0){
			return list.get(0);
		}else{
			return null;
		}
	}

}
