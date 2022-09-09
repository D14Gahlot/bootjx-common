package com.boot.jx.postman.pbook;

import java.io.Serializable;

import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public class PBWebsite implements Serializable, Comparable<PBWebsite> {

	private static final long serialVersionUID = -7496133827194014822L;
	public String url;
	public String type;
	public String label;

	@Override
	public String toString() {
		return url;
	}

	@Override
	public int compareTo(PBWebsite o) {
		if (o == null) {
			return 1;
		}
		return this.toString().compareTo(o.toString());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PBWebsite that = (PBWebsite) o;
		if (ArgUtil.equalsIgnoreCase(this.url, that.url)) {
			return false;
		}
		return true;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
