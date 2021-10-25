package com.boot.jx.postman.wa360;

import java.io.Serializable;
import java.util.List;

public class WA360Template implements Serializable {
    private static final long serialVersionUID = -3743792916425816429L;

    private String category;

    private String lang;

    private String name;

    private String namespace;

    private String status;

    private String rejected_reason;

    private List<Object> components;

    public String getCategory() {
	return category;
    }

    public void setCategory(String category) {
	this.category = category;
    }

    public String getLang() {
	return lang;
    }

    public void setLang(String lang) {
	this.lang = lang;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getNamespace() {
	return namespace;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
    }

    public String getRejected_reason() {
	return rejected_reason;
    }

    public void setRejected_reason(String rejected_reason) {
	this.rejected_reason = rejected_reason;
    }

    public List<Object> getComponents() {
	return components;
    }

    public void setComponents(List<Object> components) {
	this.components = components;
    }
}
