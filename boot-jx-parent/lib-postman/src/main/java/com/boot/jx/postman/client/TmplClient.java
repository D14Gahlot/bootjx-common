package com.boot.jx.postman.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostmanPackages.ICommonTmplPackage;
import com.boot.jx.postman.model.File;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;

@Component
public class TmplClient {

	public static class PATH {
		public static final String TMPL_FILE_PROCESS = "/tmpl/file/process";
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TmplClient.class);

	@Autowired
	private RestService restService;

	@Autowired
	private PostManClient postManClient;

	@Value("${app.tmpl.local}")
	private boolean isTmplLocal;

	@Autowired(required = false)
	private ICommonTmplPackage iCommonTmplPackage;

	public ApiResponse<File, Object> process(File file, ContactType contactType) throws PostManException {
		if (isTmplLocal && ArgUtil.is(iCommonTmplPackage)) {
			return ApiResponse.buildResult(iCommonTmplPackage.process(file, contactType));
		}
		return restService.ajax(postManClient.getPostmapURL()).path(PATH.TMPL_FILE_PROCESS)
				.queryParam("contactType", contactType).queryParam(PostManClient.PARAM_LANG, postManClient.getLang())
				.contentTypeJson().acceptJson().post(file)
				.as(new ParameterizedTypeReference<ApiResponse<File, Object>>() {
				});
	}

}
