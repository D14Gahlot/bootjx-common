package com.boot.jx.common.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigMeta implements Serializable {

    public static enum OPTIONS_TYPE {
	TEXT, OPTIONS, RANGE, NUMBER, COLOR, COLOR_PALLETE
    }

    public static enum DATA_TYPE {
	TIMESPAN
    }

    public static enum CONVERT_TYPE {
	TIME_MILLIS
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
    private String desc;
    private String group;
    private String path;
    private Object defaultValue;
    private boolean optional;
    private boolean readonly;
    private boolean createonly;
    private boolean writeonly;
    private boolean hidden;
    private boolean deprecated;

    private OPTIONS_TYPE inputType;
    private DATA_TYPE dataType;
    private CONVERT_TYPE converterType;

    private List<ConfigOption> options;
    private String source;

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
	if (this.inputType == null) {
	    this.inputType = OPTIONS_TYPE.OPTIONS;
	}
	if (this.options == null) {
	    this.options = new ArrayList<ConfigOption>();
	}
	return this.options;
    }

    public ConfigMeta options(String src) {
	this.options = this.options();
	this.source = src;
	return this;
    }

    public ConfigMeta options(ConfigOption... options) {
	this.options = this.options();
	for (ConfigOption configOption : options) {
	    this.options.add(configOption);
	}
	return this;
    }

    public ConfigMeta optionValues(Object... optionValues) {
	this.options = this.options();
	for (Object optionValue : optionValues) {
	    this.options.add(new ConfigOption(optionValue));
	}
	return this;
    }

    public String getSource() {
	return source;
    }

    public void setSource(String src) {
	this.source = src;
    }

    public ConfigMeta source(String src) {
	this.source = src;
	return this;
    }

    public ConfigMeta optionsOnOff() {
	return this.options(ConfigOption.ON, ConfigOption.OFF);
    }

    public OPTIONS_TYPE getInputType() {
	return inputType;
    }

    public void setInputType(OPTIONS_TYPE inputType) {
	this.inputType = inputType;
    }

    public ConfigMeta inputType(OPTIONS_TYPE inputType) {
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

    public ConfigMeta defaultFalse() {
	this.defaultValue = Boolean.FALSE;
	return this;
    }

    public ConfigMeta defaultTrue() {
	this.defaultValue = Boolean.TRUE;
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

    public DATA_TYPE getDataType() {
	return dataType;
    }

    public void setDataType(DATA_TYPE dataType) {
	this.dataType = dataType;
    }

    public CONVERT_TYPE getConverterType() {
	return converterType;
    }

    public void setConverterType(CONVERT_TYPE converterType) {
	this.converterType = converterType;
    }

    public boolean isDeprecated() {
	return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
	this.deprecated = deprecated;
    }

    public ConfigMeta deprecated() {
	this.deprecated = true;
	return this;
    }

    public ConfigMeta createonly() {
	this.createonly = true;
	return this;
    }

    public boolean isCreateonly() {
	return createonly;
    }

    public void setCreateonly(boolean createonly) {
	this.createonly = createonly;
    }

    public boolean isWriteonly() {
	return writeonly;
    }

    public void setWriteonly(boolean writeonly) {
	this.writeonly = writeonly;
    }

    public ConfigMeta writeonly() {
	this.writeonly = true;
	return this;
    }

    public String getDesc() {
	return desc;
    }

    public void setDesc(String desc) {
	this.desc = desc;
    }

    public ConfigMeta desc(String desc) {
	this.desc = desc;
	return this;
    }

    public String getGroup() {
	return group;
    }

    public void setGroup(String group) {
	this.group = group;
    }

    public ConfigMeta group(String group) {
	this.group = group;
	return this;
    }
}
