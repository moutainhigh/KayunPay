package com.dutiantech.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.model.RecommendInfo;
import com.dutiantech.util.CommonUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class RecommendInfoService extends BaseService{
	
	/**
	 * 查询推荐列表   模糊查询
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<RecommendInfo> queryShareListLike(Integer pageNumber, Integer pageSize, String option) {
		String sqlSelect = "select * " ;
		String sqlFrom = "from t_recommend_info ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"aUserName","bUserName"};
		makeExp4AnyLike(buff, paras, keys, option, "and","or");
		String sqlOrder = " order by rid desc ";
		return RecommendInfo.rmdInfoDao.paginate(pageNumber, pageSize, sqlSelect,
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 查询某个用户的推荐列表
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<RecommendInfo> queryShareList(Integer pageNumber, Integer pageSize, String userCode) {
		String sqlSelect = "select *" ;
		String sqlFrom = "from t_recommend_info ";
		String sqlWhere = "where aUserCode = ?";
		String sqlOrder = " order by bRegDate desc, bRegTime desc";
		return RecommendInfo.rmdInfoDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere + sqlOrder, userCode);
	}
	
	/**
	 * 查询这个用户的推荐人数
	 * @param aUserCode
	 * @return
	 */
	public long queryMyTuiJian(String aUserCode){
		return Db.queryLong("select COALESCE(count(rid),0) from t_recommend_info where aUserCode = ?",aUserCode);
	}
	
	/**
	 * 查询所有推荐人userCode
	 * @param aUserCode
	 * @return
	 */
	public List<String> queryTuiJianUserCode(String aUserCode){
		List<String> list = Db.query("select bUserCode from t_recommend_info where aUserCode = ?",aUserCode);
		return list;
	}
	
	/**
	 * 查我推荐的人的明细(8.1日有邀请关系的才算返佣)
	 * @param pageNumber
	 * @param pageSize
	 * @param aUserCode
	 * @return
	 */
	public Map<String,Object> queryMyTuiJianDetail(Integer pageNumber, Integer pageSize, String aUserCode){
		Map<String,Object> result = new HashMap<String, Object>();
		String sqlSelect = "select *" ;
		String sqlFrom = "from t_recommend_info ";
		String sqlWhere = "where aUserCode = ? and bRegDate >= '20160801'";
		String sqlOrder = " order by bRegDate desc, bRegTime desc";
		Page<RecommendInfo> pages = RecommendInfo.rmdInfoDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere + sqlOrder, aUserCode);
		List<Map<String,String>> tjr = new ArrayList<Map<String,String>>();
		for (int i = 0; i < pages.getList().size(); i++) {
			Map<String,String> juju = new HashMap<String, String>();
			RecommendInfo ri = pages.getList().get(i);
			juju.put("aUserCode", ri.getStr("aUserCode"));
			juju.put("aUserName", ri.getStr("aUserName"));
			juju.put("bUserCode", ri.getStr("bUserCode"));
			juju.put("bUserName", ri.getStr("bUserName"));
			juju.put("bRegDate", ri.getStr("bRegDate"));
			juju.put("bRegTime", ri.getStr("bRegTime"));
			
			long amount_fanyong = Db.queryBigDecimal("select COALESCE(SUM(rewardAmount),0) from t_recommend_reward where aUserCode = ? and bUserCode = ? and rewardType = 'B' ",aUserCode,ri.getStr("bUserCode") ).longValue();
			long amount_touzi = Db.queryBigDecimal("select COALESCE(SUM(payAmount),0) from t_loan_trace where loanState in ('N','O','P') and payUserCode = ?",ri.getStr("bUserCode") ).longValue();
			juju.put("amount_fanyong", String.format("%.2f", CommonUtil.yunsuan(amount_fanyong+"", "100.00", "chu", 2).doubleValue()));
			juju.put("amount_touzi", String.format("%.2f", CommonUtil.yunsuan(amount_touzi+"", "100.00", "chu", 2).doubleValue()));
			tjr.add(juju);
		}
		result.put("firstPage", pages.isFirstPage());
		result.put("lastPage", pages.isLastPage());
		result.put("totalRow", pages.getTotalRow());
		result.put("totalPage", pages.getTotalPage());
		result.put("pageSize", pages.getPageSize());
		result.put("pageNumber", pages.getPageNumber());
		result.put("list", tjr);
		return result;
	}

	/**
	 * 查询时间段之内有被推荐人注册的用户Code
	 * @param startDate	开始时间，格式：yyyyMMdd
	 * @param endDate	结束时间，格式：yyyyMMdd
	 * @return
	 */
	public List<String> queryUserCodeByHasRecommend(String startDate,String endDate){
		return Db.query("SELECT DISTINCT aUserCode FROM t_recommend_info WHERE bRegDate BETWEEN ? AND ?", startDate, endDate);
	}
	
	/**
	 * 在活动期间查询所有被推荐人userCode  hw
	 * @param 
	 * @return
	 */
	public List<String> queryTuiJianUserCodeAndDate(String aUserCode,String startDate,String endDate){
		List<String> list = Db.query("SELECT bUserCode FROM t_recommend_info  WHERE bUserCode IN(SELECT userCode FROM t_user_info WHERE isAuthed='2') AND bRegDate BETWEEN ? AND ? and aUserCode=?",startDate,endDate,aUserCode);
		return list;
	}
	
	/**
	 * WJW
	 * 根据被推荐人userCode查询推荐人userCode
	 * @param bUserCode 被推荐人userCode
	 * @return
	 */
	public String queryAUserCodeByBUserCode(String bUserCode){
		return Db.queryStr("select aUserCode from t_recommend_info where bUserCode=?",bUserCode);
	}
	
	/**
	 * WJW
	 * 在时间段内查询推荐人列表及推荐用户数
	 * @param aUserCode 推荐人UserCode
	 * @param startDate 被推荐人注册时间上限 YYMMDD
	 * @param endDate	被推荐人注册时间下限 YYMMDD
	 * @return
	 */
	public List<RecommendInfo> queryAUserCodeByBRegDate(String startDate,String endDate){
		return RecommendInfo.rmdInfoDao.find("SELECT aUserCode,aUserName,count(1) num FROM t_recommend_info  WHERE bRegDate BETWEEN ? AND ? group by aUserCode",startDate,endDate);
	}
	
	/**
	 * TZQ——根据推荐人查询总的被推荐人数和实际参与投资的人数
	 * @param startDate		活动开始时间
	 * @param endDate		活动结束时间或至今
	 * @param aUserCode		推荐人编码
	 * @return
	 */
	public RecommendInfo queryBuserCodeAndInvest(String aUserCode ,String startDate,String endDate){
		return RecommendInfo.rmdInfoDao.findFirst("select COUNT(DISTINCT r.bUserCode) rNum,COUNT(DISTINCT t.payUserCode) aNum from t_recommend_info r LEFT JOIN t_loan_trace t ON r.bUserCode = t.payUserCode WHERE r.aUserCode = ? AND r.bRegDate BETWEEN ? AND ?",
				aUserCode, startDate, endDate);
	}
	
}
