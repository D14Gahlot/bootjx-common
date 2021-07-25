package com.boot.jx.xms.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.swagger.ApiMockModelProperty;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.OutBoundMsg;

@Controller
public class OutboundApiV1 {

	public static class OptInV1 {
		@ApiMockModelProperty(example = "WHATSAPP", value = "Contact Type")
		public ContactType contactType;

		@ApiMockModelProperty(example = "919988776655", value = "Contact of user")
		public String contactTo;

		@ApiMockModelProperty(example = "919999998888", value = "Contact to be used to send message "
				+ "eg your business number or email address")
		public String lane;
	}

	@ApiClientParams
	@ResponseBody
	@RequestMapping(value = "/api/v1/message/send", method = { RequestMethod.POST })
	public ApiResponse<OutboxMessage, Object> sendMessage(@RequestBody OutBoundMsg outboxMessage) {
		return ApiResponse.buildResult(new OutboxMessage());
	}

	@ApiClientParams
	@ResponseBody
	@RequestMapping(value = "/api/v1/opt/in", method = { RequestMethod.POST })
	public ApiResponse<OptInV1, Object> optIn(@RequestBody OptInV1 optIn) {
		return ApiResponse.buildResult(new OptInV1());
	}

	@ApiClientParams
	@ResponseBody
	@RequestMapping(value = "/api/v1/media/upload", method = { RequestMethod.POST })
	public ApiResponse<Attachment, Object> uploadMedia(@RequestParam String type, @RequestParam MultipartFile file)
			throws Exception {
		return ApiResponse.buildResult(new Attachment());
	}

}
