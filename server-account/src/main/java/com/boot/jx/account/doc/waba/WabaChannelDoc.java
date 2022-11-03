package com.boot.jx.account.doc.waba;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditCreateEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.postman.PMEnvironment;
import com.fasterxml.jackson.annotation.JsonView;

@Document(collection = "PARTNER_WABA_CHANNELS")
@TypeAlias("WabaChannel")
public class WabaChannelDoc implements IDocument, AuditCreateEntity, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;
	@Indexed
	private String clientId;

	private Map<String, Object> channel;

	private Long createdStamp;
	private String createdBy;
	private Long modifiedStamp;
	private String modifiedBy;
	private Boolean isActive;
	private Boolean isSyncd;
	private Long syncdStamp;

	@JsonView(PMEnvironment.ProtectedProperty.class)
	private Map<String, Object> key;

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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Boolean getIsSyncd() {
		return isSyncd;
	}

	public void setIsSyncd(Boolean isSyncd) {
		this.isSyncd = isSyncd;
	}

	public Long getSyncdStamp() {
		return syncdStamp;
	}

	public void setSyncdStamp(Long syncdStamp) {
		this.syncdStamp = syncdStamp;
	}

	public Map<String, Object> getChannel() {
		return channel;
	}

	public void setChannel(Map<String, Object> channel) {
		this.channel = channel;
	}

	public Map<String, Object> getKey() {
		return key;
	}

	public void setKey(Map<String, Object> key) {
		this.key = key;
	}

}
