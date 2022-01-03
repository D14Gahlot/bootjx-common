package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.postman.model.ext.CommonMsgLocation;
import com.boot.jx.postman.model.ext.CommonMsgText;
import com.boot.jx.postman.model.ext.HSMTemplate;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutBoundMsgBasic implements Serializable, JsonIgnoreUnknown {
    private static final long serialVersionUID = 8095410793094848402L;

    @JsonProperty("channelId")
    @ApiMockModelProperty(example = "91SERVICENUMBER",
	    value = "The ID that identifies the channel over which the message should be sent.")
    public String channelId;

    @JsonProperty("to")
    @ApiMockModelProperty(value = "Either a channel-specific identifier for the receiver"
	    + " (e.g. MSISDN for SMS or WhatsApp channels), or email.")
    public OutBoundContact toContact;

    @ApiMockModelProperty(example = "text", value = "message-type",
	    allowableValues = "audio,document,image,location,system,text,video,voice,contacts,template,optin,optout")
    public String type;

    @ApiMockModelProperty(example = "false", value = "Mask the outgoing message data")
    public boolean mask;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OutBoundMsg extends OutBoundMsgBasic {
	private static final long serialVersionUID = 5358190519262995012L;

	@ApiMockModelProperty(required = false)
	public CommonMsgText text;

	@ApiMockModelProperty(required = false)
	public OutBoundMsgMedia audio;
	@ApiMockModelProperty(required = false)
	public OutBoundMsgMedia document;
	@ApiMockModelProperty(required = false)
	public OutBoundMsgMedia image;
	// public Media sticker;
	@ApiMockModelProperty(required = false)
	public OutBoundMsgMedia video;

	@ApiMockModelProperty(hidden = true)
	public List<CommonMsgContactCard> contacts;

	@ApiMockModelProperty(hidden = true)
	public CommonMsgLocation location;

	@ApiMockModelProperty(required = false)
	public HSMTemplate template;

	public CommonMsgText getText() {
	    return text;
	}

	public void setText(CommonMsgText text) {
	    this.text = text;
	}

	public OutBoundMsgMedia getAudio() {
	    return audio;
	}

	public void setAudio(OutBoundMsgMedia audio) {
	    this.audio = audio;
	}

	public OutBoundMsgMedia getDocument() {
	    return document;
	}

	public void setDocument(OutBoundMsgMedia document) {
	    this.document = document;
	}

	public OutBoundMsgMedia getImage() {
	    return image;
	}

	public void setImage(OutBoundMsgMedia image) {
	    this.image = image;
	}

	public OutBoundMsgMedia getVideo() {
	    return video;
	}

	public void setVideo(OutBoundMsgMedia video) {
	    this.video = video;
	}

	public List<CommonMsgContactCard> getContacts() {
	    return contacts;
	}

	public void setContacts(List<CommonMsgContactCard> contacts) {
	    this.contacts = contacts;
	}

	public CommonMsgLocation getLocation() {
	    return location;
	}

	public void setLocation(CommonMsgLocation location) {
	    this.location = location;
	}

	public HSMTemplate getTemplate() {
	    return template;
	}

	public void setTemplate(HSMTemplate template) {
	    this.template = template;
	}
    }

    public String getChannelId() {
	return channelId;
    }

    public void setChannelId(String channelId) {
	this.channelId = channelId;
    }

    public String getType() {
	return type;
    }

    public void setType(String type) {
	this.type = type;
    }

    public boolean isMask() {
	return mask;
    }

    public void setMask(boolean mask) {
	this.mask = mask;
    }

    public OutBoundContact getToContact() {
	return toContact;
    }

    public void setToContact(OutBoundContact toContact) {
	this.toContact = toContact;
    }

}
