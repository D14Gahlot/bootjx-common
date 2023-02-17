package com.boot.jx.contak.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.contak.dto.ContakModel;
import com.boot.jx.contak.dto.ContakTemplate;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "CONTAK_MESSAGES")
public class ContakMessageDoc implements Serializable {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String noteId;

	@Indexed
	public String phoneId;

	@Indexed
	public String domain;

	public TimeStampIndex createdAt;

	public TimeStampIndex relayedAt;

	public TimeStampIndex expiredAt;

	public TimeStampIndex readAt;

	public TimeStampIndex deliveredAt;

	public String title;

	public String message;

	public String otp;
	public String type; // OTP,TRAN,PROM
	public String pubKey;
	public long msgGenId;
	public String companyId;
	public String companyName;
	public long companyStamp;
	public String logoUrl;

	public ContakModel model;
	public String modelEncrypted;
	public ContakTemplate template;

	private List<String> tags;

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public long getMsgGenId() {
		return msgGenId;
	}

	public void setMsgGenId(long msgGenId) {
		this.msgGenId = msgGenId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public TimeStampIndex getRelayedAt() {
		return relayedAt;
	}

	public void setRelayedAt(TimeStampIndex relayedAt) {
		this.relayedAt = relayedAt;
	}

	public ContakTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ContakTemplate template) {
		this.template = template;
	}

	public ContakTemplate template() {
		if (!ArgUtil.is(this.template)) {
			this.template = new ContakTemplate();
		}
		return this.template;
	}

	public ContakModel getModel() {
		return model;
	}

	public void setModel(ContakModel model) {
		this.model = model;
	}

	public ContakModel model() {
		if (!ArgUtil.is(this.model)) {
			this.model = new ContakModel();
		}
		return this.model;
	}

	public long getCompanyStamp() {
		return companyStamp;
	}

	public void setCompanyStamp(long companyStamp) {
		this.companyStamp = companyStamp;
	}
}