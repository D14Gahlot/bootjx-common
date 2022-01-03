package com.boot.jx.postman.model.ext;

import java.util.Map;

import com.boot.jx.def.CommonInterfaces.ICommonTemplate;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(as = HSMTemplate.class)
public class HSMTemplate implements ICommonTemplate {

    private static final long serialVersionUID = -7807618398296479402L;

    @ApiMockModelProperty(example = "60d236c6142e53561cb7716c", required = false, value = "Unique Template Id",
	    notes = "Explicit template.id to be used for message")
    public String id;

    @ApiMockModelProperty(example = "FEEDBACK", required = false, value = "Code of Template", hidden = true,
	    notes = "Template Code will be searched in the repository and match will be served."
		    + "\n code will be ignored in case template.id is provided")
    public String code;

    @ApiMockModelProperty(example = "en_US", required = false, value = "Language of Template to pick", hidden = true,
	    notes = "Language is an optional param which fallback to en, in all the scenarios of missing params/template"
		    + "\n lang is ignored when template.id is provided")
    public String lang;

    @ApiMockModelProperty(example = "{ \"amount\" : 10, \"currency\" : \"INR\" }", required = false,
	    value = "Data will be used to resolve placeholders in template, in case of missing value blank will be attempted, "
		    + "\n Kindly note Template may be rejected in case it does not match the approved format")
    public Map<String, Object> data;

    public HSMTemplate id(String id) {
	this.id = id;
	return this;
    }

    public HSMTemplate code(String code) {
	this.code = code;
	return this;
    }

    public HSMTemplate lang(String lang) {
	this.lang = lang;
	return this;
    }

    public HSMTemplate data(Map<String, Object> data) {
	this.data = data;
	return this;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getCode() {
	return code;
    }

    public void setCode(String code) {
	this.code = code;
    }

    public String getLang() {
	return lang;
    }

    public void setLang(String lang) {
	this.lang = lang;
    }

    public Map<String, Object> getData() {
	return data;
    }

    public void setData(Map<String, Object> data) {
	this.data = data;
    }


}