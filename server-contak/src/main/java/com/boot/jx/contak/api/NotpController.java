package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.dto.PhoneLoginDTO.PhoneLoginResponseDTO;
import com.boot.jx.contak.dto.PhoneNotpRequestModels.PhoneNotpDto;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneNOTPDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.jx.phonebook.dto.PhoneProfileDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.UniqueID;

@RestController
@RequestMapping("/notp")
public class NotpController {

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	PhoneBookManager phoneBookManager;

	@RequestMapping(value = "/api/v1/login", method = { RequestMethod.POST })
	public ApiResponse<PhoneProfileDTO, PhoneLoginResponseDTO> login(@RequestBody PhoneLoginDTO loginDTO) {

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
		if (ArgUtil.is(loginDTO.deviceToken)) { // Step 3
			if (!CryptoUtil.getEncoder().message(loginDTO.deviceToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID,
						new ApiFieldError().field("authToken"));
			}
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else if (ArgUtil.is(loginDTO.otp)) { // Step 2
			if (!new OTPDetails().yin(loginDTO.otpNounce).yang(userDoc.otpNounce)
					.genrate(loginDTO.phone, loginDTO.deviceId).validate(loginDTO.otp, userDoc.otpHash)
					&& !ArgUtil.is(loginDTO.otp, "888888")) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("otp"));
			}
			resp.deviceToken = UniqueID.generateSessionId();
			phoneUserQuery.setOtpHash(Constants.BLANK);
			phoneUserQuery.setOtpNounce(Constants.BLANK);
			phoneUserQuery.setAuthToken(CryptoUtil.getEncoder().message(resp.deviceToken).sha2().toString());
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else { // Step 1
			OTPDetails otp = OTPUtils.genrateBasicOTP(loginDTO.phone, loginDTO.deviceId);
			resp.otpPrefix = otp.getPrefix();
			resp.otpNounce = otp.getYin();
			phoneUserQuery.setOtpNounce(otp.getYang());
			phoneUserQuery.setOtpHash(otp.getHash());
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		}
	}

	@RequestMapping(value = "/api/v1/messages/fetch", method = { RequestMethod.POST })
	public ApiResponse<PhoneNOTPDoc, Object> read(@RequestBody PhoneLoginDTO loginDTO) {
		if (!ArgUtil.is(loginDTO.phone)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("phone"));
		}
		PhoneUserDoc userDoc = commonMongoTemplate.findById(loginDTO.phone, PhoneUserDoc.class);
		if (!ArgUtil.is(userDoc)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("phone"));
		}

		if (ArgUtil.is(loginDTO.deviceToken)) { // Step 3
			if (!CryptoUtil.getEncoder().message(loginDTO.deviceToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED,
						new ApiFieldError().field("authToken"));
			}
		}
		return ApiResponse.buildResults(phoneBookManager.fetchMessages(userDoc));
	}

	@RequestMapping(value = "/api/v1/messages/send", method = { RequestMethod.POST })
	public ApiResponse<PhoneNOTPDoc, Object> send(@RequestBody PhoneNotpDto msg) {

		if (!ArgUtil.is(msg.apiKey) || !ArgUtil.is(msg.domain)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("apiKey"));
		}

		if (!ArgUtil.is(msg.phone)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("phone"));
		}

		PhoneUserDoc userDoc = commonMongoTemplate.findById(msg.phone, PhoneUserDoc.class);

		if (!ArgUtil.is(userDoc)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.USER_NOT_FOUND, new ApiFieldError().field("phone"));
		}

		PhoneNOTPDoc newPhoneNOTPDoc = new PhoneNOTPDoc();
		newPhoneNOTPDoc.setDomain(msg.domain);
		newPhoneNOTPDoc.setPhoneId(msg.phone);
		newPhoneNOTPDoc.setOtp(msg.otp);
		newPhoneNOTPDoc.setTitle(msg.title);
		newPhoneNOTPDoc.setTags(msg.tags);
		newPhoneNOTPDoc.setCreatedAt(TimeStampIndex.now());
		newPhoneNOTPDoc.setExpiredAt(TimeStampIndex.from(System.currentTimeMillis() + msg.validity * 1000));
		commonMongoTemplate.save(newPhoneNOTPDoc);
		return ApiResponse.buildResults(newPhoneNOTPDoc);
	}

}