package com.boot.jx.inbound;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.cdn.BootJxConfigService;
import com.boot.jx.chat.ConnectorHandlerFactory;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.ioutbound.MessageService;
import com.boot.jx.ioutbound.OutBoundMsgBasic.OutBoundMsg;
import com.boot.jx.ioutbound.OutBoundReciept;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConfiguration.PMConfigurationModel;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.postman.PMContextUtil;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.manager.ConfigManager;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.ConfigMaster;
import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Controller
@RequestMapping("/test")
public class InBoundControllerTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(InBoundControllerTest.class);

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired(required = false)
	private PMCommonConfig pmCommonConfig;

	@Autowired(required = false)
	private BootJxConfigService bootJxConfigService;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private MessageService messageService;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Autowired
	private ConnectorHandlerFactory connectorHandlerFactory;

	@Autowired
	private ConfigMaster configMaster;

	@Autowired(required = false)
	private ConfigManager configManager;

	private PMConfigurationModel validateApiKey() {
		String apiKey = commonHttpRequest.get(ParamKeys.X_API_KEY);
		if (!ArgUtil.is(apiKey)) {
			ApiResponseUtil.throwException("Missing " + ParamKeys.X_API_KEY);
		}
		PMConfigurationModel config = pmEnvironment.local();
		ClientApp apiKeyConfig = config.clientApiKey(apiKey);

		if (!ArgUtil.is(apiKeyConfig)) {
			ApiResponseUtil.throwException("Invalid " + ParamKeys.X_API_KEY);
		}

		if (ArgUtil.areEqual(apiKey, apiKeyConfig.getKey())) {
			PMContextUtil.clientApp(apiKeyConfig);
		}
		return config;
	}

	@ApiRequest(session = true)
	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String pluginCustomer(Model model, HttpServletRequest request) throws InterruptedException {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());
		if (pmCommonConfig != null) {
			model.addAllAttributes(pmCommonConfig.appAttributes());
		}

		if (ArgUtil.is(bootJxConfigService)) {
			model.addAllAttributes(bootJxConfigService.bootJxAttributesModel().cdnApp("test").cdnAEntry("dev")
					.preventUpgradeInsecureRequest().map());
		}

		return "app-test";
	}

	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/message/send", method = { RequestMethod.POST })
	@ResponseBody
	public ApiResponse<OutBoundReciept, Object> sendMessage(@RequestBody OutBoundMsg message) {
		PMConfigurationModel config = validateApiKey();
		return ApiResponse.buildResult(messageService.send(message));
	}

	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/setup/channel/webhook", method = { RequestMethod.GET })
	@ResponseBody
	public ApiResponse<MapModel, Object> getChannelWebhook(@RequestParam String channelId) {
		if (ArgUtil.not(channelId)) {
			ApiResponseUtil.throwException("Select Channel");
		}
		PMConfigurationModel config = validateApiKey();
		ChannelConfig channelDto = config.channel(channelId);

		if (!ArgUtil.is(channelDto)) {
			channelDto = pmEnvironment.config().channel(channelId);
		}

		String webhook_url = pmClientConfig.getWebhookUrl(channelDto, null);
		String webhook_path = PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelDto);
		return ApiResponse.buildResult(MapModel.createInstance().put("webhook_url", webhook_url)
				.put("webhook_path", webhook_path).put("webhook_context", appConfig.getAppPrefix()));
	}

	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/setup/channel/webhook", method = { RequestMethod.POST })
	@ResponseBody
	public ApiResponse<MapModel, Object> resetChannelWebhook(@RequestParam String channelId,
			@RequestParam(required = false) String endpoint, @RequestParam(required = false) String context) {
		if (ArgUtil.not(channelId)) {
			ApiResponseUtil.throwException("Select Channel");
		}
		PMConfigurationModel config = validateApiKey();
		ChannelConfig channelDto = config.channel(channelId);

		if (!ArgUtil.is(channelDto)) {
			ApiResponseUtil.throwException("Cannot Update Webhook for Shared/Sandbox Channels");
		}

		context = ArgUtil.nonEmpty(context, appConfig.getAppPrefix());
		if (ArgUtil.is(endpoint)) {
			PMContextUtil.publicUrl(String.format("%s%s", endpoint, context));
		}
		String webhook_url = pmClientConfig.getWebhookUrl(channelDto, null);
		String webhook_path = PostManUtil.CHANNEL_CALLBACK_PATH(config.getAccountKey(), channelDto);
		connectorHandlerFactory.onChannelUpdate(channelDto);
		return ApiResponse.buildResult(MapModel.createInstance().put("channelId", channelId)
				.put("webhook_url", webhook_url).put("webhook_path", webhook_path).put("webhook_context", context));
	}

	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/setup/clientapp/webhook", method = { RequestMethod.POST })
	@ResponseBody
	public ApiResponse<MapModel, Object> setClientAppWebhook(@RequestParam String url,
			@RequestParam(required = false) String forward) {
		PMConfigurationModel config = validateApiKey();
		ClientApp x = PMContextUtil.clientApp();
		if (ArgUtil.is(x)) {
			ClientAppConfigDoc xo = configMaster.findById(x.getId(), ClientAppConfigDoc.class);
			if (ArgUtil.areEqual(xo.getAppType(), ClientApp.APP_TYPE_WEBHOOK)) {
				xo.setWebhook(url);
				if (ArgUtil.is(forward)) {
					xo.setForward(forward);
				}
				if (ArgUtil.is(configManager)) {
					configManager.save(xo);
					configManager.refresh();
				}
			} else {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("appType").obzect("WebhookUrlRequest")
						.codeKey("INCORRECT_APP_TYPE").description("ClientApp is not configured for Webhook type"));
			}
		} else {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("appType").obzect("WebhookUrlRequest")
					.codeKey("INCORRECT_APP_TYPE").description("ClientApp is not configured"));
		}
		return ApiResponse.buildResult(MapModel.createInstance().put("appId", x.getId()).put("webhook_url", url));
	}

}
