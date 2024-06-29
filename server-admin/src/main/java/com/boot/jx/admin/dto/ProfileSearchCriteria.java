package com.boot.jx.admin.dto;

public class ProfileSearchCriteria {
	private String key;
    private String operator;
    private Object value;
    
    public ProfileSearchCriteria(String key, String operator, Object value) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

}
