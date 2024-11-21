package com.boot.jx.outbound;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppConfig;
import com.boot.jx.cdn.BootJxConfigService;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMCommonConfig;
import com.boot.jx.postman.client.CommonServiceClient;
import com.boot.jx.postman.model.ext.SessionBoundEvent;

@RestController
@RequestMapping("/outbound")
public class OutBoundController {

	private static final Logger LOGGER = LoggerFactory.getLogger(OutBoundController.class);

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired(required = false)
	private PMCommonConfig pmCommonConfig;

	@Autowired(required = false)
	private BootJxConfigService bootJxConfigService;

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private MessageService messageService;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Autowired
	private CommonServiceClient commonServiceClient;

	@RequestMapping(value = "/chrono/session-event-timer/api/v1/message/status", method = RequestMethod.GET)
	public SessionBoundEvent publishSessionBoundEventMessage(Model model, HttpServletRequest request,
			@RequestBody SessionBoundEvent event) throws InterruptedException {
		commonServiceClient.publishSessionBoundEvent(event);
		return event;
	}

	@RequestMapping(value = "/chrono/session-event-timer/api/v1/message/in-out", method = RequestMethod.GET)
	public SessionBoundEvent publishSessionBoundEventStatus(Model model, HttpServletRequest request,
			@RequestBody SessionBoundEvent event) throws InterruptedException {
		commonServiceClient.publishSessionBoundEvent(event);
		return event;
	}

}
