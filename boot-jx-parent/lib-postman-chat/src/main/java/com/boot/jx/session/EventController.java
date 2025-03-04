package com.boot.jx.session;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.model.ext.SessionBoundEvent;

@RestController
@RequestMapping("/events")
public class EventController {

	private static final Logger LOGGER = LoggerFactory.getLogger(EventController.class);

	@Autowired
	private ChatSessionService chatSessionService;

	@RequestMapping(value = "/api/v1/session/status/close", method = RequestMethod.POST)
	public ApiResponse<InBoundEvent, Object> closeSessionBySessionId(Model model, HttpServletRequest request,
			@RequestBody SessionBoundEvent event) throws InterruptedException {
		return ApiResponse.buildResult(chatSessionService.closeSession(event.getSessionId()).value());
	}

}
