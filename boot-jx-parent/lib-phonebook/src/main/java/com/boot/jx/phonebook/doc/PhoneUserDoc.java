package com.boot.jx.phonebook.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PHONE_USER")
public class PhoneUserDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	private String mobile;

	public PBName name;

	public PBPhone phone;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String authToken;

	public Boolean positive;

	public boolean syncRequired;

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

	public boolean isSyncRequired() {
		return syncRequired;
	}

	public void setSyncRequired(boolean syncRequired) {
		this.syncRequired = syncRequired;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public PBName getName() {
		return name;
	}

	public void setName(PBName name) {
		this.name = name;
	}

}