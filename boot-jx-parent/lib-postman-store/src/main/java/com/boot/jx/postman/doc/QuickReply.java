package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;

@Document(collection = "DICT_QUICK_REPS")
@TypeAlias("QuickReply")
public class QuickReply implements Serializable, OldDocVersion<QuickReply>, AuditableEntity {
	private static final long serialVersionUID = -5649094988762846983L;

	@Id
	private String id;
	private String title;
	private String category;
	private String message;
	private String template;
	private List<QuickReply> oldVersions;
	private String createdBy;
	private Long createdStamp;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<QuickReply> getOldVersions() {
		return oldVersions;
	}

	public void setOldVersions(List<QuickReply> oldVersions) {
		this.oldVersions = oldVersions;
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

}
