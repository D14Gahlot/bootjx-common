package com.boot.jx.api;

import java.util.List;

public class ListRequestModel<T> implements IRespModel {
	private static final long serialVersionUID = 546575527587061399L;
	public List<T> values;

	public List<T> getValues() {
		return this.values;
	}

	public void setValues(List<T> values) {
		this.values = values;
	}
}
