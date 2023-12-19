package com.boot.jx.phonebook.doc;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQueryBuilder.DocQueryBuilder;

public class PhoneUserQuery extends DocQueryBuilder<PhoneUserDoc> {

	public PhoneUserQuery(PhoneUserDoc doc) {
		super(doc);
	}

	@Override
	public PhoneUserDoc newDoc(String id) {
		PhoneUserDoc doc = new PhoneUserDoc();
		doc.phoneId = id;
		return doc;
	}

	@Override
	public String getId(PhoneUserDoc doc) {
		return doc.phoneId;
	}

	public PhoneUserQuery setOtpNounce(String yang) {
		doc.setOtpNounce(yang);
		this.set("otpNounce", yang);
		return this;
	}

	public PhoneUserQuery setOtpStamp(long otpStamp) {
		doc.setOtpStamp(otpStamp);
		this.set("otpStamp", otpStamp);
		return this;
	}

	public PhoneUserQuery setOtpCounter(long otpCounter) {
		doc.setOtpCounter(otpCounter);
		this.set("otpCounter", otpCounter);
		return this;
	}

	public PhoneUserQuery setOtpHash(String hash) {
		doc.setOtpHash(hash);
		this.set("otpHash", hash);
		return this;
	}

	public PhoneUserQuery setAuthToken(String authToken) {
		doc.setAuthToken(authToken);
		this.set("authToken", authToken);
		return this;
	}

	public PhoneUserQuery setLoginToken(String loginToken) {
		doc.setLoginToken(loginToken);
		this.set("loginToken", loginToken);
		return this;
	}

	public PhoneUserQuery setDeviceId(String deviceId) {
		doc.setDeviceId(deviceId);
		this.set("deviceId", deviceId);
		return this;
	}

	public PhoneUserQuery setLastTimeActiveAt(TimeStampIndex timeStampIndex) {
		doc.setLastActiveAt(timeStampIndex);
		this.set("lastActiveAt", timeStampIndex);
		return this;
	}

	public void setLastLoginAt(TimeStampIndex lastLoginAt) {
		doc.setLastLoginAt(lastLoginAt);
		this.set("lastLoginAt", lastLoginAt);
	}
}
