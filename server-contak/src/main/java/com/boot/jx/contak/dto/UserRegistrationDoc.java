package com.boot.jx.contak.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Deprecated
@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "USER_REG")
public class UserRegistrationDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String userRegistrationId;

	@Indexed
	public String companyName;

	@Indexed
	public String userPhoneNumber;

	@Indexed
	public String companyId;

	public TimeStampIndex createdAt;

	public TimeStampIndex expiredAt;

	public TimeStampIndex readAt;

	public TimeStampIndex deliveredAt;

	public String userPubKey;
	public String loginToken;

	public String getUserRegistrationId() {
		return userRegistrationId;
	}

	public void setUserRegistrationId(String userRegistrationId) {
		this.userRegistrationId = userRegistrationId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}

	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public TimeStampIndex getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(TimeStampIndex expiredAt) {
		this.expiredAt = expiredAt;
	}

	public TimeStampIndex getReadAt() {
		return readAt;
	}

	public void setReadAt(TimeStampIndex readAt) {
		this.readAt = readAt;
	}

	public TimeStampIndex getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(TimeStampIndex deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public String getUserPubKey() {
		return userPubKey;
	}

	public void setUserPubKey(String userPubKey) {
		this.userPubKey = userPubKey;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

}