package com.boot.jx.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.def.CommonInterfaces.ICommonTemplate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonTemplate implements Serializable, ICommonTemplate {

    private static final long serialVersionUID = -254665668785648863L;
    private String lang;
    private String code;
    private String id;
    private Map<String, Object> data = new HashMap<String, Object>();

    @Override
    public String getCode() {
	return code;
    }

    @Override
    public void setCode(String code) {
	this.code = code;
    }

    @Override
    public String getId() {
	return id;
    }

    @Override
    public void setId(String id) {
	this.id = id;
    }

    @Override
    public String getLang() {
	return lang;
    }

    @Override
    public void setLang(String lang) {
	this.lang = lang;
    }

    public String toString() {
	return String.format("%s_%s(%s)", code, lang, id);
    }

    public Map<String, Object> getData() {
	return data;
    }

    public void setData(Map<String, Object> data) {
	this.data = data;
    }
}
