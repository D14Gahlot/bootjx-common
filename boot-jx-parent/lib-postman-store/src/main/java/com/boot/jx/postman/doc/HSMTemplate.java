package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;

@Document(collection = HSMTemplate.COLLECTION_NAME)
@TypeAlias("HSMTemplate")
public class HSMTemplate implements Serializable, OldDocVersion<HSMTemplate>, BasicTemplate, AuditableEntity {

	public static final String COLLECTION_NAME = "DICT_HSM_TEMPLATES";
	public static final String COLLECTION_NAME_TRASH = "TRASH_DICT_HSM_TEMPLATES";

	private static final long serialVersionUID = 5953299041958788771L;

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	@Indexed
	private String code;

	@Indexed
	private String contactType;

	@Indexed
	private String lang;

	private String category;

	private String desc;
	private String title;

	private String template;

	private Map<String, Object> meta;

	protected Map<String, Object> options;
	protected Map<String, Object> data;

	@Field("oldVersions")
	@Reference
	private List<HSMTemplate> oldVersions;

	private String createdBy;
	private Long createdStamp;

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	@Override
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@Override
	public List<HSMTemplate> getOldVersions() {
		return oldVersions;
	}

	@Override
	public void setOldVersions(List<HSMTemplate> oldVersions) {
		this.oldVersions = oldVersions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Map<String, Object> getMeta() {
		return meta;
	}

	public void setMeta(Map<String, Object> meta) {
		this.meta = meta;
	}

	@Override
	public Map<String, Object> meta() {
		if (this.meta == null) {
			this.meta = new HashMap<String, Object>();
		}
		return this.meta;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	@Override
	public Map<String, Object> options() {
		if (this.options == null) {
			this.options = new HashMap<String, Object>();
		}
		return this.options;
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

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

}
