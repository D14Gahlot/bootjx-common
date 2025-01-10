package com.boot.jx.postman.doc;

import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;
@Document(collection = "MASTER_PROFILE_FILTER")
@TypeAlias("ProfileFilterMaster")
public class ProfileFilterMasterDoc extends TimeStampDoc implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9013008834161079131L;
	
	@Id
	String id;
	@Unique
	String filterName;
	String filterCriteria;
	List<List<Object>> _filterCriteria =new ArrayList<>();
	
	
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
	public List<List<Object>> get_filterCriteria() {
		return _filterCriteria;
	}
	public void set_filterCriteria(List<List<Object>> _filterCriteria) {
		this._filterCriteria = _filterCriteria;
	}
	
	
	
}

