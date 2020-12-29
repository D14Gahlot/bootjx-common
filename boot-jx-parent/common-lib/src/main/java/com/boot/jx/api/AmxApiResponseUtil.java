package com.boot.jx.api;

import java.util.ArrayList;
import java.util.List;

import com.boot.jx.AppConstants;
import com.boot.jx.exception.AmxApiError;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiErrorException;
import com.boot.jx.exception.IExceptionEnum;
import com.boot.utils.ArgUtil;
import com.boot.utils.ContextUtil;

public class AmxApiResponseUtil {

	@SuppressWarnings("unchecked")
	public static List<String> getLogs() {
		Object logsObject = ContextUtil.map().get(AppConstants.REQUEST_LOGS_XKEY);
		List<String> logs = null;
		if (logsObject == null) {
			logs = new ArrayList<String>();
			// logs.add(String.format(format, args));
			ContextUtil.map().put(AppConstants.REQUEST_LOGS_XKEY, logs);
		} else {
			logs = (List<String>) logsObject;
		}
		return logs;
	}

	public static void addLog(String format, Object... args) {
		if (ArgUtil.is(format)) {
			getLogs().add(String.format(format, args));
		}
	}

	@SuppressWarnings("unchecked")
	public static List<AmxFieldError> getErrors() {
		Object warningsObject = ContextUtil.map().get(AppConstants.REQUEST_ERROR_XKEY);
		List<AmxFieldError> warnings = null;
		if (warningsObject == null) {
			warnings = new ArrayList<AmxFieldError>();
			ContextUtil.map().put(AppConstants.REQUEST_ERROR_XKEY, warnings);
		} else {
			warnings = (List<AmxFieldError>) warningsObject;
		}
		return warnings;
	}

	@SuppressWarnings("unchecked")
	public static List<AmxFieldError> getWarnings() {
		Object userDeviceClientObject = ContextUtil.map().get(AppConstants.REQUEST_WARNING_XKEY);
		List<AmxFieldError> warnings = null;
		if (userDeviceClientObject == null) {
			warnings = new ArrayList<AmxFieldError>();
			ContextUtil.map().put(AppConstants.REQUEST_WARNING_XKEY, warnings);
		} else {
			warnings = (List<AmxFieldError>) userDeviceClientObject;
		}
		return warnings;
	}

	public static void addWarning(AmxFieldError warning) {
		List<AmxFieldError> amxFieldWarnings = getWarnings();
		for (AmxFieldError amxFieldWarning : amxFieldWarnings) {
			// Find duplicate Warnings
			if (amxFieldWarning.toString().equals(warning.toString())) {
				return;
			}
		}
		amxFieldWarnings.add(warning);
	}

	public static void addWarning(List<AmxFieldError> warnings) {
		if (!ArgUtil.isEmpty(warnings)) {
			for (AmxFieldError warning : warnings) {
				addWarning(warning);
			}
		}
	}

	public static void addWarning(String warning) {
		AmxFieldError w = new AmxFieldError();
		w.setDescription(warning);
		addWarning(w);
	}

	public static void addWarning(AmxApiException warning) {
		AmxFieldError w = new AmxFieldError();
		if (ArgUtil.is(warning.getError())) {
			w.setCode(ArgUtil.parseAsString(warning.getError().getStatusCode()));
			w.setCodeKey(ArgUtil.parseAsString(warning.getError().getStatusKey()));
		}
		w.setDescription(warning.getErrorMessage());
		w.setDescriptionKey(warning.getErrorKey());
		addWarning(w);
	}

	// Errors
	public static void addError(AmxFieldError warning) {
		List<AmxFieldError> amxFieldErrors = getErrors();
		for (AmxFieldError amxFieldWarning : amxFieldErrors) {
			// Find duplicate Errors
			if (amxFieldWarning.toString().equals(warning.toString())) {
				return;
			}
		}
		amxFieldErrors.add(warning);
	}

	public static void addError(List<AmxFieldError> errors) {
		if (!ArgUtil.isEmpty(errors)) {
			for (AmxFieldError warning : errors) {
				addError(warning);
			}
		}
	}

	public static void addError(String error) {
		AmxFieldError w = new AmxFieldError();
		w.setDescription(error);
		addError(w);
	}

	public static void addError(AmxApiException error) {
		AmxFieldError w = new AmxFieldError();
		if (ArgUtil.is(error.getError())) {
			w.setCode(ArgUtil.parseAsString(error.getError().getStatusCode()));
			w.setCodeKey(ArgUtil.parseAsString(error.getError().getStatusKey()));
		}
		w.setDescription(error.getErrorMessage());
		w.setDescriptionKey(error.getErrorKey());
		addError(w);
	}

	// Exception
	public static void throwException() {
		List<AmxFieldError> errors = getErrors();
		if (errors.size() > 0) {
			throwException(errors.get(0));
		}
	}

	public static void throwException(Exception exception) {
		AmxApiException.evaluate(exception);
	}

	public static void throwException(AmxApiError error) {
		throw new ApiErrorException(error);
	}

	public static void throwException(AmxFieldError fieldError) {
		AmxApiError error = new AmxApiError();
		error.setStatusKey(fieldError.getCodeKey());
		error.setErrorKey(fieldError.getDescriptionKey());
		error.setMessage(fieldError.getDescription());
		throwException(new ApiErrorException(error));
	}

	public static void throwException(String errorMessage) {
		throwException(new ApiErrorException(errorMessage));
	}

	public static void throwException(IExceptionEnum errorCode, String errorMessage) {
		throwException(new ApiErrorException(errorCode, errorMessage));
	}

	@SuppressWarnings("unchecked")
	public static List<String> getExceptionLogs() {
		Object exceptionLogObject = ContextUtil.map().get(AppConstants.EXCEPTION_LOGS_XKEY);
		List<String> exclogs = null;
		if (exceptionLogObject == null) {
			exclogs = new ArrayList<String>();
			ContextUtil.map().put(AppConstants.EXCEPTION_LOGS_XKEY, exclogs);
		} else {
			exclogs = (List<String>) exceptionLogObject;
		}
		return exclogs;
	}

	public static void addExceptionLog(String log) {
		getExceptionLogs().add(log);
	}
}
