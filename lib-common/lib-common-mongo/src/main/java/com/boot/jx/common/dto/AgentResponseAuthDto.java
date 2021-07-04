package com.boot.jx.common.dto;

public class AgentResponseAuthDto extends AgentResponseDto<AgentResponseAuthDto> {

	private static final long serialVersionUID = -5273371227763139845L;

	private DepartmentResponseAuthDto dept;

	@Override
	public AgentResponseAuthDto newInstance() {
		return new AgentResponseAuthDto();
	}

	public DepartmentResponseAuthDto getDept() {
		return dept;
	}

	public void setDept(DepartmentResponseAuthDto dept) {
		this.dept = dept;
	}

	public AgentResponseAuthDto dept(DepartmentResponseAuthDto dept) {
		this.dept = dept;
		return this;
	}

}
