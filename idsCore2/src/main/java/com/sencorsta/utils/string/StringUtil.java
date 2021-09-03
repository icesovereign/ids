package com.sencorsta.utils.string;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串常用功能工具类
 * @author ICe
 */
public final class StringUtil {

	private StringUtil() { }

	public final static String join(int[] os, String tag) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < os.length; i++) {
			if(i>0) {
				builder.append(tag);
			}
			builder.append(os[i]);
		}
		return builder.toString();
	}
	
	public final static String join(Object[] os, String tag) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < os.length; i++) {
			if(i>0) {
				builder.append(tag);
			}
			builder.append(os[i]);
		}
		return builder.toString();
	}
	
	/**
	 * 分解字符串
	 */
	public final static String[] split(final String txt, final String tag) {
		StringTokenizer tokenizer = new StringTokenizer(txt, tag);
		String[] result = new String[tokenizer.countTokens()];
		for (int index = 0; tokenizer.hasMoreTokens();) {
			result[index++] = tokenizer.nextToken();
		}
		return result;
	}

	/**
	 * 判断是否为null
	 * 
	 * @param param
	 * @return
	 */
	public final static boolean isEmpty(String param) {
		return param == null || param.length() == 0 || param.trim().equals("");
	}
	
	public final static boolean isNotEmpty(String param) {
		return !isEmpty(param);
	}

