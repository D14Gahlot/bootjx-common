package com.boot.jx.postman.fb;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookUserProfile implements Serializable {

	private static final long serialVersionUID = 7610011653823671347L;

	@JsonProperty("first_name")
	private String firstName;

	@JsonProperty("last_name")
	private String lastName;

	@JsonProperty("profile_pic")
	private String profilePic;

	@JsonProperty("locale")
	private String locale;

	@JsonProperty("timezone")
	private Object timezone;

	@JsonProperty("timezone")
	private String gender;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Object getTimezone() {
		return timezone;
	}

	public void setTimezone(Object timezone) {
		this.timezone = timezone;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
