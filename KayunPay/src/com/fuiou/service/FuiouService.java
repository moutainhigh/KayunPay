package com.fuiou.service;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.dutiantech.service.BaseService;
import com.fuiou.data.AppRegReqData;
import com.fuiou.data.AppSignCardReqData;
import com.fuiou.data.AppTransReqData;
import com.fuiou.data.ArtifRegReqData;
import com.fuiou.data.AuthConfigReqData;
import com.fuiou.data.CancelUserForPageReqData;
import com.fuiou.data.CashWithSetReqData;
import com.fuiou.data.CashWithSetRspData;
import com.fuiou.data.ChangeCard2ReqData;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.FreezeReqData;
import com.fuiou.data.ModifyMobileReqData;
import com.fuiou.data.PreAuthCancelReqData;
import com.fuiou.data.PreAuthReqData;
import com.fuiou.data.PreAuthRspData;
import com.fuiou.data.QueryBalanceReqData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.data.QueryBalanceRspData;
import com.fuiou.data.QueryChangeCardReqData;
import com.fuiou.data.QueryChangeCardRspData;
import com.fuiou.data.QueryCzTxReq;
import com.fuiou.data.QueryCzTxRspData;
import com.fuiou.data.QueryCzTxRspDetailData;
import com.fuiou.data.QueryDetail;
import com.fuiou.data.QueryOpResultSet;
import com.fuiou.data.QueryReqData;
import com.fuiou.data.QueryRspData;
import com.fuiou.data.QueryTxnReqData;
import com.fuiou.data.QueryTxnRspData;
import com.fuiou.data.QueryTxnRspDetailData;
import com.fuiou.data.QueryUserInfsReqData;
import com.fuiou.data.QueryUserInfsRspData;
import com.fuiou.data.QueryUserInfsRspDetailData;
import com.fuiou.data.QueryUserInfs_v2ReqData;
import com.fuiou.data.QueryUserInfs_v2RspData;
import com.fuiou.data.QueryUserInfs_v2RspDetailData;
import com.fuiou.data.RegReqData;
import com.fuiou.data.ResetPassWordReqData;
import com.fuiou.data.ReturnLoginReqData;
import com.fuiou.data.TransferBmuAndFreezeReqData;
import com.fuiou.data.TransferBmuReqData;
import com.fuiou.data.UnFreezeRspData;
import com.fuiou.data.WebArtifRegReqData;
import com.fuiou.data.WebRegReqData;
import com.fuiou.data.WtRechargeReqData;
import com.fuiou.data.WtRechargeRspData;
import com.fuiou.data.WtWithdrawReqData;
import com.fuiou.data.WtWithdrawRspData;
import com.fuiou.data.Wy500012ReqData;
import com.fuiou.data.P2p500405ReqData;
import com.fuiou.http.WebUtils;
import com.fuiou.util.ConfigReader;
import com.fuiou.util.Object2Xml;
import com.fuiou.util.SecurityUtils;
import com.fuiou.util.StringUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * 富友金账户系统请求Service
 * @author aj
 *
 */
public class FuiouService extends BaseService {
	
