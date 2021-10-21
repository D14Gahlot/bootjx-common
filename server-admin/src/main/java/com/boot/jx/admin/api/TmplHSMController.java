package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.model.MapModel;

@RestController
public class TmplHSMController {

    @Autowired
    private WA360Client wa360Client;

    @Autowired
    private PMEnvironment pmEnvironment;

    @RequestMapping(value = "/api/tmpl/waba_templates", method = { RequestMethod.GET })
    public ApiResponse<Object, Object> listPushTemplates(@RequestParam String channelId) {
	ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
	MapModel resp = wa360Client.fetchTemplates(channelConfig);
	return ApiResponse.instance().results(resp.keyEntry("waba_templates").asList())
		.meta(resp.remove("waba_templates").toMap());
    }

}
