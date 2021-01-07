package com.boot.jx.postman.fb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-facebook.properties")
@EnableEncryptableProperties
public class FacebooClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacebooClient.class);

	@Autowired
	RestService restService;

	@Autowired
	private Environment environment;

	public String registerWebhook(String token, String challenge, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String verifyToken = environment.getProperty("facebook." + lane + ".verifyToken");
		if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
			return challenge;
		} else {
			return "Wrong Token";
		}
	}

	public void sendReply(String id, String text, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String accessToken = environment.getProperty("facebook.lane." + lane + ".accessToken");
		FacebookMessageResponse response = new FacebookMessageResponse();
		response.setMessageType("text");
		response.getRecipient().put("id", id);
		response.getMessage().put("text", text);
		String result = restService.ajax("https://graph.facebook.com/v2.6/me/messages?access_token=" + accessToken)
				.post(response).asString();
		LOGGER.info("Message result to {} : {}", id, result);

	}

	public FacebookUserProfile getUserProfile(String psid, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String accessToken = environment.getProperty("facebook.lane." + lane + ".accessToken");
		return restService.ajax("https://graph.facebook.com").path("/{psid}").pathParam("psid", psid)
				.queryParam("fields", "first_name,last_name,profile_pic").queryParam("access_token", accessToken).get()
				.as(FacebookUserProfile.class);

	}
}
