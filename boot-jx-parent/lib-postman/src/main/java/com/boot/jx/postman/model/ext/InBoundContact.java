package com.boot.jx.postman.model.ext;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InBoundContact {
    @ApiMockModelProperty(example = "WHATSAPP", value = "Contact Type")
    public ContactType contactType;

    @ApiMockModelProperty(example = "FACEBOOK_DM", value = "Channel used")
    private String channel;

    @ApiMockModelProperty(example = "919999998888",
	    value = "Contact Used by User while sending message" + "eg your business number or email address")
    public String lane;

    @ApiMockModelProperty(example = "1234567", value = "Unique Id assigined to user by Service")
    public String contactId;

    @JsonProperty("csid")
    @ApiMockModelProperty(example = "91YOURNUMBER", value = "Channel Specific ID", required = false)
    public String csid;

    @ApiMockModelProperty(example = "C34567", value = "Unique Id assigned to Contact by Core Business Application",
	    required = false)
    public String profileId;

    public InBoundContactProfile profile;

    public static InBoundContact from(Contactable contactable) {
	InBoundContact contact = new InBoundContact();
	contact.contactType = contactable.type();
	contact.channel = contactable.getChannelType();
	contact.lane = contactable.getLane();
	contact.contactId = contactable.getContactId();
	contact.csid = contactable.getCsid();
	return contact;
    }

}
