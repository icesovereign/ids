package com.sencorsta.utils.string;


import java.io.IOException;


/**
 * Code工具类
 * @author ICe
 */
public final class CodeUtil {

	public static final long N = 825;


	public final static String encode(String data,long mask) throws IOException {
		byte[] buf = ZipUtil.encode(data);
		byte c = (byte) (buf.length % 0xF);
		for (int i = 0; i < buf.length; i++) {
			buf[i] ^= mask;
			buf[i] ^= c;
		}
		return Base64.encodeToString(buf);
	}

	public final static String encode(String data) throws IOException {
		return encode(data,N);
	}

	public final static String decode(String data,long mask) throws IOException {
		byte[] buf = Base64.decode(data);
		int c = buf.length % 0xF;
		for (int i = 0; i < buf.length; i++) {
			buf[i] ^= c;
			buf[i] ^= mask;
		}
		return ZipUtil.decode(buf);
	}

	public final static String decode(String data) throws IOException {
		return decode(data,N);
	}



}
