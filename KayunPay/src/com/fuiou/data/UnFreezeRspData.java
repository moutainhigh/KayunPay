package com.fuiou.data;

/**
 * 解冻返回
 * @author aj
 *
 */
public class UnFreezeRspData {
	private String resp_code="";//响应码
	private String mchnt_cd="";//商户代码
	private String mchnt_txn_ssn="";//请求流水号
	private String amt="";
	private String suc_amt="";
	private String resp_desc="";
	public String getResp_desc() {
		return resp_desc;
	}
	public void setResp_desc(String resp_desc) {
		this.resp_desc = resp_desc;
	}
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
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
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		this.amt = amt;
	}
	public String getSuc_amt() {
		return suc_amt;
	}
	public void setSuc_amt(String suc_amt) {
		this.suc_amt = suc_amt;
	}
	@Override
	public String toString() {
		return "UnFreezeRspData [resp_code=" + resp_code + ", mchnt_cd="
				+ mchnt_cd + ", mchnt_txn_ssn=" + mchnt_txn_ssn + ", amt="
				+ amt + ", suc_amt=" + suc_amt + "]";
	}
	
}
