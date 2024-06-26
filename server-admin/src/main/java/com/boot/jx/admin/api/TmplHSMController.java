package com.boot.jx.admin.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.HSMTemplate3rdParty;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.manager.ThirdPartyTemplateManager;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;
import com.boot.utils.UniqueID;

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
			@RequestParam(required = false) String hsmTemplateId) {
		return new ApiResponse<HSMTemplate3rdParty, Object>()
				.data(thirdPartyTmplManager.link(templateId, hsmTemplateId));
	}

	@RequestMapping(value = "/api/tmpl/hsm/map_vars", method = { RequestMethod.POST })
	public ApiResponse<HSMTemplate3rdParty, Object> mapVars(@RequestParam String templateId,
			@RequestBody Map<String, Object> varMap) {
		return new ApiResponse<HSMTemplate3rdParty, Object>().data(thirdPartyTmplManager.varMap(templateId, varMap));
	}

	@RequestMapping(value = "/api/tmpl/hsm/meta", method = { RequestMethod.POST })
	public ApiResponse<HSMTemplateDoc, Object> updateHsmMeta(@RequestParam String templateId,
			@RequestBody Map<String, Object> newMata) {
		HSMTemplateDoc template = mongoTemplate.findById(templateId, HSMTemplateDoc.class);
		if (ArgUtil.is(template)) {
			template.meta().putAll(newMata);
			mongoTemplate.save(template);
		}
		return new ApiResponse<HSMTemplateDoc, Object>().result(template);
	}

	@RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.GET })
	public ApiResponse<HSMTemplate3rdParty, Object> listWabaTemplates(@RequestParam String channelId,
			@RequestParam(required = false) String templateCode,
			@RequestParam(required = false, defaultValue = "false") boolean sync) {
		ChannelConfig channelConfig = pmEnvironment.local().channel(channelId);

		if (!ArgUtil.is(channelConfig)) {
			return ApiResponse.build();
		}

		if (sync) {
			thirdPartyTmplManager.refreshWA360Templates(channelConfig);
		}
		return new ApiResponse<HSMTemplate3rdParty, Object>()
				.results(thirdPartyTmplManager.getTemplates(channelConfig, templateCode));
	}

	@RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.POST })
	public ApiResponse<HSMTemplate3rdParty, Object> createWabaTemplates(@RequestBody HSMTemplate3rdParty extTemplate) {
		ChannelConfig channelConfig = pmEnvironment.local().channel(extTemplate.getChannelId());

		String successMessage = null;
		HSMTemplate3rdParty temp = null;
		boolean editable = true;
		if (ArgUtil.is(extTemplate.getId())) {
			temp = mongoTemplate.findById(extTemplate.getId(), HSMTemplate3rdParty.class);
			if (ArgUtil.is(temp)) {
				String status = ArgUtil.parseAsString(temp.getTemplate().get("status"), Constants.BLANK);
				if ("pending".equalsIgnoreCase(status) || "submitted".equalsIgnoreCase(status)) {
					editable = false;
				}
				if (ArgUtil.is(extTemplate.getTemplate())) {
					extTemplate.getTemplate().put("status", status);
				}
			}
		}

		if (editable && ArgUtil.is(extTemplate.getTemplate())) {
			HSMTemplate3rdParty createTemplate = thirdPartyTmplManager.createhWA360Templates(channelConfig,
					extTemplate.getTemplate());
			successMessage = "Template submitted to waba";
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
			if (!ArgUtil.is(successMessage)) {
				successMessage = "Template Updated";
			}
		}
		return new ApiResponse<HSMTemplate3rdParty, Object>().result(temp).message(successMessage);
	}

	@RequestMapping(value = "/api/tmpl/hsm/waba_templates", method = { RequestMethod.DELETE })
	public ApiResponse<HSMTemplate3rdParty, Object> deleteWabaTemplate(@RequestParam String id,
			@RequestParam(required = false) String channelId) {
		HSMTemplate3rdParty temp = mongoTemplate.findById(id, HSMTemplate3rdParty.class);
		if (ArgUtil.is(temp)) {
			ChannelConfig channelConfig = pmEnvironment.local()
					.channel(ArgUtil.nonEmpty(channelId, temp.getChannelId()));
			thirdPartyTmplManager.deleteWA360Templates(channelConfig, temp);
		}
		return new ApiResponse<HSMTemplate3rdParty, Object>().data(temp);
	}

	@PutMapping("/api/tmpl/hsm/waba_migrate")
	public ApiResponse<HSMTemplate3rdParty, Object> MigrateTemplate(@RequestParam String oldChannelId,
			@RequestParam String newChannelId) {
		Query query = new Query(Criteria.where("channelId").is(oldChannelId));
		Update update = new Update().set("channelId", newChannelId);

		mongoTemplate.updateMulti(query, update, HSMTemplate3rdParty.class);
		return new ApiResponse<HSMTemplate3rdParty, Object>().message("Template Migrated");
	}

	// HSMTemplate
	@RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.GET })
	public ApiResponse<HSMTemplateDoc, Object> listPushTemplates() {
		return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplateDoc.class));
	}

	@RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.DELETE })
	public ApiResponse<HSMTemplateDoc, Object> deletePushTemplates(@RequestParam String id) {
		HSMTemplateDoc qr = mongoTemplate.findById(id, HSMTemplateDoc.class);
		mongoTemplate.trash(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplateDoc.class)).data(qr)
				.message("PushTemplate deleted");
	}

	@Autowired
	protected TmplClient tmplClient;

	@RequestMapping(value = "/api/tmpl/hsm", method = { RequestMethod.POST })
	public ApiResponse<HSMTemplateDoc, Object> createPushTemplates(@RequestBody HSMTemplateDoc newVersion) {

		if (ArgUtil.is(newVersion.getId())) {
			HSMTemplateDoc oldVersion = mongoTemplate.findById(newVersion.getId(), HSMTemplateDoc.class);
			if (ArgUtil.is(oldVersion)) {
				mongoTemplate.archive(oldVersion);
			}
		}

		MapModel tmplOptions = MapModel.from(newVersion.options());
		Attachment tmplattachment = tmplOptions.keyEntry("attachment").as(Attachment.class);
		if (ArgUtil.is(tmplattachment) && !ArgUtil.is(tmplattachment.getId())) {
			tmplattachment.setId(UniqueID.generateString62());
			tmplOptions.keyEntry("attachment").save(tmplattachment);
		}

		auditDetailProvider.auditCreate(newVersion);
		mongoTemplate.save(newVersion);

		if (ArgUtil.is(tmplattachment) && ArgUtil.is(tmplattachment.getMediaTemplate())) {
			OutboxMessage outboxMessage = new OutboxMessage();
			outboxMessage.setModel(JsonUtil.deepCopy(newVersion.getModel()));
			CommonTemplateMeta hsmTemp = new CommonTemplateMeta();
			hsmTemp.setId(newVersion.getId());
			hsmTemp.setCode(newVersion.getCode());
			outboxMessage.setHsm(hsmTemp);
			tmplClient.process(outboxMessage);

			for (Attachment attachment : outboxMessage.getAttachments()) {
				if (ArgUtil.is(attachment.getMediaTemplate()) && tmplattachment.getId().equals(attachment.getId())) {
					tmplattachment.setMediaURL(attachment.getMediaURL());
					tmplOptions.keyEntry("attachment").save(tmplattachment);
				}
			}
			mongoTemplate.save(newVersion);
		}

		return ApiResponse.buildResults(mongoTemplate.findAll(HSMTemplateDoc.class)).data(newVersion)
				.message("QuickReply created");
	}
}
