package com.bootloaderjs;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import com.boot.utils.CryptoUtil;

public class ModeTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^LINK <(.*)>$");

	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long timeout = 100L;
		System.out.println("DIV=" + (TimeUnit.NANOSECONDS.toSeconds(timeout) - 1));

	}

	public static void generate() {
		System.out.println(CryptoUtil.generateHMAC("appd-kwt.amxremit.com", "traceId"));
	}

}
