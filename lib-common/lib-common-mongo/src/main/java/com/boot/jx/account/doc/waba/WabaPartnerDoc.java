package com.boot.jx.account.doc.waba;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditCreateEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;

@Document(collection = "PARTNER_WABA_CLIENTS")
@TypeAlias("WabaPartner")
public class WabaPartnerDoc implements IDocument, AuditCreateEntity, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;

	private Boolean isPrimaryPartner;
	private Map<String, Object> authorization;
	private Map<String, Object> profile;

	@Indexed
	private String partnerId;
	private String username;
	private String password;

	@Indexed
	private String clientId;
	private Map<String, Object> client;
	private Map<String, Object> balance;
	
	private String domainUserId;
	private List<String> allowedChannel;
	private List<String> revokedChannel;

	private Long createdStamp;
	private String createdBy;
	private Long modifiedStamp;
	private String modifiedBy;
	private Boolean isActive;
	private Boolean isSyncd;
	private Long syncdStamp;
	private Long balanceStamp;

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

	public Map<String, Object> getClient() {
		return client;
	}

	public void setClient(Map<String, Object> client) {
		this.client = client;
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

	public Map<String, Object> getBalance() {
		return balance;
	}

	public void setBalance(Map<String, Object> balance) {
		this.balance = balance;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getBalanceStamp() {
		return balanceStamp;
	}

	public void setBalanceStamp(Long balanceStamp) {
		this.balanceStamp = balanceStamp;
	}

}
