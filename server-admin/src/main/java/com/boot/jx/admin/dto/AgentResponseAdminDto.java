package com.boot.jx.admin.dto;

import com.boot.jx.common.dto.AgentResponseDto;

public class AgentResponseAdminDto extends AgentResponseDto<AgentResponseAdminDto> {

	private static final long serialVersionUID = 559612484746960788L;
	private String agent_department;
	private String agent_email;
	private String agent_number;
	private String agent_password;
	private Long createdStamp;
	private String create_by;
	private Long modifiedStamp;
	private String modified_by;

	private DepartmentResponseAdminDto dept;

	public String getAgent_department() {
		return agent_department;
	}

	public void setAgent_department(String agent_department) {
		this.agent_department = agent_department;
	}

	public String getAgent_email() {
		return agent_email;
	}

	public void setAgent_email(String agent_email) {
		this.agent_email = agent_email;
	}

	public String getAgent_number() {
		return agent_number;
	}

	public void setAgent_number(String agent_number) {
		this.agent_number = agent_number;
	}

	public String getCreate_by() {
		return create_by;
	}

	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}

	public String getModified_by() {
		return modified_by;
	}

	public void setModified_by(String modified_by) {
		this.modified_by = modified_by;
	}

	public DepartmentResponseAdminDto getDept() {
		return dept;
	}

	public void setDept(DepartmentResponseAdminDto dept) {
		this.dept = dept;
	}

	@Override
	public AgentResponseAdminDto newInstance() {
		return new AgentResponseAdminDto();
	}

	public String getAgent_password() {
		return agent_password;
	}

	public void setAgent_password(String agent_password) {
		this.agent_password = agent_password;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public Long getModifiedStamp() {
		return modifiedStamp;
	}

	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}

}
