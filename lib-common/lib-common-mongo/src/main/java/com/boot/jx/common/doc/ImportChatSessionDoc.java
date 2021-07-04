package com.boot.jx.common.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.dict.ContactType;

@Document(collection = "CHAT_IMPORT_LOGS")
@TypeAlias("ImportChatSessionDoc")
public class ImportChatSessionDoc implements Serializable {

	private static final long serialVersionUID = 2054374420663935322L;

	@Id
	private String id;

	private String fileMD5;
	private String fileName;
	private String fileSize;
	private String fileId;

	private Integer countSessions;
	private Integer countMessages;

	private String contactMobile;
	private String contactName;
	private String contact;
	private String sender;
	private String lane;
	private ContactType contactType;
	private String contactId;
	private String timezone;
	private String status;

	// Audit Info
	private String createdBy;
	private Long createdStamp;
	private List<String> sessions;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileMD5() {
		return fileMD5;
	}

	public void setFileMD5(String fileMD5) {
		this.fileMD5 = fileMD5;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileSize() {
		return fileSize;
	}

	public void setFileSize(String fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public Integer getCountSessions() {
		return countSessions;
	}

	public void setCountSessions(Integer countSessions) {
		this.countSessions = countSessions;
	}

	public Integer getCountMessages() {
		return countMessages;
	}

	public void setCountMessages(Integer countMessages) {
		this.countMessages = countMessages;
	}

	public String getContactMobile() {
		return contactMobile;
	}

	public void setContactMobile(String contactMobile) {
		this.contactMobile = contactMobile;
	}

	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getSessions() {
		return sessions;
	}

	public void setSessions(List<String> sessions) {
		this.sessions = sessions;
	}

}
