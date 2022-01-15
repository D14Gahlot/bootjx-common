package com.boot.jx.xms.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boot.jx.postman.model.ext.InBoundAction;
import com.boot.jx.postman.model.ext.InBoundContact;
import com.boot.jx.postman.model.ext.InBoundMsg;
import com.boot.jx.postman.model.ext.InBoundWrapper;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.ApiCallbacktParams;
import com.boot.jx.xms.dto.ContactInfoUpdate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Webhooks", description = XmsConstants.INBOUND_WEBHOOKS_DESCRIPTION)
@Controller
public class InboundWebhooks {

    @ApiOperation(value = "Receiving Messages", notes = "${swagger.InboundWebhooks.onMessageCallback.description}")
    @ResponseBody
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/message/receive", method = { RequestMethod.POST })
    public InBoundMsg onMessageCallback(@RequestBody InBoundWrapper inboxMessage) {
	return new InBoundMsg();
    }

    @ApiOperation(value = "Contact Information", notes = "${swagger.InboundWebhooks.onMessageCallback.description}")
    @ResponseBody
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/contact/info", method = { RequestMethod.POST })
    public ContactInfoUpdate onProfileCallback(@RequestBody InBoundContact contactInfoRequest) {
	return new ContactInfoUpdate();
    }

    @ApiOperation(value = "Receiving Events", notes = "${swagger.InboundWebhooks.onActionCallback.description}")
    @ResponseBody
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/action/event", method = { RequestMethod.POST })
    public InBoundAction onActionCallback(@RequestBody InBoundAction actionInfo) {
	return new InBoundAction();
    }

}
