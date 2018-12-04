package com.dutiantech.controller.portal;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.Market;
import com.dutiantech.model.MarketUser;
import com.dutiantech.model.PrizeRecords;
import com.dutiantech.model.Prizes;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.RecommendReward;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.MarketService;
import com.dutiantech.service.MarketUserService;
import com.dutiantech.service.PrizeRecordsService;
import com.dutiantech.service.PrizesService;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.vo.Award;
import com.fuiou.data.CommonRspData;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 活动专题相关接口
 * 
 * @author shiqingsong
 *
 */
public class ActivityController extends BaseController {

	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private MarketService marketService = getService(MarketService.class);
	private MarketUserService marketUserService = getService(MarketUserService.class);
	private PrizesService prizesService = getService(PrizesService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private PrizeRecordsService prizeRecordsService = getService(PrizeRecordsService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
    private LoanTransferService loanTransferService=getService(LoanTransferService.class);
    private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
    private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private RecommendInfoService recommendInfoService = getService(RecommendInfoService.class);
	private RecommendRewardService recommendRewardService = getService(RecommendRewardService.class);
    private String MESSAGE_PATH = "//home//www//leaveMessage.txt";
    private String AUDIT_MESSAGE_PATH = "//home//www//auditMessage.txt";
	
    /**
	 * 一战到底活动
	 * 
	 * @return
	 */
	@ActionKey("/activity4month201603")
	@ResponseCached(cachedKey = "activity4month201603", cachedKeyParm = "", mode = "remote", time = 5 * 60)
	@Before(PkMsgInterceptor.class)
	public Message activity4month201603() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(
				"20160416000000", "20160515235959", 1, 40);
		result.put("month", month);
		String x3 = DateUtil.getNowDate();
		List<FundsTrace> day = fundsTraceService.countToubiao(x3 + "000000", x3
				+ "235959", 1, 20);
		result.put("day", day);
		long today_payAmount = fundsTraceService.countPayAmount(x3 + "000000",
				x3 + "235959");
		result.put("today_payAmount", today_payAmount);
		long month_payAmount = fundsTraceService.countPayAmount(
				"20160416000000", "20160515235959");
		result.put("month_payAmount", month_payAmount);
		return succ("查询成功", result);
	}

	/**
	 * 3月活动数据导出
	 * 
	 * @return
	 */
	@ActionKey("/activity4month201603_down")
	@ResponseCached(cachedKey = "activity4month201603_down", cachedKeyParm = "", mode = "remote", time = 5 * 60)
	public void activity4month201603_down() {
		String date = getPara("date");
		List<FundsTrace> day = fundsTraceService.countToubiao(date + "000000",
				date + "235959", 1, 20);
		long today_payAmount = fundsTraceService.countPayAmount(
				date + "000000", date + "235959");
		char c = 0x09;
		String str = "时间" + c + "用户唯一标识" + c + "用户昵称" + c + "投资金额" + c + c
				+ "总投资金额\n";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < day.size(); i++) {
			FundsTrace r = day.get(i);
			String outDate = r.getStr("traceDateTime");
			outDate = outDate.substring(0, 4) + "-" + outDate.substring(4, 6)
					+ "-" + outDate.substring(6, 8) + " "
					+ outDate.substring(8, 10) + ":"
					+ outDate.substring(10, 12) + ":"
					+ outDate.substring(12, 14);
			sb.append(outDate);
			sb.append(c);
			sb.append(r.getStr("userCode"));
			sb.append(c);
			sb.append(r.getStr("userName"));
			sb.append(c);
			sb.append(r.getBigDecimal("traceAmount"));
			sb.append(c);
			sb.append(c);
			if (i == 0) {
				sb.append(today_payAmount);
				sb.append(c);
			}
			sb.append("\n");
		}

		str += sb.toString();

		try {
			String userAgent = getRequest().getHeader("USER-AGENT");
			String fileName = "3月份活动获奖名单_" + date + ".xls";
			if (userAgent.contains("MSIE")) {// IE浏览器
				fileName = URLEncoder.encode(fileName, "UTF8");
			} else if (userAgent.contains("Mozilla")) {// google,火狐浏览器
				fileName = new String(fileName.getBytes(), "ISO8859-1");
			} else {
				fileName = URLEncoder.encode(fileName, "UTF8");// 其他浏览器
			}

			HttpServletResponse response = getResponse();
			response.reset();
			response.addHeader("Content-Disposition", "attachment;filename="
					+ fileName);
			response.addHeader("Content-Length", "" + str.getBytes().length);
			response.setContentType("application/vnd.ms-excel;charset=UTF-8;");
			OutputStream os = new BufferedOutputStream(getResponse()
					.getOutputStream());
			os.write(str.getBytes("gbk"));
			os.flush();
			os.close();
		} catch (Exception e) {
		}
		renderNull();
	}

	/**
	 * 现金券内部测试
	 * 
	 * @return
	 */
	@ActionKey("/xjq_test")
	@Before(PkMsgInterceptor.class)
	public Message xjq_test() {
		String mobile = getPara("mobile");
		// String smsMsg = getPara("smsMsg","");
		if (StringUtil.isBlank(mobile)) {
			return error("01", "手机号为空", "");
		}
		// if(StringUtil.isBlank(smsMsg)){
		// return error("01", "短信验证码为空", "");
		// }
		// //验证短信验证码
		// if(CommonUtil.validateSMS("SMS_MSG_TEST_" + mobile, smsMsg) ==
		// false){
		// return error("02", "短信验证码不正确", "");
		// }

		// 验证手机号是否存在
		User user = userService.find4mobile(mobile);
		if (null == user) {
			return error("03", "用户不存在", "");
		}
		String userCode = user.getStr("userCode");
		UserInfo userInfo = userInfoService.findById(userCode);

		// 验证并限制同一种券同一用户可以领取数量
		String ticketName = "内部测试现金券";
		List<Tickets> listTicket = ticketService.queryTickets(userCode,
				ticketName);
		if (null != listTicket && listTicket.size() > 0) {
			return error("04", "您已经领取过了哦!", "");
		}

		// 添加现金券
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		tickets.set("userMobile", mobile);
		tickets.set("userName", user.getStr("userName"));
		tickets.set("userTrueName", userInfo.getStr("userCardName"));
		tickets.set("userCode", userCode);
		tickets.set("tname", ticketName);
		tickets.set("expDate", "20160331");
		tickets.set("makeSource", SysEnum.makeSource.D.val());
		tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeSourceDesc", SysEnum.makeSource.D.desc());
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
		String tmpKey = "TT.A_";
		int amount = CACHED.getInt(tmpKey + "payAmount");
		int exAmount = CACHED.getInt(tmpKey + "exAmount");
		int exRate = CACHED.getInt(tmpKey + "exRate");
		int exLimit = CACHED.getInt(tmpKey + "exLimit");
		String exType = CACHED.getStr(tmpKey + "type");
		JSONObject useExObj = new JSONObject();
		useExObj.put("amount", exAmount);
		useExObj.put("rate", exRate);
		useExObj.put("limit", exLimit);
		tickets.set("amount", amount);
		tickets.set("useEx", useExObj.toJSONString());
		tickets.set("rate", 0);
		tickets.set("ttype", exType);

		boolean save = tickets.save();
		// ticketService.save(userCode, user.getStr("userName"), mobile,
		// userInfo.getStr("userCardName"), "内部测试现金体验券", "20160331", "A", null,
		// SysEnum.makeSource.A);
		if (save == false) {
			return error("05", "发放现金券有问题,胖揍豆豆去!", "");
		}
		return succ("恭喜您,捡到10块钱!快邀朋友一起来捡钱吧!", "");
	}

