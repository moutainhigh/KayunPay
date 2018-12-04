package com.dutiantech.controller.branch;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.interceptor.YistageInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNxjd;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNxjdService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jx.service.JXService;
import com.jx.util.SignUtil_lj;

public class YiStageController extends BaseController {

	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private BanksService banksService = getService(BanksService.class); 
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	private  FundsServiceV2 fundsServiceV2=getService(FundsServiceV2.class);
	private BanksService banksV2Service = getService(BanksService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private LoanNxjdService loanNxjdService = getService(LoanNxjdService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	/**
	 * 存管账户开户 非加密开户
	 */
	@AuthNum(value = 999)
	@Before({YistageInterceptor.class})
	public void openDepositAccount() {
		
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数

		if(!CommonUtil.jxPort){
			renderJson(error4json("666","江西银行对接中。。。。",""));
			return;
		}
//		String params = getRequestString4stream();//获取json参数
//		params = "{\"return_info\":\"请求错误\",\"return_data\":\"\",\"return_code\":\"666\",\"token\":\"\"}";
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");//手机号
		String email = jsonObject.getString("email");//邮箱
		String qq = jsonObject.getString("qq");
		String wechat = jsonObject.getString("wechat");
		String cardId = jsonObject.getString("cardId");//身份证号
		String notifyUrl = jsonObject.getString("retUrl");//后台通知链接
		String sex = jsonObject.getString("sex");//性别
		String age = jsonObject.getString("age");//年龄
		String address = jsonObject.getString("address");//地址
		String education = jsonObject.getString("education");//学历
		String workCity = jsonObject.getString("workCity");//工作城市
		String workType = jsonObject.getString("workType");//工作类型
//		String notify = jsonObject.getString("notifyUrl");
		String trueName = jsonObject.getString("trueName");//真实姓名
		String acctUse = "00000";//账户用途，00000-普通账户
		String identity = "2";//身份属性  1：出借角色  2：借款角色  3：代偿角色
	
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01", "请传入手机号", null));
			return;
		}
		if(StringUtil.isBlank(trueName)){
			renderJson(error4json("02", "真实姓名不能为空", null));
			return;
		}
		
		if(!CommonUtil.isMobile(mobile.trim())){
			renderJson(error4json("03", "手机号校验失败", null));
			return;
		}
		
//		String enMobile = mobile;
//		try {
//			enMobile = CommonUtil.encryptUserMobile(mobile);
//		} catch (Exception e) {
//			msg = error("05","手机号加密错误","");
//			renderJson(msg);
//			return;
//		}
		User user = userService.findByMobile(mobile);
		String userName = "";
		String userCode="";
		if(user == null){
			List<User> listUser = null;
			//创建一个userName
			do{
				userName = "yfq"+UIDUtil.generate().substring(0,6);
				//验证用户名是否被使用
				listUser = userService.find4userName(userName);
			} while (null != listUser && listUser.size()>0);
			//创建用户的userCode
			String regUserCode = UIDUtil.generate();
			userCode = regUserCode;
			try{
				String sysDesc = "易分期注册用户" + DateUtil.getNowDateTime();
				boolean b = userService.save(regUserCode,mobile,"00@yrhx.com","000000",userName,getRequestIP(),sysDesc);
				if(b==false){
					//记录日志
					BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册失败",null);
					renderJson(error4json("07", "注册第三方平台账户失败", ""));
					return;
				}else{
					//更新用户标识
					Boolean updateUserType = userService.updateUserType(regUserCode, "J");
					if(updateUserType == false){
						//记录日志
						BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册时更新用户标识错误",null);
						renderJson(error4json("07", "注册时更新用户标识错误", ""));
						return;
					}else{
						String encryptUserMobile = CommonUtil.encryptUserMobile(mobile);
						BIZ_LOG_INFO(encryptUserMobile, BIZ_TYPE.REGISTER, "注册用户成功");
					}
					
				}
			}catch(Exception e){
				//记录日志
				BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册失败",e);
				renderJson(error4json("08", "注册失败", ""));
				return;
			}
		}else{
			userCode = user.getStr("userCode");
			userName = user.getStr("userName");
			Integer countTrace4User = loanTraceService.countTrace4User(userCode);//查询该用户是否有在投的标
			if(countTrace4User>0){
				renderJson(error4json("08", "该用户有未结清的标的，请联系客服了解情况", ""));
				return;
			}
			Boolean type = userService.updateUserType(userCode, "J");
			if(!type){
				BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "更新用户标识失败",null);
				renderJson(error4json("08", "更新用户标识失败", ""));
				return;
			}
		}
		
		//查询易分期中的数据是否存在
		YiStageUserInfo yfq = yiStageUserInfoService.queryByUserCode(userCode);
		if(yfq == null){
			boolean save = yiStageUserInfoService.saveYfqUser(userCode, userName, mobile,sex,age,address,education,workCity,workType,qq,wechat,email);
			if(!save){
				renderJson(error4json("01","新增信息失败",""));
				return;
			}
		}

		String userCardName = null;
		String userCardId = null;
		UserInfo userInfo = null;
		
		userInfo = userInfoService.findById(userCode);
		
		String idType = "01";//证件类型
		if(userInfo == null||!"2".equals(userInfo.getStr("isAuthed"))){// 若未实名认证，则验证身份参数
			if(!IdCardUtils.validateCard(cardId)){
				renderJson(error4json("03","身份证号不正确！",""));
				return;
			}
			
			// 验证身份证是否已经被认证
			UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
			if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
				renderJson(error4json("04", "身份证号已被认证", ""));
				return;
			}
			
			userCardName = trueName;
			userCardId = cardId;
			
		} else{//若已实名
			try {
				userCardName = userInfo.getStr("userCardName");
				userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
				if(!trueName.equals(userCardName)||!cardId.equals(userCardId)){
					renderJson(error4json("04","此用户实名认证信息核实失败，请联系客服",""));
					return;
				}
			} catch (Exception e) {
				renderJson(error4json("05", "用户身份证号解析错误", ""));
				return;
			}
		}
		
		
		
