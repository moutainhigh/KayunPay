package com.dutiantech.model;

import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Model;

public class FanLiTouUserInfo extends Model<FanLiTouUserInfo> {
	
	private static final long serialVersionUID = 2847002358471786782L;
	
	public static final FanLiTouUserInfo fanlitouDao = new FanLiTouUserInfo();

	/**
	 * 	通过手机号查询
	 * @param mobile	加密,规则同用户手机号
	 * @return
	 */
	public FanLiTouUserInfo queryByMobile(String mobile){
		String userMobile;
		try {
			userMobile = CommonUtil.encryptUserMobile(mobile);
		} catch (Exception e) {
			return null;
		}
		
		FanLiTouUserInfo userInfo = findFirst("select * from t_flt_userinfo where mobile = ?", userMobile ) ;
		
		return userInfo ;
	}
	
	/**
	 * 	与返利投绑定
	 * @param userCode
	 * @param userName
	 * @param userMobile
	 * @param fcode
	 * @param uid
	 * @param bindType
	 * 		0	-- 	新注册用户
	 * 		1	--	老用户绑定
	 * @return
	 */
	public String saveUser(String userCode , String userName , String userMobile , String fcode , String uid , int bindType ){
		FanLiTouUserInfo newUser = new FanLiTouUserInfo() ;
		String token = UIDUtil.generate() ;
		newUser.set("userCode", userCode ) ;
		newUser.set("userName", userName ) ;
		try {
			newUser.set("mobile", CommonUtil.encryptUserMobile(userMobile) ) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		newUser.set("fltuid", uid ) ;
		newUser.set("token", token ) ;
		newUser.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
		newUser.set("bindType", bindType ) ;
		if( newUser.save() == true ){
			return token;
		}else{
			return null ;
		}
	}
	
	public FanLiTouUserInfo queryByUID(String uid){
		return findFirst("select * from t_flt_userinfo where fltuid=?" , uid ) ;
	}
	
}
