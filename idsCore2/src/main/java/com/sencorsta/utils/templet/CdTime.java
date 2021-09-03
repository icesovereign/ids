package com.sencorsta.utils.templet;

/**
 * @author ICe
 *
 */
public class CdTime {
	private long trigger;
	private Range cd;

	public CdTime(long trigger, long cdMin,long cdMax) {
		super();
		this.trigger = trigger;
		this.cd = new Range(cdMin, cdMax);
	}

	public CdTime(long trigger, long cd) {
		super();
		this.trigger = trigger;
		this.cd = new Range(cd, cd);
	}

	public long getTrigger() {
		return trigger;
	}
	public void setTrigger(long trigger) {
		this.trigger = trigger;
	}
	public Range getCd() {
		return cd;
	}
	public void setCd(Range cd) {
		this.cd = cd;
	}
	public boolean isTrigger(long currentTime) {
		return currentTime>trigger;
	}
	public void setNextTrigger(long currentTime) {
		this.trigger=cd.getRandom()+currentTime;
	}
	
}