		//  查询用户是否已开存管户-按证件号查询电子账号
		Map<String, String> accountIdQuery = null;
		try {
			// 根据证件号查询存管电子账号
			accountIdQuery = JXQueryController.accountIdQuery(idType, userCardId);
		} catch (Exception e) {
			renderJson(error4json("09", "存管系统异常", ""));
			return;
		}
		if (accountIdQuery != null && !StringUtil.isBlank(accountIdQuery.get("accountId"))) {
			renderJson(error4json("cg9999", "存管账户已存在", ""));//cg9999为存管账户已存在专用返回码
			return;
		}
		
		
		HttpServletResponse response  =  getResponse();
		// 根据身份证编号获取性别
		String gender = IdCardUtils.getGenderByIdCard(userCardId);
		String sexValue = gender;
		if(!StringUtil.isBlank(sex)){
			switch (sex) {
			case "男":
				sexValue = "M";
				break;
			case "女":
				sexValue = "F";
				break;
			default:
				break;
			}
		}
		//返回交易页面链接
		String retPageUrl = "openaccount";//yfq要跳转的交易页面
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String successfulUrl = "";
		String rnotifyUrl =CommonUtil.CALLBACK_URL+ "/yfq/openAccountCallbackForYfq?uCode=" + userCode +"&notify="+notifyUrl+"&mobile="+mobile;
//		JXappController jxappController = new JXappController();
		accountOpenPage(idType, cardId,trueName, sexValue, acctUse, mobile, identity, response, userCode, retUrl, successfulUrl, rnotifyUrl,traceCodeYfq);
		
//		renderText("success");
		
	}
	
	
	/**
	 * 存管账户开户 加密开户 暂时没用
	 */
	@AuthNum(value = 999)
	@Before({YistageInterceptor.class})
	public void openDepositAccountForYfq() {
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		if(!CommonUtil.jxPort){
			renderJson(error4json("666","江西银行对接中。。。。",""));
			return;
		}
//		String params = getRequestString4stream();//获取json参数
//		params = "{\"return_info\":\"请求错误\",\"return_data\":\"\",\"return_code\":\"666\",\"token\":\"\"}";
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");//手机号
		String cardId = jsonObject.getString("cardId");//身份证号
		String notifyUrl = jsonObject.getString("retUrl");//后台通知链接
		String sex = jsonObject.getString("sex");//性别
		String age = jsonObject.getString("age");//年龄
		String email = jsonObject.getString("email");//邮箱
		String qq = jsonObject.getString("qq");
		String wechat = jsonObject.getString("wechat");
		String address = jsonObject.getString("address");//地址
		String education = jsonObject.getString("education");//学历
		String workCity = jsonObject.getString("workCity");//工作城市
		String workType = jsonObject.getString("workType");//工作类型
//		String notify = jsonObject.getString("notifyUrl");
		String trueName = jsonObject.getString("trueName");//真实姓名
		String acctUse = "00000";//账户用途，00000-普通账户
		String identity = "2";//身份属性  1：出借角色  2：借款角色  3：代偿角色
	
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01", "请传入手机号", null));
			return;
		}
		if(StringUtil.isBlank(trueName)){
			renderJson(error4json("02", "真实姓名不能为空", null));
			return;
		}
		
		if(!CommonUtil.isMobile(mobile.trim())){
			renderJson(error4json("03", "手机号校验失败", null));
			return;
		}
		
//		String enMobile = mobile;
//		try {
//			enMobile = CommonUtil.encryptUserMobile(mobile);
//		} catch (Exception e) {
//			msg = error("05","手机号加密错误","");
//			renderJson(msg);
//			return;
//		}
		User user = userService.findByMobile(mobile);
		String userName = "";
		String userCode="";
		if(user == null){
			List<User> listUser = null;
			//创建一个userName
			do{
				userName = "yfq"+UIDUtil.generate().substring(0,6);
				//验证用户名是否被使用
				listUser = userService.find4userName(userName);
			} while (null != listUser && listUser.size()>0);
			//创建用户的userCode
			String regUserCode = UIDUtil.generate();
			userCode = regUserCode;
			try{
				String sysDesc = "易分期注册用户" + DateUtil.getNowDateTime();
				boolean b = userService.save(regUserCode,mobile,"00@yrhx.com","000000",userName,getRequestIP(),sysDesc);
				if(b==false){
					//记录日志
					BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册失败",null);
					renderJson(error4json("07", "注册第三方平台账户失败", ""));
					return;
				}else{
					//更新用户标识
					Boolean updateUserType = userService.updateUserType(regUserCode, "J");
					if(updateUserType == false){
						//记录日志
						BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册时更新用户标识错误",null);
						renderJson(error4json("07", "注册时更新用户标识错误", ""));
						return;
					}else{
						String encryptUserMobile = CommonUtil.encryptUserMobile(mobile);
						BIZ_LOG_INFO(encryptUserMobile, BIZ_TYPE.REGISTER, "注册用户成功");
					}
					
				}
			}catch(Exception e){
				//记录日志
				BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "注册失败",e);
				renderJson(error4json("08", "注册失败", ""));
				return;
			}
		}else{
			userCode = user.getStr("userCode");
			Integer countTrace4User = loanTraceService.countTrace4User(userCode);
			if(countTrace4User>0){
				renderJson(error4json("08", "该用户有未结清的标的，请联系客服了解情况", ""));
				return;
			}
			Boolean type = userService.updateUserType(userCode, "J");
			if(!type){
				BIZ_LOG_ERROR(mobile, BIZ_TYPE.REGISTER, "更新用户标识失败",null);
				renderJson(error4json("08", "更新用户标识失败", ""));
				return;
			}
		}
		
		//查询易分期中的数据是否存在
		YiStageUserInfo yfq = yiStageUserInfoService.queryByUserCode(userCode);
		if(yfq == null){
			boolean save = yiStageUserInfoService.saveYfqUser(userCode, userName, mobile,sex,age,address,education,workCity,workType,qq,wechat,email);
			if(!save){
				renderJson(error4json("01","新增信息失败",""));
				return;
			}
		}

		String userCardName = null;
		String userCardId = null;
		UserInfo userInfo = null;
		
		userInfo = userInfoService.findById(userCode);
		
		String idType = "01";//证件类型
		if(userInfo == null||!"2".equals(userInfo.getStr("isAuthed"))){// 若未实名认证，则验证身份参数
			if(!IdCardUtils.validateCard(cardId)){
				renderJson(error4json("03","身份证号不正确！",""));
				return;
			}
			
			// 验证身份证是否已经被认证
			UserInfo tmpUserInfo = userInfoService.findByCardId(cardId);
			if (tmpUserInfo != null && "2".equals(tmpUserInfo.getStr("isAuthed"))) {
				renderJson(error4json("04", "身份证号已被认证", ""));
				return;
			}
			
			userCardName = trueName;
			userCardId = cardId;
			
		} else{//若已实名
			try {
				userCardName = userInfo.getStr("userCardName");
				userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
				if(!trueName.equals(userCardName)||!cardId.equals(userCardId)){
					renderJson(error4json("04","此用户实名认证信息核实失败，请联系客服",""));
					return;
				}
			} catch (Exception e) {
				renderJson(error4json("05", "用户身份证号解析错误", ""));
				return;
			}
		}
		
		
		
		// 查询用户是否已开存管户-按证件号查询电子账号
		Map<String, String> accountIdQuery = null;
		try {
			// 根据证件号查询存管电子账号
			accountIdQuery = JXQueryController.accountIdQuery(idType, userCardId);
		} catch (Exception e) {
			renderJson(error4json("09", "存管系统异常", ""));
			return;
		}
		if (accountIdQuery != null && !StringUtil.isBlank(accountIdQuery.get("accountId"))) {
			renderJson(error4json("09", "存管账户已存在", ""));
			return;
		}
		
		
		HttpServletResponse response  =  getResponse();
		// 根据身份证编号获取性别
		String gender = IdCardUtils.getGenderByIdCard(userCardId);
		String sexValue = gender;
		if(!StringUtil.isBlank(sex)){
			switch (sex) {
			case "男":
				sexValue = "M";
				break;
			case "女":
				sexValue = "F";
				break;
			default:
				break;
			}
		}
		//返回交易页面链接
		String retPageUrl="openaccount";
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String successfulUrl = "";
		String rnotifyUrl =CommonUtil.CALLBACK_URL+ "/yfq/openAccountCallbackForYfq1?uCode=" + userCode +"&notify="+notifyUrl+"&mobile="+mobile;
		
		accountOpenEncryptPage(idType, userCardName, sexValue, acctUse, mobile, identity, response, userCode, retUrl, successfulUrl, rnotifyUrl,traceCodeYfq);
		
