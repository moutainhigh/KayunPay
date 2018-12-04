package com.dutiantech.controller.portal;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BankCode;
import com.dutiantech.model.BankOreaCode;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.ChangeBankTrace;
import com.dutiantech.model.Funds;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.fuiou.data.ChangeCard2ReqData;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.RegReqData;
import com.fuiou.data.ResetPassWordReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jx.http.WebUtils;

public class BankV2Controller extends BaseController {

	private BanksService banksService = getService(BanksService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private UserService userService = getService(UserService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);

//	@ActionKey("/bankv2_save")
//	@AuthNum(value = 999)
//	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
//	public Message saveNewBankV2() {
//
//		String smsCode = getPara("smsCode");
//		String userCode = getUserCode();
//		if (StringUtil.isBlank(smsCode) == true) {
//			return error("01", "短信验证码不能为空", "");
//		}
//
//		// 验证短信验证码
//		if (CommonUtil.validateSMS("SMS_MSG_BINDCARD_" + userCode, smsCode) == false) {
//			// 记录日志
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 ,短信验证码不正确", null);
//			return error("02", "短信验证码不正确", "");
//		}
//
//		BanksV2 bankV2 = banksService.findByUserCode(userCode);
//		if (bankV2 != null) {
//			return error("03", "已绑定理财卡，无需重复绑定!", null);
//		}
//
//		bankV2 = getModel(BanksV2.class);
//		if (StringUtil.isBlank(bankV2.getStr("bankNo"))) {
//			return error("11", "未填写银行卡号", "");
//		}
//		if (StringUtil.isBlank(bankV2.getStr("bankType"))) {
//			return error("11", "银行卡类型异常", "");
//		}
//
//		// 查询卡信息
//		Map<String, String> bankInfo = banksService.queryBankInfoByBin(bankV2.getStr("bankNo"));
//		bankV2.set("bankName", bankInfo.get("bank_name"));
//		bankV2.set("bankType", bankInfo.get("bank_code"));
//
//		UserInfo uinfo = UserInfo.userInfoDao.findById(userCode);
//		String cardId = uinfo.getStr("userCardId");
//		String trueName = uinfo.getStr("userCardName");
//		// String userName = uinfo.getStr("");
//		// String mobileNo = uinfo.getStr("");
//
//		bankV2.set("userCode", userCode);
//		bankV2.set("trueName", trueName);
//		bankV2.set("cardid", cardId);
//		bankV2.set("mobile", "000");
//		bankV2.set("createDateTime", DateUtil.getNowDateTime());
//		bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
//		bankV2.set("isDefault", "1");
//		bankV2.set("status", "0");
//		bankV2.set("agreeCode", UIDUtil.generate());
//
//		if (bankV2.save()) {
//			// 绑卡成功，获取可用积分
//			fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
//			return succ("ok", "添加成功");
//		}
//
//		return error("11", "添加失败", null);
//	}

	// 存管系统绑卡开户
	@ActionKey("/bankv3_save")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message saveNewBankV2() {
		String userCode = getUserCode();
		String bankNo = getPara("bankNo");
		String bankName = getPara("bankName");
		String cityName = getPara("CityName");
		String provinceName = getPara("provinceName");
		String bankCode = getPara("bankCode");
		String cityCode = getPara("CityCode");
		String smsCode = getPara("smsCode");
		String trueName = getPara("trueName");// 真实姓名
		String cardId = getPara("cardId", "");// 身份证号
		String payPwd = getPara("payPwd", "");// 平台支付密码

		String userCardName = null;
		String userCardId = null;
		String mobile = null;
		UserInfo userInfo = null;
		BanksV2 banksV2 = null;

		// 验证短信验证码
		if (StringUtil.isBlank(smsCode) == true) {
			return error("01", "短信验证码不能为空", "");
		}
		if (CommonUtil.validateSMS("SMS_MSG_BINDCARD_" + userCode, smsCode) == false) {
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 ,短信验证码不正确", null);
			return error("02", "短信验证码不正确", "");
		}

		// 验证用户是否已实名认证
		userInfo = userInfoService.findById(userCode);
		if (userInfo == null || !"2".equals(userInfo.getStr("isAuthed"))) { // 如果用户未实名认证，则验证前台身份认证参数
			if (!IdCardUtils.validateCard(cardId)) {
				return error("03", "身份证号不正确!", "");
			}
			// 验证身份证是否已经被认证
			UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
			if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
				return error("05", "身份证号已被认证", "");
			}
			userCardName = trueName;
			userCardId = cardId;
		} else {
			try {
				userCardName = userInfo.getStr("userCardName");
				userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			} catch (Exception e) {
				return error("00", "解析用户身份证号错误", "");
			}
		}

		if (StringUtil.isBlank(userCardName)) {
			return error("00", "真实姓名不能为空", null);
		}

		// 验证用户
		User user = getUser();
		if (null == user) {
			return error("07", "用户查询错误", "");
		}

		// 验证银行卡是否已被绑定
		banksV2 = banksService.findByBankNo(bankNo);
		if (banksV2 != null) {
			if (!userCode.equals(banksV2.getStr("userCode"))) {
				return error("08", "该银行卡已被使用", "");
			}
		}

		// 验证银行代码
		if (StringUtil.isBlank(bankCode)) {
			bankCode = CommonUtil.checkBankCode(bankName); // 银行代码为空，则通过银行名称查找银行卡代码
			if (StringUtil.isBlank(bankCode)) {
				return error("12", "绑定银行卡失败，银行代码错误", "");
			}
		}

		// 验证地区代码
		if (StringUtil.isBlank(cityCode)) {
			String[] cc = CommonUtil.checkCity(provinceName);
			if (null == cc) {
				return error("13", "绑定银行卡失败，地区代码错误", "");
			}
			cityName = cc[0];
			cityCode = cc[1];
		}

		// 手机号与身份证号解密
		try {
			mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
		} catch (Exception e) {
			return error("09", "系统错误", "");
		}

		// 查询是否已开存管户
		boolean isDepositAccount = false;
		if (CommonUtil.jxPort) {
			// TODO 江西银行存管绑卡
		} else if (CommonUtil.fuiouPort) {
			isDepositAccount = FuiouController.isFuiouAccount(user);
		} else {
			return error("99", "存管系统对接中...", "");
		}
		
		// 缓存用户信息
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		// 存管通道选择
		if (CommonUtil.jxPort) {
			// TODO 江西银行存管绑卡
		} else if (CommonUtil.fuiouPort) {
			FuiouController fuiouController = new FuiouController();
			try {
				if (isDepositAccount) {
					String traceCode = CommonUtil.genMchntSsn();
					CommonRspData commonRspData = null;
					RegReqData regReqData = new RegReqData();
					regReqData.setVer(FuiouController.VER);
					regReqData.setMchnt_cd(FuiouController.MCHNT_CD);// 商户号
					regReqData.setMchnt_txn_ssn(traceCode);// 流水号
					regReqData.setCust_nm(userCardName);// 用户名
					regReqData.setCertif_tp("0");// 证件类型
					regReqData.setCertif_id(userCardId);// 身份证号
					regReqData.setMobile_no(mobile);// 手机号
					regReqData.setCity_id(cityCode);// 银行卡地区号
					regReqData.setParent_bank_id(bankCode);// 银行代码
					regReqData.setCapAcntNo(bankNo);// 银行卡号
					String fuiouPayPwd = mobile.substring(mobile.length() - 6, mobile.length());// 登录密码（默认loginid后6位）
					try {
						regReqData.setLpassword(CommonUtil.encryptPasswd(fuiouPayPwd));// 登录密码密文
					} catch (UnsupportedEncodingException e1) {
						return error("14", "提交开户信息失败", "");
					}
					
					// 调用开户接口
					commonRspData = FuiouService.reg(regReqData);
					if ("0000".equals(commonRspData.getResp_code())) { // 存管账户开通成功
						// 更新平台数据库存数据
						banksV2 = BanksV2.bankV2Dao.findById(userCode);
						if (banksV2 != null) {
							banksV2.set("trueName", userCardName);
							banksV2.set("cardCity", provinceName + "|" + cityName);
							banksV2.set("ssn", traceCode);
							banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
							if (banksV2.update()) {
								user.set("loginId", user.getStr("userMobile"));
								user.update();
							} else {
								return error("15", "激活银行卡失败，本地数据添加失败", null);
							}
						} else { // 添加银行卡信息
							BanksV2 bankV2 = getModel(BanksV2.class);
							bankV2.set("bankName", bankName);
							bankV2.set("bankNo", bankNo);
							bankV2.set("bankType", bankCode);
							bankV2.set("cardCity", provinceName + "|" + cityName);
							bankV2.set("userCode", userCode);
							bankV2.set("userName", user.getStr("userName"));
							bankV2.set("trueName", userCardName);
							bankV2.set("cardid", CommonUtil.encryptUserCardId(userCardId));
							bankV2.set("mobile", "000");
							bankV2.set("createDateTime", DateUtil.getNowDateTime());
							bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
							bankV2.set("isDefault", "1");
							bankV2.set("status", "0");
							bankV2.set("agreeCode", traceCode);
							bankV2.set("ssn", traceCode);
							if (bankV2.save()) {
								user.set("loginId", user.getStr("userMobile"));
								if (user.update()) {
									// 绑卡成功，获取可用积分
//									fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
									resultMap.put("loginId", user.getStr("loginId"));
								}
							} else {
								return error("16", "本地数据更新失败", null);
							}
						}
						// 非借款人账户 转账
						if (!"J".equals(user.getStr("userType"))) {
							Funds funds = fundsServiceV2.findById(userCode);
							long avBalance = funds.getLong("avBalance");
							long frozeBalance = funds.getLong("frozeBalance");
							if (avBalance + frozeBalance > 0) {
								QueryBalanceResultData queryBalanceResultData = fuiouController.queryBalance(user);
								if (Long.parseLong(queryBalanceResultData.getCa_balance()) == 0) {
									CommonRspData comm = fuiouController.transferBmu(avBalance + frozeBalance, user,
											FuiouTraceType.INCOME);
									if ("0000".equals(comm.getResp_code())) {
										if (frozeBalance > 0) {
											fuiouController.freeze(user, frozeBalance);
										}
									} else {
										user.set("loginId", "");
										user.update();
										return error("17", "开通失败，存管资金充入失败", null);
									}
								}
								QueryBalanceResultData queryB = fuiouTraceService.BalanceFunds(user);
								if (Long.parseLong(queryB.getCa_balance()) == 0) {
									CommonRspData comm = fuiouTraceService.gorefund(avBalance + frozeBalance, user,
											FuiouTraceType.E);
									if ("0000".equals(comm.getResp_code())) {
										if (frozeBalance > 0) {
											fuiouTraceService.freeze(user, frozeBalance);
										}
									} else {
										user.set("loginId", "");
										user.update();
										return error("17", "开通失败，存管资金充入失败", null);
									}
								}
							}
						}

						// 本地实名认证、支付密码补充
						if (!"2".equals(userInfo.getStr("isAuthed"))) {
							Message msgAuto = certificationAuto(userCode, trueName,
									CommonUtil.encryptUserCardId(userCardId), "");// 实名认证
							if (msgAuto != null) {
								return msgAuto;
							}
						}
						if (!StringUtil.isBlank(payPwd)) {
							Message msgPayPwd = updatePayPwd(userCode, user, payPwd);// 设置平台支付密码
							if (msgPayPwd != null) {
								return msgPayPwd;
							}
						}

						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "激活账户成功 ");
						Memcached.set("PORTAL_USER_" + userCode, user);
						resultMap.put("result_info", "激活开通成功");
						return succ("ok", resultMap);
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR,
								commonRspData.getResp_desc() + "&" + commonRspData.getMchnt_txn_ssn(), null);
						return error("18", "添加失败", commonRspData.getResp_code());
					}
				}
			} catch (Exception e) {
				return error("20", "系统错误，添加失败", null);
			}
		} else {
			return error("99", "存管系统对接中...", "");
		}
		return null;
	}

	@ActionKey("/bankv2_list")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message queryBankList() {
		String userCode = getUserCode();
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if (banksV2 == null) {
			banksV2 = new BanksV2();
		} else {
			banksV2.remove("modifyDateTime");
			banksV2.remove("payType");
			banksV2.remove("mobile");
			banksV2.remove("cardid");
			banksV2.remove("bankMac");
			banksV2.remove("agreeCode");
			banksV2.remove("createDateTime");
			banksV2.remove("uid");
		}
		return succ("ok", banksV2);
	}

	// 查询银行代码 地区代码
	@ActionKey("/querBankName")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message querBankCodes() {
		List<BankCode> tmp = BankCode.bankCodeDao.find("select bankCode,bankName from t_bank_Code ");
		return succ("bankcodes", tmp);
	}

	@ActionKey("/queryBankProvinceCodes")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message queryBankProvinceCodes() {
		List<BankOreaCode> tmp = BankOreaCode.bankOreaCodeDao.find(
				"SELECT provinceCode,provinceName from t_bank_OreaCode GROUP BY provinceName ORDER BY provinceCode ");
		return succ("province", tmp);
	}

	@ActionKey("/queryBankCityCodes")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message queryBankCityCodes() {
		String provinceCode = getPara("provinceCode");
		List<BankOreaCode> tmp = BankOreaCode.bankOreaCodeDao.find(
				"SELECT cityCode ,cityName from t_bank_OreaCode where provinceCode=? ORDER BY cityCode", provinceCode);
		return succ("city", tmp);
	}

	// 更换存管支付密码
	@ActionKey("/changefuiouPayPwd")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void changefuiouPayPwd() {
		User user = getUser();
		String loginId = user.getStr("loginId");
		if (null != loginId && !"".equals(loginId)) {
			try {
				loginId = CommonUtil.decryptUserMobile(loginId);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			ResetPassWordReqData resetPassWordReqData = new ResetPassWordReqData();
			resetPassWordReqData.setLogin_id(loginId);
			resetPassWordReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
			String ssn = CommonUtil.genShortUID();
			resetPassWordReqData.setMchnt_txn_ssn(ssn);
			resetPassWordReqData.setBusi_tp("3");
			resetPassWordReqData.setBack_url(CommonUtil.ADDRESS + "/C01");
			try {
				FuiouService.resetPassWord(resetPassWordReqData, getResponse());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		renderNull();
	}

	// 查询用户开通存管相关信息
	@ActionKey("/checkUserBankInfor")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message checkUserBankInfor() {
		String userCode = getUserCode();
		BanksV2 bankv2 = BanksV2.bankV2Dao.findById(userCode);
		Map<String, Object> result = new HashMap<String, Object>();
		String cardid = bankv2.getStr("cardid");
		try {
			cardid = CommonUtil.decryptUserCardId(cardid);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String cardNo = bankv2.getStr("bankNo");
		result.put("trueName", bankv2.getStr("trueName"));
		result.put("userCardId", cardid);
		result.put("cardNo", cardNo);
		return succ("查询成功", result);
	}

	/**
	 * 跳转富友更换银行卡页面
	 */
	@ActionKey("/changeBankV2")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void changeBankV2() {
		String userCode = getUserCode();
		User user = getUser();
		BanksV2 banksV2 = banksService.findBanks4User(userCode).get(0);
		String loginId = user.getStr("loginId");
		try {
			loginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		if (null != loginId && !"".equals(loginId)) {
			ChangeCard2ReqData changeCard = new ChangeCard2ReqData();
			changeCard.setLogin_id(loginId);
			changeCard.setMchnt_cd(CommonUtil.MCHNT_CD);
			String ssn = CommonUtil.genMchntSsn();
			changeCard.setMchnt_txn_ssn(ssn);
			changeCard.setPage_notify_url(CommonUtil.ADDRESS + "/index");
			// 添加修改银行卡信息记录
			ChangeBankTrace changebankTrace = new ChangeBankTrace();
			changebankTrace.set("userCode", userCode);
			changebankTrace.set("ssn", ssn);
			changebankTrace.set("state", "5");
			changebankTrace.set("creatDate", DateUtil.getNowDateTime());
			changebankTrace.set("upDate", DateUtil.getNowDateTime());
			changebankTrace.set("oldBankCardId", banksV2.getStr("bankNo"));
			if (changebankTrace.save()) {
				try {
					FuiouService.changeCard2(changeCard, getResponse());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		renderNull();

	}

	/**
	 * 实名认证
	 * 
	 * @param userCode
	 * @param trueName
	 * @param cardId
	 * @return
	 */
	private Message certificationAuto(String userCode, String trueName, String md5CardId, String idType) {
		// 次数限制
		Long count = Db.queryLong("select count(1) from t_auth_log where userCode = ?", userCode);
		if (count > 3) {
			return error("01", "认证次数超限制", "");
		}

		boolean update = userInfoService.newUserAuth(userCode, trueName, md5CardId, "", "2", idType);
		if (!update) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
			return error("02", "已经认证,请勿重复提交!", "");
		}

		// 记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户自动认证成功");

		// 身份认证成功赠送可用积分
//		fundsServiceV2.doPoints(userCode, 0, 2000, "注册送积分");

		// 身份认证后，邀请人添加30元现金抵用券 5月活动,9月继续，常态
		try {
			User user = userService.findById(userCode);
			RecommendInfo rmd = RecommendInfo.rmdInfoDao.findFirst("select * from t_recommend_info where bUserCode = ?",
					user.getStr("userCode"));
			if (rmd != null) {
				User shareUser = userService.findById(rmd.getStr("aUserCode"));
				if (shareUser != null) {
					// 实名认证送券
					boolean aa = ticketService.saveADV(shareUser.getStr("userCode"), "50元现金券【好友实名认证奖励】",
							DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
					if (aa) {
						String mobile = userService.getMobile(shareUser.getStr("userCode"));
						String content = CommonUtil.SMS_MSG_TICKET.replace("[huoDongName]", "推荐好友实名认证")
								.replace("[ticketAmount]", "50");
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", content);
						smsLog.set("userCode", shareUser.getStr("userCode"));
						smsLog.set("userName", shareUser.getStr("userName"));
						smsLog.set("type", "15");
						smsLog.set("typeName", "送现金券活动");
						smsLog.set("status", 9);
						smsLog.set("sendDate", DateUtil.getNowDate());
						smsLog.set("sendDateTime", DateUtil.getNowDateTime());
						smsLog.set("break", "");
						smsLogService.save(smsLog);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 设置平台支付密码
	 * 
	 * @return
	 */
	private Message updatePayPwd(String userCode, User user, String payPwd) {
		try {
			payPwd = CommonUtil.encryptPasswd(payPwd);
			user.set("payPasswd", payPwd);
			user.update();
		} catch (Exception e) {
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USER, "修改支付密码异常", e);
			return error("01", "修改支付密码异常", e.getMessage());
		}

		// 记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改支付密码成功");

		return null;
	}

	/**
	 * 调用多合一授权接口
	 * 缴费授权(页面调用) WJW
	 * 
	 * @return
	 */
	@ActionKey("/paymentAuthPage")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void paymentAuthPage() {
		String maxAmt = getPara("maxAmt","20000");// 签约最大金额,单位：元，最多两位小数
		String deadline = getPara("deadline",DateUtil.updateDate(new Date(), 5, Calendar.YEAR, "yyyyMMdd"));// 签约到期日
		String userCode = getUserCode();// 用户编号

		if (StringUtil.isBlank(maxAmt)) {
			WebUtils.writePromptHtml("签约最大金额为空", "/C01", "UTF-8",getResponse());
			return;
		}
		if (StringUtil.isBlank(deadline)) {
			WebUtils.writePromptHtml("签约到期日为空", "/C01", "UTF-8",getResponse());
			return;
		}
		if (StringUtil.isBlank(userCode)) {
			WebUtils.writePromptHtml("请重新登录", "/C01", "UTF-8",getResponse());
			return;
		}
		User user = userService.findById(userCode);
		UserInfo userInfo=userInfoService.findById(userCode);
		
		if (user == null) {
			WebUtils.writePromptHtml("用户不存在", "/C01", "UTF-8",getResponse());
			return;
		}
		if(userInfo == null){
			WebUtils.writePromptHtml("用户认证信息异常", "/C01", "UTF-8", getResponse());
		}
		String jxAccountId = user.getStr("jxAccountId");// 用户银行存管电子账号
		if (StringUtil.isBlank(jxAccountId)) {
			WebUtils.writePromptHtml("请先开通江西银行存管", "/C01", "UTF-8",getResponse());
			return;
		}
		String name=userInfo.getStr("userCardName");
		String idNo="";
		try {
			idNo=CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			WebUtils.writePromptHtml("身份证账户解析异常", "/C01", "UTF-8",getResponse());
			return;
		}
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String retUrl=CommonUtil.ADDRESS+"/C01";
		String notifyUrl=CommonUtil.CALLBACK_URL + "/paymentAuthPageCallback";
		JXController.termsAuthPage(jxAccountId, name, idNo, "1", "", "", "1", "", "", "", "", "", maxAmt, deadline, "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(),"");
	}

	/**
	 * 实名认证、开存管户对接江西银行
	 */
	@ActionKey("/jxDepositOpenAccount")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void jxDepositOpenAccount() {
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		User user = getUser();
		if (null == user) {
			msg = error("01", "用户查询错误", "");
			renderJson(msg);
			return;
		}
		String userCode = user.getStr("userCode");
		String trueName = getPara("trueName", "");// 真实姓名
		String cardId = getPara("cardId", "");// 身份证号
		String mobile = getPara("mobile", "");

		String userCardName = null;
		String userCardId = null;

		UserInfo userInfo = null;
		if (cardId == null || cardId.length() != 18) {
			msg = error("02", "身份证不是18位", null);
			renderJson(msg);
			return;
		}
		if (mobile == null || mobile.length() != 11) {
			msg = error("03", "手机号不是11位", null);
			renderJson(msg);
			return;
		}
		// 验证用户是否已实名认证
		userInfo = userInfoService.findById(userCode);
		// 证件类型——投资人为默认为身份证
		String idType = "01";
		if (userInfo == null || !"2".equals(userInfo.getStr("isAuthed"))) {// 若未实名认证，则验证身份参数
			if (!IdCardUtils.validateCard(cardId)) {
				msg = error("04", "身份证号不正确！", "");
				renderJson(msg);
				return;
			}
			// 验证身份证是否已经被认证
			UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
			if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
				msg = error("05", "身份证号已被认证", "");
				renderJson(msg);
				return;
			}
			userCardName = trueName;
			userCardId = cardId;

		} else {// 若已实名
			try {
				userCardName = userInfo.getStr("userCardName");
				userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			} catch (Exception e) {
				msg = error("06", "用户身份证号解析错误", "");
				renderJson(msg);
				return;
			}
		}

		if (StringUtil.isBlank(userCardName)) {
			msg = error("07", "真实姓名不能为空", null);
			renderJson(msg);
			return;
		}

		if(!CommonUtil.isMobile(mobile.trim())){
			msg = error("08", "手机号校验失败", "");
			renderJson(msg);
			return;
		}
		
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("openAccount_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			// 查询用户是否已开存管户-按证件号查询电子账号
			Map<String, String> accountIdQuery = null;
			try {
				// 根据证件号查询存管电子账号
				accountIdQuery = JXQueryController.accountIdQuery(idType, userCardId);
			} catch (Exception e) {
				msg = error("09", "存管系统异常", "");
				renderJson(msg);
				return;
			}
			if (accountIdQuery != null && !StringUtil.isBlank(accountIdQuery.get("accountId"))) {
				msg = error("10", "存管账户已存在", "");
				renderJson(msg);
				return;
			}
			
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("openAccount_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("openAccount_"+verifyOrderId+userCode);
		HttpServletResponse response = getResponse();
		int type = 1;
		// 账户用途：00000-普通账户 投资人默认
		String acctUse = "";
		// 身份属性：1：出借角色 2：借款角色 3：代偿角色
		String identity = "";
		// 根据身份证编号获取性别
		String gender = IdCardUtils.getGenderByIdCard(userCardId);
		switch (type) {
		case 1:
			acctUse = "00000";// 出借人-普通账户
			identity = "1";
			break;
		case 2:
			acctUse = "00000";// 借款人-普通账户
			identity = "2";
			break;
		case 3:
			acctUse = "00100";// 代偿角色-担保账户
			identity = "3";
			break;
		case 4:
			acctUse = "10000";// 出借人-红包账户
			identity = "1";
			break;
		case 5:
			acctUse = "01000";// 出借人-手续费账户
			identity = "1";
			break;
		}
		String retUrl = CommonUtil.ADDRESS;
		String successfulUrl = CommonUtil.ADDRESS+"/depositoryUser?step=3";
		String notifyUrl = CommonUtil.CALLBACK_URL + "/openAccountCallback?uCode=" + userCode+"&mobile="+mobile;
		// 调用存管加密开户接口 modified by rain 20180907
		JXController.accountOpenEncryptPage(idType, userCardName, gender, mobile, "", acctUse, "", identity, retUrl, successfulUrl, notifyUrl,response);
	}

	/**
	 * 20180822
	 * 设置存管密码
	 */
	@ActionKey("/setDepositPwd")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void setDepositPwd() {
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		String userCode = getUserCode();
		if (userCode == null) {
			msg = error("01", "未登录", "");
			renderJson(msg);
			return;
		}
		HttpServletResponse response = getResponse();
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);

		if (null == user || null == userInfo) {
			msg = error("01", "未查找到用户信息", "");
			renderJson(msg);
			return;
		}
		String jxAccountId = user.getStr("jxAccountId");
		if (null == jxAccountId || "".equals(jxAccountId)) {
			msg = error("02", "请先开通存管账户再设置存管密码", "");
			renderJson(msg);
			return;
		}
		String mobile = "";
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if(banksV2 == null){
			mobile = user.getStr("userMobile");
		}else{
			mobile = banksV2.getStr("mobile");
		}
		
		String idNo = userInfo.getStr("userCardId");
		String idType = userInfo.getStr("idType");
		if (StringUtil.isBlank(idType)) {
			msg = error("03", "未查找到用户有证件类型", "");
			renderJson(msg);
			return;
		}
		try {
			mobile = CommonUtil.decryptUserMobile(mobile);
			idNo = CommonUtil.decryptUserCardId(idNo);
		} catch (Exception e) {
			msg = error("03", "手机号或证件号解析异常", "");
			renderJson(msg);
			return;
		}
		if (StringUtil.isBlank(mobile) || StringUtil.isBlank(idNo)) {
			msg = error("05", "手机号或证件号为空", "");
			renderJson(msg);
			return;
		}
		String name = userInfo.getStr("userCardName");
		if (null == name || "".equals(name)) {
			msg = error("06", "姓名不能为空", "");
			renderJson(msg);
			return;
		}
		//检查用户是否绑定过银行卡
//				String isBind = JXController.verifyBindCard(jxAccountId, "0");
//				if("n".equals(isBind)){//没有绑定过银行卡,无法设置交易密码
//					msg = error("07", "该用户未绑卡，不能设置交易密码", "");
//					renderJson(msg);
//					return;
//				}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("setPwd_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("setPwd_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("setPwd_"+verifyOrderId+userCode);
		//String successfulUrl = CommonUtil.ADDRESS+"/depositoryUser?step=3&verifyPwd=y";
		String retUrl = CommonUtil.ADDRESS+"/C01";
		String notifyUrl = CommonUtil.CALLBACK_URL + "/setPwdCallback?userCode="+userCode;
		JXController.passwordResetPage(jxAccountId, idType, retUrl, notifyUrl, response);
		renderNull();
	}
	/**
	 * 20180822
	 * 修改存管密码
	 */
	@ActionKey("/modifyDepositPwd")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void modifyDepositPwd() {
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		String userCode = getUserCode();
		if (userCode == null) {
			msg = error("01", "未登录", "");
			renderJson(msg);
			return;
		}
		HttpServletResponse response = getResponse();
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);

		if (null == user || null == userInfo) {
			msg = error("02", "未查找到用户信息", "");
			renderJson(msg);
			return;
		}
		String jxAccountId = user.getStr("jxAccountId");
		if (null == jxAccountId || "".equals(jxAccountId)) {
			msg = error("03", "请先开通存管账户再修改存管密码", "");
			renderJson(msg);
			return;
		}
		String mobile = "";
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if(banksV2 == null){
			mobile = user.getStr("userMobile");
		}else{
			mobile = banksV2.getStr("mobile");
		}
		try {
			mobile = CommonUtil.decryptUserMobile(mobile);
			
		} catch (Exception e) {
			msg = error("04", "手机号解析异常", "");
			renderJson(msg);
			return;
		}
		if (StringUtil.isBlank(mobile) ) {
			msg = error("05", "手机号不能为空", "");
			renderJson(msg);
			return;
		}
		String name = userInfo.getStr("userCardName");
		if (null == name || "".equals(name)) {
			msg = error("06", "姓名不能为空", "");
			renderJson(msg);
			return;
		}
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
			msg = error("07", "未设置过电子账户密码", "");
			renderJson(msg);
			return;
		}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("modifyPwd_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("modifyPwd_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("modifyPwd_"+verifyOrderId+userCode);
		//String successfulUrl = CommonUtil.ADDRESS+"/depositoryUser?step=3&verifyPwd=y";
		String retUrl = CommonUtil.ADDRESS+"/C01";
		String notifyUrl = CommonUtil.CALLBACK_URL + "/modifyPwdCallback?userCode="+userCode;
		JXController.passwordUpdate(jxAccountId, name, retUrl, notifyUrl, response);
		renderNull();
	}
	/**
	 * 20180831起接口失效
	 * 密码重置增强
	 * 
	 * 功能说明：必须曾经设置过电子账户密码，验证
	 * 
	 * 短信验证码密码重置之前要先调用发短信接口
	 */
	@ActionKey("/resetPwdPlus")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message resetPwdPlus(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		if(user == null){
			return error("01", "用户未登录", "");
		}
		String smsCode = getPara("smsCode","");//短信验证码
		//前导业务授权码
		String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_RESETPWD_"+user.getStr("userCode"));
		if(StringUtil.isBlank(smsCode) || StringUtil.isBlank(lastSrvAuthCode)){
			return error("01", "重置密码_短信验证信息不全", "");
		}
		
		String jxAccountId = user.getStr("jxAccountId");
		if(null == jxAccountId || "".equals(jxAccountId)){
			return error("02", "未开通存管，请前往用户中心开通存管", "");
		}
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
			return error("02", "未设置过电子账户密码", "");
		}
		String userMobile = "";
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if(banksV2 == null){
			return error("02", "未查找到理财卡信息", "");
		}
		userMobile = banksV2.getStr("mobile");
		
		UserInfo userInfo = userInfoService.findById(userCode);
		if(null == userInfo){
			return error("03", "未查找到用户信息", "");
		}
		String userCardName = userInfo.getStr("userCardName");
		if(null == userCardName || "".equals(userCardName)){
			return error("03", "用户姓名为空，请完善实名信息", "");
		}
		String userCardId = userInfo.getStr("userCardId");
		
		try {
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			userCardId = CommonUtil.decryptUserCardId(userCardId);
		} catch (Exception e) {
			return error("04", "解析身份信息异常", e.getStackTrace());
		}
		
		String idType = userInfo.getStr("idType");
		if(null == idType || "".equals(idType)){
			return error("05", "证件类型与证件不符", "");
		}
		//返回交易页面链接
		String retUrl = CommonUtil.ADDRESS+"/C01";
		//后台通知链接
		String notifyUrl = CommonUtil.CALLBACK_URL + "/resetPwdCallback?userCode=" + userCode;
		//交易成功跳转链接 
		String successfulUrl = "";
		HttpServletResponse response = getResponse();
		JXController jxController = new JXController();
		jxController.passwordResetPlus(jxAccountId, idType, userCardId, userCardName, userMobile, lastSrvAuthCode, smsCode, retUrl, notifyUrl, successfulUrl, response);
		return succ("00", "重置密码申请成功");
	}

	/**
	 * 绑定银行卡：用户需要换绑卡时，平台调用页面
	 * 
	 * 绑卡，跳转到江西银行页面，由用户输入新的绑卡账户、交易密码、获
	 * 
	 * 取短信验证码完成绑卡操作
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/depositBindCard")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void depositBindCard(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		User user = getUser();
		if(user == null){
			msg = error("01", "用户未登录，请登录之后再进行相关操作", "");
			renderJson(msg);
			return;
		}
		String userCode = user.getStr("userCode");
		String jxAccountId = user.getStr("jxAccountId");
		if(null == jxAccountId || "".equals(jxAccountId)){
			msg = error("02", "未开通存管，请前往中心管理页面开通", "");
			renderJson(msg);
			return;
		}
		UserInfo userInfo = userInfoService.findById(userCode);
		if(null == userInfo){
			msg = error("03", "未查找到用户信息", "");
			renderJson(msg);
			return;
		}
		String idType = userInfo.getStr("idType");
		if(null == idType || "".equals(idType)){
			msg = error("04", "证件类型与证件号不符", "");
			renderJson(msg);
			return;
		}
		String userCardName = userInfo.getStr("userCardName");
		if(null == userCardName || "".equals(userCardName)){
			msg = error("05", "用户姓名为空", "");
			renderJson(msg);
			return;
		}
		String idNo = userInfo.getStr("userCardId");
		try {
			idNo = CommonUtil.decryptUserCardId(idNo); 
		} catch (Exception e) {
			msg = error("06", "证件号解析异常", e.getStackTrace());
			renderJson(msg);
			return;
		}
		
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("bindCard_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
			if(cardDetails != null){
				List<Map<String,String>> list = (List<Map<String,String>>)cardDetails.get("subPacks");
				if(list != null && list.size() > 0){
					msg = error("09", "已存在有效签约关系", "");
					renderJson(msg);
					return;
				}
			}
			
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("bindCard_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("bindCard_"+verifyOrderId+userCode);
		//客户IP
		String userIP = getRequestIP();
		
		if(userIP == null){
			userIP = "";
		}
		String isBind = JXController.verifyBindCard(jxAccountId, "0");
		String successfulUrl = CommonUtil.ADDRESS+"/B02";
		if("n".equals(isBind)){//没有绑定过银行卡——老用户
			successfulUrl= CommonUtil.ADDRESS + "/depositoryUserUsed?step=1";
		}
		String retUrl = CommonUtil.ADDRESS+"/B02";
		String notifyUrl = CommonUtil.CALLBACK_URL + "/depositBindCardCallback";
		
		HttpServletResponse response = getResponse();
		JXController.bindCardPage(idType, idNo, userCardName, jxAccountId, userIP, retUrl, notifyUrl, successfulUrl, response);
		
		renderNull();
	}

	/**
	 * 解绑银行卡：当电子账户在账务系统余额为0，且
	 * 
	 * 没有未结清的标的时，可以使用本接口解绑曾经绑定的银行卡 调用方式：接口调用
	 * 
	 * @return
	 */
	@ActionKey("/unbindCard")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void unbindCard(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		User user = getUser();
		if(null == user){
			msg = error("01", "用户未登录", "");
			renderJson(msg);
			return;
		}
		String userCode = user.getStr("userCode");
		String jxAccountId = user.getStr("jxAccountId");
		if(null == jxAccountId || "".equals(jxAccountId)){
			msg = error("02", "未开通存管，请前往中心管理页面开通", "");
			renderJson(msg);
			return;
		}
		
		UserInfo userInfo = userInfoService.findById(userCode);
		if(userInfo == null){
			msg = error("03", "未查找到用户信息", "");
			renderJson(msg);
			return;
		}
		String idNo = userInfo.getStr("userCardId");
		String userCardName = userInfo.getStr("userCardName");
		if(null == userCardName || "".equals(userCardName)){
			msg = error("05", "用户姓名不能为空", "");
			renderJson(msg);
			return;
		}
		String idType = userInfo.getStr("idType");
		if(null == idType || "".equals(idType)){
			msg = error("06", "证件类型不能为空", "");
			renderJson(msg);
			return;
		}
		
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if(banksV2 == null){
			msg = error("07", "请完善银行卡信息", "");
			renderJson(msg);
			return;
		}
		String userMobile = banksV2.getStr("mobile");//存管手机号
		try {
			idNo = CommonUtil.decryptUserCardId(idNo);
			userMobile = CommonUtil.decryptUserMobile(userMobile);
		} catch (Exception e) {
			msg = error("04", "身份信息解析异常", "");
			renderJson(msg);
			return;
		}
		String bankNo = banksV2.getStr("bankNo");
		if(null == bankNo || "".equals(bankNo)){
			msg = error("08", "请绑定银行卡", "");
			renderJson(msg);
			return;
		}
		//检查用户的账户余额是否为0
		Funds funds = fundsServiceV2.findById(userCode);
		long avBalance = funds.getLong("avBalance");
		long frozeBalance = funds.getLong("frozeBalance");
		
		Map<String, String> balanceQuery = JXQueryController.balanceQuery(jxAccountId);
		if(balanceQuery == null){
			msg = error("09", "error_balance_query", "");
			renderJson(msg);
			return;
		}
		long availBal = StringUtil.getMoneyCent(balanceQuery.get("availBal"));
		long currBal = StringUtil.getMoneyCent(balanceQuery.get("currBal"));
		if(avBalance != 0 || frozeBalance != 0 || availBal != 0 || currBal != 0){
			msg = error("09", "无法解绑银行卡_账户余额不为0", "");
			renderJson(msg);
			return;
		}
		
		//检查用户是否有未结清的标的
		int a = loanTraceService.countTrace4User(userCode);
		if(a > 0){
			msg = error("09", "无法解绑银行卡_还有标的未结清", "");
			renderJson(msg);
			return;
		}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("bindCard_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("bindCard_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("bindCard_"+verifyOrderId+userCode);
		String retUrl=CommonUtil.ADDRESS+"/B02";
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String  notifyUrl=CommonUtil.CALLBACK_URL+"/unbindCardPageCallback?userCode="+userCode;
		//调用解绑银行卡接口
		JXController.unbindCardPage(jxAccountId, userCardName, idType, idNo, bankNo, userMobile, retUrl, forgotPwdUrl, notifyUrl, getResponse());
		renderNull();
	}

	/**
	 * 短信接口20180930起停用
	 * 存管请求发送短信验证码接口 功能：向指定的手机号号码发送验证码，支持短
	 * 
	 * 信发送和语音呼叫方式
	 * 
	 * @return
	 */
	@ActionKey("/commonSmsCode")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message commonSmsCode(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		String userCode = getUserCode();
		if(StringUtil.isBlank(userCode)){
			return error("01", "用户未登录", null);
		}
		String mobile = "";
		//请求类型：1-即信发短信    2-银行发短信   不填默认为1
		String reqType = getPara("reqType","1");
		//对应业务交易代码
		String type = getPara("type");
		if(StringUtil.isBlank(type)){
			return error("03", "未收到短信业务交易代码", "");
		}
		
		if("0".equals(type)){//修改存管手机号时短信发送至新手机号，需要前台传
			mobile = getPara("mobile","");
			if(StringUtil.isBlank(mobile)){
				return error("03", "手机号不能为空", "");
			}
		}else{
			BanksV2 banksV2 = banksService.findByUserCode(userCode);
			if(banksV2 == null){
				return error("02", "未查找到用户信息", "");
			}
			mobile = banksV2.getStr("mobile");
			try {
				mobile = CommonUtil.decryptUserMobile(mobile);
			} catch (Exception e) {
				return error("05", "发送短信：手机号解析异常","");
			}
		}
		String cardNo = "";//reqType为2时cardNo必填
		if("2".equals(reqType)){
			BanksV2 banksV2 = banksService.findByUserCode(userCode);
			
			if(banksV2 == null){
				return error("03", "请完善银行卡信息", "");
			}
			cardNo = banksV2.getStr("bankNo");
			if(cardNo == null || "".equals(cardNo)){
				return error("03", "请完善银行卡信息", "");
			}
		}
		
		//业务交易代码
		String srvTxCode = "";
		String memcachedKey = "";
		switch (type) {
		case "0":
			srvTxCode = "mobileModifyPlus";//修改存管手机号
			memcachedKey = "SMS_MSG_MODIFYMOBILE_";
			break;
		case "1":
			srvTxCode = "passwordResetPlus";//存管密码重置
			memcachedKey = "SMS_MSG_RESETPWD_";
			break;
		case "2":
			srvTxCode = "autoBidAuthPlus";//自动投标签约
			memcachedKey = "SMS_MSG_AUTOBID_";
			break;
		case "3":
			srvTxCode = "autoCreditInvestAuthPlus";//自动债权转让
			memcachedKey = "SMS_MSG_AUTOCREDITINVEST_";
			break;
		default :
			return error("05", "短信业务交易代码与操作不符", "");
		}
		String smsType = "";//验证码类型(暂时不用，可选)
		//返回数据
		Map<String, String> resultMap = new HashMap<>();
		//调用短信接口
		Map<String, String> resMap = JXController.smsCodeApply(mobile, reqType, srvTxCode, cardNo, smsType );
		
		if("00000000".equals(resMap.get("retCode"))){
			resultMap.put("mobile", resMap.get("mobile"));
			//业务交易代码
			resultMap.put("srvTxCode", resMap.get("srvTxCode"));
			//业务授权码 存入缓存中
			String srvAuthCode = resMap.get("srvAuthCode");
			if("2".equals(type)){
				String dateTime = DateUtil.updateDate(new Date(), 60, Calendar.SECOND, "yyyyMMddHHmmss");
				srvAuthCode = dateTime + srvAuthCode;
			}
			Memcached.set(memcachedKey+ userCode, srvAuthCode, 10*60*1000);
			
			return succ("短信发送成功", resMap.get("smsSeq"));
		}else{
			return error("10", "短信发送失败,"+resMap.get("retCode")+":"+resMap.get("retMsg"), "");
		}
	}

	/**
	 * 存管账户手机号修改增强版
	 * 
	 * @return
	 */
	@ActionKey("/modifyDepositMobile")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modifyDepositMobile(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		User user = getUser();
		if(null == user){
			return error("01", "请登录之后再操作", "");
		}
		String newMobile = getPara("mobile");
		if(newMobile == null || "".equals(newMobile)){
			return error("02", "新手机号不能为空", "");
		}
		String smsCode = getPara("smsCode");
		//前导业务授权码
		String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_MODIFYMOBILE_" + user.getStr("userCode"));
		if(StringUtil.isBlank(smsCode)){
			return error("02", "短信验证码为空，请检查", "");
		}
		if(null == lastSrvAuthCode || "".equals(lastSrvAuthCode)){
			return error("03", "用户未授权", "");
		}
		String jxAccountId = user.getStr("jxAccountId");
		if(null == jxAccountId || "".equals(jxAccountId)){
			return error("02", "未开通存管账户，请前往会员中心开通", "");
		}
		
		//String option = "1";//默认1：修改
		Map<String, String> resultMap = new HashMap<>();
		
		Map<String, String> resMap = JXController.mobileModifyPlus(jxAccountId, newMobile, lastSrvAuthCode, smsCode);
		//调用修改存管手机号接口
		if("00000000".equals(resMap.get("retCode"))){
			BanksV2 banksV2 = banksService.findByUserCode(user.getStr("userCode"));
			try {
				newMobile = CommonUtil.encryptUserMobile(newMobile);
			} catch (Exception e) {
				return error("09", "新手机号解析异常", "");
			}
			banksV2.set("mobile", newMobile);//新存管手机号
			banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
			
			if(!banksV2.update()){
				BIZ_LOG_ERROR(user.getStr("userCode"), BIZ_TYPE.BANK, "修改存管手机号_更新本地数据失败", null);
			}
			
			resultMap.put("retCode", resMap.get("retCode"));
			resultMap.put("retMsg", resMap.get("retMsg"));
			return succ("存管手机号修改成功", resMap.get("retCode"));
		}else{
			return error("10", "存管手机号修改失败,"+resMap.get("retCode")+":"+resMap.get("retMsg"), "");
		}
	}

	/**
	 * 检查用户是否设置过存管密码
	 * 
	 * @return pinFlag:1-设置过密码 0-未设置过密 码
	 */
	@ActionKey("/checkPwd")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class })
	public Message checkPwd() {
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		if (user == null) {
			return error("01", "未登录",null);
		}
		String jxAccountId = user.getStr("jxAccountId");
		Map<String, String> result = new HashMap<>();
		result.put("loginId", jxAccountId);
		String verifyPwd = JXTraceService.verifyPwd(jxAccountId);
		result.put("pinFlag", verifyPwd);
		return succ("查询成功", result);
	}
	
	/**
	 * 验证绑卡关系
	 * @return
	 */
	@ActionKey("/verifyBindCard")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message verifyBindCard(){
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		if(user== null){
			return error("01", "未加载用户信息", "");
		}
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			return error("02", "还未开通存管账户", "");
		}
		String isBind = JXController.verifyBindCard(jxAccountId, "0");
		return succ("00", isBind);
	}
		
		
	/**
	 * 临时设置圈存圈提交易密码
	 * @return
	 */
	/*@ActionKey("/tempSetPwd")
	public Message tempSetPwdForQCorQT(){
		String type = getPara("type");
		String mobile = "";
		String accountId = "";
		String idType = "";
		String idNo = "";
		String name = "武汉易融恒信金融信息服务有限公司";
		if("1".equals(type)){//红包账户
			mobile = JXService.RED_ACCOUNT_MOBILE;
			accountId = JXService.RED_ENVELOPES;
			idType = "25";
			idNo = "91420103303535758R";
		}else{//手续费账户
			mobile = JXService.FEES_ACCOUNT_MOBILE;
			accountId = JXService.FEES;
			idType = "20";
			idNo = "G1042010304144850U";
		}
		HttpServletResponse response = getResponse();
		String successfulUrl = "";
		String retUrl = CommonUtil.ADDRESS;
		String notifyUrl = CommonUtil.ADDRESS+"/tempShowResult";
		JXController.passwordset(accountId, idType, idNo, name, mobile, successfulUrl, response, retUrl, notifyUrl);
		return succ("ok", "提交成功");
	}*/
		
	/**
	 * 临时处理设置密码响应
	 */
	/*@SuppressWarnings("unchecked")
	@ActionKey("/tempShowResult")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void tempShowResult(){
		HttpServletRequest request = getRequest();
		//响应数据
		String parameter = request.getParameter("bgData");
		
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		boolean bool = JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		if(bool){
			renderText("success");
		}
	}*/
}
