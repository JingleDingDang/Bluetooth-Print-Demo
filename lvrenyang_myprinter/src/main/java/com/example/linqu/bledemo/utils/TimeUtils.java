package com.example.linqu.bledemo.utils;

public class TimeUtils {

	public static void WaitMs(long ms) {
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < ms)
			;
	}
}
