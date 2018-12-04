package com.dutiantech.vo;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.util.CommonUtil;

public class VipV2 {
	public static List<VipV2> VIPS = new ArrayList<VipV2>();
	static {
		// VIPS.add( new VipV2(0, 299, 1 , "少尉" , 200 , 300,0) ) ;
		// VIPS.add( new VipV2(300, 999, 2 , "中尉" , 200 , 300,0) ) ;
		// VIPS.add( new VipV2(1000, 2999, 3 , "上尉" , 300 , 200,1) ) ;
		// VIPS.add( new VipV2(3000, 7999, 4 , "少校" , 200 , 100,3) ) ;
		// VIPS.add( new VipV2(8000, 15999, 5 , "中校" , 200 , 0,5) ) ;
		// VIPS.add( new VipV2(16000, 59999, 6 , "上校" , 100 , 0,8) ) ;
		// VIPS.add( new VipV2(60000, -1, 7 , "将军" , 0 , 0,-1) ) ;
		// TODO 7.1更新
		// VIPS.add( new VipV2(0, 299, 1 , "少尉" , 200 , 100,0) ) ;
		// VIPS.add( new VipV2(300, 999, 2 , "中尉" , 200 , 100,1) ) ;
		// VIPS.add( new VipV2(1000, 2999, 3 , "上尉" , 200 , 100,2) ) ;
		// VIPS.add( new VipV2(3000, 7999, 4 , "少校" , 100 , 100,3) ) ;
		// VIPS.add( new VipV2(8000, 15999, 5 , "中校" , 100 , 100,5) ) ;
		// VIPS.add( new VipV2(16000, 59999, 6 , "上校" , 100 , 0,8) ) ;
		// VIPS.add( new VipV2(60000, -1, 7 , "将军" , 0 , 0,-1) ) ;
		// 2018.3.15更新
		VIPS.add(new VipV2(1, "新手", 800, 0, 1, 0, 1000000,0, 0));
		VIPS.add(new VipV2(2, "普通会员", 800, 0, 2, 1000000, 2000000,0, 50));
		VIPS.add(new VipV2(3, "青铜会员", 800, 0, 3, 2000000, 5000000,0, 80));
		VIPS.add(new VipV2(4, "白银会员", 800, 0, 5, 5000000, 10000000,50,100));
		VIPS.add(new VipV2(5, "黄金会员", 800, 0, 8, 10000000, 20000000,80, 150));
		VIPS.add(new VipV2(6, "白金会员", 800, 0, 10, 20000000, 50000000,100, 200));
		VIPS.add(new VipV2(7, "钻石会员", 800, 0, 20, 50000000, 100000000,120, 250));
		VIPS.add(new VipV2(8, "黑钻会员", 800, 0, -1, 100000000, 500000000,150, 300));
		VIPS.add(new VipV2(9, "至尊会员", 800, 0, -1, 500000000, 0,200, 350));
	}

	/**
	 * 根据待收获取会员对象
	 * 
	 * @param beRecyAmount
	 *            待收金额
	 * @return
	 */
	public static VipV2 getVipBybeRecyAmount(long beRecyAmount) {
		if (beRecyAmount <= 0) {// 待收小于0，返回新手对象
			return VIPS.get(0);
		}
		if (beRecyAmount >= VIPS.get(VIPS.size() - 1).getVipMinAmount()) {// 待收超过至尊会员最小值，返回至尊对象
			return VIPS.get(VIPS.size() - 1);
		}

		for (VipV2 vipV2 : VIPS) {
			if (vipV2.getVipMinAmount() <= beRecyAmount && beRecyAmount < vipV2.getVipMaxAmount()) {
				return vipV2;
			}

		}
		return VIPS.get(0);// 默认返回新手对象
	}

	/**
	 * 
	 * @param score
	 *            333.12则输入33312
	 * @return
	 */
	public static VipV2 getVipByValue(long score) {
		score = CommonUtil.yunsuan(score + "", "100", "chu", 0).longValue();
		VipV2 vip = null;
		for (VipV2 tmpVip : VIPS) {
			long minScore = tmpVip.getVipMinScore();
			long maxScore = tmpVip.getVipMaxScore();
			if (score >= minScore && (score <= maxScore || maxScore == -1)) {
				vip = tmpVip;
			}
		}
		if (vip == null)
			vip = VIPS.get(0);
		return vip;
	}

