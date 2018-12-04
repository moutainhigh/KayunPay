package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.User;
import com.dutiantech.util.StringUtil;
import com.dutiantech.vo.VipV2;
import com.jfinal.plugin.activerecord.Db;

public class VIPService extends BaseService {
	
	/**
	 * 按活跃积分查询用户
	 * @param userScore		用户活跃积分
	 * @param pageNumber	第几页,1开始
	 * @param pageSize
	 * @return
	 */
	public List<User> findUserByUserScore(long minScore, long maxScore,int pageNumber,int pageSize){
		if(maxScore > 0){
			return User.userDao.find("select * from t_user where userScore >= ? and userScore <= ? limit ?,?",minScore,maxScore,(pageNumber-1) * pageSize, pageSize);
		}else{
			return User.userDao.find("select * from t_user where userScore >= ? limit ?,?",minScore,(pageNumber-1) * pageSize, pageSize);
		}
		
	}
	
	/**
	 * 按活跃积分统计用户数量
	 * @param userScore	333.12则输入33312
	 * @return
	 */
	public long countUserByUserScore(long minScore, long maxScore){
		if(maxScore > 0){
			return Db.queryLong("select count(userCode) from t_user where userScore >= ? and userScore <= ?",minScore,maxScore);
		}else{
			return Db.queryLong("select count(userCode) from t_user where userScore >= ? ",minScore);
		}
		
	}
	

	/**
	 * 更新用户VIP等级信息
	 * @param userCode	用户编码
	 * @param vipLevel	VIP等级
	 * @return
	 */
	public boolean updateUserLevel(String userCode,VipV2 vip){
		if(StringUtil.isBlank(userCode)){//更改全部
			return Db.update("update t_user set vipLevel = ?, vipLevelName = ?, vipInterestRate = ?, vipRiskRate = ?",vip.getVipLevel(),vip.getVipLevelName(),vip.getVipInterestRate(),vip.getVipRiskRate()) > 0;
		}else {//根据userCode更改
			return Db.update("update t_user set vipLevel = ?, vipLevelName = ?, vipInterestRate = ?, vipRiskRate = ? where userCode = ?",vip.getVipLevel(),vip.getVipLevelName(),vip.getVipInterestRate(),vip.getVipRiskRate(),userCode) > 0;
		}
	}
	
	/**
	 * 用户VIP降级
	 * @param userCode	用户编码
	 * @param nowLevel	当前等级
	 * @param ss		降几级
	 * @return
	 */
	public boolean downUserLevel(String userCode,int nowLevel,int ss){
		VipV2 vip = VipV2.getVipByLevel(nowLevel-ss);
		return Db.update("update t_user set vipLevel = ?, vipLevelName = ?, vipInterestRate = ?, vipRiskRate = ? where userCode = ?",vip.getVipLevel(),vip.getVipLevelName(),vip.getVipInterestRate(),vip.getVipRiskRate(),userCode) > 0;
	}
	
	/**
	 * 查询指定还款日期范围内的用户待收本金+待收利息
	 * @param userCode
	 * @param beginDate
	 * @param endDate
	 * @return
	 */
	public long FindBeRecyPrincipal4Future(String userCode,String beginDate, String endDate){
		return Db.queryBigDecimal("select COALESCE(sum(nextAmount+nextInterest),0) from t_loan_trace where loanRecyDate >= ? and loanRecyDate <= ? and payUserCode = ?",beginDate,endDate,userCode).longValue();
	}
	
	/**
	 * 查询一个用户的待收本息
	 * @param userCode
	 * @return
	 */
	public long findBeRecyMoney(String userCode){
		return Db.queryLong("select beRecyPrincipal + beRecyInterest from t_funds where userCode = ?",userCode);
	}
	
	/**
	 * 实时查询用户VIP等级
	 * @param userCode
	 * @return
	 */
	public int findUserVipLevelByUserCode(String userCode){
		return Db.queryInt("select vipLevel from t_user where userCode = ?",userCode);
	}

}
