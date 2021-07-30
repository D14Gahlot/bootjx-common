package com.boot.jx.postman.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.event.TriggerEvent;
import com.boot.jx.postman.PMConstants.PostManUrls;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.tunnel.ITunnelService;
import com.maxmind.geoip2.exception.GeoIp2Exception;

/**
 * The Class GeoServiceController.
 */
@RestController
public class EventController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);

	private static ITunnelService tunnelService;

	/**
	 * Geo location.
	 *
	 * @param ip the ip
	 * @return the geo location
	 * @throws PostManException the post man exception
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws GeoIp2Exception  the geo ip 2 exception
	 */
	@RequestMapping(value = PostManUrls.EVENT_PUBLISH, method = RequestMethod.GET)
	public long publishEvent(@RequestParam String event, @PathVariable String id,
			@RequestParam(required = false) String value) throws PostManException, IOException {
		return tunnelService.shout(event, new TriggerEvent(event, id, value));
	}

	@Autowired
	PostManConfig postManConfig;

	@RequestMapping(value = "properties/test", method = RequestMethod.GET)
	public Map<String, Object> test() throws PostManException, IOException {
		Map<String, Object> map = new HashMap<String, Object>();

		map.put("key", postManConfig.getTenant());
		LOGGER.info("=====");
		map.put("key2", postManConfig.getTenant());

		return map;
	}
}
