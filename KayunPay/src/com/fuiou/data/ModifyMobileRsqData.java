package com.fuiou.data;

import java.io.Serializable;


/**
 * 
 * @author aj
 *
 */
public class ModifyMobileRsqData extends PublicClassRsqData implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String login_id= "";
	private String new_mobile= "";
	private String signature= "";
	
	public String createSignValue(){
		String src = login_id + "|" + mchnt_cd + "|" + mchnt_txn_ssn +"|" + new_mobile + "|"+ resp_code + "|" + resp_desc;
		System.out.println("回调个人PC/APP端更换手机号接口验证签名明文"+src);
		return src;
	}

	public String getLogin_id() {
		return login_id;
	}

	public void setLogin_id(String login_id) {
		this.login_id = login_id;
	}

	public String getNew_mobile() {
		return new_mobile;
	}

	public void setNew_mobile(String new_mobile) {
		this.new_mobile = new_mobile;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	
	
	
	
	
}
