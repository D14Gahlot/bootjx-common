package com.boot.jx.postman.fb;

import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;

import com.boot.jx.dict.FileType;
import com.boot.jx.exception.AmxException;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-facebook.properties")
@EnableEncryptableProperties
public class FacebooClient implements MessageClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacebooClient.class);

	@Autowired
	RestService restService;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private ExtUtilService extUtilService;

	private FacebookConfig getConfig(String lane) {
		if (ArgUtil.isEmpty(lane)) {
			throw new PostManException("No lane " + lane);
		}
		FacebookConfig config = environment.config().facebook(lane);

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

	@Override
	public OutboxMessage send(OutboxMessage outboxMessage) {
		String to = CollectionUtil.getOne(outboxMessage.getTo());
		String lane = outboxMessage.getLane();

		FacebookMessageResp resp = null;
		StringJoiner msgIds = new StringJoiner(",");

		try {
			if (ArgUtil.is(outboxMessage.getAttachments())) {
				for (Attachment attachment : outboxMessage.getAttachments()) {

					FacebookMessageRequest req = new FacebookMessageRequest();
					req.recipientId(to);
					if (ArgUtil.is(attachment.getMediaURL())) {
						if (ArgUtil.areEqual(attachment.getMediaType(), FileType.IMAGE.toString())) {
							req.attachmentType("image").attachmentUrl(attachment.getMediaURL());
						} else {
							req.messageType("text");
							req.messageText(extUtilService.tinyUrl(attachment.getMediaURL()));
							// req.attachmentType("file").attachmentUrl(attachment.getMediaURL());
						}
					}
					resp = sendReply(lane, req);
					msgIds.add(ArgUtil.parseAsString(resp.getMessageId()));
				}
			}

			if (ArgUtil.is(outboxMessage.getMessage())) {
				FacebookMessageRequest req = new FacebookMessageRequest();
				req.recipientId(to);
				req.messageType("text");
				req.messageText(outboxMessage.getMessage());
				resp = sendReply(lane, req);
				if (ArgUtil.is(resp.getMessageId()))
					msgIds.add(ArgUtil.parseAsString(resp.getMessageId()));
			}
		} catch (HttpStatusCodeException | AmxException e) {
			if (e instanceof HttpStatusCodeException)
				resp = JsonUtil.parse(((HttpStatusCodeException) e).getResponseBodyAsString(),
						FacebookMessageResp.class);
			else
				resp = JsonUtil.parse(e.getMessage(), FacebookMessageResp.class);
			
			outboxMessage.logs().add(resp.getError().getMessage());
			outboxMessage.logs()
					.add(String.format("%s-%s", resp.getError().getCode(), resp.getError().getErrorSubcode()));
			outboxMessage.logs().add(ArgUtil.parseAsString(resp.getError().getFbtraceId()));
		}

		outboxMessage.setMessageIdExt(msgIds.toString());

		return outboxMessage;
	}

}
