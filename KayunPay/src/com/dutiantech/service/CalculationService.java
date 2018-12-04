package com.dutiantech.service;

import java.math.BigDecimal;
import java.util.List;

import com.dutiantech.model.Share;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

public class CalculationService extends Thread {

	@Override
	public void run() {
		setup1();//更新用户待收、已收、待还、已还、资金、及理财人待回收笔数
		setup2();//更新借款人借款总额、待还笔数、已还笔数
		setup3();//更新用户资金账户利息管理费、充值总计、提现总计
		setup4();//同步用户推荐信息
//		try {
//			sleep(60 * 1000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		setup5();//更新投标流水的下一次回收本金 回收利息   同步数据时导入
	}
	
	/**
	 * 更新资金账户的待收、已收、待还、已还
	 */
	@SuppressWarnings({ "unchecked", "unused" })
	public void setup1(){
		int startIndex = 0 ;
		int size = 50 ;
		boolean isGo = true ;
		long total = Db.queryLong("select count(tid) from tmp_tender");
		int doCount = 1 ;
		while( isGo ){
			List<Object[]> tenders = getTenderList(startIndex, size) ;
			if( doCount <= total ){
				for(Object[] tender : tenders){
					System.err.println("[更新资金账户:待收、已收、待还、已还]当前进度第" + (doCount)+"/"+ total + "条投标流水" );
					String traceCode = (String) tender[0];//投标流水
					String loanCode = (String) tender[1];//贷款标编码
					String payUserCode = (String) tender[2];//投标用户编码
					long return_money = (long) tender[3];//已还本金
					long remain_money = (long) tender[4];//剩余本金
					long repay_amount = (long) tender[5];//应还总额
					long return_amount = (long) tender[6];//已还总额
					long remain_amount = (long) tender[7];//剩余总额
					long loan_money = (long) tender[8];//贷款总金额
					int num = (int) tender[9];//还款期数
					int repay_num = (int) tender[10];//已还期数
					String tstatus = (String) tender[11];//状态
					if(!tstatus.equals("21")){//债权转让复制的流水，不做借款标的统计
						String effectDate = Db.queryStr("select effectDate from t_loan_info where loanCode = ?",loanCode);
						if(!StringUtil.isBlank(effectDate)){
							String nextDate = CommonUtil.anyRepaymentDate4string(effectDate,repay_num+1 );
							if(repay_num==num){
								nextDate = "00000000";
							}
							Db.update("update t_loan_info set reciedCount = ?,backDate = ? where loanCode = ?",repay_num,nextDate,loanCode);
							Db.update("update t_loan_trace set loanRecyDate = ?,loanRecyCount=? where loanCode = ?",nextDate,num - repay_num,loanCode);
						}
					}
					//------------更新理财人相关  begin----------
					int beRecyCount = num - repay_num;//待回收笔数
					long beRecyPrincipal = remain_money;//待回收本金
					long beRecyInterest = remain_amount - remain_money;//待回收利息
					long reciedPrincipal = return_money;//已回收本金
					long reciedInterest = return_amount - return_money;//已回收利息
//					if(!tstatus.equals("21")){
						Db.update("update t_funds set beRecyCount = beRecyCount + ?,beRecyPrincipal=beRecyPrincipal+?,"
								+ "beRecyInterest=beRecyInterest+?,reciedPrincipal = reciedPrincipal + ?,"
								+ "reciedInterest = reciedInterest + ? where userCode = ?",beRecyCount,beRecyPrincipal,beRecyInterest,reciedPrincipal,reciedInterest,payUserCode);
//					}
					//------------更新理财人相关  end  ----------
					//------------更新借款人相关  begin----------
					String loanUserCode = Db.queryFirst("select userCode from t_loan_info where loanCode = ?",loanCode);
					if(!tstatus.equals("21")){////债权转让复制的流水，不做借款人的统计
						long beRecyPrincipal4loan =remain_money;
						long beRecyInterest4loan = remain_amount - remain_money ;
						Db.update("update t_funds set beRecyPrincipal4loan = beRecyPrincipal4loan + ?,"
								+ " beRecyInterest4loan = beRecyInterest4loan + ? where userCode = ?",beRecyPrincipal4loan,beRecyInterest4loan,loanUserCode);
						//------------更新借款人相关  end  ----------
					}
					System.out.println("[更新资金账户:待收、已收、待还、已还]处理,投标人["+payUserCode+"],借款人["+loanUserCode+"],投标流水["+traceCode+"]");
					doCount ++ ;
				}
			}else{
				isGo = false ;
			}
			startIndex ++ ;
		}
	}
	
