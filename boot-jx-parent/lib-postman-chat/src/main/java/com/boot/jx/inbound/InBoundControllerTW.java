package com.boot.jx.inbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.connectors.TwitterConnector;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.TwitterMessageResponse;
import com.boot.jx.vendor.VendorContext.ApiVendorHeaders;

import twitter4j.TwitterException;

@RestController
public class InBoundControllerTW {

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private TwitterClient twitterClient;
	
	@Autowired
	TwitterConnector twitterConnector;


	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/callback", method = RequestMethod.POST)
	public Update onReceiveMessage(@RequestBody Update update) throws InterruptedException {
		return update;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/send", method = RequestMethod.POST)
	//public Update onSend(@RequestBody Update update) throws InterruptedException {
		public InboxMessage onSend(@RequestBody InboxMessage ibmsg) throws InterruptedException, NumberFormatException, TwitterException {
		twitterClient.sendReply(ibmsg.getMessageId(), ibmsg.getMessage(), ibmsg.getLane());
		return ibmsg;
	}

	
	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/get", method = RequestMethod.GET)
	public TwitterMessageResponse onReceive() throws InterruptedException, TwitterException {
		TwitterMessageResponse tmr = twitterConnector.fetch();
		
		if(tmr!=null && !tmr.getInboxMsg().isEmpty()) {
			for(InboxMessage event :tmr.getInboxMsg()) {
			inBoundService.invokeMethods(event);
			twitterClient.getTwitter().destroyDirectMessage(Long.parseLong(event.getMessageIdExt()));
			}
		}
		
		 return tmr;
	}
	
	

}
