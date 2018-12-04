package com.dutiantech.controller.admin;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.admin.validator.UserAddUserValidator;
import com.dutiantech.controller.admin.validator.UserModifyUserValidator;
import com.dutiantech.controller.admin.validator.UserResetPwdValidator;
import com.dutiantech.controller.admin.validator.UserUpdateIsAuthed;
import com.dutiantech.controller.admin.validator.UserUpdateUserInfoValidator;
import com.dutiantech.controller.admin.validator.UserUpdateUserState;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.BankCode;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.ChangeBankTrace;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Share;
import com.dutiantech.model.TempReport;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.TransferWay;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.ViewLoanAmount;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.HistoryRecyService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.ShareService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.TransferWayService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.VIPService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DESUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.vo.TemplateMessage;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.ChangeCard2ReqData;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.ModifyMobileReqData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.QueryChangeCardReqData;
import com.fuiou.data.QueryChangeCardRspData;
import com.fuiou.data.RegReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.service.JXService;

public class UserController extends BaseController {

	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private VIPService vipService = getService(VIPService.class);
	private BanksService bankService = getService(BanksService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private ShareService shareService = getService(ShareService.class);
	private RecommendInfoService recommendInfoService = getService(RecommendInfoService.class);
	private HistoryRecyService historyRecyService = getService(HistoryRecyService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private AutoMapSerivce autoMapService = getService(AutoMapSerivce.class);
	private BanksService banksService = getService(BanksService.class);
	private TransferWayService transferWayService = getService(TransferWayService.class);
	private static final Logger scanVipLevelLogger = Logger.getLogger("scanVipLevelLogger");

	
	
	static{
		LoggerUtil.initLogger("scanVipLevel", scanVipLevelLogger);
	}
	
	@ActionKey("/autoCreateUser")
	@AuthNum(value=999)
	public void autoCreateUser() {
		Random rand = new Random();
		int createNum = rand.nextInt(10) + 11;
		for (int i = 0; i < createNum; i++) {
			String userCode = UIDUtil.generate();
			userService.save(userCode, getTel(), "", userCode, UIDUtil.generate().substring(0, 8).toUpperCase(), "127.0.0.1", "后台自动创建用户");	
		}
	}
	
	private String[] telFirst="134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");
	public int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }
    private String getTel() {
        int index=getNum(0,telFirst.length-1);
        String first=telFirst[index];
        String second=String.valueOf(getNum(1,888)+10000).substring(1);
        String third=String.valueOf(getNum(1,9100)+10000).substring(1);
        return first+second+third;
    }
	
