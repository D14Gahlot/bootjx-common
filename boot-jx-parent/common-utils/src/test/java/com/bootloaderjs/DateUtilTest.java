package com.bootloaderjs;

import java.util.concurrent.TimeUnit;

import com.boot.utils.TimeUtils;

public class DateUtilTest {

	public static void main(String args[]) {

		long yeterday = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);
		long todays = System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1);
		System.out.println("yeterday " + TimeUtils.isExpired(yeterday, "mid"));
		System.out.println("todays " + TimeUtils.isExpired(todays, "mid"));

	}
}
