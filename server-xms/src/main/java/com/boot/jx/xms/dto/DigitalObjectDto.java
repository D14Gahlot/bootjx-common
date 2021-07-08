package com.boot.jx.xms.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalObjectDto {
	String type;
	String id;
	Map<String,Object> data;
	

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	List<DigitalObjectLink> links;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<DigitalObjectLink> getLinks() {
		return links;
	}

	public void setLinks(List<DigitalObjectLink> links) {
		this.links = links;
	}

	public List<DigitalObjectLink> links() {
		if (this.links == null) {
			this.links = new ArrayList<>();
		}
		return links;
	}
	
	
}
