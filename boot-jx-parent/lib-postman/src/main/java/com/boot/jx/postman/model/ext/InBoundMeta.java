package com.boot.jx.postman.model.ext;

import com.boot.jx.swagger.ApiMockModelProperty;

public class InBoundMeta {
	@ApiMockModelProperty(example = "alex", value = "your domain name")
	public String domain;

	@ApiMockModelProperty(example = "server.com", value = "Server as per Enviroment")
	public String server;

	@ApiMockModelProperty(example = "61ec7d9c2ce85742b201c5ab", value = "Client App Id if webhook is set for an App ",
			required = false)
	public String appId;

	public boolean debug;

	public InBoundMeta domain(String domain) {
		this.domain = domain;
		return this;
	}

	public InBoundMeta server(String server) {
		this.server = server;
		return this;
	}

	public InBoundMeta appId(String appId) {
		this.appId = appId;
		return this;
	}
	
	public InBoundMeta debug(boolean debug) {
		this.debug = debug;
		return this;
	}
}