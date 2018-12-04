package com.dutiantech.controller.portal;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.config.AdminConfig;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.FanLiTouUserInfo;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.AES;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.core.ActionKey;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

public class FanLiTouController extends BaseController{

	private UserService userService = getService(UserService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	static String LOGIN_TOKEN_CACHED_KEY = "fanlitou_login_token_" ;
	static String signKey = "ef6d3e5feae0a8c7b3e5cf7d97656a92";
	static String AESKey = "97166e0a5033b943";
	static AES AESUtil = new AES(AESKey) ; 
	static boolean isDev = AdminConfig.isDevMode ;
	
	private String getRequestHost(){
		String host = "";
		HttpServletRequest request = getRequest() ;
		int port = request.getServerPort() ;
		if( port == 443){
			host = "https://";
		}else{
			host = "http://";
		}
		host += request.getServerName() ;
		
		return host ;
	}
	
	/**
	 * 获取可投资标列表接口
	 */
	@ActionKey("/flt/bid_list")
	@AuthNum(value=999)
	public void bid_list(){
		
		int pageNumber = getParaToInt("pageIndex",1);
		int pageSize = getParaToInt("pageCount",20);
		
		if(pageNumber <= 0){
			pageNumber = 1;
		}
		if(pageSize > 20 ){
			pageSize = 20;
		}
		
		JSONObject object = new JSONObject();
//		if(checkSignNoCrypt()){
//			object.put("success", false);
//			object.put("message", "未能通过安全校验");
//			object.put("totalCount", 0);
//			object.put("bidList", new String[]{});
//			renderJson(object);
//			return ;
//		}
//		
//		String loanState = "J";
//		int nowdate=Integer.parseInt(DateUtil.getNowDate());
//		//若在活动11.11活动时间内   返利投不可见 ws
//		Page<LoanInfo> loanInfos = loanInfoService.findByPortal4Flt(pageNumber, pageSize,loanState,null,null,"18","18","20171111","20171117");
//		//end
//		object.put("success", true);
//		object.put("message", "查询标的列表成功");
//		object.put("totalCount", loanInfos.getTotalRow());
//		object.put("bidList", getBidList(loanInfos.getList()));
		object.put("success", true);
		object.put("message", "查询标的列表成功");
		object.put("totalCount", 0);
		object.put("bidList", new String[]{});
		renderJson(object);
		return ;
	}
	
	public JSONArray getBidList(List<LoanInfo> listLoan){
		JSONArray objArray = new JSONArray();
		for (int i = 0; i < listLoan.size(); i++) {
			LoanInfo loanInfo = listLoan.get(i);
			JSONObject obj = new JSONObject();
			obj.put("bidId", loanInfo.get("loanCode"));//产品Id
			
			String releaseDateTime = loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime");
			
			if(Long.valueOf(DateUtil.getNowDateTime()) >= Long.valueOf(releaseDateTime)){
				obj.put("status", 1);
			}else{
				obj.put("status", 5);
			}
			
			obj.put("name", loanInfo.get("loanTitle"));//产品名称
			obj.put("minInvestAmount", loanInfo.getInt("minLoanAmount")/100);//最小投资金额
			obj.put("introduction", loanInfo.get("loanDesc"));//产品描述
			//obj.put("pcUrl", "https://www.yrhx.com/Z02_1?loanCode=" + loanInfo.get("loanCode"));//PC访问地址
			obj.put("pcUrl", getRequestHost() + "/Z02_1?loanCode=" + loanInfo.get("loanCode"));//PC访问地址
			obj.put("mobileUrl", "http://m.yrhx.com/m/#loan_info?loanCode=" + loanInfo.get("loanCode"));//移动（wap）端访问地址
			obj.put("totalAmount", loanInfo.getLong("loanAmount")/100);//标的总金额
			obj.put("remainAmount", loanInfo.getLong("loanBalance")/100);//剩余可投金额
			obj.put("duration", loanInfo.getInt("loanTimeLimit"));//标的期限
			obj.put("durationUnit", 30);//标的期限单位
			obj.put("isNewUser", loanInfo.getInt("benefits4new") > 0 );//是否新手标
			obj.put("repaymentType", getRefundType(loanInfo.getStr("refundType")));//还款方式
			obj.put("isGroup", false);
			obj.put("interestRate", 
					new BigDecimal((loanInfo.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP));//标的利率
			obj.put("awardInterestRate", 
					new BigDecimal((loanInfo.getInt("rewardRateByYear")+loanInfo.getInt("benefits4new"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP));//奖励利率
			objArray.add(obj);
		}
		return objArray;
	}
	
	private int getRefundType(String refundType){
		if(refundType.equals("A")){
			return 4;
		}else{
			return 1;
		}
	}
	
	/**
	 * 	获取单个标的详情接口
	 */
	@ActionKey("/flt/get_single_bid")
	@AuthNum(value=999)
	public void get_single_bid(){
		JSONObject object = new JSONObject();
		String bid_id = getPara("bid_id");
		if(checkSignNoCrypt() || StringUtil.isBlank(bid_id)){
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数bid_id为空");
			object.put("bid", new String[]{});
			renderJson(object);
			return ;
		}
		
		LoanInfo loanInfo = loanInfoService.findById(bid_id);
		object.put("success", true);
		object.put("message", "查询标的详情成功");
		object.put("bid", getBidDetial(loanInfo));
		renderJson(object);
		return ;
	}
	
	public JSONObject getBidDetial(LoanInfo loanInfo){
		JSONObject obj = new JSONObject();
		obj.put("bidId", loanInfo.get("loanCode"));//产品Id
		obj.put("status", getLoanStatus(loanInfo.getStr("loanState")));
		obj.put("fullTime", getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")));//满标时间
		obj.put("name", loanInfo.get("loanTitle"));//产品名称
		obj.put("minInvestAmount", loanInfo.getInt("minLoanAmount")/100);//最小投资金额
		obj.put("introduction", loanInfo.get("loanDesc"));//产品描述
		//obj.put("pcUrl", "https://www.yrhx.com/Z02_1?loanCode=" + loanInfo.get("loanCode"));//PC访问地址
		obj.put("pcUrl", getRequestHost() + "/Z02_1?loanCode=" + loanInfo.get("loanCode"));//PC访问地址
		obj.put("mobileUrl", "http://m.yrhx.com/m?loanCode=" + loanInfo.get("loanCode"));//移动（wap）端访问地址
		obj.put("totalAmount", loanInfo.getLong("loanAmount")/100);//标的总金额
		obj.put("remainAmount", loanInfo.getLong("loanBalance")/100);//剩余可投金额
		obj.put("duration", loanInfo.getInt("loanTimeLimit"));//标的期限
		obj.put("durationUnit", 30);//标的期限单位
		obj.put("isNewUser", loanInfo.getInt("benefits4new") > 0 );//是否新手标
		obj.put("repaymentType", getRefundType(loanInfo.getStr("refundType")));//还款方式
		obj.put("isGroup", false);
		obj.put("interestRate", 
				new BigDecimal((loanInfo.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP));//标的利率
		obj.put("awardInterestRate", 
				new BigDecimal((loanInfo.getInt("rewardRateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP));//奖励利率
		return obj;
	}
	
	public String getFormatDateTime(String dateTime){
		if(dateTime.length() < 14){
			return "0000-00-00 00:00:00";
		}
		return dateTime.substring(0,4) + "-" + dateTime.substring(4,6)+ "-" + dateTime.substring(6,8)
				 + " " + dateTime.substring(8,10) + ":" + dateTime.substring(10,12) + ":" + dateTime.substring(12,14);
	}
	
	public String getFormatDate(String date){
		if(date.length() < 8){
			return "0000-00-00";
		}
		return date.substring(0,4) + "-" + date.substring(4,6)+ "-" + date.substring(6,8);
	}
	
	public int getLoanStatus(String loanState){
		int status = 2;
		switch (loanState) {
			case "J":
				status = 1;
				break;
			case "M":
				status = 2;
				break;
			case "N":
				status = 3;
				break;
			case "O":
				status = 4;
				break;
			case "P":
				status = 4;
			case "Q":
				status = 4;
			case "R":
				status = 4;
				break;
			case "L":
				status = -1;
				break;
			default:
				break;
		}
		return status;
	}
	
	
	/**
	 * 新用户注册接口
	 */
	@ActionKey("/flt/register")
	@AuthNum(value=999)
	public void register(){
		
		if( checkSign() == false ){
			renderJson(makeSignFailurePkg());
			return ;
		}
		
		String userMobile = getParamByDecrypt("phone_num") ;
		String fcode = getParamByDecrypt("fcode");
		String uid = getParamByDecrypt("uid");

		//验证是否已经存在此手机号
		User user = userService.find4mobile(userMobile);
		if( user != null ){
			if( FanLiTouUserInfo.fanlitouDao.queryByMobile(userMobile) != null ){
				renderJson( makeReturnPkg("42", "已绑定返利投渠道"));
				return ;
			}
			
			renderJson( makeReturnPkg("42", "其他渠道用户"));
			return ;
		}
		
		
		String loginPasswd = UIDUtil.generate().substring(12, 20);	//random,随机8位密码
		String userName =  userMobile.substring(0, 3) + "****" + userMobile.substring(7, 11);//"返利投_" + UIDUtil.generate().substring(5, 10) ;	//random 10位字符
		String regUserCode = UIDUtil.generate();
		
		boolean b = userService.save(regUserCode,userMobile, "00@yrhx.com", loginPasswd,userName, getRequestIP(),
				String.format("[%s][%s]", fcode,uid)  );
		if(b == false){
			//记录日志
			BIZ_LOG_ERROR(userMobile, BIZ_TYPE.REGISTER, "注册失败",null);
			renderJson( makeReturnPkg("42", "数据落地异常"));
		}else{
			String token = FanLiTouUserInfo.fanlitouDao.saveUser(regUserCode, userName, userMobile, fcode, uid, 0 ) ;
			if( token == null ){
				renderJson(makeReturnPkg("42", "用户平台生成成功,但绑定返利投关系失败"));
				return ;
			}
			
			ticketService.toReward4newUser(regUserCode);
			// 注册成功送可用积分 add by stonexk at 20170601
//			fundsServiceV2.doPoints(regUserCode, 0 , 1000, "注册送积分") ;
			// end add
			
			JSONObject retPkg = makeReturnPkg("01", "注册成功");
			retPkg.put("user_name", encrypt(userName) ) ;
			retPkg.put("register_token", encrypt(token) ) ;
			
			//TODO 发送带密码的短信
			String msgContent = CommonUtil.SMS_MSG_REGISTER_FLT.replace("[name]", userName).replace("[mobile]", userMobile).replace("[pwd]", loginPasswd);
			SMSClient.sendSms(userMobile, msgContent);
			
			renderJson( retPkg );
		}
		
	}
	
	/**
	 * 老用户账户绑定接口
	 */
	@ActionKey("/flt/bind")
	@AuthNum(value=999)
	public void bind(){

		if( checkSign() == false ){
			renderJson(makeSignFailurePkg());
			return ;
		}

		String uid = getParamByDecrypt("uid");
		String source = getParamByDecrypt("source");
		String fcode = getParamByDecrypt("fcode");
//		String bid_url = getPara("bid_url");
		
		String queryString = "?fltuid="+uid+"&source=" + fcode;
		
		if( "wap".equals( source.toLowerCase() ) == true ){
			redirect("http://m.yrhx.com/m" + queryString, true);
		}else{
			Memcached.set( fcode + uid , uid , 360*1000 ) ;
			redirect( getRequestHost() + "/login" + queryString, true );//TODO 动态添加接口
		}
		
	}
	
	/**
	 * 获取login token接口
	 */
	@ActionKey("/flt/token")
	@AuthNum(value=999)
	public void token(){

		if( checkSign() == false ){
			renderJson(makeSignFailurePkg());
			return ;
		}

		String uid = getParamByDecrypt("uid");
		String regToken = getParamByDecrypt("register_token");
		
		FanLiTouUserInfo userInfo = FanLiTouUserInfo.fanlitouDao.queryByUID(uid) ;
		if( userInfo == null ){
			renderJson(makeReturnPkg("45","无用户信息"));
			return ;
		}
		
		String tmpToken = userInfo.getStr("token") ;
		if( tmpToken.equals(regToken) == false ){
			renderJson(makeReturnPkg("41", "注册Token不匹配"));
			return ;
		}else{
			JSONObject ret = makeReturnPkg("05", "获取Token成功");
			
			String loginToken = UIDUtil.generate() ;
			ret.put("login_token", encrypt( loginToken ) ) ;
			
			String userCode = userInfo.getStr("userCode") ;
			
			//save token		cached 30s
			Memcached.set( LOGIN_TOKEN_CACHED_KEY + loginToken  , userCode , System.currentTimeMillis() + 30*1000 ) ;
			
			renderJson( ret );
		}
	}
	

	/**
	 * 登录授权接口
	 */
	@ActionKey("/flt/login")
	@AuthNum(value=999)
	public void login(){

		if( checkSign() == false ){
			renderJson(makeSignFailurePkg());
			return ;
		}
		
		String fltLoginToken = getParamByDecrypt("login_token");
		Object plfLoginToken = Memcached.get( LOGIN_TOKEN_CACHED_KEY + fltLoginToken );
		if( plfLoginToken == null ){
			renderJson(makeReturnPkg("45", "login token 已过期!"));
			return ;
		}else{
			//clear
			Memcached.delete( LOGIN_TOKEN_CACHED_KEY + fltLoginToken ) ;
		}
		
		String uid = getParamByDecrypt("uid") ;
		FanLiTouUserInfo userInfo = FanLiTouUserInfo.fanlitouDao.queryByUID(uid) ;
		if( userInfo == null ){
			renderJson(makeReturnPkg("45", "返利投渠道用户信息不存在"));
			return ;
		}
		
		String tmpRegToken = userInfo.getStr("token");
		String fltRegToken = getParamByDecrypt("register_token");
		if( tmpRegToken.equals(fltRegToken) == false ){
			renderJson(makeReturnPkg("41", "注册Token不一致!"));
			return ;
		}
		
		String userCode = userInfo.getStr("userCode");
		User user = User.userDao.findFirst("select * from t_user where userCode = ? ", userCode ) ;
		String userState = user.getStr("userState");
		if( userState.equals("N") == false ){
			renderJson(makeReturnPkg("45", "用户在平台已被冻结"));
			return ;
		}
		Memcached.set("PORTAL_USER_" + userCode , user ) ;
		//修改用户登录相关字段
		userService.updateUser4Login(userCode, getRequestIP());

		//记录日志
		BIZ_LOG_INFO( userCode , BIZ_TYPE.LOGIN , "用户从返利投授权登陆 " );
		
//		long exTime = 7*24*60*60 ;
		String token = UserUtil.UserEnCode(userCode, getRequestIP(),AuthInterceptor.INVALIDTIME, null) ;
		setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token , AuthInterceptor.INVALIDTIME );

		long cv = System.currentTimeMillis()/1000/60/60/24 ;
		setCookie("userCode_" + cv , userCode, 3600*100 ) ;
		setCookie("userName_" + cv, user.getStr("userName"), 3600*100 ) ;
		
		String source = getParamByDecrypt("source") ;
		String bid_url = getPara("bid_url");
		
		if( StrKit.isBlank(bid_url) == false ){
			//has url
//			bid_url = AESUtil.decrypt(bid_url) ;
			redirect(bid_url);
		}else{
			// no url
			if( "WAP".equals(source) == true ){
				redirect("http://m.yrhx.com/m");
			}else{
				forward("/portal/A00.html");
			}
		}
		
	}
	
	/**
	 * 注册关系查询接口
	 */
	@ActionKey("/flt/register_query")
	@AuthNum(value=999)
	public void register_query(){
		if( checkSign() == false ){
			renderJson(makeSignFailurePkg());
			return ;
		}

//		String uid = getParamByDecrypt("uid");
		String mobile = getParamByDecrypt("phone_num");
		
		FanLiTouUserInfo userInfo = FanLiTouUserInfo.fanlitouDao.queryByMobile(mobile) ;
		User user = userService.find4mobile(mobile) ;

		if( user == null ){
			renderJson(makeReturnPkg("10", "新用户,未注册"));
			return ;
		}else{
			if( userInfo == null ){
				renderJson(makeReturnPkg("11", "老用户，非渠道用户"));
				return ;
			}
		}

		JSONObject ret = null ;
		if( userInfo.getInt("bindType") == 0 ){
			ret = makeReturnPkg("12", "返利投渠道用户");
		}else{
			ret = makeReturnPkg("14", "老用户，已绑定返利投渠道用户") ;
		}
		
		ret.put("register_token", encrypt(userInfo.getStr("token")) );
		ret.put("user_name", encrypt(userInfo.getStr("userName")) );
		
		renderJson(ret);
	}
	
	/**
	 * 投资记录查询接口
	 */
	@ActionKey("/flt/query")
	@AuthNum(value=999)
	public void query(){
		
		JSONObject object = new JSONObject();
		String start_time = getPara("start_time");
		String end_time = getPara("end_time");
		int pageNumber = getParaToInt("pageIndex",1);
		int pageSize = getParaToInt("pageCount",20);
		if(pageNumber <= 0){
			pageNumber = 1;
		}
		if(pageSize > 50){
			pageSize = 50;
		}
		
		if(checkSignNoCrypt() || StringUtil.isBlank(start_time) || StringUtil.isBlank(end_time)){
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数start_time ,end_time为空");
			object.put("totalCount", 0);
			object.put("bid", new String[]{});
			renderJson(object);
			return ;
		}
		
		
		Page<LoanTrace> loanTraces = loanTraceService.findByPage4flt(pageNumber, pageSize, start_time.replace("-", "")+"000000", 
				end_time.replace("-", "")+"235959");
		
		object.put("success", true);
		object.put("message", "查询投标流水成功");
		object.put("totalCount", loanTraces.getTotalRow());
		object.put("orders", getLoanTraceList(loanTraces.getList()));
		renderJson(object);
		return ;
		
	}
	
	private JSONArray getLoanTraceList(List<LoanTrace> listLoanTrace){
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
			obj.put("bidId", loanTrace.get("loanCode"));//产品Id
			obj.put("bidStatus", getLoanStatus(loanTrace.getStr("loanState")));
			obj.put("bidName", loanTrace.get("loanTitle"));//产品名称
			obj.put("investAmount", loanTrace.getLong("payAmount")/100);//投资金额
			obj.put("investTime", getFormatDateTime(loanTrace.getStr("loanDateTime")));//投标的时间
			obj.put("isFirstInvest", 
					null == loanTraceService.query4flt(loanTrace.getStr("payUserCode"), loanTrace.getStr("loanDateTime")) ? 1 : 0 );//用户该笔投资记录在平台是否为首次投资
			objArray.add(obj);
		}
		return objArray;
	}
	
	
	
	
	/**
	 * 债转（提前还款）记录查询接口
	 */
	@ActionKey("/flt/assign")
	@AuthNum(value=999)
	public void assign(){
		
		JSONObject object = new JSONObject();
		String start_time = getPara("start_time");
		String end_time = getPara("end_time");
		int pageNumber = getParaToInt("pageIndex",1);
		int pageSize = getParaToInt("pageCount",20);
		if(pageNumber <= 0){
			pageNumber = 1;
		}
		if(pageSize > 50){
			pageSize = 50;
		}
		
		if(checkSignNoCrypt() || StringUtil.isBlank(start_time) || StringUtil.isBlank(end_time)){
			object.put("success", false);
			object.put("message", "未能通过安全校验或参数start_time ,end_time为空");
			object.put("totalCount", 0);
			object.put("bid", new String[]{});
			renderJson(object);
			return ;
		}
		
		Page<LoanTransfer> loanTraces = loanTransferService.findByPage4flt(pageNumber, pageSize, start_time.replace("-", ""), 
				end_time.replace("-", ""));
		
		object.put("success", true);
		object.put("message", "查询投标流水成功");
		object.put("totalCount", loanTraces.getTotalRow());
		object.put("orders", getLoanTransferList(loanTraces.getList()));
		renderJson(object);
		return ;
		
	}
	private JSONArray getLoanTransferList(List<LoanTransfer> listLoanTransfer){
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
			obj.put("bidId", loanTranfer.get("loanCode"));//产品Id
			obj.put("bidName", loanTranfer.get("loanTitle"));//产品名称
			obj.put("investAmount", loanTranfer.getInt("payAmount")/100);//投资金额
			obj.put("investTime", getFormatDateTime(loanTrace.getStr("loanDateTime")));//投标时间
			obj.put("isAssign", true);//是否债权转让
			obj.put("isFullAssign", true);//是否全部债权转让
			obj.put("assignAmount", loanTranfer.getInt("leftAmount")/100);//债权转让金额
			obj.put("assignDate", getFormatDate(loanTranfer.getStr("gotDate")));//债权转让时间
			obj.put("isAdvancedRepay", false);//是否提前还款
			obj.put("advancedRepayDate", null);//提前还款时间
			objArray.add(obj);
		}
		return objArray;
	}
	
	
	
	/**
	 * 	校验签名
	 * @return
	 * 		true	验证通过
	 * 		false 	验证失败
	 */
	private boolean checkSign(){
		
		String tmpSign = getParamByDecrypt("sign" ) ;
		String t = getParamByDecrypt("t") ;
		String checkValue = "" ;
		
		try {
			checkValue = MD5Code.crypt( t + signKey );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false ;
		}
		
		if( isDev ){
			System.out.println(String.format( "Check Sign : [%s][%s][%s]" , t , tmpSign , checkValue ));
		}
		
		return  checkValue.equals(tmpSign) ;
		
	}
	

	private boolean checkSignNoCrypt(){
		
		String tmpSign = getPara("sign" ) ;
		String t = getPara("t") ;
		String checkValue = "" ;
		
		try {
			checkValue = MD5Code.crypt( t + signKey );
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false ;
		}
		
		return  checkValue.equals(tmpSign) ;
		
	}
	
	/**
	 * 	安全未通过校验
	 * @return
	 */
	private JSONObject makeSignFailurePkg(){
		return makeReturnPkg("41", "未通过安全校验");
	}
	
	private JSONObject makeReturnPkg(String status , String msg ){
		JSONObject retPkg = new JSONObject() ;
		retPkg.put("status", encrypt( status )) ;
		retPkg.put("msg", encrypt( msg ) ) ;
		return retPkg ;
	}
	
	private String getParamByDecrypt(String key ){
		String tmpValue = getPara(key) ;
		String val = AESUtil.decrypt(tmpValue);
		if( isDev ){
			System.out.println(String.format("Crypt value:%s  Decrypt value : %s ", tmpValue , val ));
		}
		return val ;
	}
	
	private String encrypt(String val){
		return AESUtil.encrypt(val) ;
	}
	
	
	
}
