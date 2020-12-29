package com.boot.jx.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.boot.jx.cache.MCQStatus.ApiMCQStatus;
import com.boot.jx.cache.MCQStatus.MCQStatusCodes;
import com.boot.jx.exception.AmxApiError;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.IExceptionEnum;
import com.boot.jx.swagger.IStatusCodeListPlugin;

import springfox.documentation.swagger.common.SwaggerPluginSupport;

@Component
@Order(SwaggerPluginSupport.SWAGGER_PLUGIN_ORDER + 1000)
public class MCQStatus extends IStatusCodeListPlugin<MCQStatusCodes, ApiMCQStatus> {

	/**
	 * Error Enum Codes for MCQ Server
	 * 
	 * @author lalittanwar
	 *
	 */
	public static enum MCQStatusCodes implements IExceptionEnum {
		DATA_ERROR, DATA_READ_ERROR, DATA_SAVE_ERROR, DATA_REMOVE_ERROR;
		@Override
		public String getStatusKey() {
			return this.toString();
		}

		@Override
		public int getStatusCode() {
			return 8000 + this.ordinal();
		}

	}

	/**
	 * 
	 * API Status Enum for MCQ Server APis
	 * 
	 * @author lalittanwar
	 *
	 */
	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface ApiMCQStatus {
		MCQStatusCodes[] value() default { MCQStatusCodes.DATA_ERROR };
	}

	/**
	 * 
	 * @author lalittanwar
	 *
	 */
	public static class MCQStatusError extends AmxApiException {

		private static final long serialVersionUID = 1L;

		public MCQStatusError(AmxApiError error) {
			super(error);
		}

		public MCQStatusError() {
			super("MCQ Server error occured");
			this.setError(MCQStatusCodes.DATA_ERROR);
		}

		public MCQStatusError(MCQStatusCodes statusCode) {
			super(statusCode);
		}

		public MCQStatusError(MCQStatusCodes statusCode, String message) {
			super(statusCode, message);
		}

		public MCQStatusError(Exception e) {
			super(e);
			this.setError(MCQStatusCodes.DATA_ERROR);
		}

		@Override
		public AmxApiException getInstance(AmxApiError apiError) {
			return new MCQStatusError(apiError);
		}

		@Override
		public IExceptionEnum getErrorIdEnum(String errorId) {
			return MCQStatusCodes.DATA_ERROR;
		}

		public static <T> T evaluate(Exception e) {
			if (e instanceof AmxApiException) {
				throw (AmxApiException) e;
			} else if (e instanceof MCQStatusError) {
				throw (MCQStatusError) e;
			} else {
				throw new MCQStatusError(e);
			}
		}

		@Override
		public boolean isReportable() {
			return false;
		}

	}

	@Override
	public Class<ApiMCQStatus> getAnnotionClass() {
		return ApiMCQStatus.class;
	}

	@Override
	public MCQStatusCodes[] getValues(ApiMCQStatus annotation) {
		return annotation.value();
	}

}
