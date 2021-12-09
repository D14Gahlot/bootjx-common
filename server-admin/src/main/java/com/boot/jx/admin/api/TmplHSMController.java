package com.boot.jx.admin.api;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.HSMTemplate;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.manager.ThirdPartyTemplateManager;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@RestController
public class TmplHSMController {

    @Autowired
    private PMEnvironment pmEnvironment;

    @Autowired
    private ThirdPartyTemplateManager thirdPartyTmplManager;

    @Autowired
    private CommonMongoTemplate mongoTemplate;

    @Autowired
    private AuditDetailProvider auditDetailProvider;

    @RequestMapping(value = "/api/tmpl/hsm/link", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> linkWabaTemplates(@RequestParam String templateId,
	    @RequestParam String hsmTemplateId) {
	return new ApiResponse<HSMTemplate3rdParty, Object>()
		.data(thirdPartyTmplManager.link(templateId, hsmTemplateId));
    }

    @RequestMapping(value = "/api/tmpl/hsm/map_vars", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> mapVars(@RequestParam String templateId,
	    @RequestBody Map<String, Object> varMap) {
	return new ApiResponse<HSMTemplate3rdParty, Object>().data(thirdPartyTmplManager.varMap(templateId, varMap));
    }

    @RequestMapping(value = "/api/tmpl/hsm/meta", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate, Object> updateHsmMeta(@RequestParam String templateId,
	    @RequestBody Map<String, Object> newMata) {
	HSMTemplate template = mongoTemplate.findById(templateId, HSMTemplate.class);
	if (ArgUtil.is(template)) {
	    template.meta().putAll(newMata);
	    mongoTemplate.save(template);
	}
	return new ApiResponse<HSMTemplate, Object>().result(template);
    }

    @RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.GET })
    public ApiResponse<HSMTemplate3rdParty, Object> listWabaTemplates(@RequestParam String channelId,
	    @RequestParam(required = false) String templateCode,
	    @RequestParam(required = false, defaultValue = "false") boolean sync) {
	ChannelConfig channelConfig = pmEnvironment.config().channels(channelId);
	if (sync) {
	    thirdPartyTmplManager.refreshWA360Templates(channelConfig);
	}
	return new ApiResponse<HSMTemplate3rdParty, Object>()
		.results(thirdPartyTmplManager.getTemplates(channelConfig, templateCode));
    }

    @RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate3rdParty, Object> createWabaTemplates(@RequestBody HSMTemplate3rdParty extTemplate) {
	ChannelConfig channelConfig = pmEnvironment.config().channels(extTemplate.getChannelId());

	HSMTemplate3rdParty temp = null;
	boolean editable = true;
	if (ArgUtil.is(extTemplate.getId())) {
	    temp = mongoTemplate.findById(extTemplate.getId(), HSMTemplate3rdParty.class);
	    if (ArgUtil.is(temp)) {
		String status = ArgUtil.parseAsString(temp.getTemplate().get("status"), Constants.BLANK);
		if ("approved".equalsIgnoreCase(status) || "pending".equalsIgnoreCase(status)
			|| "submitted".equalsIgnoreCase(status)) {
		    editable = false;
		}
	    }
	}

	if (editable && ArgUtil.is(extTemplate.getTemplate())) {
	    HSMTemplate3rdParty createTemplate = thirdPartyTmplManager.createhWA360Templates(channelConfig,
		    extTemplate.getTemplate());
	    extTemplate.setId(createTemplate.getId());
	    thirdPartyTmplManager.refreshWA360Templates(channelConfig);
	}

	if (ArgUtil.is(extTemplate.getId())) {
	    temp = mongoTemplate.findById(extTemplate.getId(), HSMTemplate3rdParty.class);
	    if (ArgUtil.is(extTemplate.getHsmTemplateId())) {
		thirdPartyTmplManager.link(temp.getId(), extTemplate.getHsmTemplateId());
	    }
	    if (ArgUtil.is(extTemplate.getVarMap())) {
		thirdPartyTmplManager.varMap(temp.getId(), extTemplate.getVarMap());
	    }
	}
	return new ApiResponse<HSMTemplate3rdParty, Object>().result(temp).message("Template submitted to waba");

    }

    // HSMTemplate
    @RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.GET })
    public ApiResponse<HSMTemplate, Object> listPushTemplates() {
	return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplate.class));
    }

    @RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.DELETE })
    public ApiResponse<HSMTemplate, Object> deletePushTemplates(@RequestParam String id) {
	HSMTemplate qr = mongoTemplate.findById(id, HSMTemplate.class);
	mongoTemplate.trash(qr);
	return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplate.class)).data(qr)
		.message("PushTemplate deleted");
    }

    @RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.POST })
    public ApiResponse<HSMTemplate, Object> createPushTemplates(@RequestBody HSMTemplate newVersion) {

	if (ArgUtil.is(newVersion.getId())) {
	    HSMTemplate oldVersion = mongoTemplate.findById(newVersion.getId(), HSMTemplate.class);
	    if (ArgUtil.is(oldVersion)) {
		mongoTemplate.archive(oldVersion);
	    }
	}
	auditDetailProvider.audit(newVersion);
	mongoTemplate.save(newVersion);
	return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplate.class)).data(newVersion)
		.message("QuickReply created");
    }
}
