package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class CommonMsgText {

    @ApiMockModelProperty(example = "your-text-message-content", value = "Message Text",
	    notes = "Contains the text of the message, which can contain URLs and formatting.")
    public String body;

}