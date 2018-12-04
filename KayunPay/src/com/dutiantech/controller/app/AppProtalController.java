package com.dutiantech.controller.app;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNotice;
import com.dutiantech.model.Notice;
import com.dutiantech.model.SignTrace;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNoticeService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.NoticeService;
import com.dutiantech.service.SignTraceService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

/**
 * @author Administrator
 *
 */
public class AppProtalController extends BaseController{
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private SignTraceService signTraceService = getService(SignTraceService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private UserService userService = getService(UserService.class);
	private BanksService banksService = getService(BanksService.class);
	private LoanNoticeService loanNoticeService = getService(LoanNoticeService.class);
	private NoticeService noticeService = getService(NoticeService.class) ;
	private LoanTransferService loanTransferService = getService(LoanTransferService.class) ;
	
	/**
	 * 版本号
	 * */
	@ActionKey("/app_ver")
	@AuthNum(value=999)
	public void ver(){
		Message msg=null;
		msg=succ("版本号", "20170922");
		renderJson(msg);
	}
	/**
	 * 首页显示
	 * */
	@ActionKey("/app_IndexPage")
	@AuthNum(value=999)
	public void IndexPage(){
		Message msg=null;
		Map<String,Object> map = new HashMap<String, Object>();
		String minLimit = getPara("minLimit");
		String maxLimit = getPara("maxLimit");
		String productType = getPara("productType","");
		String loanState = "J,M,N,O,P,Q,R";
		//加载首页标
		Page<LoanInfo> loanInfos = loanInfoService.findByPortal(1, 1,loanState,"1",productType,minLimit,maxLimit);
		LoanInfo apploanInfo=loanInfos.getList().get(0);
		map.put("loanInfos", apploanInfo);
		map.put("serverTime", DateUtil.getNowDateTime());
		//加载活动
		String sql="select title,url,pic ";
		sql+="from t_notice ";
		sql+="where status=? and type =? and addDateTime>20170601000000 and title not like '%运营报告%'";
		sql+="order by id desc limit 5";
		List<Notice> noticeList = Notice.noticeDao.find(sql,1,8);
		//加载滚动最新公告(3条)
		List<Notice> newsList = Notice.noticeDao.find("SELECT id nid,title,IFNULL(url,'') url FROM t_notice WHERE type = '0' AND `status` = '1' ORDER BY id DESC LIMIT 3");
//		for (int i = 0; i < newsList.size(); i++) {
//			Notice tmp = newsList.get(i);
//			tmp.put("nid", tmp.getInt("id"));
//			tmp.put("url", "");
//			tmp.remove("id");
//		}
		map.put("noticeList", noticeList);
		map.put("newsList", newsList);
		msg= succ("ok", map);
		renderJson(msg);
	}
	/**
	 * 点击签到
	 * */
	@ActionKey("/app_singIn")
	@AuthNum(value=999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class})
	public void singIn(){
		Message msg=null;
		String userCode = getUserCode();
		if (userCode == null) {
			msg= error("02", "用户未登录", "noLogin");
		}else{
			
			// 根据设置时间暂停签到功能
//			String pauseSatrtDate = "20180821";
//			if (DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), pauseSatrtDate) == 0 || 
//					DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), pauseSatrtDate) == 1) {
//				renderJson(error("99", "签到功能暂停~", null));
//				return;
//			}
			// 根据设置时间恢复签到功能
			String resumeDate = "20180905";
			if (DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), resumeDate) == -1) {
				renderJson(error("99", "签到功能暂停~", null));
				return;
			}
			
