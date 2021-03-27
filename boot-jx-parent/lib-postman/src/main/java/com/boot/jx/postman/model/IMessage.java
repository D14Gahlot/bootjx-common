package com.boot.jx.postman.model;

import com.boot.jx.dict.ContactType;

public interface IMessage {

	String getContactId();

	ContactType getContactType();

	String forContact();

	String getSessionId();

	MessageSession session();

}
