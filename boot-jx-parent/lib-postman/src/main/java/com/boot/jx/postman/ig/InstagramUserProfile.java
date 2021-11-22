package com.boot.jx.postman.ig;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InstagramUserProfile implements Serializable {

	private static final long serialVersionUID = 7610011653823671347L;

	@JsonProperty("name")
	private String name;

	@JsonProperty("profile_pic")
	private String profilePic;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}
}
