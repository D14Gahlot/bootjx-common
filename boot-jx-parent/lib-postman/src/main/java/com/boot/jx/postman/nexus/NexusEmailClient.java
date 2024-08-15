package com.boot.jx.postman.nexus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpServerException;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.channel.ChannelClientFactory.ChannelClient;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ConnectorMapping;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;

@Component
@ConnectorMapping(contactType = ContactType.EMAIL, channel = CHANNEL_TYPE.OUTLOOK)
public class NexusEmailClient implements ChannelClient {

	@Autowired
	private RestService restService;

	@Value("${mry.nexus.url}")
	private String nexusUrl;

	@Async
	@Retryable(value = ApiHttpServerException.class, maxAttempts = 3, backoff = @Backoff(delay = 3000))
	public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
		restService.ajax(nexusUrl).path("/email/api/v1/outlook/" + channelConfig.getChannelId() + "/message/send")
				.postJson(MapModel.createInstance().put("refId", outboxMessage.getMessageId())
						.put("messageId", outboxMessage.getMessageId())
						.put("messageIdRef", outboxMessage.getMessageIdRef())
						.put("messageIdExt", outboxMessage.getMessageIdExt()).put("id", outboxMessage.getId()))
				.asNone();
		return outboxMessage;
	}

	@Override
	public MapModel updateTemplates(ChannelConfig channelConfig, MapModel from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapModel createTemplates(ChannelConfig channelConfig, MapModel from) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapModel fetchTemplates(ChannelConfig channelConfig) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MapModel listOfFlows(ChannelConfig channelConfig) {
		// TODO Auto-generated method stub
		return null;
	}

}
