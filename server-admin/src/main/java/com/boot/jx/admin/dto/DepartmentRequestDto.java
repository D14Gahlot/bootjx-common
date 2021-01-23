package com.boot.jx.admin.dto;

public class DepartmentRequestDto {
	private Integer dept_id;
	private String deptCode;
	private String deptName;
	private String deptEmail;
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}
	public String getDeptName() {
		return deptName;
	}
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}
	public String getDeptEmail() {
		return deptEmail;
	}
	public void setDeptEmail(String deptEmail) {
		this.deptEmail = deptEmail;
	}
	public Integer getDept_id() {
		return dept_id;
	}
	public void setDept_id(Integer dept_id) {
		this.dept_id = dept_id;
	}
	
	
}
