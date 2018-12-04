package com.dutiantech.service.common;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;

/**
 * 积分通用业务类
 * @author StoneXK
 *
 */
public class FundsCommonService {

	/**
	 * 操作用户积分，记录积分操作流水
	 * @param userCode 操作用户
	 * @param opType	
	 * 		0 - 增加
	 * 	 	1 - 减少
	 * @param doAmount	操作积分（单位：分）
	 * @param remark
	 * @return
	 */
	public static Funds doPoints(String userCode, int opType, long doAmount, String remark) {
		String nowDate = DateUtil.getNowDate();
		String nowTime = DateUtil.getNowTime();
		List<Object> ps = new ArrayList<Object>();
		ps.add(doAmount);
		ps.add(nowDate + nowTime);
		ps.add(nowDate);
		ps.add(nowTime);
		ps.add(userCode);
		Funds funds = null;
		long avpoints = 0;
		String opChar = "-";
		if (opType == 0) {
			opChar = "+";
		}
		String upSql = "update t_funds set  points=points" + opChar + "? ,updateDateTime=?,"
				+ "updateDate=?,updateTime=? where userCode=? ";
		if (opType == 1) {
			funds = Funds.fundsDao.findById(userCode);
			long points = funds.getLong("points");
			if (points < doAmount) {
				throw new BaseBizRunTimeException("B5", "可用积分不足[" + points / 10 / 10 + "]", points / 10 / 10);
			}
			// 兼容
			upSql += " and (points-?)>=0 ";
			ps.add(doAmount);
		}

		int result = Db.update(upSql, ps.toArray());
		if (result > 0) {
			// 修改成功
			funds = Funds.fundsDao.findById(userCode);
			avpoints = funds.getLong("points");
		} else {
			// 修改失败
			throw new BaseBizRunTimeException("B6", "可用积分不足,操作失败!", null);
		}

		FundsTrace fundsTrace = new FundsTrace();
		fundsTrace.set("userCode", userCode);
		fundsTrace.set("userName", User.userDao.findByIdLoadColumns(userCode, "userName").getStr("userName"));
		fundsTrace.set("traceType", opType == 0 ? SysEnum.traceType.J.val() : SysEnum.traceType.Z.val());
		fundsTrace.set("fundsType", opType == 0 ? SysEnum.fundsType.J.val() : SysEnum.fundsType.D.val());
		fundsTrace.set("traceAmount", doAmount);
		fundsTrace.set("traceBalance", funds.getLong("avBalance"));
		fundsTrace.set("traceFrozeBalance", funds.getLong("frozeBalance"));
		fundsTrace.set("traceFee", 0);
		fundsTrace.set("traceCode", UIDUtil.generate());
		fundsTrace.set("traceDate", DateUtil.getNowDate());
		fundsTrace.set("traceDateTime", DateUtil.getNowDateTime());
		fundsTrace.set("traceTime", DateUtil.getNowTime());
		fundsTrace.set("traceSynState", "N");
		fundsTrace.set("traceTypeName", opType == 0 ? SysEnum.traceType.J.desc() : SysEnum.traceType.Z.desc());// 交易类型
		avpoints = CommonUtil.yunsuan(avpoints + "", "" + 100, "chu", 0).longValue();
		fundsTrace.set("traceRemark", remark + ",当前可用积分余额[" + avpoints + "],");
		fundsTrace.save();
		return funds;
	}
}
