package com.dutiantech.plugin.syxpay;

import java.util.HashMap;
import java.util.Map;

import com.dutiantech.plugin.syxpay.request.PayInMessage;
import com.dutiantech.plugin.syxpay.request.PayOutMessage;
import com.dutiantech.plugin.syxpay.request.QueryPayInMessage;
import com.dutiantech.plugin.syxpay.request.QueryPayOutMessage;
import com.dutiantech.plugin.syxpay.util.SYXUtils;
import com.dutiantech.util.CommonUtil;

public class SYXPayKit {
	
	/**
	 * 代扣支持的银行
	 */
	public static Map<String , String> PAYOUT_BANK_CODE = new HashMap<String , String>() ;
	
	/**
	 * 连连支付银行代码转商银信-代扣
	 */
	public static Map<String,String> LianLian_PayOut_CONVERT_BANK_CODE = new HashMap<String, String>();
	
	/**
	 * 代付支持的银行
	 */
	public static Map<String , String> PAYIN_BANK_CODE = new HashMap<String , String>() ;
	
	/**
	 * 连连支付银行代码转商银信-代付
	 */
	public static Map<String,String> LianLian_PayIn_CONVERT_BANK_CODE = new HashMap<String, String>();
	
	static{
		//代扣--------------------------------
		PAYOUT_BANK_CODE.put("ICBC", "中国工商银行");LianLian_PayOut_CONVERT_BANK_CODE.put("01020000", "ICBC");
		PAYOUT_BANK_CODE.put("ABC", "中国农业银行");LianLian_PayOut_CONVERT_BANK_CODE.put("01030000", "ABC");
		PAYOUT_BANK_CODE.put("BOC", "中国银行");LianLian_PayOut_CONVERT_BANK_CODE.put("01040000", "BOC");
		PAYOUT_BANK_CODE.put("CCB", "中国建设银行");LianLian_PayOut_CONVERT_BANK_CODE.put("01050000", "CCB");
		PAYOUT_BANK_CODE.put("COMM", "交通银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03010000", "COMM");
		PAYOUT_BANK_CODE.put("CMB", "招商银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03080000", "CMB");
		PAYOUT_BANK_CODE.put("CIB", "兴业银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03090000", "CIB");
		PAYOUT_BANK_CODE.put("CEB", "中国光大银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03030000", "CEB");
		PAYOUT_BANK_CODE.put("CMBC", "中国民生银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03050000", "CMBC");
		PAYOUT_BANK_CODE.put("CITIC", "中信银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03020000", "CITIC");
		//代付SPABANK
		PAYOUT_BANK_CODE.put("SPABANK", "平安银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03070000", "SPABANK");
		PAYOUT_BANK_CODE.put("SPDB", "浦发银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03100000", "SPDB");
		PAYOUT_BANK_CODE.put("PSBC", "中国邮政储蓄银行");LianLian_PayOut_CONVERT_BANK_CODE.put("01000000", "PSBC");
		//代付GDB
		PAYOUT_BANK_CODE.put("GDB", "广发银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03060000", "GDB");
		//代付HXBANK
		PAYOUT_BANK_CODE.put("HXBANK", "华夏银行");LianLian_PayOut_CONVERT_BANK_CODE.put("03040000", "HXBANK");
		
		
		//代付----------------------------------------------
		PAYIN_BANK_CODE.put("ICBC", "中国工商银行");LianLian_PayIn_CONVERT_BANK_CODE.put("01020000", "ICBC");
		PAYIN_BANK_CODE.put("ABC", "中国农业银行");LianLian_PayIn_CONVERT_BANK_CODE.put("01030000", "ABC");
		PAYIN_BANK_CODE.put("BOC", "中国银行");LianLian_PayIn_CONVERT_BANK_CODE.put("01040000", "BOC");
		PAYIN_BANK_CODE.put("CCB", "中国建设银行");LianLian_PayIn_CONVERT_BANK_CODE.put("01050000", "CCB");
		PAYIN_BANK_CODE.put("COMM", "交通银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03010000", "COMM");
		PAYIN_BANK_CODE.put("CMB", "招商银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03080000", "CMB");
		PAYIN_BANK_CODE.put("CIB", "兴业银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03090000", "CIB");
		PAYIN_BANK_CODE.put("CEB", "中国光大银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03030000", "CEB");
		PAYIN_BANK_CODE.put("CMBC", "中国民生银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03050000", "CMBC");
		PAYIN_BANK_CODE.put("CITIC", "中信银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03020000", "CITIC");
		//代付SPABANK
		PAYIN_BANK_CODE.put("SPABANK", "平安银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03070000", "SPABANK");
		PAYIN_BANK_CODE.put("SPDB", "浦发银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03100000", "SPDB");
		PAYIN_BANK_CODE.put("PSBC", "中国邮政储蓄银行");LianLian_PayIn_CONVERT_BANK_CODE.put("01000000", "PSBC");
		//代付GDB
		PAYIN_BANK_CODE.put("GDB", "广发银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03060000", "GDB");
		//代付HXBANK
		PAYIN_BANK_CODE.put("HXBANK", "华夏银行");LianLian_PayIn_CONVERT_BANK_CODE.put("03040000", "HXBANK");
	}
	
