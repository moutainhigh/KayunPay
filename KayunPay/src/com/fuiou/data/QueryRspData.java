package com.fuiou.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 明细查询返回参数
 * @author aj
 *
 */
public class QueryRspData {
	private String resp_code;//返回码
	private String mchnt_cd;//商户号
	private String mchnt_txn_ssn;//交易流水
	private List<QueryOpResultSet> opResultSet = new ArrayList<QueryOpResultSet>();
	
	@Override
	public String toString() {
		return "QueryRspData [resp_code=" + resp_code + ", mchnt_cd="
				+ mchnt_cd + ", mchnt_txn_ssn=" + mchnt_txn_ssn
				+ ", opResultSet=" + opResultSet + "]";
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
	public List<QueryOpResultSet> getOpResultSet() {
		return opResultSet;
	}
	public void setOpResultSet(List<QueryOpResultSet> opResultSet) {
		this.opResultSet = opResultSet;
	}
	
}
