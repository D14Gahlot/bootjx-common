package com.boot.jx.admin.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.boot.jx.model.IResourceEntity;
import com.boot.utils.ArgUtil;

@Entity
public class Department implements IResourceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer dept_id;
	@Column(unique = true, nullable = false, length = 5)
	private String dept_code;
	@Column(nullable = false)
	private String dept_name;
	private String dept_email;
	private Date created_date;
	private String create_by = "A";
	private Date modified_date;
	@Column
	private String modified_by = "A";
	@Column(length = 1)
	private String isactive;

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

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public Integer getDept_id() {
		return dept_id;
	}

	public void setDept_id(Integer dept_id) {
		this.dept_id = dept_id;
	}

	@Override
	public BigDecimal resourceId() {
		return ArgUtil.parseAsBigDecimal(this.dept_id);
	}

	@Override
	public String resourceName() {
		return this.dept_name;
	}

	@Override
	public String resourceCode() {
		return this.dept_code;
	}

	@Override
	public String resourceLocalName() {
		return this.dept_name;
	}

}
