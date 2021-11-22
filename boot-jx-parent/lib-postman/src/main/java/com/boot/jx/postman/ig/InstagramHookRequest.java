package com.boot.jx.postman.ig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramHookRequest implements Serializable {
	private static final long serialVersionUID = 8236994911573208877L;
	private String object;
	private List<InstagramEntry> entry = new ArrayList<>();
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	public List<InstagramEntry> getEntry() {
		return entry;
	}
	public void setEntry(List<InstagramEntry> entry) {
		this.entry = entry;
	}
}
