package com.boot.jx.postman.model.ext;

import java.util.List;

import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonProperty;

public class InBoundMsgStatus {

    @ApiMockModelProperty(example = "wa919988776655_918828218374", value = "Unique Contact Id of user")
    public String contactId;

    @JsonProperty("id")
    @ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Service")
    public String messageId;

    @ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Channel if any")
    public String messageIdExt;

    @ApiMockModelProperty(example = "156753339076", value = "message-timestamp")
    public Long timestamp;

    @ApiMockModelProperty(example = "DLVRD", value = "latest status shortcode of message",
	    allowableValues = "READ,DLVRD,SENT,FAILD,DELTD")
    public Status status;

    @ApiMockModelProperty(example = "DLVRD", value = "latest status of message",
	    allowableValues = "read, delivered, sent, failed, deleted", hidden = true, required = false)
    public String statusDescription;

    public static class InBoundMsgStatusError {

	@ApiMockModelProperty(example = "470", value = "Error code.\n")
	public String code;

	@ApiMockModelProperty(
		example = "Failed to send message because you are outside the support window for freeform messages to this user. Please use a valid HSM notification or reconsider.",
		value = "Error code")
	public String title;

	@ApiMockModelProperty(value = "Error details provided, if available/applicable", required = false)
	public String details;

	@ApiMockModelProperty(example = "https://developers.facebook.com/docs/whatsapp/api/errors#error",
		value = "Location for error detail", required = false)
	public String href;
    }

    public List<InBoundMsgStatusError> errors;

}