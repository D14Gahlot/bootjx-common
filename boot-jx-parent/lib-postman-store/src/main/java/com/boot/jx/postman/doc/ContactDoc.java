package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.dict.ContactType;

@Document(collection = "CONTACT")
public class ContactDoc implements Serializable {
	private static final long serialVersionUID = -6046846959629225232L;
	private String mobile;
	private String userid;
	private ContactType contactType;
	private Map<String, Object> filter;

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Map<String, Object> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, Object> filter) {
		this.filter = filter;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}
}
