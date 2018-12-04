package com.fuiou.data;

import java.io.Serializable;

import com.fuiou.util.SecurityUtils;

/**
 * 11,23:商户端个人用户跳转登录页面（网页版）数据组装
 * @author aj
 *
 */
public class ReturnLoginReqData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mchnt_cd="";
	private String mchnt_txn_ssn="";
	private String cust_no="";
	private String location="";
	private String amt="";
	private String signature="";
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
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getAmt() {
		return amt;
	}
	public void setAmt(String amt) {
		this.amt = amt;
	}
	public String getSignature() {
		signature = SecurityUtils.sign(this.createSignValueFor());
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String createSignValueFor(){
		String src =amt+"|"+cust_no +"|"+ location +"|"+mchnt_cd+"|"+mchnt_txn_ssn;
		System.out.println("请求接口验证签名明文>>>>"+src);
		return src;
	}
}
