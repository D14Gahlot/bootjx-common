package com.boot.jx.admin.dto;

import java.sql.Date;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;

public class AgentResponseDto extends ADocumentDTO<AgentResponseDto> {

	private static final long serialVersionUID = 559612484746960788L;
	private String agent_id;
	private String dept_id;
	private String agent_code;
	private String agent_name;
	private String agent_department;
	private String agent_email;
	private String agent_number;
	private String agent_password;
	private String agent_channels;
	private Date created_date;
	private String create_by;
	private Date modified_date;
	private String modified_by;
	private String isactive;
	private boolean isAdmin;
	private DepartmentResponseDto dept;

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

	public Date getCreated_date() {
		return created_date;
	}

	public void setCreated_date(Date created_date) {
		this.created_date = created_date;
	}

	public String getCreate_by() {
		return create_by;
	}

	public void setCreate_by(String create_by) {
		this.create_by = create_by;
	}

	public Date getModified_date() {
		return modified_date;
	}

	public void setModified_date(Date modified_date) {
		this.modified_date = modified_date;
	}

	public String getModified_by() {
		return modified_by;
	}

	public void setModified_by(String modified_by) {
		this.modified_by = modified_by;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public String getAgent_id() {
		return agent_id;
	}

	public void setAgent_id(String agent_id) {
		this.agent_id = agent_id;
	}

	public DepartmentResponseDto getDept() {
		return dept;
	}

	public void setDept(DepartmentResponseDto dept) {
		this.dept = dept;
	}

	@Override
	public AgentResponseDto newInstance() {
		return new AgentResponseDto();
	}

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	public String getAgent_channels() {
		return agent_channels;
	}

	public void setAgent_channels(String agent_channels) {
		this.agent_channels = agent_channels;
	}

	public String getAgent_password() {
		return agent_password;
	}

	public void setAgent_password(String agent_password) {
		this.agent_password = agent_password;
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

}
