package com.boot.jx.postman.fb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostManException;
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
	private PMEnvironment environment;

	private FacebookConfig getConfig(String lane) {
		if (ArgUtil.isEmpty(lane)) {
			throw new PostManException("No lane " + lane);
		}
		FacebookConfig config = environment.get().facebook(lane);

		if (!ArgUtil.is(config)) {
			throw new PostManException("No Config for lane " + lane);
		}
		return config;
	}

	public String registerWebhook(String token, String challenge, String lane) {
		FacebookConfig config = getConfig(lane);
		String verifyToken = config.getVerifyToken();
		if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
			return challenge;
		} else {
			return "Wrong Token";
		}
	}

	public FacebookMessageResp sendReply(String lane, FacebookMessageRequest resp) {
		FacebookConfig config = getConfig(lane);
		return restService.ajax("https://graph.facebook.com/v2.6/me/messages?access_token=" + config.getAccessToken())
				.post(resp).as(new ParameterizedTypeReference<FacebookMessageResp>() {
				});
	}

	public FacebookMessageResp sendReply(String id, String text, String lane) {
		FacebookMessageRequest response = new FacebookMessageRequest();
		response.messageType("text");
		response.recipientId(id);
		response.messageText(text);
		return sendReply(lane, response);
	}

	public FacebookUserProfile getUserProfile(String psid, String lane) {
		FacebookConfig config = getConfig(lane);
		return restService.ajax("https://graph.facebook.com").path("/{psid}").pathParam("psid", psid)
				.queryParam("fields", "first_name,last_name,profile_pic,email,id")
				.queryParam("access_token", config.getAccessToken()).get().as(FacebookUserProfile.class);

	}

}
