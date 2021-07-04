package com.boot.jx.postman.dto;

import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactDTO implements java.io.Serializable {

	private static final long serialVersionUID = 8977954934029643371L;

	@ApiMockModelProperty(example = "wa919876543210", required = false)
	@JsonProperty("contactId")
	private String contactId;

	@JsonProperty("contactType")
	private String contactType;

	private String lane;

	private String csid;

	@ApiMockModelProperty(example = "John Doe", required = false)
	private String name;

	@ApiMockModelProperty(example = "John.Doe@company.co", required = false)
	private String email;

	@ApiMockModelProperty(example = "919876543210", required = false)
	private String phone;

	private String profilePic;

	private List<String> labelId;
	private ChatUserProfileDTO profile;

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

	public List<String> getLabelId() {
		return labelId;
	}

	public void setLabelId(List<String> labelId) {
		this.labelId = labelId;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public ChatUserProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ChatUserProfileDTO profile) {
		this.profile = profile;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public String getCsid() {
		return csid;
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}

}
