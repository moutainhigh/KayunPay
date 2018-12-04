package com.dutiantech.model;

import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

/**
 * 中奖记录表
 * @author shiqingsong
 *
 */
public class PrizeRecords extends Model<PrizeRecords> {
	
	private static final long serialVersionUID = 1415513983355490335L;
	
	public static final PrizeRecords prizeRecordsDao = new PrizeRecords();
	
	
	/**
	 * 分页获取未处理的奖品
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<PrizeRecords> queryNoDispose(){
		return prizeRecordsDao.paginate(1, 100, 
				"select * ", "from t_prize_records where status = '0' and prizeLevel < 6 order by addDateTime asc");
	}
	
	/**
	 * 修改虚拟奖品状态
	 * @param recordCode
	 * @return
	 */
	public boolean updateRecordStatus(String recordCode){
		return Db.update("update t_prize_records set status = 1,updateDateTime = ? where recordCode = ? and prizeLevel < 6 ",
				DateUtil.getNowDateTime(),recordCode) > 0;
	}
	
}






