package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;
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
    private AccountStore accountStore;

    @RequestMapping(value = { "/auth/**", "/app/**" }, method = { RequestMethod.GET })
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

	AccountDoc account = accountStore.findOneByEmail(signupContact.getEmail(), AccountDoc.class);
	if (ArgUtil.is(account)) {
	    ApiResponseUtil.throwException("Email address already in use. Try reset password.");
	}

	AccountMeta keys = new AccountMeta();
	keys.setEmailVerificationCode(UUID.randomUUID().toString());

	account = new AccountDoc();
	account.setContact(signupContact);
	account.setMeta(keys);

	accountStore.save(account);
	sessionService.sendResetMail(account, "tenant-verify-email");

	return ApiResponse.build().message("Verification email sent");
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/set/pass" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> verifyEmail(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody String code, @RequestBody String account,
	    @RequestBody String newpass) throws NoSuchAlgorithmException {
	AccountDoc accountDoc = accountStore.findById(account, AccountDoc.class);
	if (!ArgUtil.is(accountDoc) || accountDoc.getMeta().getEmailVerificationCode().equals(code)) {
	    ApiResponseUtil.throwException("Invalid Link");
	}

	accountDoc.getMeta().setPasswordHash(CryptoUtil.getSHA2Hash(newpass));
	accountDoc.getMeta().setEmailVerificationCode(null);
	sessionService.login(accountDoc, request);
	return ApiResponse.build().message("Password set successfuly");
    }

    @ResponseBody
    @RequestMapping(value = { "/pub/forgot/pass" }, method = { RequestMethod.POST })
    public ApiResponse<Object, Object> forgotPass(Model model, HttpServletRequest request,
	    HttpServletResponse httpServletResponse, @RequestBody String email) throws NoSuchAlgorithmException {

	AccountDoc accountDoc = accountStore.findOneByEmail(email, AccountDoc.class);

	if (ArgUtil.is(accountDoc)) {
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
	    HttpServletResponse httpServletResponse, @RequestBody String email, @RequestBody String password,
	    @RequestBody String newpass) throws NoSuchAlgorithmException {

	AccountDoc accountDoc = accountStore.findOneByEmail(email, AccountDoc.class);

	if (!ArgUtil.is(accountDoc)
		|| !ArgUtil.areEqual(CryptoUtil.getSHA2Hash(newpass), accountDoc.getMeta().getPasswordHash())) {
	    ApiResponseUtil.throwException("Invalid Email or Password");
	}

	sessionService.login(accountDoc, request);
	return ApiResponse.build().message("Login Success");
    }

}
