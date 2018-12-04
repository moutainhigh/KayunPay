package com.dutiantech.controller.app;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.CACHED;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.JXappController;
import com.dutiantech.interceptor.AppInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.AutoLoan_v2;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.AutoLoanService;
import com.dutiantech.service.AutoMapSerivce;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.QueryBalanceResultData;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

public class AppLoanInfoController extends BaseController {
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanApplyService loanApplyService = getService(LoanApplyService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class ) ;
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private UserService userService = getService(UserService.class);
	private AutoLoanService autoLoanService = getService(AutoLoanService.class);
	private AutoMapSerivce autoMapService = getService(AutoMapSerivce.class);
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private BanksService banksService=getService(BanksService.class);
	private YiStageUserInfoService yiStageUserInfoService=getService(YiStageUserInfoService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	/**
	 * 查询标书投资项目列表
	 * 
	 * @param pageNumber
	 * @param pageSize
	 */
	@ActionKey("/appQueryFinancialBid")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	@ResponseCached(cachedKey = "t_queryFinancialBid", cachedKeyParm = "pageNumber|pageSize|type|minLimit|maxLimit|productType", mode = "remote", time = 2)
	public void appQueryFinancialBid() {
		Message msg = null;
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String type = "";
		String minLimit = "1";
		String maxLimit = "24";
		String productType = "";
		String loanState = "J,M,N,O,P,Q,R";

		DecimalFormat df = new DecimalFormat("0.00");
		Map<String, Object> map = new HashMap<String, Object>();
		Page<LoanInfo> loanInfos = loanInfoService.findByPortal(pageNumber,
				pageSize, loanState, type, productType, minLimit, maxLimit);
		//
		List<LoanInfo> list = loanInfos.getList();
		for (int i = 0; i < list.size(); i++) {
			LoanInfo loanInfo = list.get(i);
			int bfb = (int) (100 * (loanInfo.getLong("loanAmount") - loanInfo
					.getLong("loanBalance")) / loanInfo.getLong("loanAmount"));
			String percent = df.format(bfb / 100.00);
			loanInfo.put("percent", percent);
			loanInfo.put("reward", loanInfo.getRewardRateByYear_());
			loanInfo.put("rewardRateByYear", loanInfo.getRewardRateByYear());
			loanInfo.put("tip", loanInfo.getTip_());
		}
		//app在投新手标置顶
		Collections.sort(list,new Comparator<LoanInfo>() {
			@Override
			public int compare(LoanInfo o1, LoanInfo o2) {
				
				if(o1.getStr("loanState").equals("J") && o2.getStr("loanState").equals("J")){
					int i = o2.getInt("benefits4new")-o1.getInt("benefits4new");//逆序
					
					return i;
				}
				return 0;
			}
		});
		//
		map.put("loanInfos", loanInfos);
		map.put("serverTime", DateUtil.getNowDateTime());
		msg = succ("获取成功", map);
		renderJson(msg);
	}
	
	/**
	 * 查询详细的标书信息页面
	 * 
	 * @return
	 * @throws Exception
	 */
	@ActionKey("/appQueryBidDetail")
	@Before({ PkMsgInterceptor.class })
	@ResponseCached(cachedKey = "appQueryBidDetail", cachedKeyParm = "loanCode", mode = "remote", time = 2)
	public void appQueryBidDetail() throws Exception {
		Message msg = null;
		String loanCode = getPara("loanCode");
		if (StringUtil.isBlank(loanCode)) {
			msg = error("01", "参数错误", "");
			renderJson(msg);
		}

		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		Map<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("productType", loanInfo.getStr("productType"));
		tmp.put("rateByYear", loanInfo.getInt("rateByYear"));
		if (loanInfo.getInt("benefits4new") == 0) {
			tmp.put("rewardRateByYear", loanInfo.getInt("rewardRateByYear"));
			
			long releaseDate = Long.parseLong(loanInfo.getStr("releaseDate"));
			if (loanInfo.getInt("benefits4new") == 0) {
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					tmp.put("newRewardRateByYear", "4.0");
					} else {
						tmp.put("newRewardRateByYear", String.valueOf(loanInfo.getInt("rewardRateByYear")/100.0));
					}
			}
		} else {
			tmp.put("benefits4new", loanInfo.getInt("benefits4new"));
			tmp.put("newBenefits4new", loanInfo.getInt("benefits4new")/100.0);
		}
		tmp.put("loanBalance", loanInfo.getLong("loanBalance"));
		tmp.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
		String tmpRefundType = loanInfo.getStr("refundType");
		if ("A".equals(tmpRefundType)) {
			tmpRefundType = "等额本息";
		} else if ("B".equals(tmpRefundType)) {
			tmpRefundType = "先息后本";
		}
		tmp.put("refundType", tmpRefundType);
		tmp.put("loanAmount", loanInfo.getLong("loanAmount"));
		tmp.put("releaseDate", loanInfo.getStr("releaseDate"));
		tmp.put("releaseTime", loanInfo.getStr("releaseTime"));
		String endDateTime = DateUtil
				.subDay(loanInfo.getStr("releaseDate"), -3)
				+ loanInfo.getStr("releaseTime");
		tmp.put("endDateTime", endDateTime);
		String tmpLastPayLoanDateTime = loanInfo.getStr("lastPayLoanDateTime");
		String lastPayLoanDateTime = "";
		if ("00000000000000".equals(tmpLastPayLoanDateTime)) {
			lastPayLoanDateTime = "";
		} else {
			lastPayLoanDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(tmpLastPayLoanDateTime));
		}
		tmp.put("lastPayLoanDateTime", lastPayLoanDateTime);
		String loanNo = loanInfo.getStr("loanNo");
		String productType = loanInfo.getStr("productType");
		if ("A".equals(productType)) {
			productType = "质押宝";
		} else if ("B".equals(productType)) {
			productType = "车稳盈";
		} else if ("C".equals(productType)) {
			productType = "房稳赚";
		} else if ("D".equals(productType)) {
			productType = "其它";
		} else if ("E".equals(productType)) {
			productType = "易分期";
		} else if ("G".equals(productType)) {
			productType = "稳定投";
		}
		String loanTitle = productType + loanNo;
		tmp.put("loanTitle", loanTitle);
		tmp.put("loanNO", loanNo);
		tmp.put("loanBrief", "");
		if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))){
			tmp = resetYistageLoanDesc(tmp, loanInfo);
		}else{
			int tmpLoanNo = Integer.parseInt(loanNo);
			LoanApply loanApply = loanApplyService.findById(tmpLoanNo);
			if(null != loanApply){
				String loanDesc = loanApply.getStr("loanDesc");
				JSONArray array = JSONArray.parseArray(loanDesc);
				for (int i = 0; i < array.size(); i++) {
					JSONObject json = array.getJSONObject(i);
					String title = json.getString("title");
					String content = json.getString("content");
					if (title.indexOf("借款人") != -1) {
						tmp.put("loanBrief", content.replaceAll("\n", ""));
						break;
					}
				}
			}
		}
		tmp.put("loanCode", loanInfo.getStr("loanCode"));
		tmp.put("serverTime", DateUtil.getNowDateTime());
		int bfb = (int) (100 * (loanInfo.getLong("loanAmount") - loanInfo
				.getLong("loanBalance")) / loanInfo.getLong("loanAmount"));
		tmp.put("bfb", bfb);
		tmp.put("tip", loanInfo.getTip_());

		msg = succ("获取成功", tmp);
		renderJson(msg);
	}
	
	/**
	 * 标书详情页面_项目描述
	 * @return
	 */
	@ActionKey("/appQueryLoanDesc")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void appQueryLoanDesc() {
		Message msg=null;
		String loanCode = getPara("loanCode");
		String ver = getPara("ver");
		if (StringUtil.isBlank(loanCode)) {
			msg= error("01", "参数错误", "");
			renderJson(msg);
		}
		
		Map<String, Object> tmp = new HashMap<String, Object>();
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		tmp.put("productType", loanInfo.getStr("productType"));
		if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))){
			if(StringUtil.isBlank(ver)){
				tmp.put("loanBrief", "");
				tmp.put("loanUseWay", "");
				tmp.put("clxx", "");
				tmp.put("wqkc", "");
				tmp.put("fksh", "");
			}else{
				tmp = resetYistageLoanDesc(tmp, loanInfo);
			}
		}else{
			int loanNo = Integer.parseInt(loanInfo.getStr("loanNo"));
			LoanApply loanApply = loanApplyService.findById(loanNo);
			String loanDesc = loanApply.getStr("loanDesc");
			JSONArray array = JSONArray.parseArray(loanDesc);
			for (int i = 0; i < array.size(); i++) {
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("title");
				String content = json.getString("content");
				if (title.indexOf("借款人") != -1) {
					tmp.put("loanBrief", content.replaceAll("\n", ""));
				} else if (title.indexOf("借款用途") != -1) {
					tmp.put("loanUseWay", content);
				} else if (title.indexOf("车辆") != -1) {
					String clxx = content.replaceAll("<br>", "");
					tmp.put("clxx", clxx.replaceAll("\n", ""));
				} else if (title.indexOf("外勤") != -1) {
					tmp.put("wqkc", content);
				} else if (title.indexOf("风控") != -1) {
					tmp.put("fksh", content);
				}
			}
		}

		msg = succ("获取成功", tmp);
		renderJson(msg);
	}
	
	/**
	 * 标书详情页面_借款人信息
	 * @return
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/appQueryLoanUser")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void appQueryLoanUser() throws NoSuchFieldException, SecurityException {
		Message msg=null;
		String ver = getPara("ver");
		String loanCode = getPara("loanCode");
		if (StringUtil.isBlank(loanCode)) {
			msg= error("01", "参数错误", "");
			renderJson(msg);
		}

		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		Map<String, Object> tmp = new HashMap<String, Object>();
		tmp.put("productType", loanInfo.getStr("productType"));
		if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))){
			if(StringUtil.isBlank(ver)){
				tmp.put("loanUserName", "");
				tmp.put("hasMarital", "");
				tmp.put("userCSHY", "");
				tmp.put("userZW", "");
				tmp.put("hasHouse", "");
				tmp.put("hasCar", "");
				tmp.put("hasHouseLoan", "");
				tmp.put("hasCarLoan", "");
				tmp.put("loanAddress", "");
				tmp.put("loanUserInfo", convertLoanUserInfo(null));
			}else{
				tmp = resetYistageLoanDesc(tmp, loanInfo);
			}
		}else{
			String loanUserInfo = loanApplyService.findLoanUserDetail(loanInfo
					.getStr("loanNo"));
			if (StringUtil.isBlank(loanUserInfo) == false) {
				JSONObject xx = JSONObject.parseObject(loanUserInfo);
				if (null != xx) {
					tmp.put("loanUserName", xx.get("loanUserName"));
					
					if ("0".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "未婚");
					} else if ("1".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "已婚");
					} else if ("2".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "离异");
					} else if ("3".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "丧偶");
					}
	
					tmp.put("userCSHY", xx.get("userCSHY") == null ? "" : xx.get("userCSHY"));
	
					tmp.put("userZW", xx.get("userZW") == null ? "" : xx.get("userZW"));
	
					if ("0".equals(xx.get("hasHouse"))) {
						tmp.put("hasHouse", "无");
					} else if ("1".equals(xx.get("hasHouse"))) {
						tmp.put("hasHouse", "有");
					}
	
					if ("0".equals(xx.get("hasCar"))) {
						tmp.put("hasCar", "无");
					} else if ("1".equals(xx.get("hasCar"))) {
						tmp.put("hasCar", "有");
					}
	
					if ("0".equals(xx.get("hasMortgage"))) {
						tmp.put("hasHouseLoan", "无");
					} else if ("1".equals(xx.get("hasMortgage"))) {
						tmp.put("hasHouseLoan", "有");
					}
	
					if ("0".equals(xx.get("hasCarLoan"))) {
						tmp.put("hasCarLoan", "无");
					} else if ("1".equals(xx.get("hasCarLoan"))) {
						tmp.put("hasCarLoan", "有");
					}
	
					tmp.put("loanAddress", xx.get("loanAddress") == null ? "" : xx.get("loanAddress"));
	
					tmp.put("loanUserInfo", convertLoanUserInfo(xx));
				}
			}
		}
		String loan_pic = loanInfo.getStr("loan_pic");
		JSONArray array = JSONArray.parseArray(loan_pic);
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			String url = "http://image1.yrhx.com/" + json.getString("code") + "/loan";
			json.put("url", url);
			json.remove("code");
		}
		List<Object> objects = (List<Object>) JSONArray.toJSON(array);
		tmp.put("loan_pic", objects);

		msg = succ("获取成功", tmp);
		renderJson(msg);
	}
	
	// 转换借款人信用信息
	private JSONArray convertLoanUserInfo(JSONObject loanUserInfo) {
		JSONArray jsonArray = new JSONArray();
		if(loanUserInfo==null){
			for(int i=0;i<13;i++){
				JSONObject json = new JSONObject();
				switch (i) {
				case 0:
					json.put("key", "借款用户");
					json.put("value", "");
					break;
				case 1:
					json.put("key", "借款人主体性质");
					json.put("value", "");
					break;
				case 2:
					json.put("key", "婚姻状况");
					json.put("value", "");
					break;
				case 3:
					json.put("key", "从事行业");
					json.put("value", "");
					break;
				case 4:
					json.put("key", "年收入");
					json.put("value", "");
					break;
				case 5:
					json.put("key", "有无购房");
					json.put("value", "");
					break;
				case 6:
					json.put("key", "有无购车");
					json.put("value", "");
					break;
				case 7:
					json.put("key", "有无房贷");
					json.put("value", "");
					break;
				case 8:
					json.put("key", "有无车贷");
					json.put("value", "");
					break;
				case 9:
					json.put("key", "逾期情况");
					json.put("value", "");
					break;
				case 10:
					json.put("key", "其他平台借款情况");
					json.put("value", "");
					break;
				case 11:
					json.put("key", "负债情况");
					json.put("value", "");
					break;
				case 12:
					json.put("key", "借款人地址");
					json.put("value", "");
					break;
				default:
					break;
				}
				jsonArray.add(json);
			}
		}else{
			Set<String> set = loanUserInfo.keySet();
			for (String setKey : set) {
				JSONObject json = new JSONObject();
				String key = "";
				String value = loanUserInfo.getString(setKey) == null ? "" : loanUserInfo.getString(setKey);
				if ("loanUserName".equals(setKey)) {
					key = "借款用户";
				}
				if ("loanUserType".equals(setKey)) {
					key = "借款人主体性质";
					if ("0".equals(value)) {
						value = "个人";
					} else if ("1".equals(value)) {
						value = "公司";
					}
				}
				if ("hasMarital".equals(setKey)) {
					key = "婚姻状况";
					if ("0".equals(value)) {
						value = "未婚";
					} else if ("1".equals(value)) {
						value = "已婚";
					} else if ("2".equals(value)) {
						value = "离异";
					} else if ("3".equals(value)) {
						value = "丧偶";
					}
				}
				if ("userCSHY".equals(setKey)) {
					key = "从事行业";
				}
//			if ("userZW".equals(setKey)) {
//				key = "职位";
//			}
				if ("annualIncome".equals(setKey)) {
					key = "年收入";
				}
				if ("hasHouse".equals(setKey)) {
					key = "有无购房";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasCar".equals(setKey)) {
					key = "有无购车";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasMortgage".equals(setKey)) {
					key = "有无房贷";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasCarLoan".equals(setKey)) {
					key = "有无车贷";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasOverdue".equals(setKey)) {
					key = "逾期情况";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasOtherP2pLoan".equals(setKey)) {
					key = "其他平台借款情况";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("hasLiabilities".equals(setKey)) {
					key = "负债情况";
					if ("0".equals(value)) {
						value = "无";
					} else if ("1".equals(value)) {
						value = "有";
					}
				}
				if ("loanAddress".equals(setKey)) {
					key = "借款人地址";
				}
				if ("userTrueName".equals(setKey)) {
					key = "借款人姓名";
				}
				if ("loanUserSex".equals(setKey)) {
					key = "性别";
				}
				if ("loanUserAge".equals(setKey)) {
					key = "年龄";
				}
				if ("loanUserWorkCity".equals(setKey)) {
					key = "工作城市";
				}
				if ("loanUserWorkType".equals(setKey)) {
					key = "工作类型";
				}
				if ("loanUserPurpose".equals(setKey)) {
					key = "借款目的";
				}
				if ("loanUserEducation".equals(setKey)) {
					key = "学历";
				}
				if ("userCardId".equals(setKey)) {
					key = "身份证";
				}
				if ("inCome".equals(setKey)) {
					key = "年收入";
				}
				if ("debt".equals(setKey)) {
					key = "负债状况";
				}
				if ("credit".equals(setKey)) {
					key = "征信逾期状况";
				}
				if ("loanUserPurpose".equals(setKey)) {
					key = "借款用途";
				}
				if (!"".equals(key)) {
					json.put("key", key);
					json.put("value", value);
					jsonArray.add(json);
				}
			}
		}
		return jsonArray;
	}
	
	/**
	 * 标书详情页面_投标流水记录
	 * 
	 * @return
	 * @throws ParseException
	 */
	@ActionKey("/appQueryLoanTrace")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void appQueryLoanTrace() throws ParseException {
		Message msg = null;
		String loanCode = getPara("loanCode");
		if (StringUtil.isBlank(loanCode)) {
			msg = error("01", "参数错误", "");
			renderJson(msg);
		}

		List<LoanTrace> listLoanTrace = LoanTrace.loanTraceDao
				.find("select payUserName,loanDateTime,payAmount,loanType,rankValue from t_loan_trace where loanCode = ? order by loanDateTime asc",
						loanCode);
		for (int i = 0; i < listLoanTrace.size(); i++) {
			LoanTrace tmp = listLoanTrace.get(i);
			String tmpLoanType = tmp.getStr("loanType");
			if ("M".equals(tmpLoanType)) {
				tmpLoanType = "手动[网页端]";
			} else if ("N".equals(tmpLoanType)) {
				tmpLoanType = "手动[移动端]";
			} else if ("A".equals(tmpLoanType)) {
				tmpLoanType = "自动" + "[" + tmp.getLong("rankValue") + "]";
			}
			listLoanTrace.get(i).put("loanType", tmpLoanType);
			String tmpLoanDateTime = tmp.getStr("loanDateTime");
			String loanDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(tmpLoanDateTime));
			tmp.put("loanDateTime", loanDateTime);
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("traces", listLoanTrace);
		msg = succ("获取成功", map);
		renderJson(msg);
	}
	
	/**
	 * 标详细信息统一接口(Apple)
	 * 
	 * @throws ParseException
	 */
	@ActionKey("/appQueryLoanInfo4Apple")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	@SuppressWarnings("unchecked")
	public void appQueryLoanInfo4Apple() throws ParseException {
		Message msg = null;
		String ver = getPara("ver");
		String loanCode = getPara("loanCode");
		if (StringUtil.isBlank(loanCode)) {
			msg = error("01", "参数错误", "");
			renderJson(msg);
		}

		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		Map<String, Object> tmp = new HashMap<String, Object>();

		// 项目详情
		tmp.put("rateByYear", loanInfo.getInt("rateByYear"));
		if (loanInfo.getInt("benefits4new") == 0) {
			tmp.put("rewardRateByYear", loanInfo.getInt("rewardRateByYear"));
			
			long releaseDate = Long.parseLong(loanInfo.getStr("releaseDate"));
			if (loanInfo.getInt("benefits4new") == 0) {
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					tmp.put("newRewardRateByYear", "4.0");
					} else {
						tmp.put("newRewardRateByYear", String.valueOf(loanInfo.getInt("rewardRateByYear")/100.0));
					}
			}
						
		} else {
			tmp.put("benefits4new", loanInfo.getInt("benefits4new"));
			tmp.put("newBenefits4new", String.valueOf(loanInfo.getInt("benefits4new")/100.0));
		}
		tmp.put("loanBalance", loanInfo.getLong("loanBalance"));
		tmp.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
		String tmpRefundType = loanInfo.getStr("refundType");
		if ("A".equals(tmpRefundType)) {
			tmpRefundType = "等额本息";
		} else if ("B".equals(tmpRefundType)) {
			tmpRefundType = "先息后本";
		}
		tmp.put("refundType", tmpRefundType);
		tmp.put("loanAmount", loanInfo.getLong("loanAmount"));
		tmp.put("releaseDate", loanInfo.getStr("releaseDate"));
		tmp.put("releaseTime", loanInfo.getStr("releaseTime"));
		String endDateTime = DateUtil
				.subDay(loanInfo.getStr("releaseDate"), -3)
				+ loanInfo.getStr("releaseTime");
		tmp.put("endDateTime", endDateTime);
		String tmpLastPayLoanDateTime = loanInfo.getStr("lastPayLoanDateTime");
		String lastPayLoanDateTime = "";
		if ("00000000000000".equals(tmpLastPayLoanDateTime)) {
			lastPayLoanDateTime = "";
		} else {
			lastPayLoanDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(tmpLastPayLoanDateTime));
		}
		tmp.put("lastPayLoanDateTime", lastPayLoanDateTime);
		String loanNo = loanInfo.getStr("loanNo");
		String productType = loanInfo.getStr("productType");
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
		} else if ("E".equals(productType)) {
			productType = "易分期";
		}
		String loanTitle4Apple = productType + loanNo;
		tmp.put("loanTitle4Apple", loanTitle4Apple);
		tmp.put("loanNO", loanNo);

		tmp.put("loanCode", loanInfo.getStr("loanCode"));
		tmp.put("serverTime", DateUtil.getNowDateTime());
		int bfb = (int) (100 * (loanInfo.getLong("loanAmount") - loanInfo
				.getLong("loanBalance")) / loanInfo.getLong("loanAmount"));
		tmp.put("bfb", bfb);
		tmp.put("tip", loanInfo.getTip_());

		// 项目描述
 		int tmpLoanNo = Integer.parseInt(loanInfo.getStr("loanNo"));
 		tmp.put("productType", loanInfo.getStr("productType"));
		LoanApply loanApply = loanApplyService.findById(tmpLoanNo);
		if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))){
			if(StringUtil.isBlank(ver)){
			String loanUserCode = loanInfo.getStr("userCode");
			User loanUser = userService.findById(loanUserCode);
			String loanUserMobile = loanUser.getStr("userMobile");
			try {
				loanUserMobile = CommonUtil.decryptUserMobile(loanUserMobile);
			} catch (Exception e) {
				loanUserMobile = "134********";
			}
			tmp.put("loanBrief", "");
			tmp.put("loanUseWay", "");
			tmp.put("clxx", "");
			tmp.put("wqkc", "");
			tmp.put("fksh", "");
			tmp.put("loanUserName",loanUserMobile.substring(0, 3)+"****"+loanUserMobile.subSequence(7, 11));
			tmp.put("hasMarital", "");
			tmp.put("userCSHY","");
			tmp.put("userZW","");
			tmp.put("hasHouse", "");
			tmp.put("hasCar", "");
			tmp.put("hasHouseLoan", "");
			tmp.put("hasCarLoan", "");
			tmp.put("loanAddress","");
			tmp.put("loanUserInfo", convertLoanUserInfo(null));
			}else{
				tmp = resetYistageLoanDesc(tmp, loanInfo);
			}
		}else{
			String loanDesc = loanApply.getStr("loanDesc");
			JSONArray array = JSONArray.parseArray(loanDesc);
			for (int i = 0; i < array.size(); i++) {
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("title");
				
				String content = json.getString("content");
				if (title.indexOf("借款人") != -1) {
					tmp.put("loanBrief", content.replaceAll("\n", ""));
				} else if (title.indexOf("借款用途") != -1) {
					tmp.put("loanUseWay", content);
				} else if (title.indexOf("车辆") != -1) {
					String clxx = content.replaceAll("<br>", "");
					tmp.put("clxx", clxx.replaceAll("\n", ""));
				} else if (title.indexOf("外勤") != -1) {
					tmp.put("wqkc", content);
				} else if (title.indexOf("风控") != -1) {
					tmp.put("fksh", content);
				}
			}
			// 借款人资料
			String loanUserInfo = loanApplyService.findLoanUserDetail(loanInfo
					.getStr("loanNo"));
			if (StringUtil.isBlank(loanUserInfo) == false) {
				JSONObject xx = JSONObject.parseObject(loanUserInfo);
				if (null != xx) {
					tmp.put("loanUserName", xx.get("loanUserName") == null ? "" : xx.get("loanUserName"));
					
					if ("0".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "未婚");
					} else if ("1".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "已婚");
					} else if ("2".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "离异");
					} else if ("3".equals(xx.get("hasMarital"))) {
						tmp.put("hasMarital", "丧偶");
					}
					
					tmp.put("userCSHY", xx.get("userCSHY") == null ? "" : xx.get("userCSHY"));
					
					tmp.put("userZW", xx.get("userZW") == null ? "" : xx.get("userZW"));
					
					if ("0".equals(xx.get("hasHouse"))) {
						tmp.put("hasHouse", "无");
					} else if ("1".equals(xx.get("hasHouse"))) {
						tmp.put("hasHouse", "有");
					}
					
					if ("0".equals(xx.get("hasCar"))) {
						tmp.put("hasCar", "无");
					} else if ("1".equals(xx.get("hasCar"))) {
						tmp.put("hasCar", "有");
					}
					
					if ("0".equals(xx.get("hasMortgage"))) {
						tmp.put("hasHouseLoan", "无");
					} else if ("1".equals(xx.get("hasMortgage"))) {
						tmp.put("hasHouseLoan", "有");
					}
					
					if ("0".equals(xx.get("hasCarLoan"))) {
						tmp.put("hasCarLoan", "无");
					} else if ("1".equals(xx.get("hasCarLoan"))) {
						tmp.put("hasCarLoan", "有");
					}
					
					tmp.put("loanAddress", xx.get("loanAddress") == null ? "" : xx.get("loanAddress"));
					
					// 借款人资料v1.1
					tmp.put("loanUserInfo", convertLoanUserInfo(xx));
				}
			}
		}
		String loan_pic = loanInfo.getStr("loan_pic");
		JSONArray arr = JSONArray.parseArray(loan_pic);
		for (int i = 0; i < arr.size(); i++) {
			JSONObject json2 = arr.getJSONObject(i);
			String url = "http://image1.yrhx.com/" + json2.getString("code")
					+ "/loan";
			json2.put("url", url);
			json2.remove("code");
		}
		List<Object> objects = (List<Object>) JSONArray.toJSON(arr);
		tmp.put("loan_pic", objects);

		// 投标流水
		List<LoanTrace> listLoanTrace = LoanTrace.loanTraceDao
				.find("select payUserName,loanDateTime,payAmount,loanType,rankValue from t_loan_trace where loanCode = ? order by loanDateTime asc",
						loanCode);
		for (int i = 0; i < listLoanTrace.size(); i++) {
			String tmpLoanType = listLoanTrace.get(i).getStr("loanType");
			if ("M".equals(tmpLoanType)) {
				tmpLoanType = "手动[网页端]";
			} else if ("N".equals(tmpLoanType)) {
				tmpLoanType = "手动[移动端]";
			} else if ("A".equals(tmpLoanType)) {
				tmpLoanType = "自动" + "[" + listLoanTrace.get(i).getLong("rankValue") + "]";
			}
			listLoanTrace.get(i).put("loanType", tmpLoanType);
			String tmpLoanDateTime = listLoanTrace.get(i)
					.getStr("loanDateTime");
			String loanDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
					.format(new SimpleDateFormat("yyyyMMddHHmmss")
							.parse(tmpLoanDateTime));
			listLoanTrace.get(i).put("loanDateTime", loanDateTime);
		}
		tmp.put("traces", listLoanTrace);

		msg = succ("获取成功", tmp);
		renderJson(msg);
		
	}
	/**
	 * 查询立即投标页面信息 ws for 安卓
	 * */
	@ActionKey("/app_queryloanInfo")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryLoanInfo(){
		String userCode=getUserCode();
		Message msg=null;
		User user=userService.findById(userCode);
		if(!JXController.isJxAccount(user)){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		String loanCode=getPara("loanCode");
		LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanCode);
		Funds funds = Funds.fundsDao.findById(userCode);
		long loanBalance = loanInfo.getLong("loanBalance");
		if(loanBalance==0){
			msg=error("02", "出借完成", null);
			renderJson(msg);
			return;
		}
		Map<String,Object> result = new HashMap<String, Object>();
		long avBalance = funds.getLong("avBalance");
		String refundType = loanInfo.getStr("refundType");
		int loanMonth=loanInfo.getInt("loanTimeLimit");
//		result.put("loanBalance", Number.longToString(loanBalance));
//		result.put("avBalance", Number.longToString(avBalance));
		// WCF 20170817
		DecimalFormat df = new DecimalFormat("0.00");
		result.put("avBalance", df.format(avBalance/100.00));
		result.put("loanBalance", df.format(loanBalance/100.00));
		// end
		result.put("refundType",refundType);
		int rateByYear=loanInfo.getInt("rateByYear");
		int rewardRateByYear=loanInfo.getInt("rewardRateByYear");
		int benefits4new=loanInfo.getInt("benefits4new");
		double rateByYearTotle=(double)(rateByYear+rewardRateByYear+benefits4new)/120000;
		result.put("rate", String.valueOf(rateByYearTotle));
		rateByYearTotle=Math.pow(rateByYearTotle+1, loanMonth);
		result.put("rateByYearTotle", String.valueOf(rateByYearTotle));
		result.put("loanMonth",String.valueOf(loanMonth) );
		String releaseDate=loanInfo.getStr("releaseDate");
		String releaseTime=loanInfo.getStr("releaseTime");
		result.put("releaseDate",releaseDate );
		result.put("releaseTime",releaseTime );
		long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
		result.put("rewardRateAmount",Float.parseFloat(rewardRateAmount+"")/100 );
		result.put("rewardRate","1");
		msg=succ("result", result);
		renderJson(msg);
		return;
	}
	/**
	 * 查询立即投标页面信息 ws for ios
	 * */
	@ActionKey("/app_queryloanInfoios")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryLoanInfo2(){
		String userCode=getUserCode();
		Message msg=null;
		User user=userService.findById(userCode);
		if(!JXController.isJxAccount(user)){
			msg=error("01", "未激活存管", null);
			renderJson(msg);
			return;
		}
		String loanCode=getPara("loanCode");
		LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanCode);
		Funds funds = Funds.fundsDao.findById(userCode);
		long loanBalance = loanInfo.getLong("loanBalance");
		if(loanBalance==0){
			msg=error("02", "出借完成", null);
			renderJson(msg);
			return;
		}
		List<Map<String, Object>> list = new ArrayList<Map<String,Object>>();
		Map<String,Object> result = new HashMap<String, Object>();
		long avBalance = funds.getLong("avBalance");
		String refundType = loanInfo.getStr("refundType");
		int loanMonth=loanInfo.getInt("loanTimeLimit");
