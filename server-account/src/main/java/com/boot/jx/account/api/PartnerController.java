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

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountMeta;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.CompanyDoc;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.account.doc.DomainUserDoc;
import com.boot.jx.account.doc.SignupContact;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.CryptoUtil.HashBuilder;

@Controller
@RequestMapping("/partner")
public class PartnerController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccountAdminService sessionService;

    @Autowired
    private AccountSessionBean adminSessionBean;

    @Autowired
    private AccountStore accountStore;

    @Autowired
    private PMEnvironment env;

    @RequestMapping(value = { "", "/", "/**", "/auth/**", "/app/**" }, method = { RequestMethod.GET })
    public String home(Model model, @RequestParam(required = false) String theme) {
	String tnt = AppContextUtil.getTenant();
	if (!tnt.equals("app")) {
	    return "redirect:" + appConfig.prop("mry.app.url") + commonHttpRequest.getRequestURI();
	}

	model.addAllAttributes(appCommonConfig.appAttributes());
	Authentication auth = AccountAdminService.getAuthentication();
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
    public String gotopanel(Model model, @PathVariable String domain, @PathVariable String panel) {
	String tnt = AppContextUtil.getTenant();
	if (!tnt.equals("app")) {
	    return "redirect:" + appConfig.prop("mry.app.url") + commonHttpRequest.getRequestURI();
	}

	model.addAllAttributes(appCommonConfig.appAttributes());
	model.addAttribute("FORM_URL", String.format("https://%s.%s/%s/auth/direct", domain,
		appConfig.prop("mry.prop.service.domain"), panel));

	String secret = appConfig.prop("mry.app.login.secret");
	if (ArgUtil.is(adminSessionBean.domainUser())) {
	    for (DomainDoc domainDoc : adminSessionBean.domainUser().getDomains()) {
		if (ArgUtil.isEqual(domainDoc.getDomain(), domain)) {
		    HashBuilder builder = new HashBuilder().interval(10000).secret(secret)
			    .message(String.format("%s@%s:%s", "superadmin", domain, domainDoc.getId()));
		    model.addAttribute("DOMAIN_USER", "superadmin");
		    model.addAttribute("DOMAIN_NAME", domainDoc.getDomain());
		    model.addAttribute("DOMAIN_ID", domainDoc.getId());
		    model.addAttribute("DOMAIN_TOKEN", builder.toHMAC().output());
		}
	    }
	}

	return "app-goto";
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/register" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> register(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody @Valid SignupContact signupContact) {

	DomainUserDoc account = accountStore.findOneByEmail(signupContact.getEmail(), DomainUserDoc.class);
	if (ArgUtil.is(account)) {
	    ApiResponseUtil.throwDuplicateInputException("Email address already in use. Try reset password.",
		    new ApiFieldError().obzect("signupContact").field("email").codeKey("ValidEmailDuplicate")
			    .description("Email address already in use."));
	}

	AccountMeta keys = new AccountMeta();
	keys.setEmailVerificationCode(UUID.randomUUID().toString());

	account = new DomainUserDoc();
	account.setContact(signupContact);
	account.setMeta(keys);

	accountStore.save(account);
	sessionService.sendResetMail(account, "tenant-verify-email");

	return ApiResponse.build().message("Verification email sent");
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/set/pass" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> verifyEmail(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestParam String code, @RequestParam String account,
	    @RequestParam String newpass) throws NoSuchAlgorithmException {

	DomainUserDoc accountDoc = accountStore.findById(account, DomainUserDoc.class);
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

	DomainUserDoc accountDoc = accountStore.findOneByEmail(email, DomainUserDoc.class);

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

	DomainUserDoc accountDoc = accountStore.findOneByEmail(email, DomainUserDoc.class);

	if (!ArgUtil.is(accountDoc)
		|| !ArgUtil.areEqual(CryptoUtil.getSHA2Hash(newpass), accountDoc.getMeta().getPassword())) {
	    ApiResponseUtil.throwInputException(new ApiFieldError().obzect("login").field("password")
		    .codeKey("ValidCredentials").description("Invalid Email or Password"));
	}

	sessionService.login(accountDoc, request);
	return ApiResponse.build().message("Login Success");
    }

    @ResponseBody
    @RequestMapping(value = { "/api/domain" }, method = { RequestMethod.GET })
    public ApiResponse<DomainDoc, Object> getDomain() {
	DomainUserDoc domainUser = adminSessionBean.domainUser();

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

	if (!ArgUtil.is(domainDoc.getCompany().getBusinessName())) {
	    domainDoc.getCompany().setBusinessName(domainUser.getContact().getCompany());
	}

	if (!ArgUtil.is(domainDoc.getCompany().getConactCountry())) {
	    domainDoc.getCompany().setConactCountry(domainUser.getContact().getCountry());
	}

	return ApiResponse.buildResult(domainDoc);
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

	DomainUserDoc domainUser = adminSessionBean.domainUser();

	if (ArgUtil.is(domainUser.getDomains())) {
	    DomainDoc domaonDoc = CollectionUtil.first(domainUser.getDomains());
	    if (!domaonDoc.getDomain().equals(domain.getDomain())) {
		ApiResponseUtil.throwInputException(new ApiFieldError().field("domain").codeKey("ValidDomainMultiple")
			.description("Domain Change Not Allowed"));
	    }
	    domaonDoc.setCompany(domain.getCompany());
	    accountStore.save(domaonDoc);
	    accountStore.save(domainUser);

	    return ApiResponse.build().message("Details updated");

	} else {
	    checkDomain(domain.getDomain());

	    DomainDoc domainDoc = new DomainDoc();
	    domainDoc.setDomain(domain.getDomain());
	    domainDoc.setCompany(domain.getCompany());
	    accountStore.save(domainDoc);

	    domainUser.domains().add(domainDoc);
	    accountStore.save(domainUser);

	    return ApiResponse.build().message("Domain created");
	}

    }

}
