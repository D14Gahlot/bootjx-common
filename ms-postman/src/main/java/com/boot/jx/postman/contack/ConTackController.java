package com.boot.jx.postman.contack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.contack.ContackConstants.ApiDeviceHeaders;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.UniqueID;

@RestController
@RequestMapping("/ext")
public class ConTackController {

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	MongoTemplate mongoTemplate;

	@Autowired
	ContackService goCoRoService;

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/login/device", method = { RequestMethod.POST })
	public ApiResponse<MobileUserContactDTO, MobileUserDTO> login(@RequestBody MobileUserLoginDTO login,
			@RequestHeader(value = "x-device-id") String deviceId) {

		MobileUserDoc x = mongoTemplate.findById(login.getMobile(), MobileUserDoc.class);

		if (!ArgUtil.is(x)) {
			x = new MobileUserDoc();
			x.setMobile(login.getMobile());
		}
		x.setAuthToken(UniqueID.generateSessionId());
		mongoTemplate.save(x);

		MobileUserDTO user = new MobileUserDTO();
		user.setAuthToken(x.getAuthToken());
		user.setName(x.getName());
		user.setMobile(x.getMobile());
		user.setScore(x.getScore());
		user.setPositive(x.getPositive());

		ArrayList<MobileUserContactDTO> contacts = new ArrayList<MobileUserContactDTO>();
		if (ArgUtil.is(x.getContacts())) {
			for (Entry<String, MobileUserContactDoc> safeContactDoc : x.getContacts().entrySet()) {
				contacts.add(EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new MobileUserContactDTO()));
			}
		}
		return ApiResponse.buildResults(contacts, user);
	}

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/contact/sync", method = { RequestMethod.POST })
	public ApiResponse<MobileUserContactDTO, MobileUserDTO> sync(@RequestHeader(value = "x-mobile-no") String mobileNo,
			@RequestHeader(value = "x-device-id") String deviceId,
			@RequestHeader(value = "x-auth-token") String authToken,
			@RequestBody ListRequestModel<MobileUserContactDTO> list) {
		MobileUserDoc safeUserDoc = mongoTemplate.findById(mobileNo, MobileUserDoc.class);
		safeUserDoc.contacts();
		Integer counter = 0;
		for (MobileUserContactDTO contactDto : list.getValues()) {
			MobileUserContactDoc contactDoc = safeUserDoc.getContacts().get(contactDto.getMobile());
			if (!ArgUtil.is(contactDoc)) {
				contactDoc = new MobileUserContactDoc();
			}

			Integer score = contactDoc.getScore();
			contactDoc = EntityDtoUtil.dtoToEntity(contactDto, contactDoc);
			contactDoc.setScore(score);

			contactDto = EntityDtoUtil.entityToDto(contactDoc, contactDto);
			safeUserDoc.getContacts().put(contactDto.getMobile(), contactDoc);
			goCoRoService.pullContactUpdate(safeUserDoc, contactDto);
			counter++;
		}
		mongoTemplate.save(safeUserDoc);
		MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
		return ApiResponse.buildResults(list.getValues(), safeUserDTO);
	}

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/contact/update", method = { RequestMethod.POST })
	public ApiResponse<MobileUserContactDTO, Object> update(@RequestHeader(value = "x-mobile-no") String mobileNo,
			@RequestHeader(value = "x-device-id") String deviceId,
			@RequestHeader(value = "x-auth-token") String authToken, @RequestBody MobileUserContactDTO contact) {
		MobileUserDoc safeUserDoc = mongoTemplate.findById(mobileNo, MobileUserDoc.class);
		safeUserDoc.contacts();
		MobileUserContactDTO contactDto = contact;
		MobileUserContactDoc contactDoc = safeUserDoc.getContacts().get(contactDto.getMobile());
		if (!ArgUtil.is(contactDoc)) {
			contactDoc = new MobileUserContactDoc();
		}

		Integer score = contactDoc.getScore();
		contactDoc = EntityDtoUtil.dtoToEntity(contactDto, contactDoc);
		contactDoc.setScore(score);

		contactDto = EntityDtoUtil.entityToDto(contactDoc, contactDto);
		safeUserDoc.getContacts().put(contactDto.getMobile(), contactDoc);
		goCoRoService.pullContactUpdate(safeUserDoc, contactDto);
		mongoTemplate.save(safeUserDoc);
		MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
		return ApiResponse.buildResults(contact, safeUserDTO);
	}

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/contact/search", method = { RequestMethod.POST })
	public ApiResponse<MobileUserContactDTO, Object> search(@RequestHeader(value = "x-mobile-no") String mobileNo,
			@RequestHeader(value = "x-device-id") String deviceId,
			@RequestHeader(value = "x-auth-token") String authToken, @RequestBody MobileUserContactDTO contact) {
		MobileUserDoc safeUserDoc = mongoTemplate.findById(mobileNo, MobileUserDoc.class);
		List<MobileUserContactDTO> contacts = new ArrayList<MobileUserContactDTO>();

		if (ArgUtil.is(safeUserDoc)) {
			if (ArgUtil.is(contact.getMobile())) {
				if (ArgUtil.is(safeUserDoc.getContacts())) {
					for (Entry<String, MobileUserContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
						if (ArgUtil.isEqual(safeContactDoc.getValue().getMobile(), contact.getMobile())) {
							contacts.add(
									EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new MobileUserContactDTO()));
						}
					}
				}
				if (ArgUtil.isEmpty(contacts) || contacts.size() < 1) {
					MobileUserDoc safeUserDoc2 = mongoTemplate.findById(contact.getMobile(), MobileUserDoc.class);
					if (ArgUtil.is(safeUserDoc2)) {
						contacts.add(EntityDtoUtil.entityToDto(safeUserDoc2, contact));
					}
				}
			} else if (ArgUtil.is(contact.getName())) {
				if (ArgUtil.is(safeUserDoc.getContacts())) {
					for (Entry<String, MobileUserContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
						if (safeContactDoc.getValue().getName().toLowerCase()
								.contains(contact.getName().toLowerCase())) {
							contacts.add(
									EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new MobileUserContactDTO()));
						}
					}
				}
			} else if (ArgUtil.is(safeUserDoc.getContacts())) {
				for (Entry<String, MobileUserContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
					contacts.add(EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new MobileUserContactDTO()));
				}
			}
		}
		return ApiResponse.buildResults(contacts);
	}

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/tests/update", method = { RequestMethod.POST })
	public ApiResponse<MobileUserTestDTO, Object> updateTest(@RequestHeader(value = "x-mobile-no") String mobileNo,
			@RequestHeader(value = "x-device-id") String deviceId,
			@RequestHeader(value = "x-auth-token") String authToken,
			@RequestBody ListRequestModel<MobileUserTestDTO> list) {
		MobileUserDoc safeUserDoc = mongoTemplate.findById(mobileNo, MobileUserDoc.class);

		List<MobileUserTestDoc> tests = new ArrayList<MobileUserTestDoc>();
		Boolean positive = null;
		for (MobileUserTestDTO testDto : list.getValues()) {
			MobileUserTestDoc testDoc = new MobileUserTestDoc();
			testDoc = EntityDtoUtil.dtoToEntity(testDto, testDoc);
			testDto = EntityDtoUtil.entityToDto(testDoc, testDto);
			tests.add(testDoc);
			positive = testDoc.getPositive();
		}
		safeUserDoc.setTests(tests);
		safeUserDoc.setPositive(positive);

		if (ArgUtil.nullAsFalse(positive)) {
			safeUserDoc.setScore(0);
		}

		mongoTemplate.save(safeUserDoc);

		if (ArgUtil.nullAsFalse(safeUserDoc.getPositive())) {
			for (Entry<String, MobileUserContactDoc> safeContactDocEntry : safeUserDoc.getContacts().entrySet()) {
				MobileUserContactDoc safeContactDoc = safeContactDocEntry.getValue();
				goCoRoService.pushContactUser(safeUserDoc, safeContactDoc);
			}
		}

		MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
		return ApiResponse.buildResults(list.getValues(), safeUserDTO);
	}

	@ApiDeviceHeaders
	@RequestMapping(value = "/api/v1/tests/fetch", method = { RequestMethod.GET })
	public ApiResponse<MobileUserTestDTO, Object> myTests(@RequestHeader(value = "x-mobile-no") String mobileNo,
			@RequestHeader(value = "x-device-id") String deviceId,
			@RequestHeader(value = "x-auth-token") String authTokent) {
		MobileUserDoc safeUserDoc = mongoTemplate.findById(mobileNo, MobileUserDoc.class);
		safeUserDoc.tests();
		List<MobileUserTestDTO> tests = new ArrayList<MobileUserTestDTO>();
		for (MobileUserTestDoc testDoc : safeUserDoc.getTests()) {
			MobileUserTestDTO testDto = new MobileUserTestDTO();
			testDto = EntityDtoUtil.entityToDto(testDoc, testDto);
			tests.add(testDto);
		}
		MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
		return ApiResponse.buildResults(tests, safeUserDTO);
	}
}