package com.fuiou.data;


/**
 * 明细查询接口请求类
 * @author aj
 *
 */
public class QueryReqData {
	private String mchnt_cd="";//商户代码
	private String mchnt_txn_ssn="";//流水号
	private String user_ids="";//用户登录ID
	private String start_day="";//起始时间
	private String end_day="";//截止时间
	private String signature="";//签名信息
	
	public String createSignValue(){
		String src = end_day + "|" + mchnt_cd+ "|" + mchnt_txn_ssn +"|"+ start_day + "|" + user_ids;
		return src;
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
	public String getUser_ids() {
		return user_ids;
	}
	public void setUser_ids(String user_ids) {
		this.user_ids = user_ids;
	}
	public String getStart_day() {
		return start_day;
	}
	public void setStart_day(String start_day) {
		this.start_day = start_day;
	}
	public String getEnd_day() {
		return end_day;
	}
	public void setEnd_day(String end_day) {
		this.end_day = end_day;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
}