	/**
	 * 积分兑换商品(5月中秋活动)
	 */
	@ActionKey("/exchange4five")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, Tx.class, PkMsgInterceptor.class })
	public Message exchange4five() {
		String mCode = getPara("mCode");
		if (StringUtil.isBlank(mCode)) {
			return error("01", "参数错误!", "");
		}

		// 验证是否存在此商品
		Market market = marketService.queryMarketDetail(mCode);
		if (null == market) {
			return error("02", "商品不存在!", "");
		}

		// 获取用户信息
		String userCode = getUserCode();
		User user = User.userDao.findById(userCode);

		// 封装对象 添加兑换记录
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);

		String address = userInfo.get("userAdress");
		if (StringUtil.isBlank(address)) {
			return error("11", "兑换失败，请您先进入个人中心补填收货地址！", "");
		}

		// //验证代收情况 代收 > 3万
		// Funds funds = fundsServiceV2.findById(userCode);
		// long beRecyPrincipal = funds.getLong("beRecyPrincipal");
		// long beRecyInterest = funds.getLong("beRecyInterest");
		// if(3000000 > (beRecyPrincipal + beRecyInterest )){
		// return error("03", "兑换失败，请核对您的投资金额是否满足条件" , "");
		// }

		// 验证商品发布 截至日期
		String startDateTime = market.getStr("startDateTime");
		String endDateTime = market.getStr("endDateTime");
		String nowDateTime = DateUtil.getNowDateTime();
		int start = "00000000000000".equals(startDateTime) ? 0 : DateUtil
				.compareDateByStr("yyyyMMddHHmmss", nowDateTime, startDateTime);
		int end = "99999999999999".equals(endDateTime) ? 0 : DateUtil
				.compareDateByStr("yyyyMMddHHmmss", endDateTime, nowDateTime);
		if (start != 0 && start != 1) {
			return error("04", "商品兑换时间还没到呢!", "");
		}

		if (end != 0 && end != 1) {
			return error("05", "商品兑换时间已经截至了!", "");
		}

		// 验证是否已经兑换过 (临时活动就不将就啦，查3次数据库)
		String[] mCodeArr = new String[] { "b3281ea5b01a419dbf07bbf9ec16e597",
				"8bccbb4af04b4e2f8093e728f5282cde",
				"6954c13c699c44fb934c299f898f5bd5" };
		for (int i = 0; i < mCodeArr.length; i++) {
			MarketUser ismarket = marketUserService.queryDetail(userCode,
					mCodeArr[i]);
			if (null != ismarket) {
				return error("07", "兑换失败，每个账号仅限进行一次兑换!", "");
			}
		}

		// 递减商品剩余数量
		boolean hasMarket = marketService.updateRemainCountNoStatus(mCode);
		if (hasMarket == false) {
			return error("09", "商品已经被抢光啦,试试其它商品吧!", "");
		}

		// 扣除可用积分
		Long mPoint = market.getLong("point");

		try {
			fundsServiceV2.doPoints(userCode, 1, mPoint, "商品兑换支出");
		} catch (BaseBizRunTimeException e) {
			return error("10", "兑换失败，请核对您的可用积分是否满足条件", "");
		}

		MarketUser marketUser = new MarketUser();
		marketUser.set("userCode", userCode);
		marketUser.set("userName", user.getStr("userName"));
		marketUser.set("userCardName", userInfo.getStr("userCardName"));
		marketUser.set("userAddress", address);
		try {
			marketUser.set("userMobile",
					CommonUtil.decryptUserMobile(user.getStr("userMobile")));
		} catch (Exception e) {
			marketUser.set("userMobile", "");
		}
		marketUser.set("mCode", mCode);
		marketUser.set("mName", market.getStr("mName"));
		marketUser.set("point", market.getLong("point"));
		boolean save = marketUserService.save(marketUser);
		if (save == false) {
			return error("08", "兑换失败，商品已经被抢光啦！", "");
		}
		return succ("恭喜您，兑换成功！礼盒将于6月6日发放！", "");
	}

	/**
	 * 5月投资排名活动
	 * 
	 * @return
	 */
	@ActionKey("/activity4month201605")
	@ResponseCached(cachedKey = "activity4month201605", cachedKeyParm = "", mode = "remote", time = 5 * 60)
	@Before(PkMsgInterceptor.class)
	public Message activity4month201605() {
		Map<String, Object> result = new HashMap<String, Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(
				"20160530000000", "20160630235959", 1, 5);
		result.put("month", month);
		return succ("查询成功", result);
	}

	/**
	 * 投资用户手机号数据导出
	 * 
	 * @return
	 */
	// @ActionKey("/exportMobile")
	public void exportMobile() {

		List<Object> query = Db
				.query("select userMobile,userName,userCode from t_user t1 where (select SUM(beRecyPrincipal + beRecyInterest ) from t_funds t2 where t1.userCode = t2.userCode) > 0");

		char c = 0x09;
		String str = "手机号" + c + "用户昵称" + c + "用户编号\n";
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < query.size(); i++) {
			Object[] o = (Object[]) query.get(i);
			String userMobile = "";
			try {
				userMobile = CommonUtil.decryptUserMobile(o[0].toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
			sb.append(userMobile);
			sb.append(c);
			sb.append(o[1]);
			sb.append(c);
			sb.append(o[2]);
			sb.append(c);
			sb.append("\n");
		}

		str += sb.toString();

		try {
			String userAgent = getRequest().getHeader("USER-AGENT");
			String fileName = "投资用户手机号.xls";
			if (userAgent.contains("MSIE")) {// IE浏览器
				fileName = URLEncoder.encode(fileName, "UTF8");
			} else if (userAgent.contains("Mozilla")) {// google,火狐浏览器
				fileName = new String(fileName.getBytes(), "ISO8859-1");
			} else {
				fileName = URLEncoder.encode(fileName, "UTF8");// 其他浏览器
			}

			HttpServletResponse response = getResponse();
			response.reset();
			response.addHeader("Content-Disposition", "attachment;filename="
					+ fileName);
			response.addHeader("Content-Length", "" + str.getBytes().length);
			response.setContentType("application/vnd.ms-excel;charset=UTF-8;");
			OutputStream os = new BufferedOutputStream(getResponse()
					.getOutputStream());
			os.write(str.getBytes("gbk"));
			os.flush();
			os.close();
		} catch (Exception e) {
		}
		renderNull();
	}

	/**
	 * 查询商品数量
	 */
	@ActionKey("/queryMarketCount")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public Message queryMarketCount() {

		Market market1 = marketService
				.queryMarketDetail("b3281ea5b01a419dbf07bbf9ec16e597");
		Market market2 = marketService
				.queryMarketDetail("8bccbb4af04b4e2f8093e728f5282cde");
		Market market3 = marketService
				.queryMarketDetail("6954c13c699c44fb934c299f898f5bd5");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("b3281ea5b01a419dbf07bbf9ec16e597",
				market1.getInt("remainCount"));
		map.put("8bccbb4af04b4e2f8093e728f5282cde",
				market2.getInt("remainCount"));
		map.put("6954c13c699c44fb934c299f898f5bd5",
				market3.getInt("remainCount"));

		return succ("查询成功", map);
	}

	/**
	 * 7/8至7/31活动投资金额排行
	 * 
	 * @return
	 */
	@ActionKey("/act0708")
	@ResponseCached(cachedKey = "act0708", cachedKeyParm = "", mode = "remote", time = 2 * 60)
	@Before(PkMsgInterceptor.class)
	public Message act0708() {
		String x1 = "20170708";
		String x2 = "20170731";
		Map<String, Object> result = new HashMap<String, Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(x1 + "000000",
				x2 + "235959", 1, 15);
		result.put("month", month);
		return succ("查询成功", result);
	}

	/**
	 * 用于活动排名 20170707 by WCF
	 */
	@ActionKey("/actRank")
	@ResponseCached(cachedKey = "actRank", cachedKeyParm = "", mode = "remote", time = 2 * 60)
	@Before(PkMsgInterceptor.class)
	public Message actRank() {
		String x1 = getPara("startTime"); // 月排行起始日期
		String x2 = getPara("endTime"); // 月排行截止日期
		int mSize = getParaToInt("mSize"); // 月排行获取前N名条数
		int dSize = getParaToInt("dSize"); // 日排行获取前N名条数
		Map<String, Object> result = new HashMap<String, Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(x1 + "000000",
				x2 + "235959", 1, mSize);
		result.put("month", month);
		if (dSize > 0) {
			String x3 = DateUtil.getNowDate();
			List<FundsTrace> day = fundsTraceService.countToubiao(
					x3 + "000000", x3 + "235959", 1, dSize);
			result.put("day", day);
		}
		return succ("查询成功", result);
	}

	/**
	 * 201708抽奖活动接口——剩余奖品
	 * 
	 * @return
	 */
	@ActionKey("/remain4active201708")
	@Before(PkMsgInterceptor.class)
	public Message remainPrize4active201708() {
		return succ(
				"查询成功",
				prizesService
						.getRemainPrizesByActive("c5c1d52bae054b4fb539dd9ee98a7011"));
	}

	/**
	 * 201708抽奖活动中奖名单
	 * 
	 * @return
	 */
	@ActionKey("/list4active201708")
	@Before(PkMsgInterceptor.class)
	public Message list4Active201708() {
		return succ("查询成功", prizeRecordsService.findByActive(
				"c5c1d52bae054b4fb539dd9ee98a7011", 1, 20));
	}

	/**
	 * 201708抽奖活动接口——查询用户个人抽奖记录
	 * 
	 * @return
	 */
	@ActionKey("/findRecords4User")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message activeRecord4User() {
		String userCode = getUserCode();
		String activeCode = "c5c1d52bae054b4fb539dd9ee98a7011";
		if (userCode == null || "".equals(userCode)) {
			return error("01", "用户未登录", false);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		PrizeRecords prizeRecords = prizeRecordsService.getByUserAndActive(
				userCode, activeCode, DateUtil.getNowDate());
		map.put("count", prizeRecords == null ? 1 : 0);
		map.put("list", prizeRecordsService.findByUser(userCode, activeCode));
		return succ("", map);
	}

	/**
	 * 201708抽奖活动接口——抽奖
	 * 
	 * @return
	 */
	@ActionKey("/activity4month201708")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message activity4month201708() {
		// 是否在活动时间内
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		boolean isInTime = DateUtil.isInTime("10:00-24:00",sdf.format(new Date()));
		if (!isInTime) {
			return error("01", "活动尚未开始", false);
		}
		String activeCode = "c5c1d52bae054b4fb539dd9ee98a7011";
		String userCode = getUserCode();
		if (userCode == null || "".equals(userCode)) {
			return error("02", "用户未登录", false);
		}
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		Funds funds = fundsServiceV2.findById(userCode);
		if (userInfo == null || funds == null || user == null) {
			return error("03", "获取用户信息失败", false);
		}
		// 查询今日是否已抽奖
		PrizeRecords prizeRecords = prizeRecordsService.getByUserAndActive(userCode, activeCode, DateUtil.getNowDate());
		if (prizeRecords == null) {
			// 是否还有剩余奖品
			long remainPrizes = prizesService.getRemainPrizesByActive(activeCode);
			if (remainPrizes != 0) {
				List<Prizes> prizes = prizesService.findPrizesByActive(activeCode);
				// 设置奖品及概率
				float probability = 100 / prizes.size();
				BigDecimal bg = new BigDecimal(probability);
				probability = bg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				List<Award> awards = new ArrayList<Award>();
				for (Prizes prize : prizes) {
					awards.add(new Award(prize.getStr("prizeCode"),probability, prize.getInt("total")- prize.getInt("count")));
				}
				// 抽奖
				Award award = Award.lottery(awards);
				Prizes prize = prizesService.getById(award.id);
				// 调整奖品库存
				prizesService.updatePrizeCount(award.id,
						prize.getInt("count") + 1);
				// 添加抽奖记录
				prizeRecordsService.save(UIDUtil.generate(), userCode,
						user.getStr("userName"), activeCode, "抢点现金买西瓜",
						award.id, prize.getStr("prizeName"), 0l, 0,
						prize.getStr("prizeType"), "1",
						prize.getLong("amount"), "活动奖励【抢点现金买西瓜】",
						"20171031235959");
				// 发放抵用券
				long limitAmount = 0l;
				switch (String.valueOf(prize.getLong("amount"))) {
				case "100":
					limitAmount = 10000;
					break;
				case "500":
					limitAmount = 50000;
					break;
				case "1000":
					limitAmount = 100000;
					break;
				case "2000":
					limitAmount = 300000;
					break;
				case "5000":
					limitAmount = 1000000;
					break;
				case "10000":
					limitAmount = 2000000;
					break;
				default:
					break;
				}
				boolean result = ticketsService.saveADV(userCode, "抢点现金买西瓜",
						"20171031", prize.getLong("amount"), limitAmount,
						"12-18-24", "Y");
				if (result) {
					int flag = -1;
					switch (new Long(prize.getLong("amount")).intValue()) {
					case 100:
						flag = 1;
						break;
					case 500:
						flag = 2;
						break;
					case 1000:
						flag = 3;
						break;
					case 2000:
						flag = 4;
						break;
					case 5000:
						flag = 5;
						break;
					case 10000:
						flag = 0;
						break;
					default:
						flag = -1;
						break;
					}
					return succ("恭喜您获得" + prize.getStr("prizeName") + "抵用券1张",
							flag);
				}
			} else {
				return error("04", "奖品剩余数不足", false);
			}
		} else {
			return error("05", "用户今日已抽奖", false);
		}
		return error("02", "奖品发放失败", false);
	}


	/**
	 * 201709 送苹果活动接口——个人投资信息
	 * 
	 * @return
	 */
	@ActionKey("/activity5User")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message activity5User() {
		String userCode = getUserCode();
		if (userCode == null || "".equals(userCode)) {
			return error("01", "用户未登录", false);
		}
		String beginDateTime = "20170901000000";
		String endDateTime = "20170930235959";
		String beginDate = "20170901";
		String endDate = "20170930";
		int loanTimeLimit = 0;// 投标期限

		List<LoanTrace> loanTraces = loanTraceService.userTenderAmount(beginDateTime, endDateTime, userCode);

		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		if (null != loanTraces) {
			for (int i = 0; i < loanTraces.size(); i++) {
				long loanAmount = 0;
				long loanAmountJQ = 0;
				LoanTransfer loanTransfer = null;
				LoanTrace loanTrace = loanTraces.get(i);
				String userName = loanTrace.getStr("payUserName");
				String traceCode = loanTrace.getStr("traceCode");
				if ("C".equals(loanTrace.getStr("isTransfer"))) { // 不是债权

					loanTimeLimit = loanTrace.getInt("loanTimeLimit");
					loanAmount = loanTrace.getLong("payAmount");
					loanAmountJQ = loanAmount * loanTimeLimit / 12;
				}  else if ("A".equals(loanTrace.getStr("isTransfer"))) {// 转让中
					loanTransfer = loanTransferService.queryLoanTransferByUser(traceCode, "B", userCode,
							beginDate, endDate);
					if (null == loanTransfer) {
						loanTimeLimit = loanTrace.getInt("loanTimeLimit");
						loanAmount = loanTrace.getLong("payAmount");
						loanAmountJQ = loanAmount * loanTimeLimit / 12;
					} 
				}

				Map<String, Object> usermap =map.get(userCode);
				if(null==usermap){
					
					usermap=new HashMap<String, Object>();
					usermap.put("loanAmount", loanAmount);
					usermap.put("loanAmountJQ", loanAmountJQ);
					usermap.put("userName", userName);
					map.put(userCode, usermap);
				}else {
					usermap.put("loanAmount",
							Long.valueOf(map.get(userCode).get("loanAmount").toString()) + loanAmount);
					usermap.put("loanAmountJQ",
							Long.valueOf(map.get(userCode).get("loanAmountJQ").toString()) + loanAmountJQ);
				}
			}
		}

		Map<String, Object> result1 = new HashMap<String, Object>();
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			result1.put("loanAmountJQ",
					Number.longToString(Long.valueOf(map.get(key).get("loanAmountJQ").toString()) / 10000));
		}
		return succ("success", result1);
	}

	
   
	/**
	 * 201709 送苹果活动接口——所有投资信息
	 * 
	 * @return
	 */
	@ActionKey("/activity5All")
	@AuthNum(value = 999)
	@Before(PkMsgInterceptor.class)
	public Message activity5All() {
		String beginDateTime = "20170901000000";
		String endDateTime = "20170930235959";
		String beginDate = "20170901";
		String endDate = "20170930";
		int loanTimeLimit = 0;// 投标期限
		int countPerson = 0;
		long countIphone = 0;
		List<LoanTrace> loanTraces = loanTraceService.allTenderAmount(beginDateTime, endDateTime);

		Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
		if (null != loanTraces) {
			for (int i = 0; i < loanTraces.size(); i++) {
				long loanAmount = 0;
				long loanAmountJQ = 0;
				LoanTransfer loanTransfer = null;
				LoanTrace loanTrace = loanTraces.get(i);
				String userName = loanTrace.getStr("payUserName");
				String traceCode = loanTrace.getStr("traceCode");
				String userCode = loanTrace.getStr("payUserCode");
				if ("C".equals(loanTrace.getStr("isTransfer"))) { // 不是债权

					loanTimeLimit = loanTrace.getInt("loanTimeLimit");
					loanAmount = loanTrace.getLong("payAmount");
					loanAmountJQ = loanAmount * loanTimeLimit / 12;
				} else if ("A".equals(loanTrace.getStr("isTransfer"))) {// 转让中
					loanTransfer = loanTransferService.queryLoanTransferByUser(traceCode, "B", userCode,beginDate, endDate);
					if (null == loanTransfer) {
						loanTimeLimit = loanTrace.getInt("loanTimeLimit");
						loanAmount = loanTrace.getLong("payAmount");
						loanAmountJQ = loanAmount * loanTimeLimit / 12;
					} 
				}
				Map<String, Object> usermap =map.get(userCode) ;
				if ( null==usermap) {
					usermap = new HashMap<String, Object>();
					usermap.put("loanAmount", loanAmount);
					usermap.put("loanAmountJQ", loanAmountJQ);
					usermap.put("userName", userName);
					usermap.put("userCode", userCode);
					map.put(userCode, usermap);
				} else {
					usermap.put("loanAmount",Long.valueOf(map.get(userCode).get("loanAmount").toString()) + loanAmount);
					usermap.put("loanAmountJQ",Long.valueOf(map.get(userCode).get("loanAmountJQ").toString()) + loanAmountJQ);
				}
			}
		}

		Map<String, Object> result1 = new HashMap<String, Object>();
		List<Map<String, Object>> newList = new ArrayList<Map<String, Object>>();
		for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			if (Integer.parseInt(map.get(key).get("loanAmountJQ").toString()) >= 40000000) {

				long personIphone = (long) (Math.floor(Double.valueOf(Long.valueOf(map.get(key).get("loanAmountJQ").toString())) / 40000000));
				countIphone += personIphone;
				countPerson++;
				map.get(key).put("personIphone", personIphone);
				map.get(key).put("loanAmount",
						Long.valueOf(map.get(key).get("loanAmount").toString()));
				map.get(key).put("loanAmountJQ",
						Long.valueOf(map.get(key).get("loanAmountJQ").toString()));
				newList.add(map.get(key));
			}
		}
		Collections.sort(newList, new Comparator<Map>() {
			public int compare(Map o1, Map o2) {
				Long award1 = (Long) o1.get("loanAmountJQ");// award1是从你list里面拿出来的一个
				Long award2 = (Long) o2.get("loanAmountJQ"); // award2是从你list里面拿出来的第二个name
				return award2.compareTo(award1);
			}
		});
		result1.put("newList", newList);
		result1.put("countIphone", countIphone);
		result1.put("countPerson", countPerson);
		System.out.println(newList.size());
		return succ("success", result1);
	}
	/**
	 * 查询用户资金流水信息
	 * 
	 * @return
	 */
	@ActionKey("/tmpQueryFundsTrace4User")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message tmpQueryFundsTrace4User() {
		String beginDateTime = getPara("beginDate") + "000000";
		String endDateTime = getPara("endDate") + "235959";

		// 获取用户标识
		String userCode = getUserCode();

		// 获取用户资金流水
		Map<String, Object> result = new HashMap<String, Object>();
		BigDecimal rs = fundsTraceService.countToubiao4One(beginDateTime,
				endDateTime, userCode);
		result.put("sumTraceAmount", rs);
		// 返回
		return succ("查询成功", result);
	}
	/**
	 * 2017 10月 翻牌活动  ws
	 * */
	@ActionKey("/TurnAroundCard")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message TurnAroundSbCard(){
		int date=Integer.parseInt(DateUtil.getNowDate());
		if(date<20171001||date>20171031){
			return error("01", "不在活动时间内", false);
		}
		String activeCode = "88563a221ed51a4ef8ef0cfbf1057578";
		String userCode = getUserCode();
		if (userCode == null || "".equals(userCode)) {
			return error("02", "用户未登录", false);
		}
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		Funds funds = fundsServiceV2.findById(userCode);
		if (userInfo == null || funds == null || user == null) {
			return error("03", "获取用户信息失败", false);
		}
		if(!FuiouController.isFuiouAccount(user)){
			return error("02","用户未开通存管",false);
		}
		long addnum=addnum(userCode, activeCode);
		long num = turnAroundCardNum(userCode,"P,N",activeCode);
		if(num+addnum==0){
			return error("04", "您的剩余抽奖次数为0", false);
		}
		//计算抽到什么奖
		if(num+addnum>0){
			List<Prizes> prizes = prizesService.findPrizesByActive(activeCode);
			// 设置奖品及概率
			float probability = 100 / prizes.size();
			BigDecimal bg = new BigDecimal(probability);
			probability = bg.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			List<Award> awards = new ArrayList<Award>();
			for (Prizes prize : prizes) {
					int leftcount=prize.getInt("total")- prize.getInt("count");
					if(leftcount<0){
						leftcount=0;
					}
					awards.add(new Award(prize.getStr("prizeCode"),probability, leftcount));
					
				}
			// 抽奖
			Award award = Award.lottery(awards);
			Prizes prize = prizesService.getById(award.id);
			Long amount=prize.getLong("amount");
			String remark="10月翻牌有礼活动";
			// 调整奖品库存
			prizesService.updatePrizeCount(award.id,prize.getInt("count") + 1);
			//区分是哪种抽奖机会  1：正常  2：补偿
			String status="1";
			if(num==0&&addnum>0){
				status="2";
			}
			// 添加抽奖记录
			boolean a=prizeRecordsService.save(UIDUtil.generate(), userCode,
					user.getStr("userName"), activeCode, "翻牌有礼",
					award.id, prize.getStr("prizeName"), 0l, prize.getInt("prizeLevel"),
					prize.getStr("prizeType"), status,
					prize.getLong("amount"), "活动奖励【翻牌有礼】",
					"20171031235959");
			if(a){
			if(prize.getInt("prizeLevel")==2) {
				fundsServiceV2.doPoints(userCode, 0, amount, remark);
			}
			if(prize.getInt("prizeLevel")==6) {
				User outUser=userService.findByMobile(CommonUtil.OUTCUSTNO);
				CommonRspData commonRspData= fuiouTraceService.refund(amount,FuiouTraceType.Q,outUser,user);
				if ("0000".equals(commonRspData.getResp_code())) {
					fundsServiceV2.recharge(userCode, amount,0, remark,SysEnum.traceType.D.val());
					rechargeTraceService.saveRechargeTrace(null,commonRspData.getMchnt_txn_ssn(), amount, "B", remark, userCode, "", "", "", "B", "SYS", remark);
				}
			}
			return succ("翻牌成功", prize);
			}
		}
		return error("05", "翻牌失败", false);
	}
	/**
	 * 10月剩余抽奖次数
	 * */
	private long turnAroundCardNum(String userCode,String traceType,String activeCode){
		String nowdate=DateUtil.getNowDate();
		long amount = fundsTraceService.sumTraceAmount(nowdate,nowdate,traceType,"D",userCode);
		long num=0;
		if(amount>=500000&&amount<2000000){
			num=1;
		}
		if(amount>=2000000&&amount<5000000){
			num=2;
		}
		if(amount>=5000000){
			num=3;
		}
		long numBeused = prizeRecordsService.findNum8User(userCode,activeCode,nowdate,nowdate,"1");
		num=num-numBeused;
		if(num<0){
			num=0;
		}
		return num;
	}
	
	/** 201710抽奖活动接口——查询未登录下抽奖记录
	 * 
	 * @return
	 */
	@ActionKey("/findRecord6All")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public Message activeRecord6User() {
		return succ("查询成功", prizeRecordsService.findByActive("88563a221ed51a4ef8ef0cfbf1057578", 1, 20));
	}
	
	/**
	 * 201710翻牌活动接口——查询用户个人抽奖记录
	 * @return
	 */
	@ActionKey("/activity6User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message activity6User(){
		String userCode=getUserCode();
		String activeCode="88563a221ed51a4ef8ef0cfbf1057578";   //活动编号
		String traceType="P,N";	//交易类型   P:投标成功 N:抵用券支出
		String fundsType="D";	//资金类型   D:支出
		String date=DateUtil.getNowDate();
		//判断用户是否登录
		if(userCode==null||"".equals(userCode)){
			return error("01", "用户未登录", false);
		}
		
		Map<String , Object> result=new HashMap<>();
		//查询用户当天剩余的抽奖次数
		long addnum=addnum(userCode, activeCode);
		long leftNum=turnAroundCardNum(userCode, traceType, activeCode);
		result.put("leftNum",leftNum+addnum);
		//查询用户抽奖记录
		List<PrizeRecords> list = prizeRecordsService.findByUser(userCode, activeCode);
		result.put("list", list);
		
		//统计用户抽到‘易’、‘融’、‘恒’、‘信’的数量
		int y=0 ,r=0 ,h=0 ,x=0;
		for (PrizeRecords prizeRecord : list) {
			String prizeType=prizeRecord.getStr("prizeType");
			switch (prizeType) {
			case "R":
				y++;
				break;
			case "S":
				r++;
				break;
			case "T":
				h++;
				break;
			case "U":
				x++;
				break;
			default:
				break;
			}
		}
		result.put("y", y);
		result.put("r", r);
		result.put("h", h);
		result.put("x", x);
		
		//查询用户当天的投资金额
		Long nowDayAmount=fundsTraceService.sumTraceAmount(date, date, traceType, fundsType, userCode);
		result.put("nowDayAmount", nowDayAmount);
		
		return succ("success", result);
	}
	
	private long addnum(String userCode,String activeCode){
	    String[] adduserMobile={"18267382083:1","13367277738:1","15337287610:1","13555835603:1","13588056644:1","13377870002:1",
				"13923857510:1","13671718109:1","13981362507:1","18669395954:1","15600625757:1",
				"13902168280:1","18112667257:1","13331893977:1","13983904426:1","13986126083:3"};
		String mobile="";
		User user=userService.findById(userCode);
		try {
			mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			return 0;
		}
		long addnum=0;
		for(int i=0;i<adduserMobile.length;i++){
			String[] mobiles=adduserMobile[i].split(":");
			if(mobile.equals(mobiles[0])){
				addnum=prizeRecordsService.findNum8User(userCode,activeCode,"20171009",DateUtil.getNowDate(),"2");
				addnum=Long.parseLong(mobiles[1])-addnum;
				if(addnum<0){
					addnum=0;
				}
				break;
			}
		}
		return addnum;
	}
	
	
	/**
	 * 双11留言板活动
	 * 1 留言
	 * */
	@ActionKey("/leaveMessage")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message leaveMessage() {
		String userCode = getUserCode();
		if (StringUtil.isBlank(userCode)) {
			return error("01", "用户未登录", false);
		}
		boolean isInTime = DateUtil.isInDateTime(DateUtil.getStrFromNowDate("yyyyMMddHHmmss"), "20171111000000", "20171111235959"); 
		if (!isInTime) {
			return error("04", "不在活动时间内", false);
		}
		String msg=getPara("msg");
		if (StringUtil.isBlank(msg)) {
			return error("02", "留言内容不能为空", false);
		}
		User user=userService.findById(userCode);
		String userName=user.getStr("userName");
		String leaveMsgDateTime=DateUtil.parseDateTime(new Date(), "MM-dd HH:mm");
		String info = userCode+"#"+userName+"#"+msg+"#"+leaveMsgDateTime;
		FileOperate fileOperate = new FileOperate();
		String strs[];
		String content;
		try {
			content = fileOperate.readTxt(MESSAGE_PATH, "UTF-8");
			if (fileOperate.isExist(MESSAGE_PATH)) {
				strs = fileOperate.readTxtLine(MESSAGE_PATH, "UTF-8");
				for (int i = 0; i < strs.length; i++) {
					if (userCode.equals(strs[i].split("#")[0])) {
						return error("03", "已经留过言了", false);
					} 
				}
			}
		} catch (IOException e1) {
			return error("05", "留言系统异常", false);
		}
		content += info;
		fundsServiceV2.doPoints(userCode, 0, 11110, "留言送积分"); // 留言成功成功送可用积分
		fileOperate.createFile(MESSAGE_PATH, content, "UTF-8");
		return succ("留言成功", true);
	}
	
	/**
	 * 双11留言板活动
	 * 3.留言展示
	 * */
	 @ActionKey("/showMsg")
	 @AuthNum(value = 999)
	 @Before({ PkMsgInterceptor.class })
	public Message  showMsg(){
		FileOperate fileOperate = new FileOperate();
		if (!fileOperate.isExist(AUDIT_MESSAGE_PATH)) {
			return error("01", "读取留言信息失败", null);
		}
		String strs[] = null;
		String strInfo[]=null;
		Map<String, Object> result1 = new HashMap<String, Object>();
		Map<String, String> map=null;
		List<Map<String, String>> list=new ArrayList<Map<String, String>>();
		try {
			strs = fileOperate.readTxtLine(AUDIT_MESSAGE_PATH, "UTF-8");
			int showNum = strs.length < 10 ? strs.length : 10;
			for (int i = 0; i < showNum; i++) {
				if(strs[i].length()>0){
					strInfo= strs[i].split("#");
					map=new HashMap<String, String>();
					if (strInfo.length >= 3) {
						map.put("userName",strInfo[0]);
						map.put("msg",strInfo[1]);
						map.put("time",strInfo[2]);
						list.add(map);
					}
				}
				
			}
		} catch (IOException e1) { 
			return error("05", "数据读取异常", false);
		}
		result1.put("list",list);
		return succ("成功获取留言",result1);
	}
	 
	 /**
	  * 双11留言板活动
	  * 展示所有留言
	  */
	 @ActionKey("/getMessage")
	 @AuthNum(value = 999)
	 @Before({AuthInterceptor.class, PkMsgInterceptor.class })
	 public Message  getMessage(){
		 String userCode = getUserCode();
		 if (!"e960414c3c7f4a7598be0a8302d95de2".equals(userCode)) {
			 return error("05", "数据读取异常", false);
		 }
		 FileOperate fileOperate = new FileOperate();
		 String strs[] = null;
		 String strInfo[]=null;
		 Map<String, Object> result=new HashMap<String, Object>();
		 Map<String, String> map=null;
		 List<Map<String, String>> list=new ArrayList<Map<String, String>>();
		 try {
			 strs = fileOperate.readTxtLine(MESSAGE_PATH, "UTF-8");
			 for (int i = 0; i < strs.length; i++) {
				 if (strs[i].length()>0) {
					 strInfo= strs[i].split("#");
					 if (strInfo.length==3) {
						 map=new HashMap<String, String>();
						 map.put("userName",strInfo[0]);
						 map.put("msg",strInfo[1]);
						 map.put("time",strInfo[2]);
					 } else if (strInfo.length==4) {
						 map=new HashMap<String, String>();
						 map.put("userName",strInfo[1]);
						 map.put("msg",strInfo[2]);
						 map.put("time",strInfo[3]);
					 }
					 list.add(map);
				 }
			 }
		 } catch (IOException e1) {
			 return error("05", "数据读取异常", false);
		 }
		 result.put("list",list);
		 return succ("查询成功", result);
	 }
	
	 /**
	  * 双11留言板活动
	  * 审核留言
	  */
	 @ActionKey("/auditMessage")
	 @AuthNum(value = 999)
	 @Before({AuthInterceptor.class, PkMsgInterceptor.class })
	 public Message auditMessage() {
		 String userCode = getUserCode();
		 if (!"e960414c3c7f4a7598be0a8302d95de2".equals(userCode)) {
			 return error("03", "审核留言异常",false);
		 }
		 String msg=getPara("msg");
		 String info="";
		if(!StringUtil.isBlank(msg)){
			 String	strs[]=msg.split("\n");
			 for (int i = 0; i < strs.length; i++) {
				 info+=strs[i]+"\r\n";
			 }
		 }
		 if (StringUtil.isBlank(msg)) {
			 return error("02", "留言内容不能为空", false);
		 }
		 FileOperate fileOperate = new FileOperate();
		 fileOperate.createFile(AUDIT_MESSAGE_PATH,info,"UTF-8");
		 return succ("","审核留言成功");
	 } 
	
	 /**
	  * 12月 让神券飞 嗨赚感恩节
	  * 日投资排行榜（前十名）
	  */
	 @ActionKey("/payAmountRanking")
	 @AuthNum(value = 999)
	 @Before({ PkMsgInterceptor.class })
	 public Message payAmountRanking(){
		 String activityEndDate = "20171218";	// 活动结束日期
		 String nowDate = DateUtil.getStrFromNowDate("yyyyMMdd");
		 List<FundsTrace> result =  null;
		 boolean isInActivityDate = DateUtil.isInDateTime(DateUtil.getStrFromNowDate("yyyyMMddHHmmss"), "20171117235959", "20171218000000");
		 if (isInActivityDate) {	// 活动期间内 取当日投资排名
			 result = fundsTraceService.queryPayAmountRanking(nowDate, nowDate, 10);//查询单日投资排名
		 } else {	// 非活动期间 取活动最后一天投资排名
			 if (DateUtil.compareDateByStr("yyyyMMdd", nowDate, activityEndDate) == 1) {
				 result = fundsTraceService.queryPayAmountRanking(activityEndDate, activityEndDate, 10);//查询单日投资排名
			 }
		 }
		 if (result != null) {
			 for (FundsTrace fundsTrace : result) {
				fundsTrace.set("traceAmount", "¥ " + Number.longToStr(Long.parseLong(fundsTrace.get("traceAmount").toString()) / 100));
			 }
		 }
		 return succ("查询成功", result);
	 }
	 
	 /**
	  * 12月 让神券飞 嗨赚感恩节
	  * 近7日投资排行榜（前十名）
	  */
	 @ActionKey("/payAmountRankingWeek")
	 @AuthNum(value = 999)
	 @Before({ PkMsgInterceptor.class })
	 public Message payAmountRankingWeek(){
		 String activityBeginDate = "20171118";	// 活动开始日期
		 String activityEndDate = "20171217";	// 活动结束日期
		 String nowDate = DateUtil.getStrFromNowDate("yyyyMMdd");	// 当前日期
		 // 如果当前日期大于活动结束日期 则当前日期为活动结束日期+1
		 if (DateUtil.compareDateByStr("yyyyMMdd", nowDate, activityEndDate) == 1) {	
			 nowDate = DateUtil.addDay(activityEndDate, 1);
		 }
		 int diffDay = Integer.parseInt(nowDate) - Integer.parseInt(activityBeginDate);
		 diffDay = diffDay > 7 ? 7 : diffDay;
		 List<Object> result = new ArrayList<Object>();
		 for (int i = 1; i <= diffDay; i++) {
			 String queryDate = DateUtil.subDay(nowDate, i);
			 List<FundsTrace> fundsTraces = fundsTraceService.queryPayAmountRanking(queryDate, queryDate, 10);
			 for (FundsTrace fundsTrace : fundsTraces) {
					fundsTrace.set("traceAmount", "¥ " + Number.longToStr(Long.parseLong(fundsTrace.get("traceAmount").toString()) / 100));
			 }
			 result.add(fundsTraces);
		 }
		 return succ("查询成功", result);
	 }
	 
	 /**
	  * 在活动期间累计投资金额和累计加息额度
	  */
	 @ActionKey("/activityAmountByUser")
	 @AuthNum(value = 999)
	 @Before({AuthInterceptor.class, PkMsgInterceptor.class })
	 public Message activityAmountByUser(){
		 String userCode = getUserCode();//拦截器已经判断userCode不为空
		
			//投资交易日期
			String beginDate = "20171118";
			String endDate = "20171217";
			
			String traceType="P";//交易类型   P:投标成功	
			//查询用户在活动时间的投资金额
			double sumAmount = fundsTraceService.sumAmountActivityByUser(beginDate, endDate, traceType, userCode) / 1000000;
			//累计加息额度为投资金额的2倍
			double raceAmount = 2 * sumAmount;
			
			Map<String, Object> mapAmount = new HashMap<String, Object>();
			mapAmount.put("sumAmount", sumAmount);
			mapAmount.put("raceAmount",raceAmount);
		    return succ("查询成功！", mapAmount);
		 
	 }
	
	 /**
	  * 12月邀请好友闯关活动
	  * 被邀请人   投资金额    投资时间
	  * @return
	  */
	 @ActionKey("/queryInviteDetailsByUserCode")
		@AuthNum(value = 999)
		@Before({AuthInterceptor.class,PkMsgInterceptor.class })
		public Message queryInviteDetailsByUserCode(){
			String userCode = getUserCode();//获取登录用户的userCode

			
			String beginDate = getPara("beginDate","");//获取开始时间
			String endDate = getPara("endDate","");//获取结束时间
			if (StringUtil.isBlank(beginDate)) {
				return error("02", "开始时间错误", null);
			}
			
			// 如果结束日期为空，则设结束时间为当前日期
			if (StringUtil.isBlank(endDate)) {
				endDate = DateUtil.getNowDate();
			}
			
			
			//活动开始的时间不能大于结束时间
			if (!(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == -1) && !(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == 0)) {
				return error("01", "活动时间有误，请检查时间", "");
			}
			
			//查询邀请好友的人数
			List<UserInfo> userInfoList = userInfoService.queryRecommendByUserCode(userCode, beginDate, endDate);
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("numInvite", userInfoList.size());//集合长度表示邀请人数
			Long sumAmount = fundsTraceService.sumInvestAmount4BeRecommondByInvite(userCode, beginDate, endDate);//邀请好友总投资额
			map.put("sumAmount",Number.longToString(sumAmount));
			//被邀请人投资详情
			List<FundsTrace> lists = fundsTraceService.queryInviteListByUserCode(userCode, beginDate, endDate);
			if(lists.size()>0){
				for(FundsTrace fundsTrace : lists){
				long traceAmount = fundsTrace.get("traceAmount");
					fundsTrace.set("traceAmount",Number.longToString(traceAmount));
					fundsTrace.set("traceDate",DateUtil.chenageDay(fundsTrace.get("traceDate").toString()));
					
				}
			}
			map.put("list",lists);
			
			return succ("查询成功", map);
			
		}
	 
	 
	 /**
	  * 封标赢取年会入场券 12.8-12.12
	  * @return
	  */
	 @ActionKey("/queryFullLoanInvestors")
	 @AuthNum(value = 999)
	 @Before({ PkMsgInterceptor.class })
	 public Message queryFullLoanInvestors(){
		 String beginDate="20171208";//活动开始日期
		 //String endDate="20171212";//活动结束日期
		 
		 //当前日期
		 String nowDate = DateUtil.getStrFromNowDate("yyyyMMdd");
		 //String nowDate = "20171202";
		 //判断是否在活动时间内
		 if(DateUtil.compareDateByStr("yyyyMMdd", nowDate, beginDate) == -1){
			 return error("01", "活动未开时，敬请期待！", false);
		 }
		 
		 //活动开始的第几天
		 int days = Integer.parseInt(nowDate)-Integer.parseInt(beginDate);
		 days = days > 4 ? 4 : days;
		 List<Map<String, Object>> totalFullLoanInfo = new ArrayList<>();
		 for (int i = days; i >= 0; i--) {
			Map<String, Object> maps = new HashMap<>();
			String activityDate = DateUtil.addDay(beginDate, i);
			maps.put("activityDate", DateUtil.chenageDay(activityDate));//活动日期
			
			List<Map<String, String>> loanInfoList = new ArrayList<>();
			//查询活动中某一天所有的满标（新手标除外）
			List<LoanInfo> loanInfos = loanInfoService.findAllFullLoan(activityDate);
			for (int j = 0; j < loanInfos.size(); j++) {
				Map<String, String> map = new HashMap<>();
				LoanInfo loanInfo = loanInfos.get(j);
				String loanTitle = loanInfo.getStr("loanTitle");//标的名称
				String loanCode = loanInfo.getStr("loanCode");//标的编号
				
				map.put("loanTitle", loanTitle);
				
				//查询当前标的封标投资人
				LoanTrace loanTrace = loanTraceService.queryFullLoanInvestor(loanCode);
				String payUserName = loanTrace.getStr("payUserName");
				String loanDateTime = loanTrace.getStr("loanDateTime");
				
				map.put("payUserName", payUserName);
				map.put("loanDateTime", DateUtil.getStrFromDate(DateUtil.getDateFromString(loanDateTime.substring(8), "HHmmss"), "HH:mm:ss"));//封标时间
				loanInfoList.add(map);
			}
			maps.put("loanInfoList", loanInfoList);
			totalFullLoanInfo.add(maps);
		}
		 return succ("查询成功", totalFullLoanInfo);
	 }
	 
	 /**
	 * WJW
	 * 赏金计划 
	 * 查看我的邀请详情
	 * @return
	 */
	 @ActionKey("/queryRewardByUserCode")
	 @AuthNum(value = 999)
	 @Before({AuthInterceptor.class,PkMsgInterceptor.class })
	 public Message queryRewardByUserCode(){
		 String beginDate = getPara("beginDate");	// 开始日期
		 String endDate = getPara("endDate");	// 结束日期
		 if (StringUtil.isBlank(endDate)) {
			 endDate = DateUtil.getNowDate();
		 }
		 
		 int pageNumber = getPageNumber();
		 int pageSize = getPageSize();
		 String userCode = getUserCode();
		 FundsController fundsController = new FundsController();
		 
		 List<RecommendReward> recommendRewards = recommendRewardService.queryRecommendRewardByAUserCodeDateRewardType(userCode, beginDate, endDate, "C");//查询用户获得推荐奖励信息
		 HashMap<String, Long> rewardAcquireMap = new HashMap<String,Long>();//已回收赏金:被推荐用户 回收金额
		 for (RecommendReward recommendReward : recommendRewards) {
			 Long rewardAmount = recommendReward.getLong("rewardAmount");//奖励金额
			 String bUserCode = recommendReward.getStr("bUserCode");//被推荐用户userCode
			 if(rewardAcquireMap.containsKey(bUserCode)){//判断赏金回款中，被推荐用户是否出现过
				 rewardAcquireMap.put(bUserCode, rewardAcquireMap.get(bUserCode)+rewardAmount);
			 }else {
				 rewardAcquireMap.put(bUserCode, rewardAmount);
			}
		}
		 Page<LoanTrace> loanTraces = loanTraceService.queryTraceByDatePage(userCode, beginDate, endDate, pageNumber, pageSize);
		 Map<String, List<Object>> moneyRewardMap = new HashMap<String,List<Object>>();
		 for (LoanTrace loanTrace : loanTraces.getList()) {
			 Long rewardAmount=0L;
			 String bUserCode = loanTrace.getStr("payUserCode");//被推荐人UserCode
			 String bUserName = loanTrace.getStr("payUserName");//被推荐人UserName
			 Long payAmount = loanTrace.get("payAmount");//投标金额
			 if(moneyRewardMap.containsKey(bUserCode)){//被推荐用户多次有效投标
				 List<Object> list = moneyRewardMap.get(bUserCode);
				 list.set(1, ((long)(list.get(1))+payAmount));
				 list.set(2, ((long)(list.get(2)) + fundsController.rewardRemain(loanTrace)));
				 continue;
			 }
			 if(rewardAcquireMap.containsKey(bUserCode)){//推荐用户是否获取赏金回款
				 rewardAmount=fundsController.rewardRemain(loanTrace)+rewardAcquireMap.get(bUserCode);
			 }else {
				 rewardAmount=fundsController.rewardRemain(loanTrace);
			 }
			 List<Object> moneyReward = new ArrayList<Object>();//单条被推荐人累计赏金信息
			 moneyReward.add(bUserName);
			 moneyReward.add(payAmount);
			 moneyReward.add(rewardAmount);
			 moneyRewardMap.put(bUserCode, moneyReward);
		 }
		 List<List<Object>> moneyRewardList = new ArrayList<List<Object>>();
		 //转换金额格式
		 for ( List<Object> list : moneyRewardMap.values()) {
			 list.set(1, "¥ " + Number.longToString(Long.parseLong(list.get(1).toString())));
			 list.set(2, "¥ " + Number.longToString(Long.parseLong(list.get(2).toString())));
			 moneyRewardList.add(list);
		 }
		 return succ("查询成功", moneyRewardList);
	}
	 
	/**
	  * WJW
	  * 赏金计划 
	  * 赏金计划排名（前十名）
	  * @return
	  */
	@ActionKey("/queryRewardRanking")
	@AuthNum(value = 999)
	@Before(PkMsgInterceptor.class)
	public Message queryRewardRanking(){
		String beginDate = getPara("beginDate");	// 开始日期
		String endDate = getPara("endDate");	// 结束日期
		if (StringUtil.isBlank(endDate)) {
			endDate = DateUtil.getNowDate();
		}
		FundsController fundsController = new FundsController();
		List<RecommendInfo> recommendInfos = recommendInfoService.queryAUserCodeByBRegDate(beginDate, endDate);
		List<List<Object>> rankList = new ArrayList<List<Object>>();
		for (RecommendInfo recommendInfo : recommendInfos) {
			List<Object> list=new ArrayList<Object>();
			String userCode = recommendInfo.getStr("aUserCode");//邀请人UserCode
			long sumRewardAcquire = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType.W.val(), fundsType.J.val(), userCode);	// 已回收赏金收益
			List<LoanTrace> loanTraces = loanTraceService.queryTraceByDate(userCode, beginDate, endDate);//查询推荐人所获赏金信息
			Long sumRewardRemain = 0L;//待回收赏金收益
			for (LoanTrace loanTrace : loanTraces) {
				sumRewardRemain += fundsController.rewardRemain(loanTrace);
			}
			list.add(recommendInfo.getStr("aUserName"));//用户名
			list.add(recommendInfo.getLong("num"));//推荐用户数
			list.add(sumRewardAcquire+sumRewardRemain);//预期赏金收益
			rankList.add(list);
		}
		
		Collections.sort(rankList, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				List<Object> list1 = (List<Object>) o1;
				List<Object> list2 = (List<Object>) o2;
				if (Long.parseLong(list1.get(2).toString()) > Long.parseLong(list2.get(2).toString())) {
					return -1;  
				}  
				if (Long.parseLong(list1.get(2).toString()) < Long.parseLong(list2.get(2).toString())) {  
					return 1;  
				}  
				return 0;  
	        }
		});
		if(rankList.size()>10){
			rankList = rankList.subList(0, 10);
		}
		for (List<Object> list : rankList) {
			list.set(2, "¥ " + Number.longToString(Long.parseLong(list.get(2).toString())));
		}
		return succ("查询成功", rankList);
	}
	
	/**
	 * 518 送加息额度活动 投多少送多少  页面显示加息额度
	 */
	@ActionKey("/queryRewardRateAmount518")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryRewardRateAmount518(){
		String userCode = getUserCode();
		String beginDate = "20180517";
		String endDate = "20180531";
		String traceType = "P";
		double sumAmount = fundsTraceService.sumAmountActivityByUser(beginDate, endDate, traceType, userCode) / 1000000;
		Map<String, Double> result = new HashMap<String, Double>();
		result.put("rewardRateAmount", sumAmount);
		return succ("ok", result);
	}
	
}
