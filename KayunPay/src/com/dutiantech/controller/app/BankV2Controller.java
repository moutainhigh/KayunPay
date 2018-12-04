package com.dutiantech.controller.app;

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
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.interceptor.AppInterceptor;
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
import com.fuiou.data.QueryUserInfsReqData;
import com.fuiou.data.QueryUserInfsRspData;
import com.fuiou.data.QueryUserInfsRspDetailData;
import com.fuiou.data.RegReqData;
import com.fuiou.data.ResetPassWordReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;

public class BankV2Controller extends BaseController{

	private BanksService banksService = getService(BanksService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService =getService(FuiouTraceService.class);
	private UserInfoService userInfoService =getService(UserInfoService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private UserService userService = getService(UserService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	//存管系统绑卡开户 20170523 ws
	@ActionKey("/app_bankv3_save")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void saveNewBankV3(){
		Message msg=null;
		String userCode=getUserCode();
		String bankNo=getPara("bankNo");
		String bankCode=getPara("bankCode");
		String bankName=getPara("bankName");
		String provinceName=getPara("provinceName");
		String cityCode=getPara("CityCode");
		String cityName=getPara("CityName");
		String smsCode=getPara("smsCode");
		if( StringUtil.isBlank(smsCode) == true ){
			msg= error("01", "短信验证码不能为空", null);
			renderJson(msg);
			return;
		}
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_BINDCARD_" + userCode, smsCode ) == false){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 ,短信验证码不正确", null);
			msg= error("02", "短信验证码不正确", "");
			renderJson(msg);
			return;
		}
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode) ;
		User user =User.userDao.findById(userCode);
		BanksV2 bankv=BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where bankNo=?", bankNo);
		if(bankv!=null){
			msg= error("13", "该银行卡已被使用", null);
			renderJson(msg);
			return;
		}
		String mobile =null;
		String userCardID =null;
		//手机号与身份证号解密
		try {
			mobile =CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			userCardID = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String userCardName = userInfo.getStr("userCardName");
		if( StringUtil.isBlank(userCardID) == true|| StringUtil.isBlank(userCardName) == true){
			msg= error("03", "未验证身份证，请在[安全中心]完善您的身份证信息", "");
			renderJson(msg);
			return;
		}
		if(null==bankCode||"".equals(bankCode)){
			String aa=CommonUtil.checkBankCode(bankName);
			bankCode=aa;
			if("".equals(bankCode)){
				msg= error("12", "绑定银行卡失败", null);
				renderJson(msg);
				return;
			}
		}
		if(null==cityCode||"".equals(cityCode)){
			String[] cc=CommonUtil.checkCity(provinceName);
			if(null==cc){
				msg= error("11", "绑定银行卡失败", null);
				renderJson(msg);
				return;
			}
			cityName=cc[0];
			cityCode=cc[1];
		}
		String traceCode=CommonUtil.genMchntSsn();
		//设置开户信息
		CommonRspData commonRspData=null;
		RegReqData regReqData=new RegReqData();
		regReqData.setVer(CommonUtil.VER);
		regReqData.setMchnt_cd(CommonUtil.MCHNT_CD);//商户号
		regReqData.setMchnt_txn_ssn(traceCode);//流水号
		regReqData.setCust_nm(userCardName);//用户名
		regReqData.setCertif_tp("0");//证件类型
		regReqData.setCertif_id(userCardID);//身份证号
		regReqData.setMobile_no(mobile);//手机号
		regReqData.setCity_id(cityCode);//银行卡地区号
		regReqData.setParent_bank_id(bankCode);//银行代码
		regReqData.setCapAcntNo(bankNo);//银行卡号
		String paypwd=mobile.substring(mobile.length()-6,mobile.length());//登录密码（默认loginid后6位）
		try {
			regReqData.setLpassword(CommonUtil.encryptPasswd(paypwd));//登录密码密文
		} catch (UnsupportedEncodingException e1) {
			msg= error("09", "提交开户信息失败", null);
			renderJson(msg);
			return;
		}
		//设置查询信息
		QueryUserInfsReqData queryUserInfsReqData =new QueryUserInfsReqData();
		queryUserInfsReqData.setVer(CommonUtil.VER);
		queryUserInfsReqData.setMchnt_cd(regReqData.getMchnt_cd());//设置商户号
		String ssn = CommonUtil.genMchntSsn();
		queryUserInfsReqData.setMchnt_txn_ssn(ssn);//设置流水号
		queryUserInfsReqData.setMchnt_txn_dt(DateUtil.getNowDate());//设置查询时间
		queryUserInfsReqData.setUser_ids(regReqData.getMobile_no());//设置手机号
		//查询是否已开户
		try {
			QueryUserInfsRspData queryUserInfsRspData=FuiouService.queryUserInfs(queryUserInfsReqData);
			List<QueryUserInfsRspDetailData> list=queryUserInfsRspData.getResults();
			if(null==list.get(0).getBank_nm()||"".equals(list.get(0).getBank_nm())){
				commonRspData=FuiouService.reg(regReqData);
				System.out.println("注册用户返回码："+commonRspData.getResp_code());
				if("0000".equals(commonRspData.getResp_code())){
					//本地数据库存数据
					BanksV2 bankV2 = getModel( BanksV2.class );
					bankV2.set("bankName",bankName ) ;
					bankV2.set("bankNo", bankNo);
					bankV2.set("bankType",bankCode ) ;
					bankV2.set("cardCity", provinceName+"|"+cityName);
					bankV2.set("userCode", userCode ) ;
					bankV2.set("userName", user.getStr("userName") ) ;
					bankV2.set("trueName", userCardName ) ;
					bankV2.set("cardid", userInfo.getStr("userCardId") ) ;
					bankV2.set("mobile", "000" ) ;
					bankV2.set("createDateTime", DateUtil.getNowDateTime() ) ;
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime() ) ;
					bankV2.set("isDefault", "1" ) ;
					bankV2.set("status", "0" ) ;
					bankV2.set("agreeCode", traceCode) ;
					bankV2.set("ssn", ssn);
					if( bankV2.save() ){
						user.set("loginId", user.getStr("userMobile"));
						if(user.update()){
							// 绑卡成功，获取可用积分 add by stonexk at 20170601
//							fundsServiceV2.doPoints(userCode, 0 , 3000, "注册送积分") ;
							// end add
							if(!"J".equals(user.getStr("userType"))){
							Funds funds =fundsServiceV2.findById(userCode);
							long avBalance=funds.getLong("avBalance");
							long frozeBalance =funds.getLong("frozeBalance");
							if(avBalance+frozeBalance>0){
								QueryBalanceResultData queryB = fuiouTraceService.BalanceFunds(user);
								if(Long.parseLong(queryB.getCa_balance())==0){
								CommonRspData comm=fuiouTraceService.gorefund(avBalance+frozeBalance, user, FuiouTraceType.E);
								if("0000".equals(comm.getResp_code())){
									if(frozeBalance>0){
									fuiouTraceService.freeze(user, frozeBalance);
									}
								}else{
									user.set("loginId", "");
									user.update();
									msg= error("09", "开通失败，存管资金充入失败", null);
									renderJson(msg);
									return;
								}
								}
								}
							}
							BIZ_LOG_INFO(userCode , BIZ_TYPE.TLIVE , "激活账户成功 " );
							Memcached.set("PORTAL_USER_" + userCode , user ) ;
							msg= succ("添加成功", "添加成功") ;
							renderJson(msg);
							return;
							
						}else{
							msg= error("09", "添加失败", null);
							renderJson(msg);
							return;
						}
						}
					}else{
					msg= error("05", "添加失败", null ) ;
					renderJson(msg);
					return;}
				
			}else{
				msg= error("06", "账户已存在", null);
				renderJson(msg);
				return;
			}
			
		} catch (Exception e) {
			msg= error("08", "添加失败", null ) ;
			renderJson(msg);
			return;
		}	
		msg= error("07", "添加失败", null);
		renderJson(msg);
		return;
		
	}
	//end

