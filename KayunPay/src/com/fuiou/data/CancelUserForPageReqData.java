package com.fuiou.data;

import java.io.Serializable;

import com.fuiou.util.StringUtil;


/**
 * 手机端APP个人用户自助开户注册（APP网页版）
 * @author aj
 *
 */
public class CancelUserForPageReqData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd = "";
	private String mchnt_txn_ssn = "";
	private String login_id = "";
	private String page_notify_url="";
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

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getPage_notify_url() {
		return page_notify_url;
	}

	public void setPage_notify_url(String page_notify_url) {
		this.page_notify_url = page_notify_url;
	}

	public String createSignValue(){
		String src = login_id+"|"+mchnt_cd+"|"+mchnt_txn_ssn;
		if(StringUtil.isNotEmpty(page_notify_url)){
			src = login_id+"|"+mchnt_cd+"|"+mchnt_txn_ssn+"|"+page_notify_url;
		}
		System.out.println("签名明文>>>>"+src);
		return src;
	}
	
}
