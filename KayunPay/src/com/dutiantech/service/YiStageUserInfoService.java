package com.dutiantech.service;

import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.util.DateUtil;

public class YiStageUserInfoService extends BaseService {

	public YiStageUserInfo findYiStageUserInfo(String mobile) {
		return YiStageUserInfo.yiStageUserInfoDao.findFirst("SELECT * FROM t_yistage_userinfo WHERE mobile = ?",
				mobile);
	}

	/**
	 * 保存易分期用户信息
	 * 
	 * @param workType
	 * @param workCity
	 * @param education
	 * @param address
	 * @param age
	 * @param sex
	 * @param userName
	 * @param userMobile
	 *            明文
	 * @param userCode
	 */
	public boolean saveYfqUser(String userCode, String userName, String userMobile, String sex, String age,
			String address, String education, String workCity, String workType,String qq,String wechat,String email) {
		YiStageUserInfo newUser = new YiStageUserInfo();
		newUser.set("userCode", userCode);
		newUser.set("userName", userName);
		newUser.set("mobile", userMobile);
		newUser.set("sex", sex);
		newUser.set("age", age);
		newUser.set("address", address);
		newUser.set("education", education);
		newUser.set("workCity", workCity);
		newUser.set("workType", workType);
		newUser.set("qq", qq);
		newUser.set("wechat", wechat);
		newUser.set("email", email);
		
		newUser.set("regDateTime", DateUtil.getNowDateTime());
		newUser.set("modifyDateTime", DateUtil.getNowDateTime());
		return newUser.save();
	}

	/**
	 * 通过手机号查询
	 * 
	 * @param mobile
	 *            手机号明文
	 * @return
	 */
	public YiStageUserInfo queryByMobile(String mobile) {
		YiStageUserInfo yiStageUserInfo = YiStageUserInfo.yiStageUserInfoDao
				.findFirst("SELECT * FROM t_yistage_userinfo WHERE mobile = ?", mobile);
		return yiStageUserInfo;
	}

	/**
	 * 通过userCode查询
	 * 
	 * @param userCode
	 * @return
	 */
	public YiStageUserInfo queryByUserCode(String userCode) {
		YiStageUserInfo yiStageUserInfo = YiStageUserInfo.yiStageUserInfoDao
				.findFirst("SELECT * FROM t_yistage_userinfo WHERE userCode = ?", userCode);
		return yiStageUserInfo;
	}
}
