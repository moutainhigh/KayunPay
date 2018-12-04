package com.dutiantech.controller.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.portal.LoanCenterController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class AutoLoanController extends BaseController {
	
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private UserService userService = getService(UserService.class);
	
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	
	private SMSLogService smsLogService = getService(SMSLogService.class);
	
	
	private static final Logger autoTouBiaoLogger = Logger.getLogger("autoTouBiaoLogger");
	
	static{
		LoggerUtil.initLogger("autoLoan", autoTouBiaoLogger);
	}
	
	public static boolean isRun = false ; //当等于true的时候阻塞
	
//	@ActionKey("/autoLoan")
	@Before({Tx.class,PkMsgInterceptor.class})
	public void autoLoan(){
		Message msg = succ("02", "其他任务未完成");
		if( isRun == false ){
			isRun = true ;	//阻塞
			msg = autox();
			isRun = false ;	//放行
		}
		renderJson(msg);
	}
	
	//@ActionKey("/autoLoan2")
	@Before({Tx.class,PkMsgInterceptor.class})
	public void autoLoan2(){
		Message msg = succ("02", "其他任务未完成");
		
		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.autoLoanKey");
		if(!key.equals(preKey)){
			msg=  error("01","密匙错误", null );
		}else{
			boolean isGo = true ;
			int pageNum = 1 ;
			while( isGo ){
				Date d = new Date(System.currentTimeMillis()+60*1000);
				LoanInfo loan = LoanInfo.loanInfoDao.findFirst("select * from t_loan_info where loanState = ?"
						+ " and loanBalance > 0 and benefits4new <= 0 and loanATAmount!=loanBalance"
						+ "and (releaseDate+releaseTime)<=? order by releaseDate asc,releaseTime asc limit ?,1", SysEnum.loanState.J.val()
						,DateUtil.parseDateTime(d, "yyyyMMddHHmmss") ,pageNum);	//获取标信息
				if( loan != null ){
					String releaseDateTime = loan.getStr("releaseDate") + loan.getStr("releaseTime") ;
					long rdt = DateUtil.getDateFromString(releaseDateTime, "yyyyMMddHHmmss").getTime() ;
					long ndt = System.currentTimeMillis() ;
					if( (ndt-rdt) >= 10*60*1000 ){
						String loanCode = loan.getStr("loanCode");
						//如果还未满标，则增加自动投标机会
						Db.update("update t_loan_info set loanATAmount=loanATAmount+loanBalance where loanCode=? and loanBalance > 0" , loanCode  );
					}
				}else{
					isGo = false ;
				}
				pageNum ++ ;
			}
		}
		renderJson(msg);
	}
	
	private Message autox(){

//		String nowTime = DateUtil.getNowTime();
//		int xx = DateUtil.compareDateByStr("HHmmss", nowTime, "110000");
//		int yy = DateUtil.compareDateByStr("HHmmss", nowTime, "103000");
//		if(xx == -1 && yy == 1){
//			return error("02","还款期间停止自动投标", null );
//		}
		String key = getPara("key", "");
		String preKey = (String) CACHED.get("S1.autoLoanKey");
		if(!key.equals(preKey)){
			return error("01","密匙错误", null );
		}
		//TODO 处理重复的自动投标配置数据
		try {
			autoTouBiaoLogger.log(Level.INFO,"自动投标开始，检查是否有重复的自动投标配置项");
			List<String> list = Db.query("select userCode from t_auto_loan_v2 where autoState ='A' GROUP BY userCode having COUNT(autoState) > 1");
			if(list!=null){
				for (int i = 0; i < list.size(); i++) {
					String userCode = list.get(i);
					autoTouBiaoLogger.log(Level.INFO,"用户["+userCode+"]自动投标配置数据重复,开始处理...");
					Integer lastAID = Db.queryInt("select aid from t_auto_loan_v2 where autoState = 'A' and userCode = ? order by aid desc limit 0,1",userCode);
					if(lastAID!=null){
						Db.update("update t_auto_loan_v2 set autoState = 'B' where userCode = ? and autoState = 'A' and aid != ?",userCode,lastAID.intValue());
						autoTouBiaoLogger.log(Level.INFO,"用户["+userCode+"]自动投标配置数据重复,处理完成...");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//TODO 开标10分钟后,重置开启自动投标
		autoTouBiaoLogger.log(Level.INFO,"自动投标开始，检查是否有开标10分钟还未满标的标");
		List<LoanInfo> loanInfos = loanInfoService.findByPage4SubAutoLoan();
		for (int i = 0; i < loanInfos.size(); i++) {
			LoanInfo tmpLoan = loanInfos.get(i);
			String releaseDate = tmpLoan.getStr("releaseDate");
			String releaseTime = tmpLoan.getStr("releaseTime");
			boolean x = CommonUtil.validateMinTime(DateUtil.getNowDateTime(), releaseDate + releaseTime);
			if(x){
				autoTouBiaoLogger.log(Level.INFO,"借款标"+tmpLoan.getStr("loanCode")+"["+tmpLoan.getStr("loanTitle")+"]开标10分钟后未满标，重置自动投标");
				Db.update("update t_loan_info set loanATAmount=loanBalance where loanState='J' and loanATAmount=0 and loanBalance>0 and loanCode = ?",tmpLoan.getStr("loanCode"));
			}
		}

		Integer pageNumber_loan = 1;
		boolean isGo = true ;
		Message result = null ;
		while( isGo ){
			autoTouBiaoLogger.log(Level.INFO,"自动投标开始，查询是否有标可自动投，第"+pageNumber_loan+"页");
//			Log.info("自动投标开始检查第" + pageNumber_loan + "页");
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
			
			lastRankValue = 0 ;
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
			autoTouBiaoLogger.log(Level.SEVERE,"自动投标处理异常，e:" + e.getLocalizedMessage() );
			return error("01", "自动投标处理异常，e:" + e.getLocalizedMessage() , result ) ;
		}
		autoTouBiaoLogger.log(Level.INFO,"自动投标完成!");
		return succ("自动投标完成", result ) ;
	}
	
	private long lastRankValue = 0 ;
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
		long rankValue = autoLoanService.queryRank(payUserCode)[1];
		if( rankValue <= lastRankValue ){
			autoTouBiaoLogger.log(Level.INFO , String.format("自动投标排名兼容[%d]->[%d]", rankValue , lastRankValue + 1 ));
			rankValue = lastRankValue + 1 ;
		}
		
		autoTouBiaoLogger.log(Level.INFO , String.format("标[%s][%s],索引:[%d],投标人:[%s][%s],当前排名：[%d]", 
				loanCode,loanTitle,index,
				payUserCode,payUserName,rankValue));
//		int axc = loan.getInt("benefits4new");
//		if(axc>0){//如果是新手标
//			long userScore = userService.findByField(payUserCode, "userScore").getLong("userScore");
//			if(userScore>60){//不是新手
//				doAutoLoan( index , loan , loanners , result );//继续递归
//			}
//		}
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
		autoMaxAmount = 99999999 ;//modify by five 2016-020-25，临时解决方案，避免有人恶意设置最大投标金额，使账户余额大于50保留排名
		long autoMinAmount = autoLoan.getLong("onceMinAmount");
		long avBalance = fundsServiceV2.findAvBalanceById(payUserCode);//用户可用余额
		long maxLoanAmount = loan.getInt("maxLoanAmount");	//最大投标金额
		long minLoanAmount = loan.getInt("minLoanAmount");	//最小投标金额
		long autoAmount = 0 ;	//应投标金额
		
		/**
		 * 	1、如果余额大于等于标允许最大单笔金额，可投余额则取最大投标金额
		 * 	2、如果余额小于标允许最大单笔金额，可投余额则取余额。
		 * 		2.1、如果可用余额小于单笔最小投资金额，则返回，账户余额不足
		 */
		if( avBalance >= autoMaxAmount ){
			autoAmount = autoMaxAmount ;
			if( autoAmount > maxLoanAmount)
				autoAmount = maxLoanAmount ;
		}else{
			autoAmount = avBalance ;
			if( autoAmount < minLoanAmount ){
				//投标失败，重新置换位置
				autoTouBiaoLogger.log(Level.INFO, String.format("用户[%s][%s][%d分]账户余额小于自动投标最小金额要求", payUserCode,payUserName,autoAmount )  );
				result.add( payUserCode + " 投标失败，原因：可用余额不足!" ) ;
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

		if( autoMinAmount <= autoAmount ){//保留最低投标金额，保留排名
			
			autoAmount = autoAmount - autoAmount%100 ;//取整
			if( avBalance > 0 && autoAmount > 0 ) {
				
				//判断标是否还有60%的自动投资资金
				long loanATAmount = loan.getLong("loanATAmount") ;
				autoTouBiaoLogger.log(Level.INFO,"["+loanCode+"]标当前自动投标余额["+loanATAmount/10.00/10.00+"]!!");
				if( loanATAmount > 0 ){
					if( autoAmount >= loanATAmount ){
						autoAmount = loanATAmount ;
					}
					
					if( autoAmount < minLoanAmount ){
						Db.update("update t_loan_info set loanATAmount=loanATAmount-? where loanCode=? and loanATAmount-?>=0 "
								, autoAmount , loanCode , autoAmount );
						return true ;	//投标结束
					}
					
					int locked = Db.update("update t_loan_info set loanATAmount=loanATAmount-? where loanCode=? and loanATAmount-?>=0 "
							, autoAmount , loanCode , autoAmount );
					if( locked > 0 ){
						LoanCenterController loanService = new LoanCenterController();
						Message msg = loanService.doLoan4bidding(loanCode, autoAmount , 
								User.userDao.findById(payUserCode),"A",rankValue,"",0) ;
						if( "00".equals(msg.getReturn_code()) == true ){
//							Log.info(String.format("[%s]自动投标成功[%s]，金额[%d]", autoLoan.get("userCode"),loan.getStr("loanTitle"),
//									autoAmount ));
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
							lastRankValue = rankValue ;
							
							autoLoan.set("payAmount", autoAmount ) ;
							autoLoan.set("userName", payUserName ) ;
							//投标金额大于等于余额不保留排名
							if( (avBalance-autoAmount)< 5000 ){
//								autoLoan.set("autoState", "B" );
//								autoLoan.set("autoDateTime", DateUtil.getNowDateTime() );
//								autoLoan.set("payAmount", autoAmount ) ;
//								autoLoan.update() ;
//								//排队排后
//								autoLoan.set("userName", payUserName ) ;
////								Log.info("["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,更新排名！");
//								autoTouBiaoLogger.log(Level.INFO,"["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,更新排名！");
//								autoLoan.remove("aid");
//								autoLoan.remove("payAmount");
//								autoLoan.set("autoState","A");
//								autoLoan.save() ;
								updateAutoRank(autoLoan);
								
								//更新排名
								//autoLoanService.updateOnce(payUserCode) ;
							}else{
								int tmpLoanOnceMaxAmount = loan.getInt("maxLoanAmount");
								if( tmpLoanOnceMaxAmount <= autoAmount ){
									//当投标金额大于等于单次最大投标时，重置排名
									updateAutoRank(autoLoan);
								}
								autoTouBiaoLogger.log(Level.INFO,"["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,余额大于50元保留排名！");
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
//					Log.info("标无投资金额!!!");
					return true ;	//结束投标
				}
				
				//可用资金转冻结资金
	//			boolean x2 = fundsServiceV2.update4prepareBid(payUserCode, (int)autoAmount, 0, "自动投标-预投标冻结可用余额");
	//			if( x2 ){
	//				//投标
	//				if( loanInfoService.update4prepareBid( loan, (int)autoAmount,payUserCode, "A" )){
	////					if( loanBalance == loanAmount ){	//满标
	////						loanInfoService.updateLoanByFull(loanCode);//满标进入满标待审
	////					}
	//					result.add( payUserCode + "，投标完成!当前标余额：" + (loanBalanceTrue - autoAmount) );
	//				}else{
	//					//回滚用户投标资金，但因事务已做完整性保证，不考虑该问题
	//					result.add( payUserCode + "用户资金衣扣，但投标失败!原因不明！" );
	//				}
	//				autoLoanService.updateOnce(payUserCode) ;
	//			}
			}

		}else{
			autoTouBiaoLogger.log(Level.INFO, String.format("用户[%s][%s]账户余额小于自动投标最小投标金额要求,保留排名,继续下一次任务", 
					payUserCode,payUserName )  );
		}
		//递归
		return doAutoLoan( index , loan , loanners , result );
		
	}
	
	private void updateAutoRank(AutoLoan_v2 autoLoan){
		autoLoan.set("autoState", "B" );
		autoLoan.set("autoDateTime", DateUtil.getNowDateTime() );
		autoLoan.update() ;
		//排队排后
//		Log.info("["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,更新排名！");
		autoTouBiaoLogger.log(Level.INFO,"["+ autoLoan.getInt("aid") +"][" + autoLoan.getStr("userName") + "]投标成功,更新排名！");
		autoLoan.remove("aid");
		autoLoan.remove("payAmount");
		autoLoan.set("autoState","A");
		autoLoan.save() ;
	}
	
	
	
	/**
	 * 根据标书条件，获取自动投标人
	 * @param loan
	 * @param pageSize_autoLoan
	 * @return
	 */
	private List<AutoLoan_v2> getAutoLoanner(LoanInfo loan , Integer pageSize_autoLoan ){

		Integer minLoanAmount = loan.getInt("minLoanAmount") ;	//最小投标金额
		Integer maxLoanAmount = loan.getInt("maxLoanAmount") ;	//最大投标金额
		String refundType = loan.getStr("refundType" ) ;
		Integer loanTimeLimit = loan.getInt("loanTimeLimit") ;
		String productType = loan.getStr("productType");
		
		/*
		 * 	单次最小投标必须大于标的最大投标金额
		 * 	单次最大投标金额必须大于标的最小投标金额
		 * 	
		 * 
		 */
		List<AutoLoan_v2> autoLoans = AutoLoan_v2.autoLoanDao.find("select a.* from t_auto_loan_v2 a,t_funds f where "
				+ "a.autoState='A' and a.onceMinAmount<=? and a.onceMinAmount>=? and a.autoMinLim<=? and a.autoMaxLim>=? "
				+ "and (a.refundType=? or a.refundType='D') and a.userCode=f.userCode and f.avBalance>=a.onceMinAmount "
				+ " and LOCATE(?,productType) > 0 "
				+ " order by a.aid asc limit 0,?"  , maxLoanAmount ,minLoanAmount, loanTimeLimit , loanTimeLimit , refundType ,productType, pageSize_autoLoan) ;
		
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
	
	@ActionKey("/queryAutoLoanInfos")
	public void queryAutoLoanInfos(){
		List<AutoLoan_v2> autoLoans = AutoLoan_v2.autoLoanDao.find("select u.userName as un ,a.*,f.avBalance as ab from t_auto_loan_v2 a,t_user u,t_funds f where a.userCode=u.userCode and a.userCode=f.userCode and a.autoState='A' order by a.aid asc");
		int rank = 1 ;
		StringBuffer buff = new StringBuffer("");
		buff.append("<table>");
		buff.append("<tr><td>排名[序号]</td><td>用户名</td><td>用户编号</td><td>金额区间</td><td>期限区间</td><td>还款类型</td><td>产品类型</td><td>最后设置时间</td><td>账户余额（元）</td></tr>");
		for(AutoLoan_v2 auto : autoLoans ){
			buff.append("<tr>");
			buff.append(String.format("<td>[%d][%d]</td>", rank , auto.getInt("aid")));
			buff.append(String.format("<td>%s</td>", auto.getStr("un")));
			buff.append(String.format("<td>%s</td>", auto.getStr("userCode")));
			buff.append(String.format("<td>%d-%d分</td>", auto.getLong("onceMinAmount") , auto.getLong("onceMaxAmount") ));
			buff.append(String.format("<td>%d-%d月</td>", auto.getInt("autoMinLim") , auto.getInt("autoMaxLim") ));
			buff.append(String.format("<td>%s</td>", auto.getStr("refundType") ));
			buff.append(String.format("<td>%s</td>", auto.getStr("productType") ));
			buff.append(String.format("<td>%s</td>", auto.getStr("autoDateTime") ));
			buff.append(String.format("<td>%f</td>", auto.getLong("ab")/10.0/10.0 ));
			buff.append("</tr>");
			/*buff.append( String.format("排名：[%d],用户名：[%s],序号：[%d],投标金额:[%d-%d分]"
					+ ",投标期限:[%d-%d月],投标类型:[%s],最后设置时间:[%s],产品类型：[%s]</br >", 
					rank , auto.getStr("userName"),auto.getInt("aid"),auto.getLong("onceMinAmount"),
					auto.getLong("onceMaxAmount"),auto.getInt("autoMinLim"),auto.getInt("autoMaxLim"),
					auto.getStr("refundType"),auto.getStr("autoDateTime"),auto.getStr("productType") ));*/
			rank ++ ;
		}
		buff.append("</table>");
		renderHtml( buff.toString() );
		
	}
	
}
