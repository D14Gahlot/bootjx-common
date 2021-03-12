package com.boot.jx.chat;

import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.dto.ContactDTO;

public class ChatDTOUtil {

	public static ContactDTO getContactDTO(ChatContactDoc chatContactDoc) {
		ContactDTO contact = new ContactDTO();

		contact.setContactId(chatContactDoc.getContactId());
		contact.setName(chatContactDoc.getName());
		contact.setPhone(chatContactDoc.getPhone());
		contact.setEmail(chatContactDoc.getEmail());

		return contact;
	}
}
