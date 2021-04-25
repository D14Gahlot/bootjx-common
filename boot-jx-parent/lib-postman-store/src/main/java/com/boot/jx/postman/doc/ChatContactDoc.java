package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.dto.ChatUserProfileDTO;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

@Document(collection = "CHAT_CONTACT")
@TypeAlias("ChatContactDoc")
public class ChatContactDoc implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@ApiMockModelProperty(example = "wa919930104050", required = false)
	@JsonProperty("contactId")
	private String contactId;

	private String csid;

	private String contactType;
	private String channelType;
	private String lane;

	private long lastInComingStamp;

	private long lastOutGoingStamp;

	private String sessionId;

	private String name;
	private String email;
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

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public long getLastInComingStamp() {
		return lastInComingStamp;
	}

	public void setLastInComingStamp(long lastInComingStamp) {
		this.lastInComingStamp = lastInComingStamp;
	}

	public long getLastOutGoingStamp() {
		return lastOutGoingStamp;
	}

	public void setLastOutGoingStamp(long lastOutGoingStamp) {
		this.lastOutGoingStamp = lastOutGoingStamp;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCsid() {
		return csid;
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<String> labelId() {
		if (ArgUtil.isEmpty(this.labelId))
			this.labelId = new ArrayList<String>();
		return labelId;
	}

	public List<String> getLabelId() {
		return labelId;
	}

	public void setLabelId(List<String> labelId) {
		this.labelId = labelId;
	}

	public ChatUserProfileDTO getProfile() {
		return profile;
	}

	public void setProfile(ChatUserProfileDTO profile) {
		this.profile = profile;
	}
}
