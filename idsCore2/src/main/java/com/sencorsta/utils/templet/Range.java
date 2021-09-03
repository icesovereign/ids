package com.sencorsta.utils.templet;


import com.sencorsta.utils.random.ICeRandom;

/**
 * @author ICe
 *
 */
public class Range {
	private long max;
	private long min;
	private long lastValue;
	
	public Range(long min,long max) {
		super();
		this.max = max;
		this.min = min;
		this.lastValue=0;
	}
	
	public long getRandom() {
		if (max==min) {
			return max;
		}
		long rangeLong = min + (((long) (ICeRandom.getDynamic().nextDouble() * (max - min+1))));
		lastValue=rangeLong;
		return rangeLong;
	}
	public long getLastValue() {
		return lastValue;
	}
	
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		this.max = max;
	}
	public long getMin() {
		return min;
	}
	public void setMin(long min) {
		this.min = min;
	}
	
	
}
