package com.dutiantech.util;

import java.util.UUID;

public class UIDUtil {

	public static final String generate(){
		String uid = UUID.randomUUID().toString() ;
		
		return uid.replace("-" ,"") ;
	}
	
	public static void main(String[] args){

		System.out.println(UIDUtil.generate() );
		System.out.println(UIDUtil.generate() );
		System.out.println(UIDUtil.generate() );
		System.out.println(UIDUtil.generate() );
		
	}
}
