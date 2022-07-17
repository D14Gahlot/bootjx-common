package com.boot.jx.common.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.exception.AmxAdvice;
import com.boot.jx.exception.AmxApiError;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.http.ApiRequest.ResponeError;
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
import com.boot.utils.HttpUtils;
import com.boot.utils.StringUtils.StringMatcher;

@ControllerAdvice
public class AppCommonAdvice extends AmxAdvice {

	public HttpStatus getHttpStatus(AmxApiException exp) {
		ApiRequestDetail apiRequestDetail = AppContextUtil.getApiRequestDetail();
		if (apiRequestDetail.getResponeError() == ResponeError.PROPAGATE) {
			return exp.getHttpStatus();
		}
		return HttpStatus.OK;
	}

	public static final Pattern DUPLICATE_FIELD = Pattern.compile(
			"duplicate key error collection: [a-zA-Z0-9_\\.]+ index:\\ ([a-zA-Z0-9_]+)('; nested exception|\\ dup\\ key)");

	@ExceptionHandler(DuplicateKeyException.class)
	@ResponseBody
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ResponseEntity<AmxApiError> handle(DuplicateKeyException exception, HttpServletRequest request,
			HttpServletResponse response) {
		List<ApiFieldError> errors = new ArrayList<ApiFieldError>();
		ApiFieldError newError = new ApiFieldError();

		StringMatcher matcher = new StringMatcher(exception.getMessage());
		if (matcher.isMatch(DUPLICATE_FIELD)) {
			newError.setField(matcher.group(1));
			newError.setDescriptionKey("DUPLICATE_KEY");
			newError.setDescription(HttpUtils.sanitze("Duplicate Key: "+newError.getField()));
		}
		errors.add(newError);
		return badRequest(exception, errors, request, response, ApiStatusCodes.PARAM_DUPLICATE);
	}

}
