package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.model.HistoryRecy;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;

public class LoanRepaymentService extends BaseService {
	
	/**
	 * 补全查询月份未生成的回款计划
	 * @param userCode
	 * @param date yyyyMM
	 */
	public void supplementByDate(String userCode, String date) { 
		if (DateUtil.isValidDate(date, "yyyyMM")) {
			List<LoanTrace> lstLoanTraces = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace "
					+ "WHERE traceCode NOT IN (SELECT loanTraceCode FROM t_loan_repayment WHERE userCode = ? GROUP BY loanTraceCode) AND payUserCode = ? "
					+ "AND loanState IN ('N','O','P') AND DATE_FORMAT(DATE_ADD(loanDateTime, INTERVAL loanTimeLimit MONTH), '%Y%m') >= ?", userCode, userCode, date);
			for (LoanTrace loanTrace : lstLoanTraces) {
				supplementByLoanCode(loanTrace.getStr("loanCode"));
			}
		}
	}
	
	/**
	 * 查询月每日回款数据
	 * @param userCode	用户编号
	 * @param date	查询日期 "yyyyMM" "yyyyMMdd"
	 * @return	Map<String, Map<String, Object>> 
	 * 				sumPrincipal 回款日回款本金总额
	 * 				sumInterest 回款日回款利息总额
	 * 				countRecy 回款日回款笔数
	 * 				loanTraces	回投标记录 
	 */
	/**
	 * @param userCode
	 * @param date
	 * @return
	 * @throws NullPointerException
	 */
	public Map<String, Map<String, Object>> queryByDate(String userCode, String date) throws NullPointerException {
		String sqlSelect = "SELECT repaymentDate, COALESCE(SUM(repaymentPrincipal), 0), COALESCE(SUM(repaymentInterest), 0), count(1) ";
		String sqlEarlyRecy = " AND (ISNULL(t2.loanCode) or t1.recyPeriod <= t2.earlyLimit)";
		String sqlFrom = " FROM t_loan_repayment t1 LEFT JOIN t_settlement_early t2 ON t1.loanCode = t2.loanCode";
		String sqlGroup = " GROUP BY repaymentDate";
		String sqlOrder = " ORDER BY repaymentDate ASC";
		
		StringBuffer buff = new StringBuffer();
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "t1.userCode", "=", userCode, "AND");
		if (DateUtil.isValidDate(date, "yyyyMM")) {
			makeExp(buff, ps, "DATE_FORMAT(repaymentDate, '%Y%m')", "=", date, "AND");
		} else if (DateUtil.isValidDate(date, "yyyyMMdd")) {
			makeExp(buff, ps, "DATE_FORMAT(repaymentDate, '%Y%m%d')", "=", date, "AND");
		}
		
		// 查询当月（日）回款数据
		List<Object[]> lstQueryResults = Db.query(sqlSelect + sqlFrom + makeSql4Where(buff) + sqlEarlyRecy + sqlGroup + sqlOrder, ps.toArray());
		if (lstQueryResults == null || lstQueryResults.size() == 0) {
			return null;
		}
		// 组装回款数据
		Map<String, Map<String, Object>> mapResults = new HashMap<String, Map<String, Object>>();
		for (Object[] queryResult : lstQueryResults) {
			Map<String, Object> mapDayResult = new HashMap<String, Object>();
			String queryDay = queryResult[0].toString();		// 回款日期
			mapDayResult.put("sumPrincipal", queryResult[1]);	// 当日累计回款本金
			mapDayResult.put("sumInterest", queryResult[2]);	// 当日累计回款利息
			mapDayResult.put("countRecy", queryResult[3]);		// 当日累计回款笔数
			
			// 查询当日回款详情列表
			List<LoanRepayment> lstLoanRepayment = null;
			lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment t1 WHERE userCode = ? AND repaymentDate = ? AND (repaymentDate <= (SELECT earlyDate FROM t_settlement_early WHERE t1.loanCode = loanCode) OR ISNULL((SELECT earlyDate FROM t_settlement_early WHERE t1.loanCode = loanCode)))", userCode, queryDay);
			for (LoanRepayment loanRepayment : lstLoanRepayment) {
				loanRepayment.set("repaymentPrincipal", StringUtil.getMoneyYuan(loanRepayment.getLong("repaymentPrincipal")));
				loanRepayment.set("repaymentInterest", StringUtil.getMoneyYuan(loanRepayment.getLong("repaymentInterest")));
				String recyStatus = String.valueOf(loanRepayment.getInt("recyStatus"));
				if ("1".equals(recyStatus)) {
					boolean isSettlementEarly = Db.queryLong("SELECT COUNT(1) FROM t_settlement_early WHERE loanCode = ?", loanRepayment.getStr("loanCode")) > 0 ? true : false;
					if (isSettlementEarly) {
						recyStatus = "提前还款成功";
					}
					recyStatus = "回款成功";
				} else {
					recyStatus = "回款中";
				}
				loanRepayment.set("recyStatus", recyStatus);
			}
			mapDayResult.put("loanRepayments", lstLoanRepayment);
			
			mapResults.put(queryResult[0].toString(), mapDayResult);
		}
		return mapResults;
	}
	
	/**
	 * 补充生成用户半年内结清，但未生成的回款计划
	 * @param userCode
	 */
	public void supplementRepaymentPlan(String userCode) {
		List<LoanTrace> lstLoanTraces = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace t1 "
				+ "WHERE payUserCode = ? AND traceCode NOT IN (SELECT loanTraceCode FROM t_loan_repayment "
				+ "WHERE usercode = ? GROUP BY loanTraceCode) "
				+ "AND loanRecyDate > DATE_FORMAT(DATE_SUB(NOW(),INTERVAL 6 MONTH), '%Y%m%d')", userCode, userCode);
		List<Object> lstResult = new ArrayList<Object>();
		for (LoanTrace loanTrace : lstLoanTraces) {
			// 删除异常回款计划
			System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
			List<LoanRepayment> lstLoanRepayments = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", loanTrace.getStr("traceCode"));
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
				throw new BaseBizRunTimeException("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
			}
			// 填充回款数据
			try {
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
				fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
				throw new BaseBizRunTimeException("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
			}
			
			// 更新债转信息
			List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTrace.getStr("traceCode"));
			for (LoanTransfer loanTransfer : lstLoanTransfer) {
				transfer(loanTransfer.getStr("transCode"), loanTransfer.getStr("gotUserCode"), loanTransfer.getStr("gotUserName"));	
			}
			
			lstResult.add(loanTrace.getStr("traceCode"));
		}
	}
	
	public void supplementByLoanCode(String loanCode) {
		List<LoanTrace> lstLoanTraces = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace WHERE loanCode = ?", loanCode);
		for (LoanTrace loanTrace : lstLoanTraces) {
			// 删除异常回款计划
			System.out.println("[" + loanTrace.getStr("traceCode") + "]清空回款计划");
			List<LoanRepayment> lstLoanRepayments = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", loanTrace.getStr("traceCode"));
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
				throw new BaseBizRunTimeException("error", "["+loanTrace.getStr("traceCode")+"]重新生成回款计划异常", null);
			}
			// 填充回款数据
			try {
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划");
				fillHistoryRepaymentPlan(loanTrace.getStr("traceCode"));
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("[" + loanTrace.getStr("traceCode") + "]填充回款计划异常");
				throw new BaseBizRunTimeException("error", "["+loanTrace.getStr("traceCode")+"]填充回款计划异常异常", null);
			}
			
			// 更新债转信息
			List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", loanTrace.getStr("traceCode"));
			for (LoanTransfer loanTransfer : lstLoanTransfer) {
				transfer(loanTransfer.getStr("transCode"), loanTransfer.getStr("gotUserCode"), loanTransfer.getStr("gotUserName"));	
			}
		}
		
		// 补全提前还款标的回款计划的回款状态
		SettlementEarly settlementEarly = SettlementEarly.settletmentEaryltDao.findById(loanCode);
		if (settlementEarly != null) {
			List<LoanRepayment> lstLoanRepayments = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanCode = ? AND recyStatus = 0", loanCode);
			for (LoanRepayment loanRepayment : lstLoanRepayments) {
				loanRepayment.set("recyStatus", 1);
				loanRepayment.update();
			}
		}
		
	}
	
	/**
	 * 根据标的投标流水生成回款计划
	 * @param loanCode	投标交易流水号
	 * @return
	 */
	public boolean generateRepaymentPlan(String loanCode) {
		List<LoanTrace> lstLoanTrace = LoanTrace.loanTraceDao.find("SELECT * FROM t_loan_trace WHERE loanCode = ?", loanCode);
		for (int j = 0; j < lstLoanTrace.size(); j++) {
			String loanTraceCode = lstLoanTrace.get(j).getStr("traceCode");
			List<LoanRepayment> lstLoanRepayment = findByLoanTrace(loanTraceCode);	// 根据投标记录查询回款计划是否已生成
			if (lstLoanRepayment.size() <= 0) {
				LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTraceCode);
				LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanTrace.getStr("loanCode"));
				Long payAmount = loanTrace.getLong("payAmount");	// 投标金额
				Integer loanTimeLimit = loanInfo.getInt("loanTimeLimit");	// 标的借款期限
				Integer rateByYear = loanInfo.getInt("rateByYear");	// 年利率
				Integer rewardRateByYear = loanTrace.getInt("rewardRateByYear");	// 奖励年利率
				String refundType = loanInfo.getStr("refundType");	// 还款方式
//				int benefits4new = loanInfo.getInt("benefits4new");	// 新手标奖励利率
//				int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));	// 标的发布日期
				
				long sumRepaymentPrincipal = 0l;
				int genCount = 0;
				for (int i = 1; i <= loanTimeLimit; i++) {
					long[] repayment = CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, i, refundType);
					if (i == loanTimeLimit) {
						repayment[0] = payAmount - sumRepaymentPrincipal;
					}
					String payUserCode = loanTrace.get("payUserCode");
					long repaymentPrincipal = repayment[0];
					long repaymentInterest = repayment[1];
					long interestFee = 0l;
					sumRepaymentPrincipal += repaymentPrincipal;
					
					// 首月加息活动计算利息(已过期)
//					if (releaseDate >= 20171111 && releaseDate <= 20171117) {
//						if (i == 1 && benefits4new == 0) {
//							repaymentInterest = CommonUtil.f_004(payAmount, rateByYear + 400, i, refundType)[1];
//						}
//					}

					//	计算利息管理费
					if( repaymentInterest > 0 ){
						User user = User.userDao.findById(payUserCode);
						int vipInterestRate = user.getInt("vipInterestRate") ;	// 利息管理费费率
						int vipRiskRate = user.getInt("vipRiskRate") ;	// 风险储备金费率
						// 利息管理费
						long tmpInterestFee = 0 ;
						if( vipInterestRate > 0 ){
							tmpInterestFee = repaymentInterest * vipInterestRate / 10 / 10 / 10 / 10;
						}
						// 风险储备金费
						long tmpRiskFee = 0 ;
						if( vipRiskRate > 0 ){
							tmpRiskFee = repaymentInterest * vipRiskRate / 10 / 10 / 10 / 10;
							if( tmpRiskFee > 0 ){
								Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1", tmpRiskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
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
					para.put("loanPeriod", loanInfo.get("loanTimeLimit"));
					para.put("recyStatus", 0);
					para.put("repaymentAmount", repaymentPrincipal + repaymentInterest);
					para.put("repaymentPrincipal", repaymentPrincipal);
					para.put("repaymentInterest", repaymentInterest);
					para.put("interestFee", interestFee);
					para.put("repaymentDate", DateUtil.addMonth(loanInfo.getStr("effectDate"), i));
					para.put("loanTraceCode", loanTrace.get("traceCode"));
					if (save(para)) {
						genCount++;
					}
				}
				if (genCount == loanTimeLimit) {	// 生成成功数量等于回款期数，则返回成功
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 新增还款计划
	 * @param para	Map集合
	 * 包含字段如下<br>
	 * userCode					用户编码<br>
	 * userName					用户名<br>
	 * loanNo					标号<br>
	 * loanCode					标的编码<br>
	 * recyPeriod				还款期数<br>
	 * recyStatus				还款状态：0=未还款，1=已还款；默认为0<br>
	 * repaymentAmount			应还款金额（本金+利息）<br>
	 * repaymentYesAmount		实际还款金额（本金+利息）<br>
	 * repaymentPrincipal		应还本金<br>
	 * repaymentYesPrincipal	实际还款本金<br>
	 * repaymentInterest		应还利息<br>
	 * repaymentYesInterest		实际还款利息<br>
	 * interestFee				应收利息管理费<br>
	 * yesInterestFee			实收利息管理费<br>
	 * repaymentDate			应还款时间<br>
	 * repaymentYesDate			实际还款时间<br>
	 * loanTraceCode			投标流水编号<br>
	 * overdueDays				逾期天数<br>
	 * overdueInterest			逾期利息<br>
	 * @return
	 */
	public boolean save(Map<String, Object> para) {
		LoanRepayment loanRepayment = new LoanRepayment();
		if (para.get("recyStatus") == null) {
			para.put("recyStatus", 0);	// 如果还款状态未填，默认为未还
		}
		para.put("addDate", DateUtil.getNowDate());
		para.put("addTime", DateUtil.getNowTime());
		para.put("addDateTime", DateUtil.getNowDateTime());
		loanRepayment._setAttrs(para);
		return loanRepayment.save();
	}
	
	/**
	 * 查询对应投标记录的全部回款计划
	 * @param loanTraceCode	查询投标记录
	 * @return
	 */
	public List<LoanRepayment> findByLoanTrace(String loanTraceCode) {
		return LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", loanTraceCode);
	}
	
	/**
	 * 查询对应投标记录的某期回款计划
	 * @param loanTraceCode	查询投标记录
	 * @param period	查询期数
	 * @return
	 */
	public LoanRepayment getByLoanTrace(String loanTraceCode, int period) {
		return LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, period);
	}
	
	/**
	 * 查询剩余未还回款计划
	 * @param loanTraceCode	查询投标记录
	 * @return
	 */
	public List<LoanRepayment> findRemaindByLoanTrace(String loanTraceCode) {
		return LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTraceCode);
	}
	
	/**
	 * 保存回款信息
	 * @param loanTraceCode	投标记录
	 * @param period	回款期数
	 * @param repaymentPrincipal	回款本金
	 * @param repaymentInterest	回款利息
	 * @param interestFee	利息手续费
	 * @param overdueDays	逾期天数
	 * @param overdueInterest	逾期利息
	 * @return
	 */
	public boolean repayment(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest) {
		LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, period);
		if (loanRepayment != null) {
			loanRepayment.set("recyStatus", 1);
			loanRepayment.set("repaymentYesAmount", repaymentPrincipal + repaymentInterest);
			loanRepayment.set("repaymentYesPrincipal", repaymentPrincipal);
			loanRepayment.set("repaymentYesInterest", repaymentInterest);
			loanRepayment.set("yesInterestFee", interestFee);
			loanRepayment.set("repaymentYesDate", DateUtil.getNowDate());
			loanRepayment.set("overdueDays", overdueDays);
			loanRepayment.set("overdueInterest", overdueInterest);
			return loanRepayment.update();
		}
		return false;
	}
	
	/**
	 * 保存回款信息
	 * @param loanTraceCode	投标记录
	 * @param period	回款期数
	 * @param repaymentPrincipal	回款本金
	 * @param repaymentInterest	回款利息
	 * @param interestFee	利息手续费
	 * @param overdueDays	逾期天数
	 * @param overdueInterest	逾期利息
	 * @param repaymentDate	回款日期 yyyyMMdd
	 * @return
	 */
	public boolean repayment(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest, String repaymentDate) {
		LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, period);
		if (loanRepayment != null) {
			loanRepayment.set("recyStatus", 1);
			loanRepayment.set("repaymentYesAmount", repaymentPrincipal + repaymentInterest);
			loanRepayment.set("repaymentYesPrincipal", repaymentPrincipal);
			loanRepayment.set("repaymentYesInterest", repaymentInterest);
			loanRepayment.set("yesInterestFee", interestFee);
			loanRepayment.set("repaymentYesDate", repaymentDate);
			loanRepayment.set("overdueDays", overdueDays);
			loanRepayment.set("overdueInterest", overdueInterest);
			return loanRepayment.update();
		}
		return false;
	}
	
	/**
	 * 提前回款
	 * @param loanTraceCode
	 * @param period
	 * @param repaymentPrincipal
	 * @param repaymentInterest
	 * @param interestFee
	 * @param overdueDays
	 * @param overdueInterest
	 * @return
	 */
	public boolean advRepayment(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest) {
		if (repayment(loanTraceCode, period, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest)) {
			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0 AND recyPeriod > ?", loanTraceCode, period);
			for (LoanRepayment loanRepayment : lstLoanRepayment) {
				if (!repayment(loanTraceCode, loanRepayment.getInt("recyPeriod"), 0, 0, 0, 0, 0)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 债转变更回款对象信息
	 * @param loanTraceCode	投标记录
	 * @param gotUserCode	承接人编号
	 * @param gotUserName	承接人用户名
	 */
	public void update4Transfer(String loanTraceCode, String gotUserCode, String gotUserName) {
		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTraceCode);
		for (LoanRepayment loanRepayment : lstLoanRepayment) {
			loanRepayment.set("userCode", gotUserCode);
			loanRepayment.set("userName", gotUserName);
			loanRepayment.update();
		}
	}
	
	/**
	 * 债转变更回款对象信息
	 * @param loanTraceCode	投标记录
	 * @param gotUserCode	承接人编号
	 * @param gotUserName	承接人用户名
	 * @param period	变更期数
	 */
	public void update4Transfer(String loanTraceCode, String gotUserCode, String gotUserName, int period) {
		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0 AND recyPeriod = ?", loanTraceCode, period);
		for (LoanRepayment loanRepayment : lstLoanRepayment) {
			loanRepayment.set("userCode", gotUserCode);
			loanRepayment.set("userName", gotUserName);
			loanRepayment.update();
		}
	}
	
	public List<LoanRepayment> find(String userCode, String repaymentDate) {
		return LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE userCode = ? AND repaymentDate = ?", userCode, repaymentDate);
	}
	
	public long queryRemaindPrincipal(String loanTraceCode) {
		return Db.queryLong("SELECT SUM(repaymentPrincipal) FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTraceCode);
	}
	
	/**
	 * 查询用户指定时间内的回款金额
	 * @param userCode	用户编码
	 * @param startDate	查询开始时间
	 * @param endDate	查询结束时间
	 * @param recyState	回款状态 0-未还款    1-已还款
	 * @return
	 */
	public long queryRealRepaymentAmount(String userCode, String startDate, String endDate, int recyStatus){
		return Db.queryBigDecimal("select COALESCE(sum(repaymentYesAmount - yesInterestFee),0) from t_loan_repayment where userCode = ? and recyStatus = ? and repaymentYesDate between ? and ?", userCode, recyStatus, startDate, endDate).longValue();
	}

	/**
	 * 保存失败回款信息 WJW
	 * @param loanTraceCode	投标记录
	 * @param period	回款期数
	 * @param repaymentPrincipal	回款本金
	 * @param repaymentInterest	回款利息
	 * @param interestFee	利息手续费
	 * @param overdueDays	逾期天数
	 * @param overdueInterest	逾期利息
	 * @param repaymentDate	回款日期 yyyyMMdd
	 * @return
	 */
	public boolean repaymentFail(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest, String repaymentDate) {
		LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, period);
		if (loanRepayment != null) {
			loanRepayment.set("recyStatus", -1);
			loanRepayment.set("repaymentYesAmount", repaymentPrincipal + repaymentInterest);
			loanRepayment.set("repaymentYesPrincipal", repaymentPrincipal);
			loanRepayment.set("repaymentYesInterest", repaymentInterest);
			loanRepayment.set("yesInterestFee", interestFee);
			loanRepayment.set("repaymentYesDate", repaymentDate);
			loanRepayment.set("overdueDays", overdueDays);
			loanRepayment.set("overdueInterest", overdueInterest);
			return loanRepayment.update();
		}
		return false;
	}
	
	/**
	 * 保存失败回款信息 WJW
	 * @param loanTraceCode	投标记录
	 * @param period	回款期数
	 * @param repaymentPrincipal	回款本金
	 * @param repaymentInterest	回款利息
	 * @param interestFee	利息手续费
	 * @param overdueDays	逾期天数
	 * @param overdueInterest	逾期利息
	 * @return
	 */
	public boolean repaymentFail(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest) {
		LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, period);
		if (loanRepayment != null) {
			loanRepayment.set("recyStatus", -1);
			loanRepayment.set("repaymentYesAmount", repaymentPrincipal + repaymentInterest);
			loanRepayment.set("repaymentYesPrincipal", repaymentPrincipal);
			loanRepayment.set("repaymentYesInterest", repaymentInterest);
			loanRepayment.set("yesInterestFee", interestFee);
			loanRepayment.set("repaymentYesDate", DateUtil.getNowDate());
			loanRepayment.set("overdueDays", overdueDays);
			loanRepayment.set("overdueInterest", overdueInterest);
			return loanRepayment.update();
		}
		return false;
	}
	
	/**
	 * 保存失败提前回款信息 WJW
	 * @param loanTraceCode
	 * @param period
	 * @param repaymentPrincipal
	 * @param repaymentInterest
	 * @param interestFee
	 * @param overdueDays
	 * @param overdueInterest
	 * @return
	 */
	public boolean advRepaymentFail(String loanTraceCode, int period, long repaymentPrincipal, long repaymentInterest, long interestFee, int overdueDays, long overdueInterest) {
		if (repayment(loanTraceCode, period, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest)) {
			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0 AND recyPeriod > ?", loanTraceCode, period);
			for (LoanRepayment loanRepayment : lstLoanRepayment) {
				if (!repaymentFail(loanTraceCode, loanRepayment.getInt("recyPeriod"), 0, 0, 0, 0, 0)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * 更改还款状态 WJW
	 * @param recyStatus	还款状态
	 * @param traceCode		投标流水号
	 * @param recyPeriod	还款期数
	 * @return
	 */
	public boolean updateRecyStatus(int recyStatus,String traceCode,int recyPeriod){
		String sql = "update t_loan_repayment set recyStatus = ? where loanTraceCode=? and recyPeriod = ?";
		return Db.update(sql, recyStatus,traceCode,recyPeriod) > 0;
	}

	/**
	 * 根据原定还款日期查询还款计划表 WJW
	 * @param repaymentDate
	 * @return
	 */
	public List<LoanRepayment> queryByRepaymentDate(String repaymentDate){
		return LoanRepayment.loanRepaymentDao.find("select * from t_loan_repayment where repaymentDate=?",repaymentDate);
	}
	
	/**
	 * 根据实际还款日期查询还款计划表 WJW
	 * @param repaymentYesDate
	 * @return
	 */
	public List<LoanRepayment> queryByRepaymentYesDate(String repaymentYesDate){
		return LoanRepayment.loanRepaymentDao.find("select * from t_loan_repayment where repaymentYesDate=?",repaymentYesDate);
	}
	
	/**
	 * 根据loanCode查询还款计划表 WJW
	 * @param loanCode
	 * @return
	 */
	public List<LoanRepayment> queryByLoanCode(String loanCode){
		String sql = "select * from t_loan_repayment where loanCode=?";
		return LoanRepayment.loanRepaymentDao.find(sql,loanCode);
	}
	
	/**
	 * 债权转让，更新回款计划
	 * @param transCode	债转流水号
	 * @param gotUserCode	承接用户编号
	 * @param gotUserName	承接用户用户名
	 * @return
	 */
	public boolean transfer(String transCode, String gotUserCode, String gotUserName) {
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
		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTraceCode);
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
	
	/**
	 * 根据投标流水生成回款计划
	 * @param loanTraceCode	投标流水号
	 */
	private void genRepaymentPlanByLoanTrace(String loanTraceCode) {
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTraceCode);
		List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", loanTraceCode);	// 根据投标记录查询回款计划是否已生成
		if (lstLoanRepayment.size() <= 0) {
			LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanTrace.getStr("loanCode"));
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
					User user = User.userDao.findById(payUserCode);
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
							Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1", tmpRiskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
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
				para.put("loanPeriod", loanInfo.get("loanTimeLimit"));
				para.put("recyStatus", 0);
				para.put("repaymentAmount", repaymentPrincipal + repaymentInterest);
				para.put("repaymentPrincipal", repaymentPrincipal);
				para.put("repaymentInterest", repaymentInterest);
				para.put("interestFee", interestFee);
				para.put("repaymentDate", DateUtil.addMonth(loanInfo.getStr("effectDate"), i));
				para.put("loanTraceCode", loanTrace.get("traceCode"));
				save(para);
			}
		}
	}
	
	private void fillHistoryRepaymentPlan(String loanTraceCode) {
		System.out.println("start fill [" + loanTraceCode + "] history repayment plan..." + DateUtil.getNowDateTime());
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTraceCode);
		LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanTrace.getStr("loanCode"));
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
				repayment(loanTraceCode, recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
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
			update4Transfer(loanTrace.getStr("traceCode"), userCode, loanTransfer.getStr("payUserName"), recyLimit);
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
			
			repayment(loanTrace.getStr("traceCode"), recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
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