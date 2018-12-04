package com.dutiantech.service;

import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.model.User;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jx.service.JXService;
import com.jx.util.RetCodeUtil;

public class UserTermsAuthService extends BaseService {

	/**
	 * 同步用户授权信息
	 * @param user
	 * @return
	 */
	public boolean syncUserTermsAuth(User user) {
		UserTermsAuth userTermsAuth = findByJxAccountId4Online(user.getStr("jxAccountId"));
		userTermsAuth = saveOrUpdate(userTermsAuth);
		return false;
	}
	
	/**
	 * 查询用户授权信息
	 * @param userCode
	 * @return
	 */
	public UserTermsAuth findByUserCode(String userCode) {
		return UserTermsAuth.userTermsAuthDao.findById(userCode);
	}
	
	/**
	 * 查询本地用户授权信息(本地若没有,则查询存管且保存) WJW
	 * @param userCode
	 * @return
	 */
	public UserTermsAuth findById(String userCode){
		UserTermsAuth userTermsAuth = UserTermsAuth.userTermsAuthDao.findById(userCode);
		if(userTermsAuth == null){
			User user = User.userDao.findById(userCode);
			String jxAccountId = user.getStr("jxAccountId");
			if(StringUtil.isBlank(jxAccountId)){
				return null;
			}
			userTermsAuth = findByJxAccountId4Online(jxAccountId);
			saveOrUpdate(userTermsAuth);
		}
		return userTermsAuth;
	}
	
	/**
	 * 根据电子账号查询UserTermsAuth对象(本地若没有,则查询存管且保存) WJW
	 * @param jxAccountId
	 * @return
	 */
	public UserTermsAuth findByJxAccountId(String jxAccountId){
		String sql = "select * from t_user_terms_auth where jxAccountId=?";
		UserTermsAuth userTermsAuth = UserTermsAuth.userTermsAuthDao.findFirst(sql,jxAccountId);
		if(userTermsAuth == null){
			userTermsAuth = findByJxAccountId4Online(jxAccountId);
			saveOrUpdate(userTermsAuth);
		}
		return userTermsAuth;
	}
	
	/**
	 * 在线查询用户授权信息，并转换成UserTermsAuth对象
	 * @param jxAccountId	江西银行存管账号
	 * @return	
	 */
	public UserTermsAuth findByJxAccountId4Online(String jxAccountId) {
		Map<String, String> mapUserTermsAuth = JXQueryController.termsAuthQuery(jxAccountId);
		UserTermsAuth userTermsAuth = null;
		if(mapUserTermsAuth != null && RetCodeUtil.isSuccRetCode(mapUserTermsAuth.get("retCode"))){
			User user = User.userDao.findFirst("SELECT userCode, userName FROM t_user WHERE jxAccountId = ?", jxAccountId);
			
			userTermsAuth = new UserTermsAuth();
			userTermsAuth.set("userCode", user.getStr("userCode"));
			userTermsAuth.set("userName", user.getStr("userName"));
			userTermsAuth.set("jxAccountId", jxAccountId);
			
			userTermsAuth.set("autoBid", mapUserTermsAuth.get("autoBid"));
			userTermsAuth.set("autoBidMaxAmt", StringUtil.getMoneyCent(mapUserTermsAuth.get("autoBidMaxAmt")));
			userTermsAuth.set("autoBidDeadline", mapUserTermsAuth.get("autoBidDeadline"));
			
//			userTermsAuth.set("autoCredit", mapUserTermsAuth.get("autoTransfer"));
			
			userTermsAuth.set("paymentAuth", mapUserTermsAuth.get("paymentAuth"));
			userTermsAuth.set("paymentMaxAmt", StringUtil.getMoneyCent(mapUserTermsAuth.get("paymentMaxAmt")));
			userTermsAuth.set("paymentDeadline", mapUserTermsAuth.get("paymentDeadline"));
			
			userTermsAuth.set("repayAuth", mapUserTermsAuth.get("repayAuth"));
			userTermsAuth.set("repayMaxAmt", StringUtil.getMoneyCent(mapUserTermsAuth.get("repayMaxAmt")));
			userTermsAuth.set("repayDeadline", mapUserTermsAuth.get("repayDeadline"));
			
			userTermsAuth.set("channel", mapUserTermsAuth.get("channel"));
		}
		return userTermsAuth;
	}
	
