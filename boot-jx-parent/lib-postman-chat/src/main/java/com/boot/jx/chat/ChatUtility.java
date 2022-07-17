package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.utils.ArgUtil;

@Component
public class ChatUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatUtility.class);

	public boolean isPushOnly(ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.isEmptyValue(chatSessionDoc.getAssignedToQueue())) {
			return true;
		}
		if (PMConstants.CHAT_MODE.isPushOnly(chatSessionDoc.getMode())) {
			return true;
		}
		return false;
	}

	public boolean inQueue(ChatSessionDoc chatSessionDoc) {
		return ArgUtil.is(chatSessionDoc.getAssignedToQueue());
	}

}
