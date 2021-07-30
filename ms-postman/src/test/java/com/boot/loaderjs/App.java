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

import com.boot.jx.AppContextUtil;
import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.boot.utils.MapBuilder;
import com.boot.utils.Urly;

public class App { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	TemplateUtils templateUtils = new TemplateUtils();
	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");

	public static void main(String[] args) throws MalformedURLException, URISyntaxException {
		long timestamp = System.currentTimeMillis();

		System.out.println((timestamp));
		System.out.println(Long.toString(timestamp));
		String uniqueKey = "pk123456789";
		String docKey = "TemporarySecret";
		System.out.println(Urly
				.parse(String.format("%s/upload/%d/%s/%s", "https://cdnd-kwt.amxremit.com/docs", timestamp, uniqueKey,
						new HashBuilder().secret(docKey).message(uniqueKey).currentTime(1587300194314L).interval(600)
								.toHMAC().output()))
				.queryParam("dir", "dir12").queryParam("docid", "did1234567").queryParam("type", "KWT_CIVILID")
				.getURL());
	}

	public static void main41(String[] args) {

		PushMessage msg = new PushMessage();

		AppContextUtil.setTenant(Tenants.DEFAULT);

		msg.addContact(new ContactMeta().or(MapBuilder.map().put("lang", "en").put("nationality", "4").toMap()));

		msg.addContact(new ContactMeta().or(MapBuilder.map().put("lang", "ar").put("nationality", "5").toMap()));

		String androidTopicStr = null;
		String iosTopicStr = null;
		String webTopicStr = null;

		int totalConditions = 0;
		if (msg.getContacts().size() > 0) {
			StringJoiner orCondition = new StringJoiner(") || (");
			int totalOrConditions = 0;
			for (ContactMeta singleContact : msg.getContacts()) {
				for (Map<String, Object> singleFilter : singleContact.getFilter()) {
					StringJoiner andCondition = new StringJoiner(PushMessage.CONDITION_SEPRATOR_AND);
					for (Entry<String, Object> entry : singleFilter.entrySet()) {
						andCondition.add("'" + PushMessage.topic(entry.getKey(), entry.getValue()) + "%sx%' in topics");
						totalConditions++;
					}
					totalConditions++;
					totalOrConditions++;
					orCondition.add(andCondition.toString());
				}
			}
			String orConditionStr = orCondition.toString();
			System.out.println(orCondition.length());
			if (totalOrConditions > 1) {
				orConditionStr = "(" + orConditionStr + ")";
			}
			androidTopicStr = orConditionStr.toString().replaceAll("%sx%", "_and");
			iosTopicStr = orConditionStr.toString().replaceAll("%sx%", "_ios");
			webTopicStr = orConditionStr.toString().replaceAll("%sx%", "_web");
		}
		System.out.println(androidTopicStr);
		System.out.println(iosTopicStr);
		System.out.println(webTopicStr);

	}

	public static void main6(String[] args) throws IOException {

		String str = "/topics/A & /topics/B";
		System.out.println(str.replaceAll("/topics/([A-Z]+)", "$1_web in topics"));
	}

	public static void main5(String[] args) {
		String from = "Al Mulla International Exchange<amxjax@gmail.com>";
		String[] path = "html/sms/omsoe".split("^html\\/");

		System.out.println(path[0] + " " + path[1]);
	}

	public static void main4(String[] args) {
		String from = "Al Mulla International Exchange<amxjax@gmail.com>";
		String[] path = "html/sms/omsoe".split("^html\\/");

		System.out.println(path[0] + " " + path[1]);
	}

	public static void main3(String[] args) {
		String from = "Al Mulla International Exchange<amxjax@gmail.com>";
		// String from = "amxjax@gmail.com";
		Matcher matcher = pattern.matcher(from);
		if (matcher.find()) {
			System.out.println(matcher.group(1) + "   =   " + matcher.group(2));
		} else {
			System.out.println(from);
		}
	}

	public static void main2(String[] args) {
		String input = "\u0628\u064A\u0627\u0646\u0627\u062A 177 \u0627\u0644\u063189\u0627\u0633\u0644";
		System.out.println(input);

		System.out.println(fixBiDi(input));

		char[] temparray = input.toCharArray();
		int left, right = 0;
		right = temparray.length - 1;
		for (left = 0; left < right; left++, right--) {
			// Swap values of left and right
			char temp = temparray[left];
			temparray[left] = temparray[right];
			temparray[right] = temp;
		}
		for (char c : temparray)
			System.out.print(c);
		System.out.println();
	}

	public static String fixBiDi(String word) {
		Bidi bidi = new Bidi(word, -2);
		if (!bidi.isMixed() && bidi.getBaseLevel() == 0) {
			return word;
		} else {
			int runCount = bidi.getRunCount();
			byte[] levels = new byte[runCount];
			Integer[] runs = new Integer[runCount];

			for (int result = 0; result < runCount; ++result) {
				levels[result] = (byte) bidi.getRunLevel(result);
				runs[result] = Integer.valueOf(result);
			}

			Bidi.reorderVisually(levels, 0, runs, 0, runCount);
			StringBuilder bidiText = new StringBuilder();

			for (int i = 0; i < runCount; ++i) {
				int index = runs[i].intValue();
				int start = bidi.getRunStart(index);
				int end = bidi.getRunLimit(index);
				byte level = levels[index];
				if ((level & 1) != 0) {
					while (true) {
						--end;
						if (end < start) {
							break;
						}

						char character = word.charAt(end);
						if (Character.isMirrored(word.codePointAt(end))) {
							bidiText.append(character);
						} else {
							bidiText.append(character);
						}
					}
				} else {
					bidiText.append(word, start, end);
				}
			}

			return bidiText.toString();
		}
	}

}
