package com.boot.jx.inbound;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PMAuditEvent;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Random;

@RestController
public class InBoundController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundController.class);

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired
	private AuditService auditService;

	@Autowired
	private ChatSessionService chatSessionService;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/inbound/callback", method = RequestMethod.POST)
	public InboxMessage onInboundCallback(@RequestBody InboxMessage inboxMessage,
			@RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
		// botService.arhive(inbound);
		if (PostManUtil.hasValidCheckSum(inboxMessage)) {
//	    PMConfiguration config = pmEnvironment.config();
//	    String channelId = PostManUtil.CHANNEL_ID(inboxMessage.contact());
//	    ChannelConfig channelConfig = config.channel(channelId);
//	    ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
//	    connector.prompt(inboxMessage);
			inBoundService.invokeMethods(inboxMessage);
		}
		return inboxMessage;
	}

	static AtomicInteger counter = new AtomicInteger(1);

	@Autowired
	private ChatStatusService inBoundStatusService;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/status/callback", method = RequestMethod.POST)
	public List<MessageReport> onStatusCallback() throws InterruptedException {
		List<MessageReport> list = new LinkedList<MessageReport>();
		String ser = ArgUtil.parseAsString(counter.getAndIncrement());
		for (int i = 0; i < 5; i++) {
			MessageReport report = new MessageReport();
			report.setMessageIdRef(ArgUtil.parseAsString(ser));
			report.setMessageIdExt(ArgUtil.parseAsString(i));
			int statusint = Random.getInt(0, 5);
			report.setStatus(Message.Status.values()[statusint]);
			report.setChangeStamp(Random.getInt(100, 999));
			list.add(report);
		}
		inBoundStatusService.offer(list);
		inBoundStatusService.process(ser);
		return list;
	}

	@ApiVendorHeaders
	@RequestMapping(value = ChatClient.PATH.ASSIGN_TO_AGENT_V2, method = RequestMethod.POST)
	public InBoundEvent assignToAgentV2(@RequestBody PMArgs params) {
		return chatSessionService.assignSessionToAgent(params).value();
	}

	@ApiVendorHeaders
	@RequestMapping(value = ChatClient.PATH.SESSION_EVENT, method = RequestMethod.POST)
	public InBoundEvent inboundEvent(@RequestBody MapModel map) {
		PMArgs pmArgs = map.keyEntry("pmArgs").as(PMArgs.class);
		InBoundEvent event = map.keyEntry("event").as(InBoundEvent.class);
		return chatSessionService.sessionEvent(event, pmArgs);
	}

	@RequestMapping(value = "/ext/inbound/v2/{channelType}/callback/{accountKey}/{channelId}/{channelKey}",
			method = { RequestMethod.POST })
	public ApiResponse<Object, Object> inboundMessageBoxEvent(@PathVariable(required = false) String channelType,
			@PathVariable(required = false) String accountKey, @PathVariable(required = false) String channelId,
			@PathVariable(required = false) String channelKey, @RequestBody Map<String, Object> data) {
		MapModel map = MapModel.from(data);
		PMConfiguration config = pmEnvironment.config();
		ChannelConfig channelConfig = config.channel(channelId);
		ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);

		try {
			MessageBoxEvent messageBoxEvent = connector.inboundMessageBoxEvent(channelConfig, map,
					new MessageBoxEvent());
			if (ArgUtil.is(messageBoxEvent.getInboxMessages())) {
				messageBoxEvent.getInboxMessages().forEach(inboxMessage -> {
					connector.prompt(inboxMessage);
					inBoundService.invokeMethodsAsync(inboxMessage);
				});
				connector.onReadInboxMessage(channelConfig, messageBoxEvent.getInboxMessages());
			} else if (ArgUtil.is(messageBoxEvent.getMessageReports())) {
				connector.onMessageReports(channelConfig, messageBoxEvent.getMessageReports());
				inBoundStatusService.update(messageBoxEvent.getMessageReports());
			}
		} catch (Exception e) {
			auditService.excep(new PMAuditEvent(PMAuditEvent.Type.INBOUND_ERROR).data(data), LOGGER, e);
		}

		return ApiResponse.build();
	}
}
