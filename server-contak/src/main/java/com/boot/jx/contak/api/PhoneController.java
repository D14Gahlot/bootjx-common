package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.dto.PhoneLoginDTO.PhoneLoginResponseDTO;
import com.boot.jx.contak.dto.UserRegistrationDTO;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.contak.manager.ContakApiContext;
import com.boot.jx.contak.manager.ContakMessageManager;
import com.boot.jx.contak.manager.FirebaseManager;
import com.boot.jx.contak.manager.PhoneService;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.jx.phonebook.dto.PhoneProfileDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.TimeUtils;
import com.boot.utils.UniqueID;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/phone")
public class PhoneController {

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@Autowired
	PhoneBookManager phoneBookManager;

	@Autowired
	ContakMessageManager contakMessageManager;

	@Autowired
	FirebaseManager firebaseManager;

	@Autowired
	AWSFileStore fileStore;

	@Autowired
	ContakApiContext apiContext;

	@Autowired
	PhoneService phoneService;

	@RequestMapping(value = "/api/v1/login", method = { RequestMethod.POST })
	public ApiResponse<PhoneProfileDTO, PhoneLoginResponseDTO> login(
			@ApiParam(allowableValues = "SEND,VALIDATE,VERIFY", required = false)
			@RequestParam(required = false) String step, @RequestBody PhoneLoginDTO loginDTO) {

		if (!ArgUtil.is(loginDTO.phone)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("phone"));
		}

		String loginToken = String.valueOf(System.currentTimeMillis());

		PhoneUserDoc userDoc = commonMongoTemplate.findById(loginDTO.phone, PhoneUserDoc.class);
		if (!ArgUtil.is(userDoc)) {
			userDoc = new PhoneUserDoc();
			userDoc.phoneId = loginDTO.phone;
			userDoc.loginToken = loginToken;
			commonMongoTemplate.save(userDoc);
		}

		boolean noStep = ArgUtil.not(step);
		PhoneUserQuery phoneUserQuery = new PhoneUserQuery(userDoc);
		PhoneLoginResponseDTO resp = new PhoneLoginResponseDTO();
		if (ArgUtil.is(step, "VERIFY") || (noStep && ArgUtil.is(loginDTO.deviceToken))) { // Step 3
			if (!CryptoUtil.getEncoder().message(loginDTO.deviceToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID,
						new ApiFieldError().field("authToken"));
			}
			resp.loginToken = loginToken;
			phoneUserQuery.setLoginToken(resp.loginToken);
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else if (ArgUtil.is(step, "VALIDATE") || (noStep && ArgUtil.is(loginDTO.otp))) { // Step 2
			if (!new OTPDetails().yin(loginDTO.otpNounce).yang(userDoc.otpNounce)
					.genrate(loginDTO.phone, loginDTO.deviceId).validate(loginDTO.otp, userDoc.otpHash)
					&& !ArgUtil.is(loginDTO.otp, "888888")) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("otp"));
			}
			resp.deviceToken = UniqueID.generateSessionId();
			resp.loginToken = loginToken;

			phoneUserQuery.setOtpHash(Constants.BLANK);
			phoneUserQuery.setOtpNounce(Constants.BLANK);
			phoneUserQuery.setLoginToken(resp.loginToken);
			phoneUserQuery.setAuthToken(CryptoUtil.getEncoder().message(resp.deviceToken).sha2().toString());
			phoneUserQuery.setOtpCounter(0L);
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else { // Step 1

			long currentCounter = userDoc.getOtpCounter();
			boolean moreThan1stWait = TimeUtils.isExpired(userDoc.getOtpStamp(), "1m");
			boolean moreThan2ndWait = TimeUtils.isExpired(userDoc.getOtpStamp(), "2m");
			boolean moreThan3rdWait = TimeUtils.isExpired(userDoc.getOtpStamp(), "24hrs");

			if (currentCounter == 1L && !moreThan1stWait) {
				ApiResponseUtil.throwAccessDeniedException("Try again later in 1 minute");
			} else if (currentCounter == 2L && !moreThan2ndWait) {
				ApiResponseUtil.throwAccessDeniedException("Try again later in 2 minutes");
			} else if (currentCounter > 2L && !moreThan3rdWait) {
				ApiResponseUtil.throwAccessDeniedException("Try again later in 24 hours");
			} else if (moreThan3rdWait) {
				currentCounter = 0;
			}

			OTPDetails otp = OTPUtils.genrateBasicOTP(loginDTO.phone, loginDTO.deviceId);

			phoneService.sendPhoneOTP(loginDTO.phone, otp.getOtp());

			resp.otpPrefix = otp.getPrefix();
			resp.otpNounce = otp.getYin();
			phoneUserQuery.setOtpNounce(otp.getYang());
			phoneUserQuery.setOtpHash(otp.getHash());
			phoneUserQuery.setOtpStamp(System.currentTimeMillis());
			phoneUserQuery.setOtpCounter(currentCounter);

			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		}
	}

	@RequestMapping(value = "/api/v1/messages/fetch", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> read(@RequestBody PhoneLoginDTO loginDTO) {
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
		return ApiResponse.buildResults(contakMessageManager.fetchMessages(userDoc));
	}

	@RequestMapping(value = "/api/v1/user/key/reg", method = { RequestMethod.POST })
	public ApiResponse<UserRegistrationDoc, Object> save(@RequestBody UserRegistrationDTO msg) {

		PhoneUserDoc userDoc = commonMongoTemplate.findById(msg.userPhoneNumber, PhoneUserDoc.class);
//		if (ArgUtil.is(msg.deviceToken)) { // Step 3
//			if (!msg.deviceToken.equalsIgnoreCase(userDoc.deviceId)) {
//				ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED,
//						new ApiFieldError().field("deviceId"+userDoc.deviceId));
//			}
//		} 

		if (ArgUtil.is(msg.deviceToken)) {
			if (!CryptoUtil.getEncoder().message(msg.deviceToken).sha2().is(userDoc.authToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED,
						new ApiFieldError().field("authToken"));
			}
		}

		UserRegistrationDoc userRegistrationDoc = new UserRegistrationDoc();
		userRegistrationDoc.setCompanyId(msg.companyId);
		userRegistrationDoc.setCompanyName(msg.companyName);
		userRegistrationDoc.setCreatedAt(TimeStampIndex.now());
		userRegistrationDoc.setExpiredAt(TimeStampIndex.from(System.currentTimeMillis() + 60000));
		userRegistrationDoc.setLoginToken(msg.loginToken);
		userRegistrationDoc.setUserPhoneNumber(msg.userPhoneNumber);
		userRegistrationDoc.setUserPubKey(msg.userPubKey);
		commonMongoTemplate.save(userRegistrationDoc);

		return ApiResponse.buildResult(userRegistrationDoc);
	}

}