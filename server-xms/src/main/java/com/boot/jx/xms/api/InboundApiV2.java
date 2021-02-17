package com.boot.jx.xms.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.swagger.ApiMockModelProperty;

@Controller
public class InboundApiV2 {

	public static class InboxMessageV1 {

		@ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by MeherY")
		private String messageId;

		@ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Channel if any")
		private String messageIdExt;

		@ApiMockModelProperty(example = "WHATSAPP", value = "Contact Type")
		public ContactType contactType;

		@ApiMockModelProperty(example = "919988776655", value = "Contact of user")
		public String contactFrom;

		@ApiMockModelProperty(example = "919988776655", value = "Name of user")
		private String contactFromName;

		@ApiMockModelProperty(example = "1234567", value = "Unique Id assigined to user by MeherY")
		private String contactId;

		@ApiMockModelProperty(example = "DIRECT_MESSAGE", value = "Channel used")
		private String channel;

		@ApiMockModelProperty(example = "919999998888", value = "Contact Used by User while sending message"
				+ "eg your business number or email address")
		public String lane;

		@ApiMockModelProperty(example = "SUPPORT", value = "Message Assignment If Any")
		private String assignedToDept;

		@ApiMockModelProperty(example = "SUPPORT", value = "Message Assignment If Any")
		private String assignedToAgent;

		@ApiMockModelProperty(example = "xsds34434", value = "SessionId")
		public String sessionId;

		@ApiMockModelProperty(example = "Hello User!", value = "Message to be sent")
		public String message;

		@ApiMockModelProperty(required = false, value = "Attached Media")
		public Attachment attachment;

		@ApiMockModelProperty(value = "Several Tags/Categories Assigned by our ML/NLP program")
		protected TagDocument tags;

		@ApiMockModelProperty(example = "{}", value = "Original Message sent by Channel")
		private Object originalMessage;

	}

	@ResponseBody
	@RequestMapping(value = "/api/v1/message/callback", method = { RequestMethod.POST })
	public ApiResponse<InboxMessageV1, Object> onMessageCallback(@RequestBody InboxMessageV1 outboxMessageV1) {
		return ApiResponse.buildResult(new InboxMessageV1());
	}

}
