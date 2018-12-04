package com.fuiou.data;

import java.util.ArrayList;
import java.util.List;


/**
 * 余额查询返回参数
 * @author aj
 *
 */
public class QueryBalanceRspData {
	private String mchnt_cd="";//商户代码
	private String mchnt_txn_ssn="";//流水号
	private String resp_code="";//存管系统返回码
	private String cust_no="";//待查询的登录帐户
	private List<QueryBalanceResultData> results = new ArrayList<QueryBalanceResultData>();
	
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
	public String getCust_no() {
		return cust_no;
	}
	public void setCust_no(String cust_no) {
		this.cust_no = cust_no;
	}
	public String getResp_code() {
		return resp_code;
	}
	public void setResp_code(String resp_code) {
		this.resp_code = resp_code;
	}
	public List<QueryBalanceResultData> getResults() {
		return results;
	}
	public void setResults(List<QueryBalanceResultData> results) {
		this.results = results;
	}
	@Override
	public String toString() {
		return "QueryBalanceRspData [mchnt_cd=" + mchnt_cd + ", mchnt_txn_ssn="
				+ mchnt_txn_ssn + ", resp_code=" + resp_code + ", cust_no="
				+ cust_no + ", results=" + results + "]";
	}
	
}
