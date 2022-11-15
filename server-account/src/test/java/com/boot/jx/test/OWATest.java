package com.boot.jx.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ExpressionException;

import com.boot.utils.CryptoUtil;

public class OWATest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static Logger LOGGER = LoggerFactory.getLogger(OWATest.class);

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ExpressionException
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws NoSuchAlgorithmException 
	 */

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, NoSuchAlgorithmException {
		//9956b1824d370b2493d72816796a6dbf8886ba1a6d9faecf3c308067f4281309
	}
}
