package com.boot.jx.agent.dto;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;

public class AgentResponseDto extends ADocumentDTO<AgentResponseDto> {

	private static final long serialVersionUID = 559612484746960788L;
	private String agent_id;
	private String dept_id;
	private String agent_code;
	private String agent_name;
	private String agent_department;
	private String agent_channels;
	private String isactive;

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

}
