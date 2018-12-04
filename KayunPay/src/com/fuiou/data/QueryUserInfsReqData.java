package com.fuiou.data;

/**
 * 用户信息查询接口请求
 * @author aj
 *
 */
public class QueryUserInfsReqData {
	private String  mchnt_cd="";
	private String  mchnt_txn_ssn="";
	private String  mchnt_txn_dt="";
	private String  user_ids="";
	private String  signature="";
	private String ver = "" ;   //新接口文档的版本号
	
	public String createSignValue(){
		String src = "";
		if("0.44".equals(ver)){
			src=mchnt_cd+"|"+mchnt_txn_dt+"|"+mchnt_txn_ssn+"|"+user_ids+"|"+ver;
		}else{
			src= mchnt_cd + "|" + mchnt_txn_dt+ "|" + mchnt_txn_ssn + "|" +user_ids ;
		}
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
	public String getUser_ids() {
		return user_ids;
	}
	public void setUser_ids(String user_ids) {
		this.user_ids = user_ids;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getVer() {
		return ver;
	}

	public void setVer(String ver) {
		this.ver = ver;
	}
	
	
	
	
}
