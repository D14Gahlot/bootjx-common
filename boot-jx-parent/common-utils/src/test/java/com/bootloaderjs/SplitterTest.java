package com.bootloaderjs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SplitterTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^com.amx.jax.logger.client.AuditFilter<(.*)>$");
	public static final Pattern LINK_CIVIL_ID = Pattern.compile("^LINK (.*)$");
	public static final Pattern LINKD_CIVIL_ID = Pattern.compile("^LINKD <(.*)>$");
	public static final Pattern ENCRYPTED_PROPERTIES = Pattern.compile("^ENC\\((.*)\\)$");

	public static final String SPLITTER_CHAR = ";";
	public static final String KEY_VALUE_SEPARATOR_CHAR = ":";

	public static final String PAYMENT_CAPTURE_CALLBACK_V2 = "/v2/capture/{paygCode}/{tenant}/{channel}/{product}/{uuid}";
	public static final String PAYMENT_CAPTURE_CALLBACK_V2_WILDCARD = "/v2/capture/{paygCode}/{tenant}/{channel}/{product}/{uuid}/*";
	public static final String PAYMENT_CAPTURE_CALLBACK_V2_REGEX = "/app/v2/capture/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/(?<trace>[0-9A-Za-z\\-]+)/?(.*)";
	public static final String PAYMENT_CAPTURE_CALLBACK_V2_REGEX2 = "/app/v2/capture/[\\w]/[\\w]/[\\w]/[\\w]/(?<traceid>[-a-zA-Z0-9])/(.*)";
	//# ^/payg2/app/v2/capture/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/([a-zA-Z0-9_]+)/(?<traceid>[0-9A-Za-z\-]+)/?(.*)
	//#  /payg2/app/v2/capture/KNET2/KWT/ONLINE/REMIT/BKJ-7TSqm-7TSqmFsIe09-005218-prm-7TSqrAmaIl7
	public static final String PAYMENT_CAPTURE_CALLBACK_V2_REGEX_TEST = "/payg/app/v2/capture/KNET/KWT/ONLINE/REMIT/BKJ-79020-7TScwusMzwU-184466-prm-7TSd5hgYrtD";

	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		 Pattern pattern = Pattern.compile("^/payg" + PAYMENT_CAPTURE_CALLBACK_V2_REGEX);
		 Matcher matcher = pattern.matcher(PAYMENT_CAPTURE_CALLBACK_V2_REGEX_TEST);
		 
		 if(matcher.find()) {
			 System.out.println(matcher.group("traceid"));
		 }
		
	}

	public static void linCivilIdCheck() {
		String testString = "ENC(uTSqb9grs1+vUv3iN8lItC0kl65lMG+8)";
		Matcher x = ENCRYPTED_PROPERTIES.matcher(testString);
		if (x.find()) {
			System.out.println(x.group(1));
		} else {
			System.out.println("No");
		}

	}

	public static void classNameCheck() {
		String testString = "com.amx.jax.mcq.SampleTask2$$EnhancerBySpringCGLIB$$82858f05";
		System.out.println(testString.split("\\$\\$")[0]);
	}

}
