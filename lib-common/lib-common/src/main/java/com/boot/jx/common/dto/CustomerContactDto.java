package com.boot.jx.common.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomerContactDto {
	private String id;
	private String  phoneNo;
	String uploadUrl;
	String successUrl;
	String errorUrl;
	
	List<Map<String,Object>> successMaps =new ArrayList<>();
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhoneNo() {
		return phoneNo;
	}
	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public String getUploadUrl() {
		return uploadUrl;
	}
	public void setUploadUrl(String uploadUrl) {
		this.uploadUrl = uploadUrl;
	}
	public String getSuccessUrl() {
		return successUrl;
	}
	public void setSuccessUrl(String successUrl) {
		this.successUrl = successUrl;
	}
	public String getErrorUrl() {
		return errorUrl;
	}
	public void setErrorUrl(String errorUrl) {
		this.errorUrl = errorUrl;
	}
	public List<Map<String, Object>> getSuccessMaps() {
		return successMaps;
	}
	public void setSuccessMaps(List<Map<String, Object>> successMaps) {
		this.successMaps = successMaps;
	}

}

