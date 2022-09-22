package com.boot.jx.connectors;

import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;

import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.MESSAGE_SEND_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessagePrompt;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

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

	public void registerWebhook(ChannelConfig channelConfig) {
		String webhookUrl = pmClientConfig.getWebhookUrl(channelConfig);
		this.registerWebhook(channelConfig, webhookUrl);
	}

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig) {
		// Register Webhook URL
		this.registerWebhook(channelConfig);
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

	private OutboxMessage process(ChannelConfig channelConfig, ChatContactDoc chatContactDoc,
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
			List<HSMTemplate3rdParty> temps = null;

			if (ArgUtil.is(outboxMessage.hsm().getLinked())) {
				temps = commonMongoTemplate.find(CommonMongoQueryBuilder.collection(HSMTemplate3rdParty.class)
						.where(Criteria.where("hsmTemplateId").is(outboxMessage.templateId()).and("channelId")
								.is(channelConfig.getChannelId()).and("code").is(outboxMessage.hsm().getLinked())));
			} else if (MESSAGE_SEND_TYPE.PUSH_MESSAGE.equals(outboxMessage.messageMetaWrapper().sendType())
					&& channelConfig.isPushAllowed() && channelConfig.isPushOnlyApproved()) {
				temps = commonMongoTemplate.find(CommonMongoQueryBuilder.collection(HSMTemplate3rdParty.class)
						.where(Criteria.where("hsmTemplateId").is(outboxMessage.templateId()).and("channelId")
								.is(channelConfig.getChannelId())));
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
				outboxMessage.setTemplateExt(resolvedTemplate);
				return outboxMessage;
			}
		}
		return outboxMessage;
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

}