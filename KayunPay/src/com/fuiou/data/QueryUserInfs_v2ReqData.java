package com.fuiou.data;

import java.io.Serializable;

/**
 * 用户信息查询接口请求
 * @author aj
 *
 */
public class QueryUserInfs_v2ReqData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String ver="";
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String mchnt_txn_dt="";
	private String user_ids="";
	private String user_idNos="";
	private String user_bankCards="";
	private String signature="";
	
	
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
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
	public String getUser_ids() {
		return user_ids;
	}
	public void setUser_ids(String user_ids) {
		this.user_ids = user_ids;
	}
	public String getUser_idNos() {
		return user_idNos;
	}
	public void setUser_idNos(String user_idNos) {
		this.user_idNos = user_idNos;
	}
	public String getUser_bankCards() {
		return user_bankCards;
	}
	public void setUser_bankCards(String user_bankCards) {
		this.user_bankCards = user_bankCards;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	/**
	 * 组合签名
	 * @return
	 */
	public String createSearchSigature_v2() {
		String src = "";
		if("0.44".equals(ver)){
			src = mchnt_cd + "|" + mchnt_txn_dt + "|" + mchnt_txn_ssn + "|" 
					+ user_bankCards + "|" + user_idNos + "|" + user_ids+ "|" +ver;
		}else{
			src = mchnt_cd + "|" + mchnt_txn_dt + "|" + mchnt_txn_ssn + "|" + user_bankCards + "|" + user_idNos + "|" + user_ids;
		}
		System.out.println("用户信息查询接口（目前只支持个人用户）签名明文>>>>"+src);
		return src;
	}
	
	
}
