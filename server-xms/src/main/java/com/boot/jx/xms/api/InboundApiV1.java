package com.boot.jx.xms.api;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.dto.ChatUserProfileDTO.CustomerLabel;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.jx.xms.XmsConstants.ApiCallbacktParams;

@Controller
public class InboundApiV1 {

	public static class InboxObjectV1 {

		@ApiMockModelProperty(example = "WHATSAPP", value = "Contact Type")
		public ContactType contactType;

		@ApiMockModelProperty(example = "919999998888", value = "Contact Used by User while sending message"
				+ "eg your business number or email address")
		public String lane;

		@ApiMockModelProperty(example = "1234567", value = "Unique Id assigined to user by MeherY")
		public String contactId;
	}

	public static class InboxMessageV1 extends InboxObjectV1 {

		@ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Service")
		private String messageId;

		@ApiMockModelProperty(example = "1234567", value = "Unique Message Id assigined by Channel if any")
		private String messageIdExt;

		@ApiMockModelProperty(example = "919988776655", value = "Contact of user")
		public String contactFrom;

		@ApiMockModelProperty(example = "919988776655", value = "Name of user")
		private String contactFromName;

		@ApiMockModelProperty(example = "DIRECT_MESSAGE", value = "Channel used")
		private String channel;

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
	@ApiCallbacktParams
	@RequestMapping(value = "/api/v1/callback/message", method = { RequestMethod.POST })
	public InboxMessageV1 onMessageCallback(@RequestBody InboxMessageV1 inboxMessage) {
		return new InboxMessageV1();
	}

	public static class ContactInfoRequestV1 extends InboxObjectV1 {
		@ApiMockModelProperty(example = "C34567", value = "Unique Id assigned to Contact by Core Business Application", required = false)
		public String profileId;

		@ApiMockModelProperty(example = "919988776655", value = "Mobile Number collected from Channel", required = false)
		public String mobile;

		@ApiMockModelProperty(example = "abc@xyz.com", value = "Email Id collected from Channel", required = false)
		public String email;

		@ApiMockModelProperty(example = "John Doe", value = "Name collected from Channel", required = false)
		public String name;
	}

	public static class ContactInfoResponseV1 {
		@ApiMockModelProperty(example = "C34567", value = "Unique Id assigned to Contact by Core Business Application", required = false)
		public String profileId;

		@ApiMockModelProperty(example = "919988776655", value = "Mobile Number if to be changed", required = false)
		public String mobile;

		@ApiMockModelProperty(example = "abc@xyz.com", value = "EmailId if to be changed", required = false)
		public String email;

		@ApiMockModelProperty(example = "John Doe", value = "Name if to be Channel", required = false)
		public String name;

		@ApiMockModelProperty(value = "Additional Labels", required = false)
		public List<CustomerLabel> labels;
	}

	@ResponseBody
	@ApiCallbacktParams
	@RequestMapping(value = "/api/v1/callback/contact/info", method = { RequestMethod.POST })
	public ContactInfoResponseV1 onProfileCallback(@RequestBody ContactInfoRequestV1 contactInfoRequest) {
		return new ContactInfoResponseV1();
	}

	public static class ActionInfoV1 extends InboxObjectV1 {
		@ApiMockModelProperty(example = "C34567", value = "Unique Id assigned to Contact by Core Business Application", required = false)
		public String profileId;

		@ApiMockModelProperty(example = "919988776655", value = "Mobile Number collected from Channel", required = false)
		public String mobile;

		@ApiMockModelProperty(example = "abc@xyz.com", value = "Email Id collected from Channel", required = false)
		public String email;

		@ApiMockModelProperty(example = "SEND_INVOICE", value = "Action Triggered by Agent/Service")
		public String actionCode;
	}

	@ResponseBody
	@ApiCallbacktParams
	@RequestMapping(value = "/api/v1/callback/action/event", method = { RequestMethod.POST })
	public ActionInfoV1 onActionCallback(@RequestBody ActionInfoV1 actionInfo) {
		return new ActionInfoV1();
	}

}
