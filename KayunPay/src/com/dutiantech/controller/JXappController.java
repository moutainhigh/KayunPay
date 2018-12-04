package com.dutiantech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FtpUtil;
import com.dutiantech.util.Property;
import com.dutiantech.util.StringUtil;
import com.jx.service.JXService;
import com.jx.util.SignUtil_lj;

public class JXappController extends BaseController {
	private UserInfoService userInfoService = getService(UserInfoService.class);
	/**
	 * WJW 页面开户（跳转至存管开户页面）
	 * 
	 * @param idType
	 *            证件类型 01-身份证18位
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param gender
	 *            性别 M:男性 F:女性
	 * @param acctUse
	 *            账户用途(00000-普通账户,10000-红包账户（只能有一个）,01000-手续费账户（只能有一个）,00100-
	 *            担保账户)
	 * @param mobile
	 *            手机号
	 * @param identity
	 *            身份属性 1:出借角色 2:借款角色 3:代偿角色
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public Message accountOpenPage(String idType, String idNo, String name, String gender, String acctUse,
			String mobile, String identity, HttpServletResponse response, String userCode, String retUrl, String successfulUrl, String notifyUrl) {
		if (idNo == null || idNo.length() != 18) {
			return error("01", "身份证不是18位", null);
		}
		if (mobile == null || mobile.length() != 11) {
			return error("03", "手机号不是11位", null);
		}

		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);// 添加通用参数

		reqMap.put("txCode", "accountOpenPage");
		reqMap.put("idType", idType);// 证件类型 01-身份证18位
		reqMap.put("idNo", idNo);// 证件号码
		reqMap.put("name", name);// 姓名
		reqMap.put("gender", gender);// 性别
		reqMap.put("mobile", mobile);// 手机号
		reqMap.put("acctUse", acctUse);// 00000普通账户
		reqMap.put("identity", identity);// 身份属性
		reqMap.put("coinstName", "易融恒信金融信息服务有限公司");// 平台名称
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("successfulUrl", successfulUrl);//交易成功跳转链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "accountOpenPage", reqMap, response);
		} catch (Exception e) {
		}
		return succ("信息提交成功", "");
	}

	/**
	 * WJW 投资人购买债权(交易金额单位:分)
	 * @param reqMap
	 * 		   	    通用参数
	 * @param accountId
	 *            购买方账号
	 * @param txAmount
	 *            交易金额
	 * @param txFee
	 *            手续费
	 * @param tsfAmount
	 *            转让金额
	 * @param forAccountId
	 *            卖出方账号
	 * @param orgOrderId
	 *            原订单号
	 * @param orgTxAmount
	 *            原交易金额
	 * @param productId
	 *            标号
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public static void creditInvest(Map<String, String> reqMap,String accountId, long txAmount, long txFee, long tsfAmount,
			String forAccountId, String orgOrderId, long orgTxAmount, String productId, HttpServletResponse response) {
		reqMap.put("txCode", "creditInvest");
		reqMap.put("accountId", accountId);// 购买方账号
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("txFee", StringUtil.getMoneyYuan(txFee));// 手续费
		reqMap.put("tsfAmount", StringUtil.getMoneyYuan(tsfAmount));// 转让金额
		reqMap.put("forAccountId", forAccountId);// 卖出方账号
		reqMap.put("orgOrderId", orgOrderId);// 原订单号
		reqMap.put("orgTxAmount", StringUtil.getMoneyYuan(orgTxAmount));// 原交易金额
		reqMap.put("productId", productId);// 标号
		reqMap.put("retUrl", CommonUtil.APPBACK_ADDRESS+"/main");// 返回交易页面链接
		reqMap.put("notifyUrl", CommonUtil.ADDRESS+"/carryOnTransferCallback");// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "creditInvest", reqMap, response);
		} catch (Exception e) {
		}

	}
	/**
	 * 重写方法加入remark
	 * */
	public static void creditInvest(Map<String, String> reqMap,String accountId, long txAmount, long txFee, long tsfAmount,
			String forAccountId, String orgOrderId, long orgTxAmount, String productId,String remark, HttpServletResponse response) {
		reqMap.put("txCode", "creditInvest");
		reqMap.put("accountId", accountId);// 购买方账号
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("txFee", StringUtil.getMoneyYuan(txFee));// 手续费
		reqMap.put("tsfAmount", StringUtil.getMoneyYuan(tsfAmount));// 转让金额
		reqMap.put("forAccountId", forAccountId);// 卖出方账号
		reqMap.put("orgOrderId", orgOrderId);// 原订单号
		reqMap.put("orgTxAmount", StringUtil.getMoneyYuan(orgTxAmount));// 原交易金额
		reqMap.put("productId", productId);// 标号
		reqMap.put("retUrl", CommonUtil.APPBACK_ADDRESS+"/main");// 返回交易页面链接
		reqMap.put("notifyUrl", CommonUtil.CALLBACK_URL+"/carryOnTransferCallback");// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "creditInvest", reqMap, response,remark);
		} catch (Exception e) {
		}

	}
	/**
	 * WJW 红包发放(交易金额单位:分)
	 * 
	 * @param accountId
	 *            红包账号
	 * @param txAmount
	 *            红包金额
	 * @param forAccountId
	 *            接收方账号
	 * @param desLineFlag
	 *            是否使用交易描述(1-使用,0-不使用)
	 * @param desLine
	 *            交易描述
	 * @return
	 */
	public static Map<String, String> voucherPay(String accountId, long txAmount, String forAccountId,
			String desLineFlag, String desLine) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);// 添加通用参数
		reqMap.put("txCode", "voucherPay");// 交易代码
		reqMap.put("accountId", accountId);// 红包账号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 红包金额
		reqMap.put("forAccountId", forAccountId);// 接收方账号
		reqMap.put("desLineFlag", desLineFlag);// 是否使用交易描述(1-使用,0-不使用)
		reqMap.put("desLine", desLine);// 交易描述
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 红包发放撤销(交易金额单位:分)
	 * 
	 * @param accountId
	 *            原交易的红包账号
	 * @param txAmount
	 *            原交易的红包金额
	 * @param forAccountId
	 *            原交易的接收方账号
	 * @param orgTxDate
	 *            原交易日期
	 * @param orgTxTime
	 *            原交易时间
	 * @param orgSeqNo
	 *            原交易流水号
	 * @return
	 */
	public static Map<String, String> voucherPayCancel(String accountId, long txAmount, String forAccountId,
			String orgTxDate, String orgTxTime, String orgSeqNo) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);// 添加通用参数
		reqMap.put("txCode", "voucherPayCancel");
		reqMap.put("accountId", accountId);
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));
		reqMap.put("forAccountId", forAccountId);
		reqMap.put("orgTxDate", orgTxDate);
		reqMap.put("orgTxTime", orgTxTime);
		reqMap.put("orgSeqNo", orgSeqNo);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 红包发放隔日撤销(交易金额单位:分)
	 * 
	 * @param accountId
	 *            原交易的红包账号
	 * @param txAmount
	 *            原交易的红包金额
	 * @param forAccountId
	 *            原交易的接收方账号
	 * @param orgTxDate
	 *            原交易日期
	 * @param orgTxTime
	 *            原交易时间
	 * @param orgSeqNo
	 *            原交易流水号
	 * @param desLineFlag
	 *            是否交易描述(0：不使用 1：使用，为空或不传默认不使用)
	 * @param desLine
	 *            交易描述
	 * @return
	 */
	public static Map<String, String> voucherPayDelayCancel(String accountId, long txAmount, String forAccountId,
			String orgTxDate, String orgTxTime, String orgSeqNo, String desLineFlag, String desLine) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);// 添加通用参数
		reqMap.put("txCode", "voucherPayDelayCancel");
		reqMap.put("accountId", accountId);
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));
		reqMap.put("forAccountId", forAccountId);
		reqMap.put("orgTxDate", orgTxDate);
		reqMap.put("orgTxTime", orgTxTime);
		reqMap.put("orgSeqNo", orgSeqNo);
		reqMap.put("desLineFlag", desLineFlag);
		reqMap.put("desLine", desLine);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次放款(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param jsonStr
	 *            accountId:投资人电子账号,orderId:订单号,txAmount:单笔流水交易金额,
	 *            forAccountId:融资人账号,productId:标号,authCode:授权码
	 * @return
	 */
	public static Map<String, String> batchLendPay(int batchNo, long txAmount, String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);// 添加通用参数
		reqMap.put("txCode", "batchLendPay");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"/batchLendPayNotifyURL");// 合法性校验通知链接
		reqMap.put("retNotifyURL", CommonUtil.NIUX_URL+"/doLoanTraceBatchCallback");// 业务结果通知

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);

			Map<String, String> map = new HashMap<>();
			map.put("accountId", jsonObject.getString("accountId"));// 投资人电子账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(jsonObject.getLong("txAmount")));// 单笔流水交易金额
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 融资人账号
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("authCode", jsonObject.getString("authCode"));// 投资人投资成功的授权号
			arrayList.add(map);
		}

		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次还款(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param retNotifyURL
	 * 			  业务通知结果
	 * @param jsonStr
	 *            accountId:融资人电子账号,txAmount:交易金额,intAmount:交易利息,
	 *            txFeeOut:还款手续费(可选),txFeeIn:收款手续费(可选),forAccountId:投资人账号,productId:标号,
	 *            authCode:授权码
	 * @return
	 */
	public static Map<String, String> batchRepay(int batchNo, long txAmount,String retNotifyURL, String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchRepay");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"/notifyURL");// 后台通知链接
		reqMap.put("retNotifyURL", retNotifyURL);// 业务结果通知

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);

			Map<String, String> map = new HashMap<>();

			map.put("accountId", jsonObject.getString("accountId"));// 融资人电子账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(jsonObject.getLong("txAmount")));// 还款本金
			map.put("intAmount", StringUtil.getMoneyYuan(jsonObject.getLong("intAmount")));// 交易利息
			if(jsonObject.getLong("txFeeOut") != null){
				map.put("txFeeOut", StringUtil.getMoneyYuan(jsonObject.getLong("txFeeOut")));// 还款手续费
			}
			if(jsonObject.getLong("txFeeIn") != null){
				map.put("txFeeIn", StringUtil.getMoneyYuan(jsonObject.getLong("txFeeIn")));// 收款手续费
			}
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 投资人账号
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("authCode", jsonObject.getString("authCode"));// 授权码
			arrayList.add(map);
		}

		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次结束债权(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param jsonStr
	 *            accountId:融资人电子账号,forAccountId:投资人账号,
	 *            productId:标号,authCode:授权码
	 * @return
	 */
	public static Map<String, String> batchCreditEnd(int batchNo, String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchCreditEnd");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"/notifyURL");// 后台通知链接
		reqMap.put("retNotifyURL", CommonUtil.NIUX_URL+"/batchCreditEndCallback");// 业务结束通知

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);

			Map<String, String> map = new HashMap<>();
			map.put("accountId", jsonObject.getString("accountId"));// 融资人电子账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 投资人账号
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("authCode", jsonObject.getString("authCode"));// 授权码
			arrayList.add(map);
		}
		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));// 请求数组

		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次撤销(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param txCounts
	 *            交易笔数
	 * @return
	 */
	public static Map<String, String> batchCancel(int batchNo, long txAmount, int txCounts) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchCancel");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("txCounts", String.valueOf(txCounts));// 交易笔数
		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次投资人购买债权(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param jsonStr
	 *            accountId:买入方账号,txAmount:交易金额,txFee:手续费,tsfAmount:卖出的债权金额,
	 *            forAccountId:卖出方账号,orgOrderId:卖出方投标的原订单号（或卖出方购买债权的原订单号）,
	 *            orgTxAmount:卖出方投标的原交易金额 （或卖出方购买债权的原转让金额） ,productId:标号,
	 *            contOrderId:签约订单号(30)
	 * @return
	 */
	public static Map<String, String> batchCreditInvest(int batchNo, long txAmount, String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchCreditInvest");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"");// 后台通知链接
		reqMap.put("retNotifyURL", CommonUtil.NIUX_URL+"");// 业务结果通知

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			Map<String, String> map = new HashMap<>();

			map.put("accountId", jsonObject.getString("accountId"));// 买入方账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(jsonObject.getLong("txAmount")));// 交易金额
			map.put("txFee", StringUtil.getMoneyYuan(jsonObject.getLong("txFee")));// 手续费
			map.put("tsfAmount", StringUtil.getMoneyYuan(jsonObject.getLong("tsfAmount")));// 卖出的债权金额
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 卖出方账号
			map.put("orgOrderId", jsonObject.getString("orgOrderId"));// 卖出方投标的原订单号（或卖出方购买债权的原订单号）
			map.put("orgTxAmount", StringUtil.getMoneyYuan(jsonObject.getLong("orgTxAmount")));// 原交易金额
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("contOrderId", jsonObject.getString("contOrderId"));// 买入方自动债权转让签约订单号
			arrayList.add(map);
		}

		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));// 请求数组

		return JXService.requestCommon(reqMap);

	}

	/**
	 * WJW 批次发红包(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param jsonStr
	 *            txAmount:单笔交易金额,forAccountId:转入方电子账号,voucherType:红包类型, name:持卡人姓名
	 * @return
	 */
	public static Map<String, String> batchVoucherPay(int batchNo, long txAmount, String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchVoucherPay");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"");// 后天通知链接
		reqMap.put("retNotifyURL", CommonUtil.NIUX_URL+"");// 业务结果通知

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			Map<String, String> map = new HashMap<>();

			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(jsonObject.getLong("txAmount")));// 交易金额
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 转入方电子账号
			map.put("voucherType", jsonObject.getString("voucherType"));// 红包类型
			map.put("name", jsonObject.getString("name"));// 持卡人姓名
			arrayList.add(map);
		}

		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));// 请求数组

		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 批次代偿(交易金额单位:分)
	 * 
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param retNotifyURL
	 * 			  业务通知结果地址
	 * @param jsonStr
	 *            accountId:风险准备金账号,txAmount:代偿本金,intAmount:交易利息,
	 *            txFeeIn:收款手续费,fineAmount:罚息金额,
	 *            forAccountId:投资人账号,productId:标号,authCode:授权码
	 * @return
	 */
	public static Map<String, String> batchSubstRepay(int batchNo, long txAmount, String retNotifyURL,String jsonStr) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchSubstRepay");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 交易金额
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"/notifyURL");// 后台通知链接
		reqMap.put("retNotifyURL", retNotifyURL);// 业务通知结果

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			Map<String, String> map = new HashMap<>();

			map.put("accountId", jsonObject.getString("accountId"));// 风险准备金账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(jsonObject.getLong("txAmount")));// 代偿本金
			map.put("intAmount", StringUtil.getMoneyYuan(jsonObject.getLong("intAmount")));// 交易利息
			if(jsonObject.getLong("txFeeIn") != null){
				map.put("txFeeIn", StringUtil.getMoneyYuan(jsonObject.getLong("txFeeIn")));// 收款手续费
			}
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 投资人账号
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("authCode", jsonObject.getString("authCode"));// 授权码
			arrayList.add(map);
		}

		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));// 请求数组

		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 缴费授权(交易金额单位:元)(页面调用)
	 * 
	 * @param accountId
	 *            电子账号
	 * @param maxAmt
	 *            签约最大金额
	 * @param deadline
	 *            签约到期日
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public static void paymentAuthPage(String accountId, String maxAmt, String deadline, HttpServletResponse response) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "paymentAuthPage");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("maxAmt", maxAmt);// 签约最大金额
		reqMap.put("deadline", deadline);// 签约到期日
		reqMap.put("retUrl", CommonUtil.APPBACK_ADDRESS+"/userinfo");// 返回交易页面链接
		reqMap.put("successfulUrl", "");
		reqMap.put("notifyUrl", CommonUtil.CALLBACK_URL + "/paymentAuthPageCallback");// 后台响应链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "paymentAuthPage", reqMap, response);
		} catch (Exception e) {
		}
	}

	/**
	 * WJW 还款授权(交易金额单位:元)(页面调用)
	 * 
	 * @param accountId
	 *            电子账号
	 * @param maxAmt
	 *            签约最大金额
	 * @param deadline
	 *            签约到期日
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public static void repayAuthPage(String accountId, String maxAmt, String deadline, HttpServletResponse response) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "repayAuthPage");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("maxAmt", maxAmt);// 签约最大金额
		reqMap.put("deadline", deadline);// 签约到期日
		reqMap.put("retUrl", CommonUtil.NIUX_URL+"/C01");// 返回交易页面链接
		reqMap.put("notifyUrl", CommonUtil.NIUX_URL+"/repayAuthPageCallback");// 后台响应链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "repayAuthPage", reqMap, response);
		} catch (Exception e) {
		}
	}

	/**
	 * WJW 产品还款授权解约(交易金额单位:元)
	 * 
	 * @param accountId
	 *            电子账号
	 * @param txType
	 *            业务类别(2-解约)
	 * @param maxAmt
	 *            签约最大金额
	 * @return
	 */
	public static Map<String, String> repayAuthCancel(String accountId, int txType, String maxAmt) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "repayAuthCancel");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("orderId", CommonUtil.genShortUID());// 交易流水号
		reqMap.put("txType", String.valueOf(txType));// 业务类别
		reqMap.put("maxAmt", maxAmt);// 签约最大金额

		return JXService.requestCommon(reqMap);
	}

	/**
	 * WJW 产品缴费授权解约(交易金额单位:元)
	 * 
	 * @param accountId
	 *            电子账号
	 * @param txType
	 *            业务类别(2-解约)
	 * @param maxAmt
	 *            签约最大金额
	 * @return
	 */
	public static Map<String, String> paymentAuthCancel(String accountId, int txType, String maxAmt) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "paymentAuthCancel");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txType", String.valueOf(txType));// 业务类别
		reqMap.put("maxAmt", maxAmt);// 签约最大金额

		return JXService.requestCommon(reqMap);
	}

	/**
	 * 快捷充值页面 TZQ
	 * 
	 * @param jxAccountId
	 *            存管账号
	 * @param bankNo
	 *            银行卡号
	 * @param userCardName
	 *            用户姓名
	 * @param userMobile
	 *            手机号
	 * @param idType
	 *            证件类型：01-身份证 20-组织机构代码 25-社会信用代码
	 * @param userCardId
	 *            证件号
	 * @param txAmount
	 *            充值金额(单位：元 精确到两位小数)
	 * @param response
	 * @return
	 */
	public Message directRechargePage(String jxAccountId, String bankNo, String userCardName, String userMobile,
			String idType, String userCardId, String txAmount, String forgotPwdUrl, String retUrl, String notifyUrl, String successfulUrl, HttpServletResponse response) {

		// 组织充值请求参数
		Map<String, String> reqMap = new TreeMap<String, String>();
		// 获取通用请求参数
		JXService.getHeadReq4App(reqMap);
		try {
			reqMap.put("txCode", "directRechargePage");// 交易代码
			reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
			reqMap.put("cardNo", StringUtil.isBlank(bankNo) ? "" : bankNo);
			reqMap.put("currency", "156");// 币种：默认156-人民币
			reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
			reqMap.put("idType", idType);

			reqMap.put("idNo", StringUtil.isBlank(userCardId) ? "" : userCardId);
			reqMap.put("mobile", StringUtil.isBlank(userMobile) ? "" : userMobile);
			reqMap.put("name", StringUtil.isBlank(userCardName) ? "" : userCardName);
			// 忘记密码跳转
			reqMap.put("forgotPwdUrl", forgotPwdUrl);
			// 交易成功跳转到指定页面
			reqMap.put("retUrl", retUrl);
			// 后台接收通知链接
			reqMap.put("notifyUrl", notifyUrl);
			reqMap.put("successfulUrl", successfulUrl);
		} catch (Exception e1) {
			return error("03", "充值参数异常," + e1.getMessage(), null);
		}

		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "" + reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo"));
	}

	/**
	 * 提现页面 TZQ
	 * 
	 * @param jxAccountId
	 *            存管账号
	 * @param userCardName
	 *            用户姓名
	 * @param bankNo
	 *            银行卡号
	 * @param idType
	 *            证件类型 01-身份证
	 * @param userCardId
	 *            证件号
	 * @param userMobile
	 *            手机号
	 * @param txAmount
	 *            提现金额(单位：元 精确到两位小数)
	 * @param txFee
	 *            手续费
	 * @param routeCode
	 *            路由代码 空：自动选择 2：人行大额通道
	 * @param cardBankCnaps
	 *            绑定银行联行号(routeCode == 2时必填)
	 * @param response
	 * @return
	 */
	public Message withdrawPage(String jxAccountId, String userCardName, String bankNo, String idType,
			String userCardId, String userMobile, String txAmount, String txFee, String routeCode, String cardBankCnaps,
			String retUrl, String notifyUrl, String forgotPwdUrl, String successfulUrl, HttpServletResponse response,String businessAccountIdFlag) {

		// 组织提现请求参数
		Map<String, String> reqMap = new TreeMap<>();
		try {
			reqMap.put("txCode", "withdraw");
			reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
			reqMap.put("cardNo", StringUtil.isBlank(bankNo) ? "" : bankNo);
			// 手续费
			reqMap.put("txFee", StringUtil.isBlank(txFee) ? "" : txFee);
			reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
			reqMap.put("idType", StringUtil.isBlank(idType) ? "" : idType);
			reqMap.put("businessAccountIdFlag", businessAccountIdFlag);
			reqMap.put("idNo", StringUtil.isBlank(userCardId) ? "" : userCardId);
			reqMap.put("mobile", StringUtil.isBlank(userMobile) ? "" : userMobile);
			reqMap.put("name", StringUtil.isBlank(userCardName) ? "" : userCardName);

			if ("2".equals(routeCode)) {// 大额提现走人行大额通道
				reqMap.put("routeCode", StringUtil.isBlank(routeCode) ? "" : routeCode);
				reqMap.put("cardBankCnaps", StringUtil.isBlank(cardBankCnaps) ? "" : cardBankCnaps);
			}
			// 忘记密码跳转
			reqMap.put("forgotPwdUrl", forgotPwdUrl);
			reqMap.put("retUrl", retUrl);
			reqMap.put("notifyUrl", notifyUrl);
			reqMap.put("successfulUrl", successfulUrl);
		} catch (Exception e1) {
			return error("03", "申请提现失败，参数异常", null);
		}
		// 获取通用请求参数
		JXService.getHeadReq4App(reqMap);
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "" + reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo"));
	}

	/**
	 * 还款申请资金冻结 TZQ
	 * 
	 * @param jxAccountId
	 *            存管电子账号
	 * @param txAmount
	 *            冻结金额(单位：元 精确到两位小数)
	 * @param productId
	 *            待还款的标的号
	 * @param frzType
	 *            冻结业务类别 0-还款冻结
	 * @param orderId
	 *            订单号
	 */
	public static Map<String, String> repayBalanceFreeze(String jxAccountId, String txAmount, String productId, String frzType,
			String orderId) {
		// 组织请求参数
		Map<String, String> reqMap = new TreeMap<>();
		// 交易代码
		reqMap.put("txCode", "balanceFreeze");
		reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
		// 订单号
		reqMap.put("orderId", StringUtil.isBlank(orderId) ? "" : orderId);
		reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
		reqMap.put("productId", StringUtil.isBlank(productId) ? "" : productId);
		reqMap.put("frzType", StringUtil.isBlank(frzType) ? "" : frzType);
		// 获取通用请求参数
		JXService.getHeadReq(reqMap);
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		Map<String, String> responseData = JXService.requestCommon(reqMap);

		return responseData;
	}

	/**
	 * 申请撤消资金解冻 TZQ
	 * 
	 * @param jxAccountId
	 *            存管账号
	 * @param txAmount
	 *            原冻结金额(单位：元 精确到两位小数)
	 * @param orgOrderId
	 *            原冻结订单号
	 * @param productId
	 *            待还款的标的号
	 * @param orderId
	 *            订单号
	 * @return
	 */
	public static Map<String, String> balanceUnfreeze(String jxAccountId, String txAmount, String orgOrderId, String orderId,
			String productId) {
		Map<String, String> reqMap = new TreeMap<>();
		// 交易代码
		reqMap.put("txCode", "balanceUnfreeze");
		reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
		// 订单号
		reqMap.put("orderId", StringUtil.isBlank(orderId) ? "" : orderId);
		reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
		reqMap.put("orgOrderId", StringUtil.isBlank(orgOrderId) ? "" : orgOrderId);
		reqMap.put("productId", StringUtil.isBlank(productId) ? "" : productId);

		// 获取通用请求参数
		JXService.getHeadReq(reqMap);
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		Map<String, String> responseData = JXService.requestCommon(reqMap);
		return responseData;
	}

	/**
	 * TZQ 代偿冻结：担保户代偿前，先调用代偿冻结接口；冻结时要校验担保户的电子账户密码 代偿冻结的金额只能在批次代偿接口进行使用
	 * 
	 * @param jxAccountId
	 *            存管账号
	 * @param txAmount
	 *            冻结金额(单位：元 精确到两位小数)
	 * @param productId
	 *            标的编号(可为空)
	 * @param response
	 * @return
	 */
	public Message refinanceFreezePage(String jxAccountId, String txAmount, String productId,
			HttpServletResponse response) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "refinanceFreezePage");
		reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
		// 订单号必须为 txDate+txTime+seqNo
		reqMap.put("orderId", reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo"));
		reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
		reqMap.put("productId", StringUtil.isBlank(productId) ? "" : productId);
		// 忘记密码中转链接--重置密码
		reqMap.put("forgotPwdUrl", "http://www.yrhx.com");
		// 用于跳转原交易页面
		reqMap.put("retUrl", "http://59.172.250.254:9523/login");
		// 后台响应链接
		reqMap.put("notifyUrl", "http://59.172.250.254:9523/refinanceFreezePageCallback");

		// 生成待签名字符串并签名
		String mergeMap = JXService.mergeMap(reqMap);
		String sign = SignUtil_lj.sign(mergeMap);

		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "代偿冻结请求成功");
	}

	/**
	 * rain 20180412 发送短信验证码
	 * 
	 * @param mobile
	 *            手机号
	 * @param reqType
	 *            请求类型 不填默认为1*** 1- 即信发短信 2- 银行发短信
	 * @param srvTxCode
	 *            业务交易代码
	 * @param cardNo
	 *            银行卡号 reqType=2时必填
	 * @param smsType
	 *            验证码类型 smsType=1是即信短信文本验证码 smsType=2是即信短信语音验证码
	 * @return
	 */
	public static Map<String, String> smsCodeApply(String mobile, String reqType, String srvTxCode, String cardNo,
			String smsType) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "smsCodeApply");// 交易代码
		reqMap.put("mobile", mobile);
		reqMap.put("reqType", reqType);
		reqMap.put("srvTxCode", srvTxCode);
		reqMap.put("cardNo", cardNo);
		reqMap.put("smsType", smsType);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * 20180831起接口 失效
	 * rain 20180412 设置电子账户的密码，必须是首次设置密码(页面接口)
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param idType
	 *            证件类型 01-身份证（18位）20-组织机构代码 25-企业社会信用代码
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param mobile
	 *            手机号
	 */
	public static void passwordset(String jxAccountId, String idType, String idNo, String name, String mobile, 
			String successfulUrl, HttpServletResponse response, String retUrl, String notifyUrl) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "passwordSet");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("idType", idType);
		reqMap.put("idNo", idNo);
		reqMap.put("name", name);
		reqMap.put("mobile", mobile);
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("successfulUrl", successfulUrl);
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "passwordset", reqMap, response);
	}

	/**
	 * 20180831起接口 失效
	 * rain 20180413 密码重置增强(页面接口) 必须曾经设置过电子账户密码，验证短信验证码密码重置之前要先调用发短信接口。
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param idType
	 *            证件类型 01-身份证（18位）20-组织机构代码 25-企业社会信用代码
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param mobile
	 *            手机号
	 * @param lastSrvAuthCode
	 *            前导业务授权码(通过请求发送短信验证码接口获取)
	 * @param smsCode
	 *            (短信验证码)
	 * @param retUrl
	 * 			  返回交易页面链接
	 * @param notifyUrl
	 * 			  后台通知链接
	 * @param successfulUrl
	 * 			 交易成功跳转链接(可选)
	 */
	public static void passwordResetPlus(String jxAccountId, String idType, String idNo, String name, String mobile,
			String lastSrvAuthCode, String smsCode, String retUrl, String notifyUrl, String successfulUrl, HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "passwordResetPlus");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("idType", idType);
		reqMap.put("idNo", idNo);
		reqMap.put("name", name);
		reqMap.put("mobile", mobile);
		reqMap.put("lastSrvAuthCode", lastSrvAuthCode);
		reqMap.put("smsCode", smsCode);
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接
		reqMap.put("successfulUrl", successfulUrl);//交易成功跳转链接
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "mobile/plus", reqMap, response);
	}

	/**
	 * 绑卡 rain 20180413(老接口)
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param idType
	 *            证件类型 01-身份证（18位）20-组织机构代码 25-企业社会信用代码
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param mobile
	 *            手机号
	 * @param cardNo
	 *            银行卡号
	 * @param lastSrvAuthCode
	 *            前导业务授权码(通过请求发送短信验证码接口获取)
	 * @param smsCode
	 *            (短信验证码)
	 * @return
	 */
	public static Map<String, String> cardBindPlus(String jxAccountId, String idType, String idNo, String name, String mobile,
			String cardNo, String lastSrvAuthCode, String smsCode) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "cardBindPlus");// 交易代码
		reqMap.put("accountId", jxAccountId);
		reqMap.put("idType", idType);
		reqMap.put("idNo", idNo);
		reqMap.put("name", name);
		reqMap.put("mobile", mobile);
		reqMap.put("cardNo", cardNo);
		reqMap.put("lastSrvAuthCode", lastSrvAuthCode);
		reqMap.put("smsCode", smsCode);
		return JXService.requestCommon(reqMap);
	}
	/**
	 * 新绑卡接口(页面)
	 * rain
	 * 20180420
	 * @param idType 证件类型 01-身份证（18位）
	 * @param idNo 证件号码
	 * @param name  姓名
	 * @param jxAccountId  电子账号
	 * @param userIP 客户IP
	 * @param retUrl 前台跳转链接
	 * @param notifyUrl 后台通知链接
	 * @param response
	 */
	public static void bindCardPage(String idType,String idNo,String name,String jxAccountId,String userIP,String retUrl,String notifyUrl,String successfulUrl,HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "bindCardPage");
		reqMap.put("idType", idType);// 订单号
		reqMap.put("idNo", idNo);
		reqMap.put("name", name);
		reqMap.put("accountId", jxAccountId);
		reqMap.put("userIP", userIP);
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接
		reqMap.put("successfulUrl", successfulUrl);
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "bindCardPage", reqMap, response);
	}
	/**
	 * 已失效
	 * 解绑银行卡 rain 20180413
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param idType
	 *            证件类型 01-身份证（18位）20-组织机构代码 25-企业社会信用代码
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param mobile
	 *            手机号
	 * @param cardNo
	 *            银行卡号
	 * @return
	 */
	public static Map<String, String> cardUnbind(String jxAccountId, String idType, String idNo, String name, String mobile,
			String cardNo) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "cardUnbind");// 交易代码
		reqMap.put("accountId", jxAccountId);
		reqMap.put("idType", idType);
		reqMap.put("idNo", idNo);
		reqMap.put("name", name);
		reqMap.put("mobile", mobile);
		reqMap.put("cardNo", cardNo);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180413 电子账户手机号修改增强
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param mobile
	 *            新手机号
	 * @param lastSrvAuthCode
	 *            前导业务授权码
	 * @param smsCode
	 *            短信验证码
	 * @return
	 */
	public static Map<String, String> mobileModifyPlus(String jxAccountId, String mobile, String lastSrvAuthCode,
			String smsCode) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "mobileModifyPlus");// 交易代码
		reqMap.put("accountId", jxAccountId);
		reqMap.put("option", "1");
		reqMap.put("mobile", mobile);
		reqMap.put("lastSrvAuthCode", lastSrvAuthCode);
		reqMap.put("smsCode", smsCode);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180411 借款人标的登记
	 * 
	 * @param jxAccountId
	 *            借款人电子账户
	 * @param productId
	 *            标的号
	 * @param raiseDate
	 *            募集日期
	 * @param raiseEndDate
	 *            募集结束日期
	 * @param intType
	 *            付息方式 0-到期与本金一起归还1-每月固定日期支付2-每月不确定日期支付
	 * @param intPayDay
	 *            付息方式为1时必填；
	 * @param duration
	 *            借款期限
	 * @param txAmount
	 *            交易金额
	 * @param rate
	 *            年化利率
	 * @param bailAccountId
	 *            担保人电子账号(可选)
	 * @return
	 */
	public static  Map<String, String> debtRegister(String jxAccountId, String productId, String raiseDate,
			String raiseEndDate, String intType, String intPayDay, String duration, String txAmount, String rate,
			String bailAccountId) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "debtRegister");// 交易代码
		reqMap.put("accountId", jxAccountId);
		reqMap.put("productId", productId);
		reqMap.put("productDesc", "是一个标");// 标的描述
		reqMap.put("raiseDate", raiseDate);
		reqMap.put("raiseEndDate", raiseEndDate);
		reqMap.put("intType", intType);
		reqMap.put("intPayDay", intPayDay);
		reqMap.put("duration", duration);
		reqMap.put("txAmount", txAmount);
		reqMap.put("rate", rate);
		reqMap.put("bailAccountId", bailAccountId);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180411 借款人标的撤销
	 * 
	 * @param jxAccountId
	 *            借款人电子账户
	 * @param loanCode
	 *            标的号
	 * @param releaseDate
	 *            募集日期
	 * @return
	 */
	public static Map<String, String> debtRegisterCancel(String jxAccountId, String loanCode, String releaseDate) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "debtRegisterCancel");// 交易代码
		reqMap.put("accountId", jxAccountId);
		reqMap.put("productId", loanCode);
		reqMap.put("raiseDate", releaseDate);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180411 投资人投标申请
	 * 
	 * @param jxAccountId
	 *            出借人电子账户
	 * @param txAmount
	 *            交易金额
	 * @param productId
	 *            标的号
	 * @param frzFlag
	 *            是否冻结金额0-不冻结1-冻结
	 * @param bonusFlag
	 *            是否使用红包
	 * @param bonusAmount
	 *            红包抵扣金额
	 * @param response
	 */
	public static void bidApply4App(String jxAccountId, String orderId, String txAmount, String productId, String frzFlag,
			String bonusFlag, String bonusAmount,String forgotPwdUrl,String retUrl,String notifyUrl,String remark, HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "bidApply");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("orderId", orderId);// 订单号
		reqMap.put("txAmount", txAmount);
		reqMap.put("productId", productId);
		reqMap.put("frzFlag", frzFlag);
		reqMap.put("bonusFlag", bonusFlag);
		reqMap.put("bonusAmount", bonusAmount);
		reqMap.put("forgotPwdUrl",forgotPwdUrl);
		reqMap.put("retUrl",retUrl);// 返回交易页面链接
		reqMap.put("notifyUrl",notifyUrl);// 后台通知链接
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "bidapply", reqMap, response,remark);
	}

	/**
	 * rain 20180411 投标申请撤销
	 * 
	 * @param jxAccountId
	 *            出借人电子账户
	 * @param txAmount
	 *            投标金额
	 * @param productId
	 *            标的号
	 * @param orgOrderId
	 *            原投标的订单号
	 * @param response
	 * @return
	 */
	public static Map<String, String> bidCancel(String jxAccountId, String txAmount, String productId, String orgOrderId,
			HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "bidCancel");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txAmount", txAmount);
		reqMap.put("productId", productId);
		reqMap.put("orgOrderId", orgOrderId);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180411 自动投标签约（页面）
	 * 
	 * @param jxAccountId
	 *            出借人电子账户
	 * @param txAmount
	 *            交易金额(单笔投标金额的上限)
	 * @param totAmount
	 *            总交易金额(自动投标总金额上限（不算已还金额))
	 * @param deadline
	 *            签约到期日期
	 * @param lastSrvAuthCode
	 *            前导业务授权码 通过请求发送短信验证码接口获取
	 * @param smsCode
	 *            短信验证码
	 *  @param reMark
	 *            备注  （暂存设置信息） 
	 * @param response
	 */
	public static void autoBidAuthPlus(String jxAccountId, String txAmount, String totAmount, String deadline,
			String lastSrvAuthCode, String smsCode,String reMark, HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "autoBidAuthPlus");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txAmount", txAmount);
		reqMap.put("totAmount", totAmount);
		reqMap.put("deadline", deadline);
		reqMap.put("lastSrvAuthCode", lastSrvAuthCode);
		reqMap.put("smsCode", smsCode);
		reqMap.put("retUrl", CommonUtil.APPBACK_ADDRESS+"/autobid");// 返回交易页面链接
		reqMap.put("notifyUrl", CommonUtil.CALLBACK_URL+"/autoBidResponse");// 后台通知链接
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "mobile/plus", reqMap, response,reMark);
	}

	/**
	 * 20180930日起接口失效
	 * 撤销自动投标签约 接口调用
	 * 
	 * @param jxAccountId
	 *            出借人电子账户
	 * @param txAmount
	 *            投标金额
	 * @param productId
	 *            标的号
	 * @param orgOrderId
	 *            原投标的订单号
	 * @param response
	 * @return
	 */
	@Deprecated
	public static Map<String, String> autoBidAuthCancel(String jxAccountId, String orgOrderId, HttpServletResponse response) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "autoBidAuthCancel");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("orgOrderId", orgOrderId);
		return JXService.requestCommon(reqMap);
	}

	/**
	 * rain 20180412 自动投标申请 接口调用
	 * 
	 * @param jxAccountId
	 *            电子账号
	 * @param txAmount
	 *            投标金额
	 * @param productId
	 *            标的号
	 * @param frzFlag
	 *            是否冻结金额 0-不冻结1-冻结为避免放款失败，应填1
	 * @param contOrderId
	 *            自动投标签约订单号
	 * @return
	 */
	public static  Map<String, String> bidAutoApply(String jxAccountId, String txAmount, String productId, String frzFlag,
			String contOrderId) {
		Map<String, String> reqMap = new HashMap<String, String>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "bidAutoApply");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("orderId", CommonUtil.genShortUID());// 订单号
		reqMap.put("txAmount", txAmount);
		reqMap.put("productId", productId);
		reqMap.put("frzFlag", frzFlag);
		reqMap.put("contOrderId", contOrderId);
		return JXService.requestCommon(reqMap);
	}
	/**
	 * app同步江西银行有效银行卡	TZQ
	 * @param user
	 */
	public void appSynBankCard(User user){
		String accountId = user.getStr("jxAccountId");
		String userCode = user.getStr("userCode");
		if(StringUtil.isBlank(accountId)){//电子账户为空
			return ;
		}
		String newBankName = "";//银行卡名称
		String cardNo = "";//银行卡号
		String mobile = "";//存管手机号
		String idNo = "";//证件号码
		String trueName = "";//姓名
		String idType = "01";//证件类型：默认身份证
		BanksV2 banksV2 = BanksV2.bankV2Dao.findById(userCode);
		try {
			if(banksV2 == null){//补录信息
				//有电子账号，但理财卡信息为空
				//根据电子账号，查询有效理财卡信息
				Map<String, Object> validCardMap = JXQueryController.cardBindDetailsQuery(accountId, "1");
				if(validCardMap != null && "00000000".equals(validCardMap.get("retCode"))){
					List<Map<String, String>> list = (List<Map<String, String>>)validCardMap.get("subPacks");
					if(list !=null && list.size() > 0){
						Map<String, String> cardMap = list.get(0);
						cardNo = cardMap.get("cardNo");//有效存管卡号
						
						//根据电子账号，查询用户的手机号
						Map<String, String> mobileMap = JXQueryController.mobileMaintainace(accountId, "0", mobile);
						if(mobileMap != null && "00000000".equals(mobileMap.get("retCode"))){
							mobile = mobileMap.get("mobile");
							idNo = mobileMap.get("idNo");
							trueName = mobileMap.get("name");
							idType = mobileMap.get("idType");
							
							//查询所属银行
							String nameOfBank = BankUtil.getNameOfBank(cardNo);
							if(!StringUtil.isBlank(nameOfBank) && nameOfBank.indexOf("·") > -1){
								newBankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
							}
							idNo = CommonUtil.encryptUserCardId(idNo);
							mobile = CommonUtil.encryptUserMobile(mobile);
						}
						//添加理财卡信息
						BanksV2 bankV2 = new BanksV2();
						bankV2.set("userCode", userCode);
						bankV2.set("userName", user.getStr("userName"));
						bankV2.set("trueName", trueName);
						bankV2.set("bankName", newBankName);
						bankV2.set("bankNo", cardNo);
						bankV2.set("bankType", "");
						bankV2.set("cardCity", "");
						bankV2.set("mobile", mobile);
						bankV2.set("createDateTime", DateUtil.getNowDateTime());
						bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
						bankV2.set("isDefault", "1");
						bankV2.set("status", "0");
						bankV2.set("agreeCode", CommonUtil.genMchntSsn());
						bankV2.set("ssn", CommonUtil.genMchntSsn());
						bankV2.set("cardid", idNo);
						if(bankV2.save()){
							UserInfo userInfo = userInfoService.findById(userCode);
							if(userInfo != null){//补录身份信息
								userInfo.set("userCardName", trueName);
								userInfo.set("idType", idType);
								userInfo.set("userCardId", idNo);
								userInfo.set("isAuthed", "2");
								userInfo.update();
							}
						}
					}
				}
			}else{
				String bankNo = banksV2.getStr("bankNo");
				String bankName = banksV2.getStr("bankName");
				Map<String, Object> cardDetail = null;
				cardDetail = JXQueryController.cardBindDetailsQuery(accountId, "1");
				if(cardDetail != null && "00000000".equals(cardDetail.get("retCode"))){
					List<Map<String, String>> list = (List<Map<String,String>>)cardDetail.get("subPacks");
					
					if(list !=null && list.size() > 0){
						Map<String, String> map = list.get(0);
						cardNo = map.get("cardNo");
						
						if(!bankNo.equals(cardNo)){//平台银行卡与存管有效银行卡不一致
							banksV2.set("bankNo", cardNo);
						}
						String nameOfBank = BankUtil.getNameOfBank(cardNo);
						if(!StringUtil.isBlank(nameOfBank)){
							newBankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
						}
						if(!StringUtil.isBlank(newBankName) && !newBankName.equals(bankName)){
							banksV2.set("bankName", newBankName);
						}
						banksV2.update();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 验证绑卡关系
	 * @param jxAccountId
	 * @param state 0:查询所有 1：查询当前有效绑定卡
	 * @return
	 */
	public static String verifyBindCard(String jxAccountId, String state){
		Map<String, Object> map = null;
		try {
			map= JXQueryController.cardBindDetailsQuery(jxAccountId, state);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(map==null){
			return "n";
		}
		List<Map<String, String>> list = (List<Map<String, String>>)map.get("subPacks");
		if(list==null || list.size() < 1){
			return "n";
		}
		return "y";
	}
	
	/**
	 * 验证是否开通缴费授权
	 * @param jxAccountId
	 * @return
	 */
	public static String verifyPaymentAuth(String jxAccountId){
		Map<String, String> termsAuthQuery = null;
		try {
			termsAuthQuery = JXQueryController.termsAuthQuery(jxAccountId);
		} catch (Exception e) {
			System.out.println("授权功能查询失败");
		}
		if(termsAuthQuery == null){
			return "n";
		}
		String auth = termsAuthQuery.get("paymentAuth");
		if("1".equals(auth)){
			return "y";
		}
		return "n";
	}
	/**
	 * 圈存
	 * @param jxAccountId
	 * @param bankNo
	 * @param txAmount
	 * @param idType
	 * @param userCardId
	 * @param userMobile
	 * @param userCardName
	 * @param retUrl
	 * @param forgotPwdUrl
	 * @param successfulUrl
	 * @param notifyUrl
	 * @param response
	 * @return
	 */
	public Message creditForLoadPage(String jxAccountId, String bankNo, String txAmount, String idType, String userCardId, String userMobile, String userCardName, 
			String retUrl, String forgotPwdUrl, String successfulUrl, String notifyUrl, HttpServletResponse response){
		// 组织充值请求参数
		Map<String, String> reqMap = new TreeMap<String, String>();
		// 获取通用请求参数
		JXService.getHeadReq(reqMap);
		try {
			reqMap.put("txCode", "creditForLoadPage");// 交易代码
			reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
			reqMap.put("cardNo", StringUtil.isBlank(bankNo) ? "" : bankNo);
			reqMap.put("currency", "156");// 币种：默认156-人民币
			reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
			reqMap.put("idType", idType);
			
			reqMap.put("idNo", StringUtil.isBlank(userCardId) ? "" : userCardId);
			reqMap.put("mobile", StringUtil.isBlank(userMobile) ? "" : userMobile);
			reqMap.put("name", StringUtil.isBlank(userCardName) ? "" : userCardName);
			// 忘记密码跳转
			reqMap.put("forgotPwdUrl", forgotPwdUrl);
			// 交易成功跳转到指定页面
			reqMap.put("retUrl", retUrl);
			// 后台接收通知链接
			reqMap.put("notifyUrl", notifyUrl);
			reqMap.put("successfulUrl", successfulUrl);
		} catch (Exception e1) {
			return error("03", "充值参数异常," + e1.getMessage(), null);
		}

		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "圈存申请成功");
	}
	
	/**
	 * 圈提
	 * @param jxAccountId
	 * @param bankNo
	 * @param txFee
	 * @param txAmount
	 * @param idType
	 * @param userCardId
	 * @param userMobile
	 * @param userCardName
	 * @param forgotPwdUrl
	 * @param retUrl
	 * @param notifyUrl
	 * @param successfulUrl
	 * @param response
	 * @return
	 */
	public Message creditForUnloadPage(String jxAccountId, String bankNo, String txFee, String txAmount, String idType, String userCardId, String userMobile,
			 String userCardName, String forgotPwdUrl, String retUrl, String notifyUrl, String successfulUrl, HttpServletResponse response){
		// 组织提现请求参数
		Map<String, String> reqMap = new TreeMap<>();
		try {
			reqMap.put("txCode", "creditForUnloadPage");
			reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
			reqMap.put("cardNo", StringUtil.isBlank(bankNo) ? "" : bankNo);
			reqMap.put("currency", "156");
			// 手续费
			reqMap.put("txFee", StringUtil.isBlank(txFee) ? "" : txFee);
			reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
			reqMap.put("idType", StringUtil.isBlank(idType) ? "" : idType);

			reqMap.put("idNo", StringUtil.isBlank(userCardId) ? "" : userCardId);
			reqMap.put("mobile", StringUtil.isBlank(userMobile) ? "" : userMobile);
			reqMap.put("name", StringUtil.isBlank(userCardName) ? "" : userCardName);
			// 忘记密码跳转
			reqMap.put("forgotPwdUrl", forgotPwdUrl);
			reqMap.put("retUrl", retUrl);
			reqMap.put("notifyUrl", notifyUrl);
			reqMap.put("successfulUrl", successfulUrl);
		} catch (Exception e1) {
			return error("03", "申请提现失败，参数异常", null);
		}
		// 获取通用请求参数
		JXService.getHeadReq(reqMap);
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "圈提现成功");
	}
	
	public static boolean isRespSuc(Map<String, String> result) {
		if (null != result.get("retCode") && "00000000".equals(result.get("retCode"))) {
			return true;
		}
		return false;
	}
	
	/**
	 * 江西aleve eve 文件从ftp下载
	 * ftpHost      服务器地址
	 * fTPUserName  用户名
	 * ftpPort      端口号
	 * ftpPassword  密码
	 * 
	 * return  boolean
	 */
	public static Map<String,Boolean> downFileFromJXFtp(String ftpHost,String ftpUserName,int ftpPort,String ftpPassword){
		String bankSerialNo = Property.getPropertyValueByKey("jx", "bankSerialNo");//银行编号
		String productSerialNo = Property.getPropertyValueByKey("jx", "productSerialNo");//产品编号
		String ftpBasePath = "C2P/";//ftp 根目录
		// 文件名：$$$$-ALEVE????-YYYYMMDD   $$$$为银行编号 ：3005,????为产品编号:0140
		// 文件名：$$$$-EVE????-YYYYMMDD   $$$$为银行编号 ：3005,????为产品编号:0140
		String nowDate = DateUtil.getNowDate();
		String fileDate  = DateUtil.delDay(nowDate, 1);//获取前一天的时间
		String dateYear =fileDate.substring(0, 4);
		String dateMonth = fileDate.substring(4,6);
		String dateDay = fileDate.substring(6,8);
		
		Map<String,Boolean> map = new HashMap<String,Boolean>();
		
		String ftpPath = ftpBasePath + dateYear + "/" + dateMonth + "/" + dateDay + "/";//文件在ftp上的目录
		String fileNamejx = bankSerialNo + "-ALEVE" + productSerialNo + "-" + fileDate;//文件名aleve
		String fileNamejxeve = bankSerialNo + "-EVE" + productSerialNo + "-" + fileDate;//文件名eve
		String localPathjx = "//data//" +dateYear+"//"+dateMonth+"//"+ dateDay;//下载存储的目录
//		String localPathjx = "/download/" +dateYear+"/"+dateMonth+"/"+ dateDay;//下载存储的目录
		
		boolean aleve = FtpUtil.downfileTraceFromFtp(fileNamejx, ftpPath, localPathjx,ftpHost,ftpUserName,ftpPort,ftpPassword);
		boolean eve = FtpUtil.downfileTraceFromFtp(fileNamejxeve, ftpPath, localPathjx,ftpHost,ftpUserName,ftpPort,ftpPassword);
		map.put(fileNamejx, aleve);
		map.put(fileNamejxeve,eve);
		return map;
	}
	/**20180822 rain
	 * 密码设置&重置（合规要求）
	 * @param jxAccountId 电子账号
	 * @param idType 证件类型
	 * @param retUrl  结果地址
	 * @param notifyUrl 异步通知地址
	 */
	public static void passwordResetPage(String jxAccountId,String idType,String retUrl,String notifyUrl,HttpServletResponse response){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "passwordResetPage");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("idType", idType);
		reqMap.put("retUrl", retUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "passwordResetPage", reqMap, response);
	}
	
	/**
	 * 20180820 rain
	 * 密码修改
	 * @param jxAccountId 电子账号
	 * @param name  持卡人姓名
	 * @param retUrl 
	 * @param notifyUrl
	 */
	public static void passwordUpdate(String jxAccountId,String name,String retUrl,String notifyUrl,HttpServletResponse response){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "passwordUpdate");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("name", name);
		reqMap.put("retUrl", retUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "passwordUpdate", reqMap, response);
	}
	
	public static void unbindCardPage(String jxAccountId,String name,String idType,String idNo,String cardNo,String mobile,String retUrl,String forgotPwdUrl,String notifyUrl,HttpServletResponse response){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "unbindCardPage");
		reqMap.put("accountId", jxAccountId);
		reqMap.put("name", name);
		reqMap.put("idType", idType);
		reqMap.put("idNo", idNo);
		reqMap.put("cardNo", cardNo);
		reqMap.put("mobile", mobile);
		reqMap.put("retUrl", forgotPwdUrl);
		reqMap.put("retUrl", retUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "unbindCardPage", reqMap, response);
	}
	/**
	 * 加密开户(合规) app
	 * modified 20180907 rain
	 * @param idType 01-身份证（18位）
	 * @param name  姓名
	 * @param gender 性别 M 男   F 女
	 * @param mobile 手机号
	 * @param email 邮箱(可空)
	 * @param acctUse 账户用途   00000 普通账户  10000 红包账户(只能一个) 01000 手续费账户(只能有一个) 00100 担保账户
	 * @param smsFlag  是否需要开通动账短信通知 0：不需要   1或空：需要
	 * @param identity 身份属性   1 出借角色  2  借款角色  3  代偿角色
	 * @param retUrl
	 * @param successfulUrl
	 * @param notifyUrl
	 */
	public static void accountOpenEncryptPage(String idType,String name,String gender,String mobile,String email,String acctUse,String smsFlag,String identity,String retUrl,String successfulUrl,String notifyUrl,HttpServletResponse response){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "accountOpenEncryptPage");
		reqMap.put("idType", idType);
		reqMap.put("name", name);
		reqMap.put("gender", gender);
		reqMap.put("mobile", mobile);
		reqMap.put("email", email);
		reqMap.put("acctUse", acctUse);
		reqMap.put("smsFlag", smsFlag);
		reqMap.put("identity", identity);
		reqMap.put("coinstName","易融恒信金融信息服务有限公司");
		reqMap.put("retUrl", retUrl);
		reqMap.put("successfulUrl", successfulUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		//生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "accountOpenEncryptPage", reqMap, response);
	}
	
	/**
	 * 多合一缴费授权接口
	 * 20180911
	 * @param accountId 电子账号
	 * @param name  姓名
	 * @param idNo  身份证号
	 * @param identity  身份属性角色  1：出借角色  2：借款角色   3：代偿角色
	 * @param autoBid   开通自动投标功能标志  0：取消 1：开通 空：不操作
	 * @param autoCredit 开通自动债转功能标志   0：取消 1：开通 空：不操作
	 * @param paymentAuth 开通缴费授权功能标志 0：取消 1：开通 空：不操作
	 * @param repayAuth  开通还款授权功能标识  0：取消 1：开通 空：不操作
	 * @param autoBidMaxAmt 自动投标签约最高金额  签约时必送以元为单位，最多两位小数
	 * @param autoBidDeadline 自动投标签约到期日  签约时必送 YYYYmmdd
	 * @param autoCreditMaxAmt 自动购买债权签约最高金额
	 * @param autoCreditDeadline 自动购买债权签约到期日
	 * @param paymentMaxAmt  缴费授权签约最高金额
	 * @param paymentDeadline 缴费授权签约到期日
	 * @param repayMaxAmt   还款授权签约最高金额
	 * @param repayDeadline  还款授权签约到期日
	 * @param forgotPwdUrl  忘记密码跳转链接
	 * @param retUrl      返回交易页面链接
	 * @param notifyUrl  后台响应链接
	 * @param response
	 */
	public static void termsAuthPage(String accountId,String name,String idNo,String identity,String autoBid,String autoCredit,String paymentAuth,String repayAuth,String autoBidMaxAmt,String autoBidDeadline,String autoCreditMaxAmt,String autoCreditDeadline,String paymentMaxAmt,String paymentDeadline,String repayMaxAmt,String repayDeadline,String forgotPwdUrl,String retUrl,String notifyUrl,HttpServletResponse response){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txCode", "termsAuthPage");
		reqMap.put("accountId", accountId);
		reqMap.put("name", name);
		reqMap.put("idNo", idNo);
		reqMap.put("identity", identity);
		if(!StringUtil.isBlank(autoBid)){
			reqMap.put("autoBid", autoBid);
			reqMap.put("autoBidMaxAmt", autoBidMaxAmt);
			reqMap.put("autoBidDeadline", autoBidDeadline);
		}
		if(!StringUtil.isBlank(autoCredit)){
			reqMap.put("autoCredit", autoCredit);
			reqMap.put("autoCreditMaxAmt", autoCreditMaxAmt);
			reqMap.put("autoCreditDeadline", autoCreditDeadline);
		}
		if(!StringUtil.isBlank(paymentAuth)){
			reqMap.put("paymentAuth", paymentAuth);
			reqMap.put("paymentMaxAmt", paymentMaxAmt);
			reqMap.put("paymentDeadline", paymentDeadline);
		}
		if(!StringUtil.isBlank(repayAuth)){
			reqMap.put("repayAuth", repayAuth);
			reqMap.put("repayMaxAmt", repayMaxAmt);
			reqMap.put("repayDeadline", repayDeadline);
		}
		reqMap.put("forgotPwdUrl", forgotPwdUrl);
		reqMap.put("retUrl", retUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		//生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "termsAuthPage", reqMap, response);
	}
}
