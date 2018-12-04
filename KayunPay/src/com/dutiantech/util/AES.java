package com.dutiantech.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	String key;
	String initVector;

	public AES(String key) {
		this.key = key;
		this.initVector = key;
	}

	public String encrypt(String value) {
		try {
			int length = 16;
			int textLength = value.getBytes("UTF-8").length;
			for (int i = 0; i < length - (textLength % length); i++) {
				value += "\0";
			}
			IvParameterSpec iv = new IvParameterSpec(this.initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(this.key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());

			return parseByte2HexStr(encrypted).trim();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public String decrypt(String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(this.initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(this.key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(parseHexStr2Byte(encrypted));

			return new String(original).trim();
//			return parseByte2HexStr(original);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		
		//String enstr = "29f48b74a40fc8c2ce0bf572f63920e54d025d24cf827180299d6d3c929b8107be71b3fddebd2a2e8b943dc20edd8d3197dc817e8a9036005883c29ba80b12127d811290f3c29121dc49b0d460b10d2c5cdc37b5d5f3d0c1903eda0fb6b15c6bce3b6e7c0ed50156036c6b278bbb3a0e";
		String enstr = "f2f1d916d4544169cd55c63d8d817c4573d0f35dd488687befd911d1eb43cee50a50f81c0ce463031456ad342a8dddc7 ";
		AES aes = new AES("97166e0a5033b943");
		String ret = aes.decrypt(enstr);
		System.out.println(ret);
		
	}

}