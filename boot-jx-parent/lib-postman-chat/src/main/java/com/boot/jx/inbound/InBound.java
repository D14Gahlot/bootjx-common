package com.boot.jx.inbound;

import org.springframework.scheduling.annotation.Async;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PMArgs;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.model.MapModel.NodeEntry;

public class InBound {

	public interface InBoundProcessor {

		public InboxMessage process(InboxMessage inboxMessage);
	}

	public interface InBoundFilter {

		public boolean doFilter(InboxMessage inboxMessage);
	}

	public interface InBoundHandler {

		public MessageContext context();

		public void onMessage(InboxMessage inboxMessage, ChatSessionDoc session);

		default public void afterMessage(InboxMessage inboxMessage, ChatSessionDoc session) {
		}

		default public void onMessageSync(InboxMessage inboxMessage, ChatSessionDoc session) {
			this.onMessage(inboxMessage, session);
			this.afterMessage(inboxMessage, session);
		}

		@Async
		default public void onMessageAsync(InboxMessage inboxMessage, ChatSessionDoc session) {
			this.onMessage(inboxMessage, session);
			this.afterMessage(inboxMessage, session);
		}

		public void doHandle(MessageReport messageReport);

		public InBoundEvent onSessionEvent(InBoundEvent inBoundEvent, PMArgs pmArgs);

		public void onSessionRoute(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs);

		@Async
		default public void onSessionRouteAsync(InBoundEvent inBoundEvent, ChatSessionDoc sessionDoc, PMArgs pmArgs) {
			this.onSessionRoute(inBoundEvent, sessionDoc, pmArgs);
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

		public NodeEntry<InBoundEvent> assignSessionToAgent(PMArgs params, ChatSessionDoc session);

	}

	public interface SessionAssginHandler {
		public NodeEntry<InBoundEvent> doAssignAgent(PMArgs pmParams);
	}

	public interface ChatSessionEvents {

		public NodeEntry<InBoundEvent> onSessionIdleOutBound(ChatSessionDoc session);

		public NodeEntry<InBoundEvent> onSessionIdleInBound(ChatSessionDoc session);

	}

	public interface MessageEvents {

		public NodeEntry<InBoundEvent> preMessageOutBound(OutboxMessage message);

		public NodeEntry<InBoundEvent> onMessageOutbound(OutboxMessage message);

		public NodeEntry<InBoundEvent> postMessageOutBound(OutboxMessage message);

		public NodeEntry<InBoundEvent> postMessageInBound(InboxMessage message);

	}

}