	/**
	 * 将json数据转为UserTermsAuth对象
	 * @param jxTraceCode
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public UserTermsAuth transByJSONStr(String jsonStr) {
		Map<String, ?> map = JSONObject.parseObject(jsonStr);
		map = (Map<String, String>) map;
		
		User user = User.userDao.findFirst("SELECT userCode, userName FROM t_user WHERE jxAccountId = ?", map.get("accountId"));

		UserTermsAuth userTermsAuth = null;
		if (user != null) {
			userTermsAuth = new UserTermsAuth();
			
			userTermsAuth.set("userCode", user.getStr("userCode"));
			userTermsAuth.set("userName", user.getStr("userName"));
			userTermsAuth.set("jxAccountId", map.get("accountId"));
			
			userTermsAuth.set("autoBid", map.get("autoBid"));
			userTermsAuth.set("autoBidMaxAmt", StringUtil.getMoneyCent(map.get("autoBidMaxAmt").toString()));
			userTermsAuth.set("autoBidDeadline", map.get("autoBidDeadline"));
			
			userTermsAuth.set("autoCredit", map.get("autoCredit"));
			userTermsAuth.set("autoCreditMaxAmt", StringUtil.getMoneyCent(map.get("autoCreditMaxAmt").toString()));
			userTermsAuth.set("autoCreditDeadline", map.get("autoCreditDeadline"));
			
			userTermsAuth.set("paymentAuth", map.get("paymentAuth"));
			userTermsAuth.set("paymentMaxAmt", StringUtil.getMoneyCent(map.get("paymentMaxAmt").toString()));
			userTermsAuth.set("paymentDeadline", map.get("paymentDeadline"));
			
			userTermsAuth.set("repayAuth", map.get("repayAuth"));
			userTermsAuth.set("repayMaxAmt", StringUtil.getMoneyCent(map.get("repayMaxAmt").toString()));
			userTermsAuth.set("repayDeadline", map.get("repayDeadline"));
			
			userTermsAuth.set("channel", map.get("channel"));
		}
		return userTermsAuth;
	}
	
	/**
	 * 保存（更新）用户存管授权信息
	 * @param userTermsAuth
	 * @return
	 */
	public UserTermsAuth saveOrUpdate(UserTermsAuth userTermsAuth) {
		if (userTermsAuth == null) {
			return null;
		}
		UserTermsAuth updateUserTermsAuth = UserTermsAuth.userTermsAuthDao.findById(userTermsAuth.getStr("userCode"));
		if (updateUserTermsAuth == null) {
			if (StringUtil.isBlank(userTermsAuth.getStr("autoBid"))) {
				userTermsAuth.set("autoBid", "0");
			}
			if (StringUtil.isBlank(userTermsAuth.getStr("autoCredit"))) {
				userTermsAuth.set("autoCredit", "0");
			}
			if (StringUtil.isBlank(userTermsAuth.getStr("paymentAuth"))) {
				userTermsAuth.set("paymentAuth", "0");
			}
			if (StringUtil.isBlank(userTermsAuth.getStr("repayAuth"))) {
				userTermsAuth.set("repayAuth", "0");
			}
			userTermsAuth.set("authDateTime", DateUtil.getNowDateTime());
			userTermsAuth.set("updateDateTime", DateUtil.getNowDateTime());
			if (userTermsAuth.save()) {
				return userTermsAuth;
			}
		} else {
			if (!StringUtil.isBlank(userTermsAuth.getStr("autoBid"))) {
				updateUserTermsAuth.set("autoBid", userTermsAuth.getStr("autoBid"));
				updateUserTermsAuth.set("autoBidMaxAmt", userTermsAuth.getLong("autoBidMaxAmt"));
				updateUserTermsAuth.set("autoBidDeadline", userTermsAuth.getStr("autoBidDeadline"));
			}
			if (!StringUtil.isBlank(userTermsAuth.getStr("autoCredit"))) {
				updateUserTermsAuth.set("autoCredit", userTermsAuth.getStr("autoCredit"));
				updateUserTermsAuth.set("autoCreditMaxAmt", userTermsAuth.getLong("autoCreditMaxAmt"));
				updateUserTermsAuth.set("autoCreditDeadline", userTermsAuth.getStr("autoCreditDeadline"));
			}
			if (!StringUtil.isBlank(userTermsAuth.getStr("paymentAuth"))) {
				updateUserTermsAuth.set("paymentAuth", userTermsAuth.getStr("paymentAuth"));
				updateUserTermsAuth.set("paymentMaxAmt", userTermsAuth.getLong("paymentMaxAmt"));
				updateUserTermsAuth.set("paymentDeadline", userTermsAuth.getStr("paymentDeadline"));
			}
			if (!StringUtil.isBlank(userTermsAuth.getStr("repayAuth"))) {
				updateUserTermsAuth.set("repayAuth", userTermsAuth.getStr("repayAuth"));
				updateUserTermsAuth.set("repayMaxAmt", userTermsAuth.getLong("repayMaxAmt"));
				updateUserTermsAuth.set("repayDeadline", userTermsAuth.getStr("repayDeadline"));
			}
			updateUserTermsAuth.set("updateDateTime", DateUtil.getNowDateTime());
			if (updateUserTermsAuth.update()) {
				return updateUserTermsAuth;
			}
		}
		return null;
	}
	
	
	/**
	 * 客户授权功能查询
	 * @param accountId  电子账号
	 * @return txCode 交易代码;accountId 电子账号;name 持卡人姓名;autoBid 自动投标功能开通标志 0：未开通 1：已开通 ;autoTransfer 自动债转功能开通标志 0：未开通 1：已开通;
	 *         agreeWithdraw 预约取现功能开通标志  0：未开通 1：已开通;agreeDeduct 代扣签约  0：未开通 1：已开通; paymentAuth 缴费授权  0：未开通 1：已开通;repayAuth 还款授权  0：未开通 1：已开通;
	 *         autoBidDeadline  自动投标到期日; autoBidMaxAmt 自动投标签约最高金额;paymentDeadline 缴费授权到期日;paymentMaxAmt 缴费签约最高金额;
	 */
	public Map<String, String> queryByJxAccountID(String jxAccountID) {
		Map<String, String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(jxAccountID)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg", "电子账号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","termsAuthQuery");
		reqMap.put("accountId", jxAccountID);
		Map<String,String> termsAuth = JXService.requestCommon(reqMap);
		
		return termsAuth;
	}
	
