package com.boot.jx.postman.doc;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "CAMPAIGN_CONTACT")
@TypeAlias("CampaignContactDoc")
public class CampaignContactDoc implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Indexed
	private String campaign_id;
	
	private String campaign_name;
	private long timestamp;
	
	private String createdBy;
	
	private boolean isActive;
	//private Map<String, String>info;
	
	private List<String> phone;

	
	public List<String> getPhone() {
		return phone;
	}
	public void setPhone(List<String> phone) {
		this.phone = phone;
	}
	public String getCampaign_name() {
		return campaign_name;
	}
	public void setCampaign_name(String campaign_name) {
		this.campaign_name = campaign_name;
	}

	public boolean isActive() {
		return isActive;
	}
	public void setIsActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getCampaign_id() {
		return campaign_id;
	}
	public void setCampaign_id(String campaign_id) {
		this.campaign_id = campaign_id;
	}
	
	public long getTimetsamp() {
		return timestamp;
	}
	public void setTimetsamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	
}
