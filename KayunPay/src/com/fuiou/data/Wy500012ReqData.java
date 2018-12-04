package com.fuiou.data;

import java.io.Serializable;


/**
 * P2P免登录直接跳转网银界面充值接口交易请求
 * @author aj
 *
 */
public class Wy500012ReqData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String login_id="";
	private String amt="";
	private String order_pay_type="";
	private String iss_ins_cd="";
	private String page_notify_url="";
	private String back_notify_url="";
	private String signature="";
	public String getMchnt_cd() {
		return mchnt_cd;
	}
	public void setMchnt_cd(String mchnt_cd) {
		this.mchnt_cd = mchnt_cd;
	}
	public String getMchnt_txn_ssn() {
		return mchnt_txn_ssn;
	}
	public void setMchnt_txn_ssn(String mchnt_txn_ssn) {
		this.mchnt_txn_ssn = mchnt_txn_ssn;
	}
	public String getLogin_id() {
		return login_id;
	}
	public void setLogin_id(String login_id) {
		this.login_id = login_id;
	}
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		this.amt = amt;
	}
	public String getOrder_pay_type() {
		return order_pay_type;
	}
	public void setOrder_pay_type(String order_pay_type) {
		this.order_pay_type = order_pay_type;
	}
	public String getIss_ins_cd() {
		return iss_ins_cd;
	}
	public void setIss_ins_cd(String iss_ins_cd) {
		this.iss_ins_cd = iss_ins_cd;
	}
	public String getPage_notify_url() {
		return page_notify_url;
	}
	public void setPage_notify_url(String page_notify_url) {
		this.page_notify_url = page_notify_url;
	}
	public String getBack_notify_url() {
		return back_notify_url;
	}
	public void setBack_notify_url(String back_notify_url) {
		this.back_notify_url = back_notify_url;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	public String createSignValue(){
		String src = amt + "|" + back_notify_url + "|" + iss_ins_cd + "|" + login_id + "|" + 
				mchnt_cd + "|" + mchnt_txn_ssn + "|" + order_pay_type + "|" + page_notify_url;
		System.out.println("P2P免登录直接跳转网银界面充值接口验证签名明文>>>>"+src);
		return src;
	}
	
	
	
}
