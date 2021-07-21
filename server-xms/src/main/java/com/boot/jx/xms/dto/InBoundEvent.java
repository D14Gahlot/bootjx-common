package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundEvent {
	@ApiMockModelProperty(example = "SEND_INVOICE", value = "Action Triggered by Agent/Service")
	public String actionCode;
}