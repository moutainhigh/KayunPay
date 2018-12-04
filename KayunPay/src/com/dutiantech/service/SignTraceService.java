package com.dutiantech.service;

import java.util.List;

import com.dutiantech.model.SignTrace;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;

public class SignTraceService extends BaseService {

	//private static final String base_selectFields = " traceCode, userCode, signDate, signTime, sustainDay, points";

	public static final SignTrace signTraceDao = new SignTrace();

	/**
	 * 添加签到记录
	 * 
	 * @param traceCode
	 *            签到流水号<br>
	 * @param userCode
	 *            用户userCode<br>
	 * @param sustainDay
	 *            连续签到<br>
	 * @param traceRemark
	 *            备注<br>
	 * @return
	 */
	public boolean saveSignTrace(String traceCode, String userCode, int sustainDay, int points, String traceRemark) {
		SignTrace st = new SignTrace();
		if (StringUtil.isBlank(traceCode)) {
			st.set("traceCode", UIDUtil.generate());
		} else {
			st.set("traceCode", traceCode);
		}
		st.set("userCode", userCode);
		st.set("signDate", DateUtil.getNowDate());
		st.set("signTime", DateUtil.getNowTime());
		st.set("sustainDay", sustainDay);
		st.set("points", points);
		st.set("traceRemark", traceRemark);
		return st.save();
	}
	
	/**
	 * 添加签到记录
	 * @param userCode	签到流水号<br>
	 * @param sustainDay	连续签到天数<br>
	 * @param points	签到获取积分<br>
	 * @param signDate	签到日期
	 * @param traceRemark	备注
	 * @return
	 */
	public boolean signIn(String userCode, int sustainDay, int points, String signDate, String traceRemark) {
		SignTrace st = new SignTrace();
		st.set("traceCode", UIDUtil.generate());
		st.set("userCode", userCode);
		st.set("signDate", signDate);
		st.set("signTime", DateUtil.getNowTime());
		st.set("sustainDay", sustainDay);
		st.set("points", points);
		st.set("traceRemark", traceRemark);
		return st.save();
	}

	/**
	 * 查询用户当日是否签到
	 * 
	 * @param userCode
	 * @return
	 */
	public boolean isSign(String userCode) {
		long result = Db.queryLong(
				"SELECT count(1) FROM t_sign_trace WHERE signDate = DATE_FORMAT(now(), '%Y%m%d') and userCode = ?",
				userCode);
		return result == 0 ? false : true;
	}

	/**
	 * 查询用户当日连接签到天数
	 * @param userCode
	 * @return
	 */
	public int findSustainDayByUser(String userCode) {
		Integer result = Db.queryInt("SELECT sustainDay FROM t_sign_trace WHERE signDate = DATE_FORMAT(DATE_ADD(now(), INTERVAL - 1 DAY), '%Y%m%d') and userCode = ?", userCode);
		return result == null ? 1 : result + 1;
	}
	
	/**
	 * 查询用户当月已签到日期
	 * @param userCode	用户名
	 * @param month	月份
	 * @return
	 */
	public String[] getSignInDaysByMonth(String userCode) {
		List<SignTrace> days = SignTrace.signTraceDao.find("SELECT DATE_FORMAT(signDate, '%d') signDate FROM t_sign_trace WHERE DATE_FORMAT(signDate, '%Y%m') =DATE_FORMAT(now(),'%Y%m') AND userCode = ?", userCode);
		String[] result = new String[days.size()];
		for (int i = 0; i < days.size(); i++) {
			result[i] = days.get(i).getStr("signDate");
			if (Integer.parseInt(result[i]) < 10) {
				result[i] = result[i].replace("0", "");
			}
		}
		return result;
	}
	/**
	 * 查询用户当月已签到日期格式yyyy-mm-dd
	 * @param userCode	用户名
	 * @param month	月份
	 * @return
	 */
	public String[] getSignInDaysByMonth2(String userCode) {
		List<SignTrace> days = SignTrace.signTraceDao.find("SELECT DATE_FORMAT(signDate, '%d') signDate FROM t_sign_trace WHERE DATE_FORMAT(signDate, '%Y%m') = DATE_FORMAT(now(),'%Y%m') AND userCode = ?", userCode);
		String[] result = new String[days.size()];
		String Date=DateUtil.getNowDateYandM();
		for (int i = 0; i < days.size(); i++) {
			result[i] = Date+days.get(i).getStr("signDate");
		}
		return result;
	}
	/**
	 * 查询用户当日签到获取积分
	 * @param userCode
	 * @return
	 */
	public long findNowPointByUser(String userCode) {
		Long result = Db.queryLong("SELECT points FROM t_sign_trace WHERE signDate = DATE_FORMAT(now(), '%Y%m%d') and userCode = ?", userCode);
		return result == null ? 0 : result;
	}
}
