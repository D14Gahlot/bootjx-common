package com.boot.jx.agent.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.postman.dto.ContactDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatSessionDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String sessionId;
	private String name;
	private String contactType;
	private String contactId;

	private String email;
	private String phone;

	private String profilePic;

	private String assignedToDept;
	private String assignedToAgent;

	private long startSessionStamp;
	private long fistResponseStamp;

	private long lastInComingStamp;

	private long assignedDeptStamp;
	private long assignedAgentStamp;

	private long lastResponseStamp;
	private long closeSessionStamp;

	boolean assigned;
	boolean active;

	private ContactDTO contact;

	private List<ChatMessageDto> messages;

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

	public List<ChatMessageDto> getMessages() {
		return messages;
	}

	public void setMessages(List<ChatMessageDto> messages) {
		this.messages = messages;
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

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isAssigned() {
		return assigned;
	}

	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	public String getAssignedToDept() {
		return assignedToDept;
	}

	public void setAssignedToDept(String assignedToDept) {
		this.assignedToDept = assignedToDept;
	}

	public String getAssignedToAgent() {
		return assignedToAgent;
	}

	public void setAssignedToAgent(String assignedToAgent) {
		this.assignedToAgent = assignedToAgent;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public long getStartSessionStamp() {
		return startSessionStamp;
	}

	public void setStartSessionStamp(long startSessionStamp) {
		this.startSessionStamp = startSessionStamp;
	}

	public long getFistResponseStamp() {
		return fistResponseStamp;
	}

	public void setFistResponseStamp(long fistResponseStamp) {
		this.fistResponseStamp = fistResponseStamp;
	}

	public long getAssignedDeptStamp() {
		return assignedDeptStamp;
	}

	public void setAssignedDeptStamp(long assignedDeptStamp) {
		this.assignedDeptStamp = assignedDeptStamp;
	}

	public long getAssignedAgentStamp() {
		return assignedAgentStamp;
	}

	public void setAssignedAgentStamp(long assignedAgentStamp) {
		this.assignedAgentStamp = assignedAgentStamp;
	}

	public long getLastResponseStamp() {
		return lastResponseStamp;
	}

	public void setLastResponseStamp(long lastResponseStamp) {
		this.lastResponseStamp = lastResponseStamp;
	}

	public long getCloseSessionStamp() {
		return closeSessionStamp;
	}

	public void setCloseSessionStamp(long closeSessionStamp) {
		this.closeSessionStamp = closeSessionStamp;
	}

	public ContactDTO getContact() {
		return contact;
	}

	public void setContact(ContactDTO contact) {
		this.contact = contact;
	}
}
