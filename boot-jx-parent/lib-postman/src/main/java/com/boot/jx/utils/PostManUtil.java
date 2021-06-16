package com.boot.jx.utils;

import java.security.NoSuchAlgorithmException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.MESSAGE_BOUND_TYPE;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

public class PostManUtil {

	public static ResponseEntity<byte[]> download(CommonFile file) {
		return ResponseEntity.ok().contentLength(file.getBody().length)
				.header("Content-Disposition", "attachment; filename=" + file.getName())
				.contentType(MediaType.valueOf(file.getFileFormat().getContentType())).body(file.getBody());
	}

	public static ResponseEntity<byte[]> render(CommonFile file) {
		return ResponseEntity.ok().contentLength(file.getBody().length)
				.contentType(MediaType.valueOf(file.getFileFormat().getContentType())).body(file.getBody());
	}

	public static String createContactId(ContactType contactType, String id, String lane) {
		if (ContactType.WHATSAPP.equals(contactType)) {
			return "wa" + id + "_" + lane;
		} else if (ContactType.FACEBOOK.equals(contactType)) {
			return "fb" + id + "_" + lane;
		} else if (ContactType.TWITTER.equals(contactType)) {
			return "tw" + id + "_" + lane;
		} else if (ContactType.TELEGRAM.equals(contactType)) {
			return "tg" + id + "_" + lane;
		} else if (ArgUtil.is(contactType)) {
			return contactType.getShortCode() + id + "_" + lane;
		}
		return id;
	}

	public static String createContactId(IMessage inboxMessage) {
		if (ArgUtil.is(inboxMessage.getContactId())) {
			return inboxMessage.getContactId();
		}
		return createContactId(inboxMessage.getContactType(), inboxMessage.forContact(), inboxMessage.getLane());
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

	public static boolean isInBound(String type) {
		return ArgUtil.isEqual(type, MESSAGE_BOUND_TYPE.INBOUND, MESSAGE_BOUND_TYPE.INBOUND_IMPORTED);
	}

	public static boolean isOutBound(String type) {
		return ArgUtil.isEqual(type, MESSAGE_BOUND_TYPE.OUTBOUND, MESSAGE_BOUND_TYPE.OUTBOUND_IMPORTED);
	}

}
