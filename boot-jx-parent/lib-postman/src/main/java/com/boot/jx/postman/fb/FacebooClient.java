package com.boot.jx.postman.fb;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.rest.RestService;
import com.boot.jx.scope.TenantProperties;
import com.boot.utils.ArgUtil;

@Component
public class FacebooClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacebooClient.class);

	private final String PAGE_TOKEN = "EAAY8WTSG17kBALg61WOdfo0klUjb9yfiEK6b3nDvfYi0eEEYNu3fRJSj1xn2IsfH6hAMYXbjXbPepwNAyd8ccBJTg2QE0nckHeMR0UmRWj0tIhhnEqhDlgDJUZC0IfZAhDdVI6IDYHS8XL8KRl706i3MARfCtvhvtA2EAKZB4yS0PVNIRYKYvYTsjghldUZD";
	private final String VERIFY_TOKEN = "A_SECRET_VERIFY_TOKEN";
	// this is for reply messages
	private final String FB_MSG_URL = "https://graph.facebook.com/v2.6/me/messages?access_token="
			+ PAGE_TOKEN;

	@Autowired
	RestService restService;

	@Autowired
	TenantProperties tenantProperties;

	public String registerWebhook(String token, String challenge, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		Properties p = tenantProperties.getProperties();
		String verifyToken = p.getProperty("facebook." + lane + ".verifyToken", VERIFY_TOKEN);
		if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
			return challenge;
		} else {
			return "Wrong Token";
		}
	}

	public void sendReply(String id, String text, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		Properties p = tenantProperties.getProperties();
		String accessToken = p.getProperty("facebook.lane." + lane + ".accessToken", PAGE_TOKEN);
		FacebookMessageResponse response = new FacebookMessageResponse();
		response.setMessageType("text");
		response.getRecipient().put("id", id);
		response.getMessage().put("text", text);
		String result = restService.ajax("https://graph.facebook.com/v2.6/me/messages?access_token="
				+ accessToken).post(response).asString();
		LOGGER.info("Message result to {} : {}", id, result);

	}
}
