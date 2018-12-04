package com.dutiantech.plugin.syxpay.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;


public class RSA
{

    public static final String SIGN_ALGORITHMS = "SHA1WithRSA";

    public static String sign(String content , String priKey ){
    	String tmpSign = sign(content , priKey , "UTF-8") ;
    	return Base64.encode( tmpSign.getBytes());
    }
    
    /**
     * RSA签名
     * 
     * @param content
     *            待签名数据
     * @param privateKey
     *            商户私钥
     * @param input_charset
     *            编码格式
     * @return 签名值
     */
    public static String sign(String content, String privateKey,
            String input_charset)
    {
        try
        {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
                    Base64.decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update(content.getBytes(input_charset));

            byte[] signed = signature.sign();

            return Base64.encode(signed);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * RSA验签名检查
     * 
     * @param content
     *            待签名数据
     * @param sign
     *            签名值
     * @param allscore_public_key
     *            支付宝公钥
     * @param input_charset
     *            编码格式
     * @return 布尔值
     */
    public static boolean doCheck(String content, String sign,
            String allscore_public_key, String input_charset)
    {
        try
        {
//        	sign = URLDecoder.decode(sign,input_charset);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.decode(allscore_public_key);
            PublicKey pubKey = keyFactory
                    .generatePublic(new X509EncodedKeySpec(encodedKey));

            java.security.Signature signature = java.security.Signature
                    .getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes(input_charset));
            
            sign = sign.replaceAll("\\*", "+");
        	sign = sign.replaceAll("-", "/");
        	
        	//have a test
        	sign = new String(Base64.decode(sign)) ;
        	
            boolean bverify = signature.verify(Base64.decode(sign));
            return bverify;

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 得到私钥
     * 
     * @param key
     *            密钥字符串（经过base64编码）
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception
    {

        byte[] keyBytes;

        keyBytes = Base64.decode(key);

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        return privateKey;
    }
    
    
   
}
