package com.dutiantech.service;

import java.util.List;
import java.util.Map;

import com.dutiantech.model.UserInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;

public class UserInfoService extends BaseService {
	
	private static final String basic_selectFields = " userCode,userCardName,userCardId,isAuthed,userAdress,ecpNme1,ecpRlation1,ecpMbile1,ecpNme2,ecpRlation2,ecpMbile2,idType ";
	
	/**
	 * 根据ID查询用户认证信息
	 * @param userCode
	 * @return
	 */
	public UserInfo findById(String userCode){
		return UserInfo.userInfoDao.findByIdLoadColumns(userCode,basic_selectFields);
	}
	/**
	 * 查询指定字段
	 * @param userCode
	 * @param fields
	 * @return
	 */
	public UserInfo findByFields(String userCode, String fields){
		return UserInfo.userInfoDao.findByIdLoadColumns(userCode,fields);
	}
	
	/**
	 * 查询用户验证状态
	 * @param userCode
	 * @return	0未进行认证 1认证进行中 2认证通过 3认证失败
	 */
	public String isAuthed(String userCode){
		String x = UserInfo.userInfoDao.findByIdLoadColumns(userCode, "userCode,isAuthed").get("isAuthed",0);
		return x;
	}
	
	/**
	 * 修改用户认证信息
	 * @param userCode	用户编码
	 * @param para		Map，字段包含如下：
	 * userCardName		真实姓名
	 * userCardId		身份证编号
	 * userAdress		用户登记住址
	 * ecpNme1			紧急联系人1
	 * ecpRlation1		紧急联系人1与用户关系
	 * ecpMbile1		紧急联系人1电话
	 * ecpNme2			紧急联系人2
	 * ecpRlation2		紧急联系人2与用户关系
	 * ecpMbile2		紧急联系人2电话
	 * @return
	 */
	public boolean updateUserInfo(String userCode,Map<String,Object> para){
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		if( userInfo == null ){
			userInfo = new UserInfo() ;
			userInfo.set("userCode", userCode ) ;
			userInfo._setAttrs(para);
			return userInfo.save() ;
		}
		userInfo._setAttrs(para);
		return userInfo.update();
	}
	
	/**
	 * 更新用户认证状态
	 * @param userCode	用户编码
	 * @param isAuthed	认证状态
	 * @return
	 */
	public boolean updateIsAuthed(String userCode, String isAuthed){
		UserInfo userInfo = UserInfo.userInfoDao.findByIdLoadColumns(userCode,"userCode,isAuthed");
		userInfo.set("isAuthed", isAuthed);
		return userInfo.update();
	}
	
	/**
	 * 用户认证
	 * @param userCode
	 * @param isAuthed
	 * @param userCardName
	 * @param userCardId
	 * @param cardImg
	 * @return
	 */
	public boolean userAuth(String userCode,String userCardName,String userCardId,String cardImg,String isAuthed){
		int result = Db.update("update t_user_info set userCardName = ? ,userCardId = ? ,cardImg = ? ,isAuthed = ? where userCode = ? and isAuthed != ?", 
				userCardName,userCardId,cardImg,isAuthed,userCode,isAuthed);
		if(result <= 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 实名认证——新增证件类型
	 * @param userCode
	 * @param userCardName
	 * @param userCardId
	 * @param cardImg
	 * @param isAuthed
	 * @param idType
	 * @return
	 */
	public boolean newUserAuth(String userCode,String userCardName,String userCardId,String cardImg,String isAuthed,String idType){
		int result = Db.update("update t_user_info set userCardName = ? ,userCardId = ? ,cardImg = ? ,isAuthed = ? ,idType = ? where userCode = ? and isAuthed != ?", 
				userCardName,userCardId,cardImg,isAuthed,idType,userCode,isAuthed);
		if(result <= 0){
			return false;
		}
		return true;
	}
	
	/**
	 * 用户电子签章认证
	 * @param userCode
	 * @return
	 */
	public boolean userCFCA(String userCode){
		return Db.update("update t_user_info set isCFCA = 1 where userCode = ?", userCode) > 0 ? true : false;
	}
	
	/**
	 * 查询时间段内已实名认证的被邀请人信息
	 * @param userCode	邀请人用户Code
	 * @param beginDate	被邀请人注册开始日期
	 * @param endDate	被邀请人注册结束日期
	 * @return
	 */
	public List<UserInfo> queryRecommendByUserCode(String userCode, String beginDate, String endDate) {
		List<UserInfo> lstUserInfo = UserInfo.userInfoDao.find("SELECT " + basic_selectFields + " FROM t_user_info WHERE userCode IN(SELECT bUserCode FROM t_recommend_info WHERE aUserCode=? AND bRegDate BETWEEN ? AND ?) AND isAuthed='2'", userCode,beginDate,endDate);
		return lstUserInfo;
	}

	/**
	 * 根据身份证号查询用户
	 * @param cardId
	 * @return
	 */
	public UserInfo findByCardId(String cardId) {
		try {
			cardId = CommonUtil.encryptUserCardId(cardId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return UserInfo.userInfoDao.findFirst("SELECT " + basic_selectFields + " FROM t_user_info WHERE userCardId = ?", cardId);
	}
	
	/**
	 * 将包含字母的身份证号，全部更新为大写字母
	 * @param userCode
	 * @throws Exception 
	 */
	public void update4CardIdUpperCase(String userCode) throws Exception {
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		String userCardId = userInfo.getStr("userCardId");
		if (!StringUtil.isBlank(userCardId)) {
			userCardId = CommonUtil.decryptUserCardId(userCardId);
			if (userCardId.contains("x")) {
				userCardId = userCardId.toUpperCase();
				userCardId = CommonUtil.encryptUserCardId(userCardId);
				userInfo.set("userCardId", userCardId);
				userInfo.update();
			}
		}
	}
	
}