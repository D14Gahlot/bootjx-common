package com.boot.jx.test.api;

import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boot.utils.CollectionUtil;

@RestController
public class TmplQuickController {

	@RequestMapping(value = "/test/map/smart_reply", method = {})
	public List<String> mapSmartReply(HttpMethod method) {
		return CollectionUtil.asList(method.toString());
	}

}
