package com.bootloaderjs;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import com.boot.utils.URLBuilder;
import com.boot.utils.Urly;

public class UrlyTest { // Noncompliant

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		URLBuilder url = Urly.parse("/front/auth/login").queryParam("logout", "")
				.queryParam("_", System.currentTimeMillis())
				.queryParam("referer", "/front/wewew/wew/wewew?wewew=wewewe");
		System.out.println("url : " + url.getRelativeURL());
	}

}
