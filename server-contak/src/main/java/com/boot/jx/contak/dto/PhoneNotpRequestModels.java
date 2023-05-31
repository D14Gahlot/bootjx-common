package com.boot.jx.contak.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class PhoneNotpRequestModels {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ContakMessgaeTemplate implements Serializable {
		private static final long serialVersionUID = -1925497867658609632L;
		public String code;
		public ContakModel model;
		public String modelEncrypted;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class PhoneNotpDto implements Serializable {
		private static final long serialVersionUID = 4064758284063588819L;

		@Deprecated
		@ApiMockModelProperty(example = "1", required = true, value = "Company Id")
		public long domainId;

		@ApiMockModelProperty(example = "xxxxxxxxxxxxxxxxxxxx", required = true, value = "API key")
		public String apiKey;

		@ApiMockModelProperty(example = "919988776655", required = true, value = "Recpt of OTP")
		public String phone;

		@Deprecated
		@ApiMockModelProperty(example = "888888", required = true, value = "OTP to be sent")
		public String otp;

		@Deprecated
		@ApiMockModelProperty(example = "Login Otp", required = true, value = "Title of Message")
		public String title;

		@ApiMockModelProperty(example = "15", required = true, value = "Validity in seconds")
		public long validity;

		@ApiMockModelProperty(example = "[AUTH,SIGNUP]", required = false, value = "Tags to be attached for message")
		public List<String> tags;

		@ApiMockModelProperty(example = "OTP", required = true, value = "Type of message")
		public String type;

		@ApiMockModelProperty(example = "XXXXXXXXXXXXXXX", required = false, value = "Public key from the client")
		public String pubKey;

		@ApiMockModelProperty(example = "1", required = true, value = "Message Gen Id")
		public String msgGenId;

		@ApiMockModelProperty(example = "1", required = true, value = "Company Id")
		public String companyId;

		@ApiMockModelProperty(example = "Acme Inc.", required = true, value = "Company Name")
		public String companyName;

		@ApiMockModelProperty(example = "www.google.com/abc.png", required = false, value = "Company Logo Url")
		public String logoUrl;

		@ApiMockModelProperty(example = "231241123123", required = false, value = "Created at timestamp")
		public long createdAt;

		@ApiMockModelProperty(example = "XXXXXX", required = false, value = "Login token")
		public String loginToken;

		public ContakMessgaeTemplate template;

	}

}
