package com.boot.loaderjs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.Bidi;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.boot.utils.MapBuilder;
import com.boot.utils.Urly;

public class EmailTest { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	TemplateUtils templateUtils = new TemplateUtils();
	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, AddressException {

		InternetAddress[] x = InternetAddress.parse("Lalit Tanwar <lalit.tanwar07@gmail.com>,LT<+919930104050>");
		
		for (InternetAddress internetAddress : x) {
			System.out.println(internetAddress.getPersonal());
			System.out.println(internetAddress.getType());
			System.out.println(internetAddress.getAddress());
		}
		

	}

}
