package com.boot.jx.account.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.jx.validation.AlphaNumValidator.ValidAlphaNum;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

@Document(collection = "DOMAIN")
@TypeAlias("DomainDoc")
public class DomainDoc implements IDocument, AuditableEntity, Serializable, Comparable<DomainDoc> {

    private static final long serialVersionUID = -3354844112176554561L;

    @Id
    private String id;

    @ValidAlphaNum
    private String domain;

    private CompanyDoc company;
    private SocialDoc social;

    private Long createdStamp;
    private String createdBy;
    private Long modifiedStamp;
    private String modifiedBy;
    private Boolean isActive;

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

    public String getDomain() {
	return domain;
    }

    public void setDomain(String domain) {
	this.domain = domain;
    }

    public CompanyDoc getCompany() {
	return company;
    }

    public void setCompany(CompanyDoc company) {
	this.company = company;
    }

    @Override
    public int compareTo(DomainDoc o) {
	return ArgUtil.parseAsString(this.domain, Constants.BLANK)
		.compareTo(ArgUtil.parseAsString(o.getDomain(), Constants.BLANK));
    }

    public SocialDoc getSocial() {
	return social;
    }

    public void setSocial(SocialDoc social) {
	this.social = social;
    }

}
