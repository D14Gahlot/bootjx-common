package com.boot.jx.xms.dto;

import com.boot.jx.swagger.ApiMockModelProperty;

public class MsgSession {
	@ApiMockModelProperty(example = "SUPPORT", value = "Message Assignment If Any")
	private String assignedToDept;

	@ApiMockModelProperty(example = "SUPPORT", value = "Message Assignment If Any")
	private String assignedToAgent;

	@ApiMockModelProperty(example = "xsds34434", value = "SessionId")
	public String sessionId;

}
