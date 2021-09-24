package com.boot.jx.common.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.impl.ConfigMeta;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.doc.HSMContentType;
import com.boot.jx.postman.doc.HSMLanguage;
import com.boot.jx.postman.doc.HSMMessageType;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ChannelPlugin;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class CommonMetaController {

    @Autowired
    private PMEnvironment pmEnvironment;

    @RequestMapping(value = "/api/meta/message_types", method = { RequestMethod.GET })
    public ApiResponse<HSMMessageType, Object> messageType() {
	return ApiResponse.buildResults(HSMMessageType.values());
    }

    @RequestMapping(value = "/api/meta/message_content_types", method = { RequestMethod.GET })
    public ApiResponse<HSMContentType, Object> messageContentType() {
	return ApiResponse.buildResults(HSMContentType.values());
    }

    @RequestMapping(value = "/api/meta/langs", method = { RequestMethod.GET })
    public ApiResponse<HSMLanguage, Object> languages() {
	return ApiResponse.buildResults(HSMLanguage.values());
    }

    @RequestMapping(value = "/api/meta/channel_types", method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> channel() {
	return ApiResponse.buildResults(new ArrayList<AChannelDetails>(ChannelPluginProvider.DETAILS.values()));
    }

    @RequestMapping(value = "/api/meta/channel_configs/{channelType}", method = { RequestMethod.GET })
    public ApiResponse<ConfigMeta, Object> channelConfig(@PathVariable CHANNEL_TYPE_ENUM channelType) {
	ChannelPlugin<? extends AChannelDetails> plugin = ChannelPluginProvider.MAP.get(channelType.toString());
	List<ConfigMeta> configs = plugin.listConfigMeta(pmEnvironment);
	return ApiResponse.buildResults(configs);
    }

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/options/channels" }, method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> listActiveLanes() {
	return ApiResponse.buildResults(pmEnvironment.config().listChannels());
    }

}
