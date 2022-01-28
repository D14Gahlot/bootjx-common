package com.boot.jx.xms.dto;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionQueueAssignment implements Serializable {
    private static final long serialVersionUID = 4064758284063588819L;
    @ApiMockModelProperty(example = "61f3810a02e14c0877fc1a32", required = true, value = "Session to be routed")
    public String sessionId;

    @ApiMockModelProperty(example = "external_bot", required = true,
	    value = "Next queue where session should be routed")
    public String queue;
}
