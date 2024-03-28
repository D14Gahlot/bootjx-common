package com.boot.jx.common.doc;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.CommonFile;

@Document(collection="MASTER_CUSTMER_PROFILE")
@TypeAlias("CustomerProfileMasterDoc")
public class CustomerProfileMasterDoc {

	@Id
	private String id;
	CommonFile files;
	private String isactive;
	private Date createdDate;
	private Long createdStamp;
	private String createBy;
	private Date modifiedDate;
	private Long modifiedStamp;
	private String modifiedBy;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getIsactive() {
		return isactive;
	}
	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Long getCreatedStamp() {
		return createdStamp;
	}
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	public Long getModifiedStamp() {
		return modifiedStamp;
	}
	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public CommonFile getFiles() {
		return files;
	}
	public void setFiles(CommonFile files) {
		this.files = files;
	}
}
