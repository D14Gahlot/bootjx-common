package com.boot.jx.agent.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.AppConfig;
import com.boot.jx.agent.AgentSessionBean;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.store.SessionStore;

@Controller
public class MsgController {

	@Autowired
	AppConfig appConfig;

	@Autowired
	SessionStore sessionStore;

	@Autowired
	AgentSessionBean agentSession;

	@ResponseBody
	@RequestMapping(value = "/api/sessions/assigned", method = { RequestMethod.GET })
	public List<ChatSessionDoc> getSessionsAssignedToMe() {
		return sessionStore.findChatSessionDocByAgent(agentSession.getAgentCode());
	}

}
