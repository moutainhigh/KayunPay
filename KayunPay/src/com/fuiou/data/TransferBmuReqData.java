package com.fuiou.data;

import com.fuiou.util.StringUtil;


/**
 * 转账(商户与个人之间)请求数据
 * @author aj
 *
 */
public class TransferBmuReqData {
	private String  ver="";
	private String  mchnt_cd="";
	private String  mchnt_txn_ssn="";
	private String  out_cust_no="";
	private String  in_cust_no="";
	private String  amt="";
	private String  contract_no="";
	private String  rem="";
	private String  signature="";
	
	public String createSignValue(){
		String src = amt + "|"+ contract_no+"|"+in_cust_no+"|"+ mchnt_cd + "|" + mchnt_txn_ssn+"|"+ out_cust_no+"|"+rem;
		if(StringUtil.isNotEmpty(ver)){	
			src = amt + "|"+ contract_no+"|"+in_cust_no+"|"+ mchnt_cd + "|" + mchnt_txn_ssn+"|"+ out_cust_no+"|"+rem +"|"+ ver;
		}
		System.out.println("签名明文>>>>"+src);
		return src;
	}

	
	public String getVer() {
		return ver;
	}
	public void setVer(String ver) {
		this.ver = ver;
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

	public String getOut_cust_no() {
		return out_cust_no;
	}

	public void setOut_cust_no(String out_cust_no) {
		this.out_cust_no = out_cust_no;
	}

	public String getIn_cust_no() {
		return in_cust_no;
	}

	public void setIn_cust_no(String in_cust_no) {
		this.in_cust_no = in_cust_no;
	}

	public String getAmt() {
		return amt;
	}

	public void setAmt(String amt) {
		this.amt = amt;
	}

	public String getContract_no() {
		return contract_no;
	}

	public void setContract_no(String contract_no) {
		this.contract_no = contract_no;
	}

	public String getRem() {
		return rem;
	}

	public void setRem(String rem) {
		this.rem = rem;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
}
