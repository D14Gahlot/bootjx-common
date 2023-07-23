package com.boot.jx.contak.cache;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.swagger.ApiMockModelProperty;

@DynamoDBTable(tableName = "OtpAlertEvent")
public class OtpAlertEvent {

	@Id
	private OtpAlertEventId otpAlertEventId;

	private String companyId;

	private String phoneId;

	@Indexed
	@ApiMockModelProperty(example = "text", value = "Inbound type",
			allowableValues = "USER_REG,MSG_OUT_DELIVERED,MSG_OUT_READ")
	public String inboundType;

	public Object inboundPayload;

	public MessageEvent event;

	// Stamps
	public TimeStampIndex createdAt;

	public TimeStampIndex notifiedAt;

	public TimeStampIndex expiredAt;

	public OtpAlertEvent() {
	}

	public OtpAlertEvent(OtpAlertEventId improvedMusicId, String companyId, String phoneId) {
		this.otpAlertEventId = improvedMusicId;
		this.companyId = companyId;
		this.phoneId = phoneId;
	}

	public OtpAlertEvent(OtpAlertEventId improvedMusicId) {
		this.otpAlertEventId = improvedMusicId;
	}

	@DynamoDBHashKey(attributeName = "EventId")
	public String getEventId() {
		return otpAlertEventId != null ? otpAlertEventId.getEventId() : null;
	}

	public void setEventId(String statusId) {
		if (otpAlertEventId == null) {
			otpAlertEventId = new OtpAlertEventId();
		}
		otpAlertEventId.setEventId(statusId);
	}

	@DynamoDBRangeKey(attributeName = "CreatedHour")
	@DynamoDBIndexRangeKey(globalSecondaryIndexName = "CompanyIndex")
	public Long getCreatedHour() {
		return otpAlertEventId != null ? otpAlertEventId.getCreatedHour() : null;
	}

	public void setCreatedHour(Long createdHour) {
		if (otpAlertEventId == null) {
			otpAlertEventId = new OtpAlertEventId();
		}
		otpAlertEventId.setCreatedHour(createdHour);
	}

	@DynamoDBAttribute(attributeName = "CompanyId")
	@DynamoDBIndexHashKey(globalSecondaryIndexName = "CompanyIndex")
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String comapnyId) {
		this.companyId = comapnyId;
	}

	@DynamoDBAttribute
	public String getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(String songTitle) {
		this.phoneId = songTitle;
	}

	@DynamoDBAttribute
	public String getInboundType() {
		return inboundType;
	}

	public void setInboundType(String inboundType) {
		this.inboundType = inboundType;
	}

	@DynamoDBAttribute
	public Object getInboundPayload() {
		return inboundPayload;
	}

	public void setInboundPayload(Object inboundPayload) {
		this.inboundPayload = inboundPayload;
	}

	@DynamoDBAttribute
	public MessageEvent getEvent() {
		return event;
	}

	public void setEvent(MessageEvent event) {
		this.event = event;
	}

	@DynamoDBAttribute
	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	@DynamoDBAttribute
	public TimeStampIndex getNotifiedAt() {
		return notifiedAt;
	}

	public void setNotifiedAt(TimeStampIndex notifiedAt) {
		this.notifiedAt = notifiedAt;
	}

	@DynamoDBAttribute
	public TimeStampIndex getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(TimeStampIndex expiredAt) {
		this.expiredAt = expiredAt;
	}
}
