package com.dutiantech.controller.admin;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.pay.SafeUtil;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.User;
import com.dutiantech.plugins.HttpRequestor;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.HistoryRecyService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.asyn.SMSTaskService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.loanState;
import com.dutiantech.util.SysEnum.traceState;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class SecretController extends BaseController {
	
	private HistoryRecyService historyRecyService = getService(HistoryRecyService.class);
	
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private RecommendRewardService rrService = getService(RecommendRewardService.class);
	
	private UserService userService = getService(UserService.class);
	
	private TicketsService ticketService = getService(TicketsService.class);
	
	
	
//	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
//	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	
//	/**
//	 * 先息后本补本金
//	 * @return
//	 */
//	@ActionKey("/bubenjinFromLast")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
//	public Message advRecy4loan(){
//		String who = getPara("whoareyou","fuck");
//		if(who.equals("3.14159265358")){
//			String traceCode = getPara("traceCode");
//			LoanTrace loanTrace = loanTraceService.findById(traceCode);
//			if(loanTrace.getInt("loanRecyCount")>0){
//				return error("01", "该投标流水待还期数大于0", null);
//			}
//			if(!loanTrace.getStr("loanState").equals("O")){
//				return error("02", "该投标流水状态不是还款完成", null);
//			}
//			if(!loanTrace.getStr("refundType").equals("B")){
//				return error("03", "该投标流水不是先息后本的还款方式", null);
//			}
//			fundsServiceV2.doAvBalance4biz(loanTrace.getStr("payUserCode"), loanTrace.getLong("payAmount") , 0 ,  traceType.R , fundsType.J , 
//					String.format("标[%s]第%d/%d期回收本金", loanTrace.getStr("loanNo") , loanTrace.getInt("loanTimeLimit"),loanTrace.getInt("loanTimeLimit")));
//			String msgContent = loanTrace.getStr("payUserName")+","+loanTrace.getLong("payAmount")+","+ loanTrace.getStr("loanNo");
//			new SMSTaskService(msgContent, "10", "回款", loanTrace.getStr("payUserCode")).run();
//			return succ("ok", null);
//		}
//		return error("09", "who are you", null);
//	}
	
	@ActionKey("/reOpenAutoLoan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message reOpenAutoLoan(){
		
		String tmpKey = getPara("key");
		if( tmpKey.equals("selangshiwo.com") == false ){
			return error("01", "呵呵,哈哈", null ) ;
		}
		String loanCode = getPara("loanCode");
		int result = Db.update("update t_loan_info set loanATAmount=loanBalance "
				+ "where loanState='J' and loanATAmount=0 and loanBalance>0 and loanCode = ?",loanCode);
		if( result <= 0 ){
			return error("02", "未发现需要更新的标", result ) ;
		}
		
		return succ("成功更新" + result + "个标",result ) ;
	}
	
	@ActionKey("/checkTotalWithdraw")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message checkTotalWithdraw(){
		
		String tmpKey = getPara("key");
		
		if( tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵哒", null ) ;
		}
		
		if(StringUtil.isBlank(tmpKey)){
			return error("02", "呵呵哒", null ) ;
		}
		
		int success_count = 0;
		int x=1,y=100;
		
		Page<Funds> pages =  fundsServiceV2.findByPage(x, y, null, null, null, null);
		
		for (int i = 0; i < pages.getList().size(); i++) {
			Funds funds = pages.getList().get(i);
			long totalWithdraw = funds.getLong("totalWithdraw");
			String userCode = funds.getStr("userCode");
			long tmp_totalWithdraw = Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceType in ('G','E') and userCode = ?",userCode).longValue();
			if(totalWithdraw != tmp_totalWithdraw){
				success_count++;
				System.out.println("userCode："+userCode);
				System.out.println("统计提现金额："+totalWithdraw);
				System.out.println("实际提现金额："+tmp_totalWithdraw);
				Db.update("update t_funds set totalWithdraw = ? where userCode = ?",tmp_totalWithdraw,userCode);
			}
		}
		
		boolean isLastPage = pages.isLastPage();
		while(!isLastPage){
			x++;
			pages = fundsServiceV2.findByPage(x, y, null, null, null, null);
			for (int i = 0; i < pages.getList().size(); i++) {
				Funds funds = pages.getList().get(i);
				long totalWithdraw = funds.getLong("totalWithdraw");
				String userCode = funds.getStr("userCode");
				long tmp_totalWithdraw = Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) from t_funds_trace where traceType in ('G','E') and userCode = ?",userCode).longValue();
				if(totalWithdraw != tmp_totalWithdraw){
					success_count++;
					System.out.println("userCode："+userCode);
					System.out.println("统计提现金额："+totalWithdraw);
					System.out.println("实际提现金额："+tmp_totalWithdraw);
					Db.update("update t_funds set totalWithdraw = ? where userCode = ?",tmp_totalWithdraw,userCode);
				}
			}
			isLastPage = pages.isLastPage();
		}
		return succ("任务完成", success_count);
	}
	