	/**
	 * 1.开户注册请求
	 * @return
	 * @throws Exception 
	 */
	public static CommonRspData reg(RegReqData reqData) throws Exception{
		if(reqData==null){
			throw new Exception("请求参数为空");
		}
		reqData.setSignature(SecurityUtils.sign(reqData.createSignValueForReg()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/reg.action", reqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	/**
	 *2. 明细查询接口
	 * @param queryReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryRspData query(QueryReqData queryReqData) throws Exception{
		if(queryReqData==null){
			throw new Exception("请求参数为空");
		}
		queryReqData.setSignature(SecurityUtils.sign(queryReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/query.action", queryReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryRspData.class);
		xstream.alias("opResult", QueryOpResultSet.class);
		xstream.alias("detail", QueryDetail.class);
		
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryRspData resultData = (QueryRspData) xstream.fromXML(xml);
		return resultData;
	}

	/**
	 * 3.余额查询
	 * @param queryBalanceReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryBalanceRspData balanceAction(QueryBalanceReqData queryBalanceReqData) throws Exception{
		if(queryBalanceReqData==null){
			throw new Exception("请求参数为空");
		}
		queryBalanceReqData.setSignature(SecurityUtils.sign(queryBalanceReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/BalanceAction.action", queryBalanceReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryBalanceRspData.class);
		xstream.alias("result", QueryBalanceResultData.class);
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryBalanceRspData resultData = (QueryBalanceRspData) xstream.fromXML(xml);
		return resultData;
	}
	
	/**
	 * 4.预授权
	 * @param preAuthReqData
	 * @return
	 * @throws Exception
	 */
	public static PreAuthRspData preAuth(PreAuthReqData preAuthReqData) throws Exception{
		if(preAuthReqData==null){
			throw new Exception("请求参数为空");
		}
		preAuthReqData.setSignature(SecurityUtils.sign(preAuthReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/preAuth.action", preAuthReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		PreAuthRspData rspData=(PreAuthRspData)Object2Xml.xml2object(plain, "plain", PreAuthRspData.class);
		return rspData;
	}
	
	/**
	 * 5.预授权撤销接口
	 * @param PreAuthCancelReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData preAuthCancel(PreAuthCancelReqData preAuthCancelReqData) throws Exception{
		if(preAuthCancelReqData==null){
			throw new Exception("请求参数为空");
		}
		preAuthCancelReqData.setSignature(SecurityUtils.sign(preAuthCancelReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/preAuthCancel.action", preAuthCancelReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	/**
	 * 6.转账(商户与个人之间)
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData transferBmu(TransferBmuReqData transferBmuReqData) throws Exception{
		if(transferBmuReqData==null){
			throw new Exception("请求参数为空");
		}
		transferBmuReqData.setSignature(SecurityUtils.sign(transferBmuReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/transferBmu.action", transferBmuReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	/**
	 * 7.划拨 (个人与个人之间)
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData transferBu(TransferBmuReqData transferBmuReqData) throws Exception{
		if(transferBmuReqData==null){
			throw new Exception("请求参数为空");
		}
		transferBmuReqData.setSignature(SecurityUtils.sign(transferBmuReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/transferBu.action", transferBmuReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	/**
	 * 8.用户信息查询接口
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryUserInfsRspData queryUserInfs(QueryUserInfsReqData queryUserInfsReqData) throws Exception{
		if(queryUserInfsReqData==null){
			throw new Exception("请求参数为空");
		}
		queryUserInfsReqData.setSignature(SecurityUtils.sign(queryUserInfsReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/queryUserInfs.action", queryUserInfsReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryUserInfsRspData.class);
		xstream.alias("result", QueryUserInfsRspDetailData.class);
		
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryUserInfsRspData resultData = (QueryUserInfsRspData) xstream.fromXML(xml);
		return resultData;
	}
	
	/**
	 *用户信息修改
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	/*public static CommonRspData modifyUserInf(ModifyUserInfReqData modifyUserInfReqData) throws Exception{
		if(modifyUserInfReqData==null){
			throw new Exception("请求参数为空");
		}
		modifyUserInfReqData.setSignature(SecurityUtils.sign(modifyUserInfReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/modifyUserInf.action", modifyUserInfReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}*/
	
	/**
	 * 9.个人用户自助开户注册（网页版）请求接口
	 */
	
	public static void webReg(WebRegReqData webRegReqData,HttpServletResponse response) throws Exception{
		if(webRegReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("ver", StringUtil.isEmpty(webRegReqData.getVer())?"":webRegReqData.getVer().trim());
		paramMap.put("mchnt_cd", StringUtil.isEmpty(webRegReqData.getMchnt_cd())?"":webRegReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(webRegReqData.getMchnt_txn_ssn())?"":webRegReqData.getMchnt_txn_ssn().trim());
		paramMap.put("user_id_from", StringUtil.isEmpty(webRegReqData.getUser_id_from())?"":webRegReqData.getUser_id_from().trim());
		paramMap.put("mobile_no", StringUtil.isEmpty(webRegReqData.getMobile_no())?"":webRegReqData.getMobile_no().trim());
		paramMap.put("cust_nm", StringUtil.isEmpty(webRegReqData.getCust_nm())?"":webRegReqData.getCust_nm().trim());
		paramMap.put("certif_tp", StringUtil.isEmpty(webRegReqData.getCertif_tp())?"":webRegReqData.getCertif_tp().trim());
		paramMap.put("certif_id", StringUtil.isEmpty(webRegReqData.getCertif_id())?"":webRegReqData.getCertif_id().trim());
		paramMap.put("email", StringUtil.isEmpty(webRegReqData.getEmail())?"":webRegReqData.getEmail().trim());
		paramMap.put("city_id", StringUtil.isEmpty(webRegReqData.getCity_id())?"":webRegReqData.getCity_id().trim());
		paramMap.put("parent_bank_id", StringUtil.isEmpty(webRegReqData.getParent_bank_id())?"":webRegReqData.getParent_bank_id().trim());
		paramMap.put("bank_nm", StringUtil.isEmpty(webRegReqData.getBank_nm())?"":webRegReqData.getBank_nm().trim());
		paramMap.put("capAcntNo", StringUtil.isEmpty(webRegReqData.getCapAcntNo())?"":webRegReqData.getCapAcntNo().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(webRegReqData.getPage_notify_url())?"":webRegReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(webRegReqData.getBack_notify_url())?"":webRegReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(webRegReqData.createSignValueForReg()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/webReg.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 10.商户端个人用户跳转登录页面（网页版）
	 */
	
	public static void webLogin(ReturnLoginReqData returnLoginReqData,HttpServletResponse response) throws Exception{
		if(returnLoginReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(returnLoginReqData.getMchnt_cd())?"":returnLoginReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(returnLoginReqData.getMchnt_txn_ssn())?"":returnLoginReqData.getMchnt_txn_ssn().trim());
		paramMap.put("cust_no", StringUtil.isEmpty(returnLoginReqData.getCust_no())?"":returnLoginReqData.getCust_no().trim());
		paramMap.put("location", StringUtil.isEmpty(returnLoginReqData.getLocation())?"":returnLoginReqData.getLocation().trim());
		paramMap.put("amt", StringUtil.isEmpty(returnLoginReqData.getAmt())?"":returnLoginReqData.getAmt().trim());
		paramMap.put("signature", SecurityUtils.sign(returnLoginReqData.createSignValueFor()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/webLogin.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	
	/**
	 * 12.冻结
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData freeze(FreezeReqData freezeReqData) throws Exception{
		if(freezeReqData==null){
			throw new Exception("请求参数为空");
		}
		freezeReqData.setSignature(SecurityUtils.sign(freezeReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/freeze.action", freezeReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	
	/**
	 * 14.	解冻（12，13，14的冻结资金解冻）
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static UnFreezeRspData unFreeze(FreezeReqData freezeReqData) throws Exception{
		if(freezeReqData==null){
			throw new Exception("请求参数为空");
		}
		freezeReqData.setSignature(SecurityUtils.sign(freezeReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/unFreeze.action", freezeReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		UnFreezeRspData rspData=(UnFreezeRspData)Object2Xml.xml2object(plain, "plain", UnFreezeRspData.class);
		return rspData;
	}
	
	/**
	 * 13.转账预冻结
	 * @param TransferBmuReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData transferBmuAndFreeze(TransferBmuAndFreezeReqData bmuAndFreezeReqData) throws Exception{
		if(bmuAndFreezeReqData==null){
			throw new Exception("请求参数为空");
		}
		bmuAndFreezeReqData.setSignature(SecurityUtils.sign(bmuAndFreezeReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/transferBmuAndFreeze.action", bmuAndFreezeReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	
	/**
	 *13. 划拨预冻结
	 * @param bmuAndFreezeReqData
	 * @return
	 * @throws Exception
	 */
	public static CommonRspData transferBuAndFreeze(TransferBmuAndFreezeReqData bmuAndFreezeReqData) throws Exception{
		if(bmuAndFreezeReqData==null){
			throw new Exception("请求参数为空");
		}
		bmuAndFreezeReqData.setSignature(SecurityUtils.sign(bmuAndFreezeReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/transferBuAndFreeze.action", bmuAndFreezeReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	/**
	 * 19.交易查询接口
	 * @param bmuAndFreezeReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryTxnRspData queryTxn(QueryTxnReqData queryTxnReqData) throws Exception{
		if(queryTxnReqData==null){
			throw new Exception("请求参数为空");
		}
		queryTxnReqData.setSignature(SecurityUtils.sign(queryTxnReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/queryTxn.action", queryTxnReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryTxnRspData.class);
		xstream.alias("result", QueryTxnRspDetailData.class);
		
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryTxnRspData resultData = (QueryTxnRspData) xstream.fromXML(xml);
		return resultData;
	}
	
	/**
	 * 20.冻结到冻结接口
	 * @param bmuAndFreezeReqData
	 * @return
	 * @throws Exception
	 */
	public static UnFreezeRspData transferBuAndFreeze2Freeze(TransferBmuAndFreezeReqData bmuAndFreezeReqData) throws Exception{
		if(bmuAndFreezeReqData==null){
			throw new Exception("请求参数为空");
		}
		bmuAndFreezeReqData.setSignature(SecurityUtils.sign(bmuAndFreezeReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/transferBuAndFreeze2Freeze.action", bmuAndFreezeReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		UnFreezeRspData resultData = (UnFreezeRspData) Object2Xml.xml2object(plain, "plain", UnFreezeRspData.class);
		return resultData;
	}
	
	/**
	 * 21.充值提现查询接口
	 * @param bmuAndFreezeReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryCzTxRspData querycztx(QueryCzTxReq queryCzTxReq) throws Exception{
		if(queryCzTxReq==null){
			throw new Exception("请求参数为空");
		}
		queryCzTxReq.setSignature(SecurityUtils.sign(queryCzTxReq.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/querycztx.action", queryCzTxReq);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryCzTxRspData.class);
		xstream.alias("result", QueryCzTxRspDetailData.class);
		
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryCzTxRspData resultData = (QueryCzTxRspData) xstream.fromXML(xml);
		return resultData;
	}
	
	/**
	 * 23.法人开户注册请求
	 * @return
	 * @throws Exception 
	 */
	public static CommonRspData artifReg(ArtifRegReqData reqData) throws Exception{
		if(reqData==null){
			throw new Exception("请求参数为空");
		}
		reqData.setSignature(SecurityUtils.sign(reqData.createSignValueForReg()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/artifReg.action", reqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CommonRspData rspData=(CommonRspData)Object2Xml.xml2object(plain, "plain", CommonRspData.class);
		return rspData;
	}
	
	
	/**
	 * 	34.	用户信息查询接口（目前只支持个人用户）
	 * @param queryTxnReqData
	 * @return
	 * @throws Exception
	 */
	public static QueryUserInfs_v2RspData queryUserInfs_v2(QueryUserInfs_v2ReqData queryUserInfs_v2ReqData) throws Exception{
		if(queryUserInfs_v2ReqData==null){
			throw new Exception("请求参数为空");
		}
		queryUserInfs_v2ReqData.setSignature(SecurityUtils.sign(queryUserInfs_v2ReqData.createSearchSigature_v2()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/queryUserInfs_v2.action", queryUserInfs_v2ReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		String xml="<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+plain;
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("plain", QueryUserInfs_v2RspData.class);
		xstream.alias("result", QueryUserInfs_v2RspDetailData.class);
		
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryUserInfs_v2RspData resultData = (QueryUserInfs_v2RspData) xstream.fromXML(xml);
		return resultData;
	}
	
	/**
	 * 35.	APP免登签约
	 * @param args
	 */
	public static void appSignCard(AppSignCardReqData appSignCardReqData,HttpServletResponse response) throws Exception{
		if(appSignCardReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appSignCardReqData.getMchnt_cd())?"":appSignCardReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appSignCardReqData.getMchnt_txn_ssn())?"":appSignCardReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appSignCardReqData.getLogin_id())?"":appSignCardReqData.getLogin_id().trim());
		paramMap.put("mobile", StringUtil.isEmpty(appSignCardReqData.getMobile())?"":appSignCardReqData.getMobile().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appSignCardReqData.getPage_notify_url())?"":appSignCardReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appSignCardReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/appSign_card.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 	36.	P2P免登录直接跳转网银界面充值接口
	 */
	public static void wy500012(Wy500012ReqData wy500012ReqData,HttpServletResponse response) throws Exception{
		if(wy500012ReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(wy500012ReqData.getMchnt_cd())?"":wy500012ReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(wy500012ReqData.getMchnt_txn_ssn())?"":wy500012ReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(wy500012ReqData.getLogin_id())?"":wy500012ReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(wy500012ReqData.getAmt())?"":wy500012ReqData.getAmt().trim());
		paramMap.put("order_pay_type", StringUtil.isEmpty(wy500012ReqData.getOrder_pay_type())?"":wy500012ReqData.getOrder_pay_type().trim());
		paramMap.put("iss_ins_cd", StringUtil.isEmpty(wy500012ReqData.getIss_ins_cd())?"":wy500012ReqData.getIss_ins_cd().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(wy500012ReqData.getPage_notify_url())?"":wy500012ReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(wy500012ReqData.getBack_notify_url())?"":wy500012ReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(wy500012ReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/500012.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 37.	用户申请注销免登陆接口(网页版)
	 */
	public static void cancelUserForPage(CancelUserForPageReqData cancelUserForPageReqData,HttpServletResponse response) throws Exception{
		if(cancelUserForPageReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(cancelUserForPageReqData.getMchnt_cd())?"":cancelUserForPageReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(cancelUserForPageReqData.getMchnt_txn_ssn())?"":cancelUserForPageReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(cancelUserForPageReqData.getLogin_id())?"":cancelUserForPageReqData.getLogin_id().trim());
		paramMap.put("signature", SecurityUtils.sign(cancelUserForPageReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/cancelUserForPage.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *39.个人PCP端更换手机号
	 */
	public static void p2p400101(ModifyMobileReqData modifyMobileReqData,HttpServletResponse response) throws Exception{
		if(modifyMobileReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(modifyMobileReqData.getMchnt_cd())?"":modifyMobileReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(modifyMobileReqData.getMchnt_txn_ssn())?"":modifyMobileReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(modifyMobileReqData.getLogin_id())?"":modifyMobileReqData.getLogin_id().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(modifyMobileReqData.getPage_notify_url())?"":modifyMobileReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(modifyMobileReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/400101.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *39.APP端更换手机号
	 */
	public static void app400101(ModifyMobileReqData modifyMobileReqData,HttpServletResponse response) throws Exception{
		if(modifyMobileReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(modifyMobileReqData.getMchnt_cd())?"":modifyMobileReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(modifyMobileReqData.getMchnt_txn_ssn())?"":modifyMobileReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(modifyMobileReqData.getLogin_id())?"":modifyMobileReqData.getLogin_id().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(modifyMobileReqData.getPage_notify_url())?"":modifyMobileReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(modifyMobileReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/400101.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 41. 用户更换银行卡查询接口
	 * @param args
	 */
	public static QueryChangeCardRspData queryChangeCard(QueryChangeCardReqData queryChangeCardReqData) throws Exception{
		if(queryChangeCardReqData==null){
			throw new Exception("请求参数为空");
		}
		queryChangeCardReqData.setSignature(SecurityUtils.sign(queryChangeCardReqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/queryChangeCard.action", queryChangeCardReqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		QueryChangeCardRspData rspData=(QueryChangeCardRspData)Object2Xml.xml2object(plain, "plain", QueryChangeCardRspData.class);
		return rspData;
	}
	
	/**
	 * 42. 商户P2P网站免登录用户更换银行卡接口
	 * @param args
	 */
	public static void changeCard2(ChangeCard2ReqData changeCard2ReqData,HttpServletResponse response) throws Exception{
		if(changeCard2ReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(changeCard2ReqData.getMchnt_cd())?"":changeCard2ReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(changeCard2ReqData.getMchnt_txn_ssn())?"":changeCard2ReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(changeCard2ReqData.getLogin_id())?"":changeCard2ReqData.getLogin_id().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(changeCard2ReqData.getPage_notify_url())?"":changeCard2ReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(changeCard2ReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/changeCard2.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 17.	手机端APP个人用户自助开户注册（APP网页版）
	 * @param args
	 */
	public static void appWebReg(AppRegReqData appRegReqData,HttpServletResponse response) throws Exception{
		if(appRegReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("ver", StringUtil.isEmpty(appRegReqData.getVer())?"":appRegReqData.getVer().trim());
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appRegReqData.getMchnt_cd())?"":appRegReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appRegReqData.getMchnt_txn_ssn())?"":appRegReqData.getMchnt_txn_ssn().trim());
		paramMap.put("user_id_from", StringUtil.isEmpty(appRegReqData.getUser_id_from())?"":appRegReqData.getUser_id_from().trim());
		paramMap.put("mobile_no", StringUtil.isEmpty(appRegReqData.getMobile_no())?"":appRegReqData.getMobile_no().trim());
		paramMap.put("cust_nm", StringUtil.isEmpty(appRegReqData.getCust_nm())?"":appRegReqData.getCust_nm().trim());
		paramMap.put("certif_tp", StringUtil.isEmpty(appRegReqData.getCertif_tp())?"":appRegReqData.getCertif_tp().trim());
		paramMap.put("certif_id", StringUtil.isEmpty(appRegReqData.getCertif_id())?"":appRegReqData.getCertif_id().trim());
		paramMap.put("email", StringUtil.isEmpty(appRegReqData.getEmail())?"":appRegReqData.getEmail().trim());
		paramMap.put("city_id", StringUtil.isEmpty(appRegReqData.getCity_id())?"":appRegReqData.getCity_id().trim());
		paramMap.put("parent_bank_id", StringUtil.isEmpty(appRegReqData.getParent_bank_id())?"":appRegReqData.getParent_bank_id().trim());
		paramMap.put("bank_nm", StringUtil.isEmpty(appRegReqData.getBank_nm())?"":appRegReqData.getBank_nm().trim());
		paramMap.put("capAcntNo", StringUtil.isEmpty(appRegReqData.getCapAcntNo())?"":appRegReqData.getCapAcntNo().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appRegReqData.getPage_notify_url())?"":appRegReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appRegReqData.getBack_notify_url())?"":appRegReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appRegReqData.createSignValueForReg()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/appWebReg.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 22.	商户手机端APP个人用户跳转登录页面（APP网页版）
	 */
	
	public static void appWebLogin(ReturnLoginReqData returnLoginReqData,HttpServletResponse response) throws Exception{
		if(returnLoginReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(returnLoginReqData.getMchnt_cd())?"":returnLoginReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(returnLoginReqData.getMchnt_txn_ssn())?"":returnLoginReqData.getMchnt_txn_ssn().trim());
		paramMap.put("cust_no", StringUtil.isEmpty(returnLoginReqData.getCust_no())?"":returnLoginReqData.getCust_no().trim());
		paramMap.put("location", StringUtil.isEmpty(returnLoginReqData.getLocation())?"":returnLoginReqData.getLocation().trim());
		paramMap.put("amt", StringUtil.isEmpty(returnLoginReqData.getAmt())?"":returnLoginReqData.getAmt().trim());
		paramMap.put("signature", SecurityUtils.sign(returnLoginReqData.createSignValueFor()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/appWebLogin.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *24.	法人用户自助开户注册（网页版）
	 */
	
	public static void webArtifReg(WebArtifRegReqData webArtifRegReqData,HttpServletResponse response) throws Exception{
		if(webArtifRegReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("ver", StringUtil.isEmpty(webArtifRegReqData.getVer())?"":webArtifRegReqData.getVer().trim());
		paramMap.put("mchnt_cd", StringUtil.isEmpty(webArtifRegReqData.getMchnt_cd())?"":webArtifRegReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(webArtifRegReqData.getMchnt_txn_ssn())?"":webArtifRegReqData.getMchnt_txn_ssn().trim());
		paramMap.put("user_id_from", StringUtil.isEmpty(webArtifRegReqData.getUser_id_from())?"":webArtifRegReqData.getUser_id_from().trim());
		paramMap.put("cust_nm", StringUtil.isEmpty(webArtifRegReqData.getCust_nm())?"":webArtifRegReqData.getCust_nm().trim());
		paramMap.put("artif_nm", StringUtil.isEmpty(webArtifRegReqData.getArtif_nm())?"":webArtifRegReqData.getArtif_nm().trim());
		paramMap.put("mobile_no", StringUtil.isEmpty(webArtifRegReqData.getMobile_no())?"":webArtifRegReqData.getMobile_no().trim());
		paramMap.put("certif_id", StringUtil.isEmpty(webArtifRegReqData.getCertif_id())?"":webArtifRegReqData.getCertif_id().trim());
		paramMap.put("email", StringUtil.isEmpty(webArtifRegReqData.getEmail())?"":webArtifRegReqData.getEmail().trim());
		paramMap.put("city_id", StringUtil.isEmpty(webArtifRegReqData.getCity_id())?"":webArtifRegReqData.getCity_id().trim());
		paramMap.put("parent_bank_id", StringUtil.isEmpty(webArtifRegReqData.getParent_bank_id())?"":webArtifRegReqData.getParent_bank_id().trim());
		paramMap.put("bank_nm", StringUtil.isEmpty(webArtifRegReqData.getBank_nm())?"":webArtifRegReqData.getBank_nm().trim());
		paramMap.put("capAcntNo", StringUtil.isEmpty(webArtifRegReqData.getCapAcntNo())?"":webArtifRegReqData.getCapAcntNo().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(webArtifRegReqData.getPage_notify_url())?"":webArtifRegReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(webArtifRegReqData.getBack_notify_url())?"":webArtifRegReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(webArtifRegReqData.createSignValueForReg()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/webArtifReg.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *25.	商户APP个人用户免登录快速充值
	 */
	
	public static void app500001(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/500001.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *26.	商户APP个人用户免登录快捷充值
	 */
	
	public static void app500002(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/500002.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *27.	商户APP个人用户免登录提现
	 */
	
	public static void app500003(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/500003.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *28.	商户P2P网站免登录快速充值接口
	 */
	
	public static void p2p500001(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/500001.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *29.	商户P2P网站免登录网银充值接口
	 */
	
	public static void p2p500002(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/500002.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *30.	商户P2P网站免登录提现接口
	 */
	
	public static void p2p500003(AppTransReqData appTransReqData,HttpServletResponse response) throws Exception{
		if(appTransReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(appTransReqData.getMchnt_cd())?"":appTransReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(appTransReqData.getMchnt_txn_ssn())?"":appTransReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(appTransReqData.getLogin_id())?"":appTransReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(appTransReqData.getAmt())?"":appTransReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(appTransReqData.getPage_notify_url())?"":appTransReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(appTransReqData.getBack_notify_url())?"":appTransReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(appTransReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/500003.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *33.	用户密码修改重置免登陆接口(网页版)
	 */
	
	public static void resetPassWord(ResetPassWordReqData resetPassWordReqData,HttpServletResponse response) throws Exception{
		if(resetPassWordReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(resetPassWordReqData.getMchnt_cd())?"":resetPassWordReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(resetPassWordReqData.getMchnt_txn_ssn())?"":resetPassWordReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(resetPassWordReqData.getLogin_id())?"":resetPassWordReqData.getLogin_id().trim());
		paramMap.put("busi_tp", StringUtil.isEmpty(resetPassWordReqData.getBusi_tp())?"":resetPassWordReqData.getBusi_tp().trim());
		paramMap.put("back_url", StringUtil.isEmpty(resetPassWordReqData.getBack_url())?"":resetPassWordReqData.getBack_url().trim());
		paramMap.put("signature", SecurityUtils.sign(resetPassWordReqData.createSignValueFor()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/resetPassWord.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *38.	用户密码修改重置免登陆接口(app版)
	 */
	
	public static void appResetPassWord(ResetPassWordReqData resetPassWordReqData,HttpServletResponse response) throws Exception{
		if(resetPassWordReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(resetPassWordReqData.getMchnt_cd())?"":resetPassWordReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(resetPassWordReqData.getMchnt_txn_ssn())?"":resetPassWordReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(resetPassWordReqData.getLogin_id())?"":resetPassWordReqData.getLogin_id().trim());
		paramMap.put("busi_tp", StringUtil.isEmpty(resetPassWordReqData.getBusi_tp())?"":resetPassWordReqData.getBusi_tp().trim());
		paramMap.put("back_url", StringUtil.isEmpty(resetPassWordReqData.getBack_url())?"":resetPassWordReqData.getBack_url().trim());
		paramMap.put("signature", SecurityUtils.sign(resetPassWordReqData.createSignValueFor()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/appResetPassWord.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 设置用户提现模式请求
	 * @return
	 * @throws Exception 
	 */
	public static CashWithSetRspData cashWithSetReq(CashWithSetReqData reqData) throws Exception{
		if(reqData==null){
			throw new Exception("请求参数为空");
		}
		reqData.setSignature(SecurityUtils.sign(reqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/cashWithSetReq.action", reqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		CashWithSetRspData rspData=(CashWithSetRspData)Object2Xml.xml2object(plain, "plain", CashWithSetRspData.class);
		return rspData;
	}
	
	/**
	 * 委托充值
	 * @param args
	 */
	public static WtRechargeRspData wtrechargeReq(WtRechargeReqData reqData) throws Exception{
		if(reqData==null){
			throw new Exception("请求参数为空");
		}
		reqData.setSignature(SecurityUtils.sign(reqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/wtrecharge.action", reqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		WtRechargeRspData rspData=(WtRechargeRspData)Object2Xml.xml2object(plain, "plain", WtRechargeRspData.class);
		return rspData;
	}
	
	/**
	 * 委托提现
	 * @param args
	 */
	public static WtWithdrawRspData wtwithdrawReq(WtWithdrawReqData reqData) throws Exception{
		if(reqData==null){
			throw new Exception("请求参数为空");
		}
		reqData.setSignature(SecurityUtils.sign(reqData.createSignValue()));
		String result = WebUtils.sendHttp(ConfigReader.getConfig("jzhUrl")+"/wtwithdraw.action", reqData);
		if(StringUtils.isEmpty(result)){
			throw new Exception("返回报文为空");
		}
		String plain=result.substring(result.indexOf("<plain>"), result.indexOf("<signature>"));
		String signature = Object2Xml.getByTag(result,"signature");
		boolean signVal = SecurityUtils.verifySign(plain, signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		WtWithdrawRspData rspData=(WtWithdrawRspData)Object2Xml.xml2object(plain, "plain", WtWithdrawRspData.class);
		return rspData;
	}
	/**
	 * PC端个人用户免登录快捷充值
	 * @param appTransReqData
	 * @param response
	 * @throws Exception
	 */
	
	public static void p2p500405(P2p500405ReqData p2p500405ReqData,HttpServletResponse response) throws Exception{
		if(p2p500405ReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(p2p500405ReqData.getMchnt_cd())?"":p2p500405ReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(p2p500405ReqData.getMchnt_txn_ssn())?"":p2p500405ReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(p2p500405ReqData.getLogin_id())?"":p2p500405ReqData.getLogin_id().trim());
		paramMap.put("amt", StringUtil.isEmpty(p2p500405ReqData.getAmt())?"":p2p500405ReqData.getAmt().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(p2p500405ReqData.getPage_notify_url())?"":p2p500405ReqData.getPage_notify_url().trim());
		paramMap.put("back_notify_url", StringUtil.isEmpty(p2p500405ReqData.getBack_notify_url())?"":p2p500405ReqData.getBack_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(p2p500405ReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/500405.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * PC金账户免登陆授权配置（短信通知+委托交易）
	 * @param authConfigReqData
	 * @param response
	 * @throws Exception
	 */
	public static void authConfig(AuthConfigReqData authConfigReqData,HttpServletResponse response) throws Exception{
		if(authConfigReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(authConfigReqData.getMchnt_cd())?"":authConfigReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(authConfigReqData.getMchnt_txn_ssn())?"":authConfigReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(authConfigReqData.getLogin_id())?"":authConfigReqData.getLogin_id().trim());
		paramMap.put("busi_tp", StringUtil.isEmpty(authConfigReqData.getBusi_tp())?"":authConfigReqData.getBusi_tp().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(authConfigReqData.getPage_notify_url())?"":authConfigReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(authConfigReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/authConfig.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * app金账户免登陆授权配置（短信通知+委托交易）
	 * @param authConfigReqData
	 * @param response
	 * @throws Exception
	 */
	public static void appAuthConfig(AuthConfigReqData authConfigReqData,HttpServletResponse response) throws Exception{
		if(authConfigReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(authConfigReqData.getMchnt_cd())?"":authConfigReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(authConfigReqData.getMchnt_txn_ssn())?"":authConfigReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(authConfigReqData.getLogin_id())?"":authConfigReqData.getLogin_id().trim());
		paramMap.put("busi_tp", StringUtil.isEmpty(authConfigReqData.getBusi_tp())?"":authConfigReqData.getBusi_tp().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(authConfigReqData.getPage_notify_url())?"":authConfigReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(authConfigReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/authConfig.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 *  商户APP免登录用户更换银行卡接口
	 * @param args
	 */
	public static void appChangeCard(ChangeCard2ReqData changeCard2ReqData,HttpServletResponse response) throws Exception{
		if(changeCard2ReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(changeCard2ReqData.getMchnt_cd())?"":changeCard2ReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(changeCard2ReqData.getMchnt_txn_ssn())?"":changeCard2ReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(changeCard2ReqData.getLogin_id())?"":changeCard2ReqData.getLogin_id().trim());
		paramMap.put("page_notify_url", StringUtil.isEmpty(changeCard2ReqData.getPage_notify_url())?"":changeCard2ReqData.getPage_notify_url().trim());
		paramMap.put("signature", SecurityUtils.sign(changeCard2ReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/appChangeCard.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	/**
	 * 用户APP申请注销免登陆接口
	 */
	public static void appCancelUserForPage(CancelUserForPageReqData cancelUserForPageReqData,HttpServletResponse response) throws Exception{
		if(cancelUserForPageReqData==null){
			throw new Exception("请求参数为空");
		}
		Map<String,String> paramMap = new HashMap<String, String>();
		paramMap.put("mchnt_cd", StringUtil.isEmpty(cancelUserForPageReqData.getMchnt_cd())?"":cancelUserForPageReqData.getMchnt_cd().trim());
		paramMap.put("mchnt_txn_ssn", StringUtil.isEmpty(cancelUserForPageReqData.getMchnt_txn_ssn())?"":cancelUserForPageReqData.getMchnt_txn_ssn().trim());
		paramMap.put("login_id", StringUtil.isEmpty(cancelUserForPageReqData.getLogin_id())?"":cancelUserForPageReqData.getLogin_id().trim());
		paramMap.put("signature", SecurityUtils.sign(cancelUserForPageReqData.createSignValue()));
		String result = WebUtils.genForwardHtml(ConfigReader.getConfig("jzhUrl")+"/app/cancelUserForPage.action", paramMap, "utf-8");
		OutputStream out = response.getOutputStream();
		out.write(result.getBytes("utf-8"));
		out.flush();
	}
	
	
	public static void main(String[] args) {
		WtRechargeReqData cz = new WtRechargeReqData();
		cz.setMchnt_cd("0002900F0096235");
		cz.setMchnt_txn_ssn("1111111111111111");
		cz.setLogin_id("18612340000");
		cz.setAmt("100");
		cz.setBack_notify_url("http://192.168.8.29:29058/jzh_test/back_notify_url.action");
		WtRechargeRspData result;
		try {
			result = FuiouService.wtrechargeReq(cz);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		WtWithdrawReqData tx = new WtWithdrawReqData();
		tx.setMchnt_cd("0002900F0096235");
		tx.setMchnt_txn_ssn("1111111111111111");
		tx.setLogin_id("18612340000");
		tx.setAmt("100");
		tx.setBack_notify_url("http://192.168.8.29:29058/jzh_test/back_notify_url.action");
		WtWithdrawRspData result1;
		try {
			result1 = FuiouService.wtwithdrawReq(tx);
			System.out.println(result1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		//委托充值 ，委托提现  回调
		/*HttpServletRequest req=ServletActionContext.getRequest();
		WtRechargeAndWtWithdrawalRspData RspData  = FuiouRspParseService.wtrechargeAndwtWithdrawalNotifyParse(req);
		System.out.println(RspData);*/
		
		
		/*CashWithSetReqData queryCzTxReq = new CashWithSetReqData();
		queryCzTxReq.setMchnt_cd("0002900F0096235");
		queryCzTxReq.setLogin_id("18612340000");
		queryCzTxReq.setCash_way("1");
		queryCzTxReq.setMchnt_txn_ssn("1111111111111111");
		CashWithSetRspData result;
		try {
			result = FuiouService.cashWithSetReq(queryCzTxReq);
			System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
