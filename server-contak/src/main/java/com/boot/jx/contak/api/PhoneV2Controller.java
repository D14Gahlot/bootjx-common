package com.boot.jx.contak.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.contak.doc.ContakMessageDoc;
import com.boot.jx.contak.dto.PhoneLoginDTO;
import com.boot.jx.contak.manager.ContakMessageManager;
import com.boot.jx.contak.service.PhoneAuthService;
import com.boot.jx.phonebook.doc.PhoneUserDoc;

@RestController
@RequestMapping("/phone/api/v2")
public class PhoneV2Controller {

	@Autowired
	private ContakMessageManager contakMessageManager;

	@Autowired
	private PhoneAuthService phoneAuthService;

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

}