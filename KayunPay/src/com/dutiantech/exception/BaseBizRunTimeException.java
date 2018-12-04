package com.dutiantech.exception;

import com.dutiantech.Message;

public class BaseBizRunTimeException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9167684091646378370L;
	
	private Message msg = null ;
	
	public BaseBizRunTimeException(String return_code , String return_info , Object return_data ){
		msg = Message.error(return_code, return_info, return_data) ;
	}
	
	public Message getExMsg(){
		return msg ;
	}
	
}
