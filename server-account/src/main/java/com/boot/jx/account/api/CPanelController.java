package com.boot.jx.account.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.ConfigManagerImpl;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.common.models.AppAuthModels.ACCESS_RULES;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMConstants.USER_ROLE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.ApiParam;

@Controller
@RequestMapping("/cpanel")
public class CPanelController {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private ConfigManagerImpl configManager;

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private AccountStore accountStore;

	@Autowired
	private AccountSessionBean adminSessionBean;

	@RequestMapping(value = { "", "/", "/**", "/app", "/app/**", "/app/*" },
			method = { RequestMethod.POST, RequestMethod.GET })
	public String cpanel(Model model, @RequestParam(required = false) String authToken) {

		model.addAllAttributes(appCommonConfig.appAttributes());

		Authentication auth = AccountAuthService.getAuthentication();

		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_NAME", adminSessionBean.domainUser().getContact().getName());
			model.addAttribute("APP_USER_ROLE", JsonUtil.toJson(adminSessionBean.getRole()));
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_NAME", "");
			model.addAttribute("APP_USER_ROLE", "['GUEST']");
		}

		model.addAttribute("APP", "cpanel");

		return "app-cpanel";
	}

	@ApiRequest(rules = AppAuthModels.ACCESS_RULES.ONLY_DUPERUSER)
	@ResponseBody
	@RequestMapping(value = "/api/manage/user/role", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<Object, Object> manageUserRol(
			@ApiParam(allowableValues = USER_ROLE.ALLOWED) @RequestParam String role, @RequestParam String email,
			@RequestParam boolean assign) {
		BusinessUserDoc user = accountStore.findUserByEmail(email);

		if (!ArgUtil.is(user)) {
			ApiResponseUtil.throwException("User Not found");
		}

		if (assign) {
			user.role().add(role);
		} else {
			user.role().remove(role);
		}
		accountStore.save(user);
		return ApiResponse.build().message("Role Modified");
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelType}", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> saveChannelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType,
			@RequestBody Map<String, Object> data) {
		return ApiResponse.buildResults(configManager.saveChannelConfig(channelType.toString(), data));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> getChannelConfig(@PathVariable String channelId,
			@RequestParam(defaultValue = "false", required = false) boolean disabled) {
		return ApiResponse.buildResults(configManager.getChannelConfig(channelId));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.DELETE })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> deleteChannelConfig(@PathVariable String channelId) {
		return ApiResponse.buildResults(configManager.updateChannelConfig(channelId, "remove"));
	}

	@ResponseBody
	@RequestMapping(value = "/api/config/channel/{channelId}/{action}", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChannelConfig, Object> sandboxChannelConfig(@PathVariable String channelId,
			@PathVariable String action) {
		return ApiResponse.buildResults(configManager.updateChannelConfig(channelId, action));
	}

	@JsonView(PMEnvironment.PublicProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.GET })
	public ApiResponse<ClientAppConfigDoc, Object> createClientApiKey() {
		return ApiResponse.buildResults(mongoTemplate.findAll(ClientAppConfigDoc.class));
	}

	@JsonView(PMEnvironment.OneTimeVisibleProperty.class)
	@ResponseBody
	@RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.POST })
	public ApiResponse<ClientAppConfigDoc, Object> createClientApiKey(@RequestBody ClientAppConfigDoc clientApiKey) {
		return ApiResponse.buildData(configManager.save(clientApiKey));
	}

	@ApiRequest(rules = PMConstants.USER_ROLE.BUSINESS_USER)
	@ResponseBody
	@RequestMapping(value = { "/api/collection/drop" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> dropCollection(@RequestParam String collectionName) {
		mongoTemplate.dropCollection(collectionName);
		return ApiResponse.build();
	}

}
