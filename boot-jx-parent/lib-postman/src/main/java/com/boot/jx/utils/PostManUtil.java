package com.boot.jx.utils;

import java.security.NoSuchAlgorithmException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.model.ContactInfo;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
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

	public static String createCsid(Contactable contact) {
		if (ArgUtil.is(contact.getCsid())) {
			return contact.getCsid();
		}
		ContactType contactType = ArgUtil.parseAsEnumT(contact.getContactType(), ContactType.class);
		if (ArgUtil.is(contactType)) {
			switch (contactType) {
			case WHATSAPP:
			case SMS:
				return contact.getPhone();
			case EMAIL:
				return contact.getEmail();
			default:
				break;
			}
		}
		return contact.getCsid();
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
		if (ArgUtil.is(inboxMessage.contact().getContactId())) {
			return inboxMessage.contact().getContactId();
		}
		return createContactId(inboxMessage.contact().type(), inboxMessage.forContact(), inboxMessage.contact().getLane());
	}

	public static String createContactId(Contactable contact) {
		if (ArgUtil.is(contact.getContactId())) {
			return contact.getContactId();
		}
		String csid = createCsid(contact);
		return createContactId(ArgUtil.parseAsEnumT(contact.getContactType(), ContactType.class), csid,
				contact.getLane());
	}

	public static Contactable updateContact(Contactable contact) {
		if (!ArgUtil.is(contact.getCsid())) {
			contact.setCsid(createCsid(contact));
		}
		if (!ArgUtil.is(contact.getContactId()) // if ContactId is Missing
				&& ArgUtil.is(contact.getContactType()) // ContactType is for contactId
				&& ArgUtil.is(contact.getCsid()) // CSID is for contactId
				&& ArgUtil.is(contact.getLane()) // Lane is for contactId
		) {
			contact.setContactId(createContactId(ArgUtil.parseAsEnumT(contact.getContactType(), ContactType.class),
					contact.getCsid(), contact.getLane()));
		}
		return contact;
	}

	public static String generateCheckSum(InboxMessage inboxMessage) {
		String checkString = inboxMessage.contact().getContactId() + inboxMessage.getSessionId()
				+ inboxMessage.getMessageId() + inboxMessage.getMessage();
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
