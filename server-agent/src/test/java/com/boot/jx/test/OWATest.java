package com.boot.jx.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.model.SessionSearchQuery;
import com.boot.utils.JsonUtil;
import com.github.gianlucanitti.javaexpreval.ExpressionException;

public class OWATest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("(:[\\w]+)|([\\s\\w]+)");

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

//		System.out.println(JsonUtil.toJson(":facebook pooja p :whatsapp".split("(\\:[\\w]+)", -1)));
//
//		System.out.println(JsonUtil.toJson(pattern.split(":facebook   pooja p   :whatsapp", -2)));
//
//		Matcher m = pattern.matcher(":facebook pooja p ss :whatsapp sss to:agent");
//
//		int i = 0;
//		while (m.find()) {
//			System.out.println(m.group(i));
//		}

		System.out.println(JsonUtil.toJson(new SessionSearchQuery().parse("pooja p to:agent").add(CHAT_MODE.AGENT).add(CHAT_MODE.BOT)));
	}
}
