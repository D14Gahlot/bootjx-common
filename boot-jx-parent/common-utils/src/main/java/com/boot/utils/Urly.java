package com.boot.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The Class Urly.
 */
public class Urly {

	/**
	 * Gets the domain name.
	 *
	 * @param url
	 *            the url
	 * @return the domain name
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public static String getDomainName(String url) throws MalformedURLException {
		if (!url.startsWith("http") && !url.startsWith("https")) {
			url = "http://" + url;
		}
		URL netUrl = new URL(url);
		String host = netUrl.getHost();
		if (host.startsWith("www")) {
			host = host.substring("www".length() + 1);
		}
		return host;
	}

	/**
	 * Gets the sub domain name.
	 *
	 * @param url
	 *            the url
	 * @return the sub domain name
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public static String getSubDomainName(String url) throws MalformedURLException {
		String[] names = url.split("\\.");
		if (names.length < 3) {
			return null;
		} else {
			return names[0];
		}
	}

	/**
	 * Gets the builder.
	 *
	 * @return the builder
	 */
	public static URLBuilder getBuilder() {
		return new URLBuilder();
	}

	/**
	 * Parses the.
	 *
	 * @param urlString
	 *            the url string
	 * @return the URL builder
	 * @throws MalformedURLException
	 *             the malformed URL exception
	 */
	public static URLBuilder parse(String urlString) throws MalformedURLException {
		URL url;
		URLBuilder builder;
		if (urlString.startsWith("/")) {
			url = new URL("https://localhost/" + urlString);
			builder = new URLBuilder();
		} else {
			url = new URL(urlString);
			builder = new URLBuilder(url.getAuthority());
			builder.setConnectionType(url.getProtocol());
		}
		builder.setPath(url.getPath());
		builder.addParameter(url.getQuery());
		return builder;
	}
}
