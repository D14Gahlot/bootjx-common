package com.amx.tests;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.CryptoUtil;

public class StringTester { // Noncompliant

	static BigDecimal country = new BigDecimal(30);
	static BigDecimal customer = new BigDecimal(30333);
	static String tnt = Tenants.DEFAULT_STR;
	static String FORMAT = "%10s : %-10s : %10s";

	public static final String XAUTH_DELIMITER = CryptoUtil.getEncoder().message("%01").decodeURL().toString();

	public static void main(String[] s) throws Exception {
		StringTester test = new StringTester();
		test.doTest();
	}

	private void doTest() throws MessagingException, IOException {
		String str = "wa360:918828218374:w";
		System.out.println(str.replace(":", "="));
		System.out.println(str.replace("=", ":"));
	}
}
