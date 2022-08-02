package com.boot.jx.contak.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class PhoneNotpRequestModels {
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PhoneNotpDto implements Serializable {
		private static final long serialVersionUID = 4064758284063588819L;
		@ApiMockModelProperty(example = "abcorp", required = true, value = "Organization domain")
		public String domain;

		@ApiMockModelProperty(example = "xxxxxxxxxxxxxxxxxxxx", required = true, value = "API key")
		public String apiKey;

		@ApiMockModelProperty(example = "919988776655", required = true, value = "Recpt of OTP")
		public String phone;

		@ApiMockModelProperty(example = "888888", required = true, value = "OTP to be sent")
		public String otp;

		@ApiMockModelProperty(example = "Login Otp", required = true, value = "Title of Message")
		public String title;

		@ApiMockModelProperty(example = "15", required = true, value = "Validity in seconds")
		public long validity;

		@ApiMockModelProperty(example = "[AUTH,SIGNUP]", required = false, value = "Tags to be attached for message")
		public List<String> tags;
	}

}
