package com.boot.jx.connectors;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(value = ContactType.WHATSAPP, channel = "RAPIWHA")
public class WARapiwhaConnector implements ConnectorHandler {

	private static Logger LOGGER = LoggerService.getLogger(WARapiwhaConnector.class);

	@Autowired
	private RestService restService;

	@Value("${rapiwha.api.key}")
	private String apiWhaKey;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public void send(String lane, String to, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(outboxMessage.getChannel());
		String text = outboxMessage.getMessage();
		if (ArgUtil.is(outboxMessage.getTemplate())) {
			TemplateReply mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
			if (ArgUtil.is(mediaReply)) {
				if ("image".equalsIgnoreCase(mediaReply.getType())) {
					outboxMessage.attachment(
							new Attachment().mediaURL(mediaReply.getUrl()).mediaType(File.FileType.IMAGE.toString()));
					text = mediaReply.getUrl();
				}
			} else {
				tmplClient.process(outboxMessage);
				text = outboxMessage.getMessage();
			}
		}
		String responseText = restService.ajax("http://panel.apiwha.com/send_message.php").field("apikey", apiWhaKey)
				.field("number", to).field("text", text).postForm().asString();
		LOGGER.info(responseText);
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	@Override
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		ChatContactDoc contact = sessionStore.getContact(inboxMessage);
		Object x = inboxMessage.getOriginalMessage();
		if (ArgUtil.is(x)) {
			Map<String, Object> map = JsonUtil.toMap(x);
			contact.setProfilePic(ArgUtil.parseAsString(map.get("profilepicture"), Constants.BLANK));
			contact.setName(ArgUtil.parseAsString(map.get("pushname"), Constants.BLANK));
			sessionStore.save(contact);
		}
		return true;
	}

	public InboxMessage toInboxMessage(Map<String, Object> dataMap, String lane) {
		InboxMessage event = new InboxMessage();
		String eventName = ArgUtil.parseAsString(dataMap.get("event"), Constants.BLANK);
		event.setContactType(ContactType.WHATSAPP);
		event.setChannel("RAPIWHA");
		event.setLane(lane);
		if ("INBOX".equals(eventName)) {
			event.from(ArgUtil.parseAsString(dataMap.get("from"), Constants.BLANK));
			event.setTo(ArgUtil.parseAsString(dataMap.get("to"), Constants.BLANK));
			event.setMessage(ArgUtil.parseAsString(dataMap.get("text"), Constants.BLANK));
			event.setFromName(ArgUtil.parseAsString(dataMap.get("pushname"), Constants.BLANK));
			event.setOriginalMessage(dataMap);
		}
		return event;
	}

}
