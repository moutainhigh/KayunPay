package com.dutiantech.controller.app;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.portal.FundsController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.RecommendInfo;
import com.dutiantech.model.RecommendReward;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.RecommendInfoService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

/**
 * 活动专题相关接口
 * 
 * @author shiqingsong
 *
 */
public class ActivityController extends BaseController {

	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private RecommendRewardService recommendRewardService = getService(RecommendRewardService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private UserService userService = getService(UserService.class);
	private RecommendInfoService recommendInfoService = getService(RecommendInfoService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	
	/**
	 * 12月邀请好友闯关活动
	 * 被邀请人   投资金额    投资时间
	 * @return
	 */
	@ActionKey("/app_queryInviteDetailsByUserCode")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class })
	public Message queryInviteDetailsByUserCode(){
		String userCode = getUserCode();//获取登录用户的userCode
		
		String beginDate = getPara("beginDate","");//获取开始时间
		String endDate = getPara("endDate","");//获取结束时间
		if (StringUtil.isBlank(beginDate)) {
			return error("02", "开始时间错误", null);
		}
		
		// 如果结束日期为空，则设结束时间为当前日期
		if (StringUtil.isBlank(endDate)) {
			endDate = DateUtil.getNowDate();
		}
		
		
		//活动开始的时间不能大于结束时间
		if (!(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == -1)) {
			return error("01", "活动时间有误，请检查时间", "");
		}
		
		//查询邀请好友的人数
		List<UserInfo> userInfoList = userInfoService.queryRecommendByUserCode(userCode, beginDate, endDate);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("numInvite", userInfoList.size());//集合长度表示邀请人数
		Long sumAmount = fundsTraceService.sumInvestAmount4BeRecommondByInvite(userCode, beginDate, endDate);//邀请好友总投资额
		map.put("sumAmount",Number.longToString(sumAmount));
		//被邀请人投资详情
		List<FundsTrace> lists = fundsTraceService.queryInviteListByUserCode(userCode, beginDate, endDate);
		if(lists.size()>0){
			for(FundsTrace fundsTrace : lists){
			long traceAmount = fundsTrace.get("traceAmount");
				fundsTrace.set("traceAmount",Number.longToString(traceAmount));
				fundsTrace.set("traceDate",DateUtil.chenageDay(fundsTrace.get("traceDate").toString()));
				
			}
		}
		map.put("list",lists);
		return succ("查询成功", map);
	}
	
