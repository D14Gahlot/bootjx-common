package com.boot.jx.postman.model;

import java.util.Map;

import com.boot.utils.ArgUtil;

public interface MessageOptions {
	public Map<String, Object> options();

	default public MessageOptions option(String key, String value) {
		this.options().get("msg_type");
		return this;
	}

	public static interface WAMessageOptions extends MessageOptions {
		default public boolean isHSM() {
			return ArgUtil.parseAsBoolean(this.options().get("isHSM"), false);
		}

		default public String getMsgType() {
			return ArgUtil.parseAsString(this.options().get("msg_type"));
		}

		default public boolean isTemplate() {
			return ArgUtil.parseAsBoolean(this.options().get("isTemplate"), false);
		}

		default public boolean isQRButtons() {
			return ArgUtil.parseAsBoolean(this.options().get("isQRButtons"), false);
		}

		default public boolean isViaAgent() {
			return ArgUtil.parseAsBoolean(this.options().get("isViaAgent"), false);
		}
	}
}