package com.dutiantech.controller;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.User;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FtpUtil;
import com.dutiantech.util.Property;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Page;
import com.jx.service.JXService;

public class JXQueryController extends BaseController {
	
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private BanksService banksService = getService(BanksService.class);
	private UserService userService = getService(UserService.class);
	
	/**
	 * 余额查询，根据电子账户
	 * @param accountId  电子账户
	 * @return txCode:交易代码; accountId：电子账户;  name:姓名 ;  accType:账户类型 0-基金账户 1-靠档计息账户 2-活期账户;
	 *         acctUse：账户用途   00000-普通账户  10000-红包账户  01000-手续费账户  00100-担保账户;availBal：可用余额;currBal:账面余额  账面余额-可用余额=冻结金额;
	 *         withdrawFlag:提现开关  0-关闭  1-打开
	 */
	public static Map<String, String> balanceQuery(String accountId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode", "10");
			reqMap.put("retMsg","电子账户不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "balanceQuery");
		reqMap.put("accountId",accountId);
		Map<String,String> balance =JXService.requestCommon(reqMap);
		
		return balance;
		
	}



	/**
	 * 电子账户各项冻结金额查询
	 * @param accountId 电子账户
	 * @return  accountId 电子账户;name 姓名; currBal 账面余额; availBal 可用余额; bidAmt 投标冻结金额; repayAmt 还款冻结金额;
	 *          trnAmt  无密债权冻结金额 ; plAmt 还款/债转后冻结金额; dcAmt 代偿冻结金额
	 */
	public static Map<String, String> freezeAmtQuery(String accountId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode", "10");
			reqMap.put("retMsg","电子账户不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","freezeAmtQuery");
		reqMap.put("accountId",accountId);
		Map<String, String> freezeAmt = JXService.requestCommon(reqMap);
		
		return freezeAmt;
	}

	//
	/**
	 * 近两日电子账户资金交易明细查询
	 * @param accountId 电子账户
	 * @param startDate  起始时间   YYYYMMDD
	 * @param endDate    结束时间   YYYYMMDD
	 * @param type       交易种类   0-所有交易 1-转入交易  2-转出交易  9-指定交易类型
	 * @param tranType   交易类型  type=9必填，后台交易类型
	 * @param rtnInd     翻页标识  空：首次查询；1：翻页查询；其它：非法；
	 * @param inpDate    交易日期  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录交易日期；YYYYMMDD
	 * @param inpTime    交易时间  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录交易时间；HH24MISSTT
	 * @param relDate    自然日期  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录自然日期；YYYYMMDD
	 * @param traceNo    流水号      翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录流水号；
	 * @return  txCode 交易代码;accountId 电子账户;startDate 起始日期;endDate结束日期; name 姓名;subPacks 结果集  list<Map<String,String>> 【accDate 入账日期
	 *           inpDate 交易日期; relDate 自然日期; inpTime 交易时间; traceNo 流水号 该字段不足6位时，请平台自行前补0;tranType 交易类型 ; orFlag 冲正撤销标志 O-原始交易 R-已经冲正或者撤销
	 *           txAmount 交易金额 ;txFlag 交易金额符号; describe 交易描述;currency 货币代码;currBal 交易后余额 ;forAccountId 对手电子账户】
	 */
	public static Map<String, Object> accountDetailsQuery2(String accountId,String startDate,String endDate,String type,String tranType,
			
			String rtnInd,String inpDate,String inpTime,String relDate,String traceNo){
		Map<String, Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			errMap.put("retCode", "10");
			errMap.put("retMsg","电子账户不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(startDate)||StringUtil.isBlank(endDate)){
			errMap.put("retCode","11");
			errMap.put("retMsg","交易的起始时间和结束时间不能为空");
			return errMap;
		}
		if("9".equals(type)){
			if(StringUtil.isBlank(tranType)){
				errMap.put("retCode","12");
				errMap.put("retMsg","交易种类为指定交易类型时，交易类型不能为空");
				return errMap;
			}
		}
		
		Map<String,String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","accountDetailsQuery2");
		reqMap.put("accountId",accountId);
		reqMap.put("startDate",startDate);
		reqMap.put("endDate",endDate);
		reqMap.put("type",type);
		reqMap.put("tranType",tranType);
		reqMap.put("rtnInd",rtnInd);
		reqMap.put("inpDate",inpDate);
		reqMap.put("inpTime", inpTime);
		reqMap.put("relDate",relDate);
		reqMap.put("traceNo",traceNo);
		Map<String, String> accountDatils = JXService.requestCommon(reqMap);
		
		String subPacks = accountDatils.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(accountDatils);
		map.put("subPacks", list);
		return map;
	}

	//
	/**
	 * 投资人债权明细查询
	 * @param accountId  电子账户
	 * @param productId  查询标的号   为空查询所有名下所有债权；标的号不区分大小写；
	 * @param state      查询记录状态 0-所有债权  1-有效债权（投标成功，且本息尚未返还完成）
	 * @param startDate  起始时间   YYYYMMDD
	 * @param endDate    结束时间    YYYYMMDD
	 * @param pageNum    页数            查询页数
	 * @param pageSize   页长            每页笔数
	 * @return txCode 交易明细;accountId 电子账户;productId 标的号;state 查询记录状态;startDate 起始日期; endDate 结束日期;
	 *         name 姓名;pageNum 页数;pageSize 页长;totalItems 总记录数;subPacks 结果集  list<Map<String,String>>【
	 *           productId 标的号;buyDate 投标日期;orderId 订单号;txAmount 交易金额; yield 预期年化收益率; forIncome 预期收益;
	 *           income 实际收益; incFlag  实际收益符号; endDate 到期日 ;state 状态;
	 *         】;
	 */
	public static Map<String,Object> creditDetailsQuery(String accountId,String productId,String state,String startDate,String endDate,String pageNum,String pageSize){
		
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			errMap.put("retCode", "10");
			errMap.put("retMsg","电子账户不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(state)){
			errMap.put("retCode","13");
			errMap.put("retMsg","查询记录状态不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(startDate)||StringUtil.isBlank(endDate)||com.dutiantech.util.DateUtil.compareDateByStr("yyyyMMdd", startDate, endDate)>=1){
			errMap.put("retCode","11");
			errMap.put("retMsg","开始时间或结束时间不对");
			return errMap;	
		}
		if(StringUtil.isBlank(pageNum)||StringUtil.isBlank(pageSize)){
			errMap.put("retCode","14");
			errMap.put("retMsg","页数或页长不能为空");
			return errMap;
		}
		Map<String,String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("accountId",accountId);
		reqMap.put("txCode","creditDetailsQuery");
		reqMap.put("productId",productId);
		reqMap.put("state",state);
		reqMap.put("startDate",startDate);
		reqMap.put("endDate",endDate);
		reqMap.put("pageNum", pageNum);
		reqMap.put("pageSize",pageSize);
		Map<String,String> creditDetails = JXService.requestCommon(reqMap);
		
		String subPacks = creditDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(creditDetails);
		map.put("subPacks", list);
		return map;
	}

	//
	/**
	 * 绑卡关系查询
	 * @param accountId  电子账户
	 * @param state      查询状态  0-所有（默认） 1-当前有效的绑定卡
	 * @return  txCode  交易代码; accountId 电子账户; name 姓名   转让方姓名; totalItems 总记录数;subPacks 结果集  list<Map<String,String>>【
	 *           cardNo 银行卡号; txnDate 绑定日期;txnTime 绑定时间;canclDate 解绑日期;canclTime 解绑时间
	 *          】
	 */
	public static Map<String, Object> cardBindDetailsQuery(String accountId,String state){
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			errMap.put("retCode","10");
			errMap.put("retMsg", "电子账户不能为空");
			return errMap;
		}
		Map<String,String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("accountId",accountId);
		reqMap.put("state", state);
		reqMap.put("txCode","cardBindDetailsQuery");
		Map<String,String> cardBindDetails = JXService.requestCommon(reqMap);
		
		String subPacks = cardBindDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(cardBindDetails);
		map.put("subPacks", list);
		return  map;
	}

	//
	/**
	 * 按证件号查询电子账号
	 * @param idType  证件类型  01-身份证 （18位）
	 * @param idNo  证件号码  
	 * @return  txCode 交易代码; idType 证件类型;idNo 证件号码;accountId 电子账号;openDate 开户日期;acctState 账户状态 空-正常  A-待激活 Z-注销;frzState 冻结状态  空-未冻结  J-司法冻结;pinLosCd  密码冻结状态  空-未挂失  Q-已挂失; 
	 */
	public static Map<String,String> accountIdQuery(String idType,String idNo){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(idType)){
			reqMap.put("retCode","15");
			reqMap.put("retMsg","证件号类型不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(idNo)){
			reqMap.put("retCode", "16");
			reqMap.put("retMsg","证件号码不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("idNo",idNo);
		reqMap.put("idType",idType);
		reqMap.put("txCode","accountIdQuery");
		Map<String, String> accountId = JXService.requestCommon(reqMap);
		
		return accountId;
	}

	//
	/**
	 * 借款人标的信息查询
	 * @param accountId  电子账户
	 * @param productId  标的号
	 * @param startDate  起始时间  YYYYMMDD
	 * @param endDate    结束时间   YYYYMMDD
	 * @param pageNum    页数
	 * @param pageSize   页长
	 * @return txCode 交易代码;accountId 电子账号;startDate 起始日期;endDate 结束日期;name 姓名;
	 *         pageNum 页数;pageSize 页长;totalItems 总记录数;subPacks 结果集  list<Map<String,String>>【
	 *          productId 标的号; raiseDate 募集日;raiseEndDate 募集结束日期;intType 付息方式 0-到期与本金一起归还1-每月固定日期支付2-每月不确定日期支付 平台仅记录;
	 *           duration 借款期限;txAmount 交易金额; rate 年化利率; fee 品台手续费;bailaccountId 担保账户    ;intDate 起息日;raiseAmount 募集金额;repaymentAmt 已还金额;repayMentInt 已还利息;state 状态;
	 *         】
	 */
	public static Map<String,Object> debtDetailsQuery(String accountId,String productId,String startDate,String endDate,String pageNum,String pageSize){
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(pageNum)||StringUtil.isBlank(pageSize)){
			errMap.put("retCode", "11");
			errMap.put("retMsg","页数或页长不能为空");
			return errMap;
		}if(StringUtil.isBlank(accountId)){
			errMap.put("retCode","10");
			errMap.put("retMsg","电子账户不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(productId) && (StringUtil.isBlank(startDate)||StringUtil.isBlank(endDate))){
			errMap.put("retCode","17");
			errMap.put("retMsg","标号和时间不能同时为空");
			return errMap;
		}
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","debtDetailsQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("productId",productId);
		reqMap.put("startDate",startDate);
		reqMap.put("endDate",endDate);
		reqMap.put("pageNum", pageNum);
		reqMap.put("pageSize",pageSize);
		Map<String, String> debtDetails = JXService.requestCommon(reqMap);
		
		String subPacks = debtDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(debtDetails);
		map.put("subPacks", list);
		return  map;
	}

	//
	/**
	 * 电子账户手机号查询
	 * @param accountId  电子账户
	 * @param option     选项  0-查询
	 * @param mobile     手机号
	 * @return  txCode 交易代码;accountId 电子账户;idType 证件类型;idNo 证件号码;name 姓名;mobile 手机号;
	 */
	public static Map<String,String> mobileMaintainace(String accountId,String option,String mobile){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(option)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg", "选项不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "mobileMaintainace");
		reqMap.put("accountId",accountId);
		reqMap.put("option",option);
		reqMap.put("mobile",mobile);
		Map<String,String> mobileMaintainace = JXService.requestCommon(reqMap);
		
		return mobileMaintainace;
	}

	//
	/**
	 * 按手机号查询电子账号信息
	 * @param mobile 手机号
	 * @return txCode 交易代码; accountId 电子账户;idType 证件类型;idNo 证件号码;name 姓名;mobile 手机号;acctState 账户状态  空-正常  A-待激活  C-止付   Z-注销;addr 地址;identity 身份角色1：出借角色2：借款角色3：代偿角色4：平台角色
	 */
	public static Map<String,String> accountQueryByMobile(String mobile){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(mobile)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg","手机号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "accountQueryByMobile");
		reqMap.put("mobile",mobile);
		Map<String,String> accountQueryByMobile = JXService.requestCommon(reqMap);
		
		return accountQueryByMobile;
	}

	//
	/**
	 * 查询交易状态      可以查询 批次放款、批次还款、批次融资人还担保账户垫款、批次结束债权交易、批次投资人购买债权、批次担保账户代偿
	 * @param accountId  电子账户  若原交易有电子账号，必填
	 * @param reqType   查询类别 1-按流水号查询（批次类交易不可用） 2-按订单号查询
	 * @param reqTxCode  查询交易代码    
	 * @param reqTxDate  查询交易日期  reqType=1，必填
	 * @param reqTxTime  查询交易时间  reqType=1，必填
	 * @param reqSeqNo   查询交易流水号  reqType=1，必填
	 * @param reqOrderId  查询订单号  reqType=2，必填
	 * @return txCode 交易代码; accountId 电子账户;reqType 查询类别;reqTxCode 查询交易代码;reqTxDate 查询交易日期;reqTxTime 查询交易时间;
	 *         reqSeqNo 查询交易流水;reqOrderId 查询订单号;txState 交易状态 S-成功  F-失败  N-交易不存在  Z-未知  D-待处理;txAmount 交易金额  若被查询交易有txAmount出现;failMsg 失败描述 txState为F时有效
	 */
	public static Map<String,String> transactionStatusQuery(String accountId,String reqType,String reqTxCode,String reqTxDate,String reqTxTime,String reqSeqNo,String reqOrderId){
		Map<String, String> reqMap = new TreeMap<>();	
		if(StringUtil.isBlank(reqType)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg","查询类别不能为空");
			return reqMap;
		}
		
		if("1".equals(reqType)){
			if(StringUtil.isBlank(reqTxDate)||StringUtil.isBlank(reqTxTime)||StringUtil.isBlank(reqSeqNo)){
				reqMap.put("retCode","17");
				reqMap.put("retMsg","按流水号查询时,交易日期时间流水号不能为空");
				return reqMap;
			}
			
		}else if("2".equals(reqType)){
			if(StringUtil.isBlank(reqOrderId)){
				reqMap.put("retCode","17");
				reqMap.put("retMsg","按订单号查询时,订单号不能为空");
				return reqMap;
			}
		}
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","transactionStatusQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("reqType", reqType);
		reqMap.put("reqTxCode",reqTxCode);
		reqMap.put("reqOrderId",reqOrderId);
		reqMap.put("reqTxDate", reqTxDate);
		reqMap.put("reqRxTime",reqTxTime);
		reqMap.put("reqSeqNo",reqSeqNo);
		
		Map<String, String> transactionStatis = JXService.requestCommon(reqMap);
		
		return transactionStatis;
	}

	//
	/**
	 * 查询批次状态
	 * @param batchTxDate  批次交易日期  YYYYMMDD
	 * @param batchNo   批次号
	 * @return txCode 交易代码; batchTxDate 批次交易日期;batchNo 批次号;batchTxCode 批次交易代码;relAmount 批次处理金额;relCounts 批次处理笔数;
	 *          batchState 批次处理状态   待处理    --A 处理中    --D  处理结束  --S  处理失败  --F  已撤销    --C;failMsg 失败描述;sucCounts 成功笔数;sucAmounts 成功金额;failCounts 失败笔数;failAmount 失败金额;
	 */
	public static Map<String,String> batchQuery(String batchTxDate,String batchNo){
		
		Map<String, String>reqMap = new TreeMap<>();
		if(StringUtil.isBlank(batchTxDate)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg","批次交易日期为空");
			return reqMap;
		}
		if(StringUtil.isBlank(batchNo)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg","批次号不能为空");
			return reqMap;
		}
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","batchQuery");
		reqMap.put("batchTxDate",batchTxDate);
		reqMap.put("batchNo",batchNo);
		Map<String, String> batchQuery = JXService.requestCommon(reqMap);
		
		return  batchQuery;
	}

	//
	/**
	 * 查询批次交易明细状态
	 * @param batchTxDate 批次交易日期 YYYYMMDD
	 * @param batchNo   批次号
	 * @param type  交易种类 0-所有交易(包括1，2，不包括9 ) 1-成功交易  2-失败交易  9-合法性校验失败交易
	 * @param pageNum  查询页数
	 * @param pageSize  每页笔数
	 * @return txCode 交易代码;batchTxDate 批次交易日期;batchNo 批次号;type 交易种类;batchTxCode 批次交易代码;pageNum 页数; pageSize 页长;
	 *          totalItems 总记录数;subPacks 结果集 list<Map<String,String>>【accountId 电子账号;orderId 订单号;txAmount 交易金额 ;forAccountId 对手电子账号;productId 标的号;
	 *          authCode 授权码;txState 交易状态  S-成功 F-失败  A-待处理  D-正在处理  C-撤销;failMsg 失败描述;
	 *          】
	 */
	public static Map<String,Object> batchDetailsQuery( String batchTxDate,String batchNo,String type,String pageNum,String pageSize){
		Map<String,String> reqMap = new TreeMap<>();
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(batchTxDate)){
			errMap.put("retCode","17");
			errMap.put("retMsg","批次交易日期为空");
			return errMap;
		}
		if(StringUtil.isBlank("batchNo")){
			errMap.put("retCode","17");
			errMap.put("retMsg","批次号为空");
			return errMap;
		}
		if(StringUtil.isBlank(type)){
			errMap.put("retCode","17");
			errMap.put("retMsg","交易种类为空");
			return errMap;
		}
		if(StringUtil.isBlank(pageNum)||StringUtil.isBlank(pageSize)){
			errMap.put("retCode","17");
			errMap.put("retMsg","页数或页长为空");
			return errMap;
		}
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","batchDetailsQuery");
		reqMap.put("batchTxDate",batchTxDate);
		reqMap.put("batchNo",batchNo);
		reqMap.put("type",type);
		reqMap.put("pageNum",pageNum);
		reqMap.put("pageSize",pageSize);
		Map<String, String> batchDetails = JXService.requestCommon(reqMap);
		
		String subPacks = batchDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(batchDetails);
		map.put("subPacks", list);
		return  map;
	}
	