	 /**
	 * WJW
	 * 赏金计划 
	 * 查看我的邀请详情
	 * @return
	 */
	 @ActionKey("/app_queryRewardByUserCode")
	 @AuthNum(value = 999)
	 @Before({AppInterceptor.class,PkMsgInterceptor.class })
	 public Message queryRewardByUserCode(){
		 String beginDate = getPara("beginDate");	// 开始日期
		 String endDate = getPara("endDate");	// 结束日期
		 if (StringUtil.isBlank(endDate)) {
			 endDate = DateUtil.getNowDate();
		 }
		 
		 int pageNumber = getPageNumber();
		 int pageSize = getPageSize();
		 String userCode = getUserCode();
		 FundsController fundsController = new FundsController();
		 
		 List<RecommendReward> recommendRewards = recommendRewardService.queryRecommendRewardByAUserCodeDateRewardType(userCode, beginDate, endDate, "C");//查询用户获得推荐奖励信息
		 HashMap<String, Long> rewardAcquireMap = new HashMap<String,Long>();//已回收赏金:被推荐用户 回收金额
		 for (RecommendReward recommendReward : recommendRewards) {
			 Long rewardAmount = recommendReward.getLong("rewardAmount");//奖励金额
			 String bUserCode = recommendReward.getStr("bUserCode");//被推荐用户userCode
			 if(rewardAcquireMap.containsKey(bUserCode)){//判断赏金回款中，被推荐用户是否出现过
				 rewardAcquireMap.put(bUserCode, rewardAcquireMap.get(bUserCode)+rewardAmount);
			 }else {
				 rewardAcquireMap.put(bUserCode, rewardAmount);
			}
		}
		 Page<LoanTrace> loanTraces = loanTraceService.queryTraceByDatePage(userCode, beginDate, endDate, pageNumber, pageSize);
		 Map<String, List<Object>> moneyRewardMap = new HashMap<String,List<Object>>();
		 for (LoanTrace loanTrace : loanTraces.getList()) {
			 Long rewardAmount=0L;
			 String bUserCode = loanTrace.getStr("payUserCode");//被推荐人UserCode
			 String bUserName = loanTrace.getStr("payUserName");//被推荐人UserName
			 Long payAmount = loanTrace.get("payAmount");//投标金额
			 if(moneyRewardMap.containsKey(bUserCode)){//被推荐用户多次有效投标
				 List<Object> list = moneyRewardMap.get(bUserCode);
				 list.set(1, ((long)(list.get(1))+payAmount));
				 list.set(2, ((long)(list.get(2)) + fundsController.rewardRemain(loanTrace)));
				 continue;
			 }
			 if(rewardAcquireMap.containsKey(bUserCode)){//推荐用户是否获取赏金回款
				 rewardAmount=fundsController.rewardRemain(loanTrace)+rewardAcquireMap.get(bUserCode);
			 }else {
				 rewardAmount=fundsController.rewardRemain(loanTrace);
			 }
			 List<Object> moneyReward = new ArrayList<Object>();//单条被推荐人累计赏金信息
			 moneyReward.add(bUserName);
			 moneyReward.add(payAmount);
			 moneyReward.add(rewardAmount);
			 moneyRewardMap.put(bUserCode, moneyReward);
		 }
		 List<List<Object>> moneyRewardList = new ArrayList<List<Object>>();
		 //转换金额格式
		 for ( List<Object> list : moneyRewardMap.values()) {
			 list.set(1, "¥ " + Number.longToString(Long.parseLong(list.get(1).toString())));
			 list.set(2, "¥ " + Number.longToString(Long.parseLong(list.get(2).toString())));
			 moneyRewardList.add(list);
		 }
		 return succ("查询成功", moneyRewardList);
	}
	 
