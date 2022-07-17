package com.boot.jx.agent;

import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.inbound.InBound.SessionAssginHandler;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.SessionStore;
import com.boot.model.MapModel.NodeEntry;

public class AssignAgentHandler implements SessionAssginHandler {

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private AgentChatHandlerImpl agentChatHandlerImpl;

    @Override
    public NodeEntry<InBoundEvent> doAssignAgent(PMArgs params) {
//	NodeEntry<InBoundEvent> eventEntry = new NodeEntry<InBoundEvent>();
//	InBoundEvent agentAssignEvent = new InBoundEvent();
//	ChatSessionDoc chatSessionDoc = sessionStore.getSession(params.getSessionId());
//	agentAssignEvent.eventCode = InBoundEvent.SESSION_ASSIGNED;
//	agentAssignEvent.sessionAssigned().oldAgent = chatSessionDoc.getAssignedToAgent();
//	agentAssignEvent.sessionAssigned().oldDept = chatSessionDoc.getAssignedToDept();
//	params = agentChatHandlerImpl.onAssign(chatSessionDoc, params);
//	if (ArgUtil.is(params)) {
//	    agentAssignEvent.sessionAssigned().newDept = params.getAssignToDeptCode();
//	    agentAssignEvent.sessionAssigned().newAgent = params.getAssignToAgentCode();
//	}
//	return eventEntry.value(agentAssignEvent);
	return null;
    }

}
