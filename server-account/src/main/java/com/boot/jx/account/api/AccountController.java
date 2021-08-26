package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccoountAuthService;
import com.boot.jx.account.AccountAuthProvider;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.MessageBox;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccoountAuthService sessionService;

    @Autowired
    private CommonMongoTemplate commonMongoTemplate;

    @Autowired
    private PostManClient postManClient;

    @RequestMapping(value = { "/auth/register/**" }, method = { RequestMethod.GET })
    public String home(Model model, @RequestParam(required = false) String theme) {
	model.addAllAttributes(appCommonConfig.appAttributes());

	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	} else {
	    model.addAttribute("APP_USER", "");
	}

	model.addAttribute("APP", "account");

	return "app-account";
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/register" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> register(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody @Valid SignupContact signupContact) {

	Query query2 = new Query();
	query2.addCriteria(Criteria.where("contact.email").is(signupContact.getEmail()));

	AccountDoc account = CollectionUtil.getOne(commonMongoTemplate.find(query2, AccountDoc.class));
	if (ArgUtil.is(account)) {
	    ApiResponseUtil.throwException("Email address already in use. Try reset password.");
	}

	AccountMeta keys = new AccountMeta();
	keys.setEmailVerificationCode(UUID.randomUUID().toString());

	account = new AccountDoc();
	account.setContact(signupContact);
	account.setMeta(keys);

	commonMongoTemplate.save(account);

	postManClient.send(new MessageBox().push(new Email().to(signupContact.getEmail()).template("new-account").put(
		"logo",
		"https://cdn.jsdelivr.net/gh/mehery-soccom/mehery-web-dist@gh-pages/dist/android-chrome-192x192.png")
		.put("website", "www.mehery.com").put("service", "MeherY")
		.put("link",
			String.format("https://app.mehery.com/account/verify-link?code=%s&account=%s",
				account.getMeta().getEmailVerificationCode(), account.getId()))
		.put("name", account.getContact().getName())));

	return ApiResponse.build().message("Verification email sent");
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/verify/email" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> verifyEmail(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody String code, @RequestBody String account,
	    @RequestBody String newpass) throws NoSuchAlgorithmException {
	AccountDoc accountDoc = commonMongoTemplate.findById(account, AccountDoc.class);
	if (!ArgUtil.is(accountDoc) || accountDoc.getMeta().getEmailVerificationCode().equals(code)) {
	    ApiResponseUtil.throwException("Invalid Link");
	}

	accountDoc.getMeta().setPasswordHash(CryptoUtil.getSHA2Hash(newpass));
	accountDoc.getMeta().setEmailVerificationCode(null);
	sessionService.login(accountDoc, request);
	return ApiResponse.build().message("Email verified");
    }

}
