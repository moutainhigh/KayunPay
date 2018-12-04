package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

/**
 * 充值记录
 * @author shiqingsong
 *
 */
public class RechargeTraceService  extends BaseService {
	
	@SuppressWarnings("unused")
	private static final String basic_selectFields = " traceCode,bankTraceCode,traceAmount,bankState,bankRemark,userCode,userName,traceState,rechargeType,traceDateTime,traceDate,modifyDateTime,modifyDate,okDateTime,settleDate,traceRemark,uid ";
	
	
	public RechargeTrace findById(String traceCode){
		return RechargeTrace.rechargeTraceDao.findById(traceCode);
	}
	
	public RechargeTrace findFieldById(String traceCode,String field){
		return RechargeTrace.rechargeTraceDao.findByIdLoadColumns(traceCode, field);
	}
	
	/**
	 * 分页查询充值流水原始记录对应的统计数据,返回Map,可用于生成excel
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param allkey
	 * @param traceState
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSum(String userCode,String beginDate, String endDate, String allkey,String traceState,String sharen, String rechargeType){

		String sqlFrom = " from t_recharge_trace t1 left join t_user t2 on t1.userCode = t2.userCode left join t_funds t3 on t1.userCode = t3.userCode";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.okDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.okDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.traceState", "=", traceState, "and");
		makeExp(buff, paras, "t1.rechargeType", "=", rechargeType, "and");
		makeExp(buff, paras, "t1.traceState", "!=", "Y", "and");
		makeExp(buff, paras, "t1.bankState", "!=", "Y", "and");
		if(!StringUtil.isBlank(sharen)){
			if(sharen.equals("1")){
				makeExp(buff, paras, "t3.loanTotal","<=", 0+"", "and");
			}else if(sharen.equals("0")){
				makeExp(buff, paras, "t3.loanTotal",">", 0+"", "and");
			}
		}
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Map<String,Object> result = new HashMap<String,Object>();
		long count_traceAmount = Db.queryBigDecimal("select COALESCE(sum(traceAmount),0) "+ sqlFrom+(makeSql4Where(buff)).toString(),paras.toArray()).longValue();
		result.put("count_traceAmount", count_traceAmount);
		return result;
	}
	
	/**
	 * 分页查询充值流水原始记录,返回Map,可用于生成excel
	 * @param userCode
	 * @param pageNumber
	 * @param pageSize
	 * @param beginDate
	 * @param endDate
	 * @param allkey
	 * @param traceState
	 * @return
	 */
	public Map<String,Object> findByPage4Noob(String userCode, Integer pageNumber, Integer pageSize, String beginDate, String endDate, String allkey,String traceState, String sharen,String rechargeType){

		String sqlSelect = "select t1.* " ;
		String sqlFrom = " from t_recharge_trace t1 left join t_user t2 on t1.userCode = t2.userCode left join t_funds t3 on t1.userCode = t3.userCode";
		String sqlOrder = " order by t1.uid desc ";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.okDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.okDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.traceState", "=", traceState, "and");
		makeExp(buff, paras, "t1.rechargeType", "=", rechargeType, "and");
		makeExp(buff, paras, "t1.traceState", "!=", "Y", "and");
		makeExp(buff, paras, "t1.bankState", "!=", "Y", "and");
		if(!StringUtil.isBlank(sharen)){
			if(sharen.equals("1")){
				makeExp(buff, paras, "t3.loanTotal","<=", 0+"", "and");
			}else if(sharen.equals("0")){
				makeExp(buff, paras, "t3.loanTotal",">", 0+"", "and");
			}
		}
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<RechargeTrace> pages = RechargeTrace.rechargeTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		Map<String,Object> result = new HashMap<String,Object>();
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
	 * 查询充值记录
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * @param beginDate		开始时间<br>
	 * @param endDate		结束时间<br>
	 * @return
	 */
	public Map<String,Object> queryRechargeTrace4User(String userCode,
			Integer pageNumber, Integer pageSize, String beginDate, String endDate , String traceState, String allkey) {
		String sqlSelect = "select t1.* " ;
		String sqlFrom = " from t_recharge_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.uid desc ";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.traceState", "=", traceState, "and");
		makeExp(buff, paras, "t1.traceDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.traceDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.traceState", "!=", "Y", "and");
		makeExp(buff, paras, "t1.bankState", "!=", "Y", "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName"};
		
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<RechargeTrace> pages = RechargeTrace.rechargeTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		
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
	 * app查询充值记录
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * @param beginDate		开始时间<br>
	 * @param endDate		结束时间<br>
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map<String,Object> appQueryRechargeTrace4User(String userCode,
			Integer pageNumber, Integer pageSize, String beginDate, String endDate , String traceState, String allkey) {
		String sqlSelect = "select t1.* " ;
		String sqlFrom = " from t_recharge_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by t1.uid desc ";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.traceState", "=", traceState, "and");
		makeExp(buff, paras, "t1.traceDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.traceDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "t1.traceState", "!=", "Y", "and");
		makeExp(buff, paras, "t1.bankState", "!=", "Y", "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName"};
		
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<RechargeTrace> pages = RechargeTrace.rechargeTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		ArrayList list=(ArrayList) pages.getList();
		for (int i = 0; i <list.size(); i++) {
			RechargeTrace rechargeTrace=(RechargeTrace)list.get(i);
			String traceDateTime=rechargeTrace.getStr("traceDateTime");
			 String traceState1= rechargeTrace.getStr("traceState");
			String traceDateTimeFormat=DateUtil.parseDateTime(DateUtil.getDateFromString(traceDateTime, "yyyyMMddHHmmss"),
						"yyyy/MM/dd HH:mm:ss");
			 
			 rechargeTrace.put("traceDateTime", traceDateTimeFormat);
			 switch (traceState1) {
			case "A":
				rechargeTrace.put("traceStateDesc", "充值中");
				break;
	        case "B":
	        	rechargeTrace.put("traceStateDesc", "充值成功");
				break;
	         case "C":
	        	 rechargeTrace.put("traceStateDesc", "充值失败");
		   break;

			default:
				break;
			}
			 result.put("list", pages.getList());
		}
		System.out.println(pages.getList());
		
		return result;

	}
	
	/**
	 * 统计充值总额
	 * @return
	 */
	public long sumRechargeTrace4User(String userCode,String beginDate, String endDate , String traceState) {
		String sqlSelect = "select COALESCE(sum(traceAmount),0) " ;
		String sqlFrom = " from t_recharge_trace ";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "userCode", "=", userCode, "and");
		makeExp(buff, paras, "traceState", "=", traceState, "and");
		makeExp(buff, paras, "traceDateTime", ">=", beginDate, "and");
		makeExp(buff, paras, "traceDateTime", "<=", endDate, "and");
		makeExp(buff, paras, "traceState", "!=", "Y", "and");
		makeExp(buff, paras, "bankState", "!=", "Y", "and");
		
		return Db.queryBigDecimal(sqlSelect+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();

	}
	
	/**
	 * 初始化充值记录基本信息<br>
	 * @param traceCode			支付订单流水号
	 * @param bankTraceCode		支付接口的流水号<br>
	 * @param traceAmount		充值金额<br>
	 * @param bankState			A – 已受理 B – 充值成功 C – 充值失败D – 充值回退 E – 其他<br>
	 * @param bankRemark		交易接口信息备注<br>
	 * @param userCode			用户userCode<br>
	 * @param userBankNo		充值银行卡卡号<br>
	 * @param userBankCode		银行卡编号(Tbank)<br>
	 * @param userBankName		银行名字<br>
	 * @param traceState		A – 充值中B – 充值成功C – 充值失败<br>
	 * @param rechargeType		SYS本系统，LL 连连支付<br>
	 * @param traceRemark		系统备注<br>
	 * @return
	 */
	public boolean saveRechargeTrace(String traceCode,String bankTraceCode,long traceAmount,String bankState,String bankRemark,String userCode,String userBankNo,String userBankCode,String userBankName,String traceState,String rechargeType,String traceRemark){
		String userName = User.userDao.findByIdLoadColumns(userCode, "userName").getStr("userName");
		String userTrueName = UserInfo.userInfoDao.findByIdLoadColumns(userCode, "userCardName").getStr("userCardName");
		
		RechargeTrace rt = new RechargeTrace();
		if(StringUtil.isBlank(traceCode)){
			rt.set("traceCode", UIDUtil.generate());
		}else{
			rt.set("traceCode", traceCode);
		}
		rt.set("bankTraceCode", bankTraceCode);
		rt.set("traceAmount", traceAmount);
		rt.set("bankState", bankState);
		rt.set("bankRemark", bankRemark);
		rt.set("userCode", userCode);
		rt.set("userName", userName);
		rt.set("userTrueName", userTrueName);
		rt.set("userBankNo", userBankNo);
		rt.set("userBankCode", userBankCode);
		rt.set("userBankName", userBankName);
		rt.set("traceState", traceState);
		rt.set("rechargeType", rechargeType);
		rt.set("traceDateTime", DateUtil.getNowDateTime());
		rt.set("traceDate", DateUtil.getNowDate());
		rt.set("modifyDateTime", DateUtil.getNowDateTime());
		rt.set("modifyDate", DateUtil.getNowDate());
		rt.set("traceRemark", traceRemark);
		return rt.save();
	}
	
	public boolean updateBankTraceCode(String traceCode, String bankTraceCode){
		return Db.update("update t_recharge_trace set bankTraceCode = ? where traceCode = ?",bankTraceCode,traceCode) > 0;
	}
	
	/**
	 * 根据充值方式，统计某段时间充值总额
	 * @param rt
	 * @return
	 */
	public long countAmountByRechargeType(RechargeTrace.RECHARGE_TYPE rt, String beginDateTime, String endDateTime){
		return Db.queryBigDecimal("select COALESCE(SUM(traceAmount),0) from t_recharge_trace where rechargeType = ? and bankState = 'B' and traceState = 'B' and okDateTime >= ? and okDateTime <= ?",rt.key(),beginDateTime, endDateTime).longValue();
	}
	/**
	 * app查询充值记录
	 * @param userCode		用户标识<br>
	 * @param pageNumber	页数<br>
	 * @param pageSize		每页条数<br>
	 * @return
	 */
	public Map<String,Object> appQueryRecharge(String userCode,
			Integer pageNumber, Integer pageSize, String allkey) {
		String sqlSelect = "select t1.* " ;
		String sqlFrom = " from t_recharge_trace t1 left join t_user t2 on t1.userCode = t2.userCode";
		String sqlOrder = " order by uid desc ";
		
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.traceState", "!=", "Y", "and");
		makeExp(buff, paras, "t1.bankState", "!=", "Y", "and");
		
		String[] keys = new String[]{"t1.userName","t1.userTrueName"};
		
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<RechargeTrace> pages = RechargeTrace.rechargeTraceDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString() + sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("pageNumber", pages.getPageNumber());
		result.put("pageSize", pages.getPageSize());
		result.put("totalPage", pages.getTotalPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("list", pages.getList());
		
		 if(0==pages.getList().size()){
			 
			 result.put("tag", "0") ;
		 }
		 else{
			 
			 result.put("tag", "1") ; 
		 }
		@SuppressWarnings("rawtypes")
		ArrayList list=(ArrayList) pages.getList();
		for (int i = 0; i <list.size(); i++) {
			RechargeTrace rechargeTrace=(RechargeTrace)list.get(i);
			String traceDateTime=rechargeTrace.getStr("traceDateTime");
			String traceAmount=Number.longToString(rechargeTrace.getLong("traceAmount"));
			 rechargeTrace.put("traceAmount",traceAmount);
			 String traceState= rechargeTrace.getStr("traceState");
			String traceDateTimeFormat=DateUtil.parseDateTime(DateUtil.getDateFromString(traceDateTime, "yyyyMMddHHmmss"),
						"yyyy/MM/dd HH:mm:ss");
			 
			 rechargeTrace.put("traceDateTime", traceDateTimeFormat);
			 switch (traceState) {
			case "A":
				rechargeTrace.put("traceStateDesc", "充值中");
				break;
	        case "B":
	        	rechargeTrace.put("traceStateDesc", "充值成功");
				break;
	         case "C":
	        	 rechargeTrace.put("traceStateDesc", "充值失败");
		   break;
		   
			default:
				break;
			}
			 result.put("list", pages.getList());
		}
		
		return result;

	}
	/**
	 * 根据充值状态查询指定时间内的充值流水
	 * @param states
	 * @return
	 */
	public List<RechargeTrace> findByStatesAndTime(String state, String startTime, String endTime){
		String[] states = state.split(",");
		return RechargeTrace.rechargeTraceDao.find("SELECT * FROM t_recharge_trace WHERE (traceState = ? OR traceState = ?) AND traceDateTime BETWEEN ? AND ?", states[0], states[1], startTime, endTime);
	}
	
	/**
	 * 更新充值流水状态
	 * @param traceState	更新状态
	 * @param traceCode		流水编号
	 * @param orignTraceState	原始状态
	 * @return
	 */
	public int updateStatus(String traceState, String traceCode, String orignTraceState) {
		return Db.update("UPDATE t_recharge_trace SET traceState = ? WHERE traceCode = ? AND traceState = ?", traceState, traceCode, orignTraceState);
	}
}
