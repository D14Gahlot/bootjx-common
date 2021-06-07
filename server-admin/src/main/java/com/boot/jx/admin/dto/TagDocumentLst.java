package com.boot.jx.admin.dto;

import java.io.Serializable;

public class TagDocumentLst implements Serializable {

	private static final long serialVersionUID = -974444236167952834L;
	String type;
	String tag;
	int count;

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
