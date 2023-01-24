package com.boot.loaderjs;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.boot.jx.tmpl.TemplateUtils;

public class EmailTest { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	TemplateUtils templateUtils = new TemplateUtils();
	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");
	public static final String TITLE_STR = "^(([\\[\\(] *)?(?i)(RE?S?|REPLY|FYI|RIF|I|FS|VB|RV|ENC|ODP|PD|YNT|ILT|SV|VS|VL|AW|WG|ΑΠ|ΣΧΕΤ|ΠΡΘ|תגובה|הועבר|主题|转发|FWD|Forward?) *([-:;)\\]][ :;\\])-]*|$)|\\]+ *$)+";
	public static final Pattern TITLE = Pattern.compile(TITLE_STR);

	public static void main(String[] args) throws MalformedURLException, URISyntaxException, AddressException {
		System.out.println("Fwd :Forward: : Start sending  Re : WhatsApp messages to your customers".replaceAll(TITLE_STR, ""));
	}

	public static void main2(String[] args) throws MalformedURLException, URISyntaxException, AddressException {
		InternetAddress[] x = InternetAddress.parse("Lalit Tanwar <lalit.tanwar07@gmail.com>,LT<+919930104050>");
		for (InternetAddress internetAddress : x) {
			System.out.println(internetAddress.getPersonal());
			System.out.println(internetAddress.getType());
			System.out.println(internetAddress.getAddress());
		}
	}

}
