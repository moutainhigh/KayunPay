package com.dutiantech.controller.admin;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ancun.ops.dto.OpsResponse;
import com.dutiantech.Log;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.UserTermsAuth;
import com.dutiantech.plugins.CFCA;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.CFCAService;
import com.dutiantech.service.FilesService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.HistoryRecyService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.ReturnedAmountService;
import com.dutiantech.service.SettlementPlanService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.task.CallService;
import com.dutiantech.task.Task;
import com.dutiantech.task.TaskService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.PropertiesUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.util.UIDUtil;
import com.fuiou.data.QueryBalanceResultData;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.http.WebUtils;
import com.jx.service.JXService;
import com.jx.util.RetCodeUtil;

public class TestController extends BaseController {

	private static final Logger logger = Logger.getLogger("bukechongfu");
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
	private UserService userService=getService(UserService.class);
	private LoanInfoService loanInfoService=getService(LoanInfoService.class);
	private LoanTraceService loanTraceService=getService(LoanTraceService.class);
	private BanksService banksService=getService(BanksService.class);
	private UserInfoService userInfoService=getService(UserInfoService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private WithdrawTraceService withdrawTraceService=getService(WithdrawTraceService.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private HistoryRecyService historyRecyService = getService(HistoryRecyService.class);
	private RecommendInfoService recommendInfoService = getService(RecommendInfoService.class);
	private ReturnedAmountService returnedAmountService = getService(ReturnedAmountService.class);
	private RecommendRewardService rrService = getService(RecommendRewardService.class);
	private SettlementPlanService settlementPlanService = getService(SettlementPlanService.class);
	private CFCAService cfcaService = getService(CFCAService.class);
	private OPUserV2Service opUserV2Service = getService(OPUserV2Service.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
	
	static {
		LoggerUtil.initLogger("test", logger);
		;
	}

	@ActionKey("/xxxx")
	public void xxxx() {

		logger.log(Level.INFO, "info级别");

		logger.log(Level.SEVERE, "severe级别");

		logger.log(Level.WARNING, "WARNING级别");

		String cardCity = getPara("cardCity");

		String province_name = cardCity.split("\\|")[0];
		String city_name = cardCity.split("\\|")[1];
		System.out.println(province_name);
		System.out.println(city_name);
		int province_code = 0;
		int city_code = 0;
		if (!StringUtil.isBlank(cardCity)) {
			if (!StringUtil.isBlank(province_name) && !StringUtil.isBlank(city_name)) {
				Integer tmp1 = Db.queryInt("select id from t_location where areaname =? or shortname=?", province_name,
						province_name);
				if (tmp1 != null)
					province_code = tmp1.intValue();

				Integer tmp2 = Db.queryInt("select id from t_location where areaname =? or shortname=?", city_name,
						city_name);
				if (tmp2 != null)
					city_code = tmp2.intValue();

				if (province_code == 110000) {
					city_code = 110100;
				}
				if (province_code == 120000) {
					city_code = 120100;
				}
				if (province_code == 310000) {
					city_code = 310100;
				}
				if (province_code == 500000) {
					city_code = 500100;
				}
			}
		}
		System.out.println("province_code:" + (province_code != 0 ? province_code + "" : ""));
		System.out.println("city_code:" + (city_code != 0 ? city_code + "" : ""));
		renderHtml(province_code != 0 ? province_code + "" : "" + "|||" + (city_code != 0 ? city_code + "" : "11"));
	}

	@ActionKey("/test1")
	public void test1() {
		Record r = Db.findById("t_user", "userCode", "aab6e7dded184336b9fcd21b526e65ab");
		renderJson(succ("ok", r));
	}

	@ActionKey("/test2")
	@AuthNum(value = 999)
	@ResponseCached(cachedKey = "test2cached1_", cachedKeyParm = "@userCode|@datetime(yyyyHHmm)|userKey", time = 10, mode = "remote")
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message test2() {

		System.out.println(getUserCode());

		User user = User.userDao.findByIdLoadColumns("0007d965d8ca6886e5009799e19be04c", "userCode,userState");
		return succ("ok", user);
	}

	@ActionKey("/test3")
	@Before(AuthInterceptor.class)
	public Message test3() {

		User user = User.userDao.findByIdLoadColumns("aab6e7dded184336b9fcd21b526e65ab", "userCode,userState");

		if (user.getStr("asdasdad") == null) {
			throw new BaseBizRunTimeException("01", "xx", null);
		}

		return succ("ok", user);
	}

	@ActionKey("/test4")
	public void test4() {
		BIZ_LOG_INFO("测试号", BIZ_TYPE.LOGIN, "测试测试");
		renderText("00");
	}

	@ActionKey("/yipay")
	public void yipay() {

		HttpServletRequest request = getRequest();
		System.out.println("Method : " + request.getMethod());
		System.out.println("CharSet : " + request.getCharacterEncoding());
		Enumeration<String> enm = request.getParameterNames();
		StringBuffer buff = new StringBuffer();
		while (enm.hasMoreElements()) {
			String key = enm.nextElement();
			System.out.println(key + "  =  " + request.getParameter(key));
			buff.append(key + "=" + request.getParameter(key) + "</br>");
		}
		renderHtml(buff.toString());

	}

	/*
	 * @ActionKey("/upload") public void upload() throws IOException{ UploadFile
	 * file = UploadFile.makeFile(getRequest()) ; UploadResult result = new
	 * UploadResult(); System.out.println( file.getData().length ); int ret =
	 * QCloudPic.upload( file.getData() , result); JSONObject obj = new
	 * JSONObject() ; if (ret == 0) { // String id = getPara("id");
	 * result.Print(); // renderHtml("name:" + file.getName() +
	 * "<br />download_url:" // + result.download_url + "<br />fileid:" +
	 * result.fileid +"<br />size:"+file.getData().length +"<br />" // +
	 * "<img src='" + result.download_url + "' />"); Map<String , String > ps =
	 * file.getParas() ; obj.put("fileid", result.fileid ); obj.put("id",
	 * ps.get("id") );
	 * 
	 * renderJson( obj ); //System.out.println("upload pic error, error=" +
	 * QCloudPic.GetError()); //renderJson(QCloudPic.PIC_CLOUD); } }
	 */

	@ActionKey("/testReadFile")
	public void testReadFile() {

		HttpServletRequest request = getRequest();
		System.out.println(request.getCharacterEncoding());

		System.out.println(request.getContentType());

		try {
			String x = PropertiesUtil.getInstance().getProperty("autoLoanKey");
			System.out.println(x + "~~~~~~~");
			renderJson(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@ActionKey("/testServer")
	public void testService() {
		try {
			Thread.sleep(5 * 60 * 1000); // 休眠5分钟，测试
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("get request!!!!");
		renderText("ok");
	}

	@ActionKey("/testServer2")
	public void testService2() {
		System.out.println("get request2222!!!!");
		renderText("ok");
	}

	@ActionKey("/gogo")
	public void startService() {

		// 开启服务，不断请求自己
		// new CallService("http://127.0.0.1/testServer", 100 ,"服务一").start();
		Log.devModel = true;
		// Task.newTask( new DayService("http://127.0.0.1/testServer2", "16:30"
		// ,"日终清算服务") );
		TaskService task = new CallService("http://127.0.0.1/autoLoan", 5000, "自动投标服务");
		task.addParam("key", "3.14159265358");
		Task.newTask(task);

		renderText("ok");
	}

	@ActionKey("/status4service")
	public void getServiceStatus() {
		Set<String> keys = Task.TASK.keySet();
		StringBuffer buff = new StringBuffer();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
			TaskService task = Task.TASK.get(it.next());
			buff.append(String.format("[%s] 运行状态 [%s]", task.getName(), task.getState().toString()));
			buff.append("<br />");
		}

		renderHtml(buff.toString());

	}

	@ActionKey("/killTask")
	public void killTask() {
		Task.clearTask("自动投标服务");
		renderText("ok");
	}

	@ActionKey("/hookService")
	public void hookService() {
		int count = 0;
		int max = 60 * 60 * 1000;
		while (count < max) {
			System.out.println(count++);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		renderHtml("ok:" + count);
	}

	@ActionKey("/memset")
	public void memset() {
		String key = getPara("key");
		String value = getPara("value");

		boolean r = Memcached.set(key, value);

		Memcached.storeCounter("test2", 100);

		renderText(r + "");
	}

	@ActionKey("/servicetest")
	public void serviceTest() {
		FilesService filesService = getService(FilesService.class);
		System.out.println(filesService);
		renderText("00");
	}

	@ActionKey("/memget")
	public void memget() {
		// String key = getPara("key");
		// renderText("" + Memcached.get(key) );
		// Memcached.storeCounter("asdasdadad222", 1) ;
		// Memcached.storeCounter("test2", 102 ) ;
		Memcached.storeCounter("counter1", 1024);
		Memcached.incr("counter1", 100);
		Memcached.decr("counter1", 300);
		Memcached.storeCounter("counter1", 1024);
		Memcached.set("setkey", "hahahah");
		Memcached.get("setkey");
		Memcached.set("haha", "deng guo qi", 10000);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Memcached.get("haha");
		Memcached.delete("setkey");
		Memcached.get("setkey");
		renderText("ok");
	}

	/*
	 * @ActionKey("/sendSmsAll") public void sendSMSAll(){ String smsContent =
	 * "【易融恒信】将于29日14点开始系统升级，预计31日14点结束。29日将提前处理维护期间回款，并处理提现。详情请查看官网"; int index
	 * = 1 ; int maxSize = 100 ; int tmpMaxPage = 100 ; SMSService smss = new
	 * SMSService(); while(true ){ if( index > tmpMaxPage ){
	 * System.out.println("转换结束"); break ; } Page<User> users =
	 * User.userDao.paginate(index, maxSize, "select userMobile from t_user ");
	 * List<User> us = users.getList(); Record record = null ; for( User user :
	 * us ){ String tmpMobile = user.getStr("userMobile"); if(
	 * StringUtil.isBlank(tmpMobile) == false ){ try{ tmpMobile =
	 * CommonUtil.decryptUserMobile(tmpMobile) ; if(
	 * CommonUtil.isMobile(tmpMobile) == true ){ record = new Record() ;
	 * record.set("mobile", tmpMobile ) ; int result = smss.sendSms(tmpMobile,
	 * smsContent ) ; record.set("isSendSuccess", result ) ;
	 * System.out.println("发送短信：" + tmpMobile ); Db.save("t_tmp_sms", record);
	 * Thread.sleep(100); //Db.update("insert into t_tmp_sms values('"
	 * +tmpMobile+"',0)") ; } }catch(Exception e){ e.printStackTrace(); } } }
	 * tmpMaxPage = users.getTotalPage(); index ++ ; } renderText("ok"); //
	 * Page<User> users =
	 * 
	 * // User user = User.userDao.pa }
	 */

	@ActionKey("/aaxxxx")
	public void testxxxx() {
		made10("cc31ccddd44842d3829cbcca6b93d662", "英俊的职业玩家");
	}

	private void made10(String userCode, String userName) {
		Tickets tickets = new Tickets();
		tickets.set("tCode", UIDUtil.generate());
		String userMobile = "13800000000";
		String userTrueName = "真实姓名";
		try {
			userMobile = User.userDao.findByIdLoadColumns(userCode, "userCode,userMobile").getStr("userMobile");
			userMobile = CommonUtil.decryptUserMobile(userMobile);
			if (CommonUtil.isMobile(userMobile) == false) {
				userMobile = "13800000000";
			}
			userTrueName = UserInfo.userInfoDao.findByIdLoadColumns(userCode, "userCardName").getStr("userCardName");
		} catch (Exception e) {
			userMobile = "13800000000";
			userTrueName = "真实姓名";
		}
		tickets.set("userMobile", userMobile);
		tickets.set("userName", userName);
		tickets.set("userTrueName", userTrueName);
		tickets.set("userCode", userCode);
		tickets.set("tname", "10元现金券【10亿冲刺幸运有你】");
		tickets.set("expDate", "20160818");
		tickets.set("usedDateTime", "00000000000000");
		tickets.set("makeDateTime", DateUtil.getNowDateTime());
		tickets.set("tMac", "1111");
		tickets.set("makeSource", "B");
		tickets.set("makeSourceDesc", "活动");
		tickets.set("makeSourceUser", "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");

		JSONObject useExObj = new JSONObject();
		useExObj.put("amount", 100000);
		useExObj.put("rate", 0);
		useExObj.put("limit", 0);

		tickets.set("tstate", "A");
		tickets.set("amount", 1000);
		tickets.set("useEx", useExObj.toJSONString());
		tickets.set("rate", 0);
		tickets.set("ttype", "A");
		tickets.save();
	}

	@ActionKey("/bacthSendSMS")
	public void xxx(){
		StringBuffer sb = new StringBuffer("<table border='1'>");
		List<User> users = User.userDao.find("select userCode,userName,userMobile from t_user where userCode not in (select userCode from t_loan_info)");
		long sucCount = 0 ;
		long errCount = 0 ;
		for (int i = 0; i < users.size(); i++) {
			String userMobile = "";
			String userCode = "";
			String userName = "";
			try {
				User user = users.get(i);
				userMobile = user.getStr("userMobile");
				userMobile = CommonUtil.decryptUserMobile(userMobile);
				userName = user.getStr("userName");
				userCode = user.getStr("userCode");
				sb.append("<tr><td>").append(userCode).append("</td>").append("<td>").append(userName).append("</td>").append("<td>").append(userMobile).append("</td>");
				if( CommonUtil.isMobile(userMobile) == true ){
					String content = "【易融恒信】平台的发展，与您一直以来的支持和关注分不开，值此国庆佳节，在此祝您节日快乐！愿得您一份信任，收益是我们最真诚的回报！";
					/**
					 * 发送短信
					 * @param mobile
					 * @param content
					 * @return
					 */
						String url = "http://m.5c.com.cn/api/send/index.php";
						String getData = "username=yrhx&password=sms123456&apikey=ac24abc5ad9df58efbf9b1be603bf998&mobile=[mobile]&content=[content]".replace(
								"[mobile]", userMobile ).replace("[content]", content);
						String result = HttpRequestUtil.sendGet(url, getData);
						System.out.println( userMobile + "  "+content+" "+result);
						if(result.equals("success")){
							sb.append("<td>发送成功</td></tr>");
							sucCount ++ ;
						}else{
							sb.append("<td>发送失败</td></tr>");
							errCount ++ ;
						}
						System.out.println("Proccess:" + sucCount + "/" + (sucCount+errCount));
				}else{
					sb.append("<td>手机号非法</td></tr>");
				}
				
			} catch (Exception e) {
				sb.append("<td>手机号非法</td></tr>");
				continue;
			}
		}
		sb.append("<tr><td colspan='4'>发送成功：").append(sucCount).append("个用户，失败：").append(errCount).append("个用户</td></tr>");
		renderHtml(sb.append("</table>").toString());
	}

//	@ActionKey("/rechargeAward")
//	public void rechargeAward() {
//		String key = getPara("key","");
//		String minAmount = getPara("minAmount", "");
//		String maxAmount = getPara("maxAmount", "");
//		if( key.equals("fucku.com") == false ){
//			renderText("2333333");
//		}else {
//			if ("0".equals(maxAmount)) {
//				maxAmount = "";
//			} else {
//				maxAmount = " and totalAmount < " + maxAmount;
//			}
//			String sql = "SELECT userCode, userName,userMobile,totalAmount,"
//					+ " CASE WHEN totalAmount > 50000000 THEN FLOOR(totalAmount * 0.001)"
//					+ " WHEN totalAmount BETWEEN 20000000 AND 49999999 THEN FLOOR(totalAmount * 0.0008)"
//					+ " WHEN totalAmount BETWEEN 10000000 AND 19999999 then FLOOR(totalAmount * 0.0005)"
//					+ " WHEN totalAmount BETWEEN 5000000 AND 9999999 THEN FLOOR(totalAmount * 0.0003)"
//					+ " WHEN totalAmount BETWEEN 1000000 AND 4999999 THEN FLOOR(totalAmount * 0.0002)"
//					+ " ELSE 0 END"
//					+ " FROM (SELECT userCode payUserCode,(SUM(traceAmount) - (SELECT COALESCE (sum(traceAmount), 0) FROM t_funds_trace WHERE userCode = t1.userCode AND traceType = 'B' AND traceDateTime >= 20170501000000 AND traceDateTime <= 20170531235959)) totalAmount"
//					+ " FROM t_funds_trace t1 WHERE"
//					+ " traceDateTime >= 20170501000000 AND traceDateTime <= 20170531235959 AND (traceType = 'P' OR traceType = 'A' OR traceType = 'N' ) GROUP BY userCode) t_loan_temp "
//					+ " INNER JOIN t_user tu ON tu.userCode = t_loan_temp.payUserCode"
//					+ " WHERE totalAmount >= " + minAmount + maxAmount
//					+ " GROUP BY payUserCode"
//					+ " order by totalAmount desc";
////			String sql = "SELECT  userCode,userName,userMobile,totalAmount,FLOOR(totalAmount*"+awardApr+") tjiangli"
////					+ " from (SELECT payUserCode,sum(payAmount) totalAmount,loanDateTime "
////					+ "from t_loan_trace  where loanDateTime between 20170410000000 and 20170430235959"
////					+ " group by payUserCode order by loanDateTime) t_loan_temp"
////					+ " INNER JOIN t_user tu  on tu.userCode=t_loan_temp.payUserCode where "
////					+ "totalAmount>=" + minAmount + maxAmount + " group by payUserCode";
//			List<String> log = new ArrayList<String>();
//			List<Object[]> awardList = Db.query(sql);
//			for (int i = 0; i < awardList.size(); i++) {
//				String remark = "5月月度待收奖励[" + DateUtil.getNowDateTime() + "]";
//				if (fundsServiceV2.recharge(awardList.get(i)[0].toString(), Long.parseLong(awardList.get(i)[4].toString()),
//						0, remark, SysEnum.traceType.D.val()) != null) {
//					rechargeTraceService.saveRechargeTrace(null, UIDUtil.generate(),
//							Long.parseLong(awardList.get(i)[4].toString()), "B", remark, awardList.get(i)[0].toString(), "",
//							"", "", "B", "SYS", remark);
//					logger.info("[No"+i+"][" + awardList.get(i)[1].toString() + "]发放奖励金额[" + awardList.get(i)[4].toString() + "]成功");
//					log.add("[No"+i+"][" + awardList.get(i)[1].toString() + "]发放奖励金额[" + awardList.get(i)[4].toString() + "]成功\r");
//				}
//			}
//			renderText(log.toString());
//		}
//	}
	/**
	 * 批量开户用户信息转excel
	 * 
	 * */
	@ActionKey("/excel")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void excel() throws UnsupportedEncodingException{
		int pageNumber =getParaToInt("pageNumber");
		HttpServletResponse response = getResponse();
		response.setCharacterEncoding("utf-8");
		String output_extType = "application/vnd.ms-excel";
		String output_html=piliangzhuanzhang(pageNumber);
		String filename="PW10_"+DateUtil.getNowDate()+"_000"+pageNumber;
		filename= new String(filename.getBytes("utf-8"), "ISO_8859_1");
		response.setHeader("Content-Disposition", "attachment;filename="+filename+".xlsx");
		renderText(output_html, output_extType);
	}
	private String piliangkaihu(int pageNumber){
		int pageSize = 1500;
		String output_html = "<table border='1'>";
		output_html+="<tr><td><b>序号</b></td><td><b>客户姓名</b></td><td><b>身份证号码</b></td><td><b>手机号码</b></td><td><b>邮箱地址</b></td><td><b>开户行省市</b></td><td><b>开户行区县</b></td><td><b>开户行行别</b></td><td><b>开户行支行名称</b></td><td><b>户名</b></td><td><b>帐号</b></td><td><b>初始密码</b></td><td><b>备注</b></td></tr>";
		String select ="select trueName,bankNo,cardCity,bankName,cardid,t2.userMobile  ";
		String sqlfrom1="from t_banks_v2 t1 LEFT JOIN t_user t2 on t1.userCode=t2.userCode ";
		String sqlfrom2="where t1.userCode in (select userCode  from t_funds  where updateDate>20170101) ";
		String sqlfrom3="and bankName in ('中国工商银行','中国农业银行','中国银行','中国建设银行','交通银行','中信银行','光大银行','华夏银行','中国民生银行','广东发展银行','平安银行','招商银行','兴业银行','上海浦东发展银行','中国邮政储蓄银行','中国招商银行') ";
		String sqlfrom4="ORDER BY t2.lastLoginDateTime desc";
		String sqlfrom =sqlfrom1+sqlfrom2+sqlfrom3+sqlfrom4;
		Page<Funds> fundsList= Funds.fundsDao.paginate(pageNumber, pageSize, select, sqlfrom);
		List<Funds> fundlist=fundsList.getList();
		for(int i=0;i<fundlist.size();i++){
			Funds fund=fundlist.get(i);
			String mobile=null;
			String trueName=fund.getStr("trueName");
			String bankName=fund.getStr("bankName");
			String bankNo=fund.getStr("bankNo");
			String cardid=fund.getStr("cardid");
			String cardCity=fund.getStr("cardCity");
			try {
				cardid = CommonUtil.decryptUserCardId(cardid);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			int aa=0;
			String[] tt=null;
			if(cardCity!=null&&!"".equals(cardCity)){
				tt=cardCity.split("[|]");
				aa=tt.length;
			}
			int age=Integer.parseInt(cardid.substring(6, 10));
			int year=Integer.parseInt(DateUtil.getNowDate().substring(0, 4));
			if(null!=trueName&&!"".equals(trueName)&&aa>0&&cardid.length()==48&&year-age>=18){
			try {
				mobile = CommonUtil.decryptUserMobile(fund.getStr("userMobile"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			String loginPassward =mobile.substring(mobile.length()-6,mobile.length());
			output_html+="<tr><td>"+(i+1)+"</td>";
			output_html+="<td>"+trueName+"</td>";
			output_html+="<td>"+"hah"+cardid+"</td>";
			output_html+="<td>"+"hah"+mobile+"</td>";
			output_html+="<td></td>";
			
			
			if(tt[0].equals("重庆")||tt[0].equals("上海")||tt[0].equals("天津")){
				output_html+="<td>"+tt[0]+"市"+"</td>";
				output_html+="<td>"+tt[0]+"</td>";
			}else if(tt[0].equals("北京")){
				output_html+="<td>"+tt[0]+"市"+"</td>";
				output_html+="<td>"+tt[0]+"市"+"</td>";
			}else if(tt[0].equals("河北")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>石家庄</td>";
			}else if(tt[0].equals("山西")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>太原</td>";
			}else if(tt[0].equals("内蒙古")){
				output_html+="<td>"+tt[0]+"自治区"+"</td>";
				output_html+="<td>呼和浩特市</td>";
			}else if(tt[0].equals("辽宁")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>沈阳</td>";
			}else if(tt[0].equals("黑龙江")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>哈尔滨</td>";
			}else if(tt[0].equals("吉林")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>长春</td>";
			}else if(tt[0].equals("江苏")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>南京</td>";
			}else if(tt[0].equals("浙江")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>杭州</td>";
			}else if(tt[0].equals("安徽")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>合肥</td>";
			}else if(tt[0].equals("福建")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>福州</td>";
			}else if(tt[0].equals("江西")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>南昌</td>";
			}else if(tt[0].equals("山东")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>济南</td>";
			}else if(tt[0].equals("河南")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>郑州</td>";
			}else if(tt[0].equals("湖北")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>武汉市</td>";
			}else if(tt[0].equals("湖南")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>长沙</td>";
			}else if(tt[0].equals("广东")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>广州</td>";
			}else if(tt[0].equals("广西")){
				output_html+="<td>"+tt[0]+"壮族自治区"+"</td>";
				output_html+="<td>南宁</td>";
			}else if(tt[0].equals("海南")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>海口</td>";
			}else if(tt[0].equals("四川")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>成都</td>";
			}else if(tt[0].equals("贵州")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>贵阳</td>";
			}else if(tt[0].equals("云南")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>昆明</td>";
			}else if(tt[0].equals("西藏")){
				output_html+="<td>"+tt[0]+"自治区"+"</td>";
				output_html+="<td>拉萨</td>";
			}else if(tt[0].equals("陕西")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>西安</td>";
			}else if(tt[0].equals("甘肃")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>兰州</td>";
			}else if(tt[0].equals("宁夏")){
				output_html+="<td>"+tt[0]+"回族自治区"+"</td>";
				output_html+="<td>银川</td>";
			}else if(tt[0].equals("青海")){
				output_html+="<td>"+tt[0]+"省"+"</td>";
				output_html+="<td>西宁</td>";
			}else if(tt[0].equals("新疆")){
				output_html+="<td>"+tt[0]+"维吾尔自治区"+"</td>";
				output_html+="<td>乌鲁木齐</td>";
			}else{
				if(aa>=2){
					output_html+="<td>"+tt[0]+"</td>";
					output_html+="<td>"+tt[1]+"</td>";
				}
			}
			if("光大银行".equals(bankName)){
				bankName="中国"+bankName;
			}else if("中国招商银行".equals(bankName)){
				bankName="招商银行";
			}else if("平安银行".equals(bankName)){
				bankName="平安银行股份有限公司";
			}else if("中国邮政储蓄银行".equals(bankName)){
				bankName="中国邮政储蓄银行股份有限公司";
			}
			output_html+="<td>"+bankName+"</td>";
			if(2==aa||1==aa){
				output_html+="<td></td>";
			}else if(3==aa){
				output_html+="<td>"+tt[2]+"</td>";
			}
			output_html+="<td>"+trueName+"</td>";
			output_html+="<td>"+"hah"+bankNo+"</td>";
			output_html+="<td>"+"hah"+loginPassward+"</td>";
			output_html+="<td></td>";
			output_html+="</tr>";
		}}
		output_html += "</table>";
		return output_html;
	}
	private String piliangzhuanzhang(int pageNumber){
		int pageSize = 1500;
		String output_html = "<table border='1'>";
		output_html+="<tr><td><b>序号</b></td><td><b>付款方登录名</b></td><td><b>付款方中文名称</b></td><td><b>付款资金来自冻结</b></td><td><b>收款方登录名</b></td><td><b>收款方中文名称</b></td><td><b>收款后立即冻结</b></td><td><b>合同号</b></td><td><b>交易金额</b></td><td><b>备注信息</b></td><td><b>预授权合同号</b></td><td><b>交易流水</b></td></tr>";
		String select ="select t4.trueName,t3.loginId,t3.avBalance,t3.frozeBalance ";
		String sqlfrom1="from (select t1.userCode, t1.loginId,t2.avBalance,t2.frozeBalance ";
		String sqlfrom2="from t_user  t1 LEFT JOIN t_funds t2 on t1.userCode=t2.userCode ";
		String sqlfrom3="where t1.loginId is not null and t1.loginId != '') t3 ";
		String sqlfrom4="LEFT JOIN t_banks_v2 t4 on t3.userCode=t4.userCode";
		String sqlfrom =sqlfrom1+sqlfrom2+sqlfrom3+sqlfrom4;
		Page<User> usersList= User.userDao.paginate(pageNumber, pageSize, select, sqlfrom);
		List<User> userlist=usersList.getList();
		int j=1;
		for(int i=0;i<userlist.size();i++){
			User user=userlist.get(i);
			String trueName=user.getStr("trueName");
			String loginId=user.getStr("loginId");
			try {
				loginId=CommonUtil.decryptUserMobile(loginId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Long avBalance=user.getLong("avBalance");
			Long frozeBalance=user.getLong("frozeBalance");
			Long allBanlance =avBalance+frozeBalance;
			boolean aa=fuiouTraceService.isfuiouAcount(user);
			if(allBanlance>0&&aa==true){
				output_html+="<tr><td>"+j+"</td>";
				j++;
				output_html+="<td>"+CommonUtil.fUIOUACCOUNT+"</td>";
				output_html+="<td>恒丰02</td>";
				output_html+="<td>否</td>";
				output_html+="<td>"+loginId+"</td>";
				output_html+="<td>"+trueName+"</td>";
				output_html+="<td>否</td><td></td>";
				output_html+="<td>"+allBanlance+"</td>";
				output_html+="<td></td><td></td><td></td></tr>";
			}
	}
		output_html += "</table>";
		return output_html;
	}
	/**
	 * 批量开户 转账
	 * */
//	@ActionKey("/piliangkaihu")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public void creatFuiouUsers(){
//		int num=getParaToInt("num");
//		int size=getParaToInt("size");
//		String select ="select trueName,bankNo,cardCity,bankName,cardid,t2.userMobile,t1.userCode ";
//		String sqlfrom1="from t_banks_v2 t1 LEFT JOIN t_user t2 on t1.userCode=t2.userCode ";
//		String sqlfrom2="where t1.userCode in (select userCode  from t_funds  where updateDate>20170101 and beRecyPrincipal4loan=0  and (avBalance != 0 or frozeBalance != 0 or beRecyPrincipal != 0)) ";
//		String sqlfrom3="and bankName in ('中国工商银行','中国农业银行','中国银行','中国建设银行','交通银行','中信银行','光大银行','华夏银行','中国民生银行','广东发展银行','平安银行','招商银行','兴业银行','上海浦东发展银行','中国邮政储蓄银行','中国招商银行') and not isnull(cardCity) ";
//		String sqlfrom4="ORDER BY t2.lastLoginDateTime desc";
//		String sqlfrom =sqlfrom1+sqlfrom2+sqlfrom3+sqlfrom4;
//		Page<Funds> fundsList= Funds.fundsDao.paginate(num, size, select, sqlfrom);
//		List<Funds> fundlist=fundsList.getList();
//		User wzuser=userService.findByMobile(CommonUtil.OUTCUSTNO);
//		for(int i=0;i<fundlist.size();i++){
//			Funds fund=fundlist.get(i);
//			String mobile=null;
//			String userCode=fund.getStr("userCode");
//			UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
//			String trueName=userInfo.getStr("userCardName");
//			String bankName=fund.getStr("bankName");
//			String bankNo=fund.getStr("bankNo");
//			String cardid=userInfo.getStr("userCardId");
//			String cardCity=fund.getStr("cardCity");
//			try {
//				cardid = CommonUtil.decryptUserCardId(cardid);
//			} catch (Exception e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			int aa=0;
//			String[] tt=null;
//			if(cardCity!=null&&!"".equals(cardCity)){
//				tt=cardCity.split("[|]");
//				aa=tt.length;
//			}
//			int age=Integer.parseInt(cardid.substring(6, 10));
//			int year=Integer.parseInt(DateUtil.getNowDate().substring(0, 4));
//			if(null!=trueName&&!"".equals(trueName)&&aa>0&&cardid.length()>0&&year-age>=18){
//			try {
//				mobile = CommonUtil.decryptUserMobile(fund.getStr("userMobile"));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if("13888888888".equals(mobile)||"13378542333".equals(mobile)||"13097512021".equals(mobile)||"18976145111".equals(mobile)){
//				continue;
//			}
//			String paypwd =mobile.substring(mobile.length()-6,mobile.length());
//			String[] city=CommonUtil.checkCity(tt[0]);
//			String cityCode=city[1];
//			String bankCode=CommonUtil.checkBankCode(bankName);
//			//调用存管接口
//			CommonRspData commonRspData=null;
//			String traceCode=CommonUtil.genMchntSsn();
//			RegReqData regReqData=new RegReqData();
//			regReqData.setVer(CommonUtil.VER);
//			regReqData.setMchnt_cd(CommonUtil.MCHNT_CD);//商户号
//			regReqData.setMchnt_txn_ssn(traceCode);//流水号
//			regReqData.setCust_nm(trueName);//用户名
//			regReqData.setCertif_tp("0");//证件类型
//			regReqData.setCertif_id(cardid);//身份证号
//			regReqData.setMobile_no(mobile);//手机号
//			regReqData.setCity_id(cityCode);//银行卡地区号
//			regReqData.setParent_bank_id(bankCode);//银行代码
//			regReqData.setCapAcntNo(bankNo);//银行卡号
//			try {
//				regReqData.setLpassword(CommonUtil.encryptPasswd(paypwd));//登录密码密文
//			} catch (UnsupportedEncodingException e1) {
//				e1.printStackTrace();
//			}
//			try {
//				commonRspData=FuiouService.reg(regReqData);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if("0000".equals(commonRspData.getResp_code())){
//				//存零时信息
//				TmpUserReg tmpUserReg = new TmpUserReg();
//				tmpUserReg.set("userCode", userCode);
//				tmpUserReg.set("mobile", mobile);
//				tmpUserReg.set("userName", trueName);
//				tmpUserReg.set("BankNo", bankNo);
//				tmpUserReg.set("bankCode", bankCode);
//				tmpUserReg.set("cityCode", cityCode);
//				tmpUserReg.set("ssn", traceCode);
//				tmpUserReg.set("issucces", "A");
//				tmpUserReg.set("remark", "已开户还未转账");
//				tmpUserReg.save();
//				//开户成功  进行转账
//				User user=userService.findById(userCode);
//				user.set("loginId", user.get("userMobile"));
//				if(user.update()){
//				Funds funds=fundsServiceV2.findById(userCode);
//				Long avBalance=funds.getLong("avBalance");
//				Long frozeBalance=funds.getLong("frozeBalance");
//				Long allBanlance =avBalance+frozeBalance;
//				if(allBanlance>0){
//					CommonRspData commm=fuiouTraceService.gorefund(allBanlance,user, FuiouTraceType.X);
//					if("0000".equals(commm.getResp_code())){
//						if(frozeBalance>0){
//							CommonRspData cc=fuiouTraceService.freeze(user, frozeBalance);
//							if(!"0000".equals(cc.getResp_code())){
//								TmpUserReg tmpUserReg2 =TmpUserReg.tmpuserregDao.findById(userCode);
//								tmpUserReg2.set("amount", avBalance);
//								tmpUserReg2.set("freeAmount", frozeBalance);
//								tmpUserReg2.set("issucces", "E");
//								tmpUserReg2.set("remark", "已开户冻结失败");
//								tmpUserReg2.update();
//							}else{
//								TmpUserReg tmpUserReg2 =TmpUserReg.tmpuserregDao.findById(userCode);
//								tmpUserReg2.set("amount", avBalance);
//								tmpUserReg2.set("freeAmount", frozeBalance);
//								tmpUserReg2.set("issucces", "B");
//								tmpUserReg2.set("remark", "已开户已转账");
//								tmpUserReg2.update();
//							}
//						}else{
//							TmpUserReg tmpUserReg2 =TmpUserReg.tmpuserregDao.findById(userCode);
//							tmpUserReg2.set("amount", avBalance);
//							tmpUserReg2.set("freeAmount", frozeBalance);
//							tmpUserReg2.set("issucces", "B");
//							tmpUserReg2.set("remark", "已开户已转账");
//							tmpUserReg2.update();
//						}
//						
//					}else{
//						TmpUserReg tmpUserReg2 =TmpUserReg.tmpuserregDao.findById(userCode);
//						tmpUserReg2.set("amount", avBalance);
//						tmpUserReg2.set("freeAmount", frozeBalance);
//						tmpUserReg2.set("issucces", "D");
//						tmpUserReg2.set("remark", "已开户转账失败");
//						tmpUserReg2.update();
//					}
//				}else{
//					TmpUserReg tmpUserReg2 =TmpUserReg.tmpuserregDao.findById(userCode);
//					tmpUserReg2.set("amount", avBalance);
//					tmpUserReg2.set("freeAmount", frozeBalance);
//					tmpUserReg2.set("issucces", "F");
//					tmpUserReg2.set("remark", "已开户账户无资金");
//					tmpUserReg2.update();
//				}
//				}
//			}else{
//				TmpUserReg tmpUserReg = new TmpUserReg();
//				tmpUserReg.set("userCode", userCode);
//				tmpUserReg.set("mobile", mobile);
//				tmpUserReg.set("userName", trueName);
//				tmpUserReg.set("BankNo", bankNo);
//				tmpUserReg.set("bankCode", bankCode);
//				tmpUserReg.set("cityCode", cityCode);
//				tmpUserReg.set("issucces", "C");
//				tmpUserReg.set("ssn", traceCode);
//				tmpUserReg.set("remark", "开户失败");
//				tmpUserReg.save();
//			}
//		}
//	}
//		}

	/**
	 * 重置10月活动每日奖品发放数量 ws 20170928 unused
	 * */
//	@ActionKey("/resetprizenum")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public Message resetprizenum(){
//		int date=Integer.parseInt(DateUtil.getNowDate());
//		if(date<20171001||date>20171031){
//			return error("01", "不在活动时间内", false);
//		}
//		String key = getPara("key", "");
//		if(key.equals("temehaidemeitiangeinichongzhi")){
//			String activeCode="88563a221ed51a4ef8ef0cfbf1057578";
//			List<Prizes> prizes=prizesService.findPrizesByActive(activeCode);
//			for(int i=0;i<prizes.size();i++){
//				Prizes prizes2=prizes.get(i);
//				prizesService.updatePrizeCount(prizes2.getStr("prizeCode"),0);
//			}
//		}
//		return succ("重置奖券数量完成", "想拿大奖多氪金");
//	}
	
	/**
	 * 为每位用户创建邀请码
	 * */
	@ActionKey("/CreateInviteCode")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message CreateInviteCode(){
		List<User> users = User.userDao.find("select * from t_user where inviteCode is null ");
		for(int i=0;i<users.size();i++){
			String inviteCode = CommonUtil.getVerifiCode(6);
			if(StringUtil.isBlank(inviteCode)){
				return error("01", "邀请码生成失败", "");
			}
			User user2 = User.userDao.findFirst("select * from t_user where inviteCode = ?", inviteCode);
			if(null != user2){
				i--;
			}else{
				System.out.println("第"+i+"条");
				User user=users.get(i);
				user.set("inviteCode", inviteCode);
				user.update();
			}
		}
		return succ("OK", "为每位用户创建邀请码完成");
	}
	
	/**
	 * 生成用户旧会员等级
	 * @return
	 */
	@ActionKey("/beforeVip")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message beforeVip(){
		List<User> users = User.userDao.find("select * from t_user");
		for (int i = 0; i < users.size(); i++) {
			User user = users.get(i);
			long userScore = user.getLong("userScore");
			if(userScore < 30000){//少尉
				user.set("beforeVip", 1);
			}else if(userScore<100000){//中尉
				user.set("beforeVip", 2);
			}else if(userScore<300000){//上尉
				user.set("beforeVip", 3);
			}else if(userScore<800000){//少校
				user.set("beforeVip", 4);
			}else if(userScore<1600000){//中校
				user.set("beforeVip", 5);
			}else if(userScore<6000000){//上校
				Integer vipLevel = user.getInt("vipLevel");//新等级
				int beforeVip = 6;//上校
				if(vipLevel < 5){//低于黄金（待收少于10W）
					beforeVip = 5;//中校
				}
				user.set("beforeVip", beforeVip);
			}else{//将军
				Integer vipLevel = user.getInt("vipLevel");//新等级
				int beforeVip = 7;//将军
				if(vipLevel < 5){//低于黄金（待收少于10W）
					beforeVip = 5;//中校
				}else if(vipLevel < 6){
					beforeVip = 6;//上校
				}
				user.set("beforeVip", beforeVip);
			}
			user.update();
			System.out.println("第"+(i+1)+"/"+users.size()+"条更新完成");
		}
		return succ("ok", "用户旧会员等级beforeVip添加完成");
	}

	
	/**
	 * 用户资金同步
	 * */
	@ActionKey("/syncFunds")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void syncFunds(){
		List<Funds> fundsList = Funds.fundsDao.find("select t1.userCode, t1.avBalance,t1.frozeBalance,t2.loginId from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode where t1.avBalance+t1.frozeBalance>300 and t2.userType is null and t2.loginId is not null ");
		int num = fundsList.size();
		User wzuser = userService.find4mobile(CommonUtil.OUTCUSTNO);
		int i = 0 ;
		for(Funds fund:fundsList){
			i++;
			String userCode = fund.getStr("userCode");
			User user = userService.findById(userCode);
			if(!FuiouController.isFuiouAccount(user)){
				logger.log(Level.INFO, "用户编号："+userCode+"第"+num+"/"+i+"未查询到此用户存管信息");
				continue;
			};
			QueryBalanceResultData QueryBalanceResultData = fuiouTraceService.BalanceFunds(user);
			//富友可用余额
			long caBanlance = Long.parseLong(QueryBalanceResultData.getCa_balance());
			//富友冻结余额
			long cfBanlance = Long.parseLong(QueryBalanceResultData.getCf_balance());
			FuiouController fuiouController = new FuiouController();
			//解冻
			if(cfBanlance>0){
				fuiouTraceService.unFreeFunds(user, cfBanlance);
			}
			if(caBanlance+cfBanlance>300){
				fuiouController.transferBu(300L, SysEnum.FuiouTraceType.I, user, wzuser, "", "委托提现手续费");
				logger.log(Level.INFO, "用户编号："+userCode+"第"+num+"/"+i+"委托提现手续费获取3块");
			}else{
				logger.log(Level.INFO, "用户编号："+userCode+"第"+num+"/"+i+"存管余额不足3块");
			}
			fund.set("avBalance", caBanlance+cfBanlance);
			fund.set("frozeBalance", 0);
			fund.update();
		}
		renderNull();
	}
	
	
	/**
	 * 批次取消债权转让 WJW
	 * @return
	 */
	@ActionKey("/batchcancelTransfer")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message batchcancelTransfer(){
		String transCodeErr = "";
		String sql = "select * from t_loan_transfer where transState = 'A'";
		List<LoanTransfer> loanTransfers = LoanTransfer.loanTransferDao.find(sql);
		for (LoanTransfer loanTransfer : loanTransfers) {
			String transCode = loanTransfer.getStr("transCode");
			String cancelTransfer = cancelTransfer(transCode);
			if(!StringUtil.isBlank(cancelTransfer)){
				transCodeErr += cancelTransfer + ",";
			}
		}
		return succ("", transCodeErr);
	}
	
	/**
	 * 单笔取消债权转让
	 * @return
	 */
	public String cancelTransfer(String transferCode){
		if(StringUtil.isBlank(transferCode)){
			return transferCode;
		}
		
		//获取用户标识
		//String userCode = getUserCode();
		
		LoanTransfer loanTransfer = loanTransferService.findById(transferCode);
		
		if(null == loanTransfer){
			return transferCode;
		}
		
		String userCode = loanTransfer.getStr("payUserCode");
		if(StringUtil.isBlank(userCode)){
			return transferCode;
		}
		
		//验证债权是否存在   是否被转让
		/*LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst(
				"select traceCode, transState,transScore from t_loan_transfer where transCode = ? and payUserCode = ? and transState = 'A' ",
				transferCode , userCode);*/
		
		//取消债权转让
		LoanTransfer cancelLoanTransfer = new LoanTransfer();
		cancelLoanTransfer.set("transCode", transferCode);
		cancelLoanTransfer.set("transState", "C");
		boolean updateResult = false;
		try{
			updateResult = cancelLoanTransfer.update();
		}catch(Exception e){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消失败", e);
		}
		
		if( updateResult == false ){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消失败",null);
			return transferCode;
		}
		//修改标书债权状态
		boolean updateTraceState = false;
		if(loanTransferService.vilidateIsTransfer(loanTransfer.getStr("traceCode"))){
			updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"A", "B");
		}else{
			updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
					"A", "C");
		}
		if(updateTraceState == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.TRANSFER, "债权取消成功,但标书债权状态修改异常", null);
			return transferCode;
		}
		
		//回滚积分
		int transScore = loanTransfer.getInt("transScore");
		fundsServiceV2.doPoints(userCode, 0 , transScore , "取消债权转让,返回冻结积分!");
		
		BIZ_LOG_INFO(userCode, BIZ_TYPE.TRANSFER, "债权取消成功");
		
		return null;
	}

	/**
	 * 红包发放隔日撤销 WJW
	 */
	@ActionKey("/voucherPayDelayCancelTest")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void voucherPayDelayCancel(){
		String type = getPara("type");
		if(StringUtil.isBlank(type)){
			return;
		}
		Map<String, Long> hashMap = new HashMap<String,Long>();
		
		String[] loanCodes = null;
		
		if("1".equals(type)){
			String[] loanCodes1 = {"5d1658c911de4eae9a64b51002b4cad4"};
			loanCodes = loanCodes1;
		}else {
			String[] loanCodes1 = {"8af196ebd464465986d886f480124ca0",
								"f87cdf234a884970a7ed9cee02196993",
								"ef1e0254d84b481992eacbfee4eb3385",
								"aa2b1571eec944649406576b9a31c84f",
								"5381f832949b46fe805427401e4a2f25",
								"f9f59b1aaa1e472b89641fbcf07d5c70",
								"4563e33055994c02a36cb6ad6e4f9bed",
								"44650a1cad0f4774bca4a5ef43d6929c",
								"a952c906e4fd40ab8811f220fbcb231a",
								"247dd7589ce94e3ea39da7ab9eef774a",
								"9f55cde3a0ca4355baee86398dab8d55",
								"22759d610ab2483cb9b3cf73ce25e1bd",
								"c0486fcd3e184bd39f97233ebfe5a958"};
			loanCodes = loanCodes1;
		}
		
		int j = 0;
		int num = loanCodes.length;
		for (String loanCode : loanCodes) {
			List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
			j++;
			System.out.println("loanCode处理:"+j+"/"+num);
			for (LoanTrace loanTrace : loanTraces) {
				String traceCode = loanTrace.getStr("traceCode");
				String payUserCode = loanTrace.getStr("payUserCode");
				User user = userService.findById(payUserCode);
				String jxAccountId = user.getStr("jxAccountId");
				if(StringUtil.isBlank(jxAccountId)){
					continue;
				}
				List<JXTrace> jxTraces = JXTrace.jxTraceDao.find("select * from t_jx_trace where txCode='voucherPay' and requestMessage like '%"+jxAccountId+"%' and requestMessage like '%"+traceCode+"%'");
				if(jxTraces != null && jxTraces.size() == 4){
					for (int i = 0; i < jxTraces.size(); i++) {
						if(i == 0 || i == 1){
							continue;
						}
						JXTrace jxTrace = jxTraces.get(i);
						String jxTraceCode = jxTrace.getStr("jxTraceCode");
						String requestMessage = jxTrace.getStr("requestMessage");
						JSONObject parseObject = JSONObject.parseObject(requestMessage);
						String accountId = parseObject.getString("accountId");
						long txAmount = StringUtil.getMoneyCent(parseObject.getString("txAmount"));
						String forAccountId = parseObject.getString("forAccountId");
						String orgTxDate = parseObject.getString("txDate");
						String orgTxTime = parseObject.getString("txTime");
						String orgSeqNo = parseObject.getString("seqNo");
						Map<String, String> voucherPayDelayCancel = JXController.voucherPayDelayCancel(accountId, txAmount, forAccountId, orgTxDate, orgTxTime, orgSeqNo, "", "");
						if(voucherPayDelayCancel == null || !"00000000".equals(voucherPayDelayCancel.get("retCode"))){
							System.out.println("jxTraceCode:"+jxTraceCode+"撤销失败");
						}
					}
				}
				
				//资金同步查询
				Map<String, String> balanceQuery = JXQueryController.balanceQuery(jxAccountId);
				if(StringUtil.isBlank(balanceQuery.get("availBal"))){
					System.out.println("jxAccountId:"+jxAccountId+"查询失败");
					continue;
				}
				double availBal = Double.parseDouble(balanceQuery.get("availBal"));
				
				hashMap.put(payUserCode, (long) (availBal * 100));
				
			}
		}
		
		for(Map.Entry<String, Long> entry:hashMap.entrySet()){
			//添加流水
			fundsServiceV2.doAvBalance4biz(entry.getKey(), entry.getValue(), 0 , traceType.INCOME, fundsType.J , "2018-05-21至2018-05-28日回款本金利息");
		}
	}
	

//	@ActionKey("/syncAllDepositAccount")
//	@Before({PkMsgInterceptor.class})
//	public Message syncAllDepositAccount() {
//		int limit = getParaToInt("limit", 1);
//		List<User> lstUser = User.userDao.find("select * from t_user where isnull(userType) "
//				+ "and userCode not in (SELECT loanUserCode FROM t_loan_apply GROUP BY loanUserCode) "
//				+ "and userCode not in (select payUserCode from t_loan_trace where loanCode in (select loanCode from t_settlement_early where earlyDate BETWEEN 20180521 AND 20180528 and estatus in ('A', 'C'))) "
//				+ "and not isnull(jxAccountId) "
//				+ "limit 0, ?", limit);
//		for (int i = 0; i < lstUser.size(); i++) {
//			User user = lstUser.get(i);
//			String userCode = user.getStr("userCode");
//			String userName = user.getStr("userName");
//			String jxAccountId = user.getStr("jxAccountId");
//			if (!StringUtil.isBlank(jxAccountId)) {
//				Map<String, String> queryResult = JXQueryController.balanceQuery(jxAccountId);
//				Long availBal = StringUtil.getMoneyCent(queryResult.get("availBal"));
//				if (availBal != 0) {
//					System.out.println("更新用户["+userCode+"]["+userName+"]可用余额:" + availBal);
//					fundsServiceV2.doAvBalance4biz(userCode, availBal, 0 , traceType.INCOME, fundsType.J , "2018-05-21至2018-05-28日回款本金利息");
//				}
//			}
//		}
//		return succ("done...", null);
//	}
	
	/**
	 * 批次放款补漏	WJW
	 */
	@ActionKey("/batchLendPayTest")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void batchLendPayTest(){
		String[] traceCodes1 = {
				"34645c553d8d4d31b54e723b9670bd3b",		
				"57ca405dd0ac4b88b29ffdeab7196808",		
				"c7fb682712cc4ec398902d00a2bee7e1",		
				"943f1f5f07744d3db16fb8fbe4367561",		
				"270d814b891240c0b3f67528cd30fed2",		
				"6826356297ed44b7ab39847e1e7da568",		
				"4adae411f7f3492fb7b973cf95c43ebf",		
				"e2c0cf6fa21a449d89cb78c7a511e76d",		
				"59199a697e00444db48651d27bc161a0",		
				"94c74faf5bc94e2885233a84003e8c3f",		
				"d6dc81fb65b1441b981947123729e616",		
				"730ab0a2a6a849d7b085bc20e8ea375f",		
				"101a7dd10f334ef09854b909bd8b14b7",		
				"8dc54c7c4bd54aeb95330c121f5f5a43",		
				"7412be3c4614499bb93c11a735282d77"					
		};
		
		JSONArray jsonArray = new JSONArray();
		long sumTxAmount = 0;
		String loanCode1 = "de3eeaf51b08469eab53d56e2d092eff";
		String type =getPara("type","1");
		String loanCode = "";
		String[] traceCodes = null;
		if("1".equals(type)){
			loanCode = loanCode1;
			traceCodes = traceCodes1;
		}else {
		}
		List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
		for (LoanTrace loanTrace : loanTraces) {
			String traceCode = loanTrace.getStr("traceCode");
			if(!Arrays.asList(traceCodes).contains(traceCode)){
				String productId = loanTrace.getStr("loanCode");
				String authCode = loanTrace.getStr("authCode");
				String payUserCode = loanTrace.getStr("payUserCode");
				User payUser = userService.findById(payUserCode);
				String accountId = payUser.getStr("jxAccountId");
				String loanUserCode = loanTrace.getStr("loanUserCode");
				User loanUser = userService.findById(loanUserCode);
				String forAccountId = loanUser.getStr("jxAccountId");
				long txAmount = loanTrace.getLong("payAmount");
				
				sumTxAmount += txAmount;
				
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("accountId", accountId);//投资人电子账号
				jsonObject.put("txAmount", txAmount);//交易金额(投标金额)
				jsonObject.put("forAccountId", forAccountId);//融资人账户
				jsonObject.put("productId", productId);//标号
				jsonObject.put("authCode", authCode);
				jsonArray.add(jsonObject);
			}
		}

		int batchNoReq = jxTraceService.batchNoByToday();//批次号
		Map<String, String> batchLendPay = JXController.batchLendPay(batchNoReq, sumTxAmount, jsonArray.toJSONString());
		boolean isReceived = "success".equals(batchLendPay.get("received"));
		System.out.println("[批次号:"+batchNoReq+"批次放款请求发送"+(isReceived?"成功":"失败")+"...]");
	}
	
	/**
	 * 投标申请撤销 WJW
	 * @return
	 */
	@ActionKey("/bidCancel")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message bidCancel(){
		String jxTraceCode = getPara("jxTraceCode");
		if(StringUtil.isBlank(jxTraceCode)){
			return error("01","jxTraceCode为空", null);
		}
		JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
		String txCode = jxTrace.getStr("txCode");
		String retCode = jxTrace.getStr("retCode");
		if("00000000".equals(retCode) && ("bidAutoApply".equals(txCode) || "bidApply".equals(txCode))){
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String accountId = parseObject.getString("accountId");
			String orgOrderId = parseObject.getString("orderId");
			String productId = parseObject.getString("productId");
			String txAmount = parseObject.getString("txAmount");
			Map<String, String> bidCancel = JXController.bidCancel(accountId, txAmount, productId, orgOrderId, getResponse());
			return succ("", "00000000".equals(bidCancel.get("retCode"))?"撤销成功":"撤销失败");
		}
		return succ("", "撤销失败");
	}
	
//	/**
//	 * 标148961批次还款(临时) WJW
//	 * @return
//	 */
//	@ActionKey("/batchRepayTest")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public Message batchRepayTest(){
//		String loanCode = "6acdb3b047354c38b0303bab8c25a511";
//		doTransfer(loanCode);//检查未处理的债权
//		List<LoanTrace> traces = loanTraceService.findLoanTraceByJieSuan(loanCode);
//		LoanInfo loan = loanInfoService.findById(loanCode);
//		String loanUserCode = loan.getStr("userCode");
//		User loanUser = userService.findById(loanUserCode);
//		String accountId = loanUser.getStr("jxAccountId");//借款人电子账号
//		
//		long sumTxAmount = 0;
//		JSONArray jsonArray = new JSONArray();
//		SettlementController settlementController = new SettlementController();
//		for (LoanTrace loanTrace : traces) {
//			long[] prepaymentAmount = settlementController.prepaymentAmount(loan, loanTrace);//提前还款资金结算
//			long txAmount = prepaymentAmount[0];//还款本金
//			long intAmount = prepaymentAmount[1];//还款利息
//			long txFeeIn = prepaymentAmount[2];//投资人手续费
//			
//			String payUserCode = loanTrace.getStr("payUserCode");//投标人userCode
//			User user = userService.findById(payUserCode);//投标人
//			String forAccountId = user.getStr("jxAccountId");//投资人电子账号
//			String authCode = loanTrace.getStr("authCode");//授权码
//			String traceCode = loanTrace.getStr("traceCode"); 
//			//发生过债转
//			if(loanTransferService.vilidateIsTransfer(traceCode)){
//				List<LoanTransfer> loanTransfers = loanTransferService.queryLoanTransferByTraceCode(traceCode, "B");
//				String transferAuthCode = loanTransfers.get(loanTransfers.size() - 1).getStr("authCode");//最后一债转authCode
//				if(!StringUtil.isBlank(transferAuthCode)){
//					authCode = transferAuthCode;
//				}
//			}
//			
//			sumTxAmount += txAmount;
//			
//			JSONObject jsonObject = new JSONObject();
//			jsonObject.put("accountId", accountId);
//			jsonObject.put("txAmount", txAmount);
//			
//			//获取用户目前缴费授权开通状态,进行手续费操作处理
//			Long paymentAuthPageState = 0l;
//			JSONObject jsonObjectState = jxTraceService.paymentAuthPageState(forAccountId);
//			if(jsonObjectState != null && "1".equals(jsonObjectState.getString("type"))){
//				paymentAuthPageState = jsonObjectState.getLong("amount");
//			}
//			//目前缴费授权开通状态不能支付手续费
//			if(paymentAuthPageState == null || paymentAuthPageState < txFeeIn){
//				jsonObject.put("intAmount", intAmount-txFeeIn);
//				
//				//给坑爹的借款人发还款利息红包,让他能够正常还款
//				try {
//					if(intAmount - txFeeIn > 0){
//						JXController.voucherPay(JXService.RED_ENVELOPES, intAmount - txFeeIn, accountId, "1", "还款垫付利息["+loanTrace.getStr("traceCode")+"]");
//					}
//				} catch (Exception e) {
//					System.out.println("红包发放利息异常,traceCode:"+loanTrace.getStr("traceCode"));
//				}
//			}else {
//				jsonObject.put("intAmount", intAmount);
//				jsonObject.put("txFeeIn", txFeeIn);
//				
//				//给坑爹的借款人发还款利息红包,让他能够正常还款
//				try {
//					if(intAmount > 0){
//						JXController.voucherPay(JXService.RED_ENVELOPES, intAmount, accountId, "1", "还款垫付利息["+loanTrace.getStr("traceCode")+"]");
//					}
//				} catch (Exception e) {
//					System.out.println("红包发放利息异常,traceCode:"+loanTrace.getStr("traceCode"));
//				}
//			}
//			
//			jsonObject.put("forAccountId", forAccountId);
//			jsonObject.put("productId", loanCode);
//			jsonObject.put("authCode", authCode);
//			jsonArray.add(jsonObject);
//			settlementController.loanTraceAdvanceSingle(loan, loanTrace,3);//处理本地单条提前还款流水
//		}
//		
//		if(jsonArray.size() < 1){
//			System.out.println("loanCode["+loanCode+"]jsonArray为空");
//			return error("", "loanCode["+loanCode+"]jsonArray为空", null);
//		}
//		
//		int batchNo = jxTraceService.batchNoByToday();//批次号
//		String retNotifyURL = CommonUtil.NIUX_URL+"/notifyURL";//业务处理回调地址(仅更新响应报文)
//		Map<String, String> batchRepay = JXController.batchRepay(batchNo, sumTxAmount, retNotifyURL, jsonArray.toJSONString());
//		
//		try {
//			doLoanInfo4adv(loan);//更新标相关事物
//		} catch (Exception e) {
//			System.out.println("结算标loanInfo异常,loanCode:["+loanCode+"]");
//		}
//		
//		SettlementEarlyService settlementEarlyService = new SettlementEarlyService();
//		SettlementEarly se = settlementEarlyService.findById(loanCode);
//		if(se != null){
//			se.set("earlyDate", DateUtil.getNowDate());
//			se.set("settlementDate", DateUtil.getNowDate());
//			se.set("settlementTime", DateUtil.getNowTime());
//			se.set("estatus", "C");
//			se.update();
//		}
//		return succ("批次还款发送"+("success".equals(batchRepay.get("received"))?"成功":"失败"), null);
//	}
//	
//	/**
//	 * 	检查债权转让，如果有结算当天的债权，直接取消债权
//	 * 
//	 */
//	private void doTransfer(String loanCode){
//		if( StringUtil.isBlank(loanCode) == true ){
//			//取消所有当天结算的债权
//			String nowDate = DateUtil.getNowDate() ;
//			Db.update("update t_loan_transfer set transState='C' where transState='A' and nextRecyDay=? ",nowDate);
//			//修改投标流水状态
//			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanRecyDate=?" , nowDate ) ;
//		}else{
//			Db.update("update t_loan_transfer set transState='C' where transState='A' and loanCode=? ",loanCode);
//			//修改投标流水状态
//			Db.update("update t_loan_trace set isTransfer='C' where isTransfer='A' and loanCode=?" , loanCode ) ;
//		}
//		
//	}
//
//	/**
//	 * 	提前还款时，标相关处理
//	 * @param loan
//	 * @return
//	 */
//	private boolean doLoanInfo4adv(LoanInfo loan ){
//		String nowSDate = DateUtil.getStrFromDate(new Date(), "MMdd");
//		int reciedCount = loan.getInt("reciedCount");
//		int limit = loan.getInt("loanTimeLimit");
//		loan.set("reciedCount", reciedCount+1 ) ;
////		loan.set("traceState", traceState.B.val() ) ;
//		loan.set("loanState", loanState.P.val() ) ;
//		loan.set("clearDate", nowSDate ) ;	//更新日期
//		loan.set("backDate", DateUtil.getNowDate() ) ;	//更新日期
//		String loanUserCode = loan.getStr("userCode");
//		loan.set("updateDate", DateUtil.getNowDate());
//		loan.set("updateTime", DateUtil.getNowTime());
//		if( loan.update() == true ){
//			long amount = loan.getLong("loanAmount");
//			int rate = loan.getInt("rateByYear");
//			int rewardRateByYear = loan.getInt("rewardRateByYear");
//			int benefits4new = loan.getInt("benefits4new");
//			rate = rate + rewardRateByYear + benefits4new;
//			String refundType = loan.getStr("refundType");
//			//标信息修改成功，修改借款人资金账户
//			Funds funds = fundsServiceV2.getFundsByUserCode(loanUserCode) ;
//
//			int loanCount = funds.getInt("loanCount");
//			int loanSuccessCount = funds.getInt("loanSuccessCount");
//			int loanBySysCount = funds.getInt("loanBySysCount");
//			long beRecyPrincipal4loan = funds.getLong("beRecyPrincipal4loan");
//			long beRecyInterest4loan = funds.getLong("beRecyInterest4loan");
//			
//			//计算剩余本金利息
//			long[] lastBenxi = CommonUtil.f_006(amount, rate, limit, reciedCount , refundType) ;
//			
//			//计算当期需还本金和利息
//			long[] benxi = CommonUtil.f_000(amount, limit, rate, (reciedCount+1) , refundType );
//			//long ben = benxi[0];//提前结算不用验证当期要还的本金是否大于余额，尼玛先息后本提前还款的话当期没本金 0>0  false
//			long xi = benxi[1] ;
//			long reciedAmount = lastBenxi[0] + xi ;
//			long avBalance = funds.getLong("avBalance") ;
//			if( reciedAmount > avBalance ){
//				funds = fundsServiceV2.recharge( loanUserCode, reciedAmount-avBalance,0 , "到期结算账户余额不足，平台代充！",SysEnum.traceType.D.val()) ;
//				loanBySysCount += (limit-reciedCount);
//			}else{
//				loanSuccessCount += (limit-reciedCount) ;
//			}
//			
//			if( lastBenxi[0] > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, lastBenxi[0] , 0 , traceType.U , fundsType.D , 
//						String.format("标[%s]第%d/%d期提前还款还本金", loan.getStr("loanNo") , reciedCount+1,limit ) );
//			}
//			if( xi > 0 ){
//				funds = fundsServiceV2.doAvBalance4biz(loanUserCode, xi , 0 , traceType.I , fundsType.D , 
//						String.format("标[%s]第%d/%d期提前还款还利息", loan.getStr("loanNo") , reciedCount+1,limit ) );
//			}
//			
//			settlementPlanService.settlement2(loan.getStr("loanCode"), reciedCount+1, lastBenxi[0], xi);
//			//loanCount-1 ，loanSuccessCount+1，beRecyPrincipal4loan-本金,beRecyInterest4loan-利息，updateDate/time
//			//更新资金账户冗余信息
//			funds.set("loanCount", loanCount  - (limit-reciedCount) ) ;
//			funds.set("loanSuccessCount", loanSuccessCount ) ;
//			funds.set("loanBySysCount", loanBySysCount ) ;
//			funds.set("beRecyPrincipal4loan", beRecyPrincipal4loan - reciedAmount + xi ) ;
//			funds.set("beRecyInterest4loan", beRecyInterest4loan - lastBenxi[1]  ) ;
//			return funds.update() ;
//			//更新账户余额,需要加流水
//		}
//		
//		return true ;
//	}

	/**
	 * 无忧存证(补漏) WJW
	 */
	@ActionKey("/saveCFCALoan")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message saveCFCALoan(){
		String loanCode = getPara("loanCode");
		if(StringUtil.isBlank(loanCode)){
			return error("", "loanCode为空", null);
		}
		
		//获取已满标信息
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
				
		//标信息保全
		try {
			OpsResponse loanInfoResponse = CFCA.cfca4loan(loanInfo.getStr("loanTitle"), 
					loanInfo.getStr("loanNo"),getProductTypeName(loanInfo.getStr("productType")),
//							new BigDecimal((loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear")+loanInfo.getInt("benefits4new"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
					new BigDecimal((loanInfo.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
				    new BigDecimal(loanInfo.getLong("loanAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",
					loanInfo.getInt("loanTimeLimit")+"个月", 
					getRefundType(loanInfo.getStr("refundType")), 
					getFormatDate(loanInfo.getStr("backDate")), 
					getFormatDateTime(loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime")),
					getFormatDateTime(loanInfo.getStr("lastPayLoanDateTime")), 
					getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")),
					loanInfo.getStr("userName"),
					CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
					loanInfo.getStr("loanDesc"));
			
			if(100000 != loanInfoResponse.getCode()){
				System.out.println("安存标信息保全失败 ！ 错误信息: " + loanInfoResponse.getMsg());
//						scanCFCALogger.warning("安存标信息保全失败 ！ 错误信息: " + loanInfoResponse.getMsg());
				return error("", "安存标信息保全失败 ！ 错误信息: " + loanInfoResponse.getMsg(), null);
			}
			
			//标保全号入库
			JSONObject parseObject = JSONObject.parseObject(loanInfoResponse.getData());
			boolean saveCFCA = cfcaService.saveCFCA(loanInfo.getStr("loanCode"), parseObject.getString("recordNo"), "2");
			if(false == saveCFCA){
				System.out.println("标保全号入库失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
//						scanCFCALogger.warning("标保全号入库失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
				return error("", "标保全号入库失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"), null);
			}
			
			//保全投标流水
			saveCFCALoanTrace(loanInfo);
		} catch (Exception e) {
			System.out.println("【标书信息保全失败】借款人身份证号解密失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
//					scanCFCALogger.warning("【标书信息保全失败】借款人身份证号解密失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
			e.printStackTrace();
			return error("", "【标书信息保全失败】借款人身份证号解密失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"), null);
		}
		return succ("保全信息成功", "");
	}
	
	/**
	 * 获取产品类型
	 * @param productType
	 * @return
	 */
	public String getProductTypeName(String productType){
		String productTypeName = "";
		switch (productType) {
			case "A":
				productTypeName = "质押宝";
				break;
			case "B":
				productTypeName = "车稳盈";
				break;
			case "C":
				productTypeName = "房稳赚";
				break;
			default:
				productTypeName = "其它";
				break;
		}
		return productTypeName;
	}
	
	/**
	 * 获取还款方式
	 * @param refundType
	 * @return
	 */
	public String getRefundType(String refundType){
		String refundTypeName = "";
		switch (refundType) {
			case "A":
				refundTypeName = "按月等额本息";
				break;
			case "B":
				refundTypeName = "先息后本";
				break;
			default:
				refundTypeName = "其它";
				break;
		}
		return refundTypeName;
	}
	
	public String getFormatDate(String date){
		if(date.length() < 8){
			return "0000-00-00";
		}
		return date.substring(0,4) + "-" + date.substring(4,6)+ "-" + date.substring(6,8);
	}
	
	public String getFormatDateTime(String dateTime){
		if(dateTime.length() < 14){
			return "0000-00-00 00:00:00";
		}
		return dateTime.substring(0,4) + "-" + dateTime.substring(4,6)+ "-" + dateTime.substring(6,8)
				 + " " + dateTime.substring(8,10) + ":" + dateTime.substring(10,12) + ":" + dateTime.substring(12,14);
	}
	
	/**
	 * 保全投标流水
	 * @param loanInfo
	 */
	public void saveCFCALoanTrace(LoanInfo loanInfo){
		//查询投标流水
		List<LoanTrace> loanTraceList = loanTraceService.findAllByLoanCode(loanInfo.getStr("loanCode"));
				
		//投标流水保全
		if(null != loanTraceList && loanTraceList.size() > 0){
			for (int j = 0; j < loanTraceList.size(); j++) {
				
				LoanTrace loanTrace = loanTraceList.get(j);
				
				UserInfo userInfo = UserInfo.userInfoDao.findById(loanTrace.getStr("payUserCode"));
				User user = User.userDao.findById(userInfo.getStr("userCode"));
				
				try {
					OpsResponse loanTranceResponse = CFCA.cfca4loanTrace(loanInfo.getStr("loanTitle"), 
							loanInfo.getStr("loanNo"), 
							getProductTypeName(loanTrace.getStr("productType")), 
//							new BigDecimal((loanTrace.getInt("rateByYear")+loanTrace.getInt("rewardRateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
							new BigDecimal((loanTrace.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
							new BigDecimal(loanInfo.getLong("loanAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",
							loanInfo.getInt("loanTimeLimit")+"个月", 
							getRefundType(loanInfo.getStr("refundType")), 
							getFormatDate(loanInfo.getStr("backDate")), 
							getFormatDateTime(loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime")),
							getFormatDateTime(loanInfo.getStr("lastPayLoanDateTime")), 
							getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")),
							loanInfo.getStr("userName"),
							CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
							userInfo.getStr("userCardName"), 
							CommonUtil.decryptUserCardId(userInfo.getStr("userCardId")), 
							new BigDecimal((loanTrace.getLong("leftAmount") + loanTrace.getLong("leftInterest"))/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",  
							new BigDecimal(loanTrace.getLong("payAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
							getFormatDateTime(loanTrace.getStr("loanDateTime")),
							getLoanTypeName(loanTrace.getStr("loanType")), 
							getFormatDateTime(loanTrace.getStr("loanDateTime")), 
							loanTrace.getStr("traceCode"),
							CommonUtil.decryptUserMobile(user.getStr("userMobile")));
					
					if(100000 != loanTranceResponse.getCode()){
						System.out.println("安存投标流水保全失败 ！ 错误信息: " + loanTranceResponse.getMsg());
//						scanCFCALogger.warning("安存投标流水保全失败 ！ 错误信息: " + loanTranceResponse.getMsg());
						continue;
					}
					
					//投资保全号入库
					JSONObject parseObject = JSONObject.parseObject(loanTranceResponse.getData());
					boolean saveCFCA = cfcaService.saveCFCA(loanTrace.getStr("traceCode"), parseObject.getString("recordNo"), "1");
					if(false == saveCFCA){
						System.out.println("投标流水保全号入库失败! traceCode : " + loanTrace.getStr("traceCode"));
//						scanCFCALogger.warning("投标流水保全号入库失败! traceCode : " + loanTrace.getStr("traceCode"));
						continue;
					}
				} catch (Exception e) {
					System.out.println("【投标流水保全失败】身份证号解密失败! traceCode : " + loanTrace.getStr("traceCode"));
//					scanCFCALogger.warning("【投标流水保全失败】身份证号解密失败! traceCode : " + loanTrace.getStr("traceCode"));
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	/**
	 * 获取投标方式
	 * @param refundType
	 * @return
	 */
	public String getLoanTypeName(String loanType){
		String loanTypeName = "";
		switch (loanType) {
			case "A":
				loanTypeName = "自动";
				break;
			case "M":
				loanTypeName = "手动";
				break;
			default:
				loanTypeName = "其它";
				break;
		}
		return loanTypeName;
	}
	
	/**
	 * 下载债权方案数据(临时) WJW
	 * @return
	 */
	@ActionKey("/exportTransferPlan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public void exportTransferPlan(){
		String type = getPara("type","");
		OPUserV2 opUserV2 = opUserV2Service.findById(getUserCode());
		String opMobile = opUserV2.getStr("op_mobile");
		String decryptUserMobile = "";
		try {
			decryptUserMobile = CommonUtil.decryptUserMobile(opMobile);
		} catch (Exception e1) {
			return;
		}
		if(!"15927334499".equals(decryptUserMobile) && !"13071238735".equals(decryptUserMobile) && "".equals(type)){//仅限吴总,黄毅或开挂玩家下载
			WebUtils.writePromptHtml("权限不足,请充钱!", "/main#", "UTF-8",getResponse());
			return;
		}
		try{
			String fileName = DateUtil.getStrFromDate(DateUtil.getDateFromString(DateUtil.getNowDateTime(),"yyyyMMddHHmmss"), "yyyy年MM月dd日 HH时mm分ss秒") + "债权方案数据.xls";
			fileName = new String(fileName.getBytes("utf-8"), "ISO_8859_1");
			HttpServletResponse response = getResponse();
			response.reset();
			response.addHeader("Content-Disposition", "attachment;filename="
					+ fileName);
			response.addHeader("Content-Length", "" + transfer_makeHtml().getBytes().length);
			response.setContentType("application/vnd.ms-excel;charset=UTF-8;");
			response.setCharacterEncoding("utf-8");
			OutputStream os = new BufferedOutputStream(getResponse()
					.getOutputStream());
			os.write(transfer_makeHtml().getBytes("utf-8"));
			os.flush();
			os.close();
		} catch (Exception e) {
		}
	renderNull();
	}
	
	private String transfer_makeHtml(){
		String output_html = "<table border='1'>";
		output_html += "<tr><td colspan='5'>债转方案数据</td></tr>";
		output_html += "<tr><td></td><td>人数</td><td>债权数</td><td>待收本金(元)</td><td>待兑付金额(元)</td></tr>";
		try{
			//方案一人数
			long fayrs = Db.queryLong("SELECT COUNT(1) from t_transfer_way where `month` = 1");
			//方案一债权条数
			long fayzqts = Db.queryLong("SELECT COUNT(1) from t_loan_transfer where refundType='E' and transState in ('A','B')");
			//方案一待收本金
			long faydsbj = Db.queryBigDecimal("select COALESCE(sum(transAmount+transFee),0) from t_loan_transfer where refundType='E' and transState in ('A','B')").longValue();
			output_html += "<tr>";
			output_html += "<td>方案一</td><td>"+fayrs+"</td><td>"+fayzqts+"</td><td>"+StringUtil.getMoneyYuan(faydsbj)+"</td><td>"+StringUtil.getMoneyYuan(faydsbj*80/100)+"</td>";
			output_html += "</tr>";
			
			//方案二人数
			long faers = Db.queryLong("SELECT COUNT(1) from t_transfer_way where qtr = 1");
			//方案二债权条数
			long faezqts = Db.queryLong("SELECT COUNT(1) from t_loan_transfer where refundType in ('F') and transState in ('A','B')");
			//方案二待收本金
			long faedsbj = Db.queryBigDecimal("select COALESCE(sum(transAmount+transFee),0) from t_loan_transfer where refundType='F' and transState in ('A','B')").longValue();
			output_html += "<tr>";
			output_html += "<td>方案二</td><td>"+faers+"</td><td>"+faezqts+"</td><td>"+StringUtil.getMoneyYuan(faedsbj)+"</td><td>"+StringUtil.getMoneyYuan(faedsbj*80/100)+"</td>";
			output_html += "</tr>";
			
			//方案三人数
			long fasrs = Db.queryLong("SELECT COUNT(1) from t_transfer_way where half=1");
			//方案三债权条数
			long faszqts = Db.queryLong("SELECT COUNT(1) from t_loan_transfer where refundType in ('H') and transState in ('A','B')");
			//方案三待收本金
			long fasdsbj = Db.queryBigDecimal("select COALESCE(sum(transAmount+transFee),0) from t_loan_transfer where refundType='H' and transState in ('A','B')").longValue();
			//方案三兑付金额
			long fasdfje = Db.queryBigDecimal("select COALESCE(sum(transAmount+transFee)*0.5,0) from t_loan_transfer where refundType='H' and transState='B'").longValue();
			output_html += "<tr>";
			output_html += "<td>方案三</td><td>"+fasrs+"</td><td>"+faszqts+"</td><td>"+StringUtil.getMoneyYuan(fasdsbj)+"</td><td>"+StringUtil.getMoneyYuan(fasdfje)+"(承接金额)</td>";
			output_html += "</tr>";
			
			//方案四
			output_html += "<tr>";
			output_html += "<td>方案四</td><td>0</td><td>0</td><td>0</td><td>0</td>";
			output_html += "</tr>";
			
			//方案五人数
			long fawrs = Db.queryLong("SELECT COUNT(1) from t_transfer_way where normal=1");
			//方案五债权条数
			long fawzqts = Db.queryLong("select count(1) from t_loan_trace where payUserCode in (select userCode from t_transfer_way where normal=1) and (loanState='N' or overdueAmount>0)");
			//方案五待收本金
			long fawdsbj = Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal),0) from t_funds where userCode in (SELECT userCode from t_transfer_way where `normal` = 1)").longValue();
			output_html += "<tr>";
			output_html += "<td>方案五</td><td>"+fawrs+"</td><td>"+fawzqts+"</td><td>"+StringUtil.getMoneyYuan(fawdsbj)+"</td><td>/</td>";
			output_html += "</tr>";
			
			//合计
			output_html += "<tr>";
			output_html += "<td>合计</td><td>"+(fayrs+faers+fasrs+fawrs)+"</td><td>"+(fayzqts+faezqts+faszqts+fawzqts)+"</td><td>"+StringUtil.getMoneyYuan(faydsbj+faedsbj+fasdsbj+fawdsbj)+"</td><td>/</td>";
			output_html += "</tr>";
			
			//总计债权条数
			long zjzqts = Db.queryLong("select count(1) from t_loan_trace where loanState='N' or overdueAmount>0");
			//总计待收本金
			long zjdsbj = Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal),0) from t_funds").longValue();
			output_html += "<tr>";
			output_html += "<td colspan='5'>注:总计债权数"+zjzqts+"条,总计待收本金共"+StringUtil.getMoneyYuan(zjdsbj)+"元,方案三待承接待收本金"+StringUtil.getMoneyYuan(fasdsbj-fasdfje*2)+"元。</td>";
			output_html += "</tr>";
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return output_html;
	}
	
	/**
	 * 开通自动债转授权 WJW
	 * @return
	 */
	@ActionKey("/autoCredit")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void AutoCredit(){
		String mobile = getPara("mobile","18372124604");
		User user = userService.findByMobile(mobile);
		String accountId = user.getStr("jxAccountId");
		String userCardName = user.getStr("userCardName");
		String userCardId = user.getStr("userCardId");
		String forgotPwdUrl=CommonUtil.ADDRESS+"/C01";
		String retUrl=CommonUtil.ADDRESS+"/C01";
		String notifyUrl=CommonUtil.NIUX_URL + "/autoCreditCallback";
		JXController.termsAuthPage(accountId, userCardName, userCardId, "1", "", "1", "", "", "", "", "10000000.00", "20240101", "", "", "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(), "");
	}
	
	/**
	 * 自动债转授权回调 WJW
	 */
	@ActionKey("/autoCreditCallback")
	public void autoCreditCallback() {
		String bgData = getPara("bgData");
		if (StringUtil.isBlank(bgData)) {
			return;
		}
		Map<String, ?> map = JSONObject.parseObject(bgData);
		Map<String, String> mapResp = (Map<String, String>) map;
		String jxTraceCode = mapResp.get("txDate") + mapResp.get("txTime") + mapResp.get("seqNo");
		// 将响应报文存入数据库
		boolean updateJxTraceResponse = JXService.updateJxTraceResponse(jxTraceCode, mapResp, JSON.toJSONString(mapResp), "", "");
		
		//开通成功保存授权信息UserTermsAuth
		if(RetCodeUtil.isSuccRetCode(mapResp.get("retCode"))){
			UserTermsAuth userTermsAuth = userTermsAuthService.transByJSONStr(bgData);
			userTermsAuthService.saveOrUpdate(userTermsAuth);
			System.out.println("自动债转授权开通成功:"+mapResp.toString());
		}
		renderText("success");
	}
}
