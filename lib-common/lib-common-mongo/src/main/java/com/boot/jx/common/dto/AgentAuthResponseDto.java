package com.boot.jx.common.dto;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AgentAuthResponseDto extends ADocumentDTO<AgentAuthResponseDto> {

	private static final long serialVersionUID = -5273371227763139845L;
	@JsonProperty("id")
	private String agent_id;
	@JsonProperty("code")
	private String agent_code;
	@JsonProperty("name")
	private String agent_name;

	@JsonProperty("code")
	private String agent_channels;

	private String isactive;

	private boolean admin;

	private boolean isSuperAdmin;

	@JsonProperty("dept_id")
	private String dept_id;

	private DepartmentResponseDto dept;

	@Override
	protected ADocumentDTO<AgentAuthResponseDto> newInstance() {
		return new AgentAuthResponseDto();
	}

	public String getAgent_id() {
		return agent_id;
	}

	public void setAgent_id(String agent_id) {
		this.agent_id = agent_id;
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

	public String getAgent_channels() {
		return agent_channels;
	}

	public void setAgent_channels(String agent_channels) {
		this.agent_channels = agent_channels;
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

	public DepartmentResponseDto getDept() {
		return dept;
	}

	public void setDept(DepartmentResponseDto dept) {
		this.dept = dept;
	}

	public AgentAuthResponseDto dept(DepartmentResponseDto dept) {
		this.dept = dept;
		return this;
	}
}
