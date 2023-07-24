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
import com.boot.jx.contak.cache.OtpAlertEvent;
import com.boot.jx.contak.cache.OtpAlertEventManager;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.dto.UserRegistrationDTO;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.contak.manager.ContakMessageManager;
import com.boot.jx.contak.service.PhoneAuthService;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.UniqueID;

@RestController
@RequestMapping("/phone/api/v2")
public class PhoneV2Controller {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private ContakMessageManager contakMessageManager;

	@Autowired
	private PhoneAuthService phoneAuthService;

	@Autowired
	private OtpAlertEventManager otpAlertEventManager;

	@RequestMapping(value = "/messages/fetch", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> read(@RequestBody PhoneLoginDTO loginDTO) {
		PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(contakMessageManager.fetchMessages(userDoc));
	}

	@RequestMapping(value = "/messages/mark/read", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> markRead(@RequestBody PhoneLoginDTO loginDTO) {
		PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(contakMessageManager.markRead(loginDTO.event.noteId));
	}

	@RequestMapping(value = "/messages/log/event", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> markFailed(@RequestBody PhoneLoginDTO loginDTO) {
		PhoneUserDoc userDoc = phoneAuthService.isUserValid(loginDTO);
		return ApiResponse.buildResults(contakMessageManager.addEventLog(loginDTO.event));
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

		otpAlertEventManager.sendHandShakeAckEvent(userRegistrationDoc);

		return ApiResponse.buildResult(userRegistrationDoc);
	}

	@RequestMapping(value = "/events/push", method = { RequestMethod.POST })
	public ApiResponse<OtpAlertEvent, Object> eventPush(@RequestParam String companyId, @RequestParam String phoneId) {
		return ApiResponse
				.buildResults(otpAlertEventManager.createOtpAlertEvent(UniqueID.generateString(), companyId, phoneId));
	}

	@RequestMapping(value = "/events/poll", method = { RequestMethod.POST })
	public ApiResponse<OtpAlertEvent, Object> eventPush(@RequestParam String companyId) {
		return ApiResponse.buildResults(otpAlertEventManager.pollOtpAlertEvents(companyId));
	}

}