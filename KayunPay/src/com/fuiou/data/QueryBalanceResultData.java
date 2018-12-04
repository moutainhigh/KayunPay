package com.fuiou.data;


/**
 * 余额查询返回参数
 * @author aj
 *
 */
public class QueryBalanceResultData {
	private String user_id="";//用户名
	private String ct_balance="";//账面总余额
	private String ca_balance="";//可用余额
	private String cf_balance="";//冻结余额
	private String cu_balance="";//未转结余额
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
	public String getCf_balance() {
		return cf_balance;
	}
	public void setCf_balance(String cf_balance) {
		this.cf_balance = cf_balance;
	}
	public String getCu_balance() {
		return cu_balance;
	}
	public void setCu_balance(String cu_balance) {
		this.cu_balance = cu_balance;
	}
	@Override
	public String toString() {
		return "QueryBalanceResultData [user_id=" + user_id + ", ct_balance="
				+ ct_balance + ", ca_balance=" + ca_balance + ", cf_balance="
				+ cf_balance + ", cu_balance=" + cu_balance + "]";
	}
	
}
