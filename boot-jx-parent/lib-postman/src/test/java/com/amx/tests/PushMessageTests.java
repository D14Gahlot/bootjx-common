package com.amx.tests;

import java.math.BigDecimal;
import java.text.ParseException;

import com.boot.jx.dict.Language;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.postman.plugin.TwitterPlugin.TwitterConfigDetails;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

public class PushMessageTests { // Noncompliant

	static BigDecimal country = new BigDecimal(30);
	static BigDecimal customer = new BigDecimal(30333);
	static String tnt = Tenants.DEFAULT_STR;
	static String FORMAT = "%10s : %-10s : %10s";

	public static void main(String[] args) throws ParseException {

		CommonFile file = new CommonFile().url("http://meherydata.s3.amazonaws.com/lntinfotech/quickmedia/5f36e371-b7d7-46ad-bc2d-2f631751c3be/WhatsApp Video 2022-06-09 at 8.20.51 PM.mp4");
		System.out.println(file.getFileType());
	
	}

	public static void main4(String[] args) throws ParseException {
		PMConfiguration config = PMConfiguration.instance();
		String key = "@$test.s";
		TwitterConfigDetails tw = new TwitterConfigDetails();
		tw.setHandler(key);
		String json = JsonUtil.toJson(config);
		System.out.println(json);
		System.out.println(JsonUtil.toJson(JsonUtil.parse(json, PMConfiguration.class)));

	}

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws ParseException
	 */
	public static void main3(String[] args) throws ParseException {
		String id;

		if (ArgUtil.is(id = getNull()))
			System.out.println("WTF " + id);

		if (ArgUtil.is(id = getNoNull()))
			System.out.println("Hmm ok " + id);
	}

	private static String getNull() {
		return null;
	}

	private static String getNoNull() {
		return "OKKKK";
	}

	private static void testOutboxMessage() {
		String json = JsonUtil.toJson(new OutboxMessage());
		System.out.println(json);
		JsonUtil.parse(json, OutboxMessage.class);

	}

	private static void print(String type, Object expected, Object actual) {
		System.out.println(String.format(FORMAT, type, actual, expected));
	}

	private static void print(String testname, ContactMeta c) {
		System.out.println("Test : " + testname);
		print("country", country, c.getCountry());
		print("tnt", tnt, c.getTenant());
		print("lang", Language.EN, c.getLang());
		print("cusomter", customer, c.getUserid());
	}

	private static void test1() {
		PushMessage msg = new PushMessage();
		msg.addToCountry(tnt, country);
		ContactMeta c = PushMessage.toContact(msg.getTo().get(0));
		print("test1", c);
	}

	private static void test2() {
		PushMessage msg = new PushMessage();
		msg.addToCountry(country);
		ContactMeta c = PushMessage.toContact(msg.getTo().get(0));
		print("test2", c);
	}

	private static void everyOne() {
		PushMessage msg = new PushMessage();
		msg.hsm().setLang(Language.HI.name());
		msg.addToEveryone();
		ContactMeta c = PushMessage.toContact(msg.getTo().get(0));
		print("everyOne", c);
	}

	private static void everyOne(String lang) {
		PushMessage msg = new PushMessage();
		msg.hsm().setLang(lang);
		msg.addToTenant(tnt, Language.fromString(lang));
		ContactMeta c = PushMessage.toContact(msg.getTo().get(0));
		print("everyOne", c);
	}

	private static void customer() {
		PushMessage msg = new PushMessage();
		msg.hsm().setLang(Language.HI.toString());
		msg.addToUser(customer);
		ContactMeta c = PushMessage.toContact(msg.getTo().get(0));
		print("customer", c);
	}
}
