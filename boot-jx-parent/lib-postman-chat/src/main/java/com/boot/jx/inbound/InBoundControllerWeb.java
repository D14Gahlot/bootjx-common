package com.boot.jx.inbound;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.agent.AgentChatHandler;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.connectors.WebConnector;
import com.boot.jx.dict.ContactType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.http.RequestType;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Controller
public class InBoundControllerWeb {

	@Autowired
	private InBoundService inBoundEngine;

	@Autowired(required = false)
	private WebConnector dummyConnector;

	@Autowired(required = false)
	private AgentChatHandler agentChatHandler;

	@Autowired
	CommonHttpRequest commonHttpRequest;

	@Autowired
	AppConfig appConfig;

	@Autowired
	SessionStore sessionStore;

	@Autowired
	MessageStore messageStore;

	@Autowired
	WebConnector webConnector;

	@ApiRequest(type = RequestType.POLL)
	@ResponseBody
	@RequestMapping(value = "/ext/outbound/web/callback", method = RequestMethod.GET)
	public OutboxMessage onReceiveMessage(@RequestParam String number) throws InterruptedException {
		return dummyConnector.pollUnreadMessage(number);
	}

	@ResponseBody
	@RequestMapping(value = "/ext/outbound/web/auth", method = RequestMethod.GET)
	public ApiResponse<OutboxMessage, Object> onAuth(@RequestParam String number) throws InterruptedException {
		String webSessionId = commonHttpRequest.get("web-session-id");
		String contactId = PostManUtil.createContactId(ContactType.WEBSITE, number);

		ChatSessionDoc session = null;
		if (ArgUtil.is(webSessionId)) {
			session = sessionStore.getValidSession(webSessionId);
		}
		List<OutboxMessage> msgs = new ArrayList<OutboxMessage>();
		if (ArgUtil.is(session)) {
			List<MessageDoc> messages = messageStore.findBySessionId(webSessionId, ContactType.WEBSITE.toString());
			for (MessageDoc messageDoc : messages) {
				OutboxMessage outboxMessage = new OutboxMessage();
				outboxMessage.setTimestamp(messageDoc.getTimestamp());
				outboxMessage.setMessage(messageDoc.getMessage());
				outboxMessage.setTemplate(messageDoc.getTemplate());
				outboxMessage.setAttachments(messageDoc.getAttachments());
				if (ArgUtil.isEqual(messageDoc.getType(), "I")) {
					outboxMessage.addTo(messageDoc.getContactId());
				} else {
					// webConnector.process(outboxMessage);
				}
				msgs.add(outboxMessage);
			}
		}

		return ApiResponse.buildResults(msgs);
	}

	@ResponseBody
	@RequestMapping(value = "/ext/inbound/web/callback", method = RequestMethod.POST)
	public InboxMessage onReceiveMessage(@RequestBody InboxMessage event) throws InterruptedException {
		event.setContactType(ContactType.WEBSITE);
		event.setLane("DUMMY");

		// Cleaning
		// event.setSessionId("600edc822743742e916202b9");
		event.setSessionId(null);
		event.setMessageId(null);
		event.session().setAgent(null);
		event.session().setDept(null);
		inBoundEngine.invokeMethods(event);

		String webSessionId = commonHttpRequest.get("web-session-id");
		if (!ArgUtil.is(webSessionId) || !webSessionId.equalsIgnoreCase(event.getSessionId())) {
			commonHttpRequest.setCookie("web-session-id", event.getSessionId());
		}
		return event;
	}

}
