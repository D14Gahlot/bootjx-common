package com.boot.jx.postman.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatUserProfileDTO implements Serializable {

	private static final long serialVersionUID = 3000520290601093027L;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ChatUserProfileRequest implements Serializable {
		private static final long serialVersionUID = -4573847059953057257L;
		String contactId;
		String mobile;
		String email;

		public String getMobile() {
			return mobile;
		}

		public void setMobile(String mobileNo) {
			this.mobile = mobileNo;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail(String emailId) {
			this.email = emailId;
		}

		public String getContactId() {
			return contactId;
		}

		public void setContactId(String contactId) {
			this.contactId = contactId;
		}

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class CustomerLabel implements Serializable {

		private static final long serialVersionUID = 3113597210867635972L;
		String name;
		Object value;
		String key;
		String type;
		String format;

		public Object getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public CustomerLabel type(String type) {
			this.type = type;
			return this;
		}

		public CustomerLabel value(String value) {
			this.value = value;
			return this;
		}

		public CustomerLabel name(String name) {
			this.name = name;
			return this;
		}

		public CustomerLabel key(String key) {
			this.key = key;
			return this;
		}

		public CustomerLabel format(String format) {
			this.format = format;
			return this;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getFormat() {
			return format;
		}

		public void setFormat(String format) {
			this.format = format;
		}
	}

	String contactId;
	String userId;
	String mobile;
	String email;
	String name;

	List<CustomerLabel> labels;

	public ChatUserProfileDTO label(String key, String value, String name) {
		if (this.labels == null) {
			this.labels = new ArrayList<CustomerLabel>();
		}

		CustomerLabel label = new CustomerLabel();
		label.setKey(key);
		label.setValue(value);
		label.setName(name);

		this.labels.add(label);
		return this;
	}

	public ChatUserProfileDTO label(CustomerLabel label) {
		if (this.labels == null) {
			this.labels = new ArrayList<CustomerLabel>();
		}
		this.labels.add(label);
		return this;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<CustomerLabel> getLabels() {
		return labels;
	}

	public void setLabels(List<CustomerLabel> labels) {
		this.labels = labels;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
