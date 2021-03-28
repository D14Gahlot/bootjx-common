package com.boot.jx.postman.model;

import java.util.Map;

import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public interface MessageOptions {

	@JsonIgnore
	public Map<String, Object> options();

	@JsonIgnore
	default public MessageOptions option(String key, String value) {
		this.options().get("msg_type");
		return this;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static interface WAMessageOptions extends MessageOptions {
		@JsonIgnore
		default public boolean isHSM() {
			return ArgUtil.parseAsBoolean(this.options().get("isHSM"), false);
		}

		@JsonIgnore
		default public String getMsgType() {
			return ArgUtil.parseAsString(this.options().get("msg_type"));
		}

		@JsonIgnore
		default public boolean isTemplate() {
			return ArgUtil.parseAsBoolean(this.options().get("isTemplate"), false);
		}

		@JsonIgnore
		default public boolean isQRButtons() {
			return ArgUtil.parseAsBoolean(this.options().get("isQRButtons"), false);
		}

		@JsonIgnore
		default public boolean isViaAgent() {
			return ArgUtil.parseAsBoolean(this.options().get("isViaAgent"), false);
		}
	}
}