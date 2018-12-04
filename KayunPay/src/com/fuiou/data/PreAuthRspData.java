package com.fuiou.data;

import java.io.UnsupportedEncodingException;

/**
 * 预授权接口返回数据
 * @author aj
 *
 */
public class PreAuthRspData {
	private String  mchnt_cd="";
	private String  mchnt_txn_ssn="";
	private String  contract_no="";
	private String  resp_code="";
	private String resp_desc="";
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
	public String getContract_no() {
		return contract_no;
	}
	public void setContract_no(String contract_no) {
		this.contract_no = contract_no;
	}
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
	@Override
	public String toString() {
		return "PreAuthRspData [mchnt_cd=" + mchnt_cd + ", mchnt_txn_ssn="
				+ mchnt_txn_ssn + ", contract_no=" + contract_no
				+ ", resp_code=" + resp_code + ", resp_desc=" + resp_desc + "]";
	}
	
	
}
