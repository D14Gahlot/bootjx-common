package com.boot.loaderjs;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import com.boot.jx.postman.gupshup.GupShupDeliveryResp;
import com.boot.jx.postman.gupshup.GupShupDeliveryResp.GupShupDeliveryDto;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

public class JsonTest { // Noncompliant
	/**
	 * This is just a test method
	 * 
	 * @param args
	 */
	TemplateUtils templateUtils = new TemplateUtils();
	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");

	public static void main(String[] args) throws URISyntaxException, IOException {

		@SuppressWarnings("deprecation")
		String response = java.net.URLDecoder.decode(
				"%5B%7B%22srcAddr%22%3A%22TESTSM%22%2C%22channel%22%3A%22WHATSAPP%22%2C%22externalId%22%3A%224378337442232250379-60a55d8b142e534a51d10e2e%22%2C%22cause%22%3A%22READ%22%2C%22errorCode%22%3A%22026%22%2C%22destAddr%22%3A%22919930104050%22%2C%22eventType%22%3A%22READ%22%2C%22eventTs%22%3A1621450221000%7D%5D");
		GupShupDeliveryResp status = new GupShupDeliveryResp();
		status.setResponse(JsonUtil.parse(response, new TypeReference<List<GupShupDeliveryDto>>() {
		}));

		for (GupShupDeliveryDto gupShupDelivery : status.getResponse()) {
			System.out.println(gupShupDelivery.getExternalId());
		}
	}

}
