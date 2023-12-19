package com.boot.jx.contak.api;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.doc.ContakTemplateDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.dto.ContakModels.ContakActor;
import com.boot.jx.contak.dto.ContakModels.ContakInboundTrigger;
import com.boot.jx.contak.manager.ContakApiContext;
import com.boot.jx.contak.manager.ContakApiContext.AUTH_RULES;
import com.boot.jx.contak.manager.ContakInboundRouter;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.utils.ArgUtil;

@RestController
@RequestMapping("/client/api/v3")
public class NodeClientV3Controller {

	@Autowired
	private ContakApiContext apiContext;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private AWSFileStore fileStore;

	@Autowired
	private ContakInboundRouter contakInboundManager;

	@ApiRequest(authenticateTenant = true, rules = { AUTH_RULES.VALID_SESSION })
	@RequestMapping(value = { "/org/{companyId}/hsm/tmpl" }, method = { RequestMethod.GET })
	public ApiResponse<ContakTemplateDoc, Object> hsmTemplate(Model model) throws NoSuchAlgorithmException {
		CompanyDoc compoc = apiContext.validateCompany();
		return ApiResponse.buildResults(commonMongoTemplate.collection(ContakTemplateDoc.class)
				.where("companyId", compoc.companyId).find().asList());
	}

	@ApiRequest(authenticateTenant = true, rules = { AUTH_RULES.VALID_SESSION })
	@RequestMapping(value = { "/org/{companyId}/hsm/tmpl" }, method = { RequestMethod.POST })
	public ApiResponse<ContakTemplateDoc, Object> hsmTemplate(Model model, @RequestBody ContakTemplateDoc template)
			throws NoSuchAlgorithmException {
		CompanyDoc compoc = apiContext.validateCompany();

		if (ArgUtil.is(template.templateId)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("templateId").codeKey("AccessDenied")
					.description("Template Cannot be modified"));
		}

		if (!ArgUtil.is(template.companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Select Organization"));
		}

		template.templateId = String.format("%s:%s", template.companyId, template.code);
		commonMongoTemplate.save(template);

		contakInboundManager.sendSystemEvent(ContakInboundTrigger
				.type(ContakInboundRouter.USER_INBOUND_TYPE.TEMPLATE_UPDATE).companyId(compoc.getCompanyId()));

		return ApiResponse.buildResult(template);
	}

	@ApiRequest(authenticateTenant = true, rules = { AUTH_RULES.VALID_SESSION })
	@RequestMapping(value = { "/org/{companyId}/hsm/tmpl/{templateId}" }, method = { RequestMethod.DELETE })
	public ApiResponse<Object, Object> hsmTemplate(Model model, @PathVariable String companyId,
			@PathVariable String templateId) throws NoSuchAlgorithmException {
		CompanyDoc compoc = apiContext.validateCompany();
		ContakTemplateDoc template = commonMongoTemplate.collection(ContakTemplateDoc.class)
				.with(Criteria.where("companyId").is(companyId).and("templateId").is(templateId)).find().asFirst();
		template.setDeleted(!template.isDeleted());
		commonMongoTemplate.saveAndAudit(template);
		contakInboundManager.sendSystemEvent(ContakInboundTrigger
				.type(ContakInboundRouter.USER_INBOUND_TYPE.TEMPLATE_UPDATE).companyId(compoc.getCompanyId()));
		return ApiResponse.build().message("Template has been " + (template.isDeleted() ? "deleted" : "restored"));
	}

	@ApiRequest(authenticateTenant = true, rules = { AUTH_RULES.VALID_SESSION })
	@RequestMapping(value = "/org/{companyId}/hsm/tmpl/{templateId}/media", method = { RequestMethod.POST })
	public ApiResponse<CommonFile, Object> uploadFile(@RequestParam(name = "file", required = false) MultipartFile file,
			@RequestParam(name = "thumbnail", required = false) MultipartFile thumbnail, @PathVariable String companyId,
			@PathVariable String templateId) {
		CompanyDoc compoc = apiContext.validateCompany();
		CommonFile f = fileStore.upload1(file,
				String.format("%s_%s/tmpl/%s", AppContextUtil.getTenant(), companyId, templateId),
				file.getOriginalFilename());
		if (ArgUtil.is(thumbnail)) {
			CommonFile thumb = fileStore.upload1(thumbnail,
					String.format("%s_%s/tmpl/%s/th", AppContextUtil.getTenant(), companyId, templateId),
					file.getOriginalFilename());
			f.setThumb(thumb.getUrl());
		}
		return ApiResponse.buildResults(f).message("Header Uplodaed");
	}

}