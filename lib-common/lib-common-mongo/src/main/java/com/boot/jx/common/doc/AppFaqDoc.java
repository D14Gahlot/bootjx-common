package com.boot.jx.common.doc;

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "APP_FAQ")
@TypeAlias("AppFaqDoc")
public class AppFaqDoc {
	
	@Id
	private String id;
	@Indexed(unique = true)
    private String code;
	private String parent;
	private List<String> relatedCodes;
	protected Map<String, Object> translation;
	private String createdBy;
	private Long createdStamp;
	private String modifiedBy;
	private Long modifiedStamp;
	
	private String status;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
		public Map<String, Object> getTranslation() {
		return translation;
	}
	public void setTranslation(Map<String, Object> translation) {
		this.translation = translation;
	}
	public List<String> getRelatedCodes() {
		return relatedCodes;
	}
	public void setRelatedCodes(List<String> relatedCodes) {
		this.relatedCodes = relatedCodes;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public Long getCreatedStamp() {
		return createdStamp;
	}
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getModifiedBy() {
		return modifiedBy;
	}
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}
	public Long getModifiedStamp() {
		return modifiedStamp;
	}
	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}
	
	
}
