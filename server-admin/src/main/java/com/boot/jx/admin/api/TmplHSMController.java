package com.boot.jx.admin.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.manager.ThirdPartyTemplateManager;
import com.boot.jx.postman.plugin.ChannelConfig;

@RestController
public class TmplHSMController {

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private ThirdPartyTemplateManager templateManager;

    @RequestMapping(value = "/api/tmpl/hsm/link", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> linkWabaTemplates(@RequestParam String templateId,
	    @RequestParam String hsmTemplateId) {
	return new ApiResponse<HSMTemplate3rdParty, Object>().data(templateManager.link(templateId, hsmTemplateId));
    }

    @RequestMapping(value = "/api/tmpl/hsm/map_vars", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> mapVars(@RequestParam String templateId,
	    @RequestParam Map<String, Object> varMap) {
	return new ApiResponse<HSMTemplate3rdParty, Object>().data(templateManager.varMap(templateId, varMap));
    }

    @RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.GET })
    public ApiResponse<HSMTemplate3rdParty, Object> listWabaTemplates(@RequestParam String channelId,
	    @RequestParam(required = false) String templateCode,
	    @RequestParam(required = false, defaultValue = "false") boolean sync) {
	ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
	if (sync) {
	    templateManager.refreshWA360Templates(channelConfig);
	}
	return new ApiResponse<HSMTemplate3rdParty, Object>()
		.results(templateManager.getTemplates(channelConfig, templateCode));
    }

    @RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> createWabaTemplates(@RequestParam String channelId,
	    @RequestBody Map<String, Object> templateStructure) {
	ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
	return new ApiResponse<HSMTemplate3rdParty, Object>()
		.result(templateManager.createhWA360Templates(channelConfig, templateStructure));
    }

}
