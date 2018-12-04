package com.dutiantech.service;

import com.dutiantech.model.ShortPassword;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;



public class PasswordService extends BaseService {
	
	private static final String basic_selectFields = "userCode,mid,wpId,openId,shortPasswd,createDateTime,modifyDateTime,mobileType,opUserCode ";

	/**
	 * 查询用户短密码信息
	 * @param userCode
	 * @param mobileType 类型  IOS ANDROID WAP WP OTHER
	 * @return
	 */
	public ShortPassword queryShortPassword(String userCode,String mobileType){
		String mid = "";
		try {
			mid = MD5Code.md5(userCode+mobileType);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
//		return ShortPassword.passwordDao.findFirst("select " + basic_selectFields + " from t_short_password where mid = ? and userCode = ? and mobileType = ?" , 
//				mid , userCode , mobileType);
		return ShortPassword.passwordDao.findById(mid);
	}
	
	/**
	 * 查询用户短密码信息
	 * @param openId
	 * @return
	 */
	public ShortPassword queryShortPassword(String openId){
		return ShortPassword.passwordDao.findFirst("select " + basic_selectFields + " from t_short_password where openId = ? and mobileType = 'WX'" , openId);
	}
	
	
	/**
	 * 删除(取消)短密码信息
	 * @param userCode
	 * @param mobileType 类型  IOS ANDROID WAP WP OTHER
	 * @return
	 */
	public boolean deleteShortPassword(String userCode,String mobileType){
		try {
			String mid = MD5Code.md5(userCode+mobileType);
			return ShortPassword.passwordDao.deleteById(mid);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	/**
	 * 初始化短密码
	 * @param userCode		用户userCode
	 * @param password		密码
	 * @param mobileType	类型  IOS ANDROID WAP WP OTHER
	 * @param openId		微信唯一标识
	 * @return
	 */
	public boolean inintShortPassword(String userCode,String password,String mobileType,String openId){
		ShortPassword sp = new ShortPassword();
		String mid = "";
		try {
			mid = MD5Code.md5(userCode+mobileType);
			sp.set("mid", mid);
			sp.set("shortPasswd",MD5Code.md5(password));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		//删除之前设置密码
		ShortPassword.passwordDao.deleteById(mid);
		//保存
		sp.set("userCode", userCode);
		sp.set("openId", openId);
		sp.set("createDateTime", DateUtil.getNowDateTime());
		sp.set("modifyDateTime", DateUtil.getNowDateTime());
		sp.set("mobileType", mobileType);
		sp.set("opUserCode", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		return sp.save();
	}
	
	/**
	 * 短密码登录验证
	 * @param mid		
	 * @param password
	 * @return
	 */
	public ShortPassword validateShortPassword(String mid,String password){
		String dbPassword = "";
		try {
			dbPassword = MD5Code.md5(password);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		ShortPassword sp = ShortPassword.passwordDao.findFirst("select " + basic_selectFields + " from t_short_password where mid = ? and shortPasswd = ?" , 
				mid , dbPassword);
		return sp;
	}
	
	
	
}