	/**
	 * 查询批次交易明细全部流水(不分页) WJW
	 * @param txDate	交易日期
	 * @param batchNo	批次号
	 * @param type		交易种类 0-所有交易(包括1，2，不包括9 ) 1-成功交易 2-失败交易 9-合法性校验失败交易
	 * @return
	 */
	public static Map<String, Object> batchDetailsQueryAll(String txDate,String batchNo,String type){
		int i = 1;
		List<Map<String, String>> subPackMaps = new ArrayList<Map<String,String>>();
		List<Map<String,String>> subPacks = null;
		Map<String,Object> batchDetailsQueryAll = null;
		do{
			Map<String, Object> batchDetailsQueryMap = JXQueryController.batchDetailsQuery(txDate, batchNo, type, i+"", "99");//批次交易存管处理明细
			String retCode = String.valueOf(batchDetailsQueryMap.get("retCode"));//响应代码
			if(!"00000000".equals(retCode)){
				return batchDetailsQueryMap;
			}
			subPacks = (List<Map<String, String>>) batchDetailsQueryMap.get("subPacks");
			if(i == 1){//查询结果正常参数初始化
				batchDetailsQueryAll = batchDetailsQueryMap;
				subPackMaps = subPacks;
			}else {
				subPackMaps.addAll(subPacks);
			}
			i++;
		}while(subPacks.size() == 99);//江西批次交易明细查询接口pageSize最大值99
		batchDetailsQueryAll.put("subPacks", subPackMaps);
		return batchDetailsQueryAll;
	}

	//
	/**
	 * 投资人购买债权查询
	 * @param accountId  电子账户
	 * @param orgOrderId   原购买债权订单号
	 * @return  txCode 交易代码;accountId  电子账号;name 姓名;forAccountId 对手电子账号;
	 *          forName 对手姓名;tsfAmount 转让金额;txAmount  转让价格 成交价格，买入方实际付出金额;
	 *          availAmount 剩余可转让金额;txFee 转让手续费;txIncome  转让所得  转让所得=转让价格-转让手续费;authCode 授权码;
	 */
	public static Map<String,String> creditInvestQuery(String accountId,String orgOrderId){
		Map<String, String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgOrderId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","原订单号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","creditInvestQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("orgOrderId",orgOrderId);
		Map<String, String> creditInvest = JXService.requestCommon(reqMap);
		return creditInvest;
	}

	//
	/**
	 * 投资人投标申请查询
	 * @param accountId  电子账户
	 * @param orgOrderId  原投标订单号
	 * @return txCode 交易代码;accountId  电子账号;name 姓名;productId  标的号;txAmount 投标金额;forIncome 预期收益;buyDate 投标日期  YYYYMMDD;
	 *         state 状态 1：投标中   2：计息中 4：本息已返还  9：已撤销  ;authCode 授权码; bonusAmount 抵扣红包金额;
	 */
	public static Map<String,String> bidApplyQuery(String accountId,String orgOrderId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgOrderId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","原订单号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","bidApplyQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("orgOrderId",orgOrderId);
		Map<String,String> bidApply = JXService.requestCommon(reqMap);
		
		return  bidApply;
	}
	/**
	 * 20180930起接口停用
	 * 投资人签约状态查询
	 * @param type 1 自动投标签约   2 自动债转签约
	 * @param accountId  电子账户
	 * @return  txCode 交易代码;accountId 电子账号;type 查询类型;state 签约状态 0：未签约  1：已签约; orderId 签约订单号; txnDate 签约日期;txnTime 签约时间;
	 *          txAmount 交易金额;totAmount 总交易金额;bidDeadline 签约到期日期;
	 */
	@Deprecated
	public static Map<String,String> creditAuthQuery(String type,String accountId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户为空");
			return reqMap;
		}
		if(StringUtil.isBlank(type)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","查询类型不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","creditAuthQuery");
		reqMap.put("type",type);
		reqMap.put("accountId",accountId);
		Map<String,String> creditAuth = JXService.requestCommon(reqMap);
		
		return creditAuth;
	}

	//
	/**
	 * 企业账户查询
	 * @param accountId  电子账号
	 * @return txCode 交易代码;accountId 电子账号;idType 证件类型  20：其他证件（组织机构代码）25：社会信用号;idNo 证件号码;name 姓名;mobile 手机号;caccount 对公账号;busId 营业执照编号;taxId 税务登记号; 
	 */
	public static Map<String,String> corprationQuery(String accountId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","corprationQuery");
		reqMap.put("accountId",accountId);
		Map<String, String> corpration = JXService.requestCommon(reqMap);
		return  corpration;
	}

	//
	/**
	 * 账户资金冻结明细查询
	 * @param accountId 电子账号
	 * @param state 查询记录状态  0: 正常    9：所有
	 * @param productId  标的号  首次查询时使用，为空时查询所有的产品，不为空时按输入的产品查询
	 * @param rtnInd  翻页标志   首次查询上送空；翻页查询上送1
	 * @param frzDate  还款冻结日期  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录的还款冻结日期；
	 * @param lastOrderId  订单号 翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录的申请流水号；
	 * @param fuissuerPage  产品发行方   翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录的产品发行方；
	 * @param productIdPage  标的号  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录的产品编号；
	 * @return txCode 交易代码;accountId 电子账号;name 姓名;currNum 本次返回交易条数;rtnInd 翻页标志 0：查询完毕  1：需继续翻页查询;subPacks 结果数组【
	 *         fuissuerPage 产品发行方;frzDate 还款冻结日期;orderId 订单号;amount 金额;state 状态  0: 正常  1：已撤销  ;productId 标的号;
	 * 】
	 */
	public static Map<String,Object> frzDetailsQuery(String accountId,String state,String productId
			
			,String rtnInd,String frzDate,String lastOrderId,String fuissuerPage,String productIdPage){
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			errMap.put("retCode","10");
			errMap.put("retMsg","电子账户为空");
			return errMap;
		}
		if(StringUtil.isBlank(state)){
			errMap.put("retCode","10");
			errMap.put("retMsg","查询记录状态不能为空");
			return errMap;
		}
		if(!StringUtil.isBlank(rtnInd)){
			if(StringUtil.isBlank(frzDate)||StringUtil.isBlank(lastOrderId)||StringUtil.isBlank(fuissuerPage)||StringUtil.isBlank(productIdPage)){
				errMap.put("retCode","10");
				errMap.put("retMsg","翻页参数不完整");
				return errMap;
			}
		}
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","frzDetailsQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("state",state);
		reqMap.put("productId",productId);
		reqMap.put("rtnInd",rtnInd);
		reqMap.put("frzDate",frzDate);
		reqMap.put("lastOrderId",lastOrderId);
		reqMap.put("fuissuerPage",fuissuerPage);
		reqMap.put("productIdPage",productIdPage);
		Map<String, String> frzDetails = JXService.requestCommon(reqMap);
		
