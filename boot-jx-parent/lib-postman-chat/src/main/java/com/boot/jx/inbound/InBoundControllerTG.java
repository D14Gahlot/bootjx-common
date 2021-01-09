package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.vendor.VendorContext.ApiVendorHeaders;

@RestController
public class InBoundControllerTG {

	@Autowired
	private InBoundService inBoundService;
	
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tg/callback", method = RequestMethod.POST)
	public Update onReceiveMessage(@RequestBody Update update) throws InterruptedException {

		return update;
	}

}
