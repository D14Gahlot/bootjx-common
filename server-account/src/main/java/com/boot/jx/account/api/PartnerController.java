package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountMeta;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.BusinessUserDoc;
import com.boot.jx.account.doc.CompanyDoc;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.doc.DomainLicenseDoc;
import com.boot.jx.account.doc.SignupContact;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.common.config.ConfigConstants;
import com.boot.jx.common.dto.UserAuthToken;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.common.models.AppAuthModels.ACCESS_RULES;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;

@Controller
@RequestMapping("/partner")
public class PartnerController {

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private AccountAuthService sessionService;

	@Autowired
	private AccountSessionBean userSessionBean;

	@Autowired
	private AccountStore accountStore;

	@Autowired
	private PMEnvironment env;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@Autowired
	private EmpAuthService empAuthService;

	@RequestMapping(value = { "", "/", "/**", "/auth/**", "/app/**" }, method = { RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		String tnt = AppContextUtil.getTenant();
		if (!Tenants.isDefault(tnt)) {
			return pmCommonConfig.mainDomainRedirect();
		}

		model.addAllAttributes(appCommonConfig.appAttributes());
		Authentication auth = AccountAuthService.getAuthentication();
		if (ArgUtil.is(auth) && ArgUtil.is(userSessionBean.domainUser())) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_NAME", userSessionBean.domainUser().contact().getName());
			model.addAttribute("APP_USER_ROLE", JsonUtil.toJson(userSessionBean.role()));
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_NAME", "");
			model.addAttribute("APP_USER_ROLE", "['GUEST']");
		}

		model.addAttribute("APP", "partner");

		return "app-partner";
	}

	@RequestMapping(value = { "/app/goto/{domain}/{panel}" }, method = { RequestMethod.GET })
	public String gotopanel(Model model, @PathVariable String domain, @PathVariable String panel,
			@RequestParam(required = false) String server) throws NoSuchAlgorithmException {
		String tnt = AppContextUtil.getTenant();

		if (!Tenants.isDefault(tnt)) {
			return pmCommonConfig.mainDomainRedirect(commonHttpRequest.getRequestURI() + "/auth/direct");
		}

		DomainDoc domainDoc = accountStore.findDomainByName(domain);

		model.addAllAttributes(appCommonConfig.appAttributes());
		model.addAttribute("FORM_URL", String.format("https://%s.%s/%s/auth/direct", domain,
				// "local.com"
				ArgUtil.anyOf(server, domainDoc.getServer(),
						env.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString()),
				panel));

		if (userSessionBean.hasAdminAccesTo(domain)) {
			UserAuthToken userLoginToken = empAuthService.createSuperLoginToken("superadmin",
					userSessionBean.domainUser().contact().getEmail(), domain, domainDoc.getId(), "admin");
			model.addAttribute("DOMAIN_USER", userLoginToken.getDomainUser());
			model.addAttribute("DOMAIN_USER_EMAIL", userLoginToken.getDomainUserEmail());
			model.addAttribute("DOMAIN_NAME", userLoginToken.getDomainName());
			model.addAttribute("DOMAIN_ID", userLoginToken.getDomainId());
			model.addAttribute("DOMAIN_TOKEN", userLoginToken.getDomainToken());
			model.addAttribute("DOMAIN_TOKEN_VALID", Constants.BLANK);
		}
		return "app-goto";
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/register" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> register(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestBody @Valid SignupContact signupContact) {
		createUser(signupContact);
//Customer registers on our website
		return ApiResponse.build().message("Verification email sent");
	}

