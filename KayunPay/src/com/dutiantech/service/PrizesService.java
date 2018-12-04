package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.Prizes;
import com.jfinal.plugin.activerecord.Db;

public class PrizesService extends BaseService {

	private static final String base_selectFields = " prizeCode, prizeName, total, count, activeCode, activeName, state, checkValue, prizeLevel, amount, prizeType, exDateTime";

	public static final Prizes prizesDao = new Prizes();

	/**
	 * 根据活动查询剩余奖品数量
	 * @param activeCode	活动编号
	 * @return	剩余奖品数量
	 */
	public int getRemainPrizesByActive(String activeCode) {
		return Integer.parseInt(Db.queryBigDecimal("select sum(`total` - `count`) from t_prizes where activeCode = ?", activeCode).toString());
	}
	
	/**
	 * 根据活动编号获取奖品列表
	 * @param activeCode	活动编号	
	 * @return	奖品列表
	 */
	public List<Prizes> findPrizesByActive(String activeCode) {
		List<Prizes> listPrizes = Prizes.prizeDao.find("select " + base_selectFields + " from t_prizes where activeCode = ?", activeCode);
		return listPrizes;
	}
	
	/**
	 * 通过奖品编号获取奖品
	 * @param prizeCode
	 * @return
	 */
	public Prizes getById(String prizeCode) {
		Prizes prizes = Prizes.prizeDao.findById(prizeCode);
		return prizes;
	}
	
	/**
	 * 更改奖品已发数量
	 * @param prizeCode
	 * @param num
	 * @return
	 */
	public boolean updatePrizeCount(String prizeCode, int num) {
		Prizes prizes = Prizes.prizeDao.findById(prizeCode);
		prizes.set("count", num);
		return prizes.update();
	}
	
//	// 总概念区间
//			float totalPro = 0f;
//			// 存储每个奖品新概率区间
//			List<Float> proSection = new ArrayList<Float>();
//			proSection.add(0f);
//			// 遍历每个奖品，设置概率区间，总的概率区间为每个概率区间的总和
//			for (Award award : awards) {
//				// 每个概率区间为奖品概率乘以1000（把三位小数转换为整）再乘以剩余奖品数量
//				totalPro += award.probability * 10 * award.count;
//				proSection.add(totalPro);
//			}
//			// 获取总的概率区间中的随机数
//			Random random = new Random();
//			float randomPro = (float) random.nextInt((int)totalPro);
//			// 判断取到的随机数在哪个奖品的概率区间中
//			for(int i = 0, size = proSection.size(); i< size; i++) {
//				 if (randomPro >= proSection.get(i) && randomPro < proSection.get(i + 1)) {
//					 return awards.get(i);
//				 }
//			}
//			return null;
	
}
