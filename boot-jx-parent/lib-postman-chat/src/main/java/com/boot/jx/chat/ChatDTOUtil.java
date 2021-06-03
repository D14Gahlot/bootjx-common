package com.boot.jx.chat;

import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.ChatUserProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.dto.ChatMessageDTO;
import com.boot.jx.postman.dto.ChatSessionDTO;
import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.store.PMStoreConstants.CHAT_STATUS;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

public class ChatDTOUtil {

	public static ChatUserProfileDTO getProfileDTO(ChatUserProfileDoc profileDoc) {
		ChatUserProfileDTO dto = EntityDtoUtil.entityToDto(profileDoc, new ChatUserProfileDTO());
		return dto;
	}

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
		contact.setLane(chatContactDoc.getLane());

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
		messageDto.setStamps(messageDoc.getStamps());

		if (ArgUtil.isEmpty(messageDto.getStamps()) && ArgUtil.is(messageDto.getStatus())) {
			Map<String, Long> stamps = new HashMap<String, Long>();
			stamps.put(messageDto.getStatus(), messageDto.getTimestamp());
			messageDto.setStamps(stamps);
		}

		return messageDto;
	}

	public static ChatSessionDTO getChatSessionDTO(ChatSessionDoc chatSessionDoc) {
		ChatSessionDTO chatSessionDto = EntityDtoUtil.entityToDto(chatSessionDoc, new ChatSessionDTO());
		chatSessionDto.setSessionId(chatSessionDto.getSessionId());
		chatSessionDto.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());
		chatSessionDto.setAssignedToAgent(chatSessionDoc.getAssignedToAgent());
		chatSessionDto.setAssignedToDept(chatSessionDoc.getAssignedToDept());
		chatSessionDto.setActive(chatSessionDoc.isActive());
		chatSessionDto.setStatus(chatSessionDoc.getStatus());

		if (!ArgUtil.is(chatSessionDto.getStatus())) {
			if (chatSessionDto.isExpired()) {
				chatSessionDto.setStatus(CHAT_STATUS.EXPIRED.toString());
			} else if (!chatSessionDto.isActive()) {
				chatSessionDto.setStatus(CHAT_STATUS.CLOSED.toString());
			} else if (chatSessionDto.isResolved()) {
				chatSessionDto.setStatus(CHAT_STATUS.RESOLVED.toString());
			} else if (chatSessionDto.getAssignedAgentStamp() == 0) {
				chatSessionDto.setStatus(CHAT_STATUS.UNASSIGNED.toString());
			} else {
				chatSessionDto.setStatus(CHAT_STATUS.OPEN.toString());
			}
		}

		return chatSessionDto;
	}

}
