package com.dutiantech.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.dutiantech.anno.AuthNum;
import com.dutiantech.model.BankCode;
import com.dutiantech.model.BankOreaCode;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.User;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Property;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.fuiou.data.AppTransReqData;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.FreezeReqData;
import com.fuiou.data.P2p500405ReqData;
import com.fuiou.data.QueryBalanceReqData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.QueryBalanceRspData;
import com.fuiou.data.QueryCzTxReq;
import com.fuiou.data.QueryCzTxRspData;
import com.fuiou.data.QueryCzTxRspDetailData;
import com.fuiou.data.QueryUserInfsReqData;
import com.fuiou.data.QueryUserInfsRspData;
import com.fuiou.data.QueryUserInfsRspDetailData;
import com.fuiou.data.TransferBmuReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.core.ActionKey;

public class FuiouController extends BaseController {

	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private UserService userService = getService(UserService.class);
	private BanksService banksService = getService(BanksService.class);
	private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);

	public static String VER;
	public static String MCHNT_CD;
	public static String FUIOU_ACCOUNT;
	public static String OUT_CUST_NO;

	static {
		VER = Property.getPropertyValueByKey("fuiou", "VER");
		MCHNT_CD = Property.getPropertyValueByKey("fuiou", "MCHNT_CD");
		FUIOU_ACCOUNT = Property.getPropertyValueByKey("fuiou", "FUIOU_ACCOUNT");
		OUT_CUST_NO = Property.getPropertyValueByKey("fuiou", "OUT_CUST_NO");
	}
	
	/**
	 * 获取Fuiou存管账号
	 */
	public String getFuiouUid(User user) {
		String fuiouId = null;
		if (user == null) {
			fuiouId = null;
		} else {
			if (StringUtil.isBlank(user.getStr("loginId"))) {
				fuiouId = null;
			} else {
				try {
					fuiouId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return fuiouId;
	}

	/**
	 * 冻结金额
	 * 
	 * @param user
	 *            用户ID
	 * @param amount
	 *            冻结金额
	 * @return
	 */
	public CommonRspData freeze(User user, long amount) {
		String fuiouId;
		CommonRspData commonRspData = null;
		FuiouTraceType fuiouTraceType = null;
		try {
			fuiouId = getFuiouUid(user);
			FreezeReqData freezeReqData = new FreezeReqData();
			freezeReqData.setVer(VER);
			freezeReqData.setCust_no(fuiouId);
			freezeReqData.setAmt(String.valueOf(amount));// 解冻金额
			freezeReqData.setMchnt_cd(MCHNT_CD);// 商户
			String ssn = CommonUtil.genMchntSsn();// 交易流水号
			freezeReqData.setMchnt_txn_ssn(ssn);
			commonRspData = FuiouService.freeze(freezeReqData);

			if ("0000".equals(commonRspData.getResp_code())) {
				fuiouTraceType = FuiouTraceType.FREEZE;
			} else {
				fuiouTraceType = FuiouTraceType.FREEZE_ERR;
			}
			fuiouTraceService.save(ssn, fuiouId, user.getStr("userCode"), String.valueOf(amount), fuiouTraceType);
		} catch (Exception e) {
			return commonRspData;
		}
		return commonRspData;

	}

	/**
	 * 存管转账(商户与个人之间)
	 * 
	 * @param payAmount
	 *            交易金额
	 * @param user
	 *            交易用户
	 * @param fuiouTraceType
	 *            交易类型: INCOME 收入; PAY 支出; REPAY_INTEREST 回收利息; REPAY_PRINCIPAL
	 *            回收本金;
	 * @return 交易返回信息
	 */
	public CommonRspData transferBmu(Long payAmount, User user, FuiouTraceType fuiouTraceType) {
		String amount = String.valueOf(payAmount);
		String userCode = user.getStr("userCode");
		String fuiouId = getFuiouUid(user);
		String ssn = CommonUtil.genMchntSsn();

		// 设置请求数据
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(VER);
		transfe.setMchnt_cd(MCHNT_CD);
		if (FuiouTraceType.INCOME.equals(fuiouTraceType)) {
			transfe.setOut_cust_no(FUIOU_ACCOUNT); // 出账账号
			transfe.setIn_cust_no(fuiouId); // 入账账号
		} else if (FuiouTraceType.PAY.equals(fuiouTraceType)) {
			transfe.setOut_cust_no(fuiouId);
			transfe.setIn_cust_no(FUIOU_ACCOUNT);
		} else if (FuiouTraceType.REPAY_INTEREST.equals(fuiouTraceType)
				|| FuiouTraceType.REPAY_PRINCIPAL.equals(fuiouTraceType)) {
			transfe.setOut_cust_no(FUIOU_ACCOUNT);
			transfe.setIn_cust_no(fuiouId);
		}
		transfe.setMchnt_txn_ssn(ssn);

		CommonRspData comm = null;
		try {
			comm = FuiouService.transferBmu(transfe); // 提交请求数据
		} catch (Exception e) {
			System.out.println(fuiouId + "用户划拨系统错误");
		}
		// 解析返回参数
		if (!"0000".equals(comm.getResp_code())) {
			if (FuiouTraceType.INCOME.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.INCOME_ERR;
			} else if (FuiouTraceType.PAY.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.PAY_ERR;
			} else if (FuiouTraceType.REPAY_INTEREST.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.REPAY_INTEREST_ERR;
			} else if (FuiouTraceType.REPAY_PRINCIPAL.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.REPAY_PRINCIPAL_ERR;
			}
		}
		// 记录流水
		fuiouTraceService.save(ssn, fuiouId, userCode, amount, fuiouTraceType);
		return comm;
	}
	/**
	 * 存管划拨(个人与个人之间)
	 * 
	 * @param payAmount
	 *            交易金额
	 * @param Outuser
	 *            交易出账用户
	 * @param Inuser
	 *            交易入账用户
	 * @param fuiouTraceType
	 *            交易类型: INCOME-收入; PAY-支出; REPAY_INTEREST-回收利息; REPAY_PRINCIPAL-
	 *            回收本金; MANUAL_RECHARGE-人工充值(公账与个人账户)
	 * @param loanCode 标编号
	 * @return 交易返回信息
	 */
	public CommonRspData  transferBu(Long payAmount,FuiouTraceType fuiouTraceType,User Outuser,User Inuser,String loanCode,String remark){
		CommonRspData comm=null;
		String amount = String.valueOf(payAmount);
		String outuserCode=Outuser.getStr("userCode");
		String inuserCode=Inuser.getStr("userCode");
		String outFuiouId = getFuiouUid(Outuser);
		String inFuiouId = getFuiouUid(Inuser);
		TransferBmuReqData transfe = new TransferBmuReqData();
		transfe.setAmt(amount);
		transfe.setVer(CommonUtil.VER);
		String ssn=CommonUtil.genMchntSsn();
		transfe.setMchnt_txn_ssn(ssn);
		transfe.setMchnt_cd(CommonUtil.MCHNT_CD);
		transfe.setOut_cust_no(outFuiouId);
		transfe.setIn_cust_no(inFuiouId);
		try {
			 comm=FuiouService.transferBu(transfe);
			//记录流水
		} catch (Exception e) {
			System.out.println(inFuiouId+"用户划拨系统错误");
		}
		if(!"0000".equals(comm.getResp_code())){
			if (FuiouTraceType.INCOME.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.INCOME_ERR;
			} else if (FuiouTraceType.PAY.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.PAY_ERR;
			} else if (FuiouTraceType.REPAY_INTEREST.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.REPAY_INTEREST_ERR;
			} else if (FuiouTraceType.REPAY_PRINCIPAL.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.REPAY_PRINCIPAL_ERR;
			} else if (FuiouTraceType.R.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.S;
			} else if (FuiouTraceType.T.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.U;
			} else if (FuiouTraceType.V.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.W;
			} else if (FuiouTraceType.I.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.J;
			} else if (FuiouTraceType.G.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.H;
			} else if (FuiouTraceType.MANUAL_RECHARGE.equals(fuiouTraceType)) {
				fuiouTraceType = FuiouTraceType.O;
			} else{
				fuiouTraceType = FuiouTraceType.Z;
			}
		}
		fuiouTraceService.save(ssn, outFuiouId, outuserCode,inFuiouId, inuserCode, amount, fuiouTraceType,loanCode,remark);
		return comm;
	}
	/**
	 * 查询存管平台账户余额
	 * 
	 * @param user
	 * @return
	 */
	public QueryBalanceResultData queryBalance(User user) {
		String loginid = null;
		QueryBalanceRspData queryBalanceRspData = null;
		try {
			loginid = CommonUtil.decryptUserMobile(user.getStr("loginId"));
			QueryBalanceReqData queryBalanceReqData = new QueryBalanceReqData();
			queryBalanceReqData.setCust_no(loginid);
			queryBalanceReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			String mchnt_txn_ssn = CommonUtil.genMchntSsn();// 交易流水号
			queryBalanceReqData.setMchnt_txn_ssn(mchnt_txn_ssn);
			queryBalanceReqData.setMchnt_txn_dt(DateUtil.getNowDate());
			queryBalanceRspData = FuiouService.balanceAction(queryBalanceReqData);
		} catch (Exception e2) {
			return new QueryBalanceResultData();
		}
		QueryBalanceResultData queryB = queryBalanceRspData.getResults().get(0);
		return queryB;
	}
	
	public static QueryBalanceResultData balanceQuery(String loginId) {
		QueryBalanceRspData queryBalanceRspData = null;
		try {
			loginId = CommonUtil.decryptUserMobile(loginId);
			QueryBalanceReqData queryBalanceReqData = new QueryBalanceReqData();
			queryBalanceReqData.setCust_no(loginId);
			queryBalanceReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			String mchnt_txn_ssn = CommonUtil.genMchntSsn();// 交易流水号
			queryBalanceReqData.setMchnt_txn_ssn(mchnt_txn_ssn);
			queryBalanceReqData.setMchnt_txn_dt(DateUtil.getNowDate());
			queryBalanceRspData = FuiouService.balanceAction(queryBalanceReqData);
		} catch (Exception e2) {
			return new QueryBalanceResultData();
		}
		QueryBalanceResultData queryB = queryBalanceRspData.getResults().get(0);
		return queryB;
	}

	/**
	 * 查询用户是否已开通存管账号
	 * 
	 * @return
	 */
	public static boolean isFuiouAccount(User user) {
		if (user == null) {
			return false;
		}
		if (!StringUtil.isBlank(user.getStr("loginId"))) {
			String fuiouLoginId = user.getStr("loginId");
			try {
				fuiouLoginId = CommonUtil.decryptUserMobile(fuiouLoginId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			QueryUserInfsReqData queryUserInfsReqData = new QueryUserInfsReqData();
			queryUserInfsReqData.setVer(FuiouController.VER);
			queryUserInfsReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			queryUserInfsReqData.setMchnt_txn_dt(DateUtil.getNowDate());
			queryUserInfsReqData.setMchnt_txn_ssn(CommonUtil.genMchntSsn());
			queryUserInfsReqData.setUser_ids(fuiouLoginId);
			try {
				QueryUserInfsRspData queryUserInfsRspData = FuiouService.queryUserInfs(queryUserInfsReqData);
				List<QueryUserInfsRspDetailData> qqqDatas = queryUserInfsRspData.getResults();
				if (null == qqqDatas.get(0).getMobile_no() || "".equals(qqqDatas.get(0).getMobile_no())) {
					return false;
				}
			} catch (Exception e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 查询平台资金信息与存管户是资金信息是否同步
	 * 
	 * @param userCode
	 * @return true - 同步 false - 不同步
	 */
	public boolean isSync(String userCode) {
		User user = userService.findById(userCode);
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		QueryBalanceResultData fuiouFunds = fuiouTraceService.BalanceFunds(user);
		if (funds != null && fuiouFunds != null) {
			if (funds.getLong("avBalance") == Long.parseLong(fuiouFunds.getCa_balance())
					|| funds.getLong("frozeBalance") == Long.parseLong(fuiouFunds.getCf_balance())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 同步存管户资金到平台账号
	 * 
	 * @param userCode
	 * @return 同步成功返回Funds 同步失败返回null
	 */
	public Funds syncAccount(String userCode) {
		User user = userService.findById(userCode);
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		QueryBalanceResultData fuiouFunds = fuiouTraceService.BalanceFunds(user);
		if (funds != null && fuiouFunds != null) {
			funds = fundsServiceV2.syncAccount(userCode, Long.parseLong(fuiouFunds.getCa_balance()),
					Long.parseLong(fuiouFunds.getCf_balance()));
			return funds;
		}
		return null;
	}

	/**
	 * 同步银行卡 ws 20170607
	 */
	public void synBankCard(User user) {
		String fuiouUid = getFuiouUid(user);
		if (!StringUtil.isBlank(fuiouUid)) {
			String userCode = user.getStr("userCode");
			BanksV2 banksV2 = banksService.findBanks4User(userCode).get(0);
			String bankNo = banksV2.getStr("bankNo");
			
			// 提交查询请求
			QueryUserInfsReqData queryUserInfsReqData = new QueryUserInfsReqData();
			queryUserInfsReqData.setVer(VER);
			queryUserInfsReqData.setMchnt_cd(MCHNT_CD);
			queryUserInfsReqData.setMchnt_txn_dt(DateUtil.getNowDate());
			queryUserInfsReqData.setMchnt_txn_ssn(CommonUtil.genMchntSsn());
			queryUserInfsReqData.setUser_ids(fuiouUid);
			QueryUserInfsRspData queryUserInfsRspData = null;
			try {
				queryUserInfsRspData = FuiouService.queryUserInfs(queryUserInfsReqData);
				List<QueryUserInfsRspDetailData> lstQueryUserInfsRspDetailData = queryUserInfsRspData.getResults();
				QueryUserInfsRspDetailData fuiouInfo = null;
				if (lstQueryUserInfsRspDetailData.size() > 0) {
					fuiouInfo = lstQueryUserInfsRspDetailData.get(0);
					String fuiouBankNo = fuiouInfo.getCapAcntNo();	// 获取存管银行卡号
					if (!bankNo.equals(fuiouBankNo)) {
						String cityId = fuiouInfo.getCity_id();	// 开户行地区码
						String bankId = fuiouInfo.getParent_bank_id();	// 开户行行别
						BankCode bankCode = BankCode.bankCodeDao.findFirst("select * from t_bank_Code where bankCode=?", bankId);
						BankOreaCode bankOreaCode = BankOreaCode.bankOreaCodeDao.findFirst("select * from t_bank_OreaCode where cityCode=?", cityId);
						String bankName = bankCode.getStr("bankName");
						String cityName = bankOreaCode.getStr("provinceName") + "|" + bankOreaCode.getStr("cityName");
						String biz_content = "银行卡修改：用户 " + userCode + " ,理财卡由【" + bankNo + "|" + banksV2.getStr("bankType")
								+ "|" + banksV2.getStr("bankName") + "】";
						banksV2.set("bankNo", fuiouBankNo);
						banksV2.set("bankName", bankName);
						banksV2.set("cardCity", cityName);
						banksV2.set("bankType", bankId);
						banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
						boolean b = banksV2.update();
						if (b == true) {
							biz_content += "修改为【" + fuiouBankNo + "|" + bankName + "|" + bankCode + "】";
							BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, biz_content);
						}
					}
				}
			} catch (Exception e) {
			}
		}
	}

	// 更换手机号通知接口
	@ActionKey("/changUserMobileSign")
	@AuthNum(value = 999)
	public void changUserMobileSign() {
		String resp_code = getPara("resp_code");
		redirect("/modifyHfMobile?code="+resp_code, true);
	}

	/**
	 * 充值提现详情查询接口
	 * @param ssn	充值提现交易流水号
	 * @param busiType	交易类型 FuiouTraceType.RECHARGE-充值；FuiouTraceType.WITHDRAW-提现
	 * @return
	 */
	public QueryCzTxRspDetailData queryCzTxDetail(String ssn, FuiouTraceType busiType) {
		RechargeTrace rechargeTrace = null;
		WithdrawTrace withdrawTrace = null;
		String startTime = null;
		String endTime = null;
		String traceDate = null;
		String userCode = null;
		String fuiouLoginId = null;
		
		if (FuiouTraceType.RECHARGE.equals(busiType)) {
			rechargeTrace = rechargeTraceService.findById(ssn);
			if (rechargeTrace != null) {
				traceDate = DateUtil.getStrFromDate(DateUtil.getDateFromString(rechargeTrace.getStr("traceDate"), "yyyyMMdd"), "yyyy-MM-dd");
				userCode = rechargeTrace.getStr("userCode");
			}
		}
		if (FuiouTraceType.WITHDRAW.equals(busiType)) {
			withdrawTrace = withdrawTraceService.findById(ssn);
			if (withdrawTrace != null) {
				traceDate = DateUtil.getStrFromDate(DateUtil.getDateFromString(withdrawTrace.getStr("createDateTime"), "yyyyMMddHHmmss"), "yyyy-MM-dd");
				userCode = withdrawTrace.getStr("userCode");
			}
		}
		if (rechargeTrace == null && withdrawTrace == null) {
			return null;
		}
		startTime = traceDate + " 00:00:00";
		endTime = traceDate + " 23:59:59";
		User user = userService.findById(userCode);
		try {
			fuiouLoginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		QueryCzTxReq queryCzTxReq = new QueryCzTxReq();
		queryCzTxReq.setVer(FuiouController.VER);
		queryCzTxReq.setMchnt_cd(FuiouController.MCHNT_CD);	// 商户代码
		queryCzTxReq.setMchnt_txn_ssn(CommonUtil.genMchntSsn());	// 流水号
		queryCzTxReq.setBusi_tp(busiType.val());	// 交易类型
		queryCzTxReq.setTxn_ssn(ssn);	// 交易流水号
		queryCzTxReq.setStart_time(startTime);	// 起始时间
		queryCzTxReq.setEnd_time(endTime);	// 截止时间
		queryCzTxReq.setCust_no(fuiouLoginId);	// 用户
		queryCzTxReq.setPage_no("1");
		queryCzTxReq.setPage_size("50");
		QueryCzTxRspData queryCzTxRspData = null;
		QueryCzTxRspDetailData queryCzTxRspDetailData = null;
		try {
			queryCzTxRspData = FuiouService.querycztx(queryCzTxReq);
			if ("0000".equals(queryCzTxRspData.getResp_code())) {
				List<QueryCzTxRspDetailData> lstQueryCzTxRspDetailData = queryCzTxRspData.getResults();
				if (lstQueryCzTxRspDetailData != null && lstQueryCzTxRspDetailData.size() > 0) {
					queryCzTxRspDetailData = lstQueryCzTxRspDetailData.get(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return queryCzTxRspDetailData;
	}

	/**
	 * 快捷充值接口
	 * @param object
	 * @param busiFrom	phone-移动端; pc-pc端 
	 * @param response
	 */
	public void fastRecharge(Map<String, String> object, String busiFrom, HttpServletResponse response) {
		if ("phone".equals(busiFrom)) {	// 移动端
			AppTransReqData appTransReqData = new AppTransReqData();
			appTransReqData.setAmt(object.get("amt"));
			appTransReqData.setLogin_id(object.get("fuiouLoginId"));
			appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			appTransReqData.setMchnt_txn_ssn(object.get("no_order"));
			appTransReqData.setPage_notify_url(object.get("page_notify_url"));
			try {
				FuiouService.app500002(appTransReqData, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if ("pc".equals(busiFrom)){	// pc端
			P2p500405ReqData p2p500405ReqData = new P2p500405ReqData();
			p2p500405ReqData.setAmt(object.get("amt"));
			p2p500405ReqData.setLogin_id(object.get("fuiouLoginId"));
			p2p500405ReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			p2p500405ReqData.setMchnt_txn_ssn(object.get("no_order"));
			p2p500405ReqData.setPage_notify_url(object.get("page_notify_url"));
			try {
				FuiouService.p2p500405(p2p500405ReqData, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
