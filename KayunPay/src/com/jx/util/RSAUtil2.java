package com.jx.util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.*;
import java.security.*;
import java.security.cert.*;
import java.security.spec.PKCS8EncodedKeySpec;

/**
 * Created by SYSTEM on 2015/8/6.
 */
public class RSAUtil2 {

    /**
     * 指定加密算法为RSA
     */
    private static final String ALGORITHM = "RSA";

    /**
     * 指定公钥存放文件
     */
    private static String PUBLIC_KEY_FILE = "D:/DEMO/webservice_publickey.cer";
    /**
     * 指定私钥存放文件
     */
    private static String PRIVATE_KEY_FILE = "D:/DEMO/webservice_privatekey.key";

    /** *//**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 256;


    /** *//**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 245;


    /**
     * 加密方法
     *
     * @param source 源数据
     * @return
     * @throws Exception
     */
    public static String encrypt(String source) throws Exception {
        Key publicKey;
        ObjectInputStream ois = null;
        try {
            /** 将文件中的公钥对象读出 */
            ois = new ObjectInputStream(new FileInputStream(
                    PUBLIC_KEY_FILE));
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
    public static String decryptRSA(String cryptograph) throws Exception {
        Key privateKey;
        ObjectInputStream ois = null;

        try {
            /** 将文件中的私钥对象读出 */
            ois = new ObjectInputStream(new FileInputStream(
                    PRIVATE_KEY_FILE));
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

        return new String(out.toByteArray(),"UTF-8");
    }



    /****
     * 加载密钥库，加载了以后，我们就能通过相应的方法获得私钥，也可以获得数字证书
     * @param keyStorePath 密钥库路径
     * @param password 密码
     * @return  密钥库
     * @throws Exception
     */
    private static KeyStore getKeyStore(String keyStorePath,String password) throws Exception{
        //实例化密钥库
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        //获得密钥库文件流
        FileInputStream is = new FileInputStream(keyStorePath);
        //加载密钥库
        ks.load(is,password.toCharArray());
        //关闭密钥库文件流
        is.close();
        return ks;
    }

    /****
     * 加载数字证书，JAVA 6仅支持x.509的数字证书
     * @param certificatePath  证书路径
     * @return   证书
     * @throws Exception
     */
    private static java.security.cert.Certificate getCertificate(String certificatePath) throws Exception{
        //实例化证书工厂
        CertificateFactory certificateFactory = CertificateFactory.getInstance("x.509");
        //取得证书文件流
        FileInputStream in = new FileInputStream(certificatePath);
        //生成证书
        java.security.cert.Certificate certificate =  certificateFactory.generateCertificate(in);
        //关闭证书文件流
        in.close();
        return certificate;
    }

    /****
     * 获得Certificate
     * @param keyStorePath 密钥库路径
     * @param alias 别名
     * @param password  密码
     * @return  证书
     * @throws Exception
     */
    private static java.security.cert.Certificate getCertificate(String keyStorePath, String alias, String password) throws Exception{
        //由密钥库获得数字证书构建数字签名对象
        //获得密钥库
        KeyStore ks = getKeyStore(keyStorePath,password);
        //获得证书
        return ks.getCertificate(alias);
    }


    /****
     * 由Certificate获得公钥，获得公钥后，通过RSA算方法实现进行"私钥加密，公钥解密"和"公钥加密，私钥解密"操作
     * @param certificatePath  证书路径
     * @return 公钥
     */
    private static PublicKey getPublicKeyByCertificate(String certificatePath)throws Exception {
        //获得证书
        java.security.cert.Certificate certificate = getCertificate(certificatePath);
        //获得公钥
        return certificate.getPublicKey();
    }



    /****
     * 公钥加密
     * @param data  等待加密数据
     * @param certificatePath  证书路径
     * @return   加密数据
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data,String certificatePath) throws Exception{
        //取得公钥
        PublicKey publicKey = getPublicKeyByCertificate(certificatePath);
        //对数据加密
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE,publicKey);
        return cipher.doFinal(data);
    }

    /****
     * 公钥解密
     * @param data  等待解密的数据
     * @param certificatePath  证书路径
     * @return  解密数据
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] data,String certificatePath)throws Exception{
        //取得公钥
        PublicKey publicKey = getPublicKeyByCertificate(certificatePath);
        //对数据解密
        Cipher cipher = Cipher.getInstance(publicKey.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /****
     * 获得私钥，获得私钥后，通过RSA算方法实现进行"私钥加密，公钥解密"和"公钥加密，私钥解密"操作
     * @param keyStorePath 密钥库路径
     * @param alias 别名
     * @param password 密码
     * @return  私钥
     */
    private static PrivateKey getPrivateKeyByKeyStore(String keyStorePath,String alias,String password)throws Exception{
        //获得密钥库
        KeyStore ks = getKeyStore(keyStorePath,password);
        //获得私钥
        return  (PrivateKey)ks.getKey(alias, password.toCharArray());

    }

    /****
     * 私钥加密
     * @param data  待加密的数据
     * @param keyStorePath  密钥库路径
     * @param alias  别名
     * @param password   密码
     * @return  加密数据
     * @throws Exception
     */
    public static byte[] encryptByPriateKey(byte[] data, String keyStorePath,String alias,String password) throws Exception{
        //获得私钥
        PrivateKey privateKey = getPrivateKeyByKeyStore(keyStorePath,alias,password);
        //对数据加密
        Cipher cipher = Cipher.getInstance(privateKey.getAlgorithm());
        return cipher.doFinal(data);
    }

    /****
     * 私钥解密
     * @param data  待解密数据
     * @param keyStorePath 密钥库路径
     * @param alias  别名
     * @param password  密码
     * @return  解密数据
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] data,String keyStorePath,String alias,String password) throws Exception{
        //取得私钥
        PrivateKey privateKey = getPrivateKeyByKeyStore(keyStorePath,alias,password);
        //对数据解密
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        return cipher.doFinal(data);
    }

    /****
     * 验证签名
     * @param data  数据
     * @param sign  签名
     * @param certificatePath  证书路径
     * @return  验证通过为真
     * @throws Exception
     */
    public static boolean verify(byte[] data,byte[] sign,String certificatePath) throws Exception{
        //获得证书
        X509Certificate x509Certificate = (X509Certificate)getCertificate(certificatePath);
        //由证书构建签名
        Signature signature = Signature.getInstance("MD5WithRSA");
        //由证书初始化签名，实际上是使用了证书中的公钥
        signature.initVerify(x509Certificate);
        signature.update(data);
        return signature.verify(sign);
    }



    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data byte[]
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data) {
        return encodeHexStr(data,true);
    }

    /**
     * 用于建立十六进制字符的输出的小写字符数组
     */
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * 用于建立十六进制字符的输出的大写字符数组
     */
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data        byte[]
     * @param toLowerCase <code>true</code> 传换成小写格式 ， <code>false</code> 传换成大写格式
     * @return 十六进制String
     */
    public static String encodeHexStr(byte[] data, boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }
    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制String
     */
    protected static String encodeHexStr(byte[] data, char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }

    /**
     * 将字节数组转换为十六进制字符数组
     *
     * @param data     byte[]
     * @param toDigits 用于控制输出的char[]
     * @return 十六进制char[]
     */
    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    /****
     * @param sign  签名
     * @param keyStorePath 密钥库路径
     * @param alias 别名
     * @param password 密码
     * @return 签名
     * @throws Exception
     */
    @SuppressWarnings("unused")
	public static byte[] sign(byte[] sign,String keyStorePath,String alias,String password)throws Exception{
        //获得证书
        X509Certificate x509Certificate = (X509Certificate) getCertificate(keyStorePath,alias,password);
        //构建签名,由证书指定签名算法
        Signature signature = Signature.getInstance("MD5WithRSA");
        //获取私钥
        PrivateKey privateKey = getPrivateKeyByKeyStore(keyStorePath,alias,password);
        //初始化签名，由私钥构建
        signature.initSign(privateKey);
        signature.update(sign);
        return signature.sign();
    }



    public static void main(String[] args) throws Exception {


        String inputStrs = "签名";
        byte[] datas = inputStrs.getBytes();
        System.err.println("私钥签名---公钥验证");
        //产生签名
        byte[] sign =sign(datas, "D:\\DEMO\\10001.keystore", "10001", "123456");
        System.err.println("签名:" + encodeHexStr(sign,true));
        //验证签名
        boolean status = verify(datas, sign, "D:\\DEMO\\CA\\new10001.cer");
        System.err.println("状态： " + status);

        if(status==true)
        {
            System.err.println("公钥加密---私钥解密");
            String inputStr = "数字证书";
            byte[] data = inputStr.getBytes();
            //公钥加密
            byte[] encrypt = encryptByPublicKey(data, "D:\\DEMO\\10001.cer");
            String str = new String(encrypt);
            //私钥解密
            byte[] decrypt = decryptByPrivateKey(encrypt, "D:\\DEMO\\10001.keystore", "10001", "123456");
            String outputStr = new String(decrypt);
            System.err.println("加密前：" + inputStr);
            System.err.println("加密后："+"\n" + str);
            System.err.println("解密后：" + outputStr);

        }


    }


    /**
     * 文件加密
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

    /**文件解密
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
    public static String encryptHand(File file1,File file2){

        String result="";
        String line = "";
        byte[] bt;
        String encryptResultStr="";
        try {
            BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (file1),"UTF-8"));
            BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (file2),"UTF-8"));
            while((line=br.readLine())!=null){
                bt=encryptByAES(result + line, PRIVATE_KEY_FILE);
                encryptResultStr =parseByte2HexStr(bt);
                bw.write(encryptResultStr);
                bw.newLine();
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
    public static void decryptHand(File file1,File file2){

        String line ="";
        byte[] decryptFrom=null;
        byte[] decryptResult=null;

        try{
            BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream(file1),"UTF-8"));
            BufferedWriter bw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream(file2),"UTF-8"));
            while((line = br.readLine())!=null){//使用readLine方法，一次读一行
                //解密
                String result="";
                decryptFrom =parseHexStr2Byte(result+line);
                decryptResult =decryptByAES(decryptFrom, PRIVATE_KEY_FILE);
                result=new String(decryptResult,"UTF-8");
                bw.write(result);
                bw.newLine();
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
    public static String encryptRSA(String source,String publicKey) throws Exception {
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
    public static String decryptRSA(String source,String privateKey) throws Exception {
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



}