//	@ActionKey("/test123")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
//	public Message test123(){
//		String userCode = "486a56a537a04a9fb26ff88e652d2bca";
//		Object []x = Db.queryFirst("select COALESCE(sum(loanRecyCount),0),COALESCE(sum(leftAmount),0),COALESCE(sum(leftInterest),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode);
////		long beRecyCount = Db.queryBigDecimal("select COALESCE(sum(loanRecyCount),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
////		long beRecyPrincipal = Db.queryBigDecimal("select COALESCE(sum(leftAmount),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
////		long beRecyInterest = Db.queryBigDecimal("select COALESCE(sum(leftInterest),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
//		long beRecyCount = Long.valueOf( x[0] +"");
//		long beRecyPrincipal = Long.valueOf( x[1]+"");
//		long beRecyInterest = Long.valueOf( x[2] + "");
////		Db.update("update t_funds set beRecyCount = ?,beRecyPrincipal = ?,beRecyInterest = ? where userCode = ?",beRecyCount,beRecyPrincipal,beRecyInterest,userCode);
//		return succ("重置用户待回收笔数、待回收本金、待回收利息 完成", beRecyCount +" " + beRecyPrincipal + " " + beRecyInterest);
//	}
	
	/**
	 * 重置用户待回收笔数、待回收本金、待回收利息
	 * @return
	 */
	
	@ActionKey("/resetBeRecy")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	@SuppressWarnings("unchecked")
	public Message resetBeRecyCount(){
		String tmpKey = getPara("key");
		if( tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵,哈哈", null ) ;
		}
		int startIndex = 0 ;
		int size = 50 ;
		boolean isGo = true ;
		long total = Db.queryLong("select count(uid) from t_user");
		Db.update("update t_funds set beRecyCount = 0, beRecyPrincipal = 0, beRecyInterest = 0");
		int doCount = 1 ;
		while( isGo ){
			List<Object[]> users = getTaskUsers(startIndex, size) ;
			if( doCount <= total ){
				for(Object[] user : users){
					System.err.println("[重置用户待回收笔数、待回收本金、待回收利息]当前进度第" + (doCount)+"/"+ total + "个用户" );
					String userCode = (String) user[0];
					String userName = (String) user[1];
					Object []x = Db.queryFirst("select COALESCE(sum(loanRecyCount),0),COALESCE(sum(leftAmount),0),COALESCE(sum(leftInterest),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode);
//					long beRecyCount = Db.queryBigDecimal("select COALESCE(sum(loanRecyCount),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
//					long beRecyPrincipal = Db.queryBigDecimal("select COALESCE(sum(leftAmount),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
//					long beRecyInterest = Db.queryBigDecimal("select COALESCE(sum(leftInterest),0) from t_loan_trace where payUserCode = ? and loanState = 'N'",userCode).longValue();
					long beRecyCount = Long.valueOf( x[0] +"");
					long beRecyPrincipal = Long.valueOf( x[1]+"");
					long beRecyInterest = Long.valueOf( x[2] + "");
					Db.update("update t_funds set beRecyCount = ?,beRecyPrincipal = ?,beRecyInterest = ? where userCode = ?",beRecyCount,beRecyPrincipal,beRecyInterest,userCode);
					System.out.println("[重置用户待回收笔数、待回收本金、待回收利息]处理["+userCode+"][" + userName +"]");
					doCount ++ ;
				}
			}else{
				isGo = false ;
			}
			startIndex ++ ;
		}
		return succ("重置用户待回收笔数、待回收本金、待回收利息 完成", doCount);
	}
	
	/**
	 * 	获取用户列表
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getTaskUsers(int index , int size ){
		index = index * size;
		String querySql = "select userCode,userName from t_user limit ?,?";
//		querySql = querySql.replace("${index}", index+"" ) ;
//		querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql , index , size ) ;
		return users ;
	}
	
	
	/**
	 * 连连支付，代付
	 * @return
	 */
	
	@ActionKey("/lldaifu")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message lldaifu(){
		
		String tmpKey = getPara("key");
		if( StringUtil.isBlank(tmpKey) || tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵哒", null ) ;
		}
		
		String bankCode = getPara("bankCode");//银行分类代码
		
		String bankName = getPara("bankName");//银行名字
		
		String cardNo = getPara("bankNo");//银行卡号
		
		String accName = getPara("userTrueName");//真实姓名
		
		long rePayAmount = getParaToLong("withdrawAmount");//代付金额,单位分
		
		String provinceName = getPara("provinceName");//省级名称，如 湖北   北京
		
		String cityName = getPara("cityName");//市级名称，如 武汉  北京
		
		int sxf = getParaToInt("sxf",0);
		
		rePayAmount = rePayAmount - sxf;
		
		double rePayAmount4double = CommonUtil.yunsuan(rePayAmount + "", "100.00", "chu", 2).doubleValue();
		String infoOrder = "连连支付付款-" + cardNo + "(" + accName +")"+"[￥"+rePayAmount4double+"]";
		String prcptcd = "";

		String order_no = UIDUtil.generate();
		Map<String , String> repayInfo = initRepayInfo() ;
		repayInfo.put("money_order", String.valueOf( rePayAmount4double ) );
		repayInfo.put("acct_name", accName );
		repayInfo.put("card_no", cardNo );
		repayInfo.put("flag_card", "0" );
		repayInfo.put("bank_code", bankCode);
		
		long province_code = 0;
		long city_code = 0;
		
		Integer tmp1 = Db.queryInt("select id from t_location where areaname =? or shortname=?",provinceName,provinceName);
		if(tmp1!=null)
			province_code = tmp1.intValue();
		
		Integer tmp2 = Db.queryInt("select id from t_location where areaname =? or shortname=?",cityName,cityName);
		if(tmp2!=null)
			city_code = tmp2.intValue();
		else
			city_code = province_code;
		
		if(province_code==110000){
			city_code = 110000;
		}
		if(province_code==120000){
			city_code = 120000;
		}
		if(province_code==310000){
			city_code = 310000;
		}
		if(province_code==500000){
			city_code = 500000;
		}
		
		repayInfo.put("province_code", province_code+"");
		repayInfo.put("city_code", city_code+"");
		repayInfo.put("brabank_name", bankName);
		repayInfo.put("info_order", infoOrder);
		repayInfo.put("prcptcd", prcptcd);
		repayInfo.put("no_order", order_no);
		sign4lianlianWithRSA( repayInfo );
		
		HttpRequestor http = new HttpRequestor() ;
		JSONObject json = null ;
		String responseBody = "";
		try {
			responseBody = http.doPost( "https://traderapi.lianlianpay.com/cardandpay.htm" , JSONObject.toJSONString(repayInfo) ) ;
			json = JSONObject.parseObject(responseBody) ;
		} catch (Exception e) {
			e.printStackTrace();
			json = new JSONObject() ;
			json.put("ret_code", "xxxx");
			json.put("ret_msg", "系统异常：" + e.getMessage()+"--------"+responseBody );
		}
		
		String retCode = json.getString("ret_code") ;
		
		if( "0000".equals(retCode) == true ){
			return succ("代付已受理", order_no);
		}else{
			return error("01", "代付失败", retCode+json.getString("ret_msg"));
		}
	}
	
	private Map<String , String> initRepayInfo(){
		String MER_NO = String.valueOf( CACHED.get("S2.paymerno_lianlian") );
		String NOTIFY_HOST = String.valueOf( CACHED.get("S2.notify_host_lianlian") );
		
		Map<String , String> trace = new TreeMap<String ,String>();
		String dt = DateUtil.getNowDateTime() ;
		
		trace.put("platform", "yrhx.com") ;
		trace.put("oid_partner", MER_NO );
		trace.put("sign_type", "RSA");
		trace.put("no_order", UIDUtil.generate() );
		trace.put("dt_order", dt );
		trace.put("notify_url", NOTIFY_HOST + "/lianlian_repayNotify4lianlian");
		trace.put("api_version", "1.2");
		
		return trace ;
	}
	
	public void sign4lianlianWithRSA(Map<String, String> params){
		Iterator<String> keys = params.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = params.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		String tmpString = buff.toString() ;
		String priKey = String.valueOf( CACHED.get("S2.rsa_prikey_lianlian"));
		String signValue = SafeUtil.sign( priKey , tmpString ) ;
		if( signValue != null ){
			params.put("sign", signValue ) ;
		}
	}
	
	/**
	 * 自动还款之后补充提前还款
	 * @return
	 */
	@ActionKey("/elarySettlementAfterAutoRecy")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message elarySettlementAfterAutoRecy(){
		String tmpKey = getPara("key");
		if( StringUtil.isBlank(tmpKey) || tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵哒", false ) ;
		}
		String loanCode = getPara("loanCode","");
		
		if(StringUtil.isBlank(loanCode)){
			return error("02", "呵呵哒", false);
		}
		
		long x = Db.queryLong("select count(loanCode) from t_loan_info where loanCode = ?",loanCode);
		if(x<1){
			return error("03", "呵呵哒", false); 
		}
		doTransfer(loanCode);	//检查未处理的债权
		LoanInfo loan = LoanInfo.loanInfoDao.findById(loanCode);
		/**
		 * 	1，结算下一个结算周期利息
		 * 	2，计算剩余本金，直接退回本金
		 */
		//更新流水
		List<LoanTrace> loanTraces = loanTraceService.findLoanTraceByJieSuan( loanCode ) ;
		doLoanTrace4adv(0 , loan , loanTraces );
		
		doLoanInfo4adv(loan) ;	//更新标相关事物
		Db.update("update t_sms_log set `status`=9 where `status`=8 and type=10 and sendDate = ?",DateUtil.getNowDate());
		return succ("ok","后置的提前还款操作完成!") ;
	}
	
	/**
	 * 	提前还款时，标相关处理
	 * @param loan
	 * @return
	 */
	private boolean doLoanInfo4adv(LoanInfo loan ){
		String nowSDate = DateUtil.getStrFromDate(new Date(), "MMdd");
		int reciedCount = loan.getInt("reciedCount");
		int limit = loan.getInt("loanTimeLimit");
		loan.set("loanState", loanState.P.val() ) ;
		loan.set("clearDate", nowSDate ) ;	//更新日期
		loan.set("backDate", DateUtil.getNowDate() ) ;	//更新日期
		String loanUserCode = loan.getStr("userCode");
		loan.set("updateDate", DateUtil.getNowDate());
		loan.set("updateTime", DateUtil.getNowTime());
		if( loan.update() == true ){
			long amount = loan.getLong("loanAmount");
			int rate = loan.getInt("rateByYear");
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			int benefits4new = loan.getInt("benefits4new");
			rate = rate + rewardRateByYear + benefits4new;
			String refundType = loan.getStr("refundType");
			//标信息修改成功，修改借款人资金账户
			Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode) ;

			int loanCount = funds.getInt("loanCount");
			int loanSuccessCount = funds.getInt("loanSuccessCount");
			int loanBySysCount = funds.getInt("loanBySysCount");
			long beRecyPrincipal4loan = funds.getLong("beRecyPrincipal4loan");
			long beRecyInterest4loan = funds.getLong("beRecyInterest4loan");
			
			//计算剩余本金利息
			long[] lastBenxi = CommonUtil.f_006(amount, rate, limit, reciedCount , refundType) ;
			
			long avBalance = funds.getLong("avBalance") ;
			if( lastBenxi[0] > avBalance ){
				if(avBalance>=0){
					funds = fundsServiceV2.recharge( loanUserCode, lastBenxi[0]-avBalance,0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
				}else{
					funds = fundsServiceV2.recharge( loanUserCode, lastBenxi[0],0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
				}
				loanBySysCount += (limit-reciedCount);
			}else{
				loanSuccessCount += (limit-reciedCount) ;
			}
			
			if( lastBenxi[0] > 0 ){
				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, lastBenxi[0]  , 0 , traceType.U , fundsType.D , 
						String.format("标[%s]第%d/%d期提前还款还本金", loan.getStr("loanNo") , reciedCount,limit ) );
			}
			//更新资金账户冗余信息
			funds.set("loanCount", loanCount  - (limit-reciedCount) ) ;
			funds.set("loanSuccessCount", loanSuccessCount ) ;
			funds.set("loanBySysCount", loanBySysCount ) ;
			funds.set("beRecyPrincipal4loan", beRecyPrincipal4loan - lastBenxi[0] ) ;
			funds.set("beRecyInterest4loan", beRecyInterest4loan - lastBenxi[1]  ) ;
			return funds.update() ;
			//更新账户余额,需要加流水
		}
		
		return true ;
	}
	
	/**
	 * 	提前还款流水处理
	 */
	@SuppressWarnings("unused")
	private void doLoanTrace4adv(int index , LoanInfo loan , List<LoanTrace> traces ){

		if( traces.size() == index ){
			return ;
		}
		LoanTrace trace = traces.get(index) ;
		index ++ ;//next
		
		String payUserCode = trace.get("payUserCode","");//投标人编码 
		String payUserName = trace.get("payUserName","");//投标人名称 
		String loanUserName = trace.get("loanUserName","");//借款人名称 
		String loanNo = trace.get("loanNo","");
		
		long payAmount = trace.getLong("payAmount");//投标金额 
		
		String trace_refundType = trace.get("refundType","O");//投标还款方式 
		int trace_rateByYear = trace.getInt("rateByYear");//投标年利率 
		int rewardRateByYear = trace.getInt("rewardRateByYear");//奖励年利率 
		trace_rateByYear = trace_rateByYear + rewardRateByYear;
		int loanTimeLimit = loan.getInt("loanTimeLimit");//还款期数 
		int reciedCount = loan.getInt("reciedCount");	//已还期数
		
		long interest = trace.getLong("nextInterest");	//只需要利息
		long traceFee = 0 ;

		long lastPrincipal = trace.getLong("leftAmount");
		long lastInterest = trace.getLong("leftInterest");
		
		//更新资金账户信息
		if( lastPrincipal > 0 ){
			fundsServiceV2.doAvBalance4biz(payUserCode, lastPrincipal , 0 ,  traceType.R , fundsType.J , 
					String.format("标[%s]第%d/%d期提前还款回收本金", loanNo , (reciedCount),loanTimeLimit));
		}

		fundsServiceV2.updateBeRecyFunds(payUserCode, 0-trace.getInt("loanRecyCount") , -lastPrincipal , 
				-lastInterest , lastPrincipal , interest ) ;
		
		/*更新投标流水*/
		long x1 = trace.getLong("leftInterest");
		long x2 = trace.getLong("nextInterest");
		trace.set("loanState", loanState.P.val() ) ;
		trace.set("traceState", traceState.B.val() ) ;
		trace.set("loanRecyDate", DateUtil.getNowDate()) ;
		trace.set("nextAmount",0 );
		trace.set("nextInterest",0 );
		trace.set("leftAmount" , 0 );
		trace.set("leftInterest" , x1-x2 );
		trace.update() ;
		
		try {
			String msgContent = payUserName+","+(interest+lastPrincipal-traceFee)+","+loanNo;
			new SMSTaskService(msgContent, "10", "回款", payUserCode).run();
			long x = 0;long y = 0;
			historyRecyService.save(loanNo, loan.getStr("loanCode"), trace.getStr("loanTitle"), trace.getStr("loanState"), trace.getStr("loanUserName"), loan.getStr("userCode"), payUserName, payUserCode, lastPrincipal, interest, payAmount,x, y,reciedCount+1,loanTimeLimit);

		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			doLoanTrace4adv(index , loan , traces );
		}
		
	}
	
	/**
	 * 	检查债权转让，如果有结算当天的债权，直接取消债权
	 * 
	 */
	private void doTransfer(String loanCode){
		if( StringUtil.isBlank(loanCode) == true ){
			//取消所有当天结算的债权
			String nowDate = DateUtil.getNowDate() ;
			Db.update("update t_loan_transfer set transState='C' where transState='A' and nextRecyDay=? ",nowDate);
			//修改投标流水状态
			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanRecyDate=?" , nowDate ) ;
		}else{
			Db.update("update t_loan_transfer set transState='C' where transState='A' and loanCode=? ",loanCode);
			//修改投标流水状态
			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanCode=?" , loanCode ) ;
		}
		
	}
	
	/**
	 * 补借款人提前还款的资金
	 * @return
	 */
	@ActionKey("/subUserTrace2")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message subUserTrace2(){
		String tmpKey = getPara("key");
		if( tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵,哈哈", null ) ;
		}
		String loanCode = getPara("loanCode");
		
		int limitIndex = getParaToInt("limitIndex");
		if(StringUtil.isBlank(loanCode)){
			return error("02", "呵呵哒", null ) ;
		}
		LoanInfo loan = loanInfoService.findById(loanCode);
		if(loan==null){
			return error("03", "呵呵哒", null ) ;
		}
		String loanUserCode = loan.getStr("userCode");
		long amount = loan.getLong("loanAmount");
		int rate = loan.getInt("rateByYear");
		int rewardRateByYear = loan.getInt("rewardRateByYear");
		int benefits4new = loan.getInt("benefits4new");
		rate = rate + rewardRateByYear + benefits4new;
		String refundType = loan.getStr("refundType");
		int limit = loan.getInt("loanTimeLimit");
		if(refundType.equals("B")){
			Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode) ;
			long[] lastBenxi = CommonUtil.f_006(amount, rate, limit, limitIndex-1 , refundType) ;
			//计算剩余本金利息
			if(lastBenxi[0]>funds.getLong("avBalance")){
				if(funds.getLong("avBalance") >= 0){
					fundsServiceV2.recharge( loanUserCode, lastBenxi[0]-funds.getLong("avBalance"),0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
				}else{
					fundsServiceV2.recharge( loanUserCode, lastBenxi[0],0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
				}
			}
			if( lastBenxi[0] > 0 ){
				fundsServiceV2.doAvBalance4biz(loanUserCode, lastBenxi[0] , 0 , traceType.U , fundsType.D , String.format("标[%s]第%d/%d期提前还款还本金", loan.getStr("loanNo") , limitIndex,limit ) );
			}
		}
		return succ("完成补充先息后本的借款人提前还款，本金部分", true);
	}
	
	/**
	 * 补借款人提前还款的资金
	 * @return
	 */
	@ActionKey("/subTicketFundsTrace")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message subTicketFundsTrace(){
		String tmpKey = getPara("key");
		if( tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵,哈哈", null ) ;
		}
		String userCode = getPara("userCode");
		
		long ticketAmount = getParaToInt("ticketAmount",0);
		
		if(StringUtil.isBlank(userCode)){
			return error("02", "呵呵哒", null ) ;
		}
		
		if(ticketAmount<1){
			return error("02", "呵呵哒", null ) ;
		}
		
		boolean x = fundsServiceV2.updateFundsWhenFullLoanWithTicket(userCode, ticketAmount);
		
		return succ("回补抵用券资金流水"+userCode, x);
	}
	
	/**
	 * 重置还款中的投标流水  剩余本金 剩余利息  下期本金 下期利息
	 * @return
	 */
	@ActionKey("/resetLoanTrace")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message resetLoanTrace(){
		String tmpKey = getPara("key");
		if( tmpKey.equals("asuuu.com") == false ){
			return error("01", "呵呵,哈哈", null ) ;
		}
		List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanState = 'N'");
		for (int i = 0; i < loanTraces.size(); i++) {
			String traceCode = loanTraces.get(i).getStr("traceCode");
			long payAmount = loanTraces.get(i).getLong("payAmount");
			int loanTimeLimit = loanTraces.get(i).getInt("loanTimeLimit");
			int loanRecyCount = loanTraces.get(i).getInt("loanRecyCount");
			int yihuanCount = loanTimeLimit - loanRecyCount;
			String refundType = loanTraces.get(i).getStr("refundType");
			int rateByYear = loanTraces.get(i).getInt("rateByYear") + loanTraces.get(i).getInt("rewardRateByYear");
			long [] leftBenXi = CommonUtil.f_006(payAmount, rateByYear, loanTimeLimit, yihuanCount, refundType);
			long [] nextBenXi = CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear, yihuanCount+1, refundType);
			Db.update("update t_loan_trace set leftAmount = ?,leftInterest = ?,nextAmount = ?,nextInterest = ? where traceCode = ?",leftBenXi[0],leftBenXi[1],nextBenXi[0],nextBenXi[1],traceCode);
			System.out.println("重置剩余本金、剩余利息、下期待收本金、下期待收利息====第"+(i+1)+"条，共"+loanTraces.size()+"条");
		}
		
		return succ("重置剩余本金、剩余利息、下期待收本金、下期待收利息完成", loanTraces.size());
	}
	
	@ActionKey("/subyaoqing")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message subyaoqing(){
		String aUserCode = getPara("aCode","");
		String bUserCode = getPara("bCode","");
		if(StringUtil.isBlank(aUserCode) || StringUtil.isBlank(bUserCode)){
			return error("01", "滚", null);
		}
		fundsServiceV2.rechargeTZFX_HY(aUserCode, 10000, 0, "邀请好友投资奖励");
		rrService.save(aUserCode, bUserCode, 10000, "A", "好友投资满1万，奖励推荐人100元");
		return succ("搞掂", null);
	}
	
	@ActionKey("/addTicket")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message addTicket(){
		String mobile = getPara("mobile");
		User user = userService.find4mobile(mobile);
		if(user==null)
			return error("01", "用户不存在", false);
		int payAmount = getParaToInt("payAmount");
		int expAmount =  getParaToInt("expAmount");
		String tName = getPara("tName");
		int limitDay = getParaToInt("limitDay");
		
		boolean result = ticketService.saveADV(user.getStr("userCode"), tName, DateUtil.addDay(DateUtil.getNowDate(), limitDay), payAmount, expAmount);
		if(result)
			return succ("ok", true);
		
		return error("09", "失败",false);
	}
	
	
	/**
	 * 同步邀请关系
	 * @return
	 */
	
	@ActionKey("/share2recommendInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message share2recommendInfo(){
		long total = 0;
		List<Object[]> list = Db.query("select userCode,userName,friendMobile,regDate,regDateTime from t_share ");
		for (int i = 0; i < list.size(); i++) {
			try {
				Object[] cc = list.get(i);
				String aUserCode = (String) cc[0];
				String aUserName =  Db.queryStr("select userName from t_user where userCode = ?",aUserCode);
				if(StringUtil.isBlank(aUserName)){
					aUserName = "";
				}
				String bUserCode = "";
				String bUserName = (String) cc[1];
				String bm = (String) cc[2];
				bm = CommonUtil.encryptUserMobile(bm);
				String b = Db.queryStr("select userCode from t_user where userMobile = ?",bm);
				if(StringUtil.isBlank(b) == false){
					bUserCode = b;
				}
				String bRegDate = (String) cc[3];
				String bRegTime = ((String) cc[4]).replace(bRegDate, "");
				String rmdType = "";
				String rmdRemark = "好友推荐注册";
				RecommendInfo rmdInfo = new RecommendInfo();
				rmdInfo.set("aUserCode", aUserCode);
				rmdInfo.set("aUserName", aUserName);
				rmdInfo.set("bUserCode", bUserCode);
				rmdInfo.set("bUserName", bUserName);
				rmdInfo.set("bRegDate", bRegDate);
				rmdInfo.set("bRegTime", bRegTime);
				rmdInfo.set("rmdType", rmdType);
				rmdInfo.set("rmdRemark", rmdRemark);
				long x = Db.queryLong("select COALESCE(count(rid),0) from t_recommend_info where aUserCode = ? and bUserCode = ?",aUserCode,bUserCode);
				if(rmdInfo.save() && x < 1){
					total ++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return succ("共导入"+total+"条推荐注册记录", null);
	}
	
	public static void main(String[] args) {
		int loan_ll = 1800;
		int trace_ll = 1830;
		long payAmount = 600000;
		int limit = 2;
		String refundType = "A";
		
		long zlx_loan = CommonUtil.f_004(payAmount, loan_ll, limit, refundType)[1];
		System.out.println("错误总利息："+zlx_loan);
		long zlx_trace = CommonUtil.f_004(payAmount, trace_ll, limit, refundType)[1];
		System.out.println("修复后总利息："+zlx_trace);
		System.out.println("总息差："+(zlx_trace - zlx_loan));
		long f_lx_loan = CommonUtil.f_000(payAmount, limit ,loan_ll , 1, refundType )[1];
		System.out.println("1:"+91.5);
		System.out.println("错误的第一期利息："+f_lx_loan);
		long f_lx_trace = CommonUtil.f_000(payAmount, limit ,trace_ll , 1, refundType )[1];
		System.out.println("修复后的第一期利息："+f_lx_trace);
		System.out.println("第一期利息差："+(f_lx_trace-f_lx_loan));
		long s_lx_trace = CommonUtil.f_000(payAmount, limit ,trace_ll , 2, refundType )[1];
		System.out.println("第二期利息"+s_lx_trace);
		System.out.println("剩余利息："+(zlx_trace - f_lx_trace));
		
	}
	
	/**
	 * 修复自动投标加息审核满标造成的bug
	 * @return
	 */
	
	@ActionKey("/ggjiaxi")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message ggjiaxi(){
		int gg = 0;int ok1 = 0;int ok2 = 0;int isok = 0;
		List<LoanTrace> traces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanDateTime >= '201609200000'  and loanState = 'N' and (rewardRateByYear = 30 or rewardRateByYear = 50 or rewardRateByYear = 20 or rewardRateByYear = 320 or rewardRateByYear = 500) ");
		System.out.println("加息有问题的投标流水共：" +traces.size()+"笔");
		for (int i = 0; i < traces.size(); i++) {
			LoanTrace loanTrace = traces.get(i);
			int loanTimeLimit = loanTrace.getInt("loanTimeLimit");
			int loanRecyCount = loanTrace.getInt("loanRecyCount");
			long nextInterest = loanTrace.getLong("nextInterest");
			long leftInterest = loanTrace.getLong("leftInterest");
			String loanCode = loanTrace.getStr("loanCode");
			//投标流水的总利率
			int loanTrace_ll = loanTrace.getInt("rateByYear") + loanTrace.getInt("rewardRateByYear");
			LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanCode);
			//借款标的总利率
			int loan_ll = loanInfo.getInt("rateByYear") + loanInfo.getInt("rewardRateByYear") + loanInfo.getInt("benefits4new");
			long payAmount = loanTrace.getLong("payAmount");
			String traceCode = loanTrace.getStr("traceCode");
			String payUserCode = loanTrace.getStr("payUserCode");
			String loanTitle = loanTrace.getStr("loanTitle");
			String refundType = loanTrace.getStr("refundType");
			int lvc = loanTrace_ll - loan_ll;
			if( (lvc == 30) || (lvc == 20) || (lvc == 50)){
				gg ++;
				long zlx_trace = CommonUtil.f_004(payAmount, loanTrace_ll, loanTimeLimit, refundType)[1];
				long zlx_loan = CommonUtil.f_004(payAmount, loan_ll, loanTimeLimit, refundType)[1];
				//总息差
				long zg_lxc = zlx_trace - zlx_loan;
				//第一期息差
				long f_lx_trace = CommonUtil.f_000(payAmount, loanTimeLimit ,loanTrace_ll , 1, refundType )[1];
				long f_lx_loan = CommonUtil.f_000(payAmount, loanTimeLimit ,loan_ll , 1, refundType )[1];
				
				//第一次投标利息差
				long f_lxc = f_lx_trace - f_lx_loan ;
				if(zg_lxc > 0){
					if((loanTimeLimit - loanRecyCount) == 1){
						//TODO 已结算1期的, 需要修复投标流水下一期利息，剩余利息，资金账户的待收利息、已收利息
						
						//第二期利息
						long s_lx_trace = CommonUtil.f_000(payAmount, loanTimeLimit ,loanTrace_ll , 2, refundType )[1];
						if(loanRecyCount == 1){
							//最后一期
							s_lx_trace = zlx_trace-f_lx_trace;//总利息-第一期利息
						}
						if(nextInterest == s_lx_trace && leftInterest == zlx_trace-f_lx_trace ){
							//无需修复的,数据一致的
							isok ++ ;
							continue;
						}
						
						System.out.println("已还1期："+loanTitle + "["+loanCode+"]"+"投标流水："+traceCode+",差额:总利息差"+zg_lxc);
						Db.update("update t_loan_trace set nextInterest = ?,leftInterest = ? where traceCode = ?",s_lx_trace,zlx_trace - f_lx_trace,traceCode);
						Db.update("update t_funds set beRecyInterest = beRecyInterest + ?,reciedInterest = reciedInterest + ? where userCode =?",zg_lxc - f_lxc,f_lxc,payUserCode);
						//补息
						fundsServiceV2.doAvBalance4biz(payUserCode, f_lxc , 0 ,  traceType.L , fundsType.J , 
								String.format("标[%s]第%d/%d期回收利息,自动加息补息", loanTrace.getStr("loanNo") , 1,loanTimeLimit));
						ok1 ++;
						
					}else if(loanTimeLimit == loanRecyCount){
						if(nextInterest == f_lx_trace && zlx_trace == leftInterest){
							//数据一致的 无需修复的
							isok ++ ;
							continue;
						}
						System.out.println("未还"+loanTitle + "["+loanCode+"]"+"投标流水："+traceCode+",差额:总利息差"+zg_lxc);
						//TODO 未结算的，需要修复投标流水的第一期利息、剩余利息，资金账户的待收利息
						Db.update("update t_loan_trace set nextInterest = ?,leftInterest = ? where traceCode = ?",f_lx_trace,zlx_trace,traceCode);
						Db.update("update t_funds set beRecyInterest = beRecyInterest + ? where userCode =?",zg_lxc,payUserCode);
						ok2++;
					}
				}
			}
			
		}
		return succ("息差的总共:"+gg+"笔投标流水，已还一期的修复 "+ok1+"笔,未还的已修复"+ok2+"笔，总修复"+(ok1+ok2)+"笔，无需修复的"+isok+"笔", true);
	}

}
