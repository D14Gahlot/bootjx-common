package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.FACEBOOK)
public class FacebookConnector extends AbstractConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookConnector.class);

    @Autowired
    private FacebooClient facebooClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TmplClient tmplClient;

    @Override
    public void registerWebHook(ChannelConfig channelConfig) {
	ApiResponseUtil.addWarning("Set webhook URL manually from Facebook Developer Portal.");
    }

    public void send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	try {
	    if (ArgUtil.is(outboxMessage.getTemplate())) {
		QuickMedia mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
		if (ArgUtil.is(mediaReply)) {
		    if ("image".equalsIgnoreCase(mediaReply.getType())) {
			outboxMessage.attachment(
				new Attachment().mediaURL(mediaReply.getUrl()).mediaType(FileType.IMAGE.toString()));
		    }
		} else {
		    tmplClient.process(outboxMessage);
		}
	    }
	    facebooClient.send(null, outboxMessage);
	    outboxMessage.updateStatus(Message.Status.SENT);
	} catch (Exception e) {
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
	    outboxMessage.logs().add(e.getMessage());
	    LOGGER.error("SEND ERROR", e);
	}
    }

    @Override
    public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	this.reply(null, inboxMessage, new OutboxMessage().message("Our agent will get in touch with you"));
	return inboxMessage;
    }

    @Override
    public boolean initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
	FacebookUserProfile profile = facebooClient.getUserProfile(inboxMessage.contact());
	ChatContactQuery contactQuery = messageContext.getChatContactQuery();
	contactQuery.setProfilePic(profile.getProfilePic());
	contactQuery.setName(profile.getFirstName() + " " + profile.getLastName());
	contactQuery.setEmail(profile.getEmail());
	return true;
    }

    @Deprecated
    public InboxMessage toInboxMessage(FacebookMessaging m, String lane) {
	InboxMessage event = new InboxMessage();
	String id = m.getSender().get("id");
	event.contact().setChannelType(CHANNEL_TYPE.FACEBOOK);
	event.setFrom(id);
	event.contact().setCsid(id);
	event.setMessage(m.getMessage().getText());
	event.to().add(m.getRecipient().get("id"));
	event.contact().type(ContactType.FACEBOOK);
	event.contact().setLane(lane);
	return event;
    }

    public InboxMessage toInboxMessage(FacebookMessaging m, ChannelConfig channelConfig) {
	// Create Default Message from Channel
	InboxMessage inboxMessage = this.createInboxMessage(channelConfig);

	// Set Contact info
	String csid = m.getSender().get("id");

	inboxMessage.contact().setCsid(csid);

	// Set Additional info
	inboxMessage.setFrom(csid);
	inboxMessage.to().add(m.getRecipient().get("id"));

	// Extract Message Details
	inboxMessage.setMessageIdExt(m.getMessage().getMid());
	inboxMessage.setMessage(m.getMessage().getText());

	return inboxMessage;
    }

    private MessageReport toMessageReport(FacebookMessaging m, ChannelConfig channelConfig) {
	MessageReport report = this.createMessageReport(channelConfig);
	String csid = m.getSender().get("id");
	report.contact().setCsid(csid);
	report.setChangeStamp(m.getTimestamp());
	if (ArgUtil.is(m.getRead())) {
	    report.setChangeStamp(m.getReadWatermark());
	    report.setStatus(Status.READ);
	}
	return report;
    }

    @Override
    public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
	    MessageBoxEvent messageBoxEvent) {
	FacebookHookRequest request = requestMap.as(FacebookHookRequest.class);
	request.getEntry().forEach(pageEntry -> {
	    pageEntry.getMessaging().forEach(m -> {
		if (ArgUtil.is(m.getMessage())) {
		    messageBoxEvent.addInboxMessage(toInboxMessage(m, channelConfig));
		} else if (ArgUtil.is(m.getRead())) {
		    messageBoxEvent.addMessageReport(toMessageReport(m, channelConfig));
		}
	    });
	});
	return messageBoxEvent;
    }

}
