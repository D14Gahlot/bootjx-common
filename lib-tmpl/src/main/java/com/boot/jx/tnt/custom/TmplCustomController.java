package com.boot.jx.tnt.custom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppConfig;
import com.boot.jx.api.ApiResponse;

@RestController
public class TmplCustomController {

	@Autowired
	AppConfig appConfig;

	// Meta APIS
	@RequestMapping(value = "/pub/meta/domain/alias", method = { RequestMethod.GET })
	public ApiResponse<Object, Object> messageType(@RequestParam String domain, @RequestParam String alias) {
		TenantClientResolver.tntMapping.put(alias, domain);
		return ApiResponse.buildResults(TenantClientResolver.tntMapping);
	}

}
