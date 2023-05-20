package com.boot.jx.contak.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ContakMessageTrace implements Serializable {

	private static final long serialVersionUID = 7243640883747555047L;

	@Id
	public String noteId;

	@Indexed
	public String phoneId;

	@Indexed
	public String domain;

	@Indexed
	public String companyId;

	public long msgGenId;

	public TimeStampIndex createdAt;

	public TimeStampIndex relayedAt;

	public TimeStampIndex expiredAt;

	public TimeStampIndex readAt;

	public TimeStampIndex deliveredAt;

	public String getNoteId() {
		return noteId;
	}

	public void setNoteId(String noteId) {
		this.noteId = noteId;
	}

	public String getPhoneId() {
		return phoneId;
	}

	public void setPhoneId(String phoneId) {
		this.phoneId = phoneId;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public TimeStampIndex getRelayedAt() {
		return relayedAt;
	}

	public void setRelayedAt(TimeStampIndex relayedAt) {
		this.relayedAt = relayedAt;
	}

	public TimeStampIndex getExpiredAt() {
		return expiredAt;
	}

	public void setExpiredAt(TimeStampIndex expiredAt) {
		this.expiredAt = expiredAt;
	}

	public TimeStampIndex getReadAt() {
		return readAt;
	}

	public void setReadAt(TimeStampIndex readAt) {
		this.readAt = readAt;
	}

	public TimeStampIndex getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(TimeStampIndex deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public long getMsgGenId() {
		return msgGenId;
	}

	public void setMsgGenId(long msgGenId) {
		this.msgGenId = msgGenId;
	}

}