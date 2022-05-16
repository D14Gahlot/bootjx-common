package com.boot.jx.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.utils.JsonUtil;
import com.github.gianlucanitti.javaexpreval.ExpressionException;

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
	 */

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		System.out.println(JsonUtil.toJson(new SessionSearchQuery().parse(":closed :WHATSAPP :email as you can see")));
	}
}
