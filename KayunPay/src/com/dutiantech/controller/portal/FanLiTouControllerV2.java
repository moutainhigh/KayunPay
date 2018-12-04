package com.dutiantech.controller.portal;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.FanLiTouUserInfo;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.VIPService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.util.AES;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.util.UserUtil;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.P2p500405ReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * 智能投顾平台合作自动化接口V1.5
 * 
 * @author StoneXK
 *
 */
public class FanLiTouControllerV2 extends BaseController {

	private UserService userService = getService(UserService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private BanksService banksService = getService(BanksService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private VIPService vipService = getService(VIPService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);

	static String LOGIN_TOKEN_CACHED_KEY = "fanlitou_login_token_";
	static String signKey = "ef6d3e5feae0a8c7b3e5cf7d97656a92";
	static String AESKey = "97166e0a5033b943";
	static AES AESUtil = new AES(AESKey);
	static boolean isDev = AdminConfig.isDevMode;

	/**
	 * 老用户账户绑定接口
	 */
	@ActionKey("/fltv2/bind")
	@AuthNum(value = 999)
	public void bind() {

		if (checkSign() == false) {
			renderJson(makeSignFailurePkg());
			return;
		}

		String uid = getParamByDecrypt("uid");
		String source = getParamByDecrypt("source");
		String fcode = getParamByDecrypt("fcode");
		// String bid_url = getPara("bid_url");

		String queryString = "?fltuid=" + uid + "&source=" + fcode;

		if ("wap".equals(source.toLowerCase()) == true) {
			redirect("http://m.yrhx.com/m" + queryString, true);
		} else {
			Memcached.set(fcode + uid, uid, 360 * 1000);
			redirect(getRequestHost() + "/login" + queryString, true);
		}

	}

	/**
	 * 获取login token接口
	 */
	@ActionKey("/fltv2/token")
	@AuthNum(value = 999)
	public void token() {

		if (checkSign() == false) {
			renderJson(makeSignFailurePkg());
			return;
		}

		String uid = getParamByDecrypt("uid");
		String regToken = getParamByDecrypt("register_token");

		FanLiTouUserInfo userInfo = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		if (userInfo == null) {
			renderJson(makeReturnPkg("45", "无用户信息"));
			return;
		}

		String tmpToken = userInfo.getStr("token");
		if (tmpToken.equals(regToken) == false) {
			renderJson(makeReturnPkg("41", "注册Token不匹配"));
			return;
		} else {
			JSONObject ret = makeReturnPkg("05", "获取Token成功");

			String loginToken = UIDUtil.generate();
			ret.put("login_token", encrypt(loginToken));

			String userCode = userInfo.getStr("userCode");

			// save token cached 30s
			Memcached.set(LOGIN_TOKEN_CACHED_KEY + loginToken, userCode, System.currentTimeMillis() + 30 * 1000);

			renderJson(ret);
		}
	}

	/**
	 * 登录授权接口
	 */
	@ActionKey("/fltv2/login")
	@AuthNum(value = 999)
	public void login() {

		if (checkSign() == false) {
			renderJson(makeSignFailurePkg());
			return;
		}

		String fltLoginToken = getParamByDecrypt("login_token");
		Object plfLoginToken = Memcached.get(LOGIN_TOKEN_CACHED_KEY + fltLoginToken);
		if (plfLoginToken == null) {
			renderJson(makeReturnPkg("45", "login token 已过期!"));
			return;
		} else {
			// clear
			Memcached.delete(LOGIN_TOKEN_CACHED_KEY + fltLoginToken);
		}

		String uid = getParamByDecrypt("uid");
		FanLiTouUserInfo userInfo = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		if (userInfo == null) {
			renderJson(makeReturnPkg("45", "返利投渠道用户信息不存在"));
			return;
		}

		String tmpRegToken = userInfo.getStr("token");
		String fltRegToken = getParamByDecrypt("register_token");
		if (tmpRegToken.equals(fltRegToken) == false) {
			renderJson(makeReturnPkg("41", "注册Token不一致!"));
			return;
		}

		String userCode = userInfo.getStr("userCode");
		User user = User.userDao.findFirst("select * from t_user where userCode = ? ", userCode);
		String userState = user.getStr("userState");
		if (userState.equals("N") == false) {
			renderJson(makeReturnPkg("45", "用户在平台已被冻结"));
			return;
		}
		Memcached.set("PORTAL_USER_" + userCode, user);
		// 修改用户登录相关字段
		userService.updateUser4Login(userCode, getRequestIP());

		// 记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.LOGIN, "用户从返利投授权登陆 ");

		// long exTime = 7*24*60*60 ;
		String token = UserUtil.UserEnCode(userCode, getRequestIP(), AuthInterceptor.INVALIDTIME, null);
		setCookieByHttpOnly(AuthInterceptor.COOKIE_NAME, token, AuthInterceptor.INVALIDTIME);

		long cv = System.currentTimeMillis() / 1000 / 60 / 60 / 24;
		setCookie("userCode_" + cv, userCode, 3600 * 100);
		setCookie("userName_" + cv, user.getStr("userName"), 3600 * 100);

		String source = getParamByDecrypt("source");
		String bid_url = getPara("bid_url");

		if (StrKit.isBlank(bid_url) == false) {
			// has url
			// bid_url = AESUtil.decrypt(bid_url) ;
			redirect(bid_url);
		} else {
			// no url
			if ("WAP".equals(source) == true) {
				redirect("http://m.yrhx.com/m");
			} else {
				forward("/portal/A00.html");
			}
		}

	}

	/**
	 * 投资记录查询接口
	 */
	@ActionKey("/fltv2/query")
	@AuthNum(value = 999)
	public void query() {

		JSONObject object = new JSONObject();
		String start_time = getPara("start_time");
		String end_time = getPara("end_time");
		int pageNumber = getParaToInt("pageIndex", 1);
		int pageSize = getParaToInt("pageCount", 20);
		if (pageNumber <= 0) {
			pageNumber = 1;
		}
		if (pageSize > 50) {
			pageSize = 50;
		}

		if (checkSignNoCrypt() || StringUtil.isBlank(start_time) || StringUtil.isBlank(end_time)) {
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数start_time ,end_time为空");
			object.put("totalCount", 0);
			object.put("bid", new String[] {});
			renderJson(object);
			return;
		}

		Page<LoanTrace> loanTraces = loanTraceService.findByPage4flt(pageNumber, pageSize,
				start_time.replace("-", "") + "000000", end_time.replace("-", "") + "235959");

		object.put("success", true);
		object.put("message", "查询投标流水成功");
		object.put("totalCount", loanTraces.getTotalRow());
		object.put("orders", getLoanTraceList(loanTraces.getList()));
		renderJson(object);
		return;

	}

	private JSONArray getLoanTraceList(List<LoanTrace> listLoanTrace) {
		JSONArray objArray = new JSONArray();
		for (int i = 0; i < listLoanTrace.size(); i++) {
			LoanTrace loanTrace = listLoanTrace.get(i);
			FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.findById(loanTrace.getStr("payUserCode"));

			JSONObject obj = new JSONObject();

			String mobile = "00000000000";
			try {
				mobile = CommonUtil.decryptUserMobile(fltUser.getStr("mobile"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			obj.put("uid", fltUser.getStr("fltuid"));
			obj.put("phoneNum", mobile);
			obj.put("bidId", loanTrace.get("loanCode"));// 产品Id
			obj.put("bidStatus", getLoanStatus(loanTrace.getStr("loanState")));
			obj.put("bidName", loanTrace.get("loanTitle"));// 产品名称
			obj.put("investAmount", loanTrace.getLong("payAmount") / 100);// 投资金额
			obj.put("investTime", getFormatDateTime(loanTrace.getStr("loanDateTime")));// 投标的时间
			obj.put("isFirstInvest", null == loanTraceService.query4flt(loanTrace.getStr("payUserCode"),
					loanTrace.getStr("loanDateTime")) ? 1 : 0);// 用户该笔投资记录在平台是否为首次投资
			objArray.add(obj);
		}
		return objArray;
	}

	/**
	 * 债转（提前还款）记录查询接口
	 */
	@ActionKey("/fltv2/assign")
	@AuthNum(value = 999)
	public void assign() {

		JSONObject object = new JSONObject();
		String start_time = getPara("start_time");
		String end_time = getPara("end_time");
		int pageNumber = getParaToInt("pageIndex", 1);
		int pageSize = getParaToInt("pageCount", 20);
		if (pageNumber <= 0) {
			pageNumber = 1;
		}
		if (pageSize > 50) {
			pageSize = 50;
		}

		if (checkSignNoCrypt() || StringUtil.isBlank(start_time) || StringUtil.isBlank(end_time)) {
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数start_time ,end_time为空");
			object.put("totalCount", 0);
			object.put("bid", new String[] {});
			renderJson(object);
			return;
		}

		Page<LoanTransfer> loanTraces = loanTransferService.findByPage4flt(pageNumber, pageSize,
				start_time.replace("-", ""), end_time.replace("-", ""));

		object.put("success", true);
		object.put("message", "查询投标流水成功");
		object.put("totalCount", loanTraces.getTotalRow());
		object.put("orders", getLoanTransferList(loanTraces.getList()));
		renderJson(object);
		return;

	}

	private JSONArray getLoanTransferList(List<LoanTransfer> listLoanTransfer) {
		JSONArray objArray = new JSONArray();
		for (int i = 0; i < listLoanTransfer.size(); i++) {
			LoanTransfer loanTranfer = listLoanTransfer.get(i);
			FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.findById(loanTranfer.getStr("payUserCode"));
			LoanTrace loanTrace = loanTraceService.findById(loanTranfer.getStr("traceCode"));

			JSONObject obj = new JSONObject();

			String mobile = "00000000000";
			try {
				mobile = CommonUtil.decryptUserMobile(fltUser.getStr("mobile"));
			} catch (Exception e) {
				e.printStackTrace();
			}

			obj.put("uid", fltUser.getStr("fltuid"));
			obj.put("phoneNum", mobile);
			obj.put("bidId", loanTranfer.get("loanCode"));// 产品Id
			obj.put("bidName", loanTranfer.get("loanTitle"));// 产品名称
			obj.put("investAmount", loanTranfer.getInt("payAmount") / 100);// 投资金额
			obj.put("investTime", getFormatDateTime(loanTrace.getStr("loanDateTime")));// 投标时间
			obj.put("isAssign", true);// 是否债权转让
			obj.put("isFullAssign", true);// 是否全部债权转让
			obj.put("assignAmount", loanTranfer.getInt("leftAmount") / 100);// 债权转让金额
			obj.put("assignDate", getFormatDate(loanTranfer.getStr("gotDate")));// 债权转让时间
			obj.put("isAdvancedRepay", false);// 是否提前还款
			obj.put("advancedRepayDate", null);// 提前还款时间
			objArray.add(obj);
		}
		return objArray;
	}

	/************************** 返利投接口 V1.5 ***************************/

	/**
	 * A001 新用户注册接口
	 * 
	 */
	@ActionKey("/fltv2/register")
	@AuthNum(value = 999)
	public void register() {
		if (checkSign() == false) {
			renderJson(makeSignFailurePkg());
			return;
		}

		String userMobile = getParamByDecrypt("phone_num");
		String fcode = getParamByDecrypt("fcode");
		String uid = getParamByDecrypt("uid");

		String loginPasswd = UIDUtil.generate().substring(12, 20); // random,随机8位密码
		String userName = userMobile.substring(0, 3) + "****" + userMobile.substring(7, 11);// "返利投_"
																							// +
																							// UIDUtil.generate().substring(5,
																							// 10)
																							// ;
																							// //random
																							// 10位字符
		String regUserCode = UIDUtil.generate();
		// 验证手机号是否已注册
		User user = userService.find4mobile(userMobile);
		if (user != null) {
			if (null != FanLiTouUserInfo.fanlitouDao.queryByMobile(userMobile)) {
				renderJson(makeReturnPkg("42", "已绑定返利投渠道"));
				return;
			}
		} else {
			// 手机号未注册，则为该手机号注册账号
			boolean b = userService.save(regUserCode, userMobile, "00@yrhx.com", loginPasswd, userName, getRequestIP(),
					String.format("[%s][%s]", fcode, uid));
			if (b == false) {
				// 日志
				BIZ_LOG_ERROR(userMobile, BIZ_TYPE.REGISTER, "注册失败", null);
				renderJson(makeReturnPkg("42", "数据落地异常"));
				return;
			}
		}

		// 绑定返利投账号
		String token = FanLiTouUserInfo.fanlitouDao.saveUser(regUserCode, userName, userMobile, fcode, uid, 0);
		if (token == null) {
			renderJson(makeReturnPkg("42", "平台注册成功，但绑定返利投关系失败"));
			return;
		}

		ticketService.toReward4newUser(regUserCode); // 注册成功赠送现金券
//		fundsServiceV2.doPoints(regUserCode, 0, 1000, "注册送积分"); // 注册成功送可用积分

		JSONObject retPkg = makeReturnPkg("01", "注册成功");
		retPkg.put("user_name", encrypt(userName));
		retPkg.put("register_token", encrypt(token));

		// TODO 发送带密码的短信
		String msgContent = CommonUtil.SMS_MSG_REGISTER_FLT.replace("[name]", userName).replace("[mobile]", userMobile)
				.replace("[pwd]", loginPasswd);
		SMSClient.sendSms(userMobile, msgContent);

		renderJson(retPkg);
	}

	/**
	 * A002 注册关系查询接口
	 * 
	 */
	@ActionKey("/fltv2/register_query")
	@AuthNum(value = 999)
	public void register_query() {
		JSONObject ret = new JSONObject();
		if (checkSign() == false) {
			renderJson(makeSignFailurePkg());
			return;
		}

		// String uid = getParamByDecrypt("uid");
		String mobile = getParamByDecrypt("phone_num");

		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByMobile(mobile);
		User user = userService.find4mobile(mobile);
		// 查询注册状态
		if (user == null) {
			ret.put("register_status", "10");
			ret.put("msg", "新用户，未注册");
			renderJson(ret);
			return;
		} else {
			if (fltUser == null) {
				ret.put("register_status", "13");
			}
		}
		if (fltUser.getInt("bindType") == 0) {
			ret.put("register_status", "11");
		} else {
			ret.put("register_status", "12");
		}
		String userCode = user.getStr("userCode");
		BanksV2 banks = banksService.findBanks4User(userCode).get(0);
		UserInfo userInfo = userInfoService.findById(userCode);
		Funds funds = fundsServiceV2.findById(userCode);
		// 是否绑卡 01-已绑卡 02-未绑卡
		ret.put("bind_card_status", banks == null ? "02" : "01");
		// 用户绑定银行卡号
		ret.put("bind_card_number", banks == null ? "" : banks.get("bankNo"));
		// 用户绑定银行卡编码
		ret.put("bind_card_bank", banks == null ? "" : banks.get("bankType"));
		// 用户是否已实名认证 01-已实名 02-未实名
		ret.put("true_name_status", (userInfo != null && "2".equals(userInfo.get("isAuthed"))) ? "01" : "02");
		// 用户认证的真实姓名
		ret.put("real_name", userInfo == null ? "" : userInfo.get("userCardName"));
		try {
			// 用户绑定的身份证号
			ret.put("id_num", userInfo == null ? "" : CommonUtil.decryptUserCardId(userInfo.getStr("userCardId")));
		} catch (Exception e) {
			makeReturnPkg("43", "查询用户绑定身份证号错误");
			e.printStackTrace();
		}
		try {
			// 用户银行卡预留手机
			ret.put("bank_phone", CommonUtil.decryptUserMobile(user.getStr("userMobile")));
		} catch (Exception e) {
			makeReturnPkg("43", "查询用户银行卡预留手机错误");
			e.printStackTrace();
		}
		// 平台用户名
		ret.put("user_name", user.get("userName"));
		// 描述信息
		ret.put("msg", "");
		// 用户账户余额，如果用户未注册，可直接设置为0
		ret.put("balance", funds == null ? 0 : funds.get("avBalance"));
		// 当前冻结总金额
		ret.put("frozen_amount", funds == null ? 0 : funds.get("frozeBalance"));
		// 当月剩余免费提现次数
		int x1 = withdrawFreeService.findFreeCountByUserCode(userCode);// 已免费提现次数
		int x2 = vipService.findUserVipLevelByUserCode(userCode);
		x1 = x1 < 0 ? 0 : x1;
		int useFree = x1;
		int vipFree = VipV2.getVipByLevel(x2).getVipTxCount();
		int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411", DateUtil.getNowDate());
		vipFree = x > 0 ? 0 : vipFree;
		ret.put("remain_free_withdraw_times", vipFree - useFree);
		// 资产总额
		ret.put("total_assets",
				funds == null ? 0
						: funds.getLong("avBalance") + funds.getLong("frozeBalance") + funds.getLong("beRecyPrincipal")
								+ funds.getLong("beRecyInterest"));
		// 待收本息
		ret.put("receiveing_princcipal_interest",
				funds == null ? 0 : funds.getLong("beRecyPrincipal") + funds.getLong("beRecyInterest"));
		// 在投标的
		ret.put("investing_bid_counts", loanTraceService.countTrace4User(user.getStr("userCode")));
		renderJson(ret);
	}

	/**
	 * A003 用户交易流水查询接口
	 * 
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/fltv2/transaction_query")
	@AuthNum(value = 999)
	public void transactionQuery() {
		JSONObject object = new JSONObject();
		String uid = getParamByDecrypt("uid");
		String start_time = getPara("start_time");
		String end_time = getPara("end_time");
		int pageNumber = getParaToInt("page", 1);
		int pageSize = getParaToInt("page_count", 20);
		if (pageNumber <= 0) {
			pageNumber = 1;
		}
		if (pageSize > 50) {
			pageSize = 50;
		}

		if (checkSignNoCrypt() || StringUtil.isBlank(start_time) || StringUtil.isBlank(end_time)) {
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数start_time ,end_time为空");
			object.put("totalCount", 0);
			object.put("bid", new String[] {});
			renderJson(object);
			return;
		}

		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		User user = null;
		try {
			user = userService.find4mobile(CommonUtil.decryptUserMobile(fltUser.getStr("mobile")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userCode = user.getStr("userCode");

		Map<String, Object> fundsTraces = fundsTraceService.findByPage(pageNumber, pageSize,
				start_time.replace("-", ""), end_time.replace("-", ""), "A,B,C,D,V,W,H,G,P,R,L,M,X,F,Y,I,U,W,Q,O", null,
				userCode);

		object.put("uid", uid);
		List<FundsTrace> listFundsTraces = null;
		if (fundsTraces.get("list") instanceof List<?>) {
			listFundsTraces = (ArrayList<FundsTrace>) fundsTraces.get("list");
		}
		object.put("transactions", getFundsTraceList(listFundsTraces));
		object.put("total_count", listFundsTraces.size());

		object.put("success", true);
		object.put("message", "查询投标流水成功");
		renderJson(object);
		return;
	}

	/**
	 * P001 可投资标列表接口
	 */
	@ActionKey("/fltv2/bid_list")
	@AuthNum(value = 999)
	public void bidList() {
		JSONObject object = new JSONObject();
		// 安全检验
		if (checkSignNoCrypt()) {
			object.put("success", false);
			object.put("message", "未能通过安全校验");
			object.put("totalCount", 0);
			object.put("bidList", new String[] {});
			renderJson(object);
			return;
		}

		int pageNumber = getParaToInt("page_index", 1);
		int pageSize = getParaToInt("page_count", 20);

		if (pageNumber <= 0) {
			pageNumber = 1;
		}
		if (pageSize > 20) {
			pageNumber = 20;
		}

		String loanState = "J";
		Page<LoanInfo> loanInfos = loanInfoService.findByPortal(pageNumber, pageSize, loanState, null, null, null,
				null);
		object.put("success", true);
		object.put("message", "查询可投标的列表成功");
		object.put("total_count", loanInfos.getTotalRow());
		object.put("bid_list", getBidList(loanInfos.getList()));
		renderJson(object);
		return;
	}

	/**
	 * P002 获取标的详情接口
	 */
	@ActionKey("/fltv2/bid_detail")
	@AuthNum(value = 999)
	public void bidDetail() {
		JSONObject object = new JSONObject();
		String bid_id = getPara("bid_id");
		if (checkSignNoCrypt() || StringUtil.isBlank(bid_id)) {
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数bid_id为空");
			object.put("bid", new String[] {});
			renderJson(object);
			return;
		}

		LoanInfo loanInfo = loanInfoService.findById(bid_id);
		object.put("success", true);
		object.put("message", "查询标的详情成功");
		object.put("bid", getBidDetial(loanInfo));
		renderJson(object);
		return;
	}

	/**
	 * T001 充值接口
	 */
	@ActionKey("/fltv2/deposit")
	@AuthNum(value = 999)
	public void deposit() {
		JSONObject object = new JSONObject();
		String uid = getPara("uid");
		double amount = Double.parseDouble(getPara("amount"));
		String fOrderNo = getPara("f_order_no");
		String source = getPara("source");
		String rUrl = getPara("r_url");
		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		User user = userService.findById(fltUser.getStr("userCode"));

		if (checkSignNoCrypt()) {
			object.put("success", false);
			object.put("message", "未能通过安全校验");
			renderJson(object);
			return;
		}

		// TODO 充值
		if ("pc".equalsIgnoreCase(source)) {
			P2p500405ReqData p2p500405ReqData = new P2p500405ReqData();
			String login_id = User.userDao.findByIdLoadColumns(user.getStr("userCode"), "loginId").getStr("loginId");
			try {
				login_id = CommonUtil.decryptUserMobile(login_id);
			} catch (Exception e) {
				e.printStackTrace();
			}
			p2p500405ReqData.setAmt(String.valueOf(amount));
			p2p500405ReqData.setLogin_id(login_id);
			p2p500405ReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
			p2p500405ReqData.setMchnt_txn_ssn(fOrderNo);
			p2p500405ReqData.setPage_notify_url(rUrl);
			try {
				FuiouService.p2p500405(p2p500405ReqData, getResponse());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * T002 充值结果查询接口
	 */
	@ActionKey("/fltv2/deposit_query")
	@AuthNum(value = 999)
	public void deposit_query() {
		JSONObject object = new JSONObject();
		// 安全检验
		if (checkSign() == false) {
			makeSignFailurePkg();
		}

		String uid = getPara("uid");
		String fOrderNo = getPara("f_order_no");
		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		Funds funds = fundsServiceV2.findById(fltUser.getStr("userCode"));

		FundsTrace fundsTrace = fundsTraceService.findById(fOrderNo);
		object.put("status", getDepositStatus(fundsTrace.getStr("traceSynState")));
		object.put("msg", fundsTrace.getStr("traceRemark"));
		object.put("plat_order_no", fOrderNo);
		object.put("balance", funds.get("avBalance"));
		renderJson(object);
		return;
	}

	/**
	 * T003 提现接口
	 */
	@ActionKey("/fltv2/withdraw")
	@AuthNum(value = 999)
	public void withdraw() {
		JSONObject object = new JSONObject();
		// 安全检验
		// if (checkSign() == false) {
		// makeSignFailurePkg();
		// }

		String uid = getPara("uid");
		long amount = getParaToLong("amount") * 100;
		String fOrderNo = getPara("f_order_no");
		String fcode = getPara("fcode");
		String source = getPara("source");
		String rUrl = getPara("r_url");

		// TODO 提现
	}

	/**
	 * T004 提现结果查询接口
	 */
	@ActionKey("/fltv2/withdraw_query")
	@AuthNum(value = 999)
	public void withdrawQuery() {
		JSONObject object = new JSONObject();
		// 安全检验
		if (checkSign() == false) {
			makeSignFailurePkg();
		}

		String uid = getPara("uid");
		String fOrderNo = getPara("f_order_no");
		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		Funds funds = fundsServiceV2.findById(fltUser.getStr("userCode"));

		FundsTrace fundsTrace = fundsTraceService.findById(fOrderNo);
		object.put("status", getDepositStatus(fundsTrace.getStr("traceSynState")));
		object.put("msg", fundsTrace.getStr("traceRemark"));
		object.put("plat_order_no", fOrderNo);
		object.put("balance", funds.get("avBalance"));
		renderJson(object);
		return;
	}
	
	/**
	 * T002/T004 充值/提现结果查询接口
	 */
	@ActionKey("/fltv2/funds_query")
	@AuthNum(value=999)
	public void fundsQuery() {
		JSONObject object = new JSONObject();
		// 安全检验
		if (checkSign() == false) {
			makeSignFailurePkg();
		}

		String uid = getPara("uid");
		String fOrderNo = getPara("f_order_no");
		FanLiTouUserInfo fltUser = FanLiTouUserInfo.fanlitouDao.queryByUID(uid);
		Funds funds = fundsServiceV2.findById(fltUser.getStr("userCode"));

		FundsTrace fundsTrace = fundsTraceService.findById(fOrderNo);
		object.put("status", getDepositStatus(fundsTrace.getStr("traceSynState")));
		object.put("msg", fundsTrace.getStr("traceRemark"));
		object.put("plat_order_no", fOrderNo);
		object.put("balance", funds.get("avBalance"));
		renderJson(object);
		return;
	}
	
	/**
	 * T005 投资接口
	 */
	@ActionKey("/fltv2/invest")
	@AuthNum(value=999)
	public void invest() {
		JSONObject object = new JSONObject();
		// 安全检验
//		if (checkSign() == false) {
//			makeSignFailurePkg();
//		}
		
		String bid = getPara("bid_id");
		long amount = getParaToLong("amount") * 100;
		String fOrderNo = getPara("f_order_no");
		String uid = getPara("uid");
		String source = getPara("source");
		
		// TODO 投资
	}
	
	/**
	 * T006 投资记录查询接口
	 */
	@ActionKey("/fltv2/invest_query")
	@AuthNum(value=999)
	public void investQuery() {
		JSONObject object = new JSONObject();
		String startTime = getPara("start_time");
		String endTime = getPara("end_time");
		// 安全检验
//		if (checkSign() == false) {
//			makeSignFailurePkg();
//		}
	}
	/****************************************************************** 私有函数 ********************************************************************/

	/**
	 * 解析交易状态
	 * 
	 * @param status
	 * @return
	 */
	private String getDepositStatus(String status) {
		switch (status) {
		case "N": // 交易处理中
			return "02";
		case "O": // 交易成功
			return "01";
		case "P": // 交易失败
			return "42";
		default:
			break;
		}
		return null;
	}

	/**
	 * 解析标的详情
	 * 
	 * @param loanInfo
	 * @return
	 */
	private JSONObject getBidDetial(LoanInfo loanInfo) {
		JSONObject obj = new JSONObject();
		obj.put("bid_id", loanInfo.get("loanCode"));// 产品Id
		obj.put("status", getLoanStatus(loanInfo.getStr("loanState")));
		obj.put("full_time", obj.getInteger("status") == -1 || obj.getInteger("status") == 1 ? null
				: getFormatDateTime(loanInfo.getStr("effectDate") + loanInfo.getStr("effectTime")));// 满标时间
		obj.put("name", loanInfo.get("loanTitle"));// 产品名称
		obj.put("min_invest_amount", loanInfo.getInt("minLoanAmount") / 100);// 最小投资金额
		obj.put("introduction", loanInfo.get("loanDesc"));// 产品描述
		obj.put("pc_url", getRequestHost() + "/Z02_1?loanCode=" + loanInfo.get("loanCode"));// PC访问地址
		obj.put("mobile_url", "http://m.yrhx.com/m?loanCode=" + loanInfo.get("loanCode"));// 移动（wap）端访问地址
		obj.put("total_amount", loanInfo.getLong("loanAmount") / 100);// 标的总金额
		obj.put("remain_amount", loanInfo.getLong("loanBalance") / 100);// 剩余可投金额
		obj.put("duration", loanInfo.getInt("loanTimeLimit"));// 标的期限
		obj.put("duration_unit", 30);// 标的期限单位
		obj.put("is_new_user", loanInfo.getInt("benefits4new") > 0);// 是否新手标
		obj.put("repayment_type", getRefundType(loanInfo.getStr("refundType")));// 还款方式
		obj.put("is_group", false);
		obj.put("interest_rate",
				new BigDecimal((loanInfo.getInt("rateByYear")) / 10.0 / 10).setScale(1, BigDecimal.ROUND_HALF_UP));// 标的利率
		obj.put("award_interest_rate", new BigDecimal((loanInfo.getInt("rewardRateByYear")) / 10.0 / 10).setScale(1,
				BigDecimal.ROUND_HALF_UP));// 奖励利率
		return obj;
	}

	/**
	 * 解析还款方式
	 * 
	 * @param refundType
	 * @return
	 */
	private int getRefundType(String refundType) {
		if (refundType.equals("A")) {
			return 4; // 按月等额本息
		} else {
			return 1; // 先息后本
		}
	}

	/**
	 * 解析标状态
	 * 
	 * @param loanState
	 * @return
	 */
	private int getLoanStatus(String loanState) {
		int status = 2;
		switch (loanState) {
		case "J": // 招标中
			status = 1;
			break;
		case "M": // 满标待审
			status = 2;
			break;
		case "N": // 还款中
			status = 3;
			break;
		case "O": // 还款成功
			status = 4;
			break;
		case "P": // 提前还款
			status = 4;
		case "Q": // 系统已代还
			status = 4;
		case "R": // 代还已回收
			status = 4;
			break;
		case "L": // 已流标
			status = -1;
			break;
		default:
			break;
		}
		return status;
	}

	/**
	 * 解析资金流水
	 * 
	 * @param listFundsTraces
	 * @return
	 */
	private JSONArray getFundsTraceList(List<FundsTrace> listFundsTraces) {
		JSONArray objArray = new JSONArray();
		for (int i = 0; i < listFundsTraces.size(); i++) {
			FundsTrace fundsTrace = listFundsTraces.get(i);
			JSONObject obj = new JSONObject();
			String traceType = fundsTrace.getStr("traceType");
			String fundsType = fundsTrace.getStr("fundsType");
			String amount = String.valueOf(fundsTrace.getLong("traceAmount"));
			if ("D".equalsIgnoreCase(fundsType)) {
				amount = "-" + amount;
			}
			switch (traceType) {
			case "G":
				traceType = "withdraw"; // 提现
				break;
			case "C":
				traceType = "deposit"; // 充值
				break;
			case "P":
				traceType = "invest"; // 投资
				break;
			case "R":
				traceType = "repay"; // 回款
				break;
			case "L":
				traceType = "repay"; // 回款
				break;
			default:
				traceType = "other"; // 其他
				break;
			}
			String traceDateTime = fundsTrace.getStr("traceDate") + fundsTrace.getStr("traceTime");
			traceDateTime = DateUtil.parseDateTime(DateUtil.getDateFromString(traceDateTime, "yyyyMMddHHmmss"),
					"yyyy-MM-dd HH:mm:ss");

			obj.put("transaction_id", fundsTrace.getStr("traceCode"));
			obj.put("transaction_type", traceType);
			obj.put("time", traceDateTime);
			obj.put("amount", amount);
			obj.put("balance", fundsTrace.getLong("traceBalance"));
			obj.put("remark", fundsTrace.getStr("traceRemark"));

			objArray.add(obj);
		}
		return objArray;
	}

	/**
	 * 解析标列表
	 * 
	 * @param listLoan
	 * @return
	 */
	private JSONArray getBidList(List<LoanInfo> listLoan) {
		JSONArray objArray = new JSONArray();
		for (int i = 0; i < listLoan.size(); i++) {
			LoanInfo loanInfo = listLoan.get(i);
			JSONObject obj = new JSONObject();
			String releaseDateTime = loanInfo.getStr("releaseDate") + loanInfo.getStr("releaseTime");

			obj.put("bid_id", loanInfo.get("loanCode"));// 产品Id
			// 标的状态
			if (Long.valueOf(DateUtil.getNowDateTime()) >= Long.valueOf(releaseDateTime)) {
				obj.put("status", 1);
			} else {
				obj.put("status", 5);
			}
			obj.put("name", loanInfo.get("loanTitle"));// 产品名称
			obj.put("min_invest_amount", loanInfo.getInt("minLoanAmount") / 100);// 最小投资金额
			obj.put("introduction", loanInfo.get("loanDesc"));// 产品描述
			obj.put("pc_url", getRequestHost() + "/Z02_1?loanCode=" + loanInfo.get("loanCode"));// PC访问地址
			obj.put("mobile_url", "http://m.yrhx.com/m?loanCode=" + loanInfo.get("loanCode"));// 移动（wap）端访问地址
			obj.put("total_amount", loanInfo.getLong("loanAmount") / 100);// 标的总金额
			obj.put("remain_amount", loanInfo.getLong("loanBalance") / 100);// 剩余可投金额
			obj.put("duration", loanInfo.getInt("loanTimeLimit"));// 标的期限
			obj.put("duration_unit", 30);// 标的期限单位
			obj.put("is_new_user", loanInfo.getInt("benefits4new") > 0 ? 1 : 0);// 是否新手标
			obj.put("repayment_type", getRefundType(loanInfo.getStr("refundType")));// 还款方式
			obj.put("is_group", false);
			obj.put("interest_rate",
					new BigDecimal((loanInfo.getInt("rateByYear")) / 10.0 / 10).setScale(1, BigDecimal.ROUND_HALF_UP));// 标的利率
			obj.put("award_interest_rate",
					new BigDecimal((loanInfo.getInt("rewardRateByYear") + loanInfo.getInt("benefits4new")) / 10.0 / 10)
							.setScale(1, BigDecimal.ROUND_HALF_UP));// 奖励利率
			objArray.add(obj);
		}
		return objArray;
	}

	private String getFormatDateTime(String dateTime) {
		if (dateTime.length() < 14) {
			return "0000-00-00 00:00:00";
		}
		return dateTime.substring(0, 4) + "-" + dateTime.substring(4, 6) + "-" + dateTime.substring(6, 8) + " "
				+ dateTime.substring(8, 10) + ":" + dateTime.substring(10, 12) + ":" + dateTime.substring(12, 14);
	}

	private String getFormatDate(String date) {
		if (date.length() < 8) {
			return "0000-00-00";
		}
		return date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
	}

	/****************************** 华丽分割线 *******************************/

	/**
	 * 校验签名
	 * 
	 * @return true 验证通过 false 验证失败
	 */
	private boolean checkSign() {

		String tmpSign = getParamByDecrypt("sign");
		String t = getParamByDecrypt("t");
		String checkValue = "";

		try {
			checkValue = MD5Code.crypt(t + signKey);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		if (isDev) {
			System.out.println(String.format("Check Sign : [%s][%s][%s]", t, tmpSign, checkValue));
		}

		return checkValue.equals(tmpSign);

	}

	private boolean checkSignNoCrypt() {

		String tmpSign = getPara("sign");
		String t = getPara("t");
		String checkValue = "";

		try {
			checkValue = MD5Code.crypt(t + signKey);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}

		return checkValue.equals(tmpSign);

	}

	/**
	 * 安全未通过校验
	 * 
	 * @return
	 */
	private JSONObject makeSignFailurePkg() {
		return makeReturnPkg("41", "未通过安全校验");
	}

	private JSONObject makeReturnPkg(String status, String msg) {
		JSONObject retPkg = new JSONObject();
		// retPkg.put("status", encrypt( status )) ;
		// retPkg.put("msg", encrypt( msg ) ) ;
		retPkg.put("status", status);
		retPkg.put("msg", msg);
		return retPkg;
	}

	private String getParamByDecrypt(String key) {
		String tmpValue = getPara(key);
		String val = AESUtil.decrypt(tmpValue);
		if (isDev) {
			System.out.println(String.format("Crypt value:%s  Decrypt value : %s ", tmpValue, val));
		}
		return val;
	}

	private String encrypt(String val) {
		return AESUtil.encrypt(val);
	}

	private String getRequestHost() {
		String host = "";
		HttpServletRequest request = getRequest();
		int port = request.getServerPort();
		if (port == 443) {
			host = "https://";
		} else {
			host = "http://";
		}
		host += request.getServerName();

		return host;
	}
}
