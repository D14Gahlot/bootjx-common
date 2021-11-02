package com.boot.jx.connectors;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.WA360Plugin;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360ConfigDetails;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.postman.wa360.WA360Constants.InBoundWrapperPaths;
import com.boot.jx.postman.wa360.WA360InboundMedia;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = CHANNEL_TYPE.WA_360D)
public class WA360Connector extends AbstractConnector<WA360ConfigDetails, WA360Plugin> {

    @Override
    public WA360Plugin getPlugin() {
	return ChannelPluginProvider.WA_360D;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WA360Connector.class);
    @Autowired
    private RestService restService;

    @Autowired
    private PMFileStoreClient pmFileStoreClient;

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

	// Create Default Message from Channel
	InboxMessage inboxMessage = this.createInboxMessage(channelConfig);

	// Set Contact info
	String contactNumber = map.entry(InBoundWrapperPaths.CONTACT_NUMBER).asString();
	String contactName = map.entry(InBoundWrapperPaths.CONTACT_NAME).asString();

	inboxMessage.contact().setCsid(contactNumber);
	inboxMessage.contact().setName(contactName);
	inboxMessage.contact().setPhone(contactNumber);

	// Set Additional info
	inboxMessage.setFrom(contactNumber);
	inboxMessage.setFromName(contactName);
	inboxMessage.to().add(channelConfig.getLane());

	// Extract Message Details
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
	} else if ("button".equals(messageType)) {
	    inboxMessage.form().put("reply_title", map.entry(InBoundWrapperPaths.SIMPLE_BUTTON_REPLY).asString());
	    inboxMessage.form().put("reply_payload", map.entry(InBoundWrapperPaths.SIMPLE_BUTTON_PAYLOAD).asString());

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

	String replyIdExt = map.entry(InBoundWrapperPaths.CONTEXT_ID).asString();
	if (ArgUtil.is(replyIdExt)) {
	    inboxMessage.setReplyIdExt(replyIdExt);
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

    @Override
    public void send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	try {
	    template(channelConfig, outboxMessage);
	    wa360Client.send(outboxMessage);
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT);
	} catch (Exception e) {
	    outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
	    outboxMessage.logs().add(e.getMessage());
	    LOGGER.error("SEND ERROR", e);
	}
    }

    private MessageReport toMessageReport(ChannelConfig channelConfig, MapModel requestMap) {
	MessageReport report = this.createMessageReport(channelConfig);
	String csid = requestMap.getString("recipient_id");
	report.contact().setCsid(csid);
	report.setChangeStamp(requestMap.getLong("timestamp", 0L) * 1000);
	report.setMessageIdExt(requestMap.getString("id"));

	String status = requestMap.getString("status");

	if ("sent".equals(status)) {
	    report.setStatus(Status.SENTX);
	} else if ("delivered".equals(status)) {
	    report.setStatus(Status.DLVRD);
	} else if ("read".equals(status)) {
	    report.setStatus(Status.READ);
	} else if ("deleted".equals(status)) {
	    report.setStatus(Status.DELTD);
	} else if ("failed".equals(status)) {
	    report.setStatus(Status.FAILD);
	    String errorCode = requestMap.pathEntry("errors/[0]/code").asString();
	    report.setReason("Code:" + errorCode);
	}

	return report;
    }

    @Override
    public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
	    MessageBoxEvent messageBoxEvent) {

	if (requestMap.containsKey("messages")) {
	    messageBoxEvent.addInboxMessage(toInboxMessage(channelConfig, requestMap));
	}

	if (requestMap.containsKey("statuses")) {
	    List<Map<String, Object>> statusMaps = requestMap.keyEntry("statuses").asListOfMap();
	    for (Map<String, Object> statusMap : statusMaps) {
		messageBoxEvent.addMessageReport(toMessageReport(channelConfig, MapModel.from(statusMap)));
	    }
	}

	return messageBoxEvent;
    }

}
