package com.boot.jx.postman.doc.tpo;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = WABAFlows.COLLECTION_NAME)
@TypeAlias("Flow")
public class WABAFlows implements Serializable {

	public static final String COLLECTION_NAME = "TP_WABA_FLOWS";

	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	private String name;
	private String status;
	private List<String> categories;
	private List<String> validationErrors;

	// Getters and Setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getValidationErrors() {
		return validationErrors;
	}

	public void setValidationErrors(List<String> validationErrors) {
		this.validationErrors = validationErrors;
	}

}