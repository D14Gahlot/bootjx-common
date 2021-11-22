package com.boot.jx.postman.ig;

import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.boot.jx.dict.FileType;
import com.boot.jx.exception.AmxException;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

@Component
@PropertySource("classpath:application-instagram.properties")
@EnableEncryptableProperties
public class InstagramClient implements MessageClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramClient.class);

    @Autowired
    RestService restService;

    @Autowired
    private PMEnvironment environment;

    @Autowired
    private ExtUtilService extUtilService;

    private InstagramConfig getConfig(String lane, String channelId) {
	if (ArgUtil.isEmpty(channelId)) {
	    if (ArgUtil.isEmpty(lane)) {
		throw new PostManException("No lane " + lane);
	    } else {
		channelId = PostManUtil.CHANNEL_ID(CHANNEL_TYPE.INSTAGRAM, lane);
	    }
	}
	ChannelConfig config = environment.config().channels(channelId);

	if (!ArgUtil.is(config)) {
	    throw new PostManException("No Config for " + channelId);
	}

	if (!ArgUtil.is(config.getInstagram())) {
	    throw new PostManException("No Instagram Config for " + channelId);
	}

	return config.getInstagram();
    }

    private InstagramConfig getConfig(Contactable contact) {
	return getConfig(null, PostManUtil.CHANNEL_ID(contact));
    }

    public String registerWebhook(String token, String challenge, String lane, String channelId) {
//	InstagramConfig config = getConfig(lane, channelId);
	String verifyToken = "TOKEN";
	if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
	    return challenge;
	} else {
	    return "Wrong Token";
	}
    }

    public InstagramMessageResp sendReply(String lane, InstagramMessageRequest resp) {
	InstagramConfig config = getConfig(lane, null);
	return restService.ajax("https://graph.facebook.com/v2.6/me/messages?access_token=" + config.getAccessToken())
		.post(resp).as(new ParameterizedTypeReference<InstagramMessageResp>() {
		});
    }

    public InstagramMessageResp sendReply(String id, String text, String lane) {
	InstagramMessageRequest response = new InstagramMessageRequest();
	response.messageType("text");
	response.recipientId(id);
	response.messageText(text);
	return sendReply(lane, response);
    }

    public InstagramUserProfile getUserProfile(Contactable contact) {
	InstagramConfig config = getConfig(contact);
	return restService.ajax("https://graph.facebook.com/v12.0").path("/{igsid}").pathParam("igsid", contact.getCsid())
		.queryParam("fields", "name,profile_pic,id")
		.queryParam("access_token", config.getAccessToken()).get().as(InstagramUserProfile.class);

    }

    @Override
    public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	String to = CollectionUtil.getOne(outboxMessage.getTo());
	String lane = outboxMessage.contact().getLane();

	InstagramMessageResp resp = null;
	StringJoiner msgIds = new StringJoiner(",");

	try {
	    if (ArgUtil.is(outboxMessage.getAttachments())) {
		for (Attachment attachment : outboxMessage.getAttachments()) {

		    InstagramMessageRequest req = new InstagramMessageRequest();
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
	    	InstagramMessageRequest req = new InstagramMessageRequest();
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
			InstagramMessageResp.class);
	    else
		resp = JsonUtil.parse(e.getMessage(), InstagramMessageResp.class);

	    outboxMessage.logs().add(resp.getError().getMessage());
	    outboxMessage.logs()
		    .add(String.format("%s-%s", resp.getError().getCode(), resp.getError().getErrorSubcode()));
	    outboxMessage.logs().add(ArgUtil.parseAsString(resp.getError().getFbtraceId()));
	}

	outboxMessage.setMessageIdExt(msgIds.toString());

	return outboxMessage;
    }

}
