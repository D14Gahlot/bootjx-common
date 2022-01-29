package com.boot.jx.dummy;

import javax.servlet.http.Cookie;

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
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.inbound.InBoundService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Controller
public class DummyUserController {

    @Autowired
    private InBoundService inBoundEngine;

    @Autowired(required = false)
    private WebConnector dummyConnector;

    @Autowired
    AppConfig appConfig;

    @Autowired
    CommonHttpRequest commonHttpRequest;

    @Autowired(required = false)
    private PMCommonConfig pmCommonConfig;

    @Autowired
    private PMEnvironment pmEnvironment;

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
	Cookie cookie = commonHttpRequest.getCookie("contactType");

	ContactType contactType = ContactType.WEBSITE;
	if (ArgUtil.is(cookie)) {
	    contactType = ArgUtil.parseAsEnumT(cookie.getValue(), contactType, ContactType.class);
	}

	event.contact().setContactType(contactType.toString());
	event.contact().setLane("DUMMY");
	event.from(number);
	event.setMessage(message);
	inBoundEngine.invokeMethods(event);
	return event;
    }

    @RequestMapping(value = "/dummy/customer", method = RequestMethod.GET)
    public String dummyCustomer(Model model, @RequestParam(required = false) String contacyType)
	    throws InterruptedException {
	commonHttpRequest.setCookie("contactType", ArgUtil.parseAsString(contacyType, ContactType.WEBSITE.toString()));
	model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR",
		pmEnvironment.keyEntry("postman.agent.scheme.color").asString());
	return "customer.plugin.bubble";
    }

    @RequestMapping(value = "/plugin/customer/**", method = RequestMethod.GET)
    public String pluginCustomer(Model model, @RequestParam(required = false) String contacyType,
	    @RequestParam(required = false, defaultValue = "/plugin/customer") String path)
	    throws InterruptedException {
	commonHttpRequest.setCookie("contactType", ArgUtil.parseAsString(contacyType, ContactType.WEBSITE.toString()));
	model.addAttribute("APP_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());
	model.addAttribute("WEBAPP_BASE", appConfig.getAppPrefix() + path);
	model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR",
		pmEnvironment.keyEntry("postman.agent.scheme.color").asString());

	if (pmCommonConfig != null) {
	    model.addAllAttributes(pmCommonConfig.appAttributes());
	}
	return "app-customer";
    }

    @RequestMapping(value = "/pub/plugin/customer/**", method = RequestMethod.GET)
    public String pluginCustomerPub(Model model, @RequestParam(required = false) String contacyType)
	    throws InterruptedException {
	return pluginCustomer(model, contacyType, "/pub/plugin/customer");
    }

    @RequestMapping(value = { "/dummy/user", "/pub/customer" }, method = RequestMethod.GET)
    public String dummyUser(@RequestParam String number, Model model) throws InterruptedException {
	model.addAttribute("LOCAL_PATH", appConfig.getAppPrefix() + "/pub");
	model.addAttribute("POSTMAN_CONTEXT", appConfig.getAppPrefix());

	if (pmCommonConfig != null) {
	    model.addAllAttributes(pmCommonConfig.appAttributes());
	}

	model.addAttribute("POSTMAN_AGENT_SCHEME_COLOR",
		pmEnvironment.keyEntry("postman.agent.scheme.color").asString());
	if (pmCommonConfig != null) {
	    model.addAttribute("CDN_URL",
		    ArgUtil.parseAsString(commonHttpRequest.get("CDN_URL"), pmCommonConfig.getCdnServer()));
	}
	return "dummyuser";
    }
}
