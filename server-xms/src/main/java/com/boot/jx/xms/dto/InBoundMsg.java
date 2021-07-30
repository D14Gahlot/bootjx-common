package com.boot.jx.xms.dto;

import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InBoundMsg {

    @JsonProperty("from")
    @ApiMockModelProperty(example = "919988776655", value = "Contact of user")
    public String contactFrom;

    @ApiMockModelProperty(example = "WA919988776655", value = "Unique Contact Id of user")
    public String contactId;

    @JsonProperty("id")
    @ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Service")
    public String messageId;

    @ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Channel if any")
    public String messageIdExt;

    @ApiMockModelProperty(example = "156753339076", value = "message-timestamp")
    public Long timestamp;

    @ApiMockModelProperty(example = "text", value = "message-type",
	    allowableValues = "audio,document,image,location,system,text,video,voice")
    public String type;

    public CommonMsgText text;
    @ApiMockModelProperty(hidden = true)
    public CommonMsgText system;
    public InBoundMsgMedia video;
    public InBoundMsgMedia voice;
    public InBoundMsgMedia audio;
    public InBoundMsgMedia document;
    public InBoundMsgMedia image;
    public CommonMsgLocation location;

    @ApiMockModelProperty(value = "Several Tags/Categories Assigned by our ML/NLP program")
    protected TagDocument tags;

    public MsgSession session;

    @ApiMockModelProperty(example = "{}",
	    value = "Original Message sent by Channel :  only if modified/error by service")
    public Object originalMessage;

}