	private BusinessUserDoc createUser(SignupContact signupContact) {
		BusinessUserDoc account = accountStore.findUserByEmail(signupContact.getEmail());
		if (ArgUtil.is(account)) {
			ApiResponseUtil.throwDuplicateInputException("Email address already in use. Try reset password.",
					new ApiFieldError().obzect("signupContact").field("email").codeKey("ValidEmailDuplicate")
							.description("Email address already in use."));
		}
		
		if(signupContact.getProduct()==null || signupContact.getProduct().isEmpty()) {
			ApiResponseUtil.throwDuplicateInputException("Select at least one product you are interested in.",
					new ApiFieldError().obzect("signupContact").field("product").codeKey("ValidProduct")
							.description("Select at least one product you are interested in."));
		}

		AccountMeta keys = new AccountMeta();
		keys.setEmailVerificationCode(UUID.randomUUID().toString());

		account = new BusinessUserDoc();
		account.setContact(signupContact);
		account.setMeta(keys);

		accountStore.save(account);
		sessionService.sendResetMail(account, "tenant-verify-email");
		sessionService.sendMailToSalesTeam(account, "new-customer-register-email");
		return account;
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/set/pass" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> verifyEmail(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestParam String code, @RequestParam String account,
			@RequestParam String newpass) throws NoSuchAlgorithmException {

		BusinessUserDoc accountDoc = accountStore.findById(account, BusinessUserDoc.class);
		if (!ArgUtil.is(accountDoc) || !ArgUtil.is(accountDoc.getMeta())
				|| !ArgUtil.is(accountDoc.getMeta().getEmailVerificationCode())
				|| !accountDoc.getMeta().getEmailVerificationCode().equals(code)) {
			ApiResponseUtil.throwException("Invalid Link");
		}

		accountDoc.getMeta().setEmailVerificationCode(null);
		accountDoc.getMeta().setEmailVerified(true);
		accountDoc.getMeta().setPassword(CryptoUtil.getSHA2Hash(newpass));

		sessionService.login(accountDoc, request);

		accountStore.save(accountDoc);
		return ApiResponse.build().message("Password set successfuly");
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/forgot/pass" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> forgotPass(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestParam String email) throws NoSuchAlgorithmException {

		BusinessUserDoc accountDoc = accountStore.findUserByEmail(email);

		if (!ArgUtil.is(accountDoc)) {
			ApiResponseUtil.throwException("Email not registered");
		}

		accountDoc.getMeta().setEmailVerificationCode(UUID.randomUUID().toString());
		accountStore.save(accountDoc);
		sessionService.sendResetMail(accountDoc, "tenant-reset-pass");

		return ApiResponse.build().message("Password Reset Email Sent");
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/login" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> login(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestParam String email, @RequestParam String password,
			@RequestParam String newpass) throws NoSuchAlgorithmException {

		BusinessUserDoc accountDoc = accountStore.findUserByEmail(email);

		if (!ArgUtil.is(accountDoc)
				|| !ArgUtil.areEqual(CryptoUtil.getSHA2Hash(newpass), accountDoc.getMeta().getPassword())) {
			ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("password")
					.codeKey("ValidCredentials").description("Invalid Email or Password"));
		}

		sessionService.login(accountDoc, request);
		return ApiResponse.build().message("Login Success");
	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain/exists", "/pub/domain/exists" }, method = { RequestMethod.GET })
	public ApiResponse<Object, Object> sisExists(@RequestParam @Valid String domain) throws NoSuchAlgorithmException {
		AppContextUtil.setTenant(Tenants.getDefault());
		DomainDoc domainDoc = accountStore.findDomainByName(domain);
		if (ArgUtil.is(domainDoc)) {
			return ApiResponse.buildMeta(domainDoc.getDomain());
		}
		return ApiResponse.buildMeta(null).statusKey("400");
	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain/check", "/pub/domain/check" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> checkDomain(@RequestParam @Valid String domain) throws NoSuchAlgorithmException {

		if (!accountStore.isValidDomainName(domain)) {
			ApiResponseUtil.throwDuplicateInputException("Domain already taken. Try different", new ApiFieldError()
					.field("domain").codeKey("ValidDomainDuplicate").description("Domain already taken."));
		}

		DomainDoc domainDoc = accountStore.findDomainByName(domain);

		if (ArgUtil.is(domainDoc)) {
			ApiResponseUtil.throwDuplicateInputException("Domain already taken. Try different", new ApiFieldError()
					.field("domain").codeKey("ValidDomainDuplicate").description("Domain already taken."));
		}

		domainDoc = new DomainDoc();
		domainDoc.setDomain(domainDoc.getDomain());
		return ApiResponse.build().message("Domain available");
	}