//		result.put("loanBalance", Number.longToString(loanBalance));
//		result.put("avBalance", Number.longToString(avBalance));
		// WCF 20170817
		DecimalFormat df = new DecimalFormat("0.00");
		result.put("avBalance", df.format(avBalance/100.00));
		result.put("loanBalance", df.format(loanBalance/100.00));
		// end
		result.put("refundType",refundType);
		int rateByYear=loanInfo.getInt("rateByYear");
		int rewardRateByYear=loanInfo.getInt("rewardRateByYear");
		int benefits4new=loanInfo.getInt("benefits4new");
		double rateByYearTotle=(double)(rateByYear+rewardRateByYear+benefits4new)/120000;
		result.put("rate", String.valueOf(rateByYearTotle));
		rateByYearTotle=Math.pow(rateByYearTotle+1, loanMonth);
		result.put("rateByYearTotle", String.valueOf(rateByYearTotle));
		result.put("loanMonth",String.valueOf(loanMonth) );
		String releaseDate=loanInfo.getStr("releaseDate");
		String releaseTime=loanInfo.getStr("releaseTime");
		result.put("releaseDate",releaseDate );
		result.put("releaseTime",releaseTime );
		long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
		result.put("rewardRateAmount",Float.parseFloat(rewardRateAmount+"")/100 );
		result.put("rewardRate","1");
		list.add(result);
		msg=succ("result", list);
		renderJson(msg);
		return;
	}
	/**
	 * 点击投标 ws
	 * */
	@ActionKey("/app_creatloantrace")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppCreatLoanTrace(){
		Message msg=null;
		User user = userService.findById(getUserCode());
		String evaluationLevel = user.getStr("evaluationLevel");
		if(StringUtil.isBlank(evaluationLevel)){
			msg = error("F1", "请先完成风险测评再继续投资", "" );
			renderJson(msg);
			return;
		}
		if(!JXController.isJxAccount(user)){
			msg = error("01", "用户还未激活存管账户", "");
			renderJson(msg);
			return;
		}
		String amt = getPara("amount");
		if(StringUtil.isBlank(amt)){
			msg = error("02", "请输入正确金额", "");
			renderJson(msg);
			return;
		}
		String jxAccountId=user.getStr("jxAccountId");
		long amount =StringUtil.getMoneyCent(amt);
		Map<String, String> pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
		if (pwdMap == null || !"1".equals(pwdMap.get("pinFlag"))) {
			msg = error("03", "请先设置存管账户交易密码", "");
			renderJson(msg);
			return;
		}
		Map<String,Object> cardDetail = JXQueryController.cardBindDetailsQuery(jxAccountId, "1");
		if(cardDetail != null &&  "00000000".equals(cardDetail.get("retCode"))){
			@SuppressWarnings("unchecked")
			List<Map<String,String>> list = (List<Map<String,String>>)cardDetail.get("subPacks");
			if(list == null || list.size()<=0){
				msg = error("04", "请先绑卡再投标", "");
				renderJson(msg);
				return;
			}
		}
//		Map<String,String> banMap=JXQueryController.balanceQuery(jxAccountId);
//		long availBal=StringUtil.getMoneyCent(banMap.get("availBal"));
		Funds funds = fundsServiceV2.findById(getUserCode());
		
		// 验证用户资金是否异常
		if (!fundsServiceV2.checkBalance(user)) {
			msg = error("05", "用户资金异常", "'");
			renderJson(msg);
			return;
		}
		
		// 验证用户余额是否足够
		if (funds.getLong("avBalance") < amount) {
			msg = error("05", "可用余额不足", "");
			renderJson(msg);
			return;
		}
		
//		if (!JXController.checkBalance(funds.getLong("avBalance"),jxAccountId)) {
//			msg = error("05", "用户资金异常", "");
//			renderJson(msg);
//			return;
//		}
//		if(availBal<amount||funds.getLong("avBalance")<amount){
//			msg = error("05", "可用余额不足", "");
//			renderJson(msg);
//			return;
//		}
		String loanCode = getPara("loanCode");
		LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanCode);
		// 暂时关闭手动投标
