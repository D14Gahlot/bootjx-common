package com.boot.jx.xms.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.xms.XmsConstants.ApiCallbacktParams;
import com.boot.jx.xms.dto.InBoundEvent;
import com.boot.jx.xms.dto.InBoundMsg;

import io.swagger.annotations.Api;

@Api(tags = "InBound Callbacks", description = "API's to be implemented by Clients, to recieve inbound messages")
@Controller
public class InboundApiV1 {

    @ResponseBody
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/message/receive", method = { RequestMethod.POST })
    public InBoundMsg onMessageCallback(@RequestBody InBoundMsg inboxMessage) {
	return new InBoundMsg();
    }

    @ResponseBody
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/action/event", method = { RequestMethod.POST })
    public InBoundEvent onActionCallback(@RequestBody InBoundEvent actionInfo) {
	return new InBoundEvent();
    }

}
