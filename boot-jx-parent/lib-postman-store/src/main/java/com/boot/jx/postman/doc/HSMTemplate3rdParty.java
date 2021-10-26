package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;

@Document(collection = HSMTemplate3rdParty.COLLECTION_NAME)
@TypeAlias("HSMTemplate3rdParty")
public class HSMTemplate3rdParty implements Serializable, AuditableEntity {

    public static final String COLLECTION_NAME = "DICT_HSM_TEMPLATES_3RD";
    public static final String COLLECTION_NAME_TRASH = "TRASH_DICT_HSM_TEMPLATES_3RD";

    private static final long serialVersionUID = 5953299041958788771L;

    @Id
    private String id;

    private String hsmTemplateId;

    private String channelId;

    private String channelType;
    private String contactType;

    private Map<String, Object> template;

    private String createdBy;
    private Long createdStamp;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public Map<String, Object> getTemplate() {
	return template;
    }

    public void setTemplate(Map<String, Object> template) {
	this.template = template;
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

    public String getChannelId() {
	return channelId;
    }

    public void setChannelId(String channelId) {
	this.channelId = channelId;
    }

    public String getHsmTemplateId() {
	return hsmTemplateId;
    }

    public void setHsmTemplateId(String hsmTemplateId) {
	this.hsmTemplateId = hsmTemplateId;
    }

    public String getChannelType() {
	return channelType;
    }

    public void setChannelType(String channelType) {
	this.channelType = channelType;
    }

    public String getContactType() {
	return contactType;
    }

    public void setContactType(String contactType) {
	this.contactType = contactType;
    }

}
