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
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.doc.config.ClientKeyConfigDoc;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@Controller
@RequestMapping("/cpanel")
public class CPanelController {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccountAdminService accountAdminService;

    @Autowired
    private AccountSessionBean sessionBean;

    @RequestMapping(value = { "/app", "/app/**", "/app/*" }, method = { RequestMethod.POST, RequestMethod.GET })
    public String cpanel(Model model, @RequestParam(required = false) String authToken) {

	model.addAllAttributes(appCommonConfig.appAttributes());

	Authentication auth = AccountAdminService.getAuthentication();

	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_ROLE", sessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}

	model.addAttribute("APP", "account");

	return "app-cpanel";
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelType}", method = { RequestMethod.POST })
    public ApiResponse<ChannelConfig, Object> saveChannelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled,
	    @RequestBody Map<String, Object> data) {
	return ApiResponse.buildResults(configManager.saveChannelConfig(channelType.toString(), disabled, data));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.GET })
    public ApiResponse<ChannelConfig, Object> getChannelConfig(@PathVariable String channelId,
	    @RequestParam(defaultValue = "false", required = false) boolean disabled) {
	return ApiResponse.buildResults(configManager.getChannelConfig(channelId));
    }

    @ResponseBody
    @RequestMapping(value = "/api/config/channel/{channelId}", method = { RequestMethod.DELETE })
    public ApiResponse<ChannelConfig, Object> deleteChannelConfig(@PathVariable String channelId) {
	return ApiResponse.buildResults(configManager.removeChannelConfig(channelId));
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/options/lanes" }, method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> listActiveLanes() {
	return ApiResponse.buildResults(pmEnvironment.config().connectors());
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.GET })
    public ApiResponse<ClientKeyConfigDoc, Object> createClientApiKey() {
	return ApiResponse.buildResults(mongoTemplate.findAll(ClientKeyConfigDoc.class));
    }

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/config/clientapikey" }, method = { RequestMethod.POST })
    public ApiResponse<ClientKeyConfigDoc, Object> createClientApiKey(@RequestBody ClientKeyConfigDoc clientApiKey) {
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
