package com.dutiantech.controller.portal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNotice;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.TransferWay;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNoticeService;
import com.dutiantech.service.LoanOverdueService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.TransferWayService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.LiCai;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.vo.VipV2;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.http.WebUtils;
import com.jx.service.JXService;


public class LoanCenterController extends BaseController{
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	private AutoMapSerivce autoMapService = getService(AutoMapSerivce.class);
	//private FundsService fundsService = getService(FundsService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private LoanNoticeService loanNoticeService = getService(LoanNoticeService.class);
	private LoanOverdueService loanOverdueService = getService(LoanOverdueService.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class ) ;
	private UserService userService = getService(UserService.class);
	private LoanApplyService loanApplyService = getService(LoanApplyService.class);
	private TicketsService ticketService = getService( TicketsService.class );
	private SMSLogService smsLogService = getService(SMSLogService.class);
//	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private TransferWayService transferWayService = getService(TransferWayService.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
	
	/**
	 * 获取我的贷款信息
	 * @param pageNumber
	 * @param pageSize
	 * @param loanState
	 * @param result
	 * @return
	 */
	@ActionKey("/queryLoan4My")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoan4My(){
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
		String loanState = getPara("loanState");
		
		//获取用户标识
		String userCode = getUserCode();
		
		Page<LoanInfo> loanInfos = loanInfoService.findByPage(userCode,pageNumber, pageSize, null, null,null,null, loanState, null);
		
		return succ("获取成功", loanInfos);
	}	
	
	
	/**
	 * 获取我的投资信息
	 * @param pageNumber
	 * @param pageSize
	 * @param result
	 * @return
	 */
	@ActionKey("/queryInvest4My")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryInvest4My", cachedKeyParm="beginDateTime|endDateTime|traceState|pageNumber|pageSize|@userCode",mode="remote" , time=5)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoanTrace4My(){
		
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
		String traceState = getPara("traceState");
		String beginDateTime = getPara("beginDateTime"); 
		String endDateTime = getPara("endDateTime"); 
		
		//获取用户标识
		String userCode = getUserCode();
		
		Page<LoanTrace> loanTraces = null;
		if(StringUtil.isBlank(traceState) == false && "A".equals(traceState)){
			traceState += ",D";
			loanTraces = loanTraceService.findByPage(pageNumber, pageSize,
					beginDateTime, endDateTime, userCode, traceState);
		} else {
			loanTraces = loanTraceService.findByPage4Overdue(pageNumber, pageSize,
					beginDateTime, endDateTime, userCode, traceState);
		}
		
//		Page<LoanTrace> loanTraces = loanTraceService.findByPage4Overdue(pageNumber, pageSize,
//				beginDateTime, endDateTime, userCode, traceState);
		
		LoanTrace loanTrace=null;
		ArrayList<LoanTrace> list = (ArrayList<LoanTrace>) loanTraces.getList();
		for (int i = 0; i < list.size(); i++) {
			loanTrace=list.get(i);
			List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanTrace.getStr("loanCode"), "n", "");//该标的逾期列表
			if(!loanOverdues.isEmpty()&&loanOverdues.size()>0){
				int repayIndex = loanOverdues.get(0).getInt("repayIndex");//第几期开始逾期
				loanTrace.put("isOverdue", "yes");
				loanTrace.put("repayIndex",repayIndex);
				loanTrace.put("loanOverdues",loanOverdues);
				
			}
			int loanTimeLimit=loanTrace.getInt("loanTimeLimit");//还款期限	int rateByYear =loanTrace.getInt("rateByYear");
			int rateByYear =loanTrace.getInt("rateByYear");
			int rewardRateByYear =loanTrace.getInt("rewardRateByYear");
			long totalRate=rateByYear+rewardRateByYear;
			long payAmount=loanTrace.getLong("payAmount");//投标金额
			String refundType=loanTrace.getStr("refundType");
			long benXi=0;
			if("B".equals(traceState)){//已回收
				LiCai ff = new LiCai(payAmount , totalRate, loanTimeLimit );
				List<Map<String , Long>> xxx = null;
				if(refundType.equals("A")){
					xxx = ff.getDengEList() ;
				}else if(refundType.equals("B")){
					 xxx = ff.getDengXiList();
				}else if(refundType.equals("C")){
					 xxx = ff.getDengXiList();
				}
				
				for (int j = 1; j <= xxx.size(); j++) {
					Map<String,Long> ck = xxx.get(j-1);
					
					benXi+=ck.get("benxi");
					}
			
				
				
			}
			loanTrace.put("benXi",Number.longToString(benXi));
		}
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNumber", loanTraces.getPageNumber());
		map.put("pageSize", loanTraces.getPageSize());
		map.put("totalPage", loanTraces.getTotalPage());
		map.put("totalRow", loanTraces.getTotalRow());
		map.put("list", loanTraces.getList());
		
		if("N".equals(traceState)){
			Object[] countByPage = loanTraceService.countByPage(beginDateTime, endDateTime, userCode);
			map.put("totalNextAmount", countByPage[0]);
			map.put("totalNextInterest", countByPage[1]);
		}
		TransferWay transferWay = transferWayService.findByUserCode(userCode);
		map.put("transferWay", transferWay);
		return succ("获取成功", map);
	}
	
	/**
	 * 获取我所有投资信息
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	@ActionKey("/queryAllInvest4My")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryAllInvest4My(){
		
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
		
		//获取用户标识
		String userCode = getUserCode();
		Page<Record> loanTraces = loanTraceService.findAllByPage2(pageNumber, pageSize,userCode);
		return succ("获取成功", loanTraces);
	}
	
	
	/**
	 * 获取用户回款日期
	 * @return
	 */
	@ActionKey("/queryBackDate4User")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryBackDate4User", cachedKeyParm="@userCode",mode="remote" , time=5)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryBackDate4User(){
		
		String userCode = getUserCode();
		
		List<LoanTrace> queryLoanBackDate = loanTraceService.queryLoanBackDate(userCode);
		
		return succ("获取成功", queryLoanBackDate);
		
	}
	
	/**
	 * 查询用户指定月份回款信息
	 * */
	@ActionKey("/haveBackDate4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message haveBackDate4User(){
		String userCode = getUserCode();
		String queryDate = getPara("date", "");	// pattern: yyyyMM
		List<String> lstRecyDays = new ArrayList<String>();
		int countRepayment = 0;
		long sumRecyPrincipal = 0l;
		long sumRecyInterest = 0l;
		
		// 补全查询月未生成回款数据的标的回款计划
		loanRepaymentService.supplementByDate(userCode, queryDate);
		
		// 查询当月回款信息
		Map<String, Map<String, Object>> mapLoanRepayments = loanRepaymentService.queryByDate(userCode, queryDate);
		if (mapLoanRepayments != null) {
			for (String day : mapLoanRepayments.keySet()) {
				lstRecyDays.add(day.substring(6, 8));
				countRepayment += Integer.parseInt(mapLoanRepayments.get(day).get("countRecy").toString());		// 累加查询月回款数
				sumRecyPrincipal += Long.parseLong(mapLoanRepayments.get(day).get("sumPrincipal").toString());	// 累加查询月回款本金总额
				sumRecyInterest += Long.parseLong(mapLoanRepayments.get(day).get("sumInterest").toString());	// 累加查询月回款利息总额
			}
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("list", lstRecyDays);
		map.put("countRepayment", countRepayment);
		map.put("monthBenXi", StringUtil.getMoneyYuan(sumRecyPrincipal + sumRecyInterest));
		return succ("获取成功", map);
	}
	
	/**
	 * 搜索用户指定日期回款详情 ws 20170914
	 * */
	@ActionKey("/BackDetil8Day")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message backDetil8Day(){
		String userCode = getUserCode();
		String queryDay = getPara("day");	// pattern: yyyyMMdd
		
		try {
			Map<String, Map<String, Object>> mapResult = loanRepaymentService.queryByDate(userCode, queryDay);
			Map<String, Object> dayResult = mapResult.get(queryDay);
			Map<String, Object> detailMap = new HashMap<String, Object>();
			detailMap.put("list", dayResult.get("loanRepayments") == null ? new ArrayList<>() : dayResult.get("loanRepayments"));
			detailMap.put("allbx", StringUtil.getMoneyYuan(Long.parseLong(dayResult.get("sumPrincipal").toString()) + Long.parseLong(dayResult.get("sumInterest").toString())));
			detailMap.put("allben", StringUtil.getMoneyYuan(Long.parseLong(dayResult.get("sumPrincipal").toString())));
			detailMap.put("allxi", StringUtil.getMoneyYuan(Long.parseLong(dayResult.get("sumInterest").toString())));
			return succ("获取成功", detailMap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return succ("当日无回款数据", null);
	}
	
	/**
	 * 统计用户回款信息
	 * @return
	 */
	@ActionKey("/countBackInfo4User")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="countBackInfo4User", cachedKeyParm="date|@userCode",mode="remote" , time=5)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message countBackInfo4User(){
		String date = getPara("date");
		if(StringUtil.isBlank(date)){
			return error("01", "参数错误", "");
		}
		String userCode = getUserCode();
		Object[] countByBackDate = loanTraceService.countByBackDate(date, userCode);
		long countByBackMonth = loanTraceService.countByBackMonth(userCode);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("count4Date", countByBackDate[0]);
		map.put("count4Amount", countByBackDate[1]);
		map.put("count4Interest", countByBackDate[2]);
		map.put("count4Month", countByBackMonth);
		return succ("获取成功", map);
	}
	
	
	/**
	 * 获取我的最近投资
	 * @param pageNumber
	 * @param pageSize
	 * @param result
	 * @return
	 */
	@ActionKey("/queryInvest4Latest")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryInvest4Latest(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		//获取用户标识
		String userCode = getUserCode();
		
		Page<LoanTrace> loanTraces = loanTraceService.queryInvest4Latest(pageNumber, pageSize,userCode);
		
		return succ("获取成功", loanTraces);
	}
	
//	/**
//	 * 查询用户是否开启自动投标
//	 * @return
//	 */
//	@ActionKey("/queryAutoLoanState")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message queryAutoLoanState(){
//		String userCode = getUserCode();
//		
//		//查询自动投标开启状态
//		AutoLoan_v2 autoLoan = autoLoanService.findByUserCode(userCode,"A");
//		//Long autoIndex = autoLoanService.getAutoIndex(userCode);
//		Map<String, Object> result = new HashMap<String, Object>();
//		//result.put("autoIndex", autoIndex);
//		int isAvailable = 0;
//		if(autoLoan != null){
//			isAvailable = 1;
//		}
//		result.put("isAvailable", isAvailable);
//		//获取可用余额
//		long avBalance = fundsServiceV2.findAvBalanceById(userCode);
//		
//		result.put("avBalance", avBalance);
//		return succ("查询完成", result);
//	}
	
	
//	/**
//	 * 停用自动投标
//	 * @return
//	 */
//	@ActionKey("/setAutoLoanState")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message setAutoLoanState(){
//		String userCode = getUserCode();
//		autoLoanService.stopAutoLoan_v2(userCode);
//		//记录日志
//		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "设置自动投标状态成功");
//		return succ("成功关闭自动投标", "");
//	}
	
	/**
	 * 停用自动投标
	 * @return
	 */
	@ActionKey("/setAutoLoanState")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message setAutoLoanState(){
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		//AutoLoan_v2 autoLoanv2 = autoLoanService.findByUserCode(userCode);
		String jxAccountId = user.getStr("jxAccountId");
		if (StringUtil.isBlank(jxAccountId)) {
			return error( "03", "请先激活江西存管账户", "");
		}
//		String jxOrderId = autoLoanv2.getStr("jxOrderId");
//		String deadLine = autoLoanv2.getStr("deadLine");
//		if(DateUtil.compareDateByStr("yyyyMMdd", deadLine, DateUtil.getNowDate())>=0){
//			try {
//				Map<String, String> tempMap = JXController.autoBidAuthCancel(jxAccountId, jxOrderId, getResponse());
//				if(null == tempMap || !"00000000".equals(tempMap.get("retCode"))){
//					return error("01", "自动投标关闭失败", "");
//				}
//			} catch (Exception e) {
//				return error("02", "自动投标关闭失败，网络连接异常", "");
//			}
//		}
		autoMapService.changeAutoState2C(userCode);
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "设置自动投标状态成功");
		return succ("成功关闭自动投标", "");
	}
	
	/**
	 * 查询自动投标配置设定
	 * @return
	 */
	@ActionKey("/queryAutoLoanSettings")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryAutoLoanSettings(){
		String userCode = getUserCode();
		AutoLoan_v2 autoLoan_v2 = autoLoanService.findByUserCode(userCode);
		long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");//加息额度
		long cashCount = ticketService.countByTtypeAndTstate(userCode, "A", "A");//现金抵用券数量
		long interestCount = ticketService.countByTtypeAndTstate(userCode, "C", "A");//加息券数量
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("autoLoan", autoLoan_v2);
		map.put("rewardRateAmount", Number.longToString(rewardRateAmount));
		map.put("cashCount", cashCount);
		map.put("interestCount", interestCount);
		return succ("查询完成", map);
	}
	
	/**
	 * 查询自动投标排位明细
	 * @return
	 */
	@ActionKey("/queryAutoLoanRankNum")
	@AuthNum(value=999)
//	@ResponseCached(cachedKey="queryAutoLoanRankNum", cachedKeyParm="@userCode",mode="remote" , time=1)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryAutoLoanRankNum(){
		int x = 0;
		try {
			String userCode = getUserCode();
			x = autoMapService.queryRankVal(userCode);
		} catch (Exception e) {
			x = 0;
		}
		return succ("查询完成", x);
	}
	
	/**
	 * 查询自动投标排位明细
	 * @return
	 */
	@ActionKey("/queryAutoLoanRankDetail")
	@AuthNum(value=999)
	//@ResponseCached(cachedKey="queryAutoLoanRankDetail", cachedKeyParm="@userCode",mode="remote" , time=1)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryAutoLoanRank(){
		String userCode = getUserCode();
		Map<String,Object> result = new HashMap<String, Object>();
		if(autoMapService.validateAutoState(userCode) != 1){
			result.put("rankNum", -1);
			result.put("rankDetail", null);
		}else{
			int rankVal = autoMapService.queryRankVal(userCode);
			if(rankVal>0){
				int rankNum = autoMapService.queryRankNum(rankVal);
				AutoLoan_v2 aa = autoLoanService.findByUserCode(userCode, "A");
				List<Record> list = autoMapService.queryRankDetail(rankVal,aa.getInt("autoMinLim"),aa.getInt("autoMaxLim"));
				result.put("rankNum", rankNum);
				result.put("rankDetail", list);
			}else{
				result.put("rankNum", -1);
				result.put("rankDetail", null);
			}
		}
		return succ("查询完成", result);
	}
	
	/**
	 * 保存自动投标配置
	 * @return
	 */
	@ActionKey("/saveAutoLoanSettings")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void saveAutoLoanSettings(){
		Message msg = null;
		if(!CommonUtil.jxPort){
			msg = error("12", "存管系统功能暂未上限,无法进行此操作", "" );
			renderJson(msg);
			return;
		}
		String userCode = getUserCode();
//		boolean unusualUserCode = loanTraceService.unusualUserCode(userCode);
//		if(unusualUserCode){
//			msg = error("13", "资金异常", "");
//			renderJson(msg);
//			return;
//		}
		User user = userService.findById(userCode);
		String evaluationLevel = user.getStr("evaluationLevel");
		if(StringUtil.isBlank(evaluationLevel)){
			msg = error("F1", "请先完成风险测评再继续设置", "" );
			renderJson(msg);
			return;
		}
		//验证是否开通缴费授权
		UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
		if(!userTermsAuth.isPaymentAuth()){
			msg = error("24", "缴费授权未开通,无法开启自动", "");
			renderJson(msg);
			return;
		}
		AutoLoan_v2 autoLoan = getModel(AutoLoan_v2.class,"autoLoan");
		//验证数据有效性
		if(null == autoLoan){
			msg = error("01", "请正确填写自动投标信息", "");
			renderJson(msg);
			return;
		}
		UserInfo userInfo = userInfoService.findById(userCode);
		//获取用户电子账户
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			msg = error("02", "未开通存管", "");
			renderJson(msg);
			return;
		}
		if(userInfo == null){
			msg = error("02", "用户认证信息异常", "");
			renderJson(msg);
			return;
		}
		//获取单次最大投标金额
		String txAmount = String.valueOf(autoLoan.getLong("onceMaxAmount")/100);
		//过期日期
