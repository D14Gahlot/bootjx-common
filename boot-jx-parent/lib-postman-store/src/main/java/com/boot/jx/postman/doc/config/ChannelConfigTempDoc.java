package com.boot.jx.postman.doc.config;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.PMConstants.CHANNEL_TYPE_ENUM;
import com.boot.jx.postman.PMEnvironment;
import com.fasterxml.jackson.annotation.JsonView;

@Document(collection = "TEMP_CONFIG_CHANNEL")
@TypeAlias("ChannelConfigTemp")
public class ChannelConfigTempDoc implements Serializable {

	private static final long serialVersionUID = -6368905475787041196L;

	@Id
	private String id;

	@Indexed
	private String domain;
	private String lane;
	@Indexed
	private CHANNEL_TYPE_ENUM channelType;
	private String channelId;

	@JsonView(PMEnvironment.ProtectedProperty.class)
	protected String channelKey;

	private boolean isProxyEnabled;
	private boolean isSandbox;
	private boolean isShared;
	private boolean isDisabled;
	private boolean isDeleted;
	private String server;

	private String channelConfigId;

	private Map<String, Object> resp;

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

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public boolean isProxyEnabled() {
		return isProxyEnabled;
	}

	public void setProxyEnabled(boolean isProxyEnabled) {
		this.isProxyEnabled = isProxyEnabled;
	}

	public boolean isSandbox() {
		return isSandbox;
	}

	public void setSandbox(boolean isSandbox) {
		this.isSandbox = isSandbox;
	}

	public boolean isShared() {
		return isShared;
	}

	public void setShared(boolean isShared) {
		this.isShared = isShared;
	}

	public boolean isDisabled() {
		return isDisabled;
	}

	public void setDisabled(boolean isDisabled) {
		this.isDisabled = isDisabled;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	public void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public CHANNEL_TYPE_ENUM getChannelType() {
		return channelType;
	}

	public void setChannelType(CHANNEL_TYPE_ENUM channelType) {
		this.channelType = channelType;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getChannelKey() {
		return channelKey;
	}

	public void setChannelKey(String channelKey) {
		this.channelKey = channelKey;
	}

	public String getChannelConfigId() {
		return channelConfigId;
	}

	public void setChannelConfigId(String channelConfigId) {
		this.channelConfigId = channelConfigId;
	}

	public Map<String, Object> getResp() {
		return resp;
	}

	public void setResp(Map<String, Object> resp) {
		this.resp = resp;
	}

}
