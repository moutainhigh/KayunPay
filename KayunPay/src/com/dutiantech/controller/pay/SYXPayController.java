package com.dutiantech.controller.pay;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.plugin.syxpay.SYXPayKit;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;

public class SYXPayController extends BaseController {

	private UserService userService = getService(UserService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private WithdrawTraceService withdrawtraceService = getService(WithdrawTraceService.class);

	@ActionKey("/stopSYXPayOut")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void stopSYXPatOut() {
		String pi = getPara("pi", "");
		if (pi.equals("3.1415926535898")) {
			System.out.println(CACHED.get("ST.SYX_PAYOUT_SWITCH"));
			CACHED.put("ST.SYX_PAYOUT_SWITCH", "OFF", false);
			renderText("success");
		}else{
			renderText("faild");
		}
	}
	
	@ActionKey("/startSYXPatOut")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void startSYXPatOut() {
		String pi = getPara("pi", "");
		if (pi.equals("3.1415926535898")) {
			CACHED.put("ST.SYX_PAYOUT_SWITCH", "ON", false);
			renderText("success");
		}else{
			renderText("faild");
		}
	}
	
	@ActionKey("/stopSYXPayIn")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void stopSYXPatIn() {
		String pi = getPara("pi", "");
		if (pi.equals("3.1415926535898")) {
			CACHED.put("ST.SYX_PAYIN_SWITCH", "OFF", false);
			renderText("success");
		}else{
			renderText("faild");
		}
	}
	
	@ActionKey("/startSYXPatIn")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void startSYXPatIn() {
		String pi = getPara("pi", "");
		if (pi.equals("3.1415926535898")) {
			CACHED.put("ST.SYX_PAYIN_SWITCH", "ON", false);
			renderText("success");
		}else{
			renderText("faild");
		}
	}

	@ActionKey("/syxpayout_callback")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void syxpayout_callback() {
		String outOrderId = getPara("outOrderId", "");// 商户订单号
		int tradeStatus = getParaToInt("tradeStatus", 4);// 1 待处理 2代扣成功 4代扣失败 6处理中 8审核拒绝
		
		String errorMessage = getPara("errorMessage", "");//代扣返回000011 超出限额
		
		// 1.先验证订单号是否存在交易
		RechargeTrace trace = RechargeTrace.rechargeTraceDao.findById(outOrderId);
		if (trace == null) {
			renderText("faild");
		} else {
			// 2.已经处理过成功或失败跳过
			if (trace.getStr("traceState").equals("A") && trace.getStr("bankState").equals("A")) {
				int lockOrder = Db.update("update t_recharge_trace set traceState=? where traceCode=? and traceState=? ",BANK_STATE.LOCKED.key(), outOrderId,BANK_STATE.ACCEPT.key());
				if (lockOrder > 0) {
					if (tradeStatus == 2) {
						// 成功
						trace.set("bankState", BANK_STATE.SUCCESS.key());
						trace.set("traceState", TRACE_STATE.SUCCESS.key());
						trace.set("settleDate", DateUtil.getNowDate());
						trace.set("bankRemark", "充值成功" );
						trace.set("traceRemark", "商银信认证支付充值成功");
						trace.set("okDateTime", DateUtil.getNowDateTime());

						try {
							String mobile = userService.getMobile(trace.getStr("userCode"));
							String content = CommonUtil.SMS_MSG_RECHARAGE_ONLINE.replace("[userName]",trace.getStr("userName")).replace("[payAmount]",CommonUtil.yunsuan(trace.getLong("traceAmount")+ "","100.0", "chu", 2).doubleValue()+ "");
							SMSLog smsLog = new SMSLog();
							smsLog.set("mobile", mobile);
							smsLog.set("content", content);
							smsLog.set("userCode", trace.getStr("userCode"));
							smsLog.set("userName", trace.getStr("userName"));
							smsLog.set("type", "12");
							smsLog.set("typeName", "在线充值");
							smsLog.set("status", 9);
							smsLog.set("sendDate", DateUtil.getNowDate());
							smsLog.set("sendDateTime",DateUtil.getNowDateTime());
							smsLog.set("break", "");
							smsLogService.save(smsLog);
						} catch (Exception e) {
							
						}
						// 修改资金账户
						fundsServiceV2.recharge(trace.getStr("userCode"),trace.getLong("traceAmount"), 0,"商银信认证支付-充值完成!", SysEnum.traceType.C.val());
					} else {
						// 失败
						trace.set("bankState", BANK_STATE.FAILD.key());
						trace.set("traceState", TRACE_STATE.FAILD.key());
						try {
							if(errorMessage.split(":").length == 2){
								if(errorMessage.split(":")[0].equals("000011")){//限额
									trace.set("bankState", BANK_STATE.Quota.key());
								}
								errorMessage = errorMessage.split(":")[1];
							}
							
						} catch (Exception e) {
							
						}
						trace.set("bankRemark", "充值失败："+errorMessage);
						trace.set("traceRemark", "商银信认证支付充值失败[" + errorMessage+ "]");
					}
					trace.update();
					renderText("success");
				} else {
					renderText("faild");
				}

			} else {
				renderText("repeat");
			}
		}
	}

	public static void main(String[] args) {
//		String x = CommonUtil.genShortUID();
//		System.out.println(x);
//		 Map<String,String> msg = SYXPayKit.payOut( x ,
//		 "1", 300, "测试代扣", "ICBC" ,"6215593202002289117", "郑贤 " ,
//		 "422201198908181811" , "18717129832" ) ;
		
//		String id = CommonUtil.genShortUID();
//		System.out.println(id);
//		Map<String, String> msg1 = SYXPayKit.payIn(id, 115800000, "中州债权放款", "祁守华",
//				"6214832706338972", "CMB", "湖北", "武汉", "中州债权放款");
//		Map<String, String> msg1 = SYXPayKit.payIn(id, 499900, "退款，原路返回", "胡庆隆",
//				"6212263602074203308", "ICBC", "广东", "广州", "退款，原路返回");
		
//		System.out.println(msg1);
		Map<String, String> msg2 = SYXPayKit.queryPayInOrder("haV2rFFcL8EDlm5Vr9fY");
		
//		Map<String, String> msg2 = SYXPayKit.queryPayOutOrder("tlzR21m83xoOc5ZOPr4I");
		
		System.out.println(JSONObject.toJSONString(msg2));

	}

	// TODO 商银信代付
	public Message daifu(String applyCode, String provinceName,String cityName) {
		
		if(AdminConfig.isDevMode)
			return error("999", "测试环境不允许代付", false);
		WithdrawTrace trace = WithdrawTrace.withdrawTraceDao.findById(applyCode);
		if (trace == null) {
			return error("03", "无此提现申请", false);
		}
		String bankCode = trace.getStr("bankType");
		String bankid = SYXPayKit.LianLian_PayIn_CONVERT_BANK_CODE.get(bankCode);
		if (StringUtil.isBlank(bankid)) {
			Db.update("update t_withdraw_trace set `status` = '0' where withdrawCode = ?",applyCode);
			return error("95", "不支持的银行卡类型", false);
		}

		String status = trace.getStr("status");
		if ("1".equals(status) == false) {
			Db.update("update t_withdraw_trace set `status` = '0' where withdrawCode = ?",applyCode);
			return error("01", "不符合提现条件-" + status, status);
		}

		String cardNo = trace.getStr("bankNo");
		String accName = trace.getStr("userTrueName");
		long rePayAmount = trace.getLong("withdrawAmount");
		int sxf = trace.getInt("sxf");
		rePayAmount = rePayAmount - sxf;
		String infoOrder = "用户提现-" + accName + "(" + trace.getStr("userName")+ ")";
		String outOrderId = CommonUtil.genShortUID();
		Map<String, String> result = null;
		
		try {
			result = SYXPayKit.payIn(outOrderId, rePayAmount,"用户提现", accName, cardNo, bankid, provinceName, cityName,infoOrder);
		} catch (Exception e) {
			Db.update("update t_withdraw_trace set `status` = '0' where withdrawCode = ?",applyCode);
			System.out.println("提现申请失败,商银信网络异常:");
			e.printStackTrace();
			return error("99", "提现申请失败,商银信网络异常", null);
		}
		// 如果status 是04就直接扣除冻结余额  完成提现， 如果00的话就是处理中，等查询轮询去查是否完成提现成功
		boolean issyx = false;
		if (result.get("retCode").equals("0000")) {
			if(result.get("status").equals("04")){
				//提现完成了
				trace.set("status", "3" ) ;
				trace.set("withdrawRemark", "商银信-提现成功,订单号：" + result.get("orderId") ) ;
				trace.set("okDateTime", DateUtil.getNowDateTime());//成功时间
				fundsServiceV2.withdrawals3_ok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getInt("sxf"),"商银信-提现成功,订单号："+ result.get("orderId"),SysEnum.traceType.G);
				issyx = true;
			}else if(result.get("status").equals("00")){
				//处理中
				trace.set("status", "2");
				issyx = true;
			}
			
			if(issyx){
				trace.set("withdrawType", "SYX");
				trace.set("modifyDateTime", DateUtil.getNowDateTime());
				if (trace.update()) {
					try {
						Db.update("update t_withdraw_trace set withdrawCode = ? where withdrawCode = ?",outOrderId, applyCode);
						String mobile = userService.getMobile(trace.getStr("userCode"));
						String content = CommonUtil.SMS_MSG_WITHDRAW_ONLINE.replace("[userName]", trace.getStr("userName")).replace("[payAmount]",CommonUtil.yunsuan(rePayAmount + "","100.00", "chu", 2).doubleValue()+ "").replace("[fee]",CommonUtil.yunsuan(sxf + "", "100.00","chu", 2).doubleValue()+ "");
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", content);
						smsLog.set("userCode", trace.getStr("userCode"));
						smsLog.set("userName", trace.getStr("userName"));
						smsLog.set("type", "16");
						smsLog.set("typeName", "提现审核通过");
						smsLog.set("status", 9);
						smsLog.set("sendDate", DateUtil.getNowDate());
						smsLog.set("sendDateTime", DateUtil.getNowDateTime());
						smsLog.set("break", "");
						smsLogService.save(smsLog);
					} catch (Exception e) {
						e.printStackTrace();
					}
					return succ("提现已受理", true);
				}
			}
			
		} else if(result.get("retCode").equals("1009")){
			Db.update("update t_withdraw_trace set `status` = '0',withdrawType = 'SYS' where withdrawCode = ?",applyCode);
			return error("92", "商银信余额不足", null);
		} else {
			Db.update("update t_withdraw_trace set `status` = '0',withdrawType = 'SYS' where withdrawCode = ?",applyCode);
			//暂不处理,给后台人工处理
			return error("03", String.format("提现处理失败[%s][%s]",result.get("retCode"), result.get("retMsg")), null);
		}
		return error("02", "提现申请失败！", null);
	}
	
