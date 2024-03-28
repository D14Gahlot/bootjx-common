package com.boot.jx.inbound;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.logger.AuditService;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMAuditEvent;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.tpo.PayloadDumpCollection;
import com.boot.jx.postman.manager.ConfigManager;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

/**
 * 
 * InBoundRouter is responsible for calling connector handler for domain ALREADY
 * decided
 * 
 * @author lalittanwar
 *
 */
@Component
public class InBoundRouter {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundRouter.class);

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired
	private AuditService auditService;

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private ChatStatusService inBoundStatusService;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private RestService restService;

	@Autowired
	private ConfigManager configManager;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	public void inboundMessageEvent(String channelId, Map<String, Object> data) {
		MapModel map = MapModel.from(data);
		PMConfiguration config = pmEnvironment.config();
		ChannelConfig channelConfig = config.channel(channelId);

		if (!ArgUtil.is(channelConfig)) {
			Contactable c = PostManUtil.parseChannelId(channelId);
			channelConfig = configManager.saveChannelConfig(c.getChannelType(), MapModel.createInstance()
					.put("channelId", channelId).put("lane", c.getLane()).put("isAutoCreated", true).toMap());

		}
		ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);

		if (!ArgUtil.is(connector)) {
			LOGGER.error("Channel Not Found for " + channelId);
		}

		try {
			MessageBoxEvent messageBoxEvent = connector.inboundMessageBoxEvent(channelConfig, map,
					new MessageBoxEvent());
			if (ArgUtil.is(messageBoxEvent) && ArgUtil.is(messageBoxEvent.getInboxMessages())) {
				messageBoxEvent.getInboxMessages().forEach(inboxMessage -> {
					connector.prompt(inboxMessage);
					inBoundService.pushMessageToInvokeAsync(inboxMessage);
				});
				connector.onReceiveInboxMessage(messageBoxEvent.getInboxMessages());
			} else if (ArgUtil.is(messageBoxEvent) && ArgUtil.is(messageBoxEvent.getMessageReports())) {
				connector.onMessageReports(messageBoxEvent.getMessageReports());
				inBoundStatusService.update(messageBoxEvent.getMessageReports());
			} else if (ArgUtil.is(channelConfig) && ArgUtil.is(channelConfig.getUnhandledInboundForward())) {
				restService.ajax(channelConfig.getUnhandledInboundForward()).post(data).asNone();
			} else {
				PayloadDumpCollection d = new PayloadDumpCollection();
				d.setType("UNHANDLED_INBOX_EVENT");
				if (ArgUtil.is(channelConfig)) {
					d.setContactType(channelConfig.getContactType());
					d.setChannelType(channelConfig.getChannelType());
					d.setChannelId(channelConfig.getChannelId());
				}
				d.setDump(data);
				commonMongoTemplate.save(d);
			}

		} catch (Exception e) {
			auditService.excep(new PMAuditEvent(PMAuditEvent.Type.INBOUND_ERROR).data(data), LOGGER, e);
		}
	}

	@Async
	public void inboundMessageEventAsync(String channelId, Map<String, Object> data) {
		this.inboundMessageEvent(channelId, data);
	}

	@Async
	public void inboundMessageEventAsync(String domain, String channelId, Map<String, Object> data) {
		AppContextUtil.clear();
		AppContextUtil.setTenant(domain);
		AppContextUtil.init();
		this.inboundMessageEvent(channelId, data);
	}

	public CommonFile reloadMedia(String sessionId, String messageId, Integer index)
			throws FileNotFoundException, IOException {
		ChatSessionDoc session = sessionStore.getSession(sessionId);
		PMConfiguration config = pmEnvironment.config();
		String channelId = PostManUtil.CHANNEL_ID(session.contact());
		ChannelConfig channelConfig = config.channel(channelId);
		ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
		if (!ArgUtil.is(connector)) {
			LOGGER.error("Channel Not Found for " + channelId);
		}
		MessageDoc msg = messageStore.findByMessageId(messageId, session.contact().getContactType());
		return connector.reloadMedia(channelConfig, msg, index);
	}

	public void reloadMedia(String sessionId, String messageId) throws FileNotFoundException, IOException {
		ChatSessionDoc session = sessionStore.getSession(sessionId);
		PMConfiguration config = pmEnvironment.config();
		String channelId = PostManUtil.CHANNEL_ID(session.contact());
		ChannelConfig channelConfig = config.channel(channelId);
		ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
		if (!ArgUtil.is(connector)) {
			LOGGER.error("Channel Not Found for " + channelId);
		}
		MessageDoc msg = messageStore.findByMessageId(messageId, session.contact().getContactType());
		connector.reloadMedia(channelConfig, msg);
	}

}
