package com.boot.jx.contak.cache;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

public class OtpAlertEventId {

	public static final String OTP_ALERT_EVENT_TABLE_PREFIX = "OtpAlertEvent";
	public static final String OTP_ALERT_EVENT_TABLE_VERSION = "V";
	public static final String OTP_ALERT_EVENT_TABLE = OTP_ALERT_EVENT_TABLE_PREFIX + OTP_ALERT_EVENT_TABLE_VERSION;
	public static final String OTP_ALERT_EVENT_COMPANY_INDEX = OTP_ALERT_EVENT_TABLE + "CompanyIndex";
	public static final String OTP_ALERT_EVENT_STATUS_INDEX = OTP_ALERT_EVENT_TABLE + "StatusIndex";

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
	@DynamoDBIndexRangeKey(globalSecondaryIndexName = OTP_ALERT_EVENT_COMPANY_INDEX)
	public Long getCreatedHour() {
		return createdHour;
	}

	public void setCreatedHour(Long createdHour) {
		this.createdHour = createdHour;
	}
}
