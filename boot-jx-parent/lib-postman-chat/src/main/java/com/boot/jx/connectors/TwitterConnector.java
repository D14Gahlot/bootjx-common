package com.boot.jx.connectors;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.gupshup.GupShupConfigClient;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.WAMessage.Channel;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterClientContext;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.DirectMessageLocalImpl;
import twitter4j.ResponseList;
import twitter4j.TwitterException;
import twitter4j.UploadedMedia;

@Component
@ConnectorMapping(contactType = ContactType.TWITTER)
public class TwitterConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterConnector.class);

	@Autowired
	private TwitterClient twitterClient;

	@Autowired
	protected GupShupConfigClient gupShupConfig;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	private ExtUtilService extUtilService;

	private String getMediaId(String lane, TemplateReply templateReply)
			throws IOException, MalformedURLException, TwitterException {
		String mediaId = ArgUtil.parseAsString(templateReply.meta().get("twitterMediaId"));
		// TODO:-Media cannot be shared, will update this api once we start using
		// "SHARED MEDIA across Multiple messgaes"
		// if (!ArgUtil.is(mediaId)) {
		TwitterClientContext ctx = twitterClient.getContext(lane);
		InputStream media = new java.net.URL(templateReply.getUrl()).openStream();
		UploadedMedia uploadedMedia = ctx.getTwitter().uploadMedia(templateReply.getTitle(), media);
		mediaId = ArgUtil.parseAsString(uploadedMedia.getMediaId());
		// templateReply.meta().put("twitterMediaId", mediaId);
		// mongoTemplate.save(templateReply);
		// }
		return mediaId;
	}

	public String attachLink(OutboxMessage outboxMessage) {
		StringJoiner sj = new StringJoiner("\n");
		sj.add(outboxMessage.getMessage());
		if (ArgUtil.is(outboxMessage.getAttachments())) {
			for (Attachment attachment : outboxMessage.getAttachments()) {
				if (ArgUtil.is(attachment.getMediaURL())) {
					sj.add(extUtilService.tinyUrl(attachment.getMediaURL()));
				}
			}
		}
		return sj.toString();
	}

	@Override
	public void send(String lane, String to, OutboxMessage outboxMessage) {
		try {
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				TemplateReply templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
				if (ArgUtil.is(templateReply)) {
					if ("image".equalsIgnoreCase(templateReply.getType())) {
						String mediaId = getMediaId(lane, templateReply);
						outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
								.mediaType(File.FileType.IMAGE.toString()).mediaId(mediaId));
						twitterClient.sendReply(to, outboxMessage.getMessage(), mediaId, lane);
					} else {
						twitterClient.sendReply(to, attachLink(outboxMessage), lane);
					}
				} else {
					tmplClient.process(outboxMessage);
					twitterClient.sendReply(to, attachLink(outboxMessage), lane);
				}
			} else {
				twitterClient.sendReply(to, attachLink(outboxMessage), lane);
			}
			outboxMessage.setStatus(OutboxMessage.Status.SENT);
		} catch (NumberFormatException e) {
			outboxMessage.setStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		} catch (TwitterException e) {
			outboxMessage.setStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		} catch (MalformedURLException e) {
			outboxMessage.setStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			outboxMessage.setStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			outboxMessage.setStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		// twitterClient.sendReply(inboxMessage.getFrom(), "Call us @ " +
		// gupShupConfig.getGupShupWaNumber(),inboxMessage.getLane());
		return inboxMessage;
	}

	@Override
	public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
		this.send(inboxMessage.getLane(), inboxMessage.getFrom(), outboxMessage);
	}

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		this.send(chatContactDoc.getLane(), chatContactDoc.getCsid(), outboxMessage);
	}

	public InboxMessage toInboxMessage(DirectMessage dm, String lane) {
		InboxMessage ibm = new InboxMessage();
		ibm.setMessageIdExt(String.valueOf(dm.getId()));
		ibm.setMessage(dm.getText());
		ibm.setFrom(String.valueOf(dm.getSenderId()));
		ibm.setTo(String.valueOf(dm.getRecipientId()));
		ibm.setChannel(Channel.DEFAULT.toString());
		ibm.setContactType(ContactType.TWITTER);
		ibm.setLane(lane);

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
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
		if (ArgUtil.is(inboxMessage.getOriginalMessage())) {
			try {
				DirectMessageLocalImpl dm = JsonUtil.parse(inboxMessage.getOriginalMessage(),
						DirectMessageLocalImpl.class);
				contact.setProfilePic(dm.getSender().getProfileImageURLHttps());
				contact.setName(dm.getSender().getName());
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

}
