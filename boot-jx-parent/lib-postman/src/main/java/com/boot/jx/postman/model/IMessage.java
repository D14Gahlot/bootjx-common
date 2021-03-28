package com.boot.jx.postman.model;

import java.io.Serializable;
import com.boot.jx.dict.ContactType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface IMessage extends Serializable {

	public String getContactId();

	public ContactType getContactType();

	public String forContact();

	public String getSessionId();

	public MessageSession session();

}
