package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.RecommendReward;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class RecommendRewardService  extends BaseService{
	
	
	/**
	 * 查询某个用户的推荐奖励列表
	 * @param pageNumber
	 * @param pageSize
	 * @param userCode
	 * @return
	 */
	public Page<RecommendReward> queryListByUser(Integer pageNumber, Integer pageSize, String userCode) {
		String sqlSelect = "select *" ;
		String sqlFrom = "from t_recommend_reward ";
		String sqlWhere = "where aUserCode = ?";
		String sqlOrder = " order by rewardDate desc, rewardTime desc";
		return RecommendReward.rmdRewardDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom + sqlWhere + sqlOrder, userCode);
	}
	
	
	/**
	 * 好友投资奖励统计
	 * @param userCode
	 * @param type 1所有 2好友投资满一万返100元  3好友投资返佣
	 * @return
	 */
	public long queryTZJL(String userCode, int type){
		if(type==2)
			return Db.queryBigDecimal("select COALESCE(SUM(rewardAmount),0) from t_recommend_reward where aUserCode = ? and rewardType = 'A'",userCode).longValue();
		else if(type==3)
			return Db.queryBigDecimal("select COALESCE(SUM(rewardAmount),0) from t_recommend_reward where aUserCode = ? and rewardType = 'B' ",userCode).longValue();
		else if(type==1)
			return Db.queryBigDecimal("select COALESCE(SUM(rewardAmount),0) from t_recommend_reward where aUserCode = ? and rewardType != 'C' ",userCode).longValue();
		return 0;
	}
	
	public boolean save(String aUserCode , String bUserCode,long rewardAmount,String rewardType,String rmk){
		RecommendReward rr = new RecommendReward();
		rr.set("aUserCode", aUserCode);
		rr.set("aUserName", User.userDao.findByIdLoadColumns(aUserCode, "userName").getStr("userName"));
		rr.set("bUserCode", bUserCode);
		rr.set("bUserName", User.userDao.findByIdLoadColumns(bUserCode, "userName").getStr("userName"));
		rr.set("rewardDate", DateUtil.getNowDate());
		rr.set("rewardTime", DateUtil.getNowTime());
		rr.set("rewardAmount", rewardAmount);
		rr.set("rewardType", rewardType);
		rr.set("rmk", rmk);
		return rr.save();
	}
	
	/**
	 * 剩余返佣总计
	 * @param aUserCode
	 * @return
	 */
	public long querySYFY(String aUserCode){
		long syfy = 0;
		List<String> list = Db.query("select bUserCode from t_recommend_info where aUserCode = ? and bRegDate>='20160801'",aUserCode);
		for (int i = 0; i < list.size(); i++) {
			List<Object[]> traces = Db.query("select payAmount,loanTimeLimit,loanRecyCount,refundType from t_loan_trace where payUserCode = ?",list.get(i));
			for (int j = 0; j < traces.size(); j++) {
				Object[] trace = traces.get(j);
				long payAmount = (long) trace[0];
				int loanTimeLimit = (int) trace[1];
				int loanRecyCount = (int) trace[2];
				String refundType = (String) trace[3];
				syfy = syfy + CommonUtil.fanyong_sy(payAmount, loanTimeLimit, (loanTimeLimit-loanRecyCount)+1, refundType);
			}
		}
		return syfy ;
	}
	
	/**
	 * 查询推荐用户推荐记录
	 * @param userCode
	 * @param beginDate	YYMMDD
	 * @param endDate	YYMMDD
	 * @param rewardType
	 * @return
	 */
	public List<RecommendReward> queryRecommendRewardByAUserCodeDateRewardType(String userCode,String beginDate,String endDate,String rewardType){
		return RecommendReward.rmdRewardDao.find("select * from t_recommend_reward where aUserCode=? and rewardDate between ? and ? and rewardType=?", userCode,beginDate,endDate,rewardType);
		
	}


}
