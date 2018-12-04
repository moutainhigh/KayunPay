package com.fuiou.data;

import com.fuiou.util.StringUtil;

/**
 * 交易通知类
 * */
public class TradeNoticeData {
	private String mchnt_cd="";//商户代码
	private String mchnt_txn_ssn="";//流水号
	private String mchnt_txn_dt="";//交易日期
	private String amt="";//交易金额
	private String suc_amt="";//成功交易金额
	private String resp_code="";//交易返回码
	private String signature="";//签名信息
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		this.amt = amt;
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

	public String getSuc_amt() {
		return suc_amt;
	}
	public void setSuc_amt(String suc_amt) {
		this.suc_amt = suc_amt;
	}
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public String createSignValue(){
		
		String src =amt+"|"+mchnt_cd+"|"+mchnt_txn_dt+"|"+mchnt_txn_ssn+"|"+resp_code+"|"+suc_amt;
		
		System.out.println("通知接口验证签名明文>>>>"+src);
		return src;
	}
}
