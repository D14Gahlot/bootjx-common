package com.boot.jx.model;

public interface AuditableEntity {

	String getCreatedBy();

	void setCreatedBy(String createdBy);

	Long getCreatedStamp();

	void setCreatedStamp(Long createdStamp);

}
