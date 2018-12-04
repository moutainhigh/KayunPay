package com.dutiantech.controller.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BizLog;
import com.dutiantech.model.User;
import com.dutiantech.service.BizLogService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class BizLogController extends BaseController {
	
	private BizLogService bizLogService = getService(BizLogService.class);
	private UserService userService = getService(UserService.class);
	
	/**
	 * 查询用户操作日志
	 * @return
	 */
	@ActionKey("/queryBizLog")
	@AuthNum(value=999)
	@Before(value=PkMsgInterceptor.class)
	public void queryBizLog(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		pageSize = pageSize > 0 && pageSize <= 20 ? pageSize : 20;
		
		String bizLevel = getPara("bizLevel","");
		
		String opType = getPara("opType","");
		
		String userCode = getPara("allkey","");
		
		String beginDateTime = getPara("beginDateTime","");
		beginDateTime = StringUtil.isBlank(beginDateTime) ? "" : beginDateTime;
		
		String endDateTime = getPara("endDateTime","");
		endDateTime = StringUtil.isBlank(endDateTime) ? "" : endDateTime;

		Page<BizLog> bizLogs = bizLogService.findByPage(pageNumber,pageSize,beginDateTime, endDateTime,userCode,opType,bizLevel);

		Message msg = succ("查询用户操作日志成功", bizLogs);
		renderJson(msg);
	}

	/**
	 * 查询用户操作日志
	 * @return
	 */
	@ActionKey("/getBizLogById")
	@AuthNum(value=999)
	@Before(value=PkMsgInterceptor.class)
	public void getBizLogById(){
		Message msg = new Message();
		String bizNo = getPara("bizNo","");
		if(StringUtil.isBlank(bizNo) == false){
			msg = error("01", "获取失败", "");
		}
		String userCode = getPara("userCode","");
		BizLog bizLog = bizLogService.getBizLogById(bizNo);
		User user = new User();
		if(StringUtil.isBlank(userCode) == false){
			user = userService.findById(userCode);
		}
		Map<String , Object> map = new HashMap<String , Object>();
		map.put("bizLog", bizLog);
		map.put("user", user);
		msg = succ("查询用户操作日志成功", map);
		renderJson(msg);
	}

	/** WJW
	 * 查询用户自动投标设置流水
	 * @return
	 */
	@ActionKey("/queryAutoLoanByPage")
	@AuthNum(value=999)
	@Before(value=PkMsgInterceptor.class)
	public Message queryAutoLoanByPage(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		pageSize = pageSize > 0 && pageSize <= 20 ? pageSize : 20;
		
		String userCode = getPara("userCode","");
		if(StringUtil.isBlank(userCode)){
			return error("01", "获取用户失败", null);
		}
		Page<BizLog> bizLogPage = bizLogService.queryAutoLoanByPage(pageNumber, pageSize, userCode);//查询用户自动投标日志
		List<BizLog> bizLogs = bizLogPage.getList();
		List<Map<String, String>> autoLoanList = new ArrayList<Map<String, String>>();//用户自动投标设置流水
		
		for (BizLog list : bizLogs) {
			Map<String, String> autoLoanMap = new HashMap<String,String>();
			autoLoanMap.put("bizDateTime", list.getStr("bizDateTime"));//设置时间
			String bizData = list.getStr("bizData");//自动投标设置内容
			String userAgent = list.getStr("httpInfo");//浏览器标识
			boolean check = CommonUtil.check(userAgent);
			if(check){
				autoLoanMap.put("accessPort","移动端");
			}else{
				autoLoanMap.put("accessPort","pc端");
			}
			Map<String, String> bizDataMap = new HashMap<String, String>();//自动投标设置内容解析Map
			if(StringUtil.isBlank(bizData)){
				autoLoanMap.put("autoState", "关闭");
			}else {
				autoLoanMap.put("autoState", "开启");
				
				//解析bizData自动投标设置信息
				if(bizData.indexOf(".") != -1){//自动投标设置格式1.0
					String[] strs = bizData.split("&");
				    for ( String s : strs ) {
				    	if(s.indexOf("callback") != -1){
				    		break;
				    	}
				    	String[] ms = null;
				    	if(s.indexOf(".") == -1){
				    		ms = s.split("=");
				    	}else {
				    		ms = s.substring(9).split("=");
						}
					    bizDataMap.put(ms[0], ms.length == 1 ? "" : ms[1]);
				    }
				    bizDataMap.put("onceMaxAmount", "");
				    bizDataMap = autoLoanCompatible(bizDataMap);
				}else if(bizData.indexOf("&") != -1){//自动投标设置格式2.0
					String[] strs = bizData.substring(1).split("&");
				    for ( String s : strs ) {
					    String[] ms = s.split("=");
					    bizDataMap.put(ms[0], ms.length == 1 ? "" : ms[1]);
				    }
				    bizDataMap.put("onceMinAmount", bizDataMap.get("onceMinAmount")+"00");
				    bizDataMap.put("onceMaxAmount", bizDataMap.get("onceMaxAmount")+"00");
				    bizDataMap = autoLoanCompatible(bizDataMap);
				}else if (bizData.indexOf("|") != -1) {//目前自动投标设置格式
					String[] strs = bizData.split("\\|");
				    for ( String s : strs ) {
					    String[] ms = s.split(":");
					    bizDataMap.put(ms[0], ms.length == 1 ? "":ms[1]);
				    }
				}
			}
			
		    if(StringUtil.isBlank(bizDataMap.get("最小金额"))){
		    	autoLoanMap.put("onceMinAmount", "");
		    }else {
		    	autoLoanMap.put("onceMinAmount", "¥ " + Number.longToString(Long.parseLong(bizDataMap.get("最小金额"))));
			}
		    
		    if(StringUtil.isBlank(bizDataMap.get("最大金额"))){
		    	autoLoanMap.put("onceMaxAmount", "");
		    }else {
		    	autoLoanMap.put("onceMaxAmount", "¥ " + Number.longToString(Long.parseLong(bizDataMap.get("最大金额"))));
			}
		    
		    if(StringUtil.isBlank(bizDataMap.get("最小期限"))){
		    	autoLoanMap.put("autoMinLim", "");
		    }else {
		    	autoLoanMap.put("autoMinLim", bizDataMap.get("最小期限"));
			}
		    
		    if(StringUtil.isBlank(bizDataMap.get("最大期限"))){
		    	autoLoanMap.put("autoMaxLim", "");
		    }else {
		    	autoLoanMap.put("autoMaxLim", bizDataMap.get("最大期限"));
			}
		    
		    if(StringUtil.isBlank(bizDataMap.get("还款方式"))){
		    	autoLoanMap.put("refundType", "");
		    }else {
		    	autoLoanMap.put("refundType", refundType(bizDataMap.get("还款方式")));
			}
		    
		    if(StringUtil.isBlank(bizDataMap.get("自动投标类型"))){
		    	autoLoanMap.put("autoType", "");
		    }else {
		    	autoLoanMap.put("autoType", autoType(bizDataMap.get("自动投标类型")));
			}
		    
		    if(!StringUtil.isBlank(bizDataMap.get("使用理财券类型"))){
	    		autoLoanMap.put("useTicket", useTicket(bizDataMap.get("使用理财券类型")));
	    	}else if (!StringUtil.isBlank(bizDataMap.get("是否使用现金券"))) {
	    		autoLoanMap.put("useTicket", useTicket(bizDataMap.get("是否使用现金券")));
			}else {
				autoLoanMap.put("useTicket", "");
			}
		    
		    if(!StringUtil.isBlank(bizDataMap.get("理财券使用优先方式"))){
		    	autoLoanMap.put("priorityMode", priorityMode(bizDataMap.get("理财券使用优先方式")));
		    }else if (!StringUtil.isBlank(bizDataMap.get("现金券使用优先方式"))) {
		    	autoLoanMap.put("priorityMode", priorityMode(bizDataMap.get("现金券使用优先方式")));
			}else {
				autoLoanMap.put("priorityMode", "");
			}
		    
		    autoLoanList.add(autoLoanMap);
		}
		return succ("01", new Page<>(autoLoanList, pageNumber, pageSize, bizLogPage.getTotalPage(), bizLogPage.getTotalRow()));
	}
	
	/** WJW
	 * 自动投标类型
	 * @return
	 */
	public String autoType(String type){
		switch (type) {
		case "A":
			return "智能投标";
		case "B":
			return "自定义投标";
		default:
			return "";
		}
	}
	
	/** WJW
	 * 还款方式
	 * @param type
	 * @return
	 */
	public String refundType(String type)	{
		switch (type) {
			case "A":
				return "等额本息";
			case "B":
				return "先息后本";		
			case "C":
				return "到期还本息";
			case "D":
				return "不限";
			case "N"://兼容旧格式
				return "不限";
			default:
				return "";
		}
	}
	
	/** WJW
	 * 使用理财券类型
	 * @return
	 */
	public String useTicket(String type){
		switch (type) {
			case "A":
				return "现金券";
			case "C":
				return "加息券";
			case "D":
				return "加息额度";
			case "N":
				return "不使用";
			default:
				return "";
		}
	}
	
	/** WJW
	 * 理财券使用优先方式
	 * @param type
	 * @return
	 */
	public String priorityMode(String type){
		switch (type) {
		case "A":
			return "期限优先";
		case "B":
			return "金额优先";
		case "C":
			return "利率优先";
		case "N":
			return "停用";
		default:
			return "";
		}
	}
	
	/** WJW
	 * 自动投标设置字段名兼容处理
	 * @param map
	 * @return
	 */
	public Map<String, String> autoLoanCompatible(Map<String, String> map){
		HashMap<String, String> autoLoanMap = new HashMap<String,String>();
		autoLoanMap.put("最小期限", StringUtil.isBlank(map.get("autoMinLim"))?"":map.get("autoMinLim"));
		autoLoanMap.put("最大期限", StringUtil.isBlank(map.get("autoMaxLim"))?"":map.get("autoMaxLim"));
		autoLoanMap.put("自动投标类型", StringUtil.isBlank(map.get("autoType"))?"":map.get("autoType"));
		autoLoanMap.put("最小金额", StringUtil.isBlank(map.get("onceMinAmount"))?"":map.get("onceMinAmount"));
		autoLoanMap.put("最大金额", StringUtil.isBlank(map.get("onceMaxAmount"))?"":map.get("onceMaxAmount"));
		autoLoanMap.put("理财券使用优先方式", StringUtil.isBlank(map.get("priorityMode"))?"":map.get("priorityMode"));
		autoLoanMap.put("还款方式", StringUtil.isBlank(map.get("refundType"))?"":map.get("refundType"));
		autoLoanMap.put("使用理财券类型", StringUtil.isBlank(map.get("useTicket"))?"":map.get("useTicket"));
		return autoLoanMap;
	}
}




