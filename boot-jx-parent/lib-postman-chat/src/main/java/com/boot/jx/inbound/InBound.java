package com.boot.jx.inbound;

import org.springframework.scheduling.annotation.Async;

import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.ext.InBoundEvent;

public class InBound {

    public interface InBoundProcessor {

	public InboxMessage process(InboxMessage inboxMessage);
    }

    public interface InBoundFilter {

	public boolean onFilter(InboxMessage inboxMessage);
    }

    public interface InBoundHandler {

	public void handle(InboxMessage inboxMessage);

	public void handle(MessageReport messageReport);

	@Async
	default public void handleAsync(InboxMessage inboxMessage) {
	    this.handle(inboxMessage);
	}

	void handle(InBoundEvent inBoundEvent);

	@Async
	default public void handleAsync(InBoundEvent inBoundEvent) {
	    this.handle(inBoundEvent);
	}
    }

}
