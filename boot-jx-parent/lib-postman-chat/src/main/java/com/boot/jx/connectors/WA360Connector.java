package com.boot.jx.connectors;

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
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.postman.wa360.WA360Constants.InBoundWrapperPaths;
import com.boot.jx.postman.wa360.WA360InboundMedia;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.JsonPath;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = CHANNEL_TYPE.WA_360D)
public class WA360Connector implements ConnectorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(WA360Connector.class);
    @Autowired
    private RestService restService;

    @Autowired
    private PMEnvironment environment;

    @Autowired
    private PMFileStoreClient pmFileStoreClient;

    @Override
    public void registerWebHook(ChannelConfig channelConfig) {
	PMConfiguration config = environment.config();
	String webhookUrl = String.format("%s/ext/inbound/wa360/callback/%s/%s/%s",
		channelConfig.getWa360d().getWebhookUrl(), config.getAccountKey(), channelConfig.getChannelId(),
		channelConfig.getChannelKey());
	restService.ajax(WA360Constants.BASE_URL).path("v1/configs/webhook")
		.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey())
		.post(MapModel.createInstance().put("url", webhookUrl).toMap()).asMap();
    }

    @Override
    public InboxMessage assignToAgent(InboxMessage inboxMessage) {
	return null;
    }

    @Override
    public void send(OutboxMessage outboxMessage) {

    }

    public InboxMessage toInboxMessage(String channelId, MapModel map) {

	PMConfiguration config = environment.config();
	ChannelConfig channelConfig = config.channels(channelId);

	String contactNumber = map.entry(InBoundWrapperPaths.CONTACT_NUMBER).asString();
	String contactName = map.entry(InBoundWrapperPaths.CONTACT_NAME).asString();

	InboxMessage inboxMessage = new InboxMessage();
	inboxMessage.contact().type(channelConfig.getContactType());
	inboxMessage.contact().setChannel(channelConfig.getChannelType());
	inboxMessage.contact().setLane(channelConfig.getLane());
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

}
