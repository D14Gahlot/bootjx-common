package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.vendor.VendorContext.ApiVendorHeaders;

import twitter4j.DirectMessageList;
import twitter4j.TwitterException;

@RestController
public class InBoundControllerTW {

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private TwitterClient twitterClient;

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/callback", method = RequestMethod.POST)
	public Update onReceiveMessage(@RequestBody Update update) throws InterruptedException {
		return update;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/send", method = RequestMethod.POST)
	public Update onSend(@RequestBody Update update) throws InterruptedException {
		return update;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/get", method = RequestMethod.GET)
	public DirectMessageList onReceive() throws InterruptedException, TwitterException {
		return twitterClient.getTwitter().getDirectMessages(5);

	}

}
