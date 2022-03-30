package com.boot.jx.account.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.account.AccountSecurityConfig;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.utils.ArgUtil;
import com.boot.utils.Urly;

@Controller
@RequestMapping("/common")
public class CommonContrller {

	@Autowired
	CommonHttpRequest req;

	@ResponseBody
	@RequestMapping(value = { "", "/", "/**" }, method = { RequestMethod.GET })
	public String cpanel(Model model, @RequestParam(required = false) String authToken, HttpServletRequest request,
			HttpServletResponse response) {

		String referrer = req.getRequestParam("referer");
		String referrerpath = request.getRequestURI().replaceFirst("/common/", "/");
		System.out.println(request.getRequestURI());
		String targetContext = "/front";
		try {
			if (ArgUtil.is(referrer)) {
				referrer = Urly.parse(referrer).getRelativeURL();
				for (Entry<String, Object> context : AccountSecurityConfig.CONTEXTS_MAP.map().entrySet()) {
					if (referrer.startsWith(context.getKey(), 1)) {
						targetContext = "/" + context.getValue();
						break;
					}
				}
			}
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return redirectResponse(request, response, targetContext + referrerpath);
	}

	private String redirectResponse(HttpServletRequest request, HttpServletResponse response, String destination) {
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
		response.setHeader("Location", destination);
		return "Location:" + destination + "/?_=" + System.currentTimeMillis();
	}

}
