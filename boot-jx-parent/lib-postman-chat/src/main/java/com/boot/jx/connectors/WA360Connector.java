package com.boot.jx.connectors;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.postman.wa360.WA360Constants.InBoundWrapperPaths;
import com.boot.jx.postman.wa360.WA360InboundMedia;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = CHANNEL_TYPE.WA_360D)
public class WA360Connector implements ConnectorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WA360Connector.class);
    @Autowired
    private RestService restService;

    @Autowired
    private PMFileStoreClient pmFileStoreClient;

    @Autowired
    private TmplClient tmplClient;

    @Autowired
    private CommonMongoTemplate commonMongoTemplate;

    @Autowired
    private WA360Client wa360Client;

    @Autowired
    PMClientConfig pmClientConfig;

    @Override
    public void registerWebHook(ChannelConfig channelConfig) {
	String webhookUrl = pmClientConfig.getWebhookUrl(channelConfig);
	restService.ajax(WA360Constants.BASE_URL).path("v1/configs/webhook")
		.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey())
		.post(MapModel.createInstance().put("url", webhookUrl).toMap()).asMap();
    }

    public InboxMessage toInboxMessage(ChannelConfig channelConfig, MapModel map) {

	InboxMessage inboxMessage = this.createInboxMessage(channelConfig);

	String contactNumber = map.entry(InBoundWrapperPaths.CONTACT_NUMBER).asString();
	String contactName = map.entry(InBoundWrapperPaths.CONTACT_NAME).asString();

	inboxMessage.contact().setCsid(contactNumber);
	inboxMessage.contact().setName(contactName);
	inboxMessage.contact().setPhone(contactNumber);

	inboxMessage.setFrom(contactNumber);
	inboxMessage.setFromName(contactName);
	inboxMessage.to().add(channelConfig.getLane());
	inboxMessage.setMessageIdExt(map.entry(InBoundWrapperPaths.MESSAGE_ID).asString());

	String messageType = map.entry(InBoundWrapperPaths.MESSAGE_TYPE).asString();

	if ("text".equals(messageType)) {
	    inboxMessage.setMessage(map.entry(InBoundWrapperPaths.MESSAGE_TEXT).asString());
	} else if ("interactive".equals(messageType)) {
	    String interactiveType = map.entry(InBoundWrapperPaths.INTERACTIVE_TYPE).asString();
	    if ("button_reply".equals(interactiveType)) {
		inboxMessage.form().put("reply_id", map.entry(InBoundWrapperPaths.INTERACTIVE_BUTTON_ID).asString());
		inboxMessage.form().put("reply_title",
			map.entry(InBoundWrapperPaths.INTERACTIVE_BUTTON_REPLY).asString());
		inboxMessage.setMessage(ArgUtil.parseAsString(inboxMessage.form().get("reply_title"), Constants.BLANK));
	    } else if ("list_reply".equals(interactiveType)) {
		inboxMessage.form().put("reply_id", map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_ID).asString());
		inboxMessage.form().put("reply_title",
			map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_REPLY).asString());
		inboxMessage.form().put("reply_desc", map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_DESC).asString());
	    }
	    inboxMessage.setMessage(ArgUtil.parseAsString(inboxMessage.form().get("reply_title"), Constants.BLANK));
	} else if ("image".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.IMAGE, FileType.IMAGE);
	} else if ("document".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.DOCUMENT, FileType.DOCUMENT);
	} else if ("audio".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.AUDIO, FileType.AUDIO);
	} else if ("voice".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.VOICE, FileType.AUDIO);
	} else if ("video".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.VIDEO, FileType.VIDEO);
	} else if ("sticker".equals(messageType)) {
	    formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.STICKER, FileType.IMAGE);
	}

	return inboxMessage;
    }

    private void formatMedia(InboxMessage inboxMessage, MapModel map, ChannelConfig channelConfig, JsonPath path,
	    FileType fileType) {
	WA360InboundMedia media = map.entry(path).as(WA360InboundMedia.class);
	CommonFile srcFile = new CommonFile().url(WA360Constants.MEDIA_URL(media.getId())).fileType(fileType)
		.format(FileFormat.from(media.getMimeType()))
		.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey());

	CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
		PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());

	inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
		.mediaSrc(srcFile.getUrl()).mediaCaption(media.getCaption()).mediaName(media.getFilename()));
    }

    private OutboxMessage resolveTemplate(OutboxMessage outboxMessage) {
	if (ArgUtil.is(outboxMessage.getTemplate())) {
	    QuickMedia templateReply = commonMongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
	    if (ArgUtil.is(templateReply)) {
		if ("image".equalsIgnoreCase(templateReply.getType())) {
		    outboxMessage.attachment(new Attachment().mediaURL(templateReply.getUrl())
			    .mediaType(FileType.IMAGE.toString()).mediaCaption(templateReply.getTitle()));
		    return outboxMessage;
		}
	    } else {
		tmplClient.process(outboxMessage);
		return outboxMessage;
	    }
	} else if (ArgUtil.is(outboxMessage.getTemplateId())) {
	    // outboxMessage.setMessage(tmplClient.process(hsmTemplate.getTemplate(),
	    // outboxMessage.getModel()));
	    tmplClient.process(outboxMessage);
	    return outboxMessage;
	} else {
	    return outboxMessage;
	}
	return outboxMessage;
    }

    @Override
    public void send(OutboxMessage outboxMessage) {
	try {
	    resolveTemplate(outboxMessage);
	    wa360Client.send(outboxMessage);
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT);
	} catch (Exception e) {
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
	    outboxMessage.logs().add(e.getMessage());
	    LOGGER.error("SEND ERROR", e);
	}
    }

    @Override
    public List<InboxMessage> extractInboxMessages(ChannelConfig channelConfig, MapModel map) {
	return CollectionUtil.asList(toInboxMessage(channelConfig, map));
    }

}
