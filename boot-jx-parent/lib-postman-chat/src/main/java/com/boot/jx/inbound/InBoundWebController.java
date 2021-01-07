package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.connectors.WebConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.inbound.InBoundService;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;

@Controller
public class InBoundWebController {

	@Autowired
	private InBoundService inBoundEngine;

	@Autowired(required = false)
	private WebConnector dummyConnector;

	@Autowired
	AppConfig appConfig;

	@ResponseBody
	@RequestMapping(value = "/ext/outbound/web/callback", method = RequestMethod.GET)
	public OutboxMessage onReceiveMessage(@RequestParam String number) throws InterruptedException {
		return dummyConnector.pollUnreadMessage(number);
	}

	@ResponseBody
	@RequestMapping(value = "/ext/inbound/web/callback", method = RequestMethod.POST)
	public InboxMessage onReceiveMessage(@RequestBody InboxMessage event) throws InterruptedException {
		event.setContactType(ContactType.WEBSITE);
		event.setLane("DUMMY");

		// Cleaning
		event.setSessionId(null);
		event.setMessageId(null);
		event.setAssignedToAgent(null);
		event.setAssignedToDept(null);
		inBoundEngine.invokeMethods(event);
		return event;
	}

}
