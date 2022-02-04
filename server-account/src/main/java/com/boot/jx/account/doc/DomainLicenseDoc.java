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
//license
@Document(collection = "DOMAIN_LICENSE")
@TypeAlias("DomainLicenseDoc")
public class DomainLicenseDoc implements IDocument, AuditableEntity, Serializable, Comparable<DomainLicenseDoc> {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3695636889174559862L;
	@Id
    private String id;
    @ValidAlphaNum
    private String domain;
    private String licenseName;
    
    private Long licenseAggrementStamp;
    private Long licenseStartStamp;
    private Long licenseEndStamp;
    private String frequency;
    private Long createdStamp;
    private String createdBy;
    private Long modifiedStamp;
    private String modifiedBy;
    private Boolean isActive;
	

	@Override
	public int compareTo(DomainLicenseDoc o) {
		return ArgUtil.parseAsString(this.domain, Constants.BLANK)
				.compareTo(ArgUtil.parseAsString(o.getDomain(), Constants.BLANK));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getLicenseName() {
		return licenseName;
	}

	public void setLicenseName(String licenseName) {
		this.licenseName = licenseName;
	}

	public Long getLicenseAggrementStamp() {
		return licenseAggrementStamp;
	}

	public void setLicenseAggrementStamp(Long licenseAggrementStamp) {
		this.licenseAggrementStamp = licenseAggrementStamp;
	}

	public Long getLicenseStartStamp() {
		return licenseStartStamp;
	}

	public void setLicenseStartStamp(Long licenseStartStamp) {
		this.licenseStartStamp = licenseStartStamp;
	}

	public Long getLicenseEndStamp() {
		return licenseEndStamp;
	}

	public void setLicenseEndStamp(Long licenseEndStamp) {
		this.licenseEndStamp = licenseEndStamp;
	}

	public String getFrequency() {
		return frequency;
	}

	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}

	public Long getModifiedStamp() {
		return modifiedStamp;
	}

	public void setModifiedStamp(Long modifiedStamp) {
		this.modifiedStamp = modifiedStamp;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String getCreatedBy() {
		// TODO Auto-generated method stub
		return createdBy;
	}

	@Override
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
		
	}

	@Override
	public Long getCreatedStamp() {
		// TODO Auto-generated method stub
		return createdStamp;
	}

	@Override
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
		
	}

}
