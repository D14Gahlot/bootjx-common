package com.boot.jx.account.doc.waba;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditCreateEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;

@Document(collection = "PARTNER_WABA_LOGS")
@TypeAlias("WabaPartnerLog")
public class WabaPartnerLog implements IDocument, AuditCreateEntity, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;

	private Boolean isPrimaryPartner;
	private Map<String, Object> authorization;
	private Map<String, Object> profile;

	@Indexed
	private String partnerId;

	@Indexed
	private String clientId;
	private String domainUserId;
	private List<String> allowedChannel;
	private List<String> revokedChannel;

	private Long createdStamp;
	private String createdBy;
	private Long modifiedStamp;
	private String modifiedBy;
	private Boolean isActive;

	private String eventType;
	private List<String> eventTypes;
	private Map<String, Object> eventPayload;

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public Long getModifiedStamp() {
		return modifiedStamp;
	}

	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Boolean getIsPrimaryPartner() {
		return isPrimaryPartner;
	}

	public void setIsPrimaryPartner(Boolean isPrimaryPartner) {
		this.isPrimaryPartner = isPrimaryPartner;
	}

	public Map<String, Object> getAuthorization() {
		return authorization;
	}

	public void setAuthorization(Map<String, Object> authorization) {
		this.authorization = authorization;
	}

	public Map<String, Object> getProfile() {
		return profile;
	}

	public void setProfile(Map<String, Object> profile) {
		this.profile = profile;
	}

	public String getDomainUserId() {
		return domainUserId;
	}

	public void setDomainUserId(String domainUserId) {
		this.domainUserId = domainUserId;
	}

	public List<String> getAllowedChannel() {
		return allowedChannel;
	}

	public void setAllowedChannel(List<String> allowedChannel) {
		this.allowedChannel = allowedChannel;
	}

	public List<String> getRevokedChannel() {
		return revokedChannel;
	}

	public void setRevokedChannel(List<String> revokedChannel) {
		this.revokedChannel = revokedChannel;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public Map<String, Object> getEventPayload() {
		return eventPayload;
	}

	public void setEventPayload(Map<String, Object> eventPayload) {
		this.eventPayload = eventPayload;
	}

	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> eventTypes() {
		if (eventTypes == null) {
			eventTypes = new ArrayList<String>();
		}
		return eventTypes;
	}

	public WabaPartnerLog eventType(String eventType) {
		this.eventType = eventType;
		this.eventTypes().add(eventType);
		return this;
	}
}
