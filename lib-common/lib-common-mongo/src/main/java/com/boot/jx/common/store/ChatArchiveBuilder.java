package com.boot.jx.common.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class ChatArchiveBuilder {

    @Autowired
    private ChatArchiveService chatArchive;

    public ChatSessionDTOBuilder sessionDTO() {
	return new ChatSessionDTOBuilder().archive(chatArchive);
    }

    public static class ChatSessionDTOBuilder {
	private ChatArchiveService archive;

	private ChatSessionDTO chatSessionDTO;
	private ChatSessionDoc chatSessionDoc;

	public ChatSessionDTOBuilder archive(ChatArchiveService archive) {
	    this.archive = archive;
	    return this;
	}

	public ChatSessionDTOBuilder from(ChatSessionDoc chatSessionDoc) {
	    this.chatSessionDoc = chatSessionDoc;
	    this.chatSessionDTO = ChatDTOUtil.getChatSessionDTO(chatSessionDoc);
	    return this;
	}

	public ChatSessionDTOBuilder from(String sessionId) {
	    return this.from(archive.getChatSessionDoc(sessionId));
	}

	public ChatSessionDTOBuilder withMessages() {
	    List<ChatMessageDTO> messageDtos = archive.getMessages(chatSessionDTO);
	    chatSessionDTO.setMessages(messageDtos);
	    return this;
	}

	public ChatSessionDTOBuilder withContact() {
	    chatSessionDTO = archive.withContact(chatSessionDTO);
	    return this;
	}

	public ChatSessionDTOBuilder isAssigned(String agentCode) {
	    chatSessionDTO.setAssigned(ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)
		    && ArgUtil.isEmptyValue(chatSessionDoc.getResolveSessionStamp()));
	    return this;
	}

	public ChatSessionDTO get() {
	    return this.chatSessionDTO;
	}

	public ChatSessionDTOBuilder addMessage(MessageDoc messageDoc) {
	    ChatMessageDTO messageDto = archive.getMessage(messageDoc, chatSessionDTO);
	    if (!ArgUtil.is(chatSessionDTO.getMessages())) {
		chatSessionDTO.setMessages(CollectionUtil.getList(messageDto));
	    } else {
		chatSessionDTO.getMessages().add(messageDto);
	    }
	    return this;
	}

    }
}
