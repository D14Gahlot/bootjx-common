package com.boot.jx.chat;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.utils.EntityDtoUtil;

public class ChatDTOUtil {

	public static ContactDTO getContactDTO(ChatContactDoc chatContactDoc) {
		ContactDTO contact = new ContactDTO();

		contact.setContactId(chatContactDoc.getContactId());
		contact.setContactType(chatContactDoc.getContactType());
		contact.setName(chatContactDoc.getName());
		contact.setPhone(chatContactDoc.getPhone());
		contact.setEmail(chatContactDoc.getEmail());
		contact.setLabelId(chatContactDoc.getLabelId());
		contact.setProfilePic(chatContactDoc.getProfilePic());
		contact.setProfile(chatContactDoc.getProfile());

		return contact;
	}

	public static ChatMessageDTO getChatMessageDTO(MessageDoc messageDoc) {
		ChatMessageDTO messageDto = new ChatMessageDTO();
		messageDto.setType(messageDoc.getType());
		messageDto.setText(messageDoc.getMessage());
		messageDto.setTemplate(messageDoc.getTemplate());
		messageDto.setTimestamp(messageDoc.getTimestamp());
		messageDto.setSessionId(messageDoc.getSessionId());
		messageDto.setMessageId(messageDoc.getMessageId());
		messageDto.setMessageIdExt(messageDoc.getMessageIdExt());
		messageDto.setMessageIdRef(messageDoc.getMessageIdRef());
		messageDto.setTags(messageDoc.getTags());
		messageDto.setAttachments(messageDoc.getAttachments());
		messageDto.setSender(messageDoc.getAgent());
		messageDto.setLogs(messageDoc.getLogs());
		messageDto.setAction(messageDoc.getAction());
		messageDto.setStatus(messageDoc.getStatus());
		return messageDto;
	}

	public static ChatSessionDTO getChatSessionDTO(ChatSessionDoc chatSessionDoc) {
		ChatSessionDTO chatSessionDto = EntityDtoUtil.entityToDto(chatSessionDoc, new ChatSessionDTO());
		chatSessionDto.setSessionId(chatSessionDto.getSessionId());
		chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
		chatSessionDto.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		chatSessionDto.setAssignedToDept(chatSessionDoc.getAssignedToDept());
		chatSessionDto.setActive(chatSessionDoc.isActive());
		return chatSessionDto;
	}
}
