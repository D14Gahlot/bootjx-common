package com.boot.jx.phonebook.dto;

import java.io.Serializable;

import com.boot.jx.phonebook.model.PBName;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileUserDTO implements Serializable {
    private static final long serialVersionUID = 1281605084248923642L;

    @ApiMockModelProperty(example = "202019806096786", required = false)
    public String mobile;

    @ApiMockModelProperty(example = "Last Dorimon", required = false)
    public PBName name;

    @ApiMockModelProperty(example = "xxxxxxxx", required = false)
    public String authToken;

    public String getMobile() {
	return mobile;
    }

    public void setMobile(String mobile) {
	this.mobile = mobile;
    }

    public String getAuthToken() {
	return authToken;
    }

    public void setAuthToken(String authToken) {
	this.authToken = authToken;
    }

    public PBName getName() {
	return name;
    }

    public void setName(PBName name) {
	this.name = name;
    }

}