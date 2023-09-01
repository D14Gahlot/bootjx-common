package com.boot.jx.contak.dto;

import java.io.Serializable;

import com.boot.jx.contak.dto.ContakModels.ContakEvent;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneLoginDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@ApiMockModelProperty(example = "202019806096786", required = true)
	public String phone;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String deviceId;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String deviceToken;

	@ApiMockModelProperty(example = "888888", required = false, value = "if not authToken")
	public String otp;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false, value = "Send if OTP")
	public String otpNounce;

	public MessageEvent event;

	public static class MessageEvent implements Serializable, ContakEvent {
		private static final long serialVersionUID = 7471706517329183180L;
		@ApiMockModelProperty(example = "xxxxxxxx", required = false, value = "Messages Delivered Id")
		public String noteId;
		@ApiMockModelProperty(example = "xxxxxxxx", required = false, value = "Message Event Name",
				allowableValues = "READ")
		public String eventName;
		public String eventSubCode;
		public String eventDesc;
		public long eventStamp;
	}

	public static class PhoneLoginResponseDTO implements Serializable {
		private static final long serialVersionUID = 1281605084248923642L;

		@ApiMockModelProperty(example = "202019806096786", required = true)
		public String phone;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String deviceToken;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String otpPrefix;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String otpNounce;

		@ApiMockModelProperty(example = "xxxxxxxx", required = false)
		public String loginToken;

		@ApiMockModelProperty(example = "1", required = false)
		public long otpCounter;

		@ApiMockModelProperty(example = "1", required = false,
				value = "milliseconds to wait before next otp send request can be sent")
		public long otpWait;
	}

}