//	private static Pattern __CHNIESE__=Pattern.compile("[[\u4e00-\u9fa5]]");
//	public static final boolean hasChinese(String txt) {
//		return __CHNIESE__.matcher(txt).find();
//	}
	/**
	 * 检查指定字符串中是否存在中文字符。
	 */
	public static final boolean hasChinese(String txt) {
		char[] chars = txt.toCharArray();
		int length = chars.length;
		for (int i = 0; i < length; i++) {
		    if (isChinese(chars[i])) {  
		        return true;  
		    }
		}
		return false;
	}

	/**
	 * 检查一组字符串是否完全由中文组成
	 */
	public final static boolean isChinese(String txt) {
		char[] chars = txt.toCharArray();
		int length = chars.length;
		for (int i = 0; i < length; i++) {
		    if (!isChinese(chars[i])) {  
		        return false;  
		    }
		}
		return true;
	}
	
	public final static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);  
	    if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS  
	            || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS  
	            || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A  
	            || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION  
	            || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION  
	            || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {  
	        return true;  
	    }
	    return false;
	}

	/**
	 * 检查是否为纯字母
	 */
	public final static boolean isAlpha(String value) {
		if (value == null || value.length() == 0) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!isAlpha(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	public final static boolean isAlpha(char c) {
		if (('A' > c || c > 'Z') && ('a' > c || c > 'z')) {
			return false;
		}
		return true;
	}
	/**
	 * 检查是否为纯字母
	 */
	public final static boolean isNumeric(String value) {
		if (value == null || value.length() == 0) {
			return false;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!isNumeric(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	public final static boolean isNumeric(char c) {
		if ('0' > c || c > '9') {
			return false;
		}
		return true;
	}
	/**
	 * 检查是否为字母与数字混合
	 */
	public final static boolean isAlphaNumeric(String value) {
		if (value == null || value.trim().length() == 0) {
			return true;
		}
		for (int i = 0; i < value.length(); i++) {
			char letter = value.charAt(i);
			if (!isAlpha(letter) && !isNumeric(letter)) {
				return false;
			}
		}
		return true;
	}
	/**
	 * 检查是否为字母、数字、_混合
	 */
	public final static boolean isAlphaNumeric_(String value) {
		if (value == null || value.trim().length() == 0) {
			return true;
		}
		for (int i = 0; i < value.length(); i++) {
			char letter = value.charAt(i);
			if (!isAlpha(letter) && !isNumeric(letter) && letter != '_') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 过滤指定字符串
	 */
	public static final String replace(String string, String oldString, String newString) {
		if (string == null) {
			return null;
		}
		if (newString == null) {
			return string;
		}
		int i = 0;
		if ((i = string.indexOf(oldString, i)) >= 0) {
			char src[] = string.toCharArray();
			char newS[] = newString.toCharArray();
			int oLength = oldString.length();
			StringBuilder buf = new StringBuilder(src.length);
			buf.append(src, 0, i).append(newS);
			i += oLength;
			int j;
			for (j = i; (i = string.indexOf(oldString, i)) > 0; j = i) {
				buf.append(src, j, i - j).append(newS);
				i += oLength;
			}

			buf.append(src, j, src.length - j);
			return buf.toString();
		}
		return string;
	}

	/**
	 * 不匹配大小写的过滤指定字符串
	 */
	public final static String replaceIgnoreCase(String line, String oldString, String newString) {
		if (line == null) {
			return null;
		}
		String lcLine = line.toLowerCase();
		String lcOldString = oldString.toLowerCase();
		int i = 0;
		if ((i = lcLine.indexOf(lcOldString, i)) >= 0) {
			char line2[] = line.toCharArray();
			char newString2[] = newString.toCharArray();
			int oLength = oldString.length();
			StringBuilder buf = new StringBuilder(line2.length);
			buf.append(line2, 0, i).append(newString2);
			i += oLength;
			int j;
			for (j = i; (i = lcLine.indexOf(lcOldString, i)) > 0; j = i) {
				buf.append(line2, j, i - j).append(newString2);
				i += oLength;
			}

			buf.append(line2, j, line2.length - j);
			return buf.toString();
		} else {
			return line;
		}
	}
	
	public static final String ASCII_CHARSET = "US-ASCII";
	public static final String GBK = "GBK";
	// 此仅为常见字符，并非西文全部的256个字符，否则验证速度可能很慢(虽然不太可能每次都遍历256个字符，但保险起见仅以此为主)
	public static String WESTERN = " 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-=[];,./~!@#$%^&*()+_{}:|<>?\'\\\"æøåÆØÅäöüÄÖÜßåäöÅÄÖÑñ";
	public final static byte[] getAsciiBytes(String data) {
		if (data == null) {
			throw new IllegalArgumentException("Parameter may not be null");
		}
		try {
			return data.getBytes(ASCII_CHARSET);
		} catch (UnsupportedEncodingException e) {
		}
		throw new RuntimeException("LGame requires ASCII support");
	}

	public final static String getAsciiString(byte[] data, int offset, int length) {
		if (data == null) {
			throw new IllegalArgumentException("Parameter may not be null");
		}
		try {
			return new String(data, offset, length, ASCII_CHARSET);
		} catch (UnsupportedEncodingException e) {
		}
		throw new RuntimeException("LGame requires ASCII support");
	}

	public final static String getAsciiString(byte[] data) {
		return getAsciiString(data, 0, data.length);
	}

	/**
	 * 转化指定字符串为指定编码格式
	 * @return
	 */
	public final static String getSpecialString(String context, String encoding) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(context.getBytes());
			InputStreamReader isr = new InputStreamReader(in, encoding);
			BufferedReader reader = new BufferedReader(isr);
			StringBuffer buffer = new StringBuffer();
			String result;
			while ((result = reader.readLine()) != null) {
				buffer.append(result);
			}
			return buffer.toString();
		} catch (Exception ex) {
			return context;
		}
	}

	/**
	 * 得到定字节长的字符串，位数不足右补空格
	 * @return
	 */
	public final static String fillSpace(String txt, int length) {
		byte[] txtBytes = txt.getBytes();
		if (txtBytes.length >= length) {
			return txt;
		}
		StringBuffer txtBuffer = new StringBuffer(length);
		txtBuffer.append(txt);
		for (int i = 0; i < (length - txtBytes.length); i++) {
			txtBuffer.append(" ");
		}
		return txtBuffer.toString();
	}

	/**
	 * 获得特定字符总数
	 */
	public final static int charCount(String txt, char c) {
		if (txt != null) {
			int count = 0;
			int length = txt.length();
			for (int i = 0; i < length; i++) {
				if (txt.charAt(i) == c) {
					count++;
				}
			}
			return count;
		}
		return 0;
	}

	public final static int getGBKLength(String txt) {
		return getBytesLength(txt, GBK);
	}
	/**
	 * 显示指定编码下的字符长度
	 */
	public final static int getBytesLength(String txt, String encoding) {
		if (txt == null || txt.length() == 0) {
			return 0;
		}
		try {
			byte bytes[] = txt.getBytes(encoding);
			int length = bytes.length;
			return length;
		} catch (UnsupportedEncodingException exception) {
			System.err.println(exception.getMessage());
		}
		return 0;
	}

	/**
	 * 根据字节数组显式转换成gbk字符集的String 不支持gbk字符集的系统，就返回操作系统默认的字符集String
	 */
	public final static String getGbkString(byte[] chars) {
		String value = null;
		try {
			value = new String(chars, GBK);
		} catch (UnsupportedEncodingException e) {
			value = new String(chars);
		}
		return value;
	}

	/**
	 * 根据String显式转换成gbk的字节数组 不支持gbk字符集的系统，就返回操作系统默认的字符集字节数组
	 */
	public final static byte[] getGbkBytes(String value) {
		byte[] bytes;
		try {
			bytes = value.getBytes(GBK);
		} catch (UnsupportedEncodingException e) {
			bytes = value.getBytes();
		}
		return bytes;
	}

	/**
	 * 判定是否由纯粹的西方字符组成
	 */
	public final static boolean isWestern(String string) {
		char[] chars = string.toCharArray();
		int size = chars.length;
		for (int i = 0; i < size; i++) {
			if (WESTERN.indexOf(chars[i]) == -1) {
				return false;
			}
		}
		return true;
	}

	public static boolean containsEmoji(String source) {
		if(source == null) return false;
		int len = source.length();
		boolean isEmoji = false;
		for (int i = 0; i < len; i++) {
			char hs = source.charAt(i);
			if (0xd800 <= hs && hs <= 0xdbff) {
				if (source.length() > 1) {
					char ls = source.charAt(i + 1);
					int uc = ((hs - 0xd800) * 0x400) + (ls - 0xdc00) + 0x10000;
					if (0x1d000 <= uc && uc <= 0x1f77f) {
						return true;
					}
				}
			} else {
				// non surrogate
				if (0x2100 <= hs && hs <= 0x27ff && hs != 0x263b) {
					return true;
				} else if (0x2B05 <= hs && hs <= 0x2b07) {
					return true;
				} else if (0x2934 <= hs && hs <= 0x2935) {
					return true;
				} else if (0x3297 <= hs && hs <= 0x3299) {
					return true;
				} else if (hs == 0xa9 || hs == 0xae || hs == 0x303d || hs == 0x3030 || hs == 0x2b55 || hs == 0x2b1c
						|| hs == 0x2b1b || hs == 0x2b50 || hs == 0x231a) {
					return true;
				}
				if (!isEmoji && source.length() > 1 && i < source.length() - 1) {
					char ls = source.charAt(i + 1);
					if (ls == 0x20e3) {
						return true;
					}
				}
			}
		}
		return isEmoji;
	}

	/**
     * 判断字符串是否为URL
     * @param urls 需要判断的String类型url
     * @return true:是URL；false:不是URL
     */
    public static boolean isHttpUrl(String urls) {
        boolean isurl = false;
        String regex = "(((https|http)?://)?([a-z0-9]+[.])|(www.))"
            + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?)";//设置正则表达式
 
        Pattern pat = Pattern.compile(regex.trim());//对比
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();//判断是否匹配
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }
    
    public static boolean isContainsHttpUrl(String urls) {
        boolean isurl = false;
        String regex = ".*(((https|http)?://)?([a-z0-9]+[.])|(www.))"
            + "\\w+[.|\\/]([a-z0-9]{0,})?[[.]([a-z0-9]{0,})]+((/[\\S&&[^,;\u4E00-\u9FA5]]+)+)?([.][a-z0-9]{0,}+|/?).*";//设置正则表达式
 
        Pattern pat = Pattern.compile(regex.trim());//对比
        Matcher mat = pat.matcher(urls.trim());
        isurl = mat.matches();//判断是否匹配
        if (isurl) {
            isurl = true;
        }
        return isurl;
    }

    public static void main(String[] args) {
		String dd="嘿嘿https://blog.csdn.net/LucasXu01/article/details/82955024就氨基酸的";
		System.out.println(isContainsHttpUrl(dd));
	}
}
