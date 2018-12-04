package com.dutiantech.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.model.JXTrace;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jx.service.JXService;

/**
 * 江西银行存管流水
 */
public class JXTraceService extends BaseService {
	/**
	 * WJW 根据jxTraceCode查询流水
	 * 
	 * @param jxTraceCode
	 * @return JXTrace
	 */
	public JXTrace findById(String jxTraceCode) {
		return JXTrace.jxTraceDao.findById(jxTraceCode);
	}

	/**
	 * WJW 新增一条流水
	 * 
	 * @param jxTraceCode
	 *            本地交易流水号
	 * @param version
	 *            版本号
	 * @param instCode
	 *            机构代码
	 * @param bankCode
	 *            银行代码
	 * @param txDate
	 *            交易日期
	 * @param txTime
	 *            交易时间
	 * @param seqNo
	 *             交易流水号
	 * @param requestChannel
	 *             请求交易渠道(000001手机APP,000002网页,000003微信,000004柜面)
	 * @param requestSign
	 *             请求签名
	 * @param batchNo
	 * 			   批次号
	 * @param txAmount
	 * 			      交易金额
	 * @param accountId
	 * 			      发起方电子账号
	 * @param forAccountId
	 * 			      对手电子账号
	 * @param orderId
	 * 			      订单号
	 * @param requestMessage
	 *             请求报文
	 * @param acqRes
	 *             请求方保留
	 * @param jxTraceStatus
	 * 			   状态
	 * @param remark
	 *             备注
	 * @return boolean
	 */
	public boolean save(String jxTraceCode, String version, String instCode, String bankCode, String txDate,
			String txTime, String seqNo, String txCode, String requestChannel, String requestSign, String batchNo, String txAmount, String accountId, String forAccountId, String orderId, String requestMessage,
			String acqRes, String jxTraceStatus, String remark) {
		JXTrace jxTrace = new JXTrace();
		jxTrace.set("jxTraceCode", jxTraceCode);
		jxTrace.set("version", version);
		jxTrace.set("instCode", instCode);
		jxTrace.set("bankCode", bankCode);
		jxTrace.set("txDate", txDate);
		jxTrace.set("txTime", txTime);
		jxTrace.set("seqNo", seqNo);
		jxTrace.set("txCode", txCode);
		jxTrace.set("requestChannel", requestChannel);
		jxTrace.set("requestSign", requestSign);
		jxTrace.set("batchNo", batchNo);
		jxTrace.set("txAmount", StringUtil.isBlank(txAmount) ? 0:txAmount);
		jxTrace.set("requestMessage", requestMessage);
		jxTrace.set("acqRes", acqRes);
		jxTrace.set("jxTraceStatus", jxTraceStatus);
		jxTrace.set("remark", remark);
		jxTrace.set("createDate", DateUtil.getNowDate());
		jxTrace.set("createTime", DateUtil.getNowTime());
		//batchLendPay:批次放款,batchRepay:批次还款,batchCreditEnd:批次结束债权,batchCreditInvest:批次投资人购买债权,batchVoucherPay:批次发红包,batchSubstRepay:批次代偿
		String[] txCodes = {"batchLendPay","batchRepay","batchCreditEnd","batchCreditInvest","batchVoucherPay","batchSubstRepay"};
		if(Arrays.asList(txCodes).contains(txCode)){
			List<String> accountIds = new ArrayList<String>();
			List<String> forAccountIds = new ArrayList<String>();
			List<String> orderIds = new ArrayList<String>();
			try {
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				String subPacks = parseObject.getString("subPacks");
				JSONArray parseArray = JSONArray.parseArray(subPacks);
				for (int i = 0; i < parseArray.size(); i++) {
					JSONObject jsonObject = parseArray.getJSONObject(i);
					String accountIdStr = jsonObject.getString("accountId");
					String forAccountIdStr = jsonObject.getString("forAccountId");
					String orderIdStr = jsonObject.getString("orderId");
					if(!StringUtil.isBlank(accountIdStr) && !accountIds.contains(accountIdStr)){
						accountIds.add(accountIdStr);
					}
					if(!StringUtil.isBlank(forAccountIdStr) && !forAccountIds.contains(forAccountIdStr)){
						forAccountIds.add(forAccountIdStr);
					}
					if(!StringUtil.isBlank(orderIdStr) && !orderIds.contains(orderIdStr)){
						orderIds.add(orderIdStr);
					}
				}
				jxTrace.set("accountId", StringUtils.join(accountIds,"&&"));
				jxTrace.set("forAccountId", StringUtils.join(forAccountIds,"&&"));
				jxTrace.set("orderId", StringUtils.join(orderIds,"&&"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			jxTrace.set("accountId", accountId);
			jxTrace.set("forAccountId", forAccountId);
			jxTrace.set("orderId", orderId);
		}
		return jxTrace.save();
	}

	/**
	 * 根据jxTraceCode更改响应报文 WJW
	 * @param responseMessage	响应报文
	 * @param jxTraceCode		主键code
	 * @return
	 */
	public boolean updateResponseMessage(String responseMessage,String jxTraceCode){
		String sql = "update t_jx_trace set responseMessage=? where jxTraceCode=?";
		return Db.update(sql, responseMessage,jxTraceCode) > 0;
	}
	
	/**
	 * WJW 流水更新响应状态
	 * 
	 * @param jxTraceCode
	 *            本地交易流水号
	 * @param responseChannel
	 *            响应交易渠道(000001手机APP,000002网页,000003微信,000004柜面)
	 * @param responseSign
	 *            响应签名
	 * @param retCode
	 *            响应代码
	 * @param retMsg
	 *            响应描述
	 * @param responseMessage
	 *            响应报文
	 * @param acqRes
	 *            请求方保留
	 * @param remark
	 *            备注
	 * @param jxTraceStatus
	 * 			  状态
	 * @return boolean
	 */
	public boolean updateResponse(String jxTraceCode, String responseChannel, String responseSign, String retCode,
			String retMsg, String responseMessage, String acqRes, String jxTraceStatus, String remark) {
		JXTrace jxTrace = findById(jxTraceCode);
		jxTrace.set("responseChannel", responseChannel);
		jxTrace.set("responseSign", responseSign);
		jxTrace.set("retCode", retCode);
		jxTrace.set("retMsg", retMsg);
		jxTrace.set("responseMessage", responseMessage);
		jxTrace.set("acqRes", acqRes);
		if(!StringUtil.isBlank(jxTraceStatus)){
			jxTrace.set("jxTraceStatus", jxTraceStatus);
		}
		if(!StringUtil.isBlank(remark)){
			jxTrace.set("remark", remark);
		}
		jxTrace.set("updateDate", DateUtil.getNowDate());
		jxTrace.set("updateTime", DateUtil.getNowTime());
		return jxTrace.update();
	}

	/**
	 * 根据今天江西银行批次流水条数生成批次号 WJW
	 * @return
	 */
	public synchronized int batchNoByToday(){
		String nowDate = DateUtil.getNowDate();
		Long count = Db.queryLong("select count(1) from t_jx_trace where txDate=? and txCode in ('batchLendPay','batchRepay','batchCreditEnd','batchCancel','batchCreditInvest','batchVoucherPay','batchSubstRepay')", nowDate);
		int batchNo = count.intValue() + 1;
		JXTrace jxTrace = findByBatchNoAndTxDate(nowDate, String.valueOf(batchNo));
		while (nowDate.equals(Memcached.get("batchNo_"+String.valueOf(batchNo))) || jxTrace != null){
			batchNo++;
			jxTrace = findByBatchNoAndTxDate(nowDate, String.valueOf(batchNo));
		}
		Memcached.set("batchNo_"+String.valueOf(batchNo), nowDate, DateUtil.getTodayToTomTime());
		return batchNo;
	}

	/**
	 * 根据交易代码及交易时间查询JXTrace列表 WJW
	 * @param txCode	交易代码
	 * @param txDate	交易时间
	 * @return
	 */
	public List<JXTrace> queryByTxCodeAndTxDate(String txCode,String txDate){
		String sql = "select * from t_jx_trace where txCode=? and txDate=?";
		return JXTrace.jxTraceDao.find(sql, txCode,txDate);
	}
	
	/**
	 * 查询江西批次流水状态 WJW
	 * @param jxTraceCode
	 * @return	1:请求未发送(或江西未实时响应) 2:未收到任何异步通知 3:通过数据合法校验 4:未通过数据合法校验 5:部分成功 6:全部成功 7:全部失败 8:响应报文存的什么鬼
	 */
	public int jxTraceState(String jxTraceCode){
		//读取t_jx_trace流水号对应交易响应报文,进行本地流水处理
		JXTrace jxTrace = findById(jxTraceCode);//交易记录
		if(jxTrace == null){//批量迁移
			return 6;
		}
		
		String responseMessage = jxTrace.getStr("responseMessage");//响应报文
		
		if("admin".equals(responseMessage)){//已人工处理
			return 6;
		}
		
		JSONObject parseObject = null;
		try {
			parseObject = JSONObject.parseObject(responseMessage);
		} catch (Exception e) {
			return 8;
		}
		
		if(StringUtil.isBlank(responseMessage)){
			return 1;
		}
		
		String received = parseObject.getString("received");//success接收成功
		if(!StringUtil.isBlank(received)){
			return 2;
		}
		
		String txCounts = parseObject.getString("txCounts");//交易笔数(业务处理结果的异步通知)
		String retCode = parseObject.getString("retCode");//响应代码(业务处理结果的异步通知)
		if(!StringUtil.isBlank(txCounts) && !StringUtil.isBlank(retCode)){
			if("00000000".equals(retCode)){//通过数据合法校验
				return 3;
			}else {//未通过数据合法校验
				return 4;
			}
		}
		
		int sucCounts = 0;//成功交易笔数
		double failAmount = 0;//失败交易金额
		int failCounts =0;//失败交易笔数
		if(!StringUtil.isBlank(parseObject.getString("sucCounts"))){
			sucCounts = Integer.valueOf(parseObject.getString("sucCounts"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failAmount"))){
			failAmount = Double.valueOf(parseObject.getString("failAmount"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failCounts"))){
			failCounts = Integer.valueOf(parseObject.getString("failCounts"));
		}
		
		if(sucCounts>0 && (failAmount>0 || failCounts>0)){
			return 5;//部分成功
		}
		if(sucCounts>0 && (failAmount==0 || failCounts==0)){
			return 6;//全部成功
		}
		if(sucCounts==0 && (failAmount>0 || failCounts>0)){
			return 7;//全部失败
		}
		if(sucCounts==0 && (failAmount==0 || failCounts==0)){
			return 8;//响应报文存的什么鬼
		}
		return 0;
	}
	
	/**
	 * 判断批次交易状态 WJW
	 * @param jxTrace
	 * @return	1:请求未发送(或江西未实时响应) 2:未收到任何异步通知 3:通过数据合法校验 4:未通过数据合法校验 5:部分成功 6:全部成功 7:全部失败 8:响应报文存的什么鬼
	 */
	public int jxTraceState(JXTrace jxTrace){
		//读取t_jx_trace流水号对应交易响应报文,进行本地流水处理
		if(jxTrace == null){//批量迁移
			return 6;
		}
		
		String responseMessage = jxTrace.getStr("responseMessage");//响应报文
		
		if("admin".equals(responseMessage)){//已人工处理
			return 6;
		}
		
		JSONObject parseObject = null;
		try {
			parseObject = JSONObject.parseObject(responseMessage);
		} catch (Exception e) {
			return 8;
		}
		
		if(StringUtil.isBlank(responseMessage)){
			return 1;
		}
		
		String received = parseObject.getString("received");//success接收成功
		if(!StringUtil.isBlank(received)){
			return 2;
		}
		
		String txCounts = parseObject.getString("txCounts");//交易笔数(业务处理结果的异步通知)
		String retCode = parseObject.getString("retCode");//响应代码(业务处理结果的异步通知)
		if(!StringUtil.isBlank(txCounts) && !StringUtil.isBlank(retCode)){
			if("00000000".equals(retCode)){//通过数据合法校验
				return 3;
			}else {//未通过数据合法校验
				return 4;
			}
		}
		
		int sucCounts = 0;//成功交易笔数
		double failAmount = 0;//失败交易金额
		int failCounts =0;//失败交易笔数
		if(!StringUtil.isBlank(parseObject.getString("sucCounts"))){
			sucCounts = Integer.valueOf(parseObject.getString("sucCounts"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failAmount"))){
			failAmount = Double.valueOf(parseObject.getString("failAmount"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failCounts"))){
			failCounts = Integer.valueOf(parseObject.getString("failCounts"));
		}
		
		if(sucCounts>0 && (failAmount>0 || failCounts>0)){
			return 5;//部分成功
		}
		if(sucCounts>0 && (failAmount==0 || failCounts==0)){
			return 6;//全部成功
		}
		if(sucCounts==0 && (failAmount>0 || failCounts>0)){
			return 7;//全部失败
		}
		if(sucCounts==0 && (failAmount==0 || failCounts==0)){
			return 8;//响应报文存的什么鬼
		}
		return 0;
	}

	/**
	 * 根据交易时间查询交易流水 WJW
	 * @param txCode	交易代码
	 * @param start		开始时间
	 * @param end		结束时间
	 * @return
	 */
	public List<JXTrace> queryByTxCodeDate(String txCode,String start,String end){
		String sql = "select * from t_jx_trace where txCode = ? and txDate between ? and ?";
		return JXTrace.jxTraceDao.find(sql,txCode,start,end);
	}
	
	/**
	 * 根据交易时间查询投标流水 WJW
	 * @param start		开始时间
	 * @param end		结束时间
	 * @return
	 */
	public List<JXTrace> queryBidApplyByDate(String start,String end){
		String sql = "select * from t_jx_trace where txCode in ('bidApply','bidAutoApply') and txDate between ? and ?";
		return JXTrace.jxTraceDao.find(sql,start,end);
	}
	
	/**
	 * 根据起始日期及标号查询投标流水 WJW
	 * @param startDate	查询起始日期
	 * @param loanCode	标号
	 * @return
	 */
	public List<JXTrace> queryBidApplyByDateAndLoanCode(String startDate,String loanCode){
		String sql = "select * from t_jx_trace where txDate >= "+startDate+" and txCode in ('bidApply','bidAutoApply') and requestMessage like '%"+loanCode+"%'";
		return JXTrace.jxTraceDao.find(sql);
	}
	
	/**
	 * 查询流水
	 * @param retCode2 
	 * @param jxTraceCode2 
	 */

	public Page<JXTrace> queryTracePage(int pageNumber,int pageSize,String product,String orderId,String orgOrderId,String txCode, String accountId, String startTxDate, String endTxDate, String jxTraceCode,
			String retCode,String state) {
				String sqlSelect = "select * ";
				String sqlFrom = "from t_jx_trace ";
				String sqlOrder = "order by txDate desc,txTime desc";
				StringBuffer buff = new StringBuffer();
				List<Object> paras = new ArrayList<Object>();
				if(!StringUtil.isBlank(orderId)){
					makeExp4like(buff, "requestMessage", orderId, paras);
				}
				if(!StringUtil.isBlank(orgOrderId)){
					makeExp4like(buff, "requestMessage", orgOrderId, paras);
				}
				if(!StringUtil.isBlank(product)){
					makeExp4like(buff, "requestMessage", product, paras);
				}
				if(!StringUtil.isBlank(accountId)){
					makeExp4like(buff, "requestMessage", accountId, paras);
				}
				if(!StringUtil.isBlank(state)){
					if("00000000".equals(state)){
						makeExp(buff,paras, "retCode", "=", state,"and");
					}else if("2".equals(state)){
						makeExp(buff,paras, "retCode", "!=", "00000000","and");
					}else{
						makeExp(buff, paras, "retCode", "is", null, "and");
					}
				}
				
				if(!StringUtil.isBlank(jxTraceCode)){
					makeExp(buff,paras, "jxTraceCode", "=", jxTraceCode,"and");
				}
				if(!StringUtil.isBlank(txCode)){
					makeExp(buff,paras,"txCode","=",txCode,"and");
				}
				if(!StringUtil.isBlank(startTxDate)){
					makeExp(buff, paras, "txDate", ">=", startTxDate, "and");
				}if(!StringUtil.isBlank(endTxDate)){
					makeExp(buff,paras,"txDate","<=",endTxDate,"and");
				}
				return JXTrace.jxTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
				

	}
	
	/**
	 * 分页查询jxTrace
	 */
	public Page<JXTrace> queryJxTraceCodeByAccountId(String startDateTime,String endDateTime,String txCodeTrans,String state,int pageNumber,int pageSize,String accountId){
		String[] txCode = {"directRechargePage","offlineRechargeCall","withdraw","voucherPay","voucherPayCancel","voucherPayDelayCancel"};
		String sqlSelect = "select * ";
		String sqlFrom = "from t_jx_trace ";
		String sqlOrder = "order by txDate desc,txTime desc";
		StringBuffer buff = new StringBuffer();
		List<Object> paras = new ArrayList<Object>();
		if(!StringUtil.isBlank(state)){
			if("00000000".equals(state)){
				makeExp(buff,paras, "retCode", "=", state,"and");
			}else if("2".equals(state)){
				makeExp(buff,paras, "retCode", "!=", "00000000","and");
			}else {
				makeExp(buff, paras, "retCode", "is", null, "and");
			}
		}
		if(!StringUtil.isBlank(startDateTime)){
			makeExp(buff, paras, "CONCAT(txDate,txTime)", ">=", startDateTime, "and");
		}
		if(!StringUtil.isBlank(endDateTime)){
			makeExp(buff,paras,"txDate","<=",endDateTime,"and");
		}
		makeExp4like(buff, "requestMessage", accountId, paras);
		if(StringUtil.isBlank(txCodeTrans)){
			makeExp4In(buff, paras, "txCode", txCode, "and");
		}else{
			makeExp(buff, paras, "txCode", "=", txCodeTrans, "and");
		}
		return JXTrace.jxTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
	}
	/**
	 * 根据流水号查询jxTrace信息
	 */
	public JXTrace queryJXTraceByJxTraceCode(String jxTraceCode){
		List<JXTrace> list = JXTrace.jxTraceDao.find("select * from t_jx_trace where jxTraceCode=?", jxTraceCode);
		if(!list.isEmpty()){
			return list.get(0);
		}
		return null;
	}
	
	
	/**
	 * 新增一条响应记录——请求由即信发起（平台没有请求报文）
	 * @param jxTraceCode
	 * @param version
	 * @param instCode
	 * @param bankCode
	 * @param txDate
	 * @param txTime
	 * @param seqNo
	 * @param txCode
	 * @param responseChannel
	 * @param responseSign
	 * @param responseMessage
	 * @param acqRes
	 * @param remark
	 * @return
	 */
	public boolean saveResponse(String jxTraceCode, String version, String instCode, String bankCode, String txDate,
			String txTime, String seqNo, String txCode, String responseChannel, String responseSign, String responseMessage,
			String acqRes, String remark){
		JXTrace jxTrace = new JXTrace();
		jxTrace.set("jxTraceCode", jxTraceCode);
		jxTrace.set("version", version);
		jxTrace.set("instCode", instCode);
		jxTrace.set("bankCode", bankCode);
		jxTrace.set("txDate", txDate);
		jxTrace.set("txTime", txTime);
		jxTrace.set("seqNo", seqNo);
		jxTrace.set("txCode", txCode);
		jxTrace.set("responseChannel", responseChannel);
		jxTrace.set("responseSign", responseSign);
		jxTrace.set("responseMessage", responseMessage);
		jxTrace.set("acqRes", acqRes);
		jxTrace.set("remark", remark);
		return jxTrace.save();
	}
	
	/**
	 * 分页查询红包发放信息
	 * @param pageNumber	第几页
	 * @param pageSize		每页多少条
	 * @return
	 */
	public Page<JXTrace> queryVoucherPaysByPage(Integer pageNumber, Integer pageSize, String allkey){
		String sqlSelect = "select jxTraceCode,txDate,txTime,seqNo,txCode,retCode,retMsg,requestMessage,remark";
		String sqlFrom = " from t_jx_trace where txCode='voucherPay'";
		String sqlOrder = " order by jxTraceCode desc";
		
		if(!StringUtil.isBlank(allkey)){
			sqlFrom += " and requestMessage like '%"+allkey+"%'";
		}
		
		return JXTrace.jxTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder);
		
	}
	
	/**
	 * 查询红包是否撤销 WJW
	 * @return	true:已撤销 false:未撤销
	 */
	public boolean voucherPayCancel(String txDate,String txTime,String seqNo){
		String sql ="select requestMessage from t_jx_trace where txCode in ('voucherPayCancel','voucherPayDelayCancel') and retCode='00000000'";
		List<JXTrace> jxTraces = JXTrace.jxTraceDao.find(sql);
		for (JXTrace jxTrace : jxTraces) {
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String orgTxDate = parseObject.getString("orgTxDate");
			String orgTxTime = parseObject.getString("orgTxTime");
			String orgSeqNo = parseObject.getString("orgSeqNo");
			if(txDate.equals(orgTxDate) && txTime.equals(orgTxTime) && seqNo.equals(orgSeqNo)){
				return true;
			}
		}
		return false;
	}

	/**
	 * 查询用户缴费授权状态 WJW
	 * @param accountId	用户电子账号
	 * @return	null:没有签约记录/ [type:(1:开通,2:到期,3:解约) time:(yyyymmdd到期时间或最后一次解约时间) amount:(签约金额,单位分)]
	 */
	@Deprecated
	public JSONObject paymentAuthPageState(String accountId){
		String maxAmt = "0";
		String deadLine = null;
		String dateCancel = null;
		String requestMessage = null;
		String paymentAuth = null;	// 开通缴费授权功能标志 0 取消  1开通
		String sqltermsAuthPage="select * from t_jx_trace where txCode='termsAuthPage' and retCode='00000000' and requestMessage like '%"+accountId+"%' and requestMessage like '%paymentAuth%' order by jxTraceCode desc";
		JXTrace termsAuthPage = JXTrace.jxTraceDao.findFirst(sqltermsAuthPage);
		if (termsAuthPage != null) {
			requestMessage = termsAuthPage.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			paymentAuth = parseObject.getString("paymentAuth");
			deadLine = "".equals(parseObject.getString("paymentDeadline")) ? "000000":parseObject.getString("paymentDeadline");//缴费授权签约到期日
			maxAmt =  "".equals(parseObject.getString("paymentMaxAmt")) ? "0":parseObject.getString("paymentMaxAmt") ;//缴费授权签约最高金额
			dateCancel = termsAuthPage.getStr("txDate")+termsAuthPage.getStr("txTime");//解约时间
		} else {
			String sqlPaymentAuthPage = "select * from t_jx_trace where txCode='paymentAuthPage' and retCode='00000000' and requestMessage like '%"+accountId+"%' order by jxTraceCode desc";
			String sqlPaymentAuthCancel = "select * from t_jx_trace where txCode='paymentAuthCancel' and retCode='00000000' and requestMessage like '%"+accountId+"%' order by jxTraceCode desc";
			JXTrace paymentAuthPage = JXTrace.jxTraceDao.findFirst(sqlPaymentAuthPage);
			JXTrace paymentAuthCancel = JXTrace.jxTraceDao.findFirst(sqlPaymentAuthCancel);
			if (paymentAuthPage != null) {
				paymentAuth = "1";
				requestMessage = paymentAuthPage.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				maxAmt = parseObject.getString("maxAmt");//签约金额
				deadLine = parseObject.getString("deadline");//签约到期时间
				if(paymentAuthCancel != null){//存在解约记录
					paymentAuth = "0";
					requestMessage = paymentAuthCancel.getStr("requestMessage");
					dateCancel = paymentAuthCancel.getStr("txDate")+paymentAuthCancel.getStr("txTime");//解约时间
					JSONObject parseObjectCancel = JSONObject.parseObject(requestMessage);
					maxAmt = parseObjectCancel.getString("maxAmt");//解约金额
				}
			}
		}
		
		if (paymentAuth != null) {
			if("0".equals(paymentAuth)){
				return JSONObjectParameter("3", dateCancel, StringUtil.getMoneyCent(maxAmt));
			} else if ("1".equals(paymentAuth)&&Long.valueOf(DateUtil.getNowDate()) > Long.valueOf(deadLine)){
				return JSONObjectParameter("2", deadLine, StringUtil.getMoneyCent(maxAmt));
			} else {
				return JSONObjectParameter("1", deadLine, StringUtil.getMoneyCent(maxAmt));
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 查询用户是否设置过密码
	 * @param accountId
	 * @return
	 */
	public static String verifyPwd(String accountId){
		Map<String, String> passwordSet = null;
		try {
			passwordSet = JXQueryController.passwordSetQuery(accountId);
		} catch (Exception e) {
		}
		if(passwordSet!=null){
			if("1".equals(passwordSet.get("pinFlag"))){
				return "y";
			}else{
				return "n";
			}
		}else{
			return "n";
		}
	}
	
	/**
	 * 查询用户还款授权状态 WJW
	 * @param accountId	用户电子账号
	 * @return	null:没有签约记录/ [type:(1:开通,2:到期,3:解约) time:(yyyymmdd到期时间或最后一次解约时间) amount:(签约金额,单位分)]
	 */
	public JSONObject repayAuthPageState(String accountId){
		String requestMessage = null;
		String repayAuth = null;
		String repayDeadline = null;
		String repayMaxAmt = null;
		String dateCancel = null;
		//多合一授权信息查询
		String sqltermsAuthPage="select * from t_jx_trace where txCode='termsAuthPage' and retCode='00000000' and requestMessage like '%"+accountId+"%' and requestMessage like '%repayAuth%' order by jxTraceCode desc";
		JXTrace termsAuthPage = JXTrace.jxTraceDao.findFirst(sqltermsAuthPage);
		if(termsAuthPage !=null){
			 requestMessage = termsAuthPage.getStr("requestMessage");
			 JSONObject parseObject = JSONObject.parseObject(requestMessage);
			 repayAuth = parseObject.getString("repayAuth");//开通还款授权功能标志 0 取消  1开通  
			 repayDeadline ="".equals(parseObject.getString("repayDeadline")) ? "000000":parseObject.getString("repayDeadline") ;//还款授权签约到期日
			 repayMaxAmt = "".equals(parseObject.getString("repayMaxAmt")) ? "0":parseObject.getString("repayMaxAmt");//还款授权签约最高金额
			 dateCancel = termsAuthPage.getStr("txDate")+termsAuthPage.getStr("txTime");//解约时间	
		}
		else{
			String sqlRepayAuthPage = "select * from t_jx_trace where txCode='repayAuthPage' and retCode='00000000' and requestMessage like '%"+accountId+"%' order by jxTraceCode desc";
			String sqlRepayAuthCancel = "select * from t_jx_trace where txCode='repayAuthCancel' and retCode='00000000' and requestMessage like '%"+accountId+"%' order by jxTraceCode desc";
			JXTrace repayAuthPage = JXTrace.jxTraceDao.findFirst(sqlRepayAuthPage);
			JXTrace repayAuthCancel = JXTrace.jxTraceDao.findFirst(sqlRepayAuthCancel);
			if(repayAuthPage != null){//用户有签约记录
				 repayAuth = "1";
				 requestMessage = repayAuthPage.getStr("requestMessage");
				 JSONObject parseObject = JSONObject.parseObject(requestMessage);
				 repayDeadline = parseObject.getString("deadline");//签约到期时间
				 repayMaxAmt = parseObject.getString("maxAmt");//签约金额
				if(repayAuthCancel != null){//存在解约记录
					repayAuth = "0";
					dateCancel = repayAuthCancel.getStr("txDate")+repayAuthCancel.getStr("txTime");//解约时间
					requestMessage = repayAuthCancel.getStr("requestMessage");
					JSONObject parseObjectCancel = JSONObject.parseObject(requestMessage);
					repayMaxAmt = parseObjectCancel.getString("maxAmt");//解约金额	
				}
			}
		}
		
		if (repayAuth != null) {
			if("0".equals(repayAuth)){
				return JSONObjectParameter("3", dateCancel, StringUtil.getMoneyCent(repayMaxAmt));
			} else if ("1".equals(repayAuth)&&Long.valueOf(DateUtil.getNowDate()) > Long.valueOf(repayDeadline)){
				return JSONObjectParameter("2", repayDeadline, StringUtil.getMoneyCent(repayMaxAmt));
			} else {
				return JSONObjectParameter("1", repayDeadline, StringUtil.getMoneyCent(repayMaxAmt));
			}
		} else {
			return null;
		}
	}
	
	/**
	 * 签约JSONObject带参方法 WJW
	 * @param type	状态
	 * @param time	时间
	 * @param amount	金额
	 * @return
	 */
	public JSONObject JSONObjectParameter(String type,String time,long amount){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("type", type);
		jsonObject.put("time", time);
		jsonObject.put("amount", amount);
		return jsonObject;
	}
	

	/**
	 * 根据交易日期及批次号查询批次记录 WJW
	 * @return
	 */
	public JXTrace findByBatchNoAndTxDate(String txDate,String batchNo){
		String sql = "select * from t_jx_trace where txDate="+txDate+" and txCode in ('batchLendPay','batchRepay','batchCreditEnd','batchCancel','batchCreditInvest','batchVoucherPay','batchSubstRepay') and requestMessage like '%\"batchNo\":\""+batchNo+"\"%'";
		return JXTrace.jxTraceDao.findFirst(sql);
	}

	/**
	 * 根据日期与标号查询标批次代偿还款记录 WJW
	 * @param txDate
	 * @param loanCode
	 * @return
	 */
	public List<JXTrace> queryLoanTrace(String txDate,String loanCode){
		String sql = "select jxTraceCode,requestMessage,txDate from t_jx_trace where txCode='batchSubstRepay' and txDate = '"+txDate+"' and requestMessage like '%"+loanCode+"%'";
		return JXTrace.jxTraceDao.find(sql);
	}
	
	/**
	 * 根据日期查询批次代偿还款记录 WJW
	 * @param txDate
	 * @return
	 */
	public List<JXTrace> queryBatchSubstRepayByTxDate(String txDate){
		String sql = "select jxTraceCode,requestMessage,retCode,responseMessage,remark from t_jx_trace where txCode='batchSubstRepay' and txDate=?";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 根据起始日期查询批次代偿还款数量 WJW
	 * @param txDate
	 * @return
	 */
	public long countBatchSubstRepayByTxDate(String txDate){
		String sql = "select count(1) from t_jx_trace where txCode='batchSubstRepay' and txDate>=?";
		return Db.queryLong(sql, txDate);
	}
	
	/**
	 * 根据日期查询资金未解冻批次代偿还款记录 WJW
	 * @param txDate
	 * @return
	 */
	public List<JXTrace> queryUnthawingBatchByTxDate(String txDate){
		String sql = "select jxTraceCode,requestMessage,retCode,responseMessage,remark from t_jx_trace where txCode='batchSubstRepay' and txDate=? and (remark <> 'y' or remark is null)";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 根据日期与traceCode查询标红包还款记录 WJW
	 * @param txDate
	 * @param traceCode
	 * @return
	 */
	public List<JXTrace> queryLoanTraceVoucher(String txDate,String traceCode){
		String sql = "select retCode from t_jx_trace where txCode='voucherPay' and txDate between '"+txDate+"' and '"+DateUtil.addDay(txDate, 1)+"' and requestMessage like '%"+traceCode+"%'";
		return JXTrace.jxTraceDao.find(sql);
	}

	/**
	 * 根据标号查询jxTrace批次放款记录 WJW
	 * @param loanCode
	 * @return
	 */
	public List<JXTrace> queryBatchLendPayByLoanCode(String loanCode){
		String sql = "select * from t_jx_trace where txCode='batchLendPay' and requestMessage like '%"+loanCode+"%'";
		return JXTrace.jxTraceDao.find(sql);
	}
	
	/**
	 * 根据日期查询红包还款发放失败记录 WJW
	 * @return
	 */
	public List<JXTrace> queryFailRepaymentVoucher(String txDate){
		String sql = "select * from t_jx_trace where txCode='voucherPay' and txDate=? and (retCode != '00000000' or retCode is null) and requestMessage like '%还款%'";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}

	/**
	 * 更改备注信息 WJW
	 * @param remark
	 * @param jxTraceCode
	 * @return
	 */
	public boolean updateRemark(String remark,String jxTraceCode){
		String sql = "update t_jx_trace set remark=? where jxTraceCode=?";
		return Db.update(sql,remark,jxTraceCode) > 0;
	}
	
	/**
	 * 按日期查询红包还款(返佣)发放失败(未处理)记录 WJW
	 * @return
	 */
	public List<JXTrace> queryFailUntreatedVoucherPay(String txDate){
		String sql = "select * from t_jx_trace where txCode='voucherPay' and txDate=? and (retCode != '00000000' or retCode is null) and (requestMessage like '%还款%' or requestMessage like '%返佣%') and remark != 'y'";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 按日期查询红包还款(返佣)记录 WJW
	 * @return
	 */
	public List<JXTrace> queryRepaymentVoucherPay(String txDate){
		String sql = "select * from t_jx_trace where txCode='voucherPay' and txDate=? and (requestMessage like '%还款%' or requestMessage like '%返佣%')";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 扫描当日TPS或交易开通验证异常批次未处理代偿还款 WJW
	 * @return
	 */
	public List<JXTrace> queryTPSUntreatedBatch(String txDate){
		String sql = "select * from t_jx_trace where txDate=? and txCode='batchSubstRepay' and retCode='JX900664' and remark != 'y'";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 扫描响应报文为空且未处理批次代偿还款记录 WJW
	 * @param txDate
	 * @return
	 */
	public List<JXTrace> queryResponseMessageNull(String txDate){
		String sql = "select * from t_jx_trace where txDate=? and txCode='batchSubstRepay' and responseMessage is null and remark != 'y';";
		return JXTrace.jxTraceDao.find(sql,txDate);
	}
	
	/**
	 * 根据起始日期查询未解冻批次数量 WJW
	 * @param txDate
	 * @return
	 */
	public long countNotUntreatedBatch(String txDate){
		String sql = "select count(1) from t_jx_trace where txDate>=? and txCode='batchSubstRepay' and remark != 'y'";
		return Db.queryLong(sql,txDate);
	}
	
	/**
	 * 根据交易代码，电子账户,订单号查询通知流水
	 * @param txCode
	 * @param jxAccountId
	 * @param orderId
	 * @param beginDateTime
	 * @param endDateTime
	 * @return
	 */
	public List<JXTrace> queryJxTrace(String txCode,String jxAccountId,String orderId,String beginDateTime,String endDateTime){
		String sqlSelect = "select *  ";
		String sqlFrom = " from t_jx_trace ";
		String sqlWhe = " where txCode = '"+txCode+"'";
		String sqlOrder = "  ORDER BY CONCAT(txDate,txTime) desc";
		if(!StringUtil.isBlank(orderId)){
			sqlWhe += " and requestMessage like '%"+orderId+"%'";
		}
		if(!StringUtil.isBlank(jxAccountId)){
			sqlWhe += " and requestMessage like '%"+jxAccountId+"%'";
		}
		if(!StringUtil.isBlank(beginDateTime)){
			sqlWhe += " and concat(txDate,txTime) >= "+beginDateTime;
		}
		if(!StringUtil.isBlank(endDateTime)){
			sqlWhe += " and concat(txDate,txTime) <= "+endDateTime;
		}
		System.out.println(sqlSelect+sqlFrom+sqlWhe+sqlOrder);
		return JXTrace.jxTraceDao.find(sqlSelect+sqlFrom+sqlWhe+sqlOrder);
	}
	
	/**
	 * 查询债权转让存管订单流水
	 * transferCode 债权流水号
	 * */
	public List<JXTrace> queryTranfers(String transferCode){
		return JXTrace.jxTraceDao.find("select * from t_jx_trace where txCode = 'creditInvest' and remark=? order by CONCAT(txDate,txTime) desc",transferCode);

	}
	
	/**
	 * 根据标号查询自动放款记录(不含失败已处理) WJW
	 * @param loanCode
	 * @return
	 */
	public List<JXTrace> queryAutoLendPayNotFailByLoanCode(String loanCode){
		String sql = "select * from t_jx_trace where txCode='autoLendPay' and requestMessage like '%"+loanCode+"%' and remark != 'y'";
		return JXTrace.jxTraceDao.find(sql);
	}
	
	/**
	 * 根据txCode分页查询列表 WJW
	 * @param pageNumber
	 * @param pageSize
	 * @param txCode
	 * @param allkey
	 * @return
	 */
	public Page<JXTrace> queryByPage(Integer pageNumber, Integer pageSize,String txCode, String allkey){
		String sqlSelect = "select *";
		String sqlFrom = " from t_jx_trace where txCode='"+txCode+"'";
		String sqlOrder = " order by txDate desc,txTime desc";
		
		if(!StringUtil.isBlank(allkey)){
			sqlFrom += " and requestMessage like '%"+allkey+"%'";
		}
		
		return JXTrace.jxTraceDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder);
	}
	/**
	 * 查询回款批次中单个标正在处理中的流水
	 * */
	public List<JXTrace> queryTraceByReturnAmountState(String date,String authCode){
		return JXTrace.jxTraceDao.find("select * from t_jx_trace where remark !='y' and requestMessage like '%"+authCode+"%' and txCode = 'batchSubstRepay' and txDate = ?",date);
	}
	
	/**
	 * 获取批次请求报文交易明细JSONArray WJW
	 * @param jxTrace
	 */
	public JSONArray getSubPacksJSONArray(JXTrace jxTrace){
		String requestMessage = jxTrace.getStr("requestMessage");
		JSONObject requestMessageJson = JSONObject.parseObject(requestMessage);
		String subPacks = requestMessageJson.getString("subPacks");
		return JSONArray.parseArray(subPacks);
	}
	
	/**
	 * 获取批次号 WJW
	 * @param jxTrace
	 * @return
	 */
	public String getBatchNo(JXTrace jxTrace){
		String requestMessage = jxTrace.getStr("requestMessage");
		JSONObject requestMessageJson = JSONObject.parseObject(requestMessage);
		return requestMessageJson.getString("batchNo");
	}
	
	/**
	 * 根据交易代码,交易日期,流水状态查询jxTrace WJW
	 * @param txDate		交易日期
	 * @param jxTraceStatus	jxTrace状态
	 * @param txCodes		交易代码可选多个
	 * @return
	 */
	public List<JXTrace> queryByTxCodeTxDateStatus(String txDate,String jxTraceStatus,String...txCodes){
		String sqlStart = "select * from t_jx_trace where txCode in (";
		String sqlTxCode = "";
		String sqlEnd = ") and txDate=? and jxTraceStatus=? order by txCode desc";
		for (String txCode : txCodes) {
			sqlTxCode += StringUtil.isBlank(sqlTxCode) ? "'"+txCode+"'":",'"+txCode+"'";
		}
		return JXTrace.jxTraceDao.find(sqlStart + sqlTxCode + sqlEnd,txDate,jxTraceStatus);
	}
	
	/**
	 * 根据交易代码和交易日期查询交易数量 WJW
	 * @param txCode
	 * @param txDate
	 * @return
	 */
	public long countByTxCodeAndTxDate(String txCode,String txDate){
		String sql = "select count(1) from t_jx_trace where txCode=? and txDate=?";
		return Db.queryLong(sql,txCode,txDate);
	}
	
	/**
	 * 批次代偿(交易金额单位:分) WJW
	 * 
	 * @param reqMap
	 * 			  通用参数
	 * @param batchNo
	 *            批次号(六位数字，当日不唯一)
	 * @param txAmount
	 *            交易金额
	 * @param retNotifyURL
	 * 			  业务通知结果地址
	 * @param jsonStr
	 *            accountId:风险准备金账号,txAmount:代偿本金,intAmount:交易利息,
	 *            txFeeIn:收款手续费,fineAmount:罚息金额,
	 *            forAccountId:投资人账号,productId:标号,authCode:授权码
	 * @return
	 */
	public boolean saveBatchSubstRepay(Map<String, String> reqMap,int batchNo, String retNotifyURL,String jsonStr,String remark) {
		reqMap.put("txCode", "batchSubstRepay");// 交易代码
		reqMap.put("batchNo", String.valueOf(batchNo));// 批次号
		reqMap.put("notifyURL", CommonUtil.NIUX_URL+"/notifyURL");// 后台通知链接
		reqMap.put("retNotifyURL", retNotifyURL);// 业务通知结果

		List<Map<String, String>> arrayList = new ArrayList<>();

		JSONArray array = JSONArray.parseArray(jsonStr);
		reqMap.put("txCounts", String.valueOf(array.size()));// 交易笔数

		long sumTxAmount = 0;//交易总金额
		for (int i = 0; i < array.size(); i++) {
			JSONObject jsonObject = array.getJSONObject(i);
			long txAmount = jsonObject.getLong("txAmount");
			sumTxAmount += txAmount;
			
			Map<String, String> map = new HashMap<>();
			map.put("accountId", jsonObject.getString("accountId"));// 风险准备金账号
			map.put("orderId", CommonUtil.genShortUID());// 订单号
			map.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 代偿本金
			map.put("intAmount", StringUtil.getMoneyYuan(jsonObject.getLong("intAmount")));// 交易利息
			map.put("txFeeIn", jsonObject.getLong("txFeeIn") != null ? StringUtil.getMoneyYuan(jsonObject.getLong("txFeeIn")):"0");// 收款手续费
			map.put("forAccountId", jsonObject.getString("forAccountId"));// 投资人账号
			map.put("productId", jsonObject.getString("productId"));// 标号
			map.put("authCode", jsonObject.getString("authCode"));// 授权码
			arrayList.add(map);
		}

		reqMap.put("txAmount", StringUtil.getMoneyYuan(sumTxAmount));// 交易金额
		reqMap.put("subPacks", JSONObject.toJSONString(arrayList));// 请求数组

		return JXService.saveRequestCommon(reqMap,remark);
	}
	
	/**
	 * 保存红包发放请求信息(交易金额单位:分) WJW
	 * 
	 * @param accountId
	 *            红包账号
	 * @param txAmount
	 *            红包金额
	 * @param forAccountId
	 *            接收方账号
	 * @param desLineFlag
	 *            是否使用交易描述(1-使用,0-不使用)
	 * @param desLine
	 *            交易描述
	 * @return
	 */
	public boolean saveVoucherPayRequest(String accountId, long txAmount, String forAccountId,
			String desLineFlag, String desLine,String remark) {
		Map<String, String> reqMap = new TreeMap<>();
		JXService.getHeadReq(reqMap);// 添加通用参数
		reqMap.put("txCode", "voucherPay");// 交易代码
		reqMap.put("accountId", accountId);// 红包账号
		reqMap.put("txAmount", StringUtil.getMoneyYuan(txAmount));// 红包金额
		reqMap.put("forAccountId", forAccountId);// 接收方账号
		reqMap.put("desLineFlag", desLineFlag);// 是否使用交易描述(1-使用,0-不使用)
		reqMap.put("desLine", desLine);// 交易描述
		return JXService.saveRequestCommon(reqMap,remark);
	}
	
	/**
	 * 构建批次还款明细 WJW
	 * @param jsonArray
	 * @param loanCode
	 * @param forAccountId
	 * @param txAmount
	 * @param intAmount
	 * @param txFeeIn
	 * @param authCode
	 * @param paymentAuthPageAmount
	 * @return
	 */
	public JSONArray batchSubstRepayJson(JSONArray jsonArray,String loanCode,String forAccountId,long txAmount,long intAmount,long txFeeIn,String authCode,long paymentAuthPageAmount){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("accountId", JXService.RISK_RESERVE);
		jsonObject.put("forAccountId", forAccountId);
		jsonObject.put("txAmount", txAmount);
		jsonObject.put("intAmount", paymentAuthPageAmount < txFeeIn ? (intAmount-txFeeIn):intAmount);
		jsonObject.put("txFeeIn", paymentAuthPageAmount < txFeeIn ? 0:txFeeIn);
		jsonObject.put("productId", loanCode);
		jsonObject.put("authCode", authCode);
		jsonArray.add(jsonObject);
		return jsonArray;
	}
	
	/**
	 * 根据日期和批次号查询jxTrace WJW
	 * @param txDate
	 * @param batchNo
	 * @return
	 */
	public JXTrace findByTxDateAndBatchNo(String txDate,String batchNo){
		String sql = "select * from t_jx_trace where txDate=? and batchNo=?";
		return JXTrace.jxTraceDao.findFirst(sql,txDate,batchNo);
	}
	
	/**
	 * 查询待解冻批次结束债权jxTrace WJW
	 * @return
	 */
	public List<JXTrace> queryThawBatchCreditInvest(){
		String sql = "select t1.jxTraceCode,t1.batchNo,t1.txDate from t_jx_trace t1 left join t_loan_transfer t2 on t1.jxTraceCode=t2.jxTraceCode where t1.txCode='batchCreditInvest' and t2.refundType in ('E','F','H') and t2.transState='A' and t2.jxTraceCode is not null group by t1.jxTraceCode";
		return JXTrace.jxTraceDao.find(sql);
	}
}
