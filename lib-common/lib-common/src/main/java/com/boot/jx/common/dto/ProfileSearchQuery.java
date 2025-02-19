package com.boot.jx.common.dto;

import java.util.ArrayList;
import java.util.List;

public class ProfileSearchQuery {

	
	List<List<ProfileSearchCriteria>> searchCriterias = new ArrayList<>();
	
	int pageNo;
	int pageSize;
	String sortBy;
	String sortDir;
	boolean booSkipLmt=false; 
	
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public String getSortBy() {
		return sortBy;
	}
	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}
	public String getSortDir() {
		return sortDir;
	}
	public void setSortDir(String sortDir) {
		this.sortDir = sortDir;
	}
	public List<List<ProfileSearchCriteria>> getSearchCriterias() {
		return searchCriterias;
	}
	public void setSearchCriterias(List<List<ProfileSearchCriteria>> searchCriterias) {
		this.searchCriterias = searchCriterias;
	}
	public boolean isBooSkipLmt() {
		return booSkipLmt;
	}
	public void setBooSkipLmt(boolean booSkipLmt) {
		this.booSkipLmt = booSkipLmt;
	}
	

}
