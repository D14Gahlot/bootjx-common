package com.boot.jx.admin.dto;

import java.util.Date;

import com.boot.jx.common.dto.DepartmentResponseDto;

public class DepartmentResponseAdminDto extends DepartmentResponseDto<DepartmentResponseAdminDto> {

	private static final long serialVersionUID = -3495791543071105507L;
	private String dept_email;
	private Date created_date;
	private String create_by;
	private Date modified_date;
	private String modified_by;
	private boolean isDefaultValue;

	public String getDept_email() {
		return dept_email;
	}

	public void setDept_email(String dept_email) {
		this.dept_email = dept_email;
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

}
