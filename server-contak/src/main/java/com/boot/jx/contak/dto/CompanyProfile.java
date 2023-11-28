package com.boot.jx.contak.dto;

import java.io.Serializable;

public class CompanyProfile implements Serializable {
	private static final long serialVersionUID = -5950577097855647609L;
	public String supportEmail;
	public String supportPhone;
	public String supportLink;

	public String getSupportEmail() {
		return supportEmail;
	}

	public void setSupportEmail(String supportEmail) {
		this.supportEmail = supportEmail;
	}

	public String getSupportPhone() {
		return supportPhone;
	}

	public void setSupportPhone(String supportPhone) {
		this.supportPhone = supportPhone;
	}

	public String getSupportLink() {
		return supportLink;
	}

	public void setSupportLink(String supportLink) {
		this.supportLink = supportLink;
	}

}