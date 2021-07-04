package com.boot.jx.views;

import java.io.Serializable;

public class TableField implements Serializable {
	private static final long serialVersionUID = -5458003634490307376L;
	private String key;
	private String label;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}
