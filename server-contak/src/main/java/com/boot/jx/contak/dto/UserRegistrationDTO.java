package com.boot.jx.contak.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class UserRegistrationDTO {
	private static final long serialVersionUID = 1281605084248923642L;

	@ApiMockModelProperty(example = "Acme Inc.", required = true)
	public String companyName;
	
	@ApiMockModelProperty(example = "1", required = true)
	public String companyId;
	
	@ApiMockModelProperty(example = "GMT+5:30", required = false)
	public String timezone;

	@ApiMockModelProperty(example = "922333XXXXX", required = false)
	public String userPhoneNumber;
	
	@ApiMockModelProperty(example = "922333XXXXX", required = false)
	public String createdAt;
	
	@ApiMockModelProperty(example = "XXXXXXXX", required = false)
	public String userPubKey;
	
	
}


