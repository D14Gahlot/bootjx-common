package com.boot.jx.postman.contack;

import java.io.Serializable;

import com.boot.jx.postman.contack.ContackConstants.SafeRelation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MobileUserContactDTO implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;
	public String mobile;
	public String name;
	public Integer score;
	public SafeRelation relation;

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
}