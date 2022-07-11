package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.ContakConstants.ApiDeviceHeaders;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.jx.phonebook.dto.PhoneLoginDTO;
import com.boot.jx.phonebook.dto.PhoneProfileDTO;
import com.boot.jx.phonebook.dto.PhoneLoginDTO.PhoneLoginResponseDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.UniqueID;

@RestController
@RequestMapping("/contak")
public class ContakController {

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	PhoneBookManager phoneBookManager;

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/login", method = { RequestMethod.POST })
	public ApiResponse<PhoneProfileDTO, PhoneLoginResponseDTO> login(@RequestBody PhoneLoginDTO loginDTO,
			@RequestHeader(value = "x-device-id") String deviceId) {

		if (!ArgUtil.is(loginDTO.phone)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("phone"));
		}
		PhoneUserDoc userDoc = commonMongoTemplate.findById(loginDTO.phone, PhoneUserDoc.class);
		if (!ArgUtil.is(userDoc)) {
			userDoc = new PhoneUserDoc();
			userDoc.phoneId = loginDTO.phone;
			commonMongoTemplate.save(userDoc);
		}

		PhoneUserQuery phoneUserQuery = new PhoneUserQuery(userDoc);
		PhoneLoginResponseDTO resp = new PhoneLoginResponseDTO();
		if (ArgUtil.is(loginDTO.otp)) { // Step 2
			if (!new OTPDetails().yin(loginDTO.otpNounce).yang(userDoc.otpNounce).genrate(loginDTO.phone, deviceId)
					.getHash().equals(userDoc.otpHash)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("otp"));
			}
			resp.authToken = UniqueID.generateSessionId();
			phoneUserQuery.setOtpHash(Constants.BLANK);
			phoneUserQuery.setOtpNounce(Constants.BLANK);
			phoneUserQuery.setAuthToken(CryptoUtil.getEncoder().message(resp.authToken).sha2().toString());
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else if (ArgUtil.is(loginDTO.authToken)) { // Step 3
			if (!CryptoUtil.getEncoder().message(loginDTO.authToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID,
						new ApiFieldError().field("authToken"));
			}
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else { // Step 1
			OTPDetails otp = OTPUtils.genrateBasicOTP(loginDTO.phone, deviceId);
			resp.otpPrefix = otp.getPrefix();
			resp.otpNounce = otp.getYin();
			phoneUserQuery.setOtpNounce(otp.getYang());
			phoneUserQuery.setOtpHash(otp.getHash());
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		}
	}

}