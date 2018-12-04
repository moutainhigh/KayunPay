package com.dutiantech.service;

import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class SMSLogService extends BaseService {

	/**
	 * 保存
	 * @param smsLog
	 * smsLog.mobile
	 * smsLog.content
	 * smsLog.type
	 * smsLog.status
	 * smsLog.userCode 可空
	 * smsLog.userName 可空
	 * smsLog.break    可空
	 * @return
	 */
	public boolean save(SMSLog smsLog){
		smsLog.set("sendDate", DateUtil.getNowDate());
		smsLog.set("sendDateTime", DateUtil.getNowDateTime());
		return smsLog.save();
	}
	
	/**
	 * 保存回款短信
	 * content
	 * type
	 * typeName
	 * userCode 
	 * @return
	 */
	public boolean save(String content, String type ,String typeName,String userCode){
		User user = User.userDao.findById(userCode);
		if(user == null){
			return false;
		}
		SMSLog smsLog = new SMSLog();
		String mobile = "";
		try {
			mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			if(!CommonUtil.isMobile(mobile)){
				return false;
			}
		} catch (Exception e) {
			return false;
		}
		//查询是否已经存在此用户待发送还款短信
		SMSLog smsLogDb = queryBeSendByUser(mobile, "10", "8");
		
		//解析短信内容
		String[] contentArr = content.split(",");
		if(contentArr.length != 3){
			return false;
		}
		String userName = contentArr[0];//用户昵称
		String money = contentArr[1];//金额
		String loanNo = contentArr[2];//标书No
		StringBuffer sb = new StringBuffer();
		if("20".equals(type)){
			sb.append("{'userName':'");
			sb.append(userName.replace("'", ""));
			sb.append("','earlymoney':'");
			sb.append(money);
			sb.append("','loanNo':'");
			sb.append(loanNo);
			sb.append("'}");	
		}else{
			sb.append("{'userName':'");
			sb.append(userName.replace("'", ""));
			sb.append("','money':'");
			sb.append(money);
			sb.append("','loanNo':'");
			sb.append(loanNo);
			sb.append("'}");
		}
		
		
		if(null == smsLogDb){
			//新增
			smsLog.set("mobile",mobile );
			smsLog.set("content", sb.toString());
			smsLog.set("userCode", userCode);
			smsLog.set("userName", user.getStr("userName"));
			smsLog.set("type", type);
			if("20".equals(type)){
				smsLog.set("type", "10");
			}
			smsLog.set("typeName", typeName);
			smsLog.set("status", 8);
			smsLog.set("sendDate", DateUtil.getNowDate());
			smsLog.set("sendDateTime", DateUtil.getNowDateTime());
			return smsLog.save();
		}else{
			//修改
			smsLogDb.set("content", smsLogDb.getStr("content")+","+sb.toString());
			return smsLogDb.update();
		}
	}
	
	/**
	 * 根据短信类型和状态查询
	 * @param pageNumber
	 * @param pageSize
	 * @param type			短信类型
	 * @param status		状态
	 * @return
	 */
	public Page<SMSLog> querySendSMS(Integer pageNumber, Integer pageSize, String type,String status){
		String sqlSelect = "select *";
		String sqlFrom = " from t_sms_log where status = ? and type = ? ";
		String sqlOrder = " order by id";
		return SMSLog.smsLogDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder ,status, type) ;
	}
	
	/**
	 * 根据状态查询短信
	 * @param pageNumber
	 * @param pageSize
	 * @param status		状态
	 * @return
	 */
	public Page<SMSLog> querySendSMSByStatus(Integer pageNumber, Integer pageSize, int status){
		String sqlSelect = "select *";
		String sqlFrom = " from t_sms_log where status = ? ";
		String sqlOrder = " order by id";
		return SMSLog.smsLogDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder ,status) ;
	}
	
	/**
	 * 根据用户查询短信
	 * @param userMobile	手机号
	 * @param type			短信类型
	 * @param status		状态
	 * @return
	 */
	public SMSLog queryBeSendByUser(String userMobile,String type,String status){
		String sql = "select * from t_sms_log where mobile = ? and status = ? and type = ?";
		return SMSLog.smsLogDao.findFirst(sql, userMobile,status,type);
	}
	
	/**
	 * 根据日期,短信类型,发送状态查询短信数量 WJW
	 * @param type	类型
	 * @param status	状态
	 * @return
	 */
	public long countByDateAndTypeAndStatus(String date,int type,int status){
		String sql = "select count(1) from t_sms_log where sendDate=? and type=? and `status`=?";
		return Db.queryLong(sql,date,type,status);
	}
	
}





