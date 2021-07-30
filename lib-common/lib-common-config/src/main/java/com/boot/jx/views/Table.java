package com.boot.jx.views;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable {

	private static final long serialVersionUID = 4804122598839771662L;

	private List<TableField> fields;

	private Integer perPage;

	public Integer getPerPage() {
		return perPage;
	}

	public void setPerPage(Integer perPage) {
		this.perPage = perPage;
	}

	public List<TableField> getFields() {
		return fields;
	}

	public void setFields(List<TableField> fields) {
		this.fields = fields;
	}

	public List<TableField> fields() {
		if (fields == null) {
			fields = new ArrayList<TableField>();
		}
		return this.fields;
	}

	public Table fields(TableField field) {
		this.fields().add(field);
		return this;
	}
}
