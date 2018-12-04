package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.model.BizLog;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Page;

/**
 * 用户操作日志服务
 * @author shiqingsong
 *
 */
public class BizLogService extends BaseService{

	public boolean saveBizLog(BizLog bizLog){
		bizLog.set("bizDateTime", DateUtil.getNowDateTime() ) ;
		bizLog.set("bizDate", DateUtil.getNowDate() ) ;
		return bizLog.save() ;
	}
	
	/**
	 * 新增日志
	 * @param userCode		用户标识
	 * @param opType		类型
	 * @param bizTitle		日志标题
	 * @param bizData		请求数据
	 * @param bizContent	日志描述
	 * @param bizFrom		来源路径
	 * @return
	 */
	public boolean saveBizLog(String userCode,String opType,String bizTitle,
			String bizData,String bizContent,String bizFrom){
		BizLog bizLog = new BizLog();
		bizLog.set("userCode", userCode);
		bizLog.set("opType", opType);
		bizLog.set("bizTitle", bizTitle);
		bizLog.set("bizData", bizData);
		bizLog.set("bizContent", bizContent);
		bizLog.set("bizFrom", bizFrom);
		bizLog.set("bizDateTime", DateUtil.getNowDateTime());
		bizLog.set("bizDate", DateUtil.getNowDate());
		return bizLog.save();
	}

	/**
	 * 分页查询用户操作日志
	 * @param pageNumber		当前页
	 * @param pageSize			每页显示条数
	 * @param beginDateTime		开始时间(yyyyMMddHHmmss)
	 * @param endDateTime		结束时间(yyyyMMddHHmmss)
	 * @param userCode			用户标识
	 * @param opType			日志类型(参见sysNum)
	 * @param bizLevel			日志级别		I - INFO	W - WARN	E - ERROR	D - DEBUG
	 * @return
	 */
	public Page<BizLog> findByPage(Integer pageNumber, Integer pageSize,
			String beginDateTime, String endDateTime, String userCode,
			String opType, String bizLevel) {
		String sqlSelect = "select bizNo,userCode,bizLevel,opType,bizTitle,"
				+ "bizData,httpInfo,bizContent,bizDateTime,bizDate,bizFrom";
		String sqlFrom = " from t_bizlog ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by bizDateTime desc";  
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "userCode", "=", userCode, "and");
		makeExp(buff, paras, "opType", "=", opType, "and");
		makeExp(buff, paras, "bizLevel", "=", bizLevel, "and");
		makeExp(buff, paras, "bizDateTime", ">=", beginDateTime, "and");
		makeExp(buff, paras, "bizDateTime", "<=", endDateTime, "and");
		//System.out.println( sqlSelect+ sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder );
		return BizLog.bizLogDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray()); 
	}

	public BizLog getBizLogById(String bizNo) {
		return BizLog.bizLogDao.findById(bizNo);
	}
	
	/**	WJW
	 * @param pageNumber 当前页
	 * @param pageSize	  每页显示页数
	 * @param userCode	  用户标识
	 * @return
	 */
	public Page<BizLog> queryAutoLoanByPage(Integer pageNumber, Integer pageSize ,String userCode){
		String sqlSelect = "select bizDateTime,bizData";
		String sqlFrom = " from t_bizlog";
		String sqlOrder = " order by bizDateTime desc"; 
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		makeExp(buff, paras, "userCode", "=", userCode, "and");
		makeExp(buff, paras, "opType", "=", "H", "and");
		return BizLog.bizLogDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder, paras.toArray());
	}
	
}
