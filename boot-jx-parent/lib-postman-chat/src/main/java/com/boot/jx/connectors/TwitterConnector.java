package com.boot.jx.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterClientContext;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.DirectMessageLocalImpl;
import twitter4j.ResponseList;
import twitter4j.TwitterException;

@Component
@ConnectorMapping(contactType = ContactType.TWITTER)
public class TwitterConnector extends AbstractConnector {

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterConnector.class);

    @Autowired
    private TwitterClient twitterClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private TmplClient tmplClient;

    @Override
    public void send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	try {
	    if (ArgUtil.is(outboxMessage.getTemplate())) {
		QuickMedia templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
		if (ArgUtil.is(templateReply)) {
		    if ("image".equalsIgnoreCase(templateReply.getType())) {
			outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
				.mediaType(FileType.IMAGE.toString()).mediaCaption(templateReply.getTitle()));
			twitterClient.send(null, outboxMessage);
		    } else {
			twitterClient.send(null, outboxMessage);
		    }
		} else {
		    tmplClient.process(outboxMessage);
		    twitterClient.send(null, outboxMessage);
		}
	    } else {
		twitterClient.send(null, outboxMessage);
	    }
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT);
	} catch (Exception e) {
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
	    outboxMessage.logs().add(e.getMessage());
	    LOGGER.error("SEND ERROR", e);
	}
    }

    @Override
    public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	return inboxMessage;
    }

    public InboxMessage toInboxMessage(DirectMessage dm, String lane) {
	InboxMessage ibm = new InboxMessage();
	ibm.setMessageIdExt(String.valueOf(dm.getId()));
	ibm.setMessage(dm.getText());
	ibm.setFrom(String.valueOf(dm.getSenderId()));
	ibm.to().add(String.valueOf(dm.getRecipientId()));
	ibm.contact().setChannelType(Channel.DEFAULT.toString());
	ibm.contact().setContactType(ContactType.TWITTER.toString());
	ibm.contact().setLane(lane);
	ibm.contact().setCsid(String.valueOf(dm.getSenderId()));

	/**
	 * NOTE:- Do not user original DirectMessageJsonImpl as it can throw
	 * serialization error
	 */
	if (dm instanceof DirectMessageLocalImpl) {
	    ibm.setOriginalMessage(dm);
	}

	return ibm;
    }

    @Override
    public boolean initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
	if (ArgUtil.is(inboxMessage.getOriginalMessage())) {
	    try {
		DirectMessageLocalImpl dm = JsonUtil.parse(inboxMessage.getOriginalMessage(),
			DirectMessageLocalImpl.class);
		ChatContactQuery contactQuery = messageContext.getChatContactQuery();
		contactQuery.setProfilePic(dm.getSender().getProfileImageURLHttps());
		contactQuery.setName(dm.getSender().getName());
	    } catch (Exception e) {
		LOGGER.error("Twitter Init Session Data Parse Errror", e);
	    }
	}
	return true;
    }

    public List<InboxMessage> messageConverter(ResponseList<DirectMessage> dml, String lane) {
	List<InboxMessage> inboxMsg = new ArrayList<InboxMessage>();
	if (ArgUtil.is(dml)) {
	    for (DirectMessage dm : dml) {
		InboxMessage ibm = toInboxMessage(dm, lane);
		inboxMsg.add(ibm);
	    }
	}
	return inboxMsg;
    }

    public List<InboxMessage> fetch(String lane) throws TwitterException {
	DirectMessageList dml = twitterClient.pollDirectMessagesReceived(lane);
	return messageConverter(dml, lane);
    }

    public List<InboxMessage> process(String lane, Map<String, Object> update) throws TwitterException {
	TwitterClientContext ctx = twitterClient.getContext(lane);
	DirectMessageList dml = DirectMessageLocalImpl.createDirectMessageList(update,
		ctx.getTwitter().getConfiguration());
	dml = ctx.removeDMsNotSentToMe(dml);
	return messageConverter(dml, lane);
    }

    @Override
    public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
	    MessageBoxEvent messageBoxEvent) {
	try {
	    messageBoxEvent.addInboxMessage(process(channelConfig.getLane(), requestMap.toMap()));
	} catch (TwitterException e) {
	    LOGGER.error("Exception while converting inbox message from twitter webhook", e);
	}
	return messageBoxEvent;
    }

    @Override
    public List<InboxMessage> onReadInboxMessage(ChannelConfig channelConfig, List<InboxMessage> inboxMessages) {
	if (inboxMessages != null && !inboxMessages.isEmpty()) {
	    for (InboxMessage event : inboxMessages) {
		try {
		    twitterClient.getContext(channelConfig.getLane()).getTwitter()
			    .destroyDirectMessage(Long.parseLong(event.getMessageIdExt()));
		} catch (NumberFormatException | TwitterException e) {
		    LOGGER.error("Exception while processing after reading inbox message from twitter webhook", e);
		}
	    }
	}
	return inboxMessages;
    }

}