	/**
	 * 添加一个用户
	 */
	@ActionKey("/createUser")
	@AuthNum(value=999)
	@Before({UserAddUserValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message createUser(){
		
		//拦截器验证不可为空---begin
		String userMobile = getPara("userMobile");
		String userEmail = getPara("userEmail");
		String loginPasswd = getPara("loginPasswd");
		String userName =getPara("userName");
		String regIP = getRequestIP(getRequest());
		//拦截器验证不可为空---end
		
		//验证是否已经存在此手机号
		User user = userService.find4mobile(userMobile);
		if(null != user){
			return error("02", "手机号已经被注册", "");
		}
		
		if(userService.save(UIDUtil.generate(),userMobile, userEmail, loginPasswd,userName, regIP,"后台创建用户"))
			return succ("添加用户操作完成", true);
		else
			return error("01", "添加用户操作未生效", false);
	}
	
	
	/**
	 * 补充或更新 用户认证信息
	 */
	@ActionKey("/updateUserInfo")
	@AuthNum(value=999)
	@Before({UserUpdateUserInfoValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateUserInfo(){
		
		//拦截验证不可为空-------begin
		String userCode = getPara("paraUserCode");
		
		String userCardName = getPara("userCardName","");//真实姓名
		String userCardId = getPara("userCardId","");//身份证编号
		String userAdress = getPara("userAdress","");//用户登记住址
		String ecpNme1 = getPara("ecpNme1","");//紧急联系人1
		String ecpRlation1 = getPara("ecpRlation1","");//紧急联系人1与用户关系
		String ecpMbile1 = getPara("ecpMbile1","");//紧急联系人1电话
		String ecpNme2 = getPara("ecpNme2","");//紧急联系人2
		String ecpRlation2 = getPara("ecpRlation2","");//紧急联系人2与用户关系
		String ecpMbile2 = getPara("ecpMbile2","");//紧急联系人2电话
		String isAuthed = getPara("isAuthed","");
		//拦截验证不可为空-------end
		
		String usercardid = null;
		try {
			usercardid = CommonUtil.encryptUserCardId(userCardId);
		} catch (Exception e) {
			throw new BaseBizRunTimeException("AX", "用户认证信息加密发生异常", e.getMessage());
		}
		
		Map<String,Object> para = new HashMap<String, Object>();
		para.put("userCardName", userCardName);para.put("userAdress", userAdress);para.put("userCardId",usercardid);
		para.put("ecpNme1", ecpNme1);para.put("ecpRlation1", ecpRlation1);
		para.put("ecpMbile1", ecpMbile1);para.put("ecpNme2", ecpNme2);
		para.put("ecpRlation2", ecpRlation2);para.put("ecpMbile2", ecpMbile2);
		if(!StringUtil.isBlank(isAuthed)){
			para.put("isAuthed", isAuthed);
		}
		
		if(userInfoService.updateUserInfo(userCode, para)){
			if(!StringUtil.isBlank(isAuthed) && isAuthed.equals("2")){
				//TODO  身份认证后，邀请人添加30元现金抵用券  5月活动,9月继续，常态
				try{
					User user = userService.findById(userCode);
					RecommendInfo rmd = RecommendInfo.rmdInfoDao.findFirst("select * from t_recommend_info where bUserCode = ?",user.getStr("userCode"));
					if(rmd != null){
						User shareUser = userService.findById(rmd.getStr("aUserCode"));
						if(shareUser!=null){
							//实名认证送券
//							boolean aa = ticketsService.save(shareUser.getStr("userCode"), shareUser.getStr("userName"),CommonUtil.decryptUserMobile(shareUser.getStr("userMobile")) , "", 
//									"30元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 15), "F", null, SysEnum.makeSource.A);
							boolean aa = ticketsService.saveADV(shareUser.getStr("userCode"), "50元现金券【好友实名认证奖励】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
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
			}
			return succ("更新用户认证信息操作完成", true);
		}else
			return error("01", "更新用户认证信息操作未生效", false);
	}
	/**
	 * 后台用户中心
	 * modify by rain 2017.11.1
	 * */
	@ActionKey("/getUserByUserCode")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getUserByUserCode(){
		String userCode = getPara("paraUserCode","");
		
		User user = userService.findUserAllInfoById(userCode);
		if(user==null ){
			return error("02","用户数据异常，请联系管理员",false);
		}
		
		try {
			String userMobile = user.getStr("userMobile");
			if(!StringUtil.isBlank(userMobile)){
				String x = CommonUtil.decryptUserMobile(userMobile);
				if(CommonUtil.isMobile(x) || CommonUtil.isPhone(x)){
					user.put("userMobile", x);
				}else{
					user.put("userMobile", " ");
				}
				
			}else{
				user.put("userMobile", " ");
			}
		} catch (Exception e) {
			user.put("userMobile", "");
		}
		try {
			String loginId = user.getStr("loginId");
			if(!StringUtil.isBlank(loginId)){
				String x = CommonUtil.decryptUserMobile(loginId);
				if(CommonUtil.isMobile(x) || CommonUtil.isPhone(x)){
					user.put("loginId", x);
				}else{
					user.put("loginId", "无");
				}
				
			}else{
				user.put("loginId", "无");
			}
		} catch (Exception e) {
			user.put("loginId", "");
		}
		try {
			String userCardId = user.getStr("userCardId");
			if(!StringUtil.isBlank(userCardId)){
				user.put("userCardId",  CommonUtil.decryptUserCardId(userCardId));
			}else{
				user.put("userCardId",  " ");
			}
		} catch (Exception e) {
			user.put("userCardId", "");
		}
		try{
			String jxAccountId = user.getStr("jxAccountId");
			if(StringUtil.isBlank(jxAccountId)){
				user.put("jxAccountId","无");
				user.put("mobile","无");
			}else{
				BanksV2 banksV2 = bankService.findByUserCode(user.getStr("userCode"));
				if(banksV2 == null){
					user.put("mobile","无");
				}else{
					user.put("mobile",CommonUtil.decryptUserMobile(banksV2.getStr("mobile")));
				}
//				Map<String, String>mobileMaintainace = JXQueryController.mobileMaintainace(jxAccountId, "0", "");
//				if(!StringUtil.isBlank(mobileMaintainace.get("mobile"))){
//					user.put("mobile",mobileMaintainace.get("mobile"));
//				}else{
//					user.put("mobile","无");
//				}
			}
		}catch (Exception e){
			user.put("jxAccountId","");
			user.put("mobile","");
		}
		
		Funds fund = fundsServiceV2.findById(userCode);
		
		long sumLeftAmount = fund.getLong("beRecyPrincipal");//代收本金(含逾期)
		long sumLeftInterest = fund.getLong("beRecyInterest");//代收利息(含逾期)
		user.put("sumLeftAmount",sumLeftAmount);
		user.put("sumLeftInterest",sumLeftInterest);
		
		//查询用户兑换方案
		TransferWay transferWay = transferWayService.findByUserCode(userCode);
		String transferWayStr = "";//方案选择
		if(transferWay != null){
			transferWayStr += "1".equals(transferWay.getStr("month")) ? (StringUtil.isBlank(transferWayStr)?"方案一(月付)":",方案一(月付)"):"";
			transferWayStr += "1".equals(transferWay.getStr("qtr")) ? (StringUtil.isBlank(transferWayStr)?"方案二(季付)":",方案二(季付)"):"";
			transferWayStr += "1".equals(transferWay.getStr("half")) ? (StringUtil.isBlank(transferWayStr)?"方案三(半价)":",方案三(半价)"):"";
			transferWayStr += "1".equals(transferWay.getStr("offline")) ? (StringUtil.isBlank(transferWayStr)?"方案四(债权置换)":",方案四(债权置换)"):"";
			transferWayStr += "1".equals(transferWay.getStr("normal")) ? (StringUtil.isBlank(transferWayStr)?"方案五(自行承担)":",方案五(自行承担)"):"";
		}else {
			transferWayStr = "用户未选择方案";
		}
		user.put("transferWayStr",transferWayStr);
		
		return succ("查询单个用户全部信息完成", user);
	}
	
	/**
	 * 根据ID查询一个用户
	 */
	@ActionKey("/getUserById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getUserById(){
		
		String userCode = getPara("paraUserCode","");

		User user = userService.findById(userCode);
		
		if(user==null || user.get("userMobile")==null || user.get("userCardId")==null){
			return error("02","用户数据异常，请联系管理员",false);
		}
		try {
			String userMobile = user.getStr("userMobile");
			if(!StringUtil.isBlank(userMobile)){
				user.put("userMobile", CommonUtil.decryptUserMobile(userMobile));
			}else{
				user.put("userMobile", " ");
			}
		} catch (Exception e) {
			user.put("userMobile", "");
		}
		return succ("查询单个用户基本信息完成", user);

	}
	
	/**
	 * 根据id查询用户认证信息
	 */
	@ActionKey("/getUserInfoById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getUserInfoById(){
		String userCode = getPara("paraUserCode","");//拦截器验证不可为空
		
		UserInfo userInfo = userInfoService.findById(userCode);
		try {
			String userCardId = userInfo.getStr("userCardId");
			if(!StringUtil.isBlank(userCardId)){
				userInfo.put("userCardId", CommonUtil.decryptUserCardId(userCardId));
			}else{
				userInfo.put("userCardId", " ");
			}
		} catch (Exception e) {
			userInfo.put("userCardId", "");
		}
		return succ("查询单个用户认证信息完成", userInfo);
	}
	
	/**
	 * 修改用户信息
	 */
	@ActionKey("/updateUser")
	@AuthNum(value=999)
	@Before({UserModifyUserValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message modifyUser(){
		String userCode = getPara("paraUserCode","");//拦截器验证不可为空
		String newPasswd = getPara("newPassword","");
		String newUserName = getPara("newUserName","");
		String newEmail = getPara("newEmail","");
		if(userService.updateUser(userCode, newUserName, newPasswd,newEmail))
			return succ("更新用户基本信息完成", true);
		else
			return error("01", "更新用户基本信息操作未生效", false);
	}
	
	/**
	 * 修改用户手机号
	 */
	@ActionKey("/updateUserMobile")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateUserMobile(){
		String oldMobile = getPara("oldMobile","");
		String newMobile = getPara("newMobile","");
		String newPassword = getPara("newPassword","");
		
		if(StringUtil.isBlank(oldMobile)){
			return error("03", "旧手机号不能为空", "");
		}
		if(StringUtil.isBlank(newMobile)){
			return error("03", "新手机号不能为空", "");
		}
		if(StringUtil.isBlank(newPassword)){
			return error("03", "初始化密码不能为空", "");
		}
		
		if(CommonUtil.isMobile(newMobile) == false){
			return error("04", "新手机号格式不正确", "");
		}
		
		User user = userService.find4mobile(oldMobile);
		if(null == user){
			return error("01", "用户不存在", "");
		}
		boolean b = userService.updateUserMobile(user.getStr("userCode"),newMobile,newPassword);
		if(b == false){
			return error("02", "新手机号已存在或修改异常", "");
		}
		//跳转存管平台手机号修改
		else{
			BIZ_LOG_INFO(user.getStr("userCode"), BIZ_TYPE.USER, "修改用户手机号：用户手机号【" + oldMobile + "】修改为 【" + newMobile + "】初始化密码为【" + newPassword + "】");
			String loginId=user.getStr("loginId");
			if(null!=loginId&&!"".equals(loginId)){
				try {
					loginId=CommonUtil.decryptUserMobile(loginId);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			ModifyMobileReqData modifyMobileReqData=new ModifyMobileReqData();
			modifyMobileReqData.setLogin_id(loginId);
			modifyMobileReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
			String ssn=CommonUtil.genMchntSsn();
			modifyMobileReqData.setMchnt_txn_ssn(ssn);
			modifyMobileReqData.setPage_notify_url(CommonUtil.ADDRESS+"/changUserMobileSign");
			try {
				FuiouService.p2p400101(modifyMobileReqData, getResponse());
			} catch (Exception e) {
				e.printStackTrace();
			}
			renderNull();
			}
		}
		return succ("修改成功,请速度联系修改密码!", "");
	}
	
	/**
	 * 取消用户理财卡绑定(连连支付)
	 */
	@ActionKey("/unbindBank4LianLian")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message unbindBank4LianLian(){
		String mobile = getPara("mobile","");
		
		if(StringUtil.isBlank(mobile)){
			return error("01", "手机号不能为空", "");
		}
		
		User user = userService.find4mobile(mobile);
		if(null == user){
			return error("02", "用户不存在", "");
		}
		
		String userCode = user.getStr("userCode");
		int x = bankService.unbindBank4LianLian(userCode);
		if(x>0){
			return succ("解绑成功!", x);
		}
		return succ("该用户还未绑定理财卡", 0);
		
	}
	
	/**
	 * 修改用户理财卡卡号
	 */
	@ActionKey("/updateUserBank")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateUserBank(){
		String mobile = getPara("mobile","");
		String bankNo = getPara("bankNo","");
		String bankType = getPara("bankType","");
		String bankName = getPara("bankName","");
		
		if(StringUtil.isBlank(mobile)){
			return error("04", "手机号不能为空", "");
		}
		if(StringUtil.isBlank(bankNo)){
			return error("04", "理财卡不能为空", "");
		}
		if(StringUtil.isBlank(bankType)){
			return error("04", "理财卡类型不能为空", "");
		}
		if(StringUtil.isBlank(bankName)){
			return error("04", "理财卡名称不能为空", "");
		}
		
		User user = userService.find4mobile(mobile);
		if(null == user){
			return error("01", "用户不存在", "");
		}
		
		String userCode = user.getStr("userCode");
		boolean x = bankService.isAuthCard(userCode, bankNo);
		if(x){
			return error("66", "该理财卡已被绑定过", "");
		}
		//boolean b = bankService.updateBankNo(userCode, bankNo, bankName, bankType);
		BanksV2 bank = null;
		try {
			bank = bankService.findBanks4User(userCode).get(0);
		} catch (Exception e) {
			return error("02", "该用户没有银行卡", "");
		}
		if(null == bank){
			return error("02", "该用户没有银行卡", "");
		}
		
		String biz_content = "银行卡修改：用户 " + userCode + " ,理财卡由【" + bank.getStr("bankNo") + "|" + bank.getStr("bankType") + "|" + bank.getStr("bankName") + "】";
		
		bank.set("bankNo", bankNo);
		bank.set("bankType", bankType);
		bank.set("bankName", bankName);
		bank.set("cardCity", "");
		boolean b = bank.update();
		if(b == false){
			return error("03", "银行卡号修改异常", "");
		}
		biz_content += "修改为【" + bankNo + "|" + bankName + "|" + bankType + "】" ;
		BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, biz_content);
		return succ("修改成功!", "");
	}
	
	/**
	 * 重置用户密码
	 */
	@ActionKey("/resetPasswd")
	@AuthNum(value=999)
	@Before({UserResetPwdValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message resetPwd(){
		String userCode = getPara("paraUserCode","");//拦截器验证不可为空
		String newPasswd = (String)CACHED.get("S0.portal-init-pwd");
		if(userService.resetPasswd(userCode, newPasswd))
			return succ("重置初始化用户密码操作完成", true);
		else
			return error("01", "重置初始化用户密码操作未生效", false);
	}
	
	/**
	 * 更新用户状态
	 */
	@ActionKey("/updateUserState")
	@AuthNum(value=999)
	@Before({UserUpdateUserState.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateUserState(){
		
		String userCode = getPara("paraUserCode");//拦截器验证不可为空
		String userState = getPara("userState");//拦截器验证不可为空
		
		if(userService.updateUserState(userCode, userState))
			return succ("更新用户状态操作完成", true);
		else
			return error("01", "更新用户状态操作未生效", false);
	}
	
	/**
	 * 更新用户认证状态
	 */
	@ActionKey("/updateIsAuthed")
	@AuthNum(value=999)
	@Before({UserUpdateIsAuthed.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateIsAuthed(){
		String userCode = getPara("paraUserCode");//拦截器验证不可为空
		String isAuthed = getPara("isAuthed");//拦截器验证不可为空
		
		if(userInfoService.updateIsAuthed(userCode, isAuthed))
			return succ("更新用户认证状态操作完成", true);
		else
			return error("01", "更新用户认证状态操作未生效", false);
	}
	
	/**
	 * 分页查询用户
	 */
	@ActionKey("/getUserInfoByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message findByPage(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		String endDate = getPara("endDate","");
		
		String userState = getPara("userState","");
		
		String allkey = getPara("allkey","").trim();
		
		Page<User> pageUser = userService.findByPage(pageNumber, pageSize, beginDate, endDate, userState, allkey);
		List<User> users = pageUser.getList();
		JSONArray jsonArray = new JSONArray();
		for (User user : users) {
			String jxAccountId = user.getStr("jxAccountId");
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userCode", user.getStr("userCode"));
			jsonObject.put("userState", user.getStr("userState"));
			jsonObject.put("vipInterestRate", user.getInt("vipInterestRate"));
			jsonObject.put("vipRiskRate", user.getInt("vipRiskRate"));
			jsonObject.put("regDate", user.getStr("regDate"));
			jsonObject.put("regTime", user.getStr("regTime"));
			jsonObject.put("points", user.getLong("points"));
			jsonObject.put("userName", user.getStr("userName"));
			jsonObject.put("userCardName", user.getStr("userCardName"));
			if (CommonUtil.fuiouPort) {
				jsonObject.put("depositAccount", user.getStr("loginId"));
//				jsonObject.put("loginId", user.getStr("loginId"));
			} else if (CommonUtil.jxPort) {
				jsonObject.put("depositAccount", jxAccountId);
//				jsonObject.put("jxAccountId", jxAccountId);
			}
			jsonObject.put("userType", user.getStr("userType"));
			jsonObject.put("vipLevel", user.getInt("vipLevel"));
			jsonObject.put("vipLevelName", user.getStr("vipLevelName"));
			jsonObject.put("sysDesc", user.getStr("sysDesc"));
			if(jxAccountId != null){
				JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(jxAccountId);
				if(paymentAuthPageState != null){
					String type = paymentAuthPageState.getString("type");
					String time = paymentAuthPageState.getString("time");
					String amount = StringUtil.getMoneyYuan(paymentAuthPageState.getLong("amount"));
					if("1".equals(type) || "2".equals(type)){
						time = DateUtil.getStrFromDate(DateUtil.getDateFromString(time,"yyyyMMdd"), "yyyy-MM-dd");
						jsonObject.put("paymentState", "到期时间:"+time+",金额:"+amount+"元");
					}else {
						time = DateUtil.getStrFromDate(DateUtil.getDateFromString(time,"yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss");
						jsonObject.put("paymentState", "解约时间:"+time+",金额:"+amount+"元");
					}
				}else {
					jsonObject.put("paymentState", "未开通");
				}
				JSONObject repayAuthPageState = jxTraceService.repayAuthPageState(jxAccountId);
				if(repayAuthPageState != null){
					String type = repayAuthPageState.getString("type");
					String time = repayAuthPageState.getString("time");
					String amount = StringUtil.getMoneyYuan(repayAuthPageState.getLong("amount"));
					if("1".equals(type) || "2".equals(type)){
						time = DateUtil.getStrFromDate(DateUtil.getDateFromString(time,"yyyyMMdd"), "yyyy-MM-dd");
						jsonObject.put("repayState", "到期时间:"+time+",金额:"+amount+"元");
					}else {
						time = DateUtil.getStrFromDate(DateUtil.getDateFromString(time,"yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss");
						jsonObject.put("repayState", "解约时间:"+time+",金额:"+amount+"元");
					}
				}else {
					jsonObject.put("repayState", "未开通");
				}
			}
			
			jsonArray.add(jsonObject);
		}
		return succ("分页查询用户完成", new Page<>(jsonArray, pageNumber, pageSize, pageUser.getTotalPage(), pageUser.getTotalRow()));
	}
	
	@ActionKey("/getUserInfo4scoreByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryUserInfo4scoreByPage(){

		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);

		String allkey = getPara("allkey","");
		
		Page<User> pageUser = userService.findInfo4scoreByPage(pageNumber, pageSize, allkey );
		
		return succ("ok", pageUser ) ;
	}

	@ActionKey("/modifyUserScore")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modifyUserScore(){
		String userCode = getPara("userCode","") ;
		if( StringUtil.isBlank( userCode) ){
			return error("01", "缺少必要参数!", null ) ;
		}
		String opValue = getPara("score");
		String opType = "+";
		if( opValue.indexOf("-") == 0 ){
			opType = "-";
			opValue = opValue.replace("-", "") ;
		}
		int score = 0 ;
		try{
			score = Integer.parseInt( opValue ) ;
		}catch(Exception e){
			return error("02", "积分应该为数值，比如1003,-4302", null ) ;
		}
		boolean upResult = userService.modifyUserScore(userCode, opType, score ) ;
		
		if( upResult == false ){
			return error("03", "修改失败!", null ) ;
		}
		return succ("ok", null ) ;
	}
	
	/**
	 * 分页查询认证进行中的用户列表
	 */
	@ActionKey("/getAuthedInfoByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message findAuthedByPage(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String allkey = getPara("allkey","");
		
		Page<User> pageUser = userService.findAuthedByPage(pageNumber, pageSize, allkey);
		
		if(pageNumber > pageUser.getTotalPage() && pageUser.getTotalPage() > 0){
			pageNumber = pageUser.getTotalPage();
			pageUser = userService.findAuthedByPage(pageNumber, pageSize, allkey);
		}
		return succ("分页查询用户完成", pageUser);
	}
	
	@ActionKey("/sendTiXianCode2WeiXin")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message sendTiXianCode2WeiXin(){
		String userCode = getPara("paraUserCode","");//拦截器验证不可为空
		if(StringUtil.isBlank(userCode)){
			return error("01", "用户不可为空", false);
		}
		String openId = Db.queryStr("select openId from t_short_password where userCode = ? and mobileType = 'WX'",userCode);
		if(StringUtil.isBlank(openId))
			return error("05", "该用户未绑定微信", false);
		if(StringUtil.isBlank(openId) == false){
			
			String mobile = "";
			try {
				String x = Db.queryStr("select userMobile from t_user where userCode = ? ",userCode);
				x = DESUtil.decode4string(x, CommonUtil.DESKEY);
				if(CommonUtil.isMobile(x))
					mobile = x;
				else
					return error("02", "手机号码异常",false);
			} catch (Exception e) {
				return error("02", "手机号码异常",false);
			}
			try {
				String msgMac = CommonUtil.getMathNumber(6);
				String wxMsgUrl = "http://wxapi.yrhx.com/weixin/service/sendTempMsg";
				TemplateMessage msgxxx = new TemplateMessage() ;
				msgxxx.setTemplateId("KOU41Tac7axGmgzxgRBQM5cd81Rge1yNznaMb_aQdGA");
				msgxxx.setTouser(openId);
				msgxxx.setData("keyword1", "易融恒信");
				msgxxx.setData("keyword2", mobile);
				msgxxx.setData("keyword3", msgMac);
				msgxxx.setData("keyword4", DateUtil.getStrFromNowDate("yyyy-MM-dd HH:mm:ss:"));
				msgxxx.setData("remark", "您正在使用提现功能，请注意验证码保密。");
				String result = HttpRequestUtil.sendGet(wxMsgUrl, "appid=wx377e1b9c96a05ce6&body="+URLEncoder.encode( msgxxx.toJSONString(),"UTF-8"));
				System.out.println( result );
				Memcached.set("SMS_MSG_WITHDRAW_" + userCode , msgMac, 10*60*1000);
				return succ("发送完成", msgMac);
			} catch (Exception e) {
				return error("03", "发送异常", false);
			}
		}
		return error("04", "发送失败", false);
		
	}
	
	/**	WJW
	 * 用户会员等级更新
	 * @return
	 */
	@ActionKey("/checkVIPLevel")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message checkVIPLevel(){
		String tmpKey = getPara("key","");
		if(StringUtil.isBlank(tmpKey)){
			return error("01", "呵呵哒", false);
		}
		
		if(tmpKey.equals("3.14159265358") == false){
			return error("02", "呵呵哒", false);
		}
		String vipDate = "20180319";//会员等级上线时间
		String today = DateUtil.getNowDate();//今天日期
		int x = DateUtil.compareDateByStr("yyyyMMdd", vipDate, today);
		
		if(x > 0){
			return error("03", "新会员制度"+vipDate+"后才生效，还未到时间", false);
		}
		
		scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务,扫描中......]");
		String nowDate = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, "yyyyMMdd")[0];
		if(nowDate.equals(today) || vipDate.equals(today)){//月头清空已免费提现次数(会员等级上线当天清零)
			scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务,当前日期:"+nowDate+",开始重置免费提现次数]");
			Db.update("update t_withdraw_free set freeCount = 0 where updateDate < ?",nowDate);
		}
		List<VipV2> vips = VipV2.VIPS;
		for (int i = 0; i < vips.size(); i++) {
			VipV2 tmpVip = vips.get(i);
			if(i == 0 && vipDate.equals(today)){//首次会员等级更新，先统一更改为新手
				boolean result = vipService.updateUserLevel("", tmpVip);
				scanVipLevelLogger.log(Level.INFO,"[定时任务:会员等级首次更新，用户全部更改为"+tmpVip.getVipLevelName()+(result ? "成功":"失败")+"]");
				continue;
			}
			List<LoanTrace> loanTraces = loanTraceService.queryBeRecyAmount(tmpVip.getVipMinAmount(), tmpVip.getVipMaxAmount());//单一等级会员列表
			long total = loanTraces.size();//总条数
			scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务,扫描符合【"+tmpVip.getVipLevelName()+"】开始......共计"+total+"个用户...]");
			if(total > 0){
				for (int j = 0; j < loanTraces.size(); j++) {
					LoanTrace loanTrace = loanTraces.get(j);
					String userCode = loanTrace.getStr("payUserCode");
					if(StringUtil.isBlank(userCode)){
						continue;
					}
					long amount = loanTrace.getBigDecimal("beRecyAmount")!=null?loanTrace.getBigDecimal("beRecyAmount").longValue():0;//用户待收
					
					boolean isOldBeRecyAmount = fundsServiceV2.updateOldBeRecyAmount(userCode, amount);//更新用户昨日待收显示
					scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务，用户["+userCode+"]昨日待收更新为"+amount+(isOldBeRecyAmount?"成功":"失败")+"]");
					
					User user = userService.findById(userCode);
					if(user==null){
						scanVipLevelLogger.log(Level.INFO,"[定时任务:会员等级首次更新，t_user表中无["+userCode+"]");
						continue;
					}
					Integer vipLevel = user.getInt("vipLevel")!=null?user.getInt("vipLevel"):1;//用户等级
					scanVipLevelLogger.log(Level.INFO,"[定时任务"+(j+1)+"/"+total+":自动扫描会员VIP等级任务,扫描符合【"+tmpVip.getVipLevelName()+"】进行中......"
							+ "用户信息:["+userCode+"]"+user.getStr("userName")+",未来30天待收本息"+amount+",当前用户等级:"+user.getInt("vipLevel")+"]");
					if(tmpVip.getVipLevel() != vipLevel){
						boolean result = vipService.updateUserLevel(userCode, tmpVip);//更改用户会员等级
						scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务,扫描符合【"+tmpVip.getVipLevelName()+"】用户等级"+vipLevel+"更改为"+tmpVip.getVipLevel()+(result?"成功":"失败")+"]");
						
						if(result){//会员等级修改成功
							UserInfo userInfo = userInfoService.findById(userCode);
							if(userInfo == null){
								scanVipLevelLogger.log(Level.INFO,"[定时任务:会员等级首次更新，t_user_info表中无【"+userCode+"】,会员信息缺失]");
								continue;
							}
							String userMobile = "";
							try {
								userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
							} catch (Exception e) {
								scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务，解析【"+userCode+"】【"+user.getStr("userName")+"】的手机号有误]");
							}
							String upgradeInterest = "";//会员升级利率
							boolean isVerify = false;
							//会员首次定级发放所定等级的升级加息券
							if(vipDate.equals(today)){
								upgradeInterest = String.valueOf(tmpVip.getUpgradeInterest());
								isVerify = verifyTickets(userCode, upgradeInterest);
								if(isVerify){
									scanVipLevelLogger.log(Level.INFO, "[定时任务:自动扫描会员VIP等级任务,首次定级扫描用户【"+userCode+"】【"+user.getStr("userName")+"】的会员等级为"+tmpVip.getVipLevel()+",发放"+(double)tmpVip.getUpgradeInterest()/100+"%的加息券1张]");
									ticketsService.saveRate(userCode, user.getStr("userName"), userMobile, userInfo.getStr("userCardName"), "会员升级加息券", DateUtil.addDay(DateUtil.getNowDate(), 30), tmpVip.getUpgradeInterest(), null, SysEnum.makeSource.A, 0, "0", "Y");
								}
								continue;
							}
							//判断用户等级是否更改成功、是升级还是降级
							if(tmpVip.getVipLevel() > vipLevel){//升级
								for (int k = vipLevel + 1; k <= tmpVip.getVipLevel(); k++) {
									upgradeInterest = String.valueOf(VipV2.getVipByLevel(k).getUpgradeInterest());
									isVerify = verifyTickets(userCode, upgradeInterest);
									if(isVerify){
										scanVipLevelLogger.log(Level.INFO, "[定时任务:自动扫描会员VIP等级任务,扫描用户【"+userCode+"】【"+user.getStr("userName")+"】的会员等级从"+vipLevel+"升到"+tmpVip.getVipLevel()+",其中会员等级"+k+"符合首次升级,发放"+(double)VipV2.getVipByLevel(k).getUpgradeInterest()/100+"%的加息券1张]");
										ticketsService.saveRate(userCode, user.getStr("userName"), userMobile, userInfo.getStr("userCardName"), "会员升级加息券", DateUtil.addDay(DateUtil.getNowDate(), 30), VipV2.getVipByLevel(k).getUpgradeInterest(), null, SysEnum.makeSource.A, 0, "0", "Y");
									}
								}
							}
						}
					}
				}
			}
			scanVipLevelLogger.log(Level.INFO,"[定时任务:自动扫描会员VIP等级任务,扫描符合【"+tmpVip.getVipLevelName()+"】完成......共计"+total+"个用户...]");
		}
		return succ("00", "扫描用户VIP等级完成");
	}
	
	/**
	 * 检查升级加息券
	 * @param userCode
	 * @param upgradeInterest	利率
	 * @return
	 */
	public boolean verifyTickets(String userCode, String upgradeInterest){
		boolean result = false;
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("userCode", userCode);
		condition.put("tname", "会员升级加息券");
		condition.put("rate", upgradeInterest);
		List<Tickets> tickets = ticketsService.QueryTicketsByCondition(condition);
		if(tickets == null || tickets.size() <= 0){
			result = true;
		}
		return result;
	}
	
	
	/**
	 * 用户报表
	 * @return
	 */
	@ActionKey("/userTempReport")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message userTempReport(){
		String tmpKey = getPara("key","");
		if(StringUtil.isBlank(tmpKey)){
			return error("01", "呵呵哒", false);
		}
		
		if(tmpKey.equals("3.14159265358") == false){
			return error("02", "呵呵哒", false);
		}
		
		int i = 0;
		while (true) {
			i++;
			Page<User> pageUser = userService.findByPage(i, 1000, null, null, null, null);
			List<User> listUser = pageUser.getList();
			if( null == listUser || listUser.size() <= 0){
				break;
			}
			for (int j = 0; j < listUser.size(); j++) {
				//用户个人信息
				User user = listUser.get(j);
				String userCode = user.getStr("userCode");
				TempReport tr = new TempReport();
				tr.set("userCode", userCode);
				tr.set("userName", user.getStr("userName"));
				tr.set("registDateTime", user.getStr("regDate"));
				long regist4Now = CommonUtil.compareDateTime(DateUtil.getNowDate(), user.getStr("regDate"), "yyyyMMdd");
				tr.set("regist4Now", regist4Now);
				
				
				//待回收信息
				Funds funds = Funds.fundsDao.findById(userCode);
				tr.set("beRecyPrincipal", funds.getLong("beRecyPrincipal"));
				tr.set("beRecyInterest", funds.getLong("beRecyInterest"));
				
				
				//第一次投资信息
				LoanTrace loanTrace = loanTraceService.findFirstLoan(userCode);
				if(null != loanTrace){
					tr.set("firstLoanInfo", loanTrace.getStr("loanDateTime")+"&"+loanTrace.getLong("payAmount")+"&" + loanTrace.getStr("loanUserName"));
				}
				
				//第一次回款信息
				FundsTrace fundsTrace =  fundsTraceService.findFirstBack(userCode);
				if(null != fundsTrace){
					tr.set("firstLoanBackInfo", fundsTrace.getStr("traceDateTime")+"&"+fundsTrace.getLong("traceAmount"));
				}
				
				//投标金额小于某金额笔数
				long loanMinAmountCount = loanTraceService.countLoanByAmount(userCode ,100000);
				tr.set("loanMinAmountCount", loanMinAmountCount);
				
				//最大一次投标金额
				long loanMaxAmount = loanTraceService.queryAmount4Max(userCode);
				tr.set("loanMaxAmount", loanMaxAmount);
				
				//月投资总金额
				long loan4Month = loanTraceService.countPayAmount4User(userCode,"20161001000000", "20161031235959");
				tr.set("loan4Month", loan4Month);
				
				//月赚取收益
				long income4Month = historyRecyService.queryIncome4Month(userCode, "20161001", "20161031");
				tr.set("income4Month", income4Month);
				
				//投标总金额，总笔数
				long[] coungTouBiaoCiShu = loanTraceService.coungTouBiaoCiShu(userCode);
				tr.set("loanAmount", coungTouBiaoCiShu[0]);
				tr.set("loanCount", coungTouBiaoCiShu[1]);
				
				//投资金额超过百分比
				int loanPercent = 0;
				
				if(coungTouBiaoCiShu[0] > 0){
					long totalCount = ViewLoanAmount.viewLoanDao.countAll();
					long minAmountCount = ViewLoanAmount.viewLoanDao.countByAmount(coungTouBiaoCiShu[0], "<=");
					loanPercent = (int)((float)minAmountCount/totalCount * 100);
				}
				tr.set("loanPercent", loanPercent);
				
				
				//总收益
				long sumTraceAmount = fundsTraceService.sumTraceAmount(null, null, "R,L", "J", userCode);
				tr.set("incomeAmount", sumTraceAmount);
				
				//投标类型
				long countLoanByCar = loanTraceService.countLoanByCar(userCode);
				long countLoanByHouse = loanTraceService.countLoanByHouse(userCode);
				tr.set("loanCar", countLoanByCar);
				tr.set("loanHouse", countLoanByHouse);
				
				//邀请总人数
				Page<Share> queryShareList = shareService.queryShareList(1, 2, userCode);
				int shareCount = queryShareList.getTotalRow();
				tr.set("shareCount", shareCount);
				
//				//投资称号
//				String loanName = getLoanTitle(coungTouBiaoCiShu[0], regist4Now, countLoanByCar > countLoanByHouse, shareCount, loanMaxAmountCount);
//				String loanKeyName = getLoanKeyName(loanName);
//				tr.set("loanName", loanName);
//				tr.set("loanKeyName", loanKeyName);
				
				TempReport tempReport = TempReport.tempReportDao.findById(userCode);
				if( null == tempReport ){
					tr.save();
				}else{
					tr.update();
				}
				System.out.println("完成条数 ： " + ((i-1)*1000 +j+1) );
			}
		}
		
		return succ("执行完成", "");
	}
	
	
	public String getLoanTitle(long amount , long registDate , 
			boolean maxLoanCar ,int shareCount,long loanMaxAmountCount){
		String title = "初入江湖";
		if(amount < 1000000){
			title = "初入江湖";
		}else if(amount < 50000000 && amount > 1000000){
			title = "小有所成";
		}else if(amount < 100000000 && amount > 50000000){
			title = "名动天下";
		}else if(amount > 100000000){
			title = "一代宗师";
		}
		
		if(registDate > 730){
			title += "&老朋友";
		}else if(registDate < 730 && amount >= 365){
			title += "&同路人";
		}else{
			title += "&君子之交";
		}
		
		if(maxLoanCar){
			title += "&极品飞车";
		}else{
			title += "&万顷良田";
		}
		
		if(shareCount > 20){
			title += "&高朋满座";
		}else if(shareCount < 20 && shareCount >= 5){
			title += "&广交四海";
		}else{
			title += "&自己偷着乐";
		}
		
		if(loanMaxAmountCount > 5){
			title += "&擒贼擒王";
		}else{
			title += "&捕鱼达人";
		}
		return title;
	}
	
	
	public String getLoanKeyName(String loanName){
		
		String keyName = "";
		
		if(loanName.indexOf("捕鱼达人") != -1){
			keyName = "谨慎";
		}else{
			keyName = "冒险";
		}
		
		if(loanName.indexOf("一代宗师") != -1){
			keyName += "&气魄";
		}else if(loanName.indexOf("名动天下") != -1){
			keyName += "&巅峰";
		}else if(loanName.indexOf("小有所成") != -1){
			keyName += "&幸运";
		}else if(loanName.indexOf("初入江湖") != -1){
			keyName += "&尝试";
		}
		
		if(loanName.indexOf("万顷良田") != -1){
			keyName += "&沉稳";
		}else {
			keyName += "&灵动";
		}
		
		
		if(loanName.indexOf("老朋友") != -1){
			keyName += "&资深";
		}
		if(loanName.indexOf("高朋满座") != -1){
			keyName += "&广泛";
		}
		return keyName;
	}
	
//20170525 ws  富友更换银行卡
	@ActionKey("/changeBankCard")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public void changeBankCard(){
		String mobile= getPara("mobile");
		String newbankNo=getPara("bankNo");
		String bankType=getPara("bankType");
		String bankName=getPara("bankName");
		User user=userService.find4mobile(mobile);
		if(user==null){
			forward("/pages/404.html",true);
		}
		String loginId=user.getStr("loginId");
		if(loginId==null||"".equals(loginId)){
			forward("/pages/404.html",true);
		}
		try {
			loginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			forward("/pages/404.html",true);
		}
		if(null!=mobile){
			ChangeCard2ReqData changeCard =new ChangeCard2ReqData();
			changeCard.setLogin_id(loginId);
			changeCard.setMchnt_cd(CommonUtil.MCHNT_CD);
			String ssn=CommonUtil.genMchntSsn();
			changeCard.setMchnt_txn_ssn(ssn);
			changeCard.setPage_notify_url(CommonUtil.ADDRESS+"/changeBankCardtrace");
			//添加修改银行卡信息记录
			ChangeBankTrace changebankTrace = getModel(ChangeBankTrace.class);
			changebankTrace.set("userCode", user.get("userCode"));
			changebankTrace.set("ssn", ssn);
			changebankTrace.set("state", "5");
			changebankTrace.set("creatDate", DateUtil.getNowDateTime());
			changebankTrace.set("upDate", DateUtil.getNowDateTime());
			BanksV2 banksV2 =bankService.findBanks4User(user.getStr("userCode")).get(0);
			changebankTrace.set("oldBankCardId", banksV2.getStr("bankNo"));
			changebankTrace.set("newBankCardId", newbankNo);
			changebankTrace.set("newBankType", bankType);
			changebankTrace.set("newBankName", bankName);
			if(changebankTrace.save()){
				try {
					FuiouService.changeCard2(changeCard, getResponse());
				} catch (Exception e) {
					forward("/pages/404.html",true);
				}
			}
		}
		renderNull();
	}
	//查询新银行卡是否被使用 20170610 ws
	@ActionKey("/isbankBeused")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message isbankBeused(){
		String newbankNo=getPara("bankNo");
		BanksV2 banksV2 =BanksV2.bankV2Dao.findFirst("select bankNo from t_banks_v2 where bankNo=?",newbankNo);
		if(null==banksV2){
			return succ("可以使用", "");
		}
		return error("66", "该理财卡已被绑定过", "");
		
	}
	//更改银行卡更换状态 20170610 ws
	@ActionKey("/changeBankCardTrace")
	@AuthNum(value=999)
	@Before(Tx.class)
	public void changeBankCardTrace(){
		String resp_code = getPara("resp_code");
		//修改银行卡更换记录
		if("0000".equals(resp_code)){
			String mchnt_txn_ssn= getPara("mchnt_txn_ssn");
			ChangeBankTrace changeBankTrace = ChangeBankTrace.changeBankTraceDao.findFirst("select * from t_changebank_trace where ssn=?",mchnt_txn_ssn);
			changeBankTrace.set("state", "0");
			changeBankTrace.set("upDate", DateUtil.getNowDateTime());
			if(changeBankTrace.update()){
				forward("/pages/update-bank-fuiou.html",true);
			}else{
				forward("/pages/404.html",true);
			}
		}
		forward("/pages/404.html",true);
	}
	//换卡页面 银行名称下拉框
	@ActionKey("/querBankName")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message querBankCodes(){
		List<BankCode> tmp=  BankCode.bankCodeDao.find("select bankCode,bankName from t_bank_Code ");
		return succ("bankcodes", tmp);
	}
	//查询银行卡更换状态
	@ActionKey("/queryBankCardTrace")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message queryBankCardTrace(){
		System.out.println("进入换卡查询接口");
		String mobile= getPara("mobile");
		if(null==mobile){
			return error("01", "手机号为空", "");
		}
		User user=userService.find4mobile(mobile);
		if(null==user){
			return error("02", "无此用户", "");
		}
		String loginId=user.getStr("loginId");
		if(loginId==null||"".equals(loginId)){
			return error("09", "此用户还未开通存管", "");
		}
		try {
			loginId = CommonUtil.decryptUserMobile(user.getStr("loginId"));
		} catch (Exception e1) {
			return error("03", "查询错误", "");
		}
		ChangeBankTrace changeBankTrace = ChangeBankTrace.changeBankTraceDao.findFirst("select * from t_changebank_trace where userCode=? and state =0",user.getStr("userCode"));
		if(null==changeBankTrace){
			return error("04", "查询错误", "");
		}
		QueryChangeCardReqData queryChangeCardReqData=new QueryChangeCardReqData();
		queryChangeCardReqData.setLogin_id(loginId);
		queryChangeCardReqData.setMchnt_cd(CommonUtil.MCHNT_CD);
		String ssn=CommonUtil.genShortUID();
		queryChangeCardReqData.setMchnt_txn_ssn(ssn);
		queryChangeCardReqData.setTxn_ssn(changeBankTrace.getStr("ssn"));
		try {
			QueryChangeCardRspData qcc=FuiouService.queryChangeCard(queryChangeCardReqData);
			if("0000".equals(qcc.getResp_code())){
				if("1".equals(qcc.getExamine_st())){
					//更新更换银行卡记录
					changeBankTrace.set("newBankNo", qcc.getCard_no());//更新新银行卡号
					changeBankTrace.set("state","1");//更新状态
					changeBankTrace.set("upDate", DateUtil.getNowDateTime());//更新时间
					if(changeBankTrace.update()){
						String userCode=user.getStr("userCode");
						BanksV2 bank=BanksV2.bankV2Dao.findById(userCode);
						String biz_content = "银行卡修改：用户 " + userCode + " ,理财卡由【" + bank.getStr("bankNo") + "|" + bank.getStr("bankType") + "|" + bank.getStr("bankName") + "】";
						String bankNo=qcc.getCard_no();
						String bankName=changeBankTrace.getStr("bankName");
						String bankType=changeBankTrace.getStr("bankType");
						bank.set("bankNo",bankNo );
						bank.set("bankType",bankType );
						bank.set("bankName",bankName );
						bank.set("cardCity", "");
						boolean b = bank.update();
						if(b == false){
							return error("03", "银行卡号修改异常", "");
						}
						biz_content += "修改为【" + bankNo + "|" + bankName + "|" + bankType + "】" ;
						BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, biz_content);
						return succ("修改成功!", "");
					}}
					if("2".equals(qcc.getExamine_st())){
						changeBankTrace.set("state","2");//更新状态
						changeBankTrace.set("upDate", DateUtil.getNowDateTime());//更新时间
						changeBankTrace.set("remark", qcc.getRemark());
						if(changeBankTrace.update()){
							return error("07","更新失败,原因为："+qcc.getRemark(), "");
						}
					}
					if("0".equals(qcc.getExamine_st())){
						return error("08","审核中", "");
					}
				
			}
		} catch (Exception e) {
			return error("05", "查询错误", "");
		}
		return error("06", "查询错误,确认查询信息是否正确,用户未提交更换银行卡或者已更换完成", "");
	}
	
	
	/**
	 * 20170906 后台根据usercode查询该用户的投资加权明细
	 * 
	 * @return
	 */
	@ActionKey("/getUserJq")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message getUserJq() {
		String userMobile =getPara("userMobile");
		//userCode="ad4599c92c5f4e8ba9353a3501f77d40";
		String beginDateTime = "20170901000000";
		String endDateTime = "20170930235959";
		String beginDate = "20170901";
		String endDate = "20170930";
		int loanTimeLimit = 0;// 投标期限
		long loanAmountTotalJQ = 0;// 总投资加权金额
		String userName="";
		if(""!=userMobile){
			try {
				userMobile=CommonUtil.encryptUserMobile(userMobile);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		List<LoanTrace> loanTraces = loanTraceService.userAmountByMobile(beginDateTime, endDateTime, userMobile);
		Map<String, Object> map = new HashMap<String, Object>();
		ArrayList<LoanTrace> list=new ArrayList<LoanTrace>();
		if (null != loanTraces) {
			for (int i = 0; i < loanTraces.size(); i++) {
				long loanAmount = 0;
				long loanAmountJQ = 0;
				LoanTransfer loanTransfer = null;
				LoanTrace loanTrace = loanTraces.get(i);
				 userName = loanTrace.getStr("payUserName");
				String traceCode = loanTrace.getStr("traceCode");
				if ("C".equals(loanTrace.getStr("isTransfer"))) { // 不是债权

					loanTimeLimit = loanTrace.getInt("loanTimeLimit");
					loanAmount = loanTrace.getLong("payAmount");
					loanAmountJQ = loanAmount * loanTimeLimit / 12;
				}  else if ("A".equals(loanTrace.getStr("isTransfer"))) {// 转让中
					loanTransfer = loanTransferService.queryLoanTransferByMobile(traceCode, "B", userMobile,
							beginDate, endDate);
					if (null == loanTransfer) {
						loanTimeLimit = loanTrace.getInt("loanTimeLimit");
						loanAmount = loanTrace.getLong("payAmount");
						loanAmountJQ = loanAmount * loanTimeLimit / 12;
					} 
				}
				loanAmountTotalJQ+=loanAmountJQ;
				loanTrace.put("userName",userName);
				loanTrace.put("loanTimeLimit",loanTimeLimit);
				loanTrace.put("loanAmount",Number.longToString(loanAmount));
				loanTrace.put("loanAmountJQ",Number.longToString(loanAmountJQ));
				list.add(loanTrace);
			}
		}
		map.put("loanAmountTotalJQ",Number.longToString(loanAmountTotalJQ));
		map.put("list", list);
		return succ("success", map);
	}
	
	/**
	 * 
	 * 呼唤朋友  全民大闯关   20171120  hW
	 * 查询邀请认证用户数据  及发放闯关的加息券
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/queryInviteData")
	@AuthNum(value=999)
	@Before({Tx.class,PkMsgInterceptor.class})
	public Message queryInviteData(){
		String startDate = getPara("startDate");	// 活动开始日期
		String endDate = getPara("endDate");	// 活动结束日期
		int countTickets = 0;
		// 密匙验证
		String key = getPara("key", "");
		if (!"3.14159265358".equals(key)) {
			return error("01", "密匙错误", null);
		}
		
		if (StringUtil.isBlank(startDate)) {
			return error("02", "开始时间错误", null);
		}
		
		// 如果结束日期为空，则设结束时间为当前日期
		if (StringUtil.isBlank(endDate)) {
			endDate = DateUtil.getNowDate();
		}
		
		// 活动开始时间不能晚于活动结束时间
		if (!(DateUtil.compareDateByStr("yyyyMMdd", startDate, endDate) == -1) && !(DateUtil.compareDateByStr("yyyyMMdd", startDate, endDate) == 0)) {
			return error("01", "活动时间有误，请检查时间", "");
		}
	
		// 查询邀请人列表
		List<String> listInvite = recommendInfoService.queryUserCodeByHasRecommend(startDate, endDate);
		if (listInvite == null || listInvite.isEmpty()) {
			return error("01", "无邀请人", "");
		}
		
		for (String userCode : listInvite) {
			Map<String, String> condition = new HashMap<String, String>();
			List<Tickets> lstTickets = null;
			User user = userService.findById(userCode);
			UserInfo userInfo = userInfoService.findById(userCode);
			String userMobile = null;
			try {
				userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
			} catch (Exception e) {
			}
			// 查询被邀请认证用户列表
			List<UserInfo> lstRecommendUserInfo = userInfoService.queryRecommendByUserCode(userCode, startDate, endDate);
			if (lstRecommendUserInfo.size() >= 1) {
				condition = new HashMap<String, String>();
				condition.put("userCode", userCode);
				condition.put("tname", "邀新加息券");
				condition.put("rate", "50");
				lstTickets = ticketsService.QueryTicketsByCondition(condition);	// 查询是否发放过0.5%邀新加息券
				if (lstTickets.size() < 1) {	// 如果未发放过0.5%邀新加息券
					// 检查被邀请人首投是否大于1000
					for (UserInfo recommendUser : lstRecommendUserInfo) {
						long firstInvestAmount = fundsTraceService.queryFirstInvestAmount(recommendUser.getStr("userCode"));
						if (firstInvestAmount >= 100000) {	// 被邀请人首投大于等于1000 则发放0.5%邀新加息券
							ticketsService.saveRate(userCode, user.getStr("userName"), userMobile, userInfo.getStr("userCardName"), "邀新加息券", DateUtil.addDay(DateUtil.getNowDate(), 30), 50, null, SysEnum.makeSource.B, 0, "0", "Y");
							lstTickets.add(new Tickets());
							countTickets ++;
							break;
						}
					}
				}
				if (lstRecommendUserInfo.size() >= 3 && lstTickets.size() >= 1) {	// 累计邀新大于3人，且已发放0.5%邀新加自券 
					condition = new HashMap<String, String>();
					condition.put("userCode", userCode);
					condition.put("tname", "邀新加息券");
					condition.put("rate", "80");
					lstTickets = ticketsService.QueryTicketsByCondition(condition);	// 查询是否发放过0.8%邀新加息券
					if (lstTickets.size() < 1) {	// 如果未发放过0.8%邀新加息券
						long sumAmount = fundsTraceService.sumInvestAmount4BeRecommondByInvite(userCode, startDate, endDate);
						if (sumAmount >= 1000000) {	// 被邀请人总投资金额大于等于10000 则发放0.8%邀新加息券
							ticketsService.saveRate(userCode, user.getStr("userName"), userMobile, userInfo.getStr("userCardName"), "邀新加息券", DateUtil.addDay(DateUtil.getNowDate(), 30), 80, null, SysEnum.makeSource.B, 0, "0", "Y");
							lstTickets.add(new Tickets());
							countTickets ++;
						}
					}
					if (lstRecommendUserInfo.size() >= 5 && lstTickets.size() >= 1) {	// 累计邀新大于3人，且已发放0.8%邀新加自券 
						condition = new HashMap<String, String>();
						condition.put("userCode", userCode);
						condition.put("tname", "邀新加息券");
						condition.put("rate", "100");
						lstTickets = ticketsService.QueryTicketsByCondition(condition);	// 查询是否发放过1%邀新加息券
						if (lstTickets.size() < 1) {	// 如果未发放过1%邀新加息券
							long sumAmount = fundsTraceService.sumInvestAmount4BeRecommondByInvite(userCode, startDate, endDate);
							if (sumAmount >= 3000000) {	// 被邀请人总投资金额大于等于30000 则发放0.8%邀新加息券
								ticketsService.saveRate(userCode, user.getStr("userName"), userMobile, userInfo.getStr("userCardName"), "邀新加息券", DateUtil.addDay(DateUtil.getNowDate(), 30), 100, null, SysEnum.makeSource.B, 0, "0", "Y");
								countTickets ++;
							}
						}
					}
				}
			}
		}
		return succ("发放邀新加息券完成，共发放加息券" + countTickets + "张", null);
	}
	
	@ActionKey("/openAccountByAdmin")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message openFuiouAccountByAdmin() {
		FuiouController fuiouController = new FuiouController();
		
		String userTrueName = getPara("userName");
		String cardNo = getPara("cardNo").toUpperCase();
		String phoneNo = getPara("phoneNo");
		String provinceName = getPara("provinceName");
		String cityName = getPara("cityName");
		String bankName = getPara("bankName");
		String cityCode = getPara("cityCode");
		String bankCode = getPara("bankCode");
		String bankNo = getPara("bankNo");

		if (StringUtil.isBlank(userTrueName)) {
			return error("01", "姓名为空!", "");
		}
		if (!IdCardUtils.validateCard(cardNo)) {
			return error("01", "身份证不正确!", "");
		}

		User user = userService.findByMobile(phoneNo);
		if (user == null) {
			return error("13", "无此用户", "");
		}
		String userCode = user.getStr("userCode");
		String md5CardId = "";
		try {
			md5CardId = CommonUtil.encryptUserCardId(cardNo);
		} catch (Exception e) {
			return error("02", "身份证加密错误", "");
		}
		// 查询身份证号是否已被绑定
		UserInfo userInfo = userInfoService.findByCardId(cardNo);
		if (userInfo != null) {
			return error("01", "该身份证号已被绑定", "");
		}
		// 查询该银行卡是否已被使用
		BanksV2 banksV2 = bankService.findByBankNo(bankNo);
		if (banksV2 != null) {
			return error("13", "该银行卡已被使用", "");
		}		
		if (!bankCode.equals(CommonUtil.checkBankCode(bankName))) {
			return error("12", "绑定银行卡失败，银行代码错误", "");
		}

		String traceCode = CommonUtil.genMchntSsn();
		// 设置开户信息
		RegReqData regReqData = new RegReqData();
		regReqData.setVer(FuiouController.VER);
		regReqData.setMchnt_cd(FuiouController.MCHNT_CD);// 商户号
		regReqData.setMchnt_txn_ssn(traceCode);// 流水号
		regReqData.setCust_nm(userTrueName);// 用户名
		regReqData.setCertif_tp("0");// 证件类型
		regReqData.setCertif_id(cardNo);// 身份证号
		regReqData.setMobile_no(phoneNo);// 手机号
		regReqData.setCity_id(cityCode);// 银行卡地区号
		regReqData.setParent_bank_id(bankCode);// 银行代码
		regReqData.setCapAcntNo(bankNo);// 银行卡号
		String payPwd = phoneNo.substring(phoneNo.length() - 7,
				phoneNo.length() - 1);// 登录密码（默认loginId后6位）
		try {
			regReqData.setLpassword(CommonUtil.encryptPasswd(payPwd));// 登录密码密文
		} catch (UnsupportedEncodingException e1) {
			return error("09", "提交开户信息失败", "");
		}

		// 查询是否已开存管户
		try {
			boolean isFuiouAccount = FuiouController.isFuiouAccount(user);
			if (isFuiouAccount == false) {
				CommonRspData commonRspData = FuiouService.reg(regReqData);
				if ("0000".equals(commonRspData.getResp_code())) {
					// 本地数据库存数据
					banksV2 = BanksV2.bankV2Dao.findById(userCode);
					if (banksV2 != null) {// 激活开户
						banksV2.set("trueName", userTrueName);
						banksV2.set("cardCity", provinceName + "|" + cityName);
						banksV2.set("ssn", traceCode);
						banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
						if (banksV2.update()) {
							user.set("loginId", user.getStr("userMobile"));
							user.update();
						} else {
							return error("12", "激活银行卡失败，本地数据添加失败", null);
						}
					} else {// 正常开户
						BanksV2 bankV2 = getModel(BanksV2.class);
						bankV2.set("bankName", bankName);
						bankV2.set("bankNo", bankNo);
						bankV2.set("bankType", bankCode);
						bankV2.set("cardCity", provinceName + "|" + cityName);
						bankV2.set("userCode", userCode);
						bankV2.set("userName", user.getStr("userName"));
						bankV2.set("trueName", userTrueName);
						bankV2.set("cardid", md5CardId);
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
								fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
							}
						} else {
							return error("11", "本地数据更新失败", null);
						}
					}

					Funds funds = fundsServiceV2.findById(userCode);
					long avBalance = funds.getLong("avBalance");
					long frozeBalance = funds.getLong("frozeBalance");
					if (avBalance + frozeBalance > 0) {
						QueryBalanceResultData fuiouFunds = fuiouController.queryBalance(user);
						if (Long.parseLong(fuiouFunds.getCa_balance()) == 0) {
							CommonRspData comm = fuiouController.transferBmu(avBalance + frozeBalance, user, FuiouTraceType.E);	// 转账到个人账户
							if ("0000".equals(comm.getResp_code())) {
								if (frozeBalance > 0) {	// 如果冻结金额大于0，则冻结存管账户相同金额
									fuiouController.freeze(user, frozeBalance);
								}
							} else {
								user.set("loginId", "");
								user.update();
								return error("01", "存管账户已开通，资金转入失败", null);
							}
						}
					}
					BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "激活账户成功 ");
					Memcached.set("PORTAL_USER_" + userCode, user);
					boolean isUpdate = userInfoService.userAuth(userCode, userTrueName, md5CardId, "", "2");
					if (!isUpdate) {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "自动认证异常或重复认证", null);
						return error("05", "已经认证,请勿重复提交!", "");
					}
					return succ("ok", "激活开通成功");

				} else {
					return error("09", "添加失败", "");
				}
			} else {
				return error("06", "存管账户已存在", "");
			}

		} catch (Exception e) {
			return error("08", "系统错误，添加失败", null);
		}
	}
 
	
	/**
	 * 后台存管开户
	 * @return
	 */
	@ActionKey("/jxOpenAccountByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message jxOpenAccountByAdmin(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		String userCode = getPara("userCode");
		User user = userService.findById(userCode);
		if (null == user) {
			return error("01", "用户查询错误", "");
		}
		String userCardName = "";
		String userCardId = "";
		UserInfo userInfo = null;
		String trueName = getPara("trueName", "");// 真实姓名
		String cardId = getPara("cardId", "");// 身份证号
		String mobile = getPara("mobile", "");
		String idType = getPara("idType");
		int type = getParaToInt("type");
		
		if(!CommonUtil.isMobile(mobile.trim())){
			return error("02", "手机号校验不通过", "");
		}
		// 验证用户是否已实名认证
		userInfo = userInfoService.findById(userCode);
		if (userInfo == null || !"2".equals(userInfo.getStr("isAuthed"))) {// 若未实名认证，则验证身份参数
			if (!IdCardUtils.validateCard(cardId)) {
				return error("03", "身份证号不正确！", "");
			}
			// 验证身份证是否已经被认证
			UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
			if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
				return error("04", "身份证号已被认证", "");
			}
			userCardName = trueName;
			userCardId = cardId;

		} else {// 若已实名
			try {
				userCardName = userInfo.getStr("userCardName");
				userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			} catch (Exception e) {
				return error("05", "用户身份证号解析错误", "");
			}
		}

		if (StringUtil.isBlank(userCardName)) {
			return error("06", "真实姓名不能为空", null);
		}

		// 查询用户是否已开存管户-按证件号查询电子账号
		HttpServletResponse response = getResponse();
		Map<String, String> accountIdQuery = null;
		try {
			// 根据证件号查询存管电子账号
			accountIdQuery = JXQueryController.accountIdQuery(idType, userCardId);
		} catch (Exception e) {
			return error("09", "存管系统异常", "");
		}
		if (accountIdQuery == null || StringUtil.isBlank(accountIdQuery.get("accountId"))) {// 未开通过存管
			// 账户用途：00000-普通账户 投资人默认
			String acctUse = "";
			// 身份属性：1：出借角色 2：借款角色 3：代偿角色
			String identity = "";
			// 根据身份证编号获取性别
			String gender = IdCardUtils.getGenderByIdCard(userCardId);
			switch (type) {
			/*case 1:
				acctUse = "00000";// 出借人-普通账户
				identity = "1";
				break;*/
			case 2:
				acctUse = "00000";// 借款人-普通账户
				identity = "2";
				break;
			case 3:
				acctUse = "00100";// 代偿角色-担保账户
				identity = "3";
				break;
			/*case 4:
				acctUse = "10000";// 出借人-红包账户
				identity = "1";
				break;
			case 5:
				acctUse = "01000";// 出借人-手续费账户
				identity = "1";
				break;*/
			default:
				return error("09", "无此账户类型", "");
			}
			String retUrl = CommonUtil.NIUX_URL+"/main";
			String successfulUrl = CommonUtil.NIUX_URL+"/main";
			String notifyUrl = CommonUtil.NIUX_URL+"/showOpenAccountRes?uCode="+userCode;
			// 调用存管开户接口
			JXController.accountOpenEncryptPage(idType, userCardName, gender, mobile.trim(), "", acctUse, "", identity, retUrl, successfulUrl, notifyUrl,response);
			return succ("调用存管开户接口成功", "");
		} else {
			return error("09", "存管账户已存在", "");
		}
	}
	
	@ActionKey("/showOpenAccountRes")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showOpenAccountRes(){
		HttpServletRequest request = getRequest();
		// 通知数据
		String bgData = request.getParameter("bgData");
		String userCode = getPara("uCode");
		if (StringUtil.isBlank(bgData)) {// 响应数据为空
			return;
		}
		@SuppressWarnings("unchecked")
		Map<String, String> map = net.sf.json.JSONObject.fromObject(bgData);
		// 生成本地报文流水号
		String jxCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		// 将响应报文存入数据库
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		BanksV2 banksV2 = null;
		UserInfo userInfo = null;
		String mobile = "";
		String cardNo = "";
		String bankName = "";
		User user = null;
		String idType = "01";//证件类型
		String idNo = "";
		String trueName = "";
		if (null != map && "00000000".equals(map.get("retCode"))) {// 存管开通成功
			// 存管电子账号
			String accountId = map.get("accountId");

			// 根据存管电子账号查询绑查关系(只查有效卡)
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
			if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

				@SuppressWarnings("unchecked")
				List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
				if (list != null && list.size() > 0) {

					Map<String, String> cardMap = list.get(0);
					cardNo = cardMap.get("cardNo");// 有效存管卡号
					// 根据电子账号查询用户手机号
					Map<String, String> mobileMap = JXQueryController.mobileMaintainace(accountId, "0", mobile);
					if (null != mobileMap && "00000000".equals(mobileMap.get("retCode"))) {
						mobile = mobileMap.get("mobile");
						idNo = mobileMap.get("idNo");
						trueName = mobileMap.get("name");
						idType = mobileMap.get("idType");
						
						//查询所属银行
						String nameOfBank = BankUtil.getNameOfBank(cardNo);
						if(!StringUtil.isBlank(nameOfBank)){
							bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
						}
						
						if (StringUtil.isBlank(userCode)) {// 如果uCode为空，根据证件号码查询用户信息
							userInfo = userInfoService.findByCardId(idNo);
							userCode = userInfo.getStr("userCode");
						}else{
							userInfo = userInfoService.findById(userCode);
						}
						
						user = userService.findById(userCode);
						try {
							idNo = CommonUtil.encryptUserCardId(idNo);
							mobile = CommonUtil.encryptUserMobile(mobile);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					//实名认证
					if (!"2".equals(userInfo.getStr("isAuthed"))) {
						try {
							Message msgAuto = certificationAuto(userCode, trueName, idNo, idType);
							if (msgAuto != null) {
								BIZ_LOG_ERROR(userCode, BIZ_TYPE.IDENTIFY, "实名认证失败", null);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}else if("2".equals(userInfo.getStr("isAuthed"))){
						userInfo.set("idType", idType);
						userInfo.update();
					}
					
				} else {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "存管开户，未查找到用户的有效卡信息", null);
					return;
				}
				// 更新数据库数据
				banksV2 = bankService.findByUserCode(userCode);
				if (banksV2 != null) {
					banksV2.set("trueName", cardDetails.get("name"));
					banksV2.set("bankNo", cardNo);
					banksV2.set("bankName", bankName);
					banksV2.set("cardid", idNo);
					banksV2.set("mobile", mobile);// 存管手机号
					banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
					banksV2.set("ssn", jxCode);
					if (banksV2.update()) {
						user.set("jxAccountId", accountId);
						user.update();
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "激活银行卡_本地数据添加失败", null);
						return;
					}
				} else {// 添加银行卡信息
					BanksV2 bankV2 = getModel(BanksV2.class);
					bankV2.set("userCode", userCode);
					bankV2.set("userName", user.getStr("userName"));
					bankV2.set("trueName", cardDetails.get("name"));
					bankV2.set("bankName", bankName);
					bankV2.set("bankNo", cardNo);
					bankV2.set("bankType", "");
					bankV2.set("cardCity", "");
					bankV2.set("mobile", mobile);
					bankV2.set("createDateTime", DateUtil.getNowDateTime());
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
					bankV2.set("isDefault", "1");
					bankV2.set("status", "0");
					bankV2.set("agreeCode", jxCode);
					bankV2.set("ssn", jxCode);
					bankV2.set("cardid", idNo);
					if (bankV2.save()) {
						user.set("jxAccountId", accountId);
						if (user.update()) {
							// 银行卡添加成功，获取可用积分
							fundsServiceV2.doPoints(userCode, 0, 3000, "注册送积分");
						} else {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加电子存管账户_本地数据更新失败", null);
							return;
						}
					} else {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "添加银行卡信息_本地数据更新失败", null);
						return;
					}
				}
			} else {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "未查找到存管有效绑卡信息", null);
				return;
			}
		} else {// 存管开户失败
			BIZ_LOG_INFO(userCode, BIZ_TYPE.TERROR, "存管开户失败", bgData);
		}
		renderText("success");
	}
	
	/**
	 * 后台实名认证
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
		return null;
	}
	
	/**
	 * 20180827
	 * 设置存管交易密码
	 * @return
	 */
	@ActionKey("/setDepositPwdByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void setDepositPwdByAdmin(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return ;
		}
		String userCode = getPara("userCode");
		if (userCode == null) {
			msg = error("01", "未登录", "");
			renderJson(msg);
			return ;
		}
		HttpServletResponse response = getResponse();
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);

		if (null == user || null == userInfo) {
			msg = error("01", "未查找到用户信息", "");
			renderJson(msg);
			return ;
		}
		String jxAccountId = user.getStr("jxAccountId");
		if (null == jxAccountId || "".equals(jxAccountId)) {
			msg = error("02", "请先开通存管账户再设置存管密码", "");
			renderJson(msg);
			return ;
		}
		
		String mobile = "";
		BanksV2 banksV2 = bankService.findByUserCode(userCode);
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
			return ;
		}
		try {
			mobile = CommonUtil.decryptUserMobile(mobile);
			idNo = CommonUtil.decryptUserCardId(idNo);
		} catch (Exception e) {
			msg = error("03", "手机号或证件号解析异常", "");
			renderJson(msg);
			return ;
		}
		if (StringUtil.isBlank(mobile) || StringUtil.isBlank(idNo)) {
			msg = error("05", "手机号或证件号为空", "");
			renderJson(msg);
			return ;
		}
		String name = userInfo.getStr("userCardName");
		if (null == name || "".equals(name)) {
			msg = error("06", "姓名不能为空", "");
			renderJson(msg);
			return ;
		}
		//检查用户是否绑定过银行卡
//				String isBind = JXController.verifyBindCard(jxAccountId, "0");
//				if("n".equals(isBind)){
//					msg = error("07", "先绑卡才能设置密码", "");
//					renderJson(msg);
//					return ;
//				}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("setPwdByAdmin_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("setPwdByAdmin_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("setPwdByAdmin_"+verifyOrderId+userCode);
		String retUrl = CommonUtil.NIUX_URL+"/main";
		String notifyUrl = CommonUtil.NIUX_URL+"/showSetPwdByAdmin?userCode="+userCode;
		JXController.passwordResetPage(jxAccountId, idType, retUrl, notifyUrl, response);
		renderNull();
	}
	
	/**
	 * 后台接收密码设置响应
	 */
	@ActionKey("/showSetPwdByAdmin")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showSetPwdByAdmin(){
		HttpServletRequest request = getRequest();
		String userCode = getPara("userCode","");//待解决
		//响应参数
		String parameter = request.getParameter("bgData");
		@SuppressWarnings("unchecked")
		Map<String, String> map = net.sf.json.JSONObject.fromObject(parameter);
		//生成本地报文流水号
		String jxCode =  "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		//将响应报文存入数据库
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		
		if(null != map && "00000000".equals(map.get("retCode"))){
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "后台操作存管密码设置成功");
		}else{
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "后台操作存管密码设置失败："+map.get("retCode")+","+map.get("retMsg"), null);
		}
		renderText("success");
	}
	/**
	 * 20180827
	 * 修改存管交易密码
	 * @return
	 */
	@ActionKey("/modifyPwdByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void modifyPwdByAdmin(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return ;
		}
		String userCode = getPara("userCode");
		if (userCode == null) {
			msg = error("01", "未登录", "");
			renderJson(msg);
			return ;
		}
		HttpServletResponse response = getResponse();
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);

		if (null == user || null == userInfo) {
			msg = error("01", "未查找到用户信息", "");
			renderJson(msg);
			return ;
		}
		String jxAccountId = user.getStr("jxAccountId");
		if (null == jxAccountId || "".equals(jxAccountId)) {
			msg = error("02", "请先开通存管账户再修改存管密码", "");
			renderJson(msg);
			return ;
		}
		
		String mobile = "";
		BanksV2 banksV2 = bankService.findByUserCode(userCode);
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
			return ;
		}
		try {
			mobile = CommonUtil.decryptUserMobile(mobile);
			idNo = CommonUtil.decryptUserCardId(idNo);
		} catch (Exception e) {
			msg = error("03", "手机号或证件号解析异常", "");
			renderJson(msg);
			return ;
		}
		if (StringUtil.isBlank(mobile) || StringUtil.isBlank(idNo)) {
			msg = error("05", "手机号或证件号为空", "");
			renderJson(msg);
			return ;
		}
		String name = userInfo.getStr("userCardName");
		if (null == name || "".equals(name)) {
			msg = error("06", "姓名不能为空", "");
			renderJson(msg);
			return ;
		}
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
			msg = error("07", "未设置过电子账户密码", "");
			renderJson(msg);
			return;
		}
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("modifyPwdByAdmin_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("modifyPwdByAdmin_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("modifyPwdByAdmin_"+verifyOrderId+userCode);
		String retUrl = CommonUtil.NIUX_URL+"/main";
		String notifyUrl = CommonUtil.NIUX_URL+"/adminModifyPwdCallback?userCode="+userCode;
		JXController.passwordUpdate(jxAccountId, name, retUrl, notifyUrl, response);
		renderNull();
	}
	/**
	 * 后台接收密码修改响应
	 */
	@ActionKey("/adminModifyPwdCallback")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void adminModifyPwdCallback(){
		HttpServletRequest request = getRequest();
		String userCode = getPara("userCode","");
		//响应参数
		String parameter = request.getParameter("bgData");
		@SuppressWarnings("unchecked")
		Map<String, String> map = net.sf.json.JSONObject.fromObject(parameter);
		//生成本地报文流水号
		String jxCode =  "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		//将响应报文存入数据库
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		
		if(null != map && "00000000".equals(map.get("retCode"))){
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.FINDPWD, "后台操作存管密码修改成功");
		}else{
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.FINDPWD, "后台操作存管密码修改失败："+map.get("retCode")+","+map.get("retMsg"), null);
		}
		renderText("success");
	}
	/**
	 * 存管短信请求接口——后台
	 * @return
	 */
	@ActionKey("/smsCode_admin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message smsCode_admin(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		String userCode = getPara("userCode","");
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
		
		BanksV2 banksV2 = bankService.findByUserCode(userCode);
		if(banksV2 == null){
			return error("02", "请完善银行卡信息", "");
		}
		
		if("0".equals(type)){//修改存管手机号时短信发送至新手机号，需要前台传
			mobile = getPara("mobile","");
			if(StringUtil.isBlank(mobile)){
				return error("03", "手机号不能为空", "");
			}
		}else{
			mobile = banksV2.getStr("mobile");
			try {
				mobile = CommonUtil.decryptUserMobile(mobile);
			} catch (Exception e) {
				return error("05", "发送短信：手机号解析异常", "");
			}
		}
		String cardNo = "";//reqType为2时cardNo必填
		if("2".equals(reqType)){
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
			memcachedKey = "SMS_MSG_MODIFYMOBILE_ADMIN_";
			break;
		case "1":
			srvTxCode = "passwordResetPlus";//存管密码重置
			memcachedKey = "SMS_MSG_RESETPWD_ADMIN_";
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
			Memcached.set(memcachedKey+ userCode, srvAuthCode, 10*60*1000);
			
			return succ("短信发送成功", "00000000");
		}else{
			return error("10", "短信发送失败,"+resMap.get("retCode")+":"+resMap.get("retMsg"), "");
		}
	}
	
	/**
	 * 重置存管交易密码——后台
	 */
	@ActionKey("/resetPwdPlusByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void resetPwdPlusByAdmin(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return ;
		}
		String userCode = getPara("userCode");
		User user = userService.findById(userCode);
		if(user == null){
			msg = error("01", "用户未登录", "");
			renderJson(msg);
			return ;
		}
		String smsCode = getPara("smsCode","");//短信验证码
		//前导业务授权码
		String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_RESETPWD_ADMIN_"+user.getStr("userCode"));
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
		
		String userMobile = "";
		BanksV2 banksV2 = bankService.findByUserCode(userCode);
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
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("retsetPwdByAdmin_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
			if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
				msg = error("02", "未设置过电子账户密码", "");
				renderJson(msg);
				return ;
			}
			
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("retsetPwdByAdmin_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("retsetPwdByAdmin_"+verifyOrderId+userCode);
		//返回交易页面链接
		String retUrl = CommonUtil.NIUX_URL+"/main";
		//后台通知链接
		String notifyUrl = CommonUtil.NIUX_URL+"/showResetPwdResult";
		//交易成功跳转链接 
		String successfulUrl = "";
		HttpServletResponse response = getResponse();
		JXController jxController = new JXController();
		jxController.passwordResetPlus(jxAccountId, idType, userCardId, userCardName, userMobile, lastSrvAuthCode, smsCode, retUrl, notifyUrl, successfulUrl, response);
		renderNull();
	}
	
	/**
	 * 接收重置交易密码响应
	 */
	@ActionKey("/showResetPwdResult")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showResetPwdResult(){
		HttpServletRequest request =getRequest();
		String userCode = getPara("userCode", "");

		String parameter =request.getParameter("bgData");
		@SuppressWarnings("unchecked")
		Map<String, String> map = net.sf.json.JSONObject.fromObject(parameter);

		if (map == null) {
			BIZ_LOG_ERROR(userCode,BIZ_TYPE.FINDPWD, "重置密码响应参数为空", null);
			return;
		}

		String jxTraceCode = "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		// 存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), map, JSON.toJSON(map).toString().replace(",", ",\r\n"));

		if ("00000000".equals(map.get("retCode"))) {
			BIZ_LOG_INFO(userCode,BIZ_TYPE.FINDPWD, "重置密码成功");
		} else {
			BIZ_LOG_INFO(userCode,BIZ_TYPE.FINDPWD, "重置密码失败：" + map.get("retCode") + "," + map.get("retMsg"));
		}
		renderText("success");
	}
	
	/**
	 * 修改存管手机号——后台
	 * @return
	 */
	@ActionKey("/modifyDepositMobileByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modifyDepositMobileByAdmin(){
		if(!CommonUtil.jxPort){
			return error("666", "江西银行对接中……", "");
		}
		String userCode = getPara("userCode","");
		User user = userService.findById(userCode);
		if(null == user){
			return error("01", "请登录之后再操作", "");
		}
		String newMobile = getPara("mobile");
		if(newMobile == null || "".equals(newMobile)){
			return error("02", "新手机号不能为空", "");
		}
		String smsCode = getPara("smsCode");
		//前导业务授权码
		String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_MODIFYMOBILE_ADMIN_" + user.getStr("userCode"));
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
			BanksV2 banksV2 = bankService.findByUserCode(user.getStr("userCode"));
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
	 * 后台解绑借款人银行卡
	 * @return
	 */
	@ActionKey("/unbindCardByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void unbindCardByAdmin(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		String userCode = getPara("userCode","");
		User user = userService.findById(userCode);
		if(null == user){
			msg = error("01", "用户未登录", "");
			renderJson(msg);
			return;
		}
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
		
		BanksV2 banksV2 = bankService.findByUserCode(userCode);
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
		//检查借款是否有未还清的标
		int a = Db.queryLong("select count(1) from t_loan_info where userCode = ? and loanState = 'N' ", userCode).intValue();
		if(a > 0){
			msg = error("09", "无法解绑银行卡_还有标的未结清", "");
			renderJson(msg);
			return;
		}
		//调用解绑银行卡接口
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("unbindCard_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("unbindCard_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("unbindCard_"+verifyOrderId+userCode);
		String retUrl=CommonUtil.NIUX_URL+"/main";
		String forgotPwdUrl=CommonUtil.NIUX_URL+"/main";
		String  notifyUrl=CommonUtil.CALLBACK_URL+"/adminUnbindCardPageCallback?userCode="+userCode;
		//调用解绑银行卡接口
		JXController.unbindCardPage(jxAccountId, userCardName, idType, idNo, bankNo, userMobile, retUrl, forgotPwdUrl, notifyUrl, getResponse());
		renderNull();
	}
	/**
	 * 后台解绑卡回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/adminUnbindCardPageCallback")
	public void unbindCardPageCallback() {
		String bgData = getPara("bgData");
		String userCode = getPara("userCode", "");
		@SuppressWarnings("unchecked")
		Map<String, String> map = net.sf.json.JSONObject.fromObject(bgData);
		//生成本地报文流水号
		String jxCode =  "" + map.get("txDate") + map.get("txTime") + map.get("seqNo");
		//将响应报文存入数据库
		JXService.updateJxTraceResponse(jxCode, map, JSON.toJSON(map).toString().replace(",", ",\r\n"));
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		if(null != map && "00000000".equals(map.get("retCode"))){
			banksV2.set("bankNo", "");
			//记录日志
			if(banksV2.update()){
				BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, "解绑银行卡成功");
			}
		}else{
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "解绑银行卡失败："+map.get("retCode")+","+map.get("retMsg"), null);
		}
		renderText("success");
	
}
	/**
	 * 换/绑存管银行卡——后台
	 */
	@ActionKey("/depositBindCardByAdmin")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void depositBindCardByAdmin(){
		Message msg = new Message();
		if(!CommonUtil.jxPort){
			msg = error("666", "江西银行对接中……", "");
			renderJson(msg);
			return;
		}
		String userCode = getPara("userCode","");
		User user = userService.findById(userCode);
		if(user == null){
			msg = error("01", "用户未登录，请登录之后再进行相关操作", "");
			renderJson(msg);
			return;
		}
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
		
		String verifyOrderId = getPara("verifyOrderId","");
		String orgOrderId = (String)Memcached.get("bindCard_admin_"+verifyOrderId+userCode);
		if(StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orgOrderId)){
			//检查用户是否已设置交易密码
			Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
			String pinFlag = pwdMap.get("pinFlag");
			if("0".equals(pinFlag)){
				msg = error("09", "还未设置电子账户交易密码", "");
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
			
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("bindCard_admin_"+verifyOrderId+userCode, verifyOrderId, 60*1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("bindCard_admin_"+verifyOrderId+userCode);
		//客户IP
		String userIP = getRequestIP();
		
		if(userIP == null){
			userIP = "";
		}
		String isBind = JXController.verifyBindCard(jxAccountId, "0");
		String successfulUrl = CommonUtil.NIUX_URL+"/main";
		if("n".equals(isBind)){//没有绑定过银行卡——老用户
			successfulUrl= CommonUtil.NIUX_URL + "/main";
		}
		String retUrl = CommonUtil.NIUX_URL+"";
		String notifyUrl = CommonUtil.NIUX_URL+"/showDepositBindCardResult";
		
		HttpServletResponse response = getResponse();
		JXController.bindCardPage(idType, idNo, userCardName, jxAccountId, userIP, retUrl, notifyUrl, successfulUrl, response);
		
		renderNull();
	}
	
	
	/**
	 * 接收换绑卡响应
	 */
	@ActionKey("/showDepositBindCardResult")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showDepositBindCardResult(){
		HttpServletRequest request = getRequest();
		
		String parameter = request.getParameter("bgData");
		
		@SuppressWarnings("unchecked")
		Map<String, String> resMap = net.sf.json.JSONObject.fromObject(parameter);
		if(resMap==null){
			return ;
		}
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		
		String bankNo = "";
		String mobile = "";
		String bankName = "";
		//获取电子账户
		String jxAccountId = resMap.get("accountId");
		User user = userService.findByJXAccountId(jxAccountId);
		if(user == null){//未找到对应的用户
			return ;
		}
		String userCode = user.getStr("userCode");
		
		if("00000000".equals(resMap.get("retCode"))){//换绑卡成功
			//通过电子账户获取有效绑卡信息
			Map<String, Object> cardMap = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
			if(cardMap != null && "00000000".equals(cardMap.get("retCode"))){
				List<Map<String, String>> list = (List<Map<String, String>>)cardMap.get("subPacks");
				if(list != null && list.size() > 0){
					Map<String, String> map = list.get(0);
					bankNo = map.get("cardNo");
					//查询所属银行
					String nameOfBank = BankUtil.getNameOfBank(bankNo);
					if(!StringUtil.isBlank(nameOfBank)){
						bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
					}
					
				}
				//根据电子账号查询用户手机号
				Map<String, String> mobileMap = JXQueryController.mobileMaintainace(jxAccountId, "0", mobile);
				if(null != mobileMap && "00000000".equals(mobileMap.get("retCode"))){
					mobile = mobileMap.get("mobile");
					try {
						mobile = CommonUtil.encryptUserMobile(mobile);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
				UserInfo userInfo = userInfoService.findById(userCode);
				
				//查询用户的绑上信息
				BanksV2 banksV2 = bankService.findByUserCode(userCode);
				if(banksV2 != null){
					banksV2.set("trueName",cardMap.get("name"));
					banksV2.set("bankNo", bankNo);
					banksV2.set("bankName",bankName ) ;
					banksV2.set("mobile", mobile);
					banksV2.set("cardid", userInfo.getStr("userCardId"));
					banksV2.set("modifyDateTime", DateUtil.getNowDateTime());
					banksV2.set("ssn", jxTraceCode);
					if(banksV2.update()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "换绑卡成功，更新本地数据", "");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡成功，未更新本地数据", null);
						return ;
					}
				}else{
					BanksV2 bankV2 = getModel(BanksV2.class);
					bankV2.set("userCode", userCode);
					bankV2.set("userName", user.getStr("userName"));
					bankV2.set("trueName", cardMap.get("name"));
					bankV2.set("bankName",bankName ) ;
					bankV2.set("bankNo", bankNo);
					bankV2.set("bankType","" ) ;
					bankV2.set("cardCity", "");
					bankV2.set("mobile", mobile);
					bankV2.set("createDateTime", DateUtil.getNowDateTime());
					bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
					bankV2.set("isDefault", "1");
					bankV2.set("status", "0");
					bankV2.set("agreeCode", jxTraceCode);
					bankV2.set("ssn", jxTraceCode);
					bankV2.set("cardid", userInfo.getStr("userCardId")) ;
					if(bankV2.save()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.TLIVE, "换绑卡成功，更新本地数据", "");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡成功，未更新本地数据", null);
						return ;
					}
				}
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "查询绑卡关系失败", null);
			}
			
		}else{
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TERROR, "换绑卡失败", null);
		}
		renderText("success");
	}
	
	/**
	 * 检查用户是否存有效存管银行卡
	 * @return
	 */
	@ActionKey("/isExistValidCard")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message isExistValidCard(){
		String userCode = getPara("userCode");
		User user = userService.findById(userCode);
		if(user== null){
			return error("01", "未加载用户信息", "");
		}
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			return error("02", "还未开通存管账户", "");
		}
		Map<String, Object> bindCard = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
		if(bindCard == null){
			return succ("00", "n");
		}
		@SuppressWarnings("unchecked")
		List<Map<String, String>> list = (List<Map<String, String>>)bindCard.get("subPacks");
		if(list==null || list.size() < 1){
			return succ("00", "n");
		}
		return succ("00", "y");
	}
	
	/**
	 * 同步自动投标设置
	 * */
	@ActionKey("/syncAutoBidSet")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message syncAutoBidSet(){
		String jxAccountId = getPara("jxAccountId");
		Map<String, String> map = JXQueryController.termsAuthQuery(jxAccountId);
		String retCode = map.get("retCode");
		if("00000000".equals(retCode)){
			String autoBid = map.get("autoBid");
			if("0".equals(autoBid)){
				return error("11", "并未开启自动投标签约", "");
			}
		}else{
			return error("12", "并未开启", "");
		}
		String orderId = map.get("orderId");
		User user = userService.findByJXAccountId(jxAccountId);
		String userCode = user.getStr("userCode");
		AutoLoan_v2 autoLoan_v2 = autoLoanService.findByUserCode(userCode);
		if(null != autoLoan_v2&&"A".equals(autoLoan_v2.getStr("autoState"))){
			return error("12", "自动投标本来就开启了", "");
		}
		List<JXTrace> jxTraces = jxTraceService.queryJxTrace("termsAuthPage", jxAccountId, "", null, null);
		if(null != jxTraces&&jxTraces.size()>0){
			JXTrace jxTrace = jxTraces.get(0);
			String userName = user.getStr("userName");
			String tempData = jxTrace.getStr("remark");
			JSONObject tempJson = JSONObject.parseObject(tempData);
			//获取设置数据
				//自动类型 autoType
				String autoType = tempJson.getString("autoType");
				//最大金额 onceMaxAmount
				long onceMaxAmount = tempJson.getLong("onceMaxAmount");
				//最小金额 onceMinAmount
				long onceMinAmount = tempJson.getLong("onceMinAmount");
				//最大期限 autoMaxLim
				int autoMaxLim = tempJson.getInteger("autoMaxLim");
				//最小期限 autoMinLim
				int autoMinLim = tempJson.getInteger("autoMinLim");
				//投资类型 refundType
				String refundType = tempJson.getString("refundType");
				//投资券类型 useTicket
				String useTicket = tempJson.getString("useTicket");
				//优先方式priorityMode
				String priorityMode = tempJson.getString("priorityMode");
				//过期时间deadline
				String deadline = tempJson.getString("deadLine");
			//---------
				int fff =1;
				try {
					fff = autoMapService.validateAutoState(userCode);
				} catch (Exception e) {
					fff = 1;
				}
				boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, onceMinAmount, onceMaxAmount, autoMinLim, autoMaxLim, refundType, "ABCD", priorityMode, useTicket, autoType,orderId,deadline);
				if(result){
					if(fff==0){
						if(result){
							String nowDate = DateUtil.getNowDate();
							int x = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161211" );
							int y = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161218" );
							if((x == 0 || x == 1) && (y == -1 || y == 0)){
								//11张30元券+11张50元券
								for (int i = 0; i < 11; i++) {
									ticketsService.saveADV(userCode, "30元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
									ticketsService.saveADV(userCode, "50元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
								}
							}
						}
					}
					String info="";
					info+="最小金额:"+onceMinAmount;
					info+="|最大金额:"+onceMaxAmount;
					info+="|最小期限:"+autoMinLim;
					info+="|最大期限:"+autoMaxLim;
					info+="|还款方式:"+refundType;
					info+="|自动投标类型:"+autoType;
					info+="|使用理财券类型:"+useTicket;
					info+="|理财券使用优先方式:"+priorityMode;
					info+="|过期时间:"+deadline;
					info+="|订单号:"+orderId;
					BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "后台手动保存自动投标配置成功",info);
					return succ("OK", "设置成功");
				}else{
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "后台手动保存自动投标配置失败;订单号："+orderId, null);
					return error("14", "设置失败", "");
				}
			
		}else{
			return error("15", "未找到设置流水", "");
		}
	}
}