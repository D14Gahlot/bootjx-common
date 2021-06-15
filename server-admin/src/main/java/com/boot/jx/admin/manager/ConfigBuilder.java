package com.boot.jx.admin.manager;

import java.io.Serializable;
import java.util.List;

import com.boot.utils.CollectionUtil;

import java.util.ArrayList;

public class ConfigBuilder implements Serializable {

	public static enum InputType {
		TEXT, OPTIONS, RANGE, NUMBER
	}

	private static final long serialVersionUID = -8418291522478302778L;

	public static final List<ConfigBuilder> LIST = new ArrayList<ConfigBuilder>();

	private String title;
	private String key;
	private InputType inputType;

	private List<Object> options;

	public ConfigBuilder(String title, String key) {
		this.key = key;
		this.title = title;
	}

	static {
		LIST.add(new ConfigBuilder("Bot Name", "postman.default.sender"));
		LIST.add(new ConfigBuilder("Contact Details Provider Webhook", "postman.contact.details.url"));
		LIST.add(new ConfigBuilder("Chat Tag Enabled", "chat.tag.enabled")
				.options(CollectionUtil.getList(Boolean.TRUE, Boolean.FALSE)));

		LIST.add(new ConfigBuilder("Chat Session Timeout", "postman.chat.session.timeout")
				.options(CollectionUtil.getList("8hr", "12hr", "16hr", "20hr", "24hr")));

		LIST.add(new ConfigBuilder("Chat Alert Timer", "postman.chat.idle.timeout")
				.options(CollectionUtil.getList("5min", "10min", "15min", "20min", "25min", "30min")));

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<Object> getOptions() {
		return options;
	}

	public void setOptions(List<Object> options) {
		this.options = options;
	}

	public ConfigBuilder options(List<Object> options) {
		if (this.inputType == null) {
			this.inputType = InputType.OPTIONS;
		}
		this.options = options;
		return this;
	}

	public InputType getInputType() {
		return inputType;
	}

	public void setInputType(InputType inputType) {
		this.inputType = inputType;
	}

	public ConfigBuilder inputType(InputType inputType) {
		this.inputType = inputType;
		return this;
	}
}
