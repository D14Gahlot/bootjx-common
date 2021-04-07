package com.boot.jx.chat;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Component
public class ChatArchive {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	public ContactDTO getContact(ChatSessionDTO chatSessionDto) {
		ChatContactDoc contact = mongoTemplate.findById(chatSessionDto.getContactId(), ChatContactDoc.class);
		return ChatDTOUtil.getContactDTO(contact);
	}

	public ChatSessionDTO withContact(ChatSessionDTO chatSessionDto) {

		ContactDTO contact = getContact(chatSessionDto);
		chatSessionDto.setContact(contact);

		chatSessionDto.setContactType(contact.getContactType());
		chatSessionDto.setName(contact.getName());
		chatSessionDto.setProfilePic(contact.getProfilePic());
		chatSessionDto.setEmail(contact.getEmail());
		chatSessionDto.setPhone(contact.getPhone());
		chatSessionDto.setContactId(contact.getContactId());

		return chatSessionDto;
	}

	public ChatSessionDTO getChatSession(String sessionId) {
		return ChatDTOUtil.getChatSessionDTO(sessionStore.getSession(sessionId));
	}

	public List<ChatMessageDTO> getMessages(ChatSessionDTO chatSessionDto) {
		if (ArgUtil.isEmpty(chatSessionDto.getContactType())) {
			chatSessionDto = withContact(chatSessionDto);
		}

		List<MessageDoc> messages = messageStore.findBySessionId(chatSessionDto.getSessionId(),
				chatSessionDto.getContactType());
		List<ChatMessageDTO> messageDtos = new ArrayList<ChatMessageDTO>();
		for (MessageDoc messageDoc : messages) {
			ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc);
			if (ArgUtil.areEqual(messageDoc.getType(), "I")) {
				messageDto.setName(chatSessionDto.getName());
			} else {
				messageDto.setName(messageDoc.getAgent());
			}
			messageDtos.add(messageDto);
		}
		return messageDtos;
	}

	public ChatSessionDTO withContact(ChatSessionDoc chatSessionDoc) {
		ChatSessionDTO chatSessionDto = ChatDTOUtil.getChatSessionDTO(chatSessionDoc);
		return withContact(chatSessionDto);
	}

	public ChatSessionDTO withMessages(ChatSessionDTO chatSessionDto) {
		List<ChatMessageDTO> messageDtos = getMessages(chatSessionDto);
		chatSessionDto.setMessages(messageDtos);
		return chatSessionDto;
	}

	@Deprecated
	public ChatSessionDTO getChatSessionDto(ChatSessionDoc chatSessionDoc, String agentCode) {
		ChatSessionDTO chatSessionDto = withContact(chatSessionDoc);

		chatSessionDto.setAssigned(ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)
				&& ArgUtil.isNone(chatSessionDoc.getResolveSessionStamp()));
		chatSessionDto = withMessages(chatSessionDto);
		return chatSessionDto;
	}

	@Deprecated
	public ChatSessionDTO getChatSessionDto(ChatSessionDTO chatSessionDto) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(chatSessionDto.getSessionId());
		return getChatSessionDto(sessionDoc, null);
	}
}
