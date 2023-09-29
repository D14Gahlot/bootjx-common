package com.boot.jx.xms.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.AmxResponseSchemes.ApiResultsMetaCompactResponse;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.config.ConfigManagerImpl;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMContextUtil;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.doc.config.ClientAppConfigDoc;
import com.boot.jx.postman.doc.config.VarsConfigDoc.CompanyVarsConfigDoc;
import com.boot.jx.postman.model.ext.MsgChannel;
import com.boot.jx.postman.store.ConfigMaster;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.jx.xms.dto.WebhookUrlRequest;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.EntityDtoUtil;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Configuration", description = "API's for configuration management",
		authorizations = @Authorization("X_API_KEY"))
@RestController
public class ConfigApiV1 {

	@Autowired
	private ConfigManagerImpl configManager;

	@Autowired
	private ConfigMaster configMaster;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@ApiOperation(value = "Set Webhook", notes = "${swagger.ConfigApiV1.setWebhookUrl.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@JsonView(PMEnvironment.PublicProperty.class)
	@RequestMapping(value = "/api/v1/config/webhook", method = { RequestMethod.POST })
	public ApiResponse<ClientApp, Object> setWebhookUrl(@RequestBody WebhookUrlRequest req) {

		ClientApp x = PMContextUtil.clientApp();
		if (ArgUtil.is(x)) {
			ClientAppConfigDoc xo = configMaster.findById(x.getId(), ClientAppConfigDoc.class);
			if (ArgUtil.areEqual(xo.getAppType(), ClientApp.APP_TYPE_WEBHOOK)) {
				xo.setWebhook(req.url);
				xo.setForward(req.forward);
				configManager.save(xo);
				configManager.refresh();
			} else {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("appType").obzect("WebhookUrlRequest")
						.codeKey("INCORRECT_APP_TYPE").description("ClientApp is not configured for Webhook type"));
			}
		} else {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("appType").obzect("WebhookUrlRequest")
					.codeKey("INCORRECT_APP_TYPE").description("ClientApp is not configured"));
		}
		return ApiResponse.buildResults(x).meta(req);
	}

	@ApiOperation(value = "Channels List", notes = "${swagger.ConfigApiV1.getChannels.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/config/channels", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResultsMetaCompactResponse<MsgChannel, Object> getChannels(
			@RequestParam(required = false, defaultValue = "false") boolean sabdnox) {
		List<AChannelConfig> list = pmEnvironment.config().listChannels();
		List<MsgChannel> newList = CollectionUtil.getList(MsgChannel.class);
		for (AChannelConfig channelConfig : list) {
			MsgChannel channel = new MsgChannel();
			EntityDtoUtil.entityToDto(channelConfig, channel);
			newList.add(channel);
		}
		return ApiResponse.buildResults(newList);
	}

	@ApiOperation(value = "HSM Templates", notes = "${swagger.ConfigApiV1.getHSMTemplates.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/config/tmpl/hsm", method = { RequestMethod.GET })
	public ApiResponse<HSMTemplateDoc, Object> getHSMTemplates(@RequestParam(required = false) String channelId) {

		MongoQueryBuilder<HSMTemplateDoc> cmq = MongoQueryBuilder.collection(HSMTemplateDoc.class);

		if (ArgUtil.is(channelId)) {
			cmq.where("approved.channelId", channelId);
		}

		return ApiResponse.buildResults(commonMongoTemplate.find(cmq));
	}

	@ApiOperation(value = "Global Vars", notes = "${swagger.ConfigApiV1.getGlobalVars.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/config/global/vars", method = { RequestMethod.GET })
	public ApiResponse<CompanyVarsConfigDoc, Object> getGlobalVars(@RequestParam(required = false) String channelId) {

		MongoQueryBuilder<CompanyVarsConfigDoc> cmq = MongoQueryBuilder.collection(CompanyVarsConfigDoc.class);

		if (ArgUtil.is(channelId)) {
			cmq.where("approved.channelId", channelId);
		}

		return ApiResponse.buildResults(commonMongoTemplate.find(cmq));
	}
}
