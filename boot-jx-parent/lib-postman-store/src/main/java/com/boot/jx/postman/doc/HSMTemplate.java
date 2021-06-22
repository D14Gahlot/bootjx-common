package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;

@Document(collection = "DICT_HSM_TEMPLATES")
@TypeAlias("HSMTemplate")
public class HSMTemplate implements Serializable, OldDocVersion<HSMTemplate>, BasicTemplate {

	private static final long serialVersionUID = 5953299041958788771L;

	@Id
	private String id;

	@Indexed(unique = true)
	private String name;

	private String category;

	private String title;

	private String template;

	private Map<String, Object> meta;

	protected Map<String, Object> options;

	private List<HSMTemplate> oldVersions;

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
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
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

}
