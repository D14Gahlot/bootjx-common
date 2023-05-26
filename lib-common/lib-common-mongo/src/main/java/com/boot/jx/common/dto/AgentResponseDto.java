package com.boot.jx.common.dto;

import java.util.List;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;
import com.boot.jx.mongo.CommonDocInterfaces.ResourceDocument;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentResponseDto<T extends AgentResponseDto<T>> implements ADocumentDTO<T> {

	private static final long serialVersionUID = -5273371227763139845L;
	@JsonProperty("id")
	private String id;
	@JsonProperty("code")
	private String agent_code;
	@JsonProperty("email")
	private String agent_email;

	@JsonProperty("phone")
	private String agent_number;

	@JsonProperty("name")
	private String agent_name;

	private String isactive;

	private boolean admin;

	@JsonProperty("enabled")
	private boolean isEnabled;

	@JsonProperty("superAdmin")
	private boolean isSuperAdmin;

	@JsonProperty("duperAdmin")
	private boolean isDuperAdmin;

	@JsonProperty("defaultValue")
	private boolean isDefaultValue;

	@JsonProperty("dept_id")
	private String dept_id;

	private List<ResourceDocument> quicktags;

	private List<ResourceDocument> quicklabels;

	private List<ResourceDocument> quickskills;

	public String getId() {
		return id;
	}

	public void setId(String agent_id) {
		this.id = agent_id;
	}

	public String getAgent_code() {
		return agent_code;
	}

	public void setAgent_code(String agent_code) {
		this.agent_code = agent_code;
	}

	public String getAgent_name() {
		return agent_name;
	}

	public void setAgent_name(String agent_name) {
		this.agent_name = agent_name;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isSuperAdmin() {
		return isSuperAdmin;
	}

	public void setSuperAdmin(boolean isSuperAdmin) {
		this.isSuperAdmin = isSuperAdmin;
	}

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	public boolean isEnabled() {
		return "Y".equalsIgnoreCase(this.isactive) || this.isEnabled;
	}

	@Override
	public AgentResponseDto<T> newInstance() {
		return new AgentResponseDto<T>();
	}

	public boolean isDefaultValue() {
		return isDefaultValue;
	}

	public void setDefaultValue(boolean isDefaultValue) {
		this.isDefaultValue = isDefaultValue;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	public String getAgent_email() {
		return agent_email;
	}

	public void setAgent_email(String agent_email) {
		this.agent_email = agent_email;
	}

	public boolean isDuperAdmin() {
		return isDuperAdmin;
	}

	public void setDuperAdmin(boolean isDuperAdmin) {
		this.isDuperAdmin = isDuperAdmin;
	}

	public List<ResourceDocument> getQuicktags() {
		return quicktags;
	}

	public void setQuicktags(List<ResourceDocument> quicktags) {
		this.quicktags = quicktags;
	}

	public List<ResourceDocument> getQuicklabels() {
		return quicklabels;
	}

	public void setQuicklabels(List<ResourceDocument> quicklabels) {
		this.quicklabels = quicklabels;
	}

	public List<ResourceDocument> getQuickskills() {
		return quickskills;
	}

	public void setQuickskills(List<ResourceDocument> quickskills) {
		this.quickskills = quickskills;
	}

	public String getAgent_number() {
		return agent_number;
	}

	public void setAgent_number(String agent_number) {
		this.agent_number = agent_number;
	}

}
