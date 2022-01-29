package com.boot.jx.postman.model.ext;

import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundMeta {
    @ApiMockModelProperty(example = "alex", value = "your domain name")
    public String domain;

    @ApiMockModelProperty(example = "server.com", value = "Server as per Enviroment")
    public String server;

    public InBoundMeta domain(String domain) {
	this.domain = domain;
	return this;
    }

    public InBoundMeta server(String server) {
	this.server = server;
	return this;
    }
}