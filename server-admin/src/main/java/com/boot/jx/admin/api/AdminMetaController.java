package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.AChannelDetails;
import com.boot.jx.postman.doc.HSMContentType;
import com.boot.jx.postman.doc.HSMLanguage;
import com.boot.jx.postman.doc.HSMMessageType;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.SafeKeyHashMap;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AdminMetaController {

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

    @Autowired
    private PMEnvironment pmEnvironment;

    @JsonView(PMEnvironment.PublicProperty.class)
    @ResponseBody
    @RequestMapping(value = { "/api/options/channels" }, method = { RequestMethod.GET })
    public ApiResponse<AChannelDetails, Object> listActiveLanes() {
	return ApiResponse.buildResults(pmEnvironment.config().listChannels());
    }

}
