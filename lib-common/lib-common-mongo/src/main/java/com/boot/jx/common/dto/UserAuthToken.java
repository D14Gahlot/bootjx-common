package com.boot.jx.common.dto;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAuthToken implements Serializable {

	private static final long serialVersionUID = -4274591422350406712L;

	public String tokenId;

	private String domainUser;
	private String domainName;
	private String domainId;
	private String domainToken;
	private String domainUserEmail;
	private String domainUserPhone;
	private String app;
	private String event;

	// OTP
	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String otpPrefix;

	@ApiMockModelProperty(example = "xxxxxxxx", required = false)
	public String otpNounce;

	public String otpHash;

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

	public String getOtpPrefix() {
		return otpPrefix;
	}

	public void setOtpPrefix(String otpPrefix) {
		this.otpPrefix = otpPrefix;
	}

	public String getOtpNounce() {
		return otpNounce;
	}

	public void setOtpNounce(String otpNounce) {
		this.otpNounce = otpNounce;
	}

	public String getOtpHash() {
		return otpHash;
	}

	public void setOtpHash(String otpHash) {
		this.otpHash = otpHash;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public String getDomainUserPhone() {
		return domainUserPhone;
	}

	public void setDomainUserPhone(String domainUserPhone) {
		this.domainUserPhone = domainUserPhone;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
}
