package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.boot.jx.account.doc.SignupContact;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.common.dto.UserLoginToken;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;

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
    private AccountSessionBean adminSessionBean;

    @Autowired
    private AccountStore accountStore;

    @Autowired
    private PMEnvironment env;

    @Autowired
    private EmpAuthService empAuthService;

    @RequestMapping(value = { "", "/", "/**", "/auth/**", "/app/**" }, method = { RequestMethod.GET })
    public String home(Model model, @RequestParam(required = false) String theme) {
	String tnt = AppContextUtil.getTenant();
	if (!tnt.equals("app")) {
	    return "redirect:" + String.format("https://app.%s%s", env.keyEntry("mry.prop.service.domain").asString(),
		    commonHttpRequest.getRequestURI());
	}

	model.addAllAttributes(appCommonConfig.appAttributes());
	Authentication auth = AccountAuthService.getAuthentication();
	if (ArgUtil.is(auth) && ArgUtil.is(adminSessionBean.domainUser())) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_NAME", adminSessionBean.domainUser().getContact().getName());
	    model.addAttribute("APP_USER_ROLE", adminSessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_NAME", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}

	model.addAttribute("APP", "partner");

	return "app-partner";
    }

    @RequestMapping(value = { "/app/goto/{domain}/{panel}" }, method = { RequestMethod.GET })
    public String gotopanel(Model model, @PathVariable String domain, @PathVariable String panel)
	    throws NoSuchAlgorithmException {
	String tnt = AppContextUtil.getTenant();
	if (!tnt.equals("app")) {
	    return "redirect:" + String.format("https://app.%s/%s/auth/direct",
		    env.keyEntry("mry.prop.service.domain").asString(), commonHttpRequest.getRequestURI());
	}

	model.addAllAttributes(appCommonConfig.appAttributes());
	model.addAttribute("FORM_URL", String.format("https://%s.%s/%s/auth/direct", domain,
		env.keyEntry("mry.prop.service.domain").asString(), panel));

	if (ArgUtil.is(adminSessionBean.domainUser())) {
	    for (DomainDoc domainDoc : adminSessionBean.domainUser().getDomains()) {
		UserLoginToken userLoginToken = empAuthService.createSuperLoginToken("superadmin", domain,
			domainDoc.getId(), "admin");
		if (ArgUtil.isEqual(domainDoc.getDomain(), domain)) {
		    model.addAttribute("DOMAIN_USER", userLoginToken.getDomainUser());
		    model.addAttribute("DOMAIN_NAME", userLoginToken.getDomainName());
		    model.addAttribute("DOMAIN_ID", userLoginToken.getDomainId());
		    model.addAttribute("DOMAIN_TOKEN", userLoginToken.getDomainToken());
		}
	    }
	}

	return "app-goto";
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/register" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> register(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody @Valid SignupContact signupContact) {

	BusinessUserDoc account = accountStore.findOneByEmail(signupContact.getEmail(), BusinessUserDoc.class);
	if (ArgUtil.is(account)) {
	    ApiResponseUtil.throwDuplicateInputException("Email address already in use. Try reset password.",
		    new ApiFieldError().obzect("signupContact").field("email").codeKey("ValidEmailDuplicate")
			    .description("Email address already in use."));
	}

	AccountMeta keys = new AccountMeta();
	keys.setEmailVerificationCode(UUID.randomUUID().toString());

	account = new BusinessUserDoc();
	account.setContact(signupContact);
	account.setMeta(keys);

	accountStore.save(account);
	sessionService.sendResetMail(account, "tenant-verify-email");
	sessionService.sendMailToSalesTeam(account, "new-customer-register-email");
//Customer registers on our website
	return ApiResponse.build().message("Verification email sent");
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

	BusinessUserDoc accountDoc = accountStore.findOneByEmail(email, BusinessUserDoc.class);

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

	BusinessUserDoc accountDoc = accountStore.findOneByEmail(email, BusinessUserDoc.class);

	if (!ArgUtil.is(accountDoc)
		|| !ArgUtil.areEqual(CryptoUtil.getSHA2Hash(newpass), accountDoc.getMeta().getPassword())) {
	    ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("password")
		    .codeKey("ValidCredentials").description("Invalid Email or Password"));
	}

	sessionService.login(accountDoc, request);
	return ApiResponse.build().message("Login Success");
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/domain" }, method = { RequestMethod.GET })
    public ApiResponse<DomainDoc, Object> getDomain(@RequestParam String domain) {
	DomainDoc domainDoc = accountStore.findDomainByName(domain);
	return ApiResponse.buildResult(domainDoc);
    }

    @ResponseBody
    @RequestMapping(value = { "/api/domain" }, method = { RequestMethod.GET })
    public ApiResponse<DomainDoc, Object> getDomain() {
	BusinessUserDoc domainUser = adminSessionBean.domainUser();

	if (!ArgUtil.is(domainUser)) {
	    ApiResponseUtil.throwException("Access Denied");
	}

	DomainDoc domainDoc = CollectionUtil.first(domainUser.getDomains());

	if (!ArgUtil.is(domainDoc)) {
	    domainDoc = new DomainDoc();
	}

	if (!ArgUtil.is(domainDoc.getCompany())) {
	    domainDoc.setCompany(new CompanyDoc());
	}
	if (!ArgUtil.is(domainDoc.getCompany().getConactEmail())) {
	    domainDoc.getCompany().setConactEmail(domainUser.getContact().getEmail());
	}

	if (!ArgUtil.is(domainDoc.getCompany().getConactPhone())) {
	    domainDoc.getCompany().setConactPhone(domainUser.getContact().getPhone());
	}

	if (!ArgUtil.is(domainDoc.getCompany().getBusinessName())) {
	    domainDoc.getCompany().setBusinessName(domainUser.getContact().getCompany());
	}

	if (!ArgUtil.is(domainDoc.getCompany().getConactCountry())) {
	    domainDoc.getCompany().setConactCountry(domainUser.getContact().getCountry());
	}

	return ApiResponse.buildResult(domainDoc);
    }

    @ResponseBody
    @RequestMapping(value = { "/api/domain/exists" }, method = { RequestMethod.GET })
    public ApiResponse<Object, Object> sisExists(@RequestParam @Valid String domain) throws NoSuchAlgorithmException {
	DomainDoc domainDoc = accountStore.findDomainByName(domain);
	if (ArgUtil.is(domainDoc)) {
	    domainDoc = new DomainDoc();
	    domainDoc.setDomain(domainDoc.getDomain());
	    return ApiResponse.buildMeta(domainDoc.getDomain());
	}
	return ApiResponse.buildMeta(null).statusKey("400");
    }

    @ResponseBody
    @RequestMapping(value = { "/api/domain/check" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> checkDomain(@RequestParam @Valid String domain) throws NoSuchAlgorithmException {

	DomainDoc domainDoc = accountStore.findDomainByName(domain);

	if (ArgUtil.is(domainDoc)) {
	    ApiResponseUtil.throwDuplicateInputException("Domain already taken. Try different", new ApiFieldError()
		    .field("domain").codeKey("ValidDomainDuplicate").description("Domain already taken."));
	}

	domainDoc = new DomainDoc();
	domainDoc.setDomain(domainDoc.getDomain());
	return ApiResponse.build().message("Domain available");
    }

    @ResponseBody
    @RequestMapping(value = { "/api/domain" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> createDomain(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody @Valid DomainDoc domain,
	    @RequestParam(required = false) boolean create) throws NoSuchAlgorithmException {

	BusinessUserDoc domainUser = adminSessionBean.domainUser();

	if (ArgUtil.is(domainUser.getDomains())) {
	    DomainDoc domainDoc = CollectionUtil.first(domainUser.getDomains());
	    if (!domainDoc.getDomain().equals(domain.getDomain())) {
		ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidDomainMultiple")
			.description("Domain Change Not Allowed"));
	    }
	    domainDoc.setCompany(domain.getCompany());
	    domainDoc.setSocial(domain.getSocial());
	    accountStore.save(domainDoc);
	    accountStore.save(domainUser);

	    return ApiResponse.build().message("Details updated");

	} else {
	    checkDomain(domain.getDomain());

	    DomainDoc domainDoc = new DomainDoc();
	    domainDoc.setDomain(domain.getDomain());
	    domainDoc.setCompany(domain.getCompany());
	    domainDoc.setSocial(domain.getSocial());

	    accountStore.save(domainDoc);

	    domainUser.domains().add(domainDoc);
	    accountStore.save(domainUser);

	    return ApiResponse.build().message("Domain created");
	}

    }

    @Autowired
    AWSFileStore fileStore;

    @ResponseBody
    @RequestMapping(value = "/api/domain/logo", method = { RequestMethod.POST })
    public ApiResponse<String, Object> upploadDomainLogo(
	    @RequestParam(name = "file", required = false) MultipartFile file) {
	BusinessUserDoc domainUser = adminSessionBean.domainUser();
	String domainUserId = domainUser.getId();
	String url = fileStore.upload1(file,
		String.format("%s/docs/%s/logo/%s", AppContextUtil.getTenant(), domainUserId, UUID.randomUUID()),
		file.getOriginalFilename()).getUrl();
	return ApiResponse.buildResults(url).message("Logo uplodaed");
    }

}
