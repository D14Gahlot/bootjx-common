package com.boot.loaderjs;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.CreatedTimeStampIndexSupport;
import com.boot.jx.postman.doc.tpo.WABAConversation;
import com.boot.jx.postman.query.WABAConversationQuery;
import com.boot.utils.ArgUtil;
import com.boot.utils.FileUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;

public class CleassCreater { // Noncompliant

	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		testUpdated();
	}

	public static void testUpdated() {
		WABAConversationQuery q = new WABAConversationQuery("id");

		System.out.println(CreatedTimeStampIndexSupport.class.isAssignableFrom(WABAConversation.class));
	}

	public static void nations() {
		try {
			InputStream is = FileUtil.getExternalResourceAsStream("ext-resources/national.json");

			String json = IoUtils.inputstream_to_string(is);
			@SuppressWarnings("unchecked")
			HashMap<String, Object> map = (HashMap<String, Object>) JsonUtil.fromJson(json, Map.class);

			JsonPath dataPath = new JsonPath("/data");

			List<Map<String, Object>> dataList = dataPath.loadList(map, null);

			for (Map<String, Object> ctry : dataList) {
				System.out.println(String.format(
						"%s(\"%s\",\"%s\"),", ArgUtil.parseAsString(ctry.get("countryName"), "ALL").toUpperCase()
								.replaceAll(" ", "_").replaceAll("[&)(\\,'\\.]", ""),
						ctry.get("countryId"), ctry.get("nationality")));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void branches() {
		try {
			InputStream is = FileUtil.getExternalResourceAsStream("ext-resources/branch.bhr.json");

			String json = IoUtils.inputstream_to_string(is);
			@SuppressWarnings("unchecked")
			HashMap<String, List<List<Object>>> map = (HashMap<String, List<List<Object>>>) JsonUtil.fromJson(json,
					Map.class);

			for (Entry<String, List<List<Object>>> items : map.entrySet()) {
				List<List<Object>> itemsList = items.getValue();
				for (List<Object> list : itemsList) {
					if (list.size() >= 5) {
						System.out.println(
								String.format("%s_%s(\"%s\",\"%s\",\"%s , %s , %s\"),", items.getKey().toUpperCase(),
										ArgUtil.parseAsString(list.get(0)).toUpperCase().replaceAll("[ &]", "_"),
										list.get(1), list.get(2), list.get(0), list.get(3), list.get(4)));
					}

				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
