package com.boot.jx.postman.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.postman.PMConstants.PostManUrls;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.GeoLocation;
import com.boot.jx.postman.service.GeoLocationServiceImpl;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import io.swagger.annotations.ApiParam;

/**
 * The Class GeoServiceController.
 */
@RestController
public class GeoServiceController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(GeoServiceController.class);

	/** The geo location service. */
	@Autowired
	GeoLocationServiceImpl geoLocationService;

	/**
	 * Geo location.
	 *
	 * @param ip
	 *            the ip
	 * @return the geo location
	 * @throws PostManException
	 *             the post man exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws GeoIp2Exception
	 *             the geo ip 2 exception
	 */
	@RequestMapping(value = PostManUrls.GEO_LOC, method = RequestMethod.GET)
	public GeoLocation geoLocation(@RequestParam @ApiParam(defaultValue = "171.50.210.63") String ip)
			throws PostManException, IOException, GeoIp2Exception {
		return geoLocationService.getLocation(ip);
	}

}
