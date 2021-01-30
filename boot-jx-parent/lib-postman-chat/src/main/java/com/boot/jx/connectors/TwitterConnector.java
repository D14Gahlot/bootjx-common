package com.boot.jx.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.internal.util.privilegedactions.GetConstraintValidatorList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.gupshup.GupShupConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterClientContext;
import com.boot.utils.ArgUtil;
import com.boot.utils.MapBuilder;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.DirectMessageLocalImpl;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;

@Component
@ConnectorMapping(ContactType.TWITTER)
public class TwitterConnector implements ConnectorHandler {

	@Autowired
	private TwitterClient twitterClient;

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MongoTemplate mongoTemplate;

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
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				TemplateReply templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
				if ("image".equalsIgnoreCase(templateReply.getType())) {
					Long mediaId = ArgUtil.parseAsLong(templateReply.meta().get("twitterMediaId"));
					if (!ArgUtil.is(mediaId)) {
						TwitterClientContext ctx = twitterClient.getContext(chatContactDoc.getLane());
						InputStream media = new java.net.URL(templateReply.getUrl()).openStream();
						UploadedMedia uploadedMedia = ctx.getTwitter().uploadMedia(templateReply.getTitle(), media);
						mediaId = uploadedMedia.getMediaId();
						templateReply.meta().put("twitterMediaId", mediaId);
						mongoTemplate.save(templateReply);
					}
					twitterClient.sendReply(chatContactDoc.getCsid(), outboxMessage.getMessage(), mediaId,
							chatContactDoc.getLane());
				}
			} else {
				twitterClient.sendReply(chatContactDoc.getCsid(), outboxMessage.getMessage(), chatContactDoc.getLane());
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (TwitterException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
	public boolean initSession(InboxMessage inboxMessage, ChatSessionDoc session) {
		if (ArgUtil.is(inboxMessage.getOriginalMessage())) {
			ChatContactDoc contact = sessionStore.getContact(inboxMessage);
			DirectMessageLocalImpl dm = (DirectMessageLocalImpl) inboxMessage.getOriginalMessage();
			contact.setProfilePic(dm.getSender().getProfileBannerURL());
			contact.setName(dm.getSender().getName());
			sessionStore.save(contact);
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
		return messageConverter(dml, lane);
	}

}
