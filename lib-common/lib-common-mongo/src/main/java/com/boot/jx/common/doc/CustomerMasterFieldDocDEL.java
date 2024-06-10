package com.boot.jx.common.doc;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;
import com.boot.jx.postman.doc.AdditionalProfilDto;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBPhone;

@Document(collection = "MASTER_CUSTOMER_FIELD")
@TypeAlias("CustomerMasterFieldDoc")
public class CustomerMasterFieldDoc extends TimeStampDoc {

	@Id
	private String id;
	private String fieldLabel;
	@Indexed(unique = true)
	private String fieldCode;
	private String fieldDesc;
	private String fieldType;
	private String isactive;
	private Date createdDate;
	private Date modifiedDate;
	private boolean isRequired;
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

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}
	
	@PostConstruct
	public void init() {
		if(additionalInfo==null) {
			this.additionalInfo = new HashMap<>();
			additionalInfo.put("title","");
			additionalInfo.put("DOB","");
			additionalInfo.put("gender","");
			
			 Set<PBPhone> alt_phones=new HashSet<>();
			 additionalInfo.put("alt_phones", alt_phones);
			
			 Set<PBEmail> alt_emails=new HashSet<>();
			 additionalInfo.put("alt_emails", alt_emails);

			
		}
		
	}

	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public void setRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

}
