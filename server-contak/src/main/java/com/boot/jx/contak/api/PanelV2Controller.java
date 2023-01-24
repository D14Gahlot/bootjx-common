package com.boot.jx.contak.api;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.ContakAuthService;
import com.boot.jx.contak.ContakSessionBean;
import com.boot.jx.contak.doc.ContakTemplateDoc;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.utils.ArgUtil;

import io.swagger.annotations.Api;

@Api(tags = "Contak Panel V2", description = "API's for Panel", hidden = true)
@Controller
@RequestMapping("/panel")
public class PanelV2Controller {
	@Autowired
	private AppConfig appConfig;

	@Autowired
	private ContakAuthService authService;

	@Autowired(required = false)
	private AppCommonConfig appCommonConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private ContakSessionBean sessionBean;

	@Autowired
	AWSFileStore fileStore;

	@ResponseBody
	@RequestMapping(value = { "/api/v2/org/{companyId}/hsm/tmpl/{templateId}" }, method = { RequestMethod.DELETE })
	public ApiResponse<Object, Object> hsmTemplate(Model model, @PathVariable String companyId,
			@PathVariable String templateId) throws NoSuchAlgorithmException {

		if (!ArgUtil.is(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Select Organization"));
		}

		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}

		commonMongoTemplate.collection(ContakTemplateDoc.class)
				.with(Criteria.where("companyId").is(companyId).and("templateId").is(templateId)).set("deleted", true);

		return ApiResponse.build().message("Template has been deleted");
	}

}
