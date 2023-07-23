package com.boot.jx.contak.cache;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

public class OtpAlertEventId {

	private String eventId;
	private Long createdHour;

	public OtpAlertEventId() {
	}

	public OtpAlertEventId(String statusId, Long createdHour) {
		this.eventId = statusId;
		this.createdHour = createdHour;
	}

	@DynamoDBHashKey(attributeName = "EventId")
	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	@DynamoDBRangeKey(attributeName = "CreatedHour")
	@DynamoDBIndexRangeKey(globalSecondaryIndexName = "CompanyIndex")
	public Long getCreatedHour() {
		return createdHour;
	}

	public void setCreatedHour(Long createdHour) {
		this.createdHour = createdHour;
	}
}
