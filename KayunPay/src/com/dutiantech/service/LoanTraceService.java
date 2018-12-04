package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.controller.export.TransformDator;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class LoanTraceService extends BaseService {
	
	private static final String basic_selectFields = " payUserCode,loanCode,loanTitle,loanNo,loanTimeLimit,traceCode,loanRecyDate,loanDateTime,loanTitle,"
			+ "loanUserCode,loanUserName,payUserName,refundType,rateByYear,rewardRateByYear,payAmount,loanState,loanRecyCount,loanType,traceState,leftAmount,leftInterest,nextAmount,nextInterest,isTransfer,loanTicket,productType,jxTraceCode,authCode,overdueAmount,overdueInterest";

	
	/**
	 * 根据id查询一条标书流水明细
	 * @param traceCode		流水编码
	 * @return
	 */
	public LoanTrace findById(String traceCode){
		return LoanTrace.loanTraceDao.findById(traceCode);
	}
	
	/**
	 * 根据时间查询投标金额(包含预投标的投资)
	 * @param beginDateTime
	 * @param endDateTime
	 * @return
	 */
	public long countPayAmountByDate1(String beginDateTime, String endDateTime){
		return Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where loanDateTime >= ? and loanDateTime <= ? and loanState in('N','J','O','P','Q')",beginDateTime,endDateTime).longValue();
	}
	
	/**
	 * 根据时间查询投标金额(不包含预投标的投资)
	 * @param beginDateTime
	 * @param endDateTime
	 * @return
	 */
	public long countPayAmountByDate2(String beginDateTime, String endDateTime){
		return Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where loanDateTime >= ? and loanDateTime <= ? and loanState in('N','O','P','Q')",beginDateTime,endDateTime).longValue();
	}
	
	/**
	 * 根据时间查询用户投标金额
	 * @param userCode
	 * @param beginDateTime
	 * @param endDateTime
	 * @return
	 */
	public long countPayAmount4User(String userCode ,String beginDateTime, String endDateTime){
		return Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where payUserCode = ? and  loanDateTime >= ? and loanDateTime <= ? and loanState in('N','O','P','Q')",userCode ,beginDateTime,endDateTime).longValue();
	}
	
	public List<LoanTrace> findAllByLoanCode(String loanCode){
		return LoanTrace.loanTraceDao.find("select "+basic_selectFields + " from t_loan_trace where loanCode = ? order by loanDateTime desc", loanCode);
	}
	public Page<LoanTrace> findPageByLoanCode(String loanCode,Integer pageNumber,Integer pageSize){
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, "select "+basic_selectFields , " from t_loan_trace where loanCode = ? order by loanDateTime desc", loanCode);
	}
	//查询所有loanCode的流水
	public Page<LoanTrace> findPageByLoanCodes(Integer pageNumber, Integer pageSize,List<Object> paras){
		String sqlExceptSelect = "from t_loan_trace where loanCode in(";
		for (int i = 0; i < paras.size(); i++) {
			if(i == (paras.size()-1)){
				sqlExceptSelect += "?";
			}else{
				sqlExceptSelect += "?,";
			}
		}
		sqlExceptSelect += ")";
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, "select "+basic_selectFields , sqlExceptSelect+" order by loanDateTime desc", paras.toArray());
	}
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAllByLoanCodeCoustom(String loanCode,TransformDator trans){
		 List<LoanTrace> loanTrace = LoanTrace.loanTraceDao.find("select "+basic_selectFields + " from t_loan_trace where loanCode = ? order by loanDateTime desc", loanCode);
		 List<Map<String,Object>> listLoanTrace = new ArrayList<Map<String,Object>>();
		 for (int i = 0; i < loanTrace.size(); i++) {
			 Object obj = trans.transform(loanTrace.get(i));
			 listLoanTrace.add((Map<String, Object>) obj);
			}
		 return listLoanTrace;
	}
	/**
	 * 查询需要结算的投标流水(只查已转让的债权流水和还款中的流水)
	 * @param loanCode
	 * @return
	 */
	public List<LoanTrace> findLoanTraceByJieSuan(String loanCode){
		return LoanTrace.loanTraceDao.find("select "+basic_selectFields + " from t_loan_trace where traceState='N' and loanState='N' and loanCode = ? order by loanDateTime desc", loanCode);
	}
	
	/**
	 * 查询佳璐数据需要的投标流水
	 * @param loanCode
	 * @return
	 */
	public List<LoanTrace> findLoanTraceJLSJ(String today){
		return LoanTrace.loanTraceDao.find("select "+basic_selectFields + " from t_loan_trace where traceState='N' and loanState='N' and loanDateTime like ? order by loanDateTime desc", today+'%');
	}
	
	/**
	 * 
	 * @param loanCode
	 * @param userCode
	 * @return
	 * 		count	投标次数
	 * 		totalAmount	投标总金额
	 */
	public Map<String , Long> totalByLoan4user(String loanCode , String userCode){
		String sql = "select count(loanCode),sum(payAmount) from t_loan_trace where loanCode=? and payUserCode=?";
		Object[] result = Db.queryFirst(sql, loanCode , userCode );
		Map<String , Long> resultMap = new HashMap<String , Long>();
		long count = (long) result[0];
		resultMap.put("count", count ) ;
		resultMap.put("totalAmount", (long)0 ) ;
		if( count > 0 ){
			resultMap.put("totalAmount", Long.parseLong(result[1]+"")) ;
		}
		return resultMap ;
	}
	
	/**
	 * 分页查询一个标的投标流水
	 * @param loanCode
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<LoanTrace> findByPage4Settlement(String loanCode,Integer pageNumber,Integer pageSize){
		String sqlSelect = "select *";
		String sqlFrom = "from t_loan_trace ";
		String sqlWhere = " where loanCode = ? and traceState in(?,?)";
		String sqlOrder = " order by uid desc";
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere+sqlOrder,loanCode,SysEnum.traceState.N.val(),SysEnum.traceState.C.val());
	}

	/**
	 * 更新投标流水状态
	 * @param traceCode		投标流水编码
	 * @param traceState	预设定的投标流水状态
	 * @param oTraceState	当前投标流水状态
	 * @return
	 */
	public boolean updateTraceState(String traceCode,String traceState, String oTraceState){
		LoanTrace loanTrace = LoanTrace.loanTraceDao.findByIdLoadColumns(traceCode, "traceCode,traceState");
		if(loanTrace.getStr("traceState").equals(oTraceState)){
			loanTrace.set("traceState", traceState);
			return loanTrace.update();
		}
		return false;
	}
	
	/**
	 * 更新投标流水债权状态
	 * @param traceCode		投标流水编码
	 * @param traceState	预设定的债权状态
	 * @param oTraceState	当前投标债权状态
	 * @return
	 */
	public boolean updateTransferState(String traceCode,String transferState, String oTransferState){
		//LoanTrace loanTrace = LoanTrace.loanTraceDao.findByIdLoadColumns(traceCode, "traceCode,isTransfer");
		//String isTransfer = loanTrace.getStr("isTransfer");
		// 兼容承接的债权再次转让
		/*if("C".equals(transferState)){
			if(transferState.equals(isTransfer) || "B".equals(isTransfer)){
				loanTrace.set("isTransfer", oTransferState);
				return loanTrace.update();
			}
		}
		if(isTransfer.equals(transferState)){
			loanTrace.set("isTransfer", oTransferState);
			return loanTrace.update();
		}*/
		return Db.update("update t_loan_trace set isTransfer = ? where traceCode = ? and (isTransfer=? or isTransfer = 'B')",oTransferState,traceCode,transferState) > 0 ? true : false;
	}
	
	/**
	 * 更新投标流水奖励利率
	 * @param traceCode			投标流水编码
	 * @param rewardRateByYear	需设定的奖励利率
	 * @param oRewardRateByYear	前置奖励利率
	 * @return
	 */
	public boolean updateRewardRateByYear(String traceCode,int rewardRateByYear, int oRewardRateByYear){
		return Db.update("update t_loan_trace set rewardRateByYear = ? where traceCode = ? and rewardRateByYear = ?",rewardRateByYear,traceCode,oRewardRateByYear) > 0 ? true : false;
	}
	
	/**
	 * 分页查询投标流水
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @param loanUserCode		借款人
	 * @param payUserCode		投标人
	 * @param loanCode			标书编码
	 * @return
	 */
	public Page<LoanTrace> findByPage(Integer pageNumber, Integer pageSize, String beginDateTime, String endDateTime,String loanUserCode, String payUserCode, String loanCode, String allkey){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace ";
		String sqlOrder = " order by uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"loanTitle","payUserName","loanUserName"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		
		makeExp(buff, ps, "loanDateTime", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "loanDateTime", "<=" , endDateTime , "and");
		makeExp(buff, ps, "loanUserCode", "=" , loanUserCode , "and");
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		makeExp(buff, ps, "loanCode", "=" , loanCode , "and");
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
	}
	
	
	/**
	 * 查询投标流水(需要加息流水)
	 * @param loanCode			标书编码
	 * @param payUserCode		投标人
	 * @return
	 */
	public LoanTrace queryLoanTrace(String loanCode,String payUserCode){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace ";
		String sqlOrder = " order by loanDateTime desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "loanCode", "=" , loanCode , "and");
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		makeExp(buff, ps, "rewardRateByYear", "=" , "200" , "and");
		Page<LoanTrace> paginate = LoanTrace.loanTraceDao.paginate(1, 1, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		
		if(null != paginate.getList()){
			return paginate.getList().get(0);
		}
		
		return null;
	}
	
	/**
	 * 分页查询投标流水(分享加息的流水)
	 * @param payUserCode		投标人
	 * @return
	 */
	public Page<LoanTrace> queryLoanTraceByPage(Integer pageNumber, Integer pageSize,String payUserCode){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace ";
		String sqlOrder = " order by loanDateTime desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		makeExp(buff, ps, "rewardRateByYear", "=" , "400" , "and");
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	
	
	/**
	 * 分页查询投标流水 返回Map,可用于生成Excel
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @param payUserCode		投标人
	 * @param loanCode			标书编码
	 * @return
	 */
	public Map<String,Object> findByPageJoinUser4Noob(Integer pageNumber, Integer pageSize, String beginDateTime, String endDateTime, String payUserCode){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace ";
		String sqlOrder = " order by uid desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "loanDateTime", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "loanDateTime", "<=" , endDateTime , "and");
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		Page<LoanTrace> pages = LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		return result;
	}
	
	/**
	 * 分页查询投标流水 返回Map,可用于生成Excel
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @param payUserCode		投标人
	 * @param loanCode			标书编码
	 * @return
	 */
	public Map<String,Object> findByPageJoinUser4NoobWithSum(Integer pageNumber, Integer pageSize, String beginDateTime, String endDateTime, String payUserCode){
		String sqlFrom = " from t_loan_trace ";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "loanDateTime", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "loanDateTime", "<=" , endDateTime , "and");
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		Map<String,Object> result = new HashMap<String, Object>();
		
		long count_payAmount = Db.queryBigDecimal("select COALESCE(sum(payAmount),0) "+sqlFrom+makeSql4Where(buff),ps.toArray()).longValue();
		result.put("count_payAmount", count_payAmount);
		return result;
	}
	
	/**
	 * 分页查询投标流水
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @param payUserCode		投标人
	 * @param traceState		状态
	 * @return
	 */
	public Page<LoanTrace> findByPage(Integer pageNumber, Integer pageSize, String beginDateTime, 
			String endDateTime,String payUserCode,String traceState){
	
		String sqlSelect = "select t2.effectDate,t2.releaseDate,t2.backDate,t1.* ";
		String sqlFrom = " from t_loan_trace t1 inner join t_loan_info t2 on t1.loanCode = t2.loanCode ";
		String sqlOrder = " order by t1.loanRecyDate ";
		String compareDateName = "t1.loanRecyDate";
		if(StringUtil.isBlank(traceState)){
			sqlOrder = " order by t1.uid desc ";
			compareDateName = "t1.loanDateTime";
		}
		//已回收投资排序
		if("B".equals(traceState)){
			sqlOrder = " order by t1.loanDateTime desc ";
			compareDateName = "t1.loanDateTime";
		}
		
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, compareDateName, ">=" , beginDateTime , "and");
		makeExp(buff, ps, compareDateName, "<=" , endDateTime , "and");
		makeExp(buff, ps, "t1.payUserCode", "=" , payUserCode , "and");
		String[] traceStates = traceState.split(",");
		makeExp4In(buff, ps, "t1.traceState", traceStates,"and");
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	
	public Page<LoanTrace> findByPage4Overdue(Integer pageNumber, Integer pageSize, String beginDateTime, 
			String endDateTime,String payUserCode,String traceState){
	
		String sqlSelect = "select t2.effectDate,t2.releaseDate,t2.backDate,t1.* ";
		String sqlFrom = " from t_loan_trace t1 inner join t_loan_info t2 on t1.loanCode = t2.loanCode ";
		String sqlOrder = " order by t1.loanRecyDate ";
		String compareDateName = "t1.loanRecyDate";
		if(StringUtil.isBlank(traceState)){
			sqlOrder = " order by t1.uid desc ";
			compareDateName = "t1.loanDateTime";
		}
		//已回收投资排序
		if("B".equals(traceState)){
			sqlOrder = " order by t1.loanDateTime desc ";
			compareDateName = "t1.loanDateTime";
		}
		
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, compareDateName, ">=" , beginDateTime , "and");
		makeExp(buff, ps, compareDateName, "<=" , endDateTime , "and");
		makeExp(buff, ps, "t1.payUserCode", "=" , payUserCode , "and");
		String[] traceStates = traceState.split(",");
		makeExp4In(buff, ps, "t1.traceState", traceStates,"and");
		String overdueSql = " OR (traceState = 'B' AND payUserCode = '" + payUserCode + "' AND t1.loanCode IN (SELECT loanCode FROM t_loan_overdue WHERE disposeStatus = 'n'))";
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+ overdueSql +sqlOrder , ps.toArray()) ;
	}
	
	/**
	 * 分页查询投标流水(返利投)
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @return
	 */
	public Page<LoanTrace> findByPage4flt(Integer pageNumber, Integer pageSize, String beginDateTime,String endDateTime){
	
		String sqlSelect = "select * ";
		String sqlFrom = " from t_loan_trace t1 where t1.traceState in ('B','N','F','G') ";
		//返利投不支持加息券
		sqlFrom+="and (t1.loanTicket not LIKE '%\"type\":\"C\"%' or t1.loanTicket is null) ";
		//返利投只返18月标
		sqlFrom+="and t1.loanTimeLimit=18 ";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "t1.isTransfer", "=" , "C" , "and");
		makeExp(buff, ps, "t1.loanDateTime", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "t1.loanDateTime", "<=" , endDateTime , "and");
