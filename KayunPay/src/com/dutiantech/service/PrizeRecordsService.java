package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.PrizeRecords;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class PrizeRecordsService extends BaseService {

	private static final String base_selectFields = " recordCode, userCode, userName, activeCode, activeName, prizeCode, prizeName, checkValue, prizeLevel, prizeType, status, amount, remark, exDateTime, addDateTime, updateDateTime";

	public static final PrizeRecords prizeRecords = new PrizeRecords();

	public Page<PrizeRecords> findByActive(String ativeCode, Integer pageNumber, Integer pageSize) {
		String sqlSelect = "SELECT " + base_selectFields;
		String sqlExceptSelect = " FROM t_prize_records WHERE activeCode = '" + ativeCode + "'";
		String sqlOrder = " ORDER BY addDateTime desc";
		return PrizeRecords.prizeRecordsDao.paginate(pageNumber, pageSize, sqlSelect, sqlExceptSelect + sqlOrder);
	}
	
	/**
	 * 查询用户抽奖记录
	 * @param userCode	用户编号
	 * @param activeCode	活动编号
	 * @return
	 */
	public List<PrizeRecords> findByUser(String userCode, String activeCode) {
		return PrizeRecords.prizeRecordsDao.find("SELECT " + base_selectFields + " FROM t_prize_records WHERE userCode = ? and activeCode = ? ORDER BY addDateTime DESC", userCode, activeCode);
	}
	
	/**
	 * 分页查询用户抽奖记录
	 * @param pageNumber
	 * @param pageSize
	 * @param allkey
	 * @param userCode
	 * @param activeCode
	 * @return
	 */
	public Page<PrizeRecords> findByPage(Integer pageNumber, Integer pageSize,String allkey,String activeCode){
		String sqlSelect = "select "+base_selectFields;
		String sqlFrom = " from t_prize_records";
		String sqlOrder=" ORDER BY addDateTime DESC";//抽奖记录添加时间排序
		StringBuffer buff = new StringBuffer("");
		List<Object> paras=new ArrayList<Object>();
		makeExp(buff, paras, "activeCode", "=", activeCode, "and");
		String[] keys = new String[]{"userName","activeName","prizeName","userCode","remark","addDateTime"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		return prizeRecords.prizeRecordsDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+makeSql4Where(buff)+sqlOrder,paras.toArray());
	}
	
	/**
	 * 查询活动列表
	 * @return
	 */
	public List<PrizeRecords> prizeList(){
		return prizeRecords.prizeRecordsDao.find("select activeCode,activeName from t_prize_records GROUP BY activeCode ORDER BY addDateTime");
		
	}
	
	/**
	 * 根据用户活动查询发券详情
	 * @param userCode
	 * @param activeCode
	 * @return
	 */
	public PrizeRecords getByUserAndActive(String userCode, String activeCode, String addDate) {
		return PrizeRecords.prizeRecordsDao.findFirst("SELECT " + base_selectFields + " FROM t_prize_records WHERE userCode = ? AND activeCode = ? AND DATE_FORMAT(addDateTime, '%Y%m%d') = ?", userCode, activeCode, addDate);
	}
	
	/**
	 * 保存抽奖记录
	 * @param recordCode
	 * @param userCode
	 * @param userName
	 * @param activeCode
	 * @param activeName
	 * @param prizeCode
	 * @param prizeName
	 * @param checkValue
	 * @param prizeLevel
	 * @param prizeType
	 * @param status
	 * @param amount
	 * @param remark
	 * @param exDateTime
	 * @return
	 */
	public boolean save(String recordCode, String userCode, String userName, String activeCode, String activeName, String prizeCode, String prizeName, long checkValue, int prizeLevel, String prizeType, String status, long amount, String remark, String exDateTime) {
		PrizeRecords prizeRecords = new PrizeRecords();
		prizeRecords.set("recordCode", recordCode);
		prizeRecords.set("userCode", userCode);
		prizeRecords.set("userName", userName);
		prizeRecords.set("activeCode", activeCode);
		prizeRecords.set("activeName", activeName);
		prizeRecords.set("prizeCode", prizeCode);
		prizeRecords.set("prizeName", prizeName);
		prizeRecords.set("checkValue", checkValue);
		prizeRecords.set("prizeLevel", prizeLevel);
		prizeRecords.set("prizeType", prizeType);
		prizeRecords.set("status", status);
		prizeRecords.set("amount", amount);
		prizeRecords.set("remark", remark);
		prizeRecords.set("exDateTime", exDateTime);
		prizeRecords.set("addDateTime", DateUtil.getNowDateTime());
		prizeRecords.set("updateDateTime", DateUtil.getNowDateTime());
		return prizeRecords.save();
	}
	/**
	 * 查询用户已抽奖次数
	 * */
	public long findNum8User(String userCode,String activeCode,String begindate,String enddate,String status){
		long num = Db.queryLong("SELECT count(*) FROM t_prize_records WHERE userCode = ? AND activeCode = ? AND DATE_FORMAT(addDateTime, '%Y%m%d') >= ? and DATE_FORMAT(addDateTime, '%Y%m%d') <= ? and status=?",userCode,activeCode,begindate,enddate,status);
		return num;
	}
}