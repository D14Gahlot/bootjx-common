package com.boot.jx.postman.doc.config;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.postman.ClientApiKey;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;

@Document(collection = "CONFIG_CLIENT_KEY")
@TypeAlias("ClientKeyConfig")
public class ClientKeyConfigDoc implements IDocument, AuditableEntity, ClientApiKey, JsonIgnoreUnknown {

    private static final long serialVersionUID = -3070718912315245729L;

    @Id
    private String id;

    @Indexed(unique = true)
    private String keyName;

    private String createdBy;
    private Long createdStamp;
    private String key;
    private String keyVersion;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getCreatedBy() {
	return createdBy;
    }

    public void setCreatedBy(String createdBy) {
	this.createdBy = createdBy;
    }

    public Long getCreatedStamp() {
	return createdStamp;
    }

    public void setCreatedStamp(Long createdStamp) {
	this.createdStamp = createdStamp;
    }

    @Override
    public String getKeyName() {
	return keyName;
    }

    public void setKeyName(String keyName) {
	this.keyName = keyName;
    }

    @Override
    public String getKeyVersion() {
	return keyVersion;
    }

    public void setKeyVersion(String keyVersion) {
	this.keyVersion = keyVersion;
    }

    @Override
    public String getKey() {
	return key;
    }

    public void setKey(String apiKey) {
	this.key = apiKey;
    }
}
