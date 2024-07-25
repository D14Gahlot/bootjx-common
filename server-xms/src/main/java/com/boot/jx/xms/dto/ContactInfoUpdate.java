package com.boot.jx.xms.dto;

import java.util.List;

import com.boot.jx.postman.dto.ChatProfileDTO.CustomerLabel;
import com.boot.jx.swagger.ApiMockModelProperty;

public class ContactInfoUpdate {
	@ApiMockModelProperty(example = "C34567", value = "Unique Id assigned to Contact by Core Business Application",
			required = false)
	public String profileId;

	@ApiMockModelProperty(example = "919988776655", value = "Mobile Number if to be changed", required = false)
	public String mobile;

	@ApiMockModelProperty(example = "abc@xyz.com", value = "EmailId if to be changed", required = false)
	public String email;

	@ApiMockModelProperty(example = "John Doe", value = "Name if to be Channel", required = false)
	public String name;

	@ApiMockModelProperty(value = "Additional Labels", required = false)
	public List<CustomerLabel> labels;
}