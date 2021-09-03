package com.sencorsta.utils.string;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class RSAEncrypt {
    public static void main(String[] args) throws Exception {
        String defaultCharsetName= Charset.defaultCharset().displayName();
        System.out.println("defaultCharsetName:"+defaultCharsetName);

        //生成公钥和私钥
        Map<String, String> keyMap1 = genKeyPair();
        Map<String, String> keyMap2 = genKeyPair();
        Map<String, String> keyMap3 = genKeyPair();

        System.out.println(keyMap1.get("publicKey"));
        System.out.println(keyMap1.get("privateKey"));
//
//        System.out.println(keyMap2.get("publicKey"));
//        System.out.println(keyMap2.get("privateKey"));
//
//        System.out.println(keyMap3.get("publicKey"));
//        System.out.println(keyMap3.get("privateKey"));

        //私钥加密
        System.out.println("公钥加密，私钥解密");
        String str1 = "公钥加密，私钥解密+++++++++++++++++++++++++++++++++++++++++++++++++公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密，私钥解密公钥加密";
        System.out.println("加密前：" + str1);
        String encode1 = RSAEncrypt.encrypt(str1, keyMap1.get("publicKey"));
        System.out.println("加密后：" + encode1);
        String decode1 = RSAEncrypt.decrypt(encode1, keyMap1.get("privateKey"));
        System.out.println("解密后：" + decode1);

        System.out.println(str1.equals(decode1));

//        //加密字符串
//        String message = "df723820";
//        System.out.println("随机生成的公钥为:" + keyMap.get(0));
//        System.out.println("随机生成的私钥为:" + keyMap.get(1));
//        String messageEn = encrypt(message,keyMap.get(0));
//        System.out.println(message + "\t加密后的字符串为:" + messageEn);
//        String messageDe = decrypt(messageEn,keyMap.get(1));
//        System.out.println("还原后的字符串为:" + messageDe);
    }



    /**
     * 随机生成密钥对
     *
     * @throws NoSuchAlgorithmException
     */
    public static Map<String, String> genKeyPair() throws NoSuchAlgorithmException {
        // KeyPairGenerator类用于生成公钥和私钥对，基于RSA算法生成对象
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        // 初始化密钥对生成器，密钥大小为96-1024位
        keyPairGen.initialize(1024, new SecureRandom());
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();   // 得到私钥
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();  // 得到公钥
        String publicKeyString = new String(Base64.getEncoder().encode(publicKey.getEncoded()));
        // 得到私钥字符串
        String privateKeyString = new String(Base64.getEncoder().encode((privateKey.getEncoded())));
        // 将公钥和私钥保存到Map
        Map<String, String> keyMap = new HashMap<String, String>();  //用于封装随机产生的公钥与私钥
        keyMap.put("publicKey", publicKeyString);  //0表示公钥
        keyMap.put("privateKey", privateKeyString);  //1表示私钥
        return keyMap;
    }

    /**
     * RSA公钥加密
     *
     * @param str       加密字符串
     * @param publicKey 公钥
     * @return 密文
     * @throws Exception 加密过程中的异常信息
     */
    public static String encrypt(String str, String publicKey) throws Exception {
        byte[] inputByte = str.getBytes("UTF-8");
        // 解码支付公钥
        byte[] key = Base64.getDecoder().decode(publicKey);
        // 实例化密钥工厂
        KeyFactory keyFactory;
        keyFactory = KeyFactory.getInstance("RSA");
        // 密钥材料转换
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
        // 产生公钥
        PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
        // 数据加密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        int blockSize = cipher.getOutputSize(inputByte.length) - 11;
        return Base64.getEncoder().encodeToString(doFinal(inputByte, cipher, blockSize));
    }

    /**
     * RSA私钥解密
     *
     * @param str        解密字符串
     * @param privateKey 私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String key) throws Exception {
        byte[] inputByte = Base64.getDecoder().decode(str.getBytes("UTF-8"));
        byte[] decodedKey = Base64.getDecoder().decode(key);
        // 取得私钥
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        // 生成私钥
        PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
        // 数据解密
        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        int blockSize = cipher.getOutputSize(inputByte.length);
        return new String(doFinal(inputByte, cipher, blockSize),"UTF-8");


//        //64位解码加密后的字符串
//        byte[] inputByte = Base64.getDecoder().decode(str.getBytes("UTF-8"));
//        //base64编码的私钥
//        byte[] decoded = Base64.getDecoder().decode(privateKey);
//        RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
//        //RSA解密
//        Cipher cipher = Cipher.getInstance("RSA");
//        cipher.init(Cipher.DECRYPT_MODE, priKey);
//        String outStr = new String(cipher.doFinal(inputByte));
//        return outStr;
    }


    /**
     * 加密解密共用核心代码，分段加密解密
     *
     * @param decryptData 要加密的数据
     * @param cipher
     * @return
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws IOException
     */
    public static byte[] doFinal(byte[] decryptData, Cipher cipher, int blockSize)
            throws IllegalBlockSizeException, BadPaddingException, IOException {
        int offSet = 0;
        byte[] cache = null;
        int i = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (decryptData.length - offSet > 0) {
            if (decryptData.length - offSet > blockSize) {
                cache = cipher.doFinal(decryptData, offSet, blockSize);
            } else {
                cache = cipher.doFinal(decryptData, offSet, decryptData.length - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * blockSize;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

}