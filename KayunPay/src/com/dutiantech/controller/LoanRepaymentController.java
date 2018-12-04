package com.dutiantech.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.HistoryRecy;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;

public class LoanRepaymentController extends BaseController {
	
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private UserService userService = getService(UserService.class);

	/**
	 * 根据loanTraceCode修复回款计划
	 * @return
	 */
	@ActionKey("/repairLoanRepayment4LoanTrace")
	@Before(PkMsgInterceptor.class)
	public Message repairLoanRepayment4LoanTrace() {
		String loanTraceCode = getPara("loanTraceCode", "");
		if (StringUtil.isBlank(loanTraceCode)) {
			return error("99", "Error: loanTraceCode is null", null);
		}
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTraceCode);
		// 删除异常回款计划
		System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
		List<LoanRepayment> lstLoanRepayments = loanRepaymentService.findByLoanTrace(loanTrace.getStr("traceCode"));
		for (LoanRepayment loanRepayment : lstLoanRepayments) {
			loanRepayment.delete();
		}
		// 重新生成回款计划
		try {
			System.out.println("[" + loanTrace.getStr("traceCode") + "]重新生成回款计划");
			genRepaymentPlanByLoanTrace(loanTrace.getStr("traceCode"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常");
			return error("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
		}
		// 填充回款数据
		try {
			System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
			fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
			return error("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
		}
		
		// 更新债转信息
		List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTrace.getStr("traceCode"));
		for (LoanTransfer loanTransfer : lstLoanTransfer) {
			transfer(loanTransfer.getStr("transCode"), loanTransfer.getStr("gotUserCode"), loanTransfer.getStr("gotUserName"));	
		}
		
		return succ("done...", null);
	}
	
	
	/**
	 * 修复回款日期在半年内，未生成的回款计划（含已结清、提前结清）
	 * @return
	 */
	@ActionKey("/repairLoanRepayment4User")
	@Before(PkMsgInterceptor.class)
	public Message repairLoanRepayment4User() {
		String userCode = getPara("userCode", "");
		if (StringUtil.isBlank(userCode)) {
			return error("99", "Error: userCode is null", null);
		}
		List<LoanTrace> lstLoanTraces = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace t1 "
				+ "WHERE payUserCode = ? AND traceCode NOT IN (SELECT loanTraceCode FROM t_loan_repayment "
				+ "WHERE usercode = ? GROUP BY loanTraceCode) "
				+ "AND loanRecyDate > DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 6 MONTH), '%Y%m%d')", userCode, userCode);
		List<Object> lstResult = new ArrayList<Object>();
		for (LoanTrace loanTrace : lstLoanTraces) {
			// 删除异常回款计划
			System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
			List<LoanRepayment> lstLoanRepayments = loanRepaymentService.findByLoanTrace(loanTrace.getStr("traceCode"));
			for (LoanRepayment loanRepayment : lstLoanRepayments) {
				loanRepayment.delete();
			}
			// 重新生成回款计划
			try {
				System.out.println("[" + loanTrace.getStr("traceCode") + "]重新生成回款计划");
				genRepaymentPlanByLoanTrace(loanTrace.getStr("traceCode"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常");
				return error("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
			}
			// 填充回款数据
			try {
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
				fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
				return error("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
			}
			
			// 更新债转信息
			List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTrace.getStr("traceCode"));
			for (LoanTransfer loanTransfer : lstLoanTransfer) {
				transfer(loanTransfer.getStr("transCode"), loanTransfer.getStr("gotUserCode"), loanTransfer.getStr("gotUserName"));	
			}
			
			lstResult.add(loanTrace.getStr("traceCode"));
		}
		return succ("done...", JSONObject.toJSON(lstResult));
	}
	
//	@ActionKey("/checkLoanRepayment")
//	@Before(PkMsgInterceptor.class)
//	public Message checkLoanRepayment() {
//		int limit = getParaToInt("limit", 1);
//		List<Object[]> lstLoanTraces = Db.query("SELECT payusercode, COUNT(1), t3.num FROM t_loan_trace t1 LEFT JOIN "
//				+ "(SELECT userCode, COUNT(1) num FROM (SELECT usercode, COUNT(1) FROM t_loan_repayment WHERE recyStatus = 0 GROUP BY loantracecode) t2 GROUP BY usercode) t3 "
//				+ "ON t1.payUserCode = t3.userCode WHERE loanState = 'N' GROUP BY payUserCode HAVING COUNT(1) <> t3.num LIMIT 0, ?", limit);
//		for (Object[] objects : lstLoanTraces) {
//			String payUserCode = objects[0].toString();
//			int loanTraceCount = Integer.parseInt(objects[1].toString());
//			int loanRepaymentCount = Integer.parseInt(objects[2].toString());
//			
//			if (loanTraceCount != loanRepaymentCount) {
//				List<String> lstLoanRepayment = Db.query("SELECT loanTraceCode FROM t_loan_repayment WHERE userCode = ? AND recyStatus = 0 GROUP BY loanTraceCode", payUserCode);
//				List<String> lstUserLoanTrace = Db.query("SELECT traceCode FROM t_loan_trace WHERE payUserCode = ? AND loanState = 'N'", payUserCode);
//				// 遍历回款计划 移除已存在的回款计划
//				for (String traceCode : lstLoanRepayment) {
//					if (lstUserLoanTrace.indexOf(traceCode) > 0) {
//						lstUserLoanTrace.remove(traceCode);
//					}
//				}
//				
//				// 根据剩余不存在的投标记录生成回款计划
//				for (String loanTraceCode : lstUserLoanTrace) {
//					// 重新生成回款计划
//					try {
//						System.out.println("[" + loanTraceCode + "]重新生成回款计划");
//						genRepaymentPlanByLoanTrace(loanTraceCode);
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("["+loanTraceCode+"]重新生成回款计划异常");
//						return error("error", "["+loanTraceCode+"]重新生成回款计划异常", null);
//					}
//					// 填充回款数据
//					try {
//						System.out.println("[" + loanTraceCode + "]填充回款计划");
//						fillHistoryRepaymentPlan(loanTraceCode);
//					} catch (Exception e) {
//						e.printStackTrace();
//						System.out.println("[" + loanTraceCode + "]填充回款计划异常");
//						return error("error", "["+loanTraceCode+"]填充回款计划异常异常", null);
//					}
//				}
//			}
//		}
//		return succ("done...", null);
//	}
	
	/**
	 * 验证回款计划数据量
	 */
//	@ActionKey("/validNum")
//	@Before(PkMsgInterceptor.class)
//	public Message validNum() {
//		int limit = getParaToInt("limit", 1);
//		List<LoanTrace> lstLoanTraces = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace WHERE loanState = 'N' AND loanTimeLimit <> (SELECT COUNT(1) FROM t_loan_repayment WHERE loanTraceCode = traceCode) LIMIT 0, ?", limit);
//		List<Object> lstResult = new ArrayList<Object>();
//		for (LoanTrace loanTrace : lstLoanTraces) {
//			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", loanTrace.getStr("traceCode"));
//			if (lstLoanRepayment.size() != loanTrace.getInt("loanTimeLimit")) {
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]回款计划异常");
//				lstResult.add(loanTrace.getStr("traceCode"));
//			}
//			// 删除异常回款计划
//			System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
//			for (LoanRepayment loanRepayment : lstLoanRepayment) {
//				loanRepayment.delete();
//			}
//			// 重新生成回款计划
//			try {
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]重新生成回款计划");
//				genRepaymentPlanByLoanTrace(loanTrace.getStr("traceCode"));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常");
//				return error("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
//			}
//			// 填充回款数据
//			try {
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
//				fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
//				return error("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
//			}
//		}
//		return succ("done...", JSONObject.toJSON(lstResult));
//	}
	
	/**
	 * 验证回款金额
	 * @return
	 */
//	@ActionKey("/validAmount")
//	@Before(PkMsgInterceptor.class)
//	public Message validAmount() {
//		String loanTraceCode = getPara("loanTraceCode", "");
//		int limit = getParaToInt("limit", 1);
//		
//		String querySql = "SELECT loanTraceCode, sum(repaymentPrincipal), sum(repaymentInterest), leftAmount, leftInterest ";
//		String fromSql = " FROM t_loan_repayment t1 LEFT JOIN t_loan_trace t2 ON t1.loanTraceCode = t2.traceCode ";
//		String whereSql = " WHERE t1.recyStatus = 0 AND t2.loanState = 'N' AND loanDateTime > 20180520000000";
//		String groupSql = " GROUP BY t1.loanTraceCode HAVING SUM(repaymentPrincipal) <> t2.leftAmount OR SUM(repaymentInterest) <> t2.leftInterest";
//		String limitSql = " LIMIT 0, " + limit;
//		
//		if (!StringUtil.isBlank(loanTraceCode)) {
//			whereSql = whereSql + " AND loanTraceCode = '" + loanTraceCode + "'";
//		}
//		
//		List<Object[]> lstLoanTraces = Db.query(querySql + fromSql + whereSql + groupSql + limitSql);
//		for (Object[] object : lstLoanTraces) {
//			LoanTrace loanTrace = loanTraceService.findById(object[0].toString());
//			// 删除异常回款计划
//			System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
//			List<LoanRepayment> lstLoanRepayments = loanRepaymentService.findByLoanTrace(loanTrace.getStr("traceCode"));
//			for (LoanRepayment loanRepayment : lstLoanRepayments) {
//				loanRepayment.delete();
//			}
//			// 重新生成回款计划
//			try {
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]重新生成回款计划");
//				genRepaymentPlanByLoanTrace(loanTrace.getStr("traceCode"));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常");
//				return error("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
//			}
//			// 填充回款数据
//			try {
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
//				fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
//			} catch (Exception e) {
//				e.printStackTrace();
//				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
//				return error("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
//			}
//			
//			
//			List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTrace.getStr("traceCode"));
//			for (LoanTransfer loanTransfer : lstLoanTransfer) {
//				transfer(loanTransfer.getStr("transCode"), loanTransfer.getStr("gotUserCode"), loanTransfer.getStr("gotUserName"));	
//			}
//		}
//		return succ("done...", null);
//	}
	
	private boolean transfer(String transCode, String gotUserCode, String gotUserName) {
		LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findById(transCode);
		if (loanTransfer == null) {
			return false;
		}
		String loanTraceCode = loanTransfer.getStr("traceCode");
		long payAmount = Long.parseLong(loanTransfer.get("leftAmount").toString());
		int rateByYear = loanTransfer.getInt("rateByYear");
		int rewardRateByYear = loanTransfer.getInt("rewardRateByYear");
		int loanPeriod = loanTransfer.getInt("loanRecyCount");
		String refundType = loanTransfer.getStr("refundType");
		
		long sumPrincipal = 0l;
//		List<LoanRepayment> lstLoanRepayment = findRemaindByLoanTrace(loanTraceCode);
//		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTraceCode);
		
		int loanTimeLimit = Db.queryInt("SELECT loanTimeLimit FROM t_loan_info WHERE loanCode = ?", loanTransfer.getStr("loanCode"));
		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod > ?", loanTraceCode, loanTimeLimit - loanTransfer.getInt("loanRecyCount"));
				
		for (int i = 0; i < lstLoanRepayment.size(); i++) {
			LoanRepayment loanRepayment = lstLoanRepayment.get(i);
			long[] repayment = CommonUtil.f_000(payAmount, loanPeriod, rateByYear + rewardRateByYear, i + 1, refundType);
			long repaymentPrincipal = repayment[0];
			long repaymentInterest = repayment[1];
			
			if (i + 1 == lstLoanRepayment.size()) {
				repaymentPrincipal = payAmount - sumPrincipal;
			}
			sumPrincipal += repaymentPrincipal;
			
			loanRepayment.set("userCode", gotUserCode);
			loanRepayment.set("userName", gotUserName);
			loanRepayment.set("repaymentAmount", repaymentPrincipal + repaymentInterest);
			loanRepayment.set("repaymentPrincipal", repaymentPrincipal);
			loanRepayment.set("repaymentInterest", repaymentInterest);
			loanRepayment.update();
			System.out.println("投标流水["+loanTransfer.getStr("traceCode")+"]债转成功...更新第" + loanRepayment.getInt("recyPeriod") + "期回款计划["+loanRepayment.getInt("rid")+"]");
		}
		return true;
	}
	
	public void genRepaymentPlanByLoanTrace(String loanTraceCode) {
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTraceCode);
		List<LoanRepayment> lstLoanRepayment = loanRepaymentService.findByLoanTrace(loanTraceCode);	// 根据投标记录查询回款计划是否已生成
		if (lstLoanRepayment.size() <= 0) {
			LoanInfo loanInfo = loanInfoService.findById(loanTrace.getStr("loanCode"));
			Long payAmount = loanTrace.getLong("payAmount");
			Integer loanTimeLimit = loanInfo.getInt("loanTimeLimit");
			Integer rateByYear = loanInfo.getInt("rateByYear");
			Integer rewardRateByYear = loanTrace.getInt("rewardRateByYear");
			String refundType = loanInfo.getStr("refundType");
			int benefits4new = loanInfo.getInt("benefits4new");
			int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
			
			// 解析加息券
			List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTraceCode);
			if (lstLoanTransfer != null && lstLoanTransfer.size() > 0) {
				String ticketRewardRate = loanTrace.getStr("loanTicket");
				if (!StringUtil.isBlank(ticketRewardRate)) {
					JSONArray jsonArray = JSONArray.parseArray(ticketRewardRate);
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					if (null != jsonObject.get("rate")) {
						rewardRateByYear = rewardRateByYear + jsonObject.getInteger("rate");
					}
				}
			}
			
			long sumRepaymentPrincipal = 0l;
			for (int i = 1; i <= loanTimeLimit; i++) {
				long[] repayment = CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, i, refundType);
				if (i == loanTimeLimit) {
					repayment[0] = payAmount - sumRepaymentPrincipal;
				}
				System.out.println("Limit " + i + "==== Repayment Principal: " + repayment[0] + " #### Repayment Interest: " + repayment[1]);
				String payUserCode = loanTrace.get("payUserCode");
				long repaymentPrincipal = repayment[0];
				long repaymentInterest = repayment[1];
				long interestFee = repayment[1] * 8 / 100;
				sumRepaymentPrincipal += repaymentPrincipal;
				
				// 首月加息活动计算利息
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					if (i == 1 && benefits4new == 0) {
						repaymentInterest = CommonUtil.f_004(payAmount, rateByYear + 400, i, refundType)[1];
					}
				}
				
