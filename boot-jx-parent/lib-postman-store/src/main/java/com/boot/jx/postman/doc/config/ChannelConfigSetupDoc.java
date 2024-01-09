package com.boot.jx.postman.doc.config;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;

@Document(collection = "CONFIG_CHANNEL_SETUP")
@TypeAlias("ChannelConfigSetup")
public class ChannelConfigSetupDoc implements Serializable {

	private static final long serialVersionUID = -6368905475787041196L;

	@Id
	private String id;
	private CHANNEL_TYPE_ENUM channelType;
	private ContactType contactType;

	private String metaAppId;
	private String metaAppSecret;
	private String metaAppVerifyToken;

	private String domain;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public CHANNEL_TYPE_ENUM getChannelType() {
		return channelType;
	}

	public void setChannelType(CHANNEL_TYPE_ENUM channelType) {
		this.channelType = channelType;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}

	public String getMetaAppId() {
		return metaAppId;
	}

	public void setMetaAppId(String metaAppId) {
		this.metaAppId = metaAppId;
	}

	public String getMetaAppSecret() {
		return metaAppSecret;
	}

	public void setMetaAppSecret(String metaAppSecret) {
		this.metaAppSecret = metaAppSecret;
	}

	public String getMetaAppVerifyToken() {
		return metaAppVerifyToken;
	}

	public void setMetaAppVerifyToken(String metaAppVerifyToken) {
		this.metaAppVerifyToken = metaAppVerifyToken;
	}

}
