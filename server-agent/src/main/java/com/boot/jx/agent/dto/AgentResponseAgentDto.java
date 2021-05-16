package com.boot.jx.agent.dto;

import com.boot.jx.common.dto.AgentResponseDto;

public class AgentResponseAgentDto extends AgentResponseDto<AgentResponseAgentDto> {

	private static final long serialVersionUID = 559612484746960788L;
	private String agent_department;

	public String getAgent_department() {
		return agent_department;
	}

	public void setAgent_department(String agent_department) {
		this.agent_department = agent_department;
	}

	@Override
	public AgentResponseAgentDto newInstance() {
		return new AgentResponseAgentDto();
	}

}
