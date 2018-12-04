package com.dutiantech.controller.pay;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jx.http.WebUtils;
import com.jx.service.JXService;

import net.sf.json.JSONObject;

/**
 * 江西银行存管交易类
 *
 */
public class JXPayController extends BaseController{
	
//	private UserInfoService userInfoService = getService(UserInfoService.class);
	private UserService userService = getService(UserService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
//	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private JXTraceService jXTraceService = getService(JXTraceService.class);
	private JXController jxController = new JXController();
	private BanksService banksV2Service = getService(BanksService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	
	private static final Logger jxPayController = Logger.getLogger("jxPayController");
	static{
		LoggerUtil.initLogger("jxPayController", jxPayController);
	}
	
	/**
	 * 提现冲正通知
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/withdrawReverseNotify")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void withdrawReverseNotify(){
		HttpServletRequest request = getRequest();
		
		String bgData = request.getParameter("bgData");
		Map<String, String> resMap = JSONObject.fromObject(bgData);
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//原交易流水
		String orgJxTraceCode = "" + resMap.get("orgTxDate") + resMap.get("orgTxTime") + resMap.get("orgSeqNo");
		Long x = Db.queryLong("select count(1) from t_jx_trace where jxTraceCode=?",jxTraceCode);
		if(x<1){
			JXService.addJxTraceResponse(jxTraceCode, resMap, JSON.toJSONString(resMap).replace(",", ",\r\n"));
		}
		WithdrawTrace withdrawTrace = WithdrawTrace.withdrawTraceDao.findFirst("select * from t_withdraw_trace where bankTraceCode=? ", orgJxTraceCode);
		String userCode = withdrawTrace.getStr("userCode");
		//User user = userService.findById(userCode);
		//String jxAccountId = user.getStr("jxAccountId");
		
		long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
		long sxf = (long)withdrawTrace.getInt("sxf");
		
		long txAmount = StringUtil.getMoneyCent(resMap.get("txAmount"));
		
		//检查提现金额是否相符
		if((withdrawAmount - sxf) != txAmount){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_交易金额不符", null);
			jxPayController.log(Level.INFO, String.format("[%s]提现冲正_交易金额不符_原流水号【%s】", userCode, orgJxTraceCode));
			return ;
		}
		
		if("999033".equals(resMap.get("retCode"))){
			//记录冲正流水
			withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "6");
			withdrawTrace.set("withdrawRemark", "提现冲正");
			if(withdrawTrace.update()){//冲正流水记录成功
				//回退用户资金
				boolean result = fundsServiceV2.withdrawRevers(userCode, withdrawAmount, "提现失败_资金回退");
				if (result) {//资金修改成功
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_同步修改资金" + StringUtil.getMoneyYuan(withdrawAmount));
					jxPayController.log(Level.INFO, String.format("[%s]提现冲正_同步修改资金", userCode));
				} else {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_修改资金失败 ", null);
					jxPayController.log(Level.INFO, String.format("[%s]提现冲正_修改资金失败", userCode));
					return ;
				}
				User user = userService.findById(userCode);
				if (null != user && user.getInt("vipLevel") < 8) {	// 黑钻以下会员，回退免提提现次数或积分
					String useFree = withdrawTrace.getStr("useFree");
					String isScore = withdrawTrace.getStr("isScore");
					if ("y".equals(useFree)) {
						withdrawFreeService.setFreeCount(userCode, -1);
						jxPayController.log(Level.INFO, String.format("[%s]提现冲正_回退免费提现次数", userCode));
					} else if ("1".equals(isScore)) {
						fundsServiceV2.doPoints(userCode, 0, 20000, "回退提现抵扣积分");
						jxPayController.log(Level.INFO, String.format("[%s]提现冲正_回退提现抵扣积分", userCode));
					}
				}
			}
		}else{
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_未知结果 ", null);
			jxPayController.log(Level.INFO, String.format("[%s]提现冲正：retCode_[%s],retMsg_[%s]", userCode, resMap.get("retCode"), resMap.get("retMsg")));
		}
		
		renderText("success");
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
	 * 红包发放 WJW
	 * @return
	 */
	@ActionKey("/voucherPay")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message voucherPay(){
		String txAmount = getPara("txAmount");
		String mobile = getPara("mobile");
		
		if(StringUtil.isBlank(txAmount)){
			return error("01", "发放金额为空", null);
		}
		
		if(StringUtil.isBlank(mobile) || mobile.length() != 11){
			return error("02", "手机号输入错误", null);
		}
		
		User user = userService.findByMobile(mobile);
		
		if(user == null){
			return error("03", "用户不存在", null);
		}
		
		String forAccountId = user.getStr("jxAccountId");
		
		if(StringUtil.isBlank(forAccountId)){
			return error("04", "用户未开通江西银行存管", null);
		}
		
		Map<String, String> voucherPay = JXController.voucherPay(JXService.RED_ENVELOPES, StringUtil.getMoneyCent(txAmount), forAccountId,
				"0", "");
		boolean flag = "00000000".equals(voucherPay.get("retCode"));
		if(flag){
			fundsServiceV2.doAvBalance4biz(user.getStr("userCode"), StringUtil.getMoneyCent(txAmount) , 0 ,  traceType.Voucher , fundsType.J , "红包发放");
		}
		
		return succ("01", flag?"成功":"失败");
	}
	
	/**
	 * 红包发送撤销 WJW
	 * @return
	 */
	@ActionKey("/voucherPayCancel")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message voucherPayCancel(){
		String txAmount = getPara("txAmount");
		String forAccountId = getPara("forAccountId");
		String jxTraceCode = getPara("jxTraceCode");
		
		if(StringUtil.isBlank(txAmount)){
			return error("01", "发放金额为空", null);
		}
		
		if(StringUtil.isBlank(forAccountId) || forAccountId.length() != 19){
			return error("02", "电子账号输入错误", null);
		}
		
		if(StringUtil.isBlank(jxTraceCode) || jxTraceCode.length() != 20){
			return error("03", "jxTraceCode不正确", null);
		}
		Map<String, String> voucherPayCancel = JXController.voucherPayCancel(JXService.RED_ENVELOPES, StringUtil.getMoneyCent(txAmount), forAccountId, jxTraceCode.substring(0, 8), jxTraceCode.substring(8,14), jxTraceCode.substring(14));
		
		boolean flag = "00000000".equals(voucherPayCancel.get("retCode"));
		User user = userService.findByJXAccountId(forAccountId);
		if(user == null){
			return error("04", "用户不存在", null);
		}
		if(flag){
			fundsServiceV2.doAvBalance4biz(user.getStr("userCode"), StringUtil.getMoneyCent(txAmount) , 0 ,  traceType.VoucherRollback , fundsType.D , "红包发放撤销");
		}
		
		return succ("01", flag?"成功":"失败");
	}
	
	/**
	 * 红包发送隔日撤销 WJW
	 * @return
	 */
	@ActionKey("/voucherPayDelayCancel")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message voucherPayDelayCancel(){
		String txAmount = getPara("txAmount");
		String forAccountId = getPara("forAccountId");
		String jxTraceCode = getPara("jxTraceCode");
		
		if(StringUtil.isBlank(txAmount)){
			return error("01", "发放金额为空", null);
		}
		
		if(StringUtil.isBlank(forAccountId) || forAccountId.length() != 19){
			return error("02", "电子账号输入错误", null);
		}
		
		if(StringUtil.isBlank(jxTraceCode) || jxTraceCode.length() != 20){
			return error("03", "jxTraceCode不正确", null);
		}
		Map<String, String> voucherPayCancel = JXController.voucherPayDelayCancel(JXService.RED_ENVELOPES, StringUtil.getMoneyCent(txAmount), forAccountId, jxTraceCode.substring(0, 8), jxTraceCode.substring(8,14), jxTraceCode.substring(14),"0","");
		
		boolean flag = "00000000".equals(voucherPayCancel.get("retCode"));
		User user = userService.findByJXAccountId(forAccountId);
		if(user == null){
			return error("04", "用户不存在", null);
		}
		if(flag){
			fundsServiceV2.doAvBalance4biz(user.getStr("userCode"), StringUtil.getMoneyCent(txAmount) , 0 ,  traceType.VoucherRollback , fundsType.D , "红包发放隔日撤销");
		}
		
		return succ("01", flag?"成功":"失败");
	}
	
	
	/**
	 * 代偿冻结 WJW
	 * @return
	 */
	@ActionKey("/refinanceFreezePage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message refinanceFreezePage(){
		String txAmount = getPara("txAmount");//冻结金额
		if(StringUtil.isBlank(txAmount)){
			return error("01", "冻结金额为空", "");
		}
		if(!StringUtil.isNumeric(txAmount)){
			return error("02", "请输入数字", "");
		}
		String productId = getPara("productId","");//冻结标号
		return jxController.refinanceFreezePage(JXService.RISK_RESERVE, txAmount, productId, getResponse());
	}
	
	/**
	 * 代偿冻结回调 WJW
	 * @return
	 */
	@ActionKey("/refinanceFreezePageCallback")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void refinanceFreezePageCallback(){
		String bgData = getRequest().getParameter("bgData");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		JSONObject fromObject = JSONObject.fromObject(bgData);
		String txDate = fromObject.getString("txDate");//交易日期
		String txTime = fromObject.getString("txTime");//交易时间
		String seqNo = fromObject.getString("seqNo");//交易流水号
		String jxTraceCode = txDate + txTime + seqNo;//t_jx_trace流水号
		
		jXTraceService.updateResponseMessage(bgData, jxTraceCode);
		
	}
	
	/**
	 * 还款授权(页面调用) WJW
	 * modified 20180912
	 * @return
	 */
	@ActionKey("/repayAuthPage")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void repayAuthPage() {
		String maxAmt = getPara("maxAmt","1350000");// 签约最大金额,单位：元，最多两位小数
		String deadline = getPara("deadline",DateUtil.updateDate(new Date(), 5, Calendar.YEAR, "yyyyMMdd"));// 签约到期日
		String userCode = getPara("userCode");// 用户编号
		if(StringUtil.isBlank(userCode)){
			userCode = getUserCode();
		}

		if (StringUtil.isBlank(maxAmt)) {
			WebUtils.writePromptHtml("签约最大金额为空", "/C01", "UTF-8",getResponse());
			return;
		}
		if (StringUtil.isBlank(deadline)) {
			WebUtils.writePromptHtml("签约到期日为空", "/C01", "UTF-8",getResponse());
			return;
		}
		if (StringUtil.isBlank(userCode)) {
			WebUtils.writePromptHtml("请重新登录", "/C01", "UTF-8",getResponse());
			return;
		}
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		if (user == null) {
			WebUtils.writePromptHtml("用户不存在", "/C01", "UTF-8",getResponse());
			return;
		}
		if(userInfo == null){
			WebUtils.writePromptHtml("用户认证信息异常", "/C01", "UTF-8",getResponse());
			return;
		}
		String jxAccountId = user.getStr("jxAccountId");// 用户银行存管电子账号
		if (StringUtil.isBlank(jxAccountId)) {
			WebUtils.writePromptHtml("请先开通江西银行存管", "/C01", "UTF-8",getResponse());
			return;
		}
		String name = userInfo.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			WebUtils.writePromptHtml("身份证号解析异常", "/C01", "UTF-8",getResponse());
			return;
		}
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String retUrl=CommonUtil.ADDRESS+"/C01";
		String notifyUrl=CommonUtil.CALLBACK_URL + "/repayAuthPageCallback";
		//调用多合一合规授权接口
		JXController.termsAuthPage(jxAccountId, name, idNo, "2", "", "", "", "1", "", "", "", "", "", "", maxAmt, deadline, forgotPwdUrl, retUrl, notifyUrl, getResponse(),"");
	}
	/**
	 * 还款授权回调 WJW
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/repayAuthPageCallback")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class })
	public void repayAuthPageCallback(){
		String bgData = getRequest().getParameter("bgData");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		
		Map<String, String> map = JSONObject.fromObject(bgData);
		// 生成本地报文流水号
		String jxCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		renderText("success");
	}
	
	/**
	 * 产品还款授权解约 WJW true:成功 false:失败
	 */
	@ActionKey("/repayAuthCancel")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void repayAuthCancel() {
		String userCode = getPara("userCode");// 用户编号
		Message message = new Message();
		if (StringUtil.isBlank(userCode)) {
			message = error("01", "userCode为空", null);
			renderJson(message);
			return;
		}
		User user = userService.findById(userCode);
		if (user == null) {
			message = error("02", "user未找到", userCode);
			renderJson(message);
			return;
		}
		UserInfo userInfo = userInfoService.findById(userCode);
		if (userInfo == null) {
			message = error("02", "用户认证信息异常", userCode);
			renderJson(message);
			return;
		}
		String jxAccountId = user.getStr("jxAccountId");// 用户银行存管电子账号
		if (StringUtil.isBlank(jxAccountId)) {
			message = error("03", "未开通江西银行存管", null);
			renderJson(message);
			return;
		}
		Map<String, String> termsAuthQuery = JXQueryController.termsAuthQuery(jxAccountId);// 客户授权功能查询
		if (termsAuthQuery == null) {
			message = error("04", "客户授权功能查询失败", null);
			renderJson(message);
			return;
		}
		String repayMaxAmt = termsAuthQuery.get("repayMaxAmt");// 还款授权签约最大金额
		if (StringUtil.isBlank(repayMaxAmt)) {
			message = error("05", "还款授权签约最大金额查询失败", null);
			renderJson(message);
			return;
		}
		String name = userInfo.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			message = error("06", "身份证号解析异常", null);
			renderJson(message);
			return;
		}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("repayAuthCancel_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("repayAuthCancel_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			message = succ("success", verifyOrderId);
			renderJson(message);
			return;
		}
		Memcached.delete("repayAuthCancel_"+verifyOrderId+userCode);
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String retUrl=CommonUtil.NIUX_URL+"/main";
		String notifyUrl=CommonUtil.CALLBACK_URL + "/repayAuthPageCallback";
		//调用江西银行取消还款授权签约接口
		JXController.termsAuthPage(jxAccountId, name, idNo, "2", "", "", "", "0", "", "", "", "", "", "", "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(),"");
		renderNull();
	}

	/**
	 * 产品缴费授权解约 WJW true:成功 false:失败
	 */
	@ActionKey("/paymentAuthCancel")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void paymentAuthCancel() {
		Message message = new Message();
		String userCode = getPara("userCode");// 用户编号
		if (StringUtil.isBlank(userCode)) {
			message = error("01", "userCode为空", null);
			renderJson(message);
			return;
		}
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		if (user == null) {
			message = error("02", "user未找到", userCode);
			renderJson(message);
			return;
		}
		if(userInfo == null){
			message = error("07", "用户认证信息异常", null);
		}
		String jxAccountId = user.getStr("jxAccountId");// 用户银行存管电子账号
		if (StringUtil.isBlank(jxAccountId)) {
			message = error("03", "未开通江西银行存管", null);
			renderJson(message);
			return;
		}
		Map<String, String> termsAuthQuery = JXQueryController.termsAuthQuery(jxAccountId);// 客户授权功能查询
		if (termsAuthQuery == null) {
			message = error("04", "客户授权功能查询失败", null);
			renderJson(message);
			return;
		}
		String paymentMaxAmt = termsAuthQuery.get("paymentMaxAmt");// 缴费授权签约最高金额
		if (StringUtil.isBlank(paymentMaxAmt)) {
			message = error("05", "缴费授权签约最高金额查询失败", null);
			renderJson(message);
			return;
		}
		//已关闭缴费授权的给出提示
		if("0".equals(termsAuthQuery.get("paymentAuth"))){
			message = error("06", "缴费授权已取消签约", null);
			renderJson(message);
			return;
		}
		String name = userInfo.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			message = error("07", "身份证号解析异常", null);
			renderJson(message);
			return;
		}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("paymentAuthCancel_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("paymentAuthCancel_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			message = succ("success", verifyOrderId);
			renderJson(message);
			return;
		}
		Memcached.delete("paymentAuthCancel_"+verifyOrderId+userCode);
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String retUrl=CommonUtil.NIUX_URL+"/main";
		String notifyUrl=CommonUtil.CALLBACK_URL + "/paymentAuthPageCallback";
		//调用江西银行取消缴费授权签约接口
		JXController.termsAuthPage(jxAccountId, name, idNo, "1", "", "", "0", "", "", "", "", "", "", "", "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(),"");
		renderNull();
	}
	
	/**
	 * 代付(从备付金账户将资金划拨到平台指定的某几个账户) WJW
	 * @return
	 */
	@ActionKey("/T1001")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message T1001(){
		String mobile = getPara("mobile");
		String txAmount = getPara("txAmount");//代付金额(元)
		if(StringUtil.isBlank(txAmount)){
			return error("01", "发放金额为空", null);
		}
		
		if(StringUtil.isBlank(mobile) || mobile.length() != 11){
			return error("02", "手机号输入错误", null);
		}
		
		User user = userService.findByMobile(mobile);
		
		if(user == null){
			return error("03", "用户不存在", null);
		}
		
		String userCode = user.getStr("userCode");
		
		BanksV2 banksV2 = banksV2Service.findByUserCode(userCode);
		if(banksV2 == null){
			return error("", "用户绑卡信息不存在", null);
		}
		String accName = banksV2.getStr("trueName");
		if(StringUtil.isBlank(accName)){
			return error("", "用户姓名为空", null);
		}
		String cardNo = banksV2.getStr("bankNo");
		if(StringUtil.isBlank(cardNo)){
			return error("", "用户卡号为空", null);
		}
		String orderNo = CommonUtil.genShortUID().substring(4);//商户请求订单编号VL16
		Map<String, String> t1001 = null;
		try{
			t1001 = JXController.T1001(orderNo,cardNo, txAmount, accName);
		} catch (Exception e) {
			Map<String, String> Q9001 = null;
			try {
				Q9001 = JXQueryController.Q9001(orderNo);
				if(!"0000".equals(Q9001.get("retCode"))){
					String retDesc = Q9001.get("retDesc");
					return error("", retDesc, null);
				}
				Object orderDetail = Q9001.get("orderDetail");
				JSONObject orderDetailObject = JSONObject.fromObject(orderDetail.toString());
				if(!"0000".equals(orderDetailObject.getString("retCode"))){
					String retDesc = orderDetailObject.getString("retDesc");
					return error("", retDesc, null);
				}
				String orderStatus = orderDetailObject.getString("orderStatus");//0:已接受, 1:处理中,2:处理成功,3:处理失败
				switch (orderStatus) {
				case "0":
					return succ("", "已接受");
				case "1":
					return succ("", "处理中");
				case "2":
					return succ("", "处理成功");
				case "3":
					return succ("", "处理失败");
				default:
					return succ("", "即信又出问题了");
				}
			} catch (Exception e2) {
				return error("", "即信代付查询接口异常", null);
			}
		}
		
		String retCode = t1001.get("retCode");
		if(!"0000".equals(retCode)){
			String retDesc = t1001.get("retDesc");
			return error("", retDesc, null);
		}
		String orderStatus = t1001.get("orderStatus");//0:已接受, 1:处理中,2:处理成功,3:处理失败
		switch (orderStatus) {
		case "0":
			return succ("", "已接受");
		case "1":
			return succ("", "处理中");
		case "2":
			return succ("", "处理成功");
		case "3":
			return succ("", "处理失败");
		default:
			return succ("", "即信又出问题了");
		}
	}

}
