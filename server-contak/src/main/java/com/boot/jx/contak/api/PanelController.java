package com.boot.jx.contak.api;

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
import com.boot.jx.contak.doc.ContakMembershipDoc;
import com.boot.jx.contak.doc.ContakUserDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.mongo.CommonMongoQB.QueryCriteria;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Contak Panel", description = "API's for Panel", hidden = true)
@Controller
@RequestMapping("/panel")
public class PanelController {
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

	@ApiOperation(value = "Page", hidden = true)
	@RequestMapping(path = { "", "/", "/**" }, method = { RequestMethod.GET, RequestMethod.POST })
	public String defaultPage(Model model) {
		model.addAttribute("APP_NAME", appConfig.getAppName());
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		model.addAttribute("CDN_URL", appConfig.getAppPrefix());
		if (ArgUtil.is(appCommonConfig)) {
			model.addAllAttributes(appCommonConfig.appAttributes());
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (ArgUtil.is(auth) && ArgUtil.is(sessionBean.domainUser())) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_NAME", ArgUtil.ifNull(sessionBean.domainUser().getName(), Constants.BLANK));
			model.addAttribute("APP_USER_ROLE", JsonUtil.toJson(sessionBean.getRole()));
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_NAME", "User");
			model.addAttribute("APP_USER_ROLE", "['GUEST']");
		}
		return "app-contak";
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/login" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> login(Model model, @RequestParam String username, @RequestParam String password,
			HttpServletRequest request, HttpServletResponse response) {
		ContakUserDoc user = authService.authenticate(username, password, request);
		return ApiResponse.build().meta(MapModel.createInstance().put("username", user.getName()).toMap());
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/companys" }, method = { RequestMethod.GET })
	public ApiResponse<ContakMembershipDoc, Object> organization(Model model) {
		List<ContakMembershipDoc> m = commonMongoTemplate.collection(ContakMembershipDoc.class)
				.where(Criteria.where("user.id").is(sessionBean.domainUser().getId()).and("active").is(true)).find()
				.asList();
		sessionBean.memberships(m);
		return ApiResponse.buildResults(m);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/memberships" }, method = { RequestMethod.POST })
	public ApiResponse<ContakMembershipDoc, Object> addmember(Model model, @RequestParam String companyId,
			@RequestParam String username, @RequestParam(required = false) String membershipType) {

		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}

		ContakUserDoc user = authService.loadUserByUsername(username);

		if (ArgUtil.not(user)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("username").codeKey("InvalidUsername").description("User not found"));
		}

		CompanyDoc company = commonMongoTemplate.collection(CompanyDoc.class).where(QueryCriteria.whereId(companyId))
				.find().asFirst();;
		if (ArgUtil.not(company)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("InvalidCompany").description("Company not found"));
		}

		ContakMembershipDoc m = commonMongoTemplate.collection(ContakMembershipDoc.class)
				.where(QueryCriteria.where("userId").is(user.getId()).and("companyId").is(companyId)).find().asFirst();
		if (ArgUtil.not(m)) {
			m = new ContakMembershipDoc();
			m.setCompany(company);
			m.setUser(user);
		}
		m.setActive(ArgUtil.is(membershipType));
		m.setMembershipType(membershipType);
		commonMongoTemplate.save(m);
		return ApiResponse.buildResult(m);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/company" }, method = { RequestMethod.POST })
	public ApiResponse<CompanyDoc, Object> addOrg(Model model, @RequestBody CompanyDoc newComp) {
		CompanyDoc compoc = commonMongoTemplate.findOne(CommonMongoQueryBuilder.collection(CompanyDoc.class)
				.where(Criteria.where("number").is(newComp.getNumber())));
		if (ArgUtil.is(compoc)) {
			ApiResponseUtil.throwDuplicateInputException(new ApiFieldError().field("number"));
		}
		CompanyDoc companyDoc = new CompanyDoc();
		companyDoc.setActive(true);
		companyDoc.setLegalBusinessName(newComp.getLegalBusinessName());
		companyDoc.setDisplayName(newComp.getDisplayName());
		companyDoc.setCountryOfOperation(newComp.getCountryOfOperation());
		companyDoc.setAddress(newComp.getAddress());
		companyDoc.setWebsiteUrl(newComp.getWebsiteUrl());

		companyDoc.setContactPersonName(newComp.getContactPersonName());
		companyDoc.setContactPhoneNumber(newComp.getContactPhoneNumber());
		companyDoc.setContactPersonEmailId(newComp.getContactPersonEmailId());
		commonMongoTemplate.save(companyDoc);

		ContakUserDoc user = sessionBean.domainUser();
		ContakMembershipDoc m = new ContakMembershipDoc();
		m.setCompany(companyDoc);
		m.setUser(user);
		commonMongoTemplate.save(m);

		return ApiResponse.buildResult(companyDoc);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/company" }, method = { RequestMethod.PUT })
	public ApiResponse<CompanyDoc, Object> updateOrg(Model model, @RequestBody CompanyDoc newComp) {
		CompanyDoc compoc = commonMongoTemplate.findOne(CommonMongoQueryBuilder.collection(CompanyDoc.class)
				.where(Criteria.where("number").is(newComp.getNumber())));
		if (ArgUtil.is(compoc)) {
			ApiResponseUtil.throwDuplicateInputException(new ApiFieldError().field("number"));
		}
		CompanyDoc companyDoc = new CompanyDoc();
		companyDoc.setActive(true);
		companyDoc.setLegalBusinessName(newComp.getLegalBusinessName());
		companyDoc.setDisplayName(newComp.getDisplayName());
		companyDoc.setCountryOfOperation(newComp.getCountryOfOperation());
		companyDoc.setAddress(newComp.getAddress());
		companyDoc.setWebsiteUrl(newComp.getWebsiteUrl());

		companyDoc.setContactPersonName(newComp.getContactPersonName());
		companyDoc.setContactPhoneNumber(newComp.getContactPhoneNumber());
		companyDoc.setContactPersonEmailId(newComp.getContactPersonEmailId());
		commonMongoTemplate.save(companyDoc);

		ContakUserDoc user = sessionBean.domainUser();
		ContakMembershipDoc m = new ContakMembershipDoc();
		m.setCompany(companyDoc);
		m.setUser(user);
		commonMongoTemplate.save(m);

		return ApiResponse.buildResult(companyDoc);
	}

	@ResponseBody
	@RequestMapping(value = "/api/v1/logo", method = { RequestMethod.POST })
	public ApiResponse<String, Object> uploadFile(@RequestParam(name = "file", required = false) MultipartFile file) {
		ContakUserDoc user = sessionBean.domainUser();
		String domainUserId = user.getId();
		String url = fileStore.upload1(file,
				String.format("%s/docs/%s/logo/%s", AppContextUtil.getTenant(), domainUserId, UUID.randomUUID()),
				file.getOriginalFilename()).getUrl();
		return ApiResponse.buildResults(url).message("Logo uplodaed");
	}
}
