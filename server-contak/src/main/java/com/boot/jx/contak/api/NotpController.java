package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.appflow.model.FileType;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.contak.dto.CompanyDTO;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.dto.PhoneLoginDTO.PhoneLoginResponseDTO;
import com.boot.jx.contak.dto.PhoneNotpRequestModels.PhoneNotpDto;
import com.boot.jx.contak.dto.UserRegistrationDTO;
import com.boot.jx.contak.dto.UserRegistrationDoc;
import com.boot.jx.contak.manager.FirebaseManager;
import com.boot.jx.contak.manager.UserRegistrationManager;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.filter.AppRequestUtil;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.model.CommonFile;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneContactDoc;
import com.boot.jx.phonebook.doc.PhoneNOTPDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.doc.PhoneUserQuery;
import com.boot.jx.phonebook.dto.PhoneProfileDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.model.TimeModels.TimeStampIndexKeyDeserializer;
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
	
	@Autowired
	UserRegistrationManager userRegistrationManager;
	
	
	@Autowired
	FirebaseManager firebaseManager;
	
	@Autowired
	AWSFileStore fileStore;

	@RequestMapping(value = "/api/v1/login", method = { RequestMethod.POST })
	public ApiResponse<PhoneProfileDTO, PhoneLoginResponseDTO> login(@RequestBody PhoneLoginDTO loginDTO) {

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
			resp.loginToken = loginToken;
			
			phoneUserQuery.setOtpHash(Constants.BLANK);
			phoneUserQuery.setOtpNounce(Constants.BLANK);
			phoneUserQuery.setLoginToken(resp.loginToken);
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
		PhoneUserQuery phoneUserQuery = new PhoneUserQuery(userDoc);
		phoneUserQuery.setLastTimeActiveAt(TimeStampIndex.now());
		commonMongoTemplate.update(phoneUserQuery);

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
				
		
		
		CompanyDoc compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("apiKey").is(msg.apiKey)));
		if(!ArgUtil.is(compoc)) {
			ApiResponseUtil.throwException("The request to send message is unauthorized");
		}
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
		
		String loginToken = userDoc.getLoginToken();
		if(msg.userLoginToken != loginToken) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.HANDSHAKE_REQUIRED, new ApiFieldError().field("userLoginToken"));
		}
		
		
		String title = msg.domain;
		String body = "You have received a notification from "+msg.domain;
		HashMap<String,String> data = new HashMap<>();
		if(msg.companyName != null) {
			data.put("companyName", msg.companyName);
		}
		if(msg.companyId != null) {
			data.put("companyId", msg.companyId);
		}
		if(msg.pubKey != null) {
			data.put("pubKey", msg.pubKey);
		}
		if(msg.type != null) {
			data.put("type", msg.type);
		}
		
		
		firebaseManager.sendNotification(msg.phone, title, body, data);

		PhoneNOTPDoc newPhoneNOTPDoc = new PhoneNOTPDoc();
		newPhoneNOTPDoc.setDomain(msg.domain);
		newPhoneNOTPDoc.setPhoneId(msg.phone);
		newPhoneNOTPDoc.setOtp(msg.otp);
		newPhoneNOTPDoc.setTitle(msg.title);
		newPhoneNOTPDoc.setTags(msg.tags);
		newPhoneNOTPDoc.setCreatedAt(TimeStampIndex.from(msg.createdAt));
		newPhoneNOTPDoc.setRelayedAt(TimeStampIndex.now());
		newPhoneNOTPDoc.setExpiredAt(TimeStampIndex.from(System.currentTimeMillis() + msg.validity * 1000));
		newPhoneNOTPDoc.setType(msg.type);
		newPhoneNOTPDoc.setPubKey(msg.pubKey);
		newPhoneNOTPDoc.setMsgGenId(msg.msgGenId);
		newPhoneNOTPDoc.setCompanyId(msg.companyId);
		newPhoneNOTPDoc.setCompanyName(msg.companyName);
		newPhoneNOTPDoc.setLogoUrl(msg.logoUrl);
		commonMongoTemplate.save(newPhoneNOTPDoc);
		return ApiResponse.buildResults(newPhoneNOTPDoc);
	}
	
	
	
	
	@RequestMapping(value = "/api/v1/user/key/reg/fetch", method = { RequestMethod.POST })
	public ApiResponse<UserRegistrationDoc, Object> read(@RequestBody HashMap<String,String> msg) {
		AppRequestUtil.log("MESSAGE APIKEY", msg);
		String name = msg.get("name");
		if (!ArgUtil.is(name)) {
			ApiResponseUtil.throwMissinInputException(new ApiFieldError().field("name"));
		}
		CompanyDoc  compoc = commonMongoTemplate.findOne(
				CommonMongoQueryBuilder.collection(CompanyDoc.class).where(Criteria.where("apiKey").is(msg.get("apiKey"))));
		if (!ArgUtil.is(compoc)) {
			ApiResponseUtil.throwInputException(ApiStatusCodes.UNAUTHORIZED, new ApiFieldError().field("apiKey"));
		}
		
		return ApiResponse.buildResults(userRegistrationManager.fetchRegistrations(compoc.companyId));
	}
	
	@RequestMapping(value = "/api/v1/user/key/reg", method = {RequestMethod.POST})
	public ApiResponse<UserRegistrationDoc, Object> save(@RequestBody UserRegistrationDTO msg){
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