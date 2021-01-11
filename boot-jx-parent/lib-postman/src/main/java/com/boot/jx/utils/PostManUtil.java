package com.boot.jx.utils;

import java.security.NoSuchAlgorithmException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;

public class PostManUtil {

	public static ResponseEntity<byte[]> download(File file) {
		return ResponseEntity.ok().contentLength(file.getBody().length)
				.header("Content-Disposition", "attachment; filename=" + file.getName())
				.contentType(MediaType.valueOf(file.getType().getContentType())).body(file.getBody());
	}

	public static ResponseEntity<byte[]> render(File file) {
		return ResponseEntity.ok().contentLength(file.getBody().length)
				.contentType(MediaType.valueOf(file.getType().getContentType())).body(file.getBody());
	}

	public static String createContactId(ContactType contactType, String id) {
		if (ContactType.WHATSAPP.equals(contactType)) {
			return "wa" + id;
		} else if (ContactType.FACEBOOK.equals(contactType)) {
			return "fb" + id;
		}else if(ContactType.TWITTER.equals(contactType)) {
			return "tw" + id;
		}
		return id;
	}

	public static String createContactId(InboxMessage inboxMessage) {
		return createContactId(inboxMessage.getContactType(), inboxMessage.getFrom());
	}

	public static String createContactId(Message<?> outMessage) {
		return createContactId(outMessage.getContactType(), CollectionUtil.getOne(outMessage.getTo()));
	}

	public static String generateCheckSum(InboxMessage inboxMessage) {
		String checkString = inboxMessage.getContactId() + inboxMessage.getSessionId() + inboxMessage.getMessageId()
				+ inboxMessage.getMessage();
		try {
			return CryptoUtil.getMD5Hash(checkString);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public static boolean hasValidCheckSum(InboxMessage inboxMessage) {
		return ArgUtil.areEqual(inboxMessage.getChecksum(), generateCheckSum(inboxMessage));
	}
}
