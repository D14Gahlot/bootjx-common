package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.Patchable;

@Document(collection = DigitalDataDoc.COLLECTION_NAME)
@TypeAlias("DigitalDataDoc")
public class DigitalDataDoc implements Serializable, Patchable<DigitalDataDoc> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** DDO --Digital data Object **/
	public static final String COLLECTION_NAME = "DDO";

	@Id
	private String id;
	private String ddoId;
	private String type;
	Map<String, Object> data;
	List<DigitalObjectLink> links;
	Long createdStamp;
	Long updatedStamp;

	public List<DigitalObjectLink> getLinks() {
		return links;
	}

	public void setLinks(List<DigitalObjectLink> links) {
		this.links = links;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public Long getUpdatedStamp() {
		return updatedStamp;
	}

	public void setUpdatedStamp(Long updatedStamp) {
		this.updatedStamp = updatedStamp;
	}

	

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDdoId() {
		return ddoId;
	}

	public void setDdoId(String ddoId) {
		this.ddoId = ddoId;
	}

	/*
	 * public DigitalWrapper getDigitalWrapper() { return digitalWrapper; }
	 * 
	 * 
	 * 
	 * public void setDigitalWrapper(DigitalWrapper digitalWrapper) {
	 * this.digitalWrapper = digitalWrapper; }
	 */

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static String getCollectionName() {
		return COLLECTION_NAME;
	}

	@Override
	public DigitalDataDoc patch() {
		// TODO Auto-generated method stub
		return null;
	}
}
