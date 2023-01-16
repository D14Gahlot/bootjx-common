package com.boot.jx.contak.api;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
import com.boot.jx.contak.doc.ContakApiKey;
import com.boot.jx.contak.doc.ContakMembershipDoc;
import com.boot.jx.contak.doc.ContakTemplateDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.Random;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

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
