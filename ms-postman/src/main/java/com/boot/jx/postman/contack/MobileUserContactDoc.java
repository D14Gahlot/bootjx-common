package com.boot.jx.postman.contack;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.contack.ContackConstants.SafeRelation;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "MOBILE_USER_CONTACT")
public class MobileUserContactDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	@ApiMockModelProperty(example = "202019806096786", required = false)
	public String mobile;

	@ApiMockModelProperty(example = "Last Dorimon", required = false)
	public String name;

	public Integer score;

	public SafeRelation relation;

	public long lastContact;

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

	public SafeRelation getRelation() {
		return relation;
	}

	public void setRelation(SafeRelation relation) {
		this.relation = relation;
	}

	public long getLastContact() {
		return lastContact;
	}

	public void setLastContact(long lastContact) {
		this.lastContact = lastContact;
	}
}