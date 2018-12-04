package com.dutiantech.controller.admin;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.http.WebUtils;
import com.jx.service.JXService;
import com.jx.util.RetCodeUtil;

public class TransferController extends BaseController {
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private UserService userService = getService(UserService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private static final Logger scanTransferLogger = Logger.getLogger("scanTransferLogger");
	
	static{
		LoggerUtil.initLogger("scanTransfer", scanTransferLogger);;
	}
	
	@ActionKey("/scanTransfer")
	@Before({Tx.class,PkMsgInterceptor.class})
	public void autoLoan(){
		Message msg = autox();
//		String returnCode = msg.getReturn_code()  ;
		renderJson(msg);
	}
	
	@SuppressWarnings("unchecked")
	private Message autox(){

		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.scanTransferKey");
		if(!key.equals(preKey)){
			return error("01","密匙错误", false );
		}
		try {
			int startIndex = 0 ;
			int size = 50 ;
			boolean isGo = true ;
			long total = Db.queryLong("select count(uid) from t_loan_transfer where transState = 'A'");
			int doCount = 1 ;
			List<String[]> tcs = new ArrayList<String[]>();
			scanTransferLogger.log(Level.INFO,"[定时任务:扫描过期的债权转让信息]扫描中......共计"+total+"条债权转让中...");
//			System.err.println("[定时任务:扫描过期的债权转让信息]扫描中......共计"+total+"条债权转让中...");
			while( isGo ){
				List<Object[]> loanTransfers = getTransferList(startIndex, size) ;
				if( doCount <= total ){
					for(Object[] loanTransfer : loanTransfers){
						String transCode = (String) loanTransfer[0];//债权转让编码
						String transDate = (String) loanTransfer[1];//债权转让发起日期
						String transTime = (String) loanTransfer[2];//债权转让发起时间
						String traceCode = (String) loanTransfer[3];//投标流水编码
						long x = CommonUtil.compareDateTime(DateUtil.getNowDate()+ " " + DateUtil.getNowTime(),transDate+ " " + transTime , "yyyyMMdd HHmmss");
						if(x>=1){
							tcs.add(new String[]{transCode,traceCode});
						}
						doCount ++ ;
					}
				}else{
					isGo = false ;
				}
				startIndex ++ ;
			}
			if(tcs.size()<1){
				scanTransferLogger.log(Level.INFO,"[定时任务:扫描过期的债权转让信息]处理过期债权,共0条债权已过期");
//				System.out.println("[定时任务:扫描过期的债权转让信息]处理过期债权,共0条债权已过期");
			}
			for (int i = 0; i < tcs.size(); i++) {
				String transCode = tcs.get(i)[0];
				String traceCode = tcs.get(i)[1];
				//回滚积分
				LoanTransfer tmp = LoanTransfer.loanTransferDao.findFirst("select transState,transScore,payUserCode from t_loan_transfer where transCode = ?",transCode);
				if(tmp.getInt("transScore")!=null && !StringUtil.isBlank(tmp.getStr("payUserCode")) && !StringUtil.isBlank(tmp.getStr("transState"))){
					int transScore = tmp.getInt("transScore");
					if(transScore>0 && tmp.getStr("transState").equals("A")){
//						fundsServiceV2.doPoints(tmp.getStr("payUserCode"), 0 , transScore , "取消债权转让,返回冻结积分!");
					}
				}
				Db.update("update t_loan_transfer set transState = 'C' where transCode = ?",transCode);
				Db.update("update t_loan_trace set isTransfer='C' where traceCode = ?",traceCode);
				scanTransferLogger.log(Level.INFO,"[定时任务:扫描过期的债权转让信息]处理过期债权,债权转让编码["+transCode+"],第"+(i+1)+"条...共"+tcs.size()+"条债权已过期");
//				System.out.println("[定时任务:扫描过期的债权转让信息]处理过期债权,债权转让编码["+transCodes.get(i)+"],第"+(i+1)+"条...共"+transCodes.size()+"条债权已过期");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			scanTransferLogger.log(Level.SEVERE,"扫描过期的债权时发生异常:"+e.getMessage());
			return error("02", "扫描过期的债权时发生异常:"+e.getMessage(), false);
		}
		scanTransferLogger.log(Level.INFO,"扫描过期的债权任务完成");
		return succ("扫描过期的债权任务完成", true ) ;
	}
	
	/**
	 * 	获取进行中的债权转让信息(无机构待接)
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getTransferList(int index , int size ){
		index = index * size;
		String querySql = "select transCode,transDate,transTime,traceCode from t_loan_transfer where transState = 'A' and refundType not in ('E','F','H') limit ?,?";
		//querySql = querySql.replace("${index}", index+"" ) ;
		//querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql,index,size) ;
		return users ;
	}
	
	
	@ActionKey("/getOverdueTransferList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getOverdueTransferList(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",10);
		String orderParam = getPara("orderParam");
		String orderType = getPara("orderType");
		String mobile = getPara("mobile");
		String refundType = getPara("refundType");
		String transStatus= getPara("transStatus","1");//1.待承接,2.批量债转中
		String payUserCode = "";
		if(!StringUtil.isBlank(mobile)){
			User user = userService.find4mobile(mobile);
			if(user != null){
				payUserCode = user.getStr("userCode");
			}else{
				payUserCode = "1";
			}
		}
		String transState = SysEnum.transState.A.val();
		Page<LoanTransfer> pageLoanTransfer = loanTransferService.queryOverdueLoanTransfer(pageNumber, pageSize, transState, transStatus, orderType, orderParam,payUserCode,refundType);
//		//查询逾期金额
//		List<LoanTransfer> loanTransfers = pageLoanTransfer.getList();
//		for(int i = 0;i<loanTransfers.size();i++){
//			LoanTransfer loanTransfer = loanTransfers.get(i);
//			LoanTrace loanTrace = LoanTrace.loanTraceDao.findByIdLoadColumns(loanTransfer.getStr("traceCode"), "overdueAmount");
//			loanTransfer.put("overdueAmount",loanTrace.getLong("overdueAmount"));
//		}
		return succ("查询成功", pageLoanTransfer);
	}
	
	/**
	 * 查询清退债权承接资金统计信息 WJW
	 * @return
	 */
	@ActionKey("/getClearTransferAmount")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getClearTransferAmount(){
		Boolean isFiling = getParaToBoolean("isFiling");//true:报备,false:未报备
		
		//查询历史总交易金额
		List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransfer("B", null, null, isFiling);
		long sumTransAmountE = 0;//已支付月付总金额
		long sumTransAmountF = 0;//已支付季付总金额
		long sumTransAmountH = 0;//已支付半价总金额
		for (LoanTransfer loanTransfer : loanTransfers) {
			String refundType = loanTransfer.getStr("refundType");
			long sumTransAmount = loanTransfer.getBigDecimal("sumTransAmount").longValue();
			if("E".equals(refundType)){
				sumTransAmountE += sumTransAmount;
			}else if ("F".equals(refundType)){
				sumTransAmountF += sumTransAmount;
			}else if("H".equals(refundType)){
				sumTransAmountH += sumTransAmount;
			}
		}
		
		//查询今日交易总金额
		List<LoanTransfer> loanTransferNows = loanTransferService.queryLoanTransfer("B", DateUtil.getNowDate(), null, isFiling);
		long sumTransAmountNowE = 0;//今日已支付月付总金额
		long sumTransAmountNowF = 0;//今日已支付季付总金额
		long sumTransAmountNowH = 0;//今日已支付半价总金额
		for (LoanTransfer loanTransfer : loanTransferNows) {
			String refundType = loanTransfer.getStr("refundType");
			long sumTransAmount = loanTransfer.getBigDecimal("sumTransAmount").longValue();
			if("E".equals(refundType)){
				sumTransAmountNowE += sumTransAmount;
			}else if ("F".equals(refundType)){
				sumTransAmountNowF += sumTransAmount;
			}else if("H".equals(refundType)){
				sumTransAmountNowH += sumTransAmount;
			}
		}
		
		//查询批次待扣总金额
		List<LoanTransfer> loanTransferWaits = loanTransferService.queryLoanTransfer("A", null, true, null);
		long sumTransAmountWaitE = 0;//待扣除月付总金额
		long sumTransAmountWaitF = 0;//待扣除季付总金额
		long sumTransAmountWaitH = 0;//待扣除半价总金额
		for (LoanTransfer loanTransfer : loanTransferWaits) {
			String refundType = loanTransfer.getStr("refundType");
			long sumTransAmount = loanTransfer.getBigDecimal("sumTransAmount").longValue();
			if("E".equals(refundType)){
				sumTransAmountWaitE += sumTransAmount;
			}else if ("F".equals(refundType)){
				sumTransAmountWaitF += sumTransAmount;
			}else if("H".equals(refundType)){
				sumTransAmountWaitH += sumTransAmount;
			}
		}
		
		Map<String, String> map = new HashMap<String,String>();
		map.put("sumTransAmountE", StringUtil.getMoneyYuan(sumTransAmountE));//已支付月付总金额
		map.put("sumTransAmountF", StringUtil.getMoneyYuan(sumTransAmountF));//已支付季付总金额
		map.put("sumTransAmountH", StringUtil.getMoneyYuan(sumTransAmountH));//已支付半价总金额
		map.put("sumTransAmountNowE", StringUtil.getMoneyYuan(sumTransAmountNowE));//今日已支付月付总金额
		map.put("sumTransAmountNowF", StringUtil.getMoneyYuan(sumTransAmountNowF));//今日已支付季付总金额
		map.put("sumTransAmountNowH", StringUtil.getMoneyYuan(sumTransAmountNowH));//今日已支付半价总金额
		map.put("sumTransAmountWaitE", StringUtil.getMoneyYuan(sumTransAmountWaitE));//待扣除月付总金额
		map.put("sumTransAmountWaitF", StringUtil.getMoneyYuan(sumTransAmountWaitF));//待扣除季付总金额
		map.put("sumTransAmountWaitH", StringUtil.getMoneyYuan(sumTransAmountWaitH));//待扣除半价总金额
		return succ("查询成功", map);
	}
	
	/**
	 * 承接逾期债权
	 * */
	@ActionKey("/inheritOverdueLoan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message inheritOverdueLoan(){
		String[] phoneNo = {"18372124604"};
		String mobile = getPara("mobile");
		if(StringUtil.isBlank(mobile)||!Arrays.asList(phoneNo).contains(mobile)){
			return error("01", "承接账错误", null);
		}
		User user = userService.find4mobile(mobile);
		if(user == null){
			return error("01", "承接账错误", null);
		}
		String userCode = user.getStr("userCode");
		String gotUserName = user.getStr("userName");
		
		//限制还款时间不让承接     10点半到11点
//		int exeTime = Integer.parseInt(DateUtil.getNowTime());
//		if(exeTime >= 103000 && exeTime <= 110000){
//			return error("12", "10:30至11:00系统正在还款，请稍后操作。", "");
//		}
		
		String transferCode = getPara("transCode");
		if(StringUtil.isBlank(transferCode)){
			return error("01", "获取债权转让编号失败", null);
		}
		
		//获取用户信息
		if(StringUtil.isBlank(userCode)){
			return error("02", "请重新登录", null);
		}
		
		//获取转让标信息
		LoanTransfer loanTransfer = loanTransferService.findById(transferCode);
		if(null == loanTransfer){
			return error("04", "债权标书获取异常", "");
		}
		String productType = loanTransfer.getStr("productType");
		String jxTraceCode = loanTransfer.getStr("jxTraceCode");
		if(SysEnum.productType.E.val().equals(productType)){
			return error("02", "易分期无法被承接", "");
		}
		if(!"A".equals(loanTransfer.getStr("transState"))){
			return error("04", "债权标书已被承接或取消", "");
		}
		if(!StringUtil.isBlank(jxTraceCode)){
			return error("04", "该债权已被批量承接,请刷新页面", "");
		}
		String traceCode = loanTransfer.getStr("traceCode");
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		if(loanTrace == null){
			WebUtils.writePromptHtml("债权获取失败", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("04", "债权获取失败", "");
		}
		long overdueAmount = loanTrace.getLong("overdueAmount");
		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//避免还款批次已提交,期间债权被承接,导致存管承接额为还款前金额,债权本地已还资金异常
		//债权还款日为T+1  修改规则日 20180815
//		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
//		if(DateUtil.getNowDate().equals(tmpRecyDate)){
//			WebUtils.writePromptHtml("该债权今日还款未完成,完成后即可承接", "/Z02?navTab=2", "UTF-8",getResponse());
//			return error("05", "该债权今日还款未完成,完成后即可承接", null);
//		}

		// 存管账户相关验证
		if (CommonUtil.jxPort) {	// 江西银行存管验证
			String jxAccountId = user.getStr("jxAccountId"); // 用户电子账号
			if(StringUtil.isBlank(jxAccountId)){
				WebUtils.writePromptHtml("未开通江西银行存管", "/Z02?navTab=2", "UTF-8",getResponse());
				return error("03", "未开通江西银行存管", null);
			}
			JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(jxAccountId);
			if(paymentAuthPageState == null || !"1".equals(paymentAuthPageState.get("type"))){
				return error("24", "缴费授权未开通,无法承接债权转让", "");
			}
		} else if (CommonUtil.fuiouPort) {	// 富友存管验证
			String fuiouLoginId = user.getStr("loginId");	// 用户存管账号
			if (StringUtil.isBlank(fuiouLoginId)) {
				return error("08", "未开通存管账号", "");
			}
		} else {
			return error("99", "存管接口对接中...", null);
		}
		
		// 验证承接人账户资金是否存在异常
//		if (!fundsServiceV2.checkBalance(user)) {
//			return error("05", "用户资金异常", "");
//		}
		
		// 验证承接人可用余额是否足够
		int transAmount = loanTransfer.getInt("transAmount");
		long avBalance = fundsServiceV2.findAvBalanceById(userCode);
		if(avBalance < transAmount){
			WebUtils.writePromptHtml("账户可用余额不足!", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("05", "账户可用余额不足!", "");
		}
		
		//验证此标是否有回款中
		String authCode = loanTrace.getStr("authCode");//授权码
		if(!StringUtil.isBlank(authCode)){
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
				return error("05", "该债权正在回款中，暂时无法承接", "");
			}
		}
		// 验证是否为承接自己的债权
		if(userCode.equals(loanTransfer.getStr("payUserCode"))){
			WebUtils.writePromptHtml("不能承接自己发出的债权", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("06", "不能承接自己发出的债权", "");
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
		
		long ticket_amount = 0;
		
		String traceRemark = "债权转让收入，让利金额：￥"+remark4transFee+"，债权转让费：￥" + remark4sysFee;
		
		//查询是否转让过
		List<LoanTransfer> isTransfer =  loanTransferService.queryLoanTransferByTraceCode(loanTransfer.getStr("traceCode") , "B");
		int rewardticketrate=0;
		if(null == isTransfer || isTransfer.size() <= 0 ){
			// 计算抵扣奖券
			try {
				String json_tickets = loanTrace.getStr("loanTicket");
				if(StringUtil.isBlank(json_tickets)==false){
					JSONArray ja = JSONArray.parseArray(json_tickets);
					for (int i = 0; i < ja.size(); i++) {
						JSONObject jsonObj = ja.getJSONObject(i);
						if(jsonObj.getString("type").equals("A")){
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
		if(loanTransfer.getStr("refundType").equals("E")||loanTransfer.getStr("refundType").equals("F")){
			ticket_amount = 0;
		}
		//转让人
		String payUserCode = loanTransfer.getStr("payUserCode");
		User payUser = userService.findById(payUserCode);
		if(StringUtil.isBlank(payUser.getStr("jxAccountId"))){
			return error("11", "发布人还未开通银行存管", "");
		}
		//若未报备   直接修改信息
		if(StringUtil.isBlank(authCode)){
			long payTransAmount = transAmount - sysFee - ticket_amount;//债权人转让收益，扣除手续费和抵用券费用
			boolean updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"A", "B");
			if(updateTraceState == false){
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权失败[08]", null);
				return error("08", "承接债权失败[08]", "");
			}
			
			if(loanTransfer.getStr("refundType").equals("H")){
				Map<String, String> redAmountMap = JXQueryController.freezeAmtQuery(JXService.RED_ENVELOPES);
				if(redAmountMap==null){
					return error("08", "红包账户余额查询失败", "");
				}else{
					String availBal = redAmountMap.get("availBal");
					if(StringUtil.isBlank(availBal)){
						return error("08", "红包账户余额查询失败", "");
					}else{
						if(payTransAmount>Float.parseFloat(availBal)*100){
							return error("08", "红包账户余额不足", "");
						}
					}
				}
//				String remark = "债权转让支出，让利金额：￥"+remark4transFee+"（用户收益：￥"+remark4transFee+"）";
//				boolean b = fundsServiceV2.carryOnTransfer(userCode, transAmount,remark);
//				if(b==true){
//					//记录日志
//					BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权扣除可用余额成功  扣除金额  : " + transAmount);
//				}
//				if(b == false){
//					BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权-添加流水失败", null);
//					return error("05", "承接债权失败-[添加扣款流水失败]!", "");
//				}
				boolean carryOnTransferTo = fundsServiceV2.carryOnTransferTo(payUserCode, payTransAmount, sysFee ,transFee,traceRemark);
				if(carryOnTransferTo==true){
					//记录日志
					BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权增加可用余额成功  收益金额  : " + payTransAmount);
				}
				
				if(carryOnTransferTo == false){
					BIZ_LOG_ERROR(payUserCode, BIZ_TYPE.TRANSFER, "承接债权-添加转让人流水失败", null);
					return error("06", "承接债权失败-[添加转让人流水失败]!", "");
				}
			}
			boolean updateTransferState = loanTransferService.updateTransferState(transferCode,userCode,user.getStr("userName"));
			if(updateTransferState == false){
				//回滚资金
				if(!loanTransfer.getStr("refundType").equals("E")&&!loanTransfer.getStr("refundType").equals("F")){
//					fundsServiceV2.doAvBalance(userCode, 0, transAmount);
					fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
				}
				//回滚流水状态
				loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
						"B", "A");
				//记录日志
				BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权失败资金回滚");
				BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权失败资金回滚");
				return error("07", "债权已被承接", "");
			}else{
				// 更新冗余信息
				String oPayUserCode = loanTrace.getStr("payUserCode");
				int rate = loanTransfer.getInt("rateByYear") + loanTransfer.getInt("rewardRateByYear") ;
				int loanRecyCount = loanTrace.getInt("loanRecyCount");//未还期数
				int limit = loanTrace.getInt("loanTimeLimit");//总期数
				int reciedCount = limit - loanRecyCount;//已还期数
				long[] benxi = new long[2];
				benxi[0] = loanTrace.getLong("leftAmount");
				benxi[1] = loanTrace.getLong("leftInterest");
				
				//假如第一次债转  并且  用的加息券   计算下期待还本息与总的利息
				String refundType=loanTrace.getStr("refundType");
				//承接人本息
				long[] benxi2 = new long[2];
				if(loanRecyCount==0){
					benxi2=benxi;
				}else{
					if("A".equals(refundType)){
						benxi2 =CommonUtil.f_004(benxi[0], rate, loanRecyCount, "A");
					}else if("B".equals(refundType)){
						benxi2 =CommonUtil.f_005(loanTrace.getLong("payAmount"), rate, limit, reciedCount, "B");
					}
					if(rewardticketrate>0){
						//承接人下期本息
						long[] nextbenxi=new long[2];
						if("A".equals(refundType)){
							nextbenxi=CommonUtil.f_000(benxi[0], loanRecyCount, rate, 1, "A");
						}else if("B".equals(refundType)){
							nextbenxi=CommonUtil.f_000(benxi[0], limit, rate, reciedCount+1, "B");
						}
						loanTrace.set("leftInterest", benxi2[1]);//修改总代收利息
						loanTrace.set("nextAmount", nextbenxi[0]);//修改下期代收本金
						loanTrace.set("nextInterest", nextbenxi[1]);//修改下期代收利息
						loanTrace.set("rewardRateByYear",loanTransfer.getInt("rewardRateByYear"));//扣除加息券利息
					}
				}
				//修改债权归属
				loanTrace.set("payUserCode", userCode );
				loanTrace.set("payUserName", user.getStr("userName") );
				
				boolean updateTrace = loanTrace.update();
				if(updateTrace == false){
					//回滚资金
					if(!loanTransfer.getStr("refundType").equals("E")&&!loanTransfer.getStr("refundType").equals("F")){
//						fundsServiceV2.doAvBalance(userCode, 0, transAmount);
						fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
					}
					//记录日志
					BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
					BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
					return error("10", "债权数据出现异常，请联系客服处理。", "");
				}
				
				//更新冗余账户 (更新理财人待还账户)
				int beRecyCount = loanRecyCount ;
				//减少原投资人账户	oPayUserCode      //  增加已回收资金  利息	 shiqingsong 2016-02-18
				long reciedInterest = transAmount - benxi[0] - transFee;
				fundsServiceV2.updateBeRecyFunds(oPayUserCode, (0-beRecyCount), (0-benxi[0]-loanTrace.getLong("overdueAmount")), (0-benxi[1]), benxi[0] ,  reciedInterest > 0 ? reciedInterest : 0);
				//增加接受人
				if(rewardticketrate>0){//若使用了加息券
					fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi2[0]+loanTrace.getLong("overdueAmount"), benxi2[1], 0 ,  0 );
				}else{
					fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi[0]+loanTrace.getLong("overdueAmount"), benxi[1], 0 ,  0 );
				}
				//若为五折债转   发放红包
				if(loanTransfer.getStr("refundType").equals("H")){
					JXController.voucherPay(JXService.RED_ENVELOPES, payTransAmount, payUser.getStr("jxAccountId"), "1", "机构接债["+loanTransfer.getStr("transCode")+"]");
				}
			}
			loanRepaymentService.transfer(transferCode, userCode, gotUserName);
			return succ("OK", "承接成功");
		}
		//报备则跳江西页面
		if (CommonUtil.jxPort) {// 江西银行存管端口开关
			String accountId = user.getStr("jxAccountId");// 购买方账号
			String forAccountId = payUser.getStr("jxAccountId");// 卖出方账号
			List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
			
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
//						String url = "F://cs//";
						String url = "//home//jx_loanTrace//";
						for(int i = 0;i<5;i++){
							String urlName = url+"3005-BIDRESP-"+(301000+i)+"-20180523";
							String[] text;
							try {
								text = file.readTxtLine(urlName, "GBK");
							} catch (IOException e) {
								WebUtils.writePromptHtml("新存管上线前债权转让失败", "/Z02?navTab=2", "UTF-8",getResponse());
								return error("05", "新存管上线前债权转让失败", null);
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
					WebUtils.writePromptHtml("新存管上线前债权暂不支持转让", "/Z02?navTab=2", "UTF-8",getResponse());
					return error("05", "新存管上线前债权暂不支持转让", null);
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
						return error("06", "未找到该债权信息", "");
					}
				}
				orgTxAmount = StringUtil.getMoneyCent(txAmount);//原债权金额
			}
			
			String productId = loanTrace.getStr("loanCode");// 标号
			Map<String, String> reqMap = JXService.getHeadReq();
//			String jxTraceCode = reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo");
//			boolean updateJxTraceCode = loanTransferService.updateJxTraceCode(jxTraceCode, transferCode);//债转流水添加jxTraceCode关联
//			if(!updateJxTraceCode){
//				BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "transferCode["+transferCode+"]关联jxTraceCode["+jxTraceCode+"]失败");
//				WebUtils.writePromptHtml("网络异常", "/Z02?navTab=2", "UTF-8",getResponse());
//				return error("15", "网络异常", "");
//			}
			//跳转江西银行页面
			JXController.creditInvest(reqMap,accountId, transAmount, sysFee + ticket_amount, orgTxAmount,
					forAccountId, orgOrderId, orgTxAmount, productId,transferCode, getResponse());
			WebUtils.writePromptHtml("债权转让请求已发送", "/Z02?navTab=2", "UTF-8",getResponse());
			return succ("债权转让请求已发送", null);
		} else if (CommonUtil.fuiouPort) {
			return error("13", "此版本不支持此功能", "");
		} else {
			return error("16", "网络错误", "");
		}
	}
	
	/**
	 * 批次债转更新响应报文 WJW
	 */
	@ActionKey("/batchCreditInvestNotifyURL")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void notifyURL(){
		String bgData = getRequest().getParameter("bgData");
		if(StringUtil.isBlank(bgData)){
			return;
		}
		renderText("success");//bgData接收成功,返回success至江西,停止骚扰
		Map<String, ?> responseMap = JSONObject.parseObject(bgData);
		Map<String, String> resMap = (Map<String, String>) responseMap;
		String jxTraceCode = resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");//t_jx_trace流水号
		boolean updateJxTraceResponse = JXService.updateJxTraceResponse(jxTraceCode, resMap, JSON.toJSONString(resMap), "", "");
		scanTransferLogger.log(Level.INFO,"批次债转报文更新"+(updateJxTraceResponse?"成功":"失败"));
	}
	
	/**
	 * 发送批次承接债权 WJW
	 * @return
	 */
	@ActionKey("/sendBatchCreditInvest")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public synchronized Message sendBatchCreditInvest(){
		String transCodeStr = getPara("transCodes");
		if(StringUtil.isBlank(transCodeStr)){
			return error("", "债转编码为空", "");
		}
		String[] transCodes = transCodeStr.split(",");
		if(transCodes.length < 1 || transCodes.length > 100){
			return error("", "债转组批数量:1 - 100条", "");
		}
		JSONArray jsonArray = new JSONArray();
		Map<String, String> reqMap = JXService.getHeadReq();
		String jxTraceCode = reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo");
		for (String transCode : transCodes) {
			LoanTransfer loanTransfer = loanTransferService.findById(transCode);
			if(loanTransfer == null){
				continue;
			}
			String traceCode = loanTransfer.getStr("traceCode");
			LoanTrace loanTrace = loanTraceService.findById(traceCode);
			if(loanTrace == null){
				continue;
			}
			String authCode = loanTrace.getStr("authCode");//投标授权码
			String productType = loanTransfer.getStr("productType");//标类型
			String transState = loanTransfer.getStr("transState");//转让状态
			
			if(SysEnum.productType.E.val().equals(productType)){//过滤易分期
				continue;
			}
			if(!"A".equals(transState)){//过滤非待承接
				continue;
			}
			
			if(StringUtil.isBlank(authCode)){//红包接债
				doLoanTransfer(loanTransfer,loanTrace, null);//更新本地流水,发送红包
			}else{//批次接债
				JSONObject jsonObject = null;//批次交易明细
				try {
					jsonObject = creditInvest(loanTransfer,loanTrace);
				} catch (Exception e) {
					scanTransferLogger.log(Level.INFO,"构建批次债转transCode:"+transCode+",异常,略过......");
					continue;
				}
				if(jsonObject == null){
					scanTransferLogger.log(Level.INFO,"构建批次债转transCode:"+transCode+",失败,略过......");
					continue;
				}
				//关联jxTraceCode,orderId
				loanTransferService.updateJxTraceCodeAndOrderId(jxTraceCode,jsonObject.getString("orderId"), transCode);
				jsonArray.add(jsonObject);
			}
		}
		
		String returnData = null;
		if(jsonArray.size() > 0){
			int batchNo = jxTraceService.batchNoByToday();
			Map<String, String> batchCreditInvest = null;
			try {
				batchCreditInvest = JXController.batchCreditInvest(reqMap, batchNo, jsonArray.toJSONString());
				returnData = "批次债转:"+batchNo+",发送"+("success".equals(batchCreditInvest.get("received"))?"成功":"失败");
			} catch (Exception e) {
				returnData = "批次债转:"+batchNo+",发送异常......";
			}
			scanTransferLogger.log(Level.INFO,returnData);
		}else {
			returnData = "未报备红包债权承接完成!";
		}
		return succ("", returnData);
	}
	
	/**
	 * 根据债转编码生成单条债转jsonObject WJW
	 * @param loanTransfer	债转流水
	 * @param loanTrace		投标流水
	 * @return
	 */
	private JSONObject creditInvest(LoanTransfer loanTransfer,LoanTrace loanTrace){
		String accountId = "6212462490000455724";// 购买方账号
		
		String transferCode = loanTransfer.getStr("transCode");//债转编码
		String traceCode = loanTransfer.getStr("traceCode");//投标编码
		int transAmount = loanTransfer.getInt("transAmount");//承接人实际付款金额
		int sysFee = loanTransfer.getInt("sysFee") ;//平台手续费
		int transFee = loanTransfer.getInt("transFee");//转让人让利金额
		String payUserCode = loanTransfer.getStr("payUserCode");//转让人
		String refundType = loanTransfer.getStr("refundType");//还款方式
		String jxTraceCode = loanTransfer.getStr("jxTraceCode");//jxTrace流水号
		
		if(!StringUtil.isBlank(jxTraceCode)){//该债转流水已在批次债转中
			return null;
		}

		String json_tickets = loanTrace.getStr("loanTicket");
		String productId = loanTrace.getStr("loanCode");
		String loanNo = loanTrace.getStr("loanNo");
		String orgJxTraceCode = loanTrace.getStr("jxTraceCode");//原交易投标流水
		String authCode = loanTrace.getStr("authCode");//投标授权码
		
		if(StringUtil.isBlank(authCode)){//未报备标承接暂时略过
			return null;
		}
		
		User payUser = userService.findById(payUserCode);
		if(payUser == null){
			return null;
		}
		String forAccountId = payUser.getStr("jxAccountId");//卖出方账号
		if(forAccountId == null){
			return null;
		}

		boolean vilidateIsTransfer = loanTransferService.vilidateIsTransfer(traceCode);//true:债转过

		String traceRemark = "债权转让收入，让利金额：￥"+StringUtil.getMoneyYuan(transFee)+"，债权转让费：￥" + StringUtil.getMoneyYuan(sysFee);
		long ticket_amount = 0;//现金券金额
		if(!vilidateIsTransfer && !StringUtil.isBlank(json_tickets) && !"E".equals(refundType) && !"F".equals(refundType)){
			try {
				Long[] analysisTickets = analysisTickets(json_tickets);
				ticket_amount = analysisTickets[0];
				traceRemark += "，现金券金额：￥"+StringUtil.getMoneyYuan(ticket_amount);
			} catch (Exception e) {
				scanTransferLogger.log(Level.INFO,"批次债转transferCode:"+transferCode+",解析奖券异常...");
			}
		}
		
		String orgOrderId = "";// 原订单号
		long orgTxAmount = 0;// 原交易金额
		if(vilidateIsTransfer){
			List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
			orgJxTraceCode = loanTransfers.get(loanTransfers.size() - 1).getStr("jxTraceCode");// 获取最后一次成功债转jx流水号
		}
		if(StringUtil.isBlank(orgJxTraceCode)){//迁移标录入订单号
			if(!StringUtil.isBlank(loanTrace.getStr("authCode"))){
				if(StringUtil.isBlank(loanTrace.getStr("orderId"))){
					FileOperate file = new FileOperate();
//						String url = "E://jx_loanTrace//";
					String url = "//home//jx_loanTrace//";
					for(int i = 0;i<5;i++){
						String urlName = url+"3005-BIDRESP-"+(301000+i)+"-20180523";
						String[] text;
						try {
							text = file.readTxtLine(urlName, "GBK");
						} catch (IOException e) {
							return null;
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
				return null;
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
					return null;
				}
			}
			orgTxAmount = StringUtil.getMoneyCent(txAmount);//原债权金额
		}
		
		//发布人添加冻结流水
		if(transAmount > 0 && !"E".equals(refundType) && !"F".equals(refundType)){
			fundsServiceV2.doAvBalanceRepayment(payUserCode, transAmount , sysFee + ticket_amount ,  traceType.B , fundsType.J , "标["+loanNo+"]"+traceRemark+"(冻结,待银行批次解冻)");
		}
		
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("accountId", accountId);//买入方账号
		jsonObject.put("orderId", CommonUtil.genShortUID());//订单号由P2P生成，使用字母+数字的组合，不区分大小写，且必须保证唯一
		jsonObject.put("txAmount", transAmount);//成交价格,买入方实际付出金额
		jsonObject.put("txFee", sysFee + ticket_amount);//向卖出方收取的手续费
		jsonObject.put("tsfAmount", orgTxAmount);//卖出的债权份额
		jsonObject.put("forAccountId", forAccountId);//卖出方账号
		jsonObject.put("orgOrderId", orgOrderId);//卖出方投标的原订单号（或卖出方购买债权的原订单号）
		jsonObject.put("orgTxAmount", orgTxAmount);//卖出方投标的原交易金额（或卖出方购买债权的原转让金额）
		jsonObject.put("productId", productId);//卖出方原标的号；标的号不区分大小写
		jsonObject.put("contOrderId", "");//买入方自动债权转让签约订单号。该字段不校验，可任意送值。
		return jsonObject;
	}
	
	/**
	 * 解冻批次债转 WJW
	 * @return
	 */
	@ActionKey("/unfreezeBatchCreditInvest")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public synchronized Message unfreezeBatchCreditInvest(){
		List<JXTrace> jxTraces = jxTraceService.queryThawBatchCreditInvest();
		for (JXTrace jxTrace : jxTraces) {
			String jxTraceCode = jxTrace.getStr("jxTraceCode");
			String txDate = jxTrace.getStr("txDate");
			String batchNo = jxTrace.getStr("batchNo");
			
			//查询批次处理状态
			Map<String, String> batchQuery = null;
			try {
				batchQuery = JXQueryController.batchQuery(txDate, batchNo);
				if(!RetCodeUtil.isSuccRetCode(batchQuery.get("retCode")) || !"S".equals(batchQuery.get("batchState"))){//批次未处理完
					continue;
				}
			} catch (Exception e) {
				continue;
			}
			
			//查询批次处理明细
			Map<String, Object> batchDetailsQueryAll = null;
			try {
				batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "0");
			} catch (Exception e) {
				continue;
			}
			
			String retCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
			String retMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
			if(!"00000000".equals(retCode)){
				scanTransferLogger.log(Level.INFO,"[处理批次债转资金解冻jxTraceCode:"+jxTraceCode+"]"+retMsg);
				continue;
			}
			List<Map<String, String>> subPacklst = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
			if(subPacklst == null || subPacklst.size() < 1){
				scanTransferLogger.log(Level.INFO,"[处理批次债转资金解冻jxTraceCode:"+jxTraceCode+"]subPacks查询为空");
				continue;
			}
			
			//获取已处理完成批次明细结果
			Map<String, JSONObject> orderIdMap = new HashMap<String,JSONObject>();//key:orderId,value:authCode
			for (Map<String, String> subPack : subPacklst) {
				String txState = subPack.get("txState");
				String orderId = subPack.get("orderId");
				if("S".equals(txState)){//成功
					JSONObject jsonObject = new JSONObject();
					String authCode = subPack.get("authCode");
					jsonObject.put("authCode", authCode);
					orderIdMap.put(orderId, jsonObject);
				}else if("F".equals(txState) || "C".equals(txState)){//失败,撤销
					orderIdMap.put(orderId, null);
				}
			}
			
			//循环处理本批次债转本地流水
			for(Map.Entry<String, JSONObject> entry:orderIdMap.entrySet()){
				String orderId = entry.getKey();
				JSONObject jsonObject = entry.getValue();
				if(jsonObject != null){//成功
					String authCode = jsonObject.getString("authCode");
					try {
						LoanTransfer loanTransfer = loanTransferService.findByOrderId(orderId);
						if(loanTransfer == null){//关联订单号不存在
							continue;
						}
						String traceCode = loanTransfer.getStr("traceCode");
						LoanTrace loanTrace = loanTraceService.findById(traceCode);
						if(loanTrace == null){
							continue;
						}
						boolean isLoanTracefer = doLoanTransfer(loanTransfer, loanTrace, authCode);
						scanTransferLogger.log(Level.INFO,"[处理批次债转资金解冻orderId:"+orderId+"]"+(isLoanTracefer ? "成功":"失败"));
					} catch (Exception e) {
						scanTransferLogger.log(Level.INFO,"[处理批次债转资金解冻orderId:"+orderId+"]异常......");
					}
				}
			}
		}
		return succ("", "批次债转资金解冻完成");
	}
	
	/**
	 * 处理单条债转流水 WJW
	 * @param loanTransfer	债转流水
	 * @param loanTrace		投标流水
	 * @param authCode		债转成功授权码
	 * @return
	 */
	public boolean doLoanTransfer(LoanTransfer loanTransfer,LoanTrace loanTrace,String authCode) {
		String gotMobile = "18372124604";// 购买方手机号
		
		String payUserCode = loanTransfer.getStr("payUserCode");//转让人
		String transCode = loanTransfer.getStr("transCode");//债权转让编号
		String traceCode = loanTransfer.getStr("traceCode");//投标编码
		int transAmount = loanTransfer.getInt("transAmount");//承接人实际付款金额
		int sysFee = loanTransfer.getInt("sysFee");//平台手续费
		int transFee = loanTransfer.getInt("transFee");//转让人让利金额
		String refundType = loanTransfer.getStr("refundType");//还款方式
		String transState = loanTransfer.getStr("transState");//转让状态
		int rateByYear = loanTransfer.getInt("rateByYear");//年利率
		int rewardRateByYear = loanTransfer.getInt("rewardRateByYear");//奖励年利率
		String loanCode = loanTransfer.getStr("loanCode");
		String loanNo = loanTransfer.getStr("loanNo");
		int rate = rateByYear + rewardRateByYear;
		if(!"A".equals(transState)){
			return false;
		}
		int loanRecyCount = loanTrace.getInt("loanRecyCount");//剩余还款期数
		int limit = loanTrace.getInt("loanTimeLimit");//总期数
		String json_tickets = loanTrace.getStr("loanTicket");//奖券
		long leftAmount = loanTrace.getLong("leftAmount");//剩余本金
		long leftInterest = loanTrace.getLong("leftInterest");//剩余利息
		long overdueAmount = loanTrace.getLong("overdueAmount");//逾期本金
		int reciedCount = limit - loanRecyCount;//已还期数

		User user = userService.find4mobile(gotMobile);//承接用户
		if(user == null){
			return false;
		}
		String userCode = user.getStr("userCode");//承接人用户编码
		String gotUserName = user.getStr("userName");//承接人用户名
		
		UserInfo userInfo=userInfoService.findById(userCode);
		if(userInfo == null){
			return false;
		}
		
		int rewardticketrate = 0;//加息券年利率
		long ticket_amount = 0;//现金券金额
		if(!loanTransferService.vilidateIsTransfer(traceCode) && !StringUtil.isBlank(json_tickets)){
			try {
				Long[] analysisTickets = analysisTickets(json_tickets);
				ticket_amount = analysisTickets[0];
				rewardticketrate = analysisTickets[1].intValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		//修改债权状态 并验证是否已经被转让
		boolean updateTransferState = loanTransferService.updateTransferState(transCode,userCode,gotUserName);
		if(!updateTransferState){//债转信息已更新
			return false;
		}
		
		//修改投标债权状态到  已转让
		boolean updateTraceState = loanTraceService.updateTransferState(traceCode,"A","B");
		if(!updateTraceState){
			return false;
		}
		
		//更新债转成功授权码
		if(!StringUtil.isBlank(authCode)){
			loanTransferService.updateAuthCode(authCode, transCode);
		}
		
		//更改债权归属
		loanTrace.set("payUserCode", userCode);
		loanTrace.set("payUserName", gotUserName);

		//重新计算剩余总利息
		long nowLeftInterest = 0;
		if("A".equals(refundType)){
			nowLeftInterest = CommonUtil.f_004(leftAmount, rate, loanRecyCount, "A")[1];
		}else if("B".equals(refundType)){
			nowLeftInterest = CommonUtil.f_005(leftAmount, rate, limit, reciedCount, "B")[1];
		}
		
		//使用过加息券,重新计算下一期本金利息
		if(rewardticketrate > 0){
			long[] nextbenxi=new long[2];//承接人下期本息
			if("A".equals(refundType)){
				nextbenxi=CommonUtil.f_000(leftAmount, loanRecyCount, rate, 1, "A");
			}else if("B".equals(refundType)){
				nextbenxi=CommonUtil.f_000(leftAmount, limit, rate, reciedCount+1, "B");
			}
			loanTrace.set("leftInterest", nowLeftInterest);//修改总代收利息
			loanTrace.set("nextAmount", nextbenxi[0]);//修改下期代收本金
			loanTrace.set("nextInterest", nextbenxi[1]);//修改下期代收利息
			loanTrace.set("rewardRateByYear",rewardRateByYear);//已扣除加息券利息
		}
		
		loanTrace.update();
		
		//更新资金流水
		if(!"E".equals(refundType) && !"F".equals(refundType)){//月付,季付无需更新资金流水
			String traceRemark = "债权转让收入,让利金额:￥"+StringUtil.getMoneyYuan(transFee)+",债权转让费:￥" + StringUtil.getMoneyYuan(sysFee)+ ",现金券金额:￥"+StringUtil.getMoneyYuan(ticket_amount);
			try {
				if(StringUtil.isBlank(authCode)){//红包承接
					User payUser = userService.findById(payUserCode);//发布人
					String payAccountId = payUser.getStr("jxAccountId");//发布人电子账号
					if(StringUtil.isBlank(payAccountId)){
						return false;
					}
					
					//更新发布人资金流水,发送红包
					fundsServiceV2.carryOnTransferTo(payUserCode, transAmount - (sysFee + ticket_amount), sysFee ,transFee,traceRemark);
					JXController.voucherPay(JXService.RED_ENVELOPES, transAmount - (sysFee + ticket_amount), payAccountId, "1", "机构接债["+transCode+"]");
				}else {//批次承接
					//解冻发布人资金
					fundsServiceV2.frozeBalance(payUserCode, transAmount, sysFee + ticket_amount, traceType.Y, fundsType.J, traceRemark+"(解冻)");
				
					//更新承接人资金
					fundsServiceV2.doAvBalance(userCode, 1, transAmount);
				}
			} catch (Exception e) {
				return false;
			}
		}
		
		//更新发布人待收
		long reciedInterest = transAmount - leftAmount - transFee;//已回收利息
		fundsServiceV2.updateBeRecyFunds(payUserCode, -loanRecyCount, -(leftAmount+overdueAmount), -leftInterest, leftAmount+overdueAmount ,  reciedInterest > 0 ? reciedInterest : 0);
		
		//更新承接人待收
		fundsServiceV2.updateBeRecyFunds(userCode, loanRecyCount, leftAmount+overdueAmount, rewardticketrate > 0 ? nowLeftInterest:leftInterest, 0, 0);
		
		//更新债转还款计划
		loanRepaymentService.transfer(transCode, userCode, gotUserName);
		
		/*
		 * 债权承接成功——给借款人发送债权变更通知
		 * 1、获取承接标的信息
		 * 2、查询借款人、转让人、承接人信息
		 * 3、生成通知短信内容、发送、保存
		 */
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
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
		return true;
	}
	
	/**
	 * 解析奖券 WJW
	 * @param loanTicket
	 * @return	Long[0]:券金额,Long[1]:券年利率
	 */
	private Long[] analysisTickets(String loanTicket){
		long ticketAmount = 0;//券金额
		long ticketRate = 0;//券年利率
		JSONArray ja = JSONArray.parseArray(loanTicket);
		for (int i = 0; i < ja.size(); i++) {
			JSONObject jsonObj = ja.getJSONObject(i);
			String type = jsonObj.getString("type");
			if("A".equals(type)){
				Long examount = jsonObj.getLong("examount");
				String isDel = jsonObj.getString("isDel");
				if(StringUtil.isBlank(isDel) && (null == examount || examount > 50000)){
					ticketAmount += jsonObj.getLong("amount");
				}else if("Y".equals(isDel)){
					ticketAmount += jsonObj.getLong("amount");
				}
			}else if("C".equals(type)){
				ticketRate += jsonObj.getInteger("rate");
			}
		}
		return new Long[]{ticketAmount,ticketRate};
	}
}