	public static String _MERCHANTID = "001016091901159";
	public static String _SIGNTYPE = "RSA";

//	public static String _PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALVysUnQAZowOKk97mJSocpOoTSt4vcanUL23mMtiFRLYI39BOhJforDqWU0Jp2GuDVxvxR9M251zz+novlWjbbeJypxz9RJaaPRp0j+U6BjHeqe903HILCJkr9cFYcMk58y/aGCutQjNdRtyJdM8JUEAQsKAK012TxcwZQZgNB7AgMBAAECgYBuUPXy/5EJ0omQdAPMKZsOKfIe2h2kyLyWKwuYVuWNAmsIyK1EIPLboQwLaMKVgYPbknVvGpO7c8r2U5BlcbckT10fcDR1ue+wpdmmkLao5R0uvVULKk3uMBihXHYz+CrKX/90Gyw246HpOU3shNdgAXfJu6cik5N7dWQjqvw+8QJBAPlfpnahjY8NhY+jdzmRcBUo+3Kcf++Za8lRF6Bqph7E7HkHJVChV7IeMWI3DH8IkeN0lRxGu7muB0XaefYmNNkCQQC6RPtxj2U8vNJtgnHtEsWymi79CwxzRhDHo8yKbGgWTHShkzT8C3q71Z2WzS+G/tJTG4XFAzsKAsZ5C628h8tzAkAsMi6Pcdxj0RKtvVvhAQyrQ2MlEeV+smMu/8c/MAeXBnGelYygeKfRMpawAG+fiAZLGJtgsoyMwPIbCDh7TpQpAkEAkHu/mneDLJi+lMkxO5ZrGT21ovw1/RhlHaY1m025c0p80XPRDoyM+DQbWATyj8ELPgHEQmxeGEbM0sm3Stz+dwJADEfbFOOIu7r918G0tiDUOMCaPqP47qqTRwxM0COnAE6UnLYfa1PLzz+QIcwwWVRJITDRAlkYzcXc0p5xHEXM7A==";
//	public static String _PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1crFJ0AGaMDipPe5iUqHKTqE0reL3Gp1C9t5jLYhUS2CN/QToSX6Kw6llNCadhrg1cb8UfTNudc8/p6L5Vo223icqcc/USWmj0adI/lOgYx3qnvdNxyCwiZK/XBWHDJOfMv2hgrrUIzXUbciXTPCVBAELCgCtNdk8XMGUGYDQewIDAQAB";
	
