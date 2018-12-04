package com.dutiantech.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.portal.LoanCenterController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.vo.VipV2;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AutoLoanV3Controller extends BaseController {

	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private UserService userService = getService(UserService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private AutoMapSerivce autoMapService = getService(AutoMapSerivce.class ) ;
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
//	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private static final Logger autoTouBiaoLogger = Logger.getLogger("autoTouBiaoLogger");
	
	static{
		LoggerUtil.initLogger("autoLoan_v3", autoTouBiaoLogger);
	}
	
	public static boolean isRun = false ; //当等于true的时候阻塞
	private Map<String, Integer> rankValueMap = new HashMap<String,Integer>();//自动投标用户排名号
	
	@ActionKey("/autoLoan_v3")
	@Before({PkMsgInterceptor.class})
	public void autoLoan(){
		Message msg = succ("02", "其他任务未完成");
		if( isRun == false ){
			isRun = true ;	//阻塞
			msg = autox();
			isRun = false ;	//放行
		}
		renderJson(msg);
	}
	
//	static final long exTime = 60*60*1000 ;
	static long exTime = 2*60*1000 ;	//测试
	static{
		if( AdminConfig.isDevMode == true )
			exTime = 2*60*1000 ;
		else
			exTime = 60*60*1000 ;
	}
	static long lastUpdateTime = System.currentTimeMillis() - exTime - 100 ;
	public void resetRank4All(){
		
		long nowTime = System.currentTimeMillis() ;
		long timeValue = nowTime - lastUpdateTime - exTime ;
		
		if( timeValue >= 0 ){
			List<AutoLoan_v2> autos = AutoLoan_v2.autoLoanDao.find("select * from t_auto_loan_v2 where autoState='A'");
			autoMapService.genAutoMap(autos) ;
			lastUpdateTime = System.currentTimeMillis() ;
		}
//		else{
//			long x = Db.queryLong("select COUNT(DISTINCT userCode) from t_auto_map where autoState = 'A'");
//			long y = Db.queryLong("select COUNT(userCode) from t_auto_loan_v2 where autoState = 'A'");
//			if(x!=y){
//				System.out.println("t_auto_map表与t_auto_loan_v2人数不一致，重新生成");
//				List<AutoLoan_v2> autos = AutoLoan_v2.autoLoanDao.find("select * from t_auto_loan_v2 where autoState='A'");
//				autoMapService.genAutoMap(autos) ;
//				System.out.println("automap重新索引完成...");
//			}
//		}
		
	}
	
	public Message autox(){
		// 密匙验证
		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.autoLoanKey");
		if(!key.equals(preKey)){
			return error("01","密匙错误", null );
		}
		// 检查开标10分钟还未满标的标
		autoTouBiaoLogger.log(Level.INFO,"自动投标开始，检查是否有开标10分钟还未满标的标");
		List<LoanInfo> loanInfos = loanInfoService.findByPage4SubAutoLoan();
		for (int i = 0; i < loanInfos.size(); i++) {
			LoanInfo tmpLoan = loanInfos.get(i);
			String releaseDate = tmpLoan.getStr("releaseDate");
			String releaseTime = tmpLoan.getStr("releaseTime");
			boolean x = CommonUtil.validateMinTime(DateUtil.getNowDateTime(), releaseDate + releaseTime, 10);
			if(x){
				autoTouBiaoLogger.log(Level.INFO,"借款标"+tmpLoan.getStr("loanCode")+"["+tmpLoan.getStr("loanTitle")+"]开标10分钟后未满标，重置自动投标");
				Db.update("update t_loan_info set loanATAmount=loanBalance where loanState='J' and loanATAmount=0 and loanBalance>0 and loanCode = ?",tmpLoan.getStr("loanCode"));
			}
		}
		
		Integer pageNumber_loan = 1;
		boolean isGo = true ;
		Message result = null ;
		while( isGo ){
			
			resetRank4All();
			
			autoTouBiaoLogger.log(Level.INFO,"自动投标开始，查询是否有标可自动投，第"+pageNumber_loan+"页");
			result = gogoX( pageNumber_loan );
			if( result.getReturn_code().equals("00") == false ){
				isGo = false  ;
			}
			pageNumber_loan ++ ;
		}
		return result ;
		
	}
	
	private Message gogoX(int pageNumber_loan){
		Integer pageSize_autoLoan = 100;
		Page<LoanInfo> pageLoanInfo = null;
		List<String > result = new ArrayList<String>() ;
		try{
			pageLoanInfo = queryLoanList( pageNumber_loan );
			if( pageLoanInfo.getList().size() <= 0 ){
				autoTouBiaoLogger.log(Level.INFO,"自动投标,没有新标");
				return error("02", "没有新标!【02】", null ) ;
			}
			LoanInfo loan = pageLoanInfo.getList().get(0);	//获取标信息
			
			if( loan == null ){
				autoTouBiaoLogger.log(Level.INFO,"自动投标,没有新标");
				return error("03", "没有新标【03】", loan ) ;
			}
			//筛选投标人
			List<AutoLoan_v2> autoLoanner = getAutoLoanner(loan, pageSize_autoLoan) ;

//			String releaseDateTime = loan.getStr("releaseDate") + loan.getStr("releaseTime") ;
//			long rdt = DateUtil.getDateFromString(releaseDateTime, "yyyyMMddHHmmss").getTime() ;
//			long ndt = System.currentTimeMillis() ;
//			if( (ndt-rdt) >= 10*60*1000 ){
//				String loanCode = loan.getStr("loanCode");
//				//如果还未满标，则增加自动投标机会
//				Db.update("update t_loan_info set loanATAmount=loanATAmount+loanBalance where loanCode=? and loanBalance > 0" , loanCode  );
//				loan = LoanInfo.loanInfoDao.findById(loanCode) ;
//			}
			autoTouBiaoLogger.log(Level.INFO , "符合条件的投标者共：" + autoLoanner.size() + "人！");
			
			//更新自动排名map
			for (AutoLoan_v2 autoLoan_v2 : autoLoanner) {
				String userCode = autoLoan_v2.getStr("userCode");
				int rankValue = autoMapService.queryRankVal(userCode);//综合排队编号
				int loanTimeLimit = loan.getInt("loanTimeLimit");
				List<Record> list = autoMapService.queryRankDetail((int)rankValue, loanTimeLimit, loanTimeLimit);
				int quantity = (int)(list.size() > 0 ? list.get(0).getLong("quantity"):0);//自动投标排名号
				rankValueMap.put(userCode, quantity);
			}
			
			if( doAutoLoan( 0 , loan , autoLoanner , result) ){
			}
			Db.update("update t_sms_log set `status`=9 where `status`=8 and type='11' and sendDate = ?",DateUtil.getNowDate());
			/*	测试注释
			int sz = autoLoanner.size() ;
			for(int index = 0 ; index < sz ; index ++ ){
				AutoLoan_v2 at2 = autoLoanner.get(index) ;
				String uc = at2.getStr("userCode");
				String un = at2.getStr("userName");
				int aid = at2.getInt("aid") ;
				long rv = autoLoanService.queryRank(uc)[1] ;
				Funds funds = Funds.fundsDao.findById(uc) ;
				logger.info(String.format("当前用户：[%s][%s],AID=[%d],rankValue=[%d],avBalance=[%d]", uc,un,aid,rv,funds.getLong("avBalance")));
			}*/
			
		}catch(Exception e){
			e.printStackTrace(); 
			autoTouBiaoLogger.log(Level.SEVERE,"自动投标处理异常，e:" + e.getMessage() );
			return error("01", "自动投标处理异常，e:" + e.getMessage() , result ) ;
		}
		autoTouBiaoLogger.log(Level.INFO,"自动投标完成!");
		return succ("自动投标完成", result ) ;
	}
	
	private synchronized boolean doAutoLoan( int index , LoanInfo loan , List<AutoLoan_v2> loanners , List<String> result ){
		
		if( loanners.size() == index ){
			//结束,全部处理完成
			return true ;
		}
		AutoLoan_v2 autoLoan = loanners.get(index);	//获取一个自动投标人
		index ++ ;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
		
		String loanCode = loan.getStr("loanCode");
		String loanTitle = loan.getStr("loanTitle");
		//reload
		loan = LoanInfo.loanInfoDao.findById(loanCode);
		long loanBalanceTrue = loan.getLong("loanBalance");//贷款标剩余额度
		
		String payUserCode = autoLoan.getStr("userCode");//投标人userCode
		String payUserName = "";
		try {
			payUserName = User.userDao.findByIdLoadColumns(payUserCode, "userName").getStr("userName");
		} catch (Exception e) {
			payUserName = " ";
		}
		// 20170821 ws   
		User payuser=userService.findById(payUserCode); 
		//提前回款资金异常
//		boolean unusualUserCode = loanTraceService.unusualUserCode(payUserCode);
//		if(unusualUserCode){
//			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：20180528提前回款资金异常,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
//			return doAutoLoan( index , loan , loanners , result );
//		}
		//验证还款类型
		String refundType=autoLoan.getStr("refundType");
		if(!refundType.equals(loan.getStr("refundType"))&&!"D".equals(refundType)){
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：不满足标还款类型,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
			return doAutoLoan( index , loan , loanners , result );
		}
		//验证是否符合投标期限
		int minLimit =  autoLoan.getInt("autoMinLim");
		int maxLimit =  autoLoan.getInt("autoMaxLim");
		int loanTimeLimit = loan.getInt("loanTimeLimit");
		if(loanTimeLimit<minLimit||loanTimeLimit>maxLimit){
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：不满足投标期限,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
			return doAutoLoan( index , loan , loanners , result );
		}
		//验证是否开通江西存管
		String jxAccountId = payuser.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			//投标用户状态未激活存管，继续递归
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：未激活存管,不符合投标条件!关闭该用户的自动投标", payUserCode,payUserName ) );
			autoLoan.set("autoState", "C" );
			autoLoan.set("autoDateTime", DateUtil.getNowDateTime() );
			autoLoan.update() ;
			return doAutoLoan( index , loan , loanners , result );
		}
		//验证是否开通缴费授权
		JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(jxAccountId);
		if(paymentAuthPageState == null || !"1".equals(paymentAuthPageState.get("type"))){
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：缴费授权未开通,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
			return doAutoLoan( index , loan , loanners , result );
		}
		//用户资金验证  WS start
		Funds funds = fundsServiceV2.findById(payUserCode);
//		boolean verifyFund = JXController.checkBalance(funds.getLong("avBalance"), jxAccountId);
//		if (!verifyFund) {
//			//投标用户状态资金异常，继续递归
//			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：资金异常,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
//			return doAutoLoan( index , loan , loanners , result );
//		}
		if (!fundsServiceV2.checkBalance(payuser)) {
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常：资金异常,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
			return doAutoLoan( index , loan , loanners , result );
		}
		//验证自动投标是否签约 ws
		Map<String, String> autoBid = JXQueryController.termsAuthQuery(jxAccountId);
		if(null == autoBid ||"0".equals(autoBid.get("autoBid"))){
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]未签约自动投标,不符合投标条件!跳过该用户", payUserCode,payUserName ) );
			return doAutoLoan( index , loan , loanners , result );
		}
		//query rank's value
		int rankValue = 0;
		if(rankValueMap.containsKey(payUserCode)){
			rankValue = rankValueMap.get(payUserCode);
		}else {
			System.out.println(payUserCode+"["+payUserName+"]rankValue异常，跳过");
			autoTouBiaoLogger.log(Level.INFO,"["+payUserCode+"]"+"["+payUserName+"]用户在t_auto_map表里查不到rankValue，跳过...");
			return doAutoLoan( index , loan , loanners , result );	//进行下一次投标
		}
//		int rankNum = 0;
		/*try {
			//rankValue = Db.queryInt("select rankValue from t_auto_map where userCode=? and autoState='A'", payUserCode ) ;
			rankValue = autoMapService.queryRankVal(payUserCode);//综合排队编号
//			rankNum = autoMapService.queryRankNum(rankValue);
		} catch (Exception e) {
			System.out.println(payUserCode+"["+payUserName+"]rankValue异常，跳过");
			autoTouBiaoLogger.log(Level.INFO,"["+payUserCode+"]"+"["+payUserName+"]用户在t_auto_map表里查不到rankValue，跳过...");
			return doAutoLoan( index , loan , loanners , result );	//进行下一次投标
		}*/
//		if(!payUserCode.equals("dc90cc1f85de4c99ae7fbaa078475f0f")){
//			return doAutoLoan( index , loan , loanners , result );	//进行下一次投标
//		}
		autoTouBiaoLogger.log(Level.INFO , String.format("标[%s][%s],索引:[%d],投标人:[%s][%s],当前排名：[%d]",loanCode,loanTitle,index,payUserCode,payUserName,rankValue));
		
		if(!userService.validateUserState(payUserCode)){
			//投标用户状态非正常，继续递归
			autoTouBiaoLogger.log(Level.INFO , String.format("用户[%s][%s]状态异常,不符合投标条件!关闭该用户的自动投标", payUserCode,payUserName ) );
			autoLoan.set("autoState", "B" );
			autoLoan.set("autoDateTime", DateUtil.getNowDateTime() );
			autoLoan.set("payAmount", 0 ) ;
			autoLoan.update() ;
			return doAutoLoan( index , loan , loanners , result );
		}
		
		long autoMaxAmount = autoLoan.getLong("onceMaxAmount");
//		autoMaxAmount = 99999999 ;//modify by five 2016-020-25，临时解决方案，避免有人恶意设置最大投标金额，使账户余额大于50保留排名
		long autoMinAmount = autoLoan.getLong("onceMinAmount");
		long avBalance = fundsServiceV2.findAvBalanceById(payUserCode);//用户可用余额
		long maxLoanAmount = loan.getInt("maxLoanAmount");	//最大投标金额
		long tmpMaxLoanAmount = 0 ;
		long loanAmount = loan.getLong("loanAmount");
		//10%限制
//		if( loanAmount <= 20000000 ){//20W
//			tmpMaxLoanAmount = 1000000 ;
//		}
//		
//		if( loanAmount >20000000 & loanAmount <= 30000000 ){//20-30W
//			tmpMaxLoanAmount = 2000000 ;
//		}
//		
//		if( loanAmount >30000000 & loanAmount <= 40000000 ){//30-40W
//			tmpMaxLoanAmount = 3000000 ;
//		}
//		
//		if( loanAmount > 40000000 ){//40W
//			tmpMaxLoanAmount = 5000000 ;
//		}
		//小于等于10W，自动投标限额1w，大于10万,自动投标限额2W
		if( loanAmount <= 10000000 ){
			tmpMaxLoanAmount = 1000000 ;
		}else{
			tmpMaxLoanAmount = 2000000 ;
		}
		
		//优先标限额,如果标限额大于自动限额，则按自动限额
		//贝贝这么要求的
		//if( maxLoanAmount > tmpMaxLoanAmount )
		maxLoanAmount = tmpMaxLoanAmount ;
		
		long minLoanAmount = loan.getInt("minLoanAmount");	//最小投标金额
		long autoAmount = 0 ;	//应投标金额
		
		if( avBalance > autoMaxAmount ){
			autoAmount = autoMaxAmount ;
		}else{
			autoAmount = avBalance ;
			if( autoAmount < minLoanAmount ){
				//投标失败，重新置换位置
				autoTouBiaoLogger.log(Level.INFO, String.format("用户[%s][%s][%d分]账户余额小于自动投标最小金额要求", payUserCode,payUserName,autoAmount )  );
				result.add( payUserCode + " 投标失败，原因：可用余额不足!" ) ;
				resetRank(autoLoan);
				return doAutoLoan( index , loan , loanners , result );	//进行下一次投标
				//autoLoanService.updateOnce(payUserCode) ;	没钱保留排名
			}
		}
		
		if( autoAmount > maxLoanAmount ){
			autoAmount = maxLoanAmount ;
		}
		
		if(loanBalanceTrue < autoAmount ){
			autoAmount = loanBalanceTrue ;//伪验证，兼容异常
		}
		//易分期为100元的整数
		long tmpPercent=100;
		if(SysEnum.productType.E.val().equals(loan.getStr("productType"))){
			tmpPercent=10000 ;
		}
			autoAmount = autoAmount - autoAmount%tmpPercent ;//取整
		if( avBalance > 0 && autoAmount > 0 ) {
			//判断标是否还有60%的自动投资资金
			long loanATAmount = loan.getLong("loanATAmount") ;
			autoTouBiaoLogger.log(Level.INFO,"["+loanCode+"]标当前自动投标余额["+loanATAmount/10.00/10.00+"]!!");
			
			if( loanATAmount > 0 ){
				if( autoAmount >= loanATAmount ){
					autoAmount = loanATAmount ;
				}
				
				if( autoAmount < autoMinAmount ){
//					Db.update("update t_loan_info set loanATAmount=loanATAmount-? where loanCode=? and loanATAmount-?>=0 "
//							, autoAmount , loanCode , autoAmount );
					//TODO 当可投金额小于设置的最小投标金额,继续下一轮,此处保留排列了,需要等贝贝确认是否保留
					//resetRank(autoLoan);	注释后占坑，还是坑！
					return doAutoLoan( index , loan , loanners , result ) ;
				}
				
				if( autoAmount > autoMaxAmount )
					autoAmount = autoMaxAmount ;
				
				if( autoAmount < minLoanAmount ){
					Db.update("update t_loan_info set loanATAmount=loanATAmount-? where loanCode=? and loanATAmount-?>=0 "
							, autoAmount , loanCode , autoAmount );
					return true ;	//投标结束
				}
				
				int locked = Db.update("update t_loan_info set loanATAmount=loanATAmount-? where loanCode=? and loanATAmount-?>=0 "
						, autoAmount , loanCode , autoAmount );
				if( locked > 0 ){
					/**
					 * 判断是不是可以用现金券
					 */
					String ticketCodes = "";
					String useTicket = "";
					try {
						useTicket = autoLoan.getStr("useTicket");
					} catch (Exception e) {
						useTicket = "N";
					}
					String priorityMode = "";
					try {
						priorityMode = autoLoan.getStr("priorityMode");
					} catch (Exception e) {
						priorityMode = "N";
					}
					if(useTicket.equals("A")&&loan.getStr("productType").equals(SysEnum.productType.E.val())){
						useTicket="N";
					}
					if(useTicket.equals("N")){//会员等级默认自动加息
						autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户未开启自动投标使用理财券,使用会员等级自动加息");
						Integer vipLevel = payuser.getInt("vipLevel");//获取会员等级
						VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
						int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
						ticketCodes = rewardInterest > 0 ? Tickets.rewardRateInterestTcode:"";
					}else if(useTicket.equals("A")||useTicket.equals("C")){
						autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户开启自动投标使用现金券");
						List<Tickets> tcs = null;
						if(priorityMode.equals("A")){
							if(useTicket.equals("A")){
								autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用现金券-期限优先");
								//按到期时间升序查现金券，使用第一个符合条件的
								tcs = ticketsService.queryATicketsByUserCode1(payUserCode,String.valueOf(loan.getInt("loanTimeLimit")));
							}else if(useTicket.equals("C")){
								autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用加息券-过期时间优先");
								tcs = ticketsService.queryATicketsByUserCodeByRewardRate(payUserCode, String.valueOf(loan.get("loanTimeLimit")),priorityMode);
							}
						}else if(priorityMode.equals("B")){
							autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用现金券-金额优先");
							//按金额降序查询现金券，使用第一个符合条件的
							tcs = ticketsService.queryATicketsByUserCode2(payUserCode, String.valueOf(loan.get("loanTimeLimit")));
						}else if(priorityMode.equals("C")){
							autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用加息券-利率优先");
							tcs = ticketsService.queryATicketsByUserCodeByRewardRate(payUserCode, String.valueOf(loan.get("loanTimeLimit")),priorityMode);
						}else{
							autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用理财券-优先方式异常");
						}
						if(tcs == null || tcs.size() == 0){//无可用理财券自动会员加息
							autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户无可用理财券,使用会员等级自动加息");
							Integer vipLevel = payuser.getInt("vipLevel");//获取会员等级
							VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
							int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
							ticketCodes = rewardInterest > 0 ? Tickets.rewardRateInterestTcode:"";
						}
						try {
							if(tcs!=null){
								for (int i = 0; i < tcs.size(); i++) {
									Tickets tt = tcs.get(i);
									String strUseEx = tt.getStr("useEx");
									JSONObject json = JSONObject.parseObject( strUseEx ) ;
									int limit = json.getIntValue("limit");
									int rate = json.getIntValue("rate");
									long tmp = json.getLongValue("amount") ;
									boolean ok1 = false;boolean ok2 = false;boolean ok3 = false;
									if("A".equals(tt.getStr("ttype"))){
										if(tmp == 0 || autoAmount >= tmp)
											ok1 = true;
									}else if("C".equals(tt.getStr("ttype"))){//加息券是额度内
										//加息券不支持18号以前的标
										int releaseDate = Integer.parseInt(loan.getStr("releaseDate"));
										if(releaseDate>=20171118){
											if(tmp == 0 || autoAmount <= tmp)
												ok1 = true;
										}
									}
									if(rate == 0 || (loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")+loan.getInt("benefits4new")) >= rate )
										ok2= true;
									if(limit == 0 || loan.getInt("loanTimeLimit") >= limit )
										ok3 = true;
									if("A".equals(tt.getStr("ttype"))){
										autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用现金券时：现金券["+tmp+"]使用金额"+ok1 +",使用利率"+ok2 +",使用期限"+ok3+"。券code"+tt.getStr("tCode"));
									}else if("C".equals(tt.getStr("ttype"))){
										autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户自动投标使用加息券时：加息券["+tmp+"]使用金额"+ok1 +",使用利率"+ok2 +",使用期限"+ok3+"。券code"+tt.getStr("tCode"));
									}
									if(ok1 && ok2 && ok3){
										ticketCodes = ticketCodes + tt.getStr("tCode");
										break;
									}
//									if(i < (tcs.size() -1)){
//										ticketCodes = ticketCodes + ",";
//									}
								}
								ticketCodes = "".equals(ticketCodes)?Tickets.rewardRateInterestTcode:ticketCodes;//奖券未使用上,默认会员等级加息
							}
						} catch (Exception e) {
							autoTouBiaoLogger.log(Level.INFO,payUserCode+"["+payUserName+"]用户解析现金券异常");
							ticketCodes = "";
						}
					}else if(useTicket.equals("D")){
						//判断加息额度是否足够
						long rewardRateAmount = funds.getLong("rewardRateAmount") ;
						if(rewardRateAmount<autoAmount){
							ticketCodes = Tickets.rewardRateInterestTcode;
						}else{
						ticketCodes=Tickets.rewardRateAomuntTcode;
						}
					}
					LoanCenterController loanService = new LoanCenterController();
					Message msg = loanService.doLoan4bidding(loanCode, autoAmount , 
							User.userDao.findById(payUserCode),"A",rankValue,ticketCodes , autoMinAmount ) ;
					if( "00".equals(msg.getReturn_code()) == true ){
//						Log.info(String.format("[%s]自动投标成功[%s]，金额[%d]", autoLoan.get("userCode"),loan.getStr("loanTitle"),
//								autoAmount ));
						try {
							String mobile = userService.getMobile(payUserCode);
							String content = CommonUtil.SMS_MSG_AUTOLOAN.replace("[userName]", payUserName)
							.replace("[loanNo]", loan.getStr("loanNo")).replace("[payAmount]", CommonUtil.yunsuan(autoAmount+"", "100", "chu", 2).doubleValue()+"");
							SMSLog smsLog = new SMSLog();
							smsLog.set("mobile", mobile);
							smsLog.set("content", content);
							smsLog.set("userCode", payUserCode);
							smsLog.set("userName", payUserName);
							smsLog.set("type", "11");smsLog.set("typeName", "自动投标");
							smsLog.set("status", 8);
							smsLog.set("sendDate", DateUtil.getNowDate());
							smsLog.set("sendDateTime", DateUtil.getNowDateTime());
							smsLog.set("break", "");
							smsLogService.save(smsLog);
						} catch (Exception e) {
							e.printStackTrace();
						}
						autoTouBiaoLogger.log(Level.INFO,String.format("[%s]自动投标成功[%s]，金额[%d]", autoLoan.get("userCode"),loan.getStr("loanTitle"),
								autoAmount ));
						
						autoLoan.set("payAmount", autoAmount ) ;
						autoLoan.set("userName", payUserName ) ;
						//投标金额大于等于余额不保留排名
						if( (avBalance-autoAmount)< 5000 ){
							resetRank(autoLoan);
						}else{
//							int tmpLoanOnceMaxAmount = loan.getInt("maxLoanAmount");
							if( maxLoanAmount <= autoAmount ){
								//当投标金额大于等于单次最大投标时，重置排名
								resetRank(autoLoan);
							}else{
								if( autoAmount >= autoMinAmount ){
									//当投标金额大于最小投标金额,重置排名
									resetRank(autoLoan);
								}
								autoTouBiaoLogger.log(Level.INFO,"["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,余额大于50元保留排名！");
							}
						}
					}else{
						//回滚
						Db.update("update t_loan_info set loanATAmount=loanATAmount+? where loanCode=? "
								, autoAmount , loanCode  );
						autoTouBiaoLogger.log(Level.INFO,String.format("[%s]自动投标失败[%s]，金额[%d],原因[%s][%s]", autoLoan.get("userCode"),loan.getStr("loanTitle"),
								autoAmount , msg.getReturn_code(),msg.getReturn_info()));
					}
				}
				
				
			}else{
				autoTouBiaoLogger.log(Level.INFO,"["+loanCode+"]标无投资金额!!!");
//				Log.info("标无投资金额!!!");
				return true ;	//结束投标
			}
			
		}
		
		return doAutoLoan( index , loan , loanners , result ) ;
	}
	
	private void resetRank(AutoLoan_v2 autoLoan ){
		autoLoan.set("autoState", "B" );
		autoLoan.set("autoDateTime", DateUtil.getNowDateTime() );
		autoLoan.delete();
		//排队排后
//		Log.info("["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,更新排名！");
		autoTouBiaoLogger.log(Level.INFO,"["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "更新排名！");
		autoLoan.remove("aid");
		autoLoan.remove("payAmount");
		autoLoan.set("autoState","A");
		autoLoan.save() ;
		
		//更新Map
		autoMapService.resetRankVal_ADV(autoLoan);
	}
	
	private List<AutoLoan_v2> getAutoLoanner(LoanInfo loan , Integer pageSize_autoLoan ){

//		Integer minLoanAmount = loan.getInt("minLoanAmount") ;	//最小投标金额
//		Integer maxLoanAmount = loan.getInt("maxLoanAmount") ;	//最大投标金额
		String refundType = loan.getStr("refundType" ) ;
		Integer loanTimeLimit = loan.getInt("loanTimeLimit") ;
//		String productType = loan.getStr("productType");
		
		/*
		 * 	单次最小投标必须大于标的最大投标金额
		 * 	单次最大投标金额必须大于标的最小投标金额
		 * 	
		 * 
		
		List<AutoLoan_v2> autoLoans = AutoLoan_v2.autoLoanDao.find("select a.* from t_auto_loan_v2 a,t_funds f where "
				+ "a.autoState='A' and a.onceMinAmount<=? and a.onceMinAmount>=? and a.autoMinLim<=? and a.autoMaxLim>=? "
				+ "and (a.refundType=? or a.refundType='D') and a.userCode=f.userCode and f.avBalance>=a.onceMinAmount "
				+ " and LOCATE(?,productType) > 0 "
				+ " order by a.aid asc limit 0,?"  , maxLoanAmount ,minLoanAmount, loanTimeLimit , loanTimeLimit , refundType ,productType, pageSize_autoLoan) ;
		*/ 
		
		List<AutoLoan_v2> autoLoans = AutoLoan_v2.autoLoanDao.find("SELECT * FROM t_auto_loan_v2 t1 left JOIN t_user t2 on t1.userCode=t2.userCode WHERE "
				+ "t1.autoState='A' AND (t1.refundType=? or t1.refundType='D') "
				+ "and t1.autoMinLim<=? and t1.autoMaxLim>=? and (t2.jxAccountId!=null or t2.jxAccountId!=?) and t1.deadLine>?",refundType , loanTimeLimit , loanTimeLimit,"",DateUtil.getNowDate());
		
		return autoLoans ;
		/*
		return autoLoanService.findByPage( 1 , pageSize_autoLoan , 
				minLoanAmount, maxLoanAmount, refundType, loanTimeLimit, rateValue) ;
				*/
	}
	
	/**
	 * 	分页获取现在可投的标，一次一条
	 * @param pageNumber_loan
	 * @param pageSize_loan
	 * @return
	 */
	private Page<LoanInfo> queryLoanList(Integer pageNumber_loan  ){
		return loanInfoService.findByPage4AutoLoan( pageNumber_loan , 1 );
	}
	
	@ActionKey("/queryAutoLoanDetails")
	public void queryAutoLoanDetails(){
		String pi = getPara("pi","");
		if(pi.equals("3.1415926535898")  == false){
			renderHtml("......");
		}else{
			List<AutoLoan_v2> autoLoans = AutoLoan_v2.autoLoanDao.find("select u.userName as un ,a.*,f.avBalance as ab from t_auto_loan_v2 a,t_user u,t_funds f where a.userCode=u.userCode and a.userCode=f.userCode and a.autoState='A' order by a.aid asc");
			int rank = 1 ;
			StringBuffer buff = new StringBuffer("");
			buff.append("<table>");
			buff.append("<tr><td>排名[序号]</td><td>用户名</td><td>用户编号</td><td>金额区间</td><td>期限区间</td><td>还款类型</td><td>最后设置时间</td><td>账户余额（元）</td></tr>");
			buff.append("<tr><td colspan = '8'>--------------------------------------------------------------------------------------------------------------------------------------------------</td></tr>");
			for(AutoLoan_v2 auto : autoLoans ){
				buff.append("<tr>");
				buff.append(String.format("<td>[%d][%d]</td>", rank , auto.getInt("aid")));
				buff.append(String.format("<td>%s</td>", auto.getStr("un")));
				buff.append(String.format("<td>%s</td>", auto.getStr("userCode")));
				buff.append(String.format("<td>%d-%d分</td>", auto.getLong("onceMinAmount") , auto.getLong("onceMaxAmount") ));
				buff.append(String.format("<td>%d-%d月</td>", auto.getInt("autoMinLim") , auto.getInt("autoMaxLim") ));
				buff.append(String.format("<td>%s</td>", auto.getStr("refundType").equals("A") ? "等额本息": "先息后本" ));
				buff.append(String.format("<td>%s</td>", auto.getStr("autoDateTime") ));
				buff.append(String.format("<td>%f</td>", auto.getLong("ab")/10.0/10.0 ));
				buff.append("</tr>");
				buff.append("<tr><td colspan='8'>");
				if(auto.getStr("useTicket").equals("A")){
					buff.append("使用现金券");
					if(auto.getStr("priorityMode").equals("A")){
						buff.append("，过期时间优先");
					}else if(auto.getStr("priorityMode").equals("B")){
						buff.append("，抵扣金额优先");
					}
				}else if(auto.getStr("useTicket").equals("C")){
					buff.append("使用加息券");
					if(auto.getStr("priorityMode").equals("A")){
						buff.append("，过期时间优先");
					}else if(auto.getStr("priorityMode").equals("C")){
						buff.append("，利率优先");
					}
				}else{
					buff.append("不使用理财券");
				}
				buff.append("</td></tr>");
				int rankVal = autoMapService.queryRankVal(auto.getStr("userCode"));
				if(rankVal > 0){
					List<Record> list = autoMapService.queryRankDetail(rankVal,auto.getInt("autoMinLim"),auto.getInt("autoMaxLim"));
					for (int i = 0; i < list.size(); i++) {
						Record rc = list.get(i);
						buff.append("<tr><td>").append(rc.getInt("loanLimit")).append("月标</td><td>排队人数：</td>");
						buff.append("<td colspan='6'>").append(rc.getLong("quantity")).append("</td></tr>");
					}
				}else{
					buff.append("<tr><td colspan = '8'>------------------------------------------------------------------自动投标排名详情刷新中......--------------------------------------------------------------</td></tr>");
				}
				buff.append("<tr><td colspan = '8'>--------------------------------------------------------------------------------------------------------------------------------------------------</td></tr>");
				rank ++ ;
			}
			buff.append("</table>");
			renderHtml( buff.toString() );
		}
	}
}
