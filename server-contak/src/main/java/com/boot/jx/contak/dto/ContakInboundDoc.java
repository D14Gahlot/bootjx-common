package com.boot.jx.contak.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "CONTAK_INBOUND")
public class ContakInboundDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String inboundId;

	@Indexed
	public String phoneId;;

	@Indexed
	public String companyId;

	@Indexed
	public String inboundType;

	public TimeStampIndex createdAt;

	public TimeStampIndex deliveredAt;

	public Object inboundPayload;

	public String getInboundId() {
		return inboundId;
	}

	public void setInboundId(String inboundId) {
		this.inboundId = inboundId;
	}

	public String getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public TimeStampIndex getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(TimeStampIndex deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public String getInboundType() {
		return inboundType;
	}

	public void setInboundType(String inboundType) {
		this.inboundType = inboundType;
	}

	public Object getInboundPayload() {
		return inboundPayload;
	}

	public void setInboundPayload(Object inboundPayload) {
		this.inboundPayload = inboundPayload;
	}

}