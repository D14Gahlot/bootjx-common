package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class CommonMsgLocation {

	@ApiMockModelProperty(example = "1 Hacker Way, Menlo Park, CA, 94025", value = "Address Text")
	public String address;

	@ApiMockModelProperty(example = "12.909090", value = "latitude")
	public String latitude;

	@ApiMockModelProperty(example = "6.908080", value = "longitude")
	public String longitude;

	@ApiMockModelProperty(example = "Main Building", value = "location-name")
	public String name;

}