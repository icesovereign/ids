package com.sencorsta.utils.string;

import java.security.MessageDigest;
import java.util.Map;

public class Md5Tools {

	public static String MD5(String s){
		char[] hexDigits={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};//ʮ������
		try{
			byte[] btInput=s.getBytes("UTF-8");//��㸶������һ��
			MessageDigest mdInst=MessageDigest.getInstance("MD5");//���MD5ժҪ�㷨��messageDigest����
			mdInst.update(btInput);//ʹ��ָ�����ֽڸ���ժҪ
			byte[] md=mdInst.digest();//�������
			//������ת����ʮ�����Ƶ��ַ�����ʽ
			int j=md.length;
			char str[]=new char[j*2];
			int k=0;
			for(int i=0;i<j;i++){
				byte byte0=md[i];
				str[k++]=hexDigits[byte0>>>4& 0xf];
				str[k++]=hexDigits[byte0 & 0xf];
			}
			return new String(str);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public static String MD5(String s,String encoding){
		char[] hexDigits={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};//ʮ������
		try{
			byte[] btInput=s.getBytes(encoding);//��㸶������һ��
			MessageDigest mdInst=MessageDigest.getInstance("MD5");//���MD5ժҪ�㷨��messageDigest����
			mdInst.update(btInput);//ʹ��ָ�����ֽڸ���ժҪ
			byte[] md=mdInst.digest();//�������
			//������ת����ʮ�����Ƶ��ַ�����ʽ
			int j=md.length;
			char str[]=new char[j*2];
			int k=0;
			for(int i=0;i<j;i++){
				byte byte0=md[i];
				str[k++]=hexDigits[byte0>>>4& 0xf];
				str[k++]=hexDigits[byte0 & 0xf];
			}
			return new String(str);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	/**
     * MD5加密
     *
     * @param string
     * @return
     */
    public static String md5Encode(String string,String encoding) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] byteArray = string.getBytes(encoding);
            byte[] md5Bytes = messageDigest.digest(byteArray);
            StringBuffer hexValue = new StringBuffer();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return string;
    }
    
    /**
     * 生成数据签名
     *
     * @param map
     * @return
     */
    public static String generateSign(Map<String, String> map, String merchantKey) {
        map.remove("sign");
        StringBuilder stringBuilder = new StringBuilder();
        for (String mapKey : map.keySet()) {
            stringBuilder.append("&").append(mapKey).append("=").append(map.get(mapKey));
        }
        stringBuilder.deleteCharAt(0).append("&key=").append(merchantKey);
        return MD5(stringBuilder.toString(),"UTF-8").toLowerCase();
    }
    
    public static String generateSignFish(Map<String, String> map, String merchantKey) {
	    StringBuilder stringBuilder = new StringBuilder();
	    for (String mapKey : map.keySet()) {
	        stringBuilder.append(map.get(mapKey));
	    }
	    stringBuilder.append(merchantKey);
	    return Md5Tools.MD5(stringBuilder.toString(),"UTF-8").toLowerCase();
	}
}
