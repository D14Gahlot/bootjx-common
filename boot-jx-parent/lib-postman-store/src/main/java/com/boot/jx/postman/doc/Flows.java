package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = Flows.COLLECTION_NAME)
@TypeAlias("Flow")
public class Flows implements Serializable {

    public static final String COLLECTION_NAME = "TP_FLOWS";

    private static final long serialVersionUID = 1L;

    private Map<String, Object> data;

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}
}