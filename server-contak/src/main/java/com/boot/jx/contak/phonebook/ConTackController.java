package com.boot.jx.contak.phonebook;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.contak.phonebook.ContackConstants.ApiDeviceHeaders;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.phonebook.doc.PhoneContactDoc;
import com.boot.jx.phonebook.doc.PhoneUserDoc;
import com.boot.jx.phonebook.dto.MobileUserDTO;
import com.boot.jx.phonebook.dto.MobileUserLoginDTO;
import com.boot.jx.phonebook.manager.PhoneBookManager;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.UniqueID;

@RestController
@RequestMapping("/phonebook")
public class ConTackController {

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
//
//    @ApiDeviceHeaders
//    @RequestMapping(value = "/api/v1/contact/sync", method = { RequestMethod.POST })
//    public ApiResponse<PhoneContactDoc, MobileUserDTO> sync(@RequestHeader(value = "x-mobile-no") String mobileNo,
//	    @RequestHeader(value = "x-device-id") String deviceId,
//	    @RequestHeader(value = "x-auth-token") String authToken,
//	    @RequestBody ListRequestModel<PhoneContactDoc> list) {
//
//	PhoneUserDoc safeUserDoc = commonMongoTemplate.findById(mobileNo, PhoneUserDoc.class);
//
//	List<PhoneContactDoc> contacts = phoneBookManager.getContacts(safeUserDoc);
//
//	Integer counter = 0;
//	for (PhoneContactDoc contactDto : list.getValues()) {
//	    PhoneContactDoc contactDoc = safeUserDoc.getContacts().get(contactDto.getMobile());
//	    if (!ArgUtil.is(contactDoc)) {
//		contactDoc = new PhoneContactDoc();
//	    }
//
//	    Integer score = contactDoc.getScore();
//	    contactDoc = EntityDtoUtil.dtoToEntity(contactDto, contactDoc);
//	    contactDoc.setScore(score);
//
//	    contactDto = EntityDtoUtil.entityToDto(contactDoc, contactDto);
//	    safeUserDoc.getContacts().put(contactDto.getMobile(), contactDoc);
//	    goCoRoService.pullContactUpdate(safeUserDoc, contactDto);
//	    counter++;
//	}
//	commonMongoTemplate.save(safeUserDoc);
//	MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
//	return ApiResponse.buildResults(list.getValues(), safeUserDTO);
//    }
//
//    @ApiDeviceHeaders
//    @RequestMapping(value = "/api/v1/contact/update", method = { RequestMethod.POST })
//    public ApiResponse<PhoneContactDoc, Object> update(@RequestHeader(value = "x-mobile-no") String mobileNo,
//	    @RequestHeader(value = "x-device-id") String deviceId,
//	    @RequestHeader(value = "x-auth-token") String authToken, @RequestBody PhoneContactDoc contact) {
//	PhoneContactDoc safeUserDoc = commonMongoTemplate.findById(mobileNo, PhoneContactDoc.class);
//	safeUserDoc.contacts();
//	PhoneContactDoc contactDto = contact;
//	PhoneContactDoc contactDoc = safeUserDoc.getContacts().get(contactDto.getMobile());
//	if (!ArgUtil.is(contactDoc)) {
//	    contactDoc = new PhoneContactDoc();
//	}
//
//	Integer score = contactDoc.getScore();
//	contactDoc = EntityDtoUtil.dtoToEntity(contactDto, contactDoc);
//	contactDoc.setScore(score);
//
//	contactDto = EntityDtoUtil.entityToDto(contactDoc, contactDto);
//	safeUserDoc.getContacts().put(contactDto.getMobile(), contactDoc);
//	goCoRoService.pullContactUpdate(safeUserDoc, contactDto);
//	mongoTemplate.save(safeUserDoc);
//	MobileUserDTO safeUserDTO = EntityDtoUtil.entityToDto(safeUserDoc, new MobileUserDTO());
//	return ApiResponse.buildResults(contact, safeUserDTO);
//    }
//
//    @ApiDeviceHeaders
//    @RequestMapping(value = "/api/v1/contact/search", method = { RequestMethod.POST })
//    public ApiResponse<PhoneContactDoc, Object> search(@RequestHeader(value = "x-mobile-no") String mobileNo,
//	    @RequestHeader(value = "x-device-id") String deviceId,
//	    @RequestHeader(value = "x-auth-token") String authToken, @RequestBody PhoneContactDoc contact) {
//	PhoneContactDoc safeUserDoc = mongoTemplate.findById(mobileNo, PhoneContactDoc.class);
//	List<PhoneContactDoc> contacts = new ArrayList<PhoneContactDoc>();
//
//	if (ArgUtil.is(safeUserDoc)) {
//	    if (ArgUtil.is(contact.getMobile())) {
//		if (ArgUtil.is(safeUserDoc.getContacts())) {
//		    for (Entry<String, PhoneContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
//			if (ArgUtil.isEqual(safeContactDoc.getValue().getMobile(), contact.getMobile())) {
//			    contacts.add(EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new PhoneContactDoc()));
//			}
//		    }
//		}
//		if (ArgUtil.isEmpty(contacts) || contacts.size() < 1) {
//		    PhoneContactDoc safeUserDoc2 = mongoTemplate.findById(contact.getMobile(), PhoneContactDoc.class);
//		    if (ArgUtil.is(safeUserDoc2)) {
//			contacts.add(EntityDtoUtil.entityToDto(safeUserDoc2, contact));
//		    }
//		}
//	    } else if (ArgUtil.is(contact.getName())) {
//		if (ArgUtil.is(safeUserDoc.getContacts())) {
//		    for (Entry<String, PhoneContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
//			if (safeContactDoc.getValue().getName().toLowerCase()
//				.contains(contact.getName().toLowerCase())) {
//			    contacts.add(EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new PhoneContactDoc()));
//			}
//		    }
//		}
//	    } else if (ArgUtil.is(safeUserDoc.getContacts())) {
//		for (Entry<String, PhoneContactDoc> safeContactDoc : safeUserDoc.getContacts().entrySet()) {
//		    contacts.add(EntityDtoUtil.entityToDto(safeContactDoc.getValue(), new PhoneContactDoc()));
//		}
//	    }
//	}
//	return ApiResponse.buildResults(contacts);
//    }

}