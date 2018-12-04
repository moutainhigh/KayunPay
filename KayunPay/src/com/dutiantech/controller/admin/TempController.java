package com.dutiantech.controller.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Funds;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 存管上线前临时类
 * @author StoneXK
 *
 */
public class TempController extends BaseController {
	
	private UserService userService = getService(UserService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	
	/**
	 * 批量修改用户积分 belong UserController
	 * @return
	 */
	@ActionKey("/batchPoints")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, Tx.class, PkMsgInterceptor.class })
	public Message batchPoints() {
		String userMobile = getPara("userMobile");
		long point = getParaToLong("point");
		String remark = getPara("remark");
		int addOrSub = getPara("point").indexOf("-") == 0 ? 1 : 0;
		
		String[] userMobiles = userMobile.split("\n");
		StringBuffer sb = new StringBuffer();
		for (String mobile : userMobiles) {
			User user = userService.find4mobile(mobile);
			String userCode = user.getStr("userCode");
			Funds funds = fundsServiceV2.findById(userCode);
			if (user != null && funds != null) {
				try {
					funds = fundsServiceV2.doPoints(userCode, addOrSub, point, remark);
					sb.append("<p>修改用户[").append(user.getStr("userName")).append("][").append(mobile).append("]积分成功，当前可用积分余额：").append(funds.getLong("points")).append("</p>");
				} catch (Exception e) {
					sb.append("<p style=\"color:red\">修改用户[").append(user.getStr("userName")).append("][").append(mobile).append("]积分失败</p>");
				}
			} else {
				sb.append("<p style=\"color:red\">修改用户[").append(user.getStr("userName")).append("][").append(mobile).append("]积分失败，用户或用户资金账号不存在</p>");
			}
		}
		return succ("suc", sb.toString());
	}
	
	/**
	 * 批量发放奖券
	 */
	@ActionKey("/batchTickets")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, Tx.class, PkMsgInterceptor.class })
	public Message batchTickets() {
		String userMobile = getPara("userMobile");
		String settingsType = getPara("settingsType");
		String remark = getPara("remark");
		String exDateTime = getPara("exDateTime");
		String loanMonth= getPara("loanMonth");
		String isDel = getPara("isDel"); 
		String opUserCode = getUserCode();
		
		String[] userMobiles = userMobile.split("\n");
		StringBuffer sb = new StringBuffer();
		for (String mobile : userMobiles) {
			User user = userService.find4mobile(mobile);
			String userCode = user.getStr("userCode");
			UserInfo userInfo = userInfoService.findById(userCode);
			Funds funds = fundsServiceV2.findById(userCode);
			if (user != null && funds != null) {
				try {
					boolean xyz = ticketsService.save(userCode, user.getStr("userName"), mobile, userInfo.getStr("userCardName"), remark, exDateTime, settingsType, opUserCode, SysEnum.makeSource.B,loanMonth,isDel);
					if (xyz) {
						sb.append("<p>用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券[").append(settingsType).append("]发放成功</p>");
					}
				} catch (Exception e) {
					sb.append("<p style=\"color:red\">用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券[").append(settingsType).append("]发放失败</p>");
				}
			} else {
				sb.append("<p style=\"color:red\">用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券[").append(settingsType).append("]发放失败！用户或用户资金账号不存在</p>");
			}
		}
		return succ("suc", sb.toString());
	}
	
	@ActionKey("/batchRateTickets")
	@AuthNum(value = 999) 
	@Before({AuthInterceptor.class, Tx.class, PkMsgInterceptor.class})
	public Message batchRateTickets() {
		String userMobile = getPara("userMobile");
		int rate = getParaToInt("rate", 0) * 100;	// 加息利息
		String opUserCode = getUserCode();	// 操作员编号
		int exAmount = getParaToInt("examount", 0) * 100;	// 限额，默认无限额
		String tName = getPara("tname");	// 加息券名称
		String expDate = getPara("expDate");	// 过期日期
		String loanMonth = getPara("loanMonth", "0");	// 期数限定
		String isDel = getPara("isDel", "Y");	// 是否抵扣
		
		if (rate == 0) {
			return error("01", "加息利率错误", null);
		}
		
		String[] userMobiles = userMobile.split("\n");
		StringBuffer sb = new StringBuffer();
		for (String mobile : userMobiles) {
			User user = userService.find4mobile(mobile);
			String userCode = user.getStr("userCode");	// 用户编号
			String userName = user.getStr("userName");	// 用户名
			
			UserInfo userInfo = userInfoService.findById(userCode);
			String userTrueName = userInfo.getStr("userCardName");
			
			Funds funds = fundsServiceV2.findById(userCode);
			
			if (user != null && funds != null) {
				try {
					boolean xyz = ticketsService.saveRate(userCode, userName, mobile, userTrueName, tName, expDate, rate, opUserCode, SysEnum.makeSource.D, exAmount, loanMonth, isDel);
					if (xyz) {
						sb.append("<p>用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券发放成功</p>");
					}
				} catch (Exception e) {
					sb.append("<p style=\"color:red\">用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券发放失败</p>");
				}
			} else {
				sb.append("<p style=\"color:red\">用户[").append(user.getStr("userName")).append("][").append(mobile).append("]奖券发放失败！用户或用户资金账号不存在</p>");
			}
		}
		return succ("done...", sb.toString());
	}
	
	
}
