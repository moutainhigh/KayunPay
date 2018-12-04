package com.fuiou.util;

public class StringUtil{
	public static boolean isNotEmpty(String str)
	{
		return ((str != null) && (str.trim().length() > 0));
	}

	public static boolean isEmpty(String aStr)
	{
		return ((aStr == null) || (aStr.trim().length() == 0));
	}
}