package com.boot.jx.xms.dto;

import java.util.Map;

import com.boot.jx.swagger.ApiMockModelProperty;

public class OutBoundMsgTemplate {

    @ApiMockModelProperty(example = "FEEDBACK", required = true, value = "Code of Template",
	    notes = "This is Template will be searched in repository and match will be served")
    public String code;

    @ApiMockModelProperty(example = "en_US", required = false, value = "Language of Template to pick",
	    notes = "Language is optional an param which fallback to en all the scenarios of missing params/template")
    public String lang;

    @ApiMockModelProperty(example = "{ amount : 10, currency : 'INR' }", required = false,
	    value = "Data will be used to resolve placeholders in template, in case of missing value blank will be attempted, "
		    + "\n Kindly note Template may be rejected in case it does not match the approved format")
    public Map<String, Object> data;

}