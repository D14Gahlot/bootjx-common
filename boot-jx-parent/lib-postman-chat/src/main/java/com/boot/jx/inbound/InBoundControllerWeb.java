package com.boot.jx.inbound;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.connectors.WebConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PMAuditEvent;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;

@Controller
public class InBoundControllerWeb {

    private static final Logger LOGGER = LoggerFactory.getLogger(InBoundControllerWeb.class);

    @Autowired
    private InBoundService inBoundService;

    @Autowired(required = false)
    private WebConnector dummyConnector;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private MessageStore messageStore;

    @Autowired
    private WebConnector connector;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired(required = false)
    private PMCommonConfig pmCommonConfig;

    @Autowired
    private AuditService auditService;

    @Autowired
    private ChatStatusService inBoundStatusService;

    @Autowired
    private AppConfig appConfig;

    @RequestMapping(value = "/plugin/customer/**", method = RequestMethod.GET)
    public String pluginCustomer(Model model, @RequestParam(required = false) String contacyType,
	    @RequestParam(required = false, defaultValue = "/plugin/customer") String path)
	    throws InterruptedException {
	commonHttpRequest.setCookie("contactType", ArgUtil.parseAsString(contacyType, ContactType.WEBSITE.toString()));
	model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("WEBAPP_BASE", appConfig.getAppPrefix() + path);
	model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR",
		pmEnvironment.keyEntry("postman.agent.scheme.color").asString());

	if (pmCommonConfig != null) {
	    model.addAllAttributes(pmCommonConfig.appAttributes());
	}

	String nounce = UniqueID.generateString62();
	model.addAttribute("NOUNCE", nounce);
	commonHttpRequest.setCookie("NOUNCE", nounce);

	return "app-customer";
    }

    @RequestMapping(value = "/pub/plugin/customer/**", method = RequestMethod.GET)
    public String pluginCustomerPub(Model model, @RequestParam(required = false) String contacyType)
	    throws InterruptedException {
	return pluginCustomer(model, contacyType, "/pub/plugin/customer");
    }

    @ApiRequest(type = RequestType.POLL)
    @ResponseBody
    @RequestMapping(value = "/ext/outbound/web/callback", method = RequestMethod.GET)
    public OutboxMessage onReceiveMessage(@RequestParam String number) throws InterruptedException {
	return dummyConnector.pollUnreadMessage(number);
    }

    @ResponseBody
    @RequestMapping(value = "/ext/outbound/web/auth", method = RequestMethod.GET)
    public ApiResponse<OutboxMessage, Object> onAuth(@RequestParam String number) throws InterruptedException {
	String webSessionId = commonHttpRequest.get("web-session-id");

	ChatSessionDoc session = null;
	if (ArgUtil.is(webSessionId)) {
	    session = sessionStore.getValidSession(webSessionId);
	}
	List<OutboxMessage> msgs = new ArrayList<OutboxMessage>();
	if (ArgUtil.is(session)) {
	    List<MessageDoc> messages = messageStore.findBySessionId(webSessionId, ContactType.WEBSITE.toString());
	    for (MessageDoc messageDoc : messages) {
		OutboxMessage outboxMessage = new OutboxMessage();
		outboxMessage.setTimestamp(messageDoc.getTimestamp());
		outboxMessage.setMessage(messageDoc.getMessage());
		outboxMessage.template(messageDoc.getTemplate());
		outboxMessage.setAttachments(messageDoc.getAttachments());
		if (ArgUtil.isEqual(messageDoc.getType(), "I")) {
		    outboxMessage.addTo(messageDoc.getContactId());
		} else {
		    // webConnector.process(outboxMessage);
		}
		msgs.add(outboxMessage);
	    }
	}

	return ApiResponse.buildResults(msgs);
    }

    @ResponseBody
    @RequestMapping(value = "/ext/outbound/web/auth/v2", method = RequestMethod.GET)
    public ApiResponse<ChatMessageDTO, Object> onAuthV2(@RequestParam String number) throws InterruptedException {
	String webSessionId = commonHttpRequest.get("web-session-id");

	ChatSessionDoc session = null;
	if (ArgUtil.is(webSessionId)) {
	    session = sessionStore.getValidSession(webSessionId);
	}
	List<ChatMessageDTO> msgs = new ArrayList<ChatMessageDTO>();
	if (ArgUtil.is(session)) {
	    List<MessageDoc> messages = messageStore.findBySessionId(webSessionId, ContactType.WEBSITE.toString());
	    for (MessageDoc messageDoc : messages) {
		ChatMessageDTO outboxMessage = ChatDTOUtil.getChatMessageDTO(messageDoc);
		if (ArgUtil.isEqual(messageDoc.getType(), "I", "O")) {
		    msgs.add(outboxMessage);
		}
	    }
	}

	return ApiResponse.buildResults(msgs);
    }

    @RequestMapping(value = "/ext/inbound/v2/web/callback/{nounce}/{channelId}/{channelKey}",
	    method = { RequestMethod.POST })
    public ApiResponse<Object, Object> inboundMessageBoxEvent(@PathVariable(required = false) String nounce,
	    @PathVariable(required = false) String channelId, @PathVariable(required = false) String channelKey,
	    @RequestBody Map<String, Object> data) {
	MapModel map = MapModel.from(data);
	PMConfiguration config = pmEnvironment.config();
	ChannelConfig channelConfig = config.channel(channelId);
	// ConnectorHandler connector = connectorHandlerFactory.get(channelConfig);
	try {

	    if (!ArgUtil.areEqual(nounce, commonHttpRequest.get("NOUNCE"))) {
		ApiResponseUtil.throwUnAuthorizedException("Invalid API");
	    }

	    if (!ArgUtil.is(channelConfig) || !ArgUtil.areEqual(channelConfig.getChannelKey(), channelKey)) {
		ApiResponseUtil.throwAccessDeniedException("Invalid Channel");
	    }

	    MessageBoxEvent messageBoxEvent = connector.inboundMessageBoxEvent(channelConfig, map,
		    new MessageBoxEvent());
	    if (ArgUtil.is(messageBoxEvent.getInboxMessages())) {
		messageBoxEvent.getInboxMessages().forEach(inboxMessage -> {
		    inBoundService.invokeMethods(inboxMessage);
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
