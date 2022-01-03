package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.xms.XmsConstants.ApiClientParams;
import com.boot.jx.xms.dto.OutBoundMsgBasic.OutBoundMsg;
import com.boot.jx.xms.dto.OutBoundReciept;
import com.boot.jx.xms.service.MessageService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "OutBound Messages", description = "API's to send OutBound Messages")
@Controller
public class OutboundApiV1 {

    @Autowired
    private MessageService messageService;

    @ApiOperation(value = "Send Message", notes = "This API can be used to Send Message")
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/message/send", method = { RequestMethod.POST })
    public ApiResponse<OutBoundReciept, Object> sendMessage(@RequestBody OutBoundMsg message) {
	return ApiResponse.buildResult(messageService.send(message));
    }

    @ApiOperation(value = "Send Multipart Message",
	    notes = "This API can be used to upload and send Message in Single Request", hidden = true)
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/message/send.media", method = { RequestMethod.POST })
    public ApiResponse<OutboxMessage, Object> sendMessage(OutBoundMsg message,
	    @RequestParam(name = "document", required = false) MultipartFile document,
	    @RequestParam(name = "audio", required = false) MultipartFile audio,
	    @RequestParam(name = "video", required = false) MultipartFile video,
	    @RequestParam(name = "image", required = false) MultipartFile image) {
	return ApiResponse.buildResult(new OutboxMessage());
    }

    @ApiOperation(value = "Upload Media",
	    notes = "This API can be used only to upload media,"
		    + "You will have to use Send Message} api to actial Send Message",
	    hidden = true)
    @ApiClientParams
    @ResponseBody
    @RequestMapping(value = "/api/v1/media/upload", method = { RequestMethod.POST })
    public ApiResponse<Attachment, Object> uploadMedia(@RequestParam String type, @RequestParam MultipartFile file)
	    throws Exception {
	return ApiResponse.buildResult(new Attachment());
    }
}
