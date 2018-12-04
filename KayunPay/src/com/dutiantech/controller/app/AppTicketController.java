package com.dutiantech.controller.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Tickets;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.TicketsService;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class AppTicketController extends BaseController{
	private TicketsService ticketService = getService(TicketsService.class);	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);

	
	/**
	 * 查询用户当前标可以使用的抵用券
	 * */
	@ActionKey("/app_queryUserTicket")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryUserTicket(){
		Message msg=null;
		String userCode = getUserCode();
		Map<String, Object> mapLz = new HashMap<String, Object>();//刘照要求把奖券的list放在一个map里    WCF   20170817
		
		int pageNumber =1;
		int pageSize = 20;
		try {
			pageNumber = getParaToInt("pageNumber",1);
			if(pageNumber < 1)
				pageNumber = 1;
		} catch (Exception e) {
			pageNumber = 1;
		}
//		try {
//			pageSize = getParaToInt("pageSize",20);
//			if(pageSize < 1)
//				pageSize = 20;
//		} catch (Exception e) {
//			pageSize = 20;
//		}
		
		String ttype = getPara("ttype");
		
		String tstate = getPara("tstate");
		
		String makeSource = getPara("makeSource");
		String amt = getPara("amount");
		double amount=0;
		if("0".equals(amt)){
			msg=error("02", "请输入金额", null);
			renderJson(msg);
			return;
		}
		if(null!=amt&&amt.length()>0){
			amount=Double.parseDouble(amt);
			if(amount==0){
				msg=error("02", "请输入金额", null);
				renderJson(msg);
				return;
			}
		}
		
		String beginDate_make = getPara("beginDate_make");
		if(!StringUtil.isBlank(beginDate_make)){
			beginDate_make = beginDate_make + "000000";
		}
		
		String endDate_make = getPara("endDate_make");
		if(!StringUtil.isBlank(endDate_make)){
			endDate_make = endDate_make + "235959";
		}
		
		String beginDate_used = getPara("beginDate_used");
		
		if(!StringUtil.isBlank(beginDate_used)){
			beginDate_used = beginDate_used + "000000";
		}
		
		String endDate_used = getPara("endDate_used");
		
		if(!StringUtil.isBlank(endDate_used)){
			endDate_used = endDate_used + "235959";
		}
		
		String beginDate_expDate = getPara("beginDate_expDate");
		
		String endDate_expDate = getPara("endDate_expDate");
		//20170726 ws
		String loanMonth=getPara("loanMonth");
		int m=0;
		if(null!=loanMonth&&loanMonth.length()>0){
			m=loanMonth.length();
			if(m==1){
				loanMonth="0"+loanMonth;
			}
		}
		if("D".equals(ttype)){
			Map<String,Object> result = new HashMap<String, Object>();
			List<Object> funds=new ArrayList<Object>();
			long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
			Map<String,Object> tmpticket = new HashMap<String, Object>();
			if(rewardRateAmount<amount*100){
				funds.add(tmpticket);
				result.put("list", funds);
				msg=error("01", "加息额度不够", result);
				renderJson(msg);
				return;
			}
			tmpticket.put("examount", Number.longToString(rewardRateAmount));
			tmpticket.put("ttype", "D");
			tmpticket.put("tcode", "rewardrateamountDyrhx");
			tmpticket.put("expDate", "2018-03-31");
			funds.add(tmpticket);
			result.put("list", funds);
			msg= succ("查询完成", result);
			renderJson(msg);
			return;
		}else{
		Map<String,Object> result =  ticketService.findByPage(pageNumber, pageSize, ttype, tstate, makeSource,userCode, beginDate_make, endDate_make, beginDate_used, endDate_used, beginDate_expDate, endDate_expDate,loanMonth);
		@SuppressWarnings("unchecked")
		List<Tickets> list =(List<Tickets>) result.get("list");
		List<Object> ticketlist= new ArrayList<Object>();
		if(list.size()>0){
			for(int i=0;i<list.size();i++){
				Tickets tickets=list.get(i);
				String json=tickets.getStr("useEx");
				JSONObject userEx=(JSONObject) JSONObject.parse(json);
				Long examount=userEx.getLong("amount");
				if(((examount<=amount*100||examount==0)&&"A".equals(ttype))||((examount>=amount*100||examount==0)&&"C".equals(ttype))||(amount==0)){
					//暂时不显示加息券  后期若回归老产品需修改APP 20180831
					if("A".equals(ttype)&&amt!=null){
						continue;
					}
					Map<String,Object> map = new HashMap<String, Object>();
					int amounts=tickets.getInt("amount");
					String expDate=tickets.getStr("expDate");
					String date="";
					date = expDate.substring(0,4);
					date += "-" ;
					date += expDate.substring(4,6);
					date += "-" ;
					date += expDate.substring(6,8);
					map.put("amount", Number.longToString(amounts));
					map.put("rate", String.valueOf((float)(tickets.getInt("rate"))/100.0));
					map.put("expDate", date);
					map.put("examount", String.valueOf(examount/100));
					String tl=tickets.getStr("loanMonth");
					if(null==tl||"0".equals(tl)){
						map.put("loanMonth", "任意");
					}else{
					map.put("loanMonth", tickets.getStr("loanMonth"));
					}
					if("A".equals(tickets.getStr("ttype"))){
					map.put("tname", "现金抵用券");
					}else if("C".equals(tickets.getStr("ttype"))){
						map.put("tname", "加息券");	
					}
					map.put("tCode", tickets.getStr("tCode"));
					ticketlist.add(map);
				}
			}
			mapLz.put("list", ticketlist);
			if(ticketlist.size()>0){
				msg= succ("查询完成", mapLz);
			}else{
				msg=error("01", "没有可以使用的奖券", mapLz);
			}
		}else{
			msg=error("01", "没有可以使用的奖券", mapLz);
		}
		renderJson(msg);
		}
	}
	

	
}
