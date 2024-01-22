package com.boot.jx.postman.doc.tpo;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.model.MessageReport;
import com.boot.model.MapModel;

@Document(collection = "Dummy Collection")
public class DummyCollection {
	List<Object> incomingRequest;

	public void setIncomingRequest(List<Object> incomingRequest) {
		this.incomingRequest = incomingRequest;
	}

	public List<Object> getIncomingRequest() {
		return incomingRequest;
	}

}
