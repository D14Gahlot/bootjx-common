package com.boot.jx.account.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.account.AccountSessionBean;
import com.boot.jx.account.doc.AccountStore;
import com.boot.jx.account.doc.DomainDoc;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.scope.tnt.Tenants;
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
	private AccountStore accountStore;

	@Autowired
	private PMCommonConfig pmCommonConfig;

	@RequestMapping(value = { "/account", "/account/**" }, method = { RequestMethod.GET })
	public String account(Model model) {
		model.addAllAttributes(appCommonConfig.appAttributes());

		Authentication auth = AccountAuthService.getAuthentication();
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
		if (!pmCommonConfig.isValidDomain()) {
			return pmCommonConfig.mainDomainRedirect();
		}
		String domainName = commonHttpRequest.get("domain");
		return domainProfile(model, domainName, true, "front");
	}

	@RequestMapping(value = { "/content/", "/content/**" }, method = { RequestMethod.GET })
	public String content(Model model) {
		String domainName = commonHttpRequest.get("domain");
		return domainProfile(model, domainName, true, "content");
	}

	private String domainProfile(Model model, String domainName, boolean setDefault, String app) {
		model.addAllAttributes(appCommonConfig.appAttributes());
		String tnt = AppContextUtil.getTenant();
		String domainId = null;

		if (!Tenants.isDefault(tnt)) {
			domainName = tnt;
			AppContextUtil.setTenant(Tenants.getDefault());
		}

		if (ArgUtil.is(domainName)) {
			if (setDefault) {
				commonHttpRequest.setCookie("domain", domainName);
			}
			DomainDoc domainDoc = accountStore.findDomainByName(domainName);
			if (ArgUtil.is(domainDoc)) {
				domainId = domainDoc.getId();
			}
		}

		if (ArgUtil.is(domainName)) {
			model.addAttribute("APP_DOMAIN", domainName);
			model.addAttribute("APP_DOMAIN_ID", domainId);
		} else {
			model.addAttribute("APP_DOMAIN", Constants.BLANK);
		}

		String appView = ArgUtil.parseAsString(commonHttpRequest.getCookie("APP_VIEW"), "DEFAULT");
		commonHttpRequest.setCookie("APP_VIEW", appView);

		model.addAttribute("APP_VIEW", appView);

		Authentication auth = AccountAuthService.getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_ROLE", adminSessionBean.getRole());
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_ROLE", "GUEST");
		}

		model.addAttribute("APP", app);
		return "app-front";
	}

	@RequestMapping(value = { "/@{domain}", "/{domain:^.*(?!swagger-ui.html)}" }, method = { RequestMethod.GET })
	public String domain(Model model, @PathVariable @ValidAlphaNum String domain) {
		return domainProfile(model, domain, false, "front");
	}
}
