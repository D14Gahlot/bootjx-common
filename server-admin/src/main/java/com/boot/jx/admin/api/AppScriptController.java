package com.boot.jx.admin.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.CONFIG_SETUP_KEY;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpClientException;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.APP_TYPE;
import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AppScriptController {

	@Autowired
	private RestService restService;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private PMEnvironment pmEnvironment;

	@RequestMapping(value = "/api/objects/appscript/{appId}", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<Map<String, Object>, Object> getAppScript(@PathVariable String appId,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortDir) {
		
		ClientApp app = pmEnvironment.local().clientApiKey(appId);
		if (!ArgUtil.is(app) || !(APP_TYPE.APP_SCRIPT.name().equals(app.getAppType())
				|| APP_TYPE.WEBHOOK.name().equals(app.getAppType())
				|| CHAT_MODE.SCRIPTUS.name().equals(app.getAppMode()))) {
			ApiResponseUtil.throwAccessDeniedException("App Not found");
		}
		
		MapModel meta = MapModel.createInstance().put("appId", app.getId()).put("appQueue", app.getQueue())
				.put("appName", app.getKeyName()).put("appMode", app.getAppMode()).put("appType", app.getAppType());

		Map<String, Object> mapp = null;
		try {
			mapp = restService.ajax(pmCommonConfig.getScriptusUrl() + "/bot/getBot")
					.queryParam("id", appId + AppContextUtil.getTenant()).queryParam("appId", appId)
					.queryParam("domain", AppContextUtil.getTenant()).get().asMap();
		} catch (ApiHttpClientException e) {
			ApiResponseUtil.addWarning("bot code not found");
		}
		return ApiResponse.buildResults(mapp).meta(meta.toMap());
	}

	@RequestMapping(value = "/api/objects/appscript/{appId}", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<Map<String, Object>, Object> saveAppScript(@PathVariable String appId,
			@RequestBody MapModel data) {
		ClientApp app = pmEnvironment.local().clientApiKey(appId);

		if (!ArgUtil.is(app) || !(APP_TYPE.APP_SCRIPT.name().equals(app.getAppType())
				|| APP_TYPE.WEBHOOK.name().equals(app.getAppType()))) {
			ApiResponseUtil.throwAccessDeniedException("App Not found");
		}

		String domain = AppContextUtil.getTenant();
		data.put("domain", domain);
		data.put("server", pmCommonConfig.getServiceServer());
		data.put("appId", app.getId());
		data.put("appKey", app.getKey());
		data.put("id", appId + AppContextUtil.getTenant());
		return ApiResponse.buildResults(
				restService.ajax(pmCommonConfig.getScriptusUrl() + "/bot/setBot").postJson(data.toMap()).asMap());

	}

	@RequestMapping(value = "/api/objects/appscript/{appId}/logs", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<Map<String, Object>, Object> getAppScriptLogs(@PathVariable String appId,
			@RequestParam(required = false) String contactId,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortDir) {

		ClientApp app = pmEnvironment.local().clientApiKey(appId);
		if (!ArgUtil.is(app) || !(APP_TYPE.APP_SCRIPT.name().equals(app.getAppType())
				|| APP_TYPE.WEBHOOK.name().equals(app.getAppType())
				|| CHAT_MODE.SCRIPTUS.name().equals(app.getAppMode()))) {
			ApiResponseUtil.throwAccessDeniedException("App Not found");
		}

		if (!ArgUtil.is(contactId)) {
			contactId = pmEnvironment.keyEntry(CONFIG_SETUP_KEY.POSTMAN_DEBUG_CONTACT).asString();
		}

		return restService.ajax(pmCommonConfig.getScriptusUrl() + "/bot/getLogs").queryParam("app_id", appId)
				.queryParam("contact_id", contactId).queryParam("domain", AppContextUtil.getTenant()).get()
				.asAmxApiResponseOfMap();
	}

}
