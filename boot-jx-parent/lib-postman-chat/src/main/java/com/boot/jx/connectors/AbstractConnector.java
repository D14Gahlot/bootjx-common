package com.boot.jx.connectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;

import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.model.CommonFileStream;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.MESSAGE_SEND_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.channel.ChannelClientFactory;
import com.boot.jx.postman.channel.ChannelClientFactory.ChannelClient;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.config.ChannelConfigLogger;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessagePrompt;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.ContactStore;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.Urly;

public abstract class AbstractConnector<CD extends AChannelDetails, P extends ChannelPlugin<CD>>
		implements ConnectorHandler {

	public static Logger LOGGER = LoggerService.getLogger(AbstractConnector.class);

	public static abstract class DefaultConnector<CD extends AChannelDetails, P extends ChannelPlugin<CD>>
			extends AbstractConnector<CD, P> {
	}

	@Autowired
	protected MessageContext messageContext;

	@Autowired
	protected PMClientConfig pmClientConfig;

	@Autowired
	protected PMEnvironment environment;

	@Autowired
	protected CommonMongoTemplate commonMongoTemplate;

	@Autowired
	protected TmplClient tmplClient;

	@Autowired
	protected ChatLogger logManager;

	@Autowired
	protected ContactStore contactStore;

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Autowired
	protected ChannelClientFactory clientFactory;

	@Override
	public ChannelClient getClient(ChannelConfig channelConfig) {
		return this.clientFactory.get(channelConfig);
	}

	@Override
	public void onException(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage,
			Exception e) {
		try {
			if (e instanceof AmxApiException) {
				String errorCode = ((AmxApiException) e).getErrorKey();
				outboxMessage.updateStatus(Message.Status.SENT_ERR);
				outboxMessage.logs().add(errorCode);
				if (ApiStatusCodes.API_ERROR.toString().equalsIgnoreCase(errorCode)) {
					logManager.error(outboxMessage, e);
				}
			} else {
				outboxMessage.updateStatus(Message.Status.SENT_EXC);
				logManager.error(outboxMessage, e);
			}
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		} catch (Exception ex) {
			LOGGER.error("SEND ERROR LOG EXCEPTION", ex);
		}

	}

	public void registerWebhook(ChannelConfig channelConfig, String webhookUrl) {
		ConnectorHandlerFactory.LOGGER.error("WEBHOOK REGISTRATION NOT DEFINED for URL");
	}

	public void registerWebhook(ChannelConfig channelConfig, ChannelConfigLogger channelConfigLogger) {
		String webhookUrl = pmClientConfig.getWebhookUrl(channelConfig, null, null);
		this.registerWebhook(channelConfig, webhookUrl);
	}

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig, ChannelConfigLogger channelConfigLogger) {
		// Register Webhook URL
		this.registerWebhook(channelConfig, channelConfigLogger);
	}

	public ChannelConfig getChannelConfig(String channleType, String lane) {
		String channelId = PostManUtil.CHANNEL_ID(channleType, lane);
		return environment.config().channel(channelId);
	}

	@Override
	public ChannelConfig getChannelConfig(IMessage iMessage) {
		String channelId = PostManUtil.CHANNEL_ID(iMessage.contact());
		ChannelConfig channelConfig = environment.config().channel(channelId);
		if (!ArgUtil.is(channelConfig) && !ContactType.WEBSITE.equals(iMessage.contact().type())) {
			ConnectorHandlerFactory.LOGGER.error(String.format("ChannelConfig not found for %s", channelId));
		}
		return channelConfig;
	}

	public MessageContext context() {
		return messageContext;
	}

	@Override
	public ChatContactDoc getChatContact(IMessage iMessage) {
		return messageContext.contact().getDoc();
	}

	protected CustomerProfileDoc findProfile(ChatContactDoc chatContactDoc) {
		return null;
	}

	@Override
	public void linkProfile(ChatSessionDoc session, InboxMessage inboxMessage) {
		try {
			ChatContactQuery contactQuery = context().contact();
			ChatContactDoc chatContactDoc = contactQuery.getDoc();
			if (!ArgUtil.is(chatContactDoc.profile().getId())) {
				CustomerProfileDoc profile = findProfile(chatContactDoc);
				if (profile != null) {
					contactStore.linkProfile(contactQuery, profile);
				}
			}
		} catch (Exception e) {
			logManager.error(e);
		}
	}

	@Override
	public void beforeSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		template(channelConfig, chatContactDoc, outboxMessage);
	}

	@Override
	public OutboxMessage template(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
			OutboxMessage outboxMessage) {
//	if (ArgUtil.is(outboxMessage.getMedia())) {
//	    QuickMedia templateReply = commonMongoTemplate.findById(outboxMessage.getTemplate().getMedia(),
//		    QuickMedia.class);
//	    if (ArgUtil.is(templateReply)) {
//		if ("image".equalsIgnoreCase(templateReply.getType())) {
//		    outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
//			    .mediaType(FileType.IMAGE.toString()).mediaCaption(templateReply.getTitle()));
//		    return outboxMessage;
//		}
//	    } else {
//		process(channelConfig, outboxMessage);
//		return outboxMessage;
//	    }
//	} else

		if (ArgUtil.is(outboxMessage.templateId()) || ArgUtil.is(outboxMessage.templateCode())) {
			// outboxMessage.setMessage(tmplClient.process(hsmTemplate.getTemplate(),
			// outboxMessage.getModel()));
			process(channelConfig, chatContactDoc, outboxMessage);
			return outboxMessage;
		} else {
			return outboxMessage;
		}
	}

	public OutboxMessage process(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
			OutboxMessage outboxMessage) {

		outboxMessage.model().put("contact", ChatDTOUtil.getContactMeta(chatContactDoc));
		outboxMessage.model().put("global", environment.local().globalVars().toObject());

		// Model Data Merge
		MapModel model = MapModel.from(outboxMessage.getModel());
		MapModel data = MapModel.createInstance();
		data.putAll(model.keyEntry(Message.DATA_KEY).asMap());
		data.putAll(outboxMessage.hsm().data());
		model.put(Message.DATA_KEY, data.toMap());
		outboxMessage.setModel(JsonUtil.deepCopy(model.toMap()));

		if (ArgUtil.isEmpty(outboxMessage.hsm().getLang())) {
			outboxMessage.hsm().lang(chatContactDoc.prefs().getLang());
		}

		tmplClient.process(outboxMessage);
		if (ArgUtil.is(outboxMessage.templateId())) {
			HSMTemplate3rdParty tpTemplate = templateExt(channelConfig, chatContactDoc, outboxMessage);
			if (ArgUtil.is(tpTemplate)) {
				outboxMessage.setTemplateExt(tpTemplate);
			}
		}
		return outboxMessage;
	}

	public HSMTemplate3rdParty templateExt(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
			OutboxMessage outboxMessage) {
		List<HSMTemplate3rdParty> temps = null;
		if (ArgUtil.is(outboxMessage.hsm().getLinked())) {
			temps = commonMongoTemplate.find(CommonMongoQueryBuilder.collection(HSMTemplate3rdParty.class)
					.where(Criteria.where("hsmTemplateId").is(outboxMessage.templateId()).and("channelId")
							.is(channelConfig.getChannelId()).and("code").is(outboxMessage.hsm().getLinked())));
		} else if (outboxMessage.messageMetaWrapper().isTemplateExt()
				|| (MESSAGE_SEND_TYPE.PUSH_MESSAGE.equals(outboxMessage.messageMetaWrapper().sendType())
						&& channelConfig.isPushAllowed() && channelConfig.isPushOnlyApproved())) {
			temps = commonMongoTemplate.find(
					CommonMongoQueryBuilder.collection(HSMTemplate3rdParty.class).where(Criteria.where("hsmTemplateId")
							.is(outboxMessage.templateId()).and("channelId").is(channelConfig.getChannelId())));
			LOGGER.debug(JsonUtil.toJson(temps));
		}

		if (ArgUtil.is(temps)) {
			HSMTemplate3rdParty resolvedTemplate = null;
			if (temps.size() > 1) {
				for (HSMTemplate3rdParty hsmTemplate3rdParty : temps) {
					if (ArgUtil.areEqual(hsmTemplate3rdParty.getLang(), outboxMessage.hsm().getLang())) {
						resolvedTemplate = hsmTemplate3rdParty;
						break;
					} else if (ArgUtil.is(hsmTemplate3rdParty.getLang())) {
						resolvedTemplate = hsmTemplate3rdParty;
					}
				}
			} else {
				resolvedTemplate = temps.get(0);
			}
			return resolvedTemplate;
		}
		return null;
	}

	@Override
	public boolean optin(ChannelConfig channelConfig, ChatContactDoc chatContactDoc) {
		return ArgUtil.is(chatContactDoc.getCsid());
	}

	@Override
	public void prompt(InboxMessage inboxMessage) {

		if (!ArgUtil.is(inboxMessage.form())) {
			return;
		}

		String replyId = ArgUtil.parseAsString(inboxMessage.form().get("reply_id"));

		if (ArgUtil.is(replyId)) {
			if (replyId.startsWith("#")) {
				String[] params = replyId.split("#");
				if (params.length == 4) {
					if (MessagePrompt.TYPE.MOREOPTIONS.equals(params[1])) {
						MessagePrompt prompt = new MessagePrompt();
						prompt.type = params[1];
						prompt.pageIndex = ArgUtil.parseAsInteger(params[2]);
						prompt.messageId = params[3];
						inboxMessage.setPrompt(prompt);
					}
				}
			}
		}
	}

	@Override
	public void reloadMedia(ChannelConfig channelConfig, MessageDoc msg) throws FileNotFoundException, IOException {
		List<Attachment> attach = msg.getAttachments();
		for (Attachment attachment : attach) {
			if (ArgUtil.is(attach) && attach.size() > 0) {
				reloadMedia(channelConfig, msg, attachment);
			}
		}
	}

	@Override
	public CommonFile reloadMedia(ChannelConfig channelConfig, MessageDoc msg, Integer index)
			throws FileNotFoundException, IOException {
		List<Attachment> attach = msg.getAttachments();
		if (ArgUtil.is(attach) && attach.size() > 0) {
			Attachment attachment = attach.get(index);
			return reloadMedia(channelConfig, msg, attachment);
		}
		return null;
	}

	public CommonFile reloadMedia(ChannelConfig channelConfig, MessageDoc msg, Attachment attachment)
			throws MalformedURLException, FileNotFoundException, IOException {
		CommonFileStream srcFile = new CommonFileStream().url(attachment.getMediaSrc())
				// .fileType(attachment.getMediaType())
				.format(FileFormat.from(attachment.getMediaMimeType()))
				// .header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey())
				.name(ArgUtil.nonEmpty(attachment.getMediaName(), attachment.getMediaCaption()));

		File fileb = Urly.parse(attachment.getMediaURL()).toFile();

		CommonFile dstFile = new CommonFile().url(attachment.getMediaURL()).path(fileb.getParent())
				.fileType(ArgUtil.parseAsEnumT(attachment.getMediaType(), FileType.class));
		return pmFileStoreClient.commitSessionFileSync(srcFile, dstFile);
	}

}
