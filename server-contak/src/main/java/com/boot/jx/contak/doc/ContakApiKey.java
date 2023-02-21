package com.boot.jx.contak.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;

@Document(collection = "CONTAK_API_KEY")
@TypeAlias("ContakApiKey")
public class ContakApiKey implements IDocument, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;

	@Indexed
	private String secretHash;

	private String sessionKey;

	private String userId;
	private String companyId;
	private String clientId;

	private boolean active;

	public TimeStampIndex createdAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getSecretHash() {
		return secretHash;
	}

	public void setSecretHash(String secretHash) {
		this.secretHash = secretHash;
	}

	public String getSessionKey() {
		return sessionKey;
	}

	public void setSessionKey(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public String sessionKey() {
		if (sessionKey == null) {
			this.sessionKey = (this.clientId + ":" + this.id + "-" + this.secretHash);
		}
		return sessionKey;
	}

}
