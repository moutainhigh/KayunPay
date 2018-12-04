package com.dutiantech.service;

import com.dutiantech.model.TransferWay;
import com.dutiantech.util.DateUtil;

public class TransferWayService extends BaseService{

	/**
	 * 查询用户债转方案
	 * */
	public TransferWay findByUserCode(String userCode){
		return TransferWay.transferWayDao.findById(userCode);
	}
	
	/**
	 * 保存用户债转方案
	 * */
	public boolean saveTransferWay(TransferWay transferWay){
		transferWay.set("setDateTime", DateUtil.getNowDateTime());
		return transferWay.save();
	}
}
