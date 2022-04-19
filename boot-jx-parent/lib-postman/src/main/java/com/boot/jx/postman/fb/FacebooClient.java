package com.boot.jx.postman.fb;

import java.util.List;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.boot.jx.dict.FileType;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.FacebookPlugin.FacebookConfigDetails;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-facebook.properties")
@EnableEncryptableProperties
public class FacebooClient implements MessageClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacebooClient.class);

	@Autowired
	private RestService restService;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private ExtUtilService extUtilService;

	public String registerWebhook(ChannelConfig channelConfig, String token, String challenge) {
		FacebookConfigDetails config = channelConfig.getFacebook();
		String verifyToken = config.getVerifyToken();
		if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
			return challenge;
		} else {
			return "Wrong Token";
		}
	}

	public FacebookMessageResp sendReply(ChannelConfig channelConfig, FacebookMessageRequest resp) {
		return restService
				.ajax("https://graph.facebook.com/v2.6/me/messages?access_token="
						+ channelConfig.getFacebook().getAccessToken())
				.post(resp).as(new ParameterizedTypeReference<FacebookMessageResp>() {
				});
	}

	public MapModel sendAdvanced(ChannelConfig config, MapModel map) {
		String url = "https://graph.facebook.com/v2.6/me/messages?access_token="
				+ config.getFacebook().getAccessToken();

		return restService.ajax(url).post(map.toMap()).asMapModel();
	}

	public FacebookUserProfile getUserProfile(ChannelConfig config, Contactable contact) {
		return restService.ajax("https://graph.facebook.com").path("/{psid}").pathParam("psid", contact.getCsid())
				.queryParam("fields", "first_name,last_name,profile_pic,email,id")
				.queryParam("access_token", config.getFacebook().getAccessToken()).get().as(FacebookUserProfile.class);
	}

	@Override
	public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
		String to = CollectionUtil.getOne(outboxMessage.getTo());
		String csid = outboxMessage.contact().getCsid();
		String lane = outboxMessage.contact().getLane();
		Boolean isTemplate = false;

		FacebookMessageResp resp = null;
		StringJoiner msgIds = new StringJoiner(",");

		MapModel reqMessage = MapModel.createInstance().put(new JsonPath("recipient/id"), csid);

		if (outboxMessage.options().containsKey("buttons")) {
			List<TmplElement> buttons = new MapModel(outboxMessage.options()).entry("buttons")
					.asList(TmplElement.class);
			if (buttons.size() > 3) {
				isTemplate = true;

				String messageTag = FacebookConstants.MESSAGE_TAG(outboxMessage.getCategoryType());

				if (ArgUtil.is(messageTag)) {
					reqMessage.put("messaging_type", "MESSAGE_TAG");
					reqMessage.put("tag", messageTag);
				} else {
					reqMessage.put("messaging_type", "RESPONSE");
				}

				MapModel messageModel = MapModel.createInstance();
				if (ArgUtil.is(outboxMessage.getMessage())) {
					messageModel.put("text", outboxMessage.getMessage());
				}
				MapModel quickreplies = MapModel.createInstance();
				for (TmplElement button : buttons) {
					quickreplies.add(MapModel.createInstance().put("title", button.getLabel())
							.put("content_type", "text").put("payload", button.getName()).toMap());
				}
				messageModel.put("quick_replies", quickreplies.list());
				reqMessage.put("message", messageModel.toMap());
			} else {
				isTemplate = true;
				MapModel messageModel = MapModel.createInstance();
				messageModel.put(new JsonPath("/attachment/type"), "template");
				MapModel payloadModel = MapModel.createInstance();
				payloadModel.put(new JsonPath("template_type"), "generic");
				MapModel elements = MapModel.createInstance();
				MapModel elementModelArray = MapModel.createInstance();
				MapModel elementButtons = MapModel.createInstance();
				MapModel elementModel = MapModel.createInstance();
				if (ArgUtil.is(outboxMessage.getSubject())) {
					elementModel.put(new JsonPath("title"), outboxMessage.getSubject());
					if (ArgUtil.is(outboxMessage.getMessage())) {
						elementModel.put(new JsonPath("subtitle"), outboxMessage.getMessage());
					}
				} else {
					if (ArgUtil.is(outboxMessage.getMessage())) {
						elementModel.put(new JsonPath("title"), outboxMessage.getMessage());
					}
				}

				for (TmplElement button : buttons) {
					if (ArgUtil.is(button.getUrl())) {
						elementButtons.add(MapModel.createInstance().put("title", button.getLabel())
								.put("type", "web_url").put("url", button.getUrl()).toMap());
					} else {
						elementButtons.add(MapModel.createInstance().put("title", button.getLabel())
								.put("type", "postback").put("payload", button.getName()).toMap());
					}
				}
				if (elementButtons.list().size() > 0) {
					elementModel.put("buttons", elementButtons.list());
				}

				elementModelArray.add(elementModel.toMap());
				elements.add(elementModelArray.list());

				payloadModel.put("template_type", "generic");
				payloadModel.put("elements", elementModelArray.list());
				messageModel.put(new JsonPath("/attachment/payload"), payloadModel.toMap());
				reqMessage.put("message", messageModel.toMap());
			}
		}
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
					resp = sendReply(channelConfig, req);
					msgIds.add(ArgUtil.parseAsString(resp.getMessageId()));
				}
			}

			if (ArgUtil.is(outboxMessage.getMessage())) {
				if (outboxMessage.options().containsKey("buttons") && isTemplate) {
					MapModel responseModel = sendAdvanced(channelConfig, reqMessage);
					if (ArgUtil.is(responseModel.get("message_id"))) {
						msgIds.add(ArgUtil.parseAsString(responseModel.get("message_id")));
					}
				} else {
					FacebookMessageRequest req = new FacebookMessageRequest();
					req.recipientId(to);
					req.messageType("text");
					req.messageText(outboxMessage.getMessage());
					resp = sendReply(channelConfig, req);
					if (ArgUtil.is(resp.getMessageId()))
						msgIds.add(ArgUtil.parseAsString(resp.getMessageId()));
				}
			}
		} catch (HttpStatusCodeException | ApiHttpException e) {
			if (e instanceof HttpStatusCodeException) {
				resp = JsonUtil.parse(((HttpStatusCodeException) e).getResponseBodyAsString(),
						FacebookMessageResp.class);
			} else {
				resp = JsonUtil.parse(((ApiHttpException) e).getResponse().getBody(), FacebookMessageResp.class);
			}
			outboxMessage.logs().add(resp.getError().getMessage());
			outboxMessage.logs()
					.add(String.format("%s-%s", resp.getError().getCode(), resp.getError().getErrorSubcode()));
			outboxMessage.logs().add(ArgUtil.parseAsString(resp.getError().getFbtraceId()));
		} catch (Exception e) {
			outboxMessage.logs().add(e.getMessage());
		}
		outboxMessage.setMessageIdExt(msgIds.toString());
		return outboxMessage;
	}

}
