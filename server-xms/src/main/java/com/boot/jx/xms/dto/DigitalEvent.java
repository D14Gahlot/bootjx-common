package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.Patchable;

@Document(collection ="DDE_EVENT")
@TypeAlias("DigitalEvent")
public class DigitalEvent implements Serializable, Patchable<DigitalDataDoc> {
	
	
	
	String id;
	String eventName;
	List<DigitalObjectLink> links;
	@Id
	Long createdStamp;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}
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
		this.createdStamp = System.currentTimeMillis();
	}
	@Override
	public DigitalDataDoc patch() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}

