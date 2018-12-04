package com.fuiou.data;

import java.io.UnsupportedEncodingException;

/**
 * app免登录交易返回(接口26,27,28,29,30,31)
 * @author aj
 *
 */
public class AppTransRspData {
	private String resp_code="";
	private String resp_desc="";
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String login_id="";
	private String amt="";
	private String signature="";
	private String rem="";
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
	}
	public String getResp_desc() {
		return resp_desc;
	}
	public void setResp_desc(String resp_desc) throws UnsupportedEncodingException {
		this.resp_desc = new String(resp_desc.getBytes("ISO8859-1"), "UTF-8").trim();
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
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		this.amt = amt;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String getRem() {
		return rem;
	}
	public void setRem(String rem) {
		this.rem = rem;
	}
	public String createSignValue(){
		String src =amt+"|"+ login_id + "|"+ mchnt_cd +"|" +mchnt_txn_ssn+"|"+resp_code ;
		System.out.println("返回签名明文>>>>"+src);
		return src;
	}
	public String createWebRechargeSignValue(){
		String src =amt+"|"+ login_id + "|"+ mchnt_cd +"|" +mchnt_txn_ssn+"|"+rem +"|" +resp_code;
		System.out.println("返回签名明文>>>>"+src);
		return src;
	}
	
	
}
