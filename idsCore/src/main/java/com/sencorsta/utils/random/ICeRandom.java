package com.sencorsta.utils.random;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class ICeRandom {
	public static ThreadLocalRandom getDynamic() {
		return ThreadLocalRandom.current();
	}
	
	static Random random=new Random(19890825);
	public static Random getStatic() {
		return random;
	}
	public static void setSeed(long seed) {
		random=new Random(seed);
	}



}
