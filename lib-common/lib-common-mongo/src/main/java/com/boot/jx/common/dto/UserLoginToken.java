package com.boot.jx.common.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLoginToken implements Serializable {

	private static final long serialVersionUID = -4274591422350406712L;

	private String domainUser;
	private String domainName;
	private String domainId;
	private String domainToken;
	private String domainUserEmail;
	private String app;

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getDomainId() {
		return domainId;
	}

	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	public String getDomainToken() {
		return domainToken;
	}

	public void setDomainToken(String domainToken) {
		this.domainToken = domainToken;
	}

	public String getApp() {
		return app;
	}

	public void setApp(String app) {
		this.app = app;
	}

	public String getDomainUser() {
		return domainUser;
	}

	public void setDomainUser(String domainUser) {
		this.domainUser = domainUser;
	}

	public String getDomainUserEmail() {
		return domainUserEmail;
	}

	public void setDomainUserEmail(String domainUserEmail) {
		this.domainUserEmail = domainUserEmail;
	}
}
