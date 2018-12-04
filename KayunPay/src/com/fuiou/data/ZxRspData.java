package com.fuiou.data;

import java.io.Serializable;

/**
 * 充值/提现通知接口数据解析
 * @author aj
 *
 */
public class ZxRspData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String mchnt_txn_dt="";
	private String login_id="";
	private String state="";
	private String signature="";

	public String createSignValue(){
		String src =login_id + "|" + mchnt_cd + "|" + mchnt_txn_dt +"|"+mchnt_txn_ssn+"|"+state;
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


	public String getMchnt_txn_dt() {
		return mchnt_txn_dt;
	}


	public void setMchnt_txn_dt(String mchnt_txn_dt) {
		this.mchnt_txn_dt = mchnt_txn_dt;
	}


	public String getLogin_id() {
		return login_id;
	}


	public void setLogin_id(String login_id) {
		this.login_id = login_id;
	}


	public String getState() {
		return state;
	}


	public void setState(String state) {
		this.state = state;
	}


	public String getSignature() {
		return signature;
	}


	public void setSignature(String signature) {
		this.signature = signature;
	}


}