	/**
	 * 根据会员等级返回会员对象
	 * 
	 * @param vipLevel
	 *            会员等级
	 * @return
	 */
	public static VipV2 getVipByLevel(int vipLevel) {
		for (VipV2 vipV2 : VIPS) {
			if (vipLevel == vipV2.getVipLevel()) {
				return vipV2;
			}
		}
		return VIPS.get(0);// 默认返回新手对象
	}

	private long vipMinScore = 0;
	private long vipMaxScore = 0;
	private int vipLevel = 1;
	private String vipLevelName;
	private int vipInterestRate = 0;// 利息管理费费率
	private int vipRiskRate = 0;// 风险储备金费率
	private int vipTxCount = 0;
	private long vipMinAmount = 0;// 最小待收金额
	private long vipMaxAmount = 0;// 最大待收金额
	private int rewardInterest=0;//加息奖励
	private int upgradeInterest=0;//升级奖励

	
	/*public VipV2(int vipLevel, String vipLevelName, int vipInterestRate, int vipRiskRate, int vipTxCount,
			long vipMinAmount, long vipMaxAmount,int rewardInterest, int upgradeInterest) {
		super();
		this.vipLevel = vipLevel;
		this.vipLevelName = vipLevelName;
		this.vipInterestRate = vipInterestRate;
		this.vipRiskRate = vipRiskRate;
		this.vipTxCount = vipTxCount;
		this.vipMinAmount = vipMinAmount;
		this.vipMaxAmount = vipMaxAmount;
		this.rewardInterest=rewardInterest;
		this.upgradeInterest=upgradeInterest;
	}*/
	
	public VipV2(int vipLevel, String vipLevelName, int vipInterestRate, int vipRiskRate, int vipTxCount,
			long vipMinAmount, long vipMaxAmount,int rewardInterest, int upgradeInterest) {
		super();
		this.vipLevel = vipLevel;
		this.vipLevelName = vipLevelName;
		this.vipInterestRate = vipInterestRate;
		this.vipRiskRate = vipRiskRate;
		this.vipTxCount = vipTxCount;
		this.vipMinAmount = vipMinAmount;
		this.vipMaxAmount = vipMaxAmount;
		this.rewardInterest=rewardInterest;
		this.upgradeInterest=upgradeInterest;
	}

	public long getVipMinScore() {
		return vipMinScore;
	}

	public void setVipMinScore(long vipMinScore) {
		this.vipMinScore = vipMinScore;
	}

	public long getVipMaxScore() {
		return vipMaxScore;
	}

	public void setVipMaxScore(long vipMaxScore) {
		this.vipMaxScore = vipMaxScore;
	}

	public int getVipLevel() {
		return vipLevel;
	}

	public void setVipLevel(int vipLevel) {
		this.vipLevel = vipLevel;
	}

	public String getVipLevelName() {
		return vipLevelName;
	}

	public void setVipLevelName(String vipLevelName) {
		this.vipLevelName = vipLevelName;
	}

	public int getVipInterestRate() {
		return vipInterestRate;
	}

	public void setVipInterestRate(int vipInterestRate) {
		this.vipInterestRate = vipInterestRate;
	}

	public int getVipRiskRate() {
		return vipRiskRate;
	}

	public void setVipRiskRate(int vipRiskRate) {
		this.vipRiskRate = vipRiskRate;
	}

	public int getVipTxCount() {
		return vipTxCount;
	}

	public void setVipTxCount(int vipTxCount) {
		this.vipTxCount = vipTxCount;
	}

	public long getVipMinAmount() {
		return vipMinAmount;
	}

	public void setVipMinAmount(long vipMinAmount) {
		this.vipMinAmount = vipMinAmount;
	}

	public long getVipMaxAmount() {
		return vipMaxAmount;
	}

	public void setVipMaxAmount(long vipMaxAmount) {
		this.vipMaxAmount = vipMaxAmount;
	}

	public int getRewardInterest() {
		return rewardInterest;
	}

	public void setRewardInterest(int rewardInterest) {
		this.rewardInterest = rewardInterest;
	}

	public int getUpgradeInterest() {
		return upgradeInterest;
	}

	public void setUpgradeInterest(int upgradeInterest) {
		this.upgradeInterest = upgradeInterest;
	}
	
	

}
