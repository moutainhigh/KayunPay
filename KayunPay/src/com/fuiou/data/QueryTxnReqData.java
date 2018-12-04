package com.fuiou.data;

/**
 * 交易查询请求
 * @author aj
 *
 */
public class QueryTxnReqData {
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String busi_tp="";
	private String start_day="";
	private String end_day="";
	private String txn_ssn="";
	private String cust_no="";
	private String txn_st="";
	private String remark="";
	private String page_no="";
	private String page_size="";
	private String signature="";
	
	public String createSignValue(){
		String src=this.busi_tp+"|"+
				this.cust_no+"|"+
				this.end_day+"|"+
				this.mchnt_cd+"|"+
				this.mchnt_txn_ssn+"|"+
				this.page_no+"|"+
				this.page_size+"|"+
				this.remark+"|"+
				this.start_day+"|"+
				this.txn_ssn+"|"+
				this.txn_st;
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
	public String getBusi_tp() {
		return busi_tp;
	}
	public void setBusi_tp(String busi_tp) {
		this.busi_tp = busi_tp;
	}
	public String getStart_day() {
		return start_day;
	}
	public void setStart_day(String start_day) {
		this.start_day = start_day;
	}
	public String getEnd_day() {
		return end_day;
	}
	public void setEnd_day(String end_day) {
		this.end_day = end_day;
	}
	public String getTxn_ssn() {
		return txn_ssn;
	}
	public void setTxn_ssn(String txn_ssn) {
		this.txn_ssn = txn_ssn;
	}
	public String getCust_no() {
		return cust_no;
	}
	public void setCust_no(String cust_no) {
		this.cust_no = cust_no;
	}
	public String getTxn_st() {
		return txn_st;
	}
	public void setTxn_st(String txn_st) {
		this.txn_st = txn_st;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	public String getPage_no() {
		return page_no;
	}
	public void setPage_no(String page_no) {
		this.page_no = page_no;
	}
	public String getPage_size() {
		return page_size;
	}
	public void setPage_size(String page_size) {
		this.page_size = page_size;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	
}
