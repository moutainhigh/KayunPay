package com.dutiantech.controller.branch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.service.YistageTraceService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jx.service.JXService;

public class YiStageNotifyController extends BaseController {

	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private BanksService banksService = getService(BanksService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private YistageTraceService yistageTraceService = getService(YistageTraceService.class);
	
	private static final Logger yiStageNotifyLogger = Logger.getLogger("yiStageNotifyLogger");

	static {
		LoggerUtil.initLogger("yiStageNotify", yiStageNotifyLogger);
	}

	/**
	 * yfq开通存管回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/openAccountCallbackForYfq")
	@AuthNum(value=999)
	public void openAccountCallbackForYfq(){
		String bgData = getPara("bgData");
		String userCode = getPara("uCode");
		String mobileLyfq = getPara("mobile");
		String notify = getPara("notify");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;
		String jxCode = getJxTraceCode(map);	// 获取交易流水号
		// 更新流水响应报文
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		BanksV2 banksV2 = null;
		UserInfo userInfo = null;
		String mobile = "";
//		String mingMobile="";
		String cardNo = "";
		String bankName = "";
		User user = null;
		String idType = "01";//证件类型
		String idNo = "";
		String mingIdNo = "";
		String trueName = "";
		String accountId = "";
		boolean isUpdate = false;
		Map<String,String> retMap = new HashMap<>();
		if (null != map && "00000000".equals(map.get("retCode"))) {// 存管开通成功
			// 存管电子账号
			accountId = map.get("accountId");
			idNo=map.get("idNo");
			idType = map.get("idType");
			trueName= map.get("name");
			
			mobile = map.get("mobile");//返回的存管手机号
			
//			mingMobile = mobile;
			mingIdNo = idNo;

			// 根据存管电子账号查询绑查关系(只查有效卡)
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
			if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

				List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
				if (list != null && list.size() > 0) {

					Map<String, String> cardMap = list.get(0);
					cardNo = cardMap.get("cardNo");// 有效存管卡号
					
						//查询所属银行
						String nameOfBank = BankUtil.getNameOfBank(cardNo);
						if(!StringUtil.isBlank(nameOfBank)){
							bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
						}
						
						if (StringUtil.isBlank(userCode)) {// 如果uCode为空，根据证件号码查询用户信息
							userInfo = userInfoService.findByCardId(idNo);
							userCode = userInfo.getStr("userCode");
						}else{
							userInfo = userInfoService.findById(userCode);
						}
						
						user = userService.findById(userCode);
						try {
							idNo = CommonUtil.encryptUserCardId(idNo);
							mobile = CommonUtil.encryptUserMobile(mobile);
						} catch (Exception e) {
							e.printStackTrace();
						}
//					}
					//实名认证
					if (!"2".equals(userInfo.getStr("isAuthed"))) {
						try {
							Message msgAuto = certificationAuto(userCode, trueName, idNo, idType);
							if (msgAuto != null) {
								BIZ_LOG_ERROR(userCode, BIZ_TYPE.IDENTIFY, "实名认证失败", null);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if("2".equals(userInfo.getStr("isAuthed"))){
						userInfo.set("idType", idType);
						userInfo.update();
					}
					
				} else {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "存管开户，未查找到用户的有效卡信息", null);
					return;
				}
				// 更新数据库数据
				banksV2 = banksService.findByUserCode(userCode);
				if (banksV2 != null) {
					banksV2.set("trueName", cardDetails.get("name"));
					banksV2.set("bankNo", cardNo);
					banksV2.set("bankName", bankName);
					banksV2.set("cardid", idNo);
					banksV2.set("mobile", mobile);// 存管手机号
					banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
					banksV2.set("ssn", jxCode);
					if (banksV2.update()) {
						user.set("jxAccountId", accountId);
						isUpdate = user.update();
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "激活银行卡_本地数据添加失败", null);
						return;
					}
				} else {// 添加银行卡信息
					BanksV2 bankV2 = getModel(BanksV2.class);
					bankV2.set("userCode", userCode);
					bankV2.set("userName", user.getStr("userName"));
					bankV2.set("trueName", cardDetails.get("name"));
					bankV2.set("bankName", bankName);
					bankV2.set("bankNo", cardNo);
					bankV2.set("bankType", "");
					bankV2.set("cardCity", "");
					bankV2.set("mobile", mobile);
					bankV2.set("createDateTime", DateUtil.getNowDateTime());
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
					bankV2.set("isDefault", "1");
					bankV2.set("status", "0");
					bankV2.set("agreeCode", jxCode);
					bankV2.set("ssn", jxCode);
					bankV2.set("cardid", idNo);
					if (bankV2.save()) {
						user.set("jxAccountId", accountId);
						isUpdate = user.update();
						if (!isUpdate) {
						
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加电子存管账户_本地数据更新失败", null);
							return;
						}
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加银行卡信息_本地数据更新失败", null);
						return;
					}
					//更新易分期用户信息
					YiStageUserInfo yfqUserInfo = yiStageUserInfoService.queryByUserCode(userCode);
					if(yfqUserInfo!=null){
						yfqUserInfo.set("userCardId", mingIdNo);
						yfqUserInfo.set("userCardName",trueName);
						if(!yfqUserInfo.update()){
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "易分期用户信息更新失败_本地数据更新失败", null);
							return;
						}
					}else{
						YiStageUserInfo yfq = new YiStageUserInfo();
						yfq.set("userCode", userCode);
						yfq.set("userName", user.getStr("userName"));
						yfq.set("mobile", mobileLyfq);
						yfq.set("userCardName", trueName);
						yfq.set("userCardId", mingIdNo);
						yfq.set("regDateTime", DateUtil.getNowDateTime());
						yfq.set("modifyDateTime", DateUtil.getNowDateTime());
						if(!yfq.save()){
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "易分期用户信息添加失败_本地数据添加失败", null);
							return;
						}
					}
				}
			} else {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "未查找到存管有效绑卡信息", null);
				return;
			}
		} else {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败", bgData);
			retMap.put("return_code","01");
			retMap.put("return_info","存管开户失败");
			
			retMap.put("mobile", mobileLyfq);
			retMap.put("bankNo",cardNo);
			retMap.put("bankName",bankName);
			retMap.put("name", "");
			retMap.put("accountId",accountId);
			retMap.put("status","0");
			renderText("success");
			yistageTraceService.updateResponseMessage(jxCode, JSON.toJSONString(retMap));
			HttpRequestUtil.sendPost(notify,JSON.toJSONString(retMap));
			return;
		}
		retMap.put("return_code","0000");
		retMap.put("return_info","存管开户成功");
		retMap.put("accountId", accountId);
		retMap.put("mobile",mobileLyfq);
		retMap.put("bankNo",cardNo);
		retMap.put("bankName",bankName);
		retMap.put("status","2");
		retMap.put("name",map.get("name"));
		renderText("success");
		yistageTraceService.updateResponseMessage(jxCode, JSON.toJSONString(retMap));
		HttpRequestUtil.sendPost(notify,JSON.toJSONString(retMap));
	
	}

	/**
	 * yfq开通存管回调   （暂时停用）
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/openAccountCallbackForYfq1")
	@AuthNum(value=999)
	public void openAccountCallbackForYfq1(){
		String bgData = getPara("bgData");
		String userCode = getPara("uCode");
		String mobileLyfq = getPara("mobile");
		String notify = getPara("notify");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;
		String jxCode = getJxTraceCode(map);	// 获取交易流水号
		// 更新流水响应报文
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		BanksV2 banksV2 = null;
		UserInfo userInfo = null;
		String mobile = "";
		String mingMobile="";
		String cardNo = "";
		String bankName = "";
		User user = null;
		String idType = "01";//证件类型
		String idNo = "";
		String mingIdNo = "";
		String trueName = "";
		String accountId = "";
		boolean isUpdate = false;
		Map<String,String> retMap = new HashMap<>();
		if ((null != map && "00000000".equals(map.get("retCode")))||((null!=map)&&!"0000000".equals(map.get("retCode"))&&"2".equals(map.get("status")))) {// 存管开通成功
			// 存管电子账号
			accountId = map.get("accountId");
			idNo=map.get("idNo");
			idType = map.get("idType");
			trueName= map.get("name");
			
			mingMobile = mobileLyfq;
			mingIdNo = idNo;

			// 根据存管电子账号查询绑查关系(只查有效卡)
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
			if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

				List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
				if (list != null && list.size() > 0) {

					Map<String, String> cardMap = list.get(0);
					cardNo = cardMap.get("cardNo");// 有效存管卡号
					mobile = mobileLyfq;
					// 根据电子账号查询用户手机号
//					Map<String, String> mobileMap = JXQueryController.mobileMaintainace(accountId, "0", mobile);
//					if (null != mobileMap && "00000000".equals(mobileMap.get("retCode"))) {
//						mobile = mobileMap.get("mobile");
//						mingMobile=mobile;
//						idNo = mobileMap.get("idNo");
//						mingIdNo = idNo;
//						trueName = mobileMap.get("name");
//						idType = mobileMap.get("idType");
						
						//查询所属银行
						String nameOfBank = BankUtil.getNameOfBank(cardNo);
						if(!StringUtil.isBlank(nameOfBank)){
							bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
						}
						
						if (StringUtil.isBlank(userCode)) {// 如果uCode为空，根据证件号码查询用户信息
							userInfo = userInfoService.findByCardId(idNo);
							userCode = userInfo.getStr("userCode");
						}else{
							userInfo = userInfoService.findById(userCode);
						}
						
						user = userService.findById(userCode);
						try {
							idNo = CommonUtil.encryptUserCardId(idNo);
							mobile = CommonUtil.encryptUserMobile(mobile);
						} catch (Exception e) {
							e.printStackTrace();
						}
//					}
					//实名认证
					if (!"2".equals(userInfo.getStr("isAuthed"))) {
						try {
							Message msgAuto = certificationAuto(userCode, trueName, idNo, idType);
							if (msgAuto != null) {
								BIZ_LOG_ERROR(userCode, BIZ_TYPE.IDENTIFY, "实名认证失败", null);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if("2".equals(userInfo.getStr("isAuthed"))){
						userInfo.set("idType", idType);
						userInfo.update();
					}
					
				} else {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "存管开户，未查找到用户的有效卡信息", null);
					return;
				}
				// 更新数据库数据
				banksV2 = banksService.findByUserCode(userCode);
				if (banksV2 != null) {
					banksV2.set("trueName", cardDetails.get("name"));
					banksV2.set("bankNo", cardNo);
					banksV2.set("bankName", bankName);
					banksV2.set("cardid", idNo);
					banksV2.set("mobile", mobile);// 存管手机号
					banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
					banksV2.set("ssn", jxCode);
					if (banksV2.update()) {
						user.set("jxAccountId", accountId);
						isUpdate = user.update();
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "激活银行卡_本地数据添加失败", null);
						return;
					}
				} else {// 添加银行卡信息
					BanksV2 bankV2 = getModel(BanksV2.class);
					bankV2.set("userCode", userCode);
					bankV2.set("userName", user.getStr("userName"));
					bankV2.set("trueName", cardDetails.get("name"));
					bankV2.set("bankName", bankName);
					bankV2.set("bankNo", cardNo);
					bankV2.set("bankType", "");
					bankV2.set("cardCity", "");
					bankV2.set("mobile", mobile);
					bankV2.set("createDateTime", DateUtil.getNowDateTime());
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
					bankV2.set("isDefault", "1");
					bankV2.set("status", "0");
					bankV2.set("agreeCode", jxCode);
					bankV2.set("ssn", jxCode);
					bankV2.set("cardid", idNo);
					if (bankV2.save()) {
						user.set("jxAccountId", accountId);
						isUpdate = user.update();
						if (!isUpdate) {
						
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加电子存管账户_本地数据更新失败", null);
							return;
						}
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加银行卡信息_本地数据更新失败", null);
						return;
					}
					//更新易分期用户信息
					YiStageUserInfo yfqUserInfo = yiStageUserInfoService.queryByUserCode(userCode);
					if(yfqUserInfo!=null){
						yfqUserInfo.set("userCardId", mingIdNo);
						yfqUserInfo.set("userCardName",trueName);
						if(!yfqUserInfo.update()){
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "易分期用户信息更新失败_本地数据更新失败", null);
							return;
						}
					}else{
						YiStageUserInfo yfq = new YiStageUserInfo();
						yfq.set("userCode", userCode);
						yfq.set("userName", user.getStr("userName"));
						yfq.set("mobile", mingMobile);
						yfq.set("userCardName", trueName);
						yfq.set("userCardId", mingIdNo);
						yfq.set("regDateTime", DateUtil.getNowDateTime());
						yfq.set("modifyDateTime", DateUtil.getNowDateTime());
						if(!yfq.save()){
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "易分期用户信息添加失败_本地数据添加失败", null);
							return;
						}
					}
				}
			} else {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "未查找到存管有效绑卡信息", null);
				return;
			}
		} else {
			if(map == null){
				BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败", bgData);
				retMap.put("return_code","01");
				retMap.put("return_info","存管开户失败,接收的数据为空");
			}
			else if(map !=null &&"0".equals(map.get("status"))){
				BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败", bgData);
				retMap.put("return_code","01");
				retMap.put("return_info","存管开户失败");
			}
			
			else{
				BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败,状态不明", bgData);
				retMap.put("return_code","01");
				retMap.put("return_info","存管开户失败,状态不明");
			}
			
			retMap.put("mobile", mobileLyfq);
			retMap.put("bankNo",cardNo);
			retMap.put("bankName",bankName);
			retMap.put("name", "");
			retMap.put("accountId",accountId);
			retMap.put("status",map.get("status"));
			renderText("success");
			yistageTraceService.updateResponseMessage(jxCode, JSON.toJSONString(retMap));
			HttpRequestUtil.sendPost(notify,JSON.toJSONString(retMap));
			return;
		}
		if("00000000".equals(map.get("retCode"))){
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户成功,交易密码设置成功", bgData);
			retMap.put("return_code","0000");
			retMap.put("return_info","存管开户成功，交易密码设置成功");
		}else{
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户成功,设置交易密码失败", bgData);
			retMap.put("return_code","0000");
			retMap.put("return_info","存管开户成功，设置交易密码失败");
		}
		retMap.put("accountId", accountId);
		retMap.put("mobile",mobileLyfq);
		retMap.put("bankNo",cardNo);
		retMap.put("bankName",bankName);
		retMap.put("status",map.get("status"));
		retMap.put("name",map.get("name"));
		renderText("success");
		yistageTraceService.updateResponseMessage(jxCode, JSON.toJSONString(retMap));
		HttpRequestUtil.sendPost(notify,JSON.toJSONString(retMap));
	
	}

	/**
	 * 缴费授权回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/openTermsAuthCallback")
	public void openTermsAuthCallback() {
		String bgData = getPara("bgData");
		String notify = getPara("notify");
		String mobile = getPara("mobile");
		if (StringUtil.isBlank(bgData)) {
			return;
		}
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> mapResp = (Map<String, String>) map;
		String jxTraceCode = getJxTraceCode(mapResp);
		// Map<String, String> mapResp = JSONObject.fromObject(bgData);
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxTraceCode, mapResp, JSON.toJSON(mapResp).toString().replace(",", ",\r\n"));
		yiStageNotifyLogger.log(Level.INFO, "[缴费授权回调][" + jxTraceCode + "]用户[" + mapResp.get("accountId")
				+ "]缴费授权处理完成..." + DateUtil.getNowDateTime());

		Map<String, Object> reMap = new HashMap<String, Object>();
		if (null != mapResp && "00000000".equals(mapResp.get("retCode"))) {
			renderText("success");
			reMap.put("txnDateTime", mapResp.get("txnDate") + mapResp.get("txnTime"));
			reMap.put("cancelDateTime", mapResp.get("cancelDate") + mapResp.get("cancelTime"));
			reMap.put("mobile", mobile);
			reMap.put("name", mapResp.get("name"));
			reMap.put("accountId", mapResp.get("accountId"));
			reMap.put("maxAmt", mapResp.get("maxAmt"));
			reMap.put("txState", mapResp.get("txState"));
			reMap.put("deadline", mapResp.get("deadline"));
			reMap.put("return_code", "0000");
			reMap.put("return_info", "开通缴费授权成功");
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
		} else {
			renderText("success");
			reMap.put("txnDateTime", "");
			reMap.put("cancelDateTime", "");
			reMap.put("mobile", mobile);
			reMap.put("name", StringUtil.isBlank(mapResp.get("name")) ? "" : mapResp.get("name"));
			reMap.put("accountId", StringUtil.isBlank(mapResp.get("accountId")) ? "" : mapResp.get("accountId"));
			reMap.put("maxAmt", StringUtil.isBlank(mapResp.get("maxAmt")) ? "" : mapResp.get("maxAmt"));
			reMap.put("txState", StringUtil.isBlank(mapResp.get("txState")) ? "" : mapResp.get("txState"));
			reMap.put("deadline", StringUtil.isBlank(mapResp.get("deadline")) ? "" : mapResp.get("deadline"));
			reMap.put("return_code", "01");
			reMap.put("return_info", "开通缴费授权失败");
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
		}
	}
	/**
	 * 20180927
	 * 多合一授权接口缴费授权回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/newOpenTermsAuthCallback")
	public void newOpenTermsAuthCallback() {
		String bgData = getPara("bgData");
		String notify = getPara("notify");
		String mobile = getPara("mobile");
		if (StringUtil.isBlank(bgData)) {
			return;
		}
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> mapResp = (Map<String, String>) map;
		String jxTraceCode = getJxTraceCode(mapResp);
		// Map<String, String> mapResp = JSONObject.fromObject(bgData);
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxTraceCode, mapResp, JSON.toJSON(mapResp).toString().replace(",", ",\r\n"));
		yiStageNotifyLogger.log(Level.INFO, "[缴费授权回调][" + jxTraceCode + "]用户[" + mapResp.get("accountId")
				+ "]缴费授权处理完成..." + DateUtil.getNowDateTime());

		Map<String, Object> reMap = new HashMap<String, Object>();
		if (null != mapResp && "00000000".equals(mapResp.get("retCode"))) {
			renderText("success");
			reMap.put("mobile", mobile);
			reMap.put("name", mapResp.get("name"));
			reMap.put("accountId", mapResp.get("accountId"));
			reMap.put("maxAmt", mapResp.get("paymentMaxAmt"));
			reMap.put("txState", mapResp.get("paymentAuth"));
			reMap.put("deadline", mapResp.get("paymentDeadline"));
			reMap.put("return_code", "0000");
			reMap.put("return_info", "开通缴费授权成功");
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
		} else {
			renderText("success");
			reMap.put("mobile", mobile);
			reMap.put("name", StringUtil.isBlank(mapResp.get("name")) ? "" : mapResp.get("name"));
			reMap.put("accountId", StringUtil.isBlank(mapResp.get("accountId")) ? "" : mapResp.get("accountId"));
			reMap.put("maxAmt", StringUtil.isBlank(mapResp.get("paymentMaxAmt")) ? "" : mapResp.get("paymentMaxAmt"));
			reMap.put("txState", StringUtil.isBlank(mapResp.get("paymentAuth")) ? "" : mapResp.get("paymentAuth"));
			reMap.put("deadline", StringUtil.isBlank(mapResp.get("paymentDeadline")) ? "" : mapResp.get("paymentDeadline"));
			reMap.put("return_code", "01"); 
			reMap.put("return_info", "开通缴费授权失败");
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
		}
	}
	/**
	 * 设置存管交易密码回调    1.3.5版本停用
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/passwordSetCallback")
	public void passwordSetCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		String notify = getPara("notify","");
		String mobile = getPara("mobile","");
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;
		String jxCode = getJxTraceCode(map);	// 获取交易流水号
		// 更新流水响应报文
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		Map<String,String> reMap = new HashMap<String,String>();
		
		if(null != map && "00000000".equals(map.get("retCode"))){
			//记录日志
			yiStageNotifyLogger.log(Level.INFO, String.format("[%s]存管密码设置成功", userCode));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "存管密码设置成功");
			renderText("success");
			reMap.put("mobile",mobile);
			reMap.put("accountId",map.get("accountId"));
			reMap.put("return_code","0000");
			reMap.put("return_info","密码设置成功");
			HttpRequestUtil.sendPost(notify,JSON.toJSONString(reMap));
			return;
			
		}else{
			//记录日志
			yiStageNotifyLogger.log(Level.INFO, String.format("[%s]存管密码设置失败：[%s][%s]", userCode, map.get("retCode"), map.get("retMsg")));
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "存管密码设置失败："+map.get("retCode")+","+map.get("retMsg"), null);
			renderText("success");
			reMap.put("mobile",mobile);
			reMap.put("accountId",map.get("accountId"));
			reMap.put("return_code","01");
			reMap.put("return_info","密码设置失败");
			HttpRequestUtil.sendPost(notify,JSON.toJSONString(reMap));
			return;
		}
		
	}

	/**
	 * 重置密码回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/yfq/repasswordSetCallback")
	public void repasswordSetCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		String notify = getPara("notify", "");
		String mobile = getPara("mobile", "");
		User user = userService.findById(userCode);
		Map<String, String> reMap = new HashMap<>();
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;

		if (map == null) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "重置密码响应参数为空", null);
			renderText("success");
			reMap.put("accountId", user == null ? "" : user.getStr("accountId"));
			reMap.put("return_code", "01");
			reMap.put("mobile",mobile);
			reMap.put("return_info", "重置密码响应参数为空");
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		}

		String jxTraceCode = getJxTraceCode(map); // 获取交易流水号
		JXService.updateJxTraceResponse(jxTraceCode.trim(), map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		if ("00000000".equals(map.get("retCode"))) {
			yiStageNotifyLogger.log(Level.INFO, String.format("[%s]重置密码成功", userCode));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "重置密码成功");
			renderText("success");
			reMap.put("accountId", map.get("accountId"));
			reMap.put("return_code", "0000");
			reMap.put("mobile", mobile);
			reMap.put("return_info", "重置密码成功");
			
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		} else {
			yiStageNotifyLogger.log(Level.WARNING,
					String.format("[%s]重置密码失败：[%s][%s]", userCode, map.get("retCode"), map.get("retMsg")));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "重置密码失败：" + map.get("retCode") + "," + map.get("retMsg"));
			renderText("success");
			reMap.put("accountId", map.get("accountId"));
			reMap.put("return_code", "01");
			reMap.put("return_info", "重置密码失败");
			reMap.put("mobile", mobile);
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		}
	}
	
	/**
	 * 密码修改回调
	 */
	@ActionKey("/yfq/repasswordUpdateCallback")
	public void repasswordUpdateCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		String notify = getPara("notify", "");
		String mobile = getPara("mobile", "");
		User user = userService.findById(userCode);
		Map<String, String> reMap = new HashMap<>();
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;

		if (map == null) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "修改存管密码响应参数为空", null);
			renderText("success");
			reMap.put("accountId", user == null ? "" : user.getStr("accountId"));
			reMap.put("return_code", "01");
			reMap.put("mobile",mobile);
			reMap.put("return_info", "修改存管密码响应参数为空");
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		}

		String jxTraceCode = getJxTraceCode(map); // 获取交易流水号
		JXService.updateJxTraceResponse(jxTraceCode.trim(), map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		if ("00000000".equals(map.get("retCode"))) {
			yiStageNotifyLogger.log(Level.INFO, String.format("[%s]修改密码成功", userCode));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "修改密码成功");
			renderText("success");
			reMap.put("accountId", map.get("accountId"));
			reMap.put("return_code", "0000");
			reMap.put("mobile", mobile);
			reMap.put("return_info", "修改密码成功");
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		} else {
			yiStageNotifyLogger.log(Level.WARNING,
					String.format("[%s]修改密码失败：[%s][%s]", userCode, map.get("retCode"), map.get("retMsg")));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "修改密码失败：" + map.get("retCode") + "," + map.get("retMsg"));
			renderText("success");
			reMap.put("accountId", map.get("accountId"));
			reMap.put("return_code", "01");
			reMap.put("return_info", "修改密码失败");
			reMap.put("mobile", mobile);
			yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
			HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
			return;
		}
	}
	
	/**
	 * 易分期提现回调接口
	 * */
	@ActionKey("/yfq/withdrawCallbackForYfq")
	public void withdrawCallbackForYfq(){
		String withdrawCode = getPara("withdrawCode","");//提现流水号
		String bgData = getPara("bgData");//江西响应参数
		String notify=getPara("notify");//易分期接收响应参数地址
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		@SuppressWarnings("unchecked")
		Map<String, String> resMap = (Map<String, String>) bgMap;
		Map<String, String> reMap = new HashMap<String, String>();
		reMap.put("accountId", "");
		reMap.put("mobile", "");
		reMap.put("txAmount", "");
		reMap.put("status", "");
		reMap.put("withdrawCode", "");
		if(null==resMap){
		reMap.put("return_code", "01");
		reMap.put("return_info", "提现回调接口响应参数是空的");
		HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
		return;
		}
		String jxTraceCode = getJxTraceCode(resMap);	// 交易流水号
		//存储响应报文
				JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
				WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
				//检查数据中有没有此流水的提现申请
				if(withdrawTrace == null){
					yiStageNotifyLogger.log(Level.INFO, String.format("提现流水号不存在:[%s]", withdrawCode));
					reMap.put("return_code", "02");
					reMap.put("return_info", "未找到流水号对应的提现订单");
					HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					return;
				}
				String userCode = withdrawTrace.getStr("userCode");
				User user= userService.findById(userCode);
				BanksV2 bankV2 = banksService.findByUserCode(userCode);
				String mobile="";
				try {
					mobile = CommonUtil.decryptUserMobile(bankV2.getStr("mobile"));
				} catch (Exception e) {
					yiStageNotifyLogger.log(Level.INFO, String.format("[%s]手机号解析异常", userCode));
				}
				
				String jxAccountId=user.getStr("jxAccountId");
				long sxf = (long)withdrawTrace.getInt("sxf");
				long amount = StringUtil.getMoneyCent(resMap.get("txAmount"));
				long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
				if(withdrawAmount != (amount + sxf)){
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "平台提现金额与存管提现金额不符", null);
					yiStageNotifyLogger.log(Level.INFO, String.format("[%s]平台提现金额与存管提现金额不符", userCode));
					reMap.put("return_code", "03");
					reMap.put("return_info", "平台提现金额与存管提现金额不符");
					yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
					HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					return;
				}
				//检查交易状态是否已更新
				if("3".equals(withdrawTrace.getStr("status")) || "1".equals(withdrawTrace.getStr("status"))){
					yiStageNotifyLogger.log(Level.INFO, String.format("用户：[%s],流水号为[%s]的提现状态为成功或已审核", userCode, withdrawCode));
					reMap.put("return_code", "04");
					reMap.put("return_info", "提现状态为成功或已审核");
					yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
					HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					return;
				}
				if(!jxTraceCode.equals(withdrawTrace.getStr("bankTraceCode"))){
					withdrawTrace.set("bankTraceCode", jxTraceCode);
				}
				reMap.put("accountId", jxAccountId);
				reMap.put("mobile", mobile);
				reMap.put("txAmount", resMap.get("txAmount"));
				reMap.put("withdrawCode", withdrawCode);
				if( JXCallbackController.dealRetCode(resMap.get("retCode"))){
					reMap.put("return_code", "0000");
					reMap.put("return_info", "提现成功");
					reMap.put("status", "1");
					withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "1");
					withdrawTrace.set("withdrawRemark", "已审核");
					yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
					HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					if(withdrawTrace.update()){
						//修改资金账户可用余额
						boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
								userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
						if (withdrawals4Fuiou == false) {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_修改资金失败 ", null);
							yiStageNotifyLogger.log(Level.INFO, String.format("[%s]提现成功_修改资金失败", userCode));
							return ;
						} else {//成功
							BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_同步修改资金" + StringUtil.getMoneyYuan(withdrawAmount));
							yiStageNotifyLogger.log(Level.INFO, String.format("[%s]提现成功_同步修改资金", userCode));
						}
					}
					else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_流水更新失败", null);
						yiStageNotifyLogger.log(Level.INFO, String.format("[%s]提现成功_流水更新失败", userCode));
						return ;
					}
					
				
				}else{
					reMap.put("return_code", resMap.get("retCode"));
					reMap.put("return_info", resMap.get("retMsg"));
					reMap.put("status", "4");
					yistageTraceService.updateResponseMessage(jxTraceCode, JSON.toJSONString(reMap));
					HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "4");
					withdrawTrace.set("withdrawRemark", "提现失败:" + resMap.get("retCode") + "_" + resMap.get("retMsg"));
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_" + resMap.get("retCode"), null);
					yiStageNotifyLogger.log(Level.INFO, String.format("[%s]提现失败:[%s],[%s]", userCode, resMap.get("retCode"), resMap.get("retMsg")));
					if (withdrawTrace.update()) {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_更新提现流水");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_流水未更新", null);
					}
				}
				renderText("success");
				
	}
	

	/*************************************
	 * PRIVATE METHOD
	 **************************************/

	/**
	 * 解析jxTraceCode
	 * 
	 * @param response
	 * @return
	 */
	private String getJxTraceCode(Map<String, String> response) {
		if (response == null) {
			return null;
		}
		return response.get("txDate") + response.get("txTime") + response.get("seqNo");
	}

	private Message certificationAuto(String userCode, String trueName, String md5CardId, String idType) {
		// 次数限制
		Long count = Db.queryLong("select count(1) from t_auth_log where userCode = ?", userCode);
		if (count > 3) {
			return error("01", "认证次数超限制", "");
		}

		boolean update = userInfoService.newUserAuth(userCode, trueName, md5CardId, "", "2", idType);
		if (!update) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
			return error("02", "已经认证,请勿重复提交!", "");
		}

		// 记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户自动认证成功");

		return null;
	}
}
