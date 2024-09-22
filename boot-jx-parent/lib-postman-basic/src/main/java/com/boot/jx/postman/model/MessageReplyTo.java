package com.boot.jx.postman.model;

import java.io.Serializable;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MessageReplyTo implements Serializable, JsonIgnoreNull, JsonIgnoreUnknown {

	@JsonProperty("type")
	@ApiMockModelProperty(example = "feedback/story", value = "type")
	public String msgReplyTo;


}
