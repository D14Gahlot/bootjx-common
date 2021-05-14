package com.boot.jx.common.dto;

public class DepartmentResponseAuthDto extends DepartmentResponseDto<DepartmentResponseAuthDto> {

	private static final long serialVersionUID = 3924319833059027100L;

	@Override
	protected DepartmentResponseAuthDto newInstance() {
		return new DepartmentResponseAuthDto();
	}

}
