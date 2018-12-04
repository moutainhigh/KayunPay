package com.jx.util;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class RSAUtils {


    /** *//**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";

    /** *//**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 256;

	  /** ָ�������㷨ΪRSA */
    private static final String ALGORITHM = "RSA";

    private static final String PROVIDER = "RSA/ECB/NoPadding";
    /** 密码加密RSA长度位 */
    private static final int KEYSIZE = 1024;
    /** ָ����Կ�Դ���ļ� */
    private static Map<Integer,KeyPair> KeyList = new HashMap<Integer,KeyPair>();


    /** *//**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;
    protected static final String TMP_BANKCODE = "30040000";
    //private static Properties bankConfig = new Properties();
    //private static final String PATH="transfer.file.keyPath";


    /**
     * ������Կ��
     * @param index ��Կ����
     * @throws Exception
     */
    private static void generateKeyPair(Integer index) throws Exception {

        /** ΪRSA�㷨����һ��KeyPairGenerator���� */
    	KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);

        /** ����������������Դ��ʼ�����KeyPairGenerator���� */
    	keyPairGenerator.initialize(KEYSIZE);

        /** ������Կ�� */
    	KeyPair keyPair = keyPairGenerator.generateKeyPair();

        KeyList.put(index, keyPair);
    }

    private static String getModulus(Integer index){
    	RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyList.get(index).getPublic();
    	return new String(Hex.encodeHex(rsaPublicKey.getModulus().toByteArray()));
    }

    private static String getPublicExponent(Integer index){
    	RSAPublicKey rsaPublicKey = (RSAPublicKey) KeyList.get(index).getPublic();
    	return new String(Hex.encodeHex(rsaPublicKey.getPublicExponent().toByteArray()));
    }

    /**
     * ��ù�Կ
     * @param index ��Կ���� ���û��������
     * @return ���� �����һλΪModulus �ڶ�λΪ PublicExponent
     */
    public static String[] getPublicKeys(Integer index){
    	String[] result = new String[2];
    	if(!KeyList.containsKey(index)){
    		try {
				generateKeyPair(index);
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	result[0] = getModulus(index).substring(2);
    	result[1] = getPublicExponent(index).substring(1);
    	return result;

    }

    /**
     * ���ܷ���
     * @param source Դ����
     * @return
     * @throws Exception
     */
    public static String encrypt(String source, Integer index) throws Exception {
        Key publicKey = KeyList.get(index).getPublic();

        /** �õ�Cipher������ʵ�ֶ�Դ���ݵ�RSA���� */
        Cipher cipher = Cipher.getInstance(PROVIDER);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b = source.getBytes();
        /** ִ�м��ܲ��� */
        byte[] b1 = cipher.doFinal(b);

        return new String(Hex.encodeHex(b1));
    }

    /**
     * �����㷨
     * @param cryptograph    ����
     * @param index ��Կ����
     * @return
     * @throws Exception
     */
    public static String decrypt(String cryptograph,Integer index) throws Exception {
        Key privateKey = KeyList.get(index).getPrivate();

        /** �õ�Cipher��������ù�Կ���ܵ����ݽ���RSA���� */
        Cipher cipher = Cipher.getInstance(PROVIDER);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] b1 = Hex.decodeHex(cryptograph.toCharArray());

        /** ִ�н��ܲ��� */
        byte[] b = cipher.doFinal(b1);
        return new String(b);
    }

    /**
	 * 根据私钥文件读取签名私钥
	 * @param signPrivatePath
	 * @return
	 */
	@SuppressWarnings("resource")
	public static String getSignPrivateKey4Client(String signPrivatePath){
		StringBuffer privateBuffer=new StringBuffer();
		try {
			InputStream inputStream = new FileInputStream(signPrivatePath);
			InputStreamReader inputReader = new InputStreamReader(inputStream);
			BufferedReader bufferReader = new BufferedReader(inputReader);
			// 读取一行
			String line = "";
			while ((line=bufferReader.readLine())!=null) {
				privateBuffer.append(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return privateBuffer.toString();
	}
	/**
	 * 根据公钥文件读取验签公钥
	 * @param signPublicPath
	 * @return
	 */
	public static String getVerifyKey4Client(String signPublicPath){
		String key="";
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			InputStream in = new FileInputStream(signPublicPath);
			//生成一个证书对象并使用从输入流 inStream 中读取的数据对它进行初始化。
			Certificate c = cf.generateCertificate(in);
			PublicKey publicKey = c.getPublicKey();
			key=new BASE64Encoder().encodeBuffer(publicKey.getEncoded());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}

    public static void main(String[] args) throws Exception {
        String privatePath="D:/DEMO/kevinPrivate.key";
        String publicPath="D:/DEMO/kevinPublic.cer";
        File file1 =new File(privatePath);
        File file2 =new File(publicPath);
        ObjectOutputStream oos1 = null;
        ObjectOutputStream oos2 = null;
        try {

            // Generate a 1024-bit RSA key pair
            KeyPairGenerator  keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            KeyPair  keypair = keyGen.genKeyPair();
            PrivateKey  privateKey = keypair.getPrivate();
            PublicKey   publicKey = keypair.getPublic();

            /** 用对象流将生成的密钥写入文件 */
            oos1 = new ObjectOutputStream(new FileOutputStream(file1));
            oos2 = new ObjectOutputStream(new FileOutputStream(file2));
            oos1.writeObject(publicKey);
            oos2.writeObject(privateKey);


        } catch (java.security.NoSuchAlgorithmException e) {
            throw e;
        }finally {
            /** 清空缓存，关闭文件输出流 */
            oos1.close();
            oos2.close();
        }
    }



    /** *//**
     * <P>
     * 私钥解密
     * </p>
     *
     * @param encryptedData 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = Base64Utils.decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }
    /**
     * 加密方法
     *
     * @param source 源数据
     * @return
     * @throws Exception
     */
    public static String encryptRSA(String source,String path) throws Exception {
        //generateKeyPair();
        Key publicKey;
        ObjectInputStream ois = null;
        try {
            /** 将文件中的公钥对象读出 */
            ois = new ObjectInputStream(new FileInputStream(
                    path));
            publicKey = (Key) ois.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            ois.close();
        }

        /** 得到Cipher对象来实现对源数据的RSA加密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] b = source.getBytes("UTF-8");
        int inputLen = b.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(b, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(b, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        out.close();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(out.toByteArray());
    }

    /**
     * 解密算法 
     *
     * @param cryptograph 密文
     * @return
     * @throws Exception
     */
    public static String decryptRSA(String cryptograph,String path) throws Exception {
        Key privateKey;
        ObjectInputStream ois = null;
        try {
            /** 将文件中的私钥对象读出 */
            ois = new ObjectInputStream(new FileInputStream(
                    path));
            privateKey = (Key) ois.readObject();
        } catch (Exception e) {
            throw e;
        } finally {
            ois.close();
        }

        /** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b1 = decoder.decodeBuffer(cryptograph);
        int inputLen = b1.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(b1, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(b1, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        /** 执行解密操作 */
        out.close();
        return new String(out.toByteArray(),"UTF-8");
    }


    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @param keyPath  加密密码
     * @return
     */
    public static byte[] encryptByAES(String content, String keyPath) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(keyPath.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            byte[] byteContent = content.getBytes("UTF-8");
            cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(byteContent);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**解密
     * @param content  待解密内容
     * @param keyPath 解密密钥
     * @return
     */
    public static byte[] decryptByAES(byte[] content, String keyPath) {
            try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(keyPath.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");// 创建密码器
            cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
            byte[] result = cipher.doFinal(content);
            return result; // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**将二进制转换成16进制
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
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**将16进制转换为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length()/2];
        for (int i = 0;i< hexStr.length()/2; i++) {
            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 加密
     * 读取旧的文件的内容加密后写入新的文件
     * @param file1,file2 想要读取的文件对象
     * @return 返回文件内容
     */
    public static String encryptHand(String keyPath,File file1,File file2){
        String result="";
        String line = "";
        byte[] bt;
        String encryptResultStr="";
        try {
            BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (file1),"UTF-8"));
            BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (file2),"UTF-8"));
            if((line=br.readLine())!=null){
        		bt=encryptByAES(result + line, keyPath);//逐行加密
                encryptResultStr =parseByte2HexStr(bt);
                bw.write(encryptResultStr);
        	}
            while((line=br.readLine())!=null){
                bt=encryptByAES(result + line, keyPath);//逐行加密
                encryptResultStr =parseByte2HexStr(bt);
                bw.newLine();
                bw.write(encryptResultStr);
            }
            bw.flush();
            bw.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     *  解密
     *  读取旧的加密文件的内容解密后写入新的文件
     * @param file1,file2 想要读取的文件对象
     * @return 返回文件内容
     */
    public static void decryptHand(String keyPath,File file1,File file2){
        String line ="";
        byte[] decryptFrom=null;
        byte[] decryptResult=null;

        try{
            BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream(file1),"UTF-8"));
            BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream(file2),"UTF-8"));
            if((line = br.readLine())!=null){
        		//解密
                String result="";
                decryptFrom =parseHexStr2Byte(result+line);
                decryptResult = decryptByAES(decryptFrom, keyPath);//逐行解密
                result=new String(decryptResult,"GBK");
                bw.write(result);
        	}
            while((line = br.readLine())!=null){
                //解密
                String result="";
                decryptFrom =parseHexStr2Byte(result+line);
                decryptResult = decryptByAES(decryptFrom, keyPath);//逐行解密
                result=new String(decryptResult,"GBK");
                bw.write(result);
            }
            bw.flush();
            bw.close();
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    
    
    /**
     * 根据公钥字符串加密方法
     *
     * @param source 源数据
     * @param publicKey 公钥字符串
     * @return
     * @throws Exception
     */
    public static String encryptRSAs(String source,String publicKey) throws Exception {
        /** 得到Cipher对象来实现对源数据的RSA加密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getPubKey(publicKey));
        byte[] b = source.getBytes("UTF-8");
        int inputLen = b.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(b, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(b, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        out.close();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(out.toByteArray());
    }

    /**
     * 根据私钥字符串解密算法 
     *
     * @param source 密文
     * @param privateKey 私钥字符串
     * @return
     * @throws Exception
     */
    public static String decryptRSAs(String source,String privateKey) throws Exception {
        /** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b1 = decoder.decodeBuffer(source);
        int inputLen = b1.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(b1, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(b1, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        /** 执行解密操作 */
        out.close();
        return new String(out.toByteArray(),"UTF-8");
    }



    /**
     * 实例化公钥
     * @s 公钥
     * @return
     */
    public static PublicKey getPubKey(String s ) throws Exception {
        PublicKey publicKey = null;
        java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
                new BASE64Decoder().decodeBuffer(s));
        // RSA对称加密算法
        java.security.KeyFactory keyFactory;
        keyFactory = java.security.KeyFactory.getInstance("RSA");
        // 取公钥匙对象
        publicKey = keyFactory.generatePublic(bobPubKeySpec);

        return publicKey;
    }

    /**
     * 实例化私钥
     *
     * @s 私钥
     * @return
     */
    public static PrivateKey getPrivateKey(String s) throws Exception{
        PrivateKey privateKey = null;
        PKCS8EncodedKeySpec priPKCS8;
        priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(s));
        KeyFactory keyf = KeyFactory.getInstance("RSA");
        privateKey = keyf.generatePrivate(priPKCS8);
        return privateKey;
    }
    
    /**
     *  用私钥对信息生成数字签名
     * @param b  数据
     * @param privateKey    私钥
     * @return
     * @throws Exception
     */
    public static String MD5WithRSASign(byte[] b,String privateKey)throws Exception{
        //解密私钥
        byte[] keyBytes = decryptBASE64(privateKey);
        //构造PKCS8EncodedKeySpec对象
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //取私钥匙对象
        PrivateKey privateKey2 = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
        //用私钥对信息生成数字签名
        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initSign(privateKey2);
        signature.update(b);

        return encryptBASE64(signature.sign());
    }

    /**
     * 校验数字签名
     * @param b  数据
     * @param publicKey 公钥
     * @param sign  数字签名
     * @return
     * @throws Exception
     */
    public static boolean MD5WithRSAVerify(byte[] b,String publicKey,String sign)throws Exception{
        //解密公钥
        byte[] keyBytes = decryptBASE64(publicKey);
        //构造X509EncodedKeySpec对象
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(keyBytes);
        //指定加密算法
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        //取公钥匙对象
        PublicKey publicKey2 = keyFactory.generatePublic(x509EncodedKeySpec);

        Signature signature = Signature.getInstance("MD5withRSA");
        signature.initVerify(publicKey2);
        signature.update(b);
        //验证签名是否正常
        return signature.verify(decryptBASE64(sign));
    }
    
    /**
     * BASE64解密
     * @param key
     * @return
     * @throws Exception
     */
	public static byte[] decryptBASE64(String key) throws Exception{
        return (new BASE64Decoder()).decodeBuffer(key);
    }

    /**
     * BASE64加密
     * @param key
     * @return
     * @throws Exception
     */
	public static String encryptBASE64(byte[] key)throws Exception{
        return (new BASE64Encoder()).encodeBuffer(key);
    }

	 /**
     * 获取文件的MD5值
     * @param 文件名
     * @return md5值
     * @throws Exception
     */
    public static byte[] getFileMD5String(File file) throws IOException, NoSuchAlgorithmException{
		FileInputStream in = new FileInputStream(file);
		FileChannel ch = in.getChannel();
		MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0,
				file.length());
		MessageDigest messagedigest = MessageDigest.getInstance("MD5");
		messagedigest.update(byteBuffer);
		ch.close();
		in.close();
		return messagedigest.digest();
	}

    /**
     * 根据公钥字符串加密方法
     *
     * @param source 源数据
     * @param publicKey 公钥字符串
     * @return
     * @throws Exception
     */
    public static String encryptRSAByte(byte[] source,String publicKey) throws Exception {
        /** 得到Cipher对象来实现对源数据的RSA加密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, getPubKey(publicKey));
        int inputLen = source.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(source, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(source, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        out.close();
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(out.toByteArray());
    }
    
    /**
     * 根据私钥字符串解密算法 
     *
     * @param source 密文
     * @param privateKey 私钥字符串
     * @return
     * @throws Exception
     */
    public static byte[] decryptRSAByte(String source,String privateKey) throws Exception {
        /** 得到Cipher对象对已用公钥加密的数据进行RSA解密 */
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey(privateKey));
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] b1 = decoder.decodeBuffer(source);
        int inputLen = b1.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(b1, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(b1, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        /** 执行解密操作 */
        out.close();
        return out.toByteArray();
    }
}