	/**
	 * 检查用户授权是否正常
	 * @param userCode
	 * @param authType	autoBid-自动投标授权; autoCredit-自动债转授权; repayAuth-还款授权; paymentAuth-缴费授权
	 * @return	true-正常
	 * 			false-未开通或已过期
	 */
	public boolean checkTermsAuth(String userCode, String authType){
		UserTermsAuth userTermsAuth = findById(userCode);
		if (userTermsAuth != null) {
			if ("1".equals(userTermsAuth.getStr(authType))) {
				String deadline = null;
				if ("autoBid".equals(authType)) {
					deadline = userTermsAuth.getStr("autoBidDeadline");
				} else if ("autoCredit".equals(authType)) {
					deadline = userTermsAuth.getStr("autoCreditDeadline");
				} else if ("repayAuth".equals(authType)) {
					deadline = userTermsAuth.getStr("repayDeadline");
				} else if ("paymentAuth".equals(authType)) {
					deadline = userTermsAuth.getStr("paymentDeadline");
				}
				if(DateUtil.compareDateByStr("yyyyMMdd", deadline, DateUtil.getNowDate()) == 1){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 更新用户风险提示书授权状态 WJW
	 * @param userCode
	 * @param riskReminder	1:授权
	 * @return
	 */
	public boolean updateRiskReminder(String userCode,String riskReminder){
		UserTermsAuth userTermsAuth = findById(userCode);
		if(userTermsAuth == null){
			return false;
		}
		userTermsAuth.set("riskReminder", riskReminder);
		return userTermsAuth.update();
	}
}
