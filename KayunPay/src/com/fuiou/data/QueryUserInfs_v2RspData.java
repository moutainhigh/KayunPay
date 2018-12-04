package com.fuiou.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 交易查询返回
 * @author aj
 *
 */
public class QueryUserInfs_v2RspData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String resp_code = "";
	private String mchnt_cd = "";
	private String mchnt_txn_ssn = "";
	private String signature = "";
	private List<QueryUserInfs_v2RspDetailData> results = new ArrayList<QueryUserInfs_v2RspDetailData>();
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
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public List<QueryUserInfs_v2RspDetailData> getResults() {
		return results;
	}
	public void setResults(List<QueryUserInfs_v2RspDetailData> results) {
		this.results = results;
	} 
	
	
	
}
