package com.boot.jx.contak.nodedocs;

import com.boot.jx.contak.dto.ContakModel;
import com.boot.jx.swagger.ApiMockModelProperty;

public class CustomerNote {

	public static class CustomerNoteTemplate {
		@ApiMockModelProperty(example = "TRANSATION_ALERT", value = "Code of template")
		public String code;

		@ApiMockModelProperty(
				value = "Dynamic values to be used to fill in template to draft final message. These values will be E2E encrypted")
		public ContakModel model;
	}

	@ApiMockModelProperty(example = "91XXXXXXXXXX", value = "Phone number of customer with ISD Code")
	public String phone;

	@ApiMockModelProperty(value = "Details of Template")
	public CustomerNoteTemplate template;

	@ApiMockModelProperty(example = "60", value = "Message validtity in Seconds")
	public long validity;

}
