package com.dutiantech.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Code {
	
	private static String seed = "selangshiwo.com";
	private static String chatset = "UTF-8";
	
	public static String md5(String src) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5"); 
		src += seed ;
		md.update(src.getBytes(chatset));
		byte b[] = md.digest(); 
//		int i;
//
//		StringBuffer buf = new StringBuffer("");
//		for (int offset = 0; offset < b.length; offset++) {
//			i = b[offset];
//			if(i<0) i+= 256;
//			if(i<16)
//			buf.append("0");
//			buf.append(Integer.toHexString(i));
//		}
		
		return CodeUtil.byteArr2HexStr(b);
	}
	
	public static String md5(String src, String se) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5"); 
		if(StringUtil.isBlank(se)) {
			se = seed;
		}
		src += se ;
		md.update(src.getBytes(chatset));
		byte b[] = md.digest(); 
		return CodeUtil.byteArr2HexStr(b);
	}
	
	public static String md5Pure(String src ) throws Exception{
		MessageDigest md = MessageDigest.getInstance("MD5"); 
		md.update(src.getBytes(chatset));
		byte b[] = md.digest(); 
		return CodeUtil.byteArr2HexStr(b);
	}
	
	public static String crypt(String str) throws UnsupportedEncodingException
	{
		if (str == null || str.length() == 0) {
		}
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes("UTF-8"));
			byte[] hash = md.digest();
			
			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0" + Integer.toHexString((0xFF & hash[i])));
				}				
				else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}				
			}
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString.toString();
	}
	
    public static String SHA1(String decript) {
        try {
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("SHA-1");
            digest.update(decript.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();
 
        } catch (NoSuchAlgorithmException e) {
            //return "";
        }
        return "";
    }
	
	public static void main(String[] args) throws Exception{
	}
	
}
