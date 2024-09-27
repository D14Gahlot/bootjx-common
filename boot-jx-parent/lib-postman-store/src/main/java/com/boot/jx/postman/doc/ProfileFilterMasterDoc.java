package com.boot.jx.postman.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;
@Document(collection = "MASTER_PROFILE_FILTER")
@TypeAlias("ProfileFilterMaster")
public class ProfileFilterMasterDoc extends TimeStampDoc {
	
	@Id
	String id;
	String filterName;
	String filterCriteria;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFilterName() {
		return filterName;
	}
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}
	public String getFilterCriteria() {
		return filterCriteria;
	}
	public void setFilterCriteria(String filterCriteria) {
		this.filterCriteria = filterCriteria;
	}
	
	
}

