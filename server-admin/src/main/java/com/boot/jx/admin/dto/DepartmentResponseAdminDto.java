package com.boot.jx.admin.dto;

import com.boot.jx.common.dto.DepartmentResponseDto;

public class DepartmentResponseAdminDto extends DepartmentResponseDto<DepartmentResponseAdminDto> {

	private static final long serialVersionUID = -3495791543071105507L;
	private String dept_email;
	private Long createdStamp;
	private String create_by;
	private Long modifiedStamp;
	private String modified_by;
	private boolean isDefaultValue;

	public String getDept_email() {
		return dept_email;
	}

	public void setDept_email(String dept_email) {
		this.dept_email = dept_email;
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

	@Override
	public DepartmentResponseAdminDto newInstance() {
		return new DepartmentResponseAdminDto();
	}

	public boolean isDefaultValue() {
		return isDefaultValue;
	}

	public void setDefaultValue(boolean isDefaultValue) {
		this.isDefaultValue = isDefaultValue;
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
