package com.boot.jx.common.doc;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.mongo.CommonDocInterfaces.Patchable;
import com.boot.jx.postman.PMConstants.DEFAULT;

@Document(collection = "DEPARTMENTS")
@TypeAlias("DepartmentDoc")
public class DepartmentDoc implements Serializable, Patchable<DepartmentDoc>, IDocument, DocVersion {

	private static final long serialVersionUID = -3381417310939635611L;

	public static DepartmentDoc NO_DEPT = new DepartmentDoc().id(DEFAULT.NO_DEPT).code(DEFAULT.NO_DEPT);

	@Id
	private String dept_id;

	@Indexed(unique = true)
	private String dept_code;
	private String dept_name;
	private String dept_email;
	private Date created_date;
	private Long createdStamp;
	private String create_by = "A";
	private Date modified_date;
	private Long modifiedStamp;
	private String modified_by = "A";
	private String isactive;
	private boolean isDefaultValue;

	private List<DocVersion> oldVersions;

	public String getDept_code() {
		return dept_code;
	}

	private DepartmentDoc id(String id) {
		this.setDept_id(id);
		return this;
	}

	private DepartmentDoc code(String code) {
		this.setDept_code(code);
		return this;
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

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	@Override
	public DepartmentDoc patch() {
		DepartmentDoc patch = new DepartmentDoc();
		patch.setDept_id(this.getDept_id());
		return patch;
	}

	public boolean isDefaultValue() {
		return isDefaultValue;
	}

	public void setDefaultValue(boolean isDefaultValue) {
		this.isDefaultValue = isDefaultValue;
	}

	public List<DocVersion> getOldVersions() {
		return oldVersions;
	}

	public void setOldVersions(List<DocVersion> oldVersions) {
		this.oldVersions = oldVersions;
	}

	public Long getModifiedStamp() {
		return modifiedStamp;
	}

	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

}
