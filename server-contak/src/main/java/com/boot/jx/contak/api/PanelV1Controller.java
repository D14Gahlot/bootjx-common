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
import com.boot.jx.contak.dto.CompanyMeta;
import com.boot.jx.model.CommonFile;
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

@Api(tags = "Contak Panel", description = "API's for Panel", hidden = true)
@Controller
@RequestMapping("/panel")
public class PanelV1Controller {
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
		if (ArgUtil.not(user)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("password").codeKey("InvalidCredentials")
					.description("Invalid username or password"));
		}
		return ApiResponse.build().meta(MapModel.createInstance().put("username", user.getName()).toMap());
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/signup" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> signup(Model model, @RequestParam String name, @RequestParam String email,
			@RequestParam String phone, HttpServletRequest request, HttpServletResponse response)
			throws NoSuchAlgorithmException {
		ContakUserDoc user = authService.loadUserByUsername(email);

		if (ArgUtil.is(user)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("username").codeKey("InvalidUsername")
					.description("User Already Registered"));
		}

		String verifyCode = Random.randomAlphaNumeric(10);
		user = new ContakUserDoc();
		user.setEmail(email);
		user.setName(name);
		user.setPhone(phone);
		user.meta().setEmailVerificationCode(CryptoUtil.getSHA2Hash(verifyCode));
		commonMongoTemplate.save(user);
		authService.sendResetMail(user, "tenant-verify-email", verifyCode);

		return ApiResponse.build()
				.meta(MapModel.createInstance().put("email", user.getEmail()).put("name", user.getName()).toMap());
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/resetpass" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> forgotpass(Model model, @RequestParam String email, HttpServletRequest request,
			HttpServletResponse response) throws NoSuchAlgorithmException {
		ContakUserDoc user = authService.loadUserByUsername(email);

		if (!ArgUtil.is(user)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("username").codeKey("InvalidUsername").description("User Not Found"));
		}
		String verifyCode = Random.randomAlphaNumeric(10);
		user.meta().setEmailVerificationCode(CryptoUtil.getSHA2Hash(verifyCode));
		commonMongoTemplate.save(user);

		authService.sendResetMail(user, "tenant-reset-pass", verifyCode);

		return ApiResponse.build()
				.meta(MapModel.createInstance().put("email", user.getEmail()).put("name", user.getName()).toMap())
				.message("Reset password link sent to email");
	}

	@ResponseBody
	@RequestMapping(value = { "/auth/v1/setpass" }, method = { RequestMethod.GET, RequestMethod.POST })
	public ApiResponse<Object, Object> setpass(Model model, @RequestParam String email, @RequestParam String varifyCode,
			String passsword, HttpServletRequest request, HttpServletResponse response) {
		ContakUserDoc user = authService.loadUserByUsername(email);

		if (!ArgUtil.is(user)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("username").codeKey("InvalidUsername").description("User Not Found"));
		}
		if (!CryptoUtil.getEncoder().message(varifyCode).sha2().is(user.meta().getEmailVerificationCode())) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("varifyCode").codeKey("InvalidLink").description("Invalid Link"));
		}
		user.meta().setEmailVerificationCode(Constants.BLANK);
		user.meta().setPassword(CryptoUtil.getEncoder().message(passsword).sha2().toString());
		commonMongoTemplate.save(user);
		// authService.authenticate(email, passsword, request);
		return ApiResponse.build()
				.meta(MapModel.createInstance().put("email", user.getEmail()).put("name", user.getName()).toMap())
				.message("Password is has been reset");
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

		ContakMembershipDoc m = authService.addMembership(user, company, membershipType);
		return ApiResponse.buildResult(m);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/company" }, method = { RequestMethod.POST })
	public ApiResponse<CompanyDoc, Object> addOrg(Model model, @RequestBody CompanyDoc newComp) {

		CompanyDoc compoc = null;
		if (ArgUtil.is(newComp.getCompanyId())) {
			if (!sessionBean.hasAdminAccesTo(newComp.getCompanyId())) {
				ApiResponseUtil.throwInputException(
						new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
			} else {
				compoc = commonMongoTemplate.findById(newComp.getCompanyId(), CompanyDoc.class);
			}
		} else {
			compoc = commonMongoTemplate.findOne(CommonMongoQueryBuilder.collection(CompanyDoc.class)
					.where(Criteria.where("displayName").is(newComp.getDisplayName())));
			if (ArgUtil.is(compoc)) {
				ApiResponseUtil.throwDuplicateInputException(
						new ApiFieldError().field("displayName").description("Invalid Display Name"));
			}
		}

		if (!ArgUtil.is(compoc)) {
			compoc = new CompanyDoc();
		}

		compoc.setActive(true);
		compoc.setLegalBusinessName(newComp.getLegalBusinessName());
		compoc.setDisplayName(newComp.getDisplayName());
		compoc.setCountryOfOperation(newComp.getCountryOfOperation());
		compoc.setAddress(newComp.getAddress());
		compoc.setWebsiteUrl(newComp.getWebsiteUrl());
		compoc.setLogoUrl(newComp.getLogoUrl());
		compoc.setPrefs(newComp.getPrefs());

		compoc.setContactPersonName(newComp.getContactPersonName());
		compoc.setContactPhoneNumber(newComp.getContactPhoneNumber());
		compoc.setContactPersonEmailId(newComp.getContactPersonEmailId());
		compoc.meta().setUpdateStamp(System.currentTimeMillis());

		commonMongoTemplate.save(compoc);
		authService.addMembership(sessionBean.domainUser(), compoc, PMConstants.USER_SHIP_TYPE.OA_OWNER);
		authService.updateLogin(sessionBean.domainUser());

		return ApiResponse.buildResult(compoc);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/company/key" }, method = { RequestMethod.POST })
	public ApiResponse<ContakApiKey, Object> resetKey(Model model, @RequestParam String companyId)
			throws NoSuchAlgorithmException {
		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}

		commonMongoTemplate.update(MQB.select(ContakApiKey.class)
				.where(QueryCriteria.where("companyId").is(companyId).and("active").is(false)).set("active", false));

		CompanyDoc compoc = commonMongoTemplate.findById(companyId, CompanyDoc.class);
		String newKeyString = UUID.randomUUID().toString();
		ContakApiKey newKey = new ContakApiKey();
		newKey.setUserId(sessionBean.domainUser().getId());
		newKey.setKey(CryptoUtil.getSHA2Hash(newKeyString));
		newKey.setCompanyId(companyId);
		newKey.setActive(true);
		commonMongoTemplate.save(newKey);
		compoc.setApi(newKey);
		commonMongoTemplate.save(compoc);
		return ApiResponse.buildResult(newKey).meta(newKey.getId() + "-" + newKeyString);
	}

	@ResponseBody
	@RequestMapping(value = "/api/v1/logo", method = { RequestMethod.POST })
	public ApiResponse<CommonFile, Object> uploadFile(
			@RequestParam(name = "file", required = false) MultipartFile file) {
		ContakUserDoc user = sessionBean.domainUser();
		String domainUserId = user.getId();
		CommonFile url = fileStore.upload1(file,
				String.format("%s/docs/%s/logo/%s", AppContextUtil.getTenant(), domainUserId, UUID.randomUUID()),
				file.getOriginalFilename());
		return ApiResponse.buildResults(url).message("Logo uplodaed");
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/hsm/tmpl" }, method = { RequestMethod.POST })
	public ApiResponse<ContakTemplateDoc, Object> hsmTemplate(Model model, @RequestBody ContakTemplateDoc template)
			throws NoSuchAlgorithmException {
		if (ArgUtil.is(template.templateId)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("templateId").codeKey("AccessDenied")
					.description("Template Cannot be modified"));
		}

		if (!ArgUtil.is(template.companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Select Organization"));
		}

		if (!sessionBean.hasAdminAccesTo(template.companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}
		template.templateId = String.format("%s:%s", template.companyId, template.code);
		commonMongoTemplate.save(template);
		return ApiResponse.buildResult(template);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/v1/hsm/tmpl" }, method = { RequestMethod.GET })
	public ApiResponse<ContakTemplateDoc, Object> hsmTemplate(Model model, @RequestParam String companyId)
			throws NoSuchAlgorithmException {

		if (!ArgUtil.is(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Select Organization"));
		}

		if (!sessionBean.hasAdminAccesTo(companyId)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("companyId").codeKey("AccessDenied").description("Access Denied"));
		}
		return ApiResponse.buildResults(
				commonMongoTemplate.collection(ContakTemplateDoc.class).where("companyId", companyId).find().asList());
	}

}
