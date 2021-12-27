package com.boot.jx.def;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import com.boot.jx.model.CommonTemplate;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class CommonInterfaces {

    @JsonDeserialize(as = CommonTemplate.class, keyUsing = CommonTemplateKeyDeserializer.class)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public interface ICommonTemplate extends Serializable {

	String getCode();

	void setCode(String code);

	String getId();

	void setId(String id);

	String getLang();

	void setLang(String lang);

	public Map<String, Object> getData();

	public void setData(Map<String, Object> data);

    }

    public class CommonTemplateKeyDeserializer extends KeyDeserializer {

	@Override
	public Object deserializeKey(String key, DeserializationContext deserializationContext)
		throws IOException, JsonProcessingException {
	    return JsonUtil.getMapper().readValue(key, CommonTemplate.class);
	}
    }
}
