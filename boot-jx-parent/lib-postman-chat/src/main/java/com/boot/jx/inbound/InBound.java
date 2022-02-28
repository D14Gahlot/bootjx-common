package com.boot.jx.inbound;

import org.springframework.scheduling.annotation.Async;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.ext.InBoundEvent;

public class InBound {

    public interface InBoundProcessor {

	public InboxMessage process(InboxMessage inboxMessage);
    }

    public interface InBoundFilter {

	public boolean doFilter(InboxMessage inboxMessage);
    }

    public interface InBoundHandler {

	public void doHandle(InboxMessage inboxMessage);

	@Async
	default public void handleAsync(InboxMessage inboxMessage) {
	    this.doHandle(inboxMessage);
	}

	public void doHandle(MessageReport messageReport);

	void onSessionRoute(ChatSessionDoc sessionDoc, InBoundEvent inBoundEvent);

	@Async
	default public void onSessionRouteAsync(ChatSessionDoc sessionDoc, InBoundEvent inBoundEvent) {
	    this.onSessionRoute(sessionDoc, inBoundEvent);
	}

	public void onSessionClose(ChatSessionDoc chatSessionDoc);

	public void onSessionInit(InBoundEvent event, ChatSessionDoc chatSessionDoc);

    }

}
