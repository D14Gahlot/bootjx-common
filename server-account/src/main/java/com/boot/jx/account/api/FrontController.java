package com.boot.jx.account.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.validation.AlphaNumValidator.ValidAlphaNum;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Controller
public class FrontController {

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccountSessionBean adminSessionBean;

    @Autowired
    private CommonHttpRequest commonHttpRequest;

    @Autowired
    AccountStore accountStore;

    @RequestMapping(value = { "/account", "/account/**" }, method = { RequestMethod.GET })
    public String account(Model model) {
	model.addAllAttributes(appCommonConfig.appAttributes());

	Authentication auth = AccountAdminService.getAuthentication();
	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_ROLE", adminSessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}
	model.addAttribute("APP", "account");
	return "app-account";
    }

    @RequestMapping(value = { "/", "/front/", "/front/**" }, method = { RequestMethod.GET })
    public String front(Model model) {
	model.addAllAttributes(appCommonConfig.appAttributes());

	String domainName = commonHttpRequest.get("domain");
	String domainId = null;

	if (ArgUtil.is(domainName)) {
	    commonHttpRequest.setCookie("domain", domainName);
	    DomainDoc domainDoc = accountStore.findDomainByName(domainName);
	    if (ArgUtil.is(domainDoc)) {
		domainId = domainDoc.getId();
	    }
	}

	if (ArgUtil.is(domainName)) {
	    model.addAttribute("APP_DOMAIN", domainName);
	    model.addAttribute("APP_DOMAIN_ID", domainId);
	    model.addAttribute("DOMAIN", domainName);
	} else {
	    model.addAttribute("APP_DOMAIN", Constants.BLANK);
	    model.addAttribute("DOMAIN", Constants.BLANK);
	}

	Authentication auth = AccountAdminService.getAuthentication();
	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_ROLE", adminSessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}

	model.addAttribute("APP", "front");

	return "app-front";
    }

    @RequestMapping(value = { "/@{domain}", "/{domain:^.*(?!swagger-ui.html)}" }, method = { RequestMethod.GET })
    public String domain(Model model, @PathVariable @ValidAlphaNum String domain) {
	model.addAllAttributes(appCommonConfig.appAttributes());

	Authentication auth = AccountAdminService.getAuthentication();
	if (ArgUtil.is(auth)) {
	    model.addAttribute("APP_USER", auth.getName());
	    model.addAttribute("APP_USER_ROLE", adminSessionBean.getRole());
	} else {
	    model.addAttribute("APP_USER", "");
	    model.addAttribute("APP_USER_ROLE", "GUEST");
	}

	model.addAttribute("DOMAIN", domain);
	model.addAttribute("APP", "front");

	return "app-front";
    }
}