//		makeExp(buff, ps, "", "" , "" , "and");
		
		sqlFrom += makeSql4WhereHasWhere(buff) ;
		sqlFrom += " and t1.payUserCode in (SELECT usercode from t_flt_userinfo) ORDER BY loanDateTime";
		
		System.out.println("sql:" + sqlFrom  );
		
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom   , ps.toArray()) ;
	}
	
	/**
	 * 返利投使用
	 * @param userCode
	 * @param loanDateTime
	 * @return
	 */
	public LoanTrace query4flt(String userCode , String loanDateTime){
		String sql = "select * from t_loan_trace where payUserCode = ? and loanDateTime < ? and traceState != 'H' ";
		return LoanTrace.loanTraceDao.findFirst(sql, userCode , loanDateTime);
	}
	
	
	/**
	 * 待赚利息总额和待收本金总额	
	 * @param payUserCode		投标人
	 * @return
	 */
	public Object[] countByPage(String beginDateTime,String endDateTime,String payUserCode){
	
		String sql = "select COALESCE(SUM(nextAmount),0),COALESCE(SUM(nextInterest),0) from t_loan_trace where traceState = 'N' ";
		
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		makeExp(buff, ps, "loanRecyDate", ">=" , beginDateTime , "and");
		makeExp(buff, ps, "loanRecyDate", "<=" , endDateTime , "and");
		
		return Db.queryFirst(sql+makeSql4WhereHasWhere(buff),ps.toArray()) ;

	}
	
	/**
	 * 查询用户有回款的日期
	 * @param payUserCode
	 * @author shiqingsong
	 * @return
	 */
	public List<LoanTrace> queryLoanBackDate(String payUserCode){
		return LoanTrace.loanTraceDao.find("SELECT loanRecyDate FROM t_loan_trace WHERE payUserCode = ? AND loanState = 'N' GROUP BY loanRecyDate", payUserCode);
	}
	
	/**
	 * 根据日期统计用户回款信息
	 * @param date
	 * @param payUserCode
	 * @return
	 */
	public Object[] countByBackDate(String date,String payUserCode){
		
		String sql = "select count(1) ,COALESCE(SUM(nextAmount),0),COALESCE(SUM(nextInterest),0) from t_loan_trace where payUserCode = ? and loanState = 'N' and loanRecyDate = ?";
		
		return Db.queryFirst(sql, payUserCode , date);
	}
	
	/**
	 * 查询用户当月回款笔数
	 * @param payUserCode
	 * @return
	 */
	public long countByBackMonth(String payUserCode){
		
		String date = DateUtil.getNowDate().substring(0, 6);
		
		String sql = "select count(1) from t_loan_trace where payUserCode = ? and loanState = 'N' and loanRecyDate like ?";
		
		return Db.queryLong(sql, payUserCode , date+"%");
	}
	
	
	/**
	 * 分页查询所有投标流水(按投标日期排序)
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param payUserCode		投标人
	 * @return
	 */
	public Page<LoanTrace> findAllByPage(Integer pageNumber, Integer pageSize,String payUserCode){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace ";
		String sqlOrder = " order by loanDateTime desc";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize,
				sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	
	public Page<Record> findAllByPage2(Integer pageNumber, Integer pageSize,String payUserCode){
		String sqlSelect = "select t2.disposeStatus, t1.*";
		String sqlFrom = " from t_loan_trace t1 LEFT JOIN (SELECT loanCode, disposeStatus FROM t_loan_overdue t3 WHERE repayIndex = (SELECT MAX(repayIndex) FROM t_loan_overdue WHERE t3.loanCode = loanCode) GROUP BY loanCode) t2 ON t1.loanCode = t2.loanCode";
		String sqlOrder = " order by loanDateTime desc";
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		return Db.paginate(pageNumber, pageSize,sqlSelect, sqlFrom + makeSql4Where(buff) + sqlOrder , ps.toArray()) ;
	}
	
	/**
	 * 新增投标流水
	 * @param para Map集合
	 * 包含字段如下<br>
	 * loanCode				标书编码<br>
	 * loanNo				标书号<br>
	 * loanTitle			标书标题<br>
	 * loanType				投标方式<br>
	 * payAmount			投标金额<br>
	 * payUserCode			投标人userCode<br>
	 * payUserName			投标人昵称<br>
	 * loanUserCode			借款人userCode<br>
	 * loanUserName			借款人真实姓名<br>
	 * isInterest			本息保障<br>
	 * isAutoLoan			是否自动放款<br>
	 * loanRecyDate			还款日期<br>
	 * loanRecyCount		还款期数<br>
	 * refundType			还款方式<br>
	 * rateByYear			年利率<br>
	 * rewardRateByYear		奖励年利率<br>
	 * traceState			流水状态<br>
	 * loanSate				标状态<br>
	 * 
	 * @return
	 */
	public boolean add(Map<String,Object> para){
		LoanTrace loanTrace = new LoanTrace();
		para.put("traceCode", UIDUtil.generate());
		para.put("loanDateTime", DateUtil.getNowDateTime());
		loanTrace._setAttrs(para);
		return loanTrace.save();
	}
	
	
	/**
	 * 根据用户标识，标书流水状态  统计    返回  总条数   总金额
	 * @param userCode
	 * @param traceState
	 * @return
	 */
	public Object[] countByLoanState(String userCode,String traceState){
		Object[] ps = Db.queryFirst("select count(1) , SUM(payAmount) from t_loan_trace where payUserCode = ? and traceState = ? ",
				userCode,traceState) ;
		return ps;
	}
	
	public long countTraces(String loanCode){
		return Db.queryLong("select count(*) from t_loan_trace where loanCode = ?",loanCode);
	}

	/**
	 * 查询可转让的债权
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<LoanTrace> findCanTransfer(Integer pageNumber,
			Integer pageSize, String userCode) {
		
		String nowDate = DateUtil.delDay(DateUtil.getNowDate(), 10) ;
		
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace where payUserCode = ? and traceState = 'N' and isTransfer != 'A' and loanRecyDate >= ? ";
		sqlFrom += " and loanCode not in (select loanCode from t_loan_overdue where disposeStatus = 'n' group by loanCode) and productType != 'E'";
		String sqlOrder = " order by loanRecyDate desc";
		
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlOrder,userCode,nowDate);
	}
	

	/**
	 * 查询最近投资
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<LoanTrace> queryInvest4Latest(Integer pageNumber,
			Integer pageSize, String userCode) {
		return LoanTrace.loanTraceDao.paginate(pageNumber,pageSize,
				"select * ","from t_loan_trace where payUserCode = ? and loanRecyCount = loanTimeLimit and loanState in('J','N') order by uid desc" , userCode);
	}
	
	/**
	 * 按日期时间统计投标数量
	 * @return
	 */
	public long countByDate(String startDateTime, String endDateTime){
		return Db.queryLong("select count(traceCode) from t_loan_trace where loanState in('J','N','O','P') and loanDateTime >= ? and loanDateTime <=?",startDateTime,endDateTime);
	}
	
	/**
	 * 查询一个月的投标次数 投标总额 平均收益率 活跃积分 
	 * @param payUserCode
	 * @return
	 */
	public Map<String,Object> countTouBiaoAll(String payUserCode){
		Map<String,Object> result = new HashMap<String, Object>();
		long tzje = Db.queryBigDecimal("select sum(payAmount) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode).longValue();
		long tbbs = Db.queryLong("select count(traceCode) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode);
		long tmp_rate = Db.queryBigDecimal("select sum(rateByYear+rewardRateByYear) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode).longValue();
		long pingjun_rate = CommonUtil.yunsuan(tmp_rate+"", tbbs+"", "chu", 0).longValue();
		long userScore = Db.queryLong("select userScore from t_user where userCode = ?",payUserCode);
		
		result.put("tzje", tzje);result.put("tbbs", tbbs);result.put("pingjun_rate", pingjun_rate);result.put("userScore", userScore);
		return result;
	}
	/**
	 * 查询一个用户投资金额和投标笔数
	 * @param payUserCode
	 * @return
	 */
	public long[] coungTouBiaoCiShu(String payUserCode){
		long tzje = Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode).longValue();
		long tbbs = Db.queryLong("select count(traceCode) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode);
		return new long[]{tzje,tbbs};
	}
	
	
	/**
	 * 查询一个用户投标笔数(还款中)
	 * @param payUserCode
	 * @return
	 */
	public Integer countTrace4User(String payUserCode){
		return Db.queryLong("select count(1) from t_loan_trace where payUserCode = ? and loanState = 'N'",payUserCode).intValue();
	}
	
	/**
	 * 按时间区间查询统计用户投资排行
	 * @param beginDateTime
	 * @param endDateTime
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public List<LoanTrace> countToubiaoFromMonth(String beginDateTime,String endDateTime,Integer pageNumber, Integer pageSize){
		pageSize = pageSize * pageNumber;
		pageNumber = (pageNumber - 1) * pageSize;
		List<LoanTrace> result = LoanTrace.loanTraceDao.find("select payUserCode,payUserName,sum(payAmount) as tbje from t_loan_trace where loanDateTime>=? and loanDateTime <=? and (loanState='N' or loanState='O' or loanState='P' or loanState='Q') GROUP BY payUserCode order by sum(payAmount) desc limit ?,?",beginDateTime,endDateTime,pageNumber,pageSize);
		for (int i = 0; i < result.size(); i++) {
			String payUserCode = result.get(i).getStr("payUserCode");
			long tbcs = Db.queryLong("select count(payUserCode) from t_loan_trace where payUserCode = ? and loanDateTime>=? and loanDateTime <=?",payUserCode,beginDateTime,endDateTime);
			result.get(i).put("tbcs",tbcs);
		}
		return result;
	}

	/**
	 * 查询用户第一次有效投标记录
	 * @param userCode
	 * @return
	 */
	public LoanTrace findFirstLoan(String userCode) {
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = " from t_loan_trace where payUserCode = ? and traceState in ('A','B','C','D','N')";
		String sqlOrder = " order by loanDateTime ";
		return LoanTrace.loanTraceDao.findFirst(sqlSelect + sqlFrom + sqlOrder,userCode);
	}

	public long countLoanByAmount(String payUserCode , long amount) {
		return Db.queryLong("select count(1) from t_loan_trace where payUserCode = ? and payAmount < ? and loanState in ('N','O','P','Q')",payUserCode , amount);
	}
	
	public long countLoanByCar(String payUserCode) {
		return Db.queryLong("select count(1) from t_loan_trace where payUserCode = ? and productType in ('A','B') and loanState in ('N','O','P','Q')",payUserCode);
	}
	public long countLoanByHouse(String payUserCode) {
		return Db.queryLong("select count(1) from t_loan_trace where payUserCode = ? and productType = 'C' and loanState in ('N','O','P','Q')",payUserCode);
	}
	
	/**
	 * 查询最大一次投标金额
	 * @param payUserCode
	 * @return
	 */
	public long queryAmount4Max(String payUserCode) {
		return Db.queryLong("select COALESCE(max(payAmount),0) from t_loan_trace where payUserCode = ? and loanState in ('N','O','P','Q')",payUserCode);
	}
	
	
	
	/**
	 * app查询最近投资
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<LoanTrace> appQueryInvest4Latest(Integer pageNumber,
			Integer pageSize, String userCode) {
		return LoanTrace.loanTraceDao.paginate(pageNumber,pageSize,
				"select loanCode,loanTitle,loanNo,payAmount,traceState,loanTimeLimit,rateByYear,rewardRateByYear,loanDateTime,productType ","from t_loan_trace where payUserCode = ? and loanRecyCount = loanTimeLimit and loanState in('J','N') order by uid desc" , userCode);
	}
	
	/**
	 * app查询投标流水
	 * @param pageNumber		第几页
	 * @param pageSize			每页多少条
	 * @param beginDateTime		开始时期时间
	 * @param endDateTime		结束日期时间
	 * @param payUserCode		投标人
	 * @param traceState		状态
	 * @return
	 */
	public Page<LoanTrace> appFindByPage(Integer pageNumber, Integer pageSize
			,String payUserCode,String traceState){
		String sqlSelect = "select t2.backDate,t1.traceCode,t1.loanRecyDate,t1.productType,t1.loanNo,t1.loanCode,t1.loanNo,t1.payAmount,t1.traceState,t1.loanTimeLimit,t1.loanRecyCount,t1.rateByYear,t1.rewardRateByYear,t1.loanDateTime,t1.nextAmount,t1.nextInterest,t1.refundType,t1.loanState";
		String sqlFrom = " from t_loan_trace t1  inner join t_loan_info t2 on t1.loanCode = t2.loanCode ";
		String sqlOrder = " order by t1.loanRecyDate ";
		if("B".equals(traceState)){
			sqlOrder = " order by t1.loanDateTime desc ";
		}
		if("N".equals(traceState)){
			sqlOrder = " order by t1.loanRecyDate asc ";
		}
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "t1.payUserCode", "=" , payUserCode , "and");
		String[] traceStates = traceState.split(",");
		makeExp4In(buff, ps, "t1.traceState", traceStates,"and");
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	/**
	 * 待赚利息总额和待收本金总额	
	 * @param payUserCode 投标人
	 * @return
	 */
	public Object[] appCountByPage(String payUserCode){
	
		String sql = "select COALESCE(SUM(nextAmount),0),COALESCE(SUM(nextInterest),0) from t_loan_trace where traceState = 'N' ";
		
		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		makeExp(buff, ps, "payUserCode", "=" , payUserCode , "and");
		
		
		return Db.queryFirst(sql+makeSql4WhereHasWhere(buff),ps.toArray()) ;

	}
	/**
	 *查询某投资人的金额(投标，现金券) rain 2017.8.25
	 * */
	public List<LoanTrace> userTenderAmount(String beginDateTime,String endDateTime,String userCode){		
		List<LoanTrace> result = LoanTrace.loanTraceDao.find("SELECT  traceCode,isTransfer,payAmount,payUserName,loanTimeLimit,loanCode from t_loan_trace where isTransfer!='B' and loanDateTime>=? and loanDateTime<=? and payUserCode=?",beginDateTime,endDateTime,userCode);
		return result;
	}
	/**
	 *根据userMobile查询某投资人的金额(投标，现金券) rain 2017.8.25
	 * */
	public List<LoanTrace> userAmountByMobile(String beginDateTime,String endDateTime,String mobile){		
		List<LoanTrace> result = LoanTrace.loanTraceDao.find("SELECT  t1.traceCode,t1.isTransfer,t1.payAmount,t1.payUserName,t1.loanTimeLimit,t1.loanCode from t_loan_trace t1 left join t_user t2 on t1.payUserCode=t2.userCode   where t1.isTransfer!='B' and t1.loanDateTime>=? and t1.loanDateTime<=? and t2.userMobile=? ",beginDateTime,endDateTime,mobile);
		return result;
	}
	/**
	 *查询某投资人的金额(债转) rain 2017.8.25
	 
	public List<LoanTransfer> userTransferAmount(String beginDate,String endDate,String userCode){		
		List<LoanTransfer> result = LoanTransfer.loanTransferDao.find("SELECT  payUserCode,payUserName,leftAmount,loanRecyCount,gotUserCode,gotUserName,loanRecyCount  from t_loan_transfer  where gotDate>=? and gotDate<=? and  gotUserCode=? ",beginDate,endDate,userCode);
		return result;
	}
	* */
	
	/**
	 *查询所有投资人的金额(投标，现金券) rain 2017.8.25
	 * */
	public List<LoanTrace> allTenderAmount(String beginDateTime,String endDateTime){		
		List<LoanTrace> result = LoanTrace.loanTraceDao.find("SELECT  payAmount,payUserName,payUserCode,loanTimeLimit,loanCode,isTransfer,traceCode from t_loan_trace where  isTransfer!='B' and loanDateTime>=? and loanDateTime<=? ",beginDateTime,endDateTime);
		return result;
	}
	
	/**
	 * 查询用户所有投资记录 ws 20170914
	 * */
	public List<LoanTrace> queryAllLoanTrace(String userCode,String day){
		String dd=day.substring(6,8);
		String sql="";
		if(!"00".equals(dd)){
			int days = DateUtil.getDaysByYearMonth(day);
			if(days <= Integer.parseInt(dd)){
//				sql=" and i.effectDate REGEXP '("+days;
//				for(int i=days+1;i<=31;i++){
//					sql+="|"+i;
//				}
//				sql+=")$'";
				sql = " AND DATE_FORMAT(t2.effectDate, '%d') >= " + days;
			}else{
//				sql=" and i.effectDate REGEXP '"+dd+"$' ";
				sql = " AND DATE_FORMAT(t2.effectDate, '%d') = " + dd;
			}
		}
//		List<LoanTrace> result=LoanTrace.loanTraceDao.find("select t.*,i.effectDate,i.releaseDate,i.benefits4new from t_loan_trace t LEFT JOIN t_loan_info i on t.loanCode=i.loanCode where t.productType != 'E' and t.payUserCode=? and t.loanState in('N','O','P') and i.effectDate>20150228 and i.effectDate<=? "+sql+" order by t.loanDateTime desc",userCode,md);
		
		List<LoanTrace> result = LoanTrace.loanTraceDao.find("SELECT t1.*, t2.effectDate, t2.releaseDate, t2.benefits4new FROM t_loan_trace t1 LEFT JOIN t_loan_info t2 ON t1.loanCode = t2.loanCode "
				+ "WHERE t1.payUserCode = ? AND t1.loanState in('N','O','P') AND t2.effectDate BETWEEN 20150228 AND ? " + sql + " ORDER BY t1.loanDateTime DESC", userCode, day);
		return result;
	}
	
	public List<LoanTrace> queryAllLoanTrace4app(String userCode){
		List<LoanTrace> result=LoanTrace.loanTraceDao.find("select t.*,i.effectDate,i.releaseDate,i.benefits4new from t_loan_trace t LEFT JOIN t_loan_info i on t.loanCode=i.loanCode where t.productType != 'E' and t.payUserCode=? and t.loanState in('N','O','P') and i.effectDate>20150228   order by t.loanDateTime desc",userCode);
		return result;
	}
	/**
	 * 查询时间段内yf结算的所有投标流水
	 * */
	public List<LoanTrace> queryYFloantrace(String begindate,String enddate){
		String sql="SELECT t1.*,t2.effectDate,t2.backDate,t2.reciedCount from t_loan_trace t1 LEFT JOIN t_loan_info t2 on t1.loanCode=t2.loanCode ";
		sql+="where t2.effectDate BETWEEN ? and ? and  t1.loanState in ('N','O','P') and t1.loanTicket like '%\"type\":\"C\"%' and t1.isTransfer != 'B' order by t2.effectDate desc,t2.effectTime desc";
		List<LoanTrace> result=LoanTrace.loanTraceDao.find(sql,begindate,enddate);
		return result;
	}
	
	/**
	 * 查询最后封标的投资记录
	 * @param loanCode 标的编码
	 * @return
	 */
	public LoanTrace queryFullLoanInvestor(String loanCode){
		String sqlSelect = "select payUserName,loanDateTime ";
		String sqlFrom = "from t_loan_trace ";
		String sqlWhere = "where loanCode = ? ORDER BY loanDateTime DESC LIMIT 0,1";
		return LoanTrace.loanTraceDao.findFirst(sqlSelect+sqlFrom+sqlWhere, loanCode);
	}
	
	/**
	 * 查询最新投标记录
	 * */
	public List<LoanTrace> queryNewInvest(int num){
		String sql="select * from t_loan_trace order by loanDateTime desc limit ?";
		return LoanTrace.loanTraceDao.find(sql,num);
	}
	
	
	/**
	 * 查询被推荐人时间段内所有投标流水(含奖券使用信息) WJW
	 * @param userCode 推荐人UserCode
	 * @param beginDate yymmdd
	 * @param endDate	yymmdd
	 * @return
	 */
	public List<LoanTrace> queryTraceByDate(String userCode,String beginDate,String endDate){
		String sql="select payUserCode,payUserName,payAmount,leftInterest,rateByYear,rewardRateByYear,loanTimeLimit,refundType,traceCode,loanTicket,loanRecyCount,loanState from t_loan_trace where payUserCode in (select bUserCode from t_recommend_info where aUserCode=? and bRegDate between ? and ?)";
		List<LoanTrace> loanTraces= LoanTrace.loanTraceDao.find(sql,userCode,beginDate,endDate);
		return loanTraces;
		
	}
	
	/**
	 * 查询被推荐人时间段内所有投标流水(含奖券使用信息)带分页 WJW
	 * @param userCode 推荐人UserCode
	 * @param beginDate yymmdd
	 * @param endDate	yymmdd
	 * @return
	 */
	public Page<LoanTrace> queryTraceByDatePage(String userCode,String beginDate,String endDate,int pageNumber,int pageSize){
		String sqlSelect="select payUserCode,payUserName,payAmount,leftInterest,rateByYear,rewardRateByYear,loanTimeLimit,refundType,traceCode,loanTicket,loanRecyCount,loanState";
		String sqlFrom=" from t_loan_trace where payUserCode in (select bUserCode from t_recommend_info where aUserCode=? and bRegDate between ? and ?)";
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom,userCode,beginDate,endDate);
	}
	
	/**
	 * 查询回款日期在指定时间内的投标流水（被推荐人）TZQ
	 * @param userCode 推荐人编码
	 * @param beginDate
	 * @param endDate
	 * @param cmFirstDay
	 * @param cmLastDay
	 * @return
	 */
	public List<LoanTrace> queryCMTraceByDate(String userCode,String beginDate,String endDate,String cmFirstDay,String cmLastDay){
		String sql="select payUserCode,payUserName,payAmount,leftInterest,rateByYear,rewardRateByYear,loanTimeLimit,refundType,traceCode,loanTicket,loanRecyCount,loanState from t_loan_trace where payUserCode in (select bUserCode from t_recommend_info where aUserCode=? and bRegDate between ? and ?) and loanRecyDate between ? and ?";
		List<LoanTrace> loanTraces= LoanTrace.loanTraceDao.find(sql,userCode,beginDate,endDate,cmFirstDay,cmLastDay);
		return loanTraces;
		
	}

	/**
	 * 查询被推荐人时间段内所有投标流水(含奖券使用信息)TZQ(不带分页)
	 * @param userCode 推荐人UserCode
	 * @param beginDate yymmdd
	 * @param endDate	yymmdd
	 * @return
	 */
	public List<LoanTrace> queryRecommendedTraceByDate(String userCode,String beginDate,String endDate){
		String sql="select payUserCode,payUserName,payAmount,leftInterest,rateByYear,rewardRateByYear,loanTimeLimit,refundType,traceCode,loanTicket,loanRecyCount,loanState"
		+ " from t_loan_trace where payUserCode in (select bUserCode from t_recommend_info where aUserCode=? and bRegDate between ? and ?)";
		return LoanTrace.loanTraceDao.find(sql, userCode, beginDate, endDate);
	}

	/**
	 * 根据昨天待收金额区间查询用户
	 * @param pageNumber
	 * @param pageSize
	 * @param mixAmount	最小待收金额
	 * @param maxAmount	最大待收金额
	 * @return
	 */
	public List<LoanTrace> queryBeRecyAmount(long mixAmount,long maxAmount){
		String sql = "select payUserCode,sum(if(loanState='N',leftAmount+leftInterest,0))+sum(overdueAmount) beRecyAmount from t_loan_trace where DATE_FORMAT(loanDateTime,'%Y%m%d') < '"+DateUtil.getNowDate()+"' group by payUserCode HAVING ";
		String sqlHaving = "";
		if(mixAmount > 0){
			sqlHaving += " beRecyAmount >= " + mixAmount;
		}
		if(maxAmount > 0){
			if(!"".equals(sqlHaving)){
				sqlHaving += " and";
			}
			sqlHaving += " beRecyAmount < " + maxAmount;
		}
		return LoanTrace.loanTraceDao.find(sql+sqlHaving) ;
	}
	
	/**
	 * 查询用户新手标已投资额度
	 * @return
	 */
	public long sumNewAmountByUserCode(String userCode){
		return Db.queryBigDecimal("select COALESCE(sum(payAmount),0) from t_loan_trace where loanCode in (select loanCode from t_loan_info where benefits4new>0) and payUserCode=?", userCode).longValue();
	}

	/** WJW
	 * 根据authCode查询投标记录
	 * @param authCode 授权码
	 * @return
	 */
	public LoanTrace findByAuthCode(String authCode){
		String sql = "select * from t_loan_trace where authCode=?";
		List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find(sql, authCode);
		return loanTraces !=null && loanTraces.size() > 0 ? loanTraces.get(0):null;
	}
	
	/** WJW
	 * 根据jxTraceCode查询投标记录
	 * @param jxTraceCode
	 * @return
	 */
	public LoanTrace findByJxTraceCode(String jxTraceCode){
		String sql = "select * from t_loan_trace where jxTraceCode=?";
		List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find(sql, jxTraceCode);
		return loanTraces !=null && loanTraces.size() > 0 ? loanTraces.get(0):null;
	}
	
	/**
	 * 更改授权码	WJW
	 * @param authCode	授权码
	 * @param traceCode	投标流水号
	 * @return
	 */
	public boolean updateAuthCode(String authCode,String traceCode){
		String sql = "update t_loan_trace set authCode=? where traceCode=?";
		return Db.update(sql,authCode,traceCode) > 0;
	}
	/**
	 * 提前回款资金异常用户列表
	 * */
	public boolean unusualUserCode(String userCode){
		String sql = "SELECT payUserCode from t_loan_trace where loanCode in(SELECT loanCode from t_settlement_early where earlyDate BETWEEN 20180521 and 20180528 and estatus != 'D') and not isnull(authCode) GROUP BY payUserCode";
		List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find(sql);
		for(LoanTrace loanTrace:loanTraces){
			String userCode2 = loanTrace.getStr("payUserCode");
			if(userCode2.equals(userCode)){
				return true;
			}
		}
		return false;
	}
	/**
	 * 通过老订单号查询标流水信息
	 * */
	public LoanTrace findTrace4OrgOrderId(String orgOrderId){
		return LoanTrace.loanTraceDao.findFirst("select * from t_loan_trace where orderId = ?",orgOrderId);
	}
	
	/**
	 * 根据标号查询有authCode的投标流水 WJW
	 * @param loanCode
	 * @return
	 */
	public List<LoanTrace> queryLoanTraces(String loanCode){
		String sql = "select * from t_loan_trace where loanCode=? and authCode is not null";
		return LoanTrace.loanTraceDao.find(sql,loanCode);
	}
	
	/**
	 * 根据投标流水最小条数查询loanCode及投标次数(authCode不为空) WJW
	 * @param num
	 * @return
	 */
	public List<LoanTrace> queryLoanTraceByLength(int num){
		String sql = "select loanCode,count(1) num from t_loan_trace where authCode is not null group by loanCode HAVING num>?";
		return LoanTrace.loanTraceDao.find(sql,num);
	}

	/**
	 * 根据回款日期查询投标流水 WJW
	 * @param date
	 * @return
	 */
	public List<LoanTrace> queryByLoanRecyDate(String date){
		String sql = "select * from t_loan_trace where loanRecyDate=?";
		return LoanTrace.loanTraceDao.find(sql,date);
	}
	
	/**
	 * 根据回款日期查询标号(authCode不为空) WJW
	 * @param date
	 * @return
	 */
	public List<String> queryLoanCodeByLoanRecyDate(String date){
		String sql = "select loanCode from t_loan_trace where loanRecyDate=? and loanState='N' and authCode is not null group by loanCode";
		return Db.query(sql,date);
	}

	/**
	 * 根据loanCode查询下一期待还本金 WJW
	 * @param loanCode
	 * @return
	 */
	public long sumNextAmount(String loanCode){
		String sql = "select COALESCE(sum(nextAmount),0) from t_loan_trace where loanCode=?";
		return Db.queryBigDecimal(sql,loanCode).longValue();
	}
	
	/**
	 * 根据loanCode查询标剩余待还本金 WJW
	 * @param loanCode
	 * @return
	 */
	public long sumLeftAmount(String loanCode){
		String sql = "select COALESCE(sum(leftAmount),0) from t_loan_trace where loanCode=?";
		return Db.queryBigDecimal(sql,loanCode).longValue();
	}
	
	/**
	 * 根据loanCode查询标逾期金额 WJW
	 * @param loanCode
	 * @return
	 */
	public long sumOverdueAmount(String loanCode){
		String sql = "select COALESCE(sum(overdueAmount),0) from t_loan_trace where loanCode=?";
		return Db.queryBigDecimal(sql,loanCode).longValue();
	}
	
	/**
	 * 根据userCode查询标逾期金额 
	 * @param userCode
	 * @return
	 */
	public long sumOverdueAmountByUserCode(String userCode){
		String sql = "select COALESCE(sum(overdueAmount),0) from t_loan_trace where payUserCode=?";
		return Db.queryBigDecimal(sql,userCode).longValue();
	}
	
	/**
	 * 更新loanTrace逾期未还金额 WJW
	 * @param overdueAmount	逾期金额
	 * @param loanCode
	 * @return
	 */
	public boolean updateOverdueAmount(long overdueAmount,String loanCode){
		String sql = "update t_loan_trace set overdueAmount=? where loanCode=?";
		return Db.update(sql,overdueAmount,loanCode) > 0;
	}
	
	/**
	 * 更新loanTrace逾期时未还利息 WJW
	 * @param overdueInterest	逾期时未还利息
	 * @param loanCode			标号
	 * @return
	 */
	public boolean updateOverdueInterest(long overdueInterest,String loanCode){
		String sql = "update t_loan_trace set overdueInterest=? where loanCode=?";
		return Db.update(sql, overdueInterest,loanCode) > 0;
	}
	
	/**
	 * 查询用户有逾期标
	 * */
	public Page<LoanTrace> queryOverdueList(String userCode,Integer pageNumber,Integer pageSize){
		String sql = "select * ";
		String sqlwhere	= "from t_loan_trace where  payUserCode = ? and loanCode in (select loanCode from t_loan_overdue where disposeStatus = 'n' group by loanCode)";
		return LoanTrace.loanTraceDao.paginate(pageNumber, pageSize,sql,sqlwhere,userCode);
	}
	
	/**
	 * 查询用户所有代待收投标流水
	 * */
	public List<LoanTrace> queryTrace(String userCode){
		String sql = "select * ";
		sql	+= "from t_loan_trace where  payUserCode = ? and  (overdueAmount >0  or traceState = 'N') ";
		return LoanTrace.loanTraceDao.find(sql,userCode);
	}
}