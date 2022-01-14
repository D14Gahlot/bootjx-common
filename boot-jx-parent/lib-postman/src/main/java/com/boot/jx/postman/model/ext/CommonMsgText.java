package com.boot.jx.postman.model.ext;

import com.boot.jx.swagger.ApiMockModelProperty;

public class CommonMsgText extends CommonMsg {

    @ApiMockModelProperty(example = "your-text-message-content", value = "Message Text",
	    notes = "Contains the text of the message, which can contain URLs and formatting.")
    public String body;

}