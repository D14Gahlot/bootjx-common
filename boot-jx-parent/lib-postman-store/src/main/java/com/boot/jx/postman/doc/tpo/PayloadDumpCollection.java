package com.boot.jx.postman.doc.tpo;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "PAYLOAD_DUMP")
public class PayloadDumpCollection {

	@Id
	private String dumpId;

	@Indexed
	private String type;

	List<Object> incomingRequest;

	public void setIncomingRequest(List<Object> incomingRequest) {
		this.incomingRequest = incomingRequest;
	}

	public List<Object> getIncomingRequest() {
		return incomingRequest;
	}

	public String getDumpId() {
		return dumpId;
	}

	public void setDumpId(String dumpId) {
		this.dumpId = dumpId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
