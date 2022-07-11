package com.boot.jx.phonebook.dto;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneLoginDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@ApiMockModelProperty(example = "202019806096786", required = true)
	public String phone;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String authToken;

	@ApiMockModelProperty(example = "if not authToken", required = false)
	public String otp;

	@ApiMockModelProperty(example = "Send if OTP", required = false)
	public String otpNounce;

	public static class PhoneLoginResponseDTO implements Serializable {
		private static final long serialVersionUID = 1281605084248923642L;

		@ApiMockModelProperty(example = "202019806096786", required = true)
		public String phone;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String authToken;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String otpPrefix;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String otpNounce;

	}

}