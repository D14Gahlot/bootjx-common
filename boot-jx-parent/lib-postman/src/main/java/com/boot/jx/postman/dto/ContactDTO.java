package com.boot.jx.postman.dto;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactDTO implements java.io.Serializable {

	private static final long serialVersionUID = 8977954934029643371L;

	@ApiMockModelProperty(example = "wa919876543210", required = false)
	@JsonProperty("contactId")
	private String contactId;

	@ApiMockModelProperty(example = "John Doe", required = false)
	private String name;

	@ApiMockModelProperty(example = "John.Doe@company.co", required = false)
	private String email;

	@ApiMockModelProperty(example = "919876543210", required = false)
	private String phone;

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}