		String subPacks = frzDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(frzDetails);
		map.put("subPacks", list);
		return  map;
	}

	//txCode 交易代码 ;accountId 电子账号;txAmount 交易金额;state  状态 0-正常   1-已撤销;productId 标的号;
	/**
	 * 电子账户密码是够设置查询
	 * @param accountId  电子账户
	 * @return txCode 交易代码;accountId 电子账号;pinFlag 是否设置过密码 0-无密码  1-有密码;
	 */
	public static Map<String,String> passwordSetQuery(String accountId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","passwordSetQuery");
		reqMap.put("accountId",accountId);
		Map<String,String> passwordSet = JXService.requestCommon(reqMap);
		
		return passwordSet;
	}

	//
	/**
	 * 单笔还款申请冻结查询
	 * @param accountId  电子账号
	 * @param orgOrderId  原订单号
	 * @return txCode 交易代码 ;accountId 电子账号;txAmount 交易金额;state  状态 0-正常   1-已撤销;productId 标的号;
	 */
	public static Map<String,String> balanceFreezeQuery(String accountId,String orgOrderId){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg","电子账户为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgOrderId)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg","原订单号为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","balanceFreezeQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("orgOrderId",orgOrderId);
		Map<String, String> balanceFreeze = JXService.requestCommon(reqMap);
		
		return balanceFreeze;
	}

	//
	/**
	 * 查询批次发红包交易明细
	 * @param batchTxDate  批次交易日期 YYYYMMDD
	 * @param batchNo 批次号
	 * @param type 交易种类  0-所有交易(包括1，2，不包括9 )  1-成功交易 2-失败交易 9-合法性校验失败交易
	 * @param pageNum  查询页数
	 * @param pageSize  每页笔数
	 * @return  txCode 交易代码;batchTxDate 批次交易日期;batchNo  批次号;type 交易种类;batchTxCode  批次交易代码;pageNum 页数;pageSize 页长;totalItems 总记录数;subPacks 结果集 list<Map<String,String>>【
	 *          orderId 订单号;txAmount 交易金额;forAccountId 对手电子账号;voucherType 红包类型 001-红包发放  002-加息券收益发放; name 持卡人姓名;tradeDesc 自定义交易描述;txState 
	 *          交易状态  S-成功  F-失败  A-待处理  D-正在处理  C-撤销 ;failMsg 失败描述;
	 * 】
	 */
	public static Map<String,Object> batchVoucherDetailsQuery(String batchTxDate,String batchNo,String type,String pageNum,String pageSize){
		Map<String,Object> errMap = new TreeMap<>();
		
		if(StringUtil.isBlank(batchTxDate)){
			errMap.put("retCode","17");
			errMap.put("retMsg","批次交易日期不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(batchNo)){
			errMap.put("retCode","17");
			errMap.put("retMsg","批次号不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(type)){
			errMap.put("retCode","17");
			errMap.put("retMsg","交易种类不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(pageNum)||StringUtil.isBlank(pageSize)){
			errMap.put("retCode","17");
			errMap.put("retMsg","页数或页长为空");
			return errMap;
		}
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "batchVoucherDetailsQuery");
		reqMap.put("batchTxDate",batchTxDate);
		reqMap.put("batchNo",batchNo);
		reqMap.put("type",type);
		reqMap.put("pageNum",pageNum);
		reqMap.put("pageSize",pageSize);
		Map<String,String> batchVoucherDetails = JXService.requestCommon(reqMap);
		
		String subPacks = batchVoucherDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(batchVoucherDetails);
		map.put("subPacks", list);
		return map;
	}

	//
	/**
	 * 单笔资金类业务交易查询
	 * @param accountId  电子账户
	 * @param orgTxDate  原交易日期  YYYYMMDD
	 * @param orgTxTime  原交易时间  HHMMSS
	 * @param orgSeqNo   原交易流水号
	 * @return  txCode 交易代码;accountId 电子账号;name 姓名;txAmount 交易金额 ;orFlag 冲正撤销标志  0:正常   1：已冲正/撤销;result 交易处理结果 00：成功 其它：无该交易或者处理失败;
	 */
	public static Map<String,String> fundTransQuery(String accountId,String orgTxDate,String orgTxTime,String orgSeqNo){
		Map<String,String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg", "电子账户不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgTxDate)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg", "原交易日期不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgTxTime)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg", "原交易时间不能为空");
			return reqMap;
		}
		if(StringUtil.isBlank(orgSeqNo)){
			reqMap.put("retCode","17");
			reqMap.put("retMsg", "原交易流水号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "fundTransQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("orgTxDate",orgTxDate);
		reqMap.put("orgTxTime",orgTxTime);
		reqMap.put("orgSeqNo", orgSeqNo);
		Map<String,String> fundTrans = JXService.requestCommon(reqMap);
		
		return fundTrans;
	}

	//
	/**
	 * 债权转让明细查询接口
	 * @param accountId  转让方电子账户
	 * @param orgOrderId  原交易申请流水号（ 转让方投标交易的流水号  ）
	 * @param forAccountId 承接方电子账号  为空时查询该客户该笔债权的所有转让信息
	 * @param turnPage 翻页标志  首次查询上送空；翻页查询上送1；
	 * @param orderId  翻页控制使用；首次查询上送空；翻页查询时上送上页返回的最后一条记录的申请流水号；
	 * @return txCode  交易代码;accountId 电子账号;name 持卡人姓名;count 本次返回交易条数;turnPage  翻页标志 0：查询完毕  1：需继续翻页查询;subPacks 结果集 list<Map<String,String>>【
	 *         orderId 转让交易申请流水号;forAccountId 承接方电子账号;forName 承接方姓名;time 原始投标人购买债权的日期;tsfAmount 转让金额;tsfPrice 转让价格;txFee 转让手续费;tsfEarnings 转让所得  其值为：转让价格—转让手续费
	 * 】
	 */
	public static Map<String,Object> creditInvesDetailsQuery(String accountId,String orgOrderId,String forAccountId,String turnPage,String orderId){
		Map<String,Object> errMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			errMap.put("retCode","10");
			errMap.put("retMsg","电子账户不能为空");
			return errMap;
		}
		if(StringUtil.isBlank(orgOrderId)){
			errMap.put("retCode","10");
			errMap.put("retMsg","原交易申请流水号不能为空");
			return errMap;
		}
		Map<String,String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","creditInvesDetailsQuery");
		reqMap.put("accountId",accountId);
		reqMap.put("orgOrderId",orgOrderId);
		reqMap.put("forAccountId",forAccountId);
		reqMap.put("turnPage", turnPage);
		reqMap.put("orderId",orderId);
		Map<String,String> creditInvesDetails = JXService.requestCommon(reqMap);
		
		String subPacks = creditInvesDetails.get("subPacks");
		JSONArray joSubPacks = JSONArray.parseArray(subPacks);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		if(joSubPacks != null && !"".equals(joSubPacks.toString())){
			for(int i = 0;i<joSubPacks.size();i++){
				Map<String,String> map = new HashMap<>();
				JSONObject jObject = joSubPacks.getJSONObject(i);
				Iterator<String> iterator = jObject.keySet().iterator();
				while(iterator.hasNext()){
					String key = iterator.next();
					String value = jObject.getString(key);
					map.put(key, value);
				}
				
				list.add(map);
			}
		}
		Map<String,Object> map = new TreeMap<String,Object>(creditInvesDetails);
		map.put("subPacks", list);
		return  map;
	}

	/**
	 * 客户授权功能查询
	 * @param accountId  电子账号
	 * @return txCode 交易代码;accountId 电子账号;name 持卡人姓名;autoBid 自动投标功能开通标志 0：未开通 1：已开通 ;autoTransfer 自动债转功能开通标志 0：未开通 1：已开通;
	 *         agreeWithdraw 预约取现功能开通标志  0：未开通 1：已开通;agreeDeduct 代扣签约  0：未开通 1：已开通; paymentAuth 缴费授权  0：未开通 1：已开通;repayAuth 还款授权  0：未开通 1：已开通;
	 *         autoBidDeadline  自动投标到期日; autoBidMaxAmt 自动投标签约最高金额;paymentDeadline 缴费授权到期日;paymentMaxAmt 缴费签约最高金额;
	 */
	public static Map<String,String> termsAuthQuery(String accountId){
		Map<String, String> reqMap = new TreeMap<>();
		if(StringUtil.isBlank(accountId)){
			reqMap.put("retCode","10");
			reqMap.put("retMsg", "电子账号不能为空");
			return reqMap;
		}
		
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode","termsAuthQuery");
		reqMap.put("accountId",accountId);
		Map<String,String> termsAuth = JXService.requestCommon(reqMap);
		
		return termsAuth;
	}
	
	
	//通过手机号查询电子账号
	public String accountIdByMobile(String mobile){
		User user = userService.findByMobile(mobile);
		String accountId = mobile;
		
		if(user !=null){
			if(!StringUtil.isBlank(user.getStr("jxAccountId"))){
				accountId = user.getStr("jxAccountId");
			};
		}
		return accountId;
	}
	//江西存管基本信息查询
	@ActionKey("/baseInformationQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message baseInformationQuery(){
		String accountId = getPara("accountId","");
		if(StringUtil.isBlank(accountId)){
			return error("01", "此用户尚未开通江西存管", null);
		}
		
		User user = userService.findByJXAccountId(accountId);
		if(user == null){
			return error("01","无此电子账户对应的平台用户","");
		}
		User userAll = userService.findUserAllInfoById(user.getStr("userCode"));
		String cardId = userAll.getStr("userCardId");
		String userCardName = userAll.getStr("userCardName");
		String idNo = "";
		try {
			idNo = CommonUtil.decryptUserCardId(cardId);
		} catch (Exception e1) {
			e1.printStackTrace();
			return error("01","解密身份证发生错误","");
		}
		Map<String, String> map = new TreeMap<>();
		Map<String, String> balanceQuery = null;
		Map<String, String> freezeAmtQuery = null;
//		Map<String,String>  mobileMaintainace = null;
		Map<String, String> accountIdQuery = null;
		
		map.put("idNo",idNo);
		map.put("name",userCardName);
		try{
			// 根据存管电子账号查询绑查关系(只查有效卡)
			Map<String, Object> cardDetails = JXQueryController.cardBindDetailsQuery(accountId, "1");
			String cardNo = "";
			if (cardDetails != null && "00000000".equals(cardDetails.get("retCode"))) {// 查询成功

			   List<Map<String, String>> list = (List<Map<String, String>>) cardDetails.get("subPacks");
				if (list != null && list.size() > 0) {

				    Map<String, String> cardMap = list.get(0);
				    cardNo = cardMap.get("cardNo");// 有效存管卡号
				}
			}
			
			map.put("cardNo",StringUtil.isBlank(cardNo)?"":cardNo);
			
			balanceQuery = balanceQuery(accountId);
			if("00000000".equals(balanceQuery.get("retCode"))){
				String accTypeValue = getAccTypeValue(balanceQuery.get("accType"));
				String acctUseValue = getAcctUseValue(balanceQuery.get("acctUse"));
				map.put("accType",accTypeValue);
				map.put("acctUse",acctUseValue);
				map.put("availBal",balanceQuery.get("availBal"));
				map.put("currBal",balanceQuery.get("currBal"));
				if("0".equals(balanceQuery.get("withdrawFlag"))){
					map.put("withdrawFlag","关闭");
				}else if("1".equals(balanceQuery.get("withdrawFlag"))){
					map.put("withdrawFlag","打开");
				}
			}
			freezeAmtQuery = freezeAmtQuery(accountId);
			if("00000000".equals(freezeAmtQuery.get("retCode"))){
				map.put("bidAmt", freezeAmtQuery.get("bidAmt"));
				map.put("repayAmt",freezeAmtQuery.get("repayAmt"));
				map.put("trnAmt",freezeAmtQuery.get("trnAmt"));
				map.put("plAmt",freezeAmtQuery.get("plAmt"));
				map.put("dcAmt",freezeAmtQuery.get("dcAmt"));
			}
			
//			mobileMaintainace = mobileMaintainace(accountId,"0","");
//			if("00000000".equals(mobileMaintainace.get("retCode"))){
//				idNo = mobileMaintainace.get("idNo");
//				map.put("idNo",mobileMaintainace.get("idNo"));
//				map.put("name",mobileMaintainace.get("name"));
//				map.put("mobile",mobileMaintainace.get("mobile"));
//			}
			
			
			if(!StringUtil.isBlank(idNo)){
				accountIdQuery = accountIdQuery("01", idNo);
				if("00000000".equals(accountIdQuery.get("retCode"))){
					map.put("openDate",accountIdQuery.get("openDate"));
				}
			}
			
			
		}catch (Exception e){
			e.printStackTrace();
			return error("01","请求失败","");
		}
		return succ("map",map);
	}
	//余额查询
	@ActionKey("/balanceQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message balanceQuery(){
		
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> balanceQuery =null;
		try{
			balanceQuery =  balanceQuery(accountId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(balanceQuery.get("retCode"))){
			return error(balanceQuery.get("retCode"),balanceQuery.get("retMsg"),null);
		}
		String accTypeValue = getAccTypeValue(balanceQuery.get("accType"));
		balanceQuery.put("acctType",accTypeValue);
		String acctUseValue = getAcctUseValue(balanceQuery.get("acctUse"));
		balanceQuery.put("acctUse",acctUseValue);
		if("0".equals(balanceQuery.get("withdrawFlag"))){
			balanceQuery.put("withdrawFlag","关闭");
		}else if("1".equals(balanceQuery.get("withdrawFlag"))){
			balanceQuery.put("withdrawFlag","打开");
		}

		return succ("查询成功", balanceQuery) ;
		
	}

	//电子账户各项冻结金额查询
	@ActionKey("/freezeAmtQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message freezeAmtQuery(){
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> freezeAmtQuery = null;
		try{
			freezeAmtQuery = freezeAmtQuery(accountId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(freezeAmtQuery.get("retCode"))){
			return error(freezeAmtQuery.get("retCode"),freezeAmtQuery.get("retMsg"),null);
		}

		return succ("查询成功", freezeAmtQuery);
	}

	//近两日的资金交易明细
	@SuppressWarnings("unchecked")
	@ActionKey("/accountDetailsQuery2")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message accountDetailsQuery2(){
		String nowDate = DateUtil.getNowDate();//获取现在时间
		String accountId = getPara("accountId","");
		String startDate = getPara("startDate",DateUtil.delDay(nowDate, 2));
		String endDate = getPara("endDate",nowDate);
		String type = getPara("type","");
		String tranType = getPara("tranType","");
		String rtnInd = getPara("rtnInd","");
		String inpDate = getPara("inpDate","");
		String inpTime = getPara("inpTime","");
		String relDate = getPara("relDate","");
		String traceNo = getPara("traceNo","");
		if(accountId.length()==11){
			 accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> accountDetailsQuery2 = null;
		try{
			accountDetailsQuery2 = accountDetailsQuery2(accountId, startDate, endDate, type, tranType, rtnInd, inpDate, inpTime, relDate, traceNo);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(accountDetailsQuery2.get("retCode").toString())){
			return error(accountDetailsQuery2.get("retCode").toString(),accountDetailsQuery2.get("retMsg").toString(),null);
		}else{
			
			Object object = accountDetailsQuery2.get("subPacks");
			ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) object;
			
			if(!list.isEmpty()){
				for(int i = 0;i<list.size();i++){
					Map<String, String> map = list.get(i);
					String orFlag = map.get("orFlag");
					String txFlagValue = getTxFlagValue(map.get("txFlag"));
					map.put("txFlag",txFlagValue);
					String orFlagValue = "";
					if("O".equals(orFlag)){
						orFlagValue = "原始交易";
					}else if("R".equals(orFlag)){
						orFlagValue = "已经冲正或者撤销";
					}
					String currency = map.get("currency");
					if("156".equals(currency)){
						map.put("currency","人民币");
					}
					map.put("orFlag",orFlagValue);
				}
			}
			return succ("查询成功", accountDetailsQuery2);
		}
	    
	}

	// 投资人债权明细查询
	@ActionKey("/creditDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message creditDetailsQuery(){
		String accountId = getPara("accountId","");
		String startDate = getPara("startDate","");
		String endDate = getPara("endDate","");
		String pageSize= getPara("pageSize","10");
		String pageNum = getPara("pageNum","1");
		String state = getPara("state","");
		String productId = getPara("productId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> creditDetailsQuery = null;
		try{
			creditDetailsQuery = creditDetailsQuery(accountId, productId, state, startDate, endDate, pageNum, pageSize);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(creditDetailsQuery.get("retCode").toString())){
			return error(creditDetailsQuery.get("retCode").toString(),creditDetailsQuery.get("retMsg").toString(),null);
		}
		ArrayList<Map<String,String>> list =(ArrayList<Map<String,String>>) creditDetailsQuery.get("subPacks");
		if(!list.isEmpty()){
			for(int i=0;i<list.size();i++){
				Map<String, String> map = list.get(i);
				String stateValue = getCreditStateValue(map.get("state"));
				String incFlagValue = getTxFlagValue(map.get("incFlag"));
				map.put("incFlag",incFlagValue);
				map.put("state",stateValue);
			}
		}
		return succ("查询成功", creditDetailsQuery);
	}

	//绑卡关系查询
	@ActionKey("/cardBindDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message cardBindDetailsQuery(){
		String state = getPara("state","");
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> cardBindDetailsQuery = null;
		try{
			cardBindDetailsQuery = cardBindDetailsQuery(accountId, state);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		if(!"00000000".equals(cardBindDetailsQuery.get("retCode").toString())){
			return error(cardBindDetailsQuery.get("retCode").toString(),cardBindDetailsQuery.get("retMsg").toString(),null);
		}
		return succ("查询成功", cardBindDetailsQuery);
	}

	//证件号查询电子账号
	@ActionKey("/accountIdQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message accountIdQuery(){
		String idNo = getPara("idNo","");
		String idType = getPara("idType","");
		Map<String, String> accountIdQuery = null;
		try{
			accountIdQuery = accountIdQuery(idType, idNo);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(accountIdQuery.get("retCode"))){
				return error(accountIdQuery.get("retCode"),accountIdQuery.get("retMsg"),null);
			}
		 String idType1 = accountIdQuery.get("idType");
		 String acctState = accountIdQuery.get("acctState");
		 String frzState = accountIdQuery.get("frzState");
		 String pinLosCd = accountIdQuery.get("pinLosCd");
		 if("01".equals(idType1)){
			 accountIdQuery.put("idType", "身份证（18位）");
		 }
		 
		 if("".equals(acctState)){
			 accountIdQuery.put("acctState", "正常");
		 }else if("A".equals(acctState)){
			 accountIdQuery.put("acctState","待激活");
		 }else if("Z".equals(acctState)){
			 accountIdQuery.put("acctState","注销");
		 }
		 
		 if("".equals(frzState)){
			 accountIdQuery.put("frzState","未冻结");
		 }else if("J".equals(frzState)){
			 accountIdQuery.put("frzState","司法冻结");
		 }
		 
		 if("".equals(pinLosCd)){
			 accountIdQuery.put("pinLosCd","未挂失");
		 }else if("Q".equals(pinLosCd)){
			 accountIdQuery.put("pinLosCd","已挂失");
		 }
		 return succ("查询成功", accountIdQuery);
	}

	//借款人标的信息查询
	@ActionKey("/debtDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message debtDetailsQuery(){
		String accountId = getPara("accountId","");
		String startDate = getPara("startDate","");
		String endDate = getPara("endDate","");
		String productId = getPara("productId","");
		String pageSize = getPara("pageSize","10");
		String pageNum = getPara("pageNum","1");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> debtDetailsQuery = null;
		try{
			debtDetailsQuery = debtDetailsQuery(accountId, productId, startDate, endDate, pageNum, pageSize);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(debtDetailsQuery.get("retCode").toString())){
			return error(debtDetailsQuery.get("retCode").toString(),debtDetailsQuery.get("retMsg").toString(),null);
		}
		
		Object object = debtDetailsQuery.get("subPacks");
		ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) object;
		if(!list.isEmpty()){
			for(int i = 0;i<list.size();i++){
				String intType = list.get(i).get("intType");
				String intTypeValue = "";
				switch (intType) {
				case "0":
					intTypeValue = "到期还本息";
					break;
				case "1":
					intTypeValue = "每月固定日期支付";
					break;
				case "2":
					intTypeValue = "每月不定日期支付";
					break;

				default:
					break;
				}
				list.get(i).put("intType",intTypeValue);
				
				String state = list.get(i).get("state");
				String stateValue = getCreditStateValue(state);
				
				list.get(i).put("state", stateValue);
			}
		}
		return succ("查询成功", debtDetailsQuery);
	}
	

	//电子账户手机号查询
	@ActionKey("/mobileMaintainace")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message mobileMaintainace(){
		String accountId = getPara("accountId","");
		String mobile = getPara("mobile","");
		String option = getPara("option","");
		Map<String, String> mobileMaintainace = null;
		try{
			mobileMaintainace = mobileMaintainace(accountId, option, mobile);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		if(!"00000000".equals(mobileMaintainace.get("retCode"))){
			return error("01",mobileMaintainace.get("retMsg"),null);
		}
		String idType = mobileMaintainace.get("idType");
		if("01".equals(idType)){
			mobileMaintainace.put("idType", "身份证（18位）");
		 }
		return succ("查询成功", mobileMaintainace);
	}

	//按手机号查询电子账户
	@ActionKey("/accountQueryByMobile")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message accountQueryByMobile(){
		String mobile = getPara("mobile","");
		Map<String, String> accountQueryByMobile =null;
		try{
			accountQueryByMobile = accountQueryByMobile(mobile);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 //如果查不到，考虑是不是开通存管的手机号和平台的手机号不一致
		 if(!"00000000".equals(accountQueryByMobile.get("retCode"))){
			 User user = userService.findByMobile(mobile);
			 if(user != null){
				 String reservedMobile = user.getStr("mobile");//江西存管预留的手机号，在t_banks_v2表中mobile字段  密文
				 if(StringUtil.isBlank(reservedMobile)||"000".equals(reservedMobile)){
					 return error("01", "尚未开通江西存管", null);
				 }
				 try{
					   String decryptUserMobile = CommonUtil.decryptUserMobile(reservedMobile);//解码手机号
						accountQueryByMobile = accountQueryByMobile(decryptUserMobile);
						if(!"00000000".equals(accountQueryByMobile.get("retCode"))){
							return error(accountQueryByMobile.get("retCode"),accountQueryByMobile.get("retMsg"),null);
						}
					}catch(Exception e){
						e.printStackTrace();
						return error("01", "请求失败", "");
					}
			 }else{
				 return error("01","无此用户","");
			 }
			 
			 /**
			  * 以下注释代码为当平台手机号和存管手机号不一致时，根据jxAccountId从存管查询手机号，如查出来，再根据查到的手机号查询电子账户
			  * 
			  * 由于查询较慢，暂时注释，如果根据t_banks_v2 查的预留手机号查电子账户有问题，转由一下查询
			  */
			 /*User user = userService.findByMobile(mobile);//通过手机号查询用户信息
			 try{
				 if(user != null){
					 String jxAccountId = user.getStr("jxAccountId");
					 if(StringUtil.isBlank(jxAccountId)){
						 return error("01","没有开通江西存管","");
					 }else{
						 Map<String, String> mobileMaintainace = mobileMaintainace(jxAccountId, "0", "");//通过电子账号查询手机号
						 if(!"00000000".equals(mobileMaintainace.get("retCode"))){
							 return error("01", "查询失败", "");
						 }
						 String mobileByJx = mobileMaintainace.get("mobile");//江西存管手机号
								accountQueryByMobile = accountQueryByMobile(mobileByJx);//再通过江西存管的手机号查询电子张号
								if(!"00000000".equals(accountQueryByMobile.get("retCode"))){
								   return error("01","查询失败",null);
								}
					 }
				 }else{
					 return error("01","无此用户",null);
				 }
			 }catch (Exception e){
				 e.printStackTrace();
					return error("01", "请求失败", "");
			 }*/
			}
		 String idType = accountQueryByMobile.get("idType");
		 String acctState = accountQueryByMobile.get("acctState");
		 String identity = accountQueryByMobile.get("identity");
		 String acctStateValue = "";
		 String identityValue = "";
			if("01".equals(idType)){
				accountQueryByMobile.put("idType", "身份证（18位）");
			 }
			switch (acctState) {
			case "":
				acctStateValue = "正常";
				break;
			case "A":
				acctStateValue = "待激活";
				break;
			case "C":
				acctStateValue = "止付";
				break;
			case "Z":
				acctStateValue ="注销";
				break;

			default:
				break;
			}
			switch (identity) {
			case "1":
				identityValue = "出借角色";
				break;
			case "2":
				identityValue = "借款角色";
				break;
			case "3":
				identityValue = "代偿角色";
				break;
			case "4":
				identityValue = "平台角色";
				break;

			default:
				break;
			}
			accountQueryByMobile.put("acctState",acctStateValue);
			accountQueryByMobile.put("identity",identityValue);
			
			return succ("查询成功", accountQueryByMobile);
	}

	//查询交易状态
	@ActionKey("/transactionStatusQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message transactionStatusQuery(){
		String accountId = getPara("accountId","");
		String reqType = getPara("reqType","2");
		String reqTxCode = getPara("reqTxCode","");
		/*String reqTxDate = getPara("reqTxDate","");
		String reqTxTime = getPara("reqTxTime","");
		String reqSeqNo = getPara("reqSeqNo","");*/
		String reqOrderId = getPara("reqOrderId","");
		String jxTraceCode = getPara("jxTraceCode","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		String reqTxDate = "";
		String reqTxTime = "";
		String reqSeqNo = "";
		//如果交易流水号不为空，进行拆分
		if(!StringUtil.isBlank(jxTraceCode) && jxTraceCode.length() == 20){
			reqTxDate = jxTraceCode.substring(0,8);
			reqTxTime = jxTraceCode.substring(8,14);
			reqSeqNo = jxTraceCode.substring(14,20);
		}
		Map<String, String> transactionStatusQuery = null;
		try{
			transactionStatusQuery = transactionStatusQuery(accountId, reqType, reqTxCode, reqTxDate, reqTxTime, reqSeqNo, reqOrderId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(transactionStatusQuery.get("retCode"))){
				return error(transactionStatusQuery.get("retCode"),transactionStatusQuery.get("retMsg"),null);
			}
		 String reqTypeRe = transactionStatusQuery.get("reqType");
		 if("1".equals(reqTypeRe)){
			 transactionStatusQuery.put("reqType", "流水号查询");
		 }else if("2".equals(reqTypeRe)){
			 transactionStatusQuery.put("reqType","订单号查询");
		 }
		 String txState = transactionStatusQuery.get("txState");
		 String txStateValue = "";
		 switch (txState) {
		case "S":
			txStateValue = "成功";
			break;
		case "F":
			txStateValue = "失败";
			break;
		case "N":
			txStateValue = "交易不存在";
			break;
		case "Z":
			txStateValue = "未知";
			break;
		case "D":
			txStateValue = "待处理";
			break;

		default:
			break;
		}
		 transactionStatusQuery.put("txState",txStateValue);
			return succ("查询成功", transactionStatusQuery);
	}

	//查询批次状态
	@ActionKey("/batchQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message batchQuery(){
		String batchTxDate = getPara("batchTxDate","");
		String batchNo = getPara("batchNo","");
		Map<String, String> batchQuery = null;
		try{
			batchQuery = batchQuery(batchTxDate, batchNo);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(batchQuery.get("retCode"))){
				return error(batchQuery.get("retCode"),batchQuery.get("retMsg"),null);
			}
		 String batchState = batchQuery.get("batchState");
		 String batchStateValue = getStateValue(batchState);
		 batchQuery.put("batchState", batchStateValue);
			return succ("查询成功", batchQuery);
	}
	
	

	//查询批次交易明细状态
	@ActionKey("/batchDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message batchDetailsQuery(){
		String batchTxDate = getPara("batchTxDate","");
		String batchNo = getPara("batchNo","");
		String type = getPara("type","");
		String pageNum = getPara("pageNum","1");
		String pageSize = getPara("pageSize","10");
		Map<String, Object> batchDetailsQuery = null;
		try{
			batchDetailsQuery = batchDetailsQuery(batchTxDate, batchNo, type, pageNum, pageSize);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(batchDetailsQuery.get("retCode").toString())){
				return error(batchDetailsQuery.get("retCode").toString(),batchDetailsQuery.get("retMsg").toString(),null);
			}
		 Object object = batchDetailsQuery.get("subPacks");
			ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) object;
			if(!list.isEmpty()){
				for(int i = 0;i<list.size();i++){
					String txState = list.get(i).get("txState");
					String txStateValue = getStateValue(txState);
					 list.get(i).put("txState",txStateValue);
				}
			}		 
			return succ("查询成功", batchDetailsQuery);
	}
	

	//投资人购买债权查询 
	@ActionKey("/creditInvestQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message creditInvestQuery(){
		String accountId =getPara("accountId","");
		String orgOrderId = getPara("orgOrderId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> creditInvestQuery = null;
		try{
			creditInvestQuery = creditInvestQuery(accountId, orgOrderId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(creditInvestQuery.get("retCode"))){
				return error(creditInvestQuery.get("retCode"),creditInvestQuery.get("retMsg"),null);
			}
			return succ("查询成功", creditInvestQuery);
	}

	//投资人投标申请查询
	@ActionKey("/bidApplyQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message bidApplyQuery(){
		String accountId = getPara("accountId","");
		String orOrderId = getPara("orOrderId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> bidApplyQuery = null;
		try{
			bidApplyQuery = bidApplyQuery(accountId, orOrderId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(bidApplyQuery.get("retCode"))){
				return error(bidApplyQuery.get("retCode"),bidApplyQuery.get("retMsg"),null);
			}
		 String state = bidApplyQuery.get("state");
		 String stateValue = "";
		 switch (state) {
		case "1":
			stateValue = "投标中";
			break;
		case "2":
			stateValue = "计息中";
			break;
		case "4":
			stateValue = "本息已返还";
			break;
		case "9":
			stateValue = "已撤销";
			break;

		default:
			break;
		}
		 bidApplyQuery.put("state",stateValue);
			return succ("查询成功", bidApplyQuery);
	}

	//投资人签约状态查询
	@ActionKey("/creditAuthQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message creditAuthQuery(){
		String accountId = getPara("accountId","");
		String type = getPara("type","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> creditAuthQuery = null;
		try{
			creditAuthQuery = termsAuthQuery(accountId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(creditAuthQuery.get("retCode"))){
				return error(creditAuthQuery.get("retCode"),creditAuthQuery.get("retMsg"),null);
			}
		 String autoBid = creditAuthQuery.get("autoBid");
		 String stateValue = "";
		 if("0".equals(autoBid)){
			 stateValue = "未签约";
		 }else if("1".equals(autoBid)){
			 stateValue = "已签约";
		 }
		 creditAuthQuery.put("state", stateValue);
		 creditAuthQuery.put("type", type);
			return succ("查询成功", creditAuthQuery);
	}

	//企业账户查询
	@ActionKey("/corprationQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message corprationQuery(){
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> corprationQuery = null;
		try{
			corprationQuery = corprationQuery(accountId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(corprationQuery.get("retCode"))){
				return error(corprationQuery.get("retCode"),corprationQuery.get("retMsg"),null);
			}
			return succ("查询成功", corprationQuery);
	}

	//账户资金冻结明细查询
	@ActionKey("/frzDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message frzDetailsQuery(){
		//6212462580000000023   75f42acbf7ed4dc695f77dda6ce48130
		String accountId = getPara("accountId","");
		String state = getPara("state","");
		String productId = getPara("productId","");
		String rtnInd = getPara("rtnInd","");
		String frzDate = getPara("frzDate","");
		String lastOrderId = getPara("lastOrderId","");
		String fuissuerPage = getPara("fuissuerPage","");
		String productIdPage = getPara("productIdPage","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> frzDetailsQuery = null;
		try{
			frzDetailsQuery = frzDetailsQuery(accountId, state, productId, rtnInd, frzDate, lastOrderId, fuissuerPage, productIdPage);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(frzDetailsQuery.get("retCode").toString())){
				return error(frzDetailsQuery.get("retCode").toString(),frzDetailsQuery.get("retMsg").toString(),null);
			}
		 ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>)frzDetailsQuery.get("subPacks");
		 if(!list.isEmpty()){
			 for(int i = 0;i<list.size();i++){
				 String subState = list.get(i).get("state");
				 String stateValue = "";
				 if("0".equals(subState)){
					 stateValue = "正常";
				 }else if("1".equals(subState)){
					 stateValue = "已撤销";
				 }
				 list.get(i).put("state", stateValue);
			 }
		 }
			return succ("查询成功", frzDetailsQuery);
	}

	//电子账户密码是否设置查询
	@ActionKey("/passwordSetQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message passwordSetQuery(){
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> passwordSetQuery = null;
		try{
			passwordSetQuery = passwordSetQuery(accountId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		
		 if(!"00000000".equals(passwordSetQuery.get("retCode"))){
				return error(passwordSetQuery.get("retCode"),passwordSetQuery.get("retMsg"),null);
			}
		 String pinFlag = passwordSetQuery.get("pinFlag");
		 String pinFlagValue = "";
		 if("0".equals(pinFlag)){
			 pinFlagValue = "无密码";
		 }else if("1".equals(pinFlag)){
			 pinFlagValue = "有密码";
		 }
		 passwordSetQuery.put("pinFlag", pinFlagValue);
		 
			return succ("查询成功", passwordSetQuery);
	}

	//单笔还款申请冻结查询
	@ActionKey("/balanceFreezeQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message balanceFreezeQuery(){
		String accountId = getPara("accountId","");
		String orgOrderId = getPara("orgOrderId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> balanceFreezeQuery = null;
		try{
			balanceFreezeQuery = balanceFreezeQuery(accountId, orgOrderId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(balanceFreezeQuery.get("retCode"))){
				return error(balanceFreezeQuery.get("retCode"),balanceFreezeQuery.get("retMsg"),null);
			}
		 String subState = balanceFreezeQuery.get("state");
		 String stateValue = "";
		 if("0".equals(subState)){
			 stateValue = "正常";
		 }else if("1".equals(subState)){
			 stateValue = "已撤销";
		 }
		 balanceFreezeQuery.put("state", stateValue);
			return succ("查询成功", balanceFreezeQuery);
	}

	//查询批次发红包交易明细
	@ActionKey("/batchVoucherDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message batchVoucherDetailsQuery(){
		String batchTxDate = getPara("batchTxDate","");
		String batchNo = getPara("batchNo","");
		String type = getPara("type","");
		String pageNum = getPara("pageNum","");
		String pageSize = getPara("pageSize","");
		Map<String, Object> batchVoucherDetailsQuery = null;
		try{
			batchVoucherDetailsQuery = batchVoucherDetailsQuery(batchTxDate, batchNo, type, pageNum, pageSize);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(batchVoucherDetailsQuery.get("retCode").toString())){
				return error("01",batchVoucherDetailsQuery.get("retMsg").toString(),null);
			}
		 ArrayList<Map<String,String>> list =(ArrayList<Map<String,String>>) batchVoucherDetailsQuery.get("subPacks");
		 if(!list.isEmpty()){
			 for(int i = 0;i<list.size();i++){
				 Map<String, String> map = list.get(i);
				 String txState = map.get("txState");
				 String voucherType = map.get("voucherType");
				 String voucherTypeValue = "";
				 if("001".equals(voucherType)){
					 voucherTypeValue = "红包发放";
				 }else if("002".equals(voucherType)){
					 voucherTypeValue="加息券收益发放";
				 }
				 map.put("txState",getStateValue(txState));
				 map.put("vocherType",voucherTypeValue);
			 }
		 }
			return succ("查询成功", batchVoucherDetailsQuery);
		 
	}
	
	//单笔资金类业务交易查询
		@ActionKey("/fundTransQuery")
		@Before({AuthInterceptor.class,PkMsgInterceptor.class})
		@AuthNum(value = 999)
		public Message fundTransQuery(){
			String accountId = getPara("accountId","");
			String jxTraceCode = getPara("jxTraceCode","");
			String orgTxDate = "";
			String orgTxTime = "";
			String orgSeqNo = "";
			if(!StringUtil.isBlank(jxTraceCode)&&jxTraceCode.length() == 20){
				orgTxDate = jxTraceCode.substring(0,8);
				orgTxTime = jxTraceCode.substring(8,14);
				orgSeqNo = jxTraceCode.substring(14,20);
			}else{
				return error("01", "交易流水号为20位", null);
			}
			if(accountId.length()==11){
				accountId = accountIdByMobile(accountId);
			}
			Map<String, String> fundTransQuery = null;
			try{
				fundTransQuery = fundTransQuery(accountId, orgTxDate, orgTxTime, orgSeqNo);
			}catch(Exception e){
				e.printStackTrace();
				return error("01", "请求失败", "");
			}
			 
			 if(!"00000000".equals(fundTransQuery.get("retCode"))){
					return error("01",fundTransQuery.get("retMsg"),null);
				}
			 JXTrace queryJXTraceByJxTraceCode = jxTraceService.queryJXTraceByJxTraceCode(jxTraceCode);
			 String txCodeTrans = "";
			 if(queryJXTraceByJxTraceCode != null){
				 txCodeTrans = queryJXTraceByJxTraceCode.getStr("txCode");
			 }
			 String resultValue = "";
			 String orFlagValue = "";
			 String orFlag = fundTransQuery.get("orFlag");
			 String result = fundTransQuery.get("result");
			 if("00".equals(result)){
				 resultValue = "成功";
			 }else{
				 resultValue="无该交易或者处理失败";
			 }
			 if("0".equals(orFlag)){
				 orFlagValue = "正常";
			 }else if("1".equals(orFlag)){
				 orFlagValue="已冲正/撤销";
			 }
			 fundTransQuery.put("orFlag",orFlagValue);
			 fundTransQuery.put("result",resultValue);
			 fundTransQuery.put("txCodeTrans",txCodeTrans);
				return succ("查询成功", fundTransQuery);
		}

	/**
	 * 单笔资金类业务交易查询
	 * 列表
	 * @return
	 */
	@ActionKey("/fundTransQueryForList")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message fundTransQuery1(){
		String accountId = getPara("accountId","");
		String jxTraceCode = getPara("jxTraceCode","");
		int pageNumber = getParaToInt("pageNum",1);
		int pageSize = getParaToInt("pageSize",10);
		String txCode = getPara("txCode","");
		String state = getPara("state","");//00000000：成功  2：失败  3：无响应
		String orgTxDate = "";
		String orgTxTime = "";
		String orgSeqNo = "";
		String startDate = DateUtil.delDay(DateUtil.getNowDate(), 2);//开始日期
		String endDate = DateUtil.getNowDate();//结束日期
		String startTime = DateUtil.getNowTime();//开始时间
		String endTime = "235959";//结束时间
		if(!StringUtil.isBlank(jxTraceCode)&&jxTraceCode.length() == 20){
			orgTxDate = jxTraceCode.substring(0,8);
			orgTxTime = jxTraceCode.substring(8,14);
			orgSeqNo = jxTraceCode.substring(14,20);
		}
	
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> map = new TreeMap<>();
		List<Map<String, String>> fundTransQuery = new ArrayList<Map<String,String>>();
		if(StringUtil.isBlank(orgTxDate)||StringUtil.isBlank(orgTxTime)||StringUtil.isBlank(orgSeqNo)){
			if(!StringUtil.isBlank(accountId)){
				 Page<JXTrace> jxTraceCodePage = jxTraceService.queryJxTraceCodeByAccountId(startDate+startTime,endDate+endTime,txCode,state,pageNumber,pageSize,accountId);
				List<JXTrace> jxTraceCodeList = jxTraceCodePage.getList();
				if(!jxTraceCodeList.isEmpty()){
					for(int i = 0;i<jxTraceCodeList.size();i++){
						String traceCode = jxTraceCodeList.get(i).getStr("jxTraceCode");
						String txCodeTrans = jxTraceCodeList.get(i).getStr("txCode");
					    orgTxDate = traceCode.substring(0,8);
						orgTxTime = traceCode.substring(8,14);
						orgSeqNo = traceCode.substring(14,20);
						Map<String, String> fundTransQuery2 = null;
						try{
							fundTransQuery2 = fundTransQuery(accountId, orgTxDate, orgTxTime, orgSeqNo);
						}catch(Exception e){
							e.printStackTrace();
							return error("01", "请求失败", "");
						}
						
						fundTransQuery2.put("txCodeTrans", txCodeTrans);
						fundTransQuery2.put("jxTraceCodeTrans",traceCode);
						fundTransQuery.add(fundTransQuery2);
						
						map.put("pageNum",jxTraceCodePage.getPageNumber());
						map.put("pageSize",jxTraceCodePage.getPageSize());
						map.put("totalItems",jxTraceCodePage.getTotalRow());
					}
				}
			}else{
				return error("01", "电子账户不能为空", null);
			}
		}else{
			Map<String, String> fundTransQuery3 = null;
			try{
				fundTransQuery3 = fundTransQuery(accountId, orgTxDate, orgTxTime, orgSeqNo);
			}catch(Exception e){
				e.printStackTrace();
				return error("01", "请求失败", "");
			}
			
			JXTrace queryJXTraceByJxTraceCode = jxTraceService.queryJXTraceByJxTraceCode(jxTraceCode);
			 String txCodeTrans = "";
			 if(queryJXTraceByJxTraceCode != null){
				 String requestMessage = queryJXTraceByJxTraceCode.getStr("requestMessage");
				 JSONObject jsonObject = JSONObject.parseObject(requestMessage);
				 txCodeTrans = jsonObject.get("txCode").toString();
			 }
			 fundTransQuery3.put("txCodeTrans", txCodeTrans);
			 fundTransQuery3.put("jxTraceCodeTrans",jxTraceCode);
			 fundTransQuery.add(fundTransQuery3);
			 map.put("pageNum",1);
			 map.put("pageSize",1);
			 map.put("totalItems",1);
			 
		}
		if(!fundTransQuery.isEmpty()){
			for(int i = 0;i<fundTransQuery.size();i++){
				String resultValue = "";
				 String orFlagValue = "";
				 String orFlag = fundTransQuery.get(i).get("orFlag");
				 String result = fundTransQuery.get(i).get("result");
				 if("00".equals(result)){
					 resultValue = "成功";
				 }else{
					 resultValue="无该交易或者处理失败";
				 }
				 if("0".equals(orFlag)){
					 orFlagValue = "正常";
				 }else if("1".equals(orFlag)){
					 orFlagValue="已冲正/撤销";
				 }
				 fundTransQuery.get(i).put("orFlag",orFlagValue);
				 fundTransQuery.get(i).put("result",resultValue);
			}
		}
		map.put("list", fundTransQuery);
		 
			return succ("查询成功", map);
	}

	//债权转让明细查询
	@ActionKey("/creditInvesDetailsQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message creditInvesDetailsQuery(){
		String accountId = getPara("accountId","");
		String orgOrderId = getPara("orgOrderId","");
		String forAccountId = getPara("forAccountId","");
		String turnPage = getPara("turnPage","");
		String orderId = getPara("orderId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> creditInvesDetailsQuery = null;
		try{
			creditInvesDetailsQuery = creditInvesDetailsQuery(accountId, orgOrderId, forAccountId, turnPage, orderId);
		}catch(Exception e){
			e.printStackTrace();
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(creditInvesDetailsQuery.get("retCode").toString())){
				return error("01",creditInvesDetailsQuery.get("retMsg").toString(),null);
			}
			return succ("查询成功", creditInvesDetailsQuery);
	}

	//客户授权功能查询
	@ActionKey("/termsAuthQuery")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message termsAuthQuery(){
		String accountId = getPara("accountId","");
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		Map<String, String> termsAuthQuery =new TreeMap<>();
		try{
			termsAuthQuery = termsAuthQuery(accountId);
		}catch(Exception e){
			return error("01", "请求失败", "");
		}
		 
		 if(!"00000000".equals(termsAuthQuery.get("retCode"))){
				return error("01",termsAuthQuery.get("retMsg"),null);
			}
		 termsAuthQuery.put("autoBid", getTermsAuthValue(termsAuthQuery.get("autoBid")));
		 termsAuthQuery.put("autoTransfer", getTermsAuthValue(termsAuthQuery.get("autoTransfer")));
		 termsAuthQuery.put("agreeWithdraw", getTermsAuthValue(termsAuthQuery.get("agreeWithdraw")));
		 termsAuthQuery.put("agreeDeduct", getTermsAuthValue(termsAuthQuery.get("agreeDeduct")));
		 termsAuthQuery.put("paymentAuth", getTermsAuthValue(termsAuthQuery.get("paymentAuth")));
		 termsAuthQuery.put("repayAuth", getTermsAuthValue(termsAuthQuery.get("repayAuth")));
		 
			return succ("查询成功", termsAuthQuery);
	}
	
	/**
	 * jx流水查询（t_jx_trace）
	 * @param termsAuth
	 * @return
	 */
	
	@ActionKey("/queryJxTrace")
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	@AuthNum(value = 999)
	public Message adminJxTrace(){
		int pageNumber =getParaToInt("pageNum",1);
		int pageSize = getParaToInt("pageSize",10);
		String txCode = getPara("txCode","");
		String accountId = getPara("accountId","");
		String startTxDate = getPara("startTxDate","");
		String endTxDate = getPara("endTxDate","");
		String jxTraceCode = getPara("jxTraceCode","");
		String retCode = getPara("retCode","");
		String state = getPara("state","");
		String productId = getPara("productId","");
		String orderId = getPara("orderId","");
		String orgOrderId = getPara("orgOrderId","");
		Map<String,Object> map = new HashMap<String,Object>();
		if(accountId.length()==11){
			accountId = accountIdByMobile(accountId);
		}
		List<Map<String,String>> lists = new ArrayList<Map<String,String>>();
		Page<JXTrace> jxTracePage = jxTraceService.queryTracePage(pageNumber,pageSize,productId,orderId,orgOrderId,txCode,accountId,startTxDate,endTxDate,jxTraceCode,retCode,state);
		List<JXTrace> list = jxTracePage.getList();
		for(int i =0;i<list.size();i++){
			JXTrace jxTrace = list.get(i);
			String requestChannel = jxTrace.getStr("requestChannel");
			String requestChannelValue = "";
			if(StringUtil.isBlank(requestChannel)){
				requestChannelValue="无请求";
			}else{
				switch (requestChannel) {
				case "000001":
					requestChannelValue = "手机";
					break;
				case "000002":
					requestChannelValue = "网页";
					break;
				case "000003":
					requestChannelValue = "微信";
					break;
				case "000004":
					requestChannelValue = "柜台";
					break;

				default:
					break;
				}
			}
			
			jxTrace.set("requestChannel", requestChannelValue);
			Map<String, String> maps = new HashMap<String,String>();
			String json = Json.getJson().toJson(jxTrace);
			JSONObject jsonObject = JSONObject.parseObject(json);
			Set<String> keySet = jsonObject.keySet();
			for(String key: keySet){
				maps.put(key,jsonObject.getString(key));
			}
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			if(!StringUtil.isBlank(requestMessage)){
				if(!parseObject.containsKey("name")){
					if(parseObject.containsKey("accountId")){
						String accountIdRequest = parseObject.get("accountId").toString();
						User user = userService.findByJXAccountId(accountIdRequest);
						if(user != null){
							String trueName = userService.findUserAllInfoById(user.getStr("userCode")).getStr("userCardName");//获取真实姓名
							maps.put("name",trueName);
						}
						
					}
				}
			}
			lists.add(maps);
		}
		map.put("list",lists);
		map.put("pageNum",jxTracePage.getPageNumber());
		map.put("pageSize",jxTracePage.getPageSize());
		map.put("totalItems",jxTracePage.getTotalRow());
		return succ("查询成功", map);
	}
	
	/**
	 * aleve文件解析显示
	 * @param termsAuth
	 * @return
	 */
	@ActionKey("/queryAleveFromDownFile")
	@Before({PkMsgInterceptor.class})
	public Message queryAleveFromDownFile(){
		String bankSerialNo = Property.getPropertyValueByKey("jx", "bankSerialNo");//银行编号
		String productSerialNo = Property.getPropertyValueByKey("jx", "productSerialNo");//产品编号
		String startDate  =  getPara("startDate","");//开始时间日期
		String endDate  =  getPara("endDate","");//开始时间日期
		String transType = getPara("transType","");//交易类型
		String cardnbr = getPara("cardnbr","");//电子账号
		String forCardnbr = getPara("forCardnbr","");//对方电子账号
		String revind = getPara("revind","2");//冲正标识
		int pageSize = getParaToInt("pageSize",10);
		int pageNum = getParaToInt("pageNum",1);
		if(!StringUtil.isBlank(startDate)&&!StringUtil.isBlank(endDate)){
			if(DateUtil.compareDateByStr("yyyyMMdd", startDate, endDate)>0){
				return error("01","开始时间不能晚于结束时间或时间格式错误",null);
			}
		}else{
			return error("01","查询时间不能为空",null);
		}
		
		List<String> dateSplit = null;
		
		try{
			dateSplit = DateUtil.dateSplit(startDate, endDate);//获取每天日期
		}catch(Exception e){
			e.printStackTrace();
			return error("01","时间解析错误",null);
		}
		List<Map<String,String>> list = new  ArrayList<Map<String,String>>();//用来存一条一条数据
		List<Map<String, String>> listSelect = new ArrayList<Map<String,String>>();//用来存放分页的结果数据
		Map<String,Object> maps = new HashMap<String,Object>();
		List<String> lists = new ArrayList<String>();

		for(int i = dateSplit.size()-1;i>=0;i--){//遍历时间，符合要求的存到list中
			String dateTime = dateSplit.get(i);
			String dateYear = dateTime.substring(0,4);
			String dateMonth = dateTime.substring(4,6);
			String dateDay = dateTime.substring(6,8);
			
//			String path = "D:\\download\\" + dateYear+"\\"+dateMonth+"\\" + dateDay;//文件目录
			String path = "//data" + File.separator + "jx_trace_file" + File.separator + dateYear + File.separator + dateMonth+ File.separator + dateDay;//文件目录
			String fileName = bankSerialNo + "-ALEVE" + productSerialNo + "-" + dateTime;//文件名
			
			try{
				lists = FtpUtil.writeToDat(path + File.separator + fileName);//读取文件
			}catch (Exception e){
				continue;
			}
		
		if(!lists.isEmpty()){
			for(int j= 0;j<lists.size();j++){
				
				Map<String,String> map = new HashMap<String,String>();
				byte[] bytes = null;
				try{
					bytes = lists.get(j).getBytes("GBK");
					String inpDate1 = FtpUtil.byteArrayToString(bytes, 52, 8);//交易时间
					String transType1 = FtpUtil.byteArrayToString(bytes, 88, 4);//交易类型
					String cardnbr1 = FtpUtil.byteArrayToString(bytes, 4, 19);//电子账号
					String forCardnbr1 = FtpUtil.byteArrayToString(bytes, 151, 19).trim();//对方的电子账号
					String revind1 = FtpUtil.byteArrayToString(bytes,170,1);//冲正标识
					if(!StringUtil.isBlank(dateTime)&&!dateTime.equals(inpDate1)){//选择时间
						continue;
					}if(!StringUtil.isBlank(transType)&&!transType.equals(transType1)){//选择交易类型
						continue;
					}
				     if(!StringUtil.isBlank(cardnbr)&&!cardnbr.equals(cardnbr1)){//选择电子账号
					    continue;
				    }
				     if(!StringUtil.isBlank(forCardnbr)&&!forCardnbr.equals(forCardnbr1)){//选择对方交易账户
				    	 continue;
				     }
				     if(!"2".equals(revind)){//选择冲正标识
				    	 if(StringUtil.isBlank(revind1)){
				    		 revind1 = "0";
				    	 }
				    	 if(!revind.equals(revind1)){
					    	 continue;
					     }
				     }
				     
					map.put("bank", FtpUtil.byteArrayToString(bytes, 0, 4));//银行卡
					map.put("cardnbr",cardnbr1);//电子账号
					String amount = FtpUtil.byteArrayToString(bytes, 23, 17);//交易金额 两位小数
					Long parseAmount = Long.parseLong(amount);//单位为分
					map.put("amount",parseAmount.toString());
					map.put("cur_num","156".equals(FtpUtil.byteArrayToString(bytes, 40, 3))?"人民币":FtpUtil.byteArrayToString(bytes, 40, 3));//货币种类
					String crflag = FtpUtil.byteArrayToString(bytes, 43, 1);//金额符号 C小于0  D大于0
					String crflagValue = null;
					if("C".equals(crflag)){
						crflagValue = "支出";
					}else if("D".equals(crflag)){
						crflagValue = "收入";
					}else{
						crflagValue = crflag;
					}
					map.put("crflag",crflagValue);
					
					map.put("valDate",FtpUtil.byteArrayToString(bytes, 44, 8));//入账时间
					
					map.put("inpDate",inpDate1);//交易时间
					map.put("relDate",FtpUtil.byteArrayToString(bytes, 60, 8));//自然日期
					map.put("inpTime",FtpUtil.byteArrayToString(bytes, 68, 8));//交易时间
					map.put("tranNo",FtpUtil.byteArrayToString(bytes, 76, 6));//交易流水号
					map.put("ori_tranNo",FtpUtil.byteArrayToString(bytes, 82, 6));//关联流水号
					map.put("transType", transType1);//交易类型
					map.put("desLine",FtpUtil.byteArrayToString(bytes, 92, 42).trim());//交易描述
					String avbAmount = FtpUtil.byteArrayToString(bytes, 134, 17);//交易后剩余金额 两位小数
					Long parseAvbAmount = Long.parseLong(avbAmount);//单位为分
					map.put("curr_bal",parseAvbAmount.toString());
					map.put("forCardnbr",forCardnbr1);//对方交易账号
					String revindValue = null;
					if("1".equals(revind1)){
						revindValue = "已撤销/冲正";
					}else{
						revindValue = "正常";
					}
					map.put("revind",revindValue);
					map.put("accchg","1".equals(FtpUtil.byteArrayToString(bytes, 171, 1))?"调账":FtpUtil.byteArrayToString(bytes, 171, 1));//交易标识
					map.put("seqNo",FtpUtil.byteArrayToString(bytes,172,6));//系统跟踪号
					map.put("ori_num",FtpUtil.byteArrayToString(bytes,178,6));//原交易流水号
					map.put("resv",FtpUtil.byteArrayToString(bytes,184,187).trim());//保留域
					list.add(map);//将一条map数据存到list
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}else{
			continue;
		}
	}
		int listLength = list.isEmpty()?0:list.size();//数据的总条数
		if(pageNum<1){
			pageNum = 1;
		}
		
		int number = (pageNum-1) * pageSize;//从第几条数据开始存
		int currentNum = listLength-number;//当前页的数据量
		int cutNum = currentNum>=pageSize?pageSize:currentNum;//如果当前页的数据量大于pageSize,截取pageSize长度，则截取cutNum长度
		
		 listSelect = list.subList(number,number+cutNum);//截取list数据
		maps.put("list", listSelect);
		maps.put("totalItems", listLength);
		maps.put("pageNum",pageNum);
		maps.put("pageSize",pageSize);
		
		return succ("succ", maps);
	}
	
	/*
	 * 授权功能是否开通
	 */
	public String getTermsAuthValue(String termsAuth){
		String termsAuthValue = "";
		switch (termsAuth) {
		case "0":
			termsAuthValue = "未开通";
			break;
		case "1":
			termsAuthValue = "已开通";
			break;

		default:
			break;
		}
		return termsAuthValue;
	}
	/*
	 * 处理状态
	 */
	public String getStateValue(String state){
		String stateValue = "";
		switch (state) {
		case "A":
			stateValue = "待处理";
			break;
		case "D":
			stateValue = "处理中";
			break;
		case "S":
			stateValue = "处理结束";
			break;
		case "F":
			stateValue = "处理失败";
			break;
		case "C":
			stateValue = "已撤销";
			break;

		default:
			break;
		}
		
		return stateValue;
	}
	
	/*
	 * 债权状态
	 */
	public String getCreditStateValue(String state){
		String stateValue = "";
		switch (state) {
		case "1":
			stateValue = "投标中";
			break;
		case "2":
			stateValue = "计息中";
			break;
		case "3":
			stateValue = "到期待返还";
			break;
		case "4":
			stateValue = "本息已返还";
			break;
		case "8":
			stateValue = "审核中";
			break;
		case "9":
			stateValue = "已撤销";
			break;

		default:
			stateValue="查询失败";
			break;
		}
		return stateValue;
		
	}
	/**
	 * 交易符号转成收入支出
	 */
	public String getTxFlagValue(String txFlag){
		String txFlagValue="";
		switch (txFlag) {
		case "+":
			txFlagValue = "收入";
			break;
		case "-":
			txFlagValue = "支出";
			break;

		default:
			break;
		}
		return txFlagValue;
	}
	/**
	 * 获取账户类型
	 * @param accType
	 * @return
	 */
	public String getAccTypeValue(String accType){
		String accTypeValue = "";
		switch (accType) {
		case "0":
			accTypeValue = "基金账户";
			break;
		case "1":
			accTypeValue = "靠档计息账户";
			break;
		case "2":
			accTypeValue = "活期账户";
			break;

		default:
			break;
		}
		return accTypeValue;
	}
	/**
	 * 账户用途
	 * @param acctUse
	 * @return
	 */
	public String getAcctUseValue(String acctUse){
		String acctUseValue = "";
		switch (acctUse) {
		case "00000":
			acctUseValue = "普通账户";
			break;
		case "10000":
			acctUseValue = "红包账户";
			break;
		case "01000":
			acctUseValue = "手续费账户";
			break;
		case "00100":
			acctUseValue = "担保账户";
			break;

		default:
			break;
		}
		return acctUseValue;
	}
	
	/**
	 * 红包发放流水查询 WJW
	 * @return
	 */
	@ActionKey("/queryVoucherPaysByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryVoucherPaysByPage(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		String allkey = getPara("allkey","");
		
		if(allkey.length() == 11){//可能为手机号
			User user = userService.findByMobile(allkey);
			String jxAccountId = user.getStr("jxAccountId");
			if(!StringUtil.isBlank(jxAccountId)){
				allkey = jxAccountId;
			}
		}
		
		Page<JXTrace> page = jxTraceService.queryVoucherPaysByPage(pageNumber, pageSize, allkey);
		List<JXTrace> jxTraces = page.getList();
		List<JSONObject> list = new ArrayList<JSONObject>();
		for (JXTrace jxTrace : jxTraces) {
			JSONObject jsonObject = new JSONObject();
			String requestMessage = jxTrace.getStr("requestMessage");
			if(!StringUtil.isBlank(requestMessage)){
				com.alibaba.fastjson.JSONObject parseObject = com.alibaba.fastjson.JSONObject.parseObject(requestMessage);
				String forAccountId = parseObject.getString("forAccountId");
				String txAmount = parseObject.getString("txAmount");
				jsonObject.put("jxTraceCode", jxTrace.getStr("jxTraceCode"));
				jsonObject.put("forAccountId", forAccountId);
				String desLine = parseObject.getString("desLine");//红包描述
				if(!StringUtil.isBlank(desLine)){
					int start = desLine.indexOf("[");
					int end = desLine.indexOf("]");
					if(start != -1 && end != -1){//存在traceCode
						String remark = "";//备注
						String traceCode = desLine.substring(start+1, end);
						LoanTrace loanTrace = loanTraceService.findById(traceCode);
						if(loanTrace == null){
							continue;
						}
//						int loanRecyCount = loanTrace.getInt("loanRecyCount");
//						int loanTimeLimit = loanTrace.getInt("loanTimeLimit");
//						int recyPeriod = loanTimeLimit - loanRecyCount;
						String loanNo = loanTrace.getStr("loanNo");
//						String loanState = loanTrace.getStr("loanState");
//						if("P".equals(loanState)){
//							remark = "标["+loanNo+"]第"+recyPeriod+"/"+loanTimeLimit+"期提前还款";
//						}else {
//							remark = "标["+loanNo+"]第"+recyPeriod+"/"+loanTimeLimit+"期还款";
//						}
						remark = "标["+loanNo+"]还款";
						if(desLine.indexOf("本金") != -1){
							jsonObject.put("remark", remark+"本金");
						}else if(desLine.indexOf("利息") != -1){
							jsonObject.put("remark", remark+"利息");
						}else if(desLine.indexOf("返佣") != -1){
							jsonObject.put("remark", remark+"好友返佣");
						}else if(desLine.indexOf("现金券") != -1){
							jsonObject.put("remark", "投标["+loanNo+"]现金券抵扣");
						}
					}
				}
				User user = userService.findByJXAccountId(forAccountId);
				if(user != null){
					jsonObject.put("userName", user.getStr("userName"));
					String decryptUserMobile = "";
					try {
						decryptUserMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					jsonObject.put("userMobile", decryptUserMobile);
				}
				jsonObject.put("txAmount", txAmount);
				jsonObject.put("retMsg", "00000000".equals(jxTrace.getStr("retCode"))?"发送成功":jxTrace.getStr("retMsg"));
				if(jxTraceService.voucherPayCancel(jxTrace.getStr("txDate"), jxTrace.getStr("txTime"), jxTrace.getStr("seqNo"))){
					jsonObject.put("retMsg", "已撤销");
				}
				list.add(jsonObject);
			}
		}
		return succ("红包发放流水查询完成", new Page<>(list, pageNumber, pageSize, page.getTotalPage(), page.getTotalRow()));
	}
	
	/**
	 * 导出最近两天的资金流水
	 */
	@ActionKey("/exportFileFor2day")
	@Before({PkMsgInterceptor.class})
	public void exportFileFor2day(){
		
		String nowDate = DateUtil.getNowDate();//获取现在时间
		String accountId = getPara("accountId","");
		String startDate = getPara("startDate",DateUtil.delDay(nowDate, 2));
		String endDate = getPara("endDate",nowDate);
		String type = getPara("type","0");
		String tranType = getPara("tranType","");
		String rtnInd = getPara("rtnInd","");
		String inpDate = getPara("inpDate","");
		String inpTime = getPara("inpTime","");
		String relDate = getPara("relDate","");
		String traceNo = getPara("traceNo","");
		if(accountId.length()==11){
			 accountId = accountIdByMobile(accountId);
		}
		Map<String, Object> accountDetailsQuery2 = null;
		
		List<Map<String,String>>lists = new ArrayList<>();
		
		boolean circulation = true;
		String name = "";
		while (circulation) {
			try{
				accountDetailsQuery2 = accountDetailsQuery2(accountId, startDate, endDate, type, tranType, rtnInd, inpDate, inpTime, relDate, traceNo);
			}catch(Exception e){
				e.printStackTrace();
				renderText("导出时异常:<br><p>"+e.getMessage());
			}
			if(!"00000000".equals(accountDetailsQuery2.get("retCode").toString())){
				renderText("导出时异常:<br><p>"+accountDetailsQuery2.get("retMsg").toString());
			}else{
				Object object = accountDetailsQuery2.get("subPacks");
				name = accountDetailsQuery2.get("name").toString();
				ArrayList<Map<String, String>> list = (ArrayList<Map<String, String>>) object;
				if(!list.isEmpty()){
					for(int i = 0;i<list.size();i++){
						Map<String, String> map = list.get(i);
						String orFlag = map.get("orFlag");
						String txFlagValue = getTxFlagValue(map.get("txFlag"));
						map.put("txFlag",txFlagValue);
						String orFlagValue = "";
						if("O".equals(orFlag)){
							orFlagValue = "原始交易";
						}else if("R".equals(orFlag)){
							orFlagValue = "已经冲正或者撤销";
						}
						String currency = map.get("currency");
						if("156".equals(currency)){
							map.put("currency","人民币");
						}
						map.put("orFlag",orFlagValue);
					}
					Map<String, String> map = list.get(list.size()-1);
					inpDate = map.get("inpDate");
					inpTime = map.get("inpTime");
					relDate = map.get("relDate");
					traceNo = map.get("traceNo");
					if(list.size()<10){
						circulation = false;
					}
					lists.addAll(list);
					rtnInd = "1";
					
				}else{
					circulation = false;
				}
			}
		}
		String output_html = "";
		String output_extType = "text/html";
		String filename = name;
		HttpServletResponse response = getResponse();
			try{
				response.setCharacterEncoding("utf-8");
				output_extType = "application/vnd.ms-excel";
				output_html ="<head><style>table td,th{vnd.ms-excel.numberformat:@;text-align: center;} table th{color:red}</style></head>";
				output_html += "<table border='1'>";
				output_html += "<tr><td colspan='13' style='text-align:center;'><b>资金交易</b></td></tr>";
				output_html += "<tr><td><b>姓名</b></td><td><b>入账时间</b></td><td><b>交易日期</b></td><td><b>自然日期</b></td><td><b>交易时间</b></td><td><b>流水号</b></td><td><b>交易类型</b></td><td><b>冲正</b></td><td><b>交易金额</b></td><td><b>交易符号</b></td><td><b>交易描述</b></td><td><b>余额</b></td><td><b>对方账号</b></td></tr>";
				output_html +=export_makeHtml(lists, name);
				output_html +="</table>";
			}catch(Exception e){
				filename = "导出异常:"+filename+DateUtil.getNowDateTime()+".xls";
			}
			try {
				filename = new String(filename.getBytes("utf-8"),"ISO_8859_1");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			response.setHeader("Content-Disposition", "attachment;filename="+filename+"-"+DateUtil.getNowDateTime()+".xls");	
		
		renderText(output_html,output_extType);
		
   
	}
	private String export_makeHtml(List<Map<String,String>> list,String name){
		String output_html = "";
		try{
			for (int i = 0; i < list.size(); i++) {
				output_html += "<tr>";
				Map<String,String> tmp = list.get(i);
				output_html +="<td>" + name + "</td>";
				output_html += "<td>"+DateUtil.chenageDay(tmp.get("accDate"))+"</td>";
				output_html += "<td>"+DateUtil.chenageDay(tmp.get("inpDate"))+"</td>";
				output_html += "<td>"+DateUtil.chenageDay(tmp.get("relDate"))+"</td>";
				output_html += "<td>"+tmp.get("inpTime")+"</td>";
				output_html += "<td>"+tmp.get("traceNo")+"</td>";
				output_html += "<td>"+new String(Property.getPropertyValueByKey("transaction-type", tmp.get("tranType")).getBytes("ISO_8859_1"),"utf-8")+"</td>";
				output_html += "<td>"+tmp.get("orFlag")+"</td>";
				output_html += "<td>"+tmp.get("txAmount")+"</td>";
				output_html += "<td>"+tmp.get("txFlag")+"</td>";
				output_html += "<td>"+tmp.get("describe")+"</td>";
		
				output_html += "<td>"+tmp.get("currBal")+"</td>";
				output_html += "<td>"+tmp.get("forAccountId")+"</td>";
				output_html += "</tr>";
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return output_html;
	}
	
	/**
	 * 满标自动放款查询 WJW
	 * @param accountId	借款人电子账号
	 * @param lendPayOrderId	申请订单号
	 * @param productId	标号
	 * @return
	 */
	public static Map<String, String> autoLendPayQuery(String accountId,String lendPayOrderId,String productId){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "autoLendPayQuery");// 交易代码
		reqMap.put("accountId", accountId);// 电子账号
		reqMap.put("lendPayOrderId", lendPayOrderId);// 订单号
		reqMap.put("productId", productId);//标号
		
		return JXService.requestCommon(reqMap);
	}
	
	/**
	 * 交易订单查询(代付、代扣) WJW
	 * @return
	 */
	public static Map<String, String> Q9001(String orderNo){
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "Q9001");
		reqMap.put("orderNo", orderNo);
		return JXService.requestCommonTransfer(reqMap);
	}
	
	/**
	 * 代付、代扣订单查询 WJW
	 * @return
	 */
	@ActionKey("/Q9001")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message Q9001(){
		String orderNo = getPara("orderNo");
		if(StringUtil.isBlank(orderNo)){
			return error("", "订单号为空", null);
		}
		
		Map<String, String> Q9001 = null;
		try {
			Q9001 = JXQueryController.Q9001(orderNo);
			Object orderDetail = Q9001.get("orderDetail");
			JSONObject orderDetailObject = JSONObject.parseObject(orderDetail.toString());
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("retCode", Q9001.get("retCode"));
			jsonObject.put("retMsg", Q9001.get("retDesc"));
			
			String status = "";
			String orderStatus = orderDetailObject.getString("orderStatus");
			switch (orderStatus) {
			case "0":
				status = "已接受";
				break;
			case "1":
				status = "处理中";
				break;
			case "2":
				status = "处理成功";
				break;
			case "3":
				status = "处理失败";
				break;
			default:
				status = "即信又出问题了";
				break;
			}
			jsonObject.put("status", status);
			jsonObject.put("orderNo", Q9001.get("orderNo"));
			return succ("查询成功", jsonObject);
		} catch (Exception e2) {//查询通道异常
			return error("", "即信查询通道异常", null);
		}
	}
	
	/**
	 * 代扣代付对账文件下载
	 */
	@ActionKey("/D8001")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void D8001(){
		String tranDate = getPara("tranDate",DateUtil.getNowDate());//请求交易日期
		String fileType = getPara("fileType","2");//1、清算文件 2、当日或历史订单
		String tranType = getPara("tranType","2");//1扣款、2付款(注:只有当fileType为2时才有效)
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);
		reqMap.put("txCode", "D8001");
		reqMap.put("tranDate", tranDate);//交易日期
		reqMap.put("fileType", fileType);//文件类型
		reqMap.put("tranType", tranType);//交易类型
		String result = JXService.transferDownload(reqMap);
		if(StringUtil.isBlank(result)){
			renderText("下载通道异常");
			return;
		}
		
		String output_html = "";
		String output_extType = "text/html";
		String filename = "";//文件名
		if("1".equals(fileType)){
			filename = "清算文件";
		}else if("1".equals(tranType)){
			filename = "扣款对账文件";
		}else if("2".equals(tranType)){
			filename = "付款对账文件";
		}
		
		HttpServletResponse response = getResponse();
			response.setCharacterEncoding("utf-8");
			output_extType = "application/vnd.ms-excel";
			try {
				output_html = transferExportFile(result);
			} catch (Exception e) {
				filename = "导出异常:"+filename+DateUtil.getNowDateTime()+".xls";
			}
			try {
				filename= new String(filename.getBytes("utf-8"), "ISO_8859_1");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			response.setHeader("Content-Disposition", "attachment;filename="+filename+"-"+DateUtil.getNowDateTime()+".xls");
		renderText(output_html, output_extType);
	}
	
	private String transferExportFile(String result){
		String output_html = "<table border='1'>";
		String[] trTexts = result.split("\r\n");
		for (String trText : trTexts) {
			output_html += "<tr>";
			String[] tdTexts = trText.split(",");
			for (String tdText : tdTexts) {
				output_html += "<td>"+tdText+"</td>";
			}
			output_html += "</tr>";
			
		}
		output_html += "</table>";
		return output_html;
	
	}
	
	/**
	 * 代付列表查询 WJW
	 * @return
	 */
	@ActionKey("/T1001List")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message T1001List(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		String allkey = getPara("allkey","");
		
		if(allkey.length() == 11 && StringUtil.isNumeric(allkey)){//可能为手机号
			User user = userService.findByMobile(allkey);
			if(user != null){
				BanksV2 banksV2 = banksService.findByUserCode(user.getStr("userCode"));
				if(banksV2 != null){
					allkey = banksV2.getStr("bankNo");
				}
			}
		}
		
		JSONArray jsonArray = new JSONArray();
		Page<JXTrace> page = jxTraceService.queryByPage(pageNumber, pageSize, "T1001", allkey);
		List<JXTrace> jxTraces = page.getList();
		for (JXTrace jxTrace : jxTraces) {
			JSONObject jsonObject = new JSONObject();
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String cardNo = parseObject.getString("cardNo");//银行卡号
			String orderNo = parseObject.getString("orderNo");//订单号
			jsonObject.put("txMsg", "代付划拨");//交易描述
			jsonObject.put("accName", parseObject.getString("accName"));//姓名
			jsonObject.put("amount", parseObject.getString("amount"));//金额
			jsonObject.put("cardNo", cardNo);//银行卡号
			jsonObject.put("orderNo", orderNo);//订单号
			
			BanksV2 banksV2 = banksService.findByBankNo(cardNo);
			String mobile = "";
			String userName = "";
			try {
				mobile = CommonUtil.decryptUserMobile(banksV2.getStr("mobile"));
				userName = banksV2.getStr("userName");
			} catch (Exception e) {
				mobile = "手机号解析错误";
				userName = "用户名解析错误";
			}
			jsonObject.put("mobile", mobile);
			jsonObject.put("userName", userName);
			
			Map<String, String> Q9001 = null;
			try {
				Q9001 = JXQueryController.Q9001(orderNo);
				Object orderDetail = Q9001.get("orderDetail");
				jsonObject.put("retMsg", Q9001.get("retDesc"));
				if(orderDetail == null){
					jsonObject.put("status", "");
					jsonArray.add(jsonObject);
					continue;
				}
				JSONObject orderDetailObject = JSONObject.parseObject(orderDetail.toString());
				
				String status = "";
				String orderStatus = orderDetailObject.getString("orderStatus");
				switch (orderStatus) {
				case "0":
					status = "已接受";
					break;
				case "1":
					status = "处理中";
					break;
				case "2":
					status = "处理成功";
					break;
				case "3":
					status = "处理失败";
					break;
				default:
					status = "即信又出问题了";
					break;
				}
				jsonObject.put("status", status);
			} catch (Exception e2) {//查询通道异常
				return error("", "即信查询通道异常", null);
			}
			jsonArray.add(jsonObject);
		}
		return succ("查询成功", new Page<>(jsonArray, pageNumber, pageSize, page.getTotalPage(), page.getTotalRow()));
	}
	
}
