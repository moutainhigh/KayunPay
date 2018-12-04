package com.dutiantech.controller.branch;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.fuiou.data.RechargeAndWithdrawalRspData;
import com.fuiou.service.FuiouRspParseService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jx.service.JXService;

public class FuiouCallbackController extends BaseController {

	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private UserService userService = getService(UserService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private BanksService banksService = getService(BanksService.class);
	private UserInfoService userInfoService =getService(UserInfoService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private AutoMapSerivce autoMapService = getService(AutoMapSerivce.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	
	private static final Logger fuiouCallbackLogger = Logger.getLogger("callbackLogger");
	
	static{
		LoggerUtil.initLogger("callback", fuiouCallbackLogger);
	}
	

	/**
	 * 提现接收通知---异步
	 */
	@ActionKey("/withdrawalAsync")
	@AuthNum(value = 999)
	public void withdrawalAsync() {		
		try {
			RechargeAndWithdrawalRspData rspData = FuiouRspParseService
					.rechargeAndWithdrawalNotifyParse(getRequest());
			String mchnt_cd = rspData.getMchnt_cd();
			String mchnt_txn_ssn = rspData.getMchnt_txn_ssn();
			String amt = rspData.getAmt();
		
			WithdrawTrace withdrawTrace = withdrawTraceService
					.findById(mchnt_txn_ssn);

			if (withdrawTrace == null) {
				renderHtml(FuiouRspParseService.notifyRspXml("1001", mchnt_cd, mchnt_txn_ssn));
				return ;
			}
			//  如果交易状态已更新，直接返回成功响应
			if ("3".equals(withdrawTrace.getStr("status"))) {
				renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
				return ;
			}
			String isScore = withdrawTrace.getStr("isScore");
			String useFree = withdrawTrace.getStr("useFree");
			long sxf = (long) withdrawTrace.getInt("sxf");			
			long amount = Long.parseLong(amt);
			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
			String userCode = withdrawTrace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现失败_商户号不符", userCode));
				renderHtml(FuiouRspParseService.notifyRspXml("1002", mchnt_cd, mchnt_txn_ssn));
				return ;
			}
			if (amt == null || !(withdrawAmount == amount + sxf )) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_提现金额不符", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现失败_提现金额不符", userCode));
				renderHtml(FuiouRspParseService.notifyRspXml("1003", mchnt_cd, mchnt_txn_ssn));
				return ;
			}
			
			// wzUser为吴总平台账号,此处用于客户提现手续费划拨
			User wzUser = userService.findByMobile(CommonUtil.OUTCUSTNO);
			User user = userService.findById(userCode);
			
			if (sxf > 0) {
				fuiouTraceService.refund(sxf, FuiouTraceType.I, user, wzUser);
			}
			
			// 如果使用会员免费提现次数，记录一次			
			if (useFree.equals("y")) {
				boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
				if (ok) {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功，提现金额 ："+ withdrawAmount / 10.0 / 10.0);
					fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s]提现成功，提现金额[%s]", userCode, withdrawAmount / 100));
				}
			}
			// 扣除提现抵扣积分
			if ("1".equals(isScore) && user.getInt("vipLevel") < 7 && (useFree.equals("y") == false)) {
				fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
				BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]扣除提现抵扣积分", userCode));
			}
			
			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "3");
			withdrawTrace.set("withdrawRemark", "提现成功");
			if (withdrawTrace.update()) {
				fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user, withdrawAmount, FuiouTraceType.B);
			} else {
				renderHtml(FuiouRspParseService.notifyRspXml("1004", mchnt_cd, mchnt_txn_ssn));
				return ;
			}
			
			//  修改资金账户可用余额
			boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
			if (withdrawals4Fuiou == false) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "申请提现失败 ", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]申请提现失败", userCode));
				renderHtml(FuiouRspParseService.notifyRspXml("1005", mchnt_cd, mchnt_txn_ssn));
				return ;
			} else {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现异步修改资金" + withdrawAmount);
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现异步修改资金", userCode));
				renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
				return ;
			}						
				
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 缴费授权回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/paymentAuthPageCallback")
	public void paymentAuthPageCallback() {
		String bgData = getPara("bgData");
		if (StringUtil.isBlank(bgData)) {
			return;
		}
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> mapResp = (Map<String, String>) map;
		String jxTraceCode = getJxTraceCode(mapResp);
//		Map<String, String> mapResp = JSONObject.fromObject(bgData);
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxTraceCode, mapResp,
				JSON.toJSON(mapResp).toString().replace(",", ",\r\n"));
		fuiouCallbackLogger.log(Level.INFO, "[缴费授权回调][" + jxTraceCode + "]用户[" + mapResp.get("accountId") + "]缴费授权处理完成..." + DateUtil.getNowDateTime());
		renderNull();
	}

	/**
	 * 投标回调处理
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/bidApplyCallback")
	public synchronized void bidApplyCallback() {
		HttpServletRequest request = getRequest();
		String parameter = request.getParameter("bgData");
		Map<String, ?> map = JSONObject.parseObject(parameter);
		Map<String, String> resMap = (Map<String, String>) map;
		if (resMap == null) {
			return;
		}
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
		if (!StringUtil.isBlank(jxTrace.getStr("retCode"))) {
			renderText("success");
			fuiouCallbackLogger.log(Level.INFO, "[投标回调]["+jxTraceCode+"]交易已处理..." + DateUtil.getNowDateTime());
			return;
		}
		String remark = "";

		JXService.updateJxTraceResponse(jxTraceCode, resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"),
				"",remark);
		jxTrace = jxTraceService.findById(jxTraceCode);
		remark = jxTrace.getStr("remark");
		String jxAccountId = resMap.get("accountId");
		String productId = resMap.get("productId");
		Funds afterFunds = null;
		boolean bidResult = false;
		User user = userService.findByJXAccountId(jxAccountId);
		if (user == null) {
			return;
		}
		String userCode = user.getStr("userCode");
		JSONObject jsonObject = JSONObject.parseObject(jxTrace.getStr("requestMessage"));
		JSONObject resJsonObject = JSONObject.parseObject(jxTrace.getStr("responseMessage"));
		String orgOrderId = jsonObject.getString("orderId");
		String authCode = resJsonObject.getString("authCode");
		long txAmount = StringUtil.getMoneyCent(jsonObject.getString("txAmount"));
		// double bonusAmount=0;
		// if("1".equals(bonusFlag)){
		// bonusAmount=Double.valueOf(jsonObject.getString("bonusAmount"));
		// }
		List<Tickets> tickets = new ArrayList<Tickets>();
		LoanInfo loanInfo = loanInfoService.findById(productId);
		long loanBalance = loanInfo.getLong("loanBalance");
		List<String> list = new ArrayList<String>();
		int rewardRateByYear=loanInfo.getInt("benefits4new")+loanInfo.getInt("rewardRateByYear");
		if ("00000000".equals(resMap.get("retCode")) && loanBalance >= txAmount) {
			int ticketRate = 0;// 奖励利率
			if (StringUtil.isBlank(remark) == false) {
				JSONArray ja = JSONArray.parseArray(remark);
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jsonObj = ja.getJSONObject(i);
					Tickets ticket = ticketsService.findByCode(jsonObj.getString("code"));
					if (ticket != null) {
						if ("C".equals(jsonObj.getString("type"))) {
							ticketRate += jsonObj.getInteger("rate");
						}
						// 判断当前投标使用的现金券状态
						Tickets orTicket = ticketsService.findByCode(jsonObj.getString("code"));
						if (!"A".equals(orTicket.getStr("tstate"))) {
							JXController.bidCancel(jxAccountId, StringUtil.getMoneyYuan(txAmount), productId, orgOrderId, getResponse());
							fuiouCallbackLogger.log(Level.INFO, "[投标回调][" + jxTraceCode + "]用户["+user.getStr("userName")+"]["+user.getStr("jxAccountId")+"]投标失败...失败原因：奖券["+orTicket.getStr("tCode")+"]重复使用.." + DateUtil.getNowDate());
							return;
						}
						list.add(jsonObj.getString("code"));

					} else {
						ticket = new Tickets();
						ticketRate += jsonObj.getInteger("rate");
						list.add(jsonObj.getString("code"));
					}
					ticket.put("ttype", jsonObj.getString("type"));
					ticket.put("tCode", jsonObj.getString("code"));
					ticket.put("amount", jsonObj.getInteger("amount"));
					ticket.put("rate", jsonObj.getInteger("rate"));
					tickets.add(ticket);
				}
			}
			loanInfo.set("rewardRateByYear", (rewardRateByYear + ticketRate));// 将加息券奖励增加到奖励年利率里
			String loantype = "M";
			if("000001".equals(jxTrace.getStr("responseChannel"))){
				loantype="N";
			}
			
			bidResult = loanInfoService.update4prepareBid(loanInfo, txAmount, userCode, loantype, 0, tickets, 0,
					jxTraceCode, authCode);
			fuiouCallbackLogger.log(Level.INFO, "[投标回调][" + jxTraceCode + "]用户["+user.getStr("userName")+"]["+user.getStr("jxAccountId")+"]投标成功..." + DateUtil.getNowDateTime());
			if (bidResult) {
				afterFunds = fundsServiceV2.avBalance2froze(user.getStr("userCode"), txAmount);
				for (int i = 0; i < list.size(); i++) {
					if ("rewardrateamountDyrhx".equals(list.get(i))) {
						fundsServiceV2.deductRewardRateAmount(userCode, txAmount, 1);
						fuiouCallbackLogger.log(Level.INFO, "[投标回调][" + jxTraceCode + "]用户["+user.getStr("userName")+"]["+user.getStr("jxAccountId")+"]投标成功...扣除加息额度：" + txAmount + "..." + DateUtil.getNowDateTime());
					} else {
						if(!Tickets.rewardRateInterestTcode.equals(list.get(i))){
							ticketsService.useTicket(userCode, list.get(i), txAmount, loanInfo.getStr("loanCode"));
							fuiouCallbackLogger.log(Level.INFO, "[投标回调][" + jxTraceCode + "]用户["+user.getStr("userName")+"]["+user.getStr("jxAccountId")+"]投标成功...使用奖券[" + list.get(i) + "]..." + DateUtil.getNowDateTime());
						}
						
					}
				}
			}
			if (bidResult == true) {
				// //投标成功，记录资金流水
				fundsTraceService.bidTrace(userCode, txAmount, afterFunds.getLong("avBalance"), afterFunds.getLong("frozeBalance"), 0, 0);
				// 手动投标发送短信
				try {
					String mobile = userService.getMobile(userCode);
					String content = CommonUtil.SMS_MSG_MLOAN.replace("[userName]", user.getStr("userName"))
							.replace("[loanNo]", loanInfo.getStr("loanNo"))
							.replace("[payAmount]", StringUtil.getMoneyYuan(txAmount));
					SMSLog smsLog = new SMSLog();
					smsLog.set("mobile", mobile);
					smsLog.set("content", content);
					smsLog.set("userCode", userCode);
					smsLog.set("userName", user.getStr("userName"));
					smsLog.set("type", "12");
					smsLog.set("typeName", "手动投标");
					smsLog.set("status", 9);
					smsLog.set("sendDate", DateUtil.getNowDate());
					smsLog.set("sendDateTime", DateUtil.getNowDateTime());
					smsLog.set("break", "");
					smsLogService.save(smsLog);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		// 回调时判断该标的可投金额是否大于该用户的投资金额
		if (loanBalance < txAmount) {
			// 小于投资金额对刚投的标进行投标撤销
			Map<String, String> reqMap = JXController.bidCancel(jxAccountId, StringUtil.getMoneyYuan(txAmount), productId, orgOrderId, getResponse());
			fuiouCallbackLogger.log(Level.INFO, "[投标回调][" + jxTraceCode + "]用户["+user.getStr("userName")+"]["+user.getStr("jxAccountId")+"]投标失败...失败原因：投标金额小于标的可投金额..." + DateUtil.getNowDateTime());
			if ("00000000".equals(reqMap.get("retCode"))) {
				if (StringUtil.isBlank(remark) == false) {
					JSONArray ja = JSONArray.parseArray(remark);
					for (int i = 0; i < ja.size(); i++) {
						JSONObject jsonObj = ja.getJSONObject(i);
						// 回滚加息额度和代金券
						if (Tickets.rewardRateAomuntTcode.equals(jsonObj.getString("code"))) {
							fundsServiceV2.deductRewardRateAmount(userCode, txAmount, 0);
						} else {
							// 代金券回滚
							ticketsService.rollBackTicket(jsonObj.getString("code"));
						}
					}
				}
			}
		}
		renderText("success");
	}

	/**
	 * 存管开户通知回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/openAccountCallback")
	public void openAccountCallback() {
		// 通知数据
		String bgData = getPara("bgData");
		String userCode = getPara("uCode");
		if (StringUtil.isBlank(bgData)) {// 响应数据为空
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
		String cardNo = "";
		String bankName = "";
		User user = null;
		String idType = "01";//证件类型
		String idNo = "";
		String trueName = "";
		boolean isUpdate = false;
		if (null != map && "00000000".equals(map.get("retCode"))) {// 存管开通成功
			// 存管电子账号
			String accountId = map.get("accountId");

			// 根据存管电子账号查询绑查关系(只查有效卡)
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
			if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

				List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
				if (list != null && list.size() > 0) {

					Map<String, String> cardMap = list.get(0);
					cardNo = cardMap.get("cardNo");// 有效存管卡号
					// 根据电子账号查询用户手机号
					Map<String, String> mobileMap = JXQueryController.mobileMaintainace(accountId, "0", mobile);
					if (null != mobileMap && "00000000".equals(mobileMap.get("retCode"))) {
						mobile = mobileMap.get("mobile");
						idNo = mobileMap.get("idNo");
						trueName = mobileMap.get("name");
						idType = mobileMap.get("idType");
						
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
					}
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
						if (isUpdate) {
							// 银行卡添加成功，获取可用积分
							fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
						} else {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加电子存管账户_本地数据更新失败", null);
							return;
						}
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加银行卡信息_本地数据更新失败", null);
						return;
					}
				}
				Memcached.set("PORTAL_USER_" + userCode, user);
			} else {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "未查找到存管有效绑卡信息", null);
				return;
			}
		} else {// 存管开户失败
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败", bgData);
		}
		renderText("success");
	}
	
	/**
	 * 设置存管交易密码回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/setPwdCallback")
	public void setPwdCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;
		String jxCode = getJxTraceCode(map);	// 获取交易流水号
		// 更新流水响应报文
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		
		if(null != map && "00000000".equals(map.get("retCode"))){
			//记录日志
			fuiouCallbackLogger.log(Level.INFO, String.format("[%s]存管密码设置成功", userCode));
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "存管密码设置成功");
		}else{
			//记录日志
			fuiouCallbackLogger.log(Level.INFO, String.format("[%s]存管密码设置失败：[%s][%s]", userCode, map.get("retCode"), map.get("retMsg")));
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "存管密码设置失败："+map.get("retCode")+","+map.get("retMsg"), null);
		}
		renderText("success");
	}
	
	/**
	 * 重置密码回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/resetPwdCallback")
	public void resetPwdCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) bgMap;
		
		if (map == null) {
			BIZ_LOG_ERROR(userCode,BIZ_TYPE.FINDPWD, "重置密码响应参数为空", null);
			return;
		}
		String jxTraceCode = getJxTraceCode(map);	// 获取交易流水号
		JXService.updateJxTraceResponse(jxTraceCode.trim(), map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		
		if ("00000000".equals(map.get("retCode"))) {
			fuiouCallbackLogger.log(Level.INFO, String.format("[%s]重置密码成功", userCode));
			BIZ_LOG_INFO(userCode,BIZ_TYPE.FINDPWD, "重置密码成功");
		} else {
			fuiouCallbackLogger.log(Level.WARNING, String.format("[%s]重置密码失败：[%s][%s]", userCode, map.get("retCode"), map.get("retMsg")));
			BIZ_LOG_INFO(userCode,BIZ_TYPE.FINDPWD, "重置密码失败：" + map.get("retCode") + "," + map.get("retMsg"));
		}
		renderText("success");
	}
	
	/**
	 * 换/绑卡回调响应
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/depositBindCardCallback")
	public void depositBindCardCallback(){
		String bgData = getPara("bgData");
		Map<String, ?> bgMap = JSONObject.parseObject(bgData);
		Map<String, String> resMap = (Map<String, String>) bgMap;
		if(resMap==null){
			return ;
		}
		String jxTraceCode = getJxTraceCode(resMap);	// 获取交易流水号
		// 更新流水响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		
		String bankNo = "";
		String mobile = "";
		String bankName = "";
		//获取电子账户
		String jxAccountId = resMap.get("accountId");
		User user = userService.findByJXAccountId(jxAccountId);
		if(user == null){//未找到对应的用户
			return ;
		}
		String userCode = user.getStr("userCode");
		
		if("00000000".equals(resMap.get("retCode"))){//换绑卡成功
			//通过电子账户获取有效绑卡信息
			Map<String, Object> cardMap = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
			if(cardMap != null && "00000000".equals(cardMap.get("retCode"))){
				List<Map<String, String>> list = (List<Map<String, String>>)cardMap.get("subPacks");
				if(list != null && list.size() > 0){
					Map<String, String> map = list.get(0);
					bankNo = map.get("cardNo");
					//查询所属银行
					String nameOfBank = BankUtil.getNameOfBank(bankNo);
					if(!StringUtil.isBlank(nameOfBank)){
						bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
					}
					
				}
				//根据电子账号查询用户手机号
				Map<String, String> mobileMap = JXQueryController.mobileMaintainace(jxAccountId, "0", mobile);
				if(null != mobileMap && "00000000".equals(mobileMap.get("retCode"))){
					mobile = mobileMap.get("mobile");
					try {
						mobile = CommonUtil.encryptUserMobile(mobile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				UserInfo userInfo = userInfoService.findById(userCode);
				
				//查询用户的绑上信息
				BanksV2 banksV2 = banksService.findByUserCode(userCode);
				if(banksV2 != null){
					banksV2.set("trueName",cardMap.get("name"));
					banksV2.set("bankNo", bankNo);
					banksV2.set("bankName",bankName ) ;
					banksV2.set("mobile", mobile);
					banksV2.set("cardid", userInfo.getStr("userCardId"));
					banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
					banksV2.set("ssn", jxTraceCode);
					if(banksV2.update()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "换绑卡成功，更新本地数据", "");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡成功，未更新本地数据", null);
						return ;
					}
				}else{
					BanksV2 bankV2 = new BanksV2();
					bankV2.set("userCode", userCode);
					bankV2.set("userName", user.getStr("userName"));
					bankV2.set("trueName", cardMap.get("name"));
					bankV2.set("bankName",bankName ) ;
					bankV2.set("bankNo", bankNo);
					bankV2.set("bankType","" ) ;
					bankV2.set("cardCity", "");
					bankV2.set("mobile", mobile);
					bankV2.set("createDateTime", DateUtil.getNowDateTime());
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
					bankV2.set("isDefault", "1");
					bankV2.set("status", "0");
					bankV2.set("agreeCode", jxTraceCode);
					bankV2.set("ssn", jxTraceCode);
					bankV2.set("cardid", userInfo.getStr("userCardId")) ;
					if(bankV2.save()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "换绑卡成功，更新本地数据", "");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡成功，未更新本地数据", null);
						return ;
					}
				}
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "查询绑卡关系失败", null);
			}
			
		}else{
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡失败", null);
		}
		renderText("success");
	}
	
	/**
	 * 快捷充值回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/quickRechargeCallback")
	public void quickRechargeCallback() {
		// 获取回调数据
		String traceCode = getPara("traceCode");
		String bgData = getPara("bgData");
		
		// 解析bgData 将json格式转换成map格式
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> mapResp = (Map<String, String>) map;
		String jxTraceCode = getJxTraceCode(mapResp);	// 交易号
		
		// 更新响应报文
		JXService.updateJxTraceResponse(jxTraceCode, mapResp, JSON.toJSON(mapResp).toString().replace(",", ",\r\n"));
		
		// 检查是否存在发起充值的流水记录
		RechargeTrace rechargeTrace = rechargeTraceService.findById(traceCode);
		if (rechargeTrace == null) {
			fuiouCallbackLogger.log(Level.WARNING, String.format("平台没有流水号为[%s]的充值发起记录", traceCode));
			return;
		}
		String userName = rechargeTrace.getStr("userName");
		String userCode = rechargeTrace.getStr("userCode");
		long traceAmount = rechargeTrace.getLong("traceAmount");
		long amt = StringUtil.getMoneyCent(mapResp.get("txAmount"));
		
		// 检查充值金额是否一致
		if (amt != traceAmount) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值金额不符", null);
			fuiouCallbackLogger.log(Level.WARNING, String.format("用户[%s]的充值金额与平台上送金额不符", userCode));
			return;
		}
		
		// 检查订单交易状态
		if ("B".equals(rechargeTrace.getStr("traceState"))) {
			fuiouCallbackLogger.log(Level.INFO, String.format("重复接收的响应：用户[%s]的充值流水号为[%s]的充值记录为成功", userCode, traceCode));
			return;
		}
		if ("F".equals(rechargeTrace.getStr("traceState"))) {
			fuiouCallbackLogger.log(Level.WARNING, String.format("重复接收响应：用户[%s]的充值订单[%s]处于锁定状态", userCode, traceCode));
			return;
		}
		
		//锁定订单,避免重复操作
		int lockOrder = rechargeTraceService.updateStatus(TRACE_STATE.LOCKED.key(), traceCode, TRACE_STATE.DOING.key());
		if (lockOrder < 1) {
			fuiouCallbackLogger.log(Level.WARNING, String.format("用户[%s]，充值订单[%s]已被锁定，正在处理中...", userCode, traceCode));
			return;
		}
		
		if ("00000000".equals(mapResp.get("retCode"))) {	// 充值成功
			// 更新订单状态
			if (!jxTraceCode.equals(rechargeTrace.getStr("bankTraceCode"))) {
				rechargeTrace.set("bankTraceCode", jxTraceCode);
			}
			rechargeTrace.set("bankState", BANK_STATE.SUCCESS.key());
			rechargeTrace.set("traceState", TRACE_STATE.SUCCESS.key());
			rechargeTrace.set("traceRemark", "快捷支付，充值成功");
			rechargeTrace.set("bankRemark", "快捷支付，充值成功");
			rechargeTrace.set("okDateTime", DateUtil.getNowDateTime());
			if (rechargeTrace.update()) {
				// 修改资金账户
				Funds funds = fundsServiceV2.recharge(userCode, StringUtil.getMoneyCent(mapResp.get("txAmount")), 0, "快捷支付，充值成功", SysEnum.traceType.C.val());
				if (funds != null) {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值同步修改资金:" + mapResp.get("txAmount"));
					fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s][%s]，充值成功_同步平台资金！", userName, userCode));
				} else {
					fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s][%s]，充值成功_平台资金未同步！", userName, userCode));
				}
			} else {
				// 资金修改不成功——充值订单解锁
//				int unLockOrder = Db.update("UPDATE t_recharge_trace SET traceState = ? WHERE traceCode = ? AND traceState = ?", TRACE_STATE.DOING.key(), traceCode, TRACE_STATE.LOCKED.key());
				int unLockOrder = rechargeTraceService.updateStatus(TRACE_STATE.DOING.key(), traceCode, TRACE_STATE.LOCKED.key());
				if (unLockOrder > 0) {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败_充值订单解锁成功", traceCode);
					fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s],充值流水[%s]更新失败_充值订单解锁成功", userCode, traceCode));
				} else {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败_充值订单解锁失败", traceCode);
					fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s],充值流水[%s]更新失败_充值订单解锁失败", userCode, traceCode));
				}
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s],充值流水[%s]更新失败", userCode, traceCode));
				return;
			}
			// 发送充值成功短信
			try {
				String mobile = userService.getMobile(userCode);
				String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE.replace("[userName]", rechargeTrace.getStr("userName")).replace("[payAmount]", mapResp.get("txAmount") + "");
				SMSLog smsLog = new SMSLog();
				smsLog.set("mobile", mobile);
				smsLog.set("content", content);
				smsLog.set("userCode", userCode);
				smsLog.set("userName", rechargeTrace.getStr("userName"));
				smsLog.set("type", "13");
				smsLog.set("typeName", "在线充值");
				smsLog.set("status", 9);
				smsLog.set("sendDate", DateUtil.getNowDate());
				smsLog.set("sendDateTime", DateUtil.getNowDateTime());
				smsLog.set("break", "");
				smsLogService.save(smsLog);
			} catch (Exception e) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值短信发送失败", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s],充值短信发送失败", userCode));
				renderText("success");
			}
		} else {	// 充值失败
			rechargeTrace.set("bankTraceCode", jxTraceCode);
			rechargeTrace.set("bankState", BANK_STATE.FAILD.key());
			rechargeTrace.set("traceState", TRACE_STATE.FAILD.key());
			rechargeTrace.set("traceRemark", "快捷支付,充值失败");
			rechargeTrace.set("bankRemark", "快捷支付,充值失败");
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败" + mapResp.get("retCode") + "," + mapResp.get("retMsg"), null);
			fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s],流水号[%s]充值失败,retCode:[%s]  retMsg:[%s]", userCode, traceCode, mapResp.get("retCode"), mapResp.get("retMsg")));
			if (rechargeTrace.update()) {					
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值失败_更新充值流水", null);
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_充值流水未更新", null);
			}
		}
		renderText("success");
	}
	

	/**
	 * useless
	 * (手机)快捷充值接受通知接口
	 * */
	@ActionKey("/app_fast_showResult4fuiou")
	@AuthNum(value = 999)
	@Before({ AppInterceptor.class, PkMsgInterceptor.class })
	public void fast_showResult4fuiouForApp() {

		try {			
			String resp_code = getPara("resp_code");
			String amt = getPara("amt");
			String mchnt_cd = getPara("mchnt_cd");
			String mchnt_txn_ssn = getPara("mchnt_txn_ssn");

			RechargeTrace trace = RechargeTrace.rechargeTraceDao
					.findById(mchnt_txn_ssn);

			if (trace == null) {
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}

			String userCode = trace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(FuiouController.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_商户号不符", null);
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}

			long payAmount = Long.parseLong(amt);
			if (amt == null || payAmount != trace.getLong("traceAmount")) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_充值金额不符", null);
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}
			
			// 如果交易状态已更新，直接返回成功页面，针对网银充值有两次同步回调
			if ("B".equals(trace.getStr("traceState"))) {
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=paysucc", true);
				return ;
				}
						
			if ("0000".equals(resp_code)) {
				// 更新订单状态
				trace.set("bankState", BANK_STATE.SUCCESS.key());
				trace.set("traceState", TRACE_STATE.SUCCESS.key());
				trace.set("traceRemark", "手机支付,充值成功");
				trace.set("bankRemark", "手机支付,充值成功");
				trace.set("okDateTime", DateUtil.getNowDateTime());
				// 发送充值成功短信
				try {
					String mobile = userService.getMobile(userCode);
					String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE.replace(
							"[userName]", trace.getStr("userName")).replace(
							"[payAmount]", amt.substring(0, amt.length() - 2) + "");
					SMSLog smsLog = new SMSLog();
					smsLog.set("mobile", mobile);
					smsLog.set("content", content);
					smsLog.set("userCode", userCode);
					smsLog.set("userName", trace.getStr("userName"));
					smsLog.set("type", "12");
					smsLog.set("typeName", "在线充值");
					smsLog.set("status", 9);
					smsLog.set("sendDate", DateUtil.getNowDate());
					smsLog.set("sendDateTime", DateUtil.getNowDateTime());
					smsLog.set("break", "");
					smsLogService.save(smsLog);
				} catch (Exception e) {
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值短信发送失败", null);
					return ;
				}
				if (trace.update()) {
					// 修改资金账户
					fundsServiceV2.recharge(userCode, payAmount, 0,"手机支付,充值成功", SysEnum.traceType.C.val());
					User user = userService.findById(userCode);
					fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn,
							user, payAmount, FuiouTraceType.A);
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "APP充值同步修改资金" + payAmount);
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=paysucc", true);
					return ;
				} else {
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
					return ;
				}
			} else {
				// 更新订单状态
				trace.set("bankState", BANK_STATE.FAILD.key());
				trace.set("traceState", TRACE_STATE.FAILD.key());
				trace.set("traceRemark", "手机支付,充值失败");
				trace.set("bankRemark", "手机支付,充值失败");
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败" + resp_code, null);
				if (trace.update()) {
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
					return ;
				}
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	}
	
	/**
	 * 接收富友返回参数 20170605 WCF
	 */
	@ActionKey("/withdrawPageNotify")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void withdrawPageNotify() {
		try {
			String resp_code = getPara("resp_code");
			String amt = getPara("amt");
			String mchnt_cd = getPara("mchnt_cd");
			String mchnt_txn_ssn = getPara("mchnt_txn_ssn");
			int type = Integer.parseInt(getPara("tag"));
			
			WithdrawTrace withdrawTrace = withdrawTraceService
					.findById(mchnt_txn_ssn);
			if (withdrawTrace == null) {
				if (type == 1) {
					redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
					return ;
				} else {
					redirect("/withdrawFaild?err=" + resp_code, true);
					return ;
				}
			}
			
			String userCode = withdrawTrace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(FuiouController.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
				if (type == 1) {
					redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
					return ;
				} else {
					redirect("/withdrawFaild?err=" + resp_code, true);
					return ;
				}
			}
			
			long sxf = (long) withdrawTrace.getInt("sxf");			
			long amount = Long.parseLong(amt);
			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
			if (amt == null || !(withdrawAmount == amount + sxf )) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_提现金额不符", null);
				if (type == 1) {
					redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
					return ;
				} else {
					redirect("/withdrawFaild?err=" + resp_code, true);
					return ;
				}
			}
			
		//  如果交易状态已更新，直接返回成功响应
			if ("3".equals(withdrawTrace.getStr("status"))) {
				forward("/pay/withdrawSuccess.html", true);
				return ;
				}
					
			if ("0000".equals(resp_code)) {
				withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
				withdrawTrace.set("status", "3");
				withdrawTrace.set("withdrawRemark", "提现成功");
				
				// wzUser为吴总平台账号,此处用于客户提现手续费划拨
				User wzUser = userService.findByMobile(CommonUtil.OUTCUSTNO);
				User user = userService.findById(userCode);
				
								
				if (withdrawTrace.update()) {
					// 划拨手续费至吴总账户
					if (sxf > 0) {
						fuiouTraceService.refund(sxf, FuiouTraceType.I, user, wzUser);
					}
										
					// 如果使用会员免费提现次数，记录一次				
					String useFree = withdrawTrace.getStr("useFree");
					if (useFree.equals("y")) {
						boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
						if (ok) {
							BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功，提现金额 ："
									+ withdrawAmount / 10.0 / 10.0);
						}
					}
					// 扣除提现抵扣积分
					String isScore = withdrawTrace.getStr("isScore");
					if ("1".equals(isScore) && user.getInt("vipLevel") < 7
							&& (useFree.equals("y") == false)) {
						fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
						BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
					}
					fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user,
							withdrawAmount, FuiouTraceType.B);
					
				//  修改资金账户可用余额
					boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
							userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
					if (withdrawals4Fuiou == false) {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现修改资金失败 ", null);
						if (type == 1) {
							redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
							return ;
						} else {
							redirect("/withdrawFaild?err=" + resp_code, true);
							return ;
						}
					} else {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现同步修改资金" + withdrawAmount);
						if (type == 1) {
							redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=getsucc",true);
							return ;
						} else {
							forward("/pay/withdrawSuccess.html", true);
							return ;
						}
					}
				} else {
					if (type == 1) {
						redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
						return ;
					} else {
						redirect("/withdrawFaild?err=" + resp_code, true);
						return ;
					}
				}
								
			} else {
				withdrawTrace.set("status", "4");
				withdrawTrace.set("withdrawRemark", "提现失败");
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败" + resp_code, null);
				if (withdrawTrace.update()) {
					if (type == 1) {
						redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
						return ;
					} else {
						redirect("/withdrawFaild?err=" + resp_code, true);
						return ;
					}
				}
				if (type == 1) {
					redirect(CommonUtil.APP_URL + "/ispaysucc.html?type=geterro",true);
					return ;
				} else {
					redirect("/withdrawFaild?err=" + resp_code, true);
					return ;
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	
	/**
	 * 提现返回通知
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/withdrawCallback")
	public void withdrawCallback(){
		String withdrawCode = getPara("withdrawCode","");
		String bgData = getPara("bgData");
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> resMap = (Map<String, String>) map;
		String jxTraceCode = getJxTraceCode(resMap);	// 交易号
		
		//存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
		
		//检查数据中有没有此流水的提现申请
		if(withdrawTrace == null){
			fuiouCallbackLogger.log(Level.INFO, String.format("提现流水号不存在:[%s]", withdrawCode));
			return ;
		}
		String userCode = withdrawTrace.getStr("userCode");
		
		long sxf = (long)withdrawTrace.getInt("sxf");
		long amount = StringUtil.getMoneyCent(resMap.get("txAmount"));
		
		long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
		if(withdrawAmount != (amount + sxf)){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "平台提现金额与存管提现金额不符", null);
			fuiouCallbackLogger.log(Level.INFO, String.format("[%s]平台提现金额与存管提现金额不符", userCode));
			return ;
		}
		
		//检查交易状态是否已更新
		if("3".equals(withdrawTrace.getStr("status")) || "1".equals(withdrawTrace.getStr("status"))){
			fuiouCallbackLogger.log(Level.INFO, String.format("用户：[%s],流水号为[%s]的提现状态为成功或已审核", userCode, withdrawCode));
			return ;
		}
		
		if(!jxTraceCode.equals(withdrawTrace.getStr("bankTraceCode"))){
			withdrawTrace.set("bankTraceCode", jxTraceCode);
		}
		
		if(dealRetCode(resMap.get("retCode"))){
			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "1");
			withdrawTrace.set("withdrawRemark", "已审核");
			
			User user = userService.findById(userCode);
			
			if(withdrawTrace.update()){
				// 如果使用会员免费提现次数，记录一次				
				String useFree = withdrawTrace.getStr("useFree");
				if (useFree.equals("y")) {
					boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
					if (ok) {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功_提现金额 ：" + StringUtil.getMoneyYuan(withdrawAmount));
						fuiouCallbackLogger.log(Level.INFO, String.format("用户[%s]提现成功_提现金额[%s]", userCode, StringUtil.getMoneyYuan(withdrawAmount)));
					}
				}
				// 扣除提现抵扣积分
				String isScore = withdrawTrace.getStr("isScore");
				if ("1".equals(isScore) && user.getInt("vipLevel") < 8
						&& (useFree.equals("y") == false)) {
					fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
					BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
					fuiouCallbackLogger.log(Level.INFO, String.format("[%s]扣除提现抵扣积分:200", userCode));
				}
				
				//修改资金账户可用余额
				boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
						userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
				if (withdrawals4Fuiou == false) {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_修改资金失败 ", null);
					fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现成功_修改资金失败", userCode));
					return ;
				} else {//成功
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_同步修改资金" + StringUtil.getMoneyYuan(withdrawAmount));
					fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现成功_同步修改资金", userCode));
				}
				
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_流水更新失败", null);
				fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现成功_流水更新失败", userCode));
				return ;
			}
		}else{
			withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "4");
			withdrawTrace.set("withdrawRemark", "提现失败:" + resMap.get("retCode") + "_" + resMap.get("retMsg"));
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_" + resMap.get("retCode"), null);
			fuiouCallbackLogger.log(Level.INFO, String.format("[%s]提现失败:[%s],[%s]", userCode, resMap.get("retCode"), resMap.get("retMsg")));
			if (withdrawTrace.update()) {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_更新提现流水");
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_流水未更新", null);
			}
		}
		renderText("success");
	}
	

	/**
	 * App提现通知接口
	 * 
	 * @return
	 */
	@ActionKey("/app_withdrawPageNotify")
	@AuthNum(value = 999)
	@Before({ AppInterceptor.class, PkMsgInterceptor.class })
	public void withdrawCallback4Fuiou() {
		try {
			String resp_code = getPara("resp_code");
			String amt = getPara("amt");
			String mchnt_cd = getPara("mchnt_cd");
			String mchnt_txn_ssn = getPara("mchnt_txn_ssn");
			
			WithdrawTrace withdrawTrace = withdrawTraceService
					.findById(mchnt_txn_ssn);
			if (withdrawTrace == null) {
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			String userCode = withdrawTrace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			long sxf = (long) withdrawTrace.getInt("sxf");			
			long amount = Long.parseLong(amt);
			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
			if (amt == null || !(withdrawAmount == amount + sxf )) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_提现金额不符", null);
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			//如果交易状态已更新，直接返回成功响应
			if ("3".equals(withdrawTrace.getStr("status"))) {
				redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=getsucc", true);
				return ;
				}
			
			if ("0000".equals(resp_code)) {
				// wzUser为吴总平台账号,此处用于客户提现手续费划拨
				User wzUser = userService.findByMobile(CommonUtil.OUTCUSTNO);
				User user = userService.findById(userCode);
				
				if (sxf > 0) {
					fuiouTraceService.refund(sxf, FuiouTraceType.I, user, wzUser);
				}
								
				// 如果使用会员免费提现次数，记录一次				
				String useFree = withdrawTrace.getStr("useFree");
				if (useFree.equals("y")) {
					boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
					if (ok) {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功，提现金额 ："
								+ withdrawAmount / 10.0 / 10.0);
					}
				}
				// 扣除提现抵扣积分
				String isScore = withdrawTrace.getStr("isScore");
				if ("1".equals(isScore) && user.getInt("vipLevel") < 7
						&& (useFree.equals("y") == false)) {
					fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
					BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
				}
				
				withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
				withdrawTrace.set("status", "3");
				withdrawTrace.set("withdrawRemark", "提现成功");
				if (withdrawTrace.update()) {
					fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user,
							withdrawAmount, FuiouTraceType.B);
				} else {
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
				}
				
				//  修改资金账户可用余额
				boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
						userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
				if (withdrawals4Fuiou == false) {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现修改资金失败 ", null);
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
				} else {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "APP提现同步修改资金" + withdrawAmount);
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=getsucc", true);
					}
				} else {
					withdrawTrace.set("status", "4");
					withdrawTrace.set("withdrawRemark", "提现失败");
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败" + resp_code, null);
					if (withdrawTrace.update()) {
						redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
						return ;
					}
					redirect(CommonUtil.APP_URL + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
					return ;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 处理提现响应码
	 * @param retCode
	 * @return
	 */
	public static boolean dealRetCode(String retCode){
		if("00000000".equals(retCode) || "CE999028".equals(retCode) || "CT9903".equals(retCode) || "CT990300".equals(retCode)
				|| "CE999999".equals(retCode) || "510000".equals(retCode) || "502".equals(retCode) || "504".equals(retCode)){
			return true;
		}
		return false;
	}
	
	
	/**
	 * 自动投标签约  通知接口
	 * */
	@SuppressWarnings("unchecked")
	@ActionKey("/autoBidResponse")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void autoBidResponse(){
		String bgData = getPara("bgData");
		Map<String, ?> maps = JSONObject.parseObject(bgData);
		Map<String, String> map = (Map<String, String>) maps;
		JSONObject jxBgData =JSONObject.parseObject(bgData);
		// 生成本地报文流水号
		String jxCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		// 将响应报文存入数据库
		JXTrace jxTrace = jxTraceService.findById(jxCode);
		String retCode = jxTrace.getStr("retCode");
		if(!StringUtil.isBlank(retCode)){
			renderText("success");
			return;
		}
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		retCode = jxBgData.getString("retCode");
		if(!"00000000".equals(retCode)){
			renderNull();
			return;
		}
 		String accountId = jxBgData.getString("accountId");
		String orderId = jxBgData.getString("orderId");
		User user = User.userDao.findFirst("select * from t_user where jxAccountId = ?",accountId);
		if(null != user){
		String userCode = user.getStr("userCode");
		String userName = user.getStr("userName");
		String tempData = jxTrace.getStr("remark");
		JSONObject tempJson = JSONObject.parseObject(tempData);
		//获取设置数据
			//自动类型 autoType
			String autoType = tempJson.getString("autoType");
			//最大金额 onceMaxAmount
			long onceMaxAmount = tempJson.getLong("onceMaxAmount");
			//最小金额 onceMinAmount
			long onceMinAmount = tempJson.getLong("onceMinAmount");
			//最大期限 autoMaxLim
			int autoMaxLim = tempJson.getInteger("autoMaxLim");
			//最小期限 autoMinLim
			int autoMinLim = tempJson.getInteger("autoMinLim");
			//投资类型 refundType
			String refundType = tempJson.getString("refundType");
			//投资券类型 useTicket
			String useTicket = tempJson.getString("useTicket");
			//优先方式priorityMode
			String priorityMode = tempJson.getString("priorityMode");
			//过期时间deadline
			String deadline = tempJson.getString("deadLine");
		//---------
			int fff =1;
			try {
				fff = autoMapService.validateAutoState(userCode);
			} catch (Exception e) {
				fff = 1;
			}
			boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, onceMinAmount, onceMaxAmount, autoMinLim, autoMaxLim, refundType, "ABCD", priorityMode, useTicket, autoType,orderId,deadline);
			if(result){
				if(fff==0){
					if(result){
						String nowDate = DateUtil.getNowDate();
						int x = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161211" );
						int y = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161218" );
						if((x == 0 || x == 1) && (y == -1 || y == 0)){
							//11张30元券+11张50元券
							for (int i = 0; i < 11; i++) {
								ticketService.saveADV(userCode, "30元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
								ticketService.saveADV(userCode, "50元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
							}
						}
					}
				}
				String info="";
				info+="最小金额:"+onceMinAmount;
				info+="|最大金额:"+onceMaxAmount;
				info+="|最小期限:"+autoMinLim;
				info+="|最大期限:"+autoMaxLim;
				info+="|还款方式:"+refundType;
				info+="|自动投标类型:"+autoType;
				info+="|使用理财券类型:"+useTicket;
				info+="|理财券使用优先方式:"+priorityMode;
				info+="|过期时间:"+deadline;
				info+="|订单号:"+orderId;
				BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "手机保存自动投标配置成功",info);
				fuiouCallbackLogger.log(Level.INFO, "[自动投标签约回调]用户["+userName+"]["+userCode+"]保存自动投标配置成功"+info);
				//发送异步成功通知
				renderText("success");
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败;订单号："+orderId, null);
				fuiouCallbackLogger.log(Level.INFO, "[自动投标签约回调]用户["+userName+"]["+userCode+"]保存自动投标配置失败；订单号" + orderId);
			}
		}	
	}
	
	/**
	 * WJW 承接债权回调接口
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/carryOnTransferCallback")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class })
	public void carryOnTransferCallback() {
		Message msg = null;
		String bgData = getRequest().getParameter("bgData");
		if(StringUtil.isBlank(bgData)){
			msg = error("01", "bgData为空", "");
			renderJson(msg);
			return;
		}
		Map<String, Object> responseMap = JSONObject.parseObject(bgData);
		String retCode = String.valueOf(responseMap.get("retCode"));
		String retMsg = String.valueOf(responseMap.get("retMsg"));
		String accountId = String.valueOf(responseMap.get("accountId"));//承接人电子账号
		String txDate = String.valueOf(responseMap.get("txDate"));//交易日期
		String txTime = String.valueOf(responseMap.get("txTime"));//交易时间
		String seqNo = String.valueOf(responseMap.get("seqNo"));//交易流水号
		String jxTraceCode = txDate + txTime + seqNo;//t_jx_trace流水号
		String authCode = String.valueOf(responseMap.get("authCode"));//承接债权成功授权码
		
		Map<String, String> map = net.sf.json.JSONObject.fromObject(bgData);
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxTraceCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		
		//债转失败
		if(!"00000000".equals(retCode)){
			msg = error("02", "承接债权转让失败", retMsg);
			renderJson(msg);
			return;
		}
		JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
		String transCode = jxTrace.getStr("remark");
		LoanTransfer loanTransfer = loanTransferService.findById(transCode);
		if(null == loanTransfer){
			msg = error("03", "未找到该债权", "");
			renderJson(msg);
			return;
		}
		if(!"A".equals(loanTransfer.getStr("transState"))){
			msg = error("03", "该债权状态已修改", "");
			renderText("success");
			return;
		}
		String traceCode = loanTransfer.getStr("traceCode");
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		if(null == loanTrace){
			msg = error("03", "未找到该投标流水", "");
			renderJson(msg);
			return;
		}
		String transferCode = loanTransfer.getStr("transCode");//债权转让编号
		int transAmount = loanTransfer.getInt("transAmount");//承接人实际付款金额
		User user = userService.findByJXAccountId(accountId);//承接用户
		if(user == null){
			msg = error("02", "根据accountId未找到user", accountId);
			renderJson(msg);
			return;
		}
		String userCode = user.getStr("userCode");//承接人用户编码
		String gotUserName = user.getStr("userName");	// 承接人用户名
		
		//修改投标债权状态到  已转让
		boolean updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
				"A", "B");
		if(updateTraceState == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权失败[08]", null);
			msg = error("08", "承接债权失败[08]", "");
			renderJson(msg);
			return;
		}
		
		//转换相关金额存入资金流水备注
		Integer sysFee = loanTransfer.getInt("sysFee") ;//平台手续费
		Integer transFee = loanTransfer.getInt("transFee");//转让人让利金额
//		Integer riskFee = loanTransfer.getInt("riskFee");//风险备用金
		
		double remark4transFee = new BigDecimal((float)transFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		double remark4sysFee = new BigDecimal((float)sysFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//		double remark4riskFee = new BigDecimal((float)riskFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//		double remark4userFee = new BigDecimal((float)(transFee-riskFee)/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		//递减承接人可用余额
//		String remark = "债权转让支出，让利金额：￥"+remark4transFee+"（用户收益：￥"+remark4userFee+"，风险备用金：￥"+remark4riskFee+"）";
		String remark = "债权转让支出，让利金额：￥"+remark4transFee+"（用户收益：￥"+remark4transFee+"）";

		boolean b = fundsServiceV2.carryOnTransfer(userCode, transAmount,remark);
		
		if(b==true){
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权扣除可用余额成功  扣除金额  : " + transAmount);
		}
		
		if(b == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权-添加流水失败", null);
			msg = error("05", "承接债权失败-[添加扣款流水失败]!", "");
			renderJson(msg);
			return;
		}
		
		long ticket_amount = 0;
		
		String traceRemark = "债权转让收入，让利金额：￥"+remark4transFee+"，债权转让费：￥" + remark4sysFee;
		
		//查询是否转让过
		List<LoanTransfer> isTransfer =  loanTransferService.queryLoanTransferByTraceCode(loanTransfer.getStr("traceCode") , "B");
		int rewardticketrate=0;
		if(null == isTransfer || isTransfer.size() <= 0 ){
			try {
				
				String json_tickets = loanTrace.getStr("loanTicket");
				if(StringUtil.isBlank(json_tickets)==false){
					JSONArray ja = JSONArray.parseArray(json_tickets);
					for (int i = 0; i < ja.size(); i++) {
						JSONObject jsonObj = ja.getJSONObject(i);
						if(jsonObj.getString("type").equals("A")){
							//20170519   ---20170726新券改动  ws
							Long examount= jsonObj.getLong("examount");
							String isDel=jsonObj.getString("isDel");
							if(null==isDel||"".equals(isDel)){
								if(null==examount||examount>50000){
									ticket_amount = jsonObj.getLong("amount");
									traceRemark += "，现金券金额：￥"+ticket_amount/10/10;
								}
							}else{
								if("Y".equals(isDel)){
									ticket_amount = jsonObj.getLong("amount");
									traceRemark += "，现金券金额：￥"+ticket_amount/10/10;
								}
							}
							//end
						}
						if(jsonObj.getString("type").equals("C")){
							rewardticketrate+=jsonObj.getInteger("rate");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//转让人
		String payUserCode = loanTransfer.getStr("payUserCode");
		
		//转让价格(承接人实际付款) - 平台手续费
		long payTransAmount = transAmount - sysFee - ticket_amount;//债权人转让收益，扣除手续费和抵用券费用
		boolean carryOnTransferTo = fundsServiceV2.carryOnTransferTo(payUserCode, payTransAmount, sysFee ,transFee,traceRemark);
		if(carryOnTransferTo==true){
			//记录日志
			BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权增加可用余额成功  收益金额  : " + payTransAmount);
		}
		
		if(carryOnTransferTo == false){
			BIZ_LOG_ERROR(payUserCode, BIZ_TYPE.TRANSFER, "承接债权-添加转让人流水失败", null);
			msg = error("06", "承接债权失败-[添加转让人流水失败]!", "");
			renderJson();
			return;
		}
		
		//修改债权状态 并验证是否已经被转让
		boolean updateTransferState = loanTransferService.updateTransferState(transferCode,userCode,user.getStr("userName"));
		if(updateTransferState == false){
			//回滚资金
			fundsServiceV2.doAvBalance(userCode, 0, transAmount);
			fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
			//回滚流水状态
			loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"B", "A");
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权失败资金回滚");
			BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权失败资金回滚");
			msg = error("07", "债权已被承接", "");
			renderJson();
			return;
		}else{
			// 更新冗余信息
			String oPayUserCode = loanTrace.getStr("payUserCode");
			int rate = loanTransfer.getInt("rateByYear") + loanTransfer.getInt("rewardRateByYear") ;
			int reciedCount = loanTrace.getInt("loanRecyCount");
			int limit = loanTrace.getInt("loanTimeLimit");
			long[] benxi = new long[2];
			benxi[0] = loanTrace.getLong("leftAmount");
			benxi[1] = loanTrace.getLong("leftInterest");
			
			//假如第一次债转  并且  用的加息券   计算下期待还本息与总的利息
			String refundType=loanTrace.getStr("refundType");
			//承接人本息
			long[] benxi2 = new long[2];
			if("A".equals(refundType)){
				benxi2 =CommonUtil.f_004(benxi[0], rate, reciedCount, "A");
			}else if("B".equals(refundType)){
				benxi2 =CommonUtil.f_005(loanTrace.getLong("payAmount"), rate, limit, limit-reciedCount, "B");
			}
			if(rewardticketrate>0){
			//承接人下期本息
				long[] nextbenxi=new long[2];
			if("A".equals(refundType)){
				nextbenxi=CommonUtil.f_000(benxi[0], reciedCount, rate, 1, "A");
			}else if("B".equals(refundType)){
				nextbenxi=CommonUtil.f_000(benxi[0], limit, rate, reciedCount, "B");
			}
			loanTrace.set("leftInterest", benxi2[1]);//修改总代收利息
			loanTrace.set("nextAmount", nextbenxi[0]);//修改下期代收本金
			loanTrace.set("nextInterest", nextbenxi[1]);//修改下期代收利息
			loanTrace.set("rewardRateByYear",loanTransfer.getInt("rewardRateByYear"));//扣除加息券利息
			}
			//修改债权归属
			loanTrace.set("payUserCode", userCode );
			loanTrace.set("payUserName", user.getStr("userName") );
			
			boolean updateTrace = loanTrace.update();
			if(updateTrace == false){
				//回滚资金
				fundsServiceV2.doAvBalance(userCode, 0, transAmount);
				fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
				//记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
				BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
				msg = error("10", "债权数据出现异常，请联系客服处理。", "");
			}
			
			//更新冗余账户 (更新理财人待还账户)
			int beRecyCount = limit - reciedCount ;
			//减少原投资人账户	oPayUserCode      //  增加已回收资金  利息	 shiqingsong 2016-02-18
			long reciedInterest = transAmount - benxi[0] - transFee;
			fundsServiceV2.updateBeRecyFunds(oPayUserCode, (0-beRecyCount), (0-benxi[0]), (0-benxi[1]), benxi[0] ,  reciedInterest > 0 ? reciedInterest : 0);
			//增加接受人
			if(rewardticketrate>0){//若使用了加息券
			fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi2[0], benxi2[1], 0 ,  0 );
			}else{
			fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi[0], benxi[1], 0 ,  0 );
			}
			
		}
		//生日当天承接债转，白银及以上按等级送积分 	2018.03
		int transScore = loanTransfer.getInt("transScore");
		String cardId="";
		UserInfo userInfo=userInfoService.findById(userCode);
		
		cardId=userInfo.getStr("userCardId");
		
		if(CommonUtil.isBirth(cardId)){
			int vipLevel = user.getInt("vipLevel");
			if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
				transScore=transScore*2;
			}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
				transScore=transScore*3;
			}
		}
		
		fundsServiceV2.doPoints(userCode, 0, transScore, "承接债权获取可用积分收益!");
		
		//增加风险备用金
		fundsServiceV2.updateRiskTotal(loanTransfer.getInt("riskFee"));
		
		/*
		 * 债权承接成功——给借款人发送债权变更通知
		 * 1、获取承接标的信息
		 * 2、查询借款人、转让人、承接人信息
		 * 3、生成通知短信内容、发送、保存
		 */
		String loanCode = loanTransfer.getStr("loanCode");
		String loanNo = loanTransfer.getStr("loanNo");
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		
		Long leftAmount = loanTrace.getLong("leftAmount");//该债权剩余的本金
		
		String lName = loanInfo.getStr("userName");//借款人姓名
		String lUserCode = loanInfo.getStr("userCode");
		String mobile = userService.getMobile(lUserCode);//借款人手机号
		
		String cName = "";//承接人姓名
		String tName = "";//转让人姓名
		String cCardId = "";//承接人身份证
		String tCardId = "";//转让人身份证
		try {
			cName = userInfo.getStr("userCardName");
			UserInfo tUserInfo = userInfoService.findById(payUserCode);
			tName = tUserInfo.getStr("userCardName");
			
			cCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			tCardId = CommonUtil.decryptUserCardId(tUserInfo.getStr("userCardId"));
			
			cCardId = cCardId.substring(0, 4) + "****" + cCardId.substring(cCardId.length()-4,cCardId.length());
			tCardId = tCardId.substring(0, 4) + "****" + tCardId.substring(tCardId.length()-4, tCardId.length());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权成功");
		BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权成功");
		
		boolean updateAuthCode = loanTransferService.updateJxInfo(authCode, jxTraceCode, transCode);
		BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债转授权码更新"+(updateAuthCode?"成功":"失败")+"["+authCode+"]");
		
		// 更新债转还款计划
//		LoanRepaymentController loanRepaymentController = new LoanRepaymentController();
//		loanRepaymentController.transfer(transferCode, userCode, gotUserName);
		loanRepaymentService.transfer(transferCode, userCode, gotUserName);
		
		boolean tranferEd = true;
		FileOperate file = new FileOperate();
		try {
			//需剔除的借款人姓名
			String[] fileNames1 = file.readTxtLine("//data//loanTranfers_file//name", "GBK");
			for(int i = 0;i<fileNames1.length;i++){
				String tranferName = fileNames1[i];
				if(lName.equals(tranferName)){
					tranferEd = false;
				}
			}
			if(tranferEd){
				//需剔除的借款标号
				String[] fileNames2 = file.readTxtLine("//data//loanTranfers_file//loanno", "GBK");
				for(int i = 0;i<fileNames2.length;i++){
					String loanNo2 = fileNames2[i];
					if(loanNo.equals(loanNo2)){
						tranferEd = false;
					}
				}
			}
			if(tranferEd){
				String msgContent = CommonUtil.SMS_SMG_TRANSFER_CHANGE.replace("[loanName]", lName).replace("[transferName]",tName ).replace("[transferCardId]", tCardId).replace("[loanNo]", loanNo)
						.replace("[transAmount]", Number.longToString(leftAmount)).replace("[carryOnName]", cName).replace("[carryOnCardId]", cCardId).replace("[nowDate]", new SimpleDateFormat("YYYY年MM月dd日").format(new Date()));
				SMSLog smsLog = new SMSLog();
				smsLog.set("mobile", mobile);
				smsLog.set("content", msgContent);
				smsLog.set("userCode", lUserCode);
				smsLog.set("userName", lName);
				smsLog.set("type", "18");smsLog.set("typeName", "债权变更通知");
				smsLog.set("status", 9);
				smsLog.set("sendDate", DateUtil.getNowDate());
				smsLog.set("sendDateTime", DateUtil.getNowDateTime());
				smsLog.set("break", "");
				smsLogService.save(smsLog);
			}
		} catch (Exception e) {
			tranferEd = false;
		}
		
		renderText("success");
			
		}
	

	// 私有方法---------------------------------------------------------------------------------------------

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
		//次数限制
		Long count = Db.queryLong("select count(1) from t_auth_log where userCode = ?" ,userCode);
		if(count > 3){
			return error("01", "认证次数超限制", "");
		}
		
		boolean update = userInfoService.newUserAuth(userCode,trueName, md5CardId, "","2",idType);
		if(!update){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
			return error("02", "已经认证,请勿重复提交!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户自动认证成功");
		
		// 身份认证成功赠送可用积分
		fundsServiceV2.doPoints(userCode, 0 , 2000, "注册送积分");
		
		// 身份认证后，邀请人添加30元现金抵用券  5月活动,9月继续，常态
		try{
			User user = userService.findById(userCode);
			RecommendInfo rmd = RecommendInfo.rmdInfoDao.findFirst("select * from t_recommend_info where bUserCode = ?",user.getStr("userCode"));
			if(rmd != null){
				User shareUser = userService.findById(rmd.getStr("aUserCode"));
				if(shareUser!=null){
					//实名认证送券
					boolean aa = ticketsService.saveADV(shareUser.getStr("userCode"), "50元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
					if(aa){
						String mobile = userService.getMobile(shareUser.getStr("userCode"));
						String content = CommonUtil.SMS_MSG_TICKET.replace("[huoDongName]", "推荐好友实名认证").replace("[ticketAmount]", "50");
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", content);
						smsLog.set("userCode", shareUser.getStr("userCode"));
						smsLog.set("userName", shareUser.getStr("userName"));
						smsLog.set("type", "15");smsLog.set("typeName", "送现金券活动");
						smsLog.set("status", 9);
						smsLog.set("sendDate", DateUtil.getNowDate());
						smsLog.set("sendDateTime", DateUtil.getNowDateTime());
						smsLog.set("break", "");
						smsLogService.save(smsLog);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

}
