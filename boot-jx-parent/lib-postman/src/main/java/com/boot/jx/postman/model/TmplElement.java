package com.boot.jx.postman.model;

import java.io.Serializable;

public class TmplElement implements Serializable {

    private static final long serialVersionUID = 8844236002971255681L;

    private String name;
    private String label;
    private String type;
    private String desc;

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

    public String getDesc() {
	return desc;
    }

    public void setDesc(String desc) {
	this.desc = desc;
    }
}
