package com.fuiou.data;

import java.io.Serializable;

import com.fuiou.util.SecurityUtils;

/**
 * 金账户免登陆授权配置（短信通知+委托交易）
 * @author aj
 *
 */
public class AuthConfigReqData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String login_id="";
	private String busi_tp="";
	private String page_notify_url="";
	private String signature="";
	
	public String createSignValue(){
		String src = page_notify_url + "|" + busi_tp + "|" + login_id + "|" + mchnt_cd + "|" + mchnt_txn_ssn;
		System.out.println("签名明文>>>>"+src);
		return src;
	}

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

	public String getBusi_tp() {
		return busi_tp;
	}

	public void setBusi_tp(String busi_tp) {
		this.busi_tp = busi_tp;
	}

	public String getPage_notify_url() {
		return page_notify_url;
	}

	public void setPage_notify_url(String page_notify_url) {
		this.page_notify_url = page_notify_url;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	
}
