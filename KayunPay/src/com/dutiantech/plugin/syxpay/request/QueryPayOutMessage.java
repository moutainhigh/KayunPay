package com.dutiantech.plugin.syxpay.request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.dutiantech.plugin.syxpay.SYXPayKit;
import com.dutiantech.plugin.syxpay.util.RSA;
import com.dutiantech.plugin.syxpay.util.RSAEncrypt;

public class QueryPayOutMessage extends TreeMap<String, String>{

	private static final long serialVersionUID = 5395318131212794978L;
	
	private List<String> encodeFields = new ArrayList<String>(); 
	
	public void setEncodeFields(String fields){
		String[] fs = fields.split("\\|") ;
		for(String f : fs){
			encodeFields.add(f) ;
		}
	}
	
	private String makeSignFields(boolean isEncode ){
		StringBuffer buff = new StringBuffer();
		Set<String> keys = this.keySet() ;
		for (String key : keys) {
			String value = this.get(key) ;
			if( value != null & "".equals(value) == false ){
				if (buff.length() != 0) {
					buff.append("&");
				}
				if( isEncode ){
					try {
						value = URLEncoder.encode( value , this.get("inputCharset") ) ;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}else{
					if( encodeFields.contains(key) == true ){
						try {
							value = RSAEncrypt.encryptForPrKey( SYXPayKit._PRIVATE_KEY , value );
							this.put(key, value) ;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				
				buff.append(key + "=" + value );
			}
		}
		return buff.toString() ;
	}
	
	public boolean isCheckSign(){
		String signData = this.get("sign" ) ;
		this.remove(signData) ;
		String signSrc = makeSignFields( false ) ;
		
		return RSA.doCheck( signSrc , signData, SYXPayKit._PUBLIC_KEY , "UTF-8" ) ;
	}
	
	public String toRequest(){
		this.put("service", "withhold");
		this.put("format", "json");
		this.put("merchantId", SYXPayKit._MERCHANTID );
		this.put("inputCharset", "UTF-8");
		String tt = makeSignFields( false ) ;
		String signData = RSA.sign( tt , SYXPayKit._PRIVATE_KEY  ) ;
		this.put("sign", signData ) ;
		this.put("signType", "RSA" );
		
		return makeSignFields(true);
	}

}
