package com.boot.jx.inbound;

import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageReport;

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
    }

}
