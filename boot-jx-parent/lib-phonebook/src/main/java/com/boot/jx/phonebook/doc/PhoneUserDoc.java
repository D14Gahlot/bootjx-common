package com.boot.jx.phonebook.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PHONE_USER")
public class PhoneUserDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String phoneId;
	public PBName name;
	public PBPhone phone;
	public String deviceId;
	public String authToken;
	public String otpHash;
	public String otpNounce;
	public long otpStamp;
	public long otpCounter;
	public TimeStampIndex lastActiveAt;
	public String loginToken;

	public String getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}

	public PBName getName() {
		return name;
	}

	public void setName(PBName name) {
		this.name = name;
	}

	public PBPhone getPhone() {
		return phone;
	}

	public void setPhone(PBPhone phone) {
		this.phone = phone;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getOtpHash() {
		return otpHash;
	}

	public void setOtpHash(String otpHash) {
		this.otpHash = otpHash;
	}

	public String getOtpNounce() {
		return otpNounce;
	}

	public void setOtpNounce(String otpNounce) {
		this.otpNounce = otpNounce;
	}

	public TimeStampIndex getLastActiveAt() {
		return lastActiveAt;
	}

	public void setLastActiveAt(TimeStampIndex lastActiveAt) {
		this.lastActiveAt = lastActiveAt;
	}

	public String getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(String loginToken) {
		this.loginToken = loginToken;
	}

	public long getOtpStamp() {
		return otpStamp;
	}

	public void setOtpStamp(long otpStamp) {
		this.otpStamp = otpStamp;
	}

	public long getOtpCounter() {
		return otpCounter;
	}

	public void setOtpCounter(long otpCounter) {
		this.otpCounter = otpCounter;
	}

}