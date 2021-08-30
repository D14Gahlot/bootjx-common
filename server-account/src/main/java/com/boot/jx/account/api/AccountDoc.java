package com.boot.jx.account.api;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.DocVersion;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.rbac.docs.RbacUser;

@Document(collection = "ACCOUNTS")
@TypeAlias("AccountDoc")
public class AccountDoc implements IDocument, DocVersion, AuditableEntity, Serializable {

    private static final long serialVersionUID = -3354844112176554561L;

    @Id
    private String id;

    private SignupContact contact;
    private AccountMeta meta;

    private Long createdStamp;
    private String createdBy;
    private Long modifiedStamp;
    private String modifiedBy;
    private Boolean isActive;

    @DBRef
    private Set<RbacUser> users;

    private List<DocVersion> oldVersions;

    public List<DocVersion> getOldVersions() {
	return oldVersions;
    }

    public void setOldVersions(List<DocVersion> oldVersions) {
	this.oldVersions = oldVersions;
    }

    public Long getCreatedStamp() {
	return createdStamp;
    }

    public void setCreatedStamp(Long createdStamp) {
	this.createdStamp = createdStamp;
    }

    public Long getModifiedStamp() {
	return modifiedStamp;
    }

    public void setModifiedStamp(Long modifiedStamp) {
	this.modifiedStamp = modifiedStamp;
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public SignupContact getContact() {
	return contact;
    }

    public void setContact(SignupContact contact) {
	this.contact = contact;
    }

    public Boolean getIsActive() {
	return isActive;
    }

    public void setIsActive(Boolean isActive) {
	this.isActive = isActive;
    }

    public String getModifiedBy() {
	return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
	this.modifiedBy = modifiedBy;
    }

    public String getCreatedBy() {
	return createdBy;
    }

    public void setCreatedBy(String createdBy) {
	this.createdBy = createdBy;
    }

    public AccountMeta getMeta() {
	return meta;
    }

    public void setMeta(AccountMeta accountKeys) {
	this.meta = accountKeys;
    }

}
