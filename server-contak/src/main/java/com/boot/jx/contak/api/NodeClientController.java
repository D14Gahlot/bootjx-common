package com.boot.jx.contak.api;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.doc.ContakTemplateDoc;
import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.contak.dto.ContakTemplate;
import com.boot.jx.contak.dto.PhoneNotpRequestModels.ContakMessgaeTemplate;
import com.boot.jx.contak.dto.PhoneNotpRequestModels.PhoneNotpDto;
import com.boot.jx.contak.manager.ContakApiContext;
import com.boot.jx.contak.manager.FirebaseManager;
import com.boot.jx.exception.ApiHttpExceptions.ApiStatusCodes;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.postman.PMConstants.ParamKeys;
import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;
import com.boot.utils.ArgUtil;

@RestController
@RequestMapping("/client")
public class NodeClientController {

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Autowired
	private FirebaseManager firebaseManager;

	@Autowired
	private ContakApiContext apiContext;

	@ApiRequest(authenticateTenant = true)
	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/api/v1/messages/send", method = { RequestMethod.POST })
	public ApiResponse<ContakMessageDoc, Object> send(@RequestBody PhoneNotpDto msg) {

		CompanyDoc compoc = apiContext.getCompany();

		if (!ArgUtil.is(compoc)) {
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
		if (msg.loginToken != null) {
			if (!msg.loginToken.equalsIgnoreCase(loginToken)) {
				ApiResponseUtil.throwInputException(ApiStatusCodes.HANDSHAKE_REQUIRED,
						new ApiFieldError().field("userLoginToken : " + msg.loginToken + " " + loginToken));
			}
		}

		ContakMessageDoc newPhoneNOTPDoc = new ContakMessageDoc();
		newPhoneNOTPDoc.setDomain(compoc.getDisplayName());
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

		if (ArgUtil.is(msg.template) && ArgUtil.is(msg.template.code)) {
			ContakTemplate tmpl = commonMongoTemplate.collection(ContakTemplateDoc.class)
					.find(Criteria.where("code").is(msg.template.code).and("companyId").is(compoc.getCompanyId()))
					.asFirst(new ContakTemplate());
			if (ArgUtil.not(tmpl)) {
				ApiResponseUtil
						.throwInputException(new ApiFieldError().field("template").description("Invalid Template"));
			}
			newPhoneNOTPDoc.modelEncrypted = msg.template.modelEncrypted;
			newPhoneNOTPDoc.model = msg.template.model;
			newPhoneNOTPDoc.setTemplate(tmpl);
			newPhoneNOTPDoc.setType(tmpl.type);
		}
		commonMongoTemplate.save(newPhoneNOTPDoc);

		// Notification
		String title = compoc.getDisplayName();
		String body = "You have received a notification from " + compoc.getDisplayName();
		HashMap<String, String> data = new HashMap<>();
		if (msg.companyName != null) {
			data.put("companyName", compoc.getDomain());
		}
		if (msg.companyId != null) {
			data.put("companyId", compoc.getCompanyId());
		}
		if (msg.pubKey != null) {
			data.put("pubKey", msg.pubKey);
		}
		if (msg.type != null) {
			data.put("type", msg.type);
		}
		firebaseManager.sendNotification(msg.phone, title, body, data);

		return ApiResponse.buildResults(newPhoneNOTPDoc);
	}

	@ApiRequest(authenticateTenant = true)
	@ApiMockParams({ @ApiMockParam(name = ParamKeys.X_API_KEY, value = "API Key", paramType = MockParamType.HEADER),
			@ApiMockParam(name = ParamKeys.X_API_ID, value = "API Id", paramType = MockParamType.HEADER) })
	@RequestMapping(value = "/api/v1/messages/template", method = { RequestMethod.POST })
	public ApiResponse<ContakTemplate, Object> template(@RequestBody ContakMessgaeTemplate template) {
		CompanyDoc compoc = apiContext.getCompany();
		ContakTemplate tmpl = commonMongoTemplate.collection(ContakTemplateDoc.class)
				.find(Criteria.where("code").is(template.code).and("companyId").is(compoc.getCompanyId()))
				.asFirst(new ContakTemplate());
		if (ArgUtil.not(tmpl)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("template").description("Invalid Template"));
		}
		return ApiResponse.buildResults(tmpl);
	}
}