//		renderText("success");
		
	}
	
	/**
	 * 借款人还款代扣 WJW
	 */
	@AuthNum(value=999)
	@Before({YistageInterceptor.class,PkMsgInterceptor.class})
	public void repay() {
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		
//		String params = getRequestString4stream();
		
		//初始化map
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("status", "");
		map.put("orderNo", "");
		
		if(StringUtil.isBlank(params)){
			renderJson(error4json("YRT10031", "请求参数为空", map));
			return;
		}
		JSONObject parseObject = JSONObject.parseObject(params);
		
		String orderNo = parseObject.getString("orderNo");//订单号
		if(StringUtil.isBlank(orderNo)){
			renderJson(error4json("YRT10032", "订单号为空", map));
			return;
		}
		
		
		String cardNo = parseObject.getString("cardNo");//银行卡号
		if(StringUtil.isBlank(cardNo) || banksService.findByBankNo(cardNo) == null){
			renderJson(error4json("YRT10033", "银行卡号错误", map));
			return;
		}
		
		String amount = StringUtil.isBlank(parseObject.getString("amount"))?"0":parseObject.getString("amount");//代付金额(元)
		if(!Number.isNumber(amount)){
			renderJson(error4json("YRT10034", "金额格式错误", map));
			return;
		}
		
		String accName = parseObject.getString("accName");//持卡人姓名
		if(StringUtil.isBlank(accName)){
			renderJson(error4json("YRT10035", "持卡人姓名为空", map));
			return;
		}
		
		Map<String, String> t1001 = null;
		try{
			t1001 = JXController.T1001(orderNo,cardNo, amount, accName);//代扣
			map.put("status", t1001.get("orderStatus"));
			map.put("orderNo", t1001.get("orderNo"));
			renderJson(error4json(t1001.get("retCode"), t1001.get("retDesc"), map));
			return;
		} catch (Exception e) {//代扣异常,主动查询
			Map<String, String> Q9001 = null;
			try {
				Q9001 = JXQueryController.Q9001(orderNo);
				String orderDetail = Q9001.get("orderDetail");
				JSONObject orderDetailObject = JSONObject.parseObject(orderDetail);
				map.put("status", orderDetailObject.getString("orderStatus"));
				map.put("orderNo", Q9001.get("orderNo"));
				renderJson(error4json(orderDetailObject.getString("retCode"), orderDetailObject.getString("retDesc"), map));
				return;
			} catch (Exception e2) {//查询通道异常
				renderJson(error4json("YRT10036", "银行查询通道异常", map));
				return;
			}
		}
	}
	
	/**
	 * 借款人还款代扣查询 WJW
	 * @return
	 */
	@AuthNum(value=999)
	@Before({YistageInterceptor.class,PkMsgInterceptor.class})
	public void repayQuery(){
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		if(StringUtil.isBlank(params)){
			renderJson(error4json("Q10031", "请求参数为空", null));
			return;
		}
		
		//初始化map
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("status", "");
		map.put("orderNo", "");
		
		JSONObject js = JSONObject.parseObject(params);
		
		String orderNo = js.getString("orderNo");
		if(StringUtil.isBlank(orderNo)){
			renderJson(error4json("YRQ90012", "订单号为空", map));
			return;
		}
		
		Map<String, String> Q9001 = null;
		try {
			Q9001 = JXQueryController.Q9001(orderNo);
			if(!"0000".equals(Q9001.get("retCode"))){
				map.put("orderNo", orderNo);
				renderJson(error4json(Q9001.get("retCode"), Q9001.get("retDesc"), map));
				return;
			}
			String orderDetail = Q9001.get("orderDetail");
			JSONObject orderDetailObject = JSONObject.parseObject(orderDetail);
			map.put("status", orderDetailObject.getString("orderStatus"));
			map.put("orderNo", Q9001.get("orderNo"));
			renderJson(error4json(orderDetailObject.getString("retCode"), orderDetailObject.getString("retDesc"), map));
			return;
		} catch (Exception e2) {//查询通道异常
			renderJson(error4json("YRQ90013", "银行查询通道异常", map));
			return;
		}
	}
	
	/**
	 * 查询标状态 WJW
	 */
	@AuthNum(value=999)
	@Before({YistageInterceptor.class,PkMsgInterceptor.class})
	public void loanStateQuery(){
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		
		if(StringUtil.isBlank(params)){
			renderJson(error4json("B10031", "请求参数为空", null));
			return;
		}
		
		//初始化map
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("status", "");
		map.put("orderNo", "");
		JSONObject js = JSONObject.parseObject(params);
		String ztyLoanCode = js.getString("loanNumber");//标号
		if(StringUtil.isBlank(ztyLoanCode)){
			renderJson(error4json("YRQLoan2", "标号为空", map));
			return;
		}
		LoanNxjd loanNxjd = loanNxjdService.findLastByZtyLoanCode(ztyLoanCode);
		if(loanNxjd == null){
			renderJson(error4json("YRQLoan3", "标记录不存在", map));
			return;
		}
		LoanInfo loan = loanInfoService.findById(loanNxjd.getStr("loanCode"));
		if(loan == null){
			renderJson(error4json("YRQLoan4", "标不存在", map));
			return;
		}
		String loanState = loan.getStr("loanState");
		String[] loanStates = {"H","J","N","O","P"};
		Arrays.sort(loanStates);
		if(Arrays.binarySearch(loanStates, loanState) == -1){
			loanState = "Z";//其他
		}
		map.put("loanState", loanState);
		renderJson(succ4json("查询成功", map));
	}
	
	
	/**
	 * 开通缴费授权
	 */
	@AuthNum(value=999)
	@Before({YistageInterceptor.class})
	public void openTermsAuth(){
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
//		String params = getRequestString4stream();
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");
//		String maxAmt = jsonObject.getString("maxAmt");//签约最大金额 单位：元
//		String deadline = jsonObject.getString("deadline");//签约到期日
//		String retUrl = jsonObject.getString("retUrl");//返回的交易页面
		String notifyUrl = jsonObject.getString("retUrl");//接收响应的地址
		String accountId = jsonObject.getString("accountId");//存管账户
//		String accountId = getPara("accountId","");
//		String notifyUrl = getPara("notifyUrl","");
		if(StringUtil.isBlank(accountId)){
			renderJson(error4json("01","存管电子账户不能为空",""));
			return;
		}
//		if(StringUtil.isBlank(maxAmt)){
//			maxAmt = "20000";
//		}
		
//		if(StringUtil.isBlank(deadline)){
//			deadline = DateUtil.updateDate(new Date(),5,Calendar.YEAR,"yyyyMMdd");//签约到期日
//		}
		User user = userService.findByJXAccountId(accountId);
		if(user == null){
			renderJson(error4json("01","用户不存在或没开通存管账户",""));
			return;
		}
		//返回交易页面链接
		String retPageUrl="openaccount";
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		
//		String retUrl = "http://www.baidu.com";
		String retNotifyUrl = CommonUtil.CALLBACK_URL+ "/yfq/openTermsAuthCallback?notify="+notifyUrl+"&mobile="+mobile;
		openTermsAuth(accountId,"50000",DateUtil.updateDate(new Date(),5,Calendar.YEAR,"yyyyMMdd"),getResponse(),retNotifyUrl,retUrl,traceCodeYfq);
	}
	
	/**
	 * 易分期缴费授权(交易金额单位:元)(页面调用)
	 * 
	 * @param accountId
	 *            电子账号
	 * @param maxAmt
	 *            签约最大金额
	 * @param deadline
	 *            签约到期日
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public static void openTermsAuth(String accountId, String maxAmt, String deadline, HttpServletResponse response,String notifyUrl,String retUrl,String trace) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));
		reqMap.put("txCode", "paymentAuthPage");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("maxAmt", maxAmt);// 签约最大金额
		reqMap.put("deadline", deadline);// 签约到期日
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("successfulUrl", "");
		reqMap.put("notifyUrl",notifyUrl);// 后台响应链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "paymentAuthPage", reqMap, response);
		} catch (Exception e) {
		}
	}
	/**
	 * 20180919
	 * 多合一合规授权
	 * 易分期调用多合一合规授权接口
	 */
	@AuthNum(value=999)
	@Before({YistageInterceptor.class})
	public void termsAuthPage(){
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
//		String params = getRequestString4stream();
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");
		String forgotPwdUrl = "app://lyfq/setting";// 忘记密码链接
//		String maxAmt = jsonObject.getString("maxAmt");//签约最大金额 单位：元
//		String deadline = jsonObject.getString("deadline");//签约到期日
//		String retUrl = jsonObject.getString("retUrl");//返回的交易页面
		String notifyUrl = jsonObject.getString("retUrl");//接收响应的地址
		String accountId = jsonObject.getString("accountId");//存管账户
//		String accountId = getPara("accountId","");
//		String notifyUrl = getPara("notifyUrl","");
		if(StringUtil.isBlank(accountId)){
			renderJson(error4json("01","存管电子账户不能为空",""));
			return;
		}
//		if(StringUtil.isBlank(maxAmt)){
//			maxAmt = "20000";
//		}
		
//		if(StringUtil.isBlank(deadline)){
//			deadline = DateUtil.updateDate(new Date(),5,Calendar.YEAR,"yyyyMMdd");//签约到期日
//		}
		User user = userService.findByJXAccountId(accountId);
		if(user == null){
			renderJson(error4json("01","用户不存在或没开通存管账户",""));
			return;
		}
		String userCode = user.getStr("userCode");
		UserInfo userInfo = userInfoService.findById(userCode);
		if(null == userInfo){
			renderJson(error4json("02","用户认证信息异常",""));
			return;
		}
		String name = userInfo.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
		} catch (Exception e) {
			renderJson(error4json("3","身份照号解密异常",""));
			return;
		}
		
		//返回交易页面链接
		String retPageUrl="openaccount";
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String retNotifyUrl = CommonUtil.CALLBACK_URL+ "/yfq/newOpenTermsAuthCallback?notify="+notifyUrl+"&mobile="+mobile;
		//调用多合一合规授权接口 20180919
		termsAuthPage(accountId, name, idNo, "2", "", "", "1", "", "", "", "", "", "50000", DateUtil.updateDate(new Date(),5,Calendar.YEAR,"yyyyMMdd"), "", "", forgotPwdUrl, retUrl, retNotifyUrl, getResponse(),traceCodeYfq);
	}
	/**
	 * 多合一合规授权接口
	 * 20180919 
	 * @param accountId 电子账号
	 * @param name  姓名
	 * @param idNo  身份证号
	 * @param identity  身份属性角色  1：出借角色  2：借款角色   3：代偿角色
	 * @param autoBid   开通自动投标功能标志  0：取消 1：开通 空：不操作
	 * @param autoCredit 开通自动债转功能标志   0：取消 1：开通 空：不操作
	 * @param paymentAuth 开通缴费授权功能标志 0：取消 1：开通 空：不操作
	 * @param repayAuth  开通还款授权功能标识  0：取消 1：开通 空：不操作
	 * @param autoBidMaxAmt 自动投标签约最高金额  签约时必送以元为单位，最多两位小数
	 * @param autoBidDeadline 自动投标签约到期日  签约时必送 YYYYmmdd
	 * @param autoCreditMaxAmt 自动购买债权签约最高金额
	 * @param autoCreditDeadline 自动购买债权签约到期日
	 * @param paymentMaxAmt  缴费授权签约最高金额
	 * @param paymentDeadline 缴费授权签约到期日
	 * @param repayMaxAmt   还款授权签约最高金额
	 * @param repayDeadline  还款授权签约到期日
	 * @param forgotPwdUrl  忘记密码跳转链接
	 * @param retUrl      返回交易页面链接
	 * @param notifyUrl  后台响应链接
	 * @param response
	 */
	public static void termsAuthPage(String accountId,String name,String idNo,String identity,String autoBid,String autoCredit,String paymentAuth,String repayAuth,String autoBidMaxAmt,String autoBidDeadline,String autoCreditMaxAmt,String autoCreditDeadline,String paymentMaxAmt,String paymentDeadline,String repayMaxAmt,String repayDeadline,String forgotPwdUrl,String retUrl,String notifyUrl,HttpServletResponse response,String traceCode){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txDate",traceCode.substring(0, 8));
		reqMap.put("txTime",traceCode.substring(8, 14));
		reqMap.put("seqNo",traceCode.substring(14,20));
		reqMap.put("txCode", "termsAuthPage");
		reqMap.put("accountId", accountId);
		reqMap.put("name", name);
		reqMap.put("idNo", idNo);
		reqMap.put("identity", identity);
		if(!StringUtil.isBlank(autoBid)){
			reqMap.put("autoBid", autoBid);
			reqMap.put("autoBidMaxAmt", autoBidMaxAmt);
			reqMap.put("autoBidDeadline", autoBidDeadline);
		}
		if(!StringUtil.isBlank(autoCredit)){
			reqMap.put("autoCredit", autoCredit);
			reqMap.put("autoCreditMaxAmt", autoCreditMaxAmt);
			reqMap.put("autoCreditDeadline", autoCreditDeadline);
		}
		if(!StringUtil.isBlank(paymentAuth)){
			reqMap.put("paymentAuth", paymentAuth);
			reqMap.put("paymentMaxAmt", paymentMaxAmt);
			reqMap.put("paymentDeadline", paymentDeadline);
		}
		if(!StringUtil.isBlank(repayAuth)){
			reqMap.put("repayAuth", repayAuth);
			reqMap.put("repayMaxAmt", repayMaxAmt);
			reqMap.put("repayDeadline", repayDeadline);
		}
		reqMap.put("forgotPwdUrl", forgotPwdUrl);
		reqMap.put("retUrl", retUrl);
		reqMap.put("notifyUrl", notifyUrl);
		//生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		//生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);
		JXService.formSubmit(JXService.PAGE_URI + "termsAuthPage", reqMap, response);
	}
	/*
	 * get发送请求，参数以拼接参数形式传送
	 */
	public void sentMsgToLyfq(String url,Map<String,String> reMap){
		StringBuffer buff = new StringBuffer("");
		Iterator<String> iterator = reMap.keySet().iterator();
		while(iterator.hasNext()){
			String key = iterator.next();
			buff.append(key + "=" + reMap.get(key) + "&");
		}
		String params = buff.toString();
		HttpRequestUtil.sendGet(url, params);
		
	}
	

	/**
	 * 加密开户（跳转至存管开户页面）
	 * 
	 * @param idType
	 *            证件类型 01-身份证18位
	 
	 * @param name
	 *            姓名
	 * @param gender
	 *            性别 M:男性 F:女性
	 * @param acctUse
	 *            账户用途(00000-普通账户,10000-红包账户（只能有一个）,01000-手续费账户（只能有一个）,00100-
	 *            担保账户)
	 * @param mobile
	 *            手机号
	 * @param identity
	 *            身份属性 1:出借角色 2:借款角色 3:代偿角色
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public Message accountOpenEncryptPage(String idType, String name, String gender, String acctUse,
			String mobile, String identity, HttpServletResponse response, String userCode, String retUrl, String successfulUrl, String notifyUrl,String trace) {
		
		if (mobile == null || mobile.length() != 11) {
			return error("03", "手机号不是11位", null);
		}

		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);// 添加通用参数

		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));
		
		reqMap.put("txCode", "accountOpenEncryptPage");
		reqMap.put("idType", idType);// 证件类型 01-身份证18位
		reqMap.put("name", name);// 姓名
		reqMap.put("gender", gender);// 性别
		reqMap.put("mobile", mobile);// 手机号
		reqMap.put("acctUse", acctUse);// 00000普通账户
		reqMap.put("identity", identity);// 身份属性
		reqMap.put("coinstName", "易融恒信金融信息服务有限公司");// 平台名称
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("successfulUrl", successfulUrl);//交易成功跳转链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "accountOpenEncryptPage", reqMap, response);
		} catch (Exception e) {
		}
		return succ("信息提交成功", "");
	}
	
	/**
	 * 密码修改  1.3.5版本
	 */
	@AuthNum(value = 999)
	@Before({YistageInterceptor.class})
	public void passwordUpdate(){
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		
//		String params = getRequestString4stream();//获取json参数
		if(StringUtil.isBlank(params)){
			renderJson(error4json("666", "请求参数错误", ""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");//获取手机号
		String retNotifyUrl = jsonObject.getString("retNotifyUrl");//通知地址
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01", "手机号不能为空", ""));
			return;
		}
		
		if(StringUtil.isBlank(retNotifyUrl)){
			renderJson(error4json("01", "业务通知地址不能为空", ""));
			return;
		}
		
		User user = userService.findByMobile(mobile);
		if(user == null ){
			renderJson(error4json("01","用户信息有误",""));
			return;
		}
		String userCode = user.getStr("userCode");
		String name = user.getStr("userCardName");//姓名
		String jxAccountId = user.getStr("jxAccountId");//存管账户
		
		if (null == jxAccountId || "".equals(jxAccountId)) {
			renderJson(error4json("02", "请先开通存管账户再设置存管密码", ""));
			return;
		}
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if (pwdMap != null && "0".equals(pwdMap.get("pinFlag"))) {
			renderJson(error4json("02", "请先设置存管账户交易密码", ""));
			return;
		}
		
		HttpServletResponse response  =  getResponse();
		String retPageUrl  =  "setting";
		//返回交易页面链接
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String notifyUrl =CommonUtil.CALLBACK_URL+ "/yfq/passwordSetCallback?userCode="+userCode+"&mobile="+mobile+"&notify="+retNotifyUrl;
		passwordUpdate(jxAccountId, name,response, retUrl, notifyUrl,traceCodeYfq);
		
	}
	
	
	/**
	 * 密码修改  1.3.5  
	 * @param jxAccountId
	 * @param name
	 * @param response
	 * @param retUrl
	 * @param notifyUrl
	 * @return
	 */
	public  Message passwordUpdate(String jxAccountId,String name,HttpServletResponse response,String retUrl,String notifyUrl,String trace){
		
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);// 添加通用参数
		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));

		reqMap.put("txCode", "passwordUpdate");
		reqMap.put("name", name);// 姓名
		reqMap.put("accountId", jxAccountId);//电子账号
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "passwordUpdate", reqMap, response);
		} catch (Exception e) {
		}
		return succ("信息提交成功", "");
	}
	
	
	
	/**
	 * 密码设置，必须是首次设置密码   此接口在1.3.5版本中停用
	 * 没有使用
	 */
	@AuthNum(value = 999)
	public void passwordSet(){
		String params = getRequestString4stream();//获取json参数
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		
		String mobile = jsonObject.getString("mobile");//获取手机号
		String retNotifyUrl = jsonObject.getString("retNotifyUrl");//业务通知结果
		
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01","手机号不能为空",""));
			return;
		}
		if(StringUtil.isBlank(retNotifyUrl)){
			renderJson(error4json("01","业务通知地址不能为空",""));
			return;
		}
		
		User user = userService.findByMobile(mobile);
		if(user == null){
			renderJson(error4json("01","用户信息有误",""));
			return;
		}
		String userCode = user.getStr("userCode");
		UserInfo userInfo = userInfoService.findById(userCode);