//		String deadLine = "20180520";
		String deadLine = autoLoan.getStr("deadLine");
		//授权码
		AutoLoan_v2 autoLoanv2 = autoLoanService.findByUserCode(userCode);
		String smsCode = "";
	
		////--------------------------start
		
		if(autoLoan.getLong("onceMaxAmount") < 5000){
			autoLoan.set("onceMaxAmount", 99999900L);
		}
		if(autoLoan.getLong("onceMinAmount") < 5000){
			autoLoan.set("onceMinAmount", 5000L);
		}
		if(autoLoan.getLong("onceMaxAmount") < 5000){
			msg = error("05", "最大金额必须大于50", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getLong("onceMinAmount") < 5000){
			msg = error("06", "最小投标金额必须大于50", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getLong("onceMinAmount") > autoLoan.getLong("onceMaxAmount")){
			msg = error("07", "最小投标金额不能大于最大投标金额", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getInt("autoMinLim")>autoLoan.getInt("autoMaxLim")){
			msg = error("07", "贷款期限范围最小期限不能大于最大期限", "");
			renderJson(msg);
			return;
		}
		String nowDate = DateUtil.getNowDate();
		int x = DateUtil.compareDateByStr("yyyyMMdd", nowDate, deadLine);
		if (x != -1){
			msg = error("13", "过期时间应大于当前日期", "");
			renderJson(msg);
			return;
		}
		if(StringUtil.isBlank(autoLoan.getStr("productType"))){
			autoLoan.set("productType", "");
		}
		
		String priorityMode = getPara("priorityMode","N");
		
		String useTicket = getPara("useTicket","N");
		
		String autoType = "B";
		
		try {
			autoType = autoLoan.getStr("autoType");
			if(StringUtil.isBlank(autoType)){
				autoType = "B";
			}
		} catch (Exception e) {
			autoType = "B";
		}
		try {
			if(useTicket.equals("A")){
				if(StringUtil.isBlank(priorityMode)){
					priorityMode = "A";
				}
				if(priorityMode.equals("A") == false && priorityMode.equals("B") == false){
					priorityMode = "A";
				}
			}
			if(useTicket.equals("C")){
				if(StringUtil.isBlank(priorityMode)){
					priorityMode = "A";
				}
				if(priorityMode.equals("A") == false && priorityMode.equals("C") == false){
					priorityMode = "A";
				}
			}
		} catch (Exception e) {
			msg = error("08", "系统错误", "");
			renderJson(msg);
			return;
		}
		//保存
		String userName = "";
		try {
			userName = userService.findByField(userCode, "userName").getStr("userName");
			if(StringUtil.isBlank(userName)){
				msg = error("09", "用户名异常",false);
				renderJson(msg);
				return;
			}
		} catch (Exception e) {
			msg = error("10", "用户名异常",false);
			renderJson(msg);
			return;
		}
		//-----------------------------end
		//存临时信息
		JSONObject tempData = new JSONObject();
		//自动类型 autoType
		tempData.put("autoType", autoLoan.getStr("autoType"));
		//最大金额 onceMaxAmount
		tempData.put("onceMaxAmount", autoLoan.getLong("onceMaxAmount"));
		//最小金额 onceMinAmount
		tempData.put("onceMinAmount", autoLoan.getLong("onceMinAmount"));
		//最大期限 autoMaxLim
		tempData.put("autoMaxLim", autoLoan.getInt("autoMaxLim"));
		//最小期限 autoMinLim
		tempData.put("autoMinLim", autoLoan.getInt("autoMinLim"));
		//投资类型 refundType
		tempData.put("refundType", autoLoan.getStr("refundType"));
		//投资券类型 useTicket
		tempData.put("useTicket", useTicket);
		//优先方式priorityMode
		tempData.put("priorityMode", priorityMode);
		//短信验证码msgAutoBid
		tempData.put("msgAutoBid", smsCode);
		//过期时间deadline
		tempData.put("deadLine", deadLine);
		String temp = tempData.toString();
		int fff =1;
		try {
			fff = autoMapService.validateAutoState(userCode);
		} catch (Exception e) {
			fff = 1;
		}
		try {
				if(fff==1){
					//查询签约状态
					long  onceMaxAmount = autoLoan.getLong("onceMaxAmount");
					if(onceMaxAmount==autoLoanv2.getLong("onceMaxAmount")&&deadLine.equals(autoLoanv2.getStr("deadLine"))){
						boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, autoLoan.getLong("onceMinAmount"), autoLoan.getLong("onceMaxAmount"), autoLoan.getInt("autoMinLim"), autoLoan.getInt("autoMaxLim"), autoLoan.getStr("refundType"), "ABCD", priorityMode, useTicket, autoType,autoLoanv2.getStr("jxOrderId"),deadLine);
						if(result){
							String info="";
							info+="最小金额:"+autoLoan.getLong("onceMinAmount");
							info+="|最大金额:"+autoLoan.getLong("onceMaxAmount");
							info+="|最小期限:"+autoLoan.getInt("autoMinLim");
							info+="|最大期限:"+autoLoan.getInt("autoMaxLim");
							info+="|还款方式:"+autoLoan.getStr("refundType");
							info+="|自动投标类型:"+autoType;
							info+="|使用理财券类型:"+useTicket;
							info+="|理财券使用优先方式:"+priorityMode;
							info+="|过期时间:"+deadLine;
							info+="|订单号:"+autoLoanv2.getStr("jxOrderId");
							BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置成功",info);
							msg = succ("设置成功", "OK");
							renderJson(msg);
							return;
							
						}else{
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败", null);
							msg = error("15", "设置失败", "");
							renderJson(msg);
							return;
						}
					}
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "首次保存自动投标送券异常", e);
			msg = error("13", "保存失败", "");
			renderJson(msg);
			return;
		}
		String name=userInfo.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			msg = error("14", "身份证号解密异常", "");
			renderJson(msg);
			return;
		}
		String forgotPwdUrl =CommonUtil.ADDRESS+"/C01";
		String retUrl = CommonUtil.ADDRESS+"/A04";
		String notifyUrl = CommonUtil.CALLBACK_URL+"/autoBidResponse";
		//跳转江西银行存管
		String type = getPara("type");
		if(!"y".equals(type)){
			//调用多合一合规授权接口
			JXController.termsAuthPage(jxAccountId, name, idNo, "1", "1", "", "", "", txAmount, deadLine, "", "", "", "", "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(),temp);
			renderNull();
			return;
		}else{
			msg = succ("01", "验证成功");
			renderJson(msg);
			return;
		}
	}
//	@ActionKey("/saveAutoLoanSettings")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message saveAutoLoanSettings(){
//		
//		AutoLoan_v2 autoLoan = getModel(AutoLoan_v2.class,"autoLoan");
//		
//		//验证数据有效性
//		if(null == autoLoan){
//			return error("01", "请正确填写自动投标信息", "");
//		}
//		
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			autoLoan.set("onceMaxAmount", 99999900L);
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			autoLoan.set("onceMinAmount", 5000L);
//		}
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			return error("01", "最大金额必须大于50", "");
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			return error("01", "最小投标金额必须大于50", "");
//		}
//		if(autoLoan.getLong("onceMinAmount") > autoLoan.getLong("onceMaxAmount")){
//			return error("01", "最小投标金额不能大于最大投标金额", "");
//		}
//		
//		if(StringUtil.isBlank(autoLoan.getStr("productType"))){
//			autoLoan.set("productType", "");
//		}
//		
//		String priorityMode = getPara("priorityMode","N");
//		
//		String useTicket = getPara("useTicket","N");
//		
//		String autoType = "B";
//		
//		try {
//			autoType = autoLoan.getStr("autoType");
//			if(StringUtil.isBlank(autoType)){
//				autoType = "B";
//			}
//		} catch (Exception e) {
//			autoType = "B";
//		}
//		try {
//			if(useTicket.equals("A")){
//				if(StringUtil.isBlank(priorityMode)){
//					priorityMode = "A";
//				}
//				if(priorityMode.equals("A") == false && priorityMode.equals("B") == false){
//					priorityMode = "A";
//				}
//			}
//			if(useTicket.equals("C")){
//				if(StringUtil.isBlank(priorityMode)){
//					priorityMode = "A";
//				}
//				if(priorityMode.equals("A") == false && priorityMode.equals("C") == false){
//					priorityMode = "A";
//				}
//			}
//		} catch (Exception e) {
//			
//		}
//		//保存
//		String userCode = getUserCode();
//		String userName = "";
//		try {
//			userName = userService.findByField(userCode, "userName").getStr("userName");
//			if(StringUtil.isBlank(userName)){
//				return error("21", "用户名异常",false);
//			}
//		} catch (Exception e) {
//			return error("21", "用户名异常",false);
//		}
//		autoLoan.set("userCode", userCode);
//		autoLoan.set("productType", "ABCD");
//		int fff =1;
//		try {
//			fff = autoMapService.validateAutoState(userCode);
//		} catch (Exception e) {
//			fff = 1;
//		}
//		boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, autoLoan.getLong("onceMinAmount"), autoLoan.getLong("onceMaxAmount"), autoLoan.getInt("autoMinLim"), autoLoan.getInt("autoMaxLim"), autoLoan.getStr("refundType"), autoLoan.getStr("productType"),priorityMode,useTicket,autoType);
//		if(result == false){
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败", null);
//			return error("01", "保存失败", "");
//		}
//		try {
//			if(fff==0){
//				String nowDate = DateUtil.getNowDate();
//				int x = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161211" );
//				int y = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161218" );
//				if((x == 0 || x == 1) && (y == -1 || y == 0)){
//					//11张30元券+11张50元券
//					for (int i = 0; i < 11; i++) {
//						ticketService.saveADV(userCode, "30元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
//						ticketService.saveADV(userCode, "50元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "首次保存自动投标送券异常", e);
//		}
//		String info="";
//		info+="最小金额:"+autoLoan.getLong("onceMinAmount");
//		info+="|最大金额:"+autoLoan.getLong("onceMaxAmount");
//		info+="|最小期限:"+autoLoan.getInt("autoMinLim");
//		info+="|最大期限:"+autoLoan.getInt("autoMaxLim");
//		info+="|还款方式:"+autoLoan.getStr("refundType");
//		info+="|自动投标类型:"+autoLoan.getStr("autoType");
//		info+="|使用理财券类型:"+useTicket;
//		info+="|理财券使用优先方式:"+priorityMode;
//		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置成功",info);
//		
//		return succ("保存成功", "");
//	}

	/**
	 * 保存自动投标配置
	 * @return
	 */
	@ActionKey("/addAutoLoanTickets")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message addAutoLoanTickets(){
		String sckey = getPara("sckey","");
		if(sckey.equals("pi3.1415926535898") == false){
			return error("00000000000000", "", false);
		}
		String userCode = getPara("pUserCode");
		
//		boolean validateDate = false;
//		String nowDate = DateUtil.getNowDate();
//		int x = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161211" );
//		int y = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20170110" );
//		if((x == 0 || x == 1) && (y == -1 || y == 0)){
//			validateDate = true;
//		}
//		if(validateDate==false){
//			return error("98", "时间过了", false);
//		}
		int fff = autoMapService.validateAutoState(userCode);
		//第一次保存
		if(fff==0){
			//11张30元券+11张50元券
			for (int i = 0; i < 11; i++) {
				ticketService.saveADV(userCode, "30元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
				ticketService.saveADV(userCode, "50元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
			}
			return succ("ok", true);		
		}
		return error("99", "不是处了", false);
	}
	
	
	
//	/**
//	 * 保存自动投标配置
//	 * @return
//	 */
//	@ActionKey("/saveAutoLoanSettings")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message saveAutoLoanSettings(){
//		
//		AutoLoan_v2 autoLoan = getModel(AutoLoan_v2.class,"autoLoan");
//		
//		//验证数据有效性
//		if(null == autoLoan){
//			return error("01", "请正确填写自动投标信息", "");
//		}
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			autoLoan.set("onceMaxAmount", 99999999L);
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			autoLoan.set("onceMinAmount", 5000L);
//		}
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			return error("01", "最大金额必须大于50", "");
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			return error("01", "最小投标金额必须大于50", "");
//		}
//		if(autoLoan.getLong("onceMinAmount") > autoLoan.getLong("onceMaxAmount")){
//			return error("01", "最小投标金额不能大于最大投标金额", "");
//		}
//		
//		if(StringUtil.isBlank(autoLoan.getStr("productType"))){
//			autoLoan.set("productType", "");
//		}
//		
//		//保存
//		String userCode = getUserCode();
//		autoLoan.set("userCode", userCode);
//		boolean result = autoLoanService.saveOrUpdateAutoLoan_v2(autoLoan);
//		if(result == false){
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败", null);
//			return error("01", "保存失败", "");
//		}
//		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置成功");
//		return succ("保存成功", "");
//	}

	
	
	/**
	 * 投标
	 * @param loancode		标书编码
	 * @param usercode		投标人用户编码
	 * @param amounts		投标金额
	 * @return 				返还标书剩余额度
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/bidding")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message bidding(){
		
		User user = userService.findById(getUserCode());
		String evaluationLevel = user.getStr("evaluationLevel");
		if(StringUtil.isBlank(evaluationLevel)){
			return error("F1", "请先完成风险测评再继续投资", null );
		}
		String loanCode = getPara("loanCode") ;
		
		long amount = 0;
		String ticketCodes = "";

		try {
			amount = getParaToLong("payAmount",(long) 0);
		} catch (Exception e) {
			return error("21", "投标金额错误[" + getPara("payAmount")+"]", null );
		}
		
		if( amount <= 0 ){
			return error("30", "投标金额错误!", null );
		}
		if( StringUtil.isBlank( loanCode ) == true ){
			return error("31", "参数错误!", null ) ;
		}
		if( loanCode.length() != 32){
			return error("32", "参数错误!", null ) ;
		}
		
		
		String orderId = getPara("orderId","");
		String orgOrderId = String.valueOf(Memcached.get("bidApply"+orderId));
		if(StringUtil.isBlank(orgOrderId) || !orgOrderId.equals(orderId)){
			// 支付通道
			if (CommonUtil.jxPort) {	// 江西银行存管
				String jxAccountId = user.getStr("jxAccountId");
				if (!JXController.isJxAccount(user)) {
					return error("22", "用户还未激活存管账户", "");
				}
				
				// 验证存管账户是否是否设置交易密码
				Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
				if (pwdMap == null || !"1".equals(pwdMap.get("pinFlag"))) {
					return error("24", "请先设置存管账户交易密码", "");
				}
				
				// 验证存管账户是否绑卡
				Map<String, Object> cardDetail = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
				if (cardDetail != null && "00000000".equals(cardDetail.get("retCode"))) {
					List<Map<String, String>> list = (List<Map<String, String>>) cardDetail.get("subPacks");
					if (list == null || list.size() <= 0) {
						return error("29", "请先绑定银行卡", "");
					}
				}
				
				// 验证存管账户是否开通缴费授权
				Map<String, String> authDetail = JXQueryController.termsAuthQuery(jxAccountId);
				if ("1".equals(authDetail.get("paymentAuth"))) {	// 已开通缴费授权，验证是否已过期
					String paymentDeadline = authDetail.get("paymentDeadline");
					int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline, DateUtil.getNowDate());
					if (x < 0) {
						return error("25", "缴费授权已过期", "");
					}
				} else {
					return error("25", "请先开通缴费授权", "");
				}
				
				// 验证用户资金是否正常
				if (!fundsServiceV2.checkBalance(user)) {
					return error("23", "用户资金异常", "");
				}
				
				// 验证用户可用余额是否足够
				long avBalance = fundsServiceV2.findAvBalanceById(user.getStr("userCode"));
				if (avBalance < amount) {
					return error("23", "可用余额不足", "");
				}
			} else if (CommonUtil.fuiouPort) {	// 富友存管
				// TODO 富友验证
			} else {
				return error("99", "存管接口对接中...", "");
			}
		}
		try {
			ticketCodes = getPara("ticket",Tickets.rewardRateInterestTcode);//默认使用会员等级加息
		} catch (Exception e) {
			ticketCodes = "";
		}
		
		return doLoan4NewBidding(loanCode , amount , user,"M"  ,ticketCodes , 0,orderId) ;	
	}
	/**
	 * 新的投标接口
	 * 新增参数 ticketCode
	 * @param loanCode
	 * @param amount
	 * @param user
	 * @param loantype
	 * @param rankValue
	 * @param ticketCodes
	 * @param onceMinAmount
	 */
	public Message doLoan4NewBidding(String loanCode , long amount , User user,String loantype  , String ticketCodes ,long onceMinAmount,String orderId){
		String userCode = user.getStr("userCode") ;
		LoanInfo loan = loanInfoService.findById(loanCode) ;
		int ticketAmount = 0 ;//现金券的抵扣金额
		long exAmount = 0;//现金券的使用条件，最少投资金额,默认0
		int rewardticketrate=0;//加息券加的利息
		String bonusFlag="0";//是否使用红包
		String bonusAmount="";//抵扣红包金额
		String tickettype="";
		List<Tickets> tickets = new ArrayList<Tickets>();//所有类型券的集合
		
		//判断标书是否存在并状态是否正常
		if( loan == null || "J".equals(loan.getStr("loanState")) == false ){
			return error("02", "未找到相关标信息", null ) ;
		}
		if(SysEnum.productType.E.val().equals(loan.getStr("productType"))&&amount%10000>0){
			return error("02", "易分期投标金额应为100的整数倍", null ) ;
		}
		/*
		 * 	1,检查标设置
		 * 	2,判断用户是否负责投标条件
		 * 	3,投标
		 */
		int minLoanAmount = loan.getInt("minLoanAmount");
		long loanBalance = loan.getLong("loanBalance");
		
		//判断是否满标
		if( loanBalance <= 0 ){
			return error("03", "已满标，请查看其他标", null ) ;
		}
		if(amount>loanBalance){
			return error("04", "投标金额应小于标的剩余可投金额", null ) ;
		}
		//检查是否已经到投标时间
		String releaseDateTime = loan.getStr("releaseDate")+loan.getStr("releaseTime");
		if( "M".equals(loantype) == true ){
			int compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), releaseDateTime);
			if(compareDateByStr != 0 && compareDateByStr != 1){
				return error("09", "未到投标时间，请查看其他标", null );
			}
		}else{
			//rankValue = autoLoanService.queryRank(userCode)[1];
			//rankValue = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode=?",userCode).getInt("aid");
		}
		
		/*
		 *	增加新手标检查
		 *		1、通过判断benefits4new 判断该为是否为新手标
		 *		2、判断当前时间是否为发标时间9点以后，如果是，则不坐新手限制。
		 *		3、新手限制条件，判断用户活跃积分是否小于100，小于等于100则为新手
		 */
		int benefits4new = loan.getInt("benefits4new");
		if( benefits4new > 0 ){
			if(Tickets.rewardRateInterestTcode.equals(ticketCodes)){//会员自动加息不适用新手标
				ticketCodes = "";
			}
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			//检查是否过了不限制的时间
			String passTime = "213000";//默认晚9点半后不限制投新手标
			try {
				passTime = (String) CACHED.get("S3.xsbgqsj");
			} catch (Exception e) {
				
			}
			//String openDateTime = DateUtil.getNowDate() + passTime;
			String openDateTime = loan.getStr("releaseDate") + passTime;
			int compareDateByStr = -1;
			compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), openDateTime);
			if(compareDateByStr<1){
				long userScore = user.getLong("userScore");
				int tmp = 3000;
				try {
					tmp = tmp * Integer.valueOf((String) CACHED.get("S3.xsjfbs"));
				} catch (Exception e) {
				}
				/*if( userScore >= tmp ){
					return error("15", "此标为新人专享", null ) ;
				}*/
				
				String regDate = user.getStr("regDate");//注册日期
				String vipDate = "20180319";//vip上线日期
				
				int x = DateUtil.compareDateByStr("yyyyMMdd", regDate, vipDate);
				if(x >= 0 || (x < 0 && userScore < tmp)){
					//新手标每个用户限额一万，2018.3.19之后注册或之前注册且活跃积分少于30
					long permitAmount=1000000 - loanTraceService.sumNewAmountByUserCode(userCode);//新手标剩余额度
					if(permitAmount < minLoanAmount){
						return error("15", "此标为新人专享", null ) ;
					}else if(amount > permitAmount){
						amount = permitAmount;
					}
				}else{
					return error("15", "此标为新人专享", null);
				}
				
			}
			loan.set("rewardRateByYear", (rewardRateByYear + benefits4new ) ) ;	//将新手奖励增加到奖励年利率里
			
		}
		
		
		/**
		 * 	TODO 解析券，记录券信息
		 */
		if( StringUtil.isBlank(ticketCodes) == false ){
			String[] tcs = ticketCodes.split(",");
			if(tcs!=null && tcs.length > 0 ){
				for (int i = 0; i < tcs.length; i++) {
					Tickets ticket=null;
					if(Tickets.rewardRateAomuntTcode.equals(tcs[i])){
						//查询加息额度
						long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
						if(rewardRateAmount<amount){
							return error("15", "加息额度不足", null);
						}
						if(benefits4new>0){
							return error("15", "新手标不能使用加息额度", null);
						}
						ticket=Tickets.getTmpTickets();
						tickettype="D";
					}else if (Tickets.rewardRateInterestTcode.equals(tcs[i])) {//会员等级自动加息
						Integer vipLevel = user.getInt("vipLevel");//获取会员等级
						VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
						int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
						ticket = rewardInterest>0?Tickets.getGradeTickets(rewardInterest):null;
						tickettype="E";
					}else{
						ticket = ticketService.findByCode2(tcs[i]);
						}
					if( ticket != null ){
						tickets.add(ticket);
						if(ticket.getStr("ttype").equals("A")){//现金券
							//bonusFlag="1";//modified 20180425
							if(loan.getStr("productType").equals(SysEnum.productType.E.val())){
								return error("15", "易分期不能使用现金券", null);
							}
							ticketAmount = ticketAmount + ticket.getInt("amount");
							//bonusAmount=String.valueOf(ticketAmount*0.01);
						}else if(ticket.getStr("ttype").equals("C")){//加息券、加息额度、会员等级加息
//							int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
//							if(releaseDate<20171118){
//								return error("15", "2017-11-18之前的标不支持使用加息券", null);
//							}
							if(benefits4new>0){
								return error("15", "新手标不能使用加息", null);
							}
							int rewardRateByYear2=loan.getInt("rewardRateByYear");
							rewardticketrate+=ticket.getInt("rate");
							loan.set("rewardRateByYear", (rewardRateByYear2 + rewardticketrate ) ) ;//将加息券奖励增加到奖励年利率里
						}
						String strUseEx = ticket.getStr("useEx");
						if( StringUtil.isBlank(strUseEx) == false ){
							JSONObject json = JSONObject.parseObject( strUseEx ) ;
							int limit = json.getIntValue("limit");
							int rate = json.getIntValue("rate");
							long tmp = json.getLongValue("amount") ;
							//加息券的额度应大于投标金额--现金券的额度应小于投标金额 ws
							if(ticket.getStr("ttype").equals("A")){
								if(tmp!=0 && amount < tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}else if(ticket.getStr("ttype").equals("C")){
								if(tmp!=0 && amount > tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}
							if(limit != 0 && loan.getInt("loanTimeLimit") < limit){
								return error("77", "请检查理财券使用期限是否符合条件", false);
							}
							if(rate != 0 && (loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")) < rate ){
								return error("77", "请检查理财券使用利率是否符合条件", false);
							}
							//判断是否符合券可投标期限 20170727 ws
							String la=String.valueOf(loan.getInt("loanTimeLimit"));
							if(la.length()==1){
								la="0"+la;
							}
							if(null!=ticket.getStr("loanMonth")&&ticket.getStr("loanMonth").length()>1&&ticket.getStr("loanMonth").indexOf(la)<0){
								return error("77", "请检查理财券使用标期是否符合条件", false);
							}
							//end
							if(tmp > exAmount){
								exAmount = tmp;
							}
						}
					}
				}
			}
		}
		
		//去小数
		amount = amount - amount%100 ;
		
		//真实投标金额(投标金额-券金额)
		//long trueAmount =0;
		
		//long avBalance = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance").getLong("avBalance");
//		if(avBalance < trueAmount){
//			return error("999", "可用余额不足!", null);
//		}
		
		int maxLoanAmount = loan.getInt("maxLoanAmount");
		if( maxLoanAmount > 0 && amount > maxLoanAmount ){
			//如果最大投标金额 小于 其中一个现金券的使用条件金额
			if(tickets != null && tickets.size() > 0){
				if(maxLoanAmount < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
		}
		//投标金额小于最小投标金额时返回错误
		if( amount < minLoanAmount && loanBalance >= minLoanAmount){
			return error("04", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
		}
		
		//当可投金额小于投标金额时, 自动将可投金额置为投标金额
		if( amount > loanBalance ){
			if(tickets != null && tickets.size() > 0){
				if(loanBalance < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			amount = loanBalance ;
			
			if( onceMinAmount > amount ){
				return error("14", "单次投标金额不满足最小投标金额要求", minLoanAmount ) ;
			}
			
			//trueAmount = amount - ticketAmount;
		}
		
		int maxLoanCount = loan.getInt("maxLoanCount");
		Map<String , Long> totalMap = loanTraceService.totalByLoan4user(loanCode , userCode );
		long userLoanCount = totalMap.get("count") ;
		long userTotalAmount = totalMap.get("totalAmount");
		if( userLoanCount > maxLoanCount ){
			return error("06", "投标次数已超限,当前最大投标次数：" + maxLoanCount + "次", maxLoanCount ) ;
		}
		
		//验证投标总额
		if( (userTotalAmount + amount) > maxLoanAmount ){
			amount = maxLoanAmount - userTotalAmount ;
			//trueAmount = amount - ticketAmount;
			if( amount <= 0 ){
				return error("07", "投标总额已超，您的该标的投资总额为：" + userTotalAmount/10/10 + "元" , userTotalAmount) ;
			}
		}
		//TODO 使用现金券时进行检查
		try {
			if( tickets!=null && tickets.size() > 0){
				for (int i = 0; i < tickets.size(); i++) {
					Tickets tts = tickets.get(i);
					if(!Tickets.rewardRateAomuntTcode.equals(tts.getStr("tCode"))&&!Tickets.rewardRateInterestTcode.equals(tts.getStr("tCode"))){
						if(tts.getStr("ttype").equals("A")||tts.getStr("ttype").equals("C")){
							if( loan == null ){
								return error("08","无使用场景",null);
							}
							//Tickets ticket = Tickets.ticketsDao.findById(tts.getStr("tCode") );
							
							//检查归属
							String tUserCode = tts.getStr("userCode") ;
							if( userCode.equals(tUserCode) == false ){
								return error("09","非法请求",null);
							}
							
							String tState = tts.getStr("tstate") ;
							//检查状态是否可用
							if( Tickets.STATE.A.key().equals( tState ) == false ){
								return error("10","现金券不可用[" + Tickets.STATE.valueOf(tState).desc() + "]",null);
							}
							//检查过期日期
							String nowDate = DateUtil.getNowDate() ;
							String expDate = tts.getStr("expDate");
							if( DateUtil.compareDateByStr("yyyyMMdd", nowDate , expDate ) > 0 ){
								return error("11","现金券已过期[" + expDate + "]",null);
							}
							
							//检查使用条件
							String strUseEx = tts.getStr("useEx");
							if( StringUtil.isBlank(strUseEx) == false ){
								long loanRate = loan.getInt("rateByYear") + loan.getInt("rewardRateByYear");//这里引用的rewardRateByYear已经包含了新手年利率与加息券利息
								long loanLimit = loan.getInt("loanTimeLimit") ;
								
								JSONObject json = JSONObject.parseObject( strUseEx ) ;
								//固定三个条件 amount / rate /limit 
								long useExAmount = json.getLongValue("amount") ;
								int exRate = json.getIntValue("rate") ;
								int exLimit = json.getIntValue("limit") ;
								if("A".equals(tts.getStr("ttype"))){
									if( amount < useExAmount ){
										return error("12", "单笔投资必须大于" + exAmount/10.0/10.0 + "元才可以使用!", null ) ;
									}
								}else if("C".equals(tts.getStr("ttype"))){
									if(amount > useExAmount&&useExAmount>0){
										return error("13", "单笔投资必须小于等于" + exAmount/10.0/10.0 + "元才可以使用!", null ) ;
									}
								}
								
								if( loanRate-tts.getInt("rate") < exRate ){
									return error("14", "借款标利率必须大于" + exRate/10.0/10.0 + "%才可以使用!", null ) ;
								}
								
								if( loanLimit < exLimit ){
									return error("15", "借款标期限必须大于" + exLimit + "才可以使用!", null ) ;
								}
								
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if("D".equals(tickettype)){
				return error("10", "使用加息额度异常","");
			} 
			return error("10", "理财券异常","");
		}
		
		try{
			
//			int ydjl = 0;
			String userAgent = "";
			try {
				userAgent  = getRequest().getHeader( "USER-AGENT" ).toLowerCase(); 
			} catch (Exception e) {
				userAgent = "";
			}
			if(null == userAgent){    
	            userAgent = "";    
	        }
	        //判断是否为移动端访问  
	        if( CommonUtil.check(userAgent)){  
	        	//ydjl = 20;
	        	loantype = "N";
	        }
	        //modified 20180425
	        String jxAccountId=user.getStr("jxAccountId");
	        String productId=loan.getStr("loanCode");
	        String txAmount=StringUtil.getMoneyYuan(amount);//投标金额
	        String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
	        String retUrl=CommonUtil.ADDRESS+"/Z02";
	        String notifyUrl = CommonUtil.CALLBACK_URL + "/bidApplyCallback";
	        JSONArray ja=null;
	        if( tickets != null && tickets.size() > 0){
				 ja = new JSONArray();
				for (int i = 0; i < tickets.size(); i++) {
					Tickets ticket = tickets.get(i);
					JSONObject ticketInfo = new JSONObject() ;
					ticketInfo.put("code", ticket.getStr("tCode") ) ;
					ticketInfo.put("type", ticket.getStr("ttype")) ;
					ticketInfo.put("amount", ticket.getInt("amount")) ;
					ticketInfo.put("rate", ticket.getInt("rate")) ;
					ticketInfo.put("isDel", ticket.getStr("isDel"));
					//end
					ja.add(ticketInfo);
				}
			}
	        String ticktes="";
	        if(ja!=null){
	        	ticktes=ja.toJSONString();
	        }
	        if(StringUtil.isBlank(orderId)){
	        	String orgOrderId=CommonUtil.genShortUID();
		        Memcached.set("bidApply"+orgOrderId,orgOrderId);
	        	return succ("00",orgOrderId);
	         }
	        else{
	        	JXController.bidApply(jxAccountId,orderId,txAmount, productId,"0", bonusFlag, bonusAmount,forgotPwdUrl,retUrl,notifyUrl,ticktes,getResponse());
	        }
	       
	    }catch(Exception e){
			e.printStackTrace( );
			return error("EX", "操作异常:" + e.getMessage() , null ) ;
		}
		return null;
	}
	/**
	 * 	
	 * 	新增参数 ticketCode
	 * @param loanCode
	 * @param amount
	 * @param user
	 * @param loantype
	 * @param rankValue
	 * @param autoOnceAmount
	 * @param ticketCode
	 * @param userAgent
	 * @return
	 */
	public Message doLoan4bidding(String loanCode , long amount , 
			User user,String loantype ,long rankValue , String ticketCodes ,long onceMinAmount ){
		
		String userCode = user.getStr("userCode") ;
		String jxAccountId = user.getStr("jxAccountId");
		//验证是否开通存管 ws 20170818
		if(StringUtil.isBlank(jxAccountId)){
			return error("21", "用户还未激活存管账户", "");
		}
		//用户资金验证   此方法新版本只作为自动投标使用   前面已有资金验证
//		Funds funds = fundsServiceV2.findById(userCode);
//		boolean verifyFund = JXController.checkBalance(funds.getLong("avBalance"), jxAccountId);
//		if (!verifyFund) {
//			return error("20", "用户资金异常", "");
//		}
		//end
		LoanInfo loan = loanInfoService.findById(loanCode) ;

		//判断标书是否存在并状态是否正常
		if( loan == null || "J".equals(loan.getStr("loanState")) == false ){
			return error("02", "未找到相关标信息", null ) ;
		}
		/*
		 * 	1,检查标设置
		 * 	2,判断用户是否负责投标条件
		 * 	3,投标
		 */
		int minLoanAmount = loan.getInt("minLoanAmount");
		long loanBalance = loan.getLong("loanBalance");
		
		//判断是否满标
		if( loanBalance <= 0 ){
			return error("03", "已满标，请查看其他标", null ) ;
		}
		
		//检查是否已经到投标时间
		String releaseDateTime = loan.getStr("releaseDate")+loan.getStr("releaseTime");
		if( "M".equals(loantype) == true ){
			int compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), releaseDateTime);
			if(compareDateByStr != 0 && compareDateByStr != 1){
				return error("09", "未到投标时间，请查看其他标", null );
			}
		}else{
			//rankValue = autoLoanService.queryRank(userCode)[1];
			//rankValue = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode=?",userCode).getInt("aid");
		}
		
		/*
		 *	增加新手标检查
		 *		1、通过判断benefits4new 判断该为是否为新手标
		 *		2、判断当前时间是否为发标时间9点以后，如果是，则不坐新手限制。
		 *		3、新手限制条件，判断用户活跃积分是否小于100，小于等于100则为新手
		 */
		int benefits4new = loan.getInt("benefits4new");
		if( benefits4new > 0 ){
			if(Tickets.rewardRateInterestTcode.equals(ticketCodes)){//会员自动加息不适用新手标
				ticketCodes = "";
			}
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			//检查是否过了不限制的时间
			String passTime = "213000";//默认晚9点半后不限制投新手标
			try {
				passTime = (String) CACHED.get("S3.xsbgqsj");
			} catch (Exception e) {
				
			}
			//String openDateTime = DateUtil.getNowDate() + passTime;
			String openDateTime = loan.getStr("releaseDate") + passTime;
			int compareDateByStr = -1;
			compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), openDateTime);
			if(compareDateByStr<1){
				long userScore = user.getLong("userScore");
				int tmp = 3000;
				try {
					tmp = tmp * Integer.valueOf((String) CACHED.get("S3.xsjfbs"));
				} catch (Exception e) {
				}
				/*if( userScore >= tmp ){
					return error("15", "此标为新人专享", null ) ;
				}*/
				
				String regDate = user.getStr("regDate");//注册日期
				String vipDate = "20180319";//vip上线日期
				
				int x = DateUtil.compareDateByStr("yyyyMMdd", regDate, vipDate);
				if(x >= 0 || (x < 0 && userScore < tmp)){
					//新手标每个用户限额一万，2018.3.19之后注册或之前注册且活跃积分少于30
					long permitAmount=1000000 - loanTraceService.sumNewAmountByUserCode(userCode);//新手标剩余额度
					if(permitAmount < minLoanAmount){
						return error("15", "此标为新人专享", null ) ;
					}else if(amount > permitAmount){
						amount = permitAmount;
					}
				}else{
					return error("15", "此标为新人专享", null);
				}
				
			}
			loan.set("rewardRateByYear", (rewardRateByYear + benefits4new ) ) ;	//将新手奖励增加到奖励年利率里
		}
		
		
		/**
		 * 	TODO 解析券，记录券信息
		 */
		List<Tickets> tickets = new ArrayList<Tickets>();//所有类型券的集合
		int ticketAmount = 0 ;//现金券的抵扣金额
		long exAmount = 0;//现金券的使用条件，最少投资金额,默认0
		int rewardticketrate=0;//加息券加的利息
		String tickettype="";
		if( StringUtil.isBlank(ticketCodes) == false ){
			String[] tcs = ticketCodes.split(",");
			if(tcs!=null && tcs.length > 0 ){
				for (int i = 0; i < tcs.length; i++) {
					Tickets ticket=null;
					if(Tickets.rewardRateAomuntTcode.equals(tcs[i])){
						//查询加息额度
						long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
						if(rewardRateAmount<amount){
							return error("15", "加息额度不足", null);
						}
						if(benefits4new>0){
							return error("15", "新手标不能使用加息额度", null);
						}
						ticket=Tickets.getTmpTickets();
						tickettype="D";
					}else if (Tickets.rewardRateInterestTcode.equals(tcs[i])) {//会员等级自动加息
						Integer vipLevel = user.getInt("vipLevel");//获取会员等级
						VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
						int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
						ticket = rewardInterest>0?Tickets.getGradeTickets(rewardInterest):null;
						tickettype="E";
					}else{
						ticket = ticketService.findByCode2(tcs[i]);
						}
					if( ticket != null ){
						tickets.add(ticket);
						if(ticket.getStr("ttype").equals("A")){//现金券
							ticketAmount = ticketAmount + ticket.getInt("amount");
						}else if(ticket.getStr("ttype").equals("C")){//加息券、加息额度、会员等级加息
//							int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
//							if(releaseDate<20171118){
//								return error("15", "2017-11-18之前的标不支持使用加息券", null);
//							}
							if(benefits4new>0){
								return error("15", "新手标不能使用加息", null);
							}
							int rewardRateByYear2=loan.getInt("rewardRateByYear");
							rewardticketrate+=ticket.getInt("rate");
							loan.set("rewardRateByYear", (rewardRateByYear2 + rewardticketrate ) ) ;//将加息券奖励增加到奖励年利率里
						}
						String strUseEx = ticket.getStr("useEx");
						if( StringUtil.isBlank(strUseEx) == false ){
							JSONObject json = JSONObject.parseObject( strUseEx ) ;
							int limit = json.getIntValue("limit");
							int rate = json.getIntValue("rate");
							long tmp = json.getLongValue("amount") ;
							//加息券的额度应大于投标金额--现金券的额度应小于投标金额 ws
							if(ticket.getStr("ttype").equals("A")){
								if(tmp!=0 && amount < tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}else if(ticket.getStr("ttype").equals("C")){
								if(tmp!=0 && amount > tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}
							if(limit != 0 && loan.getInt("loanTimeLimit") < limit){
								return error("77", "请检查理财券使用期限是否符合条件", false);
							}
							if(rate != 0 && (loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")) < rate ){
								return error("77", "请检查理财券使用利率是否符合条件", false);
							}
							//判断是否符合券可投标期限 20170727 ws
							String la=String.valueOf(loan.getInt("loanTimeLimit"));
							if(la.length()==1){
								la="0"+la;
							}
							if(null!=ticket.getStr("loanMonth")&&ticket.getStr("loanMonth").length()>1&&ticket.getStr("loanMonth").indexOf(la)<0){
								return error("77", "请检查理财券使用标期是否符合条件", false);
							}
							//end
							if(tmp > exAmount){
								exAmount = tmp;
							}
						}
					}
				}
			}
		}
		
		//去小数
		amount = amount - amount%100 ;
		
		//真实投标金额(投标金额-券金额)
		long trueAmount = amount-ticketAmount;
		
		long avBalance = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance").getLong("avBalance");
		if(avBalance < trueAmount){
			return error("999", "可用余额不足!", null);
		}
		
		int maxLoanAmount = loan.getInt("maxLoanAmount");
		if( maxLoanAmount > 0 && amount > maxLoanAmount ){
			//如果最大投标金额 小于 其中一个现金券的使用条件金额
			if(tickets != null && tickets.size() > 0){
				if(maxLoanAmount < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			//重新计算投标金额+券金额
			//自动投标不受标限制限制,贝贝要求的
			if( loantype.equals("A") == false )
				amount = maxLoanAmount;
			trueAmount =  amount - ticketAmount;
		}
		
		
		//投标金额小于最小投标金额时返回错误
		if( amount < minLoanAmount && loanBalance >= minLoanAmount){
			return error("04", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
		}
		
		//当可投金额小于投标金额时, 自动将可投金额置为投标金额
		if( amount > loanBalance ){
			if(tickets != null && tickets.size() > 0){
				if(loanBalance < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			//TODO 当为自动投标时，不做余额兼容，不投出有设有最小投标金额的
//			if( loantype.equals("A") == true )
//				return error("14", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
			//重新计算投标金额+券金额
			amount = loanBalance ;
			
			if( onceMinAmount > amount ){
				return error("14", "单次投标金额不满足最小投标金额要求", minLoanAmount ) ;
			}
			
			trueAmount = amount - ticketAmount;
		}
		
		int maxLoanCount = loan.getInt("maxLoanCount");
		Map<String , Long> totalMap = loanTraceService.totalByLoan4user(loanCode , userCode );
		long userLoanCount = totalMap.get("count") ;
		long userTotalAmount = totalMap.get("totalAmount");
		if( userLoanCount > maxLoanCount ){
			return error("06", "投标次数已超限,当前最大投标次数：" + maxLoanCount + "次", maxLoanCount ) ;
		}
		
		//验证投标总额
		if( (userTotalAmount + amount) > maxLoanAmount ){
			amount = maxLoanAmount - userTotalAmount ;
			trueAmount = amount - ticketAmount;
			if( amount <= 0 ){
				return error("07", "投标总额已超，您的该标的投资总额为：" + userTotalAmount/10/10 + "元" , userTotalAmount) ;
			}
		}
		
//		int zdjl = 0;//自动投标奖励利率
//		if( "A".equals( loantype ) == true ){
//			if( autoOnceAmount > amount ){
//				return error("21", "投标金额不符" , "" ) ;
//			}
//			try {
//				int x = loan.getInt("loanTimeLimit");
//				if(x>=1 && x <=6){
//					zdjl = Integer.valueOf( (String) CACHED.get("S1.autoLoanRate16"));
//				}else if(x >= 7 && x<=18){
//					zdjl = Integer.valueOf( (String) CACHED.get("S1.autoLoanRate718"));
//				}
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//				zdjl = 0;
//			}
//		}
		//TODO 更新现金券使用状态
		try {
			if( tickets!=null && tickets.size() > 0){
				for (int i = 0; i < tickets.size(); i++) {
					Tickets tts = tickets.get(i);
					//扣除加息额度 ws 20171122
					if("D".equals(tickettype)){
						fundsServiceV2.deductRewardRateAmount(userCode, amount, 1);
					}else if("E".equals(tickettype)){//会员等级自动加息
					}else{
						if(tts.getStr("ttype").equals("A")||tts.getStr("ttype").equals("C")){
							Tickets tmp = ticketService.useTicket4A(userCode, tts.getStr("tCode"), amount, loan ) ; 
							if( tmp == null ){
								return error("10", "理财券异常:"+tts.getStr("tCode"),"") ;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if("D".equals(tickettype)){
				return error("10", "使用加息额度异常","");
			} 
			return error("10", "理财券异常","");
		}
		
//		if( amount > ticketAmount)
//			amount = amount - ticketAmount ;
		/*
		 * 	1，可用资金转冻结资金
		 * 	2，减少标可用余额，失败回滚资金操作，成功增加流水
		 * 	3，增加资金流水，增加投标流水
		 */
		//操作失败则会抛出异常
		
		Funds afterFunds = null;
		boolean bidResult = false;
		try{
			
//			int ydjl = 0;
			String userAgent = "";
			try {
				userAgent  = getRequest().getHeader( "USER-AGENT" ).toLowerCase(); 
			} catch (Exception e) {
				userAgent = "";
			}
			if(null == userAgent){    
	            userAgent = "";    
	        }
	        //判断是否为移动端访问  
	        if( CommonUtil.check(userAgent)){  
	        	//ydjl = 20;
	        	loantype = "N";
	        }
			
			//可用余额转冻结余额
	        afterFunds = fundsServiceV2.avBalance2froze(userCode, trueAmount+ticketAmount );
	    	//投标 冻结投资人账户余额 2017.5.26 rain  
//	    	CommonRspData commonRspDataTender=fuiouTraceService.freeze(user, trueAmount);
	    	//江西银行电子账户 ws
	    	AutoLoan_v2 autoLoan_v2 = autoLoanService.findByUserCode(userCode);
	    	Map<String, String> result = JXController.bidAutoApply(jxAccountId, Number.longToString2(trueAmount+ticketAmount), loanCode, "0", autoLoan_v2.getStr("jxOrderId"));
			if( ("00000000").equals(result.get("retCode"))){
				
//				if(loanBalance-amount<1){
//					loanInfoService.updateLoanByFull(loanCode);//如果投满了，设置满标
//				} 
				//TODO 预投标资金流水，备注信息会记录抵扣金额
				//TODO 单个现金券会记录到投标流水
				String jxTraceCode = result.get("txDate") + result.get("txTime") + result.get("seqNo");
				String orderId = result.get("orderId");
				String authCode = result.get("authCode");
				bidResult = loanInfoService.update4prepareBid( loan, amount,
						userCode , loantype , rankValue , tickets,0,jxTraceCode,authCode) ;
				if(bidResult == true){
					//投标成功，记录资金流水
					fundsTraceService.bidTrace4auto(userCode , amount, afterFunds.getLong("avBalance"), 
							afterFunds.getLong("frozeBalance"), 0 ,ticketAmount );
					//手动投标发送短信
					if(loantype.equals("A") == false){
						try {
							String mobile = userService.getMobile(userCode);
							
							String content = CommonUtil.SMS_MSG_MLOAN.replace("[userName]", user.getStr("userName")).replace("[loanNo]", loan.getStr("loanNo"))
									.replace("[payAmount]", CommonUtil.yunsuan(amount+"", "100", "chu", 2).doubleValue()+"");
							SMSLog smsLog = new SMSLog();
							smsLog.set("mobile", mobile);
							smsLog.set("content", content);
							smsLog.set("userCode", userCode);
							smsLog.set("userName", user.getStr("userName"));
							smsLog.set("type", "12");smsLog.set("typeName", "手动投标");
							smsLog.set("status", 9);
							smsLog.set("sendDate", DateUtil.getNowDate());
							smsLog.set("sendDateTime", DateUtil.getNowDateTime());
							smsLog.set("break", "");
							smsLogService.save(smsLog);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return succ("投标成功,投标金额：" + trueAmount/10.0/10.0 + "元", null );
				}else{
//					fuiouTraceService.unFreeFunds(user, trueAmount);
					//投标失败   自动投标撤销
					JXController.bidCancel(jxAccountId, Number.longToString2(trueAmount+ticketAmount), loanCode, orderId, getResponse());
					fundsServiceV2.frozeBalance2avBalance(userCode, trueAmount );
					//回滚加息额度
					if("D".equals(tickettype)){
						fundsServiceV2.deductRewardRateAmount(userCode, amount, 0);
					}else{
					if(tickets != null && tickets.size() > 0){
						for (int i = 0; i < tickets.size(); i++) {
							//代金券回滚
							ticketService.rollBackTicket(tickets.get(i).getStr("tCode"));
						}
					}
					} 
//					return error("21", "投标流水添加失败", "");
					return error("21", "投标失败", "");
				}
			}else{
				//操作失败，解冻投资人的金额  2017.6.7 rain
				//冻结资金回滚
				fundsServiceV2.frozeBalance2avBalance(userCode, trueAmount );
				//回滚加息额度
				if("D".equals(tickettype)){
					fundsServiceV2.deductRewardRateAmount(userCode, amount, 0);
				}else{
				if(tickets != null && tickets.size() > 0){
					for (int i = 0; i < tickets.size(); i++) {
						//代金券回滚
						ticketService.rollBackTicket(tickets.get(i).getStr("tCode"));
					}
				}
				}
				return error("08", "存管系统异常，投标失败" , null);
			}
		}catch(Exception e){
			e.printStackTrace( );
			return error("EX", "操作异常:" + e.getMessage() , null ) ;
		}
		
	}
	
	/**
	 * 我要理财页面-分页及条件查询标书投资项目
	 * @param pageNumber
	 * @param pageSize
	 * @param result
	 * @return
	 */
	@ActionKey("/queryFinancialBid")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="t_queryFinancialBid", cachedKeyParm="pageNumber|pageSize|type|minLimit|maxLimit|productType",mode="remote" , time=2)
	public Message queryFinancialBid(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String type = getPara("type");
		String minLimit = getPara("minLimit");
		String maxLimit = getPara("maxLimit");
		
		String productType = getPara("productType","");
		
		String loanState = "J,M,N,O,P,Q,R";
		
		Page<LoanInfo> loanInfos = loanInfoService.findByPortal(pageNumber, pageSize,loanState,type,productType,minLimit,maxLimit);
		
		//20171109 修改前台显示奖励年利率，仅限双十一活动期间发布的标
		List<LoanInfo> list = loanInfos.getList();
		for (int i = 0; i < list.size(); i++) {
			LoanInfo loanInfo = list.get(i);
			if (!StringUtil.isBlank(loanInfo.getStr("releaseDate"))) {
				long releaseDate = Long.parseLong(loanInfo.getStr("releaseDate"));
				if (loanInfo.getInt("benefits4new") == 0) {
					if (releaseDate >= 20171111 && releaseDate <= 20171117) {
						loanInfo.put("rewardRateByYear", 400);
						loanInfo.put("ifShow", "yes");
					} else {
						loanInfo.put("ifShow", "no");
					}
				}
			}
		}
		//end
		
		//转让中债权总数
		Integer count = loanTransferService.queryTransferCount("A");
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("loanInfos", loanInfos);
		map.put("serverTime", DateUtil.getNowDateTime());
		map.put("transferCount", count);
		return succ("获取成功", map);
	}
	
	
	/**
	 * 我要理财页面-查询详细的标书信息页面(已经登录)
	 * @return
	 */
	@ActionKey("/queryBidDetail")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryBidDetail", cachedKeyParm="loanCode|@userCode",mode="remote" , time=2)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryBidDetail(){
		String loanCode = getPara("loanCode");
		String userCode = getUserCode();
		return queryBidDetail(userCode,loanCode);
	}
	
	/**
	 * 我要理财页面-查询详细的标书信息页面(未登录)
	 * @return
	 */
	@ActionKey("/queryBidDetailNoLogin")
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="queryBidDetailNoLogin", cachedKeyParm="loanCode",mode="remote" , time=2)
	public Message queryBidDetailNoLogin(){
		String loanCode = getPara("loanCode");
		return queryBidDetail(null,loanCode);
	}
	
	/**
	 * 我要理财-标书详情页面查询投标记录
	 * @return
	 */
	@ActionKey("/queryLoanTrace")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
//	@ResponseCached(cachedKey="queryLoanTrace", cachedKeyParm="loanCode",mode="remote" , time=1)
	public Message queryLoanTrace(){
		
		String loanCode = getPara("loanCode");
		if(StringUtil.isBlank(loanCode)){
			return error("01", "参数错误", "");
		}
		
		LoanInfo loanInfo = new LoanInfo();
		loanInfo = loanInfoService.findById(loanCode);
		
		List<LoanTrace> listLoanTrace = LoanTrace.loanTraceDao.find(
				"select * from t_loan_trace where loanCode = ? order by loanDateTime asc", loanCode);
		
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("loanAmount", loanInfo.getLong("loanAmount"));
		map.put("loanBalance", loanInfo.getLong("loanBalance"));
		map.put("releaseDate", loanInfo.getStr("releaseDate"));
		map.put("releaseTime", loanInfo.getStr("releaseTime"));
		map.put("effectDate", loanInfo.getStr("effectDate"));
		map.put("effectTime", loanInfo.getStr("effectTime"));
		map.put("traces", listLoanTrace);
		
		return succ("获取成功", map);
	}
	
	
	/**
	 * 查询可转让债权
	 * @param remark
	 * @return
	 */
	@ActionKey("/queryCanTransferList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryCanTransferList(){
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		//获取用户标识
		String userCode = getUserCode();
		
		Page<LoanTrace> loanTraces = loanTraceService.findCanTransfer(pageNumber, pageSize,userCode);
		
		return succ("获取成功", loanTraces);
	}

	
	/**
	 * 查询一周内应回收欠款列表
	 * @return
	 */
	@ActionKey("/queryLoanInfo4Week")
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="queryLoanInfo4Week", cachedKeyParm="date|pageNumber|pageSize",mode="remote" , time=3)
	public Message queryLoanInfo4Week(){
		String date = getPara("date");
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();

		Page<LoanInfo> loanInfos = loanInfoService.findLoanInfo4Week(pageNumber,pageSize, date);
		
		return succ("查询成功", loanInfos);
	}
	
	
	/**
	 * 逾期已归还列表
	 * @return
	 */
	@ActionKey("/queryOverdueTrace4yes")
	@Before({PkMsgInterceptor.class})
//	@ResponseCached(cachedKey="queryOverdueTrace4yes", cachedKeyParm="pageNumber|pageSize",mode="remote" , time=60*60)
	public Message queryOverdueTrace4yes(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		Page<LoanOverdue> loanOverdues = LoanOverdue.overdueTraceDao.paginate(pageNumber, pageSize, 
				"select * ", "from t_loan_overdue where disposeStatus = 'y' order by disposeDateTime desc");
		
		return succ("查询成功", loanOverdues);
		
	}
	
	/**
	 * 逾期列表
	 * @return
	 */
	@ActionKey("/queryOverdueTrace30")
	@Before({PkMsgInterceptor.class})
//	@ResponseCached(cachedKey="queryOverdueTrace30", cachedKeyParm="pageNumber|pageSize|type",mode="remote" , time=60*60)
	public Message queryOverdueTrace30(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		Integer type = getParaToInt("type");
		
		Page<LoanOverdue> loanOverdues = loanOverdueService.queryOverdueTrace30(pageNumber,pageSize,type);
		
		return succ("查询成功", loanOverdues);
		
	}
	
	/**
	 * 查询逾期信息
	 * @return
	 */
	@ActionKey("/queryOverdueTraceByCode")
	@Before({PkMsgInterceptor.class})
	public Message queryOverdueTraceByCode(){
		String overdueCode = getPara("overdueCode");
		if(StringUtil.isBlank(overdueCode)){
			return error("01", "参数错误", "");
		}
		LoanOverdue loanOverdue = loanOverdueService.findById(overdueCode);
		return succ("查询成功", loanOverdue);
		
	}
	
	/**
	 * 查询兑付中标的信息
	 * */
	@ActionKey("/queryPayTransfer4My")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryPayTransfer4My(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",10);
		String refundType = getPara("refundType","");
		String transState = getPara("transState","");
		String beginDate = getPara("beginDate","");
		String endDate = getPara("endDate","");
		String userCode = getUserCode();
		Page<LoanTransfer> pagePayTransfer = loanTransferService.findByPage4PayLoan(pageNumber,pageSize,refundType,transState,beginDate,endDate,userCode);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNumber", pagePayTransfer.getPageNumber());
		map.put("pageSize", pagePayTransfer.getPageSize());
		map.put("totalPage", pagePayTransfer.getTotalPage());
		map.put("totalRow", pagePayTransfer.getTotalRow());
		map.put("list", pagePayTransfer.getList());
		long leftDuiFuAmount = Db.queryBigDecimal("select COALESCE(SUM(transAmount+transFee),0) from t_loan_transfer where payUserCode = ? and refundType in ('E','F') and transState != 'C'",userCode).longValue();
		map.put("leftDuiFuAmount", leftDuiFuAmount);
		return succ("查询成功", map);
	}
	
	
	/**
	 * 查询债权转让
	 * @return
	 */
	@ActionKey("/queryLoanTransfer")
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="queryLoanTransfer", cachedKeyParm="pageNumber|pageSize|minLimit|maxLimit|transState|orderParam|orderType",mode="remote" , time=5)
	public Message queryLoanTransfer(){
		
		String transState = getPara("transState","A");
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String minLimit = getPara("minLimit");
		String maxLimit = getPara("maxLimit");
		String orderParam = getPara("orderParam","");
		String orderType = getPara("orderType","DESC");
		//获取3天前日期
		//String delDay = DateUtil.delDay(DateUtil.getNowDate(), 3L);
		
		Page<LoanTransfer> loanTransfers = loanTransferService.queryCanTransfer(pageNumber, pageSize,minLimit,maxLimit,transState,orderParam,orderType);
		Integer count = loanTransferService.queryTransferCount("A");
		Map<String , Object> map = new HashMap<String, Object>();
		map.put("firstPage", loanTransfers.isFirstPage());
		map.put("lastPage", loanTransfers.isLastPage());
		map.put("pageNumber", loanTransfers.getPageNumber());
		map.put("pageSize", loanTransfers.getPageSize());
		map.put("totalPage", loanTransfers.getTotalPage());
		map.put("totalRow", loanTransfers.getTotalRow());
		//债权添加标来源
		List<LoanTransfer> loanTransfersList = loanTransfers.getList();
		for(int i = 0;i<loanTransfersList.size();i++){
			LoanTransfer loanTransfer = loanTransfersList.get(i);
			String loanCode = loanTransfer.getStr("loanCode");
			LoanInfo loanInfo = loanInfoService.findById(loanCode);
			if(loanInfo == null){
				loanTransfer.put("loanArea", "未知");
			}else{
				loanTransfer.put("loanArea", loanInfo.getStr("loanArea"));
			}
		}
		map.put("list", loanTransfers.getList());
		map.put("transferCount", count);
		
		return succ("查询成功", map);
		
	}

	
	/**
	 * 查询指定债权转让
	 * @return
	 */
	@ActionKey("/queryLoanTransfer4Code")
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="queryLoanTransfer4Code", cachedKeyParm="transferCode",mode="remote" , time=5)
	public Message queryLoanTransfer4Code(){
		
		String transferCode = getPara("transferCode");
		
		//验证数据完整性
		if(StringUtil.isBlank(transferCode)){
			return error("01", "参数错误", "");
		}
		
		LoanTransfer transferInfo = LoanTransfer.loanTransferDao.findById(transferCode);
		
		return succ("查询成功", transferInfo);
		
	}
	
	
	/**
	 * 查询转让的债权
	 * @return
	 */
	@ActionKey("/queryLoanTransfer4user")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoanTransfer4user(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		//获取用户标识
		String userCode = getUserCode();
		Page<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransfer(pageNumber, pageSize, userCode);
		Long totalLeftAmount = loanTransferService.sumLeftAmount(userCode);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("totalLeftAmount",  totalLeftAmount);
		map.put("loanTransfers", loanTransfers);
		return succ("查询成功", map);
	}
	
	/**
	 * 查询承接的债权
	 * @return
	 */
	@ActionKey("/queryGotLoanTransfer4user")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryGotLoanTransfer4user(){
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		
		//获取用户标识
		String userCode = getUserCode();
		Page<LoanTransfer> loanTransfers = loanTransferService.queryGotLoanTransfer(pageNumber, pageSize, userCode);
		Long totalGotAmount = loanTransferService.sumGotAmount(userCode);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("totalGotAmount",  totalGotAmount);
		map.put("loanTransfers", loanTransfers);
		return succ("查询成功", map);
	}
	
	
	/**
	 * 统计承接的债权总金额
	 * @return
	 */
	@ActionKey("/queryGotTransferTotal")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryGotTransferTotal(){
		String userCode = getUserCode();
		Long sumGotAmount = loanTransferService.sumGotAmount(userCode);
		Map<String,Object> totalMap = new HashMap<String, Object>();
		totalMap.put("totalAmount",  sumGotAmount);
		return succ("查询成功", totalMap);
	}
	
	
	/**
	 * 发布债权转让
	 * @return
	 */
	@ActionKey("/debentureTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message debentureTransfer(){
//		if(true){
//			return error("01", "债权转让功能维护中,敬请期待...", "");
//		}
		if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
			return error("01", "亲,存管系统正在上线哦", null);
		}
		
		String traceCode = getPara("traceCode");
		Integer transFee = 0;
		try{
			transFee = getParaToInt("transFee");
		}catch(Exception e){
			return error("03", "请输出正确让利金额", "");
		}
		
		//验证
		if(StringUtil.isBlank(traceCode)){
			return error("01", "转让标书编号错误", traceCode);
		}
		if(transFee < 0){
			return error("01", "让利金额错误", transFee);
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
			return error("01", "版本过低无法进行此操作", "");
		}
		
		//验证用户是否标的投资者
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(traceCode);
		if(null == loanTrace){
			return error("02", "未找到此债权记录", traceCode);
		}
		String productType = loanTrace.getStr("productType");
		if(SysEnum.productType.E.val().equals(productType)){
			return error("02", "易分期不支持债权转让", traceCode);
		}
		String loanCode = loanTrace.getStr("loanCode");
		List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", null);
		if(loanOverdues !=null&&loanOverdues.size()>0){
			return error("30", "此标逾期中，暂时不能债转", traceCode);
		}
		String authCode = loanTrace.getStr("authCode");
		if(StringUtil.isBlank(authCode)){
			return error("29", "此标暂时不能转让，点击确认查看详情", traceCode);
		}
		
		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//当天为T+1回款日不可发布债权
		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
		if(DateUtil.getNowDate().equals(tmpRecyDate)){
			return error("05", "该债权今日还款未完成,暂时无法发布", "");
		}
		
		if(CommonUtil.jxPort){
			if(StringUtil.isBlank(user.getStr("jxAccountId"))){
				return error("24", "未激活存管帐号，不能进行此操作", "");
			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
				return error("24", "缴费授权未开通", "");
			}
//			if(StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
//				return error("24", "新存管上线前债权暂不支持转让", "");
//			}
			if(StringUtil.isBlank(loanTrace.getStr("authCode"))){
				return error("24", "该债权暂不支持转让", "");
			}
		}
		
		try {
			if(loanTrace.getStr("payUserCode").equals(userCode) == false){
				return error("02", "只有您投资的标才能转让", traceCode);
				}
		} catch (Exception e) {
			return error("02", "只有您投资的标才能转让", traceCode);
		}
		
		//验证是否已经转让
		//LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("select * from t_loan_transfer where transState != 'C' and traceCode = ? ", traceCode);
		String isTransfer = loanTrace.getStr("isTransfer");
		if(isTransfer.equals("A")){
			return error("06", "该标书正在转让,请勿重复操作!", "");
		}
		
		//检查让利金额 小于让利百分比（配置项中修改）
		int f = 1;
		try{
			f = Integer.parseInt(CACHED.get("ZQ.transFee").toString());
		}catch(Exception e){
		}
		if(transFee > ( loanTrace.getLong("leftAmount") / 10.0/10.0 * f )){
			return error("07", "让利金额不能超过投标金额的百分之"+f, "");
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
			return error("187", "此投资使用了现金抵用券，但剩余本金小于抵用券抵扣现金金额", "");
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
			return error("02", "修改投标流水状态失败", null);
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
			return error("02", "债权转让发布失败", null);
		}
		BIZ_LOG_INFO(traceCode, BIZ_TYPE.TRANSFER, "债权转让发布成功");
		return succ("发布债权成功", "");
	}
	
	/**
	 * 发布逾期债转
	 * 注：与正常发布的手续费，补偿利息，积分计算方式不一样
	 * */
	@ActionKey("/debentureOverdueTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message debentureOverdueTransfer(){
	if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
		return error("01", "亲,存管系统正在上线哦", null);
	}
	
	String traceCode = getPara("traceCode");
	Integer transFee = 0;
	try{
		transFee = getParaToInt("transFee");
	}catch(Exception e){
		return error("03", "请输出正确让利金额", "");
	}
	
	//验证
	if(StringUtil.isBlank(traceCode)){
		return error("01", "转让标书编号错误", traceCode);
	}
	if(transFee < 0){
		return error("01", "让利金额错误", transFee);
	}
	//获取用户信息
	String userCode = getUserCode();
	User user = User.userDao.findById(userCode);
	if(CommonUtil.fuiouPort){
		return error("01", "版本过低无法进行此操作", "");
	}
	
	//验证用户是否标的投资者
	LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(traceCode);
	if(null == loanTrace){
		return error("02", "未找到此债权记录", traceCode);
	}
	String productType = loanTrace.getStr("productType");
	if(SysEnum.productType.E.val().equals(productType)){
		return error("02", "易分期不支持债权转让", traceCode);
	}
	String jgTransfer = getPara("jgTransfer");
	if(!"y".equals(jgTransfer)){
		String loanCode = loanTrace.getStr("loanCode");
		List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", null);
		if(loanOverdues ==null){
			return error("30", "此标请在可转让债权页面发布", traceCode);
		}
	}
	String authCode = loanTrace.getStr("authCode");
	if(StringUtil.isBlank(authCode)){
		return error("29", "此标暂时不能转让，点击确认查看详情", traceCode);
	}
	
	String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
	//当天为T+1回款日不可发布债权
	String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
	if(DateUtil.getNowDate().equals(tmpRecyDate)){
		return error("05", "该债权今日还款未完成,暂时无法发布", "");
	}
	
	if(CommonUtil.jxPort){
		if(StringUtil.isBlank(user.getStr("jxAccountId"))){
			return error("24", "未激活存管帐号，不能进行此操作", "");
		}
		UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
		if(!userTermsAuth.isPaymentAuth()){
			return error("24", "缴费授权未开通", "");
		}
//		if(StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
//			return error("24", "新存管上线前债权暂不支持转让", "");
//		}
		if(StringUtil.isBlank(loanTrace.getStr("authCode"))){
			return error("24", "该债权暂不支持转让", "");
		}
	}
	
	try {
		if(loanTrace.getStr("payUserCode").equals(userCode) == false){
			return error("02", "只有您投资的标才能转让", traceCode);
			}
	} catch (Exception e) {
		return error("02", "只有您投资的标才能转让", traceCode);
	}
	
	//验证是否已经转让
	//LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("select * from t_loan_transfer where transState != 'C' and traceCode = ? ", traceCode);
	String isTransfer = loanTrace.getStr("isTransfer");
	if(isTransfer.equals("A")){
		return error("06", "该标书正在转让,请勿重复操作!", "");
	}
	
	//检查让利金额 小于让利百分比（配置项中修改）逾期标让利大于总本金的40% 小于70%
	int tmpMax = 0;int tmpMin = 0;
	if("y".equals(jgTransfer)){
		tmpMax = 50;tmpMin = 30;
	}else{
		tmpMax = 70;tmpMin = 40;
	}
	if(transFee > ( (loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount")) / 10.0/10.0 * tmpMax )){
		return error("07", "让利金额不能超过投标金额的百分之"+tmpMax, "");
	}else if(transFee < ( (loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount")) / 10.0/10.0 * tmpMin )){
		return error("07", "让利金额不能低于投标金额的百分之"+tmpMin, "");
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
		return error("187", "此投资使用了现金抵用券，但剩余本金小于抵用券抵扣现金金额", "");
	}
	//计算转让积分
	int loanTimeLimit = loanTrace.getInt("loanTimeLimit");
	long amount = loanTrace.getLong("payAmount");
//	String refundType = loanTrace.getStr("refundType");
//	String effectDate =  loanInfoService.findFieldById(loanTrace.getStr("loanCode"),"effectDate");//审核满标日期
//	String lastSettlementDate = CommonUtil.anyRepaymentDate4string(effectDate, loanTimeLimit);//最后一期还款日期
//	int allDays = (int) CommonUtil.compareDateTime(lastSettlementDate, effectDate, "yyyyMMdd");
//	int leftDays = (int) CommonUtil.compareDateTime(lastSettlementDate, DateUtil.getNowDate(), "yyyyMMdd");
//	long allScore = CommonUtil.f_005(amount, loanTimeLimit , refundType) ;
//	long tmp = CommonUtil.yunsuan(allScore+"", allDays+"", "chu", 0).longValue();
//	int leftScore = (int) CommonUtil.yunsuan(tmp+"", leftDays+"", "cheng", 0).longValue();
//	
//	//扣除转让人积分
//	fundsServiceV2.doPoints(userCode, 1 , leftScore , "发布转让债权，冻结积分");
	//修改标书债权状态
	boolean updateTraceState = loanTraceService.updateTransferState(loanTrace.getStr("traceCode"), "C", "A");
	if(updateTraceState == false){
		BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "转让债权时修改投标流水状态修改异常", null);
		return error("02", "修改投标流水状态失败", null);
	}
	//保存债权信息
	//临时修改额外年利率（若用加息券）为transfer保存做准备
	if(rewardticketrate>0){
		int rewardRateByYear=loanTrace.getInt("rewardRateByYear");
		loanTrace.set("rewardRateByYear", rewardRateByYear-rewardticketrate);
	}
	
	boolean result = loanTransferService.saveOverdueLoanTransfer(transFee, 0 ,loanTrace);
	if(result == false){
		BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权转让发布失败", null);
		return error("02", "债权转让发布失败", null);
	}
	BIZ_LOG_INFO(traceCode, BIZ_TYPE.TRANSFER, "债权转让发布成功");
	return succ("发布债权成功", "");
}
	
	/**
	 * 兑付债转
	 * 
	 * */
	@ActionKey("/duiFuTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message duiFuTransfer(){
		if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
			return error("01", "亲,存管系统正在上线哦", null);
		}
		
		String traceCode = getPara("traceCode");
		String traceCodes = getPara("traceCodes");
		int transFee = 0;
		
		//验证
		List<LoanTrace> traceList = new ArrayList<LoanTrace>();
		if(StringUtil.isBlank(traceCode)){
			if(StringUtil.isBlank(traceCodes)){
				return error("01", "转让标书编号错误", traceCode);
			}else{
				String[] traceCodeArray = traceCodes.split(",");
				for(int i=0;i<traceCodeArray.length;i++){
					LoanTrace loanTraceTemp = LoanTrace.loanTraceDao.findById(traceCodeArray[i]);
					if(loanTraceTemp!=null){
//						if(StringUtil.isBlank(loanTraceTemp.getStr("authCode"))){
//							return error("29", "标号"+loanTraceTemp.getStr("loanNo")+"暂时无法发布，点击确认查看详情", "");
//						}
						if(loanTraceTemp.getStr("productType").equals("E")){
							return error("22", "标号"+loanTraceTemp.getStr("loanNo")+"易分期无法债转", "");
						}
					}else{
						return error("02", "未找到此债权记录", traceCode);
					}
					traceList.add(loanTraceTemp);
				}
			}
		}else{
			LoanTrace loanTraceTemp = LoanTrace.loanTraceDao.findById(traceCode);
			traceList.add(loanTraceTemp);
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
			return error("01", "版本过低无法进行此操作", "");
		}
		
		if(CommonUtil.jxPort){
			if(StringUtil.isBlank(user.getStr("jxAccountId"))){
				return error("24", "未激活存管帐号，不能进行此操作", "");
			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
				return error("24", "缴费授权未开通", "");
			}
//			if(StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
//				return error("24", "新存管上线前债权暂不支持转让", "");
//			}
		}
		TransferWay transferWay = transferWayService.findByUserCode(userCode);
		String payType = getPara("payType");
		//获取兑付回款方式
		if(transferWay==null||transferWay.getStr("offline").equals("1")||transferWay.getStr("normal").equals("1")){
			return error("33", "所选支付方式无法发布", "");
		}
		if(StringUtil.isBlank(payType)||(!payType.equals("E")&&!payType.equals("F")&&!payType.equals("H"))){
			return error("33", "支付方式获取失败", "");
		}
		if("E".equals(payType)&&!transferWay.getStr("month").equals("1")){
			return error("33", "支付方式错误", "");
		}
		if("F".equals(payType)&&!transferWay.getStr("qtr").equals("1")){
			return error("33", "支付方式错误", "");
		}
		if("H".equals(payType)&&!transferWay.getStr("half").equals("1")){
			return error("33", "支付方式错误", "");
		}
		for(int n = 0;n<traceList.size();n++){
		//验证用户是否标的投资者
		LoanTrace loanTrace = traceList.get(n);
		if(null == loanTrace){
			if(n>0){
				continue;
			}
			return error("02", "未找到此债权记录", traceCode);
		}
		String productType = loanTrace.getStr("productType");
		if(SysEnum.productType.E.val().equals(productType)){
			if(n>0){
				continue;
			}
			return error("02", "易分期不支持债权转让", traceCode);
		}
//		String authCode = loanTrace.getStr("authCode");
//		if(StringUtil.isBlank(authCode)){
//			if(n>0){
//				continue;
//			}
//			return error("29", "此标暂时不能转让，点击确认查看详情", traceCode);
//		}
		
//		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//当天为T+1回款日不可发布债权
//		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
//		if(DateUtil.getNowDate().equals(tmpRecyDate)){
//			return error("05", "该债权今日还款未完成,暂时无法发布", "");
//		}
		
		
		try {
			if(loanTrace.getStr("payUserCode").equals(userCode) == false){
				if(n>0){
					continue;
				}
				return error("02", "只有您投资的标才能转让", traceCode);
				}
		} catch (Exception e) {
			if(n>0){
				continue;
			}
			return error("02", "只有您投资的标才能转让", traceCode);
		}
		
		//验证是否已经转让
		//LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("select * from t_loan_transfer where transState != 'C' and traceCode = ? ", traceCode);
		String isTransfer = loanTrace.getStr("isTransfer");
		if(isTransfer.equals("A")){
			if(n>0){
				continue;
			}
			return error("06", "该标书正在转让,请勿重复操作!", "");
		}
		if("E".equals(payType)||"F".equals(payType)){
			transFee = (int)(loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount")-1);
		}else if("H".equals(payType)){
			transFee = (int)(loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount"))/2;
		}else{
			if(n>0){
				continue;
			}
			return error("06", "支付方式获取失败", "");
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
		if(loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount") <= ticket_amount){
			if(n>0){
				continue;
			}
			return error("187", "此投资使用了现金抵用券，但剩余本金小于抵用券抵扣现金金额", "");
		}
		boolean updateTraceState = loanTraceService.updateTransferState(loanTrace.getStr("traceCode"), "C", "A");
		if(updateTraceState == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "转让债权时修改投标流水状态修改异常", null);
			if(n>0){
				continue;
			}
			return error("02", "修改投标流水状态失败", null);
		}
		//保存债权信息
		//临时修改额外年利率（若用加息券）为transfer保存做准备
		if(rewardticketrate>0){
			int rewardRateByYear=loanTrace.getInt("rewardRateByYear");
			loanTrace.set("rewardRateByYear", rewardRateByYear-rewardticketrate);
		}
		//临时修改兑付回款方式
		loanTrace.set("refundType", payType);
		boolean result = loanTransferService.saveOverdueLoanTransfer(transFee, 0 ,loanTrace);
		if(result == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权转让发布失败", null);
			if(n>0){
				continue;
			}
			return error("02", "债权转让发布失败", null);
		}
		}
		BIZ_LOG_INFO(traceCode, BIZ_TYPE.TRANSFER, "债权转让发布成功");
		return succ("发布债权完成", "");
	}
	
	
	/**
	 * 取消债权转让
	 * @return
	 */
	@ActionKey("/cancelTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message cancelTransfer(){
		String transferCode = getPara("transferCode");
		
		if(StringUtil.isBlank(transferCode)){
			return error("01", "参数错误", "transferCode : " + transferCode);
		}
		
		//获取用户标识
		String userCode = getUserCode();
		
		// add for new source at 20180925, no test
//		loanTransferService.cancelTransfer(transferCode, userCode);
		// end add
		
		//验证债权是否存在   是否被转让
		LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst(
				"select traceCode, transState,transScore,refundType from t_loan_transfer where transCode = ? and payUserCode = ? and transState = 'A' ",
				transferCode , userCode);
		if(null == loanTransfer){
			BIZ_LOG_WARN(userCode, BIZ_TYPE.TRANSFER, "债权取消失败，债权未查到或已转让");
			return error("02", "债权未查到或已转让", "");
		}
		if(loanTransfer.getStr("refundType").equals("E")||loanTransfer.getStr("refundType").equals("F")||loanTransfer.getStr("refundType").equals("H")){
			return error("02", "取消失败", "");
		}
		List<JXTrace> jxTraces = jxTraceService.queryTranfers(transferCode);
		if(null!=jxTraces&&jxTraces.size()>0){
			for(int i = 0 ;i<jxTraces.size();i++){
				JXTrace jxTrace = jxTraces.get(i);
				String retCode = jxTrace.getStr("retCode");
				if(StringUtil.isBlank(retCode)){
					String txDateTime = jxTrace.getStr("txDate")+jxTrace.getStr("txTime");
					if(i==0&&DateUtil.differentMinuteByMillisecond(txDateTime, DateUtil.getNowDateTime(), "yyyyMMddHHmmss")<=20){
						return error("11", "此债权转让订单已提交，等待支付结果中，暂时无法关闭", "");
					}
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject jsonObject = JSONObject.parseObject(requestMessage);
					String orgAccountId = jsonObject.getString("accountId");
					String orgOrderId = jsonObject.getString("orderId");
					Map<String,String> map = JXQueryController.creditInvestQuery(orgAccountId, orgOrderId);
					if("00000000".equals(map.get("retCode"))){
						return error("13", "此标已债权转让完成,等待银行处理中，无法关闭", "");
					}
				}else{
					if("00000000".equals(retCode)){
						return error("13", "此标已债权转让完成,等待银行处理中，无法关闭", "");
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
			return error("03", "债权取消失败", null);
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
			return error("05", "债权取消成功,但标书债权状态修改异常", null);
		}
		
		//回滚积分
		int transScore = loanTransfer.getInt("transScore");
		//fundsServiceV2.doPoints(userCode, 0 , transScore , "取消债权转让,返回冻结积分!");
		
		BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权取消成功");
		
		return succ("债权取消成功", "");
	}
	
	/**
	 * 承接债权转让
	 * @return
	 */
	@ActionKey("/carryOnTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message carryOnTransfer(){
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		
		//限制还款时间不让承接     10点半到11点
//		int exeTime = Integer.parseInt(DateUtil.getNowTime());
//		if(exeTime >= 103000 && exeTime <= 110000){
//			return error("12", "10:30至11:00系统正在还款，请稍后操作。", "");
//		}
		
		String transferCode = getPara("transferCode");
		if(StringUtil.isBlank(transferCode)){
			WebUtils.writePromptHtml("获取债权转让编号失败", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("01", "获取债权转让编号失败", null);
		}
		
		//获取用户信息
		if(StringUtil.isBlank(userCode)){
			WebUtils.writePromptHtml("请重新登录", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("02", "请重新登录", null);
		}
		
		//获取转让标信息
		LoanTransfer loanTransfer = loanTransferService.findById(transferCode);
		if(null == loanTransfer){
			WebUtils.writePromptHtml("债权标书获取异常", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("04", "债权标书获取异常", "");
		}
		String productType = loanTransfer.getStr("productType");
		if(SysEnum.productType.E.val().equals(productType)){
			return error("02", "易分期无法被承接", "");
		}
		if(!"A".equals(loanTransfer.getStr("transState"))){
			return error("04", "债权标书已被承接或取消", "");
		}
		String traceCode = loanTransfer.getStr("traceCode");
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		if(loanTrace == null){
			WebUtils.writePromptHtml("债权获取失败", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("04", "债权获取失败", "");
		}
		
		String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
		//避免还款批次已提交,期间债权被承接,导致存管承接额为还款前金额,债权本地已还资金异常
		//债权还款日为T+1  修改规则日 20180815
		String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
		if(DateUtil.getNowDate().equals(tmpRecyDate)){
			WebUtils.writePromptHtml("该债权今日还款未完成,完成后即可承接", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("05", "该债权今日还款未完成,完成后即可承接", null);
		}

		// 存管账户相关验证
		if (CommonUtil.jxPort) {	// 江西银行存管验证
			String jxAccountId = user.getStr("jxAccountId"); // 用户电子账号
			if(StringUtil.isBlank(jxAccountId)){
				WebUtils.writePromptHtml("未开通江西银行存管", "/Z02?navTab=2", "UTF-8",getResponse());
				return error("03", "未开通江西银行存管", null);
			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
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
		if (!fundsServiceV2.checkBalance(user)) {
			return error("05", "用户资金异常", "");
		}
		
		// 验证承接人可用余额是否足够
		int transAmount = loanTransfer.getInt("transAmount");
		long avBalance = fundsServiceV2.findAvBalanceById(userCode);
		if(avBalance < transAmount){
			WebUtils.writePromptHtml("账户可用余额不足!", "/Z02?navTab=2", "UTF-8",getResponse());
			return error("05", "账户可用余额不足!", "");
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
			return error("05", "该债权正在回款中，暂时无法承接", "");
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
		//转让人
		String payUserCode = loanTransfer.getStr("payUserCode");
		User payUser = userService.findById(payUserCode);
		
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
			//若为兑付债转，则不计算现金券的金额
			if(loanTransfer.getStr("refundType").equals("E")||loanTransfer.getStr("refundType").equals("F")){
				ticket_amount = 0;
			}
			//跳转江西银行页面
			JXController.creditInvest(reqMap,accountId, transAmount, sysFee + ticket_amount, orgTxAmount,
					forAccountId, orgOrderId, orgTxAmount, productId,transferCode, getResponse());
			WebUtils.writePromptHtml("债权转让请求已发送", "/Z02?navTab=2", "UTF-8",getResponse());
			return succ("债权转让请求已发送", null);
		} else if (CommonUtil.fuiouPort) {
			return error("13", "此版本不支持此功能", "");
			// TODO 富友债权承接接口
//			
//			String payPwd = getPara("payPwd");
//			String smsMsg = getPara("smsMsg");
//			
//			//手机端验证短信验证码  	PC端验证 图片验证码
//			if(StringUtil.isBlank(smsMsg)){
//				if( validateCaptcha("cv") == false ){
//					Message msg = error("11", "验证码错误", "");
//					return msg;
//				}
//			}else{
//				if(CommonUtil.validateSMS("SMS_MSG_TRANSFER_" + userCode, smsMsg) == false){
//					return error("07", "短信验证码不正确。", "");
//				}
//			}
//			
//			//验证支付密码
//			if(!CommonUtil.validatePwd(userCode, payPwd, user.getStr("payPasswd"))){
//				return error("10", "支付密码错误或被冻结", "");
//			}
//			
//			//修改投标债权状态到  已转让
//			boolean updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"), "A", "B");
//			if(updateTraceState == false){
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权失败[08]", null);
//				return error("13", "承接债权失败[08]", "");
//			}
			
//			boolean b = fundsServiceV2.carryOnTransfer(userCode, transAmount,remark);
//			if(b==true){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权扣除可用余额成功  扣除金额  : " + transAmount);
//			}
//			if(b == false){
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "承接债权-添加流水失败", null);
//				return error("14", "承接债权失败-[添加扣款流水失败]!", "");
//			}
			
			//老存管代码   暂时注释  start	
//			//转让价格(承接人实际付款) - 平台手续费
//			long payTransAmount = transAmount - sysFee - ticket_amount;//债权人转让收益，扣除手续费和抵用券费用
//			boolean carryOnTransferTo = fundsServiceV2.carryOnTransferTo(payUserCode, payTransAmount, sysFee ,transFee,traceRemark);
//			if(carryOnTransferTo==true){
//				//记录日志
//				BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权增加可用余额成功  收益金额  : " + payTransAmount);
//			}
//			
//			if(carryOnTransferTo == false){
//				BIZ_LOG_ERROR(payUserCode, BIZ_TYPE.TRANSFER, "承接债权-添加转让人流水失败", null);
//				return error("16", "承接债权失败-[添加转让人流水失败]!", "");
//			}
//			
//			//修改债权状态 并验证是否已经被转让
//			boolean updateTransferState = loanTransferService.updateTransferState(transferCode,userCode,user.getStr("userName"));
//			if(updateTransferState == false){
//				//回滚资金
//				fundsServiceV2.doAvBalance(userCode, 0, transAmount);
//				fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
//				//回滚流水状态
//				loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
//						"B", "A");
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权失败资金回滚");
//				BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权失败资金回滚");
//				return error("17", "债权已被承接", "");
//			}else{
//				//更新冗余信息     by wc
////				CommonUtil.f_000(amount, limit, rate, countLimit, rt)
//				
//				if(null == loanTrace){
//					//回滚资金
//					fundsServiceV2.doAvBalance(userCode, 0, transAmount);
//					fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
//					//记录日志
//					BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//					BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//					return error("18", "债权数据出现异常，请联系客服处理。", "");
//				}
//				
//				String oPayUserCode = loanTrace.getStr("payUserCode");
////				long amount = loanTrace.getLong("payAmount");
//				int rate = loanTransfer.getInt("rateByYear") + loanTransfer.getInt("rewardRateByYear") ;
//				int reciedCount = loanTrace.getInt("loanRecyCount");
//				int limit = loanTrace.getInt("loanTimeLimit");
////				String type = loanTrace.getStr("refundType") ;
//				long[] benxi = new long[2];//CommonUtil.f_005(amount, rate, limit, reciedCount, type) ;
//				benxi[0] = loanTrace.getLong("leftAmount");
//				benxi[1] = loanTrace.getLong("leftInterest");
//				
//				//假如第一次债转  并且  用的加息券   计算下期待还本息与总的利息
//				String refundType=loanTrace.getStr("refundType");
//				//承接人本息
//				long[] benxi2 = new long[2];
//				if("A".equals(refundType)){
//					benxi2 =CommonUtil.f_004(benxi[0], rate, reciedCount, "A");
//				}else if("B".equals(refundType)){
//					benxi2 =CommonUtil.f_005(loanTrace.getLong("payAmount"), rate, limit, limit-reciedCount, "B");
//				}
//				if(rewardticketrate>0){
//				//承接人下期本息
//					long[] nextbenxi=new long[2];
//				if("A".equals(refundType)){
//					nextbenxi=CommonUtil.f_000(benxi[0], reciedCount, rate, 1, "A");
//				}else if("B".equals(refundType)){
//					nextbenxi=CommonUtil.f_000(benxi[0], limit, rate, reciedCount, "B");
//				}
//				loanTrace.set("leftInterest", benxi2[1]);//修改总代收利息
//				loanTrace.set("nextAmount", nextbenxi[0]);//修改下期代收本金
//				loanTrace.set("nextInterest", nextbenxi[1]);//修改下期代收利息
//				loanTrace.set("rewardRateByYear",loanTransfer.getInt("rewardRateByYear"));//扣除加息券利息
//				}
//				//修改债权归属
//				loanTrace.set("payUserCode", userCode );
//				loanTrace.set("payUserName", user.getStr("userName") );
//				
//				boolean updateTrace = loanTrace.update();
//				if(updateTrace == false){
//					//回滚资金
//					fundsServiceV2.doAvBalance(userCode, 0, transAmount);
//					fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
//					//记录日志
//					BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//					BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//					return error("19", "债权数据出现异常，请联系客服处理。", "");
//				}
//				//存管系统资金流动  20170616 ws
//				if("".equals(payUser.getStr("loginId"))||null==payUser.getStr("loginId")){
//					User wzuser=userService.findByMobile(CommonUtil.OUTCUSTNO);
//					CommonRspData com=	fuiouTraceService.refund(Long.valueOf(transAmount),FuiouTraceType.T,user,wzuser,transferCode);
//					if(!"0000".equals(com.getResp_code())){
//						//回滚资金
//						fundsServiceV2.doAvBalance(userCode, 0, transAmount);
//						fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
//						//记录日志
//						BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//						BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//						return error("20", "债权数据出现异常，请联系客服处理。", "");
//					}
//						loanTransfer.set("transCode", com.getMchnt_txn_ssn());
//						loanTransfer.update();
//					}else{
//						User wzuser =userService.findByMobile(CommonUtil.OUTCUSTNO);
//						CommonRspData comm=fuiouTraceService.refund(Long.valueOf(transAmount), FuiouTraceType.T, user,payUser,transferCode);//扣除承接人资金
//						if("0000".equals(comm.getResp_code())){
//							fuiouTraceService.refund(sysFee + ticket_amount, FuiouTraceType.V, payUser,wzuser,transferCode);//转让人扣除服务费+抵用券
//							loanTransfer.set("transCode", comm.getMchnt_txn_ssn());
//							loanTransfer.update();
//						}else{
//					//回滚资金
//							fundsServiceV2.doAvBalance(userCode, 0, transAmount);
//							fundsServiceV2.doAvBalance(payUserCode, 1, payTransAmount);
//					//记录日志
//							BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//							BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "债权数据出现异常资金回滚");
//							return error("21", "债权数据出现异常，请联系客服处理。", "");
//				}}
//				//end
//				
//				//更新冗余账户 (更新理财人待还账户)
//				int beRecyCount = limit - reciedCount ;
//				//减少原投资人账户	oPayUserCode      //  增加已回收资金  利息	 shiqingsong 2016-02-18
//				long reciedInterest = transAmount - benxi[0] - transFee;
//				fundsServiceV2.updateBeRecyFunds(oPayUserCode, (0-beRecyCount), (0-benxi[0]), (0-benxi[1]), benxi[0] ,  reciedInterest > 0 ? reciedInterest : 0);
//				//增加接受人
//				if(rewardticketrate>0){//若使用了加息券
//				fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi2[0], benxi2[1], 0 ,  0 );
//				}else{
//				fundsServiceV2.updateBeRecyFunds(userCode, beRecyCount, benxi[0], benxi[1], 0 ,  0 );
//				}
//				
//			}
//			//生日当天承接债转，白银及以上按等级送积分 	2018.03
//			int transScore = loanTransfer.getInt("transScore");
//			String cardId="";
//			UserInfo userInfo=userInfoService.findById(userCode);
//			
//			cardId=userInfo.getStr("userCardId");
//			
//			if(CommonUtil.isBirth(cardId)){
//				int vipLevel = user.getInt("vipLevel");
//				if(vipLevel >=4 && vipLevel <= 6){//白银、黄金、白金双倍积分
//					transScore=transScore*2;
//				}else if(vipLevel >=7 && vipLevel <= 9){//钻石、黑钻、至尊三倍积分
//					transScore=transScore*3;
//				}
//			}
//			
//			
//			fundsServiceV2.doPoints(userCode, 0, transScore, "承接债权获取可用积分收益!");
//			//userService.updateScore(userCode, transScore) ;
//			
//			//增加风险备用金
//			fundsServiceV2.updateRiskTotal(loanTransfer.getInt("riskFee"));
//			
//			/*
//			 * 债权承接成功——给借款人发送债权变更通知
//			 * 1、获取承接标的信息
//			 * 2、查询借款人、转让人、承接人信息
//			 * 3、生成通知短信内容、发送、保存
//			 */
//			String loanCode = loanTransfer.getStr("loanCode");
//			String loanNo = loanTransfer.getStr("loanNo");
//			LoanInfo loanInfo = loanInfoService.findById(loanCode);
//			
//			Long leftAmount = loanTrace.getLong("leftAmount");//该债权剩余的本金
//			
//			String lName = loanInfo.getStr("userName");//借款人姓名
//			String lUserCode = loanInfo.getStr("userCode");
//			String mobile = userService.getMobile(lUserCode);//借款人手机号
//			
//			String cName = "";//承接人姓名
//			String tName = "";//转让人姓名
//			String cCardId = "";//承接人身份证
//			String tCardId = "";//转让人身份证
//			try {
//				cName = userInfo.getStr("userCardName");
//				UserInfo tUserInfo = userInfoService.findById(payUserCode);
//				tName = tUserInfo.getStr("userCardName");
//				
//				cCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
//				tCardId = CommonUtil.decryptUserCardId(tUserInfo.getStr("userCardId"));
//				
//				cCardId = cCardId.substring(0, 4) + "****" + cCardId.substring(cCardId.length()-4,cCardId.length());
//				tCardId = tCardId.substring(0, 4) + "****" + tCardId.substring(tCardId.length()-4, tCardId.length());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			String msgContent = CommonUtil.SMS_SMG_TRANSFER_CHANGE.replace("[loanName]", lName).replace("[transferName]",tName ).replace("[transferCardId]", tCardId).replace("[loanNo]", loanNo)
//					.replace("[transAmount]", Number.longToString(leftAmount)).replace("[carryOnName]", cName).replace("[carryOnCardId]", cCardId).replace("[nowDate]", new SimpleDateFormat("YYYY年MM月dd日").format(new Date()));
//			SMSLog smsLog = new SMSLog();
//			smsLog.set("mobile", mobile);
//			smsLog.set("content", msgContent);
//			smsLog.set("userCode", lUserCode);
//			smsLog.set("userName", lName);
//			smsLog.set("type", "18");smsLog.set("typeName", "债权变更通知");
//			smsLog.set("status", 9);
//			smsLog.set("sendDate", DateUtil.getNowDate());
//			smsLog.set("sendDateTime", DateUtil.getNowDateTime());
//			smsLog.set("break", "");
//			smsLogService.save(smsLog);
//			
//			//记录日志
//			BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "承接债权成功");
//			BIZ_LOG_INFO(payUserCode, BIZ_TYPE.TRANSFER, "转让债权成功");
//			
//			return succ("承接债权成功", "");
			//end
		} else {
			return error("16", "网络错误", "");
		}
	}
	
	
	/**
	 * 获取发标预告
	 * @return
	 */
	@ActionKey("/queryNotice")
	@Before({PkMsgInterceptor.class})
	@ResponseCached(cachedKey="queryNotice", cachedKeyParm="",mode="remote" , time=60)
	public Message queryNotice(){
		Page<LoanNotice> loanNotices = loanNoticeService.findByPage(1, 10, DateUtil.getNowDateTime(), null, "0", null);
		return succ("00", loanNotices);
	}
	
	
	
	/**
	 * 根据标书标识获取某个用户的投标情况
	 * @return
	 */
	@ActionKey("/queryBidding4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFunds4user() {
		
		String loanCode = getPara("loanCode");
		
		if(StringUtil.isBlank(loanCode)){
			return error("01", "参数错误", null);
		}
		
		//获取用户标识
		String userCode = getUserCode();
		
		//获取用户资金信息
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		
		//投标信息
		Map<String , Long> totalMap = loanTraceService.totalByLoan4user(loanCode , userCode );
		totalMap.put("avBalance", funds.getLong("avBalance"));
		
		//返回
		return succ("查询成功", totalMap);
	}
	
	/**
	 * 查询回款详情
	 */
	@ActionKey("/queryLoanReturn")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoanReturn(){
		String traceCode = getPara("traceCode");
		if(StringUtil.isBlank(traceCode)){
			return error("01", "参数错误", "");
		}
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		long amount = loanTrace.getLong("payAmount");
		int limit = loanTrace.getInt("loanTimeLimit");
		int rate = loanTrace.getInt("rateByYear")+loanTrace.getInt("rewardRateByYear");
		LiCai licai = new LiCai(amount, rate, limit);
		Map<String,Object> map = new HashMap<String, Object>();
		List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
		if("A".equals(loanTrace.getStr("refundType"))){
			licaiMap = licai.getDengEList();
		}else{
			licaiMap = licai.getDengXiList();
		}
		map.put("loanTrace", loanTrace);
		map.put("licaiMap", licaiMap);
		return succ("查询成功", map);
		
	}	
	
	/**
	 * 查询债转标详情
	 * */
	@ActionKey("/queryLoanTransferByTraceCode")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoanTransferByTraceCode(){
		String traceCode=getPara("tracecode");
		List<LoanTransfer> Transfers =  loanTransferService.queryLoanTransferByTraceCode(traceCode , "B");
		return succ("Transfers", Transfers);
	}	
	
	
	/////////////////////////////   私有函数     ////////////////////////////////////////////
	
	
	/**
	 * 我要理财页面-查询详细的标书信息页面
	 * @return
	 */
	private Message queryBidDetail(String userCode , String loanCode){
		if(StringUtil.isBlank(loanCode)){
			return error("01", "标书编码不合法", null);
		}
		LoanInfo loanInfo = loanInfoService.findById(loanCode);	
		Map<String,Object> tmp = new HashMap<String, Object>();
		List<LoanOverdue> loanOverdues = loanOverdueService.findByLoanCode(loanCode, "n", "");//该标的逾期列表
		if(!loanOverdues.isEmpty()&&loanOverdues.size()>0){
			int repayIndex = loanOverdues.get(0).getInt("repayIndex");//第几期开始逾期
			tmp.put("isOverdue", "yes");
			tmp.put("repayIndex",repayIndex);
		}	
		tmp.put("loanCode", loanInfo.getStr("loanCode"));
		tmp.put("loanNO", loanInfo.getStr("loanNo"));
		tmp.put("loanTitle", loanInfo.getStr("loanTitle"));
		tmp.put("rateByYear", loanInfo.getInt("rateByYear"));
		if (!StringUtil.isBlank(loanInfo.getStr("releaseDate"))) {
			long releaseDate = Long.parseLong(loanInfo.getStr("releaseDate"));
			if (loanInfo.getInt("benefits4new") == 0) {
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					loanInfo.put("rewardRateByYear", 400);
					loanInfo.put("ifShow", "yes");
				} else {
					loanInfo.put("ifShow", "no");
				}
			}
			tmp.put("ifShow", loanInfo.getStr("ifShow"));
		}
		tmp.put("rewardRateByYear", loanInfo.getInt("rewardRateByYear"));
		tmp.put("loanAmount", loanInfo.getLong("loanAmount"));
		tmp.put("loanBalance", loanInfo.getLong("loanBalance"));
		tmp.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
		tmp.put("refundType", loanInfo.getStr("refundType"));
		tmp.put("loanState", loanInfo.getStr("loanState"));
		tmp.put("loanArea", loanInfo.getStr("loanArea"));
		tmp.put("releaseDate", loanInfo.getStr("releaseDate"));
		tmp.put("releaseTime", loanInfo.getStr("releaseTime"));
		tmp.put("minLoanAmount", loanInfo.getInt("minLoanAmount"));
		tmp.put("maxLoanAmount", loanInfo.getInt("maxLoanAmount"));
		tmp.put("loanBaseDesc", loanInfo.getStr("loanBaseDesc"));
		tmp.put("loanUsedDesc", loanInfo.getStr("loanUsedDesc"));
		tmp.put("loanerDataDesc", loanInfo.getStr("loanerDataDesc"));
		tmp.put("loanInvDesc", loanInfo.getStr("loanInvDesc"));
		tmp.put("loanPic", loanInfo.getStr("loan_pic"));
		tmp.put("loanRiskDesc", "");
		tmp.put("loanDesc", loanInfo.getStr("loanDesc"));
		tmp.put("reciedCount", loanInfo.getInt("reciedCount"));
		tmp.put("effectDate", loanInfo.getStr("effectDate"));
		tmp.put("effectTime", loanInfo.getStr("effectTime"));
		tmp.put("benefits4new", loanInfo.getInt("benefits4new"));
		tmp.put("serverTime", DateUtil.getNowDateTime());
		tmp.put("lastPayLoanDateTime", loanInfo.getStr("lastPayLoanDateTime"));
		tmp.put("productType", loanInfo.getStr("productType"));
		tmp.put("loanThumbnail", loanInfo.getStr("loanThumbnail"));
		tmp.put("contractNo", loanInfo.getStr("contractNo"));
		String loanUserInfo = loanApplyService.findLoanUserDetail(loanInfo.getStr("loanNo"));
		if(StringUtil.isBlank(loanUserInfo) == false){
			JSONObject xx = JSONObject.parseObject(loanUserInfo);
			if(null != xx){
				xx.remove("loanTrueName");xx.remove("loanCardId");xx.remove("loanMobile");xx.remove("loanMail");
				tmp.put("loanUserInfo", xx.toJSONString());
			}
		}
		if("E".equals(loanInfo.getStr("productType"))){
			String loanDesc = loanInfo.getStr("loanDesc");
			JSONObject xx = new JSONObject();
			xx.put("loanUserName", "加载中...");
			xx.put("loanUserSex", "加载中...");
			xx.put("loanUserAge", "加载中...");
			xx.put("loanUserAddress", "加载中...");
			xx.put("loanUserWorkCity", "加载中...");
			xx.put("loanUserWorkType", "加载中...");
			xx.put("loanUserPurpose", "加载中...");
			xx.put("loanUserEducation", "加载中...");
			xx.put("userTrueName", "加载中...");
			xx.put("userCardId", "加载中...");
			if(null != loanDesc){
				xx = JSONObject.parseObject(loanDesc);
				YiStageUserInfo yiStageUserInfo = yiStageUserInfoService.queryByUserCode(loanInfo.getStr("userCode"));
				String mobile = yiStageUserInfo.getStr("mobile");
				String userCardId = yiStageUserInfo.getStr("userCardId");
				String userCardName = yiStageUserInfo.getStr("userCardName");
				mobile = mobile.substring(0, 3)+"*****"+mobile.substring(8, 11);
				userCardId = userCardId.substring(0, 4)+"********"+userCardId.substring(14,18);
				userCardName = userCardName.substring(0, 1)+"**";
				xx.put("loanUserName", mobile);
				xx.put("userTrueName", userCardName);
				xx.put("userCardId", userCardId);
			}
			tmp.put("yiStageUserInfo", xx.toJSONString());
		}
		if(!StringUtil.isBlank(userCode)){
			long avBalance = 0L;
			Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
			if(funds != null){
				avBalance = funds.getLong("avBalance");
			}
			tmp.put("avBalance", avBalance);
		}
		return succ("获取成功", tmp);
	}
	
	/**
	 * 逾期列表
	 * */
	@ActionKey("/queryOverdueList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryOverdueByUserCode(){
		String userCode = getUserCode();
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",10);
		Map<String, Object> result = new HashMap<String, Object>();
		Page<LoanTrace> loanTracePage = loanTraceService.queryOverdueList(userCode, pageNumber, pageSize);
		List<LoanTrace> loanTraces = loanTracePage.getList();
		for(int i = 0;i<loanTraces.size();i++){
			LoanTrace loanTrace = loanTraces.get(i);
			String loanCode = loanTrace.getStr("loanCode");
				LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanCode);
				if(loanInfo != null){
					loanTrace.put("effectDate", loanInfo.getStr("effectDate"));
					loanTrace.put("releaseDate", loanInfo.getStr("releaseDate"));
				}else{
					loanTrace.put("effectDate", loanTrace.getStr("loanDateTime").substring(0, 8));
					loanTrace.put("releaseDate", loanTrace.getStr("loanDateTime").substring(0, 8));
				}
				List<LoanOverdue> overdues = loanOverdueService.findByLoanCode(loanCode, "n", "");
				if(overdues.size()>0){
					LoanOverdue loanOverdue = overdues.get(0);
					int repayIndex = loanOverdue.getInt("repayIndex");
					loanTrace.put("repayIndex", repayIndex);
					loanTrace.put("overdueDate", loanOverdue.getStr("overdueDate"));
				}else{
					loanTrace.put("repayIndex", "");
					loanTrace.put("overdueDate", "00000000");
				}
				loanTrace.put("isOverdue", "yes");
				loanTrace.put("reciedOverdueTime", overdues.size());
				loanTrace.put("loanOverdues",overdues);
		}
		result.put("loanTracePage", loanTracePage);
		result.put("num", loanTracePage.getTotalRow());
		long allOverdueAmount = loanTraceService.sumOverdueAmountByUserCode(userCode);
		result.put("allOverdueAmount", allOverdueAmount);
		//8.15后总回款金额
		String beginDate = "20180815";
		long allBackInterestAmount = fundsServiceV2.sumTotalBackAmount(userCode,SysEnum.traceType.L.val(),beginDate,DateUtil.getNowDate());
		long allBackPrincipalAmount = fundsServiceV2.sumTotalBackAmount(userCode,SysEnum.traceType.R.val(),beginDate,DateUtil.getNowDate());
		result.put("allBackAmount", allBackInterestAmount+allBackPrincipalAmount);
		return succ("查询成功", result);
	}
	
	/**
	 * 设置用户债权转让方案
	 * */
	@ActionKey("/setTransferWay")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message setTransferWay(){
		String userCode = getUserCode();
		String transferWay = getPara("transferWay");
		String half = getPara("half");
		if(StringUtil.isBlank(transferWay)){
			return error("01", "获取方案失败", "");
		}
		//若为全部的   开始批量发布债权
		if(!CommonUtil.jxPort && !CommonUtil.fuiouPort){
			return error("01", "亲,存管系统正在上线哦", null);
		}
		User user = User.userDao.findById(userCode);
		if(CommonUtil.fuiouPort){
			return error("01", "版本过低无法进行此操作", "");
		}
		if(CommonUtil.jxPort){
			if(StringUtil.isBlank(user.getStr("jxAccountId"))){
				return error("24", "未激活存管帐号，不能进行此操作", "");
			}
			UserTermsAuth userTermsAuth = userTermsAuthService.findById(userCode);
			if(!userTermsAuth.isPaymentAuth()){
				return error("24", "缴费授权未开通", "");
			}
//		if(StringUtil.isBlank(loanTrace.getStr("jxTraceCode"))){
//							return error("24", "新存管上线前债权暂不支持转让", "");
//		}
		}
		String monthWay = "0";
		String qtrWay = "0";
		String halfWay = "0";
		String offlineWay = "0";
		String normalWay = "0";
		String payType = "";
		switch (transferWay) {
		case "A":
			monthWay = "1";
			if(!StringUtil.isBlank(half)){
				halfWay = "1";
			}else{
				payType = "E";
			}
			break;
		case "B":
			qtrWay = "1";
			if(!StringUtil.isBlank(half)){
				halfWay = "1";
			}else{
				payType = "F";
			}
			break;
		case "C":
			halfWay = "1";
			payType = "H";
			break;
		case "D":
			offlineWay = "1";
			break;
		case "E":
			normalWay = "1";
			break;
		default:
			break;
		}
		TransferWay userTransferWay = transferWayService.findByUserCode(userCode);
		if(userTransferWay == null){
			userTransferWay = new TransferWay();
		}else{
			return error("02", "无法重复提交", "");
		}
		userTransferWay.set("userCode", userCode);
		userTransferWay.set("month", monthWay);
		userTransferWay.set("qtr", qtrWay);
		userTransferWay.set("half", halfWay);
		userTransferWay.set("offline", offlineWay);
		userTransferWay.set("normal", normalWay);
		boolean result = transferWayService.saveTransferWay(userTransferWay);
		if(!result){
			return error("02", "设置失败", "");
		}
		
		if(StringUtil.isBlank(payType)){
			return succ("OK", "设置完成");
		}
		List<LoanTrace> loanTraces = loanTraceService.queryTrace(userCode);
		for(LoanTrace loanTrace:loanTraces){
			
			String traceCode = loanTrace.getStr("traceCode");
			int transFee = 0;
			
			//验证
			if(StringUtil.isBlank(traceCode)){
				continue;
			}
			//验证用户是否标的投资者
			String productType = loanTrace.getStr("productType");
			if(SysEnum.productType.E.val().equals(productType)){
				continue;
			}
//			String authCode = loanTrace.getStr("authCode");
//			if(StringUtil.isBlank(authCode)){
//				continue;
//			}
			
			String loanRecyDate = loanTrace.getStr("loanRecyDate");//下一还款日期
			//当天为T+1回款日不可发布债权
//			String tmpRecyDate = DateUtil.addDay(loanRecyDate, 1);
//			if(DateUtil.getNowDate().equals(tmpRecyDate)){
//				continue;
//			}
//			
			
			try {
				if(loanTrace.getStr("payUserCode").equals(userCode) == false){
					continue;
					}
			} catch (Exception e) {
				continue;
			}
			
			//验证是否已经转让
			//LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("select * from t_loan_transfer where transState != 'C' and traceCode = ? ", traceCode);
			String isTransfer = loanTrace.getStr("isTransfer");
			if(isTransfer.equals("A")){
				continue;
			}
			
			if("E".equals(payType)||"F".equals(payType)){
				transFee = (int)(loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount")-1);
			}else if("H".equals(payType)){
				transFee = (int)(loanTrace.getLong("leftAmount")+loanTrace.getLong("overdueAmount"))/2;
			}else{
				continue;
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
				continue;
			}
			//获取兑付回款方式
			if(StringUtil.isBlank(payType)||(!payType.equals("E")&&!payType.equals("F")&&!payType.equals("H"))){
				continue;
			}
			boolean updateTraceState = loanTraceService.updateTransferState(loanTrace.getStr("traceCode"), "C", "A");
			if(updateTraceState == false){
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "转让债权时修改投标流水状态修改异常", null);
				continue;
			}
			//保存债权信息
			//临时修改额外年利率（若用加息券）为transfer保存做准备
			if(rewardticketrate>0){
				int rewardRateByYear=loanTrace.getInt("rewardRateByYear");
				loanTrace.set("rewardRateByYear", rewardRateByYear-rewardticketrate);
			}
			//临时修改兑付回款方式
			loanTrace.set("refundType", payType);
			boolean ret = loanTransferService.saveOverdueLoanTransfer(transFee, 0 ,loanTrace);
			if(ret == false){
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权转让发布失败", null);
				continue;
			}
			BIZ_LOG_INFO(traceCode, BIZ_TYPE.TRANSFER, "债权转让发布成功");
		}
		return succ("OK", "设置完成");
	}
	
	/**
	 * 分页查询机构债转  50%
	 * */
	@ActionKey("/queryTransferList4JG")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message queryTransferList4JG(){
		int pageNumber = getParaToInt("pageNumber",1);
		int pageSize = getParaToInt("pageSize",1);
		String transState = getPara("transState");
		Page<LoanTransfer> page = loanTransferService.queryTransfer4JG(pageNumber, pageSize, transState, "ASC", "transDateTime");
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pageNumber", page.getPageNumber());
		map.put("pageSize", page.getPageSize());
		map.put("totalPage", page.getTotalPage());
		map.put("totalRow", page.getTotalRow());
		List<LoanTransfer> loanTransfers = page.getList();
		for(int i = 0;i<loanTransfers.size();i++){
			LoanTransfer loanTransfer = loanTransfers.get(i);
			String transDate = loanTransfer.getStr("transDate");
			String transTime = loanTransfer.getStr("transTime");
			int uid = loanTransfer.getInt("uid");
			long x = Db.queryLong("select count(1) from t_loan_transfer where refundType = 'H' and transState in ('A','B') and CONCAT(transDate,transTime) <=? ORDER BY transDate asc,transTime asc,uid asc",transDate+transTime);
			long y = Db.queryLong("select count(1) from t_loan_transfer where refundType = 'H' and transState in ('A','B') and CONCAT(transDate,transTime) = ? and uid > ? ORDER BY transDate asc,transTime asc,uid asc",transDate+transTime,uid);
			loanTransfer.put("NO", x-y);
		}
		map.put("list", page.getList());
		return succ("查询成功", map);
	}
	
	/**
	 * 查询用户是否已设置债权方案
	 * */
	@ActionKey("/queryTransferWay")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryTransferWay(){
		String userCode = getUserCode();
		TransferWay transferWay = transferWayService.findByUserCode(userCode);
		if(transferWay == null){
			return error("01", "还未设置", "");
		}else{
			return succ("已设置成功", transferWay);
		}
		
	}
}
