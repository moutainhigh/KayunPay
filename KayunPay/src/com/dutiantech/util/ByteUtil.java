package com.dutiantech.util;

public class ByteUtil {
	private static String HEX_STR = "0123456789ABCDEF";
	private static String[] BINARY_ARRAY = { "0000", "0001", "0010", "0011",
			"0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
			"1100", "1101", "1110", "1111" };

	/**
	 * 
	 * @param str
	 * @return 转换为二进制字符串
	 */
	public static String bytes2BinaryStr(byte[] bArray) {

		String outStr = "";
		int pos = 0;
		for (byte b : bArray) {
			// 高四位
			pos = (b & 0xF0) >> 4;
			outStr += BINARY_ARRAY[pos];
			// 低四位
			pos = b & 0x0F;
			outStr += BINARY_ARRAY[pos];
		}
		return outStr;

	}

	/**
	 * 
	 * @param bytes
	 * @return 将二进制转换为十六进制字符输出
	 */
	public static String BinaryToHEX_STRing(byte[] bytes) {

		String result = "";
		String hex = "";
		for (int i = 0; i < bytes.length; i++) {
			// 字节高4位
			hex = String.valueOf(HEX_STR.charAt((bytes[i] & 0xF0) >> 4));
			// 字节低4位
			hex += String.valueOf(HEX_STR.charAt(bytes[i] & 0x0F));
			result += hex + "";
		}
		return result;
	}

	/**
	 * 
	 * @param HEX_STRing
	 * @return 将十六进制转换为字节数组
	 */
	public static byte[] HEX_STRingToBinary(String HEX_STRing) {
		// HEX_STRing的长度对2取整，作为bytes的长度
		int len = HEX_STRing.length() / 2;
		byte[] bytes = new byte[len];
		byte high = 0;// 字节高四位
		byte low = 0;// 字节低四位

		for (int i = 0; i < len; i++) {
			// 右移四位得到高位
			high = (byte) ((HEX_STR.indexOf(HEX_STRing.charAt(2 * i))) << 4);
			low = (byte) HEX_STR.indexOf(HEX_STRing.charAt(2 * i + 1));
			bytes[i] = (byte) (high | low);// 高地位做或运算
		}
		return bytes;
	}

	public static String binStr2HEX_STR(String binStr){
		int strLen = binStr.length() ;
		if( strLen%4 != 0 ){
			return null ;
		}
		int index = 0 ;
		int length = 4 ;
		StringBuffer sb = new StringBuffer("") ;
		char[] cs = binStr.toCharArray() ;
		String tempStr = "" ;
		while( index < strLen ){
			tempStr += cs[index];
			index ++ ;
			if( index == length ){
				int hexVal = Integer.parseInt( tempStr , 2) ;
				sb.append( HEX_STR.charAt(hexVal) ) ;
				length += 4 ;
				tempStr = "" ;
			}
		}
		return sb.toString() ;
	}
	
	/**
	 * @param hex
	 * @return
	 */
	/**
	 * @param hexStr
	 * @return
	 */
	public static String HEX_STR2binStr(String hexStr ){
		int strLen = hexStr.length() ;
		if( strLen % 2 != 0 )
			return null ;
		
		int index = 0 ;
		StringBuffer sb = new StringBuffer("");
		while( index < strLen ){
			int cVal = HEX_STR.indexOf( hexStr.charAt( index ) );
			sb.append( BINARY_ARRAY[cVal] ) ;
			index ++ ;
		}
		
		return sb.toString() ;
	}
	
	public static void main(String[] args) {
//		String str = "二进制与十六进制互转测试";  
//        System.out.println("源字符串：\n"+str);  
//          
//        String HEX_STRing = BinaryToHEX_STRing(str.getBytes());  
//        System.out.println("转换为十六进制：\n"+HEX_STRing);  
//        System.out.println("转换为二进制：\n"+bytes2BinaryStr(str.getBytes()));  
//          
//        byte [] bArray = HEX_STRingToBinary(HEX_STRing);  
//        System.out.println("将str的十六进制文件转换为二进制再转为String：\n"+new String(bArray));
		String v = binStr2HEX_STR("1101101000101101") ;
		System.out.println( v );
		System.out.println( HEX_STR2binStr( v ));
	}

}
