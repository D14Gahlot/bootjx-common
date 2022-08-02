package com.boot.jx.bot;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatClient.PATH;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Controller
public class OutBoundControllerBot {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutBoundControllerBot.class);

	@Autowired
	private BotEngine botEngine;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private RestService restService;

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@ResponseBody
	@RequestMapping(value = "/ext/app/{botCode}/{appId}/{appKeyHash}", method = { RequestMethod.POST })
	public ApiResponse<Object, Object> inboundMessageBoxEvent(@PathVariable String botCode, @PathVariable String appId,
			@PathVariable String appKeyHash, @RequestBody Map<String, Object> data) {
		MapModel map = MapModel.from(data);
		ClientApp app = pmEnvironment.config().clientApiKey(appId);

		String isFrwrded = commonHttpRequest.get("X-Forwarded-Service");

		if (!ArgUtil.is(isFrwrded) && ArgUtil.is(app.getOutboundhook())) {
			restService.ajax(app.getOutboundhook()).header("X-Forwarded-Service", pmCommonConfig.getServiceServer())
					.post(data).asNone();
		} else if (app.equals(CHAT_MODE.BOT)) {
			ChatController controller = botEngine.getBotByCode("bot_" + app.getAppType().toLowerCase());
			if (ArgUtil.is(controller)) {
				controller.onPostOutboundMessage(map);
			}
		} else if (app.equals(CHAT_MODE.WEBHOOK)) {
			String webhook = ArgUtil.is(app.getWebhook()) ? app.getWebhook()
					: (pmCommonConfig.getScriptusUrl() + PATH.APP_SCRIPT_FRWRD_OUTBOUND);

			restService.ajax(webhook).post(data).asNone();
		}

		return ApiResponse.build();
	}

}