	@ActionKey("/app_bankv2_list")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryBankList(){
		Message message=null;
		String userCode = getUserCode() ;
		BanksV2 bankV2 = BanksV2.bankV2Dao.findFirst("select trueName,bankNo,bankType,bankName from t_banks_v2 where userCode=?", userCode );
		if( bankV2 == null )
			bankV2 = new BanksV2() ;
		else{
			bankV2.remove("modifyDateTime");
			bankV2.remove("payType");
			bankV2.remove("mobile");
			bankV2.remove("cardid");
			bankV2.remove("bankMac");
			bankV2.remove("agreeCode");
			bankV2.remove("createDateTime");
			bankV2.remove("uid");
			String bankNo = bankV2.getStr("bankNo") ;
			//bankNo = "**** **** **** " + bankNo.substring(12);
			bankNo = bankNo.substring(bankNo.length()-3, bankNo.length());
			bankV2.set("bankNo", bankNo ) ;
		}
		message=succ("ok", bankV2 ) ;
		renderJson(message); 
	}
	
	/**
	 * 查询城市代码
	 * provinceCode 省代码
	 * */
	@ActionKey("/app_queryBankCityCodes")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void queryBankCityCodes(){
		String provinceCode= getPara("provinceCode");
		Message msg=null;
		List<BankOreaCode> citylist = BankOreaCode.bankOreaCodeDao.find("SELECT cityCode ,cityName from t_bank_oreacode where provinceCode=? ORDER BY cityCode",provinceCode);
		msg=succ("city", citylist);
		renderJson(msg);
	}
	
	
		//更换存管支付密码
		@ActionKey("/app_changefuiouPayPwd")
		@AuthNum(value=999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void changefuiouPayPwd(){
			User user= getUser();
			String loginId= user.getStr("loginId");
			if(null!=loginId&&!"".equals(loginId)){
				try {
					loginId=CommonUtil.decryptUserMobile(loginId);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				ResetPassWordReqData resetPassWordReqData=new ResetPassWordReqData();
				resetPassWordReqData.setLogin_id(loginId);
				resetPassWordReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
				String ssn=CommonUtil.genShortUID();
				resetPassWordReqData.setMchnt_txn_ssn(ssn);
				resetPassWordReqData.setBusi_tp("3");
				resetPassWordReqData.setBack_url(CommonUtil.ADDRESS+"/C01");
				try {
					FuiouService.resetPassWord(resetPassWordReqData, getResponse());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			renderNull();
		}
		//查询用户开通存管相关信息
		@ActionKey("/app_checkUserBankInfor")
		@AuthNum(value=999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public Message checkUserBankInfor(){
			String userCode=getUserCode();
			BanksV2 bankv2 = BanksV2.bankV2Dao.findById(userCode);
		//	User user = getUser();
			Map<String,Object> result = new HashMap<String,Object>();
			String cardid=bankv2.getStr("cardid");
		//	String mobile = user.getStr("userMobile");
			try {
				cardid=CommonUtil.decryptUserCardId(cardid);
		//		mobile=CommonUtil.decryptUserMobile(mobile);
			} catch (Exception e) {
				e.printStackTrace();
			}
			String cardNo=bankv2.getStr("bankNo");
			result.put("trueName", bankv2.getStr("trueName"));
	//		result.put("mobile", mobile);
			result.put("userCardId", cardid);
			result.put("cardNo", cardNo);
			return succ("查询成功", result);
		}
		/**
		 * 跳转富友更换银行卡页面
		 * */
		@ActionKey("/app_changeBankV2")
		@AuthNum(value=999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void changeBankV2(){
			String userCode=getUserCode();
			User user=getUser();
			BanksV2 banksV2=banksService.findBanks4User(userCode).get(0);
			String loginId=user.getStr("loginId");
			try {
				loginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			if(null!=loginId&&!"".equals(loginId)){
				ChangeCard2ReqData changeCard =new ChangeCard2ReqData();
				changeCard.setLogin_id(loginId);
				changeCard.setMchnt_cd(CommonUtil.MCHNT_CD);
				String ssn=CommonUtil.genMchntSsn();
				changeCard.setMchnt_txn_ssn(ssn);
				changeCard.setPage_notify_url(CommonUtil.ADDRESS+"/index");
				//添加修改银行卡信息记录
				ChangeBankTrace changebankTrace = new ChangeBankTrace();
				changebankTrace.set("userCode", userCode);
				changebankTrace.set("ssn", ssn);
				changebankTrace.set("state", "5");
				changebankTrace.set("creatDate", DateUtil.getNowDateTime());
				changebankTrace.set("upDate", DateUtil.getNowDateTime());
				changebankTrace.set("oldBankCardId", banksV2.getStr("bankNo"));
				if(changebankTrace.save()){
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
		 * 绑定银行卡页面信息
		 * */
		@ActionKey("/app_Banks")
		@AuthNum(value=999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void AppBanks(){
			String userCode= getUserCode();
			Message msg=null;
			UserInfo userInfo=userInfoService.findById(userCode);
			BanksV2 banksV2 = BanksV2.bankV2Dao.findById(userCode);
			String cardid=userInfo.getStr("userCardId");
			try {
				cardid=CommonUtil.decryptUserCardId(cardid);
			} catch (Exception e) {
				// TODO: handle exception
			}
			if("".equals(cardid)){
				msg=error("01", "未实名认证", null);
			}else{
			if(null!=banksV2){
				msg=error("02", "已绑定银行卡", null);
			}else{
				String truename=userInfo.getStr("userCardName");
				Map<String,Object> map = new HashMap<String, Object>();
				map.put("truename", truename);
				map.put("cardid", cardid);
				List<BankCode> BankList=  BankCode.bankCodeDao.find("select bankCode,bankName from t_bank_Code ");
				List<BankOreaCode> BankProvinceList = BankOreaCode.bankOreaCodeDao.find("SELECT provinceCode,provinceName from t_bank_OreaCode where provinceCode!=0 GROUP BY provinceName ORDER BY provinceCode ");
				map.put("BankList", BankList);
				map.put("BankProvinceList", BankProvinceList);
				renderJson(map);
				return;
			}
			}
			renderJson(msg);
		}
		
		
		/*
		  * 存管系统绑卡开户 20180109 hw 
		  * 实名认证、存管激活
		  * start
		  */
		@ActionKey("/app_bankv2_save")
		@AuthNum(value = 999)
		@Before({ AppInterceptor.class, PkMsgInterceptor.class })
		public void saveNewBankV2AndCertification() {
			Message msg = null;
			String userCode = getUserCode();
			String bankNo = getPara("bankNo");
			String bankCode = getPara("bankCode");
			String bankName = getPara("bankName");
			String provinceName = getPara("provinceName");
			String cityCode = getPara("CityCode");//省市代码
			String cityName = getPara("CityName");
			String smsCode = getPara("smsCode");//短信验证码

			String trueName = getPara("trueName");// 真实姓名
			String cardId = getPara("cardId", "");// 身份证号
			//String payPwd = getPara("payPwd", "");// 平台支付密码

			String userCardName = null;//真实姓名
			String userCardId = null;//身份证号
			String mobile = null;//手机号

			UserInfo userInfo = null;
			BanksV2 banksV2 = null;

			if (StringUtil.isBlank(smsCode) == true) {
				msg = error("01", "短信验证码不能为空", null);
				renderJson(msg);
				return;
			}
			// 验证短信验证码
			if (CommonUtil.validateSMS("SMS_MSG_BINDCARD_" + userCode, smsCode) == false) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 ,短信验证码不正确", null);
				msg = error("02", "短信验证码不正确", "");
				renderJson(msg);
				return;
			}

			// 验证用户是否已实名认证
			userInfo = UserInfo.userInfoDao.findById(userCode);
			if (userInfo == null || !"2".equals(userInfo.getStr("isAuthed"))) { // 如果用户未实名认证，则验证前台身份认证参数
				if (!IdCardUtils.validateCard(cardId)) {
					msg = error("03", "身份证号不正确！", "");
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
			} else {
				try {
					userCardName = userInfo.getStr("userCardName");
					userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
				} catch (Exception e) {
					msg = error("00", "解析用户身份证号错误", "");
					renderJson(msg);
					return;
				}
			}

			User user = User.userDao.findById(userCode);
			BanksV2 bankv = BanksV2.bankV2Dao.findFirst("select * from t_banks_v2 where bankNo=?", bankNo);
			if (bankv != null) {
				if (!userCode.equals(bankv.getStr("userCode"))) {
					msg = error("13", "该银行卡已被使用", null);
					renderJson(msg);
					return;
				}
			}
			
			if (null == bankCode || "".equals(bankCode)) {
				String aa = CommonUtil.checkBankCode(bankName);
				bankCode = aa;
				if ("".equals(bankCode)) {
					msg = error("12", "绑定银行卡失败1", null);
					renderJson(msg);
					return;
				}
			}
			if (null == cityCode || "".equals(cityCode)) {
				String[] cc = CommonUtil.checkCity(provinceName);
				if (null == cc) {
					msg = error("11", "绑定银行卡失败2", null);
					renderJson(msg);
					return;
				}
				cityName = cc[0];
				cityCode = cc[1];
			}
			try {
				mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			} catch (Exception e) {
				msg = error("09", "系统错误", "");
				renderJson(msg);
				return;
			}

			String traceCode = CommonUtil.genMchntSsn();
			// 设置开户信息
			CommonRspData commonRspData = null;
			RegReqData regReqData = new RegReqData();
			regReqData.setVer(CommonUtil.VER);
			regReqData.setMchnt_cd(CommonUtil.MCHNT_CD);// 商户号
			regReqData.setMchnt_txn_ssn(traceCode);// 流水号
			regReqData.setCust_nm(userCardName);// 用户名
			regReqData.setCertif_tp("0");// 证件类型
			regReqData.setCertif_id(userCardId);// 身份证号
			regReqData.setMobile_no(mobile);// 手机号
			regReqData.setCity_id(cityCode);// 银行卡地区号
			regReqData.setParent_bank_id(bankCode);// 银行代码
			regReqData.setCapAcntNo(bankNo);// 银行卡号
			String paypwd = mobile.substring(mobile.length() - 6, mobile.length() );// 登录密码（默认loginid后6位）
			try {
				regReqData.setLpassword(CommonUtil.encryptPasswd(paypwd));// 登录密码密文
			} catch (UnsupportedEncodingException e1) {
				msg = error("09", "提交开户信息失败", null);
				renderJson(msg);
				return;
			}
			// 设置查询信息
			QueryUserInfsReqData queryUserInfsReqData = new QueryUserInfsReqData();
			queryUserInfsReqData.setVer(CommonUtil.VER);
			queryUserInfsReqData.setMchnt_cd(regReqData.getMchnt_cd());// 设置商户号
			String ssn = CommonUtil.genMchntSsn();
			queryUserInfsReqData.setMchnt_txn_ssn(ssn);// 设置流水号
			queryUserInfsReqData.setMchnt_txn_dt(DateUtil.getNowDate());// 设置查询时间
			queryUserInfsReqData.setUser_ids(regReqData.getMobile_no());// 设置手机号
			// 查询是否已开户
			try {
				QueryUserInfsRspData queryUserInfsRspData = FuiouService.queryUserInfs(queryUserInfsReqData);
				List<QueryUserInfsRspDetailData> list = queryUserInfsRspData.getResults();
				if (null == list.get(0).getBank_nm() || "".equals(list.get(0).getBank_nm())) {
					commonRspData = FuiouService.reg(regReqData);
					System.out.println("注册用户返回码：" + commonRspData.getResp_code());
					if ("0000".equals(commonRspData.getResp_code())) {
						// 本地数据库存数据
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
								msg = error("15", "激活银行卡失败，本地数据添加失败", null);
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
							bankV2.set("cardid",CommonUtil.encryptUserCardId(userCardId)) ;
							bankV2.set("mobile", "000");
							bankV2.set("createDateTime", DateUtil.getNowDateTime());
							bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
							bankV2.set("isDefault", "1");
							bankV2.set("status", "0");
							bankV2.set("agreeCode", traceCode);
							bankV2.set("ssn", ssn);
							if (bankV2.save()) {
								user.set("loginId", user.getStr("userMobile"));
								if (user.update()) {
//									fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
								}
							} else {
								msg = error("16", "本地数据更新失败", null);
								renderJson(msg);
								return;
							}
						}

						if (!"J".equals(user.getStr("userType"))) {
							Funds funds = fundsServiceV2.findById(userCode);
							long avBalance = funds.getLong("avBalance");
							long frozeBalance = funds.getLong("frozeBalance");
							if (avBalance + frozeBalance > 0) {
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
										msg = error("09", "开通失败，存管资金充入失败", null);
										renderJson(msg);
										return;
									}
								}
							}
						}
						// 本地实名认证、支付密码补充
						if (!"2".equals(userInfo.getStr("isAuthed"))) {
							Message msgAuto = certificationAuto(userCode, trueName,
									CommonUtil.encryptUserCardId(userCardId),"");
							if (msgAuto != null) {
								renderJson(msgAuto);
								return;
							}
						}
						
						//设置平台支付密码，app暂时未用
						/*if (!StringUtil.isBlank(paypwd)) {
							Message msgPayPwd = updatePayPwd(userCode, user, payPwd);// 设置平台支付密码
							if (msgPayPwd != null) {
								renderJson(msgPayPwd);
								return;

							}
						}*/

						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "激活账户成功 ");
						Memcached.set("PORTAL_USER_" + userCode, user);
						msg = succ("添加成功", "添加成功");
						renderJson(msg);
						return;

					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, commonRspData.getResp_desc()+"&"+commonRspData.getMchnt_txn_ssn(),null);
						msg = error("09", "添加失败", null);
						renderJson(msg);
						return;
					}

				} else {
					msg = error("06", "账户已存在", null);
					renderJson(msg);
					return;
				}

			} catch (Exception e) {
				msg = error("08", "添加失败", null);
				renderJson(msg);
				return;
			}

		}
		/**
		 * 绑定银行卡页面信息 20180110 hw
		 * 
		 * */
		@ActionKey("/app_Banks2")
		@AuthNum(value=999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void AppBanks2(){
			String userCode= getUserCode();
			UserInfo userInfo=userInfoService.findById(userCode);
			String cardid=userInfo.getStr("userCardId");
			Map<String, Object> map = new HashMap<String,Object>();
			try {
				cardid=CommonUtil.decryptUserCardId(cardid);
			} catch (Exception e) {
				renderJson(map);
				return;
			}
			String truename = userInfo.getStr("userCardName");
			List<BankCode> bankList = BankCode.bankCodeDao.find("select bankCode,bankName from t_bank_Code ");
			List<BankOreaCode> bankProvinceList = BankOreaCode.bankOreaCodeDao.find("SELECT provinceCode,provinceName from t_bank_OreaCode where provinceCode!=0 GROUP BY provinceName ORDER BY provinceCode ");
			map.put("BankList",bankList);
			map.put("BankProvinceList",bankProvinceList);
			map.put("truename", truename);
			map.put("cardid", cardid);
			renderJson(map);
			return;
		}
		
		
		
		//为修改支付密码，app暂时未用（app实名认证、激活存管、修改支付密码一体化时使用）
		/*private Message updatePayPwd(String userCode, User user, String payPwd) {
			try {
				payPwd = CommonUtil.encryptPasswd(payPwd);
				user.set("payPasswd", payPwd);
				user.update();
			} catch (Exception e) {
				//记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.USER, "修改支付密码异常", e);
				return error("01", "修改支付密码异常", e.getMessage());
			}
			
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.USER, "修改支付密码成功");
			
			return null;
		}*/
		private Message certificationAuto(String userCode, String trueName, String md5CardId, String idType) {
			//次数限制
			Long count = Db.queryLong("select count(1) from t_auth_log where userCode = ?" ,userCode);
			if(count > 3){
				return error("01", "认证次数超限制", "");
			}
			
			boolean update = userInfoService.newUserAuth(userCode,trueName, md5CardId, "","2",idType);
			if(!update){
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
				return error("02", "已经认证,请勿重复提交!", "");
			}
			
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "用户自动认证成功");
			
			// 身份认证成功赠送可用积分
//			fundsServiceV2.doPoints(userCode, 0 , 2000, "注册送积分");
			
			// 身份认证后，邀请人添加30元现金抵用券  5月活动,9月继续，常态
			try{
				User user = userService.findById(userCode);
				RecommendInfo rmd = RecommendInfo.rmdInfoDao.findFirst("select * from t_recommend_info where bUserCode = ?",user.getStr("userCode"));
				if(rmd != null){
					User shareUser = userService.findById(rmd.getStr("aUserCode"));
					if(shareUser!=null){
						//实名认证送券
						boolean aa = ticketService.saveADV(shareUser.getStr("userCode"), "50元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
						if(aa){
							String mobile = userService.getMobile(shareUser.getStr("userCode"));
							String content = CommonUtil.SMS_MSG_TICKET.replace("[huoDongName]", "推荐好友实名认证").replace("[ticketAmount]", "50");
							SMSLog smsLog = new SMSLog();
							smsLog.set("mobile", mobile);
							smsLog.set("content", content);
							smsLog.set("userCode", shareUser.getStr("userCode"));
							smsLog.set("userName", shareUser.getStr("userName"));
							smsLog.set("type", "15");smsLog.set("typeName", "送现金券活动");
							smsLog.set("status", 9);
							smsLog.set("sendDate", DateUtil.getNowDate());
							smsLog.set("sendDateTime", DateUtil.getNowDateTime());
							smsLog.set("break", "");
							smsLogService.save(smsLog);
						}
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}
		//end
		
///////////////////////////////////////////江西存管开户操作//////////////////////////////////////////////////////////////
		/**
		* app存管开户——江西
		*/
		@ActionKey("/app_depositOpenAccountByJX")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void app_depositOpenAccountByJX(){
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

			// 验证用户是否已实名认证
			userInfo = userInfoService.findById(userCode);
			// 证件类型——投资人为默认为身份证
			String idType = "01";
			if (userInfo == null || !"2".equals(userInfo.getStr("isAuthed"))) {// 若未实名认证，则验证身份参数
				if (!IdCardUtils.validateCard(cardId)) {
					msg = error("03", "身份证号不正确！", "");
					renderJson(msg);
					return;
				}
				// 验证身份证是否已经被认证
				UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
				if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
					msg = error("04", "身份证号已被认证", "");
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
					msg = error("05", "用户身份证号解析错误", "");
					renderJson(msg);
					return;
				}
			}

			if (StringUtil.isBlank(userCardName)) {
				msg = error("06", "真实姓名不能为空", null);
				renderJson(msg);
				return;
			}

			if(!CommonUtil.isMobile(mobile.trim())){
				msg = error("07", "手机号校验失败", "");
				renderJson(msg);
				return;
			}
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
				msg = error("09", "存管账户已存在", "");
				renderJson(msg);
				return;
			}
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
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String successfulUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/openAccountCallback?uCode=" + userCode+"&mobile="+mobile;
			// app调用存管加密开户接口 20180907
			JXappController.accountOpenEncryptPage(idType, userCardName, gender, mobile, "", acctUse, "", identity, retUrl, successfulUrl, notifyUrl, response);
			
		}
		
		/**20180823
		 * app设置存管交易密码
		 */
		@ActionKey("/app_setDepositPwd")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void app_setDepositPwd(){
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
//			String isBind = JXController.verifyBindCard(jxAccountId, "0");
//			if("n".equals(isBind)){//没有绑定过银行卡,无法设置交易密码
//				msg = error("07", "该用户未绑卡，不能设置交易密码", "");
//				renderJson(msg);
//				return;
//			}
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/setPwdCallback?userCode="+userCode;
			JXappController.passwordResetPage(jxAccountId, idType, retUrl, notifyUrl, response);
			renderNull();
		}
		/**
		 * 20180823
		 * 修改存管密码
		 */
		@ActionKey("/app_modifyDepositPwd")
		@AuthNum(value = 999)
		@Before({ AppInterceptor.class, PkMsgInterceptor.class })
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
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/modifyPwdCallback?userCode="+userCode;
			JXController.passwordUpdate(jxAccountId, name, retUrl, notifyUrl, response);
			renderNull();
		}
		/**
		 * 20180831起接口失效
		 * app重置存管交易密码
		 */
		@ActionKey("/app_resetPwdPlus")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void appResetPwdPlus(){
			Message msg = new Message();
			if(!CommonUtil.jxPort){
				msg = error("666", "江西银行对接中……", "");
				renderJson(msg);
				return ;
			}
			String userCode = getUserCode();
			User user = userService.findById(userCode);
			if(user == null){
				msg = error("01", "用户未登录", "");
				renderJson(msg);
				return ;
			}
			String smsCode = getPara("smsCode","");//短信验证码
			//前导业务授权码
			String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_RESETPWD_"+user.getStr("userCode"));
			if(StringUtil.isBlank(smsCode) || StringUtil.isBlank(lastSrvAuthCode)){
				msg = error("01", "重置密码_短信验证信息不全", "");
				renderJson(msg);
				return ;
			}
			
			String jxAccountId = user.getStr("jxAccountId");
			if(null == jxAccountId || "".equals(jxAccountId)){
				msg = error("02", "未开通存管，请前往用户中心开通存管", "");
				renderJson(msg);
				return ;
			}
			Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
			if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
				msg = error("02", "未设置过电子账户密码", "");
				renderJson(msg);
				return ;
			}
			String userMobile = "";
			BanksV2 banksV2 = banksService.findByUserCode(userCode);
			if(banksV2 == null){
				msg = error("02", "未查找到理财卡信息", "");
				renderJson(msg);
				return ;
			}
			userMobile = banksV2.getStr("mobile");
			
			UserInfo userInfo = userInfoService.findById(userCode);
			if(null == userInfo){
				msg = error("03", "未查找到用户信息", "");
				renderJson(msg);
				return ;
			}
			String userCardName = userInfo.getStr("userCardName");
			if(null == userCardName || "".equals(userCardName)){
				msg = error("03", "用户姓名为空，请完善实名信息", "");
				renderJson(msg);
				return ;
			}
			String userCardId = userInfo.getStr("userCardId");
			
			try {
				userMobile = CommonUtil.decryptUserMobile(userMobile);
				userCardId = CommonUtil.decryptUserCardId(userCardId);
			} catch (Exception e) {
				msg = error("04", "解析身份信息异常", "");
				renderJson(msg);
				return ;
			}
			
			String idType = userInfo.getStr("idType");
			if(null == idType || "".equals(idType)){
				msg = error("05", "证件类型与证件不符", "");
				renderJson(msg);
				return ;
			}
			//返回交易页面链接
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			//后台通知链接
			String notifyUrl = CommonUtil.CALLBACK_URL+"/resetPwdCallback?userCode="+userCode;
			//交易成功跳转链接 
			String successfulUrl = "";
			HttpServletResponse response = getResponse();
			JXappController.passwordResetPlus(jxAccountId, idType, userCardId, userCardName, userMobile, lastSrvAuthCode, smsCode, retUrl, notifyUrl, successfulUrl, response);
			renderNull();
		}
		
		/**
		 * app换/绑存管银行卡
		 */
		@SuppressWarnings("unchecked")
		@ActionKey("/app_depositBindCard")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void appDepositBindCard(){
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
				msg = error("06", "证件号解析异常", "");
				renderJson(msg);
				return;
			}
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
			if(cardDetails != null){
				List<Map<String,String>> list = (List<Map<String,String>>)cardDetails.get("subPacks");
				if(list != null && list.size() > 0){
					msg = error("09", "已存在有效签约关系", "");
					renderJson(msg);
					return;
				}
			}
			
			//客户IP
			String userIP = getRequestIP();
			
			if(userIP == null){
				userIP = "";
			}
			String successfulUrl = "";
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String notifyUrl = CommonUtil.CALLBACK_URL+"/depositBindCardCallback";
			
			HttpServletResponse response = getResponse();
			JXappController.bindCardPage(idType, idNo, userCardName, jxAccountId, userIP, retUrl, notifyUrl, successfulUrl, response);
			
			renderNull();
		}
		
		/**
		 * 20180823
		 * app解绑银行卡(页面接口)
		 */
		@ActionKey("/app_unbindCard")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public void appUnbindCard(){
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
			//调用解绑银行卡接口
			String retUrl=CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String forgotPwdUrl=CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+userMobile;
			String  notifyUrl=CommonUtil.CALLBACK_URL+"/unbindCardPageCallback?userCode="+userCode;
		    JXappController.unbindCardPage(jxAccountId, userCardName, idType, idNo, bankNo, userMobile, retUrl, forgotPwdUrl, notifyUrl, getResponse());
		    renderNull();	
		}
		
		/**
		 * app修改存管手机号
		 */
		@ActionKey("/app_modifyDepositMobile")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public Message appModifyDepositMobile(){
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
			
			Map<String, String> resMap = JXappController.mobileModifyPlus(jxAccountId, newMobile, lastSrvAuthCode, smsCode);
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
		 * app验证存管账户
		 */
		@ActionKey("/verifyDepositAccount")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class,PkMsgInterceptor.class})
		public Message verifyDepositAccount(){
			String userCode = getUserCode();
			User user = userService.findById(userCode);
			if(user == null){
				return error("01", "请登录之后再操作", "");
			}
			String userName = user.getStr("userName");
			
			String isOpenAccountId = "未开通";
			String isSetPwd = "未设置";
			String isBindCard = "未绑定";
			String isPaymentAuth = "未授权";
			Map<String, String> map = new HashMap<>();
			if(StringUtil.isBlank(userName)){
				return error("02", "未查找到用户信息", "");
			}
			String accountId = user.getStr("jxAccountId");
			if(!StringUtil.isBlank(accountId)){
				isOpenAccountId = "y";
				//检查用户是否绑过卡
				isBindCard = JXappController.verifyBindCard(accountId, "0");
				//检查用户是否设置过密码
				isSetPwd = JXTraceService.verifyPwd(accountId);
				//检查用户是否开通缴费授权
				isPaymentAuth = JXappController.verifyPaymentAuth(accountId);
			}
			map.put("userName", userName);
			map.put("isOpenAccountId", "y".equals(isOpenAccountId)?"已开通":"未开通");
			map.put("isSetPwd", "y".equals(isSetPwd)?"已设置":"未设置");
			map.put("isBindCard", "y".equals(isBindCard)?"已绑定":"未绑定");
			map.put("isPaymentAuth", "y".equals(isPaymentAuth)?"已授权":"未授权");
			
			return succ("00", map);
		}
		
		/**
		 * 缴费授权
		 */
		@ActionKey("/app_paymentAuth")
		@AuthNum(value = 999)
		@Before({ AppInterceptor.class, PkMsgInterceptor.class })
		public void appPaymentAuth(){
			String maxAmt = getPara("maxAmt","20000");// 签约最大金额,单位：元，最多两位小数
			String deadline = getPara("deadline",DateUtil.updateDate(new Date(), 5, Calendar.YEAR, "yyyyMMdd"));// 签约到期日
			String userCode = getUserCode();// 用户编号
			Message message = new Message();
			if (StringUtil.isBlank(maxAmt)) {
				message = error("01", "签约最大金额不能为空", null);
				renderJson(message);
				return;
			}
			if (StringUtil.isBlank(deadline)) {
				message = error("02", "签约到期日不能为空", null);
				renderJson(message);
				return;
				
			}
			if (StringUtil.isBlank(userCode)) {
				message = error("03", "请重新登录", null);
				renderJson(message);
				return;
			}
			User user = userService.findById(userCode);
			if (user == null) {
				message = error("04", "用户不存在", null);
				renderJson(message);
				return;
			}
			UserInfo userInfo = userInfoService.findById(userCode);
			if(userInfo == null){
				message = error("05", "用户认证信息异常", null);
				renderJson(message);
				return;
			}
			String jxAccountId = user.getStr("jxAccountId");// 用户银行存管电子账号
			if (StringUtil.isBlank(jxAccountId)) {
				message = error("06", "请先开通江西银行存管", null);
				renderJson(message);
				return;
			}
			String name = userInfo.getStr("userCardName");
			String idNo = "";
			try {
				idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			} catch (Exception e) {
				message = error("07", "身份证号解析异常", null);
				renderJson(message);
				return;
			}
			BanksV2 banksV2 = banksService.findByUserCode(userCode);
			if(banksV2 == null){
				message = error("08", "请完善银行卡信息", "");
				renderJson(message);
				return;
			}
		
			String userMobile = "";
			try {
				userMobile = CommonUtil.decryptUserMobile(banksV2.getStr("mobile"));//存管手机号
			} catch (Exception e) {
				message = error("09", "手机号解析异常", "");
				renderJson(message);
				return;
			}
			
			String forgotPwdUrl = CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+userMobile;
			String retUrl = CommonUtil.APPBACK_ADDRESS+"/userinfo";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/paymentAuthPageCallback";
			//调用新的多合一合规授权接口
			JXappController.termsAuthPage(jxAccountId, name, idNo, "1", "", "", "1", "", "", "", "", "", maxAmt, deadline, "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse());
			renderNull();
		}
		
		/**
		 * app查询银行卡信息
		 */
		@ActionKey("/app_queryBankInfo")
		@AuthNum(value = 999)
		@Before({AppInterceptor.class, PkMsgInterceptor.class})
		public void queryBankInfo(){
			Message msg = new Message();
			String userCode = getUserCode();
			User user = userService.findById(userCode);
			if(user == null){
				msg = error("01", "未查找到用户信息", "");
				renderJson(msg);
				return ;
			}
			if(StringUtil.isBlank(user.getStr("jxAccountId"))){
				msg = error("02", "未激活存管", "");
				renderJson(msg);
				return ;
			}
			BanksV2 banksV2 = banksService.findByUserCode(userCode);
			if(banksV2 == null){
				msg = error("03", "未绑定银行卡", "");
			}else{
				Map<String, String> resultMap = new HashMap<>();
				String bankName = banksV2.getStr("bankName");
				String bankNo = banksV2.getStr("bankNo");
				if(!StringUtil.isBlank(bankNo) && bankNo.length() > 6){
					bankNo = bankNo.substring(0,6) + "******" + bankNo.substring(bankNo.length()-4, bankNo.length());
				}
				String trueName = banksV2.getStr("trueName");
				resultMap.put("bankName", bankName);
				resultMap.put("bankNo", bankNo);
				resultMap.put("trueName", trueName);
				msg = succ("查询成功", resultMap);
			}
			renderJson(msg);
			return ;
		}
		
}
