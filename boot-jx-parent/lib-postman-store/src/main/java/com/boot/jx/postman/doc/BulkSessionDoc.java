package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.AuditableEntity;

@Document(collection = "BULK_SESSION")
@TypeAlias("BulkSessionDoc")
public class BulkSessionDoc implements AuditableEntity, Serializable {

	private static final long serialVersionUID = 2126642970366757413L;

	@Id
	private String bulkSessionId;

	private String createdBy;
	private Long createdStamp;

	private String templateId;
	private String template;

	private ContactType contactType;
	private String lane;
	private String message;

	private String status;

	private Integer messageCount;
	private Integer messageSentCount;
	private Integer messageFailedCount;

	private Map<String, Long> stats;

	@Override
	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Override
	public Long getCreatedStamp() {
		return createdStamp;
	}

	@Override
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(Integer messageCount) {
		this.messageCount = messageCount;
	}

	public Integer getMessageSentCount() {
		return messageSentCount;
	}

	public void setMessageSentCount(Integer messageSentCount) {
		this.messageSentCount = messageSentCount;
	}

	public Integer getMessageFailedCount() {
		return messageFailedCount;
	}

	public void setMessageFailedCount(Integer messageFailedCount) {
		this.messageFailedCount = messageFailedCount;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getBulkSessionId() {
		return bulkSessionId;
	}

	public void setBulkSessionId(String bulkSessionId) {
		this.bulkSessionId = bulkSessionId;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public Map<String, Long> getStats() {
		return stats;
	}

	public void setStats(Map<String, Long> stats) {
		this.stats = stats;
	}

	public Map<String, Long> stats() {
		if (this.stats == null) {
			this.stats = new HashMap<String, Long>();
		}
		return this.stats;
	}

}
