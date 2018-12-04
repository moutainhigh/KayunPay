package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.LoanOverdue;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class LoanOverdueService extends BaseService {
	
	private static final String basic_selectFields = " overdueCode,loanNo,loanCode,loanUserCode,loanUserName,loanAmount,loanTitle,loanTimeLimit,repayIndex,principal,interest,overdueAmount,disposeStatus,disposeDateTime,refundType,overdueDate,overdueTime,remark ";
	
	/**
	 * 增加逾期记录
	 *  @param para		map集合，属性包括以下：<br>
	 *  loanCode			贷款标编码<br>
	 *  loanUserCode		借款人用户编码<br>
	 *  loanUserName		借款人昵称<br>
	 *  loanAmount			贷款金额<br>
	 *  loanTitle			贷款标题<br>
	 *  loanTimeLimit		贷款期限<br>
	 *  repayIndex			当前是还第几期<br>
	 *  principal			这一期应付本金<br>
	 *  interest			这一期应付利息<br>
	 *  refundType			还款方式
	 * @return
	 */
	public boolean save(Map<String,Object> para){
		LoanOverdue overdueTrace = new LoanOverdue();
		overdueTrace._setAttrs(para);
		overdueTrace.set("overdueCode", UIDUtil.generate());
		overdueTrace.set("disposeStatus", "n");
		overdueTrace.set("disposeDateTime", "");
//		overdueTrace.set("overdueDate", DateUtil.getNowDate());
//		overdueTrace.set("overdueTime", DateUtil.getNowTime());
		return overdueTrace.save();
	}
	
	/**
	 * 分页查询逾期记录
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param disposeStatus
	 * @param allkey
	 * @return
	 */
	public Page<LoanOverdue> findByPage(Integer pageNumber, Integer pageSize, String beginDate, String endDate, String disposeStatus, String allkey){
		String sqlSelect = "select "+basic_selectFields;
		String sqlFrom = "from t_loan_overdue ";
		String sqlOrder = " order by overdueDate desc,disposeDateTime desc";

		StringBuffer buff = new StringBuffer("");
		List<Object> ps = new ArrayList<Object>();
		
		String[] keys = new String[]{"loanUserName","loanTitle","loanNo"};
		makeExp4AnyLike(buff, ps, keys, allkey, "and","or");
		
		makeExp(buff, ps, "overdueDate", ">=" , beginDate , "and");
		makeExp(buff, ps, "overdueDate", "<=" , endDate , "and");
		makeExp(buff, ps, "disposeStatus", "=" , disposeStatus , "and");
		

		return LoanOverdue.overdueTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder , ps.toArray()) ;
	}
	/**
	 * 查询单个逾期记录明细
	 * @param transCode
	 * @return
	 */
	public LoanOverdue findById(String overdueCode){
		LoanOverdue overdueTrace = LoanOverdue.overdueTraceDao.findById(overdueCode);
		return overdueTrace;
	}

	/**
	 * 查询逾期列表
	 * @param pageNumber
	 * @param pageSize
	 * @param type 0 90天以上   1  90天以内
	 * @return
	 */
	public Page<LoanOverdue> queryOverdueTrace30(Integer pageNumber,
			Integer pageSize, Integer type) {
		String date = DateUtil.delDay(DateUtil.getNowDate(), 90);
		String param = "";
		if(type != null && type == 1){
			param = ">";
		}else{
			param = "<=";
		}
		
		Page<LoanOverdue> loanOverdues = LoanOverdue.overdueTraceDao.paginate(pageNumber, pageSize, 
				"select * ", "from t_loan_overdue where disposeStatus = 'n' and overdueDate " + param + " ? order by overdueDate desc,overdueTime desc" , date );

		return loanOverdues;
	}

	/**
	 * 统计逾期总额
	 * @return
	 */
	public Long totalOverdueAmount(){
		String date = DateUtil.delDay(DateUtil.getNowDate(), 90);
		String sql = "select COALESCE(sum(overdueAmount),0) from t_loan_overdue where overdueDate > ? ";
		return Db.queryBigDecimal(sql, date).longValue();
	}
	
	/**
	 * 统计逾期 坏账总额
	 * @return
	 */
	public Long totalBadAmount(){
		String date = DateUtil.delDay(DateUtil.getNowDate(), 90);
		String sql = "select COALESCE(sum(overdueAmount),0) from t_loan_overdue where disposeStatus = 'n' and overdueDate <= ? ";
		return Db.queryBigDecimal(sql, date).longValue();
	}
	
	
	/**
	 * 处理逾期
	 * @param overdueCode
	 * @param disposeStatus
	 * @param remark
	 */
	public boolean disposeOverdue(String overdueCode, String disposeStatus,
			String remark) {
		
		return Db.update("update t_loan_overdue set disposeStatus = ? , remark = ? where overdueCode = ?", disposeStatus,remark,overdueCode) > 0;
		
	}
	
	
	/**
	 * 删除逾期
	 * @param overdueCode
	 * @param remark
	 */
	public boolean deleteOverdue(String overdueCode) {
		return LoanOverdue.overdueTraceDao.deleteById(overdueCode);
	}
	
	/**
	 * 查询指定日待还逾期流水
	 * */
	public List<LoanOverdue> findOverdue4Date(String date){
		return LoanOverdue.overdueTraceDao.find("select * from t_loan_overdue where disposeStatus = 'n' and DATE_FORMAT(disposeDateTime,'%Y%m%d') = ? group by loanCode",date);
	}
	
	/**
	 * 根据还款状态查询还款日期之前未处理逾期还款标编码 WJW
	 * @param endDate
	 * @return
	 */
	public List<String> queryBydisposeStatusAndEndDate(String disposeStatus,String endDate){
		String sql = "select DISTINCT loanCode from t_loan_overdue where disposeStatus=? and DATE_FORMAT(disposeDateTime,'%Y%m%d')<=?";
		return Db.query(sql,disposeStatus,endDate);
	}
	
	/**
	 * 根据逾期处理状态及时间查询逾期流水 WJW
	 * @param disposeStatus	y:已处理,n:未处理
	 * @param disposeDateTime	逾期设置时间
	 * @return
	 */
	public List<LoanOverdue> queryByStatusAndTime(String disposeStatus,String disposeDateTime){
		return LoanOverdue.overdueTraceDao.find("select * from t_loan_overdue where disposeStatus = ? and disposeDateTime = ?",disposeStatus,disposeDateTime);
	}
	
	/**
	 * 根据逾期处理状态及时间查询最后一期逾期记录 WJW
	 * @param disposeStatus	y:已处理,n:未处理
	 * @param disposeDateTime	逾期设置时间
	 * @return
	 */
	public List<LoanOverdue> queryLastRepayIndexByStatusAndTime(String disposeStatus,String disposeDateTime){
		return LoanOverdue.overdueTraceDao.find("select * from (select * from t_loan_overdue where disposeStatus=? and disposeDateTime=? order by repayIndex desc) t1 group by loanCode",disposeStatus,disposeDateTime);
	}
	
	/**
	 * 查询单个标逾期流水
	 * */
	public List<LoanOverdue> findByLoanCode(String loanCode,String disposeStatus,String disposeDate){
		String sql = "select * from t_loan_overdue where loanCode = ? ";
		if(!StringUtil.isBlank(disposeStatus)){
			sql += " and disposeStatus = '"+disposeStatus+"' ";
		}
		if(!StringUtil.isBlank(disposeDate)){
			sql += " and DATE_FORMAT(disposeDateTime,'%Y%m%d') = "+disposeDate;
		}
		sql += " order by overdueDate asc";
		return  LoanOverdue.overdueTraceDao.find(sql,loanCode);
	}
	
	/**
	 * 未处理逾期标修改逾期状态
	 * */
	public boolean updateStatus(String loanCode, String disposeStatus,String remark){
		return Db.update("update t_loan_overdue set disposeStatus = ? , remark = ? where loanCode = ? and disposeStatus = 'n'",disposeStatus,remark,loanCode)>0;
	}
	
	/**
	 * 修改逾期待还时间
	 * */
	public boolean updateDisposeDate(String loanCode, String disposeDate,String remark){
		return Db.update("update t_loan_overdue set disposeDateTime = ? , remark = ? where loanCode = ? and disposeStatus = 'n'",disposeDate,remark,loanCode)>0;
	}
	
	/**
	 * 根据取消逾期日期查询逾期列表 WJW
	 * @param disposeDate
	 * @return
	 */
	public List<LoanOverdue> queryByDisposeDate(String disposeDate){
		String sql = "select * from t_loan_overdue where DATE_FORMAT(disposeDateTime,'%Y%m%d')=?";
		return LoanOverdue.overdueTraceDao.find(sql,disposeDate);
	}
	
	/**
	 * 逾期中列表
	 * */
	public List<LoanOverdue> queryOverdueListIng(){
		String sql = "SELECT aa.*,SUM(aa.overdueAmount) sumOverdueAmount,COUNT(aa.loanCode) overdueCount ";
			sql += " from (SELECT t1.loanCode,t1.loanNo ,t1.loanUserName ,t1.loanAmount ,t1.loanTitle ,t1.loanTimeLimit ,repayIndex ,overdueAmount ,overdueDate ,loanArea ";
			sql += " from t_loan_overdue t1 LEFT JOIN t_loan_info t2 on t1.loanCode = t2.loanCode where overdueDate > 20180815 and (disposeDateTime is null or disposeDateTime = '') and disposeStatus = 'n' ORDER BY CONCAT(overdueDate,overdueTime) ) aa ";
			sql += " GROUP BY loanCode ORDER BY overdueDate";
		return LoanOverdue.overdueTraceDao.find(sql);
	}
	
	/**
	 * 取消逾期列表
	 */
	public List<LoanOverdue> queryCancelOverdueLoan(String date){
		
		String sql = " select t1.*,t2.loanAmount loanAmount,t2.loanArea from (select t.loanCode,t.loanNo,t.loanUserName,t.loanTitle,t.loanTimeLimit,t.repayIndex,sum(t.principal) sumprincipal,count(1) num1 from (select * from t_loan_overdue where disposeStatus='n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')= ? order by repayIndex) t group by t.loanCode) t1 left join t_loan_info t2 on t1.loanCode=t2.loanCode ";

		return LoanOverdue.overdueTraceDao.find(sql, date);
	}
}




