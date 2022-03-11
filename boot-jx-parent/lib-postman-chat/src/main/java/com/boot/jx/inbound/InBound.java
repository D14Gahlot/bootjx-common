package com.boot.jx.inbound;

import org.springframework.scheduling.annotation.Async;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.PMParams;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.model.MapModel.NodeEntry;

public class InBound {

    public interface InBoundProcessor {

	public InboxMessage process(InboxMessage inboxMessage);
    }

    public interface InBoundFilter {

	public boolean doFilter(InboxMessage inboxMessage);
    }

    public interface InBoundHandler {

	public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session);

	@Async
	default public void onMessageAsync(InboxMessage inboxMessage, ChatSessionDoc session) {
	    this.onMessage(inboxMessage, session);
	}

	public void doHandle(MessageReport messageReport);

	void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc);

	@Async
	default public void onSessionRouteAsync(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc) {
	    this.onSessionRoute(inBoundEvent, sessionDoc);
	}

	public void onSessionResolve(InBoundEvent event, ChatSessionDoc chatSessionDoc);

	@Async
	default public void onSessionResolveAsync(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc) {
	    this.onSessionResolve(inBoundEvent, sessionDoc);
	}

	public void onSessionClose(InBoundEvent event, ChatSessionDoc chatSessionDoc);

	@Async
	default public void onSessionCloseAsync(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc) {
	    this.onSessionClose(inBoundEvent, sessionDoc);
	}

	public void onSessionInit(InBoundEvent event, ChatSessionDoc chatSessionDoc);

	public NodeEntry<InBoundEvent> assignSessionToAgent(PMParams params);

	public NodeEntry<InBoundEvent> assignSessionToAgent(ChatSessionDoc session, String deptCode, String agentCode);

    }

    public interface SessionAssginHandler {
	public NodeEntry<InBoundEvent> doAssignAgent(PMParams pmParams);
    }

}
