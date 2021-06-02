package com.boot.jx.postman.contack;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "MOBILE_USER")
public class MobileUserDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	@ApiMockModelProperty(example = "202019806096786", required = false)
	public String mobile;

	@ApiMockModelProperty(example = "Last Dorimon", required = false)
	public String name;

	@ApiMockModelProperty(example = "911647_295805", required = false)
	public Map<String, MobileUserContactDoc> contacts;

	public List<MobileUserTestDoc> tests;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String authToken;

	public Integer score;

	public Boolean positive;

	public boolean syncRequired;

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

	public Map<String, MobileUserContactDoc> contacts() {
		if (!ArgUtil.is(this.contacts)) {
			this.contacts = new HashMap<String, MobileUserContactDoc>();
		}
		return contacts;
	}

	public List<MobileUserTestDoc> tests() {
		if (!ArgUtil.is(this.tests)) {
			this.tests = new ArrayList<MobileUserTestDoc>();
		}
		return tests;
	}

	public Map<String, MobileUserContactDoc> getContacts() {
		return contacts;
	}

	public void setContacts(Map<String, MobileUserContactDoc> contacts) {
		this.contacts = contacts;
	}

	public List<MobileUserTestDoc> getTests() {
		return tests;
	}

	public void setTests(List<MobileUserTestDoc> tests) {
		this.tests = tests;
	}

	public boolean isSyncRequired() {
		return syncRequired;
	}

	public void setSyncRequired(boolean syncRequired) {
		this.syncRequired = syncRequired;
	}

}