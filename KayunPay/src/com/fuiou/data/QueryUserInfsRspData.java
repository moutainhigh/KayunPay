package com.fuiou.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息查询接口返回
 * @author aj
 *
 */
public class QueryUserInfsRspData {
	private String  mchnt_cd="";
	private String  mchnt_txn_ssn="";
	private String  resp_code="";
	private String mchnt_nm="";
	private List<QueryUserInfsRspDetailData> results = new ArrayList<QueryUserInfsRspDetailData>();
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
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
	}
	public List<QueryUserInfsRspDetailData> getResults() {
		return results;
	}
	public void setResults(List<QueryUserInfsRspDetailData> results) {
		this.results = results;
	}
	public String getMchnt_nm() {
		return mchnt_nm;
	}
	public void setMchnt_nm(String mchnt_nm) {
		this.mchnt_nm = mchnt_nm;
	}
	@Override
	public String toString() {
		return "QueryUserInfsRspData [mchnt_cd=" + mchnt_cd
				+ ", mchnt_txn_ssn=" + mchnt_txn_ssn + ", resp_code="
				+ resp_code + ", mchnt_nm=" + mchnt_nm + ", results=" + results
				+ "]";
	}
	
	
	
}
