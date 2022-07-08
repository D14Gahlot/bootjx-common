package com.boot.jx.contak.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.contak.ContakConstants.ApiDeviceHeaders;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneContactDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.dto.MobileUserDTO;
import com.boot.jx.phonebook.dto.MobileUserLoginDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
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
	@RequestMapping(value = "/api/v1/login/device", method = { RequestMethod.POST })
	public ApiResponse<PhoneContactDoc, MobileUserDTO> login(@RequestBody MobileUserLoginDTO login,
			@RequestHeader(value = "x-device-id") String deviceId) {

		PhoneUserDoc x = commonMongoTemplate.findById(login.getMobile(), PhoneUserDoc.class);

		if (!ArgUtil.is(x)) {
			x = new PhoneUserDoc();
			x.setMobile(login.getMobile());
		}
		x.setAuthToken(UniqueID.generateSessionId());
		commonMongoTemplate.save(x);

		MobileUserDTO user = new MobileUserDTO();
		user.setAuthToken(x.getAuthToken());
		user.setName(x.getName());
		user.setMobile(x.getMobile());

		List<PhoneContactDoc> userContacts = phoneBookManager.getContacts(x);

		return ApiResponse.buildResults(userContacts, user);
	}

}