			Map<String,Object> map = new HashMap<String, Object>();
			if (signTraceService.isSign(userCode)) {
				msg= succ("已签到", null);
			} else {
				//防止恶意刷积分
				if(StringUtil.isBlank((String)Memcached.get("signToken_" + userCode)) 
						|| !DateUtil.getNowDate().equals(Memcached.get("signToken_" + userCode))){
					Memcached.set("signToken_" + userCode, DateUtil.getNowDate(), 30*1000);
				}else{
					msg = error("01", "请勿重复签到，若签到未成功，请30秒之后再操作！", "");
					renderJson(msg);
					return;
				}
				// 查询连续登录天数
				int sustainDay = signTraceService.findSustainDayByUser(userCode);
				// 账户积分变动
				int pointDay = sustainDay % 20 == 0 ? 20 : sustainDay % 20;
				int points = (SignTrace.POINT_MAP.get(pointDay) == null ? 0 : SignTrace.POINT_MAP.get(pointDay)) + 300; 
				fundsServiceV2.doPoints(userCode, 0 , points,"签到获取积分") ;
				// 保存签到记录
				signTraceService.saveSignTrace(null, userCode, pointDay, points, "");
				map.put("points", points / 100);
				map.put("sustain_day", sustainDay);
				msg= succ("签到成功", map);
			}
		}
		renderJson(msg);
	}
	
	/**
	 * 查询签到记录
	 * @return
	 */
	@ActionKey("/app_signHistory")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class}) 
	public void signHistory() {
		Message msg=null;
		Map<String, Object> map = new HashMap<String, Object>();
		String userCode = getUserCode();
		if ("null".equals(userCode) || userCode == null) {
			msg= error("01", "用户未登录", "noLogin");
		}else{
			String issign="签到成功";
			if (signTraceService.isSign(userCode)) {
				issign="已签到";
			}
			// 获取用户签到日期
			String[] signDays = signTraceService.getSignInDaysByMonth2(userCode);
			map.put("signDays", signDays);
			int sustainDay = signTraceService.findSustainDayByUser(userCode);
			long points=signTraceService.findNowPointByUser(userCode);
			map.put("points", points/100);
			map.put("sustainDay", sustainDay);
			Funds funds = fundsServiceV2.findById(userCode);
			long mypoints=funds.getLong("points");
			map.put("mypoints", mypoints/100);
			msg= succ(issign, map);
		}
		renderJson(msg);
	}
	/**
	 * 个人资料
	 * */
	@ActionKey("/app_userdetil")
	@AuthNum(value=999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class})
	public void userDetil(){
		String userCode= getUserCode();
		UserInfo userInfo = userInfoService.findById(userCode);
		Map<String,Object> map = new HashMap<String, Object>();
		Message msg=null;
		if(null==userInfo){
			msg=error("01","您还未实名认证", null);
		}else{
			String userCardId=userInfo.getStr("userCardId");
			try {
				userCardId=CommonUtil.decryptUserCardId(userCardId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(StringUtil.isBlank(userCardId)){
				msg= error("01", "未进行实名认证!", null);
			}else{
			String gender = IdCardUtils.getGenderByIdCard(userCardId);
			if ("M".equals(gender)) {
				gender = "男"; // 性别 男:0
			} else if ("F".equals(gender)) {
				gender = "女"; // 性别 女:1
			}
			String isAuthed=userInfo.getStr("isAuthed");
			if("2".equals(isAuthed)){
				map.put("userCardName", userInfo.getStr("userCardName"));
				map.put("userCardId", userCardId);
				map.put("sex", gender);
				if(null==userInfo.getStr("userAdress")||"".equals(userInfo.getStr("userAdress"))){
					map.put("userAddress", "");
				}else{
					map.put("userAddress", userInfo.getStr("userAdress"));
				}
				if(null==userInfo.getStr("ecpNme1")||"".equals(userInfo.getStr("ecpNme1"))){
					map.put("ecpNme1", "");
				}else{
					map.put("ecpNme1", userInfo.getStr("ecpNme1"));
				}
				if(null==userInfo.getStr("ecpMbile1")||"".equals(userInfo.getStr("ecpMbile1"))){
					map.put("ecpMbile1", "");
				}else{
					map.put("ecpMbile1", userInfo.getStr("ecpMbile1"));
				}
				if(null==userInfo.getStr("ecpRlation1")||"".equals(userInfo.getStr("ecpRlation1"))){
					map.put("ecpRlation1", "");
				}else{
					map.put("ecpRlation1", userInfo.getStr("ecpRlation1"));
				}
				msg=succ("ok", map);
			}else{
				msg=error("02", "实名认证中或未通过!", null);
			}
		}}
		renderJson(msg);
	}
	/**
	 * 保存个人资料
	 * */
	@ActionKey("/app_updateuserdetil")
	@AuthNum(value=999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class})
	public void updateUserDetil(){
		Message msg=null;
		String userCode = getUserCode();
		UserInfo userInfo = userInfoService.findById(userCode);
		//获取参数
		String userAdress =getPara("userAdress");
		String ecpNme1 =getPara("ecpNme1");
		String ecpRlation1 =getPara("ecpRlation1");
		String ecpMbile1 =getPara("ecpMbile1");
		userInfo.set("userAdress", userAdress);
		userInfo.set("ecpNme1", ecpNme1);
		userInfo.set("ecpRlation1", ecpRlation1);
		userInfo.set("ecpMbile1", ecpMbile1);
		//删除不可修改字段
		userInfo.remove("userCardName","userCardId","cardImg","isAuthed","userInfoMac");
		//修改
		boolean update = userInfo.update();
		if(update == false){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.USERINFO, "修改用户信息失败-02", null);
			msg= error("02", "修改失败!", "");
		}else{
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.USERINFO, "修改用户信息成功");
		msg= succ("修改成功","");
		}
		renderJson(msg);
		
	}
	/**
	 * 会员中心
	 * */
	@ActionKey("/app_vipdetil")
	@AuthNum(value=999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class})
	public void vipDetil(){
		String userCode=getUserCode();
		Message msg=null;
		Map<String,Object> map = new HashMap<String, Object>();
		User user = userService.findById(userCode);
		UserInfo userInfo =userInfoService.findById(userCode);
		List<BanksV2> banksV2 = banksService.findBanks4User(userCode);
		String userName= user.getStr("userName");
		String vipLevelName =user.getStr("vipLevelName");
		String evaluateResult = user.getEvaluateResult();//评测结果
		
		boolean tag=false;//是否开通存管
		Long  mypoints=user.getLong("userScore")/100;
		boolean isUserInfo= true;//是否完善 true为完善
		if(null==userInfo || StringUtil.isBlank(userInfo.getStr("userCardName"))
				|| StringUtil.isBlank(userInfo.getStr("userAdress"))
				|| StringUtil.isBlank(userInfo.getStr("ecpNme1"))
				|| StringUtil.isBlank(userInfo.getStr("ecpRlation1"))
				|| StringUtil.isBlank(userInfo.getStr("ecpMbile1"))) {
			isUserInfo=false;
		}
		//boolean ii=JXController.isJxAccount(user);
		String accountId = user.getStr("jxAccountId");
		String isBank="已绑定";
		String depositMobile = "";//存管手机号
		String isSetPwd = "n";
		String isPaymentAuth = "n";
		String isBindCard = "n";
		if(!StringUtil.isBlank(accountId)){
			//判断用户是否设置密码、是否授权
			isSetPwd = JXTraceService.verifyPwd(accountId);
			isPaymentAuth = JXappController.verifyPaymentAuth(accountId);
			//检查是否绑定过银行卡
			isBindCard = JXappController.verifyBindCard(accountId, "0");
			tag=true;
		}
		if(banksV2.size()==0||null==banksV2){//理财卡信息为空
			isBank="未激活";
		}else{
			if("y".equals(isBindCard)){//绑定过银行卡
				if(StringUtil.isBlank(banksV2.get(0).getStr("bankNo"))){
					isBank="未绑定";
				}
				depositMobile = banksV2.get(0).getStr("mobile");
				try {
					if(!"000".equals(depositMobile)){
						depositMobile = CommonUtil.decryptUserMobile(depositMobile);
					}
				} catch (Exception e) {
					msg = error("00", "解析用户身份证号错误", "");
					renderJson(msg);
					return;
				}
			}else{//未绑定过银行卡
				isBank="未激活";
			}
		}
		String userMobile= user.getStr("userMobile");
		String cardId=userInfo.getStr("userCardId");
		try {
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			cardId = CommonUtil.decryptUserCardId(cardId);
		} catch (Exception e) {
			msg = error("00", "解析用户身份证号或手机号错误", "");
			renderJson(msg);
			return;
	
		}
		boolean isrealname=true;//是否认证  true为已认证
		if("".equals(cardId)||null==cardId){
			isrealname=false;
		}
		Funds funds = fundsServiceV2.findById(userCode);
		Long kyjf = funds.getLong("points")/100;//可用积分
		
		map.put("isSetPwd", "y".equals(isSetPwd)?"已设置":"未设置");
		map.put("isPaymentAuth", "y".equals(isPaymentAuth)?"已授权":"未授权");
		map.put("depositMobile", depositMobile);
		map.put("kyjf", kyjf);
		map.put("isrealname", isrealname?"已认证":"未认证");
		map.put("userMobile", userMobile);
		map.put("userName", userName);
		map.put("vipLevelName", vipLevelName);
		map.put("mypoints", mypoints);
		map.put("isUserInfo", isUserInfo?"已完善":"未完善");
		map.put("isBank", isBank);
		map.put("tag",String.valueOf(tag));//老版app接受的string
		map.put("openDeposit",tag);//新版app判断是否开通存管
		map.put("evaluateResult", evaluateResult);
		msg=succ("ok", map);
		renderJson(msg);
	}
	
	/**
	 * 平台数据 ws
	 * */
	@ActionKey("/app_apppaasdata")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public void passData(){
		Message msg=null;
		Map<String,Object> resultMap = new HashMap<String, Object>();
		String sql = "select COALESCE(sum(reciedInterest),0),COALESCE(sum(beRecyPrincipal+beRecyInterest),0) from t_funds";
		String sql2 = "select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in('N','O','P','Q')";
		Object[] result = Db.queryFirst(sql);
		long loanAmount = Db.queryBigDecimal(sql2).longValue();
		long payTotal =  Long.parseLong(result[0].toString());
		long reciedTotal = Long.parseLong(result[1].toString());
//		resultMap.put("payTotal", Number.longToString(payTotal));//累积赚取利益
//		resultMap.put("reciedTotal", Number.longToString(reciedTotal));//待收金额
//		resultMap.put("payAmountTotal", Number.longToString(loanAmount));//交易总额
		resultMap.put("payTotal", "0");//累积赚取利益
		resultMap.put("reciedTotal", "0");//待收金额
		resultMap.put("payAmountTotal", "0");//交易总额
		long tzr = Db.queryLong("select count(userCode) from t_funds where userCode not in (select userCode from t_loan_info)");
		long jkr = Db.queryLong("select count(userCode) from t_funds where userCode in (select userCode from t_loan_info)");
		resultMap.put("tzr", String.valueOf(tzr));
		resultMap.put("jkr", String.valueOf(jkr));
		//按日累积金额
		int dayLength = getParaToInt("dayLength",7);
		List<Map<String,Object>> result2 = new ArrayList<Map<String,Object>>();
		List<String> days = CommonUtil.getAnyDateFromDayLength(dayLength, 0, 0, 0,"yyyyMMdd",0);
		int dd=days.size();
		for (int i = dd-1; i >=0; i--) {
			String tmp1 = days.get(i);
			long sum_loanAmount = loanInfoService.countLoanAmountByEffectDate(tmp1, tmp1);
			long sum_transferAmoun = loanTransferService.sumGotAmount4Date(tmp1, tmp1);
			Map<String,Object> tmp2 = new HashMap<String, Object>();
			tmp2.put("date", tmp1);
//			tmp2.put("data", String.valueOf((sum_loanAmount+sum_transferAmoun)/10000/100));
			tmp2.put("data", "0");
			result2.add(tmp2);
		}
		resultMap.put("days", result2);
		//按月累积金额
		int monthLength = getParaToInt("monthLength",7);
		List<Map<String,Object>> result3 = new ArrayList<Map<String,Object>>();
		List<Map<String,String>> months = CommonUtil.getAnyDateFromMonthLength(monthLength, 0, 0, "yyyyMMdd", 0);
		int mm=months.size();
		for (int i = mm-1; i >=0; i--) {
			String start = months.get(i).get("start");
			String end = months.get(i).get("end");
			long sum_loanAmount = loanInfoService.countLoanAmountByEffectDate(start, end);
			long sum_transferAmoun = loanTransferService.sumGotAmount4Date(start, end);
			Map<String,Object> tmp = new HashMap<String, Object>();
			tmp.put("date", start.substring(0,6));
//			tmp.put("data", String.valueOf((sum_loanAmount+sum_transferAmoun)/100/10000/100));
			tmp.put("data", "0");
			result3.add(tmp);
		}
		resultMap.put("months", result3);
		msg= succ("查询成功", resultMap);
		renderJson(msg);
	}
	
	/**
	 * 新标预告
	 * */
	@ActionKey("/app_querynewloans")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public void queryNewLoans(){
		Message msg= null;
		Page<LoanNotice> loanNotices = loanNoticeService.findByPage(
				1, 20, DateUtil.getNowDateTime(), null, "0", null);
		List<LoanNotice> list = loanNotices.getList();
		for(int i=0;i<list.size();i++){
			LoanNotice loanNotice=list.get(i);
			String date =loanNotice.getStr("overDateTime");
			String hh=date.substring(date.length()-6,date.length()-4);
			String mm=date.substring(date.length()-4,date.length()-2);
			String time =hh+":"+mm;
			loanNotice.put("time",time);
		}
		msg= succ("查询成功", loanNotices);
		renderJson(msg);
	}
	
	/**
	 * 邀请好友链接地址
	 * */
	@ActionKey("/app_friendurl")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void friendlyLink() {
		Message msg = null;
		String userCode = getUserCode();
		String url = "http://m.yrhx.com/m/#user_enter?u=" + userCode;
		msg = succ("邀请成功", url);
		renderJson(msg);
	}
	
	/**
	 * 新手福利
	 * @throws ParseException 
	 * */
	@ActionKey("/app_newloan")
	@AuthNum(value=999)
	@Before(PkMsgInterceptor.class)
	public void newLoan() {
		Message msg=null;
		Map<String,Object> map = new HashMap<String, Object>();
		String minLimit = getPara("minLimit");
		String maxLimit = getPara("maxLimit");
		String product_Type = getPara("productType","");
		String loanState = "J,M,N,O,P,Q,R";
		//加载首页标
		Page<LoanInfo> loanInfos = loanInfoService.findByPortal(1, 1,loanState,"1",product_Type,minLimit,maxLimit);
		LoanInfo apploanInfo=loanInfos.getList().get(0);
		DecimalFormat df = new DecimalFormat("0.00");
		int bfb = (int) (100 * (apploanInfo.getLong("loanAmount") - apploanInfo.getLong("loanBalance")) / apploanInfo.getLong("loanAmount"));
		String percent = df.format(bfb / 100.00);
		apploanInfo.put("percent", percent);
		apploanInfo.put("loanAmount", df.format(apploanInfo.getLong("loanAmount")/100));		
		apploanInfo.put("loanBalance", df.format(apploanInfo.getLong("loanBalance")/100));
		apploanInfo.put("maxLoanAmount", df.format(apploanInfo.getInt("maxLoanAmount")/100));
		apploanInfo.put("minLoanAmount", df.format(apploanInfo.getInt("minLoanAmount")/100));
		apploanInfo.put("rateByYear", df.format(apploanInfo.getInt("rateByYear")/100.00));
		apploanInfo.put("rewardRateByYear", df.format(apploanInfo.getInt("rewardRateByYear")/100.00));
		apploanInfo.put("benefits4new", df.format(apploanInfo.getInt("benefits4new")/100.00));
		
		String tmpLastPayLoanDateTime = apploanInfo.getStr("lastPayLoanDateTime");
		String lastPayLoanDateTime = "";
		if ("00000000000000".equals(tmpLastPayLoanDateTime)) {
			lastPayLoanDateTime = "";
		} else {
			try {
				lastPayLoanDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new SimpleDateFormat("yyyyHHddHHmmss")
								.parse(tmpLastPayLoanDateTime));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		apploanInfo.put("lastPayLoanDateTime", lastPayLoanDateTime);
		
		String productType = apploanInfo.getStr("productType");
		if ("A".equals(productType)) {
			productType = "质押宝";
		} else if ("B".equals(productType)) {
			productType = "车稳盈";
		} else if ("C".equals(productType)) {
			productType = "房稳赚";
		} else if ("D".equals(productType)) {
			productType = "其它";
		} else if ("G".equals(productType)) {
			productType = "稳定投";
		}
		apploanInfo.put("productType", productType);
		String refundType = apploanInfo.getStr("refundType");
		if ("A".equals(refundType)) {
			refundType = "等额本息";
		} else if ("B".equals(refundType)) {
			refundType = "先息后本";
		}
		apploanInfo.put("refundType", refundType);
		
		map.put("loanInfos", apploanInfo);
		msg=succ("新手福利", map);
		renderJson(msg);
		}

	/**
	 * 最新公告列表
	 * 20170908 WCF
	 */
	@ActionKey("/appNoticeList")
	@AuthNum(value = 999)
	public void noticeList() {
		Message msg = null;
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");

		// 验证数据完整性
		if (null == pageNumber || pageNumber <= 0) {
			pageNumber = 1;
		}
		if (null == pageSize || pageSize <= 0 || pageSize > 20) {
			pageSize = 20;
		}

		Page<Notice> queryNotice = noticeService.queryNotice(pageNumber,
				pageSize, "0", "1");
		List<Notice> list = queryNotice.getList();
		for (int i = 0; i < list.size(); i++) {
			Notice tmp = list.get(i);
			String upDateTime = "";
			try {
				upDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new SimpleDateFormat("yyyyMMddHHmmss")
								.parse(tmp.getStr("upDateTime")));
			} catch (ParseException e) {
				msg = error("01", "查询失败", "");
				renderJson(msg);
				return;
			}
			tmp.put("upDateTime", upDateTime);
			tmp.put("nid", tmp.getInt("id"));
			tmp.remove("description", "clicks", "addDateTime", "pic", "url", "type", "id");
		}
		msg = succ("list", queryNotice);
		renderJson(msg);
	}
	
	/**
	 * 最新公告详情
	 * @param nid 公告id
	 * 20170908 WCF
	 */
	@ActionKey("/appNoticeDetail")
	@AuthNum(value = 999)
	public void noticeDetail() {
		Message msg = new Message();
		String id = getPara("nid");
		
		Notice notice = Notice.noticeDao.findFirst("SELECT title,content,upDateTime FROM t_notice WHERE id = ?", id);
		String upDateTime = "";
		try {
			upDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(notice.getStr("upDateTime")));
		} catch (ParseException e) {
			msg = error("01", "查询失败", "");
			renderJson(msg);
			return;
		}
		notice.put("upDateTime", upDateTime);
		
		msg = succ("detail", notice);
		renderJson(msg);
	}

	/**
	 * app生成图形验证码
	 * */
	@ActionKey("/appGetVerifiCode")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void getVerifiCode() {
		Message msg = null;
		String verifiCode = "";// 验证码
		String verifiId = "";// 验证标识
		Map<String, String> map = new HashMap<String, String>();
		verifiId = CommonUtil.genMchntSsn();
		verifiCode = CommonUtil.getVerifiCode();
		// cached
		Memcached.set("verifi_code_" + verifiId, verifiCode, 60 * 10 * 1000);
		map.put("verifiId", verifiId);
		map.put("verifiCode", verifiCode);
		msg = succ("map", map);
		renderJson(msg);
	}
	
	/**
	 * 最新新闻列表
	 * 20180104 WCF
	 */
	@ActionKey("/appNewsList")
	@AuthNum(value = 999)
	public void newsList() {
		Message msg = null;
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");

		// 验证数据完整性
		if (null == pageNumber || pageNumber <= 0) {
			pageNumber = 1;
		}
		if (null == pageSize || pageSize <= 0 || pageSize > 20) {
			pageSize = 20;
		}

		Page<Notice> queryNotice = noticeService.queryNotice(pageNumber,
				pageSize, "1", "1");
		List<Notice> list = queryNotice.getList();
		for (int i = 0; i < list.size(); i++) {
			Notice tmp = list.get(i);
			String upDateTime = "";
			try {
				upDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new SimpleDateFormat("yyyyMMddHHmmss")
								.parse(tmp.getStr("upDateTime")));
			} catch (ParseException e) {
				msg = error("01", "查询失败", "");
				renderJson(msg);
				return;
			}
			tmp.put("upDateTime", upDateTime);
			tmp.put("nid", tmp.getInt("id"));
			tmp.remove("description", "clicks", "addDateTime", "pic", "url", "type", "id");
		}
		msg = succ("list", queryNotice);
		renderJson(msg);
	}
	
	/**
	 * 最新新闻详情
	 * @param nid 公告id
	 * 20180104 WCF
	 */
	@ActionKey("/appNewsDetail")
	@AuthNum(value = 999)
	public void newsDetail() {
		Message msg = new Message();
		String id = getPara("nid");
		
		Notice notice = noticeService.queryNewsDetail(id);
		String upDateTime = "";
		try {
			upDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(notice.getStr("upDateTime")));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		notice.put("upDateTime", upDateTime);
		
		msg = succ("detail", notice);
		renderJson(msg);
	}
	
	/**
	 * app 获取新闻活动公告等列表
	 * pageNumber 页数
	 * pageSize   条数
	 * type 内型  0 最新公告  1  公司新闻  7  app新闻  8  app活动  9  app运营报告
	 */
	@ActionKey("/appQueryNoticeList")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void queryNoticeList(){
		Message msg = new Message();
		Integer pageNumber = getParaToInt("pageNumber");
		Integer pageSize = getParaToInt("pageSize");
		
		String type = getPara("type");
		if(StringUtil.isBlank(type)){
			msg = error("01", "查询失败", "");
			renderJson(msg);
			return;
		}
		if (null == pageNumber || pageNumber <= 0) {
			pageNumber = 1;
		}
		if (null == pageSize || pageSize <= 0 || pageSize > 20) {
			if("0".equals(type)){
				pageSize = 3;
			}else if("1".equals(type)){
				pageSize = 2;
			}else if("7".equals(type)){
				pageSize = 5;
			}else if("8".equals(type)){
				pageSize = 5;
			}else if("9".equals(type)){
				pageSize = 5;
			}else{
				pageSize = 20;
			}
		}
		String title = null;
		if("9".equals(type)){
			title = "%运营报告%";
			type = "8";
		}
		Page<Notice> pageNotice = noticeService.queryNotice4App(pageNumber, pageSize, type, title);
		List<Notice> list = pageNotice.getList();
		for (int i = 0; i < list.size(); i++) {
			Notice tmp = list.get(i);
			String upDateTime = "";
			try {
				upDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new SimpleDateFormat("yyyyMMddHHmmss")
								.parse(tmp.getStr("upDateTime")));
			} catch (ParseException e) {
				msg = error("02", "查询失败", "");
				renderJson(msg);
				return;
			}
			tmp.put("upDateTime", upDateTime);
		}
		msg = succ("OK", pageNotice);
		renderJson(msg);
	}
	
	/**
	 * app短信接口20180930起停用
	 * 存管请求发送短信验证码接口 功能：向指定的手机号号码发送验证码，支持短
	 * 
	 * 信发送和语音呼叫方式
	 * 
	 * @return
	 */
	@ActionKey("/appCommonSmsCode")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
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
		Map<String, String> resMap = JXappController.smsCodeApply(mobile, reqType, srvTxCode, cardNo, smsType );
		
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
	
}
