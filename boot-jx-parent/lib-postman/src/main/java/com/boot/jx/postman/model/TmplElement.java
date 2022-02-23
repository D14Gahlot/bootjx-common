package com.boot.jx.postman.model;

import java.io.Serializable;

public class TmplElement implements Serializable {

    private static final long serialVersionUID = 8844236002971255681L;

    public static class TYPES {
	public static final String QUICK_REPLY = "QUICK_REPLY";
	public static final String URL = "URL";
    }

    private String name;
    private String label;
    private String type;
    private String desc;
    private String url;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public TmplElement name(String name) {
	this.name = name;
	return this;
    }

    public TmplElement label(String label) {
	this.label = label;
	return this;
    }

    public TmplElement type(String type) {
	this.type = type;
	return this;
    }

    public TmplElement url(String url) {
	this.url = url;
	return this;
    }

    public String getDesc() {
	return desc;
    }

    public void setDesc(String desc) {
	this.desc = desc;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }
}
