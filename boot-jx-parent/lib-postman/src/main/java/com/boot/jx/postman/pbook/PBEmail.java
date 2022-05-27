package com.boot.jx.postman.pbook;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PBEmail implements Serializable {

	private static final long serialVersionUID = 13406808264190167L;
	public String email;
	public String type;
	public String label;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public PBEmail email(String email) {
		this.email = email;
		return this;
	}

	public PBEmail type(String type) {
		this.type = type;
		return this;
	}

	public PBEmail label(String label) {
		this.label = label;
		return this;
	}
}
