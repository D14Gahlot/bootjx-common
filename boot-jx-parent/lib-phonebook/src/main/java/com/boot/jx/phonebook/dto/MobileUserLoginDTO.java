package com.boot.jx.phonebook.dto;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileUserLoginDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@ApiMockModelProperty(example = "202019806096786", required = true)
	public String mobile;

	@ApiMockModelProperty(example = "xxxxxxxx", required = true)
	public String idToken;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}