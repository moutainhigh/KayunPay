package com.fuiou.data;

import java.io.Serializable;

/**
 * 
 * @author aj
 *
 */
public class QueryChangeCardRspData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String resp_code="";
	private String desc_code="";
	private String mchnt_txn_ssn="";
	private String login_id="";
	private String mchnt_cd="";
	private String txn_ssn="";
	private String bank_nm="";
	private String card_no="";
	private String examine_st="";
	private String remark="";
	private String signature="";
	
	
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
	}
	public String getDesc_code() {
		return desc_code;
	}
	public void setDesc_code(String desc_code) {
		this.desc_code = desc_code;
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
	public String getMchnt_cd() {
		return mchnt_cd;
	}
	public void setMchnt_cd(String mchnt_cd) {
		this.mchnt_cd = mchnt_cd;
	}
	public String getTxn_ssn() {
		return txn_ssn;
	}
	public void setTxn_ssn(String txn_ssn) {
		this.txn_ssn = txn_ssn;
	}
	public String getBank_nm() {
		return bank_nm;
	}
	public void setBank_nm(String bank_nm) {
		this.bank_nm = bank_nm;
	}
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public String getExamine_st() {
		return examine_st;
	}
	public void setExamine_st(String examine_st) {
		this.examine_st = examine_st;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	
	
	
	
}
