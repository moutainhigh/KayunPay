package com.dutiantech.controller.app;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanOverdueService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SMSService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.http.WebUtils;
import com.jx.service.JXService;

public class AppTransferController extends BaseController {
	
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private UserService userService = getService(UserService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private UserInfoService userInfoService=getService(UserInfoService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private SMSService smsService = getService(SMSService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private LoanOverdueService loanOverdueService = getService(LoanOverdueService.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
	/**
	 * 查询债权转让列表
	 * @param pageNumber
	 * @param pageSize
	 */
	@ActionKey("/appQueryCanTransfer")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void appQueryCanTransfer() {
		Message msg = null;
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String minLimit = "1";
		String maxLimit = "24";
		String transState = "A";
		String orderParam = "";
		String orderType = "DESC";

		Page<LoanTransfer> loanTransfers = loanTransferService
				.queryCanTransfer(pageNumber, pageSize, minLimit, maxLimit,
						transState, orderParam, orderType);
		Integer count = loanTransferService.queryTransferCount("A");
		Map<String, Object> map = new HashMap<String, Object>();
		DecimalFormat df = new DecimalFormat("0.00");
		map.put("firstPage", loanTransfers.isFirstPage());
		map.put("lastPage", loanTransfers.isLastPage());
		map.put("pageNumber", loanTransfers.getPageNumber());
		map.put("pageSize", loanTransfers.getPageSize());
		map.put("totalPage", loanTransfers.getTotalPage());
		map.put("totalRow", loanTransfers.getTotalRow());
		
		List<LoanTransfer> list = loanTransfers.getList();
		for (int i = 0; i < list.size(); i++) {
			LoanTransfer loanTransfer = list.get(i);
			String loanNo = loanTransfer.getStr("loanNo");
			String productType = loanTransfer.getStr("productType");
			if ("A".equals(productType)) {
				productType = "质押宝";
			} else if ("B".equals(productType)) {
				productType = "车稳盈";
			} else if ("C".equals(productType)) {
				productType = "房稳赚";
			} else if ("D".equals(productType)) {
				productType = "其它";
			} else if ("G".equals(productType)) {
				productType = "稳定投";
			}
			String loanTitle = productType + loanNo;
			loanTransfer.put("loanTitle", loanTitle);
			int rateByYear = loanTransfer.getInt("rateByYear");
			int rewardByYear = loanTransfer.getInt("rewardRateByYear");
			int newRateByYear = rateByYear + rewardByYear;
			String  rateByYearStr = df.format(newRateByYear/100.00);
//			String transRate = df.format(loanTransfer.getInt("transRate")/100.00) + "%";
			String transRate = "0.00%";
			loanTransfer.put("transRate", transRate);
			loanTransfer.put("rateByYearStr", rateByYearStr);
		}
		map.put("list", list);
		map.put("transferCount", count);

		msg = succ("查询成功", map);
		renderJson(msg);
	}
	
	/**
	 * 查询债转标详细信息
	 * @param transCode
	 * @param userCode
	 * @throws ParseException
	 */
	@ActionKey("/appQueryTransferDetail")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void appQueryTransferDetail() throws ParseException {
		Message msg = null;
		String transCode = getPara("transCode");
		String userCode = getUserCode();
		if (userCode == null) {
			userCode = getPara("userCode");
		}
		if (StringUtil.isBlank(transCode)) {
			msg = error("01", "参数错误", "");
			renderJson(msg);
			return ;
		}

		LoanTransfer loanTransfer = loanTransferService.findById(transCode);
		if(null == loanTransfer){
			msg = error("01", "债权标书获取异常", "");
			renderJson(msg);
			return ;
		}
		LoanInfo loanInfo = loanInfoService.findById(loanTransfer.getStr("loanCode"));
		if(null == loanInfo){
			msg = error("02", "债权标书获取异常", "");
			renderJson(msg);
			return ;
		}
		String effectDate = new SimpleDateFormat("yyyy-MM-dd")
							.format(new SimpleDateFormat("yyyyMMdd")
							.parse(loanInfo.getStr("effectDate")));
		Map<String, Object> tmp = new HashMap<String, Object>();
		DecimalFormat df = new DecimalFormat("0.00");
		//添加债权标来源
		String loanArea = loanInfo.getStr("loanArea");
		String productType = loanInfo.getStr("productType");
		String loanTitle="";
		if("A".equals(productType)){
			loanTitle="质押宝";
		}
		if("B".equals(productType)){
			loanTitle="车稳盈";
		}
		if("C".equals(productType)){
			loanTitle="房稳赚";
		}
		if("G".equals(productType)){
			loanTitle="稳定投";
		}
		loanTitle = "["+loanArea+"]"+loanTitle+loanInfo.getStr("loanNo");
		
		tmp.put("loanTitle", loanTitle);
		tmp.put("transAmount", df.format(loanTransfer.getInt("transAmount")/100.00));
		tmp.put("leftAmount", df.format(loanTransfer.getInt("leftAmount")/100.00));
		tmp.put("loanRecyCount", loanTransfer.getInt("loanRecyCount"));
		tmp.put("cjsy", df.format(loanTransfer.getInt("transFee")/100.00));
		String nextRecyDay = new SimpleDateFormat("yyyy-MM-dd")
							.format(new SimpleDateFormat("yyyyMMdd")
							.parse(loanTransfer.getStr("nextRecyDay")));
		tmp.put("nextRecyDay", nextRecyDay);
		tmp.put("effectDate", effectDate);
		tmp.put("cjsy", df.format(loanTransfer.getInt("transFee")/100.00));
		tmp.put("transferUserName", loanTransfer.getStr("payUserName"));
		
		long avBalance = fundsServiceV2.findAvBalanceById(userCode);
		tmp.put("avBalance", df.format(avBalance/100.00));
		tmp.put("transCode", transCode);
		tmp.put("userCode", userCode);
		
		msg = succ("查询成功", tmp);
		renderJson(msg);
	}
	
	/**
	 * 确认承接债权
	 * 
	 * @param transCode
	 * @param userCode
	 * @param payPwd
	 */
	@ActionKey("/carryOnTransfer4App")
	@AuthNum(value = 999)
	@Before({ AppInterceptor.class, Tx.class, PkMsgInterceptor.class })
	public void carryOnTransfer4App() {
		Message msg = null;
		if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
			msg = error("01", "亲,存管系统正在上线哦", "");
			renderJson(msg);
			return;
		}
		
		
		//限制还款时间不让承接     10点半到11点
//		int exeTime = Integer.parseInt(DateUtil.getNowTime());
//		if(exeTime >= 103000 && exeTime <= 110000){
//			return error("12", "10:30至11:00系统正在还款，请稍后操作。", "");
//		}
		
		String transferCode = getPara("transCode");
		if(StringUtil.isBlank(transferCode)){
			msg = error("01", "获取债权转让编号失败", "");
			renderJson(msg);
			return;
		}
		
		
		//获取用户信息
		String userCode = getUserCode();
		//提前回款资金异常  20180528  Ws
//		boolean unusualUserCode = loanTraceService.unusualUserCode(userCode);
//		if(unusualUserCode){
//			return error("13", "资金异常", "");
//		}
		if(StringUtil.isBlank(userCode)){
			msg = error("02", "请重新登录", "");
			renderJson(msg);
			return;
		}
		User user = userService.findById(userCode);
		
		if(CommonUtil.jxPort){
			String jxAccountId = user.getStr("jxAccountId");//用户电子账号
			if(StringUtil.isBlank(jxAccountId)){
				msg = error("03", "未开通江西银行存管", "");
				renderJson(msg);
				return;
			}
//			JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(jxAccountId);
//			if(paymentAuthPageState == null || !"1".equals(paymentAuthPageState.get("type"))){
//				msg = error("03", "缴费授权未开通,无法承接债权转让", "");
//				renderJson(msg);
//				return;
//			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
				msg = error("03", "缴费授权未开通,无法承接债权转让", "");
				renderJson(msg);
				return;
			}
		}
		
		//验证承接人可用余额是否足够
		long avBalance = fundsServiceV2.findAvBalanceById(userCode);
		//获取转让标信息
		LoanTransfer loanTransfer = loanTransferService.findById(transferCode);
		if(null == loanTransfer){
			msg = error("04", "债权标书获取异常", "");
			renderJson(msg);
			return;
		}
		String productType = loanTransfer.getStr("productType");
		if(SysEnum.productType.E.val().equals(productType)){
			msg = error("04", "易分期无法被承接", "");
			renderJson(msg);
		}
		if(!"A".equals(loanTransfer.getStr("transState"))){
			msg = error("04", "债权标书已被承接或取消", "");
			renderJson(msg);
			return;
		}
		String traceCode = loanTransfer.getStr("traceCode");
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		if(loanTrace == null){
			msg = error("04", "债权获取失败", "");
			renderJson(msg);
			return;
		}
		
		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//避免还款批次已提交,期间债权被承接,导致存管承接额为还款前金额,债权本地已还资金异常
		//债权还款日为T+1  修改规则日 20180815
		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
		if(DateUtil.getNowDate().equals(tmpRecyDate)){
			msg = error("05", "该债权今日还款未完成,完成后即可承接", "");
			renderJson(msg);
			return;
		}
		
		int transAmount = loanTransfer.getInt("transAmount");
		if(avBalance < transAmount){
			msg = error("05", "账户可用余额不足!", "");
			renderJson(msg);
			return;
		}
		
		//验证此标是否有回款中
		String authCode = loanTrace.getStr("authCode");//授权码
		//发生过债转
		if(loanTransferService.vilidateIsTransfer(traceCode)){
			List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
			String transferAuthCode = loanTransfers.get(loanTransfers.size() - 1).getStr("authCode");//最后一债转authCode
			if(!StringUtil.isBlank(transferAuthCode)){
				authCode = transferAuthCode;
			}
		}
		List<JXTrace> jxTraces = jxTraceService.queryTraceByReturnAmountState(DateUtil.getNowDate(), authCode);
		if(jxTraces.size()>0){
			msg = error("05", "该债权正在回款中，暂时无法承接", "");
			renderJson(msg);
			return;
		}
		
		if(userCode.equals(loanTransfer.getStr("payUserCode"))){
			WebUtils.writePromptHtml("不能承接自己发出的债权", "/Z02?navTab=2", "UTF-8",getResponse());
			msg = error("06", "不能承接自己发出的债权", "");
			renderJson(msg);
			return;
		}
		
		if(CommonUtil.fuiouPort){
			msg = error("06", "APP版本过低", "");
			renderJson(msg);
			return;
		}
		
		//转换相关金额存入资金流水备注
		Integer sysFee = loanTransfer.getInt("sysFee") ;//平台手续费
		Integer transFee = loanTransfer.getInt("transFee");//转让人让利金额
//		Integer riskFee = loanTransfer.getInt("riskFee");//风险备用金
//		Integer userFee = loanTransfer.getInt("userFee");//用户额外获得收益
		
		double remark4transFee = new BigDecimal((float)transFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		double remark4sysFee = new BigDecimal((float)sysFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//		double remark4riskFee = new BigDecimal((float)riskFee/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//		double remark4userFee = new BigDecimal((float)(transFee-riskFee)/10.0/10.0).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		
		//递减承接人可用余额
//		String remark = "债权转让支出，让利金额：￥"+remark4transFee+"（用户收益：￥"+remark4userFee+"，风险备用金：￥"+remark4riskFee+"）";
		
		if(CommonUtil.fuiouPort){
//			boolean b = fundsServiceV2.carryOnTransfer(userCode, transAmount,remark);
//			
//			if(b==true){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权扣除可用余额成功  扣除金额  : " + transAmount);
//			}
//			
//			if(b == false){
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权-添加流水失败", null);
//				msg = error("14", "承接债权失败-[添加扣款流水失败]!", "");
//				renderJson(msg);
//				return;
//			}
			msg = error("13", "此版本不支持此功能", "");
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
		User payUser=userService.findById(payUserCode);
		
		if (CommonUtil.jxPort) {// 江西银行存管端口开关
			String accountId = user.getStr("jxAccountId");// 购买方账号
			String forAccountId = payUser.getStr("jxAccountId");// 卖出方账号
			List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
			Map<String,String> banMap=JXQueryController.balanceQuery(accountId);
			if(banMap == null){
				msg = error("05", "网络延时", "");
				renderJson(msg);
				return;
			}
			String availBal = banMap.get("availBal");
			if(StringUtil.isBlank(availBal) || StringUtil.getMoneyCent(availBal) < transAmount){
				msg = error("05", "存管可用余额不足", "");
				renderJson(msg);
				return;
			}
			
			String orgOrderId = "";// 原订单号
			long orgTxAmount = 0;// 原交易金额
			String orgJxTraceCode = "";//原交易流水
			if (loanTransfers == null || loanTransfers.size() < 1) {// 没有成功债转记录,查询投标流水
				orgJxTraceCode = loanTrace.getStr("jxTraceCode");
			} else {// 有成功债转记录
				orgJxTraceCode = loanTransfers.get(loanTransfers.size() - 1).getStr("jxTraceCode");// 获取最后一次成功债转jx流水号
			}
			if(StringUtil.isBlank(orgJxTraceCode)){//迁移标录入订单号
				if(!StringUtil.isBlank(loanTrace.getStr("authCode"))){
					if(StringUtil.isBlank(loanTrace.getStr("orderId"))){
					FileOperate file = new FileOperate();
//					String url = "F://cs//";
					String url = "//home//jx_loanTrace//";
					for(int i = 0;i<5;i++){
						String urlName = url+"3005-BIDRESP-"+(301000+i)+"-20180523";
						String[] text;
						try {
							text = file.readTxtLine(urlName, "GBK");
						} catch (IOException e) {
							msg = error("05", "新存管上线前债权转让失败", "");
							renderJson(msg);
							return;
						}
						for(int j = 0;j<text.length;j++){
							String str = text[j];
							String uid = str.substring(63,79).trim();
							if(uid.equals(loanTrace.getInt("uid").toString())){
								orgOrderId = str.substring(49,79).trim();
								orgTxAmount = Long.parseLong(str.substring(79,92));
								loanTrace.set("orderId", orgOrderId);
								loanTrace.set("orgAmount", orgTxAmount);
								loanTrace.update();
								break;
							}
						}
					}
					}else{
						orgOrderId = loanTrace.getStr("orderId");
						orgTxAmount = loanTrace.getLong("orgAmount");
					}
				}else{
				msg = error("05", "新存管上线前债权暂不支持转让", "");
				renderJson(msg);
				return;
				}
			}else{
			JXTrace jxTrace = jxTraceService.findById(orgJxTraceCode);
			String requestMessage = jxTrace.getStr("requestMessage");//请求报文
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			orgOrderId = parseObject.getString("orderId");
			String txAmount = parseObject.getString("tsfAmount");
			if(StringUtil.isBlank(txAmount)){
				txAmount = parseObject.getString("txAmount");
				if(StringUtil.isBlank(txAmount)){
					msg = error("06", "未找到该债权信息", "");
					renderJson(msg);
					return;
				}
			}
			orgTxAmount = StringUtil.getMoneyCent(txAmount);//原债权金额
			}
			
			String productId = loanTrace.getStr("loanCode");// 标号
			Map<String, String> reqMap = JXService.getHeadReq4App();
			//跳转江西银行页面
			JXappController.creditInvest(reqMap,accountId, transAmount, sysFee + ticket_amount, orgTxAmount,
					forAccountId, orgOrderId, orgTxAmount, productId,transferCode, getResponse());
			renderNull();
			return;
		}else{
			msg = error("16", "网络错误", "");
			renderJson(msg);
			return;
		}
	}
	
	
	/**
	 * 我的债权转让页面---已承接的债权
	 * */
	@ActionKey("/app_querymyloantranfer")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryMyLoanTranfer(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Message msg=null;
		//获取用户标识
		String userCode = getUserCode();
		Page<LoanTransfer> loanTransfers = loanTransferService.queryGotLoanTransfer(pageNumber, pageSize, userCode);
		List<LoanTransfer> list = loanTransfers.getList();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		int aa=list.size();
		for(int i=0;i<aa;i++){
			Map<String,Object> map = new HashMap<String, Object>();
			LoanTransfer loantransfer=list.get(i);
			String productType = loantransfer.getStr("productType");
			String loantitle="";
			if("A".equals(productType)){
				loantitle="质押宝";
			}
			if("B".equals(productType)){
				loantitle="车稳盈";
			}
			if("C".equals(productType)){
				loantitle="房稳赚";
			}
			if("G".equals(productType)){
				loantitle="稳定投";
			}
			loantitle+=loantransfer.getStr("loanNo");
			String transAmount =Number.longToString((long)(loantransfer.getInt("transAmount")));
			double rateByY = (double)(loantransfer.getInt("rateByYear")+loantransfer.getInt("rewardRateByYear"));
			String rateByYear =String.valueOf(rateByY/100);
			String loanRecyCount = String.valueOf(loantransfer.getInt("loanRecyCount"));
			String date =loantransfer.getStr("gotDate");
			date = DateUtil.chenageDay(date);
			map.put("loantitle", loantitle);
			map.put("transAmount", transAmount);
			map.put("rateByYear", rateByYear);
			map.put("loanRecyCount", loanRecyCount);
			map.put("date", date);
			map.put("loanCode", loantransfer.getStr("loanCode"));
			resultList.add(map);
		}
		msg=succ("ok", resultList);
		renderJson(msg);
	}
	
	
	/**
	 * 我的债权转让页面---已发布转让的债权
	 * */
	@ActionKey("/app_havebeenloantranfered")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppHaveBeeenLoanTranfered(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Message msg=null;
		//获取用户标识
		String userCode = getUserCode();
		Page<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransfer(pageNumber, pageSize, userCode);
		List<LoanTransfer> list = loanTransfers.getList();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		int aa=list.size();
		for(int i=0;i<aa;i++){
			Map<String,Object> map = new HashMap<String, Object>();
			LoanTransfer loantransfer=list.get(i);
			String productType = loantransfer.getStr("productType");
			String loantitle="";
			if("A".equals(productType)){
				loantitle="质押宝";
			}
			if("B".equals(productType)){
				loantitle="车稳盈";
			}
			if("C".equals(productType)){
				loantitle="房稳赚";
			}
			if("G".equals(productType)){
				loantitle="稳定投";
			}
			loantitle+=loantransfer.getStr("loanNo");
			String transAmount =Number.longToString((long)(loantransfer.getInt("transAmount")));
			String loanRecyCount = String.valueOf(loantransfer.getInt("loanRecyCount"));
			String date =loantransfer.getStr("transDate");
			date = DateUtil.chenageDay(date);
			String transState = "";
			if (loantransfer.getStr("transState").equals("A") ) {
				transState = "转让中";
			} else if (loantransfer.getStr("transState").equals("B")) {
				transState = "已转让";
			}else if (loantransfer.getStr("transState").equals("C")) {
				transState = "已取消";
			}else if (loantransfer.getStr("transState").equals("D")) {
				transState = "已过期";
			}
			map.put("transState", transState);
			map.put("loantitle", loantitle);
			map.put("transAmount", transAmount);
			map.put("loanRecyCount", loanRecyCount);
			map.put("date", date);
			map.put("loanCode", loantransfer.getStr("loanCode"));
			map.put("transferCode", loantransfer.getStr("transCode"));
			resultList.add(map);
		}
		msg=succ("ok", resultList);
		renderJson(msg);
	}
	
	/**
	 * 我的债权转让页面---可转让的债权
	 * */
	@ActionKey("/app_canbeloantranfered")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppCanBeLoanTranfered(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Message msg=null;
		//获取用户标识
		String userCode = getUserCode();
		Page<LoanTrace> loanTraces = loanTraceService.findCanTransfer(pageNumber, pageSize,userCode);
		List<LoanTrace> list = loanTraces.getList();
		List<Map<String, Object>> resultList = new ArrayList<Map<String,Object>>();
		int aa=list.size();
		for(int i=0;i<aa;i++){
			Map<String,Object> map = new HashMap<String, Object>();
			LoanTrace loanTrace=list.get(i);
			String productType = loanTrace.getStr("productType");
			String loantitle="";
			if("A".equals(productType)){
				loantitle="质押宝";
			}
			if("B".equals(productType)){
				loantitle="车稳盈";
			}
			if("C".equals(productType)){
				loantitle="房稳赚";
			}
			if("G".equals(productType)){
				loantitle="稳定投";
			}
			loantitle+=loanTrace.getStr("loanNo");
			String date =loanTrace.getStr("loanRecyDate");
			date = DateUtil.chenageDay(date);
			long leftAmount = loanTrace.getLong("leftAmount");
			int loanTimeLimit= loanTrace.getInt("loanTimeLimit");
			int loanRecyCount=  loanTrace.getInt("loanRecyCount");
			String duetime=(loanTimeLimit-loanRecyCount+1)+"/"+loanTimeLimit;
			map.put("loantitle", loantitle);
			map.put("date", date);
			map.put("loanCode", loanTrace.getStr("loanCode"));
			map.put("traceCode", loanTrace.getStr("traceCode"));
			map.put("leftAmount", Number.longToString(leftAmount));
			map.put("duetime", duetime);
			resultList.add(map);
		}
		msg=succ("ok", resultList);
		renderJson(msg);
	}
	

	/**
	 * 发布债权转让
	 * */
	@ActionKey("/app_loantranfer")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppLoanTranfer(){
		Message msg = null;
//		if(true){
//			msg = error("01", "债权转让维护中，敬请期待", "");
//			renderJson(msg);
//			return;
//		}
		if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
			msg = error("01", "亲,存管系统正在上线哦", "");
			renderJson(msg);
			return;
		}
		
		String traceCode = getPara("traceCode");
		Integer transFee = 0;
		try{
			transFee = getParaToInt("transFee")*100;
		}catch(Exception e){
			msg = error("03", "请输出正确让利金额", "");
			renderJson(msg);
			return;
		}
		
		//验证
		if(StringUtil.isBlank(traceCode)){
			msg =  error("01", "转让标书编号错误", "");
			renderJson(msg);
			return;
		}
		if(transFee < 0){
			msg =  error("01", "让利金额错误", "");
			renderJson(msg);
			return;
		}
		
		//获取用户信息
		String userCode = getUserCode();
		//提前回款资金异常  20180528  Ws
//		boolean unusualUserCode = loanTraceService.unusualUserCode(userCode);
//		if(unusualUserCode){
//			return error("13", "资金异常", "");
//		}
		User user = User.userDao.findById(userCode);
		if(CommonUtil.fuiouPort){
			msg = error("01", "版本过低无法进行此操作", "");
			renderJson(msg);
			return;
		}
		
		//验证用户是否标的投资者
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(traceCode);
		if(null == loanTrace){
			msg =  error("02", "未找到此债权信息", "");
			renderJson(msg);
			return;
		}
		String productType = loanTrace.getStr("productType");
		if(SysEnum.productType.E.val().equals(productType)){
			msg =  error("02", "易分期不支持债权转让", traceCode);
			renderJson(msg);
			return;
		}
		String loanCode = loanTrace.getStr("loanCode");
		List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", null);
		if(loanOverdues !=null&&loanOverdues.size()>0){
			msg = error("30", "此标跟进中，暂时不能债转", traceCode);
			renderJson(msg);
			return;
		}
		String authCode = loanTrace.getStr("authCode");
		if(StringUtil.isBlank(authCode)){
			msg = error("29", "此标暂时不能转让", traceCode);
			renderJson(msg);
			return;
		}
		
		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//当天为T+1回款日不可发布债权
		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
		if(DateUtil.getNowDate().equals(tmpRecyDate)){
			msg = error("05", "该债权今日还款未完成,暂时无法发布", "");
			renderJson(msg);
			return;
		}
		
		if(CommonUtil.jxPort){
			if(StringUtil.isBlank(user.getStr("jxAccountId"))){
				msg =  error("24", "未激活存管帐号，不能进行此操作", "");
				renderJson(msg);
				return;
			}
//			JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(user.getStr("jxAccountId"));
//			if(paymentAuthPageState == null || !"1".equals(paymentAuthPageState.get("type"))){
//				msg = error("24", "缴费授权未开通", "");
//				renderJson(msg);
//				return;
//			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
				msg = error("24", "缴费授权未开通", "");
				renderJson(msg);
				return;
			}
//			if(StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
//				return error("24", "新存管上线前债权暂不支持转让", "");
//			}
			if(StringUtil.isBlank(loanTrace.getStr("authCode"))){
				msg = error("24", "该债权暂不支持转让", "");
				renderJson(msg);
				return;
			}
		}
		
		try {
			if(loanTrace.getStr("payUserCode").equals(userCode) == false){
				msg = error("02", "只有您投资的标才能转让", traceCode);
				renderJson(msg);
				return;
				}
		} catch (Exception e) {
				msg = error("02", "只有您投资的标才能转让", traceCode);
				renderJson(msg);
				return;
		}
		
		//验证是否已经转让
		String isTransfer = loanTrace.getStr("isTransfer");
		if(isTransfer.equals("A")){
			msg = error("06", "该标书正在转让,请勿重复操作!", "");
			renderJson(msg);
			return;
		}
		
		//检查让利金额 小于让利百分比（配置项中修改）
		int f = 1;
		try{
			f = Integer.parseInt(CACHED.get("ZQ.transFee").toString());
		}catch(Exception e){
		}
		if(transFee > ( loanTrace.getLong("leftAmount") / 10.0/10.0 * f )){
			msg = error("07", "让利金额不能超过投标金额的百分之"+f, "");
			renderJson(msg);
			return;
		}
		long ticket_amount = 0;
		int rewardticketrate=0;//加息券利息
		try {
			/**
			 * TODO 债权转让兼容抵用券
			 */
			String json_tickets = loanTrace.getStr("loanTicket");
			if(StringUtil.isBlank(json_tickets)==false){
				JSONArray ja = JSONArray.parseArray(json_tickets);
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jsonObj = ja.getJSONObject(i);
					if(jsonObj.getString("type").equals("A")){
						//20170519-----20170726新券调整  ws
						Long examount= jsonObj.getLong("examount");
						String isDel=jsonObj.getString("isDel");
						if(null==isDel||"".equals(isDel)){
							if(null==examount||examount>50000){
								ticket_amount = ticket_amount + jsonObj.getLong("amount");
							}
						}else{
							if("Y".equals(isDel)){
								ticket_amount = ticket_amount + jsonObj.getLong("amount");
							}
						}
						//end
					}
					//若第一次债转 获取加息券利息
					if(jsonObj.getString("type").equals("C")){
						if("C".equals(isTransfer)){
						rewardticketrate+=jsonObj.getInteger("rate");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			ticket_amount = 0;
		}
		if(loanTrace.getLong("leftAmount") <= ticket_amount){
			msg = error("187", "此投资使用了现金抵用券，但剩余本金小于抵用券抵扣现金金额", "");
			renderJson(msg);
			return;
		}
		//计算转让积分
		int loanTimeLimit = loanTrace.getInt("loanTimeLimit");
		long amount = loanTrace.getLong("payAmount");
		String refundType = loanTrace.getStr("refundType");
		String effectDate =  loanInfoService.findFieldById(loanTrace.getStr("loanCode"),"effectDate");//审核满标日期
		String lastSettlementDate = CommonUtil.anyRepaymentDate4string(effectDate, loanTimeLimit);//最后一期还款日期
		int allDays = (int) CommonUtil.compareDateTime(lastSettlementDate, effectDate, "yyyyMMdd");
		int leftDays = (int) CommonUtil.compareDateTime(lastSettlementDate, DateUtil.getNowDate(), "yyyyMMdd");
		long allScore = CommonUtil.f_005(amount, loanTimeLimit , refundType) ;
		long tmp = CommonUtil.yunsuan(allScore+"", allDays+"", "chu", 0).longValue();
		int leftScore = (int) CommonUtil.yunsuan(tmp+"", leftDays+"", "cheng", 0).longValue();
		
		//扣除转让人积分
		//fundsServiceV2.doPoints(userCode, 1 , leftScore , "发布转让债权，冻结积分");
		//修改标书债权状态
		boolean updateTraceState = loanTraceService.updateTransferState(loanTrace.getStr("traceCode"), "C", "A");
		if(updateTraceState == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "转让债权时修改投标流水状态修改异常", null);
			msg = error("02", "修改投标流水状态失败", "");
			renderJson(msg);
			return;
		}
		
		//保存债权信息
		//临时修改额外年利率（若用加息券）为transfer保存做准备
		if(rewardticketrate>0){
			int rewardRateByYear=loanTrace.getInt("rewardRateByYear");
			loanTrace.set("rewardRateByYear", rewardRateByYear-rewardticketrate);
		}
		boolean result = loanTransferService.saveLoanTransfer(transFee, leftScore ,loanTrace);
		if(result == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权转让发布失败", null);
			msg = error("02", "债权转让发布失败", "");
			renderJson(msg);
			return;
		}
		
		
		BIZ_LOG_INFO(traceCode, BIZ_TYPE.TRANSFER, "债权转让发布成功");
		
		msg = succ("发布债权成功", "");
		renderJson(msg);
	}
	
	/**
	 * 取消债权转让
	 * */
	@ActionKey("/app_cancelloantranfer")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppCancelLoanTranfer(){
		Message msg=null;
		String transferCode = getPara("transferCode");
		
		if(StringUtil.isBlank(transferCode)){
			msg= error("01", "参数错误", "transferCode : " + transferCode);
			renderJson(msg);
			return;
		}
		
		//获取用户标识
		String userCode = getUserCode();
		
		//验证债权是否存在   是否被转让
		LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst(
				"select traceCode, transState,transScore,refundType from t_loan_transfer where transCode = ? and payUserCode = ? and transState = 'A' ",
				transferCode , userCode);
		if(null == loanTransfer){
			BIZ_LOG_WARN(userCode, BIZ_TYPE.TRANSFER, "债权取消失败，债权未查到或已转让");
			msg= error("02", "债权未查到或已转让", null);
			renderJson(msg);
			return;
		}
		if(loanTransfer.getStr("refundType").equals("E")||loanTransfer.getStr("refundType").equals("F")||loanTransfer.getStr("refundType").equals("H")){
			msg= error("02", "取消失败", "");
			renderJson(msg);
			return;
		}
		List<JXTrace> jxTraces = jxTraceService.queryTranfers(transferCode);
		if(null!=jxTraces&&jxTraces.size()>0){
			for(int i = 0 ;i<jxTraces.size();i++){
				JXTrace jxTrace = jxTraces.get(i);
				String retCode = jxTrace.getStr("retCode");
				if(StringUtil.isBlank(retCode)){
					String txDateTime = jxTrace.getStr("txDate")+jxTrace.getStr("txTime");
					if(i==0&&DateUtil.differentMinuteByMillisecond(txDateTime, DateUtil.getNowDateTime(), "yyyyMMddHHmmss")<=20){
						msg= error("11", "此债权转让订单已提交，等待支付结果中，暂时无法关闭", "");
						renderJson(msg);
						return;
					}
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject jsonObject = JSONObject.parseObject(requestMessage);
					String orgAccountId = jsonObject.getString("accountId");
					String orgOrderId = jsonObject.getString("orderId");
					Map<String,String> map = JXQueryController.creditInvestQuery(orgAccountId, orgOrderId);
					if("00000000".equals(map.get("retCode"))){
						msg= error("13", "此标已债权转让完成,等待银行处理中，无法关闭", "");
						renderJson(msg);
						return;
					}
				}else{
					if("00000000".equals(retCode)){
						msg= error("13", "此标已债权转让完成,等待银行处理中，无法关闭", "");
						renderJson(msg);
						return;
					}
				}
			}
		}
		//取消债权转让
		LoanTransfer cancelLoanTransfer = new LoanTransfer();
		cancelLoanTransfer.set("transCode", transferCode);
		cancelLoanTransfer.set("transState", "C");
		boolean updateResult = false;
		try{
			updateResult = cancelLoanTransfer.update();
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消失败", e);
		}
		
		if( updateResult == false ){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消失败",null);
			msg= error("03", "债权取消失败", null);
			renderJson(msg);
			return;
		}
		//修改标书债权状态
		boolean updateTraceState = false;
		if(loanTransferService.vilidateIsTransfer(loanTransfer.getStr("traceCode"))){
			updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"A", "B");
		}else{
			updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"A", "C");
		}
		if(updateTraceState == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消成功,但标书债权状态修改异常", null);
			msg= error("05", "债权取消成功,但标书债权状态修改异常", null);
			renderJson(msg);
			return;
		}
		
		//回滚积分
		int transScore = loanTransfer.getInt("transScore");
		//fundsServiceV2.doPoints(userCode, 0 , transScore , "取消债权转让,返回冻结积分!");
		
		BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权取消成功");
		
		msg= succ("债权取消成功", null);
		renderJson(msg);
	}
	/**
	 * 债转详细确认页面
	 * */
	@ActionKey("/app_isloantranfer")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppIsLoanTranfer(){
		Message msg=null;
		String traceCode = getPara("traceCode");
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(traceCode);
		String productType = loanTrace.getStr("productType");
		String loantitle="";
		if("A".equals(productType)){
			loantitle="质押宝";
		}
		if("B".equals(productType)){
			loantitle="车稳盈";
		}
		if("C".equals(productType)){
			loantitle="房稳赚";
		}
		if("G".equals(productType)){
			loantitle="稳定投";
		}
		loantitle+=loanTrace.getStr("loanNo");
		long leftAmount = loanTrace.getLong("leftAmount");
		int loanTimeLimit= loanTrace.getInt("loanTimeLimit");
		int loanRecyCount=  loanTrace.getInt("loanRecyCount");
		String duetime=(loanTimeLimit-loanRecyCount+1)+"/"+loanTimeLimit;
		//年利率
		String rateByYear=(loanTrace.getInt("rateByYear")+loanTrace.getInt("rewardRateByYear"))/100.00+"%";
		long sxf = (leftAmount/200)*3;
		long maxRl = leftAmount/10;
		Map<String,Object> map = new HashMap<String, Object>();
		
		map.put("rateByYear", rateByYear);
		map.put("loantitle", loantitle);
		map.put("leftAmount", Number.longToString(leftAmount));
		map.put("duetime", duetime);
		map.put("sxf", Number.longToString2(sxf));
		map.put("maxRl", Number.longToString(maxRl));
		map.put("traceCode", traceCode);
		
		// 查询使用抵扣现金券金额
		long ticketAmount = 0l;
		String loanTicket = loanTrace.getStr("loanTicket");
		if(!loanTransferService.vilidateIsTransfer(traceCode)){
			if (StringUtil.isBlank(loanTicket) == false) {
				JSONObject jsonTicket = JSONObject.parseArray(loanTicket).getJSONObject(0);
				if (null != jsonTicket) {
					String isDel = StringUtil.isBlank(jsonTicket.getString("isDel")) ? null : jsonTicket.getString("isDel");
					Long amount = StringUtil.isBlank(jsonTicket.getString("amount")) ? null : Long.parseLong(jsonTicket.getString("amount"));
					Long exAmount = StringUtil.isBlank(jsonTicket.getString("examount")) ? null : Long.parseLong(jsonTicket.getString("examount"));
						if(null!=isDel){
							if("Y".equals(isDel)){
								ticketAmount = amount;
							}
						}else{
							if(exAmount != null){
								if(exAmount > 50000){
									ticketAmount = amount;
								}
							}else{
								ticketAmount = amount;
							}
						}

				}
			}
		}
		map.put("ticketAmount", Number.longToStr(ticketAmount / 10 / 10));
		
		msg = succ("查询成功", map);
		renderJson(msg);
	}
}
