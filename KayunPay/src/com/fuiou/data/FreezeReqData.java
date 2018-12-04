package com.fuiou.data;

import com.fuiou.util.StringUtil;


/**
 * 冻结解冻请求
 * @author aj
 *
 */
public class FreezeReqData {
	private String ver="";
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String cust_no="";
	private String amt="";
	private String rem="";
	private String signature="";
	
	public String createSignValue(){
		String src = amt + "|" +cust_no+"|"+ mchnt_cd + "|" + mchnt_txn_ssn+"|"+ rem;
		if(StringUtil.isNotEmpty(ver)){
			src = amt + "|" +cust_no+"|"+ mchnt_cd + "|" + mchnt_txn_ssn+"|"+ rem +"|"+ ver;
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
	public String getCust_no() {
		return cust_no;
	}
	public void setCust_no(String cust_no) {
		this.cust_no = cust_no;
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
