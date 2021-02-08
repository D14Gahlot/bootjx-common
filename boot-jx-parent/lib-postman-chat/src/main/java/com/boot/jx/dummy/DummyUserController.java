package com.boot.jx.dummy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
public class DummyUserController {

	@Autowired
	private InBoundService inBoundEngine;

	@Autowired(required = false)
	private WebConnector dummyConnector;

	@Autowired
	AppConfig appConfig;

	@ResponseBody
	@RequestMapping(value = "/dummy/messages", method = RequestMethod.GET)
	public OutboxMessage onReceiveMessage(@RequestParam String number) throws InterruptedException {
		return dummyConnector.pollUnreadMessage(number);
	}

	@ResponseBody
	@RequestMapping(value = "/dummy/messages", method = RequestMethod.POST)
	public InboxMessage onReceiveMessage(@RequestParam String message, @RequestParam String number)
			throws InterruptedException {
		InboxMessage event = new InboxMessage();
		event.setContactType(ContactType.WEBSITE);
		event.setLane("DUMMY");
		event.from(number);
		event.setMessage(message);
		inBoundEngine.invokeMethods(event);
		return event;
	}

	@RequestMapping(value = "/dummy/user", method = RequestMethod.GET)
	public String dummyUser(@RequestParam String number, Model model) throws InterruptedException {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		return "dummyuser";
	}

	@RequestMapping(value = "/dummy/customer", method = RequestMethod.GET)
	public String dummyCustomer(Model model) throws InterruptedException {
		model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
		return "customer.plugin.bubble";
	}
}
