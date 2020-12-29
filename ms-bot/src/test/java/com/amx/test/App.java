package com.amx.test;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Pattern;

import com.boot.utils.DateFormatUtil;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

public class App { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	public static XmlMapper xmlMapper = new XmlMapper();

	/**
	 * 2001:0db8:0000:0000:0000:ff00:0042:7879
	 * 
	 * 12.244.233.165
	 * 
	 * @param ipAddress
	 * @return
	 */
	private static String ipParse(String ipAddress) {
		String[] slots = ipAddress.split(":|\\.");
		return String.join("-", Arrays.copyOfRange(slots, 0, slots.length / 4 * 3));
	}

	public static void main(String[] args) throws ParseException {
		DateFormat f = DateFormatUtil.determineDateFormat("16 July, 2020 06:09:52 PM AST");

		System.out.println(f.parse("16 July, 2020 06:09:52 PM AST"));

	}

}
