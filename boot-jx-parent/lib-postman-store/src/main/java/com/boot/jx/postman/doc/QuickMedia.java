package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;

@Document(collection = "DICT_TEMPLATES")
@TypeAlias("QuickMedia")
public class QuickMedia implements Serializable, OldDocVersion<QuickMedia>, AuditableEntity {

    private static final long serialVersionUID = 7942286016346691701L;

    @Id
    private String name;

    private String type;

    private String title;

    private String category;

    private String content;

    private String url;

    private Map<String, Object> meta;

    private List<QuickMedia> oldVersions;

    private String createdBy;
    private Long createdStamp;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getCategory() {
	return category;
    }

    public void setCategory(String category) {
	this.category = category;
    }

    public String getContent() {
	return content;
    }

    public void setContent(String content) {
	this.content = content;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public Map<String, Object> getMeta() {
	return meta;
    }

    public void setMeta(Map<String, Object> meta) {
	this.meta = meta;
    }

    public Map<String, Object> meta() {
	if (this.meta == null) {
	    this.meta = new HashMap<String, Object>();
	}
	return this.meta;
    }

    @Override
    public void setOldVersions(List<QuickMedia> oldVersions) {
	this.oldVersions = oldVersions;
    }

    @Override
    public List<QuickMedia> getOldVersions() {
	return oldVersions;
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
