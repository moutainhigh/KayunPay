package com.dutiantech.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Number {

	final static char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
			'9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y',
			'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
			'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y',
			'Z' };

	final static Map<Character, Integer> digitMap = new HashMap<Character, Integer>();

	static {
		for (int i = 0; i < digits.length; i++) {
			digitMap.put(digits[i], (int) i);
		}
	}

	/**
	 * 支持的最大进制数
	 */
	public static final int MAX_RADIX = digits.length;

	/**
	 * 支持的最小进制数
	 */
	public static final int MIN_RADIX = 2;

	/**
	 * 将长整型数值转换为指定的进制数（最大支持62进制，字母数字已经用尽）
	 * 
	 * @param i
	 * @param radix
	 * @return
	 */
	public static String toString(long i, int radix) {
		if (radix < MIN_RADIX || radix > MAX_RADIX)
			radix = 10;
		if (radix == 10)
			return Long.toString(i);

		final int size = 65;
		int charPos = 64;

		char[] buf = new char[size];
		boolean negative = (i < 0);

		if (!negative) {
			i = -i;
		}

		while (i <= -radix) {
			buf[charPos--] = digits[(int) (-(i % radix))];
			i = i / radix;
		}
		buf[charPos] = digits[(int) (-i)];

		if (negative) {
			buf[--charPos] = '-';
		}

		return new String(buf, charPos, (size - charPos));
	}

	static NumberFormatException forInputString(String s) {
		return new NumberFormatException("For input string: \"" + s + "\"");
	}

	/**
	 * 将字符串转换为长整型数字
	 * 
	 * @param s
	 *            数字字符串
	 * @param radix
	 *            进制数
	 * @return
	 */
	public static long toNumber(String s, int radix) {
		if (s == null) {
			throw new NumberFormatException("null");
		}

		if (radix < MIN_RADIX) {
			throw new NumberFormatException("radix " + radix
					+ " less than Numbers.MIN_RADIX");
		}
		if (radix > MAX_RADIX) {
			throw new NumberFormatException("radix " + radix
					+ " greater than Numbers.MAX_RADIX");
		}

		long result = 0;
		boolean negative = false;
		int i = 0, len = s.length();
		long limit = -Long.MAX_VALUE;
		long multmin;
		Integer digit;

		if (len > 0) {
			char firstChar = s.charAt(0);
			if (firstChar < '0') {
				if (firstChar == '-') {
					negative = true;
					limit = Long.MIN_VALUE;
				} else if (firstChar != '+')
					throw forInputString(s);

				if (len == 1) {
					throw forInputString(s);
				}
				i++;
			}
			multmin = limit / radix;
			while (i < len) {
				digit = digitMap.get(s.charAt(i++));
				if (digit == null) {
					throw forInputString(s);
				}
				if (digit < 0) {
					throw forInputString(s);
				}
				if (result < multmin) {
					throw forInputString(s);
				}
				result *= radix;
				if (result < limit + digit) {
					throw forInputString(s);
				}
				result -= digit;
			}
		} else {
			throw forInputString(s);
		}
		return negative ? result : -result;
	}
	/**
	 * 数据格式化 2位小数 不作运算 rain 2017.7.20
	 * */
	public static String doubleToStr(double num) {


		double num1 = (double) num / 100;

		
		DecimalFormat df = new DecimalFormat ( "###,##0.00" ) ;
		return df.format (num1) ;
	}
	
	/**
	 * 数据格式化 2位小数  四舍五入
	 * */
	public static String doubleToStrByTwoDecimal(double num) {
		BigDecimal   b   =   new   BigDecimal(num);
		double   f1   =   b.setScale(2,   RoundingMode.HALF_UP).doubleValue();
		DecimalFormat df = new DecimalFormat ( "###,##0.00" ) ;
		return df.format (f1) ;
	}
	/**
	 * 数据格式化 2位小数 不作运算 rain 2017.7.20
	 * */
	public static String longToStr(long num) {
		DecimalFormat df = new DecimalFormat ( "###,##0.00" ) ;
		return df.format (num) ;
	}
	/**
	 * 数据格式化 2位小数 rain 2017.7.20
	 * */
	public static String longToString(long num) {

		double num1 = (double) num / 100;

		
		DecimalFormat df = new DecimalFormat ( "###,##0.00" ) ;
		return df.format (num1) ;
	}
	/**
	 * 不带逗号
	 * */
	public static String longToString2(long num) {

		double num1 = (double) num / 100;

		
		DecimalFormat df = new DecimalFormat ( "#####0.00" ) ;
		return df.format (num1) ;
	}
	/**
	 * 数据格式化 1位小数 rain 2017.7.20
	 * */
	public static String longToString1(long num) {

		double num1 = (double) num / 100;

		
		DecimalFormat df = new DecimalFormat ( "###,##0.0" ) ;
		return df.format (num1) ;
	}
	
	
	/*
	 * **/
    public static long ObjectTOLong(Object longObj)  
    {  
        return Long.valueOf(String.valueOf(longObj));  
    }  

    public static String ObjectToString(Object object) {
    	DecimalFormat decimalFormat = new DecimalFormat("######.#######");
    	return decimalFormat.format(object);
    }
    
	 /**
	  * 判断字符串是否是数字(限两位小数)
	  * @param str
	  * @return
	  */
	 public static boolean isNumber(String str){ 
       Pattern pattern=Pattern.compile("^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"); // 判断小数点后2位的数字的正则表达式
       Matcher match=pattern.matcher(str); 
       if(match.matches()==false){ 
          return false; 
       }else{ 
          return true; 
       } 
	 }
    
}
