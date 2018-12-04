package com.fuiou.data;

import java.io.UnsupportedEncodingException;

/**
 * 公用返回参数类
 * @author aj
 *
 */
public class CommonRspData {
	private String resp_code="";//响应码
	private String resp_desc="";//响应消息
	private String mchnt_cd="";//商户代码
	private String mchnt_txn_ssn="";//请求流水号
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
	@Override
	public String toString() {
		return "CommonRspData [resp_code=" + resp_code + ", resp_desc="
				+ resp_desc + ", mchnt_cd=" + mchnt_cd + ", mchnt_txn_ssn="
				+ mchnt_txn_ssn + "]";
	}
}
