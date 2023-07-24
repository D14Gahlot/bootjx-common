package com.boot.jx.contak.cache;

import java.util.Map;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.boot.jx.contak.dto.PhoneLoginDTO.MessageEvent;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.model.TimeModels.ITimeStampIndexAbstract;

@DynamoDBTable(tableName = OtpAlertEventId.OTP_ALERT_EVENT_TABLE)
public class OtpAlertEvent {

	@Id
	private OtpAlertEventId otpAlertEventId;

	private String companyId;

	private String phoneId;

	public static enum CompanyQueueStatus {
		CRTD, NTFD, FLD, XPRD
	}

	@ApiMockModelProperty(example = "text", value = "Current Status", allowableValues = "CRTD,NTFD,FLD,XPRD")
	private String status;

	private String companyQueue;

	@ApiMockModelProperty(example = "text", value = "Inbound type",
			allowableValues = "USER_REG,MSG_OUT_DELIVERED,MSG_OUT_READ")
	public String inboundType;

	public Map<String, Object> inboundPayload;

	public MessageEventDynmo event;

	// Stamps
	public TimeStampIndexDynmo createdAt;

	public TimeStampIndexDynmo notifiedAt;

	public TimeStampIndexDynmo expiredAt;

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
	@DynamoDBIndexRangeKey(globalSecondaryIndexName = OtpAlertEventId.OTP_ALERT_EVENT_COMPANY_INDEX)
	public Long getCreatedHour() {
		return otpAlertEventId != null ? otpAlertEventId.getCreatedHour() : null;
	}

	public void setCreatedHour(Long createdHour) {
		if (otpAlertEventId == null) {
			otpAlertEventId = new OtpAlertEventId();
		}
		otpAlertEventId.setCreatedHour(createdHour);
	}

	@DynamoDBIndexHashKey(globalSecondaryIndexName = OtpAlertEventId.OTP_ALERT_EVENT_COMPANY_INDEX)
	public String getCompanyQueue() {
		return companyQueue;
	}

	public void setCompanyQueue(String companyQueue) {
		this.companyQueue = companyQueue;
	}

	@DynamoDBAttribute(attributeName = "CompanyId")
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String comapnyId) {
		this.companyId = comapnyId;
	}

	@DynamoDBAttribute
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
	public Map<String, Object> getInboundPayload() {
		return inboundPayload;
	}

	public void setInboundPayload(Map<String, Object> inboundPayload) {
		this.inboundPayload = inboundPayload;
	}

	@DynamoDBAttribute
	public MessageEventDynmo getEvent() {
		return event;
	}

	public void setEvent(MessageEventDynmo event) {
		this.event = event;
	}

	@DynamoDBAttribute
	public TimeStampIndexDynmo getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndexDynmo createdAt) {
		this.createdAt = createdAt;
	}

	@DynamoDBAttribute
	public TimeStampIndexDynmo getNotifiedAt() {
		return notifiedAt;
	}

	public void setNotifiedAt(TimeStampIndexDynmo notifiedAt) {
		this.notifiedAt = notifiedAt;
	}

	@DynamoDBAttribute
	public TimeStampIndexDynmo getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(TimeStampIndexDynmo expiredAt) {
		this.expiredAt = expiredAt;
	}

	@DynamoDBDocument
	public static class TimeStampIndexDynmo extends ITimeStampIndexAbstract<TimeStampIndexDynmo> {
		private static final long serialVersionUID = 680494594671730973L;

		public static TimeStampIndexDynmo from(long stamp) {
			return new TimeStampIndexDynmo().fromStamp(stamp);
		}

		public static TimeStampIndexDynmo now() {
			return new TimeStampIndexDynmo().fromNow();
		}

		public static TimeStampIndexDynmo from(TimeStampIndex createdAt) {
			return new TimeStampIndexDynmo().fromStamp(createdAt.getStamp());
		}
	}

	@DynamoDBDocument
	public static class MessageEventDynmo extends MessageEvent {
		private static final long serialVersionUID = 1L;
	}

	public OtpAlertEvent update(CompanyQueueStatus status) {
		this.setStatus(status.name());
		this.companyQueue = this.companyId + "#" + status.name();
		return this;
	}
}