	/**
	 * 定时任务，轮询查询提现中的订单，如果成功减掉冻结金额
	 * @return
	 */
	@ActionKey("/syxpayin_task_query")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public Message syxpayin_task_query() {
		String key = getPara("key","");
		if(key.equals("3.14159265358") == false){
			return error("01", "nono", false);
		}
		List<WithdrawTrace> list = WithdrawTrace.withdrawTraceDao.find("select * from t_withdraw_trace where `status` = '2' and withdrawType = 'SYX'");
		for (int i = 0; i < list.size(); i++) {
			WithdrawTrace trace = list.get(i);
			Map<String, String> result = SYXPayKit.queryPayInOrder(trace.getStr("withdrawCode"));
			if(result.get("retCode").equals("0000") && result.get("status").equals("04")){
				String status = "";
				try {
					status = WithdrawTrace.withdrawTraceDao.findById(trace.getStr("withdrawCode")).getStr("status");
					if(StringUtil.isBlank(status))
						status = "";
				} catch (Exception e) {
					status = "";
				}
				if(status.equals("2")){
					trace.set("status", "3");
					trace.set("withdrawRemark", "商银信-提现成功,订单号：" + result.get("orderId") ) ;
					trace.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
					trace.set("okDateTime", DateUtil.getNowDateTime());//成功时间
					fundsServiceV2.withdrawals3_ok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getInt("sxf"),"商银信-提现成功,订单号："+ result.get("orderId"),SysEnum.traceType.G);
					trace.update();
				}
				
			}else if(result.get("retCode").equals("0000") && result.get("status").equals("00")){
				continue;
			}else{
				String status = "";
				try {
					status = WithdrawTrace.withdrawTraceDao.findById(trace.getStr("withdrawCode")).getStr("status");
					if(StringUtil.isBlank(status))
						status = "";
				} catch (Exception e) {
					status = "";
				}
				if(status.equals("2")){
					trace.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
					trace.set("status", "4" ) ;
					trace.set("withdrawRemark", String.format("商银信-提现失败[%s]", result.get("retMsg"))) ;
					
					boolean axs = false;
					try {
						//代付通知失败后，不再恢复账户资金和积分，重新生成一笔待审核的提现流水
						axs = withdrawtraceService.madeNewWithdrawTrace(trace);
					} catch (Exception e) {
						axs = false;
					}
					//如果生成新的待审核提现流水失败，则恢复账户资金和积分
					if(axs == false){
						fundsServiceV2.withdrawals3_buok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getStr("isScore"),"商银信-提现失败,补回提现金额、积分");
						if(trace.getStr("useFree").equals("y")){
							withdrawFreeService.setFreeCount(trace.getStr("userCode"), -1);
						}
					}else{
						trace.set("withdrawRemark", String.format("商银信-提现失败[%s],自动重新提现(待审)", result.get("retMsg"))) ;
						trace.set("status", "Y");
					}
					trace.update();
				}
				
			}
		}
		return succ("ok", "查询商银信代付提现中的订单，处理完成...");
		
	}
}