	public static String _PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAPvkFrHN5EuBjjO4No+JzJfetLw+C8UN9AUzCQi/oGdn3ZPJ90qCuWy57pPbUNyjvihAJQIC2HLfdfI16sgDB2zaxE6tsGibK6E11HFfusaCVnOilNCVXOmS+74WCF91uzeDAZBKypM/JQes/jUsnVVEobd9xU+jxbp0eWLs6YqNAgMBAAECgYB+F+0AbwgUgDNWUqYFbr7bW6HwBcNQjq6UF7szBPJgMU9rVxQS2aPG+MCkhYpSGSns7kdYXWJLBu/9It5354REDHWeKy2ML8FLU2OFGF/R5ve7Dod7fUh1r6rnnJV9ayYtkpuMGgWohqBfBOKiUFmfMuXnyO1WxF5y22lRvyx8JQJBAP71+us9lUrQsth2El3KUysG0JmrKH0WLUyDpHDeDLSQcbUxjUflwPbi3W1HHry1Te4PfwB/crzfzPJqMdE48GcCQQD86ufLwI7mmDGJiTj7vIKdFRzRSYbY4CeXhDlkg9F7Rt/MUjnrBpLnAAEdvCKAQyx4N8KvgE+0gNnaU/HX+sTrAkAwcUQvlg/bmzOf9S8gKuUKc80GpYKX4bLRquF/oHEBjcBNEREq6/hx8EDqFm08paYc6UzUJ2MBh/REyvCUNCFZAkBZQinmDaIhHYs2B9i1dxT9jQrrjbBqb8lpPr/mimLux9eJy2cnW92Sarz6GoBulcZm8v7hYz497M91rvSGDblbAkEA5PuGgZlDJcyvj0VQSEyCSUW33ZiPO4CBcbOBG9g3FWK/fLgKziVo0XyhFZ0MAEBGEaSKriZ1av8ZkewXtQ6r6A==";
	public static String _PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCY9FNgwnpBE02MDe658rSYwP5VYJIUt2OD6+VY3rkB27xN+0M1fmT81Yz+wP5rqW3zZ7LCCZcmfPwawg6TCQN9tu0Z/fw/jWbVYWC2dw2PKqOXX7o9BjXo7YwNAFj261zJPVFTRnbWtTkiRdUPb5dJ5bCut2HfVWvtoc5Vcn7kgQIDAQAB";
	
	//代扣支付
	public static String _PAY_OUT_URL = "https://paymenta.allscore.com/olgateway/withhold/withholdPay.htm";
	//代付支付
	public static String _PAY_IN_URL = "https://paymenta.allscore.com/olgateway/agentpay/singleAgentPay.htm";
	
	//代扣订单查询
	public static String _QUERY_PAYOUT = "https://paymenta.allscore.com/olgateway/withhold/queryOrderProtocol.htm";
	//代付订单查询
	public static String _QUERY_PAYIN = "https://paymenta.allscore.com/olgateway/agentpay/querySingleAgentPay.htm";
	
	
	public static String _POST_PAY_OUT_NOTIFY_URL = "http://www.yrhx.com/syxpayout_callback";
	
//	public static String _POST_PAY_OUT_NOTIFY_URL = "http://59.172.250.254:9875/syxpayout_callback";
	
	public static String _POST_PAY_IN_NOTIFY_URL = "http://www.yrhx.com/syxpayin_callback";
	
//	public static String _POST_PAY_IN_NOTIFY_URL = "http://59.172.250.254:9875/syxpayin_callback";
	
