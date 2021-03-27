package com.boot.jx.postman.model;

import com.boot.jx.dict.ContactType;

public interface IMessage {

	String getContactId();

	ContactType getContactType();

	String getFrom();

	String getSessionId();

	MessageSession session();

}
