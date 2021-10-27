package com.boot.jx.connectors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConstants.MESSAGE_SEND_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

public abstract class AbstractConnector<CD extends AChannelDetails, P extends ChannelPlugin<CD>>
	implements ConnectorHandler {

    abstract public P getPlugin();

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

    @Override
    public void registerWebHook(ChannelConfig channelConfig) {
	String webhookUrl = pmClientConfig.getWebhookUrl(channelConfig);
	registerWebHook(channelConfig, webhookUrl);
    }

    public void registerWebHook(ChannelConfig channelConfig, String webhookUrl) {
	ConnectorHandlerFactory.LOGGER.error("WEBHOOK REGISTRATION NOT DEFINED for URL");
    }

    @Override
    public ChannelConfig getChannelConfig(IMessage iMessage) {
	String channelId = PostManUtil.CHANNEL_ID(iMessage.contact());
	ChannelConfig channelConfig = environment.config().channels(channelId);
	if (!ArgUtil.is(channelConfig) && !ContactType.WEBSITE.equals(iMessage.contact().type())) {
	    ConnectorHandlerFactory.LOGGER.error(String.format("ChannelConfig not found for %s", channelId));
	}
	return channelConfig;
    }

    @Override
    public OutboxMessage template(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	if (ArgUtil.is(outboxMessage.getTemplate())) {
	    QuickMedia templateReply = commonMongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
	    if (ArgUtil.is(templateReply)) {
		if ("image".equalsIgnoreCase(templateReply.getType())) {
		    outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
			    .mediaType(FileType.IMAGE.toString()).mediaCaption(templateReply.getTitle()));
		    return outboxMessage;
		}
	    } else {
		process(channelConfig, outboxMessage);
		return outboxMessage;
	    }
	} else if (ArgUtil.is(outboxMessage.getTemplateId())) {
	    // outboxMessage.setMessage(tmplClient.process(hsmTemplate.getTemplate(),
	    // outboxMessage.getModel()));
	    process(channelConfig, outboxMessage);
	    return outboxMessage;
	} else {
	    return outboxMessage;
	}
	return outboxMessage;
    }

    private OutboxMessage process(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	if (ArgUtil.is(outboxMessage.getTemplateId())) {
	    if (MESSAGE_SEND_TYPE.PUSH_MESSAGE.equals(outboxMessage.messageMetaWrapper().sendType())
		    && channelConfig.isPushAllowed() && channelConfig.isPushOnlyApproved()) {
		List<HSMTemplate3rdParty> temps = commonMongoTemplate.find(CommonMongoQueryBuilder
			.collection(HSMTemplate3rdParty.class).where("hsmTemplateId", outboxMessage.getTemplateId()));
		if (ArgUtil.is(temps)) {
		    HSMTemplate3rdParty resolvedTemplate = null;
		    if (temps.size() > 1) {
			for (HSMTemplate3rdParty hsmTemplate3rdParty : temps) {
			    if (ArgUtil.areEqual(hsmTemplate3rdParty.getLang(), outboxMessage.getLang())) {
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
	}
	tmplClient.process(outboxMessage);
	return outboxMessage;
    }
}