	/**
	 * 代扣
	 * @param outOrderId	订单号
	 * @param licenseNo		协议号
	 * @param transAmt		交易金额
	 * @param subject		用途描述
	 * @param bankid		银行编码
	 * @param bankCardNo	银行卡号
	 * @param realName		持卡人姓名
	 * @param cardId		身份证
	 * @param phoneNo		电话号
	 * @return
	 */
	public static Map<String,String> payOut( String outOrderId , String licenseNo , long transAmt ,	String subject , String bankid ,String bankCardNo , String realName , String cardId ,String phoneNo){
		
		PayOutMessage msg = new PayOutMessage() ;
		
		msg.setEncodeFields("bankCardNo|realName|cardId|phoneNo");
		
		msg.put("outOrderId", outOrderId ) ;
		msg.put("licenseNo", licenseNo ) ;
		
		msg.put("transAmt", CommonUtil.yunsuan(transAmt + "", "100.00", "chu", 2).doubleValue()+"") ;
		msg.put("subject", subject ) ;
		msg.put("bankId", bankid ) ;
		msg.put("bankCardNo", bankCardNo ) ;
		msg.put("realName", realName ) ;
		msg.put("cardId", cardId ) ;
		msg.put("phoneNo", phoneNo ) ;
//		msg.put("province", "北京市");
//		msg.put("city", "110100");
		msg.put("notifyUrl", SYXPayKit._POST_PAY_OUT_NOTIFY_URL ) ;
		msg = SYXUtils.postByHttps1( SYXPayKit._PAY_OUT_URL , msg.toRequest() );
		return msg;
		
	}
	
	/**
	 * 代付
	 * @param outOrderId	订单号
	 * @param payAmount		交易金额
	 * @param subject		用途描述
	 * @param cardHolder	持卡人姓名
	 * @param bankCardNo	卡号
	 * @param bankid		银行卡编码
	 * @param bankProvince	银行卡省份
	 * @param bankCity		银行卡城市
	 * @param remark		备注
	 * @return
	 */
	public static Map<String,String> payIn(String outOrderId,long payAmount, String subject,String cardHolder,String bankCardNo,String bankid,String bankProvince,String bankCity,String remark){
		PayInMessage msg = new PayInMessage() ;
		
		msg.setEncodeFields("bankCardNo");
		msg.setReplaceFields("bankCardNo");
		
		msg.put("outOrderId", outOrderId ) ;
		msg.put("payAmount", CommonUtil.yunsuan(payAmount + "", "100.00", "chu", 2).doubleValue()+"" ) ;
		msg.put("cardHolder", cardHolder);
		msg.put("bankCardNo", bankCardNo);
		msg.put("bankName", SYXPayKit.PAYIN_BANK_CODE.get(bankid));
		msg.put("bankBranchName", SYXPayKit.PAYIN_BANK_CODE.get(bankid));
		msg.put("bankProvince", bankProvince);
		msg.put("bankCity", bankCity);
		msg.put("bankCode", bankid);
		
		msg.put("subject", subject ) ;
		msg.put("remark", remark);
		msg.put("notifyUrl", SYXPayKit._POST_PAY_IN_NOTIFY_URL ) ;
		msg = SYXUtils.postByHttps2( SYXPayKit._PAY_IN_URL , msg.toRequest() );
		return msg;
	}
	
	
	/**
	 * 代扣订单查询
	 * @param orderId		商银信的订单号
	 * @return
	 * @author 
	 */
	public static Map<String,String> queryPayOutOrder( String  orderId){
		
		QueryPayOutMessage msg = new QueryPayOutMessage() ;
		msg.put("orderId", orderId);
		
		//TODO 此处返回需要转换为统一返回      
		//订单状态说明：  1：待处理；2：代扣成功；4：代扣失败  6:  提交处理中;8: 审核拒绝
		msg = SYXUtils.postByHttps4( SYXPayKit._QUERY_PAYOUT , msg.toRequest() );
		return msg;
		
	}
	
	/**
	 * 代扣订单查询
	 * @param orderId		商银信的订单号
	 * @return
	 * @author 
	 */
	public static Map<String,String> queryPayInOrder( String  orderId){
		
		QueryPayInMessage opm = new QueryPayInMessage();
		opm.put("outOrderId", orderId);
		//TODO 此处返回需要转换为统一返回
		opm = SYXUtils.postByHttps3( SYXPayKit._QUERY_PAYIN , opm.toRequest() );
		return opm;
		
	}
	
}