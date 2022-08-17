package com.boot.jx.admin.dto;

import java.util.List;
import java.util.Map;

public class CsvDto {
	String referenceKey;
	List<Map<Object,Object>> lstMap;
	Map<Object,List<Object>> csvMap;
	List<String> lstErrors;
	public List<Map<Object, Object>> getLstMap() {
		return lstMap;
	}
	public void setLstMap(List<Map<Object, Object>> lstMap) {
		this.lstMap = lstMap;
	}
	public Map<Object, List<Object>> getCsvMap() {
		return csvMap;
	}
	public void setCsvMap(Map<Object, List<Object>> csvMap) {
		this.csvMap = csvMap;
	}
	public List<String> getLstErrors() {
		return lstErrors;
	}
	public void setLstErrors(List<String> lstErrors) {
		this.lstErrors = lstErrors;
	}
	public String getReferenceKey() {
		return referenceKey;
	}
	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}
}
