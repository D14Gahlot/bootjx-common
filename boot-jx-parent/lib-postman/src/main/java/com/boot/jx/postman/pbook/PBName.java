package com.boot.jx.postman.pbook;

import java.io.Serializable;

import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PBName implements Serializable, JsonIgnoreUnknown {
	private static final long serialVersionUID = -2023402204499331768L;
	public String firstName;
	public String formattedName;
	public String lastName;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getFormattedName() {
		return formattedName;
	}

	public void setFormattedName(String formattedName) {
		this.formattedName = formattedName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
