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
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.client.DocumentService;
import com.maxmind.geoip2.exception.GeoIp2Exception;

import io.swagger.annotations.ApiParam;

/**
 * The Class GeoServiceController.
 */
@RestController
public class DocumentServiceController {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentServiceController.class);

	/** The geo location service. */
	@Autowired
	DocumentService documentService;

	@RequestMapping(value = PostManUrls.DOC_UPLOAD_URL, method = RequestMethod.GET)
	public String geoLocation(@RequestParam @ApiParam(defaultValue = "171.50.210.63") String ip)
			throws PostManException, IOException, GeoIp2Exception {
		return documentService.generateUrl();
	}

}
