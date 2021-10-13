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
import com.boot.jx.chat.ChatStatusReportService;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.postman.PMClientConfig;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageReport;
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
    private PMClientConfig chatClientConfig;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private ConnectorHandlerFactory connectorHandlerFactory;

    @ApiVendorHeaders
    @RequestMapping(value = "/int/webhook/callback", method = RequestMethod.POST)
    public String setWebHook(@RequestParam(required = false) String callbackUrl) throws InterruptedException {
	chatClientConfig.setInboundForwardUrl(callbackUrl);
	return callbackUrl;
    }

    @RequestMapping(value = "/int/webhook/callback", method = RequestMethod.GET)
    public String getWebHook() throws InterruptedException {
	return chatClientConfig.getInboundForwardUrl();
    }

    @ApiVendorHeaders
    @RequestMapping(value = "/int/inbound/callback", method = RequestMethod.POST)
    public InboxMessage onInboundCallback(@RequestBody InboxMessage inboxMessage,
	    @RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
	// botService.arhive(inbound);
	if (PostManUtil.hasValidCheckSum(inboxMessage)) {
	    inBoundService.invokeMethods(inboxMessage);
	}
	return inboxMessage;
    }

    static AtomicInteger counter = new AtomicInteger(1);

    @Autowired
    private ChatStatusReportService chatStatusReportService;

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
	    report.setTimestamp(Random.getInt(100, 999));
	    list.add(report);
	}
	chatStatusReportService.offer(list);
	chatStatusReportService.process(ser);
	return list;
    }

    @ApiVendorHeaders
    @RequestMapping(value = ChatClient.PATH.ASSIGN_TO_AGENT, method = RequestMethod.POST)
    public ApiResponse<InboxMessage, ?> assignToAgent(@RequestBody InboxMessage inboxMessage)
	    throws InterruptedException {
	return inBoundService.assignToAgent(inboxMessage);
    }

    @RequestMapping(value = "/ext/inbound/v2/{channelType}/callback/{accountKey}/{channelId}/{channelKey}",
	    method = { RequestMethod.POST })
    public ApiResponse<Object, Object> onWA360Message(@PathVariable(required = false) String channelType,
	    @PathVariable(required = false) String accountKey, @PathVariable(required = false) String channelId,
	    @PathVariable(required = false) String channelKey, @RequestBody Map<String, Object> data) {
	MapModel map = MapModel.from(data);
	PMConfiguration config = pmEnvironment.config();
	ChannelConfig channelConfig = config.channels(channelId);
	ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
	List<InboxMessage> inboundMessages = connector.extractInboxMessages(channelConfig, map);
	inboundMessages.forEach(inboxMessage -> {
	    inBoundService.invokeMethodsAsync(inboxMessage);
	    connector.onReadInboxMessage(channelConfig, inboundMessages);
	});
	return ApiResponse.build();
    }
}
