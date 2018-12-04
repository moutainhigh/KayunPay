package com.fuiou.data;

import java.io.Serializable;

/**
 * 商户P2P网站免登录用户更换银行卡接口求参数类
 * @author aj
 *
 */
public class ChangeCard2ReqData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd = "";
	private String mchnt_txn_ssn = "";
	private String login_id = "";
	private String page_notify_url = "";
	private String signature = "";
	/**
	 * 生成客户请求平台的注册验签数据
	 * @return
	 */
	public String createSignValue(){
		String src = login_id + "|" + mchnt_cd + "|" + mchnt_txn_ssn + "|"+ page_notify_url;
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
