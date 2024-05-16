package com.boot.jx.connectors;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.config.ChannelConfigTempDoc;
import com.boot.jx.postman.fb.FacbookAttachment;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookEntry;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.fb.FacebookMessaging;
import com.boot.jx.postman.fb.FacebookUserProfile;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ConnectorMapping;
import com.boot.jx.postman.plugin.FacebookPlugin;
import com.boot.jx.postman.plugin.FacebookPlugin.FacebookConfigDetails;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
@ConnectorMapping(contactType = ContactType.FACEBOOK)
public class FacebookConnector extends AbstractConnector<FacebookConfigDetails, FacebookPlugin> {
	private static final Logger LOGGER = LoggerFactory.getLogger(FacebookConnector.class);

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private ChatLogger logManager;

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Autowired
	private RestService restService;

	public List<ChannelConfig> onRegister(ChannelConfig setup, ChannelConfigTempDoc channelConfigTemp) {
		List<ChannelConfig> channels = new ArrayList<ChannelConfig>();
		try {

			MapModel resp = MapModel.from(channelConfigTemp.getResp());
			String redirectUri = resp.pathEntry("_.redirect_uri").asString();

			MapModel accessToken = restService.ajax("https://graph.facebook.com/v18.0").path("/oauth/access_token")
					.field("client_id", setup.getFacebook().getMasterAppId())
					.field("client_secret", setup.getFacebook().getMasterAppSecret())
					// .field("redirect_uri", redirectUri)
					.field("code", resp.pathEntry("authResponse.code").asString())
					//
					.submit().asMapModel();
			channelConfigTemp.log("oauth/access_token", accessToken.toMap());

//			MapModel accessToken2 = restService.ajax("https://graph.facebook.com/v18.0").path("/oauth/access_token")
//					.field("client_id", setup.getFacebook().getMasterAppId())
//					.field("client_secret", setup.getFacebook().getMasterAppSecret())
//					.field("fb_exchange_token", accessToken.keyEntry("access_token").asString())
//					.field("grant_type", "fb_exchange_token")
//					.submit().asMapModel();
//			channelConfigTemp.log("oauth/access_token2", accessToken2.toMap());

			MapModel me = restService.ajax("https://graph.facebook.com/v18.0").path("/me/accounts")
					.queryParam("access_token", accessToken.keyEntry("access_token").asString())
					// .queryParam("fields", "id,name,access_token,link")
					.get().asMapModel();
			channelConfigTemp.log("me/accounts", me.toMap());

			me.keyEntry("data").asListOfMap().forEach(page -> {
				MapModel pagemap = MapModel.from(page);
				ChannelConfig channel = new ChannelConfig();
				channel.setFacebook(new FacebookConfigDetails());
				channel.getFacebook().setAccessToken(pagemap.keyEntry("access_token").asString());
				channel.getFacebook().setPageId(pagemap.keyEntry("id").asString());
				channel.getFacebook().setHandler(channel.getFacebook().getPageId());
				channel.getFacebook().setType("page");
				channel.getFacebook().setMasterAppId(setup.getFacebook().getMasterAppId());
				channel.setName(pagemap.keyEntry("name").asString());
				channels.add(channel);
			});
		} catch (ApiHttpException e) {
			channelConfigTemp.log("exception", MapModel.from(e.getResponse().getBody()).toMap());
		}
		commonMongoTemplate.save(channelConfigTemp);
		return channels;
	}

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig) {
		restService.ajax("https://graph.facebook.com/v18.0/").path(channelConfig.getFacebook().getPageId())
				.path("/subscribed_apps").field("access_token", channelConfig.getFacebook().getAccessToken())
				.field("subscribed_fields",
						"message_deliveries, message_echoes, message_reads, messages, messaging_optins, messaging_postbacks")
				.submit().asMap();
		// channelConfigTemp.log("me/accounts", subscriptions.toMap());
		// ApiResponseUtil.addWarning("Set webhook URL manually from Facebook Developer
		// Portal.");
	}

	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		template(channelConfig, chatContactDoc, outboxMessage);
		facebooClient.send(channelConfig, outboxMessage);
		outboxMessage.updateStatus(Message.Status.SENT);
	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		this.reply(null, null, new OutboxMessage().message("Our agent will get in touch with you"), inboxMessage);
		return inboxMessage;
	}

	@Override
	public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		ChannelConfig config = getChannelConfig(inboxMessage);
		try {
			FacebookUserProfile profile = facebooClient.getUserProfile(config, inboxMessage.contact());
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
		event.contact().setChannelType(CHANNEL_TYPE.FACEBOOK);
		event.setFrom(id);
		event.contact().setCsid(id);
		if (ArgUtil.is(m.getPostBack()) && ArgUtil.is(m.getPostBack().getTitle())) {
			event.setMessage(m.getPostBack().getTitle());
		} else {
			event.setMessage(m.getMessage().getText());
		}
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

		/**
		 * https://developers.facebook.com/docs/messenger-platform/reference/webhook-events/messages
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
					} else if ("fallback".equals(attchment.getType())) {
						inboxMessage.attachment(new Attachment().mediaURL(attchment.getPayload().getUrl())
								.mediaCaption(attchment.getPayload().getTitle())
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
		return report;
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		FacebookHookRequest request = requestMap.as(FacebookHookRequest.class);
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
