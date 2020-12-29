package com.boot.jx.postman.hooks;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppConfig;
import com.boot.jx.api.AmxApiResponse;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.service.ApiWhaService;
import com.boot.utils.JsonUtil;

import io.swagger.annotations.Api;

@RestController
@Api(value = "WhatsAppHooks")
public class WhatsAppHooks {

	private static final Logger LOGGER = LoggerService.getLogger(WhatsAppHooks.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ApiWhaService apiWhaService;

	@RequestMapping(value = "/postman/webhook/apiwha/{secret}/update", method = { RequestMethod.POST })
	public AmxApiResponse<Object, Object> onAPIWHAMessage(@RequestParam(required = false) String secret,
			@RequestParam String data) {
		try {
			Map<String, Object> dataMap = JsonUtil.getMapFromJsonString(data);
			apiWhaService.onMessage(dataMap);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AmxApiResponse.build();
	}

}
