package com.boot.jx;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonPropertyOrder({ "param", "enabled", "property", "value" })
public enum AppParam {

	PRINT_TRACK_BODY(false), DEBUG_INFO,

	APP_ENV, APP_GROUP, APP_NAME, APP_ID, APP_VERSION, APP_BUILDTIMESTAMP, APP_CONTEXT_PREFIX,

	APP_PROD, APP_SWAGGER, APP_DEBUG, APP_CACHE, APP_AUTH_ENABLED, APP_LOGGER, APP_MONITOR,

	// Defaults
	DEFAULT_TENANT,

	JAX_CDN_URL, JAX_APP_URL, JAX_SERVICE_URL, JAX_POSTMAN_URL, JAX_PAYMENT_URL, JAX_LOGGER_URL, JAX_SSO_URL,
	JAX_AUTH_URL, JAX_RADAR_URL,

	SPRING_REDIS_HOST, SPRING_REDIS_PORT, JAX_PRICER_URL, SPRING_APP_NAME, JAX_SERVICE_PROVIDER_URL, JAX_CASHIER_URL,
	JAX_DROOL_URL;

	@JsonProperty("_id")
	String param;

	boolean enabled;
	String value;
	String property;

	AppParam() {
		this(false);
	}

	AppParam(boolean enabled) {
		this.param = this.name();
		this.enabled = enabled;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

}
