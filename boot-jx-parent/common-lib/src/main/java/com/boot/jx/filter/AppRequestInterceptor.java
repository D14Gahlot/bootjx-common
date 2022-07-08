package com.boot.jx.filter;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.boot.jx.rest.RestService;

@Component
public class AppRequestInterceptor extends HandlerInterceptorAdapter {

	final String sameSiteAttribute = "; SameSite=None";
	final String secureAttribute = "; Secure";

	@Autowired
	RestService restService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		restService.importMetaFromStatic(request);
		return super.preHandle(request, response, handler);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

		// addEtagHeader(request, response);

		Collection<String> setCookieHeaders = response.getHeaders(HttpHeaders.SET_COOKIE);

		if (setCookieHeaders == null || setCookieHeaders.isEmpty())
			return;

		setCookieHeaders.stream().filter(StringUtils::isNotBlank).map(header -> {
			if (header.toLowerCase().contains("samesite")) {
				return header;
			} else {
				return header.concat(sameSiteAttribute);
			}
		}).map(header -> {
			if (header.toLowerCase().contains("secure")) {
				return header;
			} else {
				return header.concat(secureAttribute);
			}
		}).forEach(finalHeader -> response.setHeader(HttpHeaders.SET_COOKIE, finalHeader));
	}
}
