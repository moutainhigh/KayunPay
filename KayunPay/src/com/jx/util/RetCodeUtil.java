package com.jx.util;

public class RetCodeUtil {
	/**
	 * 根据retCode判断是否成功 WJW
	 * @param retCode
	 * @return
	 */
	public static boolean isSuccRetCode(String retCode){
		return "00000000".equals(retCode);
	}
}
