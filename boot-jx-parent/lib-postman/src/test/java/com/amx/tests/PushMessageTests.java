package com.amx.tests;

import java.math.BigDecimal;
import java.text.ParseException;

import com.boot.jx.dict.Language;
import com.boot.jx.postman.model.Contact;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.JsonUtil;

public class PushMessageTests { // Noncompliant

	static BigDecimal country = new BigDecimal(30);
	static BigDecimal customer = new BigDecimal(30333);
	static String tnt = Tenants.DEFAULT_STR;
	static String FORMAT = "%10s : %-10s : %10s";

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main(String[] args) throws ParseException {
		testOutboxMessage();
	}

	private static void testOutboxMessage() {
		String json = JsonUtil.toJson(new OutboxMessage());
		System.out.println(json);
		JsonUtil.parse(json, OutboxMessage.class);

	}

	private static void print(String type, Object expected, Object actual) {
		System.out.println(String.format(FORMAT, type, actual, expected));
	}

	private static void print(String testname, Contact c) {
		System.out.println("Test : " + testname);
		print("country", country, c.getCountry());
		print("tnt", tnt, c.getTenant());
		print("lang", Language.EN, c.getLang());
		print("cusomter", customer, c.getUserid());
	}

	private static void test1() {
		PushMessage msg = new PushMessage();
		msg.addToCountry(tnt, country);
		Contact c = PushMessage.toContact(msg.getTo().get(0));
		print("test1", c);
	}

	private static void test2() {
		PushMessage msg = new PushMessage();
		msg.addToCountry(country);
		Contact c = PushMessage.toContact(msg.getTo().get(0));
		print("test2", c);
	}

	private static void everyOne() {
		PushMessage msg = new PushMessage();
		msg.setLang(Language.HI);
		msg.addToEveryone();
		Contact c = PushMessage.toContact(msg.getTo().get(0));
		print("everyOne", c);
	}

	private static void everyOne(Language lang) {
		PushMessage msg = new PushMessage();
		msg.setLang(lang);
		msg.addToTenant(tnt, lang);
		Contact c = PushMessage.toContact(msg.getTo().get(0));
		print("everyOne", c);
	}

	private static void customer() {
		PushMessage msg = new PushMessage();
		msg.setLang(Language.HI);
		msg.addToUser(customer);
		Contact c = PushMessage.toContact(msg.getTo().get(0));
		print("customer", c);
	}
}
