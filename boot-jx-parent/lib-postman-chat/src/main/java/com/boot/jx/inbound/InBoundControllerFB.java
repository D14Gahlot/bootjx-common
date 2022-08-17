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

import com.boot.jx.connectors.FacebookConnector;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.fb.FacebooClient;
import com.boot.jx.postman.fb.FacebookHookRequest;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;

@RestController
public class InBoundControllerFB {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundControllerFB.class);

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private FacebooClient facebooClient;

	@Autowired
	private FacebookConnector facebookConnector;

	@Autowired
	private PMEnvironment pmEnvironment;

	@RequestMapping(value = { "/ext/inbound/v2/fb/callback/{accountKey}/{channelId}/{channelKey}" },
			method = RequestMethod.GET)
	public Object get(@RequestParam(name = "hub.verify_token") String token,
			@RequestParam(name = "hub.challenge") String challenge,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature,
			@PathVariable(required = false) String accountKey, @PathVariable(required = false) String channelId,
			@PathVariable(required = false) String channelKey) {
		ChannelConfig channelConfig = pmEnvironment.local().channel(channelId);
		return facebooClient.registerWebhook(channelConfig, token, challenge);
	}

	@Deprecated
	// @ApiRequest(feature = "WA_GUPSHUP_INBOUND")
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessage(@RequestBody FacebookHookRequest request,
			@RequestParam(required = false) String lane,
			@RequestHeader(required = false, value = "X-Hub-Signature") String signature) throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethodsAsync(event);
				// facebooClient.sendReply(event.getContactId(), "Helo", pageEntry.getId());
			});
		});
		return request;
	}

	@Deprecated
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/fb/callback/{lane}", method = RequestMethod.POST)
	public FacebookHookRequest onReceiveMessageLane(@RequestBody FacebookHookRequest request, @PathVariable String lane)
			throws InterruptedException {
		request.getEntry().forEach(pageEntry -> {
			pageEntry.getMessaging().forEach(m -> {
				InboxMessage event = facebookConnector.toInboxMessage(m, pageEntry.getId());
				inBoundService.invokeMethodsAsync(event);
			});
		});
		return request;
	}

}