	@ApiRequest(rules = AppAuthModels.ACCESS_RULES.ONLY_DUPERUSER)
	@ResponseBody
	@RequestMapping(value = { "/api/users" }, method = { RequestMethod.GET })
	public ApiResponse<Map<String, Object>, Object> getDomainUsers() {
		List<BusinessUserDoc> domainUsers = accountStore
				.find(CommonMongoQueryBuilder.collection(BusinessUserDoc.class).skipDBRef());
		return ApiResponse.buildResults(domainUsers.stream().map(u -> u.toDTO()).collect(Collectors.toList()));
	}

	@ResponseBody
	@RequestMapping(value = { "/pub/domain" }, method = { RequestMethod.GET })
	public ApiResponse<DomainDoc, Object> getDomain(@RequestParam String domain) {
		DomainDoc domainDoc = accountStore.findDomainByName(domain);
		return ApiResponse.buildResult(domainDoc);
	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain" }, method = { RequestMethod.GET })
	public ApiResponse<DomainDoc, Object> getDomains(@RequestParam(required = false) String user,
			@RequestParam(required = false) String domain) {
		BusinessUserDoc currentUser = userSessionBean.domainUser();

		if (!ArgUtil.is(currentUser)) {
			ApiResponseUtil.throwException("Access Denied");
		}

		BusinessUserDoc domainUser = currentUser;
		Collection<DomainDoc> domainDocs = domainUser.getDomains();

		if (userSessionBean.role().contains(PMConstants.USER_ROLE.DUPER_USER)) {
			if (ArgUtil.is(user)) {
				domainUser = accountStore.findUserByEmail(user);
				domainDocs = domainUser.getDomains();
			} else if (ArgUtil.is(domain)) {
				domainDocs = accountStore.find(CommonMongoQueryBuilder.collection(DomainDoc.class)
						.where(Criteria.where("domain").regex(domain, "i")));
			}
		}

		ApiResponse<DomainDoc, Object> resp = ApiResponse.instance(DomainDoc.class);

		if (ArgUtil.is(domainDocs)) {
			for (DomainDoc domainDoc : domainDocs) { // DomainDoc domainDoc =
				if (ArgUtil.isEmpty(domainDoc.getPrimaryOwner())) {
					domainDoc.setPrimaryOwner(domainUser.contact().getEmail());
					accountStore.save(domainDoc);
				}
				domainDoc = defaultDomain(domainUser, domainDoc);
				resp.addResult(domainDoc);
			}
		} else {
			resp.addResult(defaultDomain(domainUser, new DomainDoc()));
		}

		return resp;
	}

	private DomainDoc defaultDomain(BusinessUserDoc domainUser, DomainDoc domainDoc) {
		if (!ArgUtil.is(domainDoc)) {
			domainDoc = new DomainDoc();
		}

		if (!ArgUtil.is(domainDoc.getCompany())) {
			domainDoc.setCompany(new CompanyDoc());
		}
		if (!ArgUtil.is(domainDoc.getCompany().getEmail())) {
			domainDoc.getCompany().setEmail(new PBEmail().email(domainUser.contact().getEmail()));
		}

		if (!ArgUtil.is(domainDoc.getCompany().getPhone())) {
			domainDoc.getCompany().setPhone(new PBPhone().phone(domainUser.contact().getPhone()));
		}

		if (!ArgUtil.is(domainDoc.getCompany().getBusinessName())) {
			domainDoc.getCompany().setBusinessName(domainUser.contact().getCompany());
		}

		if (!ArgUtil.is(domainDoc.getCompany().getAddress())) {
			domainDoc.getCompany().setAddress(new PBAddress().country(domainUser.contact().getCountry()));
		}
		return domainDoc;
	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> createDomain(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestBody @Valid DomainDoc domain,
			@RequestParam(required = false) boolean create) throws NoSuchAlgorithmException {

		BusinessUserDoc domainUser = userSessionBean.domainUser();

		if (ArgUtil.is(domainUser.getDomains()) && ArgUtil.is(domain.getId())) {
			Optional<DomainDoc> domaiNational = sessionService.getDomainAsOwner(domain.getDomain());
			if (!domaiNational.isPresent() || !domaiNational.get().getDomain().equals(domain.getDomain())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidDomainMultiple")
						.description("Domain Change Not Allowed"));
			}
			domaiNational.get().setCompany(domain.getCompany());
			domaiNational.get().setSocial(domain.getSocial());
			accountStore.save(domaiNational.get());
			accountStore.save(domainUser);
			return ApiResponse.build().message("Details updated");
		}
		checkDomain(domain.getDomain());

		DomainDoc domainDoc = new DomainDoc();
		domainDoc.setDomain(domain.getDomain());
		domainDoc.setCompany(domain.getCompany());
		domainDoc.setSocial(domain.getSocial());
		domainDoc.setServer(env.keyEntry(ConfigConstants.APP_KEY.PROP_SERVICE_SERVER).asString());
		domainDoc.setTimeZoneOffSet(domain.getTimeZoneOffSet());
		accountStore.save(domainDoc);

		domainUser.domains().add(domainDoc);
		accountStore.save(domainUser);

		return ApiResponse.build().message("Domain created");

	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain/user" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> domainUser(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestParam String email, @RequestParam String domain,
			@RequestParam(required = false, defaultValue = "false") boolean remove,
			@RequestParam(required = false) String name, @RequestParam(required = false) String company

	) throws NoSuchAlgorithmException {

		BusinessUserDoc domainUser = userSessionBean.domainUser();

		Optional<DomainDoc> domaiNational = Optional.empty();
		if (userSessionBean.role().contains(PMConstants.USER_ROLE.DUPER_USER)) {
			DomainDoc domainDoc = accountStore.findDomainByName(domain);
			if (ArgUtil.is(domainDoc)) {
				domaiNational = Optional.of(domainDoc);
			}
		} else {
			if (!ArgUtil.is(domainUser.getDomains())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidDomainNotFound")
						.description("Domain Not found"));
			}
			domaiNational = domainUser.getDomains().stream().filter(d -> d.getDomain().equals(domain)).findFirst();
		}

		if (!domaiNational.isPresent() || !domaiNational.get().getDomain().equals(domain)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("domain").codeKey("ValidDomainNotFound").description("Domain Not found"));
		}

