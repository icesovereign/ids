package com.sencorsta.utils.number;

import java.util.ArrayList;
import java.util.Date;
import java.util.Stack;

public class NumberConvert {
	private static String numStr = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	//private static String numStr = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
	private static char[] array = numStr.toCharArray();

	// 10进制转为其他进制，除留取余，逆序排列
	public static String toN(long number, int N) {
		Long rest = number;
		Stack<Character> stack = new Stack<Character>();
		StringBuilder result = new StringBuilder(0);
		while (rest != 0) {
			stack.add(array[Long.valueOf((rest % N)).intValue()]);
			rest = rest / N;
		}
		for (; !stack.isEmpty();) {
			result.append(stack.pop());
		}
		return result.length() == 0 ? "0" : result.toString();

	}

	// 其他进制转为10进制，按权展开
	public static long to10(String number, int N) {
		char ch[] = number.toCharArray();
		int len = ch.length;
		long result = 0;
		if (N == 10) {
			return Long.parseLong(number);
		}
		long base = 1;
		for (int i = len - 1; i >= 0; i--) {
			int index = numStr.indexOf(ch[i]);
			result += index * base;
			base *= N;
		}

		return result;
	}

	public static void main(String[] args) {

		ArrayList<Long> tempList = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			tempList.add(new Date().getTime());
		}
		// long num = new Date().getTime();

		for (Long num : tempList) {
			System.out.println("转换前:" + num);
			String s = NumberConvert.toN(num, 62);
			System.out.println("转换后:" + s);

			System.out.println("反换:" + NumberConvert.to10(s, 62));
			System.out.println(Long.MAX_VALUE/new Date().getTime());
		}
	}
}