				if( repaymentInterest > 0 ){
					//计算利息管理费
					User user = userService.findUserAllInfoById(payUserCode) ;
					int vipInterestRate = user.getInt("vipInterestRate") ;//利息管理费费率
					String effectDate = loanInfo.getStr("effectDate");//满标日期
					if(Long.parseLong(effectDate) < 20180319){//会员等级更新之前，利息管理费费率不变
						if(user.getInt("beforeVip") != null){
							vipInterestRate = historyInterest(user.getInt("beforeVip"));//根据原用户等级，获取对应等级利息管理费费率
						}
					}
					int vipRiskRate = user.getInt("vipRiskRate") ;
					long tmpInterestFee = 0 ;
					//利息管理费
					if( vipInterestRate > 0 ){
						tmpInterestFee = repaymentInterest * vipInterestRate / 10 / 10 / 10 / 10;
					}
					long tmpRiskFee = 0 ;
					if( vipRiskRate > 0 ){
						tmpRiskFee = repaymentInterest * vipRiskRate / 10 / 10 / 10 / 10;
						if( tmpRiskFee > 0 ){
							Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1",
									tmpRiskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
						}
					}
					interestFee = tmpInterestFee + tmpRiskFee ;
				}
				
				Map<String, Object> para = new HashMap<String, Object>();
				para.put("userCode", payUserCode);
				para.put("userName", loanTrace.get("payUserName"));
				para.put("loanNo", loanInfo.get("loanNo"));
				para.put("loanCode", loanInfo.get("loanCode"));
				para.put("payAmount", loanTrace.get("payAmount"));
				para.put("recyPeriod", i);
				para.put("recyStatus", 0);
				para.put("repaymentAmount", repaymentPrincipal + repaymentInterest);
				para.put("repaymentPrincipal", repaymentPrincipal);
				para.put("repaymentInterest", repaymentInterest);
				para.put("interestFee", interestFee);
				para.put("repaymentDate", DateUtil.addMonth(loanInfo.getStr("effectDate"), i));
				para.put("loanTraceCode", loanTrace.get("traceCode"));
				loanRepaymentService.save(para);
			}
		}
	}
	
	private void fillHistoryRepaymentPlan(String loanTraceCode) {
		System.out.println("start fill [" + loanTraceCode + "] history repayment plan..." + DateUtil.getNowDateTime());
		LoanTrace loanTrace = loanTraceService.findById(loanTraceCode);
		LoanInfo loanInfo = loanInfoService.findById(loanTrace.getStr("loanCode"));
		int reciedCount = loanInfo.getInt("reciedCount");
		int recyLimit;
		
		for (int i = 1; i <= reciedCount; i++) {
			System.out.print("repayment period: " + i + "... ");
			HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT recyAmount, recyInterest, recyDate FROM t_history_recy WHERE loanCode = ? AND recyLimit = ? AND payUserCode = ? AND payAmount = ?", loanInfo.getStr("loanCode"), i, loanTrace.getStr("payUserCode"), loanTrace.getLong("payAmount"));
			recyLimit = i;
			if (historyRecy != null) {
				long repaymentPrincipal = historyRecy.getLong("recyAmount");
				long repaymentInterest = historyRecy.getLong("recyInterest");
				long interestFee = 0;
				int overdueDays = 0;
				long overdueInterest = 0;
				String repaymentDate = historyRecy.getStr("recyDate");

				int benefits4new = loanInfo.getInt("benefits4new");
				int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
				// 首月加息活动计算利息
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					if (i == 1 && benefits4new == 0) {
						repaymentInterest = CommonUtil.f_004(loanTrace.getLong("payAmount"), loanInfo.getInt("rateByYear") + 400, i, loanInfo.getStr("refundType"))[1];
					}
				}
				
//				FundsTrace fundsTrace = FundsTrace.fundsTraceDao.findFirst("SELECT * FROM t_funds_trace WHERE userCode = '"+loanTrace.getStr("payUserCode")+"' AND traceAmount = "+repaymentInterest+" AND traceRemark like '%["+loanInfo.getStr("loanNo")+"]第"+i+"%' AND traceType = 'L'"); 
//				if (fundsTrace != null) {
//					interestFee = fundsTrace.getInt("traceFee");
//				}
				System.out.print("recyPrincipal:" + repaymentPrincipal + "  recyInterest:" + repaymentInterest);
				loanRepaymentService.repayment(loanTraceCode, recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
			} else {
				getLoanTransfer(loanTrace.getStr("payUserCode"), loanInfo, loanTrace, recyLimit, loanTrace.getLong("payAmount"));
			}
			System.out.println();
		}
	}
	
	private void getLoanTransfer(String userCode, LoanInfo loanInfo, LoanTrace loanTrace, int recyLimit, long payAmount) {
		LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("SELECT * FROM t_loan_transfer WHERE gotUserCode = ? AND traceCode = ?", userCode, loanTrace.getStr("traceCode"));
		if (loanTransfer == null) {
			return;
		}
		userCode = loanTransfer.getStr("payUserCode");
		HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT recyAmount, recyInterest, recyDate FROM t_history_recy WHERE loanCode = ? AND payUserCode = ? AND recyLimit = ? AND payAmount = ?", loanInfo.getStr("loanCode"), loanTransfer.getStr("payUserCode"), recyLimit, payAmount);
		if (historyRecy != null) {
			// 更新回款计划剩余期限债权人
			loanRepaymentService.update4Transfer(loanTrace.getStr("traceCode"), userCode, loanTransfer.getStr("payUserName"), recyLimit);
			long repaymentPrincipal = historyRecy.getLong("recyAmount");
			long repaymentInterest = historyRecy.getLong("recyInterest");
			long interestFee = 0;
			int overdueDays = 0;
			long overdueInterest = 0;
			String repaymentDate = historyRecy.getStr("recyDate");
			
			int benefits4new = loanInfo.getInt("benefits4new");
			int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
			// 首月加息活动计算利息
			if (releaseDate >= 20171111 && releaseDate <= 20171117) {
				if (recyLimit == 1 && benefits4new == 0) {
					repaymentInterest = CommonUtil.f_004(loanTrace.getLong("payAmount"), loanInfo.getInt("rateByYear") + 400, recyLimit, loanInfo.getStr("refundType"))[1];
				}
			}
			
			loanRepaymentService.repayment(loanTrace.getStr("traceCode"), recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
		} else {
			getLoanTransfer(userCode, loanInfo, loanTrace, recyLimit, payAmount);
		}
	}
	
	private int historyInterest(Integer beforeVip){
		switch (beforeVip) {
			case 1://少尉
				return 300;
			case 2://中尉
				return 300;
			case 3://上尉
				return 300;
			case 4://少校
				return 200;
			case 5://中校
				return 200;
			case 6://上校
				return 100;
			case 7://将军
				return 0;
		}
		return 0;
	}
}
