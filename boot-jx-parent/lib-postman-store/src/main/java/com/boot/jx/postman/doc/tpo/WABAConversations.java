package com.boot.jx.postman.doc.tpo;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.doc.ContactDetailDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = WABAConversations.COLLECTION_NAME)
@TypeAlias("TP_WABA_CONVERSATIONS")
@JsonIgnoreProperties(ignoreUnknown = true)
public class WABAConversations implements Serializable {

	private static final long serialVersionUID = 4116849214262304471L;

	public static final String COLLECTION_NAME = "TP_WABA_CONVERSATIONS";

	@Id
	private String id;

	private ContactDetailDoc contact;

	private Map<String, Object> conversation;
	private Map<String, Object> pricing;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Object> getConversation() {
		return conversation;
	}

	public void setConversation(Map<String, Object> conversation) {
		this.conversation = conversation;
	}

	public Map<String, Object> getPricing() {
		return pricing;
	}

	public void setPricing(Map<String, Object> pricing) {
		this.pricing = pricing;
	}

	public ContactDetailDoc getContact() {
		return contact;
	}

	public void setContact(ContactDetailDoc contact) {
		this.contact = contact;
	}

	public Contactable contact() {
		if (this.contact == null) {
			this.contact = new ContactDetailDoc();
		}
		return this.contact;
	}
}
