package com.boot.jx.common.store;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.QuickTag;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ChatProfileDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;

@Component
public class ChatArchiveService {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	public ContactDTO getContact(ChatSessionDTO chatSessionDto) {
		ChatContactDoc contact = mongoTemplate.findById(chatSessionDto.getContactId(), ChatContactDoc.class);
		ContactDTO dto = ChatDTOUtil.getContactDTO(contact);
		if (ArgUtil.is(contact)) {
			if (ArgUtil.is(contact.getProfileId())) {
				ChatProfileDoc profileDoc = mongoTemplate.findById(contact.getProfileId(),
						ChatProfileDoc.class);
				ChatProfileDTO profileDTO = ChatDTOUtil.getProfileDTO(profileDoc);
				dto.setProfile(profileDTO);
			} else if (ArgUtil.is(contact.getPhone())) {
				Query query = new Query();
				query.addCriteria(Criteria.where("mobile").is(contact.getPhone()));
				ChatProfileDoc profileDoc = mongoTemplate.findOne(query, ChatProfileDoc.class);
				ChatProfileDTO profileDTO = ChatDTOUtil.getProfileDTO(profileDoc);
				dto.setProfile(profileDTO);
			}
		}
		return dto;
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

	public ChatSessionDoc getChatSessionDoc(String sessionId) {
		return sessionStore.getSession(sessionId);
	}

	public ChatSessionDTO getChatSession(String sessionId) {
		
		return ChatDTOUtil.getChatSessionDTO(getChatSessionDoc(sessionId));
	}

	public ChatSessionDTO getChatSession(ChatSessionDTO chatSessionDto) {
		return getChatSession(chatSessionDto.getSessionId());
	}

	public ChatMessageDTO getMessage(MessageDoc messageDoc, ChatSessionDoc chatSessionDoc) {
		if (ArgUtil.is(messageDoc)) {
			return ChatDTOUtil.getChatMessageDTO(messageDoc, chatSessionDoc.getContactName(),
					ArgUtil.nonEmpty(messageDoc.getAgent(), messageDoc.getQueue(), chatSessionDoc.getAssignedToAgent(),
							chatSessionDoc.getAssignedToQueue()));
		}
		return new ChatMessageDTO();
	}

	public ChatMessageDTO createMessageDTO(MessageDoc messageDoc, ChatSessionDTO chatSessionDto) {
		if (!ArgUtil.is(messageDoc)) {
			return null;
		}
		ChatMessageDTO messageDto = ChatDTOUtil.getChatMessageDTO(messageDoc, chatSessionDto.getName(),
				ArgUtil.nonEmpty(messageDoc.getAgent(), messageDoc.getQueue(), chatSessionDto.getAssignedToAgent(),
						chatSessionDto.getAssignedToQueue()));
		return messageDto;
	}

	public List<ChatMessageDTO> createMessageDTO(List<MessageDoc> messages, ChatSessionDTO chatSessionDto) {
		List<ChatMessageDTO> messageDtos = new ArrayList<ChatMessageDTO>();
		for (MessageDoc messageDoc : messages) {
			ChatMessageDTO messageDto = createMessageDTO(messageDoc, chatSessionDto);
			messageDtos.add(messageDto);
		}
		return messageDtos;
	}

	public List<ChatMessageDTO> getMessages(ChatSessionDTO chatSessionDto) {

		if (ArgUtil.isEmpty(chatSessionDto.getContactType())) {
			chatSessionDto = withContact(chatSessionDto);
		}
		List<MessageDoc> messages = messageStore.findBySessionId(chatSessionDto.getSessionId(),
				chatSessionDto.getContactType());
		return createMessageDTO(messages, chatSessionDto);
	}

	public ChatSessionDTO withMessages(ChatSessionDTO chatSessionDto) {
		List<ChatMessageDTO> messageDtos = getMessages(chatSessionDto);
		chatSessionDto.setMessages(messageDtos);
		return chatSessionDto;
	}

	// With Doc Input
	public ChatSessionDTO getChatSession(ChatSessionDoc chatSessionDoc) {
		return ChatDTOUtil.getChatSessionDTO(chatSessionDoc);
	}

	public ChatSessionDTO withContact(ChatSessionDoc chatSessionDoc) {
		ChatSessionDTO chatSessionDto = ChatDTOUtil.getChatSessionDTO(chatSessionDoc);
		return withContact(chatSessionDto);
	}

	@Deprecated
	public ChatSessionDTO getChatSessionDto(ChatSessionDoc chatSessionDoc, String agentCode) {
		ChatSessionDTO chatSessionDto = getChatSession(chatSessionDoc);
		chatSessionDto = withContact(chatSessionDto);

		chatSessionDto.setAssigned(ArgUtil.areEqual(chatSessionDoc.getAssignedToAgent(), agentCode)
				&& ArgUtil.isEmptyValue(chatSessionDoc.getResolveSessionStamp()));
		chatSessionDto = withMessages(chatSessionDto);
		return chatSessionDto;
	}

	@Deprecated
	public ChatSessionDTO getChatSessionDto(ChatSessionDTO chatSessionDto) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(chatSessionDto.getSessionId());
		return getChatSessionDto(sessionDoc, null);
	}
	
	public List<String> getTagCodeFromId(List<String> tags){
		List<String> tagCodeList = new ArrayList<>();
		for(String tagid:tags) {
			QuickTag quickTag = mongoTemplate.findById(tagid, QuickTag.class);
			if(ArgUtil.is(quickTag)) {
				tagCodeList.add(quickTag.getCode());
			}else {
				tagCodeList.add(tagid);
			}
		}
		return tagCodeList;
	}

}
