package com.boot.jx.postman.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.gupshup.GupShupClientChat;
import com.boot.jx.postman.gupshup.GupShupConstants;
import com.boot.jx.postman.gupshup.GupShupClientNotify;
import com.boot.jx.postman.gupshup.GupShupResp;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.maxmind.geoip2.exception.GeoIp2Exception;

/**
 * The Class GeoServiceController.
 */
@RestController
public class GupShupController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GupShupController.class);

	/** The geo location service. */
	@Autowired
	GupShupClientChat gupShupChatClient;

	@Autowired
	GupShupClientNotify gupShupNotifyClient;

	@RequestMapping(value = "/gupshup/optin", method = RequestMethod.POST)
	public GupShupResp optIn(
			@RequestParam GupShupConstants.SessionType sessionType,
			@RequestParam String phone)
			throws PostManException, IOException, GeoIp2Exception {
		switch (sessionType) {
		case NOTIFICATION:
			return gupShupNotifyClient.optIn(phone);
		default:
			return gupShupChatClient.optIn(phone);
		}
	}

	@RequestMapping(value = "/gupshup/optout", method = RequestMethod.POST)
	public GupShupResp optout(
			@RequestParam GupShupConstants.SessionType sessionType,
			@RequestParam String phone)
			throws PostManException, IOException, GeoIp2Exception {
		switch (sessionType) {
		case NOTIFICATION:
			return gupShupNotifyClient.optOut(phone);
		default:
			return gupShupChatClient.optOut(phone);
		}
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/gupshup/message", method = RequestMethod.POST)
	public GupShupResp message(
			@RequestParam GupShupConstants.SessionType sessionType,
			@RequestParam String phone, @RequestParam String message)
			throws PostManException, IOException, GeoIp2Exception {
		switch (sessionType) {
		case NOTIFICATION:
			return gupShupNotifyClient.sendMessage(phone, message);
		default:
			return null;
			//return gupShupChatClient.sendMessage(phone, message);
		}
	}

}
