package com.boot.jx.inbound;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.connectors.TwitterConnector;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.tw.TwitterClient;
import com.boot.jx.postman.tw.WebhookInfo;
import com.boot.jx.scope.vendor.VendorContext.ApiVendorHeaders;
import com.boot.utils.ArgUtil;

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

	@Value("${postman.twitter.webhook.lanes}")
	private String[] webhookLanes;

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/callback/{lane}", method = { RequestMethod.POST, })
	public List<InboxMessage> onReceiveMessagePost(@PathVariable String lane, @RequestBody Map<String, Object> update)
			throws InterruptedException, TwitterException {
		List<InboxMessage> tmr = twitterConnector.process(lane, update);

		if (tmr != null && !tmr.isEmpty()) {
			for (InboxMessage event : tmr) {
				inBoundService.invokeMethods(event);
				twitterClient.getContext(lane).getTwitter()
						.destroyDirectMessage(Long.parseLong(event.getMessageIdExt()));
			}
		}
		return tmr;
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/callback/{lane}", method = { RequestMethod.GET })
	public Map<String, String> onReceiveMessageGet(@PathVariable String lane, @RequestParam String crc_token) {
		return twitterClient.verifyCRC(lane, crc_token);
	}

	@ApiVendorHeaders
	@RequestMapping(value = "/ext/inbound/tw/send", method = RequestMethod.POST)
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
				twitterClient.getContext(lane).getTwitter()
						.destroyDirectMessage(Long.parseLong(event.getMessageIdExt()));
			}
		}
		return tmr;
	}

	@RequestMapping(value = "/ext/inbound/tw/registerwebhook", method = RequestMethod.POST)
	public WebhookInfo registerwebhook(@RequestParam(required = false) String lane)
			throws InterruptedException, TwitterException {
		twitterClient.registerWebhook(lane);
		WebhookInfo x = twitterClient.getContext(lane).getWebhookManager().getWebhookInfo();
		return x;
	}

	@RequestMapping(value = "/ext/inbound/tw/registerwebhook", method = RequestMethod.GET)
	public WebhookInfo getwebhook(@RequestParam(required = false) String lane)
			throws InterruptedException, TwitterException {
		WebhookInfo x = twitterClient.getContext(lane).getWebhookManager().getWebhookInfo();
		return x;
	}

	@RequestMapping(value = "/ext/inbound/tw/crc", method = RequestMethod.GET)
	public WebhookInfo triggerCRC(@RequestParam(required = false) String lane)
			throws InterruptedException, TwitterException {
		twitterClient.getContext(lane).getWebhookManager().triggerCRC();
		WebhookInfo x = twitterClient.getContext(lane).getWebhookManager().getWebhookInfo();
		return x;
	}

	@Scheduled(fixedDelay = 5000)
	public void registerService() {
		for (String lane : pollingLanes) {
			try {
				if (ArgUtil.is(lane)) {
					pollDirectMessages(lane);
				}
			} catch (InterruptedException | TwitterException e) {
				e.printStackTrace();
			}
		}
		for (String lane : webhookLanes) {
			if (ArgUtil.is(lane)) {
				twitterClient.registerWebhookOnce(lane);
			}
		}
	}

}
