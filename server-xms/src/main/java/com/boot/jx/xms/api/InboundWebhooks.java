package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.common.config.ConfigConstants.SETUP_KEY;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.ext.InBoundAction;
import com.boot.jx.postman.model.ext.InBoundContact;
import com.boot.jx.postman.model.ext.InBoundWrapper;
import com.boot.jx.rest.RestService;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.ApiCallbacktParams;
import com.boot.jx.xms.XmsVendorConfigurer;
import com.boot.jx.xms.dto.ContactInfoUpdate;
import com.boot.utils.ArgUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "Inbound Requests", description = XmsConstants.INBOUND_WEBHOOKS_DESCRIPTION)
@RestController
public class InboundWebhooks {

    @Autowired
    private RestService restService;

    @Autowired
    private PMEnvironment pmEnvironment;

    private void forwardDummy(Object req) {
	ClientApp x = XmsVendorConfigurer.getClientApp();
	if (ArgUtil.is(x) && ArgUtil.is(x.getWebhook())) {
	    restService.ajax(x.getWebhook()).post(req).asMapModel();
	}
    }

    @ApiOperation(value = "Receive Notifications", notes = "${swagger.InboundWebhooks.onMessageCallback.description}")
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/message/receive", method = { RequestMethod.POST })
    public InBoundWrapper onMessageCallback(@RequestBody InBoundWrapper inboxMessage) {
	forwardDummy(inboxMessage);
	return inboxMessage;
    }

    @ApiOperation(value = "Contact Information", notes = "${swagger.InboundWebhooks.onProfileCallback.description}")
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/contact/info", method = { RequestMethod.POST })
    public ContactInfoUpdate onProfileCallback(@RequestBody InBoundContact contactInfoRequest) {
	PMConfigurationObject conatctUrlEntry = pmEnvironment.keyEntry(SETUP_KEY.POSTMAN_CONTACT_DETAILS_URL);
	if (conatctUrlEntry.exists()) {
	    return restService.ajax(conatctUrlEntry.asString()).post(contactInfoRequest).as(ContactInfoUpdate.class);
	}
	return new ContactInfoUpdate();
    }

    @ApiOperation(value = "Receiving Events", notes = "${swagger.InboundWebhooks.onActionCallback.description}",
	    hidden = true)
    @ApiCallbacktParams
    @RequestMapping(value = "/api/v1/action/event", method = { RequestMethod.POST })
    public InBoundAction onActionCallback(@RequestBody InBoundAction actionInfo) {
	return new InBoundAction();
    }

}
