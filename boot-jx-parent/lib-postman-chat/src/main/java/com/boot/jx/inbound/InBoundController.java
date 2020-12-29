package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppConfig;
import com.boot.jx.connectors.FacebookConnector;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.gupshup.GupShupInboundV2;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.service.ContactCleanerService;
import com.boot.jx.rest.RestService;
import com.boot.jx.vendor.VendorContext.ApiVendorHeaders;

@RestController
public class InBoundController {

	@Autowired
	private InBoundService inBoundEngine;

	@Autowired
	private GupShupChatClient gupShupChatClient;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ContactCleanerService contactCleanerService;

	@Autowired
	private RestService restService;

	@ApiVendorHeaders
	@RequestMapping(value = "/int/inbound/callback", method = RequestMethod.POST)
	public InboxMessage onInboundCallback(@RequestBody InboxMessage inboxMessage,
			@RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
		// botService.arhive(inbound);
		inBoundEngine.invokeMethods(inboxMessage);
		return inboxMessage;
	}

	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/gupshup/callback", method = RequestMethod.POST)
	public GupShupInbound onReceiveMessage(@RequestBody GupShupInboundV2 inboundV2,
			@RequestParam(required = false, defaultValue = "false") boolean routed) throws InterruptedException {
		GupShupInbound inbound = gupShupChatClient.parseAsGupShupInbound(inboundV2);
		if (!routed && !appConfig.isProdMode() && contactCleanerService.isWhatsAppTest(inbound.getMobile())) {
			return restService.ajax("https://apid-kwt.amxremit.com/bot/ext/inbound/gupshup/callback?routed=true")
					.post(inboundV2).as(GupShupInbound.class);
		} else {
			// botService.arhive(inbound);
			InboxMessage event = gupShupChatClient.parseAsInboxMessage(inboundV2);
			inBoundEngine.invokeMethods(event);
		}
		return inbound;
	}

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private FacebookConnector facebookConnector;

	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.GET)
	public String get(@RequestParam(name = "hub.verify_token") String token,
			@RequestParam(name = "hub.challenge") String challenge, @RequestParam(required = false) String lane) {
		return facebooClient.registerWebhook(token, challenge, lane);
	}

	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessage(@RequestBody FacebookHookRequest request,
			@RequestParam(required = false) String lane) throws InterruptedException {
		request.getEntry().forEach(e -> {
			e.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, lane);
				inBoundEngine.invokeMethods(event);
			});
		});
		return request;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback/{lane}", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessageLane(@RequestBody FacebookHookRequest request, @PathVariable String lane)
			throws InterruptedException {
		request.getEntry().forEach(e -> {
			e.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, lane);
				inBoundEngine.invokeMethods(event);
			});
		});
		return request;
	}
}
