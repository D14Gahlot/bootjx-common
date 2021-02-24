package com.boot.jx.inbound;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.connectors.WARapiwhaConnector;
import com.boot.jx.postman.client.GupShupChatClient;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.gupshup.GupShupInboundV2;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.utils.JsonUtil;

@RestController
public class InBoundControllerWA {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundControllerWA.class);

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private GupShupChatClient gupShupChatClient;

	@Autowired
	private WARapiwhaConnector waRapiwhaConnector;

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

	@RequestMapping(value = "/postman/webhook/apiwha/{secret}/update", method = { RequestMethod.POST })
	public ApiResponse<Object, Object> onAPIWHAMessage(@RequestParam(required = false) String secret,
			@RequestParam String data) {
		try {
			Map<String, Object> dataMap = JsonUtil.getMapFromJsonString(data);
			waRapiwhaConnector.toInboxMessage(dataMap, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ApiResponse.build();
	}
}
