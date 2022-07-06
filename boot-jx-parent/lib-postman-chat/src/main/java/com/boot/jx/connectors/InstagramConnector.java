package com.boot.jx.connectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.fb.FacbookAttachment;
import com.boot.jx.postman.fb.FacebookConstants.InBoundWrapperPaths;
import com.boot.jx.postman.fb.FacebookEntry;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.InstagramClient;
import com.boot.jx.postman.fb.InstagramUserProfile;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.InstagramPlugin;
import com.boot.jx.postman.plugin.InstagramPlugin.InstagramConfig;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.INSTAGRAM)
public class InstagramConnector extends AbstractConnector<InstagramConfig, InstagramPlugin> {
	private static final Logger LOGGER = LoggerFactory.getLogger(InstagramConnector.class);

	@Override
	public InstagramPlugin getPlugin() {
		return ChannelPluginProvider.INSTAGRAM;
	}

	@Autowired
	private InstagramClient instaClient;

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig) {
		ApiResponseUtil.addWarning("Set webhook URL manually from Facebook Developer Portal.");
	}

	@Override
	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		// System.out.println("onSend=====" + JsonUtil.toJson(outboxMessage));
		template(channelConfig, chatContactDoc, outboxMessage);
		instaClient.send(channelConfig, outboxMessage);
		outboxMessage.updateStatus(Message.Status.SENT);
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		this.reply(null, null, new OutboxMessage().message("Our agent will get in touch with you"), inboxMessage);
		return inboxMessage;
	}

	@Override
	public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		// System.out.println("initSession=====" + JsonUtil.toJson(inboxMessage));
		try {
			ChannelConfig config = getChannelConfig(inboxMessage);
			InstagramUserProfile profile = instaClient.getUserProfile(config, inboxMessage.contact());
			ChatContactQuery contactQuery = messageContext.contact();
			contactQuery.setProfilePic(profile.getProfilePic());
			contactQuery.setName(profile.getName());
			contactQuery.setEmail(profile.getEmail());
		} catch (ApiHttpException e) {
			logManager.error(inboxMessage, e);
		}
		return null;
	}

	@Deprecated
	public InboxMessage toInboxMessage(FacebookMessaging m, String lane) {
		InboxMessage event = new InboxMessage();
		String id = m.getSender().get("id");
		event.contact().setChannelType(CHANNEL_TYPE.INSTAGRAM);
		event.setFrom(id);
		event.contact().setCsid(id);
		if (ArgUtil.is(m.getPostBack()) && ArgUtil.is(m.getPostBack().getTitle())) {
			event.setMessage(m.getPostBack().getTitle());
		} else {
			event.setMessage(m.getMessage().getText());
		}

		event.to().add(m.getRecipient().get("id"));
		event.contact().type(ContactType.INSTAGRAM);
		event.contact().setLane(lane);
		// System.out.println("toInboxMessage-------" + JsonUtil.toJson(m));
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

		// System.out.println("toInboxMessage======" + JsonUtil.toJson(inboxMessage));

		/**
		 * https://developers.facebook.com/docs/messenger-platform/instagram/features/webhook
		 */
		if (ArgUtil.is(m.getMessage())) {
			if (ArgUtil.is(m.getMessage().getAttachments())
					&& ArgUtil.is(m.getMessage().getAttachments()[0].getPayload())) {
				if (ArgUtil.is(m.getMessage().getAttachments()[0].getPayload().getUrl())) {
					FacbookAttachment attchment = m.getMessage().getAttachments()[0];
					FileType attachmentType = ArgUtil.parseAsEnumT(attchment.getType(), FileType.class);
					if (ArgUtil.is(attachmentType)) {
						inboxMessage.setFormatType(attachmentType.toString().toLowerCase());
						inboxMessage.attachment(new Attachment().mediaURL(attchment.getPayload().getUrl())
								.mediaType(attachmentType).mediaSrc(attchment.getPayload().getUrl()));
					} else if (ArgUtil.isEqual(attchment.getType(), "story_mention", "share")) {
						inboxMessage.attachment(new Attachment().mediaURL(attchment.getPayload().getUrl())
								.mediaCaption(attchment.getPayload().getTitle()).mediaType(m)
								.mediaSrc(attchment.getPayload().getUrl()).mediaType(FileType.URL)
								.mediaSubType(attchment.getType()));
					}
				}
			}
			// Extract Message Details
			inboxMessage.setMessageIdExt(m.getMessage().getMid());
			inboxMessage.setMessage(m.getMessage().getText());

			MapModel qr = m.getMessage().getQuickReply();
			if (ArgUtil.is(qr)) {
				inboxMessage.form().put("reply_id", qr.getString("payload"));
				inboxMessage.form().put("reply_title", m.getMessage().getText());
			}

			MapModel rt = m.getMessage().getReplyTo();
			if (ArgUtil.is(rt)) {
				inboxMessage.setReplyIdExt(rt.getString("mid"));
				if (rt.containsKey("story")) {
					inboxMessage.replyTo().put("type", "story");
					inboxMessage.replyTo().put("id", rt.entry(InBoundWrapperPaths.STORY_ID).asString());
					inboxMessage.replyTo().put("url", rt.entry(InBoundWrapperPaths.STORY_URL).asString());
				}
			}
		} else if (ArgUtil.is(m.getPostBack()) && ArgUtil.is(m.getPostBack().getTitle())) {
			inboxMessage.setMessageIdExt(m.getPostBack().getMid());
			inboxMessage.setMessage(m.getPostBack().getTitle());
			inboxMessage.form().put("reply_id", m.getPostBack().getPayload());
			inboxMessage.form().put("reply_title", m.getPostBack().getTitle());
		}

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
		} else if (ArgUtil.is(m.getMessage().isIs_deleted())) {
			report.setMessageIdExt(m.getMessage().getMid());
			report.setChangeStamp(m.getTimestamp());
			report.setStatus(Status.DELTD);
		}

		// System.out.println("toMessageReport======" + JsonUtil.toJson(report));

		return report;
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		FacebookHookRequest request = requestMap.as(FacebookHookRequest.class);
		requestMap.toJson();
		request.getEntry().forEach(pageEntry -> {
			ChannelConfig channelConfigDefault = channelConfig;
			String pageId = pageEntry.getId();
			if (!ArgUtil.is(channelConfig.getLane(), pageId)) {
				channelConfigDefault = getChannelConfig(channelConfig.getChannelType(), pageId);
			}
			if (ArgUtil.is(channelConfigDefault)) {
				inboundMessageBoxEvent(messageBoxEvent, pageEntry, channelConfigDefault);
			}
		});
		return messageBoxEvent;
	}

	private void inboundMessageBoxEvent(MessageBoxEvent messageBoxEvent, FacebookEntry pageEntry,
			final ChannelConfig channelConfig) {
		pageEntry.getMessaging().forEach(m -> {
			if ((ArgUtil.is(m.getMessage()) && (m.getMessage().isIs_deleted())) // Message is deleted
					|| ArgUtil.is(m.getRead()) // or Message is Read
			) {
				messageBoxEvent.addMessageReport(toMessageReport(m, channelConfig));
			} else if (ArgUtil.is(m.getMessage()) || ArgUtil.is(m.getPostBack())) {
				InboxMessage inboxMessage = toInboxMessage(m, channelConfig);
				if (!ArgUtil.areEqual(channelConfig.getLane(), inboxMessage.contact().getCsid())) {
					messageBoxEvent.addInboxMessage(inboxMessage);
				}
			}
		});
	}
}
