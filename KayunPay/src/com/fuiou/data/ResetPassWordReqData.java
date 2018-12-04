package com.fuiou.data;

import java.io.Serializable;


/**
 *用户密码修改重置免登陆接口(网页版)请求参数类
 *用户密码修改重置免登陆接口(app版)请求参数类
 * @author aj
 *
 */
public class ResetPassWordReqData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String login_id="";
	private String busi_tp="";
	private String back_url="";
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
	public String getBusi_tp() {
		return busi_tp;
	}
	public void setBusi_tp(String busi_tp) {
		this.busi_tp = busi_tp;
	}
	public String getBack_url() {
		return back_url;
	}
	public void setBack_url(String back_url) {
		this.back_url = back_url;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String createSignValueFor(){
		String src =busi_tp+"|"+login_id+"|"+mchnt_cd+"|"+mchnt_txn_ssn;
		System.out.println("请求接口验证签名明文>>>>"+src);
		return src;
	}

	
	
}
