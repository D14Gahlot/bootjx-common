package com.boot.loaderjs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import com.boot.jx.postman.model.DocResult;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.utils.FileUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonUtil;

public class AppJsonTest { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	TemplateUtils templateUtils = new TemplateUtils();
	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");

	public static void main(String[] args) throws URISyntaxException, IOException {
		InputStream is = FileUtil.getExternalResourceAsStream("ext-resources/doc_response.json");
		String json = IoUtils.inputstream_to_string(is);

		DocResult map = JsonUtil.fromJson(json, DocResult.class);

		System.out.println("getId		" + map.getId());
		System.out.println("getFileID	" + map.getFileId());
		System.out.println("getUserID	" + map.getUserID());
		
		System.out.println(JsonUtil.toJson(map));
		
	}

}
