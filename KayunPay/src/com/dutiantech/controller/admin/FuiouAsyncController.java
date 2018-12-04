package com.dutiantech.controller.admin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.fuiou.data.QueryCzTxRspDetailData;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * @author WCF 用于接收富友发送的异步通知
 */
public class FuiouAsyncController extends BaseController {
	
	private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private UserService userService = getService(UserService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	
	private static final Logger fuiouAsyncLogger = Logger.getLogger("fuiouAsyncLogger");
	static Map<String, RechargeTrace> _LOCK_RECHARGE_TRACE = new HashMap<String, RechargeTrace>();
	static Map<String, WithdrawTrace> _LOCK_WITHDRAW_TRACE = new HashMap<String, WithdrawTrace>();

	static{
		LoggerUtil.initLogger("fuiouAsyncLogger", fuiouAsyncLogger);
	}
	
	private synchronized void lockTrace(String traceCode, String traceType) {
		if ("recharge".equals(traceType)) {
			RechargeTrace rechargeTrace = rechargeTraceService.findById(traceCode);
			_LOCK_RECHARGE_TRACE.put(traceCode, rechargeTrace);
		}
		if ("withdraw".equals(traceType)) {
			WithdrawTrace withdrawTrace = withdrawTraceService.findById(traceCode);
			_LOCK_WITHDRAW_TRACE.put(traceCode, withdrawTrace);
		}
	}
	
	private boolean isLock(String traceCode, String traceType) {
		if ("recharge".equals(traceType)) {
			return (_LOCK_RECHARGE_TRACE.get(traceCode) != null);
		}
		if ("withdraw".equals(traceType)) {
			return (_LOCK_WITHDRAW_TRACE.get(traceCode) != null);
		}
		return false;
	}
	
	private void unlock(String traceCode, String traceType) {
		if ("recharge".equals(traceType)) {
			_LOCK_RECHARGE_TRACE.remove(traceCode);
		}
		if ("withdraw".equals(traceType)) {
			_LOCK_WITHDRAW_TRACE.remove(traceCode);
		}
	}
	
	@ActionKey("/scanFuiouCzTxTrace")
	@Before({Tx.class, PkMsgInterceptor.class})
	public void scanFuiouCzTxTrace() {
		Message msg = fuiouCzTxTask();
		renderJson(msg);
	}
	
	@ActionKey("/unLockFuiouCzTxTrace")
	@Before({Tx.class, PkMsgInterceptor.class})
	public void unlockFuiouCzTxTrace() {
		Iterator<Map.Entry<String, RechargeTrace>> rechargeIterator = _LOCK_RECHARGE_TRACE.entrySet().iterator();
		while (rechargeIterator.hasNext()) {
			Map.Entry<String, RechargeTrace> entry = rechargeIterator.next();
			unlock(entry.getKey(), "recharge");
		}
		Iterator<Map.Entry<String, WithdrawTrace>> withdrawIterator = _LOCK_WITHDRAW_TRACE.entrySet().iterator();
		while (withdrawIterator.hasNext()) {
			Map.Entry<String, WithdrawTrace> entry = withdrawIterator.next();
			unlock(entry.getKey(), "withdraw");
		}
		Message msg = succ("00", "00");
		renderJson(msg);
	}
	
	private Message fuiouCzTxTask(){
		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.scanTouBiaoKey");
		if(!key.equals(preKey)){
			return error("01","密匙错误", false );
		}
		int intervalTime = 10;	// 30分钟内 超过10分钟未处理的流水
		
		// 处理充值流水
		try {
			FuiouController fuiouController = new FuiouController();
			long total = Db.queryLong("SELECT COUNT(1) FROM t_recharge_trace WHERE traceState = 'A' AND traceDateTime BETWEEN DATE_FORMAT(DATE_SUB(now(),INTERVAL 30 MINUTE), '%Y%m%d%H%i%s') AND DATE_FORMAT(DATE_SUB(now(),INTERVAL " + intervalTime + " MINUTE), '%Y%m%d%H%i%s')"); 
			int doCount = 1;
			fuiouAsyncLogger.log(Level.INFO, "[定时任务：自动扫描并处理充值流水]扫描中.....共计" + total + "未处理充值流水...");
			List<Object[]> lstRechargeTraces = getRechargeTraces(intervalTime);
			for (Object[] rechargeTrace : lstRechargeTraces) {
				String traceCode = rechargeTrace[0].toString();
				String userCode = rechargeTrace[1].toString();
				User user = userService.findById(userCode);
				
//				if (isLock(traceCode, "recharge")) {
//					return error("11", "充值流水[" + traceCode + "]正在处理中", null) ;
//				}
//				
//				lockTrace(traceCode, "recharge");
				
				fuiouAsyncLogger.log(Level.INFO, "[定时任务：自动扫描并处理充值流水],流水[" + traceCode + "],第" + doCount + "个...共" + total + "条流水未处理");
				// 主动请求富友接口，查询流水状态
				QueryCzTxRspDetailData queryCzTxRspDetailData = fuiouController.queryCzTxDetail(traceCode, FuiouTraceType.RECHARGE);
				if (queryCzTxRspDetailData != null) {
					String txnRspCd = queryCzTxRspDetailData.getTxn_rsp_cd();	// 返回码
//					String rspCdDesc = queryCzTxRspDetailData.getRsp_cd_desc();	// 返回码描述
					RechargeTrace trace = rechargeTraceService.findById(traceCode);
					long payAmount = trace.getLong("traceAmount");
					// 更新流水状态
					if ("0000".equals(txnRspCd)) {
						trace.set("bankState", BANK_STATE.SUCCESS.key());
						trace.set("traceState", TRACE_STATE.SUCCESS.key());
						if ("FAST".equals(trace.getStr("rechargeType"))) {
							trace.set("traceRemark", "快捷支付,充值成功");
							trace.set("bankRemark", "快捷支付,充值成功");
						}
						if ("WY".equals(trace.getStr("rechargeType"))) {
							trace.set("traceRemark", "网银支付,充值成功");
							trace.set("bankRemark", "网银支付,充值成功");
						}
						if ("PHONE".equals(trace.getStr("rechargeType"))) {
							trace.set("traceRemark", "手机支付,充值成功");
							trace.set("bankRemark", "手机支付,充值成功");
						}
						trace.set("okDateTime", DateUtil.getNowDateTime());
						if (trace.update()) {
							// 修改资金账户 添加资金流水
							String remark = "";
							if ("FAST".equals(trace.getStr("rechargeType"))) {
								remark = "快捷支付,充值成功";
							}
							if ("WY".equals(trace.getStr("rechargeType"))) {
								remark = "网银支付,充值成功";
							}
							if ("PHONE".equals(trace.getStr("rechargeType"))) {
								remark = "手机支付,充值成功";
							}
							fundsServiceV2.recharge(userCode, payAmount, 0, remark, SysEnum.traceType.C.val());
							fuiouTraceService.fuiouTraceContent(traceCode, user, payAmount, FuiouTraceType.RECHARGE);
							fuiouAsyncLogger.log(Level.INFO, "用户[" + userCode + "]充值异步修改资金[" + payAmount + "]");
							
							// 发送充值短信
							try {
								String mobile = userService.getMobile(userCode);
								String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE
										.replace("[userName]", trace.getStr("userName"))
										.replace("[payAmount]", CommonUtil.yunsuan(trace.getLong("traceAmount") + "","100.0", "chu", 2) + "");
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
							}
//							unlock(traceCode, "recharge");
						} else {
							// 记录日志
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
						}
					} else {
						fuiouTraceService.fuiouTraceError(traceCode, user, payAmount, FuiouTraceType.RECHARGE);
					}
				}
				doCount++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fuiouAsyncLogger.log(Level.SEVERE, "自动扫描并处理充值流水相关信息时发生异常" + e.getMessage());
			throw e;
		}
		
		// 处理提现流水
		try {
			FuiouController fuiouController = new FuiouController();
			long total = Db.queryLong("SELECT COUNT(1) FROM t_withdraw_trace WHERE `status` = 2 AND createDateTime BETWEEN DATE_FORMAT(DATE_SUB(now(),INTERVAL 30 MINUTE), '%Y%m%d%H%i%s') AND DATE_FORMAT(DATE_SUB(now(),INTERVAL " + intervalTime + " MINUTE), '%Y%m%d%H%i%s')"); 
			int doCount = 1;
			fuiouAsyncLogger.log(Level.INFO, "[定时任务：自动扫描并处理提现流水]扫描中.....共计" + total + "未处理提现流水...");
			List<WithdrawTrace> lstWithdrawTraces = getWithdrawTraces(intervalTime);
			for (WithdrawTrace withdrawTrace : lstWithdrawTraces) {
				String traceCode = withdrawTrace.getStr("withdrawCode");
				String userCode = withdrawTrace.getStr("userCode");
				User user = userService.findById(userCode);
				
//				if (isLock(traceCode, "withdraw")) {
//					return error("11", "提现流水[" + traceCode + "]正在处理中", null) ;
//				}
//				
//				lockTrace(traceCode, "withdraw");
				
				fuiouAsyncLogger.log(Level.INFO, "[定时任务：自动扫描并处理提现流水],流水[" + traceCode + "],第" + doCount + "个...共" + total + "条流水未处理");
				// 主动请求富友接口，查询流水状态
				QueryCzTxRspDetailData queryCzTxRspDetailData = fuiouController.queryCzTxDetail(traceCode, FuiouTraceType.WITHDRAW);
				if (queryCzTxRspDetailData != null) {
					String txnRspCd = queryCzTxRspDetailData.getTxn_rsp_cd();	// 返回码
//					String rspCdDesc = queryCzTxRspDetailData.getRsp_cd_desc();	// 返回码描述
					if ("0000".equals(txnRspCd)) {	// 交易成功，处理资金、流水及划拨业务
						String isScore = withdrawTrace.getStr("isScore");
						String useFree = withdrawTrace.getStr("useFree");
						long sxf = (long) withdrawTrace.getInt("sxf");
						long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
						if (queryCzTxRspDetailData.getTxn_amt() == null || withdrawAmount != Long.parseLong(queryCzTxRspDetailData.getTxn_amt()) + sxf) {
							// 记录日志
							fuiouAsyncLogger.log(Level.INFO, String.format("[%s]提现失败_提现金额不符", userCode));
						}
						
						// wzUser为吴总平台账号,此处用于客户提现手续费划拨
						User wzUser = userService.findByMobile(FuiouController.OUT_CUST_NO);
						
						if (sxf > 0) {
							fuiouTraceService.refund(sxf, FuiouTraceType.I, user, wzUser);
						}
						
						// 如果使用会员免费提现次数，记录一次			
						if (useFree.equals("y")) {
							boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
							if (ok) {
								BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功，提现金额 ："+ withdrawAmount / 10.0 / 10.0);
								fuiouAsyncLogger.log(Level.INFO, String.format("用户[%s]提现成功，提现金额[%s]", userCode, withdrawAmount / 100));
							}
						}
						// 扣除提现抵扣积分
						if ("1".equals(isScore) && user.getInt("vipLevel") < 7 && (useFree.equals("y") == false)) {
							fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
							BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
							fuiouAsyncLogger.log(Level.INFO, String.format("[%s]扣除提现抵扣积分", userCode));
						}
						
						withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
						withdrawTrace.set("status", "3");
						withdrawTrace.set("withdrawRemark", "提现成功");
						if (withdrawTrace.update()) {
							fuiouTraceService.fuiouTraceContent(traceCode, user, withdrawAmount, FuiouTraceType.WITHDRAW);
						} else {
							fuiouAsyncLogger.log(Level.INFO, String.format("用户[%s]异步更新提现流水失败", userCode));
						}
						
						//  修改资金账户可用余额
						boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
						if (withdrawals4Fuiou == false) {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "申请提现失败 ", null);
							fuiouAsyncLogger.log(Level.INFO, String.format("[%s]申请提现失败", userCode));
						} else {
							BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现异步修改资金" + withdrawAmount);
							fuiouAsyncLogger.log(Level.INFO, String.format("[%s]提现异步修改资金", userCode));
//							unlock(traceCode, "withdraw");
						}
					}
				}
				doCount ++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			fuiouAsyncLogger.log(Level.SEVERE, "自动扫描并处理提现流水相关信息时发生异常" + e.getMessage());
			throw e;
		} 
//		unlockFuiouCzTxTrace();
		fuiouAsyncLogger.log(Level.INFO, "自动扫描并处理充值提现流水相关信息任务完成");
		return succ("自动扫描并处理今日充值提现流水相关信息任务完成", true ) ;
	}

//	/**
//	 * 充值接收通知---异步
//	 */
//	@ActionKey("/rechargeAsync")
//	@AuthNum(value = 999)
//	public void rechargeAsync() {
//		try {
//			RechargeAndWithdrawalRspData rspData = FuiouRspParseService.rechargeAndWithdrawalNotifyParse(getRequest());
//			String mchnt_cd = rspData.getMchnt_cd();
//			String mchnt_txn_ssn = rspData.getMchnt_txn_ssn();
//			String amt = rspData.getAmt();
//
//			RechargeTrace trace = RechargeTrace.rechargeTraceDao.findById(mchnt_txn_ssn);
//			if (trace == null) {
//				renderHtml(FuiouRspParseService.notifyRspXml("0001", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//
//			if ("ON".equals(FuiouAsyncController.SWITCH)) {
//				FuiouAsyncController.SWITCH = "OFF";
//				// 如果交易状态已更新，直接返回成功响应
//				if ("B".equals(trace.getStr("traceState"))) {
//					renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				}
//				String userCode = trace.getStr("userCode");
//				if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
//					// 记录日志
//					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_商户号不符", null);
//					renderHtml(FuiouRspParseService.notifyRspXml("0002", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				}
//				long payAmount = Long.parseLong(amt);
//				if (amt == null || payAmount != trace.getLong("traceAmount")) {
//					// 记录日志
//					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_充值金额不符", null);
//					renderHtml(FuiouRspParseService.notifyRspXml("0003", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				}
//
//				// 更新订单状态
//				trace.set("bankState", BANK_STATE.SUCCESS.key());
//				trace.set("traceState", TRACE_STATE.SUCCESS.key());
//				if ("FAST".equals(trace.getStr("rechargeType"))) {
//					trace.set("traceRemark", "快捷支付,充值成功");
//					trace.set("bankRemark", "快捷支付,充值成功");
//				}
//				if ("WY".equals(trace.getStr("rechargeType"))) {
//					trace.set("traceRemark", "网银支付,充值成功");
//					trace.set("bankRemark", "网银支付,充值成功");
//				}
//				if ("PHONE".equals(trace.getStr("rechargeType"))) {
//					trace.set("traceRemark", "手机支付,充值成功");
//					trace.set("bankRemark", "手机支付,充值成功");
//				}
//				trace.set("okDateTime", DateUtil.getNowDateTime());
//				try {
//					String mobile = userService.getMobile(userCode);
//					String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE
//							.replace("[userName]", trace.getStr("userName"))
//							.replace("[payAmount]", amt.substring(0, amt.length() - 2) + "");
//					SMSLog smsLog = new SMSLog();
//					smsLog.set("mobile", mobile);
//					smsLog.set("content", content);
//					smsLog.set("userCode", userCode);
//					smsLog.set("userName", trace.getStr("userName"));
//					smsLog.set("type", "12");
//					smsLog.set("typeName", "在线充值");
//					smsLog.set("status", 9);
//					smsLog.set("sendDate", DateUtil.getNowDate());
//					smsLog.set("sendDateTime", DateUtil.getNowDateTime());
//					smsLog.set("break", "");
//					smsLogService.save(smsLog);
//				} catch (Exception e) {
//					// 记录日志
//					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值短信发送失败", null);
//					renderHtml(FuiouRspParseService.notifyRspXml("0004", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				}
//				if (trace.update()) {
//					// 修改资金账户 添加资金流水
//					String remark = "";
//					if ("FAST".equals(trace.getStr("rechargeType"))) {
//						remark = "快捷支付,充值成功";
//					}
//					if ("WY".equals(trace.getStr("rechargeType"))) {
//						remark = "网银支付,充值成功";
//					}
//					if ("PHONE".equals(trace.getStr("rechargeType"))) {
//						remark = "手机支付,充值成功";
//					}
//					fundsServiceV2.recharge(userCode, payAmount, 0, remark, SysEnum.traceType.C.val());
//					User user = userService.findById(userCode);
//					fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user, payAmount, FuiouTraceType.A);
//					
//					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值异步修改资金" + payAmount);
//					renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				} else {
//					// 记录日志
//					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
//					renderHtml(FuiouRspParseService.notifyRspXml("0005", mchnt_cd, mchnt_txn_ssn));
//					FuiouAsyncController.SWITCH = "ON";
//					return;
//				}
//			} else {
//				return;
//			}
//		} catch (Exception e1) {
//			FuiouAsyncController.SWITCH = "ON";
//			e1.printStackTrace();
//		}
//		return;
//	}

//	/**
//	 * 提现接收通知---异步
//	 */
//	@ActionKey("/withdrawalAsync")
//	@AuthNum(value = 999)
//	public void withdrawalAsync() {
//		try {
//			Thread.sleep(5000);
//			RechargeAndWithdrawalRspData rspData = FuiouRspParseService.rechargeAndWithdrawalNotifyParse(getRequest());
//			String mchnt_cd = rspData.getMchnt_cd();
//			String mchnt_txn_ssn = rspData.getMchnt_txn_ssn();
//			String amt = rspData.getAmt();
//
//			WithdrawTrace withdrawTrace = withdrawTraceService.findById(mchnt_txn_ssn);
//
//			if (withdrawTrace == null) {
//				renderHtml(FuiouRspParseService.notifyRspXml("1001", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//			// 如果交易状态已更新，直接返回成功响应
//			if ("3".equals(withdrawTrace.getStr("status"))) {
//				renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//			String isScore = withdrawTrace.getStr("isScore");
//			String useFree = withdrawTrace.getStr("useFree");
//			long sxf = (long) withdrawTrace.getInt("sxf");
//			long amount = Long.parseLong(amt);
//			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
//			String userCode = withdrawTrace.getStr("userCode");
//			if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
//				// 记录日志
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
//				renderHtml(FuiouRspParseService.notifyRspXml("1002", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//			if (amt == null || !(withdrawAmount == amount + sxf)) {
//				// 记录日志
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_提现金额不符", null);
//				renderHtml(FuiouRspParseService.notifyRspXml("1003", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//
//			// wzUser为吴总平台账号,此处用于客户提现手续费划拨
//			User wzUser = userService.findByMobile(CommonUtil.OUTCUSTNO);
//			User user = userService.findById(userCode);
//
//			if (sxf > 0) {
//				fuiouTraceService.refund(sxf, FuiouTraceType.I, user, wzUser);
//			}
//
//			// 如果使用会员免费提现次数，记录一次
//			if (useFree.equals("y")) {
//				boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
//				if (ok) {
//					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功，提现金额 ：" + withdrawAmount / 10.0 / 10.0);
//				}
//			}
//			// 扣除提现抵扣积分
//			if ("1".equals(isScore) && user.getInt("vipLevel") < 7 && (useFree.equals("y") == false)) {
//				fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
//			}
//
//			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
//			withdrawTrace.set("status", "3");
//			withdrawTrace.set("withdrawRemark", "提现成功");
//			if (withdrawTrace.update()) {
//				fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user, withdrawAmount, FuiouTraceType.B);
//			} else {
//				renderHtml(FuiouRspParseService.notifyRspXml("1004", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//
//			// 修改资金账户可用余额
//			boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
//					userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
//			if (withdrawals4Fuiou == false) {
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "申请提现失败 ", null);
//				renderHtml(FuiouRspParseService.notifyRspXml("1005", mchnt_cd, mchnt_txn_ssn));
//				return;
//			} else {
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现异步修改资金" + withdrawAmount);
//				renderHtml(FuiouRspParseService.notifyRspXml("0000", mchnt_cd, mchnt_txn_ssn));
//				return;
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 查询间隔时间内未处理的充值流水
	 * @param intervalTime	间隔时间，单位：分钟
	 * @return
	 */
	private List<Object[]> getRechargeTraces(int intervalTime){
		String querySql = "SELECT traceCode, userCode FROM t_recharge_trace WHERE traceState = 'A' AND traceDateTime BETWEEN DATE_FORMAT(DATE_SUB(now(),INTERVAL 30 MINUTE), '%Y%m%d%H%i%s') AND DATE_FORMAT(DATE_SUB(now(),INTERVAL " + intervalTime + " MINUTE), '%Y%m%d%H%i%s')";
		List<Object[]> lstRechargeTraces = Db.query(querySql);
		return lstRechargeTraces ;
	}
	
	/**
	 * 查询间隔时间内未处理的提现流水
	 * @param intervalTime	间隔时间，单位：分钟
	 * @return
	 */
	private List<WithdrawTrace> getWithdrawTraces(int intervalTime) {
		String querySql = "SELECT * FROM t_withdraw_trace WHERE `status` = 2 AND createDateTime BETWEEN DATE_FORMAT(DATE_SUB(now(),INTERVAL 30 MINUTE), '%Y%m%d%H%i%s') AND DATE_FORMAT(DATE_SUB(now(),INTERVAL " + intervalTime + " MINUTE), '%Y%m%d%H%i%s')";
		List<WithdrawTrace> lstWithdrawTraces = WithdrawTrace.withdrawTraceDao.find(querySql);
		return lstWithdrawTraces;
	}
}
