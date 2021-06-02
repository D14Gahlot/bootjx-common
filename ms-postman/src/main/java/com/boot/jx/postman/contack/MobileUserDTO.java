package com.boot.jx.postman.contack;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileUserDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@ApiMockModelProperty(example = "202019806096786", required = false)
	public String mobile;

	@ApiMockModelProperty(example = "Last Dorimon", required = false)
	public String name;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String authToken;

	public Integer score;

	public Boolean positive;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public Boolean getPositive() {
		return positive;
	}

	public void setPositive(Boolean positive) {
		this.positive = positive;
	}

}