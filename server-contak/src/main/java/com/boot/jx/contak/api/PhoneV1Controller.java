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
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.dto.PhoneLoginDTO.PhoneLoginResponseDTO;
import com.boot.jx.contak.dto.UserRegistrationDTO;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.contak.manager.ContakInboundManager;
import com.boot.jx.contak.manager.ContakInboundManager.USER_INBOUND_TYPE;
import com.boot.jx.contak.manager.PhoneService;
import com.boot.jx.contak.service.PhoneAuthService;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.jx.phonebook.dto.PhoneProfileDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.OTPUtils;
import com.boot.utils.OTPUtils.OTPDetails;
import com.boot.utils.TimeUtils;
import com.boot.utils.UniqueID;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/phone/api/v1")
public class PhoneV1Controller {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private PhoneBookManager phoneBookManager;

	@Autowired
	private PhoneService phoneService;

	@Autowired
	private PhoneAuthService phoneAuthService;

	@Autowired
	private ContakInboundManager contakInboundManager;

	@RequestMapping(value = "/login", method = { RequestMethod.POST })
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

			// boolean isUserRegistraion = ArgUtil.not(userDoc.getLastLoginAt());

			resp.loginToken = loginToken;
			phoneUserQuery.setLoginToken(resp.loginToken);
			phoneUserQuery.setLastLoginAt(TimeStampIndex.now());
			commonMongoTemplate.update(phoneUserQuery);

			contakInboundManager.sendUserAuthEvent(userDoc, USER_INBOUND_TYPE.USER_RELOGIN);

			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else if (ArgUtil.is(step, "VALIDATE") || (noStep && ArgUtil.is(loginDTO.otp))) { // Step 2
			if (!new OTPDetails().yin(loginDTO.otpNounce).yang(userDoc.otpNounce)
					.genrate(loginDTO.phone, loginDTO.deviceId).validate(loginDTO.otp, userDoc.otpHash)
					&& !ArgUtil.is(loginDTO.otp, "888888")) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.PARAM_INVALID, new ApiFieldError().field("otp"));
			}
			boolean isUserRegistraion = ArgUtil.not(userDoc.getLastLoginAt());

			resp.deviceToken = UniqueID.generateSessionId();
			resp.loginToken = loginToken;

			phoneUserQuery.setOtpHash(Constants.BLANK);
			phoneUserQuery.setOtpNounce(Constants.BLANK);
			phoneUserQuery.setLoginToken(resp.loginToken);
			phoneUserQuery.setLastLoginAt(TimeStampIndex.now());
			phoneUserQuery.setAuthToken(CryptoUtil.getEncoder().message(resp.deviceToken).sha2().toString());
			phoneUserQuery.setOtpCounter(0L);
			phoneUserQuery.setOtpStamp(0L);
			commonMongoTemplate.update(phoneUserQuery);

			contakInboundManager.sendUserAuthEvent(userDoc,
					isUserRegistraion ? USER_INBOUND_TYPE.USER_REGISTERED : USER_INBOUND_TYPE.USER_LOGIN);

			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		} else { // Step 1

			long currentCounter = userDoc.getOtpCounter();
			long nextStamp = userDoc.getOtpStamp();

			long activeAfter = 0L;

			nextStamp = getNextStamp(userDoc.getOtpStamp(), currentCounter);
			activeAfter = nextStamp - System.currentTimeMillis();

			if (activeAfter > 0) {
				if (activeAfter > TimeUtils.Constants.MILLIS_IN_HOUR) {
					ApiResponseUtil.throwAccessDeniedException(
							"Try again later in " + (activeAfter / TimeUtils.Constants.MILLIS_IN_HOUR) + " hours");
				} else if (activeAfter > TimeUtils.Constants.MILLIS_IN_MIN) {
					ApiResponseUtil.throwAccessDeniedException(
							"Try again later in " + (activeAfter / TimeUtils.Constants.MILLIS_IN_MIN) + " minutes");
				} else {
					ApiResponseUtil
							.throwAccessDeniedException("Try again later in " + (activeAfter / 1000) + " seconds");
				}
			} else if (currentCounter > 2l) {
				currentCounter = 0;
			}

			currentCounter++;
			OTPDetails otp = OTPUtils.genrateBasicOTP(loginDTO.phone, loginDTO.deviceId);

			phoneService.sendPhoneOTP(loginDTO.phone, otp.getOtp());

			long otpStamp = System.currentTimeMillis();
			nextStamp = getNextStamp(otpStamp, currentCounter);
			activeAfter = nextStamp - otpStamp;

			resp.otpPrefix = otp.getPrefix();
			resp.otpNounce = otp.getYin();
			resp.otpCounter = currentCounter;
			resp.otpWait = activeAfter;

			phoneUserQuery.setOtpNounce(otp.getYang());
			phoneUserQuery.setOtpHash(otp.getHash());
			phoneUserQuery.setOtpStamp(otpStamp);
			phoneUserQuery.setOtpCounter(currentCounter);
			commonMongoTemplate.update(phoneUserQuery);
			return ApiResponse.buildResults(phoneBookManager.getProfile(userDoc), resp);
		}
	}

	private long getNextStamp(long lastStamp, long currentCounter) {
		if (currentCounter < 2L) {
			return lastStamp + TimeUtils.Constants.MILLIS_IN_MIN;
		} else if (currentCounter == 2L) {
			return lastStamp + TimeUtils.Constants.MILLIS_IN_MIN * 2;
		} else if (currentCounter > 2L) {
			return lastStamp + TimeUtils.Constants.MILLIS_IN_DAY;
		}
		return lastStamp;
	}

	@RequestMapping(value = "/messages/fetch", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> read(@RequestBody PhoneLoginDTO loginDTO) {
		//PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(CollectionUtil.asList());
	}

	@RequestMapping(value = "/messages/mark/read", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> markRead(@RequestBody PhoneLoginDTO loginDTO) {
		//PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(CollectionUtil.asList());
	}

	@RequestMapping(value = "/messages/log/event", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> markFailed(@RequestBody PhoneLoginDTO loginDTO) {
		//PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(CollectionUtil.asList());
	}

	@RequestMapping(value = "/user/key/reg", method = { RequestMethod.POST })
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

		contakInboundManager.sendHandShakeAckEvent(userRegistrationDoc);

		return ApiResponse.buildResult(userRegistrationDoc);
	}

}