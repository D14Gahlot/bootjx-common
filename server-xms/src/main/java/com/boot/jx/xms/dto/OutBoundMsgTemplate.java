package com.boot.jx.xms.dto;

import java.util.Map;

import com.boot.jx.swagger.ApiMockModelProperty;

public class OutBoundMsgTemplate {

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

}