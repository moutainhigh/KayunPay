package com.dutiantech.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;


/**
 * DES加解密
 * 	
 * 
 * @author wc
 *
 */
public class DESUtil {
	
	private static final String charset = "UTF-8";
	private static final String DES_ALGORITHM = "DES";
	
	private static final SecretKey getKey(byte[] bs) throws NoSuchAlgorithmException{
		/*因为linux下的随即key每次生成结果一样，所以在window下正常解密，但linux下会报错，抛出异常
        KeyGenerator _generator = KeyGenerator.getInstance("DES");  
        _generator.init( new SecureRandom( bs ) );  
        Key key = _generator.generateKey();  
        
        */
	    //防止linux下 随机生成key
	    SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
	    secureRandom.setSeed( bs );
	    
	    // 为我们选择的DES算法生成一个KeyGenerator对象
	    KeyGenerator kg = null;
	    kg = KeyGenerator.getInstance(DES_ALGORITHM);
	    kg.init(secureRandom);
	    //kg.init(56, secureRandom);
	    
	    // 生成密钥
	    return kg.generateKey();
        
	}
	
	/**
	 * 	自定义Key加密
	 * 		所有byte建议用UTF-8编码
	 * 
	 * @param src	原数据
	 * @param key	加密密钥
	 * @return	加密后的byte[]
	 * @throws InvalidKeyException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeySpecException 
	 */
	public static final byte[] encode(byte[] src , byte[] key ) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException{
        byte[] byteFina = null;  
        Cipher cipher;  
        try {  
            cipher = Cipher.getInstance(DES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE , getKey( key ));
            byteFina = cipher.doFinal( src );  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            cipher = null;  
        }  
        return byteFina;  
	}
	
	/**
	 * 	DES解密函数
	 * 
	 * @param byteD
	 * @param key
	 * @return
	 */
	public static final byte[] decode (byte[] byteD , byte[] key) {  
        Cipher cipher;  
        byte[] byteFina = null;  
        try {  
            cipher = Cipher.getInstance("DES/ECB/NoPadding");  
            cipher.init(Cipher.DECRYPT_MODE, getKey(key) );  
            byteFina = cipher.doFinal(byteD);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            cipher = null;  
        }  
        return byteFina;  
  
    }
	
	public static final String encode4string(String src , String key ) throws Exception{
		
		byte[] enc = DESUtil.encode( src.getBytes(charset) , key.getBytes(charset)) ;
		
		return CodeUtil.byteArr2HexStr( enc );
	}

	
	public static final String decode4string( String src , String key ) throws Exception{
		
		byte[] dec = DESUtil.decode( CodeUtil.hexStr2ByteArr(src) , key.getBytes(charset) ) ;  
		
		//return CodeUtil.byteArr2HexStr( dec );
		return new String(dec,charset).trim() ;
	}
	
	public static void main(String[] args) throws Exception {
		String a = encode4string("18971198822", "selangshiwo.com" ) ;
		System.out.println(a);
		a = "6522d96fc16fcad07537ec53bb383046";
		String b = decode4string(a, "selangshiwo.com");
		System.out.println(b);
	}
	
}