//		BanksV2 banks = banksService.findByUserCode(userCode);
		String name = user.getStr("userCardName");//姓名
		String userCardId = userInfo.getStr("userCardId");//身份证号
		String jxAccountId = user.getStr("jxAccountId");//存管账户
		String idType = userInfo.getStr("idType");//证件类型
		
		if (null == jxAccountId || "".equals(jxAccountId)) {
			renderJson(error4json("02", "请先开通存管账户再设置存管密码", ""));
			return;
		}
		if (StringUtil.isBlank(idType)) {
			renderJson(error4json("03", "未查找到用户有证件类型", ""));
			return;
		}
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userCardId);
		} catch (Exception e) {
			renderJson(error4json("03", "证件号解析异常", ""));
			return;
		}
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if (pwdMap != null && "1".equals(pwdMap.get("pinFlag"))) {
			renderJson(error4json("02", "已经设置过存管账户交易密码", ""));
			return;
		}
		
		HttpServletResponse response  =  getResponse();
		String successfulUrl = "";
		//返回交易页面链接
		String retPageUrl  =  "setting";
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String notifyUrl =CommonUtil.CALLBACK_URL+ "/yfq/passwordSetCallback?userCode="+userCode+"&mobile="+mobile+"&notify="+retNotifyUrl;
		JXappController.passwordset(jxAccountId, idType, idNo, name, mobile, successfulUrl, response, retUrl, notifyUrl);
	}
	
	
	
	/**
	 * 密码重置(1.3.4合规要求)
	 */
	@AuthNum(value = 999)
	@Before({YistageInterceptor.class})
	public void passwordResetPage(){
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		if(!CommonUtil.jxPort){
			renderJson(error4json("666", "江西银行对接中……", ""));
			return ;
		}
//		String params = getRequestString4stream();//获取json参数
		if (StringUtil.isBlank(params)) {
			renderJson(error4json("666","请求参数错误",""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
//		String smsCode = jsonObject.getString("smsCode");//短信获取
		
		String mobile = jsonObject.getString("mobile");//获取手机号
		String retNotifyUrl = jsonObject.getString("retNotifyUrl");//业务通知结果
		String url = jsonObject.getString("url");
		
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01","手机号不能为空",""));
			return;
		}
		if(StringUtil.isBlank(retNotifyUrl)){
			renderJson(error4json("01","业务通知地址不能为空",""));
			return;
		}
		
		User user = userService.findByMobile(mobile);
		if(user == null){
			renderJson(error4json("01","用户信息有误",""));
			return;
		}
		
		/*//前导业务授权码
		String lastSrvAuthCode = (String)Memcached.get("SMS_MSG_RESETPWD_"+user.getStr("userCode"));
		if(StringUtil.isBlank(smsCode) || StringUtil.isBlank(lastSrvAuthCode)){
			msg = error("01", "重置密码_短信验证信息不全", "");
			renderJson(msg);
			return ;
		}
		*/
		String userCode = user.getStr("userCode");
		UserInfo userInfo = userInfoService.findById(userCode);
//		BanksV2 banks = banksService.findByUserCode(userCode);
		String name = user.getStr("userCardName");//姓名
		String userCardId = userInfo.getStr("userCardId");//身份证号
		String jxAccountId = user.getStr("jxAccountId");//存管账户
		String idType = userInfo.getStr("idType");//证件类型
//		String idType = banks.getStr("idType");//证件类型
		
		if (null == jxAccountId || "".equals(jxAccountId)) {
			renderJson(error4json("02", "请先开通存管账户", ""));
			return;
		}
		if (StringUtil.isBlank(idType)) {
			renderJson(error4json("03", "未查找到用户有证件类型", ""));
			return;
		}
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userCardId);
		} catch (Exception e) {
			renderJson(error4json("03", "证件号解析异常", ""));
			return;
		}
		
		//1.3.5版接口中首次设置和重置密码改为同一个接口，所以没必要去查询是否设置过交易密码
		/*Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if(pwdMap == null || "0".equals(pwdMap.get("pinFlag"))){
			renderJson(error4json("02", "未设置过电子账户密码", ""));
			return ;
		}*/
		//返回交易页面链接
		
		String retPageUrl =  StringUtil.isBlank(url)? "setting" : url;
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		//后台通知链接
		String notifyUrl =CommonUtil.CALLBACK_URL+ "/yfq/repasswordSetCallback?userCode="+userCode+"&mobile="+mobile+"&notify="+retNotifyUrl;
		HttpServletResponse response  =  getResponse();
		passwordResetPage(jxAccountId, idType, idNo, name, mobile, retUrl, response, notifyUrl,traceCodeYfq);
	
		renderNull();
	}
	
	
	
	/**
	 * 短信接口
	 * 停用
	 * @return
	 */
	@AuthNum(value = 999)
	public void commonSmsCodeForyfq(){
		Message msg = new Message();
	if(!CommonUtil.jxPort){
		renderJson(error4json("666", "江西银行对接中……", ""));
		return;
	}
	String params = getRequestString4stream();//获取json参数
	if (StringUtil.isBlank(params)) {
		renderJson(error4json("666","请求参数错误",""));
		return;
	}
	JSONObject jsonObject = JSONObject.parseObject(params);
	
	String mobile = jsonObject.getString("mobile");//获取旧手机号
	String newMobile = jsonObject.getString("newMobile");//获取旧手机号
	//对应业务交易代码
	String type = jsonObject.getString("type");
	
	//请求类型：1-即信发短信    2-银行发短信   不填默认为1
	String reqType = jsonObject.getString("reqType");
	if(StringUtil.isBlank(reqType)){
		reqType = "1";
	}
	if(StringUtil.isBlank(type)){
		type="1";
	}
	
	if(StringUtil.isBlank(type)){
		renderJson(error4json("03", "未收到短信业务交易代码", ""));
		return ;
	}
	
	if(StringUtil.isBlank(mobile)){
		renderJson(error4json("01","手机号不能为空",""));
		return ;
	}
	
	User user = userService.find4mobile(mobile);
	if(user== null){
		renderJson(error4json("01","用户信息核查失败",""));
		return ;
	}
	String userCode = user.getStr("userCode");
	if("0".equals(type)){//修改存管手机号时短信发送至新手机号，需要前台传
		
		if(StringUtil.isBlank(newMobile)){
			renderJson(error4json("03", "手机号不能为空", ""));
			return ;
		}
	}
	String cardNo = "";//reqType为2时cardNo必填
	if("2".equals(reqType)){
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		
		if(banksV2 == null){
			renderJson(error4json("03", "请完善银行卡信息", ""));
			return ;
		}
		cardNo = banksV2.getStr("bankNo");
		if(cardNo == null || "".equals(cardNo)){
			renderJson(error4json("03", "请完善银行卡信息", ""));
			return ;
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

	default :
		msg = error("05", "短信业务交易代码与操作不符", "");
		renderJson(msg);
		return ;
	}
	String smsType = "";//验证码类型(暂时不用，可选)
	//返回数据
	Map<String, String> resultMap = new HashMap<>();
	//调用短信接口
	Map<String, String> resMap = JXappController.smsCodeApply(mobile, reqType, srvTxCode, cardNo, smsType );
	
	if("00000000".equals(resMap.get("retCode"))){
		resultMap.put("mobile", resMap.get("mobile"));
		//业务交易代码
		resultMap.put("srvTxCode", resMap.get("srvTxCode"));
		//业务授权码 存入缓存中
		String srvAuthCode = resMap.get("srvAuthCode");
		
		Memcached.set(memcachedKey+ userCode, srvAuthCode, 10*60*1000);
		renderJson(succ4json("短信发送成功", resMap.get("smsSeq")));
		return ;
	}else{
		renderJson(error4json("10", "短信发送失败,"+resMap.get("retCode")+":"+resMap.get("retMsg"), ""));
		return ;
	}
	
}
	
	/**
	 * 存管余额查询
	 */
	@Before({YistageInterceptor.class})
	public void depositAmountQuery(){
//		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String para = (String)getRequest().getAttribute("params");//获取请求接口的参数
		
		
//		String para = getRequestString4stream();
		JSONObject parseObject = JSONObject.parseObject(para);
		String mobile = parseObject.getString("mobile");
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01","手机号不能为空",""));
			return;
		}
		
		User user = userService.findByMobile(mobile);
		if(user == null){
			renderJson(error4json("01", "查无此用户", ""));
			return;
		}
		Map<String,String> map = new HashMap<>();
		String accountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(accountId)){
			renderJson(error4json("01", "此用户尚未开通江西存管", ""));
			return;
		}
		Map<String,String> balanceQuery = JXQueryController.balanceQuery(accountId);
		if("00000000".equals(balanceQuery.get("retCode"))){
			map.put("availBal",balanceQuery.get("availBal"));
			map.put("currBal",balanceQuery.get("currBal"));
			map.put("accountId",accountId);
			renderJson(succ4json("查询存管余额成功", map));
			return;
		}else{
			map.put("availBal","0");
			map.put("currBal","0");
			map.put("accountId",accountId);
			renderJson(error4json("01",balanceQuery.get("retMsg"), map));
			return;
			
		}
		
	}
	
	/**
	 * 存管信息查询接口
	 */
	@Before({YistageInterceptor.class})
	public void depositInfoQuery() {
		
		String para = (String)getRequest().getAttribute("params");//获取请求接口的参数
	
//		String para = getRequestString4stream();
		JSONObject parseObject = JSONObject.parseObject(para);
		String mobile = parseObject.getString("mobile");//手机号
		Map<String, String> map = new HashMap<String, String>();
		//String mobile = getPara("mobile");//手机号
		User user = userService.findByMobile(mobile);
		String accountId ="";
		String termsAuth ="";//缴费授权签约状态
		String termsAuthDeadline = "";//缴费授权签约到期时间
		String status="0";//用户状态  0 新用户 1 出借角色 2 借款角色 3 代偿角色 4平台角色 5 已注册用户但未开存管
		String cardNo = "";
		String bankName="";
		String name = "";
		String maxAmt = "";
		String txnDateTime = "";
		String openStatus = "0";//开户状态 0 未开户 1 开户成功 2开户成功,密码未设置
		if(StringUtil.isBlank(mobile)){
			renderJson(error4json("01","手机号不能为空",""));
			return;
		}
		if(null!=user){
			//已开通存管
			accountId = user.getStr("jxAccountId");
			
			if(!StringUtil.isBlank(accountId)){
				
				Map<String, String> passwordSetQuery = JXQueryController.passwordSetQuery(accountId);
				if(passwordSetQuery != null && "00000000".equals(passwordSetQuery.get("retCode"))){
					if("1".equals(passwordSetQuery.get("pinFlag"))){
						openStatus = "1";
					}else{
						openStatus = "2";
					}
				}else{
					openStatus = "2";
				}
				
				//根据手机号查询电子账号信息
				Map<String, String> accountQueryByMobile = JXQueryController.accountQueryByMobile(mobile);
				if(JXController.isRespSuc(accountQueryByMobile) ){
					status = accountQueryByMobile.get("identity");	
					name = accountQueryByMobile.get("name");
				}
				//查询用户的授权信息
				Map<String,String> termsAuthQuery = JXQueryController.termsAuthQuery(accountId);
				if(JXController.isRespSuc(termsAuthQuery)){
					termsAuth=termsAuthQuery.get("paymentAuth");
					termsAuthDeadline=termsAuthQuery.get("paymentDeadline");	
					maxAmt = termsAuthQuery.get("paymentMaxAmt");
				}
				
				// 根据存管电子账号查询绑查关系(只查有效卡)
				Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
				if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

					List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
					if (list != null && list.size() > 0) {

						Map<String, String> cardMap = list.get(0);
						cardNo = cardMap.get("cardNo");// 有效存管卡号
						
							//查询所属银行
							String nameOfBank = BankUtil.getNameOfBank(cardNo);
							if(!StringUtil.isBlank(nameOfBank)){
								bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
							}
					}
				}
		
			}	
			else{
				openStatus = "0";
				status = "5";
				
			}
		}
		map.put("mobile", mobile);
		map.put("accountId", accountId);
		map.put("termsAuth", termsAuth);
		map.put("termsAuthDeadline", termsAuthDeadline);
		map.put("status", status);
		map.put("bankNo",cardNo);
		map.put("bankName",bankName);
		map.put("name",name);
		map.put("maxAmt",maxAmt);
		map.put("txnDateTime",txnDateTime);
		map.put("openStatus",openStatus);
		renderJson(succ4json("查询存管信息成功", map));
		return;
	}
	
	/**
	 * 重置密码（1.3.4合规要求）
	 * @param accountId       电子账户
	 * @param idType          证件类型
	 * @param idNo            证件号码
	 * @param name            姓名
	 * @param mobile          手机号
	 * @param retUrl          跳转链接
	 * @param response
	 * @param notifyUrl       接收参数链接
	 * 
	 */
	
	public Message passwordResetPage(String accountId, String idType, String idNo, String name,
			String mobile, String retUrl, HttpServletResponse response,String notifyUrl,String trace) {
	
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);// 添加通用参数
		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));

		reqMap.put("txCode", "passwordResetPage");
		reqMap.put("idType", idType);// 证件类型 01-身份证18位
		reqMap.put("name", name);// 姓名
		reqMap.put("accountId", accountId);// 电子账户
		reqMap.put("mobile", mobile);// 手机号
		reqMap.put("idNo", idNo);// 证件号码
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "passwordResetPage", reqMap, response);
		} catch (Exception e) {
		}
		return succ("信息提交成功", "");
	}
	/**
	 * 易分期用户提现
	 */
	@Before({YistageInterceptor.class})
	public void withdraw() {
		String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
		String params = (String)getRequest().getAttribute("params");//获取请求接口的参数

		Message msg = new Message();
//		String params = getRequestString4stream();// 获取json参数
		if(StringUtil.isBlank(params)){
			renderJson(error4json("01", "请求参数不能为空", ""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String mobile = jsonObject.getString("mobile");// 手机号
		String withdrawCode = jsonObject.getString("withdrawCode");// 易分期订单号-提现流水号
		String notifyUrl = jsonObject.getString("notifyUrl");// 后台通知链接
		String txAmount = jsonObject.getString("txAmount");// 交易金额
		String txFee = jsonObject.getString("txFee");// 手续费
		String routeCode = jsonObject.getString("routeCode");// 路由代码
		String forgotPwdUrl = jsonObject.getString("forgotPwdUrl");// 忘记密码链接
		String businessAccountIdFlag = jsonObject.getString("businessAccountIdFlag");// 账户提现标识
		String cardBankCnaps = jsonObject.getString("cardBankCnaps");// 联行号		
		if (StringUtil.isBlank(businessAccountIdFlag)) {
			businessAccountIdFlag = "N";
		}
		if (StringUtil.isBlank(mobile)) {
			renderJson(error4json("01", "手机号不能为空", ""));
			return;
		}
		
		if (!CommonUtil.isMobile(mobile.trim())) {
			renderJson(error4json("02", "手机号校验失败", ""));
			return;
		}
		User user = userService.findByMobile(mobile);
		YiStageUserInfo yiStageUserInfo=yiStageUserInfoService.findYiStageUserInfo(mobile);
		if (null == user || null ==yiStageUserInfo) {
			renderJson(error4json("04", "无此用户", ""));
			return;
		}
		if (StringUtil.isBlank(withdrawCode)) {
			renderJson(error4json("05", "提现订单号不能为空", ""));
			return;
		}
		if (StringUtil.isBlank(txAmount)) {
			renderJson(error4json("06", "提现金额不能为空", ""));
			return;
		}
		if (StringUtil.isBlank(txFee)) {
			renderJson(error4json("07", "提现手续费不能为空", ""));
			return;
		}
		if (StringUtil.isBlank(forgotPwdUrl)) {
			renderJson(error4json("08", "忘记密码的链接地址不能为空", ""));
			return;
		}
		if ("2".equals(routeCode)) { // 提现通道验证
			if (StringUtil.isBlank(cardBankCnaps)) {
				renderJson(error4json("09", "大额提现_联行号不能为空", ""));
				return;
			}
		}
		String userCode = user.getStr("userCode");
		String jxAccountId = user.getStr("jxAccountId");
		if (StringUtil.isBlank(jxAccountId)) {
			renderJson(error4json("10", "用户还未激活存管账户", ""));
			return;
		}
		UserInfo userInfo = userInfoService.findById(userCode); // 用户信息
		BanksV2 banksV2 = banksV2Service.findByUserCode(userCode); // 获取银行卡信息
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		if (banksV2 == null) { // 绑卡验证
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，未查找到理财卡信息");
			renderJson(error4json("11", "未查找到理财卡信息", ""));
			return;
		}
		if (StringUtil.isBlank(banksV2.getStr("bankNo"))) {
			renderJson(error4json("12", "无银行卡号", ""));
			return;
		}
		// 参数解析
		String userCardId = null;
		String userMobile = null;
		try {
			userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId")); // 解析用户身份证号
			userMobile = CommonUtil.decryptUserMobile(banksV2.getStr("mobile")); // 解析存管预留手机号
		} catch (Exception e) {
			renderJson(error4json("13", "身份证或手机号解析异常", ""));
			return;
		}
		// 验证缴费授权
		Map<String, String> authDetail = JXQueryController.termsAuthQuery(user.getStr("jxAccountId"));
		if ("1".equals(authDetail.get("paymentAuth"))) {// 开通了缴费授权
			// 缴费授权到期日
			String paymentDeadline = authDetail.get("paymentDeadline");
			// 缴费签约最高金额
			String paymentMaxAmt = authDetail.get("paymentMaxAmt");
			int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline,
					DateUtil.getNowDate());
			if (x < 0) {
				renderJson(error4json("14", "缴费授权已过期", ""));
				return;
			}
			if (StringUtil.getMoneyCent(paymentMaxAmt) < StringUtil.getMoneyCent(txFee)) {
				renderJson(error4json("15", "手续费超过了授权金额", ""));
				return;
			}
		} else {
			renderJson(error4json("16", "提现未授权", ""));
			return;
		}
		// 验证提现资金是否足够
		long avBalance = funds.getLong("avBalance");
		if (avBalance < StringUtil.getMoneyCent(txAmount)) {
			renderJson(error4json("17", "提现用户的可用余额不足", ""));
			return;
		}
		long withdrawAmount=StringUtil.getMoneyCent(txAmount);
		// 新增提现申请记录
		//String withdrawCode = CommonUtil.genMchntSsn(); // 提现交易号
		boolean result = withdrawTraceService.save(withdrawCode, userCode,funds.getStr("userName"), userInfo.getStr("userCardName"),banksV2.getStr("bankNo"), banksV2.getStr("bankNo"),"", banksV2.getStr("bankName"),
				"", withdrawAmount, "2", "0", "易分期用户申请提现", "", "", true);
		if (result == false) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-07", null);	// 记录日志
			renderJson(error4json("18", "提现异常18", ""));
			return;
		} else {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ：" + txAmount);	// 记录日志				
		}
		//返回交易页面链接
		String retPageUrl="withdraw";
		String retUrl = CommonUtil.ADDRESS+"/yfq/exchangePage"+"?retPageUrl="+retPageUrl;
		String successfulUrl = "";
		String rnotifyUrl = CommonUtil.CALLBACK_URL+"/yfq/withdrawCallbackForYfq?notify=" + notifyUrl + "&withdrawCode=" + withdrawCode;
		JXappController jxAppController = new JXappController();
		HttpServletResponse response = getResponse();
		msg = withdrawPage(jxAccountId, userInfo.getStr("userCardName"), banksV2.getStr("bankNo"), userInfo.getStr("idType"), userCardId, userMobile, txAmount, txFee, routeCode, cardBankCnaps, retUrl, rnotifyUrl, forgotPwdUrl, successfulUrl, response, businessAccountIdFlag,traceCodeYfq);
		String jxTraceCode = (String)msg.getReturn_data();
		if(!StringUtil.isBlank(jxTraceCode)){
			WithdrawTrace trace = withdrawTraceService.findById(withdrawCode);
			if(trace != null){
				trace.set("bankTraceCode", jxTraceCode.trim());
				if(trace.update()){
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！");
				}else{
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号 失败！", null);
				}
			}
		}
	}
	
	//存管返回的交易页面    
	public void exchangePage(){
		String retUrl = getPara("retPageUrl");
		redirect("app://lyfq/"+retUrl);
	}
	
	/**
	 *  页面开户（跳转至存管开户页面）
	 * 
	 * @param idType
	 *            证件类型 01-身份证18位
	 * @param idNo
	 *            证件号码
	 * @param name
	 *            姓名
	 * @param gender
	 *            性别 M:男性 F:女性
	 * @param acctUse
	 *            账户用途(00000-普通账户,10000-红包账户（只能有一个）,01000-手续费账户（只能有一个）,00100-
	 *            担保账户)
	 * @param mobile
	 *            手机号
	 * @param identity
	 *            身份属性 1:出借角色 2:借款角色 3:代偿角色
	 * @param response
	 *            HttpServletResponse
	 * @return
	 */
	public Message accountOpenPage(String idType, String idNo, String name, String gender, String acctUse,
			String mobile, String identity, HttpServletResponse response, String userCode, String retUrl, String successfulUrl, String notifyUrl,String trace) {
		if (idNo == null || idNo.length() != 18) {
			return error("01", "身份证不是18位", null);
		}
		if (mobile == null || mobile.length() != 11) {
			return error("03", "手机号不是11位", null);
		}

		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq4App(reqMap);// 添加通用参数
		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));
		
		reqMap.put("txCode", "accountOpenPage");
		reqMap.put("idType", idType);// 证件类型 01-身份证18位
		reqMap.put("idNo", idNo);// 证件号码
		reqMap.put("name", name);// 姓名
		reqMap.put("gender", gender);// 性别
		reqMap.put("mobile", mobile);// 手机号
		reqMap.put("acctUse", acctUse);// 00000普通账户
		reqMap.put("identity", identity);// 身份属性
		reqMap.put("coinstName", "易融恒信金融信息服务有限公司");// 平台名称
		reqMap.put("retUrl", retUrl);// 返回交易页面链接
		reqMap.put("successfulUrl", successfulUrl);//交易成功跳转链接
		reqMap.put("notifyUrl", notifyUrl);// 后台通知链接

		// 生成待签名字符串
		String requestMapMerged = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(requestMapMerged);
		reqMap.put("sign", sign);

		try {
			JXService.formSubmit(JXService.PAGE_URI + "accountOpenPage", reqMap, response);
		} catch (Exception e) {
		}
		return succ("信息提交成功", "");
	}
	
	/**
	 * 提现页面 
	 * 
	 * @param jxAccountId
	 *            存管账号
	 * @param userCardName
	 *            用户姓名
	 * @param bankNo
	 *            银行卡号
	 * @param idType
	 *            证件类型 01-身份证
	 * @param userCardId
	 *            证件号
	 * @param userMobile
	 *            手机号
	 * @param txAmount
	 *            提现金额(单位：元 精确到两位小数)
	 * @param txFee
	 *            手续费
	 * @param routeCode
	 *            路由代码 空：自动选择 2：人行大额通道
	 * @param cardBankCnaps
	 *            绑定银行联行号(routeCode == 2时必填)
	 * @param response
	 * @return
	 */
	public Message withdrawPage(String jxAccountId, String userCardName, String bankNo, String idType,
			String userCardId, String userMobile, String txAmount, String txFee, String routeCode, String cardBankCnaps,
			String retUrl, String notifyUrl, String forgotPwdUrl, String successfulUrl, HttpServletResponse response,String businessAccountIdFlag,String trace) {

		// 组织提现请求参数
		Map<String, String> reqMap = new TreeMap<>();
		try {
			reqMap.put("txCode", "withdraw");
			reqMap.put("accountId", StringUtil.isBlank(jxAccountId) ? "" : jxAccountId);
			reqMap.put("cardNo", StringUtil.isBlank(bankNo) ? "" : bankNo);
			// 手续费
			reqMap.put("txFee", StringUtil.isBlank(txFee) ? "" : txFee);
			reqMap.put("txAmount", StringUtil.isBlank(txAmount) ? "" : txAmount);
			reqMap.put("idType", StringUtil.isBlank(idType) ? "" : idType);
			reqMap.put("businessAccountIdFlag", businessAccountIdFlag);
			reqMap.put("idNo", StringUtil.isBlank(userCardId) ? "" : userCardId);
			reqMap.put("mobile", StringUtil.isBlank(userMobile) ? "" : userMobile);
			reqMap.put("name", StringUtil.isBlank(userCardName) ? "" : userCardName);

			if ("2".equals(routeCode)) {// 大额提现走人行大额通道
				reqMap.put("routeCode", StringUtil.isBlank(routeCode) ? "" : routeCode);
				reqMap.put("cardBankCnaps", StringUtil.isBlank(cardBankCnaps) ? "" : cardBankCnaps);
			}
			// 忘记密码跳转
			reqMap.put("forgotPwdUrl", forgotPwdUrl);
			reqMap.put("retUrl", retUrl);
			reqMap.put("notifyUrl", notifyUrl);
			reqMap.put("successfulUrl", successfulUrl);
		} catch (Exception e1) {
			return error("03", "申请提现失败，参数异常", null);
		}
		// 获取通用请求参数
		JXService.getHeadReq4App(reqMap);
		reqMap.put("txDate",trace.substring(0, 8));
		reqMap.put("txTime",trace.substring(8, 14));
		reqMap.put("seqNo",trace.substring(14,20));
		// 生成待签名字符串
		String mergeMap = JXService.mergeMap(reqMap);
		// 生成签名
		String sign = SignUtil_lj.sign(mergeMap);
		reqMap.put("sign", sign);

		JXService.formSubmit(JXService.PAGE_URI + reqMap.get("txCode"), reqMap, response);

		return succ("00", "" + reqMap.get("txDate") + reqMap.get("txTime") + reqMap.get("seqNo"));
	}



	 
	///////////////////////////////////////////////////
//	/**
//	 * 输出jsonObject对象
//	 * @param retCode	响应码
//	 * @param retMsg	响应描述
//	 * @param map		通用参数
//	 */
//	private void returnJson(String retCode,String retMsg,Map<String,Object> map){
//		JSONObject jsonObject = new JSONObject();
//		jsonObject.put("retCode", retCode);
//		jsonObject.put("retMsg", retMsg);
//		for(Map.Entry<String, Object> entry:map.entrySet()){
//			jsonObject.put(entry.getKey(), entry.getValue());
//		}
//		renderJson(jsonObject);
//	}
	
}
