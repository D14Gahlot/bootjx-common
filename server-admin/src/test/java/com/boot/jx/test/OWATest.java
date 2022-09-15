package com.boot.jx.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.utils.FileUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonUtil;
import com.github.gianlucanitti.javaexpreval.ExpressionException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

public class OWATest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	private static Logger LOGGER = LoggerFactory.getLogger(OWATest.class);

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ExpressionException
	 * @throws URISyntaxException
	 * @throws NumberParseException
	 * @throws IOException
	 */

	public static void main(String[] args) throws URISyntaxException, NumberParseException, IOException {

		InputStream leftStream = FileUtil.getExternalOrInternalResourceAsStream("sample/test.json", OWATest.class);

		String leftJson = IoUtils.inputstream_to_string(leftStream);
		Map<String, Object> leftMap = JsonUtil.fromJsonToMap(leftJson);

		System.out.println(JsonUtil.toJson(leftMap));
	}
}
