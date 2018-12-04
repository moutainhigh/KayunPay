package com.fuiou.service;


import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import com.fuiou.data.AppSignCardRspData;
import com.fuiou.data.AppTransRspData;
import com.fuiou.data.AuthConfigReqData;
import com.fuiou.data.AuthConfigRspData;
import com.fuiou.data.CancelUserForPageRspData;
import com.fuiou.data.ChangeCard2RspData;
import com.fuiou.data.ModifyMobileRsqData;
import com.fuiou.data.P2p500405RspData;
import com.fuiou.data.RechargeAndWithdrawalRspData;
import com.fuiou.data.ResetPassWordReqData;
import com.fuiou.data.TransRspData;
import com.fuiou.data.TxTpBackMchntRspData;
import com.fuiou.data.UserModNotifyRsp;
import com.fuiou.data.WebArtifRegRspData;
import com.fuiou.data.WebRegRspData;
import com.fuiou.data.WtRechargeAndWtWithdrawalRspData;
import com.fuiou.data.Wy500012RspData;
import com.fuiou.data.ZxRspData;
import com.fuiou.util.SecurityUtils;

/**
 * 网页版数据解析
 * @author aj
 *
 */
public class FuiouRspParseService {
	
	/**
	 * 
	 * 9.个人用户自助开户注册
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static WebRegRspData webRegRspParse(HttpServletRequest req) throws Exception{
		WebRegRspData regRspData = new WebRegRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	
	/**
	 * 16,17:充值通知接口
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static RechargeAndWithdrawalRspData rechargeAndWithdrawalNotifyParse(HttpServletRequest req) throws Exception{
		RechargeAndWithdrawalRspData rspData = new RechargeAndWithdrawalRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	/**
	 * 
	 * 18.手机端APP个人用户自助开户注册（APP网页版）
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static WebRegRspData appWebRegRspParse(HttpServletRequest req) throws Exception{
		WebRegRspData regRspData = new WebRegRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 
	 * 19.交易通知接口（限接口方式请求的交易）
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static TransRspData transRspParse(HttpServletRequest req) throws Exception{
		TransRspData rspData = new TransRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	

	/**
	 * 
	 * 25.法人用户自助开户注册
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static WebArtifRegRspData webArtifRegRspParse(HttpServletRequest req) throws Exception{
		WebArtifRegRspData regRspData = new WebArtifRegRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 
	 * 30商户P2P网站免登录网银充值接口
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static AppTransRspData appWebRechargeRspParse(HttpServletRequest req) throws Exception{
		AppTransRspData regRspData = new AppTransRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createWebRechargeSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	
	/**
	 * 
	 * app免登录交易返回(接口25,26,27,28,30)
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static AppTransRspData appTransRspParse(HttpServletRequest req) throws Exception{
		AppTransRspData regRspData = new AppTransRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 
	 * 32.用户修改信息通知接口
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static UserModNotifyRsp userModNotifyParse(HttpServletRequest req) throws Exception{
		UserModNotifyRsp regRspData = new UserModNotifyRsp();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean grSignVal = SecurityUtils.verifySign(regRspData.createSignValueForWebRegBack(), signature);
		boolean frSignVal = SecurityUtils.verifySign(regRspData.createSignValueForWebArtifRegBack(), signature);
		if(!grSignVal&&!frSignVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 32.用户注销通知接口
	 * @return
	 * @throws Exception 
	 */
	public static ZxRspData zxRspParse(HttpServletRequest req) throws Exception{
		ZxRspData rspData = new ZxRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	
	/**
	 * 
	 * 33.用户密码修改重置免登陆接口(网页版)
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	
	/**
	 * 36.P2P免登录直接跳转网银界面充值接口
	 */
	public static Wy500012RspData wy500012RspParse(HttpServletRequest req) throws Exception{
		Wy500012RspData regRspData = new Wy500012RspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 39.个人PC/APP端更换手机号
	 */
	public static ModifyMobileRsqData modifyMobileRspParse(HttpServletRequest req) throws Exception{
		ModifyMobileRsqData regRspData = new ModifyMobileRsqData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 40.提现退票通知接口
	 */
	public static TxTpBackMchntRspData txTpBackMchntRspParse(HttpServletRequest req) throws Exception{
		TxTpBackMchntRspData rspData = new TxTpBackMchntRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	
	/**
	 * 
	 * 商户P2P网站免登录用户更换银行卡接口回调
	 */
	public static ChangeCard2RspData changeCard2RspDataRspParse(HttpServletRequest req) throws Exception{
		ChangeCard2RspData regRspData = new ChangeCard2RspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(regRspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(regRspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return regRspData;
	}
	
	/**
	 * 
	 * 35.	APP免登签约接口回调
	 */
	public static AppSignCardRspData appSignCardRspParse(HttpServletRequest req) throws Exception{
		AppSignCardRspData rspData = new AppSignCardRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	/**
	 * 委托充值 和委托提现接口回调
	 * @param req
	 * @return
	 * @throws Exception
	 */
	public static WtRechargeAndWtWithdrawalRspData wtrechargeAndwtWithdrawalNotifyParse(HttpServletRequest req) throws Exception{
		WtRechargeAndWtWithdrawalRspData rspData = new WtRechargeAndWtWithdrawalRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	/**
	 * 
	 * PC端个人用户免登录快捷充值返回
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static P2p500405RspData p2p500405RspParse(HttpServletRequest req) throws Exception{
		P2p500405RspData rspData = new P2p500405RspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	/**
	 * 
	 * PC金账户免登陆授权配置（短信通知+委托交易）返回
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static AuthConfigRspData authConfigRspParse(HttpServletRequest req) throws Exception{
		AuthConfigRspData rspData = new AuthConfigRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	/**
	 * 
	 * 用户申请注销免登陆接口(网页版)返回
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	public static CancelUserForPageRspData cancelUserForPageRspParse(HttpServletRequest req) throws Exception{
		CancelUserForPageRspData rspData = new CancelUserForPageRspData();
		String signature = req.getParameter("signature");
		BeanUtils.populate(rspData, req.getParameterMap());
		boolean signVal = SecurityUtils.verifySign(rspData.createSignValue(), signature);
		if(!signVal){
			throw new Exception("接口返回签名错误!");
		}
		return rspData;
	}
	
	
	
	/**
	 * 金账户交易通知返回XML
	 * @param resp_code
	 * @param mchnt_cd
	 * @param mchnt_txn_ssn
	 * @return
	 * @throws Exception 
	 */
	public static String notifyRspXml(String resp_code,String mchnt_cd,String mchnt_txn_ssn) throws Exception{
		if(StringUtils.isEmpty(resp_code)||StringUtils.isEmpty(mchnt_cd)||StringUtils.isEmpty(mchnt_txn_ssn)){
			throw new Exception("参数为空");
		}
		String plain = "<plain>";
		plain +="<resp_code>"+resp_code+"</resp_code>";
		plain +="<mchnt_cd>"+mchnt_cd+"</mchnt_cd>";
		plain +="<mchnt_txn_ssn>"+mchnt_txn_ssn+"</mchnt_txn_ssn>";
		plain +="</plain>";
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<ap>");
		sb.append(plain);
		sb.append("<signature>"+SecurityUtils.sign(plain)+"</signature>");
		sb.append("</ap>");
		return sb.toString();
	}
	
}
