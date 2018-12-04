package com.dutiantech.controller.app;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.VIPService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.AppTransReqData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class FundsController extends BaseController {
	private  UserService userService=getService(UserService.class);
	private  FundsServiceV2 fundsServiceV2=getService(FundsServiceV2.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private VIPService vipService = getService(VIPService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private RechargeTraceService rechargeTraceService=getService(RechargeTraceService.class);
	private FundsTraceService fundsTraceService=getService(FundsTraceService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	/**
	 * 查询用户资金账户信息
	 * @return
	 */
	@ActionKey("/app_queryFunds4User")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public Message queryFunds4user() {
		// TODO 查询用户资金账户信息
		return null;
	}
	
	/**
	 * App提现页面信息
	 * 银行名称+银行卡尾号+银行卡
	 * @return
	 */
	@ActionKey("/app_querywithdraw")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryWithdraw() {
		String userCode=getUserCode();
		User user=userService.findById(userCode);
		Message msg=null;
		if("".equals(user.getStr("loginId"))||null==user.getStr("loginId")){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		BanksV2 banksV2=BanksV2.bankV2Dao.findById(userCode);
		if(null==banksV2){
			msg=error("02", "未绑定银行卡", null);
		}else{
			Map<String,Object> result = new HashMap<String, Object>();
			String bankName=banksV2.getStr("bankName");
			String bankNo=banksV2.getStr("bankNo");
			int ll=bankNo.length();
			String banknolast4=bankNo.substring(ll-4, ll);
			Funds funds=fundsServiceV2.findById(userCode);
			String avbalance=Number.longToString(funds.getLong("avBalance"));
			String points=Number.longToString(funds.getLong("points"));//可用积分
			String leftfree="";
			//免费提现次数
			int x1 = withdrawFreeService.findFreeCountByUserCode(userCode);//已免费提现次数
			int x2 = vipService.findUserVipLevelByUserCode(userCode);//vip免费次数
			if(x1<0){x1=0;}//userFree
			x2=VipV2.getVipByLevel(x2).getVipTxCount();//vipFree
			if(-1==x2){
				leftfree="不限";
			}else{
				int x3=x2-x1;
				if(x3<=0){
					leftfree="0";
				}else if(x3>0){
					leftfree=String.valueOf(x3);
				}
			}
			result.put("bankName", bankName);//银行名
			result.put("avbalance", avbalance);//余额
			result.put("banknolast4", banknolast4);//卡号后4位
			result.put("points", points);//可用积分
			result.put("leftfree", leftfree);//剩余提现免费次数
			result.put("charges", "2.00");//手续费
			result.put("integral", "200.00");//需要积分
			Long totalVest=funds.getLong("beRecyPrincipal")+funds.getLong("reciedPrincipal")+funds.getLong("reciedInterest");
			Long noChargeMoney = totalVest -funds.getLong("totalWithdraw");					
			if(  noChargeMoney < 0  ){						
				noChargeMoney = (long) 0;						
			}else{
				 if( noChargeMoney >= funds.getLong("avBalance")  ){
					 noChargeMoney = funds.getLong("avBalance");
				 }
			}
			result.put("noChargeMoney", Number.longToString(noChargeMoney));//无服务费可提现额度
			msg=succ("withdraw", result);
		}
		renderJson(msg);
	}
	/**
	 * App点击提现判断
	 * */
	@ActionKey("/app_checkwithdraw")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppCheckWithdraw() {
		Message msg=null;
		String userCode = getUserCode();
		String login_id = User.userDao.findByIdLoadColumns(userCode,"loginId").getStr("loginId");
		if(null==login_id||"".equals(login_id)){
			msg= error("02", "还未激活存管", null);
			renderJson(msg);
			return;
		}
		User user = User.userDao.findById(userCode);
		long amount = 0;
		double am=Double.parseDouble(getPara("amount"))*100;
		amount =new Double(am).longValue();
		String useFree = getPara("useFree");// 如果使用免费提现，验证免费提现次数
		String isScore = getPara("isScore");//是否使用积分
		try {
			if (amount < 300) {
				msg= error("03", "提现金额不能小于3元", null);
				renderJson(msg);
				return;
			}
		} catch (Exception e) {
			msg= error("05", "请输出正确提现金额", null);
			renderJson(msg);
			return;
		}
		// 限制首次提现金额
		Page<WithdrawTrace> withdrawTrace = withdrawTraceService.findByPage(
						userCode, 1, 10, null, null, null, null);
		if (null == withdrawTrace || withdrawTrace.getTotalRow() <= 0) {
			if (amount < 5000) {
				msg= error("03", "首次提现金额不能小于50元", null);
				renderJson(msg);
				return;
			}
		}
		try {
			// 获取银行卡信息
			BanksV2 bank = BanksV2.bankV2Dao.findById(userCode);

			// 验证卡号是否正确
			if (bank == null) {
				// 记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，卡号不存在");
				msg= error("07", "卡号不存在", null);
				renderJson(msg);
				return;
			}

			// 验证银行卡所在地信息是否完全
			try {
				if (StringUtil.isBlank(bank.getStr("cardCity"))){
					msg= error("99", "银行卡省市信息不全", null);
					renderJson(msg);
					return;
					}
			} catch (Exception e) {
				msg= error("99", "银行卡省市信息不全", null);
				renderJson(msg);
				return;
			}

			// 判断账户可用资金是否足够提现
			Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
			//验证是否开通存管 ws 20170818
			//用户资金验证
			QueryBalanceResultData fuiouFunds =	fuiouTraceService.BalanceFunds(user);
			if (funds.getLong("avBalance") != Long.parseLong(fuiouFunds.getCa_balance()) || 
					funds.getLong("frozeBalance") != Long.parseLong(fuiouFunds.getCf_balance())) {
				msg= error("20", "用户资金异常", null);
				renderJson(msg);
				return;
			}
			//end
			long avBalance = funds.getLong("avBalance");
			if (null == funds || avBalance < amount) {
				// 记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户余额不足");
				msg= error("08", "账户余额不足", null);
				renderJson(msg);
				return;
			}
			if (useFree.equals("y")) {
				int useFreeCount = withdrawFreeService.findFreeCountByUserCode(userCode);// 已经免费提现的次数
				int userVipLevel = user.getInt("vipLevel");
				VipV2 vip = VipV2.getVipByLevel(userVipLevel);
				int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411",
						DateUtil.getNowDate());
				if (x > 0) {
					msg= error("03", "新会员免费提现制度2016年4月11后生效", false);
					renderJson(msg);
					return;
				}
				if (vip.getVipTxCount() != -1) {
					if (useFreeCount >= vip.getVipTxCount()) {
						msg= error("117", "您已免费提现" + useFreeCount
								+ "次，您的会员级别目前最多可免费提现" + vip.getVipTxCount()
								+ "次", false);
						renderJson(msg);
						return;
					}
				}
			}
			if ("1".equals(isScore) && useFree.equals("y")) {
				msg= error("118", "积分抵扣、免费提现只能选择单独一种方式", false);
				renderJson(msg);
				return;
			}
			// 判断积分是否足够
			if ("1".equals(isScore) && funds.getLong("points") < 20000) {
				// 记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户积分不足");
				msg= error("09", "账户积分不足", null);
				renderJson(msg);
				return;
			}}catch (Exception e) {
				e.printStackTrace();
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
				msg= error("06", "提现异常06", null);
				renderJson(msg);
				return;
			}
		// 记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现日志");
		msg= succ("申请提现成功", null);
		renderJson(msg);
	}
	
	/**
	 * App提现
	 * @return
	 */
	@ActionKey("/app_withdraw")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppWithdraw() {
		String userCode=getUserCode();
		// 获取参数并验证
		long amount = 0;
		double am=Double.parseDouble(getPara("amount"))*100;
		amount =new Double(am).longValue();
		String useFree = getPara("useFree");// 如果使用免费提现，验证免费提现次数
		String isScore = getPara("isScore");//是否使用积分

		try {
			Funds funds=fundsServiceV2.getFundsByUserCode(userCode);
			BanksV2 bank=BanksV2.bankV2Dao.findById(userCode);
			UserInfo userInfo = userInfoService.findById(userCode);
			String withdrawCode = CommonUtil.genMchntSsn();
			// 新增提现申请记录
			boolean result = false;
			result = withdrawTraceService.save(withdrawCode, userCode,
					funds.getStr("userName"), userInfo.getStr("userCardName"),
					bank.getStr("bankNo"), bank.getStr("bankNo"),
					bank.getStr("bankType"), bank.getStr("bankName"),
					bank.getStr("cardCity"), amount, "2", isScore, "用户申请提现", "",
					useFree, false);
			if (result == false) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS,
						"用户申请提现失败，提现异常-07", null);
			} else {
				// 记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ："
						+ amount / 10.0 / 10.0);				
			}
			//  计算提现手续费
			long fees = withdrawTraceService.calculateSysFee(userCode, amount, isScore, useFree);
			long realAmt = amount - fees;
			AppTransReqData appTransReqData = new AppTransReqData();
			String login_id = User.userDao.findByIdLoadColumns(userCode,"loginId").getStr("loginId");
			try {
				login_id = CommonUtil.decryptUserMobile(login_id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			appTransReqData.setAmt(String.valueOf(realAmt));
			appTransReqData.setLogin_id(login_id);
			appTransReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
			appTransReqData.setMchnt_txn_ssn(withdrawCode);
			appTransReqData.setPage_notify_url(CommonUtil.ADDRESS
					+ "/app_withdrawPageNotify");
			//商户APP个人用户免登录提现
			FuiouService.app500003(appTransReqData, getResponse());
			renderNull();
		} catch (Exception e) {
			e.printStackTrace();
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
		}
	}

	/**
	 * App提现通知接口
	 * 
	 * @return
	 */
	@ActionKey("/app_withdrawPageNotify")
	@AuthNum(value = 999)
	@Before({ AppInterceptor.class, PkMsgInterceptor.class })
	public void AppWithdrawPageNotify() {
		try {
			String resp_code = getPara("resp_code");
			String amt = getPara("amt");
			String mchnt_cd = getPara("mchnt_cd");
			String mchnt_txn_ssn = getPara("mchnt_txn_ssn");
			
			WithdrawTrace withdrawTrace = withdrawTraceService
					.findById(mchnt_txn_ssn);
			if (withdrawTrace == null) {
				redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			String userCode = withdrawTrace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
				redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			long sxf = (long) withdrawTrace.getInt("sxf");			
			long amount = Long.parseLong(amt);
			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
			if (amt == null || !(withdrawAmount == amount + sxf )) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_提现金额不符", null);
				redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
			}
			
			//如果交易状态已更新，直接返回成功响应
			if ("3".equals(withdrawTrace.getStr("status"))) {
				redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=getsucc", true);
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
					redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
				}
				
				//  修改资金账户可用余额
				boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
						userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
				if (withdrawals4Fuiou == false) {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现修改资金失败 ", null);
					redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
				} else {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "APP提现同步修改资金" + withdrawAmount);
					redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=getsucc", true);
					}
				} else {
					withdrawTrace.set("status", "4");
					withdrawTrace.set("withdrawRemark", "提现失败");
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败" + resp_code, null);
					if (withdrawTrace.update()) {
						redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
						return ;
					}
					redirect(CommonUtil.ADDRESS + "/ispaysuccforapp?type=geterro&err=" + resp_code, true);
					return ;
				}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * app充值信息查询
	 * */
	@ActionKey("app_queryRecharge")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryRecharge(){
		String userCode=getUserCode();
		User user=userService.findById(userCode);
		Message msg=null;
		if("".equals(user.getStr("loginId"))||null==user.getStr("loginId")){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		BanksV2 banksV2=BanksV2.bankV2Dao.findById(userCode);
		if(null==banksV2){
			msg=error("02", "未绑定银行卡", null);
		}else{
			Map<String,Object> resultMap = new HashMap<String, Object>();
			String bankName=banksV2.getStr("bankName");
			String bankNo=banksV2.getStr("bankNo");
			String banknolast4=bankNo.substring(bankNo.length()-4, bankNo.length());
			resultMap.put("bankName", bankName);
			resultMap.put("bankNo", banknolast4);//银行卡后四位
			msg=succ("查询成功", resultMap);
		}
		renderJson(msg);
		return;
	}
	/**
	 * app充值
	 * rain2017.7.24
	 * */
	@ActionKey("/app_recharge") // 快捷充值
	@AuthNum(value = 999)
	@Before({ AppInterceptor.class, PkMsgInterceptor.class })
	public void appRecharge(){
		Message message=null;
		long amount = 0;
		double am=Double.parseDouble(getPara("amount"))*100;
		amount =new Double(am).longValue();
		try {
			
			if (amount <= 0) {
				message= error("01", "参数异常", amount);
				renderJson(message);
				return;
			}
			if (amount < 10000) {
				message= error("777", "最低充值金额100元", amount);
				renderJson(message);
				return;
			}
		} catch (Exception e) {
			message= error("01", "参数异常," + e.getMessage(), null);
			renderJson(message);
			return;
		}
		String userCode = getUserCode();
		String login_id = User.userDao.findByIdLoadColumns(userCode, "loginId").getStr("loginId");
		if (login_id == null || "".equals(login_id)) {
			message=error("02", "未激活存管账号,请到用户管理页面激活", null);
			renderJson(message);
			return;
		}
		BanksV2 bankV2 = BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where userCode=?", userCode);
		if (bankV2 == null) {
			message=error("02", "未绑定理财卡", null);
			renderJson(message);
			return;
		}
		String cardId = "";
		String bankType = bankV2.getStr("bankType");
		String trueName = bankV2.getStr("trueName");
		String bankNo = bankV2.getStr("bankNo");
		String bankName = bankV2.getStr("bankName");
		try {
			cardId = CommonUtil.decryptUserCardId(bankV2.getStr("cardid"));
		} catch (Exception e) {
		}
		if (StringUtil.isBlank(cardId) == true) {
			message=error("10", "未做实名认证", null);
			renderJson(message);
			return;
		}
		Map<String, String> payParam = initPayInfo();
		payParam.put("user_id", userCode);
		payParam.put("name_goods", "易融恒信用户app充值");
		payParam.put("info_order", "易融恒信app理财充值-快捷充值.");
		payParam.put("money_order", String.valueOf(amount));
		payParam.put("userreq_ip", getRequestIP());
		payParam.put("bank_code", bankType);
		payParam.put("money_order", String.valueOf(amount / 10.00 / 10.00));
		payParam.put("id_type", "0");
		payParam.put("id_no", cardId);
		payParam.put("acct_name", trueName);
		payParam.put("userBankName", bankName);
		payParam.put("card_no", bankNo);
		payParam.put("page_notify_url", CommonUtil.ADDRESS + "/app_fast_showResult4fuiou");
		// 风控信息
				User user = userService.findById(userCode);
				payParam.put("risk_item", bindRiskInfo(user));
				// 订单入库
				RechargeTrace trace = map2trace(payParam);
				trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.PHONE.key());
				trace.set("traceAmount", amount);
				trace.set("bankRemark", "手机支付,发起申请");
				trace.set("traceRemark", "手机支付,发起申请");
				trace.set("userName", user.getStr("userName"));
				trace.set("userBankNo", bankNo);
				AppTransReqData appTransReqData =new AppTransReqData();
				try {
					login_id = CommonUtil.decryptUserMobile(login_id);
				} catch (Exception e) {
					e.printStackTrace();
				}
				appTransReqData.setAmt(String.valueOf(amount));
				appTransReqData.setLogin_id(login_id);
				appTransReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
				appTransReqData.setMchnt_txn_ssn(payParam.get("no_order"));
				appTransReqData.setPage_notify_url(payParam.get("page_notify_url"));
				try {
					FuiouService.app500002(appTransReqData, getResponse());
					renderNull();
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (trace.save()) {
					message= Message.succ("ok","生成充值订单成功");
				} else {
					message=Message.error("14", "生成充值订单失败!", null);
				}
				renderJson(message);
	}
	/**
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
				redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}

			String userCode = trace.getStr("userCode");
			if (mchnt_cd == null || !mchnt_cd.equals(CommonUtil.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_商户号不符", null);
				redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}

			long payAmount = Long.parseLong(amt);
			if (amt == null || payAmount != trace.getLong("traceAmount")) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_充值金额不符", null);
				redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}
			
			// 如果交易状态已更新，直接返回成功页面，针对网银充值有两次同步回调
			if ("B".equals(trace.getStr("traceState"))) {
				redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=paysucc", true);
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
					redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=paysucc", true);
					return ;
				} else {
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
					redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
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
					redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
					return ;
				}
				redirect(CommonUtil.ADDRESS+"/ispaysuccforapp?type=payerro&err=" + resp_code, true);
				return ;
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	private RechargeTrace map2trace(Map<String, String> map) {
		RechargeTrace trace = new RechargeTrace();
		String userCode = String.valueOf(map.get("user_id"));
		UserInfo info = userInfoService.findById(userCode);
		if (info != null) {
			trace.set("userTrueName", info.getStr("userCardName"));
		}
		String tmpBankCode = String.valueOf(map.get("bank_code"));
		trace.set("userBankCode", tmpBankCode);
		trace.set("userBankName", map.get("userBankName"));
		trace.set("traceCode", map.get("no_order"));
		trace.set("bankTraceCode", map.get("no_order"));
		trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key()); // A-已受理
		trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key());
		trace.set("userCode", userCode);
		trace.set("traceDateTime", DateUtil.getNowDateTime());
		trace.set("traceDate", DateUtil.getNowDate());
		trace.set("modifyDateTime", DateUtil.getNowDateTime());
		trace.set("modifyDate", DateUtil.getNowDate());

		return trace;
	}
	/**
	 * 初始化充值信息 商户号，流水号 
	 * @return
	 */
	private Map<String, String> initPayInfo() {
		Map<String, String> payParam = new TreeMap<String, String>();
		String nowDateTime = DateUtil.getNowDateTime();

		payParam.put("version", "1.0");
		payParam.put("charset_name", "UTF-8");
		payParam.put("oid_partner", CommonUtil.MCHNT_CD); // 商户号
		payParam.put("timestamp", nowDateTime);
		payParam.put("sign_type", "RSA");
		payParam.put("busi_partner", "101001");
		payParam.put("no_order", CommonUtil.genMchntSsn()); // 流水号
		payParam.put("dt_order", nowDateTime);
		payParam.put("valid_order", "30"); // 默认30分钟
		payParam.put("pay_type", "1"); // 默认只支持借记卡

		return payParam;
	}
	private String bindRiskInfo(User user) {
		JSONObject params = new JSONObject();
		if (user != null) {
			String userCode = user.getStr("userCode");
			UserInfo uInfo = UserInfo.userInfoDao.findById(userCode);
			params.put("user_info_mercht_userno", userCode);
			String tmpMobile = user.getStr("userMobile");
			try {
				String uMobile = CommonUtil.decryptUserMobile(tmpMobile);
				params.put("user_info_bind_phone", uMobile);
				params.put("user_info_mercht_userlogin", uMobile);
				params.put("user_info_mail", user.getStr("userEmail"));
			} catch (Exception e) {
				params.put("user_info_bind_phone", "0000000");
			}
			params.put("user_info_dt_register", user.getStr("regDate") + user.getStr("regTime"));
			params.put("user_info_dt_register", user.getStr("regIP"));
			params.put("frms_ware_category", "2009");
			params.put("user_info_full_name", uInfo.getStr("userCardName"));
			try {
				String tmpCardId = CommonUtil.decryptUserCardId(uInfo.getStr("userCardId"));
				if (StringUtil.isBlank(tmpCardId) == false) {
					params.put("user_info_id_no", tmpCardId);
					params.put("user_info_identify_state", "1");
					params.put("user_info_identify_type", "3");
				} else {
					params.put("user_info_identify_state", "0");
				}
			} catch (Exception e) {
				params.put("user_info_id_no", "");
			}
		} else {
			throw new BaseBizRunTimeException("11", "无法获取该用户的信息", null);
		}
		return params.toJSONString();
	}
	/**
	 * app充值记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_queryRechargeTrace")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryRechargeTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        
		//获取用户标识
		String userCode = getUserCode();
		
		//获取充值记录
		Map<String, Object> result = rechargeTraceService.appQueryRecharge(
	 			userCode , pageNumber, pageSize,null);
		
		message= succ("充值记录查询成功", result);
		renderJson(message);
		
	}
	/**
	 * app提现记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_queryWithdrawTrace")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryWithdrawTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        
		//获取用户标识
		String userCode = getUserCode();
		
		//获取充值记录
		Map<String, Object> result = withdrawTraceService.appQueryWithdraw(
	 			userCode , pageNumber, pageSize,null);
		
		message= succ("提现记录查询成功", result);
		renderJson(message);
		
	}
	/**
	 * app资金记录
	 * 
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * */
	@ActionKey("/app_QueryFundsTraceList")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void appQuerFundsTrace() {
		 Message message=null;
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
        //获取用户标识
		String userCode = getUserCode();
		Map<String, Object> result=null;
		//获取资金记录
		result = fundsTraceService.appQueryFundsTraceList(pageNumber, pageSize,userCode);
		message= succ("资金记录查询成功", result);
		renderJson(message);
		
	}
	/**
	 * app资金详情
	 * */
	@ActionKey("/app_queryfundsDetail")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryfundsDetail", cachedKeyParm="traceCode|@userCode",mode="remote" , time=2)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryfundsDetail(){
		String traceCode = getPara("traceCode");
	
		Message message=null;
		if(StringUtil.isBlank(traceCode)){
			message= error("01", "资金流水号不合法", null);
			renderJson(message);
			return ;
		}
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize(); 
		Map<String, Object> result=null;
		result = fundsTraceService.appQueryFundsTraceDetail(pageNumber, pageSize,traceCode);
		message= succ("资金详情查询成功", result);
		renderJson(message);
	}
	}
