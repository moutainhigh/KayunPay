package com.dutiantech.service;

import com.dutiantech.model.YistageTrace;
import com.jx.util.DateUtil;

public class YistageTraceService extends BaseService{
	
	/**
	 * 保存请求记录
	 * @param traceCode
	 * @param requestAction
	 */
	public void save(String traceCode,String requestAction,String readData) {
		
		YistageTrace yistageTrace = new YistageTrace();
		yistageTrace.set("traceCode", traceCode);
		yistageTrace.set("requestAction",requestAction);
		yistageTrace.set("traceDate", DateUtil.getDate());
		yistageTrace.set("traceTime",DateUtil.getTime());
		yistageTrace.set("updateDate",DateUtil.getDate());
		yistageTrace.set("updateTime",DateUtil.getTime());
		yistageTrace.set("requestMessage",readData);
//		yistageTrace.set("responseMessage","");
		yistageTrace.save();
	}

	/**
	 * 根据traceCode查询流水
	 * @param traceCode
	 * @return
	 */
	public YistageTrace findByTraceCode(String traceCode) {
		String sql = "select traceCode,requestAction,requestMessage,responseMessage,traceDate,traceTime,updateDate,updateTime from t_yistage_trace where traceCode=? ";
		YistageTrace yistageTrace = YistageTrace.YistageTraceDao.findFirst(sql, traceCode);
		return yistageTrace;
	}
	
	/**
	 * 更新响应报文
	 */
	public Boolean updateResponseMessage(String traceCode,String responseMessage){
		YistageTrace yistageTrace = YistageTrace.YistageTraceDao.findById(traceCode);
		if(yistageTrace != null){
			yistageTrace.set("updateDate", DateUtil.getDate());
			yistageTrace.set("updateTime", DateUtil.getTime());
			yistageTrace.set("responseMessage", responseMessage);
			return yistageTrace.update();
		}else{
			return false;
		}
		
	}
}