//		int benefits4new = loanInfo.getInt("benefits4new");
//		if( benefits4new == 0 ){
//			msg=error("21", "暂不支持手动投标", null );
//			renderJson(msg);
//			return;
//		}
		
		String resDate=loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime");
		String nowDate=DateUtil.getNowDateTime();
		if(DateUtil.compareDateByStr("yyyyMMddHHmmss", resDate, nowDate)>=0){
			msg = error("06", "还未到时间", null);
			renderJson(msg);
			return;
		}
		
		if( amount <= 0 ){
			msg= error("07", "投标金额错误!", null ) ;
			renderJson(msg);
			return;
		}
		if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))&&amount%10000>0){
			msg= error("07", "易分期投标金额应为100的整数倍!", "" ) ;
			renderJson(msg);
			return;
		}
		if( StringUtil.isBlank( loanCode ) == true ){
			msg= error("08", "参数错误!", null ) ;
			renderJson(msg);
			return;
		}
		if( loanCode.length() != 32){
			msg = error("09", "参数错误!", null ) ;
			renderJson(msg);
			return;
		}
		String ticketCodes = "";
		try {
			ticketCodes = getPara("ticket",Tickets.rewardRateInterestTcode);//默认使用会员等级加息
		} catch (Exception e) {
			ticketCodes = "";
		}
		if( StringUtil.isBlank(ticketCodes) == false &&!"D".equals(ticketCodes)){
			String[] tcs = ticketCodes.split(",");
			if(tcs!=null && tcs.length > 0 ){
				for (int i = 0; i < tcs.length; i++) {
					Tickets ticket = ticketService.findByCode(tcs[i]);
					if(ticket!=null&&!"A".equals(ticket.getStr("tstate"))){
						msg = error("10", "投标失败[33]", "");
						renderJson(msg);
						return;
					}
					}
				}
			}
		if("D".equals(ticketCodes)){
			ticketCodes=Tickets.rewardRateAomuntTcode;
		}
		msg= doLoan4NewBidding( loanCode , amount , user,"N"  ,ticketCodes , 0 ) ;
		renderJson(msg);
		}
	/**
	 * 新的投标接口
	 * 新增参数 ticketCode
	 * @param loanCode
	 * @param amount
	 * @param user
	 * @param loantype
	 * @param rankValue
	 * @param ticketCodes
	 * @param onceMinAmount
	 */
	public Message doLoan4NewBidding(String loanCode , long amount , User user,String loantype  , String ticketCodes ,long onceMinAmount){
		String userCode = user.getStr("userCode") ;
		LoanInfo loan = loanInfoService.findById(loanCode) ;
		int ticketAmount = 0 ;//现金券的抵扣金额
		long exAmount = 0;//现金券的使用条件，最少投资金额,默认0
		int rewardticketrate=0;//加息券加的利息
		//modified 20180425
		String bonusFlag="0";//是否使用红包
		String bonusAmount="";//抵扣红包金额
		String tickettype="";
		List<Tickets> tickets = new ArrayList<Tickets>();//所有类型券的集合
		
		//判断标书是否存在并状态是否正常
		if( loan == null || "J".equals(loan.getStr("loanState")) == false ){
			return error("02", "未找到相关标信息", null ) ;
		}
		/*
		 * 	1,检查标设置
		 * 	2,判断用户是否负责投标条件
		 * 	3,投标
		 */
		int minLoanAmount = loan.getInt("minLoanAmount");
		long loanBalance = loan.getLong("loanBalance");
		
		//判断是否满标
		if( loanBalance <= 0 ){
			return error("03", "已满标，请查看其他标", null ) ;
		}
		if(amount>loanBalance){
			return error("04", "投标金额应小于标的剩余可投金额", null ) ;
		}
		//检查是否已经到投标时间
		String releaseDateTime = loan.getStr("releaseDate")+loan.getStr("releaseTime");
		if( "M".equals(loantype) == true ){
			int compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), releaseDateTime);
			if(compareDateByStr != 0 && compareDateByStr != 1){
				return error("09", "未到投标时间，请查看其他标", null );
			}
		}else{
			//rankValue = autoLoanService.queryRank(userCode)[1];
			//rankValue = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode=?",userCode).getInt("aid");
		}
		
		/*
		 *	增加新手标检查
		 *		1、通过判断benefits4new 判断该为是否为新手标
		 *		2、判断当前时间是否为发标时间9点以后，如果是，则不坐新手限制。
		 *		3、新手限制条件，判断用户活跃积分是否小于100，小于等于100则为新手
		 */
		int benefits4new = loan.getInt("benefits4new");
		if( benefits4new > 0 ){
			if(Tickets.rewardRateInterestTcode.equals(ticketCodes)){//会员自动加息不适用新手标
				ticketCodes = "";
			}
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			//检查是否过了不限制的时间
			String passTime = "213000";//默认晚9点半后不限制投新手标
			try {
				passTime = (String) CACHED.get("S3.xsbgqsj");
			} catch (Exception e) {
				
			}
			//String openDateTime = DateUtil.getNowDate() + passTime;
			String openDateTime = loan.getStr("releaseDate") + passTime;
			int compareDateByStr = -1;
			compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), openDateTime);
			if(compareDateByStr<1){
				long userScore = user.getLong("userScore");
				int tmp = 3000;
				try {
					tmp = tmp * Integer.valueOf((String) CACHED.get("S3.xsjfbs"));
				} catch (Exception e) {
				}
				/*if( userScore >= tmp ){
					return error("15", "此标为新人专享", null ) ;
				}*/
				
				String regDate = user.getStr("regDate");//注册日期
				String vipDate = "20180319";//vip上线日期
				
				int x = DateUtil.compareDateByStr("yyyyMMdd", regDate, vipDate);
				if(x >= 0 || (x < 0 && userScore < tmp)){
					//新手标每个用户限额一万，2018.3.19之后注册或之前注册且活跃积分少于30
					long permitAmount=1000000 - loanTraceService.sumNewAmountByUserCode(userCode);//新手标剩余额度
					if(permitAmount < minLoanAmount){
						return error("15", "此标为新人专享", null ) ;
					}else if(amount > permitAmount){
						amount = permitAmount;
					}
				}else{
					return error("15", "此标为新人专享", null);
				}
				
			}
			loan.set("rewardRateByYear", (rewardRateByYear + benefits4new ) ) ;	//将新手奖励增加到奖励年利率里
			
		}
		
		
		/**
		 * 	TODO 解析券，记录券信息
		 */
		
		
		if( StringUtil.isBlank(ticketCodes) == false ){
			String[] tcs = ticketCodes.split(",");
			if(tcs!=null && tcs.length > 0 ){
				for (int i = 0; i < tcs.length; i++) {
					Tickets ticket=null;
					if(Tickets.rewardRateAomuntTcode.equals(tcs[i])){
						//查询加息额度
						long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
						if(rewardRateAmount<amount){
							return error("15", "加息额度不足", null);
						}
						if(benefits4new>0){
							return error("15", "新手标不能使用加息额度", null);
						}
						ticket=Tickets.getTmpTickets();
						tickettype="D";
					}else if (Tickets.rewardRateInterestTcode.equals(tcs[i])) {//会员等级自动加息
						Integer vipLevel = user.getInt("vipLevel");//获取会员等级
						VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
						int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
						ticket = rewardInterest>0?Tickets.getGradeTickets(rewardInterest):null;
						tickettype="E";
					}else{
						ticket = ticketService.findByCode2(tcs[i]);
						}
					if( ticket != null ){
						tickets.add(ticket);
						if(ticket.getStr("ttype").equals("A")){//现金券
							//bonusFlag="1";//modified 20180425
							if(loan.getStr("productType").equals(SysEnum.productType.E.val())){
								return error("15", "易分期不能使用现金券", null);
							}
							ticketAmount = ticketAmount + ticket.getInt("amount");
							//bonusAmount=String.valueOf(ticketAmount*0.01);
						}else if(ticket.getStr("ttype").equals("C")){//加息券、加息额度、会员等级加息
//							int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
//							if(releaseDate<20171118){
//								return error("15", "2017-11-18之前的标不支持使用加息券", null);
//							}
							if(benefits4new>0){
								return error("15", "新手标不能使用加息", null);
							}
							int rewardRateByYear2=loan.getInt("rewardRateByYear");
							rewardticketrate+=ticket.getInt("rate");
							loan.set("rewardRateByYear", (rewardRateByYear2 + rewardticketrate ) ) ;//将加息券奖励增加到奖励年利率里
						}
						String strUseEx = ticket.getStr("useEx");
						if( StringUtil.isBlank(strUseEx) == false ){
							JSONObject json = JSONObject.parseObject( strUseEx ) ;
							int limit = json.getIntValue("limit");
							int rate = json.getIntValue("rate");
							long tmp = json.getLongValue("amount") ;
							//加息券的额度应大于投标金额--现金券的额度应小于投标金额 ws
							if(ticket.getStr("ttype").equals("A")){
								if(tmp!=0 && amount < tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}else if(ticket.getStr("ttype").equals("C")){
								if(tmp!=0 && amount > tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}
							if(limit != 0 && loan.getInt("loanTimeLimit") < limit){
								return error("77", "请检查理财券使用期限是否符合条件", false);
							}
							if(rate != 0 && (loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")) < rate ){
								return error("77", "请检查理财券使用利率是否符合条件", false);
							}
							//判断是否符合券可投标期限 20170727 ws
							String la=String.valueOf(loan.getInt("loanTimeLimit"));
							if(la.length()==1){
								la="0"+la;
							}
							if(null!=ticket.getStr("loanMonth")&&ticket.getStr("loanMonth").length()>1&&ticket.getStr("loanMonth").indexOf(la)<0){
								return error("77", "请检查理财券使用标期是否符合条件", false);
							}
							//end
							if(tmp > exAmount){
								exAmount = tmp;
							}
						}
					}
				}
			}
		}
		
		//去小数
		amount = amount - amount%100 ;
		
		//真实投标金额(投标金额-券金额)
		//long trueAmount =0;
		
		//long avBalance = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance").getLong("avBalance");
//		if(avBalance < trueAmount){
//			return error("999", "可用余额不足!", null);
//		}
		
		int maxLoanAmount = loan.getInt("maxLoanAmount");
		if( maxLoanAmount > 0 && amount > maxLoanAmount ){
			//如果最大投标金额 小于 其中一个现金券的使用条件金额
			if(tickets != null && tickets.size() > 0){
				if(maxLoanAmount < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			}
		//投标金额小于最小投标金额时返回错误
		if( amount < minLoanAmount && loanBalance >= minLoanAmount){
			return error("04", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
		}
		
		//当可投金额小于投标金额时, 自动将可投金额置为投标金额
		if( amount > loanBalance ){
			if(tickets != null && tickets.size() > 0){
				if(loanBalance < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			amount = loanBalance ;
			
			if( onceMinAmount > amount ){
				return error("14", "单次投标金额不满足最小投标金额要求", minLoanAmount ) ;
			}
			
			//trueAmount = amount - ticketAmount;
		}
		
		int maxLoanCount = loan.getInt("maxLoanCount");
		Map<String , Long> totalMap = loanTraceService.totalByLoan4user(loanCode , userCode );
		long userLoanCount = totalMap.get("count") ;
		long userTotalAmount = totalMap.get("totalAmount");
		if( userLoanCount > maxLoanCount ){
			return error("06", "投标次数已超限,当前最大投标次数：" + maxLoanCount + "次", maxLoanCount ) ;
		}
		
		//验证投标总额
		if( (userTotalAmount + amount) > maxLoanAmount ){
			amount = maxLoanAmount - userTotalAmount ;
			//trueAmount = amount - ticketAmount;
			if( amount <= 0 ){
				return error("07", "投标总额已超，您的该标的投资总额为：" + userTotalAmount/10/10 + "元" , userTotalAmount) ;
			}
		}
		//TODO 使用现金券时进行检查
		try {
			if( tickets!=null && tickets.size() > 0){
				for (int i = 0; i < tickets.size(); i++) {
					Tickets tts = tickets.get(i);
					if(!Tickets.rewardRateAomuntTcode.equals(tts.getStr("tCode"))&&!Tickets.rewardRateInterestTcode.equals(tts.getStr("tCode"))){
						if(tts.getStr("ttype").equals("A")||tts.getStr("ttype").equals("C")){
							if( loan == null ){
								return error("08","无使用场景",null);
							}
							//Tickets ticket = Tickets.ticketsDao.findById(tts.getStr("tCode") );
							
							//检查归属
							String tUserCode = tts.getStr("userCode") ;
							if( userCode.equals(tUserCode) == false ){
								return error("09","非法请求",null);
							}
							
							String tState = tts.getStr("tstate") ;
							//检查状态是否可用
							if( Tickets.STATE.A.key().equals( tState ) == false ){
								return error("10","现金券不可用[" + Tickets.STATE.valueOf(tState).desc() + "]",null);
							}
							//检查过期日期
							String nowDate = DateUtil.getNowDate() ;
							String expDate = tts.getStr("expDate");
							if( DateUtil.compareDateByStr("yyyyMMdd", nowDate , expDate ) > 0 ){
								return error("11","现金券已过期[" + expDate + "]",null);
							}
							
							//检查使用条件
							String strUseEx = tts.getStr("useEx");
							if( StringUtil.isBlank(strUseEx) == false ){
								long loanRate = loan.getInt("rateByYear") + loan.getInt("rewardRateByYear");//这里引用的rewardRateByYear已经包含了新手年利率与加息券利息
								long loanLimit = loan.getInt("loanTimeLimit") ;
								
								JSONObject json = JSONObject.parseObject( strUseEx ) ;
								//固定三个条件 amount / rate /limit 
								long useExAmount = json.getLongValue("amount") ;
								int exRate = json.getIntValue("rate") ;
								int exLimit = json.getIntValue("limit") ;
								if("A".equals(tts.getStr("ttype"))){
									if( amount < useExAmount ){
										return error("12", "单笔投资必须大于" + exAmount/10.0/10.0 + "元才可以使用!", null ) ;
									}
								}else if("C".equals(tts.getStr("ttype"))){
									if(amount > useExAmount&&useExAmount>0){
										return error("13", "单笔投资必须小于等于" + exAmount/10.0/10.0 + "元才可以使用!", null ) ;
									}
								}
								
								if( loanRate-tts.getInt("rate") < exRate ){
									return error("14", "借款标利率必须大于" + exRate/10.0/10.0 + "%才可以使用!", null ) ;
								}
								
								if( loanLimit < exLimit ){
									return error("15", "借款标期限必须大于" + exLimit + "才可以使用!", null ) ;
								}
								
							}
							
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if("D".equals(tickettype)){
				return error("10", "使用加息额度异常","");
			} 
			return error("10", "理财券异常","");
		}
		
		try{
			//modified 20180425
	        String jxAccountId=user.getStr("jxAccountId");
	        String productId=loan.getStr("loanCode");
	        String txAmount=StringUtil.getMoneyYuan(amount);//投标金额
	        BanksV2 bank = banksService.findBanks4User(userCode).get(0);
	        String mobile =CommonUtil.decryptUserMobile(bank.getStr("mobile"));
	        String forgotPwdUrl =CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+mobile;
	        String retUrl=CommonUtil.APPBACK_ADDRESS+"/main";
	        String notifyUrl=CommonUtil.CALLBACK_URL+"/bidApplyCallback";
	        JSONArray ja=null;
	        if( tickets != null && tickets.size() > 0){
				 ja = new JSONArray();
				for (int i = 0; i < tickets.size(); i++) {
					Tickets ticket = tickets.get(i);
					JSONObject ticketInfo = new JSONObject() ;
					ticketInfo.put("code", ticket.getStr("tCode") ) ;
					ticketInfo.put("type", ticket.getStr("ttype")) ;
					ticketInfo.put("amount", ticket.getInt("amount")) ;
					ticketInfo.put("rate", ticket.getInt("rate")) ;
					ticketInfo.put("isDel", ticket.getStr("isDel"));
					//end
					ja.add(ticketInfo);
				}
			}
	        String ticktes="";
	        if(ja!=null){
	        	ticktes=ja.toJSONString();
	        }
	        JXappController.bidApply4App(jxAccountId,CommonUtil.genShortUID(),txAmount, productId,"0", bonusFlag, bonusAmount,forgotPwdUrl,retUrl,notifyUrl,ticktes,getResponse());
	       
	       
	    }catch(Exception e){
			e.printStackTrace( );
			return error("EX", "操作异常:" + e.getMessage() , null ) ;
		}
		return null;
		
	}

	@SuppressWarnings("unused")
	private Message doLoan4bidding(String loanCode , long amount , User user,String loantype ,long rankValue , String ticketCodes ,long onceMinAmount ){
		String userCode = user.getStr("userCode") ;
		//验证是否开通存管 rain 20180606
		if(!JXController.isJxAccount(user)){
			return error("21", "用户还未激活存管账户", null);
		}
		//用户资金验证
		Funds funds = fundsServiceV2.findById(userCode);
		QueryBalanceResultData fuiouFunds =	fuiouTraceService.BalanceFunds(user);
		if (funds.getLong("avBalance") != Long.parseLong(fuiouFunds.getCa_balance()) || 
				funds.getLong("frozeBalance") != Long.parseLong(fuiouFunds.getCf_balance())) {
			return error("20", "用户资金异常", null);
		}
		//end
		LoanInfo loan = loanInfoService.findById(loanCode);

		//判断标书是否存在并状态是否正常
		if( loan == null || "J".equals(loan.getStr("loanState")) == false ){
			return error("02", "未找到相关标信息", null ) ;
		}
		/*
		 * 	1,检查标设置
		 * 	2,判断用户是否负责投标条件
		 * 	3,投标
		 */
		int minLoanAmount = loan.getInt("minLoanAmount");
		long loanBalance = loan.getLong("loanBalance");
		
		//判断是否满标
		if( loanBalance <= 0 ){
			return error("03", "已满标，请查看其他标", null ) ;
		}
		
		//检查是否已经到投标时间
		String releaseDateTime = loan.getStr("releaseDate")+loan.getStr("releaseTime");
		if( "M".equals(loantype) == true ){
			int compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), releaseDateTime);
			if(compareDateByStr != 0 && compareDateByStr != 1){
				return error("09", "未到投标时间，请查看其他标", null );
			}
		}else{
			//rankValue = autoLoanService.queryRank(userCode)[1];
			//rankValue = AutoLoan_v2.autoLoanDao.findFirst("select * from t_auto_loan_v2 where userCode=?",userCode).getInt("aid");
		}
		
		/*
		 *	增加新手标检查
		 *		1、通过判断benefits4new 判断该为是否为新手标
		 *		2、判断当前时间是否为发标时间9点以后，如果是，则不坐新手限制。
		 *		3、新手限制条件，判断用户活跃积分是否小于100，小于等于100则为新手
		 */
		int benefits4new = loan.getInt("benefits4new");
		if( benefits4new > 0 ){
			if(Tickets.rewardRateInterestTcode.equals(ticketCodes)){//会员自动加息不适用新手标
				ticketCodes = "";
			}
			int rewardRateByYear = loan.getInt("rewardRateByYear");
			//检查是否过了不限制的时间
			String passTime = "213000";//默认晚9点半后不限制投新手标
			try {
				passTime = (String) CACHED.get("S3.xsbgqsj");
			} catch (Exception e) {
				
			}
			//String openDateTime = DateUtil.getNowDate() + passTime;
			String openDateTime = loan.getStr("releaseDate") + passTime;
			int compareDateByStr = -1;
			compareDateByStr = DateUtil.compareDateByStr("yyyyMMddHHmmss",
					DateUtil.getNowDateTime(), openDateTime);
			if(compareDateByStr<1){
				long userScore = user.getLong("userScore");
				int tmp = 3000;
				try {
					tmp = tmp * Integer.valueOf((String) CACHED.get("S3.xsjfbs"));
				} catch (Exception e) {
				}
				/*if( userScore >= tmp ){
					return error("15", "此标为新人专享", null ) ;
				}*/
				String regDate = user.getStr("regDate");//注册日期
				String vipDate = "20180319";//vip上线日期
				int x = DateUtil.compareDateByStr("yyyyMMdd", regDate, vipDate);
				if(x >= 0 || (x < 0 && userScore < tmp)){
					//新手标每个用户限额一万，2018.3.19之后注册或之前注册且活跃积分少于30
					long permitAmount=1000000 - loanTraceService.sumNewAmountByUserCode(userCode);//新手标剩余额度
					if(permitAmount < minLoanAmount){
						return error("15", "此标为新人专享", null ) ;
					}else if(amount > permitAmount){
						amount = permitAmount;
					}
				}else{
					return error("15", "此标为新人专享", null);
				}
				
			}
			loan.set("rewardRateByYear", (rewardRateByYear + benefits4new ) ) ;	//将新手奖励增加到奖励年利率里
		}
		
		
		/**
		 * 	TODO 解析券，记录券信息
		 */
		List<Tickets> tickets = new ArrayList<Tickets>();//所有类型券的集合
		int ticketAmount = 0 ;//现金券的抵扣金额
		long exAmount = 0;//现金券的使用条件，最少投资金额,默认0
		int rewardticketrate=0;//加息券加的利息
		String tickettype="";
		if( StringUtil.isBlank(ticketCodes) == false ){
			String[] tcs = ticketCodes.split(",");
			if(tcs!=null && tcs.length > 0 ){
				for (int i = 0; i < tcs.length; i++) {
					Tickets ticket=null;
					if(Tickets.rewardRateAomuntTcode.equals(tcs[i])){
						//查询加息额度
						long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");
						if(rewardRateAmount<amount){
							return error("15", "加息额度不足", null);
						}
						if(benefits4new>0){
							return error("15", "新手标不能使用加息额度", null);
						}
						ticket=Tickets.getTmpTickets();
						tickettype="D";
					}else if (Tickets.rewardRateInterestTcode.equals(tcs[i])) {//会员等级自动加息
						Integer vipLevel = user.getInt("vipLevel");//获取会员等级
						VipV2 vipV2 = VipV2.getVipByLevel(vipLevel);//该用户所处会员等级对象
						int rewardInterest = vipV2.getRewardInterest();//等级加息奖励利率
						ticket = rewardInterest>0?Tickets.getGradeTickets(rewardInterest):null;
						tickettype="E";
					}else{
						ticket = ticketService.findByCode2(tcs[i]);
						}
					if( ticket != null ){
						tickets.add(ticket);
						if(ticket.getStr("ttype").equals("A")){//现金券
							ticketAmount = ticketAmount + ticket.getInt("amount");
						}else if(ticket.getStr("ttype").equals("C")){//加息券、加息额度、会员等级自动加息
//							int releaseDate=Integer.parseInt(loan.getStr("releaseDate"));
//							if(releaseDate<20171118){
//								return error("15", "2017-11-18之前的标不支持使用加息券", null);
//							}
							if(benefits4new>0){
								return error("15", "新手标不能使用加息", null);
							}
							int rewardRateByYear2=loan.getInt("rewardRateByYear");
							rewardticketrate+=ticket.getInt("rate");
							loan.set("rewardRateByYear", (rewardRateByYear2 + rewardticketrate ) ) ;//将加息券奖励增加到奖励年利率里
						}
						String strUseEx = ticket.getStr("useEx");
						if( StringUtil.isBlank(strUseEx) == false ){
							JSONObject json = JSONObject.parseObject( strUseEx ) ;
							int limit = json.getIntValue("limit");
							int rate = json.getIntValue("rate");
							long tmp = json.getLongValue("amount") ;
							//加息券的额度应大于投标金额--现金券的额度应小于投标金额 ws
							if(ticket.getStr("ttype").equals("A")){
								if(tmp!=0 && amount < tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}else if(ticket.getStr("ttype").equals("C")){
								if(tmp!=0 && amount > tmp){
								return error("77", "请检查理财券使用金额是否符合条件", false);
								}
							}
							if(limit != 0 && loan.getInt("loanTimeLimit") < limit){
								return error("77", "请检查理财券使用期限是否符合条件", false);
							}
							if(rate != 0 && (loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")) < rate ){
								return error("77", "请检查理财券使用利率是否符合条件", false);
							}
							//判断是否符合券可投标期限 20170727 ws
							String la=String.valueOf(loan.getInt("loanTimeLimit"));
							if(la.length()==1){
								la="0"+la;
							}
							if(null!=ticket.getStr("loanMonth")&&ticket.getStr("loanMonth").length()>1&&ticket.getStr("loanMonth").indexOf(la)<0){
								return error("77", "请检查理财券使用标期是否符合条件", false);
							}
							//end
							if(tmp > exAmount){
								exAmount = tmp;
							}
						}
					}
				}
			}
		}
		
		//去小数
		amount = amount - amount%100 ;
		
		//真实投标金额(投标金额-券金额)
		long trueAmount = amount-ticketAmount;
		
		long avBalance = Funds.fundsDao.findByIdLoadColumns(userCode, "avBalance").getLong("avBalance");
		if(avBalance < trueAmount){
			return error("999", "可用余额不足!", null);
		}
		
		int maxLoanAmount = loan.getInt("maxLoanAmount");
		if( maxLoanAmount > 0 && amount > maxLoanAmount ){
			//如果最大投标金额 小于 其中一个现金券的使用条件金额
			if(tickets != null && tickets.size() > 0){
				if(maxLoanAmount < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			//重新计算投标金额+券金额
			//自动投标不受标限制限制,贝贝要求的
			if( loantype.equals("A") == false )
				amount = maxLoanAmount;
			trueAmount =  amount - ticketAmount;
		}
		
		
		//投标金额小于最小投标金额时返回错误
		if( amount < minLoanAmount && loanBalance >= minLoanAmount){
			return error("04", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
		}
		
		//当可投金额小于投标金额时, 自动将可投金额置为投标金额
		if( amount > loanBalance ){
			if(tickets != null && tickets.size() > 0){
				if(loanBalance < exAmount){
					return error("05", "可投金额不足,无法使用该券!","") ;
				}
			}
			//TODO 当为自动投标时，不做余额兼容，不投出有设有最小投标金额的
//			if( loantype.equals("A") == true )
//				return error("14", "投标金额最小要求为：" + minLoanAmount/10/10 + "元", minLoanAmount ) ;
			//重新计算投标金额+券金额
			amount = loanBalance ;
			
			if( onceMinAmount > amount ){
				return error("14", "单次投标金额不满足最小投标金额要求", minLoanAmount ) ;
			}
			
			trueAmount = amount - ticketAmount;
		}
		
		int maxLoanCount = loan.getInt("maxLoanCount");
		Map<String , Long> totalMap = loanTraceService.totalByLoan4user(loanCode , userCode );
		long userLoanCount = totalMap.get("count") ;
		long userTotalAmount = totalMap.get("totalAmount");
		if( userLoanCount > maxLoanCount ){
			return error("06", "投标次数已超限,当前最大投标次数：" + maxLoanCount + "次", maxLoanCount ) ;
		}
		
		//验证投标总额
		if( (userTotalAmount + amount) > maxLoanAmount ){
			amount = maxLoanAmount - userTotalAmount ;
			trueAmount = amount - ticketAmount;
			if( amount <= 0 ){
				return error("07", "投标总额已超，您的该标的投资总额为：" + userTotalAmount/10/10 + "元" , userTotalAmount) ;
			}
		}
		
//		int zdjl = 0;//自动投标奖励利率
//		if( "A".equals( loantype ) == true ){
//			if( autoOnceAmount > amount ){
//				return error("21", "投标金额不符" , "" ) ;
//			}
//			try {
//				int x = loan.getInt("loanTimeLimit");
//				if(x>=1 && x <=6){
//					zdjl = Integer.valueOf( (String) CACHED.get("S1.autoLoanRate16"));
//				}else if(x >= 7 && x<=18){
//					zdjl = Integer.valueOf( (String) CACHED.get("S1.autoLoanRate718"));
//				}
//				
//			} catch (Exception e) {
//				e.printStackTrace();
//				zdjl = 0;
//			}
//		}
		//TODO 更新现金券使用状态
		try {
			if( tickets!=null && tickets.size() > 0){
				for (int i = 0; i < tickets.size(); i++) {
					Tickets tts = tickets.get(i);
					//扣除加息额度 ws 20171122
					if("D".equals(tickettype)){
						fundsServiceV2.deductRewardRateAmount(userCode, amount, 1);
					}else if("E".equals(tickettype)){//会员等级自动加息
					}else{
					if(tts.getStr("ttype").equals("A")||tts.getStr("ttype").equals("C")){
						Tickets tmp = ticketService.useTicket4A(userCode, tts.getStr("tCode"), amount, loan ) ; 
						if( tmp == null ){
							return error("10", "理财券异常:"+tts.getStr("tCode"),"") ;
						}
					}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if("D".equals(tickettype)){
				return error("10", "使用加息额度异常","");
			} 
			return error("10", "理财券异常","");
		}
		
//		if( amount > ticketAmount)
//			amount = amount - ticketAmount ;
		/*
		 * 	1，可用资金转冻结资金
		 * 	2，减少标可用余额，失败回滚资金操作，成功增加流水
		 * 	3，增加资金流水，增加投标流水
		 */
		//操作失败则会抛出异常
		
		Funds afterFunds = null;
		boolean bidResult = false;
		try{
			
//			int ydjl = 0;
			String userAgent = "";
			try {
				userAgent  = getRequest().getHeader( "USER-AGENT" ).toLowerCase(); 
			} catch (Exception e) {
				userAgent = "";
			}
			if(null == userAgent){    
	            userAgent = "";    
	        }
	        //判断是否为移动端访问  
	        if( CommonUtil.check(userAgent)){  
	        	//ydjl = 20;
	        	loantype = "N";
	        }
			
	        //可用余额转冻结余额
	        afterFunds = fundsServiceV2.avBalance2froze(userCode, trueAmount );
	    	//投标 冻结投资人账户余额 2017.5.26 rain  
	    	CommonRspData commonRspDataTender=fuiouTraceService.freeze(user, trueAmount);
			if( ("0000").equals(commonRspDataTender.getResp_code())){
				
//				if(loanBalance-amount<1){
//					loanInfoService.updateLoanByFull(loanCode);//如果投满了，设置满标
//				} 
				//TODO 预投标资金流水，备注信息会记录抵扣金额
				//TODO 单个现金券会记录到投标流水
				bidResult = loanInfoService.update4prepareBid( loan, amount,
						userCode , loantype , rankValue , tickets,0,null,null) ;
				if(bidResult == true){
					//投标成功，记录资金流水
					fundsTraceService.bidTrace(userCode , amount, afterFunds.getLong("avBalance"), 
							afterFunds.getLong("frozeBalance"), 0 ,ticketAmount );
					//手动投标发送短信
					if(loantype.equals("A") == false){
						try {
							String mobile = userService.getMobile(userCode);
							
							String content = CommonUtil.SMS_MSG_MLOAN.replace("[userName]", user.getStr("userName")).replace("[loanNo]", loan.getStr("loanNo"))
									.replace("[payAmount]", CommonUtil.yunsuan(amount+"", "100", "chu", 2).doubleValue()+"");
							SMSLog smsLog = new SMSLog();
							smsLog.set("mobile", mobile);
							smsLog.set("content", content);
							smsLog.set("userCode", userCode);
							smsLog.set("userName", user.getStr("userName"));
							smsLog.set("type", "12");smsLog.set("typeName", "手动投标");
							smsLog.set("status", 9);
							smsLog.set("sendDate", DateUtil.getNowDate());
							smsLog.set("sendDateTime", DateUtil.getNowDateTime());
							smsLog.set("break", "");
							smsLogService.save(smsLog);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return succ("投标成功,投标金额：" + trueAmount/10.0/10.0 + "元", null );
				}else{
					fuiouTraceService.unFreeFunds(user, trueAmount);
					fundsServiceV2.frozeBalance2avBalance(userCode, trueAmount );
					//回滚加息额度
					if("D".equals(tickettype)){
						fundsServiceV2.deductRewardRateAmount(userCode, amount, 0);
					}else{
					if(tickets != null && tickets.size() > 0){
						for (int i = 0; i < tickets.size(); i++) {
							//代金券回滚
							ticketService.rollBackTicket(tickets.get(i).getStr("tCode"));
						}
					}
					} 
//					return error("21", "投标流水添加失败", "");
					return error("21", "投标失败", "");
				}
			}else{
				//操作失败，解冻投资人的金额  2017.6.7 rain
				//冻结资金回滚
				fundsServiceV2.frozeBalance2avBalance(userCode, trueAmount );
				//回滚加息额度
				if("D".equals(tickettype)){
					fundsServiceV2.deductRewardRateAmount(userCode, amount, 0);
				}else{
				if(tickets != null && tickets.size() > 0){
					for (int i = 0; i < tickets.size(); i++) {
						//代金券回滚
						ticketService.rollBackTicket(tickets.get(i).getStr("tCode"));
					}
				}
				}
				return error("08", "存管系统异常，投标失败" , null);
			}
		}catch(Exception e){
			e.printStackTrace( );
			return error("EX", "操作异常:" + e.getMessage() , null ) ;
		}
		
	
	}
	
	/**
	 * 保存并开启自动投标 ws
	 * */
	@ActionKey("/app_autoloan")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void saveAutoLoanSettings(){
		Message msg = null;
		if(!CommonUtil.jxPort){
			msg = error("12", "存管系统功能暂未上限,无法进行此操作", "" );
			renderJson(msg);
			return;
		}
		String userCode = getUserCode();
//		boolean unusualUserCode = loanTraceService.unusualUserCode(userCode);
//		if(unusualUserCode){
//			msg = error("13", "资金异常", "");
//			renderJson(msg);
//			return;
//		}
		User user = userService.findById(userCode);
		String evaluationLevel = user.getStr("evaluationLevel");
		if(StringUtil.isBlank(evaluationLevel)){
			msg = error("F1", "请先完成风险测评再继续设置", "" );
			renderJson(msg);
			return;
		}
		//验证是否开通缴费授权
		JSONObject paymentAuthPageState = jxTraceService.paymentAuthPageState(user.getStr("jxAccountId"));
		if(paymentAuthPageState == null || !"1".equals(paymentAuthPageState.get("type"))){
			msg = error("24", "缴费授权未开通,无法开启自动", "");
			renderJson(msg);
			return;
		}
		AutoLoan_v2 autoLoan = new AutoLoan_v2();
		autoLoan.set("autoType", getPara("autoType","A"));
		autoLoan.set("onceMaxAmount", getParaToLong("onceMaxAmount",999999L)*100);
		autoLoan.set("onceMinAmount", getParaToLong("onceMinAmount",50L)*100);
		autoLoan.set("autoMinLim", getParaToInt("autoMinLim",1));
		autoLoan.set("autoMaxLim", getParaToInt("autoMaxLim",24));
		autoLoan.set("refundType", getPara("refundType","D"));
		autoLoan.set("deadLine",getPara("deadLine"));
		if ("N".equals(autoLoan.get("refundType"))) {
			autoLoan.set("refundType", "D");
		}
		if(autoLoan.getInt("autoMinLim")>autoLoan.getInt("autoMaxLim")){
			msg = error("03", "贷款期限范围最小期限不能大于最大期限", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getStr("autoType").equals("A")){
			autoLoan.set("onceMaxAmount", 99999900L);
			autoLoan.set("onceMinAmount", 5000L);
			autoLoan.set("refundType", "D");
		}
		//获取用户电子账户
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			msg = error("02", "未开通存管", "");
			renderJson(msg);
			return;
		}
		//获取单次最大投标金额
		String txAmount = String.valueOf(autoLoan.getLong("onceMaxAmount")/100);
		//总投标金额  默认999999999
		String totAmount = "999999999";
		//过期日期
//		String deadLine = "20180520";
		String deadLine = autoLoan.getStr("deadLine");
		if(!DateUtil.isValidDate(deadLine, "yyyyMMdd")){
			msg = error("02", "请选择过期时间", "");
			renderJson(msg);
			return;
		}
		//授权码
		AutoLoan_v2 autoLoanv2 = autoLoanService.findByUserCode(userCode);
		String smsCode = "";
		
		
		////--------------------------start
		
		if(autoLoan.getLong("onceMaxAmount") < 5000){
			autoLoan.set("onceMaxAmount", 99999900L);
		}
		if(autoLoan.getLong("onceMinAmount") < 5000){
			autoLoan.set("onceMinAmount", 5000L);
		}
		if(autoLoan.getLong("onceMaxAmount") < 5000){
			msg = error("05", "最大金额必须大于50", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getLong("onceMinAmount") < 5000){
			msg = error("06", "最小投标金额必须大于50", "");
			renderJson(msg);
			return;
		}
		if(autoLoan.getLong("onceMinAmount") > autoLoan.getLong("onceMaxAmount")){
			msg = error("07", "最小投标金额不能大于最大投标金额", "");
			renderJson(msg);
			return;
		}
		String nowDate = DateUtil.getNowDate();
		int x = DateUtil.compareDateByStr("yyyyMMdd", nowDate, deadLine);
		if (x != -1){
			msg = error("08", "过期时间应大于当前日期", "");
			renderJson(msg);
			return;
		}
		if(StringUtil.isBlank(autoLoan.getStr("productType"))){
			autoLoan.set("productType", "");
		}
		
		String priorityMode = getPara("priorityMode","N");
		
		String useTicket = getPara("useTicket","N");
		
		String autoType = "B";
		
		try {
			autoType = autoLoan.getStr("autoType");
			if(StringUtil.isBlank(autoType)){
				autoType = "B";
			}
		} catch (Exception e) {
			autoType = "B";
		}
		try {
			if(useTicket.equals("A")){
				if(StringUtil.isBlank(priorityMode)){
					priorityMode = "A";
				}
				if(priorityMode.equals("A") == false && priorityMode.equals("B") == false){
					priorityMode = "A";
				}
			}
			if(useTicket.equals("C")){
				if(StringUtil.isBlank(priorityMode)){
					priorityMode = "A";
				}
				if(priorityMode.equals("A") == false && priorityMode.equals("C") == false){
					priorityMode = "A";
				}
			}
		} catch (Exception e) {
			msg = error("08", "系统错误", "");
			renderJson(msg);
			return;
		}
		//保存
		String userName = "";
		try {
			userName = userService.findByField(userCode, "userName").getStr("userName");
			if(StringUtil.isBlank(userName)){
				msg = error("09", "用户名异常",false);
				renderJson(msg);
				return;
			}
		} catch (Exception e) {
			msg = error("10", "用户名异常",false);
			renderJson(msg);
			return;
		}
		//-----------------------------end
		//存临时信息
		JSONObject tempData = new JSONObject();
		//自动类型 autoType
		tempData.put("autoType", autoLoan.getStr("autoType"));
		//最大金额 onceMaxAmount
		tempData.put("onceMaxAmount", autoLoan.getLong("onceMaxAmount"));
		//最小金额 onceMinAmount
		tempData.put("onceMinAmount", autoLoan.getLong("onceMinAmount"));
		//最大期限 autoMaxLim
		tempData.put("autoMaxLim", autoLoan.getInt("autoMaxLim"));
		//最小期限 autoMinLim
		tempData.put("autoMinLim", autoLoan.getInt("autoMinLim"));
		//投资类型 refundType
		tempData.put("refundType", autoLoan.getStr("refundType"));
		//投资券类型 useTicket
		tempData.put("useTicket", useTicket);
		//优先方式priorityMode
		tempData.put("priorityMode", priorityMode);
		//短信验证码msgAutoBid
		tempData.put("msgAutoBid", smsCode);
		//过期时间deadline
		tempData.put("deadLine", deadLine);
		String temp = tempData.toString();
		int fff =1;
		try {
			fff = autoMapService.validateAutoState(userCode);
		} catch (Exception e) {
			fff = 1;
		}
		try {
				if(fff==1){
					//查询签约状态
					long  onceMaxAmount = autoLoan.getLong("onceMaxAmount");
					if(onceMaxAmount==autoLoanv2.getLong("onceMaxAmount")&&deadLine.equals(autoLoanv2.getStr("deadLine"))){
						boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, autoLoan.getLong("onceMinAmount"), autoLoan.getLong("onceMaxAmount"), autoLoan.getInt("autoMinLim"), autoLoan.getInt("autoMaxLim"), autoLoan.getStr("refundType"), "ABCD", priorityMode, useTicket, autoType,autoLoanv2.getStr("jxOrderId"),deadLine);
						if(result){
							String info="";
							info+="最小金额:"+autoLoan.getLong("onceMinAmount");
							info+="|最大金额:"+autoLoan.getLong("onceMaxAmount");
							info+="|最小期限:"+autoLoan.getInt("autoMinLim");
							info+="|最大期限:"+autoLoan.getInt("autoMaxLim");
							info+="|还款方式:"+autoLoan.getStr("refundType");
							info+="|自动投标类型:"+autoType;
							info+="|使用理财券类型:"+useTicket;
							info+="|理财券使用优先方式:"+priorityMode;
							info+="|过期时间:"+deadLine;
							info+="|订单号:"+autoLoanv2.getStr("jxOrderId");
							BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "手机保存自动投标配置成功",info);
							msg = succ("设置成功", "OK");
							renderJson(msg);
							return;
							
						}else{
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败", null);
							msg = error("15", "设置失败", "");
							renderJson(msg);
							return;
						}
					}
				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "首次保存自动投标送券异常", e);
			msg = error("13", "保存失败", "");
			renderJson(msg);
			return;
		}
		UserInfo userInfo = userInfoService.findById(userCode);
		if(userInfo == null){
			msg = error("15", "用户认证信息异常", "");
			renderJson(msg);
			return;
		}
		String mobile="";
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
			mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
		} catch (Exception e) {
			msg = error("14", "身份证号或手机号解密异常", "");
			renderJson(msg);
			return;
		}
		String forgotPwdUrl = CommonUtil.APPBACK_ADDRESS+"/changepassword?mobile="+mobile;
		String retUrl = CommonUtil.APPBACK_ADDRESS+"/autobid";
		String notifyUrl = CommonUtil.CALLBACK_URL+"/autoBidResponse";
		String name=userInfo.getStr("userCardName");
		//跳转江西银行存管
		String type = getPara("type");
		if("y".equals(type)){
			//调用多合一合规授权接口
			JXController.termsAuthPage(jxAccountId, name, idNo, "1", "1", "", "", "", txAmount, deadLine, "", "", "", "", "", "", forgotPwdUrl, retUrl, notifyUrl, getResponse(),temp);
			renderNull();
			return;
		}else{
			msg = succ("01", "验证成功");
			renderJson(msg);
			return;
		}
	}

//	public void AppAutoLoan(){
//		String userCode = getUserCode();
//		Message msg=null;
//		AutoLoan_v2 autoLoan = new AutoLoan_v2();
//		autoLoan.set("autoType", getPara("autoType","A"));
//		autoLoan.set("onceMaxAmount", getParaToLong("onceMaxAmount",999999L)*100);
//		autoLoan.set("onceMinAmount", getParaToLong("onceMinAmount",50L)*100);
//		autoLoan.set("autoMinLim", getParaToInt("autoMinLim",1));
//		autoLoan.set("autoMaxLim", getParaToInt("autoMaxLim",24));
//		autoLoan.set("refundType", getPara("refundType","D"));
//		if ("N".equals(autoLoan.get("refundType"))) {
//			autoLoan.set("refundType", "D");
//		}
//		if(autoLoan.getStr("autoType").equals("A")){
//			autoLoan.set("onceMaxAmount", 99999900L);
//			autoLoan.set("onceMinAmount", 5000L);
//			autoLoan.set("refundType", "D");
//		}
//		
//		//验证数据有效性
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			autoLoan.set("onceMaxAmount", 99999900L);
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			autoLoan.set("onceMinAmount", 5000L);
//		}
//		if(autoLoan.getLong("onceMaxAmount") < 5000){
//			msg= error("01", "最大金额必须大于50", null);
//			renderJson(msg);
//			return;
//		}
//		if(autoLoan.getLong("onceMinAmount") < 5000){
//			msg= error("01", "最小投标金额必须大于50", null);
//			renderJson(msg);
//			return;
//		}
//		if(autoLoan.getLong("onceMinAmount") > autoLoan.getLong("onceMaxAmount")){
//			msg= error("01", "最小投标金额不能大于最大投标金额", null);
//			renderJson(msg);
//			return;
//		}
//		
//		if(StringUtil.isBlank(autoLoan.getStr("productType"))){
//			autoLoan.set("productType", "");
//		}
//		
//		String priorityMode = getPara("priorityMode","N");
//		
//		String useTicket = getPara("useTicket","N");
//		
//		String autoType = "B";
//		
//		try {
//			autoType = autoLoan.getStr("autoType");
//			if(StringUtil.isBlank(autoType)){
//				autoType = "B";
//			}
//		} catch (Exception e) {
//			autoType = "B";
//		}
//		try {
//			if(useTicket.equals("A")){
//				if(StringUtil.isBlank(priorityMode)){
//					priorityMode = "A";
//				}
//				if(priorityMode.equals("A") == false && priorityMode.equals("B") == false){
//					priorityMode = "A";
//				}
//			}
//		} catch (Exception e) {
//			
//		}
//		//保存
//		
//		String userName = "";
//		try {
//			userName = userService.findByField(userCode, "userName").getStr("userName");
//			if(StringUtil.isBlank(userName)){
//				msg= error("21", "用户名异常",false);
//				renderJson(msg);
//				return;
//			}
//		} catch (Exception e) {
//			msg= error("21", "用户名异常",false);
//			renderJson(msg);
//			return;
//		}
//		autoLoan.set("userCode", userCode);
//		autoLoan.set("productType", "ABCD");
//		int fff =1;
//		try {
//			fff = autoMapService.validateAutoState(userCode);
//		} catch (Exception e) {
//			fff = 1;
//		}
//		boolean result = autoMapService.saveAutoLoanSettings(userCode, userName, autoLoan.getLong("onceMinAmount"), autoLoan.getLong("onceMaxAmount"), autoLoan.getInt("autoMinLim"), autoLoan.getInt("autoMaxLim"), autoLoan.getStr("refundType"), autoLoan.getStr("productType"),priorityMode,useTicket,autoType);
//		if(result == false){
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置失败", null);
//			msg= error("01", "保存失败", "");
//			renderJson(msg);
//			return;
//		}
//		try {
//			if(fff==0){
//				String nowDate = DateUtil.getNowDate();
//				int x = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161211" );
//				int y = DateUtil.compareDateByStr("yyyyMMdd",nowDate,"20161218" );
//				if((x == 0 || x == 1) && (y == -1 || y == 0)){
//					//11张30元券+11张50元券
//					for (int i = 0; i < 11; i++) {
//						ticketService.saveADV(userCode, "30元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
//						ticketService.saveADV(userCode, "50元现金抵扣券", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
//					}
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.AUTOLOAN, "首次保存自动投标送券异常", e);
//		}
//		String info="";
//		info+="最小金额:"+autoLoan.getLong("onceMinAmount");
//		info+="|最大金额:"+autoLoan.getLong("onceMaxAmount");
//		info+="|最小期限:"+autoLoan.getInt("autoMinLim");
//		info+="|最大期限:"+autoLoan.getInt("autoMaxLim");
//		info+="|还款方式:"+autoLoan.getStr("refundType");
//		info+="|自动投标类型:"+autoLoan.getStr("autoType");
//		info+="|是否使用现金券:"+useTicket;
//		info+="|现金券使用优先方式:"+priorityMode;
//		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "保存自动投标配置成功",info);
//		
//		msg= succ("保存成功", "");
//		renderJson(msg);
//	}
	
	
	/**
	 * 查询自动投标信息 ws
	 * */
	@ActionKey("/app_queryautoloanInfo")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppQueryAutoLoanInfo(){
		String userCode = getUserCode();
		Message msg=null;
		User user = userService.findById(userCode);
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			msg = error("02", "还未开通银行存管账户", "");
			renderJson(msg);
			return;
		}
		Map<String,Object> map = new HashMap<String, Object>();
		AutoLoan_v2 autoLoan_v2 = autoLoanService.findByUserCode(userCode);
		long rewardRateAmount = fundsServiceV2.findById(userCode).getLong("rewardRateAmount");//加息额度
		long cashCount = ticketService.countByTtypeAndTstate(userCode, "A", "A");//现金抵用券数量
		long interestCount = ticketService.countByTtypeAndTstate(userCode, "C", "A");//加息券数量
		if(null==autoLoan_v2){
			msg=error("01", "还未开启自动投标", "");
			renderJson(msg);
			return;
		}
		Funds funds = Funds.fundsDao.findById(userCode);
		long avBa = funds.getLong("avBalance");
		String avBalance = Number.longToString(avBa);
		int x = 0;//排名
		try {
			x = autoMapService.queryRankVal(userCode);
		} catch (Exception e) {
			x = 0;
		}
		String autoType = autoLoan_v2.getStr("autoType");
		String autoMinLim = String.valueOf(autoLoan_v2.getInt("autoMinLim"));
		String autoMaxLim = String.valueOf(autoLoan_v2.getInt("autoMaxLim"));
		String useTicket = autoLoan_v2.getStr("useTicket");
		String priorityMode = autoLoan_v2.getStr("priorityMode");
		map.put("avBalance", avBalance);
		map.put("rank", x);
		map.put("autoType", autoType);
		map.put("autoMinLim", autoMinLim);
		map.put("autoMaxLim", autoMaxLim);
		map.put("useTicket", useTicket);
		map.put("priorityMode", priorityMode);
		map.put("onceMinAmount", "50");
		map.put("onceMaxAmount", "999999");
		map.put("refundType", "D");
		map.put("rewardRateAmount", Number.longToString(rewardRateAmount));
		map.put("cashCount", cashCount);
		map.put("interestCount", interestCount);
		String deadLine = autoLoan_v2.getStr("deadLine");
		if(StringUtil.isBlank(deadLine)){
			deadLine = "";
		}
		map.put("deadLine", deadLine);
		if("B".equals(autoType)){//自定义
			String onceMinAmount=String.valueOf(autoLoan_v2.getLong("onceMinAmount")/100);
			String onceMaxAmount=String.valueOf(autoLoan_v2.getLong("onceMaxAmount")/100);
			String refundType=autoLoan_v2.getStr("refundType");
			map.put("onceMinAmount", onceMinAmount);
			map.put("onceMaxAmount", onceMaxAmount);
			map.put("refundType", refundType);
		}
		msg= succ(autoLoan_v2.getStr("autoState"), map);
		renderJson(msg);
	}
	
	/**
	 * 关闭自动投标 ws
	 * */
	@ActionKey("/app_closeautoloan")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppCloseAutoLoan(){
		Message msg=null;
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		AutoLoan_v2 autoLoanv2 = autoLoanService.findByUserCode(userCode);
		if(null == autoLoanv2 ||"C".equals(autoLoanv2.getStr("autoState"))){
			msg = error( "02", "自动投标已关闭", "");
			renderJson(msg);
			return;
		}
		String jxAccountId = user.getStr("jxAccountId");
		if (StringUtil.isBlank(jxAccountId)) {
			msg = error( "03", "请先激活江西存管账户", "");
			renderJson(msg);
			return;
		}
//		String jxOrderId = autoLoanv2.getStr("jxOrderId");
//		String deadLine = autoLoanv2.getStr("deadLine");
//		if(DateUtil.compareDateByStr("yyyyMMdd", deadLine, DateUtil.getNowDate())>=0){
//			try {
//				Map<String, String> tempMap = JXController.autoBidAuthCancel(jxAccountId, jxOrderId, getResponse());
//				if(null == tempMap || !"00000000".equals(tempMap.get("retCode"))){
//					msg = error("01", "自动投标关闭失败", "");
//					renderJson(msg);
//					return;
//				}
//			} catch (Exception e) {
//				msg = error("02", "自动投标关闭失败，网络连接异常", "");
//				renderJson(msg);
//				return;
//			}
//		}
		autoMapService.changeAutoState2C(userCode);
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.AUTOLOAN, "设置自动投标状态成功");
		msg = succ("成功关闭自动投标", "");
		renderJson(msg);
		return;
	}
	/**
	 * 自动投标队列详情 ws
	 * */
	@ActionKey("/app_autoloanarray")
	@AuthNum(value=999)
	@Before({AppInterceptor.class,PkMsgInterceptor.class})
	public void AppAutoLoanArray(){
		String userCode = getUserCode();
		Message msg=null;
		Map<String,Object> result = new HashMap<String, Object>();
		int auint= autoMapService.validateAutoState(userCode);
		if(auint != 1){
			msg=error("01", "未保存自动投标", result);
			renderJson(msg);
			return;
		}else{
			int rankVal = autoMapService.queryRankVal(userCode);
			if(rankVal>0){
				AutoLoan_v2 aa = autoLoanService.findByUserCode(userCode, "A");
				List<Record> list = autoMapService.queryRankDetail(rankVal,aa.getInt("autoMinLim"),aa.getInt("autoMaxLim"));
				List<Map<String, Object>> resList=new ArrayList<Map<String,Object>>();
				for(int i=0;i<list.size();i++){
					Map<String, Object> map = new HashMap<String, Object>();
					Record record = list.get(i);
					int loanlimit=record.getInt("loanLimit");
					if((loanlimit>18&&loanlimit<24)||(loanlimit>12&&loanlimit<18)){
						continue;
					}
					String amt = String.valueOf(record.get("amount"));
					long amount = Long.parseLong(amt);
					map.put("amount", Number.longToString(amount));
					map.put("loanLimit", String.valueOf(record.get("loanLimit")));
					map.put("quantity", String.valueOf(record.get("quantity")));
					resList.add(map);
					msg=succ("查询完成", resList);
				}
			}else{
				msg=error("01", "未保存自动投标", result);
				renderJson(msg);
				return;
			}
		}
		renderJson(msg);
	}
	
	
	/**
	 * app易分期项目描述设置
	 * */
	private Map<String, Object> resetYistageLoanDesc(Map<String, Object> tmp,LoanInfo loanInfo){
		JSONObject json = JSONObject.parseObject(loanInfo.getStr("loanDesc"));
		String userCode = loanInfo.getStr("userCode");
		YiStageUserInfo yiStageUserInfo = yiStageUserInfoService.queryByUserCode(userCode);
		String usedMobile = yiStageUserInfo.getStr("mobile");
		String userCardName = yiStageUserInfo.getStr("userCardName");
		String userCardId = yiStageUserInfo.getStr("userCardId");
		json.put("loanUserName", usedMobile.substring(0, 3)+"*****"+usedMobile.substring(8, 11));
		json.put("userCardName", userCardName.substring(0, 1)+"**");
		json.put("userCardId", userCardId.substring(0, 4)+"********"+userCardId.substring(14,18));
		json.put("inCome", "10万以内");
		json.put("debt", "无");
		json.put("credit", "无");
		json.put("loanUserPurpose", "旅游消费");
		tmp.put("loanUserInfo", convertLoanUserInfo(json));
		//借款描述
		String loanBrief = "此为个人旅游消费分期产品，每个借款人的身份信息等实体数据都进行关联分析审核，多角度筛选，全方位进行风险量化，确保借款人的还款来源稳定。";
		//风险提示
		String riskPoint = "市场有风险 ，理财需谨慎。请您在投资前，仔细阅读并知悉《网络借贷风险揭示书》";
		//多重保障
		List<String> guarantee = new ArrayList<String>();
		guarantee.add("1.  江西银行存管，对交易进行资金存管。");
		guarantee.add("2.  严格的信用评估及核查制度，引入大数据进行风控筛选。");
		guarantee.add("3.  合作机构对借款人个人经济能力做多维度评估，筛选优质客户。");
		guarantee.add("4.  顾问律所将对易融恒信提供专业的法律帮助。");
		guarantee.add("5.  合作方将进行担保及逾期债权承接。");
		tmp.put("loanBrief", loanBrief);
		tmp.put("riskPoint", riskPoint);
		tmp.put("guarantee", guarantee);
		tmp.put("loanUseWay", "旅游消费");
		return tmp;
	}
}
