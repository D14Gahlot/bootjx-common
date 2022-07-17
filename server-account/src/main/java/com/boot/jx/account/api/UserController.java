package com.boot.jx.account.api;

import java.security.NoSuchAlgorithmException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfigPackage.AppCommonConfig;
import com.boot.jx.account.AccountAuthService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.dto.UserLoginToken;
import com.boot.jx.common.service.EmpAuthService;
import com.boot.utils.ArgUtil;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private AppCommonConfig appCommonConfig;

	@Autowired
	private EmpAuthService empAuthService;

	@RequestMapping(value = { "/auth/**", "/app/**" }, method = { RequestMethod.GET })
	public String home(Model model, @RequestParam(required = false) String theme) {
		model.addAllAttributes(appCommonConfig.appAttributes());

		Authentication auth = AccountAuthService.getAuthentication();
		if (ArgUtil.is(auth)) {
			model.addAttribute("APP_USER", auth.getName());
			model.addAttribute("APP_USER_ROLE", "ACCOUNT_ADMIN");
		} else {
			model.addAttribute("APP_USER", "");
			model.addAttribute("APP_USER_ROLE", "GUEST");
		}

		model.addAttribute("APP", "account");

		return "app-account";
	}

	@ResponseBody
	@RequestMapping(value = "/pub/login", method = { RequestMethod.POST })
	public ApiResponse<UserLoginToken, Object> agentLogin(@RequestParam String username, @RequestParam String password,
			@RequestParam(required = false) String app, @RequestParam String tnt, @RequestParam String domainId)
			throws NoSuchAlgorithmException {
		return ApiResponse
				.buildData(empAuthService.createAgentLoginToken(username, username, password, tnt, domainId, app));
	}

}
