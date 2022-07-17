package com.boot.jx.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ChatService;
import com.boot.jx.inbound.InBound.InBoundFilter;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessagePrompt;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.store.MessageStore;
import com.boot.utils.ArgUtil;

@Component
public class DefaultInBoundFilter implements InBoundFilter {

    @Autowired
    private ChatService chatService;

    @Autowired
    MessageStore messageStore;

    @Override
    public boolean doFilter(InboxMessage inboxMessage) {

	if (ArgUtil.is(inboxMessage.getPrompt())) {
	    if (MessagePrompt.TYPE.MOREOPTIONS.equals(inboxMessage.getPrompt().type)) {
		try {
		    MessageDoc msg = messageStore.findByMessageId(inboxMessage.getPrompt().messageId,
			    inboxMessage.contact().getContactType());
		    String templateId = msg.getHsm().getId();
		    OutboxMessage om = inboxMessage.replyMessage("More Options").templateId(templateId)
			    .model(msg.getModel()).prompt(inboxMessage.getPrompt());
		    chatService.reply(inboxMessage, om);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		return false;
	    }
	}

	return true;
    }

}
