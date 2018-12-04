package com.fuiou.data;

import java.io.Serializable;

/**
 * 用户提现模式请求参数类
 * @author aj
 *
 */
public class WtWithdrawReqData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd = "";
	private String mchnt_txn_ssn = "";
	private String login_id = "";
	private String amt = "";
	private String rem ="";
	private String back_notify_url ="";
	private String signature = "";

	
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
	public String getRem() {
		return rem;
	}
	public void setRem(String rem) {
		this.rem = rem;
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

	/**
	 * 生成客户请求平台的注册验签数据
	 * @return
	 */
	public String createSignValue(){
		String src =  amt+"|"+ back_notify_url + "|"+ login_id + "|"+ mchnt_cd +"|" +mchnt_txn_ssn+"|"+rem;
		System.out.println("委托提现签名明文>>>>"+src);
		return src;
	}
}
