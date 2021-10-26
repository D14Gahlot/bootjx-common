package com.boot.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.boot.json.JsonSerializerType;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.boot.utils.TimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapModel implements JsonSerializerType<Object> {

    public static interface EntryMeta {
	public String getKey();
    }

    public static class MapEntry {
	private Object value;

	public MapEntry(Object value) {
	    this.value = value;
	}

	public String asString() {
	    return ArgUtil.parseAsString(value);
	}

	/**
	 * Save as {@link #defaultValue(Object)} but type is always {@link String}
	 * 
	 * @param defaultvalue
	 * @return
	 */
	public String asString(String defaultvalue) {
	    return ArgUtil.parseAsString(value, defaultvalue);
	}

	public Long asLong() {
	    return ArgUtil.parseAsLong(value);
	}

	/**
	 * Save as {@link #defaultValue(Object)} but type is always {@link Long}
	 * 
	 * @param defaultvalue
	 * @return
	 */
	public Long asLong(Long defaultvalue) {
	    return ArgUtil.parseAsLong(value, defaultvalue);
	}

	public BigDecimal asBigDecimal() {
	    return ArgUtil.parseAsBigDecimal(value);
	}

	/**
	 * Save as {@link #defaultValue(Object)} but type is always {@link BigDecimal}
	 * 
	 * @param defaultvalue
	 * @return
	 */
	public BigDecimal asBigDecimal(BigDecimal defaultvalue) {
	    return ArgUtil.parseAsBigDecimal(value, defaultvalue);
	}

	public Boolean asBoolean() {
	    return ArgUtil.parseAsBoolean(value, false);
	}

	public Long asMillis() {
	    return TimeUtils.toMillis(ArgUtil.parseAsString(value, Constants.BLANK));
	}

	/**
	 * Save as {@link #defaultValue(Object)} but type is always {@link Boolean}
	 * 
	 * @param defaultvalue
	 * @return
	 */
	public Boolean asBoolean(boolean defaultvalue) {
	    return ArgUtil.parseAsBoolean(value, defaultvalue);
	}

	public <T extends Enum<T>> T asEnum(Class<T> clazz) {
	    return ArgUtil.parseAsEnumT(value, clazz);
	}

	public <T> T defaultValue(T defaultValue) {
	    return ArgUtil.parseAsT(value, defaultValue, false);
	}

	public <T> T as(Class<T> clazz) {
	    return JsonUtil.getMapper().convertValue(value, clazz);
	}

	public <T> T as(TypeReference<T> toValueTypeRef) {
	    return JsonUtil.getMapper().convertValue(value, toValueTypeRef);
	}

	public <T> List<T> asList(Class<T> clazz) {
	    List<Object> list = this.asList();
	    List<T> newList = new ArrayList<T>();
	    for (Object object : list) {
		newList.add(JsonUtil.parse(object, clazz));
	    }
	    return newList;
	}

	public List<Object> asList() {
	    return ArgUtil.parseAsListOfT(value, new Object(), Constants.EMPTY_LIST, false);
	}

	public List<Map<String, Object>> asListOfMap() {
	    return ArgUtil.parseAsListOfT(value, new HashMap<String, Object>(), new ArrayList<Map<String, Object>>(),
		    false);
	}

	public boolean exists() {
	    return ArgUtil.is(value);
	}

	public Object getValue() {
	    return value;
	}

	public void setValue(Object value) {
	    this.value = value;
	}

    }

    protected Map<String, Object> map;

    public MapModel() {
	this.map = new HashMap<String, Object>();
    }

    public MapModel(Map<String, Object> map) {
	this.map = map;
    }

    @SuppressWarnings("unchecked")
    public MapModel(String json) {
	this.map = JsonUtil.fromJson(json, Map.class);
    }

    public MapEntry entry(String key) {
	return new MapEntry(this.map().get(key));
    }

    public MapEntry entry(JsonPath jsonPath) {
	return new MapEntry(jsonPath.load(this.map, null));
    }

    public MapEntry keyEntry(String key) {
	return this.entry(key);
    }

    public MapEntry pathEntry(String path) {
	return this.entry(new JsonPath(path));
    }

    public MapEntry path(JsonPath jsonPath) {
	return this.entry(jsonPath);
    }

    public MapEntry first() {
	return new MapEntry(this.getFirst());
    }

    public Object get(String key) {
	return this.map().get(key);
    }

    public Object get(String key, Object defaultValue) {
	return this.map().getOrDefault(key, defaultValue);
    }

    public Object getFirst() {
	for (Entry<String, Object> iterable_element : map().entrySet()) {
	    return iterable_element.getValue();
	}
	return null;
    }

    public String getString(String key) {
	return ArgUtil.parseAsString(this.get(key));
    }

    public String getString(String key, String defaultvalue) {
	return ArgUtil.parseAsString(this.get(key), defaultvalue);
    }

    public Long getLong(String key) {
	return ArgUtil.parseAsLong(this.get(key));
    }

    public Long getLong(String key, Long defaultvalue) {
	return ArgUtil.parseAsLong(this.get(key), defaultvalue);
    }

    public Integer getInteger(String key) {
	return ArgUtil.parseAsInteger(this.get(key));
    }

    public Integer getInteger(String key, Integer defaultvalue) {
	return ArgUtil.parseAsInteger(this.get(key, defaultvalue));
    }

    public BigDecimal getBigDecimal(String key) {
	return ArgUtil.parseAsBigDecimal(this.get(key));
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultvalue) {
	return ArgUtil.parseAsBigDecimal(this.get(key), defaultvalue);
    }

    public <T> T getAs(String key, Class<T> clazz) {
	return JsonUtil.getMapper().convertValue(this.get(key), clazz);
    }

    public <T extends Enum<T>> T getAsEnum(String key, Class<T> clazz) {
	return ArgUtil.parseAsEnumT(this.get(key), clazz);
    }

    @SuppressWarnings("unchecked")
    public MapModel getMap(String key) {
	return new MapModel((Map<String, Object>) this.get(key));
    }

    @Override
    public Object toObject() {
	return this.map();
    }

    public MapModel fromMap(Map<String, Object> map) {
	this.map = map;
	return this;
    }

    public Map<String, Object> map() {
	if (this.map == null) {
	    this.map = new HashMap<String, Object>();
	}
	return this.map;
    }

    public Map<String, Object> toMap() {
	return this.map();
    }

    public String toJson() {
	return JsonUtil.toJson(this.map());
    }

    public <T> T as(Class<T> clazz) {
	return JsonUtil.getMapper().convertValue(this.map(), clazz);
    }

    public static MapModel from(Map<String, Object> map) {
	return new MapModel(map);
    }

    public static MapModel createInstance() {
	return new MapModel(new HashMap<String, Object>());
    }

    public MapModel putAll(Map<? extends String, ? extends Object> source) {
	this.map().putAll(source);
	return this;
    }

    public MapModel putAll(MapModel source) {
	this.map().putAll(source.toMap());
	return this;
    }

    public MapModel put(String key, Object value) {
	this.map().put(key, value);
	return this;
    }

    public MapModel put(JsonPath jsonPath, Object value) {
	jsonPath.save(this.map(), value);
	return this;
    }

    public MapModel remove(String key) {
	this.map().remove(key);
	return this;
    }
}