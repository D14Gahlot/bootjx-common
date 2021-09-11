package com.boot.jx.account.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAdminService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.validation.AlphaNumValidator.ValidAlphaNum;
import com.boot.utils.ArgUtil;

@Controller
public class FrontController {

    @Autowired
    private AppCommonConfig appCommonConfig;

    @Autowired
    private AccountSessionBean adminSessionBean;

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

    @RequestMapping(value = { "/", "/*!swagger-ui.html", "/**!swagger-ui.html", "/front/", "/front/**" },
	    method = { RequestMethod.GET })
    public String front(Model model, @RequestParam(required = false) String theme) {
	model.addAllAttributes(appCommonConfig.appAttributes());

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

    @RequestMapping(value = { "/@{domain}", "/{domain:^.*(?!swagger-ui)}" }, method = { RequestMethod.GET })
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
