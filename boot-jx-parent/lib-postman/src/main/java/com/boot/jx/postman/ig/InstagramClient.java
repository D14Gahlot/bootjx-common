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
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.rest.RestService;
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

    public String registerWebhook(String token, String challenge, String lane, String channelId) {
//	InstagramConfig config = getConfig(lane, channelId);
	String verifyToken = "TOKEN";
	if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
	    return challenge;
	} else {
	    return "Wrong Token";
	}
    }

    private InstagramMessageResp sendReply(ChannelConfig config, InstagramMessageRequest resp) {
	return restService
		.ajax("https://graph.facebook.com/v2.6/me/messages?access_token="
			+ config.getInstagram().getAccessToken())
		.post(resp).as(new ParameterizedTypeReference<InstagramMessageResp>() {
		});
    }

    public InstagramUserProfile getUserProfile(ChannelConfig config, Contactable contact) {
	return restService.ajax("https://graph.facebook.com/v12.0").path("/{igsid}")
		.pathParam("igsid", contact.getCsid()).queryParam("fields", "name,profile_pic,id")
		.queryParam("access_token", config.getInstagram().getAccessToken()).get()
		.as(InstagramUserProfile.class);

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
		    resp = sendReply(channelConfig, req);
		    msgIds.add(ArgUtil.parseAsString(resp.getMessageId()));
		}
	    }

	    if (ArgUtil.is(outboxMessage.getMessage())) {
		InstagramMessageRequest req = new InstagramMessageRequest();
		req.recipientId(to);
		req.messageType("text");
		req.messageText(outboxMessage.getMessage());
		resp = sendReply(channelConfig, req);
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
