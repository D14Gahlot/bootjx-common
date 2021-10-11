package com.boot.jx.common.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigMeta implements Serializable {

    public static enum InputType {
	TEXT, OPTIONS, RANGE, NUMBER, COLOR, COLOR_PALLETE
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

    public static class ColorPalette {
	String primary;
	String secondary;
	String accent;

	public ColorPalette() {
	    this.primary = "#FFFFFF";
	    this.secondary = "#FDFDFD";
	    this.accent = "#1DC4E9";
	}

	public String getPrimary() {
	    return primary;
	}

	public void setPrimary(String primary) {
	    this.primary = primary;
	}

	public String getSecondary() {
	    return secondary;
	}

	public void setSecondary(String secondary) {
	    this.secondary = secondary;
	}

	public String getAccent() {
	    return accent;
	}

	public void setAccent(String accent) {
	    this.accent = accent;
	}

    }

    private static final long serialVersionUID = -8418291522478302778L;

    private String title;
    private String key;
    private String path;
    private Object defaultValue;
    private boolean optional;
    private boolean readonly;
    private boolean hidden;

    private InputType inputType;

    private List<ConfigOption> options;

    public ConfigMeta() {
    }

    public ConfigMeta(String title, String key) {
	this.key = key;
	this.title = title;
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

    public List<ConfigOption> getOptions() {
	return options;
    }

    public void setOptions(List<ConfigOption> options) {
	this.options = options;
    }

    public List<ConfigOption> options() {
	if (this.options == null) {
	    this.options = new ArrayList<ConfigOption>();
	}
	return this.options;
    }

    public ConfigMeta options(ConfigOption... options) {
	if (this.inputType == null) {
	    this.inputType = InputType.OPTIONS;
	}
	this.options = this.options();
	for (ConfigOption configOption : options) {
	    this.options.add(configOption);
	}
	return this;
    }

    public ConfigMeta optionValues(Object... optionValues) {
	if (this.inputType == null) {
	    this.inputType = InputType.OPTIONS;
	}
	this.options = this.options();
	for (Object optionValue : optionValues) {
	    this.options.add(new ConfigOption(optionValue));
	}
	return this;
    }

    public ConfigMeta optionsOnOff() {
	return this.options(ConfigOption.ON, ConfigOption.OFF);
    }

    public InputType getInputType() {
	return inputType;
    }

    public void setInputType(InputType inputType) {
	this.inputType = inputType;
    }

    public ConfigMeta inputType(InputType inputType) {
	this.inputType = inputType;
	return this;
    }

    public Object getDefaultValue() {
	return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
	this.defaultValue = defaultValue;
    }

    public ConfigMeta defaultValue(Object defaultValue) {
	this.defaultValue = defaultValue;
	return this;
    }

    public ConfigMeta title(String title) {
	this.title = title;
	return this;
    }

    public ConfigMeta key(String key) {
	this.key = key;
	return this;
    }

    public static List<ConfigMeta> createList() {
	return new ArrayList<ConfigMeta>();
    }

    public boolean isOptional() {
	return optional;
    }

    public void setOptional(boolean optional) {
	this.optional = optional;
    }

    public ConfigMeta optional() {
	this.optional = true;
	return this;
    }

    public boolean isReadonly() {
	return readonly;
    }

    public void setReadonly(boolean readonly) {
	this.readonly = readonly;
    }

    public ConfigMeta readonly() {
	this.readonly = true;
	return this;
    }

    public boolean isHidden() {
	return hidden;
    }

    public void setHidden(boolean hidden) {
	this.hidden = hidden;
    }

    public ConfigMeta hidden() {
	this.hidden = true;
	return this;
    }

    public String getPath() {
	return path;
    }

    public void setPath(String path) {
	this.path = path;
    }

    public ConfigMeta path(String path) {
	this.path = path;
	return this;
    }

}