	/**
	 * 更新借款人贷款总额、待还笔数、冗余最后一次投标时间
	 */
	@SuppressWarnings("unchecked")
	public void setup2(){
		int startIndex = 0 ;
		int size = 50 ;
		boolean isGo = true ;
		long total = Db.queryLong("select count(uid) from t_loan_info where loanState in ('N','O','P','Q','R')");
		int doCount = 1 ;
		while( isGo ){
			List<Object[]> loanInfos = getLoanInfoList(startIndex, size) ;
			if( doCount <= total ){
				for(Object[] loanInfo : loanInfos){
					System.err.println("[更新资金账户:借款人借款总额、待还笔数、已还笔数、冗余最后一次投标时间]当前进度第" + (doCount)+"/"+ total + "条借款标" );
					long loanAmount = (long) loanInfo[0];
					int loanTimeLimit = (int) loanInfo[1];
					int reciedCount = (int) loanInfo[2];
					String loanCode = (String) loanInfo[3];
					String loanUserCode = (String) loanInfo[4];
					long loanTotal = loanAmount;//贷款总额
					int loanCount = loanTimeLimit-reciedCount;//正在还款笔数，待还笔数
					int loanSuccessCount = reciedCount;//已还笔数
//					long tmp = Db.queryLong("select count(uid) from t_loan_trace where loanCode = ?",loanCode);
//					System.out.println(tmp);
//					if(tmp>0){
//						String loanRecyDate = LoanTrace.loanTraceDao.findFirst("select loanRecyDate from t_loan_trace where loanCode = ?",loanCode).getStr("loanRecyDate");
//						Db.update("update t_loan_info set backDate = ? where loanCode = ?",loanRecyDate,loanCode);
//					}
					Record rc = Db.findFirst("select loanDateTime from t_loan_trace where loanCode = ? order by uid desc",loanCode);
					if(rc!=null){
						String lastPayLoanDateTime = rc.getStr("loanDateTime");
						Db.update("update t_loan_info set lastPayLoanDateTime = ? where loanCode = ?",lastPayLoanDateTime,loanCode);
					}
					Db.update("update t_funds set loanTotal = loanTotal + ?, loanCount = loanCount + ?,"
							+ " loanSuccessCount = loanSuccessCount + ? where userCode = ? ",loanTotal,loanCount,loanSuccessCount,loanUserCode);
					System.out.println("[更新资金账户:借款人借款总额、待还笔数、已还笔数、冗余最后一次投标时间]处理,借款人["+loanUserCode+"],贷款标编码["+loanCode+"]");
					
					doCount ++ ;
				}
			}else{
				isGo = false ;
			}
			startIndex ++ ;
		}
	}
	
	/**
	 * 统计利息管理费信息、充值总额、提现总额
	 */
	@SuppressWarnings("unchecked")
	public void setup3(){
		int startIndex = 0 ;
		int size = 50 ;
		boolean isGo = true ;
		long total = Db.queryLong("select count(uid) from t_user");
		int doCount = 1 ;
		while( isGo ){
			List<Object[]> users = getTaskUsers(startIndex, size) ;
			if( doCount <= total ){
				for(Object[] user : users){
					System.err.println("[更新资金账户：利息管理费、充值总计、提现总计]当前进度第" + (doCount)+"/"+ total + "个用户" );
					String userCode = user[0]+"";
					String userName = user[1]+"" ;
					String querySql1 = "select sum(traceAmount) from t_funds_trace where traceSysType=17 and userCode=?";
					BigDecimal tmp1 = Db.queryBigDecimal(querySql1 , userCode);
					if(tmp1!=null){
						String updateSql1 = "update t_funds set recyMFee4loan = ? where userCode = ?";
						Db.update(updateSql1,tmp1.longValue(),userCode);
					}
					String querySql2 = "select sum(traceAmount) from t_recharge_trace where bankState='B' and traceState='B' and userCode=?";
					BigDecimal tmp2 = Db.queryBigDecimal(querySql2 , userCode);
					if(tmp2!=null){
						String updateSql2 = "update t_funds set totalRecharge = ? where userCode = ?";
						Db.update(updateSql2,tmp2.longValue(),userCode);
					}
					String querySql3 = "select sum(withdrawAmount) from t_withdraw_trace where status='3' and userCode=?";
					BigDecimal tmp3 = Db.queryBigDecimal(querySql3 , userCode);
					if(tmp3!=null){
						String updateSql3 = "update t_funds set totalWithdraw = ? where userCode = ?";
						Db.update(updateSql3,tmp3.longValue(),userCode);
					}
					System.out.println("[更新资金账户：利息管理费、充值总计、提现总计]处理["+userCode+"][" + userName +"]");
					doCount ++ ;
				}
			}else{
				isGo = false ;
			}
			startIndex ++ ;
		}
	}
	
