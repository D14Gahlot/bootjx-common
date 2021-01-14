package com.boot.jx.connectors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterClientContext;
import com.boot.utils.ArgUtil;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.DirectMessageLocalImpl;
import twitter4j.ResponseList;
import twitter4j.TwitterException;

@Component
@ConnectorMapping(ContactType.TWITTER)
public class TwitterConnector implements ConnectorHandler {

	@Autowired
	private TwitterClient twitterClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Autowired
	private SessionStore sessionStore;

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		// twitterClient.sendReply(inboxMessage.getFrom(), "Call us @ " +
		// gupShupConfig.getGupShupWaNumber(),inboxMessage.getLane());
		return inboxMessage;
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		try {
			twitterClient.sendReply(inboxMessage.getFrom(), outboxMessage.getMessage(), inboxMessage.getLane());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
			twitterClient.sendReply(chatContactDoc.getCsid(), outboxMessage.getMessage(), chatContactDoc.getLane());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public InboxMessage toInboxMessage(DirectMessage dm, String lane) {
		InboxMessage ibm = new InboxMessage();
		ibm.setMessageIdExt(String.valueOf(dm.getId()));
		ibm.setMessage(dm.getText());
		ibm.setFrom(String.valueOf(dm.getSenderId()));
		ibm.setTo(String.valueOf(dm.getRecipientId()));
		ibm.setChannel(Channel.DEFAULT.toString());
		ibm.setContactType(ContactType.TWITTER);
		return ibm;
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
		return messageConverter(dml, lane);
	}

}
