package com.boot.jx.test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.github.gianlucanitti.javaexpreval.ExpressionException;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class OWATest { // Noncompliant

    public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

    private static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

    private static Logger LOGGER = LoggerFactory.getLogger(OWATest.class);

    /**
     * This is just a test method
     * 
     * @param args
     * @throws ExpressionException
     * @throws MalformedURLException
     * @throws URISyntaxException
     * @throws NumberParseException
     */

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, NumberParseException {

	PhoneNumber phoneNumber = new PhoneNumber();

	String number = "+ 0 + 919930 104 050 4 4 ";

	number = number.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();

	// number = StringUtils.trim(number, '+');

	System.out.println("number  [" + number + "]");

	PHONE_NUMBER_UTIL.parse("+" + number, "IN", phoneNumber);
	System.out.println(String.format("+%s %s", phoneNumber.getCountryCode(),
	phoneNumber.getNationalNumber()));

    }
}
