package com.boot.jx.postman.doc.config;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.utils.ArgUtil;

@Document(collection = "CONFIG_COMP_VARS")
@TypeAlias("CompanyVarsConfig")
public class CompanyVarsConfigDoc extends PMConfigurationObject {

	private static final long serialVersionUID = -4251710793999219993L;

	@Id
	private String id;

	private String group;

	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getGroup() {
		return ArgUtil.nonEmpty(this.group, "default");
	}

	public void setGroup(String group) {
		this.group = group;
	}

}
