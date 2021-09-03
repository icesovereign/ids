package com.sencorsta.utils.templet;
/**
 * @author ICe
 *
 */
public class Between {
	boolean isGreater;
	boolean isGreaterEqual;
	boolean isLess;
	boolean isLessEqual;
	long valueGreater;
	long valueLess;

	public Between(String valueLess, boolean isLessEqual, String valueGreater, boolean isGreaterEqual) {
		if (!valueLess.equals("")) {
			this.isLess = true;
			this.isLessEqual = isLessEqual;
			this.valueLess = Long.parseLong(valueLess);
		}

		if (!valueGreater.equals("")) {
			this.isGreater = true;
			this.isGreaterEqual = isGreaterEqual;
			this.valueGreater = Long.parseLong(valueGreater);
		}
	}

	public boolean isBetween(long value) {
		boolean temp = true;
		if (isGreater) {
			if (isGreaterEqual) {
				if (!(value>=valueGreater)) {
					temp=false;
				}
			}else {
				if (!(value>valueGreater)) {
					temp=false;
				}
			}
		}
		if (isLess) {
			if (isLessEqual) {
				if (!(value<=valueLess)) {
					temp=false;
				}
			}else {
				if (!(value<valueLess)) {
					temp=false;
				}
			}
		}
		return temp;
	}
	
	public static void main(String[] args) {
		Between b=new Between("", false, "500", true);
		System.out.println(b.isBetween(1001));
		System.out.println(b.isBetween(1000));
		System.out.println(b.isBetween(550));
		System.out.println(b.isBetween(500));
		System.out.println(b.isBetween(499));
	}

}
