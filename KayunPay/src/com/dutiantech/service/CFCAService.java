package com.dutiantech.service;

import com.dutiantech.model.CFCAInfo;
import com.dutiantech.util.DateUtil;


/**
 * CFCA服务
 * @author shiqingsong
 *
 */
public class CFCAService extends BaseService{

	/**
	 * 保存CFCA信息
	 * @param bCode
	 * @param recordNo
	 * @param type
	 * @return
	 */
	public boolean saveCFCA(String bCode ,String recordNo , String type){
		CFCAInfo cfca = new CFCAInfo();
		cfca.set("bCode", bCode);
		cfca.set("recordNo", recordNo);
		cfca.set("type", type);
		cfca.set("addDateTime", DateUtil.getNowDateTime());
		
		CFCAInfo cfcaInfo = queryByCode(bCode);
		
		if(null != cfcaInfo){
			cfcaInfo.set("recordNo", recordNo);
			cfcaInfo.set("addDateTime", DateUtil.getNowDateTime());
			return cfcaInfo.update();
		}
		
		return cfca.save();
	}
	
	public CFCAInfo queryByCode(String bCode){
		String sql = "select * from t_cfca_info where bCode = ?";
		return CFCAInfo.cfcaDao.findFirst(sql, bCode);
	}
	
	
}










