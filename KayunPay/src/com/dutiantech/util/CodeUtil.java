package com.dutiantech.util;

/**
 * 	编码工具类
 * 		byte2hex
 * 
 * @author wc
 *
 */
public class CodeUtil {

    /** 
     * 将byte数组转换为表示16进制值的字符串， 如：byte[]{8,18}转换为：0813， 和public static byte[] 
     * hexStr2ByteArr(String strIn) 互为可逆的转换过程 
 
     *  
     * @param arrB 
     *            需要转换的byte数组 
 
     * @return 转换后的字符串 
     * @throws Exception 
     *             本方法不处理任何异常，所有异常全部抛出 
     */  
    public static String byteArr2HexStr(byte[] arrB) throws Exception {  
        int iLen = arrB.length;  
        // 每个byte用两个字符才能表示，所以字符串的长度是数组长度的两倍  
  
        StringBuffer sb = new StringBuffer(iLen * 2);  
        for (int i = 0; i < iLen; i++) {  
            int intTmp = arrB[i];  
            // 把负数转换为正数  
            while (intTmp < 0) {  
                intTmp = intTmp + 256;  
            }  
            // 小于0F的数需要在前面补0  
            if (intTmp < 16) {  
                sb.append("0");  
            }  
            sb.append(Integer.toString(intTmp, 16));  
        }  
        return sb.toString();  
    } 
    
    /** 
     * 将表示16进制值的字符串转换为byte数组， 和public static String byteArr2HexStr(byte[] arrB) 
     * 互为可逆的转换过程 
 
     *  
     * @param strIn 
     *            需要转换的字符串 
     * @return 转换后的byte数组 
 
     * @throws Exception 
     *             本方法不处理任何异常，所有异常全部抛出 
     */  
    public static byte[] hexStr2ByteArr(String strIn) throws Exception {  
        byte[] arrB = strIn.getBytes();  
        int iLen = arrB.length;  
  
        // 两个字符表示一个字节，所以字节数组长度是字符串长度除以2  
        byte[] arrOut = new byte[iLen / 2];  
        for (int i = 0; i < iLen; i = i + 2) {  
            String strTmp = new String(arrB, i, 2);  
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);  
        }  
        return arrOut;  
    }  
    
    
	private static int startIndex = 5;
	private static int endIndex = 10;

	/**
	 * 生成OPENMAC
	 * 
	 * @param openid
	 * @return
	 * @throws Exception
	 */
	public static String genOpenMac(String openId) throws Exception {

		String md5Val = MD5Code.md5(openId).substring(startIndex, endIndex);

		// String desVal = DESUtil.encode4string( openId + md5Val , desKey ) ;
		String desVal = openId + md5Val;

		return desVal;
	}
	
	public static void main(String[] args) {
		try {
//			for(int i=0;i<10;i++){
//				System.out.println(UIDUtil.generate()+",");
//			}
//			System.out.println(CodeUtil.genOpenMac("adaee77500714e0b8012a20a5655dfd8"));
//			System.out.println(CodeUtil.getOpenId("adaee77500714e0b8012a20a5655dfd8a7d36"));
//			System.out.println(CodeUtil.genOpenMac("bdc9b5c9f31b4c4d9d67c95b7c09cc93"));
//			System.out.println(CodeUtil.getOpenId("adaee77500714e0b8012a20a5655dfd8a7d36"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 从OPENMAC中获取OPENID 返回null的时候为异常
	 * 
	 * @param openIdMac
	 * @return
	 */
	public static String getOpenId(String openMac) {
		String openid = null;
		try {
			// String desValue = DESUtil.decode4string( openMac , desKey ) ;
			String desValue = openMac;

			openid = desValue.substring(0 , 32);

			String oMac = desValue.substring(32 , 37);

			String macValue = MD5Code.md5(openid).substring(startIndex,
					endIndex);

			if (macValue.equals(oMac) == false)
				openid = null;

		} catch (Exception e) {
			openid = null;
		}

		return openid;
	}
	
}
