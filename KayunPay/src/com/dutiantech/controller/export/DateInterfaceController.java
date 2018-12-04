package com.dutiantech.controller.export;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.Message;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.User;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.UserUtil;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

/**
 * 第三方平台数据对接
 * @author shiqingsong
 *
 */
public class DateInterfaceController extends BaseController {

	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private UserService userService = getService(UserService.class);
	private static String token0="";//缓存token
	/**
	 * 网贷之家登录
	 */
	@ActionKey("/wdzjLogin")
	@ResponseCached(cachedKey = "wdzjLogin", cachedKeyParm = "", mode = "remote", time = 60*60)
	public void wdzjLogin() {
		Message msg=null;
		String username=getPara("username","");
		String password=getPara("password","");
		if("".equals(username)){
			msg=error("01", "用户名不能为空", null);
			renderJson(msg);
			return;
		}
		if("".equals(password)){
			msg=error("02", "密码不能为空", null);
			renderJson(msg);
			return;
		}
		try {
			username = MD5Code.md5(username);
			password = MD5Code.md5(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String username0 = "efe2c5120c2547e234966261bd552c1c";//默认用户名 wdzjUsername
		String password0 = "e3a3a8f1ff0cf83bb71e07db6ae14503";//默认密码 wdzjPassword
		
		//验证账户密码是否正确
		if(!(username0.equals(username)&&password0.equals(password))){
			msg=error("01", "账号密码错误", null);
			renderJson(msg);
			return;
		}
		token0 = UserUtil.UserEnCode(username0, getRequestIP(), UserUtil.defaultEnCodeKey);
		//setCookieByHttpOnly( AuthInterceptor.COOKIE_NAME , token0 , 60*60 );//设置cookie
		
		Map<String, Object> map=new HashMap<String,Object>();
		map.put("token", token0);
		Map<String, Map<String, Object>> mMap=new HashMap<String, Map<String, Object>>();
		mMap.put("data",map);
		renderJson(mMap);
	}
	
	/**
	 * 网贷之家
	 * @return
	 */
	@ActionKey("/wdzj")
	@ResponseCached(cachedKey="wdzj", cachedKeyParm="",mode="remote" , time=60*60)
	public void wdzj(){
		Message msg=null;
		String token = getPara("token","");
		//String token0=getCookie(AuthInterceptor.COOKIE_NAME);
		if(!"".equals(token)){
			if(!token.equals(token0)){
				msg=error("01", "token错误", null);
				renderJson(msg);
				return;
			}
		}
		
		Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
		String date = getPara("date",sdf.format(d));//满标时间，和返回的满标的successTime一致
		int page = getParaToInt("page",1);//当前页，currentPage
		if(page<1){
			page=1;
		}
		int pageSize = getParaToInt("pageSize",20);//每页的借款标数
		if(pageSize<1){
			pageSize=20;
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		//根据时间获取标列表
		Page<LoanInfo> loanInfoWDZJ = loanInfoService.findByWDZJ(page, pageSize, date,date);
		int totalRow = loanInfoWDZJ.getTotalRow();//总标数
		long countWDZJ = loanInfoService.countWDZJ(date, date);//投标总额
		
		map.put("token", token);
		map.put("totalPage", loanInfoWDZJ.getTotalPage());//总页数
		map.put("currentPage", loanInfoWDZJ.getPageNumber());//当前页数
		
		//借款标信息
		List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
		if(totalRow>0){
			List<LoanInfo> list = loanInfoWDZJ.getList();
			for (LoanInfo loanInfo : list) {
				Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
				String loanCode = loanInfo.getStr("loanCode");//借款编码，主键
				long loanAmount=loanInfo.getLong("loanAmount");//借款标金额,单位分
				
				List<LoanTrace> loanTrace = loanTraceService.findAllByLoanCode(loanCode);//根据贷款编号查询投标流水
				if(loanTrace==null||loanTrace.size()==0){
					countWDZJ-=loanAmount;//投标流水为空不计入投标总额
					totalRow-=1;
					System.out.println(loanCode);
					continue;
				}
				
				long loanBalance=loanInfo.getLong("loanBalance");//借款标可投金额，单位分
				int rateByYear = loanInfo.getInt("rateByYear");//年利率
				int rewardRateByYear = loanInfo.getInt("rewardRateByYear");//奖励年利率
				String releaseDate=loanInfo.getStr("releaseDate");//发布日期
				String releaseTime=loanInfo.getStr("releaseTime");//发布时间
				String datetime = loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime");//满标时间
				
				mapLoanTrace.put("subscribes", getLoanTrace(loanTrace));//投资人数据
				mapLoanTrace.put("projectId", loanCode);//项目主键(唯一)
				mapLoanTrace.put("title", loanInfo.getStr("loanTitle"));//借款标题
				mapLoanTrace.put("amount", loanBalance>0?((double)(loanAmount-loanBalance)/100):((double)loanAmount/100));//借款金额(若标未满截标，以投标总额为准)
				mapLoanTrace.put("schedule", "100");//进度（只传满标数据，进度均为100）
				mapLoanTrace.put("interestRate", (double)(rateByYear+rewardRateByYear)/100+"%");//利率
				mapLoanTrace.put("deadline", loanInfo.getInt("loanTimeLimit"));//借款期限
				mapLoanTrace.put("deadlineUnit", "月");//期限单位
				mapLoanTrace.put("reward", 0);//如果平台系统无奖励字段，则统一返回0
				if("E".equals(loanInfo.getStr("productType"))){
					mapLoanTrace.put("type", getLoanTypeName(loanInfo.getStr("loanType"))+"(旅游分期)");//借款标类型
				}else {
					mapLoanTrace.put("type", getLoanTypeName(loanInfo.getStr("loanType"))+"(车贷)");//借款标类型
				}
				mapLoanTrace.put("repaymentType", getRefundType2(loanInfo.getStr("refundType")));//还款方式
				mapLoanTrace.put("plateType", "");//标所属平台频道板块(非必需)
				mapLoanTrace.put("guarantorsType", "");//保障担保机构名称(非必需)
				mapLoanTrace.put("province", "");//借款人所在省份(非必需)
				mapLoanTrace.put("city", "");//借款人所在城市(非必需)
				mapLoanTrace.put("userName", loanInfo.getStr("userCode"));//发标人(借款人)ID
				mapLoanTrace.put("userAvatarUrl", "");//发标人头像的URL
				mapLoanTrace.put("amountUsedDesc", getLoanUsedType(loanInfo.getStr("loanUsedType")));//借款用途(非必需)
				mapLoanTrace.put("revenue", 0);//营收(平台收取的服务费、管理费)(非必需)
				mapLoanTrace.put("loanUrl", "www.yrhx.com/Z02_1?loanCode=" + loanCode);//标的详细页面地址链接
				mapLoanTrace.put("successTime", DateUtil.getStrFromDate(DateUtil.getDateFromString(datetime, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss") );//标的成功时间(满标的时间)
				mapLoanTrace.put("publishTime", DateUtil.getStrFromDate(DateUtil.getDateFromString((releaseDate+releaseTime), "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));//发标时间(非必需)
				mapLoanTrace.put("isAgency", 1);//是否机构借款(0:否,1:是)(非必需)
				
				borrowList.add(mapLoanTrace);//借款标信息
			}
			map.put("borrowList", borrowList);//借款标信息
		}
		map.put("totalAmount", (double)countWDZJ/100);//当天借款标总额
		map.put("totalCount", totalRow);//总标数
		renderJson(map);
	}
	
	/**
	 * 网贷之家提前还款
	 */
	@ActionKey("/wdzjPrepayment")
	@ResponseCached(cachedKey="wdzj", cachedKeyParm="",mode="remote" , time=60*60)
	public void wdzjPrepayment(){
		Message msg=null;
		String token = getPara("token","");
		//String token0=getCookie(AuthInterceptor.COOKIE_NAME);
		if(!"".equals(token)){
			if(!token.equals(token0)){
				msg=error("01", "token错误", null);
				renderJson(msg);
				return;
			}
		}
		
		Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        
		String date = getPara("date",sdf.format(d));//满标时间，和返回的满标的successTime一致
		int page = getParaToInt("page",1);//当前页，currentPage
		if(page<1){
			page=1;
		}
		int pageSize = getParaToInt("pageSize",20);//每页的借款标数
		if(pageSize<1){
			pageSize=20;
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		//根据时间获取标列表
		Page<LoanInfo> loanInfo = loanInfoService.findByWDZJPrepayment(page, pageSize, date,date);
		int totalRow = loanInfo.getTotalRow();//总标数
		
		map.put("totalPage", loanInfo.getTotalPage());//总页数
		map.put("currentPage", loanInfo.getPageNumber());//当前页数
		List<Map<String, Object>> preapys=new ArrayList<Map<String,Object>>();//提前还款信息
		
		//借款标信息
		if(totalRow>0){
			List<LoanInfo> list = loanInfo.getList();
			for (LoanInfo lt : list) {
				Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
				mapLoanTrace.put("projectId", lt.getStr("loanCode"));//项目主键
				mapLoanTrace.put("deadline", lt.getInt("loanTimeLimit"));//借款期限
				mapLoanTrace.put("deadlineUnit", "月");//期限单位
				preapys.add(mapLoanTrace);
			}
		}
		map.put("preapys", preapys);//提前还款信息
		renderJson(map);
	}
	
	/**
	 * 佳璐数据
	 * @return
	 */
	@ResponseCached(cachedKey="jlsjloan", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void jlsjloan(){
		String date = getPara("date");
		
		List<LoanInfo> loanInfoJLSJ = loanInfoService.findByJLSJ(date);
		
		List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
		//投标流水
			for (int i = 0; i < loanInfoJLSJ.size(); i++) {
				LoanInfo loanInfo = loanInfoJLSJ.get(i);
				Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
				String loanCode = loanInfo.getStr("loanCode");
				mapLoanTrace.put("SITE_CD", "易融恒信");
				mapLoanTrace.put("BID_ID", loanCode);
				mapLoanTrace.put("BORROWER_NAME", loanInfo.getStr("userName"));
				mapLoanTrace.put("BORROWER_UID", loanInfo.getStr("userCode"));
				mapLoanTrace.put("BID_TP", getProductTypeName(loanInfo.getStr("loanType")));
				mapLoanTrace.put("AMOUNT", (float)loanInfo.getLong("loanAmount")/10/10);
				mapLoanTrace.put("PERIOD", loanInfo.getInt("loanTimeLimit"));
				mapLoanTrace.put("PERIOD_TP","m");
				mapLoanTrace.put("RTN_TP", getRefundType2(loanInfo.getStr("refundType")));
				mapLoanTrace.put("RATE", ((float)loanInfo.getInt("rateByYear")/10/10) + "%");
				mapLoanTrace.put("REWARD_RT", ((float)loanInfo.getInt("rewardRateByYear")/10/10) + "%");
				mapLoanTrace.put("BID_STATUS", 100);
				
				//兼容有问题旧数据时间
				String datetime = loanInfo.getStr("lastPayLoanDateTime");
				if(StringUtil.isBlank(datetime) || "00000000000000".equals(datetime)){
					datetime = loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime");
				}
				mapLoanTrace.put("BID_OVER_TIME", DateUtil.getStrFromDate(DateUtil.getDateFromString(datetime, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss") );
				mapLoanTrace.put("BID_TITLE",loanInfo.getStr("loanTitle"));
				
				/*List<LoanTrace> loanTrace = loanTraceService.findAllByLoanCode(loanCode);
				mapLoanTrace.put("subscribes", getLoanTrace(loanTrace));*/
				borrowList.add(mapLoanTrace);
			}
		 renderJson(borrowList);
	}
	/**
	 * 佳璐数据
	 * @return
	 */
	@ResponseCached(cachedKey="jlsjinvest", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void jlsjinvest(){
		String date = getPara("date");
		
		List<LoanTrace> loantraceJLSJ = loanTraceService.findLoanTraceJLSJ(date);
		
		List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
		//投标流水
		for (int i = 0; i < loantraceJLSJ.size(); i++) {
			LoanTrace loanTrace = loantraceJLSJ.get(i);
			Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
			mapLoanTrace.put("SITE_CD", "易融恒信");
			mapLoanTrace.put("BID_ID", loanTrace.getStr("loanCode"));
			mapLoanTrace.put("INVESTOR_NAME", loanTrace.getStr("payUserName"));
			mapLoanTrace.put("INVESTOR_UID", loanTrace.getStr("payUserCode"));
			mapLoanTrace.put("INV_TIME", loanTrace.getStr("loanDateTime"));
			mapLoanTrace.put("AMOUNT", loanTrace.getStr("payAmount"));
			mapLoanTrace.put("BID_STATUS", "100");
			
			
			/*List<LoanTrace> loanTrace = loanTraceService.findAllByLoanCode(loanCode);
				mapLoanTrace.put("subscribes", getLoanTrace(loanTrace));*/
			borrowList.add(mapLoanTrace);
		}
		renderJson(borrowList);
	}
	
	
	@ResponseCached(cachedKey="tianyan", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void tianyan(){
		int status = getParaToInt("status");
		int pageIndex = getParaToInt("page_index");
		
		
		
		//验证数据完整性
		if(pageIndex <= 0){
			pageIndex = 1;
		}
		
		
		int pageSize = getParaToInt("page_size",1) ;
		if( pageSize > 20 )
			pageSize = 20 ;
		
		String timeFrom = getPara("time_from");
		String timeTo = getPara("time_to");
		
		int result_code=0;
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		
		@SuppressWarnings("serial")
		Map<String,Object> loanType = new HashMap<String, Object>(){{
			
			/*0 代表信用标,1 担保标;2 抵押,质押标, 3 秒标;
			4 债权转让标(流转标,二级市场标的);5 理财计划(宝类业务_活期);
			6 其它;7 净值标;8 活动标(体验标).9 理财计划(宝类业务_定期).
			3，4，5标类型不参与贷款余额计算；请注意5【理财计划(宝类业务_活期)】和9【理财计划(宝类业务_定期)】的区分；4债权转让标指的是不会产生新待还的转让，如果会产生新待还，请返回其他标类型.*/
			
			//信用标
			this.put("A", 0);
			// 抵押标
			this.put("B", 2);
			// 担保标
			this.put("C", 2);
			// 流转标
			this.put("D", 2);
			// 质押标
			this.put("E", 2);
			
			
			
			// 抵押担保标
			this.put("F",2);
			// 抵押流转标
			this.put("G", 2);
			// 担保流转标
			this.put("H", 2);
			// 质押流转标
			this.put("I", 2);
			// 机构标
			this.put("J", 2);
			
			
		}};
		Map<String,Object> refundType = new HashMap<String, Object>();
		
		/*
		0 代表其他;1 按月等额本息还款;2按月付息,到期还本;
		3 按天计息,一次性还本付息;4,按月计息,一次性还本付息;
		5 按季分期还款;6 为等额本金,按月还本金;7 先息期本.*/
		
		//A 按月等额本息
		refundType.put("A", 1);
		// B 先息后本
		refundType.put("B", 2);
		
		
		if(status==1){
			try {
				
				Page<LoanInfo> loanInfoTY = loanInfoService.findByTY(pageIndex, pageSize,status, timeFrom,timeTo);
				
				
				map.put("page_count", loanInfoTY.getTotalPage());
				map.put("page_index", loanInfoTY.getPageNumber());
				
				
				
				
				
				List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
				//投标流水
				if(loanInfoTY.getTotalRow() > 0){
					List<LoanInfo> list = loanInfoTY.getList();
					for (int i = 0; i < list.size(); i++) {
						LoanInfo loanInfo = list.get(i);
						Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
						mapLoanTrace.put("id", loanInfo.getStr("loanCode"));
						mapLoanTrace.put("url",  "www.yrhx.com/Z02_1?loanCode=" + loanInfo.getStr("loanCode"));
						mapLoanTrace.put("platform_name", "易融恒信");
						mapLoanTrace.put("title", loanInfo.getStr("loanTitle"));
						mapLoanTrace.put("username", loanInfo.getStr("userName"));
						mapLoanTrace.put("status", status);
						mapLoanTrace.put("userid", loanInfo.getStr("userCode"));
						mapLoanTrace.put("c_type", loanType.get(loanInfo.getStr("loanType")));
						mapLoanTrace.put("amount", (float)loanInfo.getLong("loanAmount")/10/10);
						mapLoanTrace.put("rate", (float)(loanInfo.getInt("rateByYear"))/10/10/10/10);
						
						mapLoanTrace.put("period", loanInfo.getInt("loanTimeLimit"));
						mapLoanTrace.put("p_type", "1");
						mapLoanTrace.put("pay_way",refundType.get( loanInfo.getStr("refundType")));
						mapLoanTrace.put("process", 1);
						mapLoanTrace.put("reward", (float)loanInfo.getInt("rewardRateByYear")/10/10/10/10);
						//mapLoanTrace.put("guarantee", loanInfo.getStr("loanCode"));
						mapLoanTrace.put("start_time",DateUtil.getStrFromDate(DateUtil.getDateFromString( loanInfo.getStr("releaseDate")+ loanInfo.getStr("releaseTime"), "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss") );
						
						
						//兼容有问题旧数据时间
						String datetime = loanInfo.getStr("lastPayLoanDateTime");
						if(StringUtil.isBlank(datetime) || "00000000000000".equals(datetime)){
							datetime = loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime");
						}
						mapLoanTrace.put("end_time", DateUtil.getStrFromDate(DateUtil.getDateFromString( datetime, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
//						mapLoanTrace.put("end_time",  loanInfo.getStr("lastPayLoanDateTime"));
						mapLoanTrace.put("invest_num", loanInfo.getInt("maxLoanCount"));
						//mapLoanTrace.put("c_reward", loanInfo.getStr("loanCode"));
						borrowList.add(mapLoanTrace);
					}
				}
				map.put("loans", borrowList);
				result_code=1;
				
				map.put("result_msg", "获取数据成功");
				
				
			} catch (Exception e) {
				map.put("result_msg", "获取数据失败");
				e.printStackTrace();
			}
		}else{
			result_code=1;
			
			map.put("result_msg", "获取数据成功");
		}
		map.put("result_code", result_code);
		renderJson(map);

	}
	/**
	 * 天眼投资人数据
	 * @return
	 */
	@ResponseCached(cachedKey="tianyanInvest", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void tianyanInvest(){
		String id = getPara("id");
		int pageIndex = getParaToInt("page_index");
		
		
		
		
		//验证数据完整性
		if(pageIndex <= 0){
			pageIndex = 1;
		}
		
		
		int pageSize = getParaToInt("page_size",1) ;
		if( pageSize > 20 )
			pageSize = 20 ;
		
		int result_code=0;
		
		Map<String,Object> map = new HashMap<String,Object>();
		
		try {
			
			Page<LoanTrace> page = loanTraceService.findPageByLoanCode(id,pageIndex,pageSize);
			
			
		
			map.put("page_count", page.getTotalPage());
			map.put("page_index", page.getPageNumber());
			
			
			
			
			List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
			//投标流水
			if(page.getTotalRow() > 0){
				List<LoanTrace> list = page.getList();
				for (int i = 0; i < list.size(); i++) {
					LoanTrace loanTrace = list.get(i);
					Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
					mapLoanTrace.put("id", loanTrace.getStr("loanCode"));
					mapLoanTrace.put("link",  "www.yrhx.com/Z02_1?loanCode=" + loanTrace.getStr("loanCode"));
					mapLoanTrace.put("username", loanTrace.getStr("payUserName"));
					mapLoanTrace.put("userid", loanTrace.getStr("payUserCode"));
					mapLoanTrace.put("type", loanTrace.getStr("loanType").equals("A")?"自动":"手动");
					mapLoanTrace.put("money", (float)loanTrace.getLong("payAmount")/10/10);
					mapLoanTrace.put("account", (float)loanTrace.getLong("payAmount")/10/10);
					mapLoanTrace.put("status","1");
					mapLoanTrace.put("add_time",DateUtil.getStrFromDate(DateUtil.getDateFromString(loanTrace.getStr("loanDateTime"), "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
		
					
					borrowList.add(mapLoanTrace);
				}
			}
			
			
			result_code=1;
			map.put("loans", borrowList);
			
			map.put("result_msg", "获取数据成功");
			
			
		} catch (Exception e) {
			map.put("result_msg", "获取数据失败");
			e.printStackTrace();
		}
		map.put("result_code", result_code);
		
		renderJson(map);
		
	}
	
	/**
	 * 网贷天眼：借款数据
	 * 请求参数：
	 * status		Int		Yes	标的状态:0.正在投标中的借款标;1.已完成(包括还款中和已完成的借款标).
	 * time_from	String	Yes	起始时间如:2014-05-09 06:10:00,
	 * 							状态为1是对应平台满标字段的值检索,状态为0就以平台发标时间字段检索.
	 * time_to		String	Yes	截止时间如:2014-05-09 06:10:00,
	 * 							状态为1是对应平台满标字段的值检索,状态为0就以平台发标时间字段检索.
	 * page_size	Int		Yes	每页记录条数.
	 * page_index	Int		Yes	请求的页码.
	 * token		String	No	请求 token 链接平台返回的秘钥或签名.
	 */
	@ActionKey("/tianyanLoanData")
	@ResponseCached(cachedKey="tianyanLoanData", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void tianyanLoanData(){
		//获取标的状态   0:正在投标中的借款标;1:已完成(包括还款中和已完成的借款标).
		int status = getParaToInt("status");
		//获取起止时间
		String timeFrom = getPara("time_from");
		String timeTo = getPara("time_to");
		//获取请求页码
		int pageIndex = getParaToInt("page_index",1);
		
		if(pageIndex <= 0){
			pageIndex=1;
		}
		//获取每页记录数量
		int pageSize = getParaToInt("page_size",1);
		
		if(pageSize >=20){
			pageSize = 20;
		}
		
		//获取链接平台的秘钥
		String token = getPara("token","");
		
		String cookie = getCookie(AuthInterceptor.COOKIE_NAME);
		
		Map<String, Object> map = new HashMap<String, Object>();
		//请求状态标识
		int result_code = 0;
		
		if(status == 1 ||status ==0){  //status 只能是0 和 1
			if(token.equals(cookie)){//验证秘钥成功
				
				@SuppressWarnings("serial")
				Map<String, Object> loanType = new HashMap<String, Object>(){{
					
					/*
					 * 0 代表信用标,1 担保标;2 抵押,质押标, 3 秒标;
					 * 4 债权转让标(流转标,二级市场标的);5 理财计划(宝类业务_活期);
					 * 6 其它;7 净值标;8 活动标(体验标).9 理财计划(宝类业务_定期).
					 * 3，4，5标类型不参与贷款余额计算；请注意5【理财计划(宝类业务_活期)】和9【理财计划(宝类业务_定期)】的区分；
					 * 4债权转让标指的是不会产生新待还的转让，如果会产生新待还，请返回其他标类型.
					 */
					this.put("A", 0);// 信用标
					this.put("B", 2);// 抵押标
					this.put("C", 1);// 担保标
					this.put("D", 2);// 流转标
					this.put("E", 2);// 质押标
					this.put("F", 2);// 抵押担保标
					this.put("G", 2);// 抵押流转标
					this.put("H", 1);// 担保流转标
					this.put("I", 2);// 质押流转标
					this.put("J", 6);// 机构标
				}};
				
				Map<String, Object> refundType = new HashMap<String, Object>();
				/*
				 * 0 代表其他;1 按月等额本息还款;2按月付息,到期还本;
				 * 3 按天计息,一次性还本付息;4,按月计息,一次性还本付息;
				 * 5 按季分期还款;6 为等额本金,按月还本金;7 先息期本;
				 * 8 按季付息,到期还本;9 按半年付息,到期还本;
				 * 10 按年付息，到期还本.
				 */
				// A：按月等额本息
				refundType.put("A", 1);
				// B： 先息后本
				refundType.put("B", 2);
				
				try {
					Page<LoanInfo> loanInfoTY = loanInfoService.findByTY(pageIndex, pageSize, status, timeFrom, timeTo);
					
					//判断是否有标
					if(loanInfoTY != null && loanInfoTY.getPageSize() > 0){//有标时
						
						map.put("page_count", loanInfoTY.getTotalPage());// page_count:总页数
						map.put("page_index", loanInfoTY.getPageNumber());// page_index:当前页码
						
						List<Map<String, Object>> loans =new ArrayList<Map<String,Object>>();
						//借款标信息
						List<LoanInfo> list = loanInfoTY.getList();
						for (int i = 0; i < list.size(); i++) {
							LoanInfo loanInfo = list.get(i);
							Map<String, Object> mapLoanTrace = new HashMap<String, Object>();
							//去除借款金额小于50的标
							if(loanInfo.getLong("loanAmount") < 5000){
								continue;
							}
							mapLoanTrace.put("id", loanInfo.getStr("loanCode"));//借款标编码
							mapLoanTrace.put("url", "www.yrhx.com/Z02_1?loanCode=" + loanInfo.getStr("loanCode"));//借款标链接
							mapLoanTrace.put("platform_name", "易融恒信");
							mapLoanTrace.put("title", loanInfo.getStr("loanTitle"));//借款标标题
							mapLoanTrace.put("username", loanInfo.get("userName"));//借款人姓名
							mapLoanTrace.put("status", status);//标的状态
							mapLoanTrace.put("userid", loanInfo.getStr("userCode"));//借款人编号
							mapLoanTrace.put("c_type", loanType.get(loanInfo.getStr("loanType")));//借款类型
							mapLoanTrace.put("amount", (float)loanInfo.getLong("loanAmount")/10/10);//借款金额
							mapLoanTrace.put("rate", (float)loanInfo.getInt("rateByYear")/10/10/10/10);//借款年利率
							mapLoanTrace.put("period", loanInfo.getInt("loanTimeLimit"));//借款期限
							mapLoanTrace.put("p_type", "1");//期限类型  0 代表天,1 代表月.   借款期限以月为单位
							mapLoanTrace.put("pay_way", refundType.get(loanInfo.getStr("refundType")));//还款方式
							mapLoanTrace.put("process", new DecimalFormat("#0.0").format((double)(loanInfo.getLong("loanAmount") - loanInfo.getLong("loanBalance"))/loanInfo.getLong("loanAmount")));//完成百分比
							mapLoanTrace.put("reward", new DecimalFormat("#0.00").format((double)loanInfo.getInt("rewardRateByYear")/10/10/10/10));//奖励年利率
							mapLoanTrace.put("guarantee", 0);//担保奖励利率  非必需
							//标的创建时间
							mapLoanTrace.put("start_time", DateUtil.getStrFromDate(DateUtil.getDateFromString( loanInfo.getStr("createDate")+ loanInfo.getStr("createTime"),"yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
							
							//status=1时end_time必选
							if(status == 1){
								//兼容旧数据时间
								String dateTime = loanInfo.getStr("lastPayLoanDateTime");
								if(StringUtil.isBlank(dateTime) || "00000000000000".equals(dateTime)){
									dateTime = loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime");
								}
								//满标时间
								mapLoanTrace.put("end_time", DateUtil.getStrFromDate(DateUtil.getDateFromString(dateTime, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
							}
							
							mapLoanTrace.put("invest_num", loanInfo.getInt("maxLoanCount"));//投资次数
							mapLoanTrace.put("c_reward", 0);//续投奖励
							loans.add(mapLoanTrace);
							
						}
						
						map.put("loans", loans);
						result_code = 1;
						
						map.put("result_msg", "获取数据成功!");
					}else{//无标时
						result_code = 1;
						map.put("page_count", 0);
						map.put("page_index", 0);
						map.put("loans", null);
						map.put("result_msg", "获取数据成功！");
					}
				} catch (Exception e) {//  获取数据异常时
					result_code = -1;
					map.put("result_msg", "获取数据失败!");
					e.printStackTrace();
				}
				
				
			}else{//token验证不通过时
				result_code = -1;
				map.put("page_count", 0);
				map.put("page_index", 0);
				map.put("result_msg", "未授权的访问！");
				map.put("loans", null);
			}
		}else{
			result_code = -1;
			map.put("page_count", 0);
			map.put("page_index", 0);
			map.put("result_msg", "请求参数有误！");
			map.put("loans", null);
		}
		
		map.put("result_code", result_code);
		
		renderJson(map);
	}
	/**
	 * 网贷天眼：投资记录
	 * status		Int		Yes	标的状态:0.正在投标中的借款标;1.已完成(包括还款中和已完成的借款标).
	 * time_from	String	Yes	起始时间如:2014-05-09 06:10:00,
	 * 							状态为1是对应平台满标字段的值检索,状态为0就以平台发标时间字段检索.
	 * time_to		String	Yes	截止时间如:2014-05-09 06:10:00,
	 * 							状态为1是对应平台满标字段的值检索,状态为0就以平台发标时间字段检索.
	 * page_size	Int		Yes	每页记录条数.
	 * page_index	Int		Yes	请求的页码.
	 * token		String	No	请求 token 链接平台返回的秘钥或签名.
	 */
	@ActionKey("/investData")
	@ResponseCached(cachedKey="investData", cachedKeyParm="", mode="remote", time=2*60*60)
	public void tianyanInvestData(){
		int status = getParaToInt("status");
		//获取起止时间
		String timeFrom = getPara("time_from");
		String timeTo = getPara("time_to");
		int pageIndex = getParaToInt("page_index");
		if(pageIndex <= 0){
			pageIndex = 1;
		}
		
		int pageSize = getParaToInt("page_size", 1);
		if(pageSize > 20){
			pageSize = 20;
		}
		
		String token = getPara("token");
		String cookie = getCookie(AuthInterceptor.COOKIE_NAME);
		int result_code = 0;	//请求标识
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		if(status==1 || status==0){//status:0 | 1
			
			if(token.equals(cookie)){//token验证成功
				//查询timeFrom~timeTo时间段内的标
				List<LoanInfo> loanInfos = loanInfoService.findByTY1(status, timeFrom, timeTo);
				
				List<Object> paras = new ArrayList<Object>();
				for (int i = 0; i < loanInfos.size(); i++) {
					paras.add(loanInfos.get(i).getStr("loanCode"));
				}
				
				//查询满足条件的投资记录
				Page<LoanTrace> loanTraces = loanTraceService.findPageByLoanCodes(pageIndex, pageSize, paras);
				
				map.put("page_index", loanTraces.getPageNumber());
				map.put("page_count", loanTraces.getTotalPage());
				
				List<Map<String, Object>> loans = new ArrayList<Map<String, Object>>();
				
				//判断是否有投资记录
				if(loanTraces != null && loanTraces.getList().size() > 0){
					List<LoanTrace> list = loanTraces.getList();
					for (int i = 0; i < list.size(); i++) {
						LoanTrace loanTrace = list.get(i);
						Map<String, Object> mapLoanTrace = new HashMap<String, Object>();
						//过滤投资金额小于50元的记录
						if(loanTrace.getLong("payAmount") < 5000){
							continue;
						}
						mapLoanTrace.put("id", loanTrace.getStr("loanCode"));//标的编号
						mapLoanTrace.put("link", "www.yrhx.com/Z02_1?loanCode=" + loanTrace.getStr("loanCode"));
						//mapLoanTrace.put("useraddress", null);//投标人所在城市
						mapLoanTrace.put("username", loanTrace.getStr("payUserName"));//投资人名称
						mapLoanTrace.put("userid", loanTrace.getStr("payUserCode"));//用户编号
						mapLoanTrace.put("type", loanTrace.getStr("loanType").equals("A")?"自动":"手动");//投标方式
						mapLoanTrace.put("money", (float)loanTrace.getLong("payAmount")/10/10);//投标金额
						mapLoanTrace.put("account", (float)loanTrace.getLong("payAmount")/10/10);//有效金额
						//mapLoanTrace.put("status","1");//投标状态
						//投标时间
						mapLoanTrace.put("add_time",DateUtil.getStrFromDate(DateUtil.getDateFromString(loanTrace.getStr("loanDateTime"),"yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
						
						loans.add(mapLoanTrace);
					}
					
					result_code = 1;
					map.put("loans", loans);
					map.put("result_msg", "获取数据成功!");
				}else{//无投标流水
					result_code = 1;
					map.put("page_index", 0);
					map.put("page_count", 0);
					map.put("loans", null);
					map.put("result_msg", "获取数据成功!");
				}
			}else{//token验证不通过
				result_code = -1;
				map.put("page_index", 0);
				map.put("page_count", 0);
				map.put("result_msg", "未授权的访问!");
				map.put("loans", null);
			}
		}else{
			result_code = -1;
			map.put("page_count", 0);
			map.put("page_index", 0);
			map.put("result_msg", "请求参数有误！");
			map.put("loans", null);
		}
		
		map.put("result_code", result_code);
		renderJson(map);
		
	}
	/**
	 * 网贷天眼：token请求地址
	 * username	String	Yes	用户名.
	 * password	String	Yes	密码.
	 */
	@ActionKey("/token")
	@ResponseCached(cachedKey = "token", cachedKeyParm = "", mode = "remote", time = 2*60*60)
	public void tianyanToken(){
		String username = getPara("username");
		String password = getPara("password");
		
		Map<String, Object> map = new HashMap<String, Object>();
		Map<String, Object> mapToken = new HashMap<String, Object>();
		
		int result = 0;
		String token = "";//秘钥
		
		//String loginAuth = "";
		if(StringUtil.isBlank(username) || StringUtil.isBlank(password)){//验证用户、密码是否为空
			result = -1;
			token = null;
		}else{
			//易融恒信对接网贷天眼:uName、pwd （自定义）
			String uName = null;
			String pwd =null;
			try {
				uName = "wdty_user";
				//密码加密
				pwd = MD5Code.md5("L94qcTHsMCYmdokN");//L94qcTHsMCYmdokN
				/*String oldPwd = CommonUtil.getSourcePwd(password);
				loginAuth = MD5Code.md5(username+oldPwd);
				
				User user = userService.find4AuthCode(loginAuth);*/
				String oldPwd = MD5Code.md5(password);
				
				if(!uName.equals(username) || !pwd.equals(oldPwd) ){//登录失败
					result = -1;
					token = null;
				}else{
					token = UserUtil.UserEnCode(username, getRequestIP(), UserUtil.defaultEnCodeKey);
					//token = UIDUtil.generate();
					result = 1;
					
					setCookieByHttpOnly(AuthInterceptor.COOKIE_NAME, token, 2*60*60);
					
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		mapToken.put("token", token);
		
		map.put("result", result);
		map.put("data", mapToken);
		renderJson(map);
	}

	
	
	/**
	 * 多赚数据
	 * @return
	 */
	@ResponseCached(cachedKey="duozhuan", cachedKeyParm="",mode="remote" , time=2*60*60)
	public void duozhuan(){

		int page = getParaToInt("page");
		int pageSize = getPageSize();
		//验证数据完整性
		if(page <= 0){
			page = 1;
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		//根据时间获取标列表
		Page<LoanInfo> loanInfoWDZJ = loanInfoService.findByDCQ(page, pageSize);
		
		map.put("totalPage", loanInfoWDZJ.getTotalPage());
		map.put("totalCount", loanInfoWDZJ.getTotalRow());
		
		TransformDator trans = new TransformDator() {
			@Override
			public Object transform(Object obj) {
				LoanTrace lt = (LoanTrace) obj;
				Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
				mapLoanTrace.put("subUserName", lt.getStr("payUserCode"));
				mapLoanTrace.put("amount", (float)lt.getLong("payAmount")/10/10);
				mapLoanTrace.put("validAmount", (float)lt.getLong("payAmount")/10/10);
				mapLoanTrace.put("addDate", DateUtil.getStrFromDate(DateUtil.getDateFromString(lt.getStr("loanDateTime"), "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
				mapLoanTrace.put("status", 1);
				mapLoanTrace.put("type", lt.getStr("loanType").equals("A")?1:0);
				return mapLoanTrace;
			}
		};
		
		
		
		Map<String,Object> refundType = new HashMap<String, Object>();
		//A 按月等额本息
		refundType.put("A", 2);
		// B 先息后本
		refundType.put("B", 7);
		List<Map<String,Object>> borrowList = new ArrayList<Map<String,Object>>();
		//投标流水
		if(loanInfoWDZJ.getTotalRow() > 0){
			List<LoanInfo> list = loanInfoWDZJ.getList();
			for (int i = 0; i < list.size(); i++) {
				LoanInfo loanInfo = list.get(i);
				Map<String,Object> mapLoanTrace = new HashMap<String, Object>();
				String loanCode = loanInfo.getStr("loanCode");
				mapLoanTrace.put("projectId", loanCode);
				mapLoanTrace.put("title", loanInfo.getStr("loanTitle"));
				mapLoanTrace.put("loanUrl", "www.yrhx.com/Z02_1?loanCode=" + loanCode);
				mapLoanTrace.put("userName", loanInfo.getStr("userCode"));
				mapLoanTrace.put("amount", (float)loanInfo.getLong("loanAmount")/10/10);
				mapLoanTrace.put("schedule", 100);
				mapLoanTrace.put("interestRate", ((float)(loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear"))/10/10) + "%");
				mapLoanTrace.put("deadline", loanInfo.getInt("loanTimeLimit"));
				mapLoanTrace.put("deadlineUnit", "月");
				mapLoanTrace.put("reward", 0);
				mapLoanTrace.put("type", getProductTypeName(loanInfo.getStr("productType")));
				mapLoanTrace.put("repaymentType", refundType.get((loanInfo.getStr("refundType"))));
				mapLoanTrace.put("warrantcom", "--");
				List<Map<String, Object>> loanTrace = loanTraceService.findAllByLoanCodeCoustom(loanCode,trans);
				mapLoanTrace.put("subscribes", loanTrace);
				borrowList.add(mapLoanTrace);
			}
		}
		map.put("borrowList", borrowList);
		renderJson(map);
	}
	
	
	/**
	 * 单个标的投标列表信息 List<LoanTrace> 转 List<Map<String,Object>>
	 * @param loanTrace
	 * @return
	 */
	private List<Map<String,Object>> getLoanTrace(List<LoanTrace> loanTrace){
		List<Map<String, Object>> listLoanTrace=new ArrayList<Map<String,Object>>();
		for (LoanTrace lt : loanTrace) {
			Map<String, Object> mapLoanTrace=new HashMap<String,Object>();
			long payAmount = lt.getLong("payAmount");//投标金额，单位分
			String loanDateTime = lt.getStr("loanDateTime");//投标日期时间
			String loanType = lt.getStr("loanType");//投标方式
			mapLoanTrace.put("subscribeUserName", lt.getStr("payUserCode"));//投标人ID
			mapLoanTrace.put("amount", (double)payAmount/100);//投标金额
			mapLoanTrace.put("validAmount", (double)payAmount/100);//有效金额
			mapLoanTrace.put("addDate", DateUtil.getStrFromDate(DateUtil.getDateFromString(loanDateTime, "yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));//投标时间
			mapLoanTrace.put("status", 1);//投标状态(平台没有这个字段的默认为1 全部通过)
			mapLoanTrace.put("type", "A".equals(loanType)?1:0);//标识手动或自动投标
			mapLoanTrace.put("sourceType", 1);//投标来源 1 ：PC端  2 ：WAP端  3 ：平台APP客户端  4 ：微信 (非必需)
			listLoanTrace.add(mapLoanTrace);
		}
		return listLoanTrace;
	}
	
	/**
	 * 贷罗盘登录
	 */
	@ActionKey("/dlpLogin")
	@ResponseCached(cachedKey="dlpLogin",cachedKeyParm="",mode="remote",time=60*60)
	public void dlpLogin(){
		Message msg = null;
		String username = getPara("username","");
		String password = getPara("password","");
		if(StringUtil.isBlank(username)){
			msg = error("01","用户名不能为空","");
			renderJson(msg);
			return;
		}
		if(StringUtil.isBlank(password)){
			msg = error("02","密码不能为空","");
			renderJson(msg);
			return;
		}
		try {
			username = MD5Code.md5(username);
			password = MD5Code.md5(password);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String username0="";
		String password0="";
		try{
			username0 = MD5Code.md5("dlpUsername");//默认用户名：dlpUsername
			password0 = MD5Code.md5("dlpPassword");//默认密码：dlpPassword
		}catch(Exception e){
			e.printStackTrace();
		}
		
		if(!(username0.equals(username)&&password0.equals(password))){
			msg = error("01","账号密码错误",null);
			renderJson(msg);
			return;
		}
		
		String token1 = UserUtil.UserEnCode(username0, getRequestIP(), UserUtil.defaultEnCodeKey);
		/*setCookieByHttpOnly(AuthInterceptor.COOKIE_NAME, token1, 2*60*60);*/
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String,Object> mMap = new HashMap<String,Object>();
		mMap.put("token", token1);
		map.put("result",1);
		map.put("resultmsg","登录成功");
		map.put("data", mMap);
		renderJson(map);
	}
	
	/**
	 * 贷罗盘数据
	 */
	@ActionKey("/dlpLoanData")
	@ResponseCached(cachedKey="dlp",cachedKeyParm="",mode="remote",time=60*60)
	public void dlpLoanData(){
		Message msg = null;
		String token = getPara("token","");
		String date = getPara("date","");
		Integer page = getParaToInt("page",1);
		if(page < 1){
			page = 1;
		}
		Integer pageSize = getParaToInt("pageSize",20);
		if(pageSize<=0){
			pageSize=20;
		}
		
		Map<String,Object> map = new HashMap<String,Object>();
		List<Map<String,Object>> loans = new ArrayList<Map<String,Object>>();
	
		/*String cookie = getCookie(AuthInterceptor.COOKIE_NAME);*/
		
		/*if(token.equals(cookie)){*/
			
			Map<String,Integer> refundType = new HashMap<String,Integer>();
			/*
			 * 1：到期还本息(到期还本付息，一次性还本付息，按日计息到期还本,一次性付款、秒还)
			 * 2：每月等额本息(按月分期，按月等额本息)
			 * 3：每季分期（按季分期，按季等额本息）
			 * 5：每月付息到期还本(先息后本)
			 * 6：等额本金(按月等额本金)
			 * 7：每季付息到期还本（按季付息到期还本）
			 * 8：每月付息分期还本
			 * 9：先付息到期还本 
			 */
			//A:按月等额本息
			refundType.put("A",2);
			//B:先息后本
			refundType.put("B",9);
			
			
			try{
				Page<LoanInfo> loanInfoDLP = loanInfoService.findByDLP(page, pageSize, date);//只传满标的标
				//查询数据不为空时
				if(loanInfoDLP !=null && loanInfoDLP.getPageSize()>0){
					map.put("totalPage",String.valueOf(loanInfoDLP.getTotalPage()));//总页数
					map.put("currentPage", String.valueOf(loanInfoDLP.getPageNumber()));//当前页
					
					
					//借款标信息
					List<LoanInfo> list = loanInfoDLP.getList();
					for(int i=0;i<list.size();i++){
						LoanInfo loanInfo = list.get(i);
						if(StringUtil.isBlank(loanInfo.getStr("loanCode"))){
							continue;
						}
						Map<String,Object> mapLoanInfo = new HashMap<String,Object>();
					
						String userCode = loanInfo.getStr("userCode");
						User user = userService.findById(userCode);
//						String loanType = getLoanTypeName(loanInfo.getStr("loanType"));//标的类型
						String projectId = loanInfo.getStr("loanCode");//项目主键  标code
						String releaseDate = loanInfo.getStr("releaseDate");//发布时间
							projectId = projectId + "_" + releaseDate;
						mapLoanInfo.put("projectId", projectId);
						mapLoanInfo.put("title",loanInfo.getStr("loanTitle"));//借款标题
						mapLoanInfo.put("amount",Double.parseDouble(StringUtil.getMoneyYuan(loanInfo.getLong("loanAmount"))));//借款金额
						mapLoanInfo.put("schedule","100");//进度    只传满编数据，进度均为100
						mapLoanInfo.put("interestRate",StringUtil.getMoneyYuan(loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear")+loanInfo.getInt("benefits4new"))+"%");
						mapLoanInfo.put("deadline",loanInfo.getInt("loanTimeLimit"));//借款期限
						mapLoanInfo.put("deadlineUnit","月");//期限单位
						mapLoanInfo.put("reward",0);//奖励
						mapLoanInfo.put("type", getLoanTypeName(loanInfo.getStr("loanType")));//标的类型
						mapLoanInfo.put("repaymentType",refundType.get(loanInfo.getStr("refundType")));//还款方式
						mapLoanInfo.put("userName",user != null?user.getStr("userName"):loanInfo.getStr("userName"));//借款人姓名
						mapLoanInfo.put("amountUsedDesc",getLoanUsedType(loanInfo.getStr("loanUsedType")));//借款用途
						mapLoanInfo.put("loanUrl","www.yrhx.com/Z02_1?loanCode=" + loanInfo.getStr("loanCode"));
						List<LoanTrace> loanTraceList = loanTraceService.findAllByLoanCode(loanInfo.getStr("loanCode"));
						if(loanTraceList == null || loanTraceList.isEmpty()){
							continue;
						}
						String loanDateTime = loanTraceList.get(0).getStr("loanDateTime");
						Date dateFrom = DateUtil.getDateFromString(loanDateTime,"yyyyMMddHHmmss");
						String lastPayLoanDateTime = DateUtil.getLongStrFromDate(dateFrom);
						mapLoanInfo.put("successTime",lastPayLoanDateTime);//标的成功时间
						String releaseDateTime = loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime");
						Date release = DateUtil.getDateFromString(releaseDateTime,"yyyyMMddHHmmss");
						String publishTime = DateUtil.getLongStrFromDate(release);
						mapLoanInfo.put("publishTime", publishTime);//发标时间
						mapLoanInfo.put("subscribes", loanTraceForDLP(loanInfo.getStr("loanCode")));//标的投资信息
						loans.add(mapLoanInfo);
					}
				}
				map.put("result",1);
				map.put("borrowList",loans);
				map.put("resultmsg","success");

			}catch(Exception e){
				map.put("result",-1);
				map.put("resultmsg","数据对接错误");
				map.put("totalPage","0");
				map.put("currentPage", "1");
				map.put("borrowList",new ArrayList<>());
				renderJson(map);
				e.printStackTrace();
				return;
				
			}
			
		/*}else{
			map.put("result",-1);
			map.put("resultmsg","token失效");
			map.put("totalPage","0");
			map.put("currentPage", "1");
			map.put("borrowList","");

		}*/
		renderJson(map);
		
	}
	
	/**
	 * 单个标的投资数据   dlp
	 * 
	 */
	public List<Map<String, Object>> loanTraceForDLP(String  loanCode){
		List<Map<String,Object>> lists = new ArrayList<Map<String,Object>>();
		
		if(!StringUtil.isBlank(loanCode)){
			
			List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
			if(loanTraces != null && !loanTraces.isEmpty()){
				for(int i = 0;i<loanTraces.size();i++){
					Map<String,Object> map = new HashMap<String,Object>();
					LoanTrace loanTrace = loanTraces.get(i);
					map.put("subscribeUserName",loanTrace.getStr("payUserName"));//投资人
					map.put("amount", Double.parseDouble(StringUtil.getMoneyYuan(loanTrace.getLong("payAmount"))));//投标金额
					map.put("validAmount",Double.parseDouble(StringUtil.getMoneyYuan(loanTrace.getLong("payAmount"))));//有效金额
					String loanDateTime= loanTrace.getStr("loanDateTime");
					Date dateFrom = DateUtil.getDateFromString(loanDateTime,"yyyyMMddHHmmss");
					String addDate = DateUtil.getLongStrFromDate(dateFrom);//投标时间
					map.put("addDate", addDate);
					map.put("status", 1);//投标状态  1：全部通过   2：部分通过
					map.put("type","A".equals(loanTrace.getStr("loanType"))?1:0);//手动或自动
					lists.add(map);
				}
			}
		}
		return lists;
	}
	
	private String getProductTypeName(String type){
		String typeName = "其它";
		switch (type) {
		case "A":
			typeName = "质押宝";
			break;
		case "B":
			typeName = "车稳盈";
			break;
		case "C":
			typeName = "房稳赚";
			break;
		default:
			break;
		}
		return typeName;
	}
	
	/**
	 * 借款标类型
	 * @param type
	 * @return
	 */
	private String getLoanTypeName(String type){
		switch (type) {
		case "A":
			return "信用标";
		case "B":
			return "抵押标";
		case "C":
			return "担保标";
		case "D":
			return "流转标";
		case "E":
			return "质押标";
		case "F":
			return "抵押担保标";
		case "G":
			return "抵押流转标";
		case "H":
			return "担保流转标";
		case "I":
			return "质押流转标";
		case "J":
			return "机构标";
		default:
			return "";
		}
	}
	
/*	private int getRefundType(String refundType){
		if(refundType.equals("A")){
			return 2;
		}else{
			return 7;
		}
		
	}*/
	
	/**
	 * 还款方式
	 * @param refundType
	 * @return
	 */
	private int getRefundType2(String refundType){
		if(refundType.equals("A")){
			return 2;
		}else{
			return 9;
		}
		
	}
	
	
	/**
	 * 借款用途
	 * @param type
	 * @return
	 */
	private String getLoanUsedType(String type){
		String usedName = "其他借款";
		switch (type) {
		case "A":
			usedName = "短期周转";
			break;
		case "B":
			usedName = "个人消费";
			break;
		case "C":
			usedName = "投资创业";
			break;
		case "D":
			usedName = "购车借款";
			break;
		case "E":
			usedName = "装修借款";
			break;
		case "F":
			usedName = "婚礼筹备";
			break;
		case "G":
			usedName = "教育培训";
			break;
		case "H":
			usedName = "医疗支出";
			break;
		case "I":
			usedName = "其他借款";
			break;
		case "J":
			usedName = "购房借款";
			break;
		default:
			break;
		}
		return usedName;
	}
	
}












