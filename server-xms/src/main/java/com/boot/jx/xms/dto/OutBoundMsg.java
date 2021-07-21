package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.jx.xms.dto.CommonMsgContact.OutBoundMsgContactAddress;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutBoundMsg implements Serializable {
    private static final long serialVersionUID = 8095410793094848402L;

    @JsonProperty("channelId")
    @ApiMockModelProperty(example = "91SERVICENUMBER",
	    value = "The ID that identifies the channel over which the message should be sent.")
    public String channelId;

    @JsonProperty("to")
    @ApiMockModelProperty(example = "+31612345678", value = "Either a channel-specific identifier for the receiver"
	    + " (e.g. MSISDN for SMS or WhatsApp channels), or the ContactId.")
    public String contactTo;

    @ApiMockModelProperty(example = "text", value = "message-type",
	    allowableValues = "audio,document,image,location,system,text,video,voice,contacts,template")
    public String type;

    public CommonMsgText text;

    public OutBoundMsgMedia audio;
    public OutBoundMsgMedia document;
    public OutBoundMsgMedia image;
    // public Media sticker;
    public OutBoundMsgMedia video;

    @ApiMockModelProperty(hidden = true)
    public List<CommonMsgContact> contacts;

    @ApiMockModelProperty(hidden = true)
    public CommonMsgLocation location;

}
