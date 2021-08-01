package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.mongo.CommonDocInterfaces.OldDocVersion;
import com.boot.jx.postman.ClientApiKey;

@Document(collection = "CONFIG_CLIENT_KEY")
@TypeAlias("ClientApiKeyDoc")
public class ClientApiKeyDoc implements OldDocVersion<ClientApiKeyDoc>, IDocument, AuditableEntity, ClientApiKey {

    private static final long serialVersionUID = -6368905475787041196L;

    @Id
    private String id;

    @Indexed(unique = true)
    private String keyName;

    private String createdBy;
    private Long createdStamp;
    private String key;
    private String keyVersion;

    @Field("oldVersions")
    private List<ClientApiKeyDoc> oldVersions;

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

    public List<ClientApiKeyDoc> getOldVersions() {
	return oldVersions;
    }

    public void setOldVersions(List<ClientApiKeyDoc> oldVersions) {
	this.oldVersions = oldVersions;
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
