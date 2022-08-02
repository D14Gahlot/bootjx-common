package com.boot.jx.contak.dto;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookUrlRequest implements Serializable {
    private static final long serialVersionUID = 8095410793094848402L;

    @JsonProperty("url")
    @ApiMockModelProperty(example = "https://www.example.com/webhook", required = true,
	    value = "The webhook URL can either be: " + "- the URL from your own application" + "- or the partner")
    public String url;

    @ApiMockModelProperty(example = "https://www.example.com/webhook", required = false, hidden = true,
	    value = "The Forward URL can either be: " + "- the URL from your own application" + "- or the partner")
    public String forward;

}