	 /**
	 * 赏金计划——邀请信息总览
	 * @return
	 */
	@ActionKey("/appRewardPlanByUsercode")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class, PkMsgInterceptor.class })
	public Message appRewardPlanByUsercode(){
		try {
			String userCode = getUserCode();//获取用户userCode
			if(StringUtil.isBlank(userCode)){
				return error("01", "用户未登录！", null);
			}
			
			String beginDate = getPara("beginDate", "");
			String endDate = getPara("endDate", "");
			
			if(StringUtil.isBlank(beginDate)){
				return error("02", "活动开始时间有误", null);
			}
			//结束日期
			if(StringUtil.isBlank(endDate)){
				endDate = DateUtil.getNowDate();
			}
			if(!(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == -1)){
				return error("01", "活动时间有误，请检查！", "");
			}
			Map<String, Object> map = new HashMap<>();
			
			//查询用户的邀请码
			User user = userService.findById(userCode);
			map.put("inviteCode", user.getStr("inviteCode"));//邀请码;
			/*
			 * 推荐总人数             recommendTotalNum
			 * 实际投资人数	actualInvestNum
			 */
			int recommendTotalNum = 0;
			int actualInvestNum = 0;
			
			//查出所有被推荐人
			RecommendInfo recommendInfo = recommendInfoService.queryBuserCodeAndInvest(userCode, beginDate, endDate);
			if(recommendInfo != null ){
				recommendTotalNum = recommendInfo.getLong("rNum").intValue();
				actualInvestNum = recommendInfo.getLong("aNum").intValue();
			}
			
			map.put("recommendTotalNum", recommendTotalNum);
			map.put("actualInvestNum", actualInvestNum);
			
			String nowDate = DateUtil.getNowDate();
			String cmFirstDay = "";//本月第一天
			String cmLastDay = "";//本月最后一天
			try {
				Date[] dates = DateUtil.getMonthBetween(DateUtil.getDateFromString(nowDate, "yyyyMMdd"));
				cmFirstDay = DateUtil.getStrFromDate(dates[0], "yyyyMMdd");
				cmLastDay = DateUtil.getStrFromDate(dates[1], "yyyyMMdd");
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			/*	
			 * 累计赏金收益	sumReward
			 * 本月赏金收益	cmReward
			 */
			Long sumReward = 0L;
			Long cmReward = 0L;
			
			sumReward = getReward(userCode, beginDate, endDate, cmFirstDay, cmLastDay, 0);
			map.put("sumReward", Number.doubleToStr((double)sumReward));
			
			cmReward = getReward(userCode, beginDate, endDate, cmFirstDay, cmLastDay, 1);
			map.put("cmReward", Number.doubleToStr((double)cmReward));
			return succ("查询成功", map);
		} catch (Exception e) {
			e.printStackTrace();
			return error("05", "查询失败，请联系客服！", null);
		}
	}
	
	/**
	 * 根据用户编码查询用户的预期赏金
	 * @param userCode		推荐人编码
	 * @param beginDate		活动开始时间
	 * @param endDate		活动结束时间
	 * @param cmFirstDay	本月第一天
	 * @param cmLastDay		本月最后一天
	 * @param type			0：累计赏金收益  1：本月赏金收益
	 * @return
	 */
	public Long getReward(String userCode, String beginDate, String endDate, String cmFirstDay, String cmLastDay, int type){
		/*
		 * 待赚取赏金		tempBeRecyReward
		 * 已赚取赏金		tempReciedReward	
		 * 累计赏金收益	tempSumReward
		 */
		Long tempBeRecyReward = 0L;
		Long tempReciedReward = 0L;
		Long tempSumReward = 0L;
		
		List<LoanTrace> loanTraces = null;
		if(type == 0){//累计
			FundsController fundsController = new FundsController();
			loanTraces = loanTraceService.queryTraceByDate(userCode, beginDate, endDate);
			for (LoanTrace loanTrace : loanTraces) {
				tempBeRecyReward += fundsController.rewardRemain(loanTrace);//待赚取
			}
			//已赚取
			tempReciedReward = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType.W.val(), fundsType.J.val(), userCode);
		}else if(type == 1){//本月
			loanTraces = loanTraceService.queryCMTraceByDate(userCode, beginDate, endDate, cmFirstDay, cmLastDay);
			for (LoanTrace loanTrace : loanTraces) {
				if("N".equals(loanTrace.getStr("loanState"))){
					loanTrace.set("loanRecyCount",loanTrace.getInt("loanRecyCount")-1 );
					tempBeRecyReward += rewardAmount(loanTrace);//待赚取
				}
			}
			//已赚取
			tempReciedReward = fundsTraceService.sumTraceAmount(cmFirstDay, cmLastDay, traceType.W.val(), fundsType.J.val(), userCode);
		}
		//赏金收益
		tempSumReward = tempBeRecyReward + tempReciedReward;
		return tempSumReward;
	}
	
	/**
	 * 赏金计划——查询用户的赏金详情
	 * @return
	 */
	@ActionKey("/queryRewardDetailByUserCode")
	@AuthNum(value = 999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class })
	public Message queryRewardDetailByUserCode(){
		String userCode = getUserCode();
		if(StringUtil.isBlank(userCode)){
			return error("01", "用户未登录！", null);
		}
		
		String beginDate = getPara("beginDate");  // 开始日期
		String endDate = getPara("endDate");	   // 结束日期
		
		if(StringUtil.isBlank(beginDate)){
			return error("02", "活动开始时间有误", null);
		}
		if (StringUtil.isBlank(endDate)) {
			endDate = DateUtil.getNowDate();
		}
		if(!(DateUtil.compareDateByStr("yyyyMMdd", beginDate, endDate) == -1)){
			return error("01", "活动时间有误，请检查！", "");
		}
		 
		FundsController fundsController = new FundsController();
		 
		List<RecommendReward> recommendRewards = recommendRewardService.queryRecommendRewardByAUserCodeDateRewardType(userCode, beginDate, endDate, "C");//查询用户获得推荐奖励信息
		HashMap<String, Long> rewardAcquireMap = new HashMap<String,Long>();//已回收赏金:被推荐用户 回收金额
		for (RecommendReward recommendReward : recommendRewards) {
			Long rewardAmount = recommendReward.getLong("rewardAmount");//奖励金额
			String bUserCode = recommendReward.getStr("bUserCode");//被推荐用户userCode
			if(rewardAcquireMap.containsKey(bUserCode)){//判断赏金回款中，被推荐用户是否出现过
			  rewardAcquireMap.put(bUserCode, rewardAcquireMap.get(bUserCode)+rewardAmount);
			}else {
				rewardAcquireMap.put(bUserCode, rewardAmount);
		    }
		}
		List<LoanTrace> loanTraces = loanTraceService.queryRecommendedTraceByDate(userCode, beginDate, endDate);
		Map<String, List<Object>> moneyRewardMap = new HashMap<String,List<Object>>();
		for (LoanTrace loanTrace : loanTraces) {
			Long rewardAmount=0L;
			String bUserCode = loanTrace.getStr("payUserCode");//被推荐人UserCode
			String bUserName = loanTrace.getStr("payUserName");//被推荐人UserName
			Long payAmount = loanTrace.get("payAmount");//投标金额
			if(moneyRewardMap.containsKey(bUserCode)){//被推荐用户多次有效投标
				List<Object> list = moneyRewardMap.get(bUserCode);
				list.set(1, ((long)(list.get(1))+payAmount));
				list.set(2, ((long)(list.get(2)) + fundsController.rewardRemain(loanTrace)));
				continue;
			}
			if(rewardAcquireMap.containsKey(bUserCode)){//推荐用户是否获取赏金回款
				rewardAmount=fundsController.rewardRemain(loanTrace)+rewardAcquireMap.get(bUserCode);
			}else {
				rewardAmount=fundsController.rewardRemain(loanTrace);
			}
		    List<Object> moneyReward = new ArrayList<Object>();//单条被推荐人累计赏金信息
			moneyReward.add(bUserName);
			moneyReward.add(payAmount);
			moneyReward.add(rewardAmount);
			moneyRewardMap.put(bUserCode, moneyReward);
		 }
		 List<List<Object>> moneyRewardList = new ArrayList<List<Object>>();
		 //转换金额格式
		 for ( List<Object> list : moneyRewardMap.values()) {
			 list.set(1,  Number.longToString(Long.parseLong(list.get(1).toString())));
			 list.set(2,  Number.longToString(Long.parseLong(list.get(2).toString())));
			 moneyRewardList.add(list);
		 }
		 return succ("查询成功", moneyRewardList);
	}
	
	/**
	 * 赏金计划
	 * 每期返现
	 * @param loanTrace
	 * @return
	 */
	public long rewardAmount(LoanTrace loanTrace){
		String json_ticket = loanTrace.getStr("loanTicket");
		Long payAmount=loanTrace.getLong("payAmount");
		boolean isTransfer = loanTransferService.vilidateIsTransfer(loanTrace.getStr("traceCode"));//标是否有过债权转让
		if(isTransfer){//发生过债权转让
			return 0L;
		}
		
		String code = "";//奖券code
		Integer rate = 0;//奖励利率
		if(!StringUtil.isBlank(json_ticket)){//使用了抵用券
			String type="";//抵用券类型
			Long amount=0L;//抵用券金额(A、B)
			JSONArray jsonArray = JSONArray.parseArray(json_ticket);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				type = jsonObject.getString("type");
				amount = jsonObject.getLong("amount");
				code = jsonObject.getString("code");
				rate = jsonObject.getInteger("rate");
			}
			
			if("C".equals(type) && !Tickets.rewardRateInterestTcode.equals(code)){//使用了加息券,加息额度(不包含会员自动加息)
				return 0L;
			}
			if("A".equals(type)||"B".equals(type)){//使用了现金抵用券
				payAmount=payAmount-amount;
			}
		}
		Integer loanTimeLimit=loanTrace.getInt("loanTimeLimit");
		Integer rateByYear=loanTrace.getInt("rateByYear");
		Integer rewardRateByYear=loanTrace.getInt("rewardRateByYear");
		Integer loanRecyCount=loanTrace.getInt("loanRecyCount");
		String refundType=loanTrace.getStr("refundType");
		
		if(Tickets.rewardRateInterestTcode.equals(code)){//使用了会员自动加息,不享受会员加息部分赏金 
			return (CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear+rewardRateByYear, loanTimeLimit-loanRecyCount, refundType)[1]*5/100)*(rateByYear+rewardRateByYear-rate)/(rateByYear+rewardRateByYear);
		}
		return CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear+rewardRateByYear, loanTimeLimit-loanRecyCount, refundType)[1]*5/100;
	}
}
