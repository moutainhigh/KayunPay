package com.fuiou.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 交易查询返回
 * 
 * @author aj
 * 
 */
public class QueryCzTxRspData {
	private String resp_code = "";
	private String mchnt_cd = "";
	private String mchnt_txn_ssn = "";
	private String busi_tp = "";
	private String total_number = "";

	private List<QueryCzTxRspDetailData> results = new ArrayList<QueryCzTxRspDetailData>();

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

	public String getBusi_tp() {
		return busi_tp;
	}

	public void setBusi_tp(String busi_tp) {
		this.busi_tp = busi_tp;
	}

	public String getTotal_number() {
		return total_number;
	}

	public void setTotal_number(String total_number) {
		this.total_number = total_number;
	}


	public List<QueryCzTxRspDetailData> getResults() {
		return results;
	}

	public void setResults(List<QueryCzTxRspDetailData> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "QueryCzTxRspData [resp_code=" + resp_code + ", mchnt_cd="
				+ mchnt_cd + ", mchnt_txn_ssn=" + mchnt_txn_ssn + ", busi_tp="
				+ busi_tp + ", total_number=" + total_number + ", results="
				+ results + "]";
	}


}
