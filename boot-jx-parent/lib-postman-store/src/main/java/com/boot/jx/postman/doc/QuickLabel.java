package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;

@Document(collection = "DICT_QUICK_LABEL")
@TypeAlias("QuickLabel")
public class QuickLabel implements Serializable, OldDocVersion<QuickLabel> {

	private static final long serialVersionUID = 2845094878124818820L;
	@Id
	private String id;
	private String title;
	private String category;
	private String code;

	private List<QuickLabel> oldVersions;

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<QuickLabel> getOldVersions() {
		return oldVersions;
	}

	public void setOldVersions(List<QuickLabel> oldVersions) {
		this.oldVersions = oldVersions;
	}
}
