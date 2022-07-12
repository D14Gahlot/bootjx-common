package com.boot.jx.connectors;

import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.others.TwilioClient;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.TwilioSMSPlugin;
import com.boot.jx.postman.plugin.TwilioSMSPlugin.TwilioConfigDetails;
import com.boot.model.MapModel;

@Component
@ConnectorMapping(contactType = ContactType.SMS, channel = CHANNEL_TYPE.SMS_TWILIO)
public class TwilioSMSConnector extends AbstractConnector<TwilioConfigDetails, TwilioSMSPlugin> {

	private static Logger LOGGER = LoggerService.getLogger(TwilioSMSConnector.class);

	@Autowired
	private TwilioClient twilioClient;

	@Override
	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
			template(channelConfig, chatContactDoc, outboxMessage); // TODO:- This is common for all connector, make it
			// generic
			twilioClient.sendSMS(channelConfig, outboxMessage);
			outboxMessage.updateStatus(OutboxMessage.Status.SENT);
		} catch (AmxApiException e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(((AmxApiException) e).getErrorKey());
		}
	}

	@Override
	public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		return null;
	}

	public InboxMessage toInboxMessage(ChannelConfig channelConfig, Map<String, Object> dataMap) {
		InboxMessage inboxMessage = this.createInboxMessage(channelConfig);
		return inboxMessage;
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		return messageBoxEvent.addInboxMessage(toInboxMessage(channelConfig, requestMap.toMap()));
	}

}
