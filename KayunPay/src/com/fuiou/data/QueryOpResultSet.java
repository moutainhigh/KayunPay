package com.fuiou.data;

import java.util.ArrayList;
import java.util.List;

/**
 * 明细查询list
 * @author aj
 *
 */
public class QueryOpResultSet {
	private String user_id="";//用户ID
	private String ct_balance="";//期初账面总余额
	private String ca_balance="";//期初可用总余额
	private String cu_balance="";//期初未转结总余额
	private String cf_balance="";//期初冻结总余额
	private List<QueryDetail> details = new ArrayList<QueryDetail>();
	@Override
	public String toString() {
		return "QueryOpResultSet [user_id=" + user_id + ", ct_balance="
				+ ct_balance + ", ca_balance=" + ca_balance + ", cu_balance="
				+ cu_balance + ", cf_balance=" + cf_balance + ", details="
				+ details + "]";
	}
	public String getUser_id() {
		return user_id;
	}
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	public String getCt_balance() {
		return ct_balance;
	}
	public void setCt_balance(String ct_balance) {
		this.ct_balance = ct_balance;
	}
	public String getCa_balance() {
		return ca_balance;
	}
	public void setCa_balance(String ca_balance) {
		this.ca_balance = ca_balance;
	}
	public String getCu_balance() {
		return cu_balance;
	}
	public void setCu_balance(String cu_balance) {
		this.cu_balance = cu_balance;
	}
	public String getCf_balance() {
		return cf_balance;
	}
	public void setCf_balance(String cf_balance) {
		this.cf_balance = cf_balance;
	}
	public List<QueryDetail> getDetails() {
		return details;
	}
	public void setDetails(List<QueryDetail> details) {
		this.details = details;
	}
	
	
}
