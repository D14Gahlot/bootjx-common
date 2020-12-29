package com.boot.jx.postman.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;

@Component
public class GoogleService {

	private String googleSecret = "6LeK33AUAAAAANWoO_wM5_3FxJ0DoPjPZp_n7pVz";

	@Autowired
	RestService restService;

	public Boolean verifyCaptcha(String responseKey, String remoteIP) {
		@SuppressWarnings("unchecked")
		Map<String, Object> resp = restService.ajax("https://www.google.com/recaptcha/api/siteverify").acceptJson()
				.field("secret", googleSecret).field("response", responseKey).field("remoteip", remoteIP).postForm()
				.as(Map.class);
		if (resp != null) {
			return ArgUtil.parseAsBoolean(resp.get("success"));
		}
		return false;

	}
}
