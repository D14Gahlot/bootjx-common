package com.boot.jx.admin.manager;

import java.io.Serializable;

import org.hibernate.validator.constraints.UniqueElements;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.doc.ContactDetailDoc;

@Document(collection = "SESSION_EXPIRY_SUMMARY")
@TypeAlias("SessionExpirySummaryMsgDoc")
public class SessionExpirySummaryDoc implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4161053044218053030L;
	@Id
	String id;
	@UniqueElements
	String chatSessionId;
	String domain;
	long timestamp;
	long startSessionStamp;
	long expiryTimeStmp;
	private ContactDetailDoc contact;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public ContactDetailDoc getContact() {
		return contact;
	}
	public void setContact(ContactDetailDoc contact) {
		this.contact = contact;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getExpiryTimeStmp() {
		return expiryTimeStmp;
	}
	public void setExpiryTimeStmp(long expiryTimeStmp) {
		this.expiryTimeStmp = expiryTimeStmp;
	}
	public long getStartSessionStamp() {
		return startSessionStamp;
	}
	public void setStartSessionStamp(long startSessionStamp) {
		this.startSessionStamp = startSessionStamp;
	}
	public String getChatSessionId() {
		return chatSessionId;
	}
	public void setChatSessionId(String chatSessionId) {
		this.chatSessionId = chatSessionId;
	}
}
