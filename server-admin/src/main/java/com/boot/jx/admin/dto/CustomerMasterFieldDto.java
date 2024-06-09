package com.boot.jx.admin.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.boot.jx.postman.doc.AdditionalProfilDto;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.model.MapModel;

public class CustomerMasterFieldDto {

	private String id;
	private String fieldLabel;
	private String fieldCode;
	private String fieldDesc;
	private String fieldType;
	private String isactive;
	private boolean isRequired;
	private Date createdDate;
	private Long createdStamp;
	private String createBy;
	private Date modifiedDate;
	private Long modifiedStamp;
	private String modifiedBy;
	public Map<String, Object> additionalInfo = new HashMap<>();
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFieldLabel() {
		return fieldLabel;
	}
	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}
	public String getFieldCode() {
		return fieldCode;
	}
	public void setFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
	}
	public String getFieldDesc() {
		return fieldDesc;
	}
	public void setFieldDesc(String fieldDesc) {
		this.fieldDesc = fieldDesc;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public String getIsactive() {
		return isactive;
	}
	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}
	public Long getCreatedStamp() {
		return createdStamp;
	}
	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}
	public String getCreateBy() {
		return createBy;
	}
	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}
	public Date getModifiedDate() {
		return modifiedDate;
	}
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
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
	public boolean getIsRequired() {
		return isRequired;
	}
	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}
	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}
	public void setAdditionalInfo(Map<String, Object> additionalInfo) {
		if(additionalInfo==null || additionalInfo.isEmpty()) {
			AdditionalProfilDto title=new AdditionalProfilDto();
			title.setObject("Title");
			title.setIsPredefined(true);
			additionalInfo.put("title",title);
			
			AdditionalProfilDto dob=new AdditionalProfilDto();
			dob.setObject("DOB");
			dob.setIsPredefined(true);
			additionalInfo.put("DOB",dob);
			
			AdditionalProfilDto gender=new AdditionalProfilDto();
			gender.setObject("Gender");
			gender.setIsPredefined(true);
			additionalInfo.put("gender",gender);
			
			
			AdditionalProfilDto phonesAltDto = new AdditionalProfilDto();
			 Set<PBPhone> alt_phones=new HashSet<>();
			 phonesAltDto.setObject(alt_phones);
			 phonesAltDto.setIsPredefined(true);
			 additionalInfo.put("alt_phones", phonesAltDto);
			
			 AdditionalProfilDto emailsAltDto = new AdditionalProfilDto();
			 Set<PBEmail> alt_emails=new HashSet<>();
			 emailsAltDto.setObject(alt_emails);
			 emailsAltDto.setIsPredefined(true);
			 additionalInfo.put("alt_emails", emailsAltDto);
			
		}
		this.additionalInfo = additionalInfo;
	}
}
