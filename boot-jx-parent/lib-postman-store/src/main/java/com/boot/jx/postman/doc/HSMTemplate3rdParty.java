package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.model.AuditableEntity;
import com.boot.jx.postman.wa360.WA360Template;

@Document(collection = HSMTemplate3rdParty.COLLECTION_NAME)
@TypeAlias("HSMTemplate3rdParty")
public class HSMTemplate3rdParty implements Serializable, AuditableEntity {

    public static final String COLLECTION_NAME = "DICT_HSM_TEMPLATES_WA360";
    public static final String COLLECTION_NAME_TRASH = "TRASH_DICT_HSM_TEMPLATES_WA360";

    private static final long serialVersionUID = 5953299041958788771L;

    @Id
    private String id;

    private String hsmTemplateId;

    private String channelId;

    private WA360Template wa360Template;

    private String createdBy;
    private Long createdStamp;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public WA360Template getWa360Template() {
	return wa360Template;
    }

    public void setWa360Template(WA360Template wa360Template) {
	this.wa360Template = wa360Template;
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

}