		BusinessUserDoc account = accountStore.findUserByEmail(email);

		if (!ArgUtil.is(account) && ArgUtil.is(email)) {
			SignupContact newUser = new SignupContact();
			newUser.setEmail(email);
			newUser.setCountry(domainUser.contact().getCountry());
			newUser.setName(name);
			newUser.setCompany(company);
			account = createUser(newUser);
		}

		if (!ArgUtil.is(account)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("email").codeKey("ValidAccountNotFound")
					.description("No Account with email."));
		}

		if (remove) {
			if (ArgUtil.isEqual(domaiNational.get().getPrimaryOwner(), email)) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("email").codeKey("ValidAccountNotFound")
						.description("Cannot Remove Primary Owner"));
			}
			List<BusinessUserDoc> domainUsers = accountStore.findAllUsersByDomainId(domaiNational.get().getId());
			if (domainUsers.size() == 1) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("email").codeKey("ValidAccountNotFound")
						.description("Cannot Remove Primary Owner"));
			}
			Optional<DomainDoc> domainFound = account.domains().stream().filter(d -> d.getDomain().equals(domain))
					.findFirst();
			account.domains().remove(domainFound.get());

			accountStore.save(account);
			return ApiResponse.build().message("Owner Removed");
		} else {
			Optional<DomainDoc> domainFound = account.domains().stream().filter(d -> d.getDomain().equals(domain))
					.findFirst();
			if (domainFound.isPresent()) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("email").codeKey("ValidAccountNotFound")
						.description("Already Mapped"));
			}
			account.domains().add(domaiNational.get());
			accountStore.save(account);
			return ApiResponse.build().message("Owner Mapped");
		}

	}

	@ResponseBody
	@RequestMapping(value = { "/api/domain/users" }, method = { RequestMethod.GET })
	public ApiResponse<Object, Object> domainUserGet(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestParam String domain) throws NoSuchAlgorithmException {
		BusinessUserDoc domainUser = userSessionBean.domainUser();

		Optional<DomainDoc> domaiNational = Optional.empty();
		if (userSessionBean.role().contains(PMConstants.USER_ROLE.DUPER_USER)) {
			DomainDoc domainDoc = accountStore.findDomainByName(domain);
			if (ArgUtil.is(domainDoc)) {
				domaiNational = Optional.of(domainDoc);
			}
		} else {
			if (!ArgUtil.is(domainUser.getDomains())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidDomainNotFound")
						.description("Domain Not found"));
			}
			domaiNational = domainUser.getDomains().stream().filter(d -> d.getDomain().equals(domain)).findFirst();
		}

		if (!domaiNational.isPresent() || !domaiNational.get().getDomain().equals(domain)) {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("domain").codeKey("ValidDomainNotFound").description("Domain Not found"));
		}
		List<BusinessUserDoc> domainUsers = accountStore.findAllUsersByDomainId(domaiNational.get().getId());

		return ApiResponse.build().results(domainUsers.stream().map(u -> u.toDTO()).collect(Collectors.toList()));
	}

	@Autowired
	AWSFileStore fileStore;

	@ResponseBody
	@RequestMapping(value = "/api/domain/logo", method = { RequestMethod.POST })
	public ApiResponse<String, Object> upploadDomainLogo(
			@RequestParam(name = "file", required = false) MultipartFile file) {
		BusinessUserDoc domainUser = userSessionBean.domainUser();
		String domainUserId = domainUser.getId();
		String url = fileStore.upload1(file,
				String.format("%s/docs/%s/logo/%s", AppContextUtil.getTenant(), domainUserId, UUID.randomUUID()),
				file.getOriginalFilename()).getUrl();
		return ApiResponse.buildResults(url).message("Logo uplodaed");
	}

	/** Domain License creation/Updataion/View **/

	@ResponseBody
	@RequestMapping(value = { "/api/domain/license", "/pub/domain/license" }, method = { RequestMethod.POST })
	public ApiResponse<Object, Object> createDomainLicense(Model model, HttpServletRequest request,
			HttpServletResponse httpServletResponse, @RequestBody @Valid DomainLicenseDoc domainLicense,
			@RequestParam(required = false) boolean create) throws NoSuchAlgorithmException {

		BusinessUserDoc domainUser = userSessionBean.domainUser();
		DomainDoc domainDoc = accountStore.findDomainByName(domainLicense.getDomain());

		if (!ArgUtil.is(domainDoc)) {
			ApiResponseUtil.throwException("Invalid Domain.");
		}

		DomainLicenseDoc domainLicDoc = accountStore.findDomainLicenseByName(domainLicense.getDomain());
		if (!ArgUtil.is(domainLicDoc)) {
			domainLicDoc = new DomainLicenseDoc();
			domainLicDoc.setDomain(domainLicense.getDomain());
			domainLicDoc.setFrequency(domainLicense.getFrequency());
			domainLicDoc.setLicenseName(domainLicense.getLicenseName());
			domainLicDoc.setLicenseAggrementStamp(System.currentTimeMillis());
			domainLicDoc.setCreatedStamp(System.currentTimeMillis());
			domainLicDoc.setIsActive(true);
			accountStore.save(domainLicDoc);
			// domainUser.domainLicense().add(domainLicDoc);
			// accountStore.save(domainUser);
			return ApiResponse.build().message("Domain license created");
		} else {

			domainLicDoc.setDomain(domainLicense.getDomain());
			domainLicDoc.setFrequency(domainLicense.getFrequency());
			domainLicDoc.setLicenseName(domainLicense.getLicenseName());
			domainLicDoc.setModifiedStamp(System.currentTimeMillis());
			domainLicDoc.setIsActive(true);
			accountStore.save(domainLicDoc);
			return ApiResponse.build().message("Details updated");
		}

	}

	@ResponseBody
	@RequestMapping(value = { "/pub/domainLicense" }, method = { RequestMethod.GET })
	public ApiResponse<DomainLicenseDoc, Object> getDomainLicense(@RequestParam String domain) {
		DomainLicenseDoc domainLicDoc = accountStore.findDomainLicenseByName(domain);
		return ApiResponse.buildResult(domainLicDoc);
	}

}
