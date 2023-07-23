package com.boot.jx.contak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

@Component
public class PhoneAuthService {

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	public PhoneUserDoc isUserValid(PhoneLoginDTO loginDTO) {
		if (!ArgUtil.is(loginDTO.phone)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("phone"));
		}
		PhoneUserDoc userDoc = commonMongoTemplate.findById(loginDTO.phone, PhoneUserDoc.class);
		if (ArgUtil.is(loginDTO.deviceId)) { // Step 3
			if (!loginDTO.deviceId.equalsIgnoreCase(userDoc.deviceId)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("deviceId"));
			}
		}
		if (!ArgUtil.is(userDoc)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("phone"));
		}
		PhoneUserQuery phoneUserQuery = new PhoneUserQuery(userDoc);
		phoneUserQuery.setLastTimeActiveAt(TimeStampIndex.now());
		commonMongoTemplate.update(phoneUserQuery);

		if (ArgUtil.is(loginDTO.deviceToken)) { // Step 3
			if (!CryptoUtil.getEncoder().message(loginDTO.deviceToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED,
						new ApiFieldError().field("authToken"));
			}
		}
		return userDoc;
	}
}
