package com.boot.jx.contak.api;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.ContakAuthService;
import com.boot.jx.contak.ContakSessionBean;
import com.boot.jx.contak.doc.ContakTemplateDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.model.CommonFile;
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
	private AWSFileStore fileStore;

	private void validateCompany(String companyId) {
		if (!ArgUtil.is(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Select Organization"));
		}

		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v2/org/{companyId}/hsm/tmpl/{templateId}" }, method = { RequestMethod.DELETE })
	public ApiResponse<Object, Object> hsmTemplate(Model model, @PathVariable String companyId,
			@PathVariable String templateId) throws NoSuchAlgorithmException {
		validateCompany(companyId);
		commonMongoTemplate.collection(ContakTemplateDoc.class)
				.with(Criteria.where("companyId").is(companyId).and("templateId").is(templateId)).set("deleted", true);
		return ApiResponse.build().message("Template has been deleted");
	}

	@ResponseBody
	@RequestMapping(value = "/api/v2/org/{companyId}/hsm/tmpl/{templateCode}/media", method = { RequestMethod.POST })
	public ApiResponse<CommonFile, Object> uploadFile(@RequestParam(name = "file", required = false) MultipartFile file,
			@RequestParam(name = "thumbnail", required = false) MultipartFile thumbnail, @PathVariable String companyId,
			@PathVariable String templateCode) {
		validateCompany(companyId);
		ContakUserDoc user = sessionBean.domainUser();
		String domainUserId = user.getId();
		CommonFile f = fileStore.upload1(file,
				String.format("%s_%s/tmpl/%s/%s", AppContextUtil.getTenant(), companyId, templateCode),
				file.getOriginalFilename());
		if (ArgUtil.is(thumbnail)) {
			CommonFile thumb = fileStore.upload1(file,
					String.format("%s_%s/tmpl/%s/th", AppContextUtil.getTenant(), companyId, templateCode),
					file.getOriginalFilename());
			f.setThumb(thumb.getUrl());
		}

		return ApiResponse.buildResults(f).message("Header Uplodaed");
	}
}
