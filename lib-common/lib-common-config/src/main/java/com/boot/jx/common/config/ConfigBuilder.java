package com.boot.jx.common.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigBuilder implements Serializable {

	public static enum InputType {
		TEXT, OPTIONS, RANGE, NUMBER, COLOR
	}

	public static class ConfigOption {

		public static ConfigOption ON = new ConfigOption(Boolean.TRUE).label("ON");
		public static ConfigOption OFF = new ConfigOption(Boolean.FALSE).label("OFF");

		private String label;
		private Object value;

		public String getLabel() {
			return label;
		}

		public Object getValue() {
			return value;
		}

		public ConfigOption(Object value) {
			this.value = value;
		}

		public ConfigOption label(String label) {
			this.label = label;
			return this;
		}
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

		LIST.add(new ConfigBuilder("Chat Tag Enabled", "chat.tag.enabled").optionsOnOff());

		LIST.add(new ConfigBuilder("Chat Session Timeout", "postman.chat.session.timeout").optionValues("8hr", "12hr",
				"16hr", "20hr", "24hr"));

		LIST.add(new ConfigBuilder("Chat Alert Timer", "postman.chat.idle.timeout").optionValues("5min", "10min",
				"15min", "20min", "25min", "30min"));

		LIST.add(new ConfigBuilder("Agent can initiate new chat", "postman.agent.chat.init").optionsOnOff());

		LIST.add(
				new ConfigBuilder("Agent Panel Color Scheme", "postman.agent.scheme.color").inputType(InputType.COLOR));

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

	public List<Object> options() {
		if (this.options == null) {
			this.options = new ArrayList<Object>();
		}
		return this.options;
	}

	public ConfigBuilder options(ConfigOption... options) {
		if (this.inputType == null) {
			this.inputType = InputType.OPTIONS;
		}
		this.options = this.options();
		for (ConfigOption configOption : options) {
			this.options.add(configOption);
		}
		return this;
	}

	public ConfigBuilder optionValues(Object... optionValues) {
		if (this.inputType == null) {
			this.inputType = InputType.OPTIONS;
		}
		this.options = this.options();
		for (Object optionValue : optionValues) {
			this.options.add(new ConfigOption(optionValue));
		}
		return this;
	}

	public ConfigBuilder optionsOnOff() {
		return this.options(ConfigOption.ON, ConfigOption.OFF);
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
