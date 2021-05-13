package com.boot.jx.common.dto;

import com.boot.jx.mongo.CommonDocInterfaces.ADocumentDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DepartmentResponseDto extends ADocumentDTO<DepartmentResponseDto> {

	private static final long serialVersionUID = 3924319833059027100L;

	@JsonProperty("id")
	private String dept_id;

	@JsonProperty("code")
	private String dept_code;

	@JsonProperty("name")
	private String dept_name;

	private String isactive;

	@Override
	protected ADocumentDTO<DepartmentResponseDto> newInstance() {
		return new DepartmentResponseDto();
	}

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	public String getDept_code() {
		return dept_code;
	}

	public void setDept_code(String dept_code) {
		this.dept_code = dept_code;
	}

	public String getDept_name() {
		return dept_name;
	}

	public void setDept_name(String dept_name) {
		this.dept_name = dept_name;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

}
