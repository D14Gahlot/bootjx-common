package com.boot.jx.xms.dto;

import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DigitalEventDto {

	@ApiMockModelProperty(example = "1234567", value = "Unique  Id assigined by event ")
	String id;
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
	/** evnet Name  customer_created , beneficairy_created, appl_created ,transaction_Created  etc**/
	@ApiMockModelProperty(example = "CUSTOMER_CREATED", value = "Event Name and action ")
	String eventName;
	List<DigitalObjectLink> links;
	
	
}
