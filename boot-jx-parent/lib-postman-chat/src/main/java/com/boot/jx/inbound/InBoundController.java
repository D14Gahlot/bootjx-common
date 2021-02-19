package com.boot.jx.inbound;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import com.boot.jx.AppConfig;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatClient;
import com.boot.jx.connectors.FacebookConnector;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.gupshup.GupShupInboundV2;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.service.ContactCleanerService;
import com.boot.jx.rest.RestService;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.JsonUtil;

@RestController
public class InBoundController {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundController.class);

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private GupShupChatClient gupShupChatClient;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ContactCleanerService contactCleanerService;

	@Autowired
	private RestService restService;

	@Autowired
	private ChatClient chatClient;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/webhook/callback", method = RequestMethod.POST)
	public String setWebHook(@RequestParam(required = false) String callbackUrl) throws InterruptedException {
		chatClient.setInboundForwardUrl(callbackUrl);
		return callbackUrl;
	}

	@RequestMapping(value = "/int/webhook/callback", method = RequestMethod.GET)
	public String getWebHook() throws InterruptedException {
		return chatClient.getInboundForwardUrl();
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

	@ApiVendorHeaders
	@RequestMapping(value = ChatClient.PATH.ASSIGN_TO_AGENT, method = RequestMethod.POST)
	public ApiResponse<InboxMessage, ?> assignToAgent(@RequestBody InboxMessage inboxMessage)
			throws InterruptedException {
		return inBoundService.assignToAgent(inboxMessage);
	}

	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/gupshup/callback", method = RequestMethod.POST)
	public InboxMessage onReceiveMessage(@RequestBody Map<String, Object> inboundMap,
			@RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
		try {
			InboxMessage event = null;
			if (inboundMap.containsKey("waNumber")) {
				event = gupShupChatClient.parseAsInboxMessage(JsonUtil.toObject(inboundMap, GupShupInbound.class));
			} else {
				event = gupShupChatClient.parseAsInboxMessage(JsonUtil.toObject(inboundMap, GupShupInboundV2.class));
			}
			event.setOriginalMessage(inboundMap);
			inBoundService.invokeMethods(event);
			return event;
		} catch (Exception e) {
			LOGGER.error("INBOUND", e);
		}
		return null;

	}

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private FacebookConnector facebookConnector;

	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.GET)
	public String get(@RequestParam(name = "hub.verify_token") String token,
			@RequestParam(name = "hub.challenge") String challenge, @RequestParam(required = false) String lane,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature) {
		return facebooClient.registerWebhook(token, challenge, lane);
	}

	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessage(@RequestBody FacebookHookRequest request,
			@RequestParam(required = false) String lane,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature) throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethods(event);
				// facebooClient.sendReply(event.getContactId(), "Helo", pageEntry.getId());
			});
		});
		return request;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback/{lane}", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessageLane(@RequestBody FacebookHookRequest request, @PathVariable String lane)
			throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethods(event);
			});
		});
		return request;
	}
}
