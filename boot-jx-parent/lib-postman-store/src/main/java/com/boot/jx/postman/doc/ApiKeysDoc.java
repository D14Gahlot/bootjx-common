package com.boot.jx.postman.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.BasicDocument;

@Document(collection = "CONFIG_APIKEY")
@TypeAlias("ApiKeysDoc")
public class ApiKeysDoc extends BasicDocument<ApiKeysDoc> {

    private static final long serialVersionUID = -6368905475787041196L;

    @Id
    private String id;
    private String name;
    private String keyVersion;

    public String getKeyVersion() {
	return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
	this.keyVersion = keyVersion;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getApiKey() {
	return apiKey;
    }

    public void setApiKey(String apiKey) {
	this.apiKey = apiKey;
    }

    private String apiKey;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

}
