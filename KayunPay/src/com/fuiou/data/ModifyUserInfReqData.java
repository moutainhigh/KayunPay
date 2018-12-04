package com.fuiou.data;

/**
 * 用户信息修改请求
 * @author aj
 *
 */
public class ModifyUserInfReqData {
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String cust_nm="";
	private String certif_id="";
	private String mobile_no="";
	private String email="";
	private String city_id="";
	private String parent_bank_id="";
	private String bank_nm="";
	private String capAcntNo="";
	private String signature="";
	
	public String createSignValue(){
		String src =bank_nm+"|"+ capAcntNo  + "|" + certif_id + "|" + city_id + "|"
				+ cust_nm + "|" + email + "|"+ mchnt_cd
				+ "|" + mchnt_txn_ssn + "|" + mobile_no + "|" + parent_bank_id;
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
	public String getCust_nm() {
		return cust_nm;
	}
	public void setCust_nm(String cust_nm) {
		this.cust_nm = cust_nm;
	}
	public String getCertif_id() {
		return certif_id;
	}
	public void setCertif_id(String certif_id) {
		this.certif_id = certif_id;
	}
	public String getMobile_no() {
		return mobile_no;
	}
	public void setMobile_no(String mobile_no) {
		this.mobile_no = mobile_no;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCity_id() {
		return city_id;
	}
	public void setCity_id(String city_id) {
		this.city_id = city_id;
	}
	public String getParent_bank_id() {
		return parent_bank_id;
	}
	public void setParent_bank_id(String parent_bank_id) {
		this.parent_bank_id = parent_bank_id;
	}
	public String getBank_nm() {
		return bank_nm;
	}
	public void setBank_nm(String bank_nm) {
		this.bank_nm = bank_nm;
	}
	public String getCapAcntNo() {
		return capAcntNo;
	}
	public void setCapAcntNo(String capAcntNo) {
		this.capAcntNo = capAcntNo;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
}
