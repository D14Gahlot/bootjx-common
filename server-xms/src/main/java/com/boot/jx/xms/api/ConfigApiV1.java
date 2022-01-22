package com.boot.jx.xms.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.AmxResponseSchemes.ApiResultsMetaCompactResponse;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.config.ConfigManager;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelConfig;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.ext.MsgChannel;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.WebhookUrlRequest;
import com.boot.utils.CollectionUtil;
import com.boot.utils.EntityDtoUtil;
import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Config APIs", description = "API's for configuration management")
@Controller
public class ConfigApiV1 {

    @Autowired
    private ConfigManager configManager;

    @Autowired
    private PMEnvironment pmEnvironment;

    @ApiOperation(value = "Set Webhook URL", notes = "${swagger.ConfigApiV1.setWebhookUrl.description}")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/config/webhook", method = { RequestMethod.POST })
    public ApiResultsMetaCompactResponse<PMConfigurationObject, Object> setWebhookUrl(
	    @RequestBody WebhookUrlRequest req) {
	PMConfigurationObject config = pmEnvironment.keyEntry(ConfigConstants.SETUP_KEY.POSTMAN_CHAT_INBOUND_WEBHOOK);
	config.setValue(req.url);
	configManager.save(config);
	return ApiResponse.buildResults(config).meta(req);
    }

    @ApiOperation(value = "Get Channels", notes = "${swagger.ConfigApiV1.getChannels.description}")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/config/channels", method = { RequestMethod.POST })
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
}
