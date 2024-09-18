package com.boot.jx.postman.dto;

import java.util.List;

import com.boot.jx.postman.model.ContactMeta;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = ContactDTO.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactDTO extends ContactMeta {

	private static final long serialVersionUID = 8977954934029643371L;

	@ApiMockModelProperty(example = "wa919876543210", required = false)
	@JsonProperty("contactId")
	private String contactId;

	public ContactDTO() {
		super();
	}

	private Boolean emailVerified;

	private Boolean phoneVerified;

	private String profilePic;

	private List<String> labelId;
	private ChatProfileDTO profile;

	private long firstInBoundStamp;
	private long firstOutBoundStamp;
	private long lastInBoundStamp;
	private long lastOutBoundStamp;
	private long lastPushStamp;
	private long lastReplyStamp;
	private long lastOptInStamp;

	private Long createdStamp;
	private String createdBy;

	private String sessionId;

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

	public String phone() {
		return phone;
	}

	public void phone(String phone) {
		this.phone = phone;
	}

	public List<String> getLabelId() {
		return labelId;
	}

	public void setLabelId(List<String> labelId) {
		this.labelId = labelId;
	}

	public String getProfilePic() {
		return profilePic;
	}

	public void setProfilePic(String profilePic) {
		this.profilePic = profilePic;
	}

	public ChatProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ChatProfileDTO profile) {
		this.profile = profile;
	}

	public long getFirstInBoundStamp() {
		return firstInBoundStamp;
	}

	public void setFirstInBoundStamp(long firstInBoundStamp) {
		this.firstInBoundStamp = firstInBoundStamp;
	}

	public long getFirstOutBoundStamp() {
		return firstOutBoundStamp;
	}

	public void setFirstOutBoundStamp(long firstOutBoundStamp) {
		this.firstOutBoundStamp = firstOutBoundStamp;
	}

	public long getLastInBoundStamp() {
		return lastInBoundStamp;
	}

	public void setLastInBoundStamp(long lastInBoundStamp) {
		this.lastInBoundStamp = lastInBoundStamp;
	}

	public long getLastOutBoundStamp() {
		return lastOutBoundStamp;
	}

	public void setLastOutBoundStamp(long lastOutBoundStamp) {
		this.lastOutBoundStamp = lastOutBoundStamp;
	}

	public long getLastPushStamp() {
		return lastPushStamp;
	}

	public void setLastPushStamp(long lastPushStamp) {
		this.lastPushStamp = lastPushStamp;
	}

	public long getLastReplyStamp() {
		return lastReplyStamp;
	}

	public void setLastReplyStamp(long lastReplyStamp) {
		this.lastReplyStamp = lastReplyStamp;
	}

	public long getLastOptInStamp() {
		return lastOptInStamp;
	}

	public void setLastOptInStamp(long lastOptInStamp) {
		this.lastOptInStamp = lastOptInStamp;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public Boolean getEmailVerified() {
		return emailVerified;
	}

	public void setEmailVerified(Boolean emailVerified) {
		this.emailVerified = emailVerified;
	}

	public Boolean getPhoneVerified() {
		return phoneVerified;
	}

	public void setPhoneVerified(Boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

}
