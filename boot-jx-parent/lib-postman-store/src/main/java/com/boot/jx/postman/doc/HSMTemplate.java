package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;

@Document(collection = "DICT_HSM_TEMPLATES")
@TypeAlias("HSMTemplate")
public class HSMTemplate implements Serializable, OldDocVersion<HSMTemplate> {

	private static final long serialVersionUID = 5953299041958788771L;

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private String category;

	private String title;

	private String template;

	private List<HSMTemplate> oldVersions;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public List<HSMTemplate> getOldVersions() {
		return oldVersions;
	}

	public void setOldVersions(List<HSMTemplate> oldVersions) {
		this.oldVersions = oldVersions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