	/**
	 * 同步推荐有奖用户信息
	 */
	@SuppressWarnings({ "unchecked" })
	public void setup4(){
		int startIndex = 0 ;
		int size = 50 ;
		boolean isGo = true ;
		long total = Db.queryLong("select count(uid) from tmp_user");
		int doCount = 1 ;
		while( isGo ){
			List<Object[]> users = getTmpUser(startIndex, size) ;
			if( doCount <= total ){
				for(Object[] user : users){
					System.err.println("[同步推荐有奖用户信息]当前进度第" + (doCount)+"/"+ total + "条推荐有奖信息" );
					String userCode = user[0]+"";//被推荐人userCode
					String umobile = user[1]+"" ;//被推荐人手机
					String tj_userCode = user[2]+"";//推荐人userCode
					String regDateTime = user[3]+"";//注册日期时间
					String regDate = user[4]+"";//注册日期
//					String tj_userName = Db.queryStr("select userName from t_user where userCode = ?",tj_userCode);
					String userName = Db.queryStr("select userName from t_user where userCode = ?",userCode);
					if(!StringUtil.isBlank(userName)){
						Share share = new Share();
						share.set("userCode", tj_userCode);
						share.set("userName", userName);
						share.set("friendMobile", umobile);
						share.set("amount", 0);
						share.set("regDateTime", regDateTime);
						share.set("regDate", regDate);
						share.set("comeFrom", "");
						share.set("remark", "");
						share.save();
						System.out.println("[同步推荐有奖用户信息]处理,被推荐人["+tj_userCode+"][" + userName +"]");
					}
					doCount ++ ;
				}
			}else{
				isGo = false ;
			}
			startIndex ++ ;
		}
	}
	
//	/**
//	 * 更新债权转让记录的关联投标流水ID
//	 */
//	@SuppressWarnings("unchecked")
//	public void setup4(){
//		int startIndex = 0 ;
//		int size = 50 ;
//		boolean isGo = true ;
//		long total = Db.queryLong("select count(uid) from t_loan_transfer where transState='B'");
//		int doCount = 1 ;
//		while( isGo ){
//			List<Object[]> loanTransfers = getLoanTransferList(startIndex, size) ;
//			if( doCount <= total ){
//				for(Object[] loanTransfer : loanTransfers){
//					System.err.println("[更新债权转让记录的关联投标流水ID]当前进度第" + (doCount)+"/"+ total + "条债权转让记录" );
//					String transCode = (String) loanTransfer[0];
//					String traceCode = (String) loanTransfer[1];
//					String loanCode = (String) loanTransfer[2];
//					String payUserCode = (String) loanTransfer[3];
//					String gotUserCode = (String) loanTransfer[4];
//					Long repay_amount =  Db.queryLong("select repay_amount from tmp_tender where uid=? and lid = ? and tstatus='21'",payUserCode,loanCode);
//					if(repay_amount!=null){
//						String tid = Db.queryStr("select tid from tmp_tender where uid = ? and repay_amount = ? and lid = ?",gotUserCode,repay_amount.longValue(),loanCode);
//						if(!StringUtil.isBlank(tid)){
//							Db.update("update t_loan_transfer set traceCode = ? where transCode = ?",tid,transCode);
//							Db.update("update t_loan_trace set traceState = 'F' where traceCode = ?",tid);
//						}
//					}
//					System.out.println("[更新债权转让记录的关联投标流水ID]处理,债权转让编码["+traceCode+"]");
//					doCount ++ ;
//				}
//			}else{
//				isGo = false ;
//			}
//			startIndex ++ ;
//		}
////		Db.update("update t_loan_trace set traceState = 'F' where traceCode in (select traceCode from t_loan_transfer where transState='B')");
//	}
	
//	/**
//	 * 更新投标流水的下一次回收本金、回收利息
//	 */
//	@SuppressWarnings("unchecked")
//	public void setup5(){
//		int startIndex = 0 ;
//		int size = 50 ;
//		boolean isGo = true ;
//		long total = Db.queryLong("select count(uid) from t_loan_trace");
//		int doCount = 1 ;
//		while( isGo ){
//			List<Object[]> loanTraces = getLoanTraceList(startIndex, size) ;
//			if( doCount <= total ){
//				for(Object[] loanTrace : loanTraces){
//					System.err.println("[更新投标记录:下一期回收本金、回收利息]当前进度第" + (doCount)+"/"+ total + "条投标流水" );
//					String traceCode = (String) loanTrace[0];//投标编码
//					long payAmount = (long) loanTrace[1];//投标金额
//					String refundType = (String) loanTrace[2];//还款方式
//					int rateByYear = (int) loanTrace[3];//年利率
//					int rewardRateByYear = (int) loanTrace[4];//奖励年利率
//					int loanTimeLimit = (int) loanTrace[5];//总期数
//					int loanRecyCount = (int) loanTrace[6];//待还期数
//					int nextLimitIndex = loanTimeLimit - loanRecyCount + 1;//下一期还款期数
//					
//					long nextAmount = 0;
//					long nextInterest = 0;
//					if(loanRecyCount>0){
//						if(refundType.equals(SysEnum.refundType.A.val())){
//							long[] x = CommonUtil.f_001(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, nextLimitIndex);
//							nextAmount = x[0];
//							nextInterest = x[1];
//						}else if(refundType.equals(SysEnum.refundType.B.val())){
//							long[] x = CommonUtil.f_002(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, nextLimitIndex);
//							nextAmount = x[0];
//							nextInterest = x[1];
//						}
//					}
//					Db.update("update t_loan_trace set nextAmount = ?,nextInterest = ? where traceCode = ?",nextAmount,nextInterest,traceCode);
//					System.out.println("[更新投标记录:下一期回收本金、回收利息]处理,投标金额["+payAmount+"],投标编码["+traceCode+"]");
//					doCount ++ ;
//				}
//			}else{
//				isGo = false ;
//			}
//			startIndex ++ ;
//		}
//	}
	
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
		//querySql = querySql.replace("${index}", index+"" ) ;
		//querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql,index,size) ;
		return users ;
	}
	
	/**
	 * 	获取投标流水记录
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getTenderList(int index , int size ){
		index = index * size;
		String querySql = "select * from tmp_tender limit ?,?";
		//querySql = querySql.replace("${index}", index+"" ) ;
		//querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql,index,size) ;
		return users ;
	}
	
	/**
	 * 	获取贷款标记录
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getLoanInfoList(int index , int size ){
		index = index * size;
		String querySql = "select loanAmount,loanTimeLimit,reciedCount,loanCode,userCode from t_loan_info where loanState in ('N','O','P','Q','R') limit ?,?";
		//querySql = querySql.replace("${index}", index+"" ) ;
		//querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql,index,size) ;
		return users ;
	}
	
	/**
	 * 	获取临时推荐用户表信息
	 * @param index
	 * @param size
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private List getTmpUser(int index , int size ){
		index = index * size;
		String querySql = "select uid,umobile,invite_uid,crDateTime,crDate from tmp_user limit ?,?";
		//querySql = querySql.replace("${index}", index+"" ) ;
		//querySql = querySql.replace("${size}", size+"" ) ;
		List users =  Db.query(querySql,index,size) ;
		return users ;
	}
	
//	/**
//	 * 	获取债权转让记录
//	 * @param index
//	 * @param size
//	 * @return
//	 */
//	@SuppressWarnings("rawtypes")
//	private List getLoanTransferList(int index , int size ){
//		index = index * size;
//		String querySql = "select transCode,traceCode,loanCode,payUserCode,gotUserCode from t_loan_transfer where transState='B' limit ${index},${size}";
//		querySql = querySql.replace("${index}", index+"" ) ;
//		querySql = querySql.replace("${size}", size+"" ) ;
//		List users =  Db.query(querySql) ;
//		return users ;
//	}
	
//	/**
//	 * 	获取投标记录
//	 * @param index
//	 * @param size
//	 * @return
//	 */
//	@SuppressWarnings("rawtypes")
//	private List getLoanTraceList(int index , int size ){
//		index = index * size;
//		String querySql = "select traceCode,payAmount,refundType,rateByYear,rewardRateByYear,loanTimeLimit,loanRecyCount from t_loan_trace limit ${index},${size}";
//		querySql = querySql.replace("${index}", index+"" ) ;
//		querySql = querySql.replace("${size}", size+"" ) ;
//		List users =  Db.query(querySql) ;
//		return users ;
//	}

}
