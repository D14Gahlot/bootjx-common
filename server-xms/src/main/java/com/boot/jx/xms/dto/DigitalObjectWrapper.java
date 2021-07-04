package com.boot.jx.xms.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalObjectWrapper<T> {
	/** DDO-Digital Data Object **/
	public static final String DDO = "DDO";
	String type;
	String id;
	T data;
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

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
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
