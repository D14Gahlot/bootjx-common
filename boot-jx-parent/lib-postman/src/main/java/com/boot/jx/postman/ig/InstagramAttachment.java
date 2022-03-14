package com.boot.jx.postman.ig;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramAttachment implements Serializable {
	private static final long serialVersionUID = 260395496229953084L;
	private String type;
	private InstagramPayload payload;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public InstagramPayload getPayload() {
		return payload;
	}

	public void setPayload(InstagramPayload payload) {
		this.payload = payload;
	}
}
