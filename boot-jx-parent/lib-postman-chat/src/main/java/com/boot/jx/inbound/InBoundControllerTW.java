package com.boot.jx.inbound;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.connectors.TwitterConnector;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.vendor.VendorContext.ApiVendorHeaders;

import twitter4j.TwitterException;

@RestController
public class InBoundControllerTW {

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private TwitterClient twitterClient;

	@Autowired
	private TwitterConnector twitterConnector;

	@Value("${postman.twitter.polling.lanes}")
	private String[] pollingLanes;

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/callback", method = RequestMethod.POST)
	public Update onReceiveMessage(@RequestBody Update update) throws InterruptedException {
		return update;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/send", method = RequestMethod.POST)
	// public Update onSend(@RequestBody Update update) throws InterruptedException
	// {
	public InboxMessage onSend(@RequestBody InboxMessage ibmsg)
			throws InterruptedException, NumberFormatException, TwitterException {
		twitterClient.sendReply(ibmsg.getMessageId(), ibmsg.getMessage(), ibmsg.getLane());
		return ibmsg;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/get", method = RequestMethod.GET)
	public List<InboxMessage> pollDirectMessages(@RequestParam(required = false) String lane)
			throws InterruptedException, TwitterException {
		List<InboxMessage> tmr = twitterConnector.fetch(lane);
		if (tmr != null && !tmr.isEmpty()) {
			for (InboxMessage event : tmr) {
				inBoundService.invokeMethods(event);
				// twitterClient.getContext(lane).getTwitter()
				// .destroyDirectMessage(Long.parseLong(event.getMessageIdExt()));
			}
		}
		return tmr;
	}

	@Scheduled(fixedDelay = 5000)
	public void registerService() {
		for (String lane : pollingLanes) {
			try {
				pollDirectMessages(lane);
			} catch (InterruptedException | TwitterException e) {
				e.printStackTrace();
			}
		}
	}

}
