package com.boot.jx.admin.dto;

import java.util.ArrayList;
import java.util.List;

public class SearchQuery {

	List<SearchCriteria> searchCriterias = new ArrayList<>();
	
	int pageNo;
	int pageSize;
	String sortBy;
	String sortDir;
	public List<SearchCriteria> getSearchCriterias() {
		return searchCriterias;
	}
	public void setSearchCriterias(List<SearchCriteria> searchCriterias) {
		this.searchCriterias = searchCriterias;
	}
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
	

}
