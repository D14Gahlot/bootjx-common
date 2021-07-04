package com.boot.jx.common.dto;

public class DepartmentResponseAuthDto extends DepartmentResponseDto<DepartmentResponseAuthDto> {

	private static final long serialVersionUID = 3924319833059027100L;

	@Override
	public DepartmentResponseAuthDto newInstance() {
		return new DepartmentResponseAuthDto();
	}

}
