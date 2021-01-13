package com.boot.jx.connectors;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterMessageResponse;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.Paging;
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

	public InboxMessage toInboxMessage(FacebookMessaging m, String lane) {
		String id = m.getSender().get("id");
		InboxMessage event = new InboxMessage();
		event.setChannel(Channel.GUPSHUP.toString());
		event.from(id);
		event.setMessage(m.getMessage().getText());
		event.setTo(m.getRecipient().get("id"));
		event.setContactType(ContactType.TWITTER);
		event.setLane(lane);
		return event;
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

	long sinceId = 0;
	String nextCursor=null;

	public TwitterMessageResponse fetch() throws TwitterException {

		if (nextCursor==null) {
			DirectMessageList dml = twitterClient.getTwitter().getDirectMessages(5);
			nextCursor =dml.getNextCursor();
			TwitterMessageResponse tmr = messageConverter(dml);
			return tmr;
		} else {
			DirectMessageList dml = twitterClient.getTwitter().getDirectMessages(5, nextCursor);
			nextCursor = dml.getNextCursor();
			TwitterMessageResponse tmr = messageConverter(dml);
			return tmr;
		}
		
	}

	public TwitterMessageResponse messageConverter(ResponseList<DirectMessage>  dml) {

		TwitterMessageResponse tmr = new TwitterMessageResponse();
		List<InboxMessage> inboxMsg = new ArrayList<InboxMessage>();
		for (DirectMessage dm : dml) {
			InboxMessage ibm = new InboxMessage();
			ibm.setMessageIdExt(String.valueOf(dm.getId()));
			ibm.setMessage(dm.getText());
			ibm.setFrom(String.valueOf(dm.getSenderId()));
			ibm.setTo(String.valueOf(dm.getRecipientId()));
			ibm.setChannel(Channel.GUPSHUP.toString());
			ibm.setContactType(ContactType.TWITTER);
			sinceId = Math.max(sinceId, dm.getId());
			System.out.println("sinceId { }"+sinceId);
			inboxMsg.add(ibm);

		}
		tmr.setInboxMsg(inboxMsg);
		return tmr